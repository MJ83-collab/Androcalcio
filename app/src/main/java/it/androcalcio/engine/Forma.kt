package it.androcalcio.engine

import kotlin.math.roundToInt

/** Forma recente: gli ultimi cinque voti di un giocatore, salvati in decimi (72 = 7,2). */
object Forma {
    fun key(playerId: Long) = "forma_$playerId"

    fun aggiungi(vecchia: String?, voto: Double): String {
        val l = (vecchia?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()) + (voto * 10).roundToInt()
        return l.takeLast(5).joinToString(",")
    }

    /** Media degli ultimi voti, oppure null se non ha ancora giocato. */
    fun media(s: List<Double>): Double? = if (s.isEmpty()) null else s.average()

    fun simbolo(m: Double?): String = when {
        m == null -> "nessuna partita"
        m >= 7.0 -> "↑ in forma"
        m < 5.5 -> "↓ in crisi"
        else -> "→ nella norma"
    }
}
