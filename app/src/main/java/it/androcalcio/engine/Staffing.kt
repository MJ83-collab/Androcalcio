package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.SeedData
import it.androcalcio.data.Staff
import kotlin.random.Random

/** Staff della prima squadra: allenatore (partita e crescita) e preparatore (infortuni). */
object Staffing {
    const val ALLENATORE = "ALL"
    const val PREPARATORE = "PRE"
    const val GIOVANILI = "GIO"     // allenatore delle giovanili: aggiorna Team.youthCoachSkill
    const val SCOUT = "SCO"         // scout: aggiorna Team.scoutSkill
    const val NEUTRAL = 50          // squadre AI e ruoli vuoti: effetto nullo
    const val VUOTO = 40            // abilità del club quando il ruolo resta scoperto
    val ROLES = listOf(ALLENATORE, PREPARATORE, GIOVANILI, SCOUT)

    fun roleName(role: String): String = when (role) {
        ALLENATORE -> "Allenatore"
        PREPARATORE -> "Preparatore"
        GIOVANILI -> "Allenatore delle giovanili"
        else -> "Scout"
    }

    /** Abilità dello staff di una squadra per ruolo; 50 (neutra) se il ruolo è vuoto. */
    fun skill(by: Map<Long, List<Staff>>, teamId: Long, role: String): Int =
        by[teamId]?.firstOrNull { it.role == role }?.skill ?: NEUTRAL

    /** Moltiplicatore sul motore partita: da circa -10% (skill 1) a +10% (skill 99). */
    fun coachFactor(skill: Int): Double = 1.0 + (skill - NEUTRAL) / 500.0

    /** Probabilità per partita che una squadra abbia un infortunato: circa 2.5% (skill 99) - 7.5% (skill 1). */
    fun injuryChance(prep: Int): Double = 0.05 * (1.0 - (prep - NEUTRAL) / 100.0)

    /** Ingaggio annuo in base all'abilità. */
    fun wage(skill: Int): Long = ((skill - 30).coerceAtLeast(5) * 12_000L / 1_000L) * 1_000L

    /**
     * Staff di una squadra AI per i ruoli mancanti (solo allenatore e preparatore).
     * Abilità legata alla forza media della rosa: massima serie ~57, seconda ~48 (±10).
     */
    fun aiStaff(teamId: Long, avgOverall: Double, missing: List<String>, rng: Random): List<Staff> =
        missing.map { role ->
            val sk = (avgOverall - 25 + rng.nextInt(-10, 11)).toInt().coerceIn(30, 90)
            Staff(
                teamId = teamId, role = role,
                name = "${SeedData.initials[rng.nextInt(SeedData.initials.length)]}. " +
                    SeedData.surnames[rng.nextInt(SeedData.surnames.size)],
                skill = sk, salary = wage(sk), contract = rng.nextInt(1, 5)
            )
        }

    /** Candidati da assumere: n per ruolo, con abilità e contratto casuali. */
    fun candidates(rng: Random, n: Int = 2): List<Staff> = ROLES.flatMap { role ->
        (1..n).map {
            val sk = rng.nextInt(35, 91)
            Staff(
                teamId = 0, role = role,
                name = "${SeedData.initials[rng.nextInt(SeedData.initials.length)]}. " +
                    SeedData.surnames[rng.nextInt(SeedData.surnames.size)],
                skill = sk, salary = wage(sk), contract = rng.nextInt(1, 5)
            )
        }
    }
}

/** Infortuni e condizione fisica: dopo ogni partita una squadra può perdere un giocatore per 1-3 turni. */
object Medical {
    /** I giocatori disponibili: gli infortunati non scendono in campo. */
    fun available(ps: List<Player>): List<Player> = ps.filter { it.injuredRounds == 0 }

    /** Calo di condizione per chi ha giocato: più alto per chi ha poca resistenza (RE) o è più anziano. */
    private fun conditionDrop(p: Player): Int = (14 - p.re / 12 + (p.age - 24).coerceAtLeast(0) / 4).coerceIn(6, 20)

    /** Recupero per chi non ha giocato: un buon preparatore lo accelera. */
    private fun conditionGain(prep: Int): Int = (10 + (prep - Staffing.NEUTRAL) / 6).coerceIn(6, 18)

    /**
     * Dopo una partita: chi è sceso in campo (playedIds) perde condizione, gli altri recuperano;
     * gli infortunati recuperano di un turno, poi può esserci un nuovo infortunio.
     * Con un piano di allenamento (solo la tua squadra) si applicano crescita, fatica e rischio extra.
     */
    fun afterMatch(
        ps: List<Player>, playedIds: Set<Long>, prep: Int, rng: Random,
        plan: TrainingPlan? = null, coach: Int = Staffing.NEUTRAL
    ): List<Player> {
        val healed0 = ps.map {
            val fo = (it.fo + rng.nextInt(-4, 5)).coerceIn(40, 99)   // la forma oscilla di partita in partita
            val cond = if (it.id in playedIds) (it.condition - conditionDrop(it)).coerceAtLeast(35)
                else (it.condition + conditionGain(prep)).coerceAtMost(100)
            val base = it.copy(fo = fo, condition = cond)
            if (it.injuredRounds > 0) base.copy(injuredRounds = it.injuredRounds - 1) else base
        }
        val healed = if (plan == null) healed0 else healed0.map { Training.apply(it, plan, coach, rng) }
        if (rng.nextDouble() >= Staffing.injuryChance(prep) * (plan?.injuryFactor() ?: 1.0)) return healed
        val ok = healed.indices.filter { healed[it].injuredRounds == 0 }
        if (ok.isEmpty()) return healed
        val i = ok[rng.nextInt(ok.size)]
        val turni = 1 + rng.nextInt(3)
        return healed.mapIndexed { j, p -> if (j == i) p.copy(injuredRounds = turni) else p }
    }
}
