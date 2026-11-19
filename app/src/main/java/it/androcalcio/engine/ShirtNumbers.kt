package it.androcalcio.engine

import it.androcalcio.data.Player

/**
 * Numeri di maglia: li assegna l'allenatore in seconda a inizio stagione (senza un vice, la società).
 * Chi ha già un numero lo tiene, se non è doppio; i nuovi arrivi ricevono il primo numero libero
 * tra quelli tipici del ruolo: 1 portiere, 2-6 difensori, 7-10 centrocampisti, 9-11 attaccanti.
 */
object ShirtNumbers {

    private val preferiti = mapOf(
        "P" to listOf(1, 12, 13),
        "D" to listOf(2, 3, 4, 5, 6),
        "C" to listOf(7, 8, 10, 6),
        "A" to listOf(9, 11, 7)
    )

    private fun rank(role: String) = when (role) { "P" -> 0; "D" -> 1; "C" -> 2; else -> 3 }

    /** Numero per ogni giocatore della rosa, senza doppi. */
    fun assign(players: List<Player>): Map<Long, Int> {
        val result = HashMap<Long, Int>()
        val used = HashSet<Int>()
        // 1. Chi ha già un numero valido lo tiene
        players.sortedBy { it.id }.forEach { p ->
            if (p.numero in 1..99 && used.add(p.numero)) result[p.id] = p.numero
        }
        // 2. Gli altri, dal portiere agli attaccanti, con il migliore per primo
        players.filter { it.id !in result }
            .sortedWith(compareBy<Player>({ rank(it.role) }, { -it.overall }, { it.id }))
            .forEach { p ->
                val n = (preferiti[p.role].orEmpty() + (1..99)).firstOrNull { it !in used } ?: 0
                if (n > 0) { used.add(n); result[p.id] = n }
            }
        return result
    }
}
