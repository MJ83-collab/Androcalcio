package it.androcalcio.data

import it.androcalcio.engine.Attr
import it.androcalcio.engine.Staffing
import it.androcalcio.engine.Stile
import kotlin.random.Random
import org.json.JSONArray
import org.json.JSONObject

/** Esportazione/importazione dei dati modificabili (squadre e rose) in JSON. */
object DataIO {
    private const val FORMAT = "androcalcio-dati"

    suspend fun wipe(dao: AndrocalcioDao) {
        dao.clearMatches(); dao.clearYouths(); dao.clearReports()
        dao.clearStaff(); dao.clearGoals(); dao.clearPlayers(); dao.clearTeams(); dao.clearMovimenti()
    }

    suspend fun export(dao: AndrocalcioDao): String {
        val players = dao.allPlayers().groupBy { it.teamId }
        val staff = dao.allStaff().groupBy { it.teamId }
        val arr = JSONArray()
        dao.allTeams().forEach { t ->
            val ga = JSONArray()
            (players[t.id] ?: emptyList()).forEach { p ->
                ga.put(JSONObject()
                    .put("name", p.name).put("role", p.role).put("age", p.age)
                    .put("overall", p.overall).put("morale", p.morale)
                    .put("salary", p.salary).put("nationality", p.nationality)
                    .put("potential", p.potential).put("contract", p.contract)
                    .put("ve", p.ve).put("re", p.re).put("ag", p.ag).put("qu", p.qu).put("fo", p.fo)
                    .put("gm", p.gm).put("en", p.en).put("pa", p.pa).put("dr", p.dr).put("fi", p.fi)
                    .put("ti", p.ti).put("ri", p.ri).put("foot", p.foot).put("pos", p.pos)
                    .put("ruoloPartita", p.ruoloPartita).put("sec2", p.sec2).put("aff2", p.aff2)
                    .put("sec3", p.sec3).put("aff3", p.aff3).put("numero", p.numero))
            }
            val sa = JSONArray()
            (staff[t.id] ?: emptyList()).forEach { s ->
                sa.put(JSONObject()
                    .put("role", s.role).put("name", s.name).put("skill", s.skill).put("stile", s.stile)
                    .put("salary", s.salary).put("contract", s.contract))
            }
            arr.put(JSONObject()
                .put("name", t.name).put("shortName", t.shortName)
                .put("colorPrimary", t.colorPrimary).put("colorSecondary", t.colorSecondary)
                .put("budget", t.budget).put("stadiumCapacity", t.stadiumCapacity)
                .put("ticketPrice", t.ticketPrice).put("sponsor", t.sponsor)
                .put("nazione", t.nazione).put("campionato", t.campionato)
                .put("youthCoachSkill", t.youthCoachSkill).put("scoutSkill", t.scoutSkill)
                .put("youthClaimed", t.youthClaimed).put("giocatori", ga).put("staff", sa))
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
        val staffs = ArrayList<List<Staff>>()
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
                val base = Player(
                    teamId = 0, name = g.getString("name"),
                    role = g.getString("role").takeIf { it in listOf("P", "D", "C", "A") } ?: "C",
                    age = g.getInt("age"), overall = g.getInt("overall").coerceIn(1, 99),
                    morale = g.getInt("morale").coerceIn(0, 100), salary = g.getLong("salary"),
                    nationality = g.getString("nationality"),
                    potential = g.optInt("potential", g.getInt("overall")).coerceIn(1, 99),
                    contract = g.optInt("contract", 3)
                )
                // file senza parametri (versioni precedenti): li ricavo dal livello
                if (!g.has("ve")) Attr.fill(base, Random(j + base.name.hashCode())) else base.copy(
                    ve = g.getInt("ve"), re = g.getInt("re"), ag = g.getInt("ag"), qu = g.getInt("qu"),
                    fo = g.optInt("fo", 80), gm = g.getInt("gm"), en = g.getInt("en"), pa = g.getInt("pa"),
                    dr = g.getInt("dr"), fi = g.getInt("fi"), ti = g.getInt("ti"), ri = g.getInt("ri"),
                    foot = g.optString("foot", "D"), pos = g.optString("pos", ""),
                    ruoloPartita = g.optString("ruoloPartita", ""),
                    numero = g.optInt("numero", 0),
                    sec2 = g.optString("sec2", it.androcalcio.engine.TacticalRoles.affine(g.optString("pos", ""), 0).first),
                    aff2 = g.optInt("aff2", it.androcalcio.engine.TacticalRoles.affine(g.optString("pos", ""), 0).second),
                    sec3 = g.optString("sec3", it.androcalcio.engine.TacticalRoles.affine(g.optString("pos", ""), 1).first),
                    aff3 = g.optInt("aff3", it.androcalcio.engine.TacticalRoles.affine(g.optString("pos", ""), 1).second)
                )
            }
            // "staff" manca nei file precedenti: in quel caso la squadra parte senza staff
            val sa = o.optJSONArray("staff")
            staffs += if (sa == null) emptyList() else (0 until sa.length()).map { k ->
                val s = sa.getJSONObject(k)
                Staff(
                    teamId = 0,
                    role = s.getString("role").takeIf { it in Staffing.ROLES } ?: Staffing.ALLENATORE,
                    name = s.getString("name"),
                    skill = s.getInt("skill").coerceIn(1, 99),
                    salary = s.getLong("salary"),
                    contract = s.getInt("contract").coerceIn(1, 5),
                    stile = s.optString("stile", "DIFESA").takeIf { st -> Stile.values().any { it.name == st } } ?: "DIFESA"
                )
            }
        }
        require(teams.isNotEmpty()) { "nessuna squadra" }
        wipe(dao)
        val ids = dao.insertTeams(teams)
        dao.insertPlayers(squads.flatMapIndexed { i, l -> l.map { it.copy(teamId = ids[i]) } })
        dao.insertStaffs(staffs.flatMapIndexed { i, l -> l.map { it.copy(teamId = ids[i]) } })
        return teams.size
    }
}
