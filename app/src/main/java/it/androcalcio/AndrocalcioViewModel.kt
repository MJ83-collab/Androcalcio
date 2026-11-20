package it.androcalcio

import it.androcalcio.engine.DefLine
import it.androcalcio.engine.Pressing
import it.androcalcio.engine.ShirtNumbers
import it.androcalcio.engine.Stadium
import it.androcalcio.engine.Insurance
import it.androcalcio.engine.Sponsorship
import it.androcalcio.engine.Evento
import it.androcalcio.engine.Scelta
import it.androcalcio.engine.Spogliatoio
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
import it.androcalcio.engine.Cup
import it.androcalcio.engine.Friendlies
import it.androcalcio.data.AlboEntry
import it.androcalcio.data.Stato
import it.androcalcio.data.StatoKeys
import it.androcalcio.data.setStatoInt
import it.androcalcio.data.statoInt
import it.androcalcio.engine.Club
import it.androcalcio.engine.ClubRequests
import it.androcalcio.engine.ClubOffer
import it.androcalcio.engine.ClubOffers
import it.androcalcio.engine.SponsorDeal
import it.androcalcio.engine.Sponsors
import it.androcalcio.engine.Strutture
import it.androcalcio.engine.Economia
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
import kotlinx.coroutines.flow.map
import it.androcalcio.engine.AssistantCoach
import it.androcalcio.engine.schemaAbituale
import it.androcalcio.engine.schemaInPartita
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

    /** Applica l'undici consigliato dall'allenatore in seconda. */
    /** Applica lo stile consigliato dall'allenatore in seconda: mentalità, pressing e linea. */
    fun applyAssistantTattica() {
        val s = assistantAdvice.value?.tattica
        if (s == null) {
            _notice.value = "Nessuna tattica consigliata: serve un allenatore in seconda e una prossima partita."
            return
        }
        setMentality(s.mentalita)
        setPressing(s.pressing)
        setLine(s.linea)
        _notice.value = "Tattica ${s.label.lowercase()} applicata: mentalità ${s.mentalita.label.lowercase()}, pressing ${s.pressing.label.lowercase()}, linea ${s.linea.label.lowercase()}."
    }

    fun applyAssistantXI() {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            val xi = assistantAdvice.value?.xi?.map { it.id }?.toSet()
            if (xi.isNullOrEmpty()) {
                _notice.value = "Non c'è un allenatore in seconda: nessun undici da applicare."
                return@launch
            }
            val ps = dao.playersOnce(me.id)
            dao.updatePlayers(ps.map { it.copy(starter = it.id in xi) })
            _notice.value = "Applicato l'undici consigliato dall'allenatore in seconda."
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

    /** Movimenti della stagione in corso della tua squadra (per il bilancio di stagione). */
    val movimentiStagione: StateFlow<List<Movimento>> =
        combine(selectedTeam, _season) { t, s -> t to s }
            .flatMapLatest { (t, s) -> if (t == null) flowOf(emptyList()) else dao.movimentiStagione(t.id, s) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private fun key(t: Team) = "${t.nazione}|${t.campionato}"

    /** Consigli dell'allenatore in seconda: undici in forma e notizie sul prossimo avversario. */
    val assistantAdvice: StateFlow<AssistantCoach.Advice?> by lazy {
        combine(selectedTeam, matches, players) { t, ms, _ -> t to ms }
            .map { (t, ms) -> if (t == null) null else buildAssistantAdvice(t, ms) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    }

    private suspend fun buildAssistantAdvice(me: Team, ms: List<MatchResult>): AssistantCoach.Advice {
        val vice = dao.staffOnce(me.id).firstOrNull { it.role == Staffing.ASSISTENTE }
        val mine = dao.playersOnce(me.id)
        val f = _formation.value
        val xi = AssistantCoach.startingXI(mine, f)
        val prossima = ms.filter { !it.played && (it.homeTeamId == me.id || it.awayTeamId == me.id) }
            .minByOrNull { it.round }
        val opp = prossima?.let { m -> dao.team(if (m.homeTeamId == me.id) m.awayTeamId else m.homeTeamId) }
        val notes = if (opp == null) emptyList() else AssistantCoach.opponentNotes(
            opp, dao.playersOnce(opp.id), mine,
            dao.staffOnce(opp.id).firstOrNull { it.role == Staffing.ALLENATORE }?.skill ?: Staffing.NEUTRAL,
            dao.staffOnce(me.id).firstOrNull { it.role == Staffing.ALLENATORE }?.skill ?: Staffing.NEUTRAL,
            f
        )
        val oppSchema = opp?.let { schemaAbituale(it.id) }
        val oppXI = if (opp == null || oppSchema == null) emptyList()
            else AssistantCoach.startingXI(dao.playersOnce(opp.id), oppSchema)
        val miaStile = Staffing.stile(mapOf(me.id to dao.staffOnce(me.id)), me.id)
        val consiglio = if (opp == null) null else AssistantCoach.tattica(
            dao.playersOnce(opp.id), mine, opp.id,
            dao.staffOnce(opp.id).firstOrNull { it.role == Staffing.ALLENATORE }?.skill ?: Staffing.NEUTRAL,
            dao.staffOnce(me.id).firstOrNull { it.role == Staffing.ALLENATORE }?.skill ?: Staffing.NEUTRAL,
            f, miaStile
        )
        return AssistantCoach.Advice(
            assistantName = vice?.name,
            xi = if (vice == null) emptyList() else xi,
            xiNotes = if (vice == null) emptyList() else AssistantCoach.xiNotes(mine, xi),
            opponent = opp?.name,
            opponentNotes = if (vice == null) emptyList() else notes,
            tattica = if (vice == null) null else consiglio?.first,
            tatticaNota = if (vice == null) null else consiglio?.second,
            opponentXI = oppXI,
            opponentSchema = oppSchema,
            opponentMedia = if (oppXI.isEmpty()) 0 else oppXI.map { it.overall }.average().toInt()
        )
    }

    val matches: StateFlow<List<MatchResult>> =
        combine(selectedTeam, _season) { t, s -> t to s }
            .flatMapLatest { (t, s) ->
                if (t == null) flowOf(emptyList()) else dao.matches(key(t), s)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Amichevoli di precampionato della tua squadra (stagione corrente). */
    val friendlyMatches: StateFlow<List<MatchResult>> =
        combine(selectedTeam, _season) { t, s -> t to s }
            .flatMapLatest { (t, s) ->
                if (t == null) flowOf(emptyList()) else dao.matches(Friendlies.key(t.id), s)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Coppa nazionale della tua squadra (tutte le divisioni della nazione). */
    val cupMatches: StateFlow<List<MatchResult>> =
        combine(selectedTeam, _season) { t, s -> t to s }
            .flatMapLatest { (t, s) ->
                if (t == null) flowOf(emptyList())
                else combine(dao.matches(Cup.key(t.nazione), s), dao.matches(Cup.superKey(t.nazione), s)) { c, sc -> c + sc }
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
                val ck = Cup.key(t.nazione)
                if (dao.fixtureCount(ck, s) == 0) {
                    val nation = dao.allTeams().filter { it.nazione == t.nazione }.map { it.id }
                    dao.insertMatches(Cup.firstStage(nation, s, ck, kotlin.random.Random.Default, t.nazione))
                }
            }
        }
    }

    /** Forza di ogni squadra: media dei valori dei giocatori (usata solo per le parità di coppa). */
    private suspend fun strengthOf(): Map<Long, Double> =
        dao.allPlayers().groupBy { it.teamId }
            .mapValues { (_, ps) -> if (ps.isEmpty()) 0.0 else ps.map { it.overall }.average() }

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
            val ms = dao.matchesOnce(key(t), _season.value) + dao.matchesOnce(Cup.key(t.nazione), _season.value)
            val teamsById = dao.allTeams().filter { it.nazione == t.nazione }.associateBy { it.id }
            val staffBy = dao.allStaff().groupBy { it.teamId }
            var playedNow = 0
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
                    if (hL != null) _formation.value else schemaInPartita(h.id, kotlin.random.Random.Default),
                    if (aL != null) _formation.value else schemaInPartita(a.id, kotlin.random.Random.Default),
                    Staffing.skill(staffBy, h.id, Staffing.ALLENATORE),
                    Staffing.skill(staffBy, a.id, Staffing.ALLENATORE),
                    homeMent = if (h.id == t.id) _mentality.value else null,
                    awayMent = if (a.id == t.id) _mentality.value else null,
                    homeBench = hL?.let { Medical.available(hp) - it.xi.toSet() },
                    awayBench = aL?.let { Medical.available(ap) - it.xi.toSet() },
                    homePress = if (h.id == t.id) _pressing.value else null,
                    awayPress = if (a.id == t.id) _pressing.value else null,
                    homeLine = if (h.id == t.id) _line.value else null,
                    awayLine = if (a.id == t.id) _line.value else null,
                    homeStile = if (h.id == t.id) null else Staffing.stile(staffBy, h.id),
                    awayStile = if (a.id == t.id) null else Staffing.stile(staffBy, a.id)
                )
                dao.updateMatch(m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true))
                playedNow++
                goalsAcc += r.events.filter { it.type == EventType.GOAL && it.playerId != 0L }.map {
                    Goal(season = _season.value, league = m.league, round = m.round,
                        teamId = if (it.home) m.homeTeamId else m.awayTeamId, playerId = it.playerId)
                }
                cache[h.id] = Medical.afterMatch(hp, r.homePlayed, Staffing.skill(staffBy, h.id, Staffing.PREPARATORE), rng,
                    planFor(h.id, t.id), Staffing.skill(staffBy, h.id, Staffing.ALLENATORE)).also { accrueInsurance(h.id, hp, it) }
                cache[a.id] = Medical.afterMatch(ap, r.awayPlayed, Staffing.skill(staffBy, a.id, Staffing.PREPARATORE), rng,
                    planFor(a.id, t.id), Staffing.skill(staffBy, a.id, Staffing.ALLENATORE)).also { accrueInsurance(a.id, ap, it) }
            }
            dao.updatePlayers(cache.values.flatten())
            dao.insertGoals(goalsAcc)
            ms.filter { it.round in rounds && it.id != skipId && (it.homeTeamId == t.id || it.awayTeamId == t.id) }
                .forEach { m0 -> dao.match(m0.id)?.let { dopoMiaPartita(t.id, it, evento = !all) } }
            if (lineupNotes.isNotEmpty()) _notice.value = lineupNotes.distinct().joinToString("\n")
            // Coppa: supplementari e rigori nelle parità, poi il turno successivo se è finito
            finalizeCup(t.nazione, _season.value, rng)
            val ck = Cup.key(t.nazione)
            dao.insertMatches(Cup.nextStage(dao.matchesOnce(ck, _season.value), teamsById.values.toList(),
                strengthOf(), _season.value, ck, t.nazione, kotlin.random.Random.Default))
            // Con "Fine stagione" si gioca fino in fondo, anche i turni di coppa sorteggiati adesso
            if (all && playedNow > 0) runRounds(true, skipId)
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
        val oppF = schemaInPartita(if (userHome) away.id else home.id, kotlin.random.Random.Default)
        val m = LiveMatch(
            home, if (userHome) res.xi else opp, away, if (userHome) opp else res.xi,
            if (userHome) _formation.value else oppF, if (userHome) oppF else _formation.value,
            Staffing.skill(staffBy, home.id, Staffing.ALLENATORE), Staffing.skill(staffBy, away.id, Staffing.ALLENATORE),
            if (userHome) _mentality.value else null, if (userHome) null else _mentality.value,
            if (userHome) myBench else null, if (userHome) null else myBench,
            manualHome = userHome, manualAway = !userHome,
            homePress = if (userHome) _pressing.value else null, awayPress = if (userHome) null else _pressing.value,
            homeLine = if (userHome) _line.value else null, awayLine = if (userHome) null else _line.value,
            homeStile = if (userHome) null else Staffing.stile(staffBy, home.id),
            awayStile = if (userHome) Staffing.stile(staffBy, away.id) else null
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
            val ms = dao.matchesOnce(key(t), _season.value) + dao.matchesOnce(Cup.key(t.nazione), _season.value)
            val round = ms.filter { !it.played }.minOfOrNull { it.round } ?: return@launch
            val mine = ms.firstOrNull { !it.played && it.round == round && (it.homeTeamId == t.id || it.awayTeamId == t.id) }
            runRounds(false, mine?.id)
            if (mine == null) { _notice.value = "In questa giornata la tua squadra riposa."; return@launch }
            val teamsById = dao.allTeams().filter { it.nazione == t.nazione }.associateBy { it.id }
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
            val lg = mr.league
            dao.updateMatch(mr.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true))
            dao.insertGoals(r.events.filter { it.type == EventType.GOAL && it.playerId != 0L }.map {
                Goal(season = _season.value, league = lg, round = mr.round,
                    teamId = if (it.home) mr.homeTeamId else mr.awayTeamId, playerId = it.playerId)
            })
            t?.let { dopoMiaPartita(it.id, mr.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals), evento = true) }
            val staffBy = dao.allStaff().groupBy { it.teamId }
            val rng = kotlin.random.Random.Default
            for (id in listOf(mr.homeTeamId, mr.awayTeamId)) {
                val played = if (id == mr.homeTeamId) r.homePlayed else r.awayPlayed
                val beforeP = dao.playersOnce(id)
                dao.updatePlayers(Medical.afterMatch(beforeP, played, Staffing.skill(staffBy, id, Staffing.PREPARATORE), rng,
                    planFor(id, t?.id), Staffing.skill(staffBy, id, Staffing.ALLENATORE)).also { accrueInsurance(id, beforeP, it) })
            }
        }
        if (mr != null && Cup.isCup(mr.round)) {
            // Partita di coppa: supplementari e rigori se in parità, poi il turno successivo se è finito
            selectedTeam.value?.let { t ->
                val fin = finalizeCup(t.nazione, _season.value, kotlin.random.Random.Default)
                fin.find { it.id == mr.id && it.homePens >= 0 }?.let {
                    _notice.value = "Parità dopo 90': supplementari e rigori, ${it.homePens}-${it.awayPens}."
                }
                val ck = Cup.key(t.nazione)
                val nation = dao.allTeams().filter { it.nazione == t.nazione }
                dao.insertMatches(Cup.nextStage(dao.matchesOnce(ck, _season.value), nation, strengthOf(),
                    _season.value, ck, t.nazione, kotlin.random.Random.Default))
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

    private fun myStrengthNow(): Int =
        players.value.map { it.overall }.sortedDescending().take(11).average().takeIf { !it.isNaN() }?.toInt() ?: 60

    private fun appealFor(p: Player): Double {
        val c = _club.value
        return Market.appeal(p, myStrengthNow(), c.reputazione, c.fiducia, c.tifosi, c.clima)
    }

    /** Ingaggio annuo che il giocatore chiederebbe alla tua squadra, per la durata indicata. */
    fun wageAsk(p: Player, years: Int = 3): Long = Market.askedWage(p, appealFor(p), years)

    /** Margine del budget ingaggi: budget ingaggi meno monte ingaggi attuale della rosa. */
    fun wageRoom(): Long = (selectedTeam.value?.wageBudget ?: 0L) - players.value.sumOf { it.salary }

    fun offer(p: Player, price: Long, wage: Long = wageAsk(p), years: Int = 3) {
        viewModelScope.launch {
            val buyer = selectedTeam.value ?: return@launch
            val d = Market.demand(p)
            val sellerSquad = dao.playersOnce(p.teamId)
            val key = sellerSquad.sortedByDescending { it.overall }.take(3).any { it.id == p.id }
            val appeal = appealFor(p)
            _offer.value = when {
                players.value.size >= Market.MAX_SQUAD ->
                    OfferResult.Rejected("Rosa piena (massimo ${Market.MAX_SQUAD} giocatori).")
                price > buyer.budget -> OfferResult.Rejected("Budget insufficiente.")
                wage > wageRoom() ->
                    OfferResult.Rejected("Oltre il budget ingaggi: restano ${money(wageRoom().coerceAtLeast(0))} l'anno.")
                key && price < (d * 1.4).toLong() ->
                    OfferResult.Rejected("${p.name} è un titolare chiave: il club non lo cede sotto ${money((d * 1.4).toLong())}.")
                price < (d * 0.85).toLong() -> OfferResult.Rejected("Offerta troppo bassa: il club non tratta.")
                price < d -> OfferResult.Counter(p, d, wage, years)
                Market.refuses(p, myStrengthNow(), appeal) ->
                    OfferResult.Rejected("${p.name} rifiuta: preferisce una squadra più forte e vincente.")
                wage < Market.askedWage(p, appeal, years) -> {
                    val ask = Market.askedWage(p, appeal, years)
                    if (wage >= (ask * 0.9).toLong()) OfferResult.WageCounter(p, price, ask, years)
                    else OfferResult.Rejected("${p.name} chiede almeno ${money(ask)} l'anno: trattativa chiusa.")
                }
                else -> {
                    val seller = dao.team(p.teamId)
                    dao.updateTeam(buyer.copy(budget = buyer.budget - price))
                    dao.updateTeam(seller.copy(budget = seller.budget + price))
                    dao.updatePlayer(p.copy(teamId = buyer.id, salary = wage, contract = years))
                    registra(buyer.id, "Acquisto ${p.name}", -price, buyer.budget - price)
                    OfferResult.Accepted("${p.name} è tuo per ${money(price)}: ${money(wage)} l'anno per $years ${if (years == 1) "anno" else "anni"}.")
                }
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
            dao.insertYouths(Youths.generate(t, s, bonusVivaio = Strutture.bonusVivaio(dao.statoInt(Strutture.VIVAIO))))
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

    /** Stato dell'assicurazione infortuni della tua squadra (salvato nelle preferenze). */
    private val _insurance = MutableStateFlow(prefs.getInt("assicurazione", Insurance.NONE).coerceIn(0, 2))
    val insurance: StateFlow<Int> = _insurance
    fun setInsurance(level: Int) {
        _insurance.value = level
        prefs.edit().putInt("assicurazione", level).apply()
    }

    /** Dopo una gara: accumula il rimborso per gli infortuni nuovi della tua squadra (pagato a fine stagione). */
    private fun accrueInsurance(teamId: Long, before: List<Player>, after: List<Player>) {
        val lvl = _insurance.value
        if (lvl == Insurance.NONE || teamId != selectedTeam.value?.id) return
        val prev = before.associateBy { it.id }
        val add = after.sumOf { a ->
            val b = prev[a.id]
            if (b != null && b.injuredRounds == 0 && a.injuredRounds > 0) Insurance.refund(a.salary, a.injuredRounds, lvl) else 0L
        }
        if (add > 0) prefs.edit().putLong("ins_pending", prefs.getLong("ins_pending", 0L) + add).apply()
    }

    /** Sceglie lo sponsor della tua squadra: l'incasso arriva a fine stagione. */
    fun setSponsor(name: String) {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            dao.updateTeam(me.copy(sponsor = name))
            _notice.value = "Sponsor: $name (${money(Sponsorship.annual(name))} l'anno)."
        }
    }

    /** Amplia lo stadio della tua squadra di un blocco di posti, pagato dal budget. */
    fun expandStadium() {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            if (!Stadium.canExpand(me.stadiumCapacity)) {
                _notice.value = "Stadio al massimo (${Stadium.MAX_CAPACITY} posti)."
                return@launch
            }
            if (Stadium.blockCost > me.budget) {
                _notice.value = "Budget insufficiente (servono ${money(Stadium.blockCost)})."
                return@launch
            }
            dao.updateTeam(me.copy(stadiumCapacity = me.stadiumCapacity + Stadium.STEP, budget = me.budget - Stadium.blockCost))
            registra(me.id, "Ampliamento stadio (+${Stadium.STEP} posti)", -Stadium.blockCost, me.budget - Stadium.blockCost)
            _notice.value = "Stadio ampliato a ${me.stadiumCapacity + Stadium.STEP} posti."
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
        prefs.edit().remove("team_id").putBoolean("partita_iniziata", false).apply()
        selectedId.value = null
        _started.value = false
        prefs.edit().putInt("season_year", 2026).apply()
        _season.value = seasonName(2026)
        SeedData.insertInto(ctx, dao)
        assegnaNumeri(0L)
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
    /** Ruolo in cui il giocatore scende in campo (uno dei tre ruoli: naturale o affine). */
    fun setRuoloPartita(p: Player, ruolo: String) { viewModelScope.launch { dao.updatePlayer(p.copy(ruoloPartita = ruolo)) } }
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
    /** Supplementari e rigori per le partite di coppa giocate in parità (una sola volta per partita). */
    private suspend fun finalizeCup(nazione: String, s: String, rng: kotlin.random.Random): List<MatchResult> =
        finalizeKey(Cup.key(nazione), s, rng)

    /** Come finalizeCup, per una qualunque chiave di competizione a eliminazione (coppa o supercoppa). */
    private suspend fun finalizeKey(key: String, s: String, rng: kotlin.random.Random): List<MatchResult> {
        val all = dao.matchesOnce(key, s)
        val changed = all.filter { it.played && it.homePens < 0 }.mapNotNull { m ->
            Cup.finalize(m, all, rng).takeIf { it != m }
        }
        if (changed.isNotEmpty()) dao.updateMatches(changed)
        return changed
    }

    /** Simula una partita di coppa in chiusura stagione: stesse regole delle giornate di campionato. */
    private suspend fun simCupMatch(
        h: Team, a: Team, me: Team, cache: HashMap<Long, List<Player>>,
        staffBy: Map<Long, List<Staff>>, rng: kotlin.random.Random
    ): MatchReport {
        val formations = Formation.values()
        fun xi(id: Long) = cache[id].orEmpty().let {
            if (id == me.id) Lineup.resolve(it, _formation.value).xi else Medical.available(it)
        }
        fun bench(id: Long, xi: List<Player>) =
            if (id == me.id) Medical.available(cache[id].orEmpty()) - xi.toSet() else null
        val hx = xi(h.id); val ax = xi(a.id)
        val r = MatchEngine.simulate(
            h, hx, a, ax,
            if (h.id == me.id) _formation.value else schemaInPartita(h.id, kotlin.random.Random.Default),
            if (a.id == me.id) _formation.value else schemaInPartita(a.id, kotlin.random.Random.Default),
            Staffing.skill(staffBy, h.id, Staffing.ALLENATORE),
            Staffing.skill(staffBy, a.id, Staffing.ALLENATORE),
            homeMent = if (h.id == me.id) _mentality.value else null,
            awayMent = if (a.id == me.id) _mentality.value else null,
            homeBench = bench(h.id, hx), awayBench = bench(a.id, ax),
            homePress = if (h.id == me.id) _pressing.value else null,
            awayPress = if (a.id == me.id) _pressing.value else null,
            homeLine = if (h.id == me.id) _line.value else null,
            awayLine = if (a.id == me.id) _line.value else null,
            homeStile = if (h.id == me.id) null else Staffing.stile(staffBy, h.id),
            awayStile = if (a.id == me.id) null else Staffing.stile(staffBy, a.id),
            rng = rng
        )
        cache[h.id] = Medical.afterMatch(cache[h.id] ?: emptyList(), r.homePlayed,
            Staffing.skill(staffBy, h.id, Staffing.PREPARATORE), rng,
            planFor(h.id, me.id), Staffing.skill(staffBy, h.id, Staffing.ALLENATORE))
            .also { accrueInsurance(h.id, cache[h.id] ?: emptyList(), it) }
        cache[a.id] = Medical.afterMatch(cache[a.id] ?: emptyList(), r.awayPlayed,
            Staffing.skill(staffBy, a.id, Staffing.PREPARATORE), rng,
            planFor(a.id, me.id), Staffing.skill(staffBy, a.id, Staffing.ALLENATORE))
            .also { accrueInsurance(a.id, cache[a.id] ?: emptyList(), it) }
        return r
    }

    /**
     * Numeri di maglia di inizio stagione, per tutte le squadre. Chi ha già un numero lo mantiene.
     * Restituisce la nota da mostrare: chi li ha assegnati (allenatore in seconda o società).
     */
    private suspend fun assegnaNumeri(meId: Long): String {
        val updated = dao.allPlayers().groupBy { it.teamId }.flatMap { (_, ps) ->
            val nums = ShirtNumbers.assign(ps)
            ps.filter { nums[it.id] != it.numero }.map { it.copy(numero = nums[it.id] ?: 0) }
        }
        if (updated.isNotEmpty()) dao.updatePlayers(updated)
        val vice = meId != 0L && dao.staffOnce(meId).any { it.role == Staffing.ASSISTENTE }
        return if (vice) "I numeri di maglia li ha assegnati l'allenatore in seconda."
        else "Senza allenatore in seconda, i numeri di maglia li ha assegnati la società."
    }

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
                            if (h.id == me.id) _formation.value else schemaInPartita(h.id, kotlin.random.Random.Default),
                            if (a.id == me.id) _formation.value else schemaInPartita(a.id, kotlin.random.Random.Default),
                            Staffing.skill(staffBy, h.id, Staffing.ALLENATORE),
                            Staffing.skill(staffBy, a.id, Staffing.ALLENATORE),
                            homeMent = if (h.id == me.id) _mentality.value else null,
                            awayMent = if (a.id == me.id) _mentality.value else null,
                            homeBench = bench(h.id, hx), awayBench = bench(a.id, ax),
                            homePress = if (h.id == me.id) _pressing.value else null,
                            awayPress = if (a.id == me.id) _pressing.value else null,
                            homeLine = if (h.id == me.id) _line.value else null,
                            awayLine = if (a.id == me.id) _line.value else null,
                            homeStile = if (h.id == me.id) null else Staffing.stile(staffBy, h.id),
                            awayStile = if (a.id == me.id) null else Staffing.stile(staffBy, a.id)
                        )
                        cache[h.id] = Medical.afterMatch(cache[h.id] ?: emptyList(), r.homePlayed,
                            Staffing.skill(staffBy, h.id, Staffing.PREPARATORE), rng,
                            planFor(h.id, me.id), Staffing.skill(staffBy, h.id, Staffing.ALLENATORE))
                            .also { accrueInsurance(h.id, cache[h.id] ?: emptyList(), it) }
                        cache[a.id] = Medical.afterMatch(cache[a.id] ?: emptyList(), r.awayPlayed,
                            Staffing.skill(staffBy, a.id, Staffing.PREPARATORE), rng,
                            planFor(a.id, me.id), Staffing.skill(staffBy, a.id, Staffing.ALLENATORE))
                            .also { accrueInsurance(a.id, cache[a.id] ?: emptyList(), it) }
                        m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true)
                    }
                }
                dao.updateMatches(sims)
                finals[nc] = standings(ts, dao.matchesOnce(key, s))
            }

            // 1b) Coppe nazionali: tutti i turni, con i successivi sorteggiati man mano
            val strength = strengthOf()
            for (nazione in teamsNow.map { it.nazione }.distinct()) {
                val nTeams = teamsNow.filter { it.nazione == nazione }
                val ck = Cup.key(nazione)
                if (dao.fixtureCount(ck, s) == 0) {
                    dao.insertMatches(Cup.firstStage(nTeams.map { it.id }, s, ck, rng, nazione))
                }
                val byId = nTeams.associateBy { it.id }
                while (true) {
                    val pending = dao.matchesOnce(ck, s).filter { !it.played }.sortedBy { it.round }
                    if (pending.isEmpty()) break
                    val done = pending.mapNotNull { m ->
                        val h = byId[m.homeTeamId] ?: return@mapNotNull null
                        val a = byId[m.awayTeamId] ?: return@mapNotNull null
                        val r = simCupMatch(h, a, me, cache, staffBy, rng)
                        m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true)
                    }
                    if (done.isEmpty()) break
                    dao.updateMatches(done)
                    finalizeCup(nazione, s, rng)
                    dao.insertMatches(Cup.nextStage(dao.matchesOnce(ck, s), nTeams, strength, s, ck,
                        nazione, rng))
                }
            }

            // 1c) Supercoppa del paese: a fine coppa; Spagna = final four (semifinali + finale)
            for (nazione in teamsNow.map { it.nazione }.distinct()) {
                val nTeams = teamsNow.filter { it.nazione == nazione }
                val byId = nTeams.associateBy { it.id }
                val sk = Cup.superKey(nazione)
                val fin = Cup.finalMatch(dao.matchesOnce(Cup.key(nazione), s)) ?: continue
                val cupWinner = Cup.winnerOfTie(listOf(fin), strength)
                val cupRunner = if (cupWinner == fin.homeTeamId) fin.awayTeamId else fin.homeTeamId
                val firstDiv = leagues.keys.firstOrNull { it.first == nazione }?.let { finals[it] }
                val leagueTop = firstDiv.orEmpty().map { it.team.id }
                if (dao.fixtureCount(sk, s) == 0) {
                    dao.insertMatches(Cup.superFirst(nazione, s, sk, leagueTop, cupWinner, cupRunner))
                }
                while (true) {
                    val all = dao.matchesOnce(sk, s)
                    val pending = all.filter { !it.played }.sortedBy { it.round }
                    if (pending.isEmpty()) {
                        val semis = all.filter { Cup.stageOf(it.round) == Cup.SUPER_STAGE }
                        val hasFinal = all.any { Cup.stageOf(it.round) == Cup.SUPER_FINAL_STAGE }
                        if (nazione == "Spagna" && semis.size == 2 && !hasFinal) {
                            val w = semis.map { Cup.winnerOfTie(listOf(it), strength) }
                            dao.insertMatches(listOf(Cup.superFinal(s, sk, w[0], w[1])))
                            continue
                        }
                        break
                    }
                    val done = pending.mapNotNull { m ->
                        val h = byId[m.homeTeamId] ?: return@mapNotNull null
                        val a = byId[m.awayTeamId] ?: return@mapNotNull null
                        val r = simCupMatch(h, a, me, cache, staffBy, rng)
                        m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true)
                    }
                    if (done.isEmpty()) break
                    dao.updateMatches(done)
                    finalizeKey(sk, s, rng)
                }
            }

            // 1d) Albo d'oro: campioni di ogni divisione, coppa e supercoppa della stagione
            val teamById = teamsNow.associateBy { it.id }
            val alboRows = ArrayList<AlboEntry>()
            leagues.keys.forEach { k ->
                finals[k]?.firstOrNull()?.let {
                    alboRows += AlboEntry(season = s, nazione = k.first, competizione = "Campionato ${k.second}",
                        vincitore = it.team.name)
                }
            }
            for (nazione in teamsNow.map { it.nazione }.distinct()) {
                for ((label, key) in listOf("Coppa" to Cup.key(nazione), "Supercoppa" to Cup.superKey(nazione))) {
                    val fin = Cup.finalMatch(dao.matchesOnce(key, s)) ?: continue
                    val w = Cup.winnerOfTie(listOf(fin), strength)
                    teamById[w]?.let { alboRows += AlboEntry(season = s, nazione = nazione, competizione = label, vincitore = it.name) }
                }
            }
            dao.deleteAlboSeason(s)
            dao.insertAlbo(alboRows)

            // 1e) Fiducia della dirigenza, umore dei tifosi e reputazione dell'allenatore
            val myKey = leagues.entries.firstOrNull { e -> e.value.any { it.id == me.id } }?.key
            val myTable = myKey?.let { finals[it] }.orEmpty()
            val myPos = myTable.indexOfFirst { it.team.id == me.id } + 1
            if (myPos > 0) {
                val strs = myTable.associate { r ->
                    r.team.id to cache[r.team.id].orEmpty().map { it.overall }.sortedDescending().take(11).average()
                }
                val exp = Club.expectedRank(strs, me.id)
                val cupWon = alboRows.any { it.competizione == "Coppa" && it.vincitore == me.name }
                val champion = myPos == 1
                val titles = alboRows.count { it.vincitore == me.name }
                val fid = dao.statoInt(StatoKeys.FIDUCIA) + Club.fiduciaDelta(myPos, exp, cupWon, champion)
                dao.setStatoInt(StatoKeys.FIDUCIA, fid)
                dao.setStatoInt(StatoKeys.TIFOSI, dao.statoInt(StatoKeys.TIFOSI) + Club.tifosiDelta(myPos, exp, champion))
                dao.setStatoInt(StatoKeys.REPUTAZIONE, dao.statoInt(StatoKeys.REPUTAZIONE) +
                    Club.reputazioneDelta(myPos, exp, titles))
                val fired = Club.shouldFire(fid)
                dao.setStato(Stato("esonero", if (fired) "1" else "0"))
                val allStr = cache.mapValues { (_, ps) -> ps.map { it.overall }.sortedDescending().take(11).average() }
                dao.setStato(Stato("offerte", ClubOffer.encode(ClubOffers.make(allStr,
                    teamsNow.associate { it.id to it.budget }, me.id, dao.statoInt(StatoKeys.REPUTAZIONE), fired, rng))))
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
                        val frac = (size - 1 - pos).toDouble() / (size - 1).coerceAtLeast(1)
                        prizes[row.team.id] = (base * frac).toLong() +
                            (Economia.DIRITTI_TV.getOrElse(tier) { 600_000L } * Economia.quotaTv(frac)).toLong()   // premio + diritti TV
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
            // Promozione o retrocessione incidono sulla reputazione dell'allenatore
            if (me.id in promoted) dao.setStatoInt(StatoKeys.REPUTAZIONE, dao.statoInt(StatoKeys.REPUTAZIONE) + 4)
            if (me.id in relegated) dao.setStatoInt(StatoKeys.REPUTAZIONE, dao.statoInt(StatoKeys.REPUTAZIONE) - 8)
            val updated = ArrayList<Player>()
            val removed = ArrayList<Player>()
            val added = ArrayList<Player>()
            val leavers = ArrayList<String>()
            var report = ""
            var meNet = 0L
            // Voci economiche della tua squadra, lette una volta sola (le sim sono già concluse)
            val insPending = prefs.getLong("ins_pending", 0L)
            prefs.edit().putLong("ins_pending", 0L).apply()
            val insPremium = Insurance.premium(_insurance.value)
            val deals = SponsorDeal.decode(dao.stato("sponsor"))
            val meSp = deals.sumOf { it.annual }
            val coach = coachSalary()
            val dealsNext = deals.map { it.copy(years = it.years - 1) }
            dao.setStato(Stato("sponsor", SponsorDeal.encode(dealsNext.filter { it.years > 0 })))
            val expired = dealsNext.filter { it.years <= 0 }.map { it.name }
            val meMc = Sponsorship.merch(me.stadiumCapacity)
            val livAll = dao.statoInt(Strutture.ALLENAMENTO)
            val upkeep = Strutture.manutenzione(livAll) + Strutture.manutenzione(dao.statoInt(Strutture.VIVAIO))
            val newTeams = teamsNow.map { t ->
                val keep = ArrayList<Player>()
                (byTeam[t.id] ?: emptyList()).forEach { p ->
                    val a = Progression.advance(p, rng, Staffing.skill(staffBy, t.id, Staffing.ALLENATORE),
                        if (t.id == me.id) Strutture.bonusAllenamento(livAll) else 0)
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
                val lg = teamsNow.filter { it.campionato == t.campionato }.map { x -> x.id to
                    cache[x.id].orEmpty().map { it.overall }.sortedDescending().take(11).average() }
                val lo = lg.minOf { it.second }; val hi = lg.maxOf { it.second }
                val rel = if (hi > lo) ((lg.firstOrNull { it.first == t.id }?.second ?: lo) - lo) / (hi - lo) else 0.5
                val gate = (t.stadiumCapacity.toLong() * t.ticketPrice * Economia.riempimento(rel) * 19).toLong()
                val net = gate - wages   // monte ingaggi intero (scala reale)
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
                    budget = (t.budget + net + (prizes[t.id] ?: 0L) +
                        (if (t.id == me.id) meSp + meMc - insPremium + insPending - coach - upkeep else 0L)).coerceAtLeast(0),
                    campionato = newLeague ?: t.campionato
                )
            }
            dao.updatePlayers(updated)
            dao.deletePlayers(removed)
            dao.insertPlayers(added)
            dao.updateTeams(newTeams)
            newTeams.find { it.id == me.id }?.let { nt ->
                val pr = prizes[me.id] ?: 0L
                val lines = buildList {
                    add("Bilancio di fine stagione (incassi, ingaggi)" to meNet)
                    if (pr != 0L) add("Premio piazzamento" to pr)
                    if (meSp > 0) add("Sponsor (${deals.size} contratti)" to meSp)
                    add("Ingaggio allenatore" to -coach)
                    if (upkeep > 0) add("Manutenzione strutture" to -upkeep)
                    add("Merchandising" to meMc)
                    if (insPremium > 0) add("Premio assicurazione" to -insPremium)
                    if (insPending > 0) add("Rimborso infortuni" to insPending)
                }
                var saldo = nt.budget - lines.sumOf { it.second }
                lines.forEach { (label, importo) ->
                    saldo += importo
                    registra(me.id, label, importo, saldo)
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
            val numeri = assegnaNumeri(me.id)

            refreshSponsorOffers(selectedTeam.value?.id ?: me.id)
            refreshClub()
            _notice.value = "Nuova stagione ${seasonName(y)}. $report $numeri" +
                (if (expired.isNotEmpty()) " Sponsor scaduti: ${expired.joinToString(", ")}." else "") +
                (if (leavers.isNotEmpty()) " Lasciano: ${leavers.joinToString(", ")}." else "")
        }
    }

    /** Albo d'oro: campioni per stagione. */
    val albo: StateFlow<List<AlboEntry>> =
        dao.albo().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Chiede un'amichevole per uno dei due posti: l'avversario può accettare o rifiutare. */
    fun requestFriendly(opponentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val s = _season.value
            val booked = dao.matchesOnce(Friendlies.key(me.id), s)
            if (booked.size >= Friendlies.COUNT) { _notice.value = "Hai già due amichevoli."; return@launch }
            if (booked.any { it.homeTeamId == opponentId || it.awayTeamId == opponentId }) {
                _notice.value = "Squadra già impegnata in un'amichevole."; return@launch
            }
            val opp = dao.allTeams().firstOrNull { it.id == opponentId } ?: return@launch
            val strength = strengthOf()
            if (!Friendlies.accepts(strength[me.id] ?: 0.0, strength[opp.id] ?: 0.0, kotlin.random.Random.Default)) {
                _notice.value = "${opp.name} ha rifiutato l'amichevole."; return@launch
            }
            dao.insertMatches(listOf(Friendlies.fixture(me.id, opp.id, booked.size, s)))
            _notice.value = "${opp.name} ha accettato: amichevole fissata."
        }
    }

    /** Gioca le amichevoli di precampionato fissate e ancora da giocare. */
    fun playFriendlies() {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val s = _season.value
            val pending = dao.matchesOnce(Friendlies.key(me.id), s).filter { !it.played }
            if (pending.isEmpty()) { _notice.value = "Nessuna amichevole da giocare."; return@launch }
            val pool = dao.allTeams()
            val byIdAll = pool.associateBy { it.id }
            val myTeam = byIdAll[me.id] ?: return@launch
            val rng = kotlin.random.Random.Default
            val fresh = HashMap(dao.allPlayers().groupBy { it.teamId })
            val staffNew = dao.allStaff().groupBy { it.teamId }
            val done = pending.sortedBy { it.round }.mapNotNull { m ->
                val h = byIdAll[m.homeTeamId] ?: return@mapNotNull null
                val a = byIdAll[m.awayTeamId] ?: return@mapNotNull null
                val r = simCupMatch(h, a, myTeam, fresh, staffNew, rng)
                m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true)
            }
            dao.updateMatches(done)
            dao.updatePlayers(fresh.values.flatten())
            val gate = done.filter { it.homeTeamId == me.id }
                .sumOf { myTeam.stadiumCapacity.toLong() * myTeam.ticketPrice * 7 / 10 }
            if (gate > 0) {
                val nt = myTeam.copy(budget = myTeam.budget + gate)
                dao.updateTeam(nt)
                registra(me.id, "Incasso amichevole di precampionato", gate, nt.budget)
            }
            _notice.value = "Amichevoli giocate."
        }
    }

    // ---- Nuova partita: scelta di nazione, campionato e squadra ----
    // Le installazioni già in uso (con una squadra salvata) risultano come partita già iniziata.
    private val _started = MutableStateFlow(
        app.getSharedPreferences("androcalcio", 0).let { it.getBoolean("partita_iniziata", it.contains("team_id")) })
    val started: StateFlow<Boolean> = _started

    /** Forza (media dei migliori 11) e monte ingaggi attuale di ogni squadra, per la schermata di scelta. */
    data class TeamInfo(val strength: Int, val wages: Long)
    val teamInfo: StateFlow<Map<Long, TeamInfo>> = dao.allPlayersFlow().map { all ->
        all.groupBy { it.teamId }.mapValues { (_, ps) ->
            TeamInfo(ps.map { it.overall }.sortedDescending().take(11).average().toInt(), ps.sumOf { it.salary })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // ---- Dirigenza: fiducia, tifosi, reputazione e richieste di fondi ----
    data class ClubState(val fiducia: Int, val tifosi: Int, val reputazione: Int, val clima: Int,
                         val richieste: Set<String>, val esonero: Boolean,
                         val offerte: List<ClubOffer> = emptyList(),
                         val sponsor: List<SponsorDeal> = emptyList(), val sponsorOfferte: List<SponsorDeal> = emptyList(),
                         val stipendioAllenatore: Long = 0L, val livAllenamento: Int = 1, val livVivaio: Int = 1)
    private val _club = MutableStateFlow(ClubState(60, 60, 30, 60, emptySet(), false))
    val club: StateFlow<ClubState> = _club

    private suspend fun refreshClub() {
        val year = prefs.getInt("season_year", 2026).toString()
        _club.value = ClubState(
            dao.statoInt(StatoKeys.FIDUCIA), dao.statoInt(StatoKeys.TIFOSI),
            dao.statoInt(StatoKeys.REPUTAZIONE), dao.statoInt(StatoKeys.CLIMA),
            ClubRequests.TIPI.filter { dao.stato(StatoKeys.RICHIESTA + it) == year }.toSet(),
            dao.stato("esonero") == "1", ClubOffer.decode(dao.stato("offerte")),
            SponsorDeal.decode(dao.stato("sponsor")), SponsorDeal.decode(dao.stato("sponsor_offerte")),
            coachSalary(), dao.statoInt(Strutture.ALLENAMENTO), dao.statoInt(Strutture.VIVAIO))
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Partite già in corso: prime offerte sponsor se non ne sono mai arrivate
            val tid = prefs.getLong("team_id", -1L)
            if (tid > 0 && dao.stato("sponsor_offerte") == null && dao.stato("sponsor") == null) refreshSponsorOffers(tid)
            refreshClub()
        }
    }

    /** Richiesta di fondi alla dirigenza: una per tipo e per stagione; accettata in base a fiducia e tifosi. */
    fun requestFunds(tipo: String, importo: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val c = _club.value
            val year = prefs.getInt("season_year", 2026).toString()
            when {
                dao.stato(StatoKeys.RICHIESTA + tipo) == year ->
                    _notice.value = "Per $tipo hai già chiesto fondi in questa stagione."
                !Club.canRequestFunds(c.fiducia, c.tifosi) ->
                    _notice.value = "La dirigenza rifiuta: fiducia o appoggio dei tifosi troppo bassi."
                importo > Club.maxFunds(c.fiducia) ->
                    _notice.value = "La dirigenza rifiuta: massimo ${money(Club.maxFunds(c.fiducia))} con questa fiducia."
                else -> {
                    dao.setStato(Stato(StatoKeys.RICHIESTA + tipo, year))
                    val nuovo = me.budget + importo
                    dao.updateTeam(me.copy(budget = nuovo))
                    registra(me.id, "Fondi dirigenza ($tipo)", importo, nuovo)
                    _notice.value = "Accettata: ${money(importo)} per $tipo."
                }
            }
            refreshClub()
        }
    }

    /** Accetta un'offerta di panchina; con `chiediGaranzie` chiedi il 50% in più (serve reputazione ≥ 50, altrimenti salta). */
    fun acceptOffer(o: ClubOffer, chiediGaranzie: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val c = _club.value
            val rest = c.offerte.filter { it.teamId != o.teamId }
            if (chiediGaranzie && c.reputazione < ClubOffers.RICHIESTA_GARANZIE_REP_MIN) {
                dao.setStato(Stato("offerte", ClubOffer.encode(rest)))
                _notice.value = "Il club ritira l'offerta: la tua reputazione non permette di chiedere garanzie."
                refreshClub(); return@launch
            }
            val team = dao.team(o.teamId)
            val garanzia = if (chiediGaranzie) o.garanzia * 3 / 2 else o.garanzia
            dao.updateTeam(team.copy(budget = team.budget + garanzia))
            dao.setStato(Stato("stipendio_allenatore", o.stipendio.toString()))
            dao.setStato(Stato("esonero", "0")); dao.setStato(Stato("offerte", ""))
            dao.setStatoInt(StatoKeys.FIDUCIA, 60); dao.setStatoInt(StatoKeys.TIFOSI, 60); dao.setStatoInt(StatoKeys.CLIMA, 60)
            selectTeam(team)
            _notice.value = "Nuova panchina: ${team.name}. Garanzia ${money(garanzia)}, ingaggio ${money(o.stipendio)} l'anno."
            refreshClub()
        }
    }

    /** Ingaggio annuo dell'allenatore: dal contratto firmato, altrimenti in base alla reputazione. */
    private suspend fun coachSalary(): Long =
        dao.stato("stipendio_allenatore")?.toLongOrNull() ?: (200_000L + dao.statoInt(StatoKeys.REPUTAZIONE) * 20_000L)

    private suspend fun myStrength(teamId: Long): Int =
        dao.playersOnce(teamId).map { it.overall }.sortedDescending().take(11).average().takeIf { !it.isNaN() }?.toInt() ?: 60

    /** Nuove offerte sponsor per lo slot libero (se ce n'è uno). */
    private suspend fun refreshSponsorOffers(teamId: Long) {
        val active = SponsorDeal.decode(dao.stato("sponsor"))
        val fama = Sponsors.fama(dao.statoInt(StatoKeys.TIFOSI), dao.statoInt(StatoKeys.REPUTAZIONE))
        dao.setStato(Stato("sponsor_offerte", SponsorDeal.encode(
            Sponsors.offers(active, myStrength(teamId), fama, kotlin.random.Random.Default))))
    }

    fun acceptSponsor(d: SponsorDeal) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val active = SponsorDeal.decode(dao.stato("sponsor"))
            if (active.size >= Sponsors.MAX_ATTIVI) { _notice.value = "Hai già ${Sponsors.MAX_ATTIVI} sponsor attivi."; return@launch }
            val list = active + d
            dao.setStato(Stato("sponsor", SponsorDeal.encode(list)))
            if (active.isEmpty()) dao.updateTeam(me.copy(sponsor = d.name))   // il primo è lo sponsor di maglia
            refreshSponsorOffers(me.id)
            _notice.value = "Firmato con ${d.name}: ${money(d.annual)} l'anno per ${d.years} ${if (d.years == 1) "anno" else "anni"}."
            refreshClub()
        }
    }

    fun rejectSponsors() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.setStato(Stato("sponsor_offerte", ""))
            _notice.value = "Offerte rifiutate: ne arriveranno altre a fine stagione."
            refreshClub()
        }
    }

    // ---- Spogliatoio: clima, tifosi a ogni giornata ed eventi con scelte ----
    private val _evento = MutableStateFlow<Evento?>(null)
    val evento: StateFlow<Evento?> = _evento

    private suspend fun dopoMiaPartita(me: Long, m: MatchResult, evento: Boolean) {
        val gf = if (m.homeTeamId == me) m.homeGoals else m.awayGoals
        val gs = if (m.homeTeamId == me) m.awayGoals else m.homeGoals
        val (dc, dt) = Spogliatoio.dopoPartita(gf, gs)
        val clima = (dao.statoInt(StatoKeys.CLIMA) + dc).coerceIn(0, 100)
        dao.setStatoInt(StatoKeys.CLIMA, clima)
        dao.setStatoInt(StatoKeys.TIFOSI, dao.statoInt(StatoKeys.TIFOSI) + dt)
        val spinta = Spogliatoio.spintaMorale(clima)
        val rosa = dao.playersOnce(me)
        if (spinta != 0) dao.updatePlayers(rosa.map { it.copy(morale = (it.morale + spinta).coerceIn(0, 100)) })
        if (evento && _evento.value == null) {
            val prep = dao.allStaff().any { it.teamId == me && it.role == Staffing.PREPARATORE }
            _evento.value = Spogliatoio.evento(rosa, clima, dao.statoInt(StatoKeys.FIDUCIA),
                dao.statoInt(StatoKeys.TIFOSI), gf < gs, prep, kotlin.random.Random.Default)
        }
        refreshClub()
    }

    fun scegli(e: Evento, c: Scelta) {
        _evento.value = null
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val rosa = dao.playersOnce(me.id)
            dao.updatePlayers(rosa.map { p ->
                val dm = if (p.id in e.giocatori) c.morale else if (e.giocatori.isEmpty()) c.morale else 0
                p.copy(morale = (p.morale + dm).coerceIn(0, 100), condition = (p.condition + c.condizione).coerceIn(0, 100))
            })
            dao.setStatoInt(StatoKeys.CLIMA, dao.statoInt(StatoKeys.CLIMA) + c.clima)
            dao.setStatoInt(StatoKeys.FIDUCIA, dao.statoInt(StatoKeys.FIDUCIA) + c.fiducia)
            dao.setStatoInt(StatoKeys.TIFOSI, dao.statoInt(StatoKeys.TIFOSI) + c.tifosi)
            _notice.value = c.esito
            refreshClub()
        }
    }

    /** Sale di livello una struttura della tua squadra, pagata dal budget mercato. */
    fun upgrade(tipo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val liv = dao.statoInt(tipo)
            when {
                liv >= Strutture.MAX -> _notice.value = "Struttura già al livello massimo."
                Strutture.costo(liv) > me.budget -> _notice.value = "Budget insufficiente (servono ${money(Strutture.costo(liv))})."
                else -> {
                    val c = Strutture.costo(liv)
                    dao.updateTeam(me.copy(budget = me.budget - c))
                    dao.setStatoInt(tipo, liv + 1, 1, Strutture.MAX)
                    registra(me.id, if (tipo == Strutture.ALLENAMENTO) "Centro allenamento liv. ${liv + 1}" else "Vivaio liv. ${liv + 1}", -c, me.budget - c)
                    _notice.value = "Livello ${liv + 1}. ${Strutture.descrizione(tipo, liv + 1)}."
                }
            }
            refreshClub()
        }
    }

    fun startGame(team: Team) {
        selectTeam(team)
        prefs.edit().putBoolean("partita_iniziata", true).apply()
        _started.value = true
        viewModelScope.launch(Dispatchers.IO) { refreshSponsorOffers(team.id); refreshClub() }
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
