package it.androcalcio.engine

/** Bonus fedeltà: chi resta nel club per almeno 3 stagioni riceve a fine stagione un premio e un po' di morale. */
object Fedelta {
    const val KEY = "fedelta"          // "nome:stagioni|nome:stagioni"
    const val ANNI_MIN = 3
    const val BONUS_PCT = 3            // percentuale dell'ingaggio annuo
    const val MORALE = 3

    fun decode(s: String?): Map<String, Int> = s.orEmpty().split("|").mapNotNull { e ->
        val i = e.lastIndexOf(':')
        val n = if (i > 0) e.substring(i + 1).toIntOrNull() else null
        if (n == null) null else e.substring(0, i) to n
    }.toMap()

    fun encode(m: Map<String, Int>): String = m.entries.joinToString("|") { "${it.key}:${it.value}" }

    /** Una stagione in più per chi è ancora in rosa; chi se n'è andato esce dall'elenco. */
    fun avanza(prima: Map<String, Int>, rosa: List<String>): Map<String, Int> = rosa.associateWith { (prima[it] ?: 0) + 1 }

    fun bonus(ingaggio: Long): Long = ingaggio * BONUS_PCT / 100
}
