package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.math.roundToInt

/** Carisma e capitano: il carisma cresce con l'esperienza (età) e il morale; il capitano è il più forte e carismatico. */
object Leadership {
    /** Capitano scelto dall'utente (0 = automatico). Vale solo se è in campo. */
    @Volatile var scelto: Long = 0L

    /** Carisma 0..100: da 18 anni (0) a 34 anni (70), più 30 punti a morale alto. */
    fun carisma(p: Player): Int {
        val eta = (p.age - 18).coerceIn(0, 16) / 16.0 * 70.0
        val morale = p.morale.coerceIn(0, 100) * 0.3
        return (eta + morale).roundToInt().coerceIn(0, 100)
    }

    /** Punteggio per scegliere il capitano: 60% media, 40% carisma. */
    fun punteggio(p: Player): Double = 0.6 * p.overall + 0.4 * carisma(p)

    /** Capitano tra i giocatori in campo (portieri esclusi): il migliore per punteggio. */
    fun capitano(inCampo: List<Player>): Player? =
        inCampo.firstOrNull { it.id == scelto && scelto != 0L } ?: inCampo.filter { it.role != "P" }.maxByOrNull { punteggio(it) } ?: inCampo.maxByOrNull { punteggio(it) }
}
