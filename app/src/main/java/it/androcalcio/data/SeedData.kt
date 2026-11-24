package it.androcalcio.data

import it.androcalcio.engine.Economia
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

        // Livello della divisione (0 = massima serie); i gironi di Serie C hanno tutti lo stesso livello
        val tierOf = RoseFisse.livelli((0 until arr.length()).map { i ->
            arr.getJSONObject(i).let { o -> RoseFisse.Squadra(o.getString("nazione"), o.getString("campionato"), o.getString("squadra")) }
        })

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
                ticketPrice = Economia.prezzoBiglietto(n, tier),
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

    private val COGNOMI_GIAPPONE = listOf("Tanaka", "Sato", "Suzuki", "Takahashi", "Watanabe", "Ito", "Yamamoto", "Nakamura", "Kobayashi",
        "Kato", "Yoshida", "Yamada", "Sasaki", "Matsumoto", "Inoue", "Kimura", "Hayashi", "Shimizu", "Mori", "Ikeda", "Hashimoto",
        "Abe", "Ogawa", "Fujita", "Okada", "Goto", "Nishimura", "Fukuda", "Ota", "Miura")
    private val INIZIALI_GIAPPONE = "HKMRSTYAN"

    /** Campionato giapponese (J1 e J2, squadre inventate): aggiunto anche ai salvataggi esistenti. */
    suspend fun insertGiappone(context: Context, dao: AndrocalcioDao) {
        val arr = JSONArray(context.assets.open("squadre_giappone.json").bufferedReader().use { it.readText() })
        val squadre = (0 until arr.length()).map { i ->
            arr.getJSONObject(i).let { o -> RoseFisse.Squadra(o.getString("nazione"), o.getString("campionato"), o.getString("squadra")) }
        }
        val tierOf = RoseFisse.livelli(squadre)
        val sizes = squadre.map { it.nazione + "|" + it.campionato }.groupingBy { it }.eachCount()
        val posSeen = mutableMapOf<String, Int>()
        val teams = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val n = o.getString("nazione"); val camp = o.getString("campionato"); val name = o.getString("squadra")
            val tier = tierOf[n + "|" + camp] ?: 0
            val rng = Random(name.hashCode())
            val key = n + "|" + camp
            val pos = posSeen.getOrDefault(key, 0).also { posSeen[key] = it + 1 }
            val (top, bottom) = RoseFisse.leagueRange(n, tier)
            val forza = top - (top - bottom) * pos / (sizes.getValue(key) - 1).coerceAtLeast(1)
            val budget = (3.0 * Market.valueAt(forza)).toLong()
            Team(
                name = name, shortName = o.getString("sigla"),
                colorPrimary = Color.parseColor(o.getString("colore1")).toLong() and 0xFFFFFFFFL,
                colorSecondary = Color.parseColor(o.getString("colore2")).toLong() and 0xFFFFFFFFL,
                budget = (budget + rng.nextLong(0, budget / 10 + 1)) / 100_000L * 100_000L,
                stadiumCapacity = (50_000 - tier * 9_000).coerceAtLeast(3_000) + rng.nextInt(0, 4_000),
                ticketPrice = Economia.prezzoBiglietto(n, tier), sponsor = o.getString("sponsor"),
                nazione = n, campionato = camp,
                youthCoachSkill = (70 - tier * 6 + rng.nextInt(-8, 9)).coerceIn(30, 95),
                scoutSkill = (70 - tier * 6 + rng.nextInt(-8, 9)).coerceIn(30, 95)
            )
        }
        val ids = dao.insertTeams(teams)
        val idPerSquadra = teams.mapIndexed { i, t -> t.name to ids[i] }.toMap()
        val giocatori = RoseFisse.genera(squadre).filter { it.squadra.isNotEmpty() }.map { r ->
            val h = (r.giocatore.name + r.squadra).hashCode()
            val nome = "${INIZIALI_GIAPPONE[Math.floorMod(h, INIZIALI_GIAPPONE.length)]}. " +
                COGNOMI_GIAPPONE[Math.floorMod(h / 7, COGNOMI_GIAPPONE.size)]
            r.giocatore.copy(teamId = idPerSquadra.getValue(r.squadra), name = nome, nationality = "Giappone")
        }
        dao.insertPlayers(giocatori)
        val wages = giocatori.groupBy { it.teamId }.mapValues { e -> e.value.sumOf { it.salary } }
        dao.updateTeams(teams.mapIndexed { i, t -> t.copy(id = ids[i], wageBudget = (1.2 * (wages[ids[i]] ?: 0L)).toLong()) })
    }

    /** Cognomi comuni per i giocatori dei campionati nuovi (v0.7); le iniziali sono casuali: nessun giocatore reale. */
    private val COGNOMI_NUOVI = mapOf(
        "Olanda" to listOf("De Boer", "Visser", "Smit", "Meijer", "De Jong", "Mulder", "Bakker", "Jansen", "Dekker", "Vos", "Kuiper",
            "Hoek", "Brouwer", "Peters", "Kok", "Veenstra", "Willems", "Hendriks", "Maas", "Postma", "Bos", "Koster", "Prins", "Schouten"),
        "Portogallo" to listOf("Ferreira", "Pereira", "Oliveira", "Santos", "Rodrigues", "Martins", "Fernandes", "Goncalves", "Almeida",
            "Ribeiro", "Pinto", "Carvalho", "Teixeira", "Moreira", "Correia", "Mendes", "Nunes", "Soares", "Vieira", "Cardoso", "Monteiro"),
        "Belgio" to listOf("Peeters", "Janssens", "Maes", "Jacobs", "Mertens", "Willems", "Claes", "Goossens", "Wouters", "De Smet",
            "Vermeulen", "Dubois", "Lambert", "Dupont", "Leclercq", "Renard", "Lemaire", "Simon", "Hermans", "Aerts", "Bogaert"),
        "Scozia" to listOf("Campbell", "Stewart", "Murray", "Reid", "Ross", "Graham", "Grant", "Fraser", "Duncan", "Hamilton", "Kerr",
            "Boyd", "Gibson", "Wallace", "Craig", "Sinclair", "Munro", "Paterson", "Lennox", "Douglas", "Cameron", "Mackay"),
        "Turchia" to listOf("Yilmaz", "Kaya", "Demir", "Sahin", "Celik", "Yildiz", "Aydin", "Ozdemir", "Arslan", "Dogan", "Kilic", "Aslan",
            "Cetin", "Kara", "Koc", "Kurt", "Ozturk", "Polat", "Tas", "Erdogan", "Guler", "Aksoy", "Bulut"),
        "Grecia" to listOf("Papadopoulos", "Georgiou", "Nikolaou", "Dimitriou", "Karagiannis", "Vasiliou", "Pappas", "Antoniou", "Ioannou",
            "Alexiou", "Konstantinou", "Makris", "Oikonomou", "Papas", "Stavrou", "Theodorou", "Kotsis", "Lazaridis", "Mavros", "Zervas")
    )
    private const val INIZIALI_NUOVE = "ABCDEGIKLMNPRSTV"

    /** Fattore sulla capienza degli stadi per i campionati nuovi: paesi più piccoli, stadi più piccoli. */
    private val FATTORE_STADIO = mapOf("Olanda" to 0.80, "Portogallo" to 0.75, "Belgio" to 0.65,
        "Scozia" to 0.60, "Turchia" to 0.85, "Grecia" to 0.65)

    /**
     * Campionati europei nuovi (v0.7): Olanda, Portogallo, Belgio, Scozia, Turchia e Grecia, solo massima serie,
     * squadre inventate e giocatori generati. Solo per le carriere nuove: i salvataggi esistenti non li ricevono.
     */
    suspend fun insertEuropa(context: Context, dao: AndrocalcioDao) {
        val arr = JSONArray(context.assets.open("squadre_europa.json").bufferedReader().use { it.readText() })
        val squadre = (0 until arr.length()).map { i ->
            arr.getJSONObject(i).let { o -> RoseFisse.Squadra(o.getString("nazione"), o.getString("campionato"), o.getString("squadra")) }
        }
        val tierOf = RoseFisse.livelli(squadre)
        val sizes = squadre.map { it.nazione + "|" + it.campionato }.groupingBy { it }.eachCount()
        val posSeen = mutableMapOf<String, Int>()
        val teams = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val n = o.getString("nazione"); val camp = o.getString("campionato"); val name = o.getString("squadra")
            val tier = tierOf[n + "|" + camp] ?: 0
            val rng = Random(name.hashCode())
            val key = n + "|" + camp
            val pos = posSeen.getOrDefault(key, 0).also { posSeen[key] = it + 1 }
            val (top, bottom) = RoseFisse.leagueRange(n, tier)
            val forza = top - (top - bottom) * pos / (sizes.getValue(key) - 1).coerceAtLeast(1)
            val budget = (3.0 * Market.valueAt(forza)).toLong()
            val capienza = ((50_000 - tier * 9_000).coerceAtLeast(3_000) * (FATTORE_STADIO[n] ?: 1.0)).toInt() + rng.nextInt(0, 3_000)
            Team(
                name = name, shortName = o.getString("sigla"),
                colorPrimary = Color.parseColor(o.getString("colore1")).toLong() and 0xFFFFFFFFL,
                colorSecondary = Color.parseColor(o.getString("colore2")).toLong() and 0xFFFFFFFFL,
                budget = (budget + rng.nextLong(0, budget / 10 + 1)) / 100_000L * 100_000L,
                stadiumCapacity = capienza, ticketPrice = Economia.prezzoBiglietto(n, tier), sponsor = o.getString("sponsor"),
                nazione = n, campionato = camp,
                youthCoachSkill = (70 - tier * 6 + rng.nextInt(-8, 9)).coerceIn(30, 95),
                scoutSkill = (70 - tier * 6 + rng.nextInt(-8, 9)).coerceIn(30, 95)
            )
        }
        val ids = dao.insertTeams(teams)
        val idPerSquadra = teams.mapIndexed { i, t -> t.name to ids[i] }.toMap()
        val giocatori = RoseFisse.genera(squadre).filter { it.squadra.isNotEmpty() }.map { r ->
            val g = r.giocatore
            // La nazionalità è già scelta dal generatore (circa 3 su 4 del paese del club): il nome segue la nazionalità
            val cognomi = COGNOMI_NUOVI[g.nationality]
            val nome = if (cognomi == null) g.name else {
                val h = (g.name + r.squadra).hashCode()
                "${INIZIALI_NUOVE[Math.floorMod(h, INIZIALI_NUOVE.length)]}. ${cognomi[Math.floorMod(h / 7, cognomi.size)]}"
            }
            g.copy(teamId = idPerSquadra.getValue(r.squadra), name = nome)
        }
        dao.insertPlayers(giocatori)
        val wages = giocatori.groupBy { it.teamId }.mapValues { e -> e.value.sumOf { it.salary } }
        dao.updateTeams(teams.mapIndexed { i, t -> t.copy(id = ids[i], wageBudget = (1.2 * (wages[ids[i]] ?: 0L)).toLong()) })
    }

    private val RICH_CLUBS = setOf(
        "Como", "Parigi SG", "Manchester Citt SB", "Chelsfield BL", "Newcastel MG", "Royal Madril",
        "Baviera MO", "Lipsia RB2", "Aston Villan VL", "Flamengo MG", "Botafogo FG"
    )
}
