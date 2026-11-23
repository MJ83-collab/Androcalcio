package it.androcalcio.engine

/** Sponsor e merchandising: incassi a fine stagione, solo per la tua squadra. */
object Sponsorship {
    /** Offerte di sponsor (nomi di fantasia): compenso annuo fisso. */
    data class Offer(val name: String, val annual: Long)

    val offers = listOf(
        Offer("Acqua Monteverde", 400_000L),
        Offer("Autoscuole Lagunari", 900_000L),
        Offer("Cantina Torre Alta", 1_500_000L)
    )

    /** Sponsor non presenti nelle offerte (dai dati di partenza): compenso base. */
    private const val DEFAULT_ANNUAL = 400_000L

    /** Compenso annuo dello sponsor attuale. */
    fun annual(sponsor: String): Long = offers.firstOrNull { it.name == sponsor }?.annual ?: DEFAULT_ANNUAL

    /** Merchandising: 25.000 € ogni 1.000 posti dello stadio, a fine stagione. */
    fun merch(capacity: Int): Long = capacity / 1_000L * 25_000L
}
