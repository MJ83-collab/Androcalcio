package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import kotlin.random.Random

/**
 * Amichevoli di precampionato della tua squadra: due partite (casa e trasferta)
 * contro squadre scelte a caso tra tutte le nazioni. Non entrano in campionato né in coppa.
 */
object Friendlies {
    const val COUNT = 2

    fun key(teamId: Long) = "AMICHEVOLE|$teamId"

    fun fixtures(me: Team, pool: List<Team>, season: String, rng: Random): List<MatchResult> {
        val opponents = pool.filter { it.id != me.id }.shuffled(rng).take(COUNT)
        val k = key(me.id)
        return opponents.mapIndexed { i, o ->
            val atHome = i % 2 == 0
            MatchResult(
                season = season, round = i + 1,
                homeTeamId = if (atHome) me.id else o.id,
                awayTeamId = if (atHome) o.id else me.id,
                homeGoals = 0, awayGoals = 0, league = k
            )
        }
    }
}
