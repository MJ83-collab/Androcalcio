package it.androcalcio.engine

import kotlin.math.pow

/** Prezzi per settore e abbonamenti: dove sta il punto giusto tra riempimento e incasso. */
object Abbonamenti {
    data class Settore(val id: String, val nome: String, val quota: Double, val peso: Double)

    /** quota = parte dei posti; peso = quanto vale un posto rispetto al prezzo base. */
    val SETTORI = listOf(
        Settore("curva", "Curve", 0.35, 0.70),
        Settore("distinti", "Distinti", 0.25, 0.90),
        Settore("tribuna", "Tribuna", 0.30, 1.30),
        Settore("vip", "Tribuna VIP e skybox", 0.10, 2.80)
    )
    const val PREZZO_MIN = 50
    const val PREZZO_MAX = 200

    fun decode(s: String?): Map<String, Int> {
        val m = s.orEmpty().split(",").mapNotNull {
            val p = it.split(":"); if (p.size == 2) p[0] to (p[1].toIntOrNull() ?: return@mapNotNull null) else null
        }.toMap()
        return SETTORI.associate { it.id to (m[it.id] ?: 100).coerceIn(PREZZO_MIN, PREZZO_MAX) }
    }
    fun encode(m: Map<String, Int>) = m.entries.joinToString(",") { "${it.key}:${it.value}" }

    /** Riempimento di un settore: sale con la domanda (tifosi, forza) e cala se il prezzo supera il 100%. */
    fun riempimento(relativo: Double, tifosi: Int, prezzoPct: Int): Double {
        val domanda = Economia.riempimento(relativo) + (tifosi - 50) / 400.0
        val elasticita = (100.0 / prezzoPct).pow(1.4)
        return (domanda * elasticita).coerceIn(0.05, 1.0)
    }

    /** Incasso di una stagione con i prezzi scelti per settore. prezzoBase = biglietto medio. */
    fun incasso(capienza: Int, prezzoBase: Int, relativo: Double, tifosi: Int, prezzi: Map<String, Int>, partite: Int): Long {
        val somma = SETTORI.sumOf { it.quota * it.peso }.takeIf { it > 0 } ?: 1.0
        val v = SETTORI.sumOf { s ->
            val pct = prezzi[s.id] ?: 100
            val posti = capienza * s.quota
            val prezzo = prezzoBase * s.peso / somma * pct / 100.0
            posti * riempimento(relativo, tifosi, pct) * prezzo
        } * partite
        return v.toLong() / 1_000L * 1_000L
    }

    /** Abbonati: quota fissa a inizio stagione, più alta con tifosi alti e prezzi bassi. In % dei posti, 5..60. */
    fun abbonatiPct(tifosi: Int, prezzoMedioPct: Int): Int =
        (20 + (tifosi - 50) * 0.5 - (prezzoMedioPct - 100) * 0.4).toInt().coerceIn(5, 60)
}

/** Rapporto tra l'incasso con i prezzi scelti e quello a prezzi standard: si applica all'incasso base della stagione. */
fun Abbonamenti.fattore(capienza: Int, prezzoBase: Int, relativo: Double, tifosi: Int, prezzi: Map<String, Int>, partite: Int): Double {
    val base = incasso(capienza, prezzoBase, relativo, tifosi, SETTORI.associate { it.id to 100 }, partite)
    if (base <= 0L) return 1.0
    return incasso(capienza, prezzoBase, relativo, tifosi, prezzi, partite).toDouble() / base
}
