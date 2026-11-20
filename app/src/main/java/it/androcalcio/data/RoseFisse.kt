package it.androcalcio.data

import it.androcalcio.engine.Attr
import it.androcalcio.engine.Market
import it.androcalcio.engine.Talenti
import kotlin.random.Random

/**
 * Rose fisse: i giocatori di partenza sono sempre gli stessi (semi fissi) e vengono
 * congelati in assets/rose_fisse.csv. Questo file non usa Android: può girare nei test.
 */
object RoseFisse {

    /** Squadra come in assets/squadre.json, nell'ordine del file. */
    data class Squadra(val nazione: String, val campionato: String, val nome: String)

    /** Giocatore con la squadra di appartenenza ("" = svincolato). */
    data class Riga(val squadra: String, val giocatore: Player)

    /** Colonne del CSV, nell'ordine usato da [toCsv] e [fromCsv]. */
    const val INTESTAZIONE = "squadra|ruolo|eta|overall|morale|salario|nazionalita|potenziale|contratto|" +
        "nome|ve|re|ag|qu|fo|gm|en|pa|dr|fi|ti|ri|piede|pos|sec2|aff2|sec3|aff3"

    private const val COLONNE = 28
    private const val SEME_SVINCOLATI = 20_260_101
    private const val SEME_TALENTI = 20_260_102

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
        "Francia" to "FRA", "Brasile" to "BRA", "Argentina" to "ARG",
        "Portogallo" to "POR", "Olanda" to "OLA", "Belgio" to "BEL", "Croazia" to "CRO",
        "Polonia" to "POL", "Serbia" to "SRB", "Stati Uniti" to "USA", "Giappone" to "GIA", "Marocco" to "MAR"
    )
    private val roles = List(2) { "P" } + List(6) { "D" } + List(6) { "C" } + List(4) { "A" }

    /** Titolari: 1 portiere, 4 difensori, 4 centrocampisti, 2 attaccanti (indici in [roles]). */
    private val STARTERS = listOf(0, 2, 3, 4, 5, 8, 9, 10, 11, 14, 15)
    /** Posti in rosa dove può comparire un wonderkid (riserve di centrocampo e attacco). */
    private val WK_SLOTS = listOf(12, 13, 16, 17)

    /** Forza (scala FC) della prima e dell'ultima squadra di ogni campionato. */
    internal fun leagueRange(nazione: String, tier: Int): Pair<Int, Int> = when (nazione) {
        "Inghilterra" -> if (tier == 0) 83 to 72 else 69 to 62
        "Spagna" -> if (tier == 0) 82 to 70 else 67 to 59
        "Italia" -> when (tier) { 0 -> 82 to 70; 1 -> 67 to 59; else -> 60 to 50 }   // Serie C (tier 2+) più debole
        "Germania" -> if (tier == 0) 82 to 69 else 67 to 59
        "Francia" -> if (tier == 0) 80 to 67 else 66 to 58
        "Brasile", "Argentina" -> if (tier == 0) 74 to 63 else 64 to 55
        else -> if (tier == 0) 72 to 62 else 62 to 54
    }

    /** Genera tutti i giocatori di partenza: rose delle squadre, talento precoce e svincolati. */
    fun genera(squadre: List<Squadra>): List<Riga> {
        // Livello della divisione: ordine di prima comparsa dentro ogni nazione (0 = massima serie)
        val tierOf = mutableMapOf<String, Int>()
        val perNation = mutableMapOf<String, Int>()
        for (s in squadre) {
            val key = s.nazione + "|" + s.campionato
            if (key !in tierOf) {
                val t = perNation.getOrDefault(s.nazione, 0)
                tierOf[key] = t
                perNation[s.nazione] = t + 1
            }
        }
        val leagueKey = squadre.map { it.nazione + "|" + it.campionato }
        val tiers = leagueKey.map { tierOf[it] ?: 0 }
        // Posizione di ogni squadra nel suo campionato (la lista è in ordine di forza) e numero di squadre
        val leagueSize = leagueKey.groupingBy { it }.eachCount()
        val seen = mutableMapOf<String, Int>()
        val rank = leagueKey.map { k -> seen.getOrDefault(k, 0).also { seen[k] = it + 1 } }

        val righe = mutableListOf<Riga>()
        squadre.forEachIndexed { idx, team ->
            val rng = Random(team.nome.hashCode() + 7)
            val southAm = team.nazione == "Brasile" || team.nazione == "Argentina"
            val (top, bottom) = leagueRange(team.nazione, tiers[idx])
            val n = leagueSize.getValue(leagueKey[idx])
            // forza della squadra (scala FC): prima del campionato = top, ultima = bottom
            val strength = top - (top - bottom) * rank[idx] / (n - 1).coerceAtLeast(1) + rng.nextInt(-1, 2)
            val nat = team.nazione.ifEmpty { "Italia" }

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
                val nome = "${initials[rng.nextInt(initials.length)]}. ${surnames[rng.nextInt(surnames.size)]}"
                val giocatore = Attr.fill(Player(
                    teamId = 0L,
                    name = nome,
                    role = role,
                    age = age,
                    overall = ov,
                    morale = rng.nextInt(50, 91),
                    salary = Market.wageAt(ov, age),
                    nationality = if (rng.nextInt(100) < 75) nat else codes.keys.random(rng),
                    potential = pot.coerceIn(ov, 99),
                    contract = rng.nextInt(1, 5)
                ), rng)
                righe += Riga(team.nome, giocatore)
            }
        }

        // Talento precoce di partenza: nella Valceresio
        squadre.indexOfFirst { it.nome == "Valceresio" }.takeIf { it >= 0 }?.let { idx ->
            righe += Riga(squadre[idx].nome, Talenti.nuovo(0L, "Gabriele Jannino", Random(SEME_TALENTI)))
        }

        // Svincolati di partenza: ruoli e livelli vari, stipendio da firmare
        val rngSvincolati = Random(SEME_SVINCOLATI)
        repeat(40) { i ->
            val ovr = rngSvincolati.nextInt(55, 80)
            val nome = "${initials[rngSvincolati.nextInt(initials.length)]}. ${surnames[rngSvincolati.nextInt(surnames.size)]}"
            val giocatore = Player(
                teamId = 0L,
                name = nome,
                role = listOf("P", "D", "C", "A")[i % 4],
                age = rngSvincolati.nextInt(21, 34),
                overall = ovr,
                morale = 60,
                salary = 0L,
                nationality = "Italia",
                potential = ovr + rngSvincolati.nextInt(0, 8),
                contract = 1
            )
            righe += Riga("", giocatore)
        }
        return righe
    }

    /** Testo CSV con intestazione: una riga per giocatore, campi separati da "|". */
    fun toCsv(righe: List<Riga>): String = buildString {
        append(INTESTAZIONE).append('\n')
        for (r in righe) {
            val p = r.giocatore
            val campi = listOf(
                r.squadra, p.role, p.age, p.overall, p.morale, p.salary, p.nationality, p.potential, p.contract,
                p.name, p.ve, p.re, p.ag, p.qu, p.fo, p.gm, p.en, p.pa, p.dr, p.fi, p.ti, p.ri,
                p.foot, p.pos, p.sec2, p.aff2, p.sec3, p.aff3
            )
            append(campi.joinToString("|")).append('\n')
        }
    }

    /** Legge il CSV prodotto da [toCsv]. La squadra "" indica uno svincolato. */
    fun fromCsv(testo: String): List<Riga> =
        testo.lines().drop(1).filter { it.isNotBlank() }.map { riga ->
            val c = riga.split("|")
            require(c.size == COLONNE) { "Riga delle rose non valida: $riga" }
            Riga(
                squadra = c[0],
                giocatore = Player(
                    teamId = 0L,
                    role = c[1], age = c[2].toInt(), overall = c[3].toInt(), morale = c[4].toInt(),
                    salary = c[5].toLong(), nationality = c[6], potential = c[7].toInt(), contract = c[8].toInt(),
                    name = c[9],
                    ve = c[10].toInt(), re = c[11].toInt(), ag = c[12].toInt(), qu = c[13].toInt(),
                    fo = c[14].toInt(), gm = c[15].toInt(), en = c[16].toInt(), pa = c[17].toInt(),
                    dr = c[18].toInt(), fi = c[19].toInt(), ti = c[20].toInt(), ri = c[21].toInt(),
                    foot = c[22], pos = c[23],
                    sec2 = c[24], aff2 = c[25].toInt(), sec3 = c[26], aff3 = c[27].toInt()
                )
            )
        }
}
