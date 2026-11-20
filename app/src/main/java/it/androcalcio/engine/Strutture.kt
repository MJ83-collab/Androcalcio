package it.androcalcio.engine

/**
 * Strutture della tua squadra: centro di allenamento e vivaio, livelli da 1 a 5.
 * Allenamento: ogni livello oltre il 1° dà l'8% di probabilità in più che un giocatore fino a 28 anni
 * guadagni +1 a fine stagione (entro il potenziale). Vivaio: +2 al potenziale dei giovani per livello.
 * Costo di salita e manutenzione annua crescono con il livello.
 */
object Strutture {
    const val MAX = 5
    const val ALLENAMENTO = "liv_allenamento"
    const val VIVAIO = "liv_vivaio"

    /** Costo per passare dal livello `da` al successivo: 2 M, 5 M, 11 M, 24 M. */
    fun costo(da: Int): Long = when (da) { 1 -> 2_000_000L; 2 -> 5_000_000L; 3 -> 11_000_000L; else -> 24_000_000L }

    /** Manutenzione annua di una struttura (pagata a fine stagione): 150.000 € per livello oltre il 1°. */
    fun manutenzione(livello: Int): Long = (livello - 1).coerceAtLeast(0) * 150_000L

    fun bonusAllenamento(livello: Int): Int = (livello - 1).coerceIn(0, MAX - 1) * 8   // % di +1 a stagione
    fun bonusVivaio(livello: Int): Int = (livello - 1).coerceIn(0, MAX - 1) * 2        // punti di potenziale

    fun descrizione(tipo: String, livello: Int) =
        if (tipo == ALLENAMENTO) "Allenamento: +${bonusAllenamento(livello)}% di crescita a stagione"
        else "Vivaio: +${bonusVivaio(livello)} di potenziale ai giovani"
}
