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

    /** Legge la lista salvata (voti in decimi separati da virgola). */
    fun leggi(s: String?): List<Double> = s?.split(",")?.mapNotNull { it.toIntOrNull()?.let { v -> v / 10.0 } } ?: emptyList()

    /** Icona della rosa: freccia su o giù se la forma è marcata. */
    fun icona(m: Double?): String = when {
        m == null -> ""
        m >= 7.0 -> "↑ "
        m < 5.5 -> "↓ "
        else -> ""
    }

    /** Rischio infortunio indicativo: più alto con l'età, la stanchezza e la poca tenuta fisica. */
    fun rischio(p: it.androcalcio.data.Player): String {
        val punti = (p.age - 28).coerceAtLeast(0) * 2 + (100 - p.condition) / 5 + (100 - p.fo) / 10
        return when {
            punti >= 14 -> "alto"
            punti >= 7 -> "medio"
            else -> "basso"
        }
    }

    fun simbolo(m: Double?): String = when {
        m == null -> "nessuna partita"
        m >= 7.0 -> "↑ in forma"
        m < 5.5 -> "↓ in crisi"
        else -> "→ nella norma"
    }
}
