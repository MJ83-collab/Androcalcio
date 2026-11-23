package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import kotlin.math.pow
import kotlin.random.Random

enum class EventType { KICKOFF, GOAL, SAVE, MISS, YELLOW, HALFTIME, FULLTIME, TACTIC, SUB, OFFSIDE, RED, INJURY }

data class MatchEvent(
    val minute: Int,
    val type: EventType,
    val home: Boolean,
    val text: String,
    val x: Float,   // 0..1, la squadra di casa attacca verso destra
    val y: Float,
    val playerId: Long = 0,
    val assistId: Long = 0    // per i gol: chi ha fatto l'assist (0 = nessuno)
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
    val awayFormation: Formation = Formation.F442,
    val homePlayed: Set<Long> = emptySet(),   // chi è sceso in campo (titolari + subentrati), per la condizione
    val awayPlayed: Set<Long> = emptySet(),
    val homeXI: List<Player> = emptyList(),   // gli 11 in campo a fine partita, per i numeri sul campo
    val awayXI: List<Player> = emptyList(),
    val playedPlayers: List<Player> = emptyList(),  // tutti quelli scesi in campo, titolari e subentrati, per le pagelle
    val playerRatings: Map<Long, Double> = emptyMap(),  // pagella 3..10 per id giocatore
    val infortunati: Set<Long> = emptySet(),  // infortunati durante la partita
    val espulsi: Set<Long> = emptySet()       // espulsi (rosso diretto o doppia ammonizione)
)

/**
 * Motore a eventi discreti: la partita viene calcolata tutta subito e produce
 * una lista di eventi con minuto e posizione; la vista 2D si limita a leggerla.
 */
object MatchEngine {

    // Ritoccato da 0,28 a 0,26 per compensare i rigori e le punizioni in più (gol/partita tornava vicino a 3,0)
    internal const val ATTACK_PER_MINUTE = 0.26

    // Calci piazzati, tarati su medie reali (fonti: CIES, Premier League, Analyst, Fantacalcio):
    /** Rigori: ~0,30 a partita. Serie A 2025/26 ~0,27 (102 in 380 gare); Champions ~0,47; top 5 leghe ~0,36. */
    internal const val PENALTY_PER_MINUTE = 0.0033
    /** Punizioni che finiscono in tiro: ~0,7 a partita. Nel terzo offensivo ne arrivano 3-4 a gara, tirate ~19,5% in Serie A. */
    internal const val SHOT_FREEKICK_PER_MINUTE = 0.0078
    internal const val HOME_BONUS = 1.24
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

    /** Morale sul rendimento: da 0,95 (morale 0) a 1,05 (morale 100); 50 non cambia nulla. */
    internal fun moraleFactor(p: Player): Double = 0.95 + 0.001 * p.morale.coerceIn(0, 100)

    internal fun ratings(ps: List<Player>, f: Formation, coach: Int, en: Map<Long, Double> = emptyMap()): Ratings {
        val k = Staffing.coachFactor(coach)
        fun e(p: Player) = (en[p.id] ?: 1.0) * TacticalRoles.factor(p) * moraleFactor(p)
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
        homeAmp: Ampiezza = Ampiezza.NORMALE, awayAmp: Ampiezza = Ampiezza.NORMALE,
        homeStile: Stile? = null, awayStile: Stile? = null,   // tattica di base dell'allenatore (squadre AI)
        homeTiri: SetPieces = SetPieces.NONE, awayTiri: SetPieces = SetPieces.NONE,
        rng: Random = Random.Default,
        meteoGol: Double = 1.0,
        homeTrap: Boolean = false, awayTrap: Boolean = false
    ): MatchReport = LiveMatch(
        home, homePlayers, away, awayPlayers, homeF, awayF, homeCoach, awayCoach,
        homeMent, awayMent, homeBench, awayBench, rng,
        homePress = homePress, awayPress = awayPress, homeLine = homeLine, awayLine = awayLine, homeAmp = homeAmp, awayAmp = awayAmp,
        homeStile = homeStile, awayStile = awayStile, homeTiri = homeTiri, awayTiri = awayTiri, meteoGol = meteoGol,
        trapH = homeTrap, trapA = awayTrap
    ).also { it.runToEnd() }.report()
}
