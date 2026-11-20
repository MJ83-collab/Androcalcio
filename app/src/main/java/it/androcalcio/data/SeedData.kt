package it.androcalcio.data

import it.androcalcio.engine.Market
import android.content.Context
import android.graphics.Color
import it.androcalcio.engine.Attr
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

    /** Titolari: 1 portiere, 4 difensori, 4 centrocampisti, 2 attaccanti (indici in [roles]). */
    private val STARTERS = listOf(0, 2, 3, 4, 5, 8, 9, 10, 11, 14, 15)
    /** Posti in rosa dove può comparire un wonderkid (riserve di centrocampo e attacco). */
    private val WK_SLOTS = listOf(12, 13, 16, 17)

    /** Forza (scala FC) della prima e dell'ultima squadra di ogni campionato. */
    private fun leagueRange(nazione: String, tier: Int): Pair<Int, Int> = when (nazione) {
        "Inghilterra" -> if (tier == 0) 83 to 72 else 69 to 62
        "Spagna" -> if (tier == 0) 82 to 70 else 67 to 59
        "Italia" -> when (tier) { 0 -> 82 to 70; 1 -> 67 to 59; else -> 60 to 50 }   // Serie C (tier 2+) più debole
        "Germania" -> if (tier == 0) 82 to 69 else 67 to 59
        "Francia" -> if (tier == 0) 80 to 67 else 66 to 58
        "Brasile", "Argentina" -> if (tier == 0) 74 to 63 else 64 to 55
        else -> if (tier == 0) 72 to 62 else 62 to 54
    }

    /** Svincolati di partenza (nessun club, teamId = SVINCOLATO_ID): ruoli e livelli vari, stipendio da firmare. */
    private fun freeAgents(n: Int = 40): List<Player> = List(n) { i ->
        val ovr = Random.nextInt(55, 80)
        Player(
            teamId = Market.SVINCOLATO_ID,
            name = "${initials[Random.nextInt(initials.length)]}. ${surnames[Random.nextInt(surnames.size)]}",
            role = listOf("P", "D", "C", "A")[i % 4],
            age = Random.nextInt(21, 34),
            overall = ovr,
            morale = 60,
            salary = 0L,
            nationality = "Italia",
            potential = ovr + Random.nextInt(0, 8),
            contract = 1
        )
    }

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
        val tiers = mutableListOf<Int>()
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
            val (top, bottom) = leagueRange(n, tier)
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
            tiers += tier
        }

        val ids = dao.insertTeams(teams)

        // Posizione di ogni squadra nel suo campionato (la lista è in ordine di forza) e numero di squadre
        val leagueKey = teams.map { it.nazione + "|" + it.campionato }
        val leagueSize = leagueKey.groupingBy { it }.eachCount()
        val seen = mutableMapOf<String, Int>()
        val rank = leagueKey.map { k -> seen.getOrDefault(k, 0).also { seen[k] = it + 1 } }

        val players = mutableListOf<Player>()
        teams.forEachIndexed { idx, team ->
            val rng = Random(team.name.hashCode() + 7)
            val southAm = team.nazione == "Brasile" || team.nazione == "Argentina"
            val (top, bottom) = leagueRange(team.nazione, tiers[idx])
            val n = leagueSize.getValue(leagueKey[idx])
            // forza della squadra (scala FC): prima del campionato = top, ultima = bottom
            val strength = top - (top - bottom) * rank[idx] / (n - 1).coerceAtLeast(1) + rng.nextInt(-1, 2)
            val nat = codes[team.nazione] ?: "ITA"

            // stelle 86-91: poche, solo nelle europee di vertice
            val starSlots = mutableSetOf<Int>()
            if (!southAm && tiers[idx] == 0) {
                val stars = when {                  // al massimo una stella per squadra
                    strength >= 81 -> 1
                    strength >= 79 -> if (rng.nextInt(100) < 35) 1 else 0
                    else -> 0
                }
                repeat(stars) { starSlots += STARTERS.random(rng) }
            }
            // Sudamerica: ogni tanto un titolare da esportazione (78-84)
            val exportSlot = if (southAm && tiers[idx] == 0 && rng.nextInt(100) < 30) STARTERS.random(rng) else -1
            // wonderkid alla FM: giovane con potenziale altissimo, più frequente in Sudamerica
            val wkSlot = if (rng.nextInt(100) < (if (southAm) 35 else 20)) WK_SLOTS.random(rng) else -1

            roles.forEachIndexed { slot, role ->
                var age = rng.nextInt(17, 37)
                val starter = slot in STARTERS
                var ov = strength + (if (starter) rng.nextInt(-3, 4) else rng.nextInt(-9, -1)) -
                    (if (age < 21) rng.nextInt(6, 11) else 0)
                var pot: Int
                when (slot) {
                    in starSlots -> {
                        age = rng.nextInt(23, 33)
                        ov = when (rng.nextInt(100)) {   // scala FC: 91 è il massimo
                            in 0..49 -> rng.nextInt(86, 88)
                            in 50..84 -> rng.nextInt(88, 90)
                            in 85..96 -> 90
                            else -> 91
                        }
                        pot = ov + rng.nextInt(0, 3)
                    }
                    exportSlot -> { age = rng.nextInt(21, 30); ov = rng.nextInt(78, 85); pot = ov + rng.nextInt(0, 5) }
                    wkSlot -> { age = rng.nextInt(17, 21); ov = rng.nextInt(65, 76); pot = rng.nextInt(86, 95) }
                    else -> pot = ov + when {               // potenziale alla FM (PA rispetto alla CA)
                        age < 21 -> rng.nextInt(8, 17)
                        age < 24 -> rng.nextInt(4, 11)
                        else -> rng.nextInt(0, 4)
                    }
                }
                ov = ov.coerceIn(40, 91)
                players += Attr.fill(Player(
                    teamId = ids[idx],
                    name = "${initials[rng.nextInt(initials.length)]}. ${surnames[rng.nextInt(surnames.size)]}",
                    role = role,
                    age = age,
                    overall = ov,
                    morale = rng.nextInt(50, 91),
                    salary = it.androcalcio.engine.Market.wageAt(ov, age),
                    nationality = if (rng.nextInt(100) < 75) nat else codes.values.random(rng),
                    potential = pot.coerceIn(ov, 99),
                    contract = rng.nextInt(1, 5)
                ), rng)
            }
        }
        dao.insertPlayers(players)
        dao.insertPlayers(freeAgents())

        // Budget ingaggi: 1,2 x stipendi della rosa (x1,5 per i club ricchi)
        val wages = players.groupBy { it.teamId }.mapValues { e -> e.value.sumOf { it.salary } }
        dao.updateTeams(teams.mapIndexed { i, t ->
            val rich = if (t.name in RICH_CLUBS) 1.5 else 1.0
            t.copy(id = ids[i], wageBudget = (1.2 * rich * (wages[ids[i]] ?: 0L)).toLong())
        })
    }

    private val RICH_CLUBS = setOf(
        "Como AZ2", "Parigi SG", "Manchester Citt SB", "Chelsfield BL", "Newcastel MG", "Royal Madril",
        "Baviera MO", "Lipsia RB2", "Aston Villan VL", "Flamengo MG", "Botafogo FG"
    )
}
