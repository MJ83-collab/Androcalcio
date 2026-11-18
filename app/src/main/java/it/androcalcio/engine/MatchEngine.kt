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

    private const val ATTACK_PER_MINUTE = 0.30
    private const val HOME_BONUS = 1.20
    private const val HOME_FINISH = 1.05   // chi gioca in casa finalizza un po' meglio

    private class Ratings(
        val atk: Double, val mid: Double, val def: Double, val gk: Double,
        val stamina: Double, val players: List<Player>
    )

    private fun top(ps: List<Player>, role: String, n: Int, f: (Player) -> Double): Double =
        ps.filter { it.role == role }.map(f).sortedDescending().take(n)
            .average().let { if (it.isNaN()) 50.0 else it }

    /** Sceglie a caso un giocatore, con più probabilità per chi ha l'aggressività alta. */
    private fun aggressive(ps: List<Player>, rng: Random): Player {
        val total = ps.sumOf { it.ag.coerceAtLeast(1) }
        var r = rng.nextInt(total)
        for (p in ps) { r -= p.ag.coerceAtLeast(1); if (r < 0) return p }
        return ps.last()
    }

    private fun ratings(ps: List<Player>, f: Formation, coach: Int): Ratings {
        val k = Staffing.coachFactor(coach)
        return Ratings(
            atk = (top(ps, "A", 3) { Attr.atk(it) } * 0.6 + top(ps, "C", 3) { Attr.atk(it) } * 0.4) * f.atkMod * k,
            mid = top(ps, "C", 4) { Attr.mid(it) } * f.midMod * k,
            def = top(ps, "D", 4) { Attr.def(it) } * f.defMod * k,
            gk = top(ps, "P", 1) { Attr.gk(it) },
            stamina = if (ps.isEmpty()) 60.0 else ps.map { it.re }.average(),
            players = ps
        )
    }

    private fun shooter(ps: List<Player>, rng: Random): Player? {
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
        rng: Random = Random.Default
    ): MatchReport {
        val h = ratings(homePlayers, homeF, homeCoach)
        val a = ratings(awayPlayers, awayF, awayCoach)
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
                // oltre il 65' la squadra meno resistente cala
                val tireH = if (minute > 65) 0.96 + 0.04 * (h.stamina / 90.0).coerceAtMost(1.0) else 1.0
                val tireA = if (minute > 65) 0.96 + 0.04 * (a.stamina / 90.0).coerceAtMost(1.0) else 1.0
                val pNow = h.mid * tireH * HOME_BONUS / (h.mid * tireH * HOME_BONUS + a.mid * tireA)
                val homeAtt = rng.nextDouble() < pNow
                val att = if (homeAtt) h else a
                val dfn = if (homeAtt) a else h
                val team = if (homeAtt) home else away
                val ratio = att.atk / dfn.def
                if (rng.nextDouble() < (0.45 * ratio).coerceIn(0.2, 0.7)) {
                    if (homeAtt) hs++ else asht++
                    val sh = shooter(att.players, rng)
                    val who = sh?.name ?: "Attaccante"
                    val (x, y) = pos(homeAtt)
                    val pGoal = (0.22 * (att.atk / dfn.gk).pow(1.5) * (if (homeAtt) HOME_FINISH else 1.0)).coerceIn(0.08, 0.45)
                    val r = rng.nextDouble()
                    when {
                        r < pGoal -> {
                            if (homeAtt) hg++ else ag++
                            events += MatchEvent(minute, EventType.GOAL, homeAtt,
                                "Gol! $who (${team.shortName})", x, y, sh?.id ?: 0L)
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
                    val p = aggressive(ps, rng)
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
