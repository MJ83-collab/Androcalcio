package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.math.abs

sealed class OfferResult {
    data class Accepted(val msg: String) : OfferResult()
    data class Rejected(val msg: String) : OfferResult()
    data class Counter(val player: Player, val price: Long) : OfferResult()
}

object Market {
    const val MAX_SQUAD = 30
    const val MIN_SQUAD = 15

    /** Valore di mercato di un giocatore di quel livello a 26-29 anni, scala reale:
     *  60 ≈ 0,6 M · 70 ≈ 4 M · 75 ≈ 10 M · 80 ≈ 25 M · 85 ≈ 62 M · 90 ≈ 153 M (+20% per punto). */
    fun valueAt(level: Int): Double = 4_000_000.0 * Math.pow(1.2, (level - 70).toDouble())

    /** Valore di mercato alla FM: livello attuale, per i giovani conta metà del potenziale, poi l'età. */
    fun value(p: Player): Long {
        val level = if (p.age < 24) p.overall + (p.potential - p.overall).coerceAtLeast(0) / 2 else p.overall
        val age = when {
            p.age < 22 -> 1.3
            p.age < 26 -> 1.15
            p.age < 30 -> 1.0
            p.age < 32 -> 0.7
            p.age < 34 -> 0.45
            else -> 0.25
        }
        val v = (valueAt(level) * age).toLong().coerceAtLeast(50_000L)
        return if (v >= 1_000_000L) v / 100_000L * 100_000L else v / 10_000L * 10_000L
    }

    /** Richiesta del club venditore: tra 95% e 125% del valore, fissa per ogni giocatore. */
    fun demand(p: Player): Long {
        val f = 0.95 + (abs(p.id.hashCode()) % 31) / 100.0
        return ((value(p) * f).toLong() / 10_000L) * 10_000L
    }
}
