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

    /** Valore di mercato: cresce col quadrato del livello, scende con l'età. */
    fun value(p: Player): Long {
        val base = (p.overall - 40).coerceAtLeast(1).let { it.toLong() * it } * 12_000L
        val age = when {
            p.age < 22 -> 1.4
            p.age < 26 -> 1.2
            p.age < 30 -> 1.0
            p.age < 33 -> 0.6
            else -> 0.3
        }
        return ((base * age).toLong() / 10_000L) * 10_000L
    }

    /** Richiesta del club venditore: tra 95% e 125% del valore, fissa per ogni giocatore. */
    fun demand(p: Player): Long {
        val f = 0.95 + (abs(p.id.hashCode()) % 31) / 100.0
        return ((value(p) * f).toLong() / 10_000L) * 10_000L
    }
}
