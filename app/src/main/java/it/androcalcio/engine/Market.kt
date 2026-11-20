package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.math.abs

sealed class OfferResult {
    data class Accepted(val msg: String) : OfferResult()
    data class Rejected(val msg: String) : OfferResult()
    data class Counter(val player: Player, val price: Long, val wage: Long = 0L, val years: Int = 3) : OfferResult()
    /** Il club è d'accordo sul prezzo ma il giocatore chiede un ingaggio diverso. */
    data class WageCounter(val player: Player, val price: Long, val wage: Long, val years: Int) : OfferResult()
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

    /** Ingaggio annuo realistico alla FM: ~8% del valore di mercato di quel livello (80 ≈ 2 M, 85 ≈ 5 M, 90 ≈ 12 M). */
    fun wageAt(level: Int, age: Int = 27): Long {
        val f = if (age < 24 || age > 32) 0.8 else 1.0
        val w = (0.08 * valueAt(level) * f).toLong().coerceAtLeast(30_000L)
        return if (w >= 1_000_000L) w / 50_000L * 50_000L else w / 10_000L * 10_000L
    }

    /** Fascino della squadra per il giocatore (0..1): forza relativa, reputazione dell'allenatore, fiducia, tifosi, clima. */
    fun appeal(p: Player, buyerStrength: Int, rep: Int, fiducia: Int, tifosi: Int, clima: Int): Double {
        val rel = (0.5 + (buyerStrength - p.overall) / 12.0).coerceIn(0.0, 1.0)
        val a = 0.4 * rel + 0.2 * rep / 100 + 0.15 * fiducia / 100 + 0.1 * tifosi / 100 + 0.15 * clima / 100
        return (a + if (p.age >= 30) 0.15 else 0.0).coerceIn(0.0, 1.0)   // dai 30 anni accettano più facilmente
    }

    /** Le stelle (80+) sotto i 30 anni rifiutano squadre deboli o poco prestigiose. */
    fun refuses(p: Player, buyerStrength: Int, appeal: Double): Boolean =
        p.overall >= 80 && p.age < 30 && (buyerStrength < p.overall - 4 || appeal < 0.35)

    /** Ingaggio annuo chiesto: base × (1,30 − 0,45 × fascino); durata sgradita = +10%. */
    fun askedWage(p: Player, appeal: Double, years: Int): Long {
        var w = wageAt(p.overall, p.age) * (1.30 - 0.45 * appeal)
        if (p.age >= 31 && years > 2) w *= 1.10
        if (p.age < 24 && years < 3) w *= 1.10
        return (w.toLong() / 10_000L) * 10_000L
    }
}
