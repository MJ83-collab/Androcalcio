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

    /** Chiavi di stato semplici (senza id di giocatori/squadre) che fanno parte della carriera. */
    val CHIAVI_STATO = listOf("fiducia", "tifosi", "reputazione", "clima", "liv_allenamento", "liv_vivaio",
        "stipendio_allenatore", "esonero", "sponsor",
        "migliorie_fatte", "migliorie_cantieri", "psicologo", "marketing", "prezzi", "obiettivo", "obiettivo_s", "obiettivo_pos",
        "prestiti", "achievement", "avanzate", "giornale", "premi", "conf_g", "giornata_mercato",
        "c_stagioni", "c_titoli", "c_coppe", "c_prom", "c_vittorie", "c_gol", "c_derby", "c_obiettivi", "c_giovani",
        "c_colloqui", "c_conferenze", "c_prestiti", "c_partite", "apparizioni")

    suspend fun wipe(dao: AndrocalcioDao) {
        dao.clearMatches(); dao.clearYouths(); dao.clearReports()
        dao.clearStaff(); dao.clearGoals(); dao.clearStats(); dao.clearClausole(); dao.clearMissions(); dao.clearConvocazioni(); dao.clearNazionaleStats(); dao.clearRosaNazionale(); dao.clearPlayers(); dao.clearTeams(); dao.clearMovimenti(); dao.clearStato()
    }

    suspend fun export(dao: AndrocalcioDao): String {
        val players = dao.allPlayers().groupBy { it.teamId }
        val staff = dao.allStaff().groupBy { it.teamId }
        val arr = JSONArray()
        dao.allTeams().forEach { t ->
            val ga = JSONArray()
            (players[t.id] ?: emptyList()).forEach { p ->
                ga.put(JSONObject()
                    .put("id", p.id).put("name", p.name).put("role", p.role).put("age", p.age)
                    .put("overall", p.overall).put("morale", p.morale)
                    .put("salary", p.salary).put("nationality", p.nationality)
                    .put("potential", p.potential).put("contract", p.contract)
                    .put("ve", p.ve).put("re", p.re).put("ag", p.ag).put("qu", p.qu).put("fo", p.fo)
                    .put("gm", p.gm).put("en", p.en).put("pa", p.pa).put("dr", p.dr).put("fi", p.fi)
                    .put("ti", p.ti).put("ri", p.ri).put("foot", p.foot).put("pos", p.pos)
                    .put("ruoloPartita", p.ruoloPartita).put("compito", p.compito).put("sec2", p.sec2).put("aff2", p.aff2)
                    .put("sec3", p.sec3).put("aff3", p.aff3).put("sec4", p.sec4).put("aff4", p.aff4).put("numero", p.numero))
            }
            val sa = JSONArray()
            (staff[t.id] ?: emptyList()).forEach { s ->
                sa.put(JSONObject()
                    .put("id", s.id).put("role", s.role).put("specialita", s.specialita).put("name", s.name).put("skill", s.skill).put("stile", s.stile)
                    .put("salary", s.salary).put("contract", s.contract))
            }
            arr.put(JSONObject()
                .put("id", t.id).put("name", t.name).put("shortName", t.shortName)
                .put("colorPrimary", t.colorPrimary).put("colorSecondary", t.colorSecondary)
                .put("budget", t.budget).put("stadiumCapacity", t.stadiumCapacity)
                .put("ticketPrice", t.ticketPrice).put("sponsor", t.sponsor)
                .put("nazione", t.nazione).put("campionato", t.campionato)
                .put("youthCoachSkill", t.youthCoachSkill).put("scoutSkill", t.scoutSkill)
                .put("youthClaimed", t.youthClaimed).put("wageBudget", t.wageBudget).put("giocatori", ga).put("staff", sa))
        }
        val statJ = JSONArray()
        dao.allStats().forEach { st ->
            statJ.put(JSONObject().put("season", st.season).put("playerId", st.playerId).put("teamId", st.teamId)
                .put("presenze", st.presenze).put("gol", st.gol))
        }
        val clJ = JSONArray()
        dao.clausoleOnce().forEach { c ->
            clJ.put(JSONObject().put("playerId", c.playerId).put("teamId", c.teamId).put("bonusPresenze", c.bonusPresenze)
                .put("bonusGol", c.bonusGol).put("bonusCampionato", c.bonusCampionato).put("bonusCoppa", c.bonusCoppa))
        }
        val miJ = JSONArray()
        dao.missionsOnce().forEach { m ->
            miJ.put(JSONObject().put("teamId", m.teamId).put("staffId", m.staffId).put("area", m.area).put("giornate", m.giornate))
        }
        val nzJ = JSONArray()
        dao.allNazionaleStats().forEach { n ->
            nzJ.put(JSONObject().put("season", n.season).put("playerId", n.playerId).put("nazione", n.nazione)
                .put("presenze", n.presenze).put("gol", n.gol))
        }
        val cvJ = JSONArray()
        dao.allConvocazioni().forEach { c ->
            cvJ.put(JSONObject().put("season", c.season).put("competizione", c.competizione).put("round", c.round)
                .put("playerId", c.playerId).put("nazione", c.nazione).put("risultato", c.risultato))
        }
        val tiriJ = JSONArray()
        dao.allTeams().forEach { t ->
            val chiavi = listOf("rig_", "pun_", "ang_", "cap_").associateWith { dao.stato(it + t.id) }
            if (chiavi.values.any { !it.isNullOrEmpty() }) tiriJ.put(JSONObject().put("teamId", t.id)
                .put("rig", chiavi["rig_"].orEmpty()).put("pun", chiavi["pun_"].orEmpty())
                .put("ang", chiavi["ang_"].orEmpty()).put("cap", chiavi["cap_"].orEmpty()))
        }
        val rnJ = JSONArray()
        dao.rosaNazionale().forEach { r -> rnJ.put(JSONObject().put("playerId", r.playerId).put("nazione", r.nazione)) }
        val stJ = JSONObject()
        CHIAVI_STATO.forEach { k -> dao.stato(k)?.let { stJ.put(k, it) } }
        return JSONObject().put("formato", FORMAT).put("versione", 3).put("stato", stJ).put("convocazioni", cvJ).put("nazionaleStats", nzJ)
            .put("rosaNazionale", rnJ).put("nazioneAllenata", dao.stato("nazione_allenata").orEmpty()).put("tiri", tiriJ)
            .put("squadre", arr).put("statistiche", statJ).put("clausole", clJ).put("missioni", miJ).toString(1)
    }

    /** Valida tutto prima di toccare il database; in caso di errore non cambia nulla. */
    suspend fun import(dao: AndrocalcioDao, text: String): Int {
        val root = JSONObject(text)
        require(root.optString("formato") == FORMAT) { "formato sconosciuto" }
        val arr = root.getJSONArray("squadre")
        val teams = ArrayList<Team>()
        val squads = ArrayList<List<Player>>()
        val staffs = ArrayList<List<Staff>>()
        val oldTeamIds = ArrayList<Long>()
        val oldPlayerIds = ArrayList<List<Long>>()
        val oldStaffIds = ArrayList<List<Long>>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            oldTeamIds += o.optLong("id", 0L)
            teams += Team(
                name = o.getString("name"), shortName = o.getString("shortName"),
                colorPrimary = o.getLong("colorPrimary"), colorSecondary = o.getLong("colorSecondary"),
                budget = o.getLong("budget"), stadiumCapacity = o.getInt("stadiumCapacity"),
                ticketPrice = o.getInt("ticketPrice"), sponsor = o.getString("sponsor"),
                nazione = o.getString("nazione"), campionato = o.getString("campionato"),
                youthCoachSkill = o.optInt("youthCoachSkill", 55),
                scoutSkill = o.optInt("scoutSkill", 55),
                youthClaimed = o.optString("youthClaimed", ""),
                wageBudget = o.optLong("wageBudget", 0)
            )
            val ga = o.getJSONArray("giocatori")
            oldPlayerIds += (0 until ga.length()).map { ga.getJSONObject(it).optLong("id", 0L) }
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
                    compito = g.optString("compito", ""),
                    numero = g.optInt("numero", 0),
                    sec2 = g.optString("sec2", it.androcalcio.engine.TacticalRoles.affine(g.optString("pos", ""), 0).first),
                    aff2 = g.optInt("aff2", it.androcalcio.engine.TacticalRoles.affine(g.optString("pos", ""), 0).second),
                    sec3 = g.optString("sec3", it.androcalcio.engine.TacticalRoles.affine(g.optString("pos", ""), 1).first),
                    aff3 = g.optInt("aff3", it.androcalcio.engine.TacticalRoles.affine(g.optString("pos", ""), 1).second),
                    sec4 = g.optString("sec4", ""), aff4 = g.optInt("aff4", 0)
                )
            }
            // "staff" manca nei file precedenti: in quel caso la squadra parte senza staff
            val sa = o.optJSONArray("staff")
            oldStaffIds += (0 until (sa?.length() ?: 0)).map { sa!!.getJSONObject(it).optLong("id", 0L) }
            staffs += if (sa == null) emptyList() else (0 until sa.length()).map { k ->
                val s = sa.getJSONObject(k)
                Staff(
                    teamId = 0,
                    role = s.getString("role").takeIf { it in Staffing.ROLES } ?: Staffing.ALLENATORE,
                    name = s.getString("name"),
                    skill = s.getInt("skill").coerceIn(1, 99),
                    salary = s.getLong("salary"),
                    contract = s.getInt("contract").coerceIn(1, 5),
                    specialita = s.optString("specialita", ""), stile = s.optString("stile", "DIFESA").takeIf { st -> Stile.values().any { it.name == st } } ?: "DIFESA"
                )
            }
        }
        require(teams.isNotEmpty()) { "nessuna squadra" }
        wipe(dao)
        val ids = dao.insertTeams(teams)
        val tMap = oldTeamIds.zip(ids).toMap()
        val pIds = dao.insertPlayers(squads.flatMapIndexed { i, l -> l.map { it.copy(teamId = ids[i]) } })
        val pMap = oldPlayerIds.flatten().zip(pIds).toMap()
        val sIds = dao.insertStaffs(staffs.flatMapIndexed { i, l -> l.map { it.copy(teamId = ids[i]) } })
        val sMap = oldStaffIds.flatten().zip(sIds).toMap()
        // Statistiche, clausole e missioni: i riferimenti vecchi vengono rimappati sui nuovi id
        root.optJSONArray("statistiche")?.let { a ->
            dao.upsertStats((0 until a.length()).mapNotNull { k ->
                val o = a.getJSONObject(k)
                val pid = pMap[o.getLong("playerId")] ?: return@mapNotNull null
                PlayerStat(o.getString("season"), pid, tMap[o.getLong("teamId")] ?: 0L,
                    o.getInt("presenze"), o.getInt("gol"))
            })
        }
        root.optJSONArray("clausole")?.let { a ->
            dao.upsertClausole((0 until a.length()).mapNotNull { k ->
                val o = a.getJSONObject(k)
                val pid = pMap[o.getLong("playerId")] ?: return@mapNotNull null
                Clausola(pid, tMap[o.getLong("teamId")] ?: return@mapNotNull null,
                    o.getLong("bonusPresenze"), o.getLong("bonusGol"), o.getLong("bonusCampionato"), o.getLong("bonusCoppa"))
            })
        }
        root.optJSONArray("convocazioni")?.let { a ->
            dao.insertConvocazioni((0 until a.length()).mapNotNull { k ->
                val o = a.getJSONObject(k)
                val pid = pMap[o.getLong("playerId")] ?: return@mapNotNull null
                Convocazione(season = o.getString("season"), competizione = o.getString("competizione"),
                    round = o.getInt("round"), playerId = pid, nazione = o.getString("nazione"),
                    risultato = o.getString("risultato"))
            })
        }
        root.optJSONArray("rosaNazionale")?.let { a ->
            dao.clearRosaNazionale()
            (0 until a.length()).forEach { k ->
                val o = a.getJSONObject(k)
                pMap[o.getLong("playerId")]?.let { pid -> dao.insertRosaNazionale(RosaNazionale(pid, o.getString("nazione"))) }
            }
        }
        root.optJSONArray("tiri")?.let { a ->
            fun mappa(s: String) = s.split(",").mapNotNull { it.toLongOrNull()?.let { id -> pMap[id] } }.joinToString(",")
            for (k in 0 until a.length()) {
                val o = a.getJSONObject(k)
                val tid = tMap[o.getLong("teamId")] ?: continue
                dao.setStato(Stato("rig_$tid", mappa(o.optString("rig"))))
                dao.setStato(Stato("pun_$tid", mappa(o.optString("pun"))))
                dao.setStato(Stato("ang_$tid", mappa(o.optString("ang"))))
                dao.setStato(Stato("cap_$tid", pMap[o.optString("cap").toLongOrNull() ?: -1L]?.toString().orEmpty()))
            }
        }
        root.optJSONObject("stato")?.let { o ->
            CHIAVI_STATO.forEach { k -> if (o.has(k)) dao.setStato(Stato(k, o.optString(k))) }
        }
        root.optString("nazioneAllenata", "").takeIf { it.isNotEmpty() }?.let { dao.setStato(Stato("nazione_allenata", it)) }
        root.optJSONArray("nazionaleStats")?.let { a ->
            dao.upsertNazionaleStats((0 until a.length()).mapNotNull { k ->
                val o = a.getJSONObject(k)
                val pid = pMap[o.getLong("playerId")] ?: return@mapNotNull null
                NazionaleStat(o.getString("season"), pid, o.getString("nazione"), o.getInt("presenze"), o.getInt("gol"))
            })
        }
        root.optJSONArray("missioni")?.let { a ->
            dao.saveMissions((0 until a.length()).mapNotNull { k ->
                val o = a.getJSONObject(k)
                val sid = sMap[o.getLong("staffId")] ?: return@mapNotNull null
                ScoutMission(teamId = tMap[o.getLong("teamId")] ?: return@mapNotNull null, staffId = sid,
                    area = o.getString("area"), giornate = o.getInt("giornate"))
            })
        }
        return teams.size
    }
}
