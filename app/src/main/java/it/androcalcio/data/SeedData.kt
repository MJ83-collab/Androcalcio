package it.androcalcio.data

import it.androcalcio.engine.Market
import android.content.Context
import android.graphics.Color
import org.json.JSONArray
import kotlin.random.Random

/**
 * Importa le squadre da assets/squadre.json (nomi fittizi) e le rose fisse da assets/rose_fisse.csv
 * (generate da RoseFisse, controllate da RoseFisseTest). Eseguito solo con database vuoto.
 */
object SeedData {

    /** Liste di nomi per i giocatori generati in partita (stessi valori delle rose fisse). */
    val initials = RoseFisse.initials
    val surnames = RoseFisse.surnames
    val codes = RoseFisse.codes

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

        val sizes = (0 until arr.length()).map { arr.getJSONObject(it).let { o -> o.getString("nazione") + "|" + o.getString("campionato") } }
            .groupingBy { it }.eachCount()
        val posSeen = mutableMapOf<String, Int>()
        val teams = mutableListOf<Team>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val n = o.getString("nazione")
            val camp = o.getString("campionato")
            val name = o.getString("squadra")
            val tier = tierOf[n + "|" + camp] ?: 0
            val rng = Random(name.hashCode())
            // budget: circa 3 volte il valore di un titolare medio della squadra (scala FC)
            val key = n + "|" + camp
            val pos = posSeen.getOrDefault(key, 0).also { posSeen[key] = it + 1 }
            val (top, bottom) = RoseFisse.leagueRange(n, tier)
            val baseStrength = top - (top - bottom) * pos / (sizes.getValue(key) - 1).coerceAtLeast(1)
            val rich = if (name in RICH_CLUBS) 1.5 else 1.0   // club ricchi: moltiplicatore su mercato e ingaggi
            val budget = (3.0 * rich * Market.valueAt(baseStrength)).toLong()
            teams += Team(
                name = name,
                shortName = o.getString("sigla"),
                colorPrimary = Color.parseColor(o.getString("colore1")).toLong() and 0xFFFFFFFFL,
                colorSecondary = Color.parseColor(o.getString("colore2")).toLong() and 0xFFFFFFFFL,
                budget = (budget + rng.nextLong(0, budget / 10 + 1)) / 100_000L * 100_000L,
                stadiumCapacity = (50_000 - tier * 9_000).coerceAtLeast(3_000) + rng.nextInt(0, 4_000),
                ticketPrice = (45 - tier * 8).coerceAtLeast(8),
                sponsor = o.getString("sponsor"),
                nazione = n,
                campionato = camp,
                youthCoachSkill = (70 - tier * 6 + rng.nextInt(-8, 9)).coerceIn(30, 95),
                scoutSkill = (70 - tier * 6 + rng.nextInt(-8, 9)).coerceIn(30, 95)
            )
        }

        val ids = dao.insertTeams(teams)

        // Rose fisse: giocatori congelati in assets/rose_fisse.csv, collegati alle squadre per nome
        val rose = context.assets.open("rose_fisse.csv").bufferedReader().use { RoseFisse.fromCsv(it.readText()) }
        val idPerSquadra = teams.mapIndexed { i, t -> t.name to ids[i] }.toMap()
        val giocatori = rose.map { r ->
            val teamId = if (r.squadra.isEmpty()) Market.SVINCOLATO_ID
                else idPerSquadra[r.squadra] ?: error("Squadra delle rose fisse non trovata: ${r.squadra}")
            r.giocatore.copy(teamId = teamId)
        }
        dao.insertPlayers(giocatori)

        // Budget ingaggi: 1,2 x stipendi della rosa (x1,5 per i club ricchi)
        val wages = giocatori.groupBy { it.teamId }.mapValues { e -> e.value.sumOf { it.salary } }
        dao.updateTeams(teams.mapIndexed { i, t ->
            val rich = if (t.name in RICH_CLUBS) 1.5 else 1.0
            t.copy(id = ids[i], wageBudget = (1.2 * rich * (wages[ids[i]] ?: 0L)).toLong())
        })
    }

    private val RICH_CLUBS = setOf(
        "Como", "Parigi SG", "Manchester Citt SB", "Chelsfield BL", "Newcastel MG", "Royal Madril",
        "Baviera MO", "Lipsia RB2", "Aston Villan VL", "Flamengo MG", "Botafogo FG"
    )
}
