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

    /** Importo massimo concesso per una richiesta: cresce con la fiducia (50 = 2 M, 100 = 10 M). */
    fun maxFunds(fiducia: Int): Long = ((fiducia - 40).coerceAtLeast(0) * 160_000L)

    fun shouldFire(fiducia: Int) = fiducia < ESONERO_SOGLIA

    fun fiduciaLabel(v: Int) = when {
        v >= 80 -> "Ottima"; v >= 60 -> "Buona"; v >= 40 -> "Discreta"; v >= ESONERO_SOGLIA -> "A rischio"; else -> "Critica"
    }
}
