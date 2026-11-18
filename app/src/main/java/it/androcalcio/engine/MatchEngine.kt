package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import kotlin.math.pow
import kotlin.random.Random

enum class EventType { KICKOFF, GOAL, SAVE, MISS, YELLOW, HALFTIME, FULLTIME, TACTIC, SUB }

data class MatchEvent(
    val minute: Int,
    val type: EventType,
    val home: Boolean,
    val text: String,
    val x: Float,   // 0..1, la squadra di casa attacca verso destra
    val y: Float,
    val playerId: Long = 0
)

data class MatchReport(
    val home: Team,
    val away: Team,
    val homeGoals: Int,
    val awayGoals: Int,
    val homeShots: Int,
    val awayShots: Int,
    val homePossession: Int,
    val events: List<MatchEvent>,
    val homeFormation: Formation = Formation.F442,
    val awayFormation: Formation = Formation.F442
)

/**
 * Motore a eventi discreti: la partita viene calcolata tutta subito e produce
 * una lista di eventi con minuto e posizione; la vista 2D si limita a leggerla.
 */
object MatchEngine {

    internal const val ATTACK_PER_MINUTE = 0.28
    internal const val HOME_BONUS = 1.20
    internal const val HOME_FINISH = 1.05   // chi gioca in casa finalizza un po' meglio

    internal class Ratings(
        val atk: Double, val mid: Double, val def: Double, val gk: Double,
        val stamina: Double, val players: List<Player>
    )

    internal fun top(ps: List<Player>, role: String, n: Int, f: (Player) -> Double): Double =
        ps.filter { it.role == role }.map(f).sortedDescending().take(n)
            .average().let { if (it.isNaN()) 50.0 else it }

    const val MAX_SUBS = 5

    /** Energia persa al minuto dopo il 55': da ~0,0045 (RE bassa) a ~0,002 (RE 99). */
    internal fun drain(p: Player): Double = 0.0045 - 0.0025 * p.re / 99.0

    /** Sceglie a caso un giocatore, con più probabilità per chi ha l'aggressività alta. */
    internal fun aggressive(ps: List<Player>, rng: Random): Player {
        val total = ps.sumOf { it.ag.coerceAtLeast(1) }
        var r = rng.nextInt(total)
        for (p in ps) { r -= p.ag.coerceAtLeast(1); if (r < 0) return p }
        return ps.last()
    }

    internal fun ratings(ps: List<Player>, f: Formation, coach: Int, en: Map<Long, Double> = emptyMap()): Ratings {
        val k = Staffing.coachFactor(coach)
        fun e(p: Player) = en[p.id] ?: 1.0
        return Ratings(
            atk = (top(ps, "A", 3) { Attr.atk(it) * e(it) } * 0.6 + top(ps, "C", 3) { Attr.atk(it) * e(it) } * 0.4) * f.atkMod * k,
            mid = top(ps, "C", 4) { Attr.mid(it) * e(it) } * f.midMod * k,
            def = top(ps, "D", 4) { Attr.def(it) * e(it) } * f.defMod * k,
            gk = top(ps, "P", 1) { Attr.gk(it) * e(it) },
            stamina = if (ps.isEmpty()) 60.0 else ps.map { it.re }.average(),
            players = ps
        )
    }

    internal fun shooter(ps: List<Player>, rng: Random): Player? {
        val weights = ps.map {
            when (it.role) {
                "A" -> (it.fi + it.ti) / 2.0 * 3.0
                "C" -> (it.fi + it.ti + it.pa) / 3.0 * 1.5
                "D" -> it.ti * 0.4
                else -> 0.0
            }
        }
        val total = weights.sum()
        if (total <= 0.0) return null
        var r = rng.nextDouble() * total
        for (i in ps.indices) {
            r -= weights[i]
            if (r <= 0.0) return ps[i]
        }
        return ps.lastOrNull()
    }

    fun simulate(
        home: Team, homePlayers: List<Player>,
        away: Team, awayPlayers: List<Player>,
        homeF: Formation = Formation.F442,
        awayF: Formation = Formation.F442,
        homeCoach: Int = Staffing.NEUTRAL,
        awayCoach: Int = Staffing.NEUTRAL,
        homeMent: Mentality? = null,        // null = decide il motore (squadre AI)
        awayMent: Mentality? = null,
        homeBench: List<Player>? = null,    // null = titolari e panchina scelti dall'allenatore
        awayBench: List<Player>? = null,
        homePress: Pressing? = null, awayPress: Pressing? = null,
        homeLine: DefLine? = null, awayLine: DefLine? = null,
        rng: Random = Random.Default
    ): MatchReport = LiveMatch(
        home, homePlayers, away, awayPlayers, homeF, awayF, homeCoach, awayCoach,
        homeMent, awayMent, homeBench, awayBench, rng,
        homePress = homePress, awayPress = awayPress, homeLine = homeLine, awayLine = awayLine
    ).also { it.runToEnd() }.report()
}
