package it.androcalcio.engine

/** Condizioni dei cambi programmati, combinabili tra loro (maschera di bit salvata nelle preferenze). */
object CambiCondizioni {
    const val STANCO = 1
    const val AMMONITO = 2
    const val SOTTO = 4
    const val SOPRA = 8

    val TUTTE = listOf(
        STANCO to "Stanco (< 88%)",
        AMMONITO to "Ammonito",
        SOTTO to "In svantaggio dal 60': entra un attaccante",
        SOPRA to "In vantaggio dal 75': entra un difensore"
    )
}
