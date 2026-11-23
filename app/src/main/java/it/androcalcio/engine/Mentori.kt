package it.androcalcio.engine

/** Mentore: un veterano bravo nello stesso reparto aiuta un giovane a crescere di un punto a fine stagione. */
object Mentori {
    fun bonus(giovane: it.androcalcio.data.Player, rosa: List<it.androcalcio.data.Player>): Int {
        if (giovane.age > 22) return 0
        val c = rosa.any { it.id != giovane.id && it.role == giovane.role && it.age >= 28 && it.overall >= giovane.overall + 5 }
        return if (c) 1 else 0
    }
}
