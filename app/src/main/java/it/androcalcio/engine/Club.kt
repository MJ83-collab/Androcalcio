package it.androcalcio.engine

/** Dirigenza, tifosi e reputazione dell'allenatore: regole di fine stagione e richieste di fondi. */
object Club {
    const val ESONERO_SOGLIA = 20
    const val FONDI_FIDUCIA_MIN = 50
    const val FONDI_TIFOSI_MIN = 40

    /** Posizione attesa (1 = prima) dalla forza della squadra nel suo campionato. */
    fun expectedRank(strengths: Map<Long, Double>, me: Long): Int =
        strengths.entries.sortedByDescending { it.value }.indexOfFirst { it.key == me } + 1

    /** Variazione di fiducia: obiettivo = posizione attesa; coppa e titolo danno un bonus. */
    fun fiduciaDelta(pos: Int, expected: Int, cupWon: Boolean, champion: Boolean): Int {
        val diff = expected - pos                       // positivo = meglio delle attese
        var d = (diff * 3).coerceIn(-20, 10)
        if (diff == 0) d = 3
        if (cupWon) d += 5
        if (champion) d += 5
        return d
    }

    fun tifosiDelta(pos: Int, expected: Int, champion: Boolean): Int =
        ((expected - pos) * 2).coerceIn(-15, 10) + if (champion) 5 else 0

    /** Promozione (+4) e retrocessione (-8) si sommano a parte, a fine stagione. */
    fun reputazioneDelta(pos: Int, expected: Int, titles: Int): Int =
        (expected - pos).coerceIn(-6, 6) + titles * 6

    fun canRequestFunds(fiducia: Int, tifosi: Int) = fiducia >= FONDI_FIDUCIA_MIN && tifosi >= FONDI_TIFOSI_MIN

    /** Importo massimo concesso per una richiesta: cresce con la fiducia (60 = 8 M, 100 = 24 M). */
    fun maxFunds(fiducia: Int): Long = ((fiducia - 40).coerceAtLeast(0) * 400_000L)

    fun shouldFire(fiducia: Int) = fiducia < ESONERO_SOGLIA

    fun fiduciaLabel(v: Int) = when {
        v >= 80 -> "Ottima"; v >= 60 -> "Buona"; v >= 40 -> "Discreta"; v >= ESONERO_SOGLIA -> "A rischio"; else -> "Critica"
    }
}

object ClubRequests {
    /** Tipi di richiesta fondi alla dirigenza. */
    val TIPI = listOf("stadio", "allenamento", "vivaio", "ingaggi")
    /** Importi base proposti (prima di tipo e prestigio). */
    private val BASE = listOf(2_000_000L, 5_000_000L, 10_000_000L)

    /**
     * Importi proposti per una richiesta: scalati sul tipo (stadio pesa più di allenamento e vivaio)
     * e sul prestigio della squadra (reputazione e tifosi): da circa 0,7x a 1,6x.
     */
    fun importi(tipo: String, reputazione: Int, tifosi: Int): List<Long> {
        val fattoreTipo = when (tipo) { "stadio" -> 1.0; "allenamento" -> 0.7; "ingaggi" -> 0.3; else -> 0.5 }
        val prestigio = 0.7 + 0.9 * ((reputazione + tifosi) / 200.0).coerceIn(0.0, 1.0)
        return BASE.map { b -> Math.round(b * fattoreTipo * prestigio / 100_000.0) * 100_000L }
    }
}

/** Offerta di panchina: club, garanzia di budget mercato e ingaggio annuo dell'allenatore. */
data class ClubOffer(val teamId: Long, val garanzia: Long, val stipendio: Long) {
    companion object {
        fun encode(l: List<ClubOffer>) = l.joinToString(";") { "${it.teamId}|${it.garanzia}|${it.stipendio}" }
        fun decode(s: String?): List<ClubOffer> = s.orEmpty().split(";").mapNotNull {
            val p = it.split("|")
            if (p.size == 3) ClubOffer(p[0].toLongOrNull() ?: return@mapNotNull null,
                p[1].toLongOrNull() ?: 0L, p[2].toLongOrNull() ?: 0L) else null
        }
    }
}

object ClubOffers {
    const val RICHIESTA_GARANZIE_REP_MIN = 50

    /**
     * Offerte di fine stagione. Esonerato: club più piccoli del tuo. Altrimenti: club di livello
     * proporzionale alla reputazione (alta = squadre blasonate), da 0 a 3 offerte.
     */
    fun make(strengths: Map<Long, Double>, budgets: Map<Long, Long>, me: Long, reputazione: Int,
             fired: Boolean, rng: kotlin.random.Random): List<ClubOffer> {
        val others = strengths.filterKeys { it != me }.entries.sortedByDescending { it.value }
        if (others.isEmpty()) return emptyList()
        val mine = strengths[me] ?: 0.0
        val picks: List<Long> = if (fired) {
            val smaller = others.filter { it.value < mine }.ifEmpty { others.takeLast(10) }
            smaller.shuffled(rng).take(3).map { it.key }
        } else {
            val n = (reputazione / 30).coerceIn(0, 3)
            if (n == 0) emptyList() else {
                val target = ((100 - reputazione) / 100.0 * (others.size - 1)).toInt()
                val from = (target - 4).coerceAtLeast(0)
                others.subList(from, (target + 5).coerceAtMost(others.size)).shuffled(rng).take(n).map { it.key }
            }
        }
        return picks.map { id ->
            val g = ((budgets[id] ?: 0L) / 4 * (50 + reputazione) / 100).coerceAtLeast(1_000_000L)
            val st = 200_000L + reputazione * 20_000L
            ClubOffer(id, g / 100_000L * 100_000L, st / 100_000L * 100_000L)
        }
    }
}
