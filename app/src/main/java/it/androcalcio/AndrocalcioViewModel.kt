package it.androcalcio

import it.androcalcio.engine.DefLine
import it.androcalcio.engine.Pressing
import it.androcalcio.engine.TrainFocus
import it.androcalcio.engine.TrainIntensity
import it.androcalcio.engine.TrainingPlan
import android.app.Application
import android.content.Intent
import android.net.Uri
import it.androcalcio.data.SaveSlots
import it.androcalcio.data.SlotInfo
import it.androcalcio.data.DataIO
import kotlinx.coroutines.Dispatchers
import java.io.File
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.androcalcio.data.AndrocalcioDatabase
import it.androcalcio.data.Player
import it.androcalcio.data.SeedData
import it.androcalcio.data.Goal
import it.androcalcio.data.Movimento
import it.androcalcio.data.Staff
import it.androcalcio.engine.Attr
import it.androcalcio.engine.EventType
import it.androcalcio.data.Team
import it.androcalcio.data.MatchResult
import it.androcalcio.engine.Fixtures
import it.androcalcio.engine.Formation
import it.androcalcio.engine.Medical
import it.androcalcio.engine.Staffing
import it.androcalcio.engine.Mentality
import it.androcalcio.engine.Lineup
import it.androcalcio.engine.LiveMatch
import it.androcalcio.data.ScoutReport
import it.androcalcio.data.Youth
import it.androcalcio.engine.MatchEngine
import it.androcalcio.engine.Progression
import it.androcalcio.engine.standings
import it.androcalcio.engine.Scouting
import it.androcalcio.engine.Youths
import it.androcalcio.engine.Market
import it.androcalcio.engine.OfferResult
import it.androcalcio.ui.components.money
import it.androcalcio.engine.MatchReport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AndrocalcioViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx: Application = app
    private val db = AndrocalcioDatabase.create(app)
    private val dao = db.dao()
    private val customFile = File(app.filesDir, "custom.json")

    val teams: StateFlow<List<Team>> =
        dao.teams().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // La squadra scelta viene ricordata anche dopo la chiusura dell'app
    private val selectedId = MutableStateFlow<Long?>(
        app.getSharedPreferences("androcalcio", 0).getLong("team_id", -1L).takeIf { it > 0 }
    )

    val selectedTeam: StateFlow<Team?> =
        combine(teams, selectedId) { list, id ->
            list.firstOrNull { it.id == id } ?: list.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val players: StateFlow<List<Player>> =
        selectedTeam
            .flatMapLatest { t -> if (t == null) flowOf(emptyList()) else dao.playersOf(t.id) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _report = MutableStateFlow<MatchReport?>(null)
    val report: StateFlow<MatchReport?> = _report

    fun play(home: Team, away: Team, formation: Formation) {
        viewModelScope.launch {
            setFormation(formation)
            _live.value = buildLive(home, away, true, null)
            startTicker()
        }
    }

    fun resetMatch() { _report.value = null }

    init {
        viewModelScope.launch {
            if (dao.teamCount() == 0) SeedData.insertInto(app, dao)
        }
    }

    private val prefs = ctx.getSharedPreferences("androcalcio", 0)
    private fun seasonName(y: Int) = "$y/${((y + 1) % 100).toString().padStart(2, '0')}"
    // Mentalità della tua squadra, ricordata tra una sessione e l'altra
    private val _mentality = MutableStateFlow(
        Mentality.values()[prefs.getInt("mentalita", Mentality.EQUILIBRATA.ordinal)])
    val mentality: StateFlow<Mentality> = _mentality
    // Modulo e formazione titolare della tua squadra
    private val _formation = MutableStateFlow(
        Formation.values()[prefs.getInt("modulo", Formation.F442.ordinal).coerceIn(0, Formation.values().size - 1)])
    val formation: StateFlow<Formation> = _formation
    fun setFormation(f: Formation) {
        _formation.value = f
        prefs.edit().putInt("modulo", f.ordinal).apply()
    }

    fun toggleStarter(p: Player) {
        viewModelScope.launch {
            val mine = dao.playersOnce(p.teamId)
            if (!p.starter && mine.count { it.starter } >= 11) {
                _notice.value = "Hai già 11 titolari: togline uno prima di aggiungere ${p.name}."
                return@launch
            }
            dao.updatePlayer(p.copy(starter = !p.starter))
        }
    }

    /** L'allenatore propone gli 11 migliori disponibili per il modulo scelto. */
    fun coachLineup() {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            val ps = dao.playersOnce(me.id)
            val xi = Lineup.coachPick(ps, _formation.value).map { it.id }.toSet()
            dao.updatePlayers(ps.map { it.copy(starter = it.id in xi) })
            _notice.value = "L'allenatore ha scelto gli 11 titolari per il ${_formation.value.label}."
        }
    }

    // Pressing e linea difensiva della tua squadra
    private val _pressing = MutableStateFlow(Pressing.values()[prefs.getInt("pressing", 1).coerceIn(0, 2)])
    val pressing: StateFlow<Pressing> = _pressing
    fun setPressing(p: Pressing) { _pressing.value = p; prefs.edit().putInt("pressing", p.ordinal).apply() }
    private val _line = MutableStateFlow(DefLine.values()[prefs.getInt("linea", 1).coerceIn(0, 2)])
    val line: StateFlow<DefLine> = _line
    private val _training = MutableStateFlow(TrainingPlan(
        TrainFocus.values()[prefs.getInt("allen_focus", 0).coerceIn(0, TrainFocus.values().size - 1)],
        TrainIntensity.values()[prefs.getInt("allen_int", 1).coerceIn(0, 2)]))
    val training: StateFlow<TrainingPlan> = _training
    fun setTraining(t: TrainingPlan) {
        _training.value = t
        prefs.edit().putInt("allen_focus", t.focus.ordinal).putInt("allen_int", t.intensity.ordinal).apply()
    }
    /** Piano di allenamento solo per la squadra dell'utente; le AI non si allenano in modo speciale. */
    private fun planFor(id: Long, userId: Long?) = if (id == userId) _training.value else null

    /** Movimenti di cassa: registrati solo per la squadra dell'utente. */
    val movimenti: StateFlow<List<Movimento>> =
        selectedTeam.flatMapLatest { t -> t?.let { dao.movimenti(it.id) } ?: flowOf(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private suspend fun registra(teamId: Long, label: String, importo: Long, saldo: Long) {
        if (teamId != selectedTeam.value?.id) return
        dao.insertMovimento(Movimento(teamId = teamId, season = _season.value, label = label, importo = importo, saldo = saldo))
    }

    fun setLine(l: DefLine) { _line.value = l; prefs.edit().putInt("linea", l.ordinal).apply() }

    fun setMentality(m: Mentality) {
        _mentality.value = m
        prefs.edit().putInt("mentalita", m.ordinal).apply()
    }

    private val _season = MutableStateFlow(seasonName(prefs.getInt("season_year", 2026)))
    val season: StateFlow<String> = _season
    private fun key(t: Team) = "${t.nazione}|${t.campionato}"

    val matches: StateFlow<List<MatchResult>> =
        combine(selectedTeam, _season) { t, s -> t to s }
            .flatMapLatest { (t, s) ->
                if (t == null) flowOf(emptyList()) else dao.matches(key(t), s)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            combine(selectedTeam.filterNotNull(), _season) { t, s -> t to s }.collect { (t, s) ->
                val k = key(t)
                if (dao.fixtureCount(k, s) == 0) {
                    val ids = dao.teamsOfLeague(t.nazione, t.campionato).map { it.id }
                    dao.insertMatches(Fixtures.generate(ids, s, k))
                }
            }
        }
    }

    /** Squadre AI senza allenatore o preparatore: ne ricevono uno casuale (anche per i salvataggi esistenti). */
    private suspend fun ensureAiStaff(meId: Long) {
        val have = dao.allStaff().groupBy { it.teamId }
        val players = dao.allPlayers().groupBy { it.teamId }
        val rng = kotlin.random.Random.Default
        val add = dao.allTeams().filter { it.id != meId }.flatMap { t ->
            val roles = have[t.id].orEmpty().map { it.role }
            val missing = listOf(Staffing.ALLENATORE, Staffing.PREPARATORE).filter { it !in roles }
            val ps = players[t.id].orEmpty()
            if (missing.isEmpty() || ps.isEmpty()) emptyList()
            else Staffing.aiStaff(t.id, ps.map { it.overall }.average(), missing, rng)
        }
        if (add.isNotEmpty()) dao.insertStaffs(add)
    }

    private fun playRounds(all: Boolean) {
        viewModelScope.launch { runRounds(all, null) }
    }

    /** Gioca la prossima giornata (o tutte); skipId = la tua partita, giocata a parte dal vivo. */
    private suspend fun runRounds(all: Boolean, skipId: Long?) {
        run {
            val t = selectedTeam.value ?: return
            ensureAiStaff(t.id)
            val ms = dao.matchesOnce(key(t), _season.value)
            val teamsById = dao.teamsOfLeague(t.nazione, t.campionato).associateBy { it.id }
            val staffBy = dao.allStaff().groupBy { it.teamId }
            val cache = HashMap<Long, List<Player>>()   // rose complete, aggiornate dopo ogni partita
            val lineupNotes = mutableListOf<String>()
            val formations = Formation.values()
            val rng = kotlin.random.Random.Default
            val goalsAcc = ArrayList<Goal>()
            val rounds = ms.filter { !it.played }.map { it.round }.distinct().sorted()
                .let { if (all) it else it.take(1) }
            for (m in ms.filter { !it.played && it.round in rounds && it.id != skipId }) {
                val h = teamsById[m.homeTeamId] ?: continue
                val a = teamsById[m.awayTeamId] ?: continue
                val hp = cache.getOrPut(h.id) { dao.playersOnce(h.id) }
                val ap = cache.getOrPut(a.id) { dao.playersOnce(a.id) }
                val hL = if (h.id == t.id) Lineup.resolve(hp, _formation.value) else null
                val aL = if (a.id == t.id) Lineup.resolve(ap, _formation.value) else null
                lineupNotes += hL?.notes.orEmpty() + aL?.notes.orEmpty()
                val r = MatchEngine.simulate(
                    h, hL?.xi ?: Medical.available(hp), a, aL?.xi ?: Medical.available(ap),
                    if (hL != null) _formation.value else formations.random(),
                    if (aL != null) _formation.value else formations.random(),
                    Staffing.skill(staffBy, h.id, Staffing.ALLENATORE),
                    Staffing.skill(staffBy, a.id, Staffing.ALLENATORE),
                    homeMent = if (h.id == t.id) _mentality.value else null,
                    awayMent = if (a.id == t.id) _mentality.value else null,
                    homeBench = hL?.let { Medical.available(hp) - it.xi.toSet() },
                    awayBench = aL?.let { Medical.available(ap) - it.xi.toSet() },
                    homePress = if (h.id == t.id) _pressing.value else null,
                    awayPress = if (a.id == t.id) _pressing.value else null,
                    homeLine = if (h.id == t.id) _line.value else null,
                    awayLine = if (a.id == t.id) _line.value else null
                )
                dao.updateMatch(m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true))
                goalsAcc += r.events.filter { it.type == EventType.GOAL && it.playerId != 0L }.map {
                    Goal(season = _season.value, league = key(t), round = m.round,
                        teamId = if (it.home) m.homeTeamId else m.awayTeamId, playerId = it.playerId)
                }
                cache[h.id] = Medical.afterMatch(hp, r.homePlayed, Staffing.skill(staffBy, h.id, Staffing.PREPARATORE), rng,
                    planFor(h.id, t.id), Staffing.skill(staffBy, h.id, Staffing.ALLENATORE))
                cache[a.id] = Medical.afterMatch(ap, r.awayPlayed, Staffing.skill(staffBy, a.id, Staffing.PREPARATORE), rng,
                    planFor(a.id, t.id), Staffing.skill(staffBy, a.id, Staffing.ALLENATORE))
            }
            dao.updatePlayers(cache.values.flatten())
            dao.insertGoals(goalsAcc)
            if (lineupNotes.isNotEmpty()) _notice.value = lineupNotes.distinct().joinToString("\n")
        }
    }

    fun playNextRound() = playRounds(false)
    fun playSeason() = playRounds(true)

    // ---- Partita dal vivo: pausa, velocità, cambi e mentalità decisi da te ----
    data class LiveInfo(
        val m: LiveMatch, val tick: Int = 0, val paused: Boolean = false, val speed: Int = 1,
        val userHome: Boolean = true, val league: MatchResult? = null, val saved: Boolean = false
    )
    private val _live = MutableStateFlow<LiveInfo?>(null)
    val live: StateFlow<LiveInfo?> = _live
    private var liveJob: kotlinx.coroutines.Job? = null

    private fun bump() { _live.value = _live.value?.let { it.copy(tick = it.tick + 1) } }

    private suspend fun buildLive(home: Team, away: Team, userHome: Boolean, league: MatchResult?): LiveInfo {
        val staffBy = dao.allStaff().groupBy { it.teamId }
        val me = if (userHome) home else away
        val all = dao.playersOnce(me.id)
        val res = Lineup.resolve(all, _formation.value)
        if (res.notes.isNotEmpty()) _notice.value = res.notes.joinToString("\n")
        val myBench = Medical.available(all) - res.xi.toSet()
        val opp = Medical.available(dao.playersOnce(if (userHome) away.id else home.id))
        val oppF = Formation.values().random()
        val m = LiveMatch(
            home, if (userHome) res.xi else opp, away, if (userHome) opp else res.xi,
            if (userHome) _formation.value else oppF, if (userHome) oppF else _formation.value,
            Staffing.skill(staffBy, home.id, Staffing.ALLENATORE), Staffing.skill(staffBy, away.id, Staffing.ALLENATORE),
            if (userHome) _mentality.value else null, if (userHome) null else _mentality.value,
            if (userHome) myBench else null, if (userHome) null else myBench,
            manualHome = userHome, manualAway = !userHome,
            homePress = if (userHome) _pressing.value else null, awayPress = if (userHome) null else _pressing.value,
            homeLine = if (userHome) _line.value else null, awayLine = if (userHome) null else _line.value
        )
        return LiveInfo(m, userHome = userHome, league = league)
    }

    private fun startTicker() {
        liveJob?.cancel()
        liveJob = viewModelScope.launch {
            while (true) {
                val i = _live.value ?: return@launch
                if (i.m.finished) break
                if (!i.paused) { i.m.step(); bump() }
                kotlinx.coroutines.delay(if (i.speed >= 20) 50L else 1000L / i.speed)
            }
            finishLive()
        }
    }

    /** Prossima giornata guardando la tua partita: le altre si giocano subito in automatico. */
    fun playNextRoundLive() {
        viewModelScope.launch {
            val t = selectedTeam.value ?: return@launch
            val ms = dao.matchesOnce(key(t), _season.value)
            val round = ms.filter { !it.played }.minOfOrNull { it.round } ?: return@launch
            val mine = ms.firstOrNull { !it.played && it.round == round && (it.homeTeamId == t.id || it.awayTeamId == t.id) }
            runRounds(false, mine?.id)
            if (mine == null) { _notice.value = "In questa giornata la tua squadra riposa."; return@launch }
            val teamsById = dao.teamsOfLeague(t.nazione, t.campionato).associateBy { it.id }
            val h = teamsById[mine.homeTeamId] ?: return@launch
            val a = teamsById[mine.awayTeamId] ?: return@launch
            _live.value = buildLive(h, a, h.id == t.id, mine)
            startTicker()
        }
    }

    fun setLivePaused(p: Boolean) { _live.value = _live.value?.copy(paused = p) }
    fun setLiveSpeed(x: Int) { _live.value = _live.value?.copy(speed = x) }
    fun liveSub(out: Player, inP: Player) {
        val i = _live.value ?: return
        if (!i.m.substitute(i.userHome, out, inP)) _notice.value = "Cambio non possibile."
        bump()
    }
    fun livePressing(p: Pressing) { val i = _live.value ?: return; i.m.setPressing(i.userHome, p); bump() }
    fun liveLine(l: DefLine) { val i = _live.value ?: return; i.m.setLine(i.userHome, l); bump() }

    fun liveMentality(m: Mentality) {
        val i = _live.value ?: return
        i.m.setMentality(i.userHome, m); bump()
    }
    /** Salta alla fine: i cambi rimasti li fa l'allenatore. */
    fun liveSkip() {
        val i = _live.value ?: return
        i.m.runToEnd(); bump()
    }

    /** Fine partita: per il campionato salva risultato, marcatori, infortuni e forma. */
    private suspend fun finishLive() {
        val i = _live.value ?: return
        if (i.saved) return
        val mr = i.league
        if (mr != null) {
            val r = i.m.report()
            val t = selectedTeam.value
            val lg = t?.let { key(it) } ?: ""
            dao.updateMatch(mr.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true))
            dao.insertGoals(r.events.filter { it.type == EventType.GOAL && it.playerId != 0L }.map {
                Goal(season = _season.value, league = lg, round = mr.round,
                    teamId = if (it.home) mr.homeTeamId else mr.awayTeamId, playerId = it.playerId)
            })
            val staffBy = dao.allStaff().groupBy { it.teamId }
            val rng = kotlin.random.Random.Default
            for (id in listOf(mr.homeTeamId, mr.awayTeamId)) {
                val played = if (id == mr.homeTeamId) r.homePlayed else r.awayPlayed
                dao.updatePlayers(Medical.afterMatch(dao.playersOnce(id), played, Staffing.skill(staffBy, id, Staffing.PREPARATORE), rng,
                    planFor(id, t?.id), Staffing.skill(staffBy, id, Staffing.ALLENATORE)))
            }
        }
        _live.value = _live.value?.copy(saved = true, tick = i.tick + 1)
    }

    fun closeLive() {
        val i = _live.value ?: return
        if (!i.m.finished) return
        liveJob?.cancel()
        _live.value = null
    }

    private val _role = MutableStateFlow("%")
    val marketRole: StateFlow<String> = _role
    fun setMarketRole(r: String) { _role.value = r }

    val market: StateFlow<List<Player>> =
        combine(selectedTeam, _role) { t, r -> t to r }
            .flatMapLatest { (t, r) ->
                if (t == null) flowOf(emptyList()) else dao.market(t.id, r)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _offer = MutableStateFlow<OfferResult?>(null)
    val offerResult: StateFlow<OfferResult?> = _offer
    fun clearOffer() { _offer.value = null }

    fun offer(p: Player, price: Long) {
        viewModelScope.launch {
            val buyer = selectedTeam.value ?: return@launch
            val d = Market.demand(p)
            _offer.value = when {
                players.value.size >= Market.MAX_SQUAD ->
                    OfferResult.Rejected("Rosa piena (massimo ${Market.MAX_SQUAD} giocatori).")
                price > buyer.budget -> OfferResult.Rejected("Budget insufficiente.")
                price >= d -> {
                    val seller = dao.team(p.teamId)
                    dao.updateTeam(buyer.copy(budget = buyer.budget - price))
                    dao.updateTeam(seller.copy(budget = seller.budget + price))
                    dao.updatePlayer(p.copy(teamId = buyer.id))
                    registra(buyer.id, "Acquisto ${p.name}", -price, buyer.budget - price)
                    OfferResult.Accepted("${p.name} è tuo per ${money(price)}.")
                }
                price >= (d * 0.85).toLong() -> OfferResult.Counter(p, d)
                else -> OfferResult.Rejected("Offerta troppo bassa: il club non tratta.")
            }
        }
    }

    fun sell(p: Player) {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            if (players.value.size <= Market.MIN_SQUAD) {
                _offer.value = OfferResult.Rejected("Rosa troppo corta (minimo ${Market.MIN_SQUAD}).")
                return@launch
            }
            val price = (Market.value(p) * 0.9).toLong()
            val buyer = teams.value.filter { it.id != me.id && it.campionato == me.campionato }
                .randomOrNull() ?: return@launch
            dao.updateTeam(me.copy(budget = me.budget + price))
            dao.updateTeam(buyer.copy(budget = (buyer.budget - price).coerceAtLeast(0)))
            dao.updatePlayer(p.copy(teamId = buyer.id))
            registra(me.id, "Vendita ${p.name} a ${buyer.name}", price, me.budget + price)
            _offer.value = OfferResult.Accepted("${p.name} ceduto a ${buyer.name} per ${money(price)}.")
        }
    }

    val youths: StateFlow<List<Youth>> =
        selectedTeam.flatMapLatest { t ->
            if (t == null) flowOf(emptyList()) else dao.youths(t.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val scouted: StateFlow<List<Player>> =
        selectedTeam.flatMapLatest { t ->
            if (t == null) flowOf(emptyList()) else dao.scouted(t.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reports: StateFlow<List<ScoutReport>> =
        selectedTeam.flatMapLatest { t ->
            if (t == null) flowOf(emptyList()) else dao.reports(t.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice
    fun clearNotice() { _notice.value = null }

    fun generateYouths() {
        viewModelScope.launch {
            val t = selectedTeam.value ?: return@launch
            val s = _season.value
            if (t.youthClaimed == s) return@launch
            dao.insertYouths(Youths.generate(t, s))
            dao.updateTeam(t.copy(youthClaimed = s))
        }
    }

    fun promote(y: Youth) {
        viewModelScope.launch {
            if (players.value.size >= Market.MAX_SQUAD) {
                _notice.value = "Rosa piena (massimo ${Market.MAX_SQUAD} giocatori)."
                return@launch
            }
            dao.insertPlayer(
                Attr.fill(Player(teamId = y.teamId, name = y.name, role = y.role, age = y.age,
                    overall = y.overall, morale = 75, salary = 30_000, nationality = y.nationality,
                    potential = y.potential, contract = 3), kotlin.random.Random)
            )
            dao.deleteYouth(y)
            _notice.value = "${y.name} promosso in prima squadra."
        }
    }

    fun discard(y: Youth) { viewModelScope.launch { dao.deleteYouth(y) } }

    fun sendScout(nazione: String, campionato: String) {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            val cost = Scouting.cost(me.scoutSkill)
            if (cost > me.budget) {
                _notice.value = "Budget insufficiente (servono ${money(cost)})."
                return@launch
            }
            val picks = dao.leaguePlayers(nazione, campionato, me.id).shuffled().take(6)
            if (picks.isEmpty()) {
                _notice.value = "Nessun giocatore da osservare in questo campionato."
                return@launch
            }
            dao.upsertReports(picks.map {
                ScoutReport(me.id, it.id, (dao.report(me.id, it.id)?.observations ?: 0) + 1)
            })
            dao.updateTeam(me.copy(budget = me.budget - cost))
            registra(me.id, "Scouting (${picks.size} giocatori)", -cost, me.budget - cost)
            _notice.value = "Lo scout ha osservato ${picks.size} giocatori (${money(cost)})."
        }
    }

    fun followPlayer(p: Player) {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            if (Scouting.FOLLOW_COST > me.budget) {
                _notice.value = "Budget insufficiente."
                return@launch
            }
            val obs = (dao.report(me.id, p.id)?.observations ?: 0) + 1
            dao.upsertReports(listOf(ScoutReport(me.id, p.id, obs)))
            dao.updateTeam(me.copy(budget = me.budget - Scouting.FOLLOW_COST))
            registra(me.id, "Osservazione ${p.name}", -Scouting.FOLLOW_COST, me.budget - Scouting.FOLLOW_COST)
        }
    }

    val askStartup = MutableStateFlow(customFile.exists())

    /** 0 = personalizzati, 1 = predefiniti, 2 = continua come ora. Nessuna copia viene sovrascritta. */
    fun chooseStartup(choice: Int) {
        askStartup.value = false
        viewModelScope.launch(Dispatchers.IO) {
            when (choice) {
                0 -> try {
                    DataIO.import(dao, customFile.readText())
                } catch (e: Exception) {
                    _notice.value = "Copia personalizzata non valida: nessuna modifica."
                }
                1 -> resetDefaultsNow()
            }
        }
    }

    private suspend fun resetDefaultsNow() {
        DataIO.wipe(dao)
        prefs.edit().putInt("season_year", 2026).apply()
        _season.value = seasonName(2026)
        SeedData.insertInto(ctx, dao)
    }

    fun resetDefaults() {
        viewModelScope.launch(Dispatchers.IO) {
            resetDefaultsNow()
            _notice.value = "Dati predefiniti ripristinati."
        }
    }

    fun saveCustom() {
        viewModelScope.launch(Dispatchers.IO) {
            customFile.writeText(DataIO.export(dao))
            _notice.value = "Copia personalizzata salvata."
        }
    }

    fun exportTo(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val text = DataIO.export(dao)
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
                _notice.value = "Dati esportati."
            } catch (e: Exception) {
                _notice.value = "Esportazione non riuscita."
            }
        }
    }

    fun importFrom(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val text = ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("vuoto")
                val n = DataIO.import(dao, text)
                _notice.value = "Importate $n squadre."
            } catch (e: Exception) {
                _notice.value = "File non valido: nessuna modifica."
            }
        }
    }

    fun saveTeam(t: Team) { viewModelScope.launch { dao.updateTeam(t) } }
    fun savePlayer(p: Player) { viewModelScope.launch { dao.updatePlayer(p) } }
    fun deletePlayer(p: Player) { viewModelScope.launch { dao.deletePlayer(p) } }

    fun addPlayer() {
        viewModelScope.launch {
            val t = selectedTeam.value ?: return@launch
            dao.insertPlayer(Attr.fill(Player(teamId = t.id, name = "Nuovo giocatore", role = "C", age = 22,
                overall = 60, morale = 70, salary = 50_000, nationality = "ITA"), kotlin.random.Random))
        }
    }

    fun renew(p: Player, years: Int) {
        viewModelScope.launch {
            if (p.morale < 40) {
                _notice.value = "${p.name} rifiuta di rinnovare (morale basso)."
                return@launch
            }
            val wage = Progression.wage(p)
            dao.updatePlayer(p.copy(contract = years, salary = wage))
            _notice.value = "${p.name} ha rinnovato per $years anni a ${money(wage)} l'anno."
        }
    }

    val goals: StateFlow<List<Goal>> =
        combine(selectedTeam, _season) { t, s -> t to s }
            .flatMapLatest { (t, s) -> if (t == null) flowOf(emptyList()) else dao.goals(key(t), s) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allPlayers: StateFlow<List<Player>> =
        dao.allPlayersFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---- Staff: allenatore e preparatore della tua squadra ----
    val staff: StateFlow<List<Staff>> =
        selectedTeam.flatMapLatest { t ->
            if (t == null) flowOf(emptyList()) else dao.staffOf(t.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _candidates = MutableStateFlow(Staffing.candidates(kotlin.random.Random.Default))
    val candidates: StateFlow<List<Staff>> = _candidates

    fun refreshCandidates() {
        _candidates.value = Staffing.candidates(kotlin.random.Random.Default)
    }

    /** Assume un candidato: sostituisce chi occupava già quel ruolo. Lo stipendio si paga a fine stagione. */
    fun hire(c: Staff) {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            val old = dao.staffOnce(me.id).filter { it.role == c.role }
            if (old.isNotEmpty()) dao.deleteStaffs(old)
            dao.addStaff(c.copy(id = 0, teamId = me.id))
            syncTeamSkill(me.id, c.role, c.skill)
            _candidates.value = _candidates.value - c
            val sost = old.firstOrNull()?.let { " Sostituisce ${it.name}." } ?: ""
            _notice.value = "${c.name} ingaggiato come ${Staffing.roleName(c.role).lowercase()}.$sost " +
                "Stipendio ${money(c.salary)} l'anno, pagato a fine stagione."
        }
    }

    fun fire(s: Staff) {
        viewModelScope.launch {
            dao.deleteStaffs(listOf(s))
            syncTeamSkill(s.teamId, s.role, Staffing.VUOTO)
            _notice.value = "${s.name} è stato licenziato."
        }
    }

    /** Vivaio e scout leggono le abilità del club: assumere le aggiorna, lasciare il ruolo vuoto le riporta a VUOTO. */
    private suspend fun syncTeamSkill(teamId: Long, role: String, skill: Int) {
        val t = dao.team(teamId)
        when (role) {
            Staffing.GIOVANILI -> dao.updateTeam(t.copy(youthCoachSkill = skill))
            Staffing.SCOUT -> dao.updateTeam(t.copy(scoutSkill = skill))
        }
    }

    /** Fine stagione: età, crescita, ritiri, contratti, bilancio; nuova stagione. */
    fun closeSeason() {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            ensureAiStaff(me.id)
            val rng = kotlin.random.Random.Default
            val s = _season.value
            val teamsNow = dao.allTeams()
            val cache = HashMap(dao.allPlayers().groupBy { it.teamId })
            val byTeam = cache
            val leagues = teamsNow.groupBy { it.nazione to it.campionato }
            val formations = Formation.values()
            val staffAll = dao.allStaff()
            val staffBy = staffAll.groupBy { it.teamId }

            // 1) Tutti i campionati: genera il calendario se manca e gioca le partite rimaste
            val finals = HashMap<Pair<String, String>, List<it.androcalcio.engine.StandingRow>>()
            for ((nc, ts) in leagues) {
                val key = "${nc.first}|${nc.second}"
                if (dao.fixtureCount(key, s) == 0) {
                    dao.insertMatches(Fixtures.generate(ts.map { it.id }, s, key))
                }
                val byId = ts.associateBy { it.id }
                // In ordine di giornata: gli infortunati saltano davvero i turni successivi
                val sims = dao.matchesOnce(key, s).filter { !it.played }.sortedBy { it.round }.mapNotNull { m ->
                    val h = byId[m.homeTeamId]
                    val a = byId[m.awayTeamId]
                    if (h == null || a == null) null else {
                        fun xi(id: Long) = cache[id].orEmpty().let {
                            if (id == me.id) Lineup.resolve(it, _formation.value).xi else Medical.available(it)
                        }
                        fun bench(id: Long, xi: List<Player>) =
                            if (id == me.id) Medical.available(cache[id].orEmpty()) - xi.toSet() else null
                        val hx = xi(h.id); val ax = xi(a.id)
                        val r = MatchEngine.simulate(
                            h, hx, a, ax,
                            if (h.id == me.id) _formation.value else formations.random(),
                            if (a.id == me.id) _formation.value else formations.random(),
                            Staffing.skill(staffBy, h.id, Staffing.ALLENATORE),
                            Staffing.skill(staffBy, a.id, Staffing.ALLENATORE),
                            homeMent = if (h.id == me.id) _mentality.value else null,
                            awayMent = if (a.id == me.id) _mentality.value else null,
                            homeBench = bench(h.id, hx), awayBench = bench(a.id, ax),
                            homePress = if (h.id == me.id) _pressing.value else null,
                            awayPress = if (a.id == me.id) _pressing.value else null,
                            homeLine = if (h.id == me.id) _line.value else null,
                            awayLine = if (a.id == me.id) _line.value else null
                        )
                        cache[h.id] = Medical.afterMatch(cache[h.id] ?: emptyList(), r.homePlayed,
                            Staffing.skill(staffBy, h.id, Staffing.PREPARATORE), rng,
                            planFor(h.id, me.id), Staffing.skill(staffBy, h.id, Staffing.ALLENATORE))
                        cache[a.id] = Medical.afterMatch(cache[a.id] ?: emptyList(), r.awayPlayed,
                            Staffing.skill(staffBy, a.id, Staffing.PREPARATORE), rng,
                            planFor(a.id, me.id), Staffing.skill(staffBy, a.id, Staffing.ALLENATORE))
                        m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true)
                    }
                }
                dao.updateMatches(sims)
                finals[nc] = standings(ts, dao.matchesOnce(key, s))
            }

            // 2) Premi, promozioni e retrocessioni (divisioni ordinate per comparsa nella lista)
            val moves = HashMap<Long, String>()
            val prizes = HashMap<Long, Long>()
            val posInfo = HashMap<Long, String>()
            val promoted = HashSet<Long>()
            val relegated = HashSet<Long>()
            leagues.keys.groupBy { it.first }.forEach { (_, keys) ->
                val ordered = keys.sortedBy { k -> leagues.getValue(k).minOf { it.id } }
                ordered.forEachIndexed { tier, k ->
                    val st = finals.getValue(k)
                    val size = st.size
                    val base = listOf(20_000_000L, 6_000_000L, 2_000_000L).getOrElse(tier) { 800_000L }
                    st.forEachIndexed { pos, row ->
                        prizes[row.team.id] = base * (size - 1 - pos) / (size - 1).coerceAtLeast(1)
                        posInfo[row.team.id] = "${pos + 1}º su $size in ${k.second}"
                    }
                    if (tier + 1 < ordered.size) {
                        val lk = ordered[tier + 1]
                        val low = finals.getValue(lk)
                        val kk = minOf(3, size / 4, low.size / 4)
                        st.takeLast(kk).forEach { moves[it.team.id] = lk.second; relegated += it.team.id }
                        low.take(kk).forEach { moves[it.team.id] = k.second; promoted += it.team.id }
                    }
                }
            }
            val updated = ArrayList<Player>()
            val removed = ArrayList<Player>()
            val added = ArrayList<Player>()
            val leavers = ArrayList<String>()
            var report = ""
            var meNet = 0L
            val newTeams = teamsNow.map { t ->
                val keep = ArrayList<Player>()
                (byTeam[t.id] ?: emptyList()).forEach { p ->
                    val a = Progression.advance(p, rng, Staffing.skill(staffBy, t.id, Staffing.ALLENATORE))
                    when {
                        a.age >= Progression.retireAge(a) -> {
                            removed += p
                            if (t.id == me.id) leavers += "${p.name} (ritiro)"
                        }
                        a.contract <= 0 && t.id == me.id -> {
                            removed += p
                            leavers += "${p.name} (contratto scaduto)"
                        }
                        else -> {
                            val f = if (a.contract <= 0) Progression.renewAI(a, rng) else a
                            updated += f
                            keep += f
                        }
                    }
                }
                val noKeeper = keep.none { it.role == "P" }
                repeat((16 - keep.size).coerceAtLeast(0)) { i ->
                    added += Progression.newYoung(t, rng, i == 0 && noKeeper)
                }
                val wages = keep.sumOf { it.salary } + (staffBy[t.id]?.sumOf { it.salary } ?: 0L)
                val gate = t.stadiumCapacity.toLong() * t.ticketPrice * 7 / 10 * 19
                val net = gate - wages * 4 / 10
                val newLeague = moves[t.id]
                if (t.id == me.id) {
                    meNet = net
                    val fate = when {
                        t.id in promoted -> "Promossa in $newLeague!"
                        t.id in relegated -> "Retrocessa in $newLeague."
                        else -> ""
                    }
                    report = "${posInfo[t.id] ?: ""}. $fate Premio ${money(prizes[t.id] ?: 0L)}, " +
                        "bilancio ${money(net)}."
                }
                t.copy(
                    budget = (t.budget + net + (prizes[t.id] ?: 0L)).coerceAtLeast(0),
                    campionato = newLeague ?: t.campionato
                )
            }
            dao.updatePlayers(updated)
            dao.deletePlayers(removed)
            dao.insertPlayers(added)
            dao.updateTeams(newTeams)
            newTeams.find { it.id == me.id }?.let { nt ->
                registra(me.id, "Bilancio di fine stagione (incassi, ingaggi)", meNet, nt.budget - (prizes[me.id] ?: 0L))
                (prizes[me.id] ?: 0L).takeIf { it != 0L }?.let { pr ->
                    registra(me.id, "Premio piazzamento", pr, nt.budget)
                }
            }

            // Staff: un anno di contratto in meno; chi arriva a zero lascia la squadra
            val staffNext = staffAll.map { it.copy(contract = it.contract - 1) }
            dao.updateStaffs(staffNext.filter { it.contract > 0 })
            dao.deleteStaffs(staffNext.filter { it.contract <= 0 })
            staffNext.filter { it.contract <= 0 && it.teamId == me.id }.forEach {
                leavers += "${it.name} (${Staffing.roleName(it.role).lowercase()}, contratto scaduto)"
            }
            // Il club torna senza figura: vivaio e scout riprendono l'abilità "vuoto"
            staffNext.filter { it.contract <= 0 }.forEach { syncTeamSkill(it.teamId, it.role, Staffing.VUOTO) }
            dao.clearYouths()
            dao.clearGoals()
            val y = prefs.getInt("season_year", 2026) + 1
            prefs.edit().putInt("season_year", y).apply()
            _season.value = seasonName(y)
            _notice.value = "Nuova stagione ${seasonName(y)}. $report" +
                (if (leavers.isNotEmpty()) " Lasciano: ${leavers.joinToString(", ")}." else "")
        }
    }

    fun selectTeam(team: Team) {
        selectedId.value = team.id
        prefs.edit().putLong("team_id", team.id).apply()
    }

    // ---- Salvataggi: 3 slot con copia completa della partita ----
    private val _slots = MutableStateFlow(SaveSlots.list(app))
    val slots: StateFlow<List<SlotInfo>> = _slots

    fun saveSlot(n: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val t = selectedTeam.value ?: return@launch
            try {
                SaveSlots.save(ctx, db, n, t.id, t.name, prefs.getInt("season_year", 2026), _season.value)
                _notice.value = "Partita salvata nello slot $n."
            } catch (e: Exception) {
                _notice.value = "Salvataggio non riuscito: ${e.message}"
            }
            _slots.value = SaveSlots.list(ctx)
        }
    }

    fun deleteSlot(n: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            SaveSlots.delete(ctx, n)
            _slots.value = SaveSlots.list(ctx)
            _notice.value = "Slot $n cancellato."
        }
    }

    /** Carica lo slot e riavvia l'app: il database in uso non si può sostituire a caldo. */
    fun loadSlot(n: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = try { SaveSlots.load(ctx, n) } catch (e: Exception) { false }
            if (!ok) {
                _notice.value = "Slot $n non caricabile."
                return@launch
            }
            val i = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
            i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            if (i != null) ctx.startActivity(i)
            Runtime.getRuntime().exit(0)
        }
    }
}
