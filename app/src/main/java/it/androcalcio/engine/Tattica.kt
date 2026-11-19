package it.androcalcio.engine

/**
 * Tattica predefinita di un allenatore: ATTACCO (squadra alta e aggressiva) oppure DIFESA (squadra bassa e compatta).
 * Ogni allenatore ha entrambe le tattiche pronte; lo stile è quello che usa di default.
 */
enum class Stile(
    val label: String,
    val mentalita: Mentality,
    val pressing: Pressing,
    val linea: DefLine
) {
    ATTACCO("Attacco", Mentality.OFFENSIVA, Pressing.ALTO, DefLine.ALTA),
    DIFESA("Difesa", Mentality.DIFENSIVA, Pressing.BASSO, DefLine.BASSA);

    /** Testo per la scheda dello staff. */
    fun descrizione(): String =
        "Tattica ${label.lowercase()}: mentalità ${mentalita.label.lowercase()}, pressing ${pressing.label.lowercase()}, linea ${linea.label.lowercase()}"
}
