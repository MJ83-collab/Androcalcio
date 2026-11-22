package it.androcalcio.engine

import it.androcalcio.data.Movimento

/**
 * Fair play finanziario: conta la perdita operativa degli ultimi tre anni (mercato e stadio non contano,
 * come nelle regole vere). Sopra la soglia: multa e blocco del mercato in entrata per la stagione dopo.
 */
object FairPlay {
    const val ANNI = 3

    data class Esito(val perdita: Long, val soglia: Long, val sforamento: Long, val multa: Long, val blocco: Boolean) {
        val livello get() = when {
            sforamento > 0 -> "Rosso"
            perdita * 100L >= soglia * 70L -> "Giallo"
            else -> "Verde"
        }
    }

    fun soglia(budget: Long): Long = 8_000_000L + budget * 3L / 10L

    /** Risultato operativo di una stagione: movimenti esclusi mercato e stadio. Negativo = perdita. */
    fun operativo(mov: List<Movimento>): Long =
        mov.filter { Bilancio.categoria(it.label) !in setOf("Mercato", "Stadio") }.sumOf { it.importo }

    fun valuta(perditeStagioni: List<Long>, budget: Long): Esito {
        val perdita = perditeStagioni.takeLast(ANNI).sum().coerceAtLeast(0L)
        val s = soglia(budget)
        val sfor = (perdita - s).coerceAtLeast(0L)
        return Esito(perdita, s, sfor, sfor / 10L, sfor > s / 4L)
    }

    fun encode(l: List<Pair<String, Long>>): String = l.joinToString(";") { "${it.first}:${it.second}" }
    fun decode(s: String?): List<Pair<String, Long>> = s.orEmpty().split(";").mapNotNull {
        val p = it.split(":")
        if (p.size == 2) p[0] to (p[1].toLongOrNull() ?: return@mapNotNull null) else null
    }
}
