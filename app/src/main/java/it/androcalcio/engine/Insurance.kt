package it.androcalcio.engine

/** Assicurazione infortuni: premio annuo a fine stagione, rimborso di una quota dell'ingaggio per ogni turno di stop. */
object Insurance {
    const val NONE = 0
    const val BASE = 1
    const val FULL = 2
    const val ROUNDS = 38   // turni di campionato: lo stipendio si ripartisce su questi

    /** Premio annuo del livello scelto. */
    fun premium(level: Int): Long = when (level) {
        BASE -> 300_000L
        FULL -> 700_000L
        else -> 0L
    }

    /** Quota dell'ingaggio rimborsata per ogni turno di stop. */
    fun cover(level: Int): Double = when (level) {
        BASE -> 0.5
        FULL -> 1.0
        else -> 0.0
    }

    /** Rimborso per un infortunio di [rounds] turni. */
    fun refund(salary: Long, rounds: Int, level: Int, turniStagione: Int = ROUNDS): Long =
        (salary / turniStagione.coerceAtLeast(1).toDouble() * rounds * cover(level)).toLong()

    /** Turni di campionato veri: andata e ritorno con [squadre] squadre (38 con 20, 34 con 18). */
    fun turni(squadre: Int): Int = if (squadre < 2) ROUNDS else (squadre - 1) * 2
}
