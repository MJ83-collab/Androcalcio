package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.SeedData
import it.androcalcio.data.Team
import it.androcalcio.data.Youth
import kotlin.random.Random

object Scouting {
    fun cost(skill: Int): Long = (150_000L - skill * 1_000L).coerceAtLeast(40_000L)
    const val FOLLOW_COST = 20_000L

    /** Stima del livello: più larga con scout scarso, si stringe a ogni osservazione. */
    fun range(p: Player, skill: Int, observations: Int): IntRange {
        val half = (14 - skill / 10 - observations * 2).coerceAtLeast(2)
        val shift = Random(p.id * 31 + observations).nextInt(-half / 2, half / 2 + 1)
        val c = p.overall + shift
        return (c - half).coerceAtLeast(1)..(c + half).coerceAtMost(99)
    }

    /** Parere sull'adattamento alla rosa, in base al ruolo. */
    fun fit(estimate: Int, role: String, squad: List<Player>): String {
        val slots = when (role) { "P" -> 1; "D" -> 4; "C" -> 4; else -> 3 }
        val sorted = squad.filter { it.role == role }.map { it.overall }.sortedDescending()
        val threshold = sorted.getOrNull(slots - 1) ?: sorted.lastOrNull() ?: 0
        return when {
            estimate >= threshold + 2 -> "Migliorerebbe la rosa"
            estimate >= threshold - 3 -> "Valida alternativa"
            else -> "Non adatto alla rosa"
        }
    }
}

object Youths {
    fun generate(team: Team, season: String, rng: Random = Random.Default): List<Youth> {
        val q = (team.budget / 1_000_000).toInt().coerceIn(1, 45)
        val nat = SeedData.codes[team.nazione] ?: "ITA"
        val roles = listOf("P", "D", "D", "D", "C", "C", "C", "A", "A", "D")
        return roles.map { role ->
            // Allenatore delle giovanili: abilità alta = potenziale più alto (da -4 a +4 circa)
            val vivaio = (team.youthCoachSkill - Staffing.NEUTRAL) / 10
            val pot = (52 + q / 2 + vivaio + rng.nextInt(-8, 25)).coerceIn(45, 95)
            val ov = (pot - rng.nextInt(15, 36)).coerceIn(30, 80)
            Youth(
                teamId = team.id, season = season,
                name = "${SeedData.initials[rng.nextInt(SeedData.initials.length)]}. " +
                    SeedData.surnames[rng.nextInt(SeedData.surnames.size)],
                role = role, age = rng.nextInt(15, 19), overall = ov, potential = pot,
                nationality = if (rng.nextInt(100) < 85) nat else SeedData.codes.values.random(rng)
            )
        }
    }

    fun judge(v: Int): String = when {
        v < 55 -> "Scarso"
        v < 65 -> "Discreto"
        v < 75 -> "Buono"
        v < 85 -> "Ottimo"
        else -> "Fuoriclasse"
    }

    /** Potenziale percepito: l'errore cala con l'abilità dell'allenatore delle giovanili. */
    fun perceived(y: Youth, coachSkill: Int): Int {
        val amp = (100 - coachSkill) / 5
        val noise = Random(y.id * 17 + 3).nextInt(-amp, amp + 1)
        return (y.potential + noise).coerceIn(30, 99)
    }
}
