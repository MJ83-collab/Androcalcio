package it.androcalcio.engine

/**
 * Difficoltà della carriera: cinque livelli più un profilo personalizzato.
 * Ogni livello agisce su budget iniziale, incassi, infortuni e pazienza della dirigenza.
 */
object Difficolta {
    /** Profilo: tutti i valori in percentuale (100 = nessun cambiamento). */
    data class Profilo(val budget: Int, val incassi: Int, val infortuni: Int, val dirigenza: Int) {
        fun coerente() = Profilo(budget.coerceIn(30, 250), incassi.coerceIn(50, 200), infortuni.coerceIn(30, 250), dirigenza.coerceIn(30, 250))
    }

    val NOMI = listOf("Principiante", "Facile", "Normale", "Difficile", "Leggenda", "Personalizzata")
    const val NORMALE = 2
    const val PERSONALIZZATA = 5
    const val FACILE_VECCHIO = 0   // indici delle versioni precedenti: 0 Facile, 1 Normale, 2 Difficile

    /** Dal vecchio indice (3 livelli) al nuovo (5 + personalizzata). */
    fun daVecchio(v: Int): Int = (v + 1).coerceIn(1, 3)

    val PROFILI = listOf(
        Profilo(budget = 200, incassi = 125, infortuni = 60, dirigenza = 60),    // Principiante
        Profilo(budget = 150, incassi = 110, infortuni = 80, dirigenza = 80),    // Facile
        Profilo(budget = 100, incassi = 100, infortuni = 100, dirigenza = 100),  // Normale
        Profilo(budget = 60, incassi = 90, infortuni = 120, dirigenza = 125),    // Difficile
        Profilo(budget = 40, incassi = 80, infortuni = 150, dirigenza = 160)     // Leggenda
    )

    fun profilo(livello: Int, personalizzato: Profilo = PROFILI[NORMALE]): Profilo =
        if (livello == PERSONALIZZATA) personalizzato.coerente() else PROFILI[livello.coerceIn(0, PROFILI.lastIndex)]

    fun moltiplicatoreBudget(livello: Int, p: Profilo = PROFILI[NORMALE]): Double = profilo(livello, p).budget / 100.0

    fun budgetIniziale(budget: Long, livello: Int, p: Profilo = PROFILI[NORMALE]): Long =
        (budget * moltiplicatoreBudget(livello, p)).toLong()

    /** Incassi di fine stagione (biglietti, sponsor, merchandising). */
    fun incassi(valore: Long, livello: Int, p: Profilo = PROFILI[NORMALE]): Long = valore * profilo(livello, p).incassi / 100

    /** Variazione di fiducia: le perdite pesano di più con dirigenza severa, i guadagni di meno. */
    fun fiducia(delta: Int, livello: Int, p: Profilo = PROFILI[NORMALE]): Int {
        val k = profilo(livello, p).dirigenza / 100.0
        return Math.round(if (delta < 0) delta * k else delta / k).toInt()
    }

    /** Punti di prevenzione infortuni (negativi = più infortuni): applicati a ogni partita. */
    fun prevenzioneExtra(livello: Int, p: Profilo = PROFILI[NORMALE]): Int = (100 - profilo(livello, p).infortuni) / 2

    fun descrizione(livello: Int, p: Profilo = PROFILI[NORMALE]): String = profilo(livello, p).let {
        "Budget ${it.budget}% · incassi ${it.incassi}% · infortuni ${it.infortuni}% · dirigenza ${it.dirigenza}%"
    }
}
