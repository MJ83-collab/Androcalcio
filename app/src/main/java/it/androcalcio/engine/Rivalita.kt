package it.androcalcio.engine

/** Derby e rivalità: quando la tua squadra gioca contro la rivale, la tifoseria e la dirigenza ne risentono di più. */
object Rivalita {
    /** Coppie di squadre rivali (nomi come in squadre.json). */
    private val coppie = listOf(
        "Milano RN" to "Inter NA",
        "Roma GR" to "Lazio BC",
        "Genova GF" to "Genova BL",
        "Torino GT" to "Piemonte BN",
        "Varese" to "Valceresio",
        "Lecco" to "Ravenna",
        "Bari" to "Taranto"
    )

    /** La rivale storica di una squadra, se ne ha una. */
    fun rivaleDi(nome: String): String? = coppie.firstNotNullOfOrNull { (x, y) ->
        when (nome) { x -> y; y -> x; else -> null }
    }

    /** Vero se le due squadre sono rivali (derby), in qualunque ordine. */
    fun isDerby(a: String, b: String): Boolean =
        coppie.any { (x, y) -> (x == a && y == b) || (x == b && y == a) }
}
