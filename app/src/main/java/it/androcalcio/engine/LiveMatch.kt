package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Partita giocata minuto per minuto: si può fermare, fare cambi e cambiare mentalità,
 * poi riprendere. Quello che è già successo resta. MatchEngine.simulate la gioca tutta di fila.
 * manualHome / manualAway: i cambi di quella squadra li decide l'utente (niente cambi automatici).
 */
class LiveMatch(
    val home: Team, homePlayers: List<Player>,
    val away: Team, awayPlayers: List<Player>,
    val homeF: Formation, val awayF: Formation,
    private val homeCoach: Int, private val awayCoach: Int,
    private var homeMent: Mentality?, private var awayMent: Mentality?,
    homeBench: List<Player>?, awayBench: List<Player>?,
    private val rng: Random = Random.Default,
    var manualHome: Boolean = false,
    var manualAway: Boolean = false,
    homePress: Pressing? = null, awayPress: Pressing? = null,   // null = sceglie l'AI
    homeLine: DefLine? = null, awayLine: DefLine? = null
) {
    private fun split(ps: List<Player>, bench: List<Player>?, f: Formation): Pair<MutableList<Player>, MutableList<Player>> =
        if (bench != null) ps.toMutableList() to bench.toMutableList()
        else Lineup.coachPick(ps, f).let { xi -> xi.toMutableList() to ps.filter { it !in xi }.toMutableList() }

    val xiH: MutableList<Player>; val benchH: MutableList<Player>
    val xiA: MutableList<Player>; val benchA: MutableList<Player>
    init {
        split(homePlayers, homeBench, homeF).let { xiH = it.first; benchH = it.second }
        split(awayPlayers, awayBench, awayF).let { xiA = it.first; benchA = it.second }
    }
    // Chi è sceso in campo (titolari + chi è entrato), per il calo di condizione a fine partita
    private val playedH = xiH.map { it.id }.toMutableSet()
    private val playedA = xiA.map { it.id }.toMutableSet()
    // Pagelle: partono da 6, salgono con i gol, scendono con i cartellini
    private val rated = HashMap<Long, Player>().apply { (xiH + xiA).forEach { put(it.id, it) } }
    private val pRatings = HashMap<Long, Double>().apply { rated.keys.forEach { put(it, 6.0) } }
    private fun bump(p: Player, d: Double) { pRatings[p.id] = (pRatings[p.id] ?: 6.0) + d }

    private val energy = HashMap<Long, Double>()
    var subsH = 0; private set
    var subsA = 0; private set
    private var h = MatchEngine.ratings(xiH, homeF, homeCoach)
    private var a = MatchEngine.ratings(xiA, awayF, awayCoach)
    private val pHome = h.mid * MatchEngine.HOME_BONUS / (h.mid * MatchEngine.HOME_BONUS + a.mid)

    val events = mutableListOf(MatchEvent(0, EventType.KICKOFF, true, "Calcio d'inizio", 0.5f, 0.5f))
    var minute = 0; private set
    var hg = 0; private set
    var ag = 0; private set
    private var hs = 0; private var asht = 0
    val finished get() = minute >= 90

    // Mentalità iniziale delle squadre AI: in base al rapporto di forza
    private fun startMent(own: MatchEngine.Ratings, opp: MatchEngine.Ratings, bonus: Double): Mentality {
        val q = (own.atk + own.mid + own.def) * bonus / (opp.atk + opp.mid + opp.def)
        return when { q < 0.93 -> Mentality.DIFENSIVA; q > 1.07 -> Mentality.OFFENSIVA; else -> Mentality.EQUILIBRATA }
    }
    private val baseH = startMent(h, a, MatchEngine.HOME_BONUS)
    private val baseA = startMent(a, h, 1.0 / MatchEngine.HOME_BONUS)
    var mH = homeMent ?: baseH; private set
    var mA = awayMent ?: baseA; private set

    fun energyOf(p: Player): Double = energy[p.id] ?: 1.0

    // Pressing e linea delle squadre AI, rispetto all'avversario:
    // pressing alto se hanno più resistenza (RE), linea alta se i difensori sono più veloci (VE) degli attaccanti avversari
    private fun avg(ps: List<Player>, role: String?, f: (Player) -> Int): Double =
        ps.filter { if (role == null) it.role != "P" else it.role == role }.map(f).average().let { if (it.isNaN()) 60.0 else it }
    private fun aiPress(own: List<Player>, opp: List<Player>): Pressing {
        val d = avg(own, null) { it.re } - avg(opp, null) { it.re }
        return when { d > 4 -> Pressing.ALTO; d < -4 -> Pressing.BASSO; else -> Pressing.MEDIO }
    }
    private fun aiLine(own: List<Player>, opp: List<Player>): DefLine {
        val d = avg(own, "D") { it.ve } - avg(opp, "A") { it.ve }
        return when { d > 4 -> DefLine.ALTA; d < -4 -> DefLine.BASSA; else -> DefLine.MEDIA }
    }
    var pressH = homePress ?: aiPress(xiH, xiA); private set
    var pressA = awayPress ?: aiPress(xiA, xiH); private set
    var lineH = homeLine ?: aiLine(xiH, xiA); private set
    var lineA = awayLine ?: aiLine(xiA, xiH); private set

    fun setPressing(homeSide: Boolean, p: Pressing) { if (homeSide) pressH = p else pressA = p }
    fun setLine(homeSide: Boolean, l: DefLine) { if (homeSide) lineH = l else lineA = l }

    /** Con la linea alta, gli attaccanti avversari veloci trovano più spazio alle spalle. */
    private fun speedPenalty(line: DefLine, attackers: List<Player>): Double {
        if (line != DefLine.ALTA) return 1.0
        val ve = attackers.filter { it.role == "A" }.map { it.ve }.average().let { if (it.isNaN()) 60.0 else it }
        return 1.0 - ((ve - 65).coerceAtLeast(0.0) / 400.0)
    }

    /** Mentalità scelta dall'utente a partita in corso. */
    fun setMentality(homeSide: Boolean, m: Mentality) {
        if (homeSide) { homeMent = m; mH = m } else { awayMent = m; mA = m }
    }

    // Nel finale le squadre AI cambiano atteggiamento in base al punteggio
    private fun live(fixed: Mentality?, base: Mentality, mine: Int, theirs: Int): Mentality = when {
        fixed != null -> fixed
        minute > 70 && mine < theirs -> Mentality.OFFENSIVA
        minute > 75 && mine - theirs == 1 -> Mentality.DIFENSIVA
        else -> base
    }

    private fun refresh() {
        h = MatchEngine.ratings(xiH, homeF, homeCoach, energy)
        a = MatchEngine.ratings(xiA, awayF, awayCoach, energy)
    }

    private fun doSub(homeSide: Boolean, out: Player, inP: Player) {
        val xi = if (homeSide) xiH else xiA
        val bench = if (homeSide) benchH else benchA
        xi[xi.indexOf(out)] = inP; bench -= inP
        (if (homeSide) playedH else playedA) += inP.id
        rated[inP.id] = inP; pRatings.putIfAbsent(inP.id, 6.0)
        if (homeSide) subsH++ else subsA++
        val t = if (homeSide) home else away
        events += MatchEvent(minute, EventType.SUB, homeSide,
            "Sostituzione (${t.shortName}): ${inP.name} per ${out.name}", 0.5f, 0.5f)
        refresh()
    }

    /** Cambio deciso dall'utente. Restituisce false se non è possibile. */
    fun substitute(homeSide: Boolean, out: Player, inP: Player): Boolean {
        val xi = if (homeSide) xiH else xiA
        val bench = if (homeSide) benchH else benchA
        val used = if (homeSide) subsH else subsA
        if (finished || used >= MatchEngine.MAX_SUBS || out !in xi || inP !in bench) return false
        doSub(homeSide, out, inP)
        return true
    }

    // Cambio automatico: esce il più stanco che ha un sostituto, entra il migliore dello stesso ruolo
    // (se la squadra perde dopo il 60', meglio un attaccante al posto di un difensore o centrocampista)
    private fun autoSub(homeSide: Boolean, losing: Boolean): Boolean {
        val xi = if (homeSide) xiH else xiA
        val bench = if (homeSide) benchH else benchA
        for (tired in xi.filter { it.role != "P" }.sortedBy { energyOf(it) }) {
            if (energyOf(tired) > 0.965 && !(losing && minute > 60)) return false
            val wantAtk = losing && minute > 60 && tired.role != "A"
            val inP = (if (wantAtk) bench.filter { it.role == "A" }.maxByOrNull { Attr.atk(it) } else null)
                ?: bench.filter { it.role == tired.role }.maxByOrNull { it.overall }
                ?: continue
            doSub(homeSide, tired, inP)
            return true
        }
        return false
    }

    private fun pos(homeAttacks: Boolean): Pair<Float, Float> {
        val x = 0.65f + rng.nextFloat() * 0.3f
        return (if (homeAttacks) x else 1f - x) to (0.2f + rng.nextFloat() * 0.6f)
    }

    /** Gioca un minuto. */
    fun step() {
        if (finished) return
        minute++
        if (minute > 55) {
            xiH.forEach { p -> energy[p.id] = (energyOf(p) - MatchEngine.drain(p) * pressH.drain).coerceAtLeast(0.7) }
            xiA.forEach { p -> energy[p.id] = (energyOf(p) - MatchEngine.drain(p) * pressA.drain).coerceAtLeast(0.7) }
            if (minute in 56..85) {
                if (!manualHome && subsH < MatchEngine.MAX_SUBS && rng.nextDouble() < 0.15) autoSub(true, hg < ag)
                if (!manualAway && subsA < MatchEngine.MAX_SUBS && rng.nextDouble() < 0.15) autoSub(false, ag < hg)
            }
            refresh()
        }
        val nH = live(homeMent, baseH, hg, ag)
        val nA = live(awayMent, baseA, ag, hg)
        if (nH != mH && minute > 70) events += MatchEvent(minute, EventType.TACTIC, true,
            "${home.shortName} passa a mentalità ${nH.label.lowercase()}", 0.5f, 0.5f)
        if (nA != mA && minute > 70) events += MatchEvent(minute, EventType.TACTIC, false,
            "${away.shortName} passa a mentalità ${nA.label.lowercase()}", 0.5f, 0.5f)
        mH = nH; mA = nA
        if (rng.nextDouble() < MatchEngine.ATTACK_PER_MINUTE * (mH.tempo + mA.tempo) / 2) {
            val ph = h.mid * MatchEngine.HOME_BONUS * pressH.poss * lineH.poss
            val pNow = ph / (ph + a.mid * pressA.poss * lineA.poss)
            val homeAtt = rng.nextDouble() < pNow
            val att = if (homeAtt) h else a
            val dfn = if (homeAtt) a else h
            val team = if (homeAtt) home else away
            val mAtt = if (homeAtt) mH else mA
            val mDef = if (homeAtt) mA else mH
            val lAtt = if (homeAtt) lineH else lineA
            val lDef = if (homeAtt) lineA else lineH
            val defPen = speedPenalty(lDef, if (homeAtt) xiH else xiA)
            val ratio = att.atk * mAtt.atk * lAtt.atk / (dfn.def * mDef.def * lDef.def * defPen)
            if (rng.nextDouble() < (0.45 * ratio).coerceIn(0.2, 0.7)) {
                if (homeAtt) hs++ else asht++
                val sh = MatchEngine.shooter(att.players, rng)
                val who = sh?.name ?: "Attaccante"
                val (x, y) = pos(homeAtt)
                val pGoal = (0.22 * (att.atk / dfn.gk).pow(1.5) *
                    (if (homeAtt) MatchEngine.HOME_FINISH else 1.0)).coerceIn(0.08, 0.45)
                val r = rng.nextDouble()
                when {
                    r < pGoal -> {
                        if (homeAtt) hg++ else ag++
                        sh?.let { bump(it, 1.0) }
                        events += MatchEvent(minute, EventType.GOAL, homeAtt,
                            "Gol! $who (${team.shortName})", x, y, sh?.id ?: 0L)
                    }
                    r < pGoal + (1 - pGoal) * 0.4 -> events += MatchEvent(
                        minute, EventType.SAVE, homeAtt, "Tiro di $who, para il portiere", x, y)
                    else -> events += MatchEvent(minute, EventType.MISS, homeAtt, "Tiro di $who fuori", x, y)
                }
            }
        }
        // Calci piazzati: rigore raro e in area, punizione più frequente dal limite
        if (rng.nextDouble() < MatchEngine.PENALTY_PER_MINUTE) setPiece(penalty = true)
        else if (rng.nextDouble() < MatchEngine.SHOT_FREEKICK_PER_MINUTE) setPiece(penalty = false)
        if (rng.nextDouble() < 0.018) {
            val side = rng.nextBoolean()
            val ps = (if (side) xiH else xiA).filter { it.role != "P" }
            if (ps.isNotEmpty()) {
                val p = MatchEngine.aggressive(ps, rng)
                val t = if (side) home else away
                bump(p, -0.5)
                events += MatchEvent(minute, EventType.YELLOW, side, "Ammonito ${p.name} (${t.shortName})", 0.5f, 0.5f)
            }
        }
        if (minute == 45) events += MatchEvent(45, EventType.HALFTIME, true, "Fine primo tempo", 0.5f, 0.5f)
        if (minute == 90) events += MatchEvent(90, EventType.FULLTIME, true, "Fine partita $hg-$ag", 0.5f, 0.5f)
    }

    /** Rigore o punizione per una squadra a caso: tira il giocatore più adatto tra quelli in campo. */
    private fun setPiece(penalty: Boolean) {
        val homeAtt = rng.nextBoolean()
        val takers = (if (homeAtt) xiH else xiA).filter { it.role != "P" }
        val taker = MatchEngine.shooter(takers, rng) ?: return
        val team = if (homeAtt) home else away
        val (x, y) = if (penalty) Pair(if (homeAtt) 0.9f else 0.1f, 0.5f) else pos(homeAtt)
        val kind = if (penalty) "Rigore" else "Punizione"
        if (homeAtt) hs++ else asht++
        val pGoal = if (penalty) 0.70 + 0.2 * taker.fi / 99.0 else 0.04 + 0.08 * taker.ti / 99.0
        if (rng.nextDouble() < pGoal) {
            if (homeAtt) hg++ else ag++
            bump(taker, 1.0)
            events += MatchEvent(minute, EventType.GOAL, homeAtt,
                "Gol su $kind! ${taker.name} (${team.shortName})", x, y, taker.id)
        } else if (rng.nextBoolean()) {
            events += MatchEvent(minute, EventType.SAVE, homeAtt, "$kind di ${taker.name}, para il portiere", x, y)
        } else {
            events += MatchEvent(minute, EventType.MISS, homeAtt, "$kind di ${taker.name} fuori", x, y)
        }
    }

    /** Pagelle finali: 3 minimo, 10 massimo, con un decimale. */
    private fun finalRatings(): Map<Long, Double> =
        pRatings.mapValues { (_, v) -> (v.coerceIn(3.0, 10.0) * 10).roundToInt() / 10.0 }

    /** Gioca fino alla fine; i cambi rimasti li fa l'allenatore. */
    fun runToEnd() {
        manualHome = false; manualAway = false
        while (!finished) step()
    }

    fun report() = MatchReport(home, away, hg, ag, hs, asht, (pHome * 100).toInt(), events.toList(), homeF, awayF,
        playedH.toSet(), playedA.toSet(), xiH.toList(), xiA.toList(),
        rated.values.toList(), finalRatings())
}
