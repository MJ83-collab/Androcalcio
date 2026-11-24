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
private const val ANGOLO_QUOTA = 0.4
/**
 * Rigore in partita: probabilità di segnare RIGORE_BASE + RIGORE_FI * fi / 99 (più il bonus del Cinico).
 * Era 0,70 + 0,2: la conversione misurata era 84,8%, sopra le medie reali (75-80%); con 0,64 scende a circa 79%.
 */
internal const val RIGORE_BASE = 0.64
internal const val RIGORE_FI = 0.20

class LiveMatch(
    val home: Team, homePlayers: List<Player>,
    val away: Team, awayPlayers: List<Player>,
    var homeF: Formation, var awayF: Formation,
    private val homeCoach: Int, private val awayCoach: Int,
    private var homeMent: Mentality?, private var awayMent: Mentality?,
    homeBench: List<Player>?, awayBench: List<Player>?,
    private val rng: Random = Random.Default,
    var manualHome: Boolean = false,
    var manualAway: Boolean = false,
    homePress: Pressing? = null, awayPress: Pressing? = null,   // null = sceglie l'AI
    homeLine: DefLine? = null, awayLine: DefLine? = null,
    homeAmp: Ampiezza = Ampiezza.NORMALE, awayAmp: Ampiezza = Ampiezza.NORMALE,
    homeStile: Stile? = null, awayStile: Stile? = null,
    private val homeTiri: SetPieces = SetPieces.NONE, private val awayTiri: SetPieces = SetPieces.NONE,
    private val meteoGol: Double = 1.0,  // meteo e campo: moltiplicano le azioni pericolose (Meteo.Condizioni.fattoreGol)
    var trapH: Boolean = false, var trapA: Boolean = false,   // trappola del fuorigioco (vale solo con la linea alta)
    val arbitro: Disciplina.Arbitro = Disciplina.Arbitro("Arbitro", 2, 2),
    val varAttivo: Boolean = true,
    /** Partita raccontata: arbitro che può sbagliare, VAR, percorso della palla e telecronaca. Le simulazioni restano pulite. */
    private val immersivo: Boolean = false,
    /** Atmosfera dello stadio 0..100 (vedi Atmosfera): conta solo nella partita dal vivo. */
    private val atmosfera: Int = 50,
    private val homeFasce: Fasce = Fasce.MISTO, private val awayFasce: Fasce = Fasce.MISTO,
    private val homeSchema: SchemaPiazzati = SchemaPiazzati.NORMALE, private val awaySchema: SchemaPiazzati = SchemaPiazzati.NORMALE
) {
    private val campo = if (immersivo) Atmosfera.fattore(atmosfera) else 1.0
    val extraH = StatExtra()
    val extraA = StatExtra()
    private val vistaCasa = manualHome
    private val rngI: Random = if (immersivo) Random(rng.nextLong()) else rng   // flusso separato: non altera il resto della partita

    /** Tappe della palla verso il punto finale: dal centrocampo, con passaggi che cambiano lato. */
    private fun percorso(homeAtt: Boolean, fine: Pair<Float, Float>): List<Pair<Float, Float>> {
        if (!immersivo) return emptyList()
        val sx = if (homeAtt) 0.28f + rngI.nextFloat() * 0.15f else 0.72f - rngI.nextFloat() * 0.15f
        val n = 2 + rngI.nextInt(3)
        val pts = ArrayList<Pair<Float, Float>>()
        pts += sx to (0.25f + rngI.nextFloat() * 0.5f)
        for (i in 1..n) pts += (sx + (fine.first - sx) * i / (n + 1f)) to (0.15f + rngI.nextFloat() * 0.7f)
        pts += fine
        return pts
    }

    /** Controllo dell'arbitro e del VAR su un gol azione. True = il gol vale. Non tocca il punteggio. */
    private fun golValido(homeAtt: Boolean, squadra: String, chi: String, x: Float, y: Float): Boolean {
        if (!immersivo) return true
        val mancato = when (arbitro.capacita) { 1 -> 0.55; 2 -> 0.30; else -> 0.12 }
        if (rngI.nextDouble() < 0.04) {   // gol irregolare (fuorigioco o fallo)
            if (rngI.nextDouble() > mancato) {
                events += MatchEvent(minute, EventType.VAR, homeAtt, "Gol annullato dall'arbitro: $chi ($squadra) era in posizione irregolare", x, y)
                return false
            }
            if (varAttivo && rngI.nextDouble() < 0.85) {
                events += MatchEvent(minute, EventType.VAR, homeAtt, "VAR: controllo in corso... gol annullato a $chi ($squadra)", x, y)
                return false
            }
            return true   // nessuno se ne accorge
        }
        if (rngI.nextDouble() < (3 - arbitro.capacita) * 0.012) {   // l'arbitro annulla un gol buono
            if (varAttivo && rngI.nextDouble() < 0.8) {
                events += MatchEvent(minute, EventType.VAR, homeAtt, "VAR: l'arbitro aveva annullato, ma il gol di $chi ($squadra) è valido!", x, y)
                return true
            }
            events += MatchEvent(minute, EventType.VAR, homeAtt, "Gol annullato dall'arbitro per un presunto fallo di $chi ($squadra): decisione contestata", x, y)
            return false
        }
        return true
    }
    var fuorigiocoH = 0; private set   // fuorigioco fischiati alla squadra di casa
    var fuorigiocoA = 0; private set
    private fun split(ps: List<Player>, bench: List<Player>?, f: Formation): Pair<MutableList<Player>, MutableList<Player>> =
        if (bench != null) ps.toMutableList() to bench.toMutableList()
        else Lineup.coachPickConPanchina(ps, f).let { (xi, panchina) -> xi.toMutableList() to panchina.toMutableList() }

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
    // Ammonizioni a testa (la seconda è rosso), espulsi e infortunati durante la partita
    private val gialli = HashMap<Long, Int>()
    private val espulsi = mutableSetOf<Long>()
    private val infortunati = mutableSetOf<Long>()
    // Minuto di ingresso e di uscita di chi ha cambiato, per i minuti giocati a fine partita
    private val minIn = HashMap<Long, Int>()
    private val minOut = HashMap<Long, Int>()
    /** Minuti giocati da un giocatore (0 se non è sceso in campo). */
    fun minutiGiocati(id: Long): Int =
        if (id !in playedH && id !in playedA) 0 else ((minOut[id] ?: minute) - (minIn[id] ?: 0)).coerceAtLeast(0)
    var subsH = 0; private set
    var subsA = 0; private set
    // Ritmo di squadra (solo la tua squadra lo sceglie in partita)
    var ritmoH = Ritmo.NORMALE; private set
    var ritmoA = Ritmo.NORMALE; private set
    // Contropiede: con meno possesso, le ripartenze sono più pericolose
    var contropiedeH = false; private set
    var contropiedeA = false; private set
    fun setContropiede(homeSide: Boolean, on: Boolean) {
        if (finished) return
        if (homeSide) contropiedeH = on else contropiedeA = on
        val t = if (homeSide) home else away
        events += MatchEvent(minute, EventType.TACTIC, homeSide,
            "${t.shortName} ${if (on) "gioca in contropiede" else "torna al gioco normale"}", 0.5f, 0.5f)
    }
    private fun conAttacco(r: MatchEngine.Ratings, k: Double) =
        MatchEngine.Ratings(r.atk * k, r.mid, r.def, r.gk, r.stamina, r.players)
    private var h = MatchEngine.ratings(xiH, homeF, homeCoach)
    private var a = MatchEngine.ratings(xiA, awayF, awayCoach)
    private val pHome = h.mid * MatchEngine.HOME_BONUS / (h.mid * MatchEngine.HOME_BONUS + a.mid)

    val events = mutableListOf(MatchEvent(0, EventType.KICKOFF, true, "Calcio d'inizio", 0.5f, 0.5f))
    var minute = 0; private set
    var hg = 0; private set
    var ag = 0; private set
    private var hs = 0; private var asht = 0
    /** Dati per il tabellone dal vivo: tiri e possesso stimato della squadra di casa (%). */
    val tiriCasa get() = hs
    val tiriOspiti get() = asht
    val possessoCasa get() = (pHome * 100).toInt()
    /** Fine della partita: 90' più il recupero (calcolato al 90' solo nella partita dal vivo). */
    var fine = 90; private set
    val finished get() = minute >= fine

    // Mentalità iniziale delle squadre AI: in base al rapporto di forza
    private fun startMent(own: MatchEngine.Ratings, opp: MatchEngine.Ratings, bonus: Double): Mentality {
        val q = (own.atk + own.mid + own.def) * bonus / (opp.atk + opp.mid + opp.def)
        return when { q < 0.93 -> Mentality.DIFENSIVA; q > 1.07 -> Mentality.OFFENSIVA; else -> Mentality.EQUILIBRATA }
    }
    // Con l'allenatore della squadra AI, la tattica di base è quella del suo stile; altrimenti decide la forza
    private val baseH = homeStile?.mentalita ?: startMent(h, a, MatchEngine.HOME_BONUS)
    private val baseA = awayStile?.mentalita ?: startMent(a, h, 1.0 / MatchEngine.HOME_BONUS)
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
    var pressH = homePress ?: homeStile?.pressing ?: aiPress(xiH, xiA); private set
    var pressA = awayPress ?: awayStile?.pressing ?: aiPress(xiA, xiH); private set
    var lineH = homeLine ?: homeStile?.linea ?: aiLine(xiH, xiA); private set
    var lineA = awayLine ?: awayStile?.linea ?: aiLine(xiA, xiH); private set

    var ampH = homeAmp; private set
    var ampA = awayAmp; private set
    fun setAmpiezza(homeSide: Boolean, a: Ampiezza) { if (homeSide) ampH = a else ampA = a }

    /** Cambio programmato dall'allenatore: usa la logica del cambio automatico. Restituisce true se ha cambiato. */
    fun cambioProgrammato(homeSide: Boolean): Boolean =
        autoSub(homeSide, if (homeSide) hg < ag else ag < hg)

    /**
     * Cambio con condizioni scelte dall'allenatore (maschera di bit, vedi CambiCondizioni). Fa al massimo un cambio
     * per chiamata. Restituisce true se ha cambiato.
     */
    fun cambioCondizionato(homeSide: Boolean, cond: Int): Boolean {
        if (finished || cond == 0 || minute < 30) return false
        val xi = if (homeSide) xiH else xiA
        val bench = if (homeSide) benchH else benchA
        if ((if (homeSide) subsH else subsA) >= MatchEngine.MAX_SUBS || bench.isEmpty()) return false
        val mio = if (homeSide) hg else ag
        val loro = if (homeSide) ag else hg
        val gioc = xi.filter { it.role != "P" }
        fun migliore(ruolo: String) = bench.filter { it.role == ruolo }.maxByOrNull { it.overall }
        if (cond and CambiCondizioni.AMMONITO != 0) {
            val g = gioc.filter { (gialli[it.id] ?: 0) >= 1 }.minByOrNull { energyOf(it) }
            val inP = g?.let { migliore(it.role) }
            if (g != null && inP != null) { doSub(homeSide, g, inP); return true }
        }
        if (cond and CambiCondizioni.STANCO != 0) {
            val g = gioc.filter { energyOf(it) < 0.88 }.minByOrNull { energyOf(it) }
            val inP = g?.let { migliore(it.role) }
            if (g != null && inP != null) { doSub(homeSide, g, inP); return true }
        }
        if (cond and CambiCondizioni.SOTTO != 0 && minute >= 60 && mio < loro) {
            val g = gioc.filter { it.role != "A" }.minByOrNull { energyOf(it) }
            val inP = migliore("A")
            if (g != null && inP != null) { doSub(homeSide, g, inP); return true }
        }
        if (cond and CambiCondizioni.SOPRA != 0 && minute >= 75 && mio > loro) {
            val g = gioc.filter { it.role == "A" }.minByOrNull { energyOf(it) }
                ?: gioc.filter { it.role == "C" }.minByOrNull { energyOf(it) }
            val inP = migliore("D") ?: migliore("C")
            if (g != null && inP != null) { doSub(homeSide, g, inP); return true }
        }
        return false
    }

    fun setPressing(homeSide: Boolean, p: Pressing) { if (homeSide) pressH = p else pressA = p }
    fun setLine(homeSide: Boolean, l: DefLine) { if (homeSide) lineH = l else lineA = l }

    /** Cambia il ritmo di una squadra durante la partita. */
    fun setRitmo(homeSide: Boolean, r: Ritmo) {
        if (finished) return
        if (homeSide) ritmoH = r else ritmoA = r
        refresh()
        val t = if (homeSide) home else away
        events += MatchEvent(minute, EventType.TACTIC, homeSide, "${t.shortName} gioca a ritmo ${r.label.lowercase()}", 0.5f, 0.5f)
    }

    /** Cambio di modulo dal vivo: i giocatori restano in campo, cambiano posizioni e peso di ogni reparto. */
    fun setModulo(homeSide: Boolean, f: Formation) {
        if (finished) return
        if (homeSide) homeF = f else awayF = f
        refresh()
        val t = if (homeSide) home else away
        events += MatchEvent(minute, EventType.TACTIC, homeSide, "${t.shortName} passa al modulo ${f.label}", 0.5f, 0.5f)
    }

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
        h = conAttacco(MatchEngine.ratings(xiH, homeF, homeCoach, energy), ritmoH.attacco)
        a = conAttacco(MatchEngine.ratings(xiA, awayF, awayCoach, energy), ritmoA.attacco)
    }

    private fun doSub(homeSide: Boolean, out: Player, inP: Player) {
        val xi = if (homeSide) xiH else xiA
        val bench = if (homeSide) benchH else benchA
        xi[xi.indexOf(out)] = inP; bench -= inP
        (if (homeSide) playedH else playedA) += inP.id
        minOut[out.id] = minute; minIn[inP.id] = minute
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
        val prima = events.size
        minute++
        if (minute > 55) {
            xiH.forEach { p -> energy[p.id] = (energyOf(p) - MatchEngine.drain(p) * pressH.drain * ritmoH.consumo).coerceAtLeast(0.7) }
            xiA.forEach { p -> energy[p.id] = (energyOf(p) - MatchEngine.drain(p) * pressA.drain * ritmoA.consumo).coerceAtLeast(0.7) }
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
        if (rng.nextDouble() < MatchEngine.ATTACK_PER_MINUTE * meteoGol * (mH.tempo + mA.tempo) / 2) {
            val ph = h.mid * MatchEngine.HOME_BONUS * campo * pressH.poss * lineH.poss * ampH.poss
            val pNow = ph / (ph + a.mid * pressA.poss * lineA.poss * ampA.poss)
            val homeAtt = rng.nextDouble() < pNow
            val att = if (homeAtt) h else a
            val dfn = if (homeAtt) a else h
            val team = if (homeAtt) home else away
            val mAtt = if (homeAtt) mH else mA
            val mDef = if (homeAtt) mA else mH
            val lAtt = if (homeAtt) lineH else lineA
            val lDef = if (homeAtt) lineA else lineH
            val defPen = speedPenalty(lDef, if (homeAtt) xiH else xiA)
            val ampAtt = if (homeAtt) ampH else ampA
            val ampDef = if (homeAtt) ampA else ampH
            var ratio = att.atk * mAtt.atk * lAtt.atk * ampAtt.atk / (dfn.def * mDef.def * lDef.def * ampDef.def * defPen)
            val contro = if (homeAtt) contropiedeH else contropiedeA
            if (contro && (if (homeAtt) pNow else 1 - pNow) < 0.5) ratio *= 1.12
            ratio *= if (homeAtt) homeFasce.fattore(xiH) else awayFasce.fattore(xiA)
            // Trappola del fuorigioco: spesso ferma l'azione, ma se salta l'attaccante va in porta da solo
            val trappola = (if (homeAtt) trapA else trapH) && lDef == DefLine.ALTA
            val rT = rng.nextDouble()
            if (trappola && rT < 0.16) {
                if (homeAtt) fuorigiocoH++ else fuorigiocoA++
                val (x, y) = pos(homeAtt)
                events += MatchEvent(minute, EventType.OFFSIDE, homeAtt, "Fuorigioco (${team.shortName}): la trappola funziona", x, y)
                ratio = 0.0
            } else if (trappola && rT < 0.21) ratio *= 1.8
            if (ratio > 0.0 && rng.nextDouble() < (0.45 * ratio).coerceIn(0.2, 0.7)) {
                if (homeAtt) hs++ else asht++
                val sh = MatchEngine.shooter(att.players, rng)
                val who = sh?.name ?: "Attaccante"
                val (x, y) = pos(homeAtt)
                val pGoal = (0.22 * (att.atk / dfn.gk).pow(1.5) *
                    (if (homeAtt) MatchEngine.HOME_FINISH else 1.0)).coerceIn(0.08, 0.45) *
                    // Cinico: nel finale con il risultato in bilico finalizza meglio (vedi Tratti.fattoreFinale)
                    (sh?.let { Tratti.fattoreFinale(it, minute, if (homeAtt) hg else ag, if (homeAtt) ag else hg) } ?: 1.0)
                val r = rng.nextDouble()
                when {
                    r < pGoal && !golValido(homeAtt, team.shortName, who, x, y) -> Unit   // annullato: resta il tiro
                    r < pGoal -> {
                        if (homeAtt) hg++ else ag++
                        sh?.let { bump(it, 1.0) }
                        val ass = assistman(att.players, sh)
                        ass?.let { bump(it, 0.4) }
                        events += MatchEvent(minute, EventType.GOAL, homeAtt,
                            "Gol! $who (${team.shortName})" + (ass?.let { ", assist ${it.name}" } ?: ""),
                            x, y, sh?.id ?: 0L, ass?.id ?: 0L, percorso(homeAtt, x to y))
                    }
                    r < pGoal + (1 - pGoal) * 0.4 -> events += MatchEvent(
                        minute, EventType.SAVE, homeAtt, "Tiro di $who, para il portiere", x, y, 0, 0, percorso(homeAtt, x to y))
                    else -> events += MatchEvent(minute, EventType.MISS, homeAtt, "Tiro di $who fuori", x, y, 0, 0, percorso(homeAtt, x to y))
                }
            }
        }
        // Calci piazzati: rigore raro e in area, punizione più frequente dal limite
        if (rng.nextDouble() < MatchEngine.PENALTY_PER_MINUTE) setPiece(penalty = true)
        else if (rng.nextDouble() < MatchEngine.SHOT_FREEKICK_PER_MINUTE) setPiece(penalty = false)
        if (rng.nextDouble() < (if (immersivo) 0.018 * (0.7 + 0.3 * arbitro.severita) else 0.018)) {
            val side = rng.nextBoolean()
            val ps = (if (side) xiH else xiA).filter { it.role != "P" }
            if (ps.isNotEmpty()) {
                val p = MatchEngine.aggressive(ps, rng)
                val t = if (side) home else away
                bump(p, -0.5)
                val n = (gialli[p.id] ?: 0) + 1
                gialli[p.id] = n
                events += MatchEvent(minute, EventType.YELLOW, side, "Ammonito ${p.name} (${t.shortName})", 0.5f, 0.5f, p.id)
                if (n >= 2) espelli(side, p, "doppia ammonizione")
            }
        }
        // Rosso diretto e infortuni in partita: rari
        if (rng.nextDouble() < 0.0005) {
            val side = rng.nextBoolean()
            val ps = (if (side) xiH else xiA).filter { it.role != "P" }
            if (ps.isNotEmpty()) {
                val p = MatchEngine.aggressive(ps, rng)
                if (immersivo && rngI.nextDouble() < (3 - arbitro.capacita) * 0.12) {   // rosso sbagliato
                    if (varAttivo && rngI.nextDouble() < 0.8) events += MatchEvent(minute, EventType.VAR, side,
                        "VAR: rosso cancellato per ${p.name}, l'intervento era regolare", 0.5f, 0.5f, p.id)
                    else espelli(side, p, "decisione contestata")
                } else espelli(side, p, "gioco violento")
            }
        }
        // Rigore dubbio: più frequente con un arbitro scarso; il VAR a volte lo concede dopo la revisione
        if (immersivo && rngI.nextDouble() < 0.0008 * (1.0 + (3 - arbitro.capacita) * 0.4)) {
            val casa = rngI.nextBoolean()
            val sq = if (casa) home.shortName else away.shortName
            events += MatchEvent(minute, if (varAttivo) EventType.VAR else EventType.COMMENTO, casa,
                if (varAttivo) "VAR: tocco di mano in area, rigore per $sq!" else "Rigore concesso a $sq per un contatto dubbio", 0.5f, 0.5f)
            setPiece(penalty = true, forzaCasa = casa)
        }
        if (rng.nextDouble() < 0.0006) infortunio(rng.nextBoolean())
        if (minute == 45) events += MatchEvent(45, EventType.HALFTIME, true, "Fine primo tempo", 0.5f, 0.5f)
        if (minute == 90 && immersivo && fine == 90) {
            // Recupero: cambi, infortuni e controlli VAR fanno allungare la partita
            val rec = (1 + (subsH + subsA) / 3 + infortunati.size + events.count { it.type == EventType.VAR } / 2 + rngI.nextInt(0, 2)).coerceIn(1, 7)
            fine = 90 + rec
            events += MatchEvent(90, EventType.COMMENTO, true, "Recupero: $rec ${if (rec == 1) "minuto" else "minuti"}", 0.5f, 0.5f)
        }
        if (minute == fine) events += MatchEvent(minute, EventType.FULLTIME, true, "Fine partita $hg-$ag", 0.5f, 0.5f)
        if (immersivo) { statisticheMinuto(); registraCalore(); telecronaca(prima) }
    }

    /** Mappa di calore: per ogni giocatore, i minuti passati in ciascuna zona (8 colonne x 5 righe, casa verso destra). */
    val calore = HashMap<Long, IntArray>()

    private fun registraCalore() {
        val ev = events.lastOrNull { it.percorso.isNotEmpty() && minute - it.minute <= 2 }
        val bx = ev?.x ?: 0.5f; val by = ev?.y ?: 0.5f
        fun reg(xi: List<Player>, f: Formation, casa: Boolean) {
            val ordine = Lineup.perPosti(xi, f)
            val coords = f.coords()
            val segno = if (casa) 1f else -1f
            ordine.forEachIndexed { i, p ->
                val (fx, fy) = coords.getOrNull(i) ?: return@forEachIndexed
                val base = if (casa) fx else 1f - fx
                var x = base + (bx - 0.5f) * 0.2f
                if (ev != null && i > 0) x += if (ev.home == casa) segno * (0.04f + 0.35f * base) else -segno * 0.15f * base
                val y = fy + (by - fy) * 0.12f
                val cella = (x.coerceIn(0f, 0.999f) * 8).toInt() + (y.coerceIn(0f, 0.999f) * 5).toInt() * 8
                calore.getOrPut(p.id) { IntArray(40) }[cella]++
            }
        }
        reg(xiH, homeF, true); reg(xiA, awayF, false)
    }

    private fun forzaDuello(xi: List<Player>): Double =
        if (xi.isEmpty()) 1.0 else xi.map { it.ag + it.re }.average() * xi.map { energyOf(it) }.average()

    /** Passaggi, duelli e chilometri del minuto: dipendono da possesso, qualità, aggressività, resistenza e fatica. */
    private fun statisticheMinuto() {
        fun corsa(e: StatExtra, xi: List<Player>, r: Ritmo) {
            e.km += xi.sumOf { (0.088 + 0.04 * it.re / 99.0) * Math.sqrt(energyOf(it)) * Math.sqrt(r.consumo) }
        }
        fun passaggi(e: StatExtra, xi: List<Player>, quota: Double) {
            if (xi.isEmpty()) return
            val acc = (0.60 + 0.30 * xi.map { it.pa }.average() / 99.0).coerceIn(0.5, 0.92)
            val n = (quota * 18 + rngI.nextInt(0, 4)).roundToInt()
            repeat(n) { e.passaggi++; if (rngI.nextDouble() < acc) e.passaggiRiusciti++ }
        }
        corsa(extraH, xiH, ritmoH); corsa(extraA, xiA, ritmoA)
        passaggi(extraH, xiH, pHome); passaggi(extraA, xiA, 1.0 - pHome)
        val fh = forzaDuello(xiH); val fa = forzaDuello(xiA)
        repeat(1 + rngI.nextInt(0, 2)) {
            extraH.duelli++; extraA.duelli++
            if (rngI.nextDouble() < fh / (fh + fa)) extraH.duelliVinti++ else extraA.duelliVinti++
        }
    }

    /** Coppia di telecronisti estratta a caso per questa partita. */
    val telecronisti: Coppia = Coppia.sorteggia(kotlin.random.Random.Default)
    private var presentati = false

    /** Un commento a caldo sull'ultimo episodio, oppure ogni cinque minuti un commento di fondo con un indizio. */
    private fun telecronaca(primaDi: Int) {
        if (!presentati) { presentati = true; events += MatchEvent(minute, EventType.COMMENTO, vistaCasa, telecronisti.presentazione(), 0.5f, 0.5f) }
        val nuovi = events.subList(primaDi, events.size)
        val episodio = nuovi.lastOrNull { it.type in setOf(EventType.GOAL, EventType.SAVE, EventType.MISS, EventType.OFFSIDE, EventType.RED) }
        if (episodio != null && rngI.nextInt(10) < 7) {
            val nome = (xiH + xiA).firstOrNull { it.id == episodio.playerId && it.id != 0L }?.name?.trim()?.split(" ")?.lastOrNull()
            Telecronaca.reazione(episodio, nome, telecronisti, rngI).forEach { t ->
                events += MatchEvent(minute, EventType.COMMENTO, vistaCasa, t, 0.5f, 0.5f) }
            return
        }
        val testo = if (episodio != null) null
        else if (minute % 5 == 2 && minute < 90) {
            val mio = if (vistaCasa) xiH else xiA
            val loro = if (vistaCasa) xiA else xiH
            fun en(l: List<Player>) = if (l.isEmpty()) 1.0 else l.map { energyOf(it) }.average()
            Telecronaca.commento(Telecronaca.Quadro(
                minute, if (vistaCasa) hg else ag, if (vistaCasa) ag else hg,
                if (vistaCasa) possessoCasa else 100 - possessoCasa,
                if (vistaCasa) hs else asht, if (vistaCasa) asht else hs,
                en(mio), en(loro), mio.size < 11, loro.size < 11, arbitro,
                if (vistaCasa) mH else mA, MatchEngine.MAX_SUBS - (if (vistaCasa) subsH else subsA)), rngI, telecronisti)
        } else null
        if (testo != null) events += MatchEvent(minute, EventType.COMMENTO, vistaCasa, testo, 0.5f, 0.5f)
    }

    /** Rigore, punizione o calcio d'angolo per una squadra a caso: tira il primo della lista in campo, altrimenti il capitano. */
    private fun setPiece(penalty: Boolean, forzaCasa: Boolean? = null) {
        val homeAtt = forzaCasa ?: rng.nextBoolean()
        val inCampo = (if (homeAtt) xiH else xiA)
        val tiri = if (homeAtt) homeTiri else awayTiri
        val angolo = !penalty && rng.nextDouble() < ANGOLO_QUOTA
        val lista = when { penalty -> tiri.rigori; angolo -> tiri.angoli; else -> tiri.punizioni }
        val taker = tiri.tiratore(lista, inCampo.filter { it.role != "P" }) ?: return
        val team = if (homeAtt) home else away
        val (x, y) = if (penalty) Pair(if (homeAtt) 0.9f else 0.1f, 0.5f) else pos(homeAtt)
        val kind = when { penalty -> "Rigore"; angolo -> "Calcio d'angolo"; else -> "Punizione" }
        if (homeAtt) hs++ else asht++
        val schema = if (homeAtt) homeSchema else awaySchema
        val scena = scenaPiazzato(homeAtt, penalty, angolo, x to y)
        val zona = if (immersivo) zonaTiro() else ""
        val pGoal = when {
            penalty -> Tratti.rigore(RIGORE_BASE + RIGORE_FI * taker.fi / 99.0, taker)   // il Cinico sbaglia meno
            // punizioni e angoli: stessa probabilità, così la taratura non cambia (lo schema scelto la sposta)
            else -> (0.04 + 0.08 * taker.ti / 99.0) * (if (angolo) schema.angoli else schema.punizioni)
        }
        if (rng.nextDouble() < pGoal) {
            if (homeAtt) hg++ else ag++
            bump(taker, 1.0)
            events += MatchEvent(minute, EventType.GOAL, homeAtt,
                "Gol su $kind! ${taker.name} (${team.shortName})$zona", x, y, taker.id, 0, scena)
        } else if (rng.nextBoolean()) {
            events += MatchEvent(minute, EventType.SAVE, homeAtt, "$kind di ${taker.name}$zona, para il portiere", x, y, 0, 0, scena)
        } else {
            events += MatchEvent(minute, EventType.MISS, homeAtt, "$kind di ${taker.name}$zona, fuori", x, y, 0, 0, scena)
        }
    }

    /**
     * Rigoristi per una serie di rigori dopo la partita: prima quelli della lista dei tiri in campo, poi gli altri
     * per finalizzazione, il portiere per ultimo. Confronto per riferimento (nei test gli id sono 0).
     */
    fun rigoristi(homeSide: Boolean): List<Player> {
        val xi = if (homeSide) xiH else xiA
        val lista = (if (homeSide) homeTiri else awayTiri).rigori
            .mapNotNull { id -> xi.firstOrNull { it.id == id && id != 0L } }
            .fold(emptyList<Player>()) { acc, p -> if (acc.any { it === p }) acc else acc + p }
        val altri = xi.filter { p -> lista.none { it === p } }
            .sortedWith(compareBy<Player> { it.role == "P" }.thenByDescending { it.fi })
        return lista + altri
    }

    /** Zona di porta scelta dal battitore (solo nella partita raccontata). */
    private fun zonaTiro(): String = when (rngI.nextInt(5)) {
        0 -> " all'incrocio"; 1 -> " sul palo lungo"; 2 -> " sul primo palo"; 3 -> " rasoterra"; else -> " centrale"
    }

    /** Scena del piazzato: dove parte il battitore e dove arriva la palla. */
    private fun scenaPiazzato(homeAtt: Boolean, penalty: Boolean, angolo: Boolean, fine: Pair<Float, Float>): List<Pair<Float, Float>> {
        if (!immersivo) return emptyList()
        val gx = if (homeAtt) 0.99f else 0.01f
        return when {
            penalty -> listOf((if (homeAtt) 0.88f else 0.12f) to 0.5f, gx to (0.42f + rngI.nextFloat() * 0.16f))
            angolo -> {
                val alto = rngI.nextBoolean()
                listOf(gx to (if (alto) 0.02f else 0.98f), (if (homeAtt) 0.9f else 0.1f) to (0.35f + rngI.nextFloat() * 0.3f), fine)
            }
            else -> listOf((if (homeAtt) fine.first - 0.18f else fine.first + 0.18f) to (0.2f + rngI.nextFloat() * 0.6f), fine)
        }
    }

    /** Chi fa l'assist a un gol: nel 70% dei casi qualcuno dei compagni, soprattutto centrocampisti con buon passaggio. */
    private fun assistman(xi: List<Player>, scorer: Player?): Player? {
        if (rng.nextDouble() > 0.7) return null
        val c = xi.filter { it.id != scorer?.id && it.role != "P" }
        if (c.isEmpty()) return null
        val w = c.map { (it.pa + it.qu) / 2.0 * (if (it.role == "C") 1.6 else if (it.role == "A") 1.2 else 0.6) }
        var r = rng.nextDouble() * w.sum()
        for (i in c.indices) { r -= w[i]; if (r <= 0) return c[i] }
        return c.last()
    }

    /** Espulsione: la squadra resta in dieci, senza sostituto. */
    private fun espelli(homeSide: Boolean, p: Player, motivo: String) {
        val xi = if (homeSide) xiH else xiA
        if (!xi.remove(p)) return
        espulsi += p.id
        bump(p, -1.5)
        val t = if (homeSide) home else away
        events += MatchEvent(minute, EventType.RED, homeSide, "Espulso ${p.name} (${t.shortName}): $motivo", 0.5f, 0.5f, p.id)
        refresh()
    }

    /** Infortunio in campo: esce subito, entra il miglior panchinaro dello stesso ruolo se ci sono cambi. */
    private fun infortunio(homeSide: Boolean) {
        val xi = if (homeSide) xiH else xiA
        val ps = xi.filter { it.role != "P" }
        if (ps.isEmpty()) return
        val p = ps[rng.nextInt(ps.size)]
        infortunati += p.id
        val t = if (homeSide) home else away
        events += MatchEvent(minute, EventType.INJURY, homeSide, "Infortunio per ${p.name} (${t.shortName})", 0.5f, 0.5f, p.id)
        val bench = if (homeSide) benchH else benchA
        val sub = bench.filter { it.role == p.role }.maxByOrNull { it.overall } ?: bench.maxByOrNull { it.overall }
        if ((if (homeSide) subsH else subsA) < MatchEngine.MAX_SUBS && sub != null) doSub(homeSide, p, sub)
        else { xi.remove(p); refresh() }
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
        rated.values.toList(), finalRatings(), infortunati.toSet(), espulsi.toSet(),
        if (immersivo) extraH else null, if (immersivo) extraA else null)
}
