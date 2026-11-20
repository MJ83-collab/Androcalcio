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
    fun generate(team: Team, season: String, rng: Random = Random.Default, bonusVivaio: Int = 0): List<Youth> {
        val q = (team.budget / 1_000_000).toInt().coerceIn(1, 45)
        val nat = SeedData.codes[team.nazione] ?: "ITA"
        val roles = listOf("P", "D", "D", "D", "C", "C", "C", "A", "A", "D")
        return roles.map { role ->
            // Allenatore delle giovanili: abilità alta = potenziale più alto (da -4 a +4 circa)
            val vivaio = (team.youthCoachSkill - Staffing.NEUTRAL) / 10
            val pot = (52 + q / 2 + vivaio + bonusVivaio + rng.nextInt(-8, 25)).coerceIn(45, 95)
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

/** Aree del mondo in cui gli osservatori possono essere mandati a cercare giocatori svincolati. */
object Missioni {
    data class Area(val nome: String, val nazioni: List<String>, val min: Int, val max: Int)

    val AREE = listOf(
        Area("Europa occidentale", listOf("Francia", "Spagna", "Inghilterra", "Germania"), 62, 80),
        Area("Sud America", listOf("Brasile", "Argentina"), 60, 80),
        Area("Europa orientale", listOf("Polonia", "Serbia", "Croazia"), 55, 74),
        Area("Africa", listOf("Nigeria", "Senegal", "Marocco", "Egitto"), 52, 72),
        Area("Nord America", listOf("Stati Uniti", "Messico", "Canada"), 55, 72),
        Area("Asia e Oceania", listOf("Giappone", "Corea del Sud", "Australia"), 50, 70)
    )

    /** Giornate di missione: un osservatore bravo rientra prima. */
    fun durata(skill: Int): Int = (3 - skill / 45).coerceIn(1, 3)

    /** Giocatori svincolati trovati al rientro: più numerosi e migliori con uno scout bravo. */
    fun trova(area: String, skill: Int, rng: Random): List<Player> {
        val a = AREE.firstOrNull { it.nome == area } ?: return emptyList()
        val n = skill / 40 + rng.nextInt(0, 3)
        return List(n) {
            val ovr = (rng.nextInt(a.min, a.max + 1) + (skill - 50) / 10).coerceIn(40, 90)
            Player(
                teamId = Market.SVINCOLATO_ID,
                name = "${SeedData.initials[rng.nextInt(SeedData.initials.length)]}. " +
                    SeedData.surnames[rng.nextInt(SeedData.surnames.size)],
                role = listOf("P", "D", "C", "A")[rng.nextInt(4)],
                age = rng.nextInt(18, 32),
                overall = ovr,
                morale = 60,
                salary = 0L,
                nationality = a.nazioni[rng.nextInt(a.nazioni.size)],
                potential = (ovr + rng.nextInt(0, 9)).coerceAtMost(95),
                contract = 1
            )
        }
    }
}

