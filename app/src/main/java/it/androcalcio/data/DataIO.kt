package it.androcalcio.data

import org.json.JSONArray
import org.json.JSONObject

/** Esportazione/importazione dei dati modificabili (squadre e rose) in JSON. */
object DataIO {
    private const val FORMAT = "androcalcio-dati"

    suspend fun wipe(dao: AndrocalcioDao) {
        dao.clearMatches(); dao.clearYouths(); dao.clearReports()
        dao.clearPlayers(); dao.clearTeams()
    }

    suspend fun export(dao: AndrocalcioDao): String {
        val players = dao.allPlayers().groupBy { it.teamId }
        val arr = JSONArray()
        dao.allTeams().forEach { t ->
            val ga = JSONArray()
            (players[t.id] ?: emptyList()).forEach { p ->
                ga.put(JSONObject()
                    .put("name", p.name).put("role", p.role).put("age", p.age)
                    .put("overall", p.overall).put("morale", p.morale)
                    .put("salary", p.salary).put("nationality", p.nationality)
                    .put("potential", p.potential).put("contract", p.contract))
            }
            arr.put(JSONObject()
                .put("name", t.name).put("shortName", t.shortName)
                .put("colorPrimary", t.colorPrimary).put("colorSecondary", t.colorSecondary)
                .put("budget", t.budget).put("stadiumCapacity", t.stadiumCapacity)
                .put("ticketPrice", t.ticketPrice).put("sponsor", t.sponsor)
                .put("nazione", t.nazione).put("campionato", t.campionato)
                .put("youthCoachSkill", t.youthCoachSkill).put("scoutSkill", t.scoutSkill)
                .put("youthClaimed", t.youthClaimed).put("giocatori", ga))
        }
        return JSONObject().put("formato", FORMAT).put("versione", 1)
            .put("squadre", arr).toString(1)
    }

    /** Valida tutto prima di toccare il database; in caso di errore non cambia nulla. */
    suspend fun import(dao: AndrocalcioDao, text: String): Int {
        val root = JSONObject(text)
        require(root.optString("formato") == FORMAT) { "formato sconosciuto" }
        val arr = root.getJSONArray("squadre")
        val teams = ArrayList<Team>()
        val squads = ArrayList<List<Player>>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            teams += Team(
                name = o.getString("name"), shortName = o.getString("shortName"),
                colorPrimary = o.getLong("colorPrimary"), colorSecondary = o.getLong("colorSecondary"),
                budget = o.getLong("budget"), stadiumCapacity = o.getInt("stadiumCapacity"),
                ticketPrice = o.getInt("ticketPrice"), sponsor = o.getString("sponsor"),
                nazione = o.getString("nazione"), campionato = o.getString("campionato"),
                youthCoachSkill = o.optInt("youthCoachSkill", 55),
                scoutSkill = o.optInt("scoutSkill", 55),
                youthClaimed = o.optString("youthClaimed", "")
            )
            val ga = o.getJSONArray("giocatori")
            squads += (0 until ga.length()).map { j ->
                val g = ga.getJSONObject(j)
                Player(
                    teamId = 0, name = g.getString("name"),
                    role = g.getString("role").takeIf { it in listOf("P", "D", "C", "A") } ?: "C",
                    age = g.getInt("age"), overall = g.getInt("overall").coerceIn(1, 99),
                    morale = g.getInt("morale").coerceIn(0, 100), salary = g.getLong("salary"),
                    nationality = g.getString("nationality"),
                    potential = g.optInt("potential", g.getInt("overall")).coerceIn(1, 99),
                    contract = g.optInt("contract", 3)
                )
            }
        }
        require(teams.isNotEmpty()) { "nessuna squadra" }
        wipe(dao)
        val ids = dao.insertTeams(teams)
        dao.insertPlayers(squads.flatMapIndexed { i, l -> l.map { it.copy(teamId = ids[i]) } })
        return teams.size
    }
}
