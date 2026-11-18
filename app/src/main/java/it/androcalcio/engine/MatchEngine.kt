package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import kotlin.math.pow
import kotlin.random.Random

enum class EventType { KICKOFF, GOAL, SAVE, MISS, YELLOW, HALFTIME, FULLTIME }

data class MatchEvent(
    val minute: Int,
    val type: EventType,
    val home: Boolean,
    val text: String,
    val x: Float,   // 0..1, la squadra di casa attacca verso destra
    val y: Float
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

    private const val ATTACK_PER_MINUTE = 0.30
    private const val HOME_BONUS = 1.05

    private class Ratings(
        val atk: Double, val mid: Double, val def: Double, val gk: Double,
        val players: List<Player>
    )

    private fun top(ps: List<Player>, role: String, n: Int): Double =
        ps.filter { it.role == role }.sortedByDescending { it.overall }.take(n)
            .map { it.overall.toDouble() }.average().let { if (it.isNaN()) 50.0 else it }

    private fun ratings(ps: List<Player>, f: Formation) = Ratings(
        atk = (top(ps, "A", 3) * 0.6 + top(ps, "C", 3) * 0.4) * f.atkMod,
        mid = top(ps, "C", 4) * f.midMod,
        def = top(ps, "D", 4) * f.defMod,
        gk = top(ps, "P", 1),
        players = ps
    )

    private fun shooter(ps: List<Player>, rng: Random): Player? {
        val weights = ps.map {
            when (it.role) {
                "A" -> it.overall * 3.0
                "C" -> it.overall * 1.5
                "D" -> it.overall * 0.4
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
        rng: Random = Random.Default
    ): MatchReport {
        val h = ratings(homePlayers, homeF)
        val a = ratings(awayPlayers, awayF)
        val pHome = h.mid * HOME_BONUS / (h.mid * HOME_BONUS + a.mid)

        val events = mutableListOf<MatchEvent>()
        var hg = 0; var ag = 0; var hs = 0; var asht = 0

        fun pos(homeAttacks: Boolean): Pair<Float, Float> {
            val x = 0.65f + rng.nextFloat() * 0.3f
            return (if (homeAttacks) x else 1f - x) to (0.2f + rng.nextFloat() * 0.6f)
        }

        events += MatchEvent(0, EventType.KICKOFF, true, "Calcio d'inizio", 0.5f, 0.5f)

        for (minute in 1..90) {
            if (rng.nextDouble() < ATTACK_PER_MINUTE) {
                val homeAtt = rng.nextDouble() < pHome
                val att = if (homeAtt) h else a
                val dfn = if (homeAtt) a else h
                val team = if (homeAtt) home else away
                val ratio = att.atk / dfn.def
                if (rng.nextDouble() < (0.45 * ratio).coerceIn(0.2, 0.7)) {
                    if (homeAtt) hs++ else asht++
                    val who = shooter(att.players, rng)?.name ?: "Attaccante"
                    val (x, y) = pos(homeAtt)
                    val pGoal = (0.22 * (att.atk / dfn.gk).pow(1.5)).coerceIn(0.08, 0.45)
                    val r = rng.nextDouble()
                    when {
                        r < pGoal -> {
                            if (homeAtt) hg++ else ag++
                            events += MatchEvent(minute, EventType.GOAL, homeAtt,
                                "Gol! $who (${team.shortName})", x, y)
                        }
                        r < pGoal + (1 - pGoal) * 0.4 -> events += MatchEvent(
                            minute, EventType.SAVE, homeAtt,
                            "Tiro di $who, para il portiere", x, y)
                        else -> events += MatchEvent(
                            minute, EventType.MISS, homeAtt,
                            "Tiro di $who fuori", x, y)
                    }
                }
            }
            if (rng.nextDouble() < 0.018) {
                val side = rng.nextBoolean()
                val ps = (if (side) homePlayers else awayPlayers).filter { it.role != "P" }
                if (ps.isNotEmpty()) {
                    val p = ps[rng.nextInt(ps.size)]
                    val t = if (side) home else away
                    events += MatchEvent(minute, EventType.YELLOW, side,
                        "Ammonito ${p.name} (${t.shortName})", 0.5f, 0.5f)
                }
            }
            if (minute == 45) events += MatchEvent(45, EventType.HALFTIME, true,
                "Fine primo tempo", 0.5f, 0.5f)
        }
        events += MatchEvent(90, EventType.FULLTIME, true, "Fine partita $hg-$ag", 0.5f, 0.5f)

        return MatchReport(home, away, hg, ag, hs, asht, (pHome * 100).toInt(), events, homeF, awayF)
    }
}
