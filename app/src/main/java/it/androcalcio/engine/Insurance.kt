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
    fun refund(salary: Long, rounds: Int, level: Int): Long =
        (salary / ROUNDS.toDouble() * rounds * cover(level)).toLong()
}
