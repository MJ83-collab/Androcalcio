package it.androcalcio.engine

/** Ampliamento dello stadio: blocchi di posti pagati una tantum dal budget, fino a un tetto. */
object Stadium {
    const val STEP = 2_000          // posti per blocco
    const val COST_PER_SEAT = 300L  // euro per posto aggiunto
    const val MAX_CAPACITY = 80_000

    /** Costo di un blocco di posti. */
    val blockCost: Long get() = STEP * COST_PER_SEAT

    /** Puoi ampliare se non superi il tetto. */
    fun canExpand(capacity: Int): Boolean = capacity + STEP <= MAX_CAPACITY
}
