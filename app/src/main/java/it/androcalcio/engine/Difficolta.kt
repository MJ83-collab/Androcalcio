package it.androcalcio.engine

/** Difficoltà della carriera: cambia il budget di mercato iniziale della tua squadra. */
object Difficolta {
    val NOMI = listOf("Facile", "Normale", "Difficile")
    const val NORMALE = 1

    fun moltiplicatoreBudget(livello: Int): Double = when (livello.coerceIn(0, NOMI.lastIndex)) {
        0 -> 1.5
        1 -> 1.0
        else -> 0.6
    }

    fun budgetIniziale(budget: Long, livello: Int): Long = (budget * moltiplicatoreBudget(livello)).toLong()

    fun descrizione(livello: Int): String = when (livello.coerceIn(0, NOMI.lastIndex)) {
        0 -> "Budget di mercato iniziale +50%."
        1 -> "Budget di mercato iniziale come da squadra."
        else -> "Budget di mercato iniziale -40%."
    }
}
