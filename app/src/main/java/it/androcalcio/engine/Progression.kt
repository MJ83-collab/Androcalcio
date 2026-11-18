package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.SeedData
import it.androcalcio.data.Team
import kotlin.random.Random

object Progression {
    /** Ingaggio annuo richiesto in base a livello ed età. */
    fun wage(p: Player): Long {
        val f = if (p.age < 24 || p.age > 32) 0.8 else 1.0
        return (((p.overall - 30).coerceAtLeast(5) * 9_000L * f).toLong() / 1_000L) * 1_000L
    }

    /** Un anno dopo: età, crescita o declino, potenziale e contratto. */
    fun advance(p: Player, rng: Random): Player {
        val age = p.age + 1
        val delta = when {
            age <= 21 -> rng.nextInt(1, 6)
            age <= 26 -> rng.nextInt(-1, 4)
            age <= 30 -> rng.nextInt(-2, 2)
            age <= 33 -> -rng.nextInt(0, 4)
            else -> -rng.nextInt(1, 5)
        }
        var ov = p.overall + delta
        if (age <= 26) ov = ov.coerceAtMost(maxOf(p.potential, p.overall))
        ov = ov.coerceIn(30, 99)
        val pot = if (age > 26) ov else maxOf(p.potential, ov)
        return p.copy(age = age, overall = ov, potential = pot, contract = p.contract - 1)
    }

    fun renewAI(p: Player, rng: Random): Player =
        p.copy(contract = rng.nextInt(1, 5), salary = wage(p))

    fun newYoung(t: Team, rng: Random, forceGoalkeeper: Boolean): Player {
        val ov = rng.nextInt(38, 56)
        return Player(
            teamId = t.id,
            name = "${SeedData.initials[rng.nextInt(SeedData.initials.length)]}. " +
                SeedData.surnames[rng.nextInt(SeedData.surnames.size)],
            role = if (forceGoalkeeper) "P" else listOf("D", "C", "A", "D", "C").random(rng),
            age = rng.nextInt(17, 20), overall = ov, morale = 70, salary = 30_000,
            nationality = SeedData.codes[t.nazione] ?: "ITA",
            potential = (ov + rng.nextInt(5, 26)).coerceAtMost(95),
            contract = rng.nextInt(2, 5)
        )
    }
}
