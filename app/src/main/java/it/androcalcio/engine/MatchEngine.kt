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

    const val MAX_SUBS = 5

    /** Energia persa al minuto dopo il 55': da ~0,0045 (RE bassa) a ~0,002 (RE 99). */
    private fun drain(p: Player): Double = 0.0045 - 0.0025 * p.re / 99.0

    /** Sceglie a caso un giocatore, con più probabilità per chi ha l'aggressività alta. */
    private fun aggressive(ps: List<Player>, rng: Random): Player {
        val total = ps.sumOf { it.ag.coerceAtLeast(1) }
        var r = rng.nextInt(total)
        for (p in ps) { r -= p.ag.coerceAtLeast(1); if (r < 0) return p }
        return ps.last()
    }

    private fun ratings(ps: List<Player>, f: Formation, coach: Int, en: Map<Long, Double> = emptyMap()): Ratings {
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
        homeMent: Mentality? = null,        // null = decide il motore (squadre AI)
        awayMent: Mentality? = null,
        homeBench: List<Player>? = null,    // null = titolari e panchina scelti dall'allenatore
        awayBench: List<Player>? = null,
        rng: Random = Random.Default
    ): MatchReport {
        // Titolari e panchina: se arriva la rosa intera, l'allenatore sceglie gli 11
        fun split(ps: List<Player>, bench: List<Player>?, f: Formation): Pair<MutableList<Player>, MutableList<Player>> =
            if (bench != null) ps.toMutableList() to bench.toMutableList()
            else Lineup.coachPick(ps, f).let { xi -> xi.toMutableList() to ps.filter { it !in xi }.toMutableList() }
        val (xiH, benchH) = split(homePlayers, homeBench, homeF)
        val (xiA, benchA) = split(awayPlayers, awayBench, awayF)
        val energy = HashMap<Long, Double>()
        var subsH = 0; var subsA = 0
        var h = ratings(xiH, homeF, homeCoach)
        var a = ratings(xiA, awayF, awayCoach)
        val pHome = h.mid * HOME_BONUS / (h.mid * HOME_BONUS + a.mid)

        val events = mutableListOf<MatchEvent>()
        var hg = 0; var ag = 0; var hs = 0; var asht = 0

        fun pos(homeAttacks: Boolean): Pair<Float, Float> {
            val x = 0.65f + rng.nextFloat() * 0.3f
            return (if (homeAttacks) x else 1f - x) to (0.2f + rng.nextFloat() * 0.6f)
        }

        events += MatchEvent(0, EventType.KICKOFF, true, "Calcio d'inizio", 0.5f, 0.5f)

        // Mentalità iniziale delle squadre AI: in base al rapporto di forza
        fun startMent(own: Ratings, opp: Ratings, bonus: Double): Mentality {
            val q = (own.atk + own.mid + own.def) * bonus / (opp.atk + opp.mid + opp.def)
            return when { q < 0.93 -> Mentality.DIFENSIVA; q > 1.07 -> Mentality.OFFENSIVA; else -> Mentality.EQUILIBRATA }
        }
        val baseH = startMent(h, a, HOME_BONUS)
        val baseA = startMent(a, h, 1.0 / HOME_BONUS)
        var mH = homeMent ?: baseH
        var mA = awayMent ?: baseA

        // Nel finale le squadre AI cambiano atteggiamento in base al punteggio
        fun live(fixed: Mentality?, base: Mentality, mine: Int, theirs: Int, minute: Int): Mentality = when {
            fixed != null -> fixed
            minute > 70 && mine < theirs -> Mentality.OFFENSIVA
            minute > 75 && mine - theirs == 1 -> Mentality.DIFENSIVA
            else -> base
        }

        // Cambio: esce il più stanco, entra il migliore della panchina nello stesso ruolo
        // (se la squadra perde dopo il 60', meglio un attaccante al posto di un difensore o centrocampista)
        fun trySub(xi: MutableList<Player>, bench: MutableList<Player>, losing: Boolean, minute: Int, homeSide: Boolean, t: Team): Boolean {
            val tired = xi.filter { it.role != "P" }.minByOrNull { energy[it.id] ?: 1.0 } ?: return false
            if ((energy[tired.id] ?: 1.0) > 0.93 && !(losing && minute > 60)) return false
            val wantAtk = losing && minute > 60 && tired.role != "A"
            val inP = (if (wantAtk) bench.filter { it.role == "A" }.maxByOrNull { Attr.atk(it) } else null)
                ?: bench.filter { it.role == tired.role }.maxByOrNull { it.overall }
                ?: return false
            xi[xi.indexOf(tired)] = inP; bench -= inP
            events += MatchEvent(minute, EventType.SUB, homeSide,
                "Sostituzione (${t.shortName}): ${inP.name} per ${tired.name}", 0.5f, 0.5f)
            return true
        }

        for (minute in 1..90) {
            if (minute > 55) {
                (xiH + xiA).forEach { p -> energy[p.id] = ((energy[p.id] ?: 1.0) - drain(p)).coerceAtLeast(0.7) }
                if (minute in 56..85) {
                    if (subsH < MAX_SUBS && rng.nextDouble() < 0.12 &&
                        trySub(xiH, benchH, hg < ag, minute, true, home)) subsH++
                    if (subsA < MAX_SUBS && rng.nextDouble() < 0.12 &&
                        trySub(xiA, benchA, ag < hg, minute, false, away)) subsA++
                }
                h = ratings(xiH, homeF, homeCoach, energy)
                a = ratings(xiA, awayF, awayCoach, energy)
            }
            val nH = live(homeMent, baseH, hg, ag, minute)
            val nA = live(awayMent, baseA, ag, hg, minute)
            if (nH != mH && minute > 70) events += MatchEvent(minute, EventType.TACTIC, true,
                "${home.shortName} passa a mentalità ${nH.label.lowercase()}", 0.5f, 0.5f)
            if (nA != mA && minute > 70) events += MatchEvent(minute, EventType.TACTIC, false,
                "${away.shortName} passa a mentalità ${nA.label.lowercase()}", 0.5f, 0.5f)
            mH = nH; mA = nA
            if (rng.nextDouble() < ATTACK_PER_MINUTE * (mH.tempo + mA.tempo) / 2) {
                val pNow = h.mid * HOME_BONUS / (h.mid * HOME_BONUS + a.mid)
                val homeAtt = rng.nextDouble() < pNow
                val att = if (homeAtt) h else a
                val dfn = if (homeAtt) a else h
                val team = if (homeAtt) home else away
                val mAtt = if (homeAtt) mH else mA
                val mDef = if (homeAtt) mA else mH
                val ratio = att.atk * mAtt.atk / (dfn.def * mDef.def)
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
                val ps = (if (side) xiH else xiA).filter { it.role != "P" }
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
