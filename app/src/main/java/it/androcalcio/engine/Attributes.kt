package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/** Parametri dei giocatori, ruoli dettagliati e valori usati dal motore partita. */
object Attr {
    // Scarto per ruolo rispetto al livello: ve, re, ag, qu, en, pa, dr, fi, ti, ri
    private val BIAS = mapOf(
        "P" to intArrayOf(-8, 0, -5, 0, -25, -10, -25, -35, -30, -20),
        "D" to intArrayOf(0, 3, 4, 0, 6, -3, -10, -22, -15, -12),
        "C" to intArrayOf(0, 4, 0, 3, 0, 6, 0, -8, -3, -5),
        "A" to intArrayOf(4, -3, -2, 0, -15, -3, 4, 8, 5, 2)
    )

    val POS_NAMES = mapOf(
        "POR" to "Portiere", "TD" to "Terzino des.", "TS" to "Terzino sin.", "DC" to "Centrale",
        "MED" to "Mediano", "REG" to "Regista", "INT" to "Interno", "MP" to "Mezzapunta",
        "ES" to "Estremo", "CA" to "Centravanti"
    )
    val FOOT_NAMES = mapOf("D" to "Destro", "S" to "Mancino", "A" to "Ambidestro")

    fun posName(p: Player): String = POS_NAMES[p.pos] ?: when (p.role) {
        "P" -> "Portiere"; "D" -> "Difensore"; "C" -> "Centrocampista"; else -> "Attaccante"
    }

    private fun randomPos(role: String, rng: Random): String = when (role) {
        "P" -> "POR"
        "D" -> listOf("TD", "TS", "DC", "DC").random(rng)
        "C" -> listOf("MED", "REG", "INT", "MP").random(rng)
        else -> listOf("ES", "CA", "CA").random(rng)
    }

    /** Assegna parametri coerenti col livello e col ruolo; il livello (overall) non cambia. */
    fun fill(p: Player, rng: Random): Player {
        val b = BIAS[p.role] ?: BIAS.getValue("C")
        fun v(i: Int) = (p.overall + b[i] + rng.nextInt(-6, 7)).coerceIn(25, 99)
        val old = if (p.age > 31) (p.age - 31) * 2 else 0
        val foot = when (rng.nextInt(100)) { in 0..24 -> "S"; in 25..29 -> "A"; else -> "D" }
        return p.copy(
            ve = (v(0) - old).coerceIn(25, 99), re = (v(1) - old).coerceIn(25, 99),
            ag = v(2), qu = v(3), fo = rng.nextInt(60, 100),
            gm = if (p.role == "P") (p.overall + 8 + rng.nextInt(-6, 7)).coerceIn(25, 99) else rng.nextInt(8, 31),
            en = v(4), pa = v(5), dr = v(6), fi = v(7), ti = v(8), ri = v(9),
            foot = foot, pos = randomPos(p.role, rng)
        )
    }

    /** Sposta tutti i parametri dello stesso scarto (crescita o declino); velocità e resistenza calano di più con l'età. */
    fun shift(p: Player, delta: Int): Player {
        fun s(x: Int, extra: Int = 0) = (x + delta - extra).coerceIn(25, 99)
        val old = if (p.age > 30) 1 else 0
        return p.copy(
            ve = s(p.ve, old), re = s(p.re, old), ag = s(p.ag), qu = s(p.qu),
            gm = if (p.role == "P") s(p.gm) else p.gm, en = s(p.en), pa = s(p.pa), dr = s(p.dr),
            fi = s(p.fi), ti = s(p.ti), ri = s(p.ri)
        )
    }

    private fun form(p: Player) = 0.94 + p.fo * 0.0006

    // Valori per il motore: metà del livello, metà dei parametri che contano per quel compito
    fun atk(p: Player): Double {
        val s = if (p.role == "A") 0.4 * p.fi + 0.25 * p.ti + 0.2 * p.dr + 0.15 * p.ve
        else 0.4 * p.pa + 0.3 * p.qu + 0.15 * p.dr + 0.15 * p.ti
        return (0.5 * p.overall + 0.5 * s) * form(p)
    }
    fun mid(p: Player) = (0.5 * p.overall + 0.5 * (0.35 * p.pa + 0.25 * p.qu + 0.2 * p.re + 0.2 * p.en)) * form(p)
    fun def(p: Player) = (0.5 * p.overall + 0.5 * (0.4 * p.en + 0.2 * p.ag + 0.2 * p.re + 0.2 * p.ve)) * form(p)
    fun gk(p: Player) = (0.5 * p.overall + 0.5 * (0.7 * p.gm + 0.3 * p.qu)) * form(p)
}
