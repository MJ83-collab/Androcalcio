package it.androcalcio.data

import android.content.Context
import android.graphics.Color
import org.json.JSONArray
import kotlin.random.Random

/**
 * Importa le squadre da assets/squadre.json (nomi fittizi) e genera una rosa
 * di giocatori inventati per ognuna. Eseguito solo con database vuoto.
 */
object SeedData {

    val initials = "ABCDEFGILMNPRSTV"
    val surnames = listOf(
        "Rossi", "Bianchi", "Verdi", "Neri", "Costa", "Ferri", "Greco", "Russo", "Conti", "Moretti",
        "Galli", "Fontana", "Serra", "Pace", "Leone", "Sala", "Longo", "Rinaldi", "Barone", "Mariani",
        "Silva", "Souza", "Lima", "Pires", "Duarte", "Navarro", "Ortega", "Molina", "Vega", "Castro",
        "Keller", "Brandt", "Vogel", "Hahn", "Lang", "Dubois", "Moreau", "Laurent", "Roche", "Perrin",
        "Hughes", "Walker", "Bennett", "Cooper", "Wells", "Gomez", "Rojas", "Acosta", "Diaz", "Sosa"
    )
    val codes = mapOf(
        "Italia" to "ITA", "Spagna" to "ESP", "Inghilterra" to "ENG", "Germania" to "GER",
        "Francia" to "FRA", "Brasile" to "BRA", "Argentina" to "ARG"
    )
    private val roles = List(2) { "P" } + List(6) { "D" } + List(6) { "C" } + List(4) { "A" }

    suspend fun insertInto(context: Context, dao: AndrocalcioDao) {
        val text = context.assets.open("squadre.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(text)

        // Livello della divisione: ordine di prima comparsa dentro ogni nazione (0 = massima serie)
        val tierOf = mutableMapOf<String, Int>()
        val perNation = mutableMapOf<String, Int>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val n = o.getString("nazione")
            val key = n + "|" + o.getString("campionato")
            if (key !in tierOf) {
                val t = perNation.getOrDefault(n, 0)
                tierOf[key] = t
                perNation[n] = t + 1
            }
        }

        val teams = mutableListOf<Team>()
        val tiers = mutableListOf<Int>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val n = o.getString("nazione")
            val camp = o.getString("campionato")
            val name = o.getString("squadra")
            val tier = tierOf[n + "|" + camp] ?: 0
            val rng = Random(name.hashCode())
            teams += Team(
                name = name,
                shortName = o.getString("sigla"),
                colorPrimary = Color.parseColor(o.getString("colore1")).toLong() and 0xFFFFFFFFL,
                colorSecondary = Color.parseColor(o.getString("colore2")).toLong() and 0xFFFFFFFFL,
                budget = (40_000_000L - tier * 9_000_000L).coerceAtLeast(1_000_000L) +
                    rng.nextLong(0, 5_000_000L),
                stadiumCapacity = (50_000 - tier * 9_000).coerceAtLeast(3_000) + rng.nextInt(0, 4_000),
                ticketPrice = (45 - tier * 8).coerceAtLeast(8),
                sponsor = o.getString("sponsor"),
                nazione = n,
                campionato = camp,
                youthCoachSkill = (70 - tier * 6 + rng.nextInt(-8, 9)).coerceIn(30, 95),
                scoutSkill = (70 - tier * 6 + rng.nextInt(-8, 9)).coerceIn(30, 95)
            )
            tiers += tier
        }

        val ids = dao.insertTeams(teams)

        val players = mutableListOf<Player>()
        teams.forEachIndexed { idx, team ->
            val rng = Random(team.name.hashCode() + 7)
            val base = 76 - tiers[idx] * 5 + rng.nextInt(-4, 5)
            val nat = codes[team.nazione] ?: "ITA"
            roles.forEach { role ->
                val age = rng.nextInt(17, 37)
                val ov = (base + rng.nextInt(-9, 9) - if (age < 21) 6 else 0).coerceIn(35, 95)
                players += Player(
                    teamId = ids[idx],
                    name = "${initials[rng.nextInt(initials.length)]}. ${surnames[rng.nextInt(surnames.size)]}",
                    role = role,
                    age = age,
                    overall = ov,
                    morale = rng.nextInt(50, 91),
                    salary = (ov - 30).coerceAtLeast(5) * 9_000L,
                    nationality = if (rng.nextInt(100) < 75) nat else codes.values.random(rng)
                )
            }
        }
        dao.insertPlayers(players)
    }
}
