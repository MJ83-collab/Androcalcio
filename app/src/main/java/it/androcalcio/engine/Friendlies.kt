package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import kotlin.random.Random

/**
 * Amichevoli di precampionato della tua squadra: due partite (casa e trasferta).
 * Avversario scelto da te; se è già impegnato in un'altra amichevole non è disponibile.
 * Richiesta accettata con una probabilità che dipende dalla forza relativa.
 * Non entrano in campionato né in coppa.
 */
object Friendlies {
    /** Amichevoli di precampionato prenotabili: servono almeno cinque per entrare in forma. */
    const val COUNT = 5

    fun key(teamId: Long) = "AMICHEVOLE|$teamId"

    /** Partita dello slot (0 o 1): slot 0 in casa, slot 1 in trasferta. */
    fun fixture(myId: Long, oppId: Long, slot: Int, season: String): MatchResult {
        val atHome = slot % 2 == 0
        return MatchResult(
            season = season, round = slot + 1,
            homeTeamId = if (atHome) myId else oppId,
            awayTeamId = if (atHome) oppId else myId,
            homeGoals = 0, awayGoals = 0, league = key(myId)
        )
    }

    /** Probabilità di accettazione: 75% se l'avversario è pari, cala di 1 punto ogni 50 di forza in più. */
    fun acceptChance(mine: Double, theirs: Double): Double =
        (0.75 - (theirs - mine) / 50.0).coerceIn(0.2, 0.9)

    fun accepts(mine: Double, theirs: Double, rng: Random): Boolean =
        rng.nextDouble() < acceptChance(mine, theirs)
}
