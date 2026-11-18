package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.SeedData
import it.androcalcio.data.Staff
import kotlin.random.Random

/** Staff della prima squadra: allenatore (partita e crescita) e preparatore (infortuni). */
object Staffing {
    const val ALLENATORE = "ALL"
    const val PREPARATORE = "PRE"
    const val NEUTRAL = 50          // squadre AI e ruoli vuoti: effetto nullo
    val ROLES = listOf(ALLENATORE, PREPARATORE)

    fun roleName(role: String): String = if (role == ALLENATORE) "Allenatore" else "Preparatore"

    /** Abilità dello staff di una squadra per ruolo; 50 (neutra) se il ruolo è vuoto. */
    fun skill(by: Map<Long, List<Staff>>, teamId: Long, role: String): Int =
        by[teamId]?.firstOrNull { it.role == role }?.skill ?: NEUTRAL

    /** Moltiplicatore sul motore partita: da circa -10% (skill 1) a +10% (skill 99). */
    fun coachFactor(skill: Int): Double = 1.0 + (skill - NEUTRAL) / 500.0

    /** Probabilità per partita che una squadra abbia un infortunato: circa 2.5% (skill 99) - 7.5% (skill 1). */
    fun injuryChance(prep: Int): Double = 0.05 * (1.0 - (prep - NEUTRAL) / 100.0)

    /** Ingaggio annuo in base all'abilità. */
    fun wage(skill: Int): Long = ((skill - 30).coerceAtLeast(5) * 12_000L / 1_000L) * 1_000L

    /** Candidati da assumere: n per ruolo, con abilità e contratto casuali. */
    fun candidates(rng: Random, n: Int = 3): List<Staff> = ROLES.flatMap { role ->
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

/** Infortuni: dopo ogni partita una squadra può perdere un giocatore per 1-3 turni. */
object Medical {
    /** I giocatori disponibili: gli infortunati non scendono in campo. */
    fun available(ps: List<Player>): List<Player> = ps.filter { it.injuredRounds == 0 }

    /** Dopo una partita: gli infortunati recuperano di un turno, poi può esserci un nuovo infortunio. */
    fun afterMatch(ps: List<Player>, prep: Int, rng: Random): List<Player> {
        val healed = ps.map { if (it.injuredRounds > 0) it.copy(injuredRounds = it.injuredRounds - 1) else it }
        if (rng.nextDouble() >= Staffing.injuryChance(prep)) return healed
        val ok = healed.indices.filter { healed[it].injuredRounds == 0 }
        if (ok.isEmpty()) return healed
        val i = ok[rng.nextInt(ok.size)]
        val turni = 1 + rng.nextInt(3)
        return healed.mapIndexed { j, p -> if (j == i) p.copy(injuredRounds = turni) else p }
    }
}
