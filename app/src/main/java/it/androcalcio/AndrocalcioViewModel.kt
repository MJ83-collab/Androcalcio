package it.androcalcio

import it.androcalcio.engine.AvatarGen
import it.androcalcio.engine.Difficolta
import it.androcalcio.engine.EffettiMiglioria
import it.androcalcio.engine.Migliorie
import it.androcalcio.engine.Abbonamenti
import it.androcalcio.engine.Achievement
import it.androcalcio.engine.Agenti
import it.androcalcio.engine.AllStar
import it.androcalcio.engine.Avanzate
import it.androcalcio.engine.Finestre
import it.androcalcio.engine.Giovanili
import it.androcalcio.engine.ScoutingGiovani
import it.androcalcio.engine.Disciplina
import it.androcalcio.engine.Personalita
import it.androcalcio.engine.Scambi
import it.androcalcio.engine.FairPlay
import it.androcalcio.engine.Storia
import it.androcalcio.engine.Mutui
import it.androcalcio.engine.NuovoProprietario
import it.androcalcio.engine.Cartellini
import it.androcalcio.engine.NuovoStadio
import it.androcalcio.engine.Prato
import it.androcalcio.engine.Minutaggio
import it.androcalcio.engine.PremioPartita
import it.androcalcio.engine.StoricoInfortuni
import it.androcalcio.engine.FormazioniSalvate
import it.androcalcio.engine.Spareggi
import it.androcalcio.engine.Analisi
import it.androcalcio.engine.Leadership
import it.androcalcio.engine.Bilancio
import it.androcalcio.engine.Ampiezza
import it.androcalcio.ui.theme.AC
import it.androcalcio.engine.Giornale
import it.androcalcio.engine.Meteo
import it.androcalcio.engine.Obiettivi
import it.androcalcio.engine.Premi
import it.androcalcio.engine.Prestiti
import it.androcalcio.engine.Relazioni
import it.androcalcio.engine.fattore
import it.androcalcio.engine.DefLine
import it.androcalcio.engine.DivisioneFine
import it.androcalcio.engine.Allenatori
import it.androcalcio.engine.Europa
import it.androcalcio.engine.FineStagione
import it.androcalcio.engine.Presidente
import it.androcalcio.engine.RigaFine
import it.androcalcio.engine.Sudamericana
import it.androcalcio.engine.Pressing
import it.androcalcio.engine.ShirtNumbers
import it.androcalcio.engine.Stadium
import it.androcalcio.engine.Insurance
import it.androcalcio.engine.Sponsorship
import it.androcalcio.engine.Evento
import it.androcalcio.engine.Scelta
import it.androcalcio.engine.Spogliatoio
import it.androcalcio.engine.TrainFocus
import it.androcalcio.engine.Training
import it.androcalcio.engine.TrainIntensity
import it.androcalcio.engine.TrainingPlan
import android.app.Application
import android.content.Intent
import android.net.Uri
import it.androcalcio.data.SaveSlots
import it.androcalcio.data.SlotInfo
import it.androcalcio.data.DataIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import it.androcalcio.engine.SetPieces
import it.androcalcio.engine.TacticalRoles
import it.androcalcio.engine.ClubOffers
import it.androcalcio.engine.SponsorDeal
import it.androcalcio.engine.Sponsors
import it.androcalcio.engine.Strutture
import it.androcalcio.engine.Economia
import it.androcalcio.engine.Formation
import it.androcalcio.engine.Forma
import it.androcalcio.engine.Mentori
import it.androcalcio.engine.Ritmo
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
import it.androcalcio.engine.InfoSquadra
import it.androcalcio.engine.Rivalita
import it.androcalcio.engine.Missioni
import it.androcalcio.data.PlayerStat
import it.androcalcio.data.Clausola
import it.androcalcio.data.ScoutMission
import it.androcalcio.data.Convocazione
import it.androcalcio.data.NazionaleStat
import it.androcalcio.data.RosaNazionale
import it.androcalcio.data.DettaglioGiocatore
import it.androcalcio.engine.Nazionali
import it.androcalcio.engine.SquadraInfo
import it.androcalcio.ui.components.money
import it.androcalcio.engine.MatchReport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.channels.awaitClose
import androidx.room.InvalidationTracker
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import it.androcalcio.engine.AssistantCoach
import it.androcalcio.engine.schemaAbituale
import it.androcalcio.engine.schemaInPartita
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
/** Partite in un ruolo diverso dal naturale per impararlo (rendimento 100%). */
private const val PARTITE_PER_RUOLO = 10

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

    private fun avvio1() {
        viewModelScope.launch {
            if (dao.teamCount() == 0) SeedData.insertInto(ctx, dao)
            withContext(Dispatchers.IO) { assicuraAllStar() }
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

    private val _ampiezza = MutableStateFlow(Ampiezza.values()[prefs.getInt("ampiezza", 1).coerceIn(0, 2)])
    val ampiezza: StateFlow<Ampiezza> = _ampiezza
    fun setAmpiezza(a: Ampiezza) { _ampiezza.value = a; prefs.edit().putInt("ampiezza", a.ordinal).apply() }
    fun liveAmpiezza(a: Ampiezza) { val i = _live.value ?: return; i.m.setAmpiezza(i.userHome, a); bump() }

    /** Cambio programmato: 0 = spento, altrimenti il minuto (60, 70, 80) in cui scatta un cambio automatico. */
    private val _cambioPronto = MutableStateFlow(prefs.getInt("cambio_pronto", 0))
    val cambioPronto: StateFlow<Int> = _cambioPronto
    fun setCambioPronto(m: Int) { _cambioPronto.value = m; prefs.edit().putInt("cambio_pronto", m).apply() }

    private val _temaNero = MutableStateFlow(prefs.getInt("tema_scelto", if (prefs.getBoolean("tema_nero", false)) 1 else 0))
    /** 0 = blu, 1 = nero, 2 = chiaro. */
    val temaNero: StateFlow<Int> = _temaNero
    /** Tema nero: sfondo nero puro, sugli schermi OLED consuma meno batteria. */
    fun setTemaNero(v: Int) { prefs.edit().putInt("tema_scelto", v).putBoolean("tema_nero", v == 1).apply(); _temaNero.value = v; AC.tema = AC.daPreferenza(v) }

    fun setLine(l: DefLine) { _line.value = l; prefs.edit().putInt("linea", l.ordinal).apply() }
    private val _trappola = MutableStateFlow(prefs.getBoolean("trappola", false))
    /** Trappola del fuorigioco: vale solo con la linea alta. */
    val trappola: StateFlow<Boolean> = _trappola
    fun setTrappola(b: Boolean) { _trappola.value = b; prefs.edit().putBoolean("trappola", b).apply() }

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

    private fun avvio2() {
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
            val attuale = have[t.id].orEmpty()
            val missing = Staffing.POSTI.filter { p -> attuale.none { Staffing.stessoPosto(it, p) } }
            val ps = players[t.id].orEmpty()
            if (missing.isEmpty() || ps.isEmpty()) emptyList()
            else Staffing.aiStaff(t.id, ps.map { it.overall }.average(), missing, rng)
        }
        if (add.isNotEmpty()) dao.insertStaffs(add)
    }

    private fun playRounds(all: Boolean) {
        viewModelScope.launch { runRounds(all, null); autoSalva() }
    }

    /** Gioca la prossima giornata (o tutte); skipId = la tua partita, giocata a parte dal vivo. */
    private suspend fun runRounds(all: Boolean, skipId: Long?, eventi: Boolean = !all) {
        risolviMercato()
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
            val statAcc = HashMap<Long, PlayerStat>()
            val fatteMie = Migliorie.decodeFatte(dao.stato("migliorie_fatte"))
            val totaliLega = ms.filter { !Cup.isCup(it.round) }.maxOfOrNull { it.round } ?: 38
            val pratoMio = dao.statoInt(Prato.KEY)
            val rounds = ms.filter { !it.played }.map { it.round }.distinct().sorted()
                .let { if (all) it else it.take(1) }
            rounds.forEach { giocaDataNazionali(_season.value, it); giocaDataU21(_season.value, it) }
            val assentiPerTurno: Map<Int, Set<Long>> = rounds.associateWith { r ->
                dao.convocazioniData(_season.value, r).map { it.playerId }.toSet()
            }
            for (m in ms.filter { !it.played && it.round in rounds && it.id != skipId }) {
                val h = teamsById[m.homeTeamId] ?: continue
                val a = teamsById[m.awayTeamId] ?: continue
                val hp = cache.getOrPut(h.id) { dao.playersOnce(h.id) }
                val ap = cache.getOrPut(a.id) { dao.playersOnce(a.id) }
                val assenti = if (Cup.isCup(m.round)) emptySet() else assentiPerTurno[m.round].orEmpty()
                val squalMie = Disciplina.decode(dao.stato("squal")).keys
                val hpD = hp.filter { it.id !in assenti && !(h.id == t.id && it.id in squalMie) }.let { if (h.id == t.id) conPremio(it) else it }
                val apD = ap.filter { it.id !in assenti && !(a.id == t.id && it.id in squalMie) }.let { if (a.id == t.id) conPremio(it) else it }
                if (h.id == t.id && hp.any { it.id in squalMie }) lineupNotes += "${hp.count { it.id in squalMie }} squalificati: oggi non giocano."
                if (a.id == t.id && ap.any { it.id in squalMie }) lineupNotes += "${ap.count { it.id in squalMie }} squalificati: oggi non giocano."

                if (h.id == t.id && hp.any { it.id in assenti }) lineupNotes += "${hp.count { it.id in assenti }} convocati in nazionale: oggi non giocano."
                if (a.id == t.id && ap.any { it.id in assenti }) lineupNotes += "${ap.count { it.id in assenti }} convocati in nazionale: oggi non giocano."
                val hL = if (h.id == t.id) Lineup.resolve(hpD, _formation.value) else null
                val aL = if (a.id == t.id) Lineup.resolve(apD, _formation.value) else null
                lineupNotes += hL?.notes.orEmpty() + aL?.notes.orEmpty()
                val r = MatchEngine.simulate(
                    h, hL?.xi ?: Medical.available(hpD), a, aL?.xi ?: Medical.available(apD),
                    if (hL != null) _formation.value else schemaInPartita(h.id, kotlin.random.Random.Default),
                    if (aL != null) _formation.value else schemaInPartita(a.id, kotlin.random.Random.Default),
                    Staffing.skill(staffBy, h.id, Staffing.ALLENATORE),
                    Staffing.skill(staffBy, a.id, Staffing.ALLENATORE),
                    homeMent = if (h.id == t.id) _mentality.value else null,
                    awayMent = if (a.id == t.id) _mentality.value else null,
                    homeBench = hL?.let { Medical.available(hpD) - it.xi.toSet() },
                    awayBench = aL?.let { Medical.available(apD) - it.xi.toSet() },
                    homePress = if (h.id == t.id) _pressing.value else null,
                    awayPress = if (a.id == t.id) _pressing.value else null,
                    homeLine = if (h.id == t.id) _line.value else null,
                    awayLine = if (a.id == t.id) _line.value else null,
                    homeAmp = if (h.id == t.id) _ampiezza.value else Ampiezza.NORMALE,
                    awayAmp = if (a.id == t.id) _ampiezza.value else Ampiezza.NORMALE,
                    homeStile = if (h.id == t.id) null else Staffing.stile(staffBy, h.id),
                    awayStile = if (a.id == t.id) null else Staffing.stile(staffBy, a.id),
                    meteoGol = condizioni(m.round, totaliLega, h, h.id == t.id, fatteMie).fattoreGol() *
                        (if (h.id == t.id) Prato.fattore(pratoMio) else 1.0),
                    homeTrap = h.id == t.id && _trappola.value, awayTrap = a.id == t.id && _trappola.value
                )
                dao.updateMatch(m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true))
                if (h.id == t.id || a.id == t.id) registraPartita(t, if (h.id == t.id) a else h, h.id == t.id, r)
                playedNow++
                accumulaStat(statAcc, _season.value, h.id, a.id, r)
                goalsAcc += r.events.filter { it.type == EventType.GOAL && it.playerId != 0L }.map {
                    Goal(season = _season.value, league = m.league, round = m.round,
                        teamId = if (it.home) m.homeTeamId else m.awayTeamId, playerId = it.playerId)
                }
                val prevenzione = Migliorie.effetti(fatteMie).infortuniPct + Difficolta.prevenzioneExtra(_opzioni.value.difficolta, profiloPersonalizzato()).coerceAtLeast(0)
                cache[h.id] = Medical.afterMatch(hp, r.homePlayed, Staffing.cura(staffBy, h.id), rng,
                    planFor(h.id, t.id), Staffing.skill(staffBy, h.id, Staffing.ALLENATORE))
                    .let { if (h.id == t.id) infortunioExtra(hp, Migliorie.prevenzione(hp, it, prevenzione, rng), r.homePlayed, rng) else it }.also { accrueInsurance(h.id, hp, it); registraInfortuni(h.id, hp, it) }
                cache[a.id] = Medical.afterMatch(ap, r.awayPlayed, Staffing.cura(staffBy, a.id), rng,
                    planFor(a.id, t.id), Staffing.skill(staffBy, a.id, Staffing.ALLENATORE))
                    .let { if (a.id == t.id) infortunioExtra(ap, Migliorie.prevenzione(ap, it, prevenzione, rng), r.awayPlayed, rng) else it }.also { accrueInsurance(a.id, ap, it); registraInfortuni(a.id, ap, it) }
            }
            dao.updatePlayers(cache.values.flatten())
            dao.insertGoals(goalsAcc)
            salvaStatistiche(_season.value, statAcc)
            avanzaCantieri(rounds.size, t.id)
            ms.filter { it.round in rounds && it.id != skipId && (it.homeTeamId == t.id || it.awayTeamId == t.id) }
                .forEach { m0 -> dao.match(m0.id)?.let { dopoMiaPartita(t.id, it, evento = eventi) } }
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

    /** Simula giornata per giornata fino al turno [fino] compreso (senza eventi a scelta lungo la strada). */
    fun simulaFino(fino: Int) {
        viewModelScope.launch {
            val t = selectedTeam.value ?: return@launch
            var giocate = 0
            while (giocate < 60) {
                val ms = dao.matchesOnce(key(t), _season.value) + dao.matchesOnce(Cup.key(t.nazione), _season.value)
                val prossimo = ms.filter { !it.played }.minOfOrNull { it.round } ?: break
                if (prossimo > fino) break
                runRounds(false, null, eventi = false); giocate++
            }
            autoSalva()
            _notice.value = listOfNotNull(_notice.value, "Simulate $giocate giornate.").joinToString("\n")
        }
    }

    // ---- Migliorie di stadio e centro sportivo ----
    data class MigliorieStato(val fatte: Set<String> = emptySet(), val cantieri: Map<String, Int> = emptyMap(),
                              val incassoStagione: Long = 0L, val incassoPerPosto: Double = 0.0)
    private val _migliorie = MutableStateFlow(MigliorieStato())
    val migliorie: StateFlow<MigliorieStato> = _migliorie

    private suspend fun caricaMigliorie() {
        val t = selectedTeam.value
        val stagione = if (t == null) 0L else Economia.incassoStadio(t.stadiumCapacity, t.ticketPrice, 0.5, 20)
        val perPosto = if (t == null || t.stadiumCapacity <= 0) 0.0 else stagione.toDouble() / t.stadiumCapacity
        _migliorie.value = MigliorieStato(Migliorie.decodeFatte(dao.stato("migliorie_fatte")),
            Migliorie.decodeCantieri(dao.stato("migliorie_cantieri")), stagione, perPosto)
    }

    fun aggiornaMigliorie() { viewModelScope.launch(Dispatchers.IO) { caricaMigliorie() } }

    /** Avvia i lavori: paga subito dal budget mercato, finiscono dopo le giornate indicate. */
    fun avviaMiglioria(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val m = Migliorie.trova(id) ?: return@launch
            val t = dao.team(me.id)
            val fatte = Migliorie.decodeFatte(dao.stato("migliorie_fatte"))
            val cantieri = Migliorie.decodeCantieri(dao.stato("migliorie_cantieri"))
            val no = Migliorie.avviabile(m, fatte, cantieri, t.budget)
            if (no != null) { _notice.value = "${m.nome}: $no."; return@launch }
            dao.updateTeam(t.copy(budget = t.budget - m.costo))
            registra(t.id, "Lavori: ${m.nome}", -m.costo, t.budget - m.costo)
            dao.setStato(Stato("migliorie_cantieri", Migliorie.encodeCantieri(cantieri + (id to m.giornate))))
            _notice.value = "Lavori avviati: ${m.nome}. Pronti tra ${m.giornate} giornate."
            caricaMigliorie(); refreshClub()
        }
    }

    /** Fa avanzare i cantieri dopo le giornate giocate e applica le migliorie finite. */
    private suspend fun avanzaCantieri(giornate: Int, teamId: Long) {
        if (giornate <= 0) return
        val cantieri = Migliorie.decodeCantieri(dao.stato("migliorie_cantieri"))
        if (cantieri.isEmpty()) return
        val (rimasti, finite) = Migliorie.avanza(cantieri, giornate)
        dao.setStato(Stato("migliorie_cantieri", Migliorie.encodeCantieri(rimasti)))
        if (finite.isEmpty()) { caricaMigliorie(); return }
        val fatte = Migliorie.decodeFatte(dao.stato("migliorie_fatte")) + finite.map { it.id }
        dao.setStato(Stato("migliorie_fatte", Migliorie.encodeFatte(fatte)))
        val e = finite.fold(EffettiMiglioria()) { a, m -> a + m.effetti }
        if (e.posti > 0) dao.team(teamId).let { dao.updateTeam(it.copy(stadiumCapacity = it.stadiumCapacity + e.posti)) }
        if (e.tifosi != 0) dao.setStatoInt(StatoKeys.TIFOSI, dao.statoInt(StatoKeys.TIFOSI) + e.tifosi)
        if (e.morale != 0) dao.updatePlayers(dao.playersOnce(teamId).map { it.copy(morale = (it.morale + e.morale).coerceIn(0, 100)) })
        _notice.value = "Lavori finiti: " + finite.joinToString { it.nome } + "."
        caricaMigliorie()
    }

    // ---- Avatar modificati dall'utente (nome -> volto), salvati in Stato "avatar" ----
    private val _avatar = MutableStateFlow<Map<String, AvatarGen.Spec>>(emptyMap())
    val avatar: StateFlow<Map<String, AvatarGen.Spec>> = _avatar
    private fun avvio3() { viewModelScope.launch(Dispatchers.IO) { _avatar.value = AvatarGen.decodeMappa(dao.stato("avatar")) } }
    /** Salva il volto di [nome]; con [spec] nullo ripristina quello generato dal nome. */
    fun salvaAvatar(nome: String, spec: AvatarGen.Spec?) {
        val nuova = if (spec == null) _avatar.value - nome else _avatar.value + (nome to spec)
        _avatar.value = nuova
        viewModelScope.launch(Dispatchers.IO) { dao.setStato(Stato("avatar", AvatarGen.encodeMappa(nuova))) }
    }

    // ---- Extra: stampa, colloqui, psicologo, marketing, obiettivi, prezzi, giornale, trofei, avanzate, prestiti ----
    data class ExtraStato(
        val psicologo: Int = 0, val marketing: Int = 0,
        val domanda: Relazioni.Domanda? = null, val conferenzaFatta: Boolean = false,
        val colloquiFatti: Set<Long> = emptySet(),
        val obiettivo: Obiettivi.Obiettivo? = null, val posizioneAttesa: Int = 0,
        val prezzi: Map<String, Int> = emptyMap(), val abbonatiPct: Int = 0,
        val premi: List<Premi.Premio> = emptyList(), val giornale: Giornale.Edizione? = null,
        val trofei: Set<String> = emptySet(), val avanzate: Avanzate.Totali = Avanzate.Totali(),
        val finestra: Finestre.Stato = Finestre.Stato.ESTIVA, val ultimoGiorno: Boolean = false,
        val prestiti: List<Prestiti.Prestito> = emptyList(),
        val meteo: Meteo.Condizioni? = null, val budget: Long = 0L, val giornata: Int = 1,
        val inPrima: Set<Long> = emptySet()
    )
    private val _extra = MutableStateFlow(ExtraStato())
    val extra: StateFlow<ExtraStato> = _extra

    private suspend fun inc(chiave: String, by: Int = 1) = dao.setStatoInt(chiave, dao.statoInt(chiave) + by, 0, Int.MAX_VALUE)
    private fun avviso(msg: String) { _notice.value = listOfNotNull(_notice.value, msg).joinToString("\n") }
    private fun segno(v: Int) = if (v > 0) "+$v" else "$v"

    /** Meteo e campo di una giornata: stessi per previsione e partita (seme fisso). */
    private fun condizioni(round: Int, totali: Int, casa: Team, mio: Boolean, fatte: Set<String>): Meteo.Condizioni {
        val rng = kotlin.random.Random(_season.value.hashCode() * 31 + round * 131 + casa.id.toInt())
        val tempo = Meteo.tempo(Meteo.meseDellaGiornata(round, totali), rng)
        return Meteo.Condizioni(tempo, Meteo.campo(tempo, mio && "ibrido" in fatte, mio && "riscaldamento" in fatte, 1, rng))
    }

    private suspend fun fissaObiettivo(t: Team) {
        val forze = strengthOf()
        val lega = dao.allTeams().filter { it.nazione == t.nazione && it.campionato == t.campionato }
        val mia = forze[t.id] ?: 0.0
        val pos = lega.count { (forze[it.id] ?: 0.0) > mia } + 1
        val o = Obiettivi.fissa(pos, lega.size, (t.budget / 40).coerceIn(100_000L, 5_000_000L))
        dao.setStato(Stato("obiettivo", Obiettivi.encode(o)))
        dao.setStato(Stato("obiettivo_s", _season.value))
        dao.setStatoInt("obiettivo_pos", pos, 0, 999)
    }

    private suspend fun caricaExtra() {
        val me = selectedTeam.value ?: return
        val t = dao.team(me.id)
        val s = _season.value
        val ms = dao.matchesOnce(key(t), s)
        val totali = ms.filter { !Cup.isCup(it.round) }.maxOfOrNull { it.round } ?: 38
        val prossima = ms.filter { !it.played }.minOfOrNull { it.round } ?: (totali + 1)
        val g = dao.statoInt(MK.GIORNATA)
        val rosa = dao.playersOnce(t.id)
        val fatte = Migliorie.decodeFatte(dao.stato("migliorie_fatte"))
        if (dao.stato("obiettivo_s") != s) fissaObiettivo(t)
        val prox = ms.firstOrNull { !it.played && it.round == prossima && (it.homeTeamId == t.id || it.awayTeamId == t.id) }
        val meteo = prox?.let { m0 -> dao.team(m0.homeTeamId).let { condizioni(m0.round, totali, it, it.id == t.id, fatte) } }
        val prezzi = Abbonamenti.decode(dao.stato("prezzi"))
        _extra.value = ExtraStato(
            psicologo = dao.statoInt("psicologo"), marketing = dao.statoInt("marketing"),
            domanda = Relazioni.domanda(g), conferenzaFatta = dao.stato("conf_g")?.toIntOrNull() == g,
            colloquiFatti = rosa.filter { dao.stato("col_${it.id}")?.toIntOrNull() == g }.map { it.id }.toSet(),
            obiettivo = Obiettivi.decode(dao.stato("obiettivo")), posizioneAttesa = dao.statoInt("obiettivo_pos"),
            prezzi = prezzi,
            abbonatiPct = Abbonamenti.abbonatiPct(dao.statoInt(StatoKeys.TIFOSI), prezzi.values.average().toInt()),
            premi = Premi.decode(dao.stato("premi")), giornale = Giornale.decode(dao.stato("giornale")),
            trofei = Achievement.decode(dao.stato("achievement")), avanzate = Avanzate.decode(dao.stato("avanzate")),
            finestra = Finestre.stato(prossima, totali), ultimoGiorno = Finestre.ultimoGiorno(prossima, totali),
            prestiti = Prestiti.decode(dao.stato("prestiti")), meteo = meteo, budget = t.budget, giornata = prossima,
            inPrima = Giovanili.decodeApparizioni(dao.stato("apparizioni")).map { it.first }.toSet()
        )
    }

    fun aggiornaExtra() { viewModelScope.launch(Dispatchers.IO) { caricaExtra() } }

    /** Sblocca i trofei raggiunti dalla carriera e li annuncia. */
    private suspend fun aggiornaTrofei() {
        val t = selectedTeam.value ?: return
        val snap = Achievement.Snapshot(
            stagioni = dao.statoInt("c_stagioni"), titoliCampionato = dao.statoInt("c_titoli"), coppe = dao.statoInt("c_coppe"),
            promozioni = dao.statoInt("c_prom"), vittorie = dao.statoInt("c_vittorie"), golSegnati = dao.statoInt("c_gol"),
            derbyVinti = dao.statoInt("c_derby"), migliorieFatte = Migliorie.decodeFatte(dao.stato("migliorie_fatte")).size,
            sponsorAttivi = SponsorDeal.decode(dao.stato("sponsor")).size, budget = dao.team(t.id).budget,
            fiducia = dao.statoInt(StatoKeys.FIDUCIA), tifosi = dao.statoInt(StatoKeys.TIFOSI),
            obiettiviRaggiunti = dao.statoInt("c_obiettivi"), giovaniPromossi = dao.statoInt("c_giovani"),
            colloqui = dao.statoInt("c_colloqui"), conferenze = dao.statoInt("c_conferenze"),
            prestiti = dao.statoInt("c_prestiti"), partiteGiocate = dao.statoInt("c_partite")
        )
        val prima = Achievement.decode(dao.stato("achievement"))
        val nuovi = Achievement.nuovi(prima, snap)
        if (nuovi.isEmpty()) return
        dao.setStato(Stato("achievement", Achievement.encode(prima + nuovi.map { it.id })))
        avviso("Trofeo sbloccato: " + nuovi.joinToString { "${it.icona} ${it.nome}" } + ".")
    }

    /** Dopo una partita della tua squadra: statistiche avanzate, giornale, contatori di carriera, trofei. */
    private suspend fun registraPartita(me: Team, opp: Team, mioHome: Boolean, r: MatchReport, live: Boolean = false) {
        val gf = if (mioHome) r.homeGoals else r.awayGoals
        val gs = if (mioHome) r.awayGoals else r.homeGoals
        dao.setStato(Stato("avanzate", Avanzate.encode(Avanzate.aggiungi(Avanzate.decode(dao.stato("avanzate")), mioHome, r))))
        inc("c_partite"); inc("c_gol", gf)
        val derby = Rivalita.isDerby(me.name, opp.name)
        if (gf > gs) { inc("c_vittorie"); if (derby) inc("c_derby") }
        val voti = r.playedPlayers.filter { it.teamId == me.id }.mapNotNull { p ->
            r.playerRatings[p.id]?.let { p.name to (Math.round(it * 2) / 2.0) }
        }
        dao.setStato(Stato("giornale", Giornale.encode(Giornale.crea(me.name, opp.name, gf, gs, derby, voti))))
        societaDopoPartita(me, opp, gf, gs, r, live)
        dopoPartitaV06(me, mioHome, gf, gs, r)
        aggiornaTrofei()
    }

    /** v0.6 dopo ogni tua partita: minuti, voti, prato, premio partita. */
    private suspend fun dopoPartitaV06(me: Team, mioHome: Boolean, gf: Int, gs: Int, r: MatchReport) {
        val rosa = dao.playersOnce(me.id)
        val giocato = if (mioHome) r.homePlayed else r.awayPlayed
        val min = Minutaggio.aggiungi(Minutaggio.decode(dao.stato(Minutaggio.KEY)), rosa.filter { it.starter }.map { it.id }.toSet(), giocato)
        val nPart = dao.statoInt(Minutaggio.KEY_PARTITE) + 1
        dao.setStato(Stato(Minutaggio.KEY, Minutaggio.encode(min))); dao.setStatoInt(Minutaggio.KEY_PARTITE, nPart, 0, 999)
        if (nPart % 4 == 0) {
            val sc = Minutaggio.scontenti(rosa, min, nPart)
            if (sc.isNotEmpty()) {
                dao.updatePlayers(sc.map { it.copy(morale = (it.morale - 3).coerceAtLeast(0)) })
                avviso("Chiedono più spazio: ${sc.joinToString(", ") { it.name }} (morale in calo).")
            }
        }
        // Voti per stagione: stagione:id:somma×10:partite
        val voti = LinkedHashMap<String, Pair<Int, Int>>()
        dao.stato("voti").orEmpty().split(";").forEach { e ->
            val x = e.split(":")
            if (x.size == 4) voti[x[0] + ":" + x[1]] = Pair(x[2].toIntOrNull() ?: 0, x[3].toIntOrNull() ?: 0)
        }
        r.playedPlayers.filter { it.teamId == me.id }.forEach { p ->
            val v = r.playerRatings[p.id] ?: return@forEach
            val k = "${_season.value}:${p.id}"; val (s0, n0) = voti[k] ?: (0 to 0)
            voti[k] = (s0 + Math.round(v * 10).toInt()) to (n0 + 1)
        }
        dao.setStato(Stato("voti", voti.entries.toList().takeLast(600).joinToString(";") { "${it.key}:${it.value.first}:${it.value.second}" }))
        if (mioHome) dao.setStatoInt(Prato.KEY, Prato.dopoPartita(dao.statoInt(Prato.KEY), dao.statoInt(Prato.KEY_CURA), kotlin.random.Random.Default.nextDouble() < 0.25))
        if (dao.stato(PremioPartita.KEY) == "1") {
            dao.setStato(Stato(PremioPartita.KEY, "")); _premio.value = false
            if (gf > gs) {
                val c = PremioPartita.costo(rosa.sumOf { it.salary })
                val tt = dao.team(me.id)
                dao.updateTeam(tt.copy(budget = tt.budget - c))
                registra(tt.id, "Premio partita ai giocatori", -c, tt.budget - c)
            }
        }
    }

    fun rispondiConferenza(i: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val g = dao.statoInt(MK.GIORNATA)
            if (dao.stato("conf_g")?.toIntOrNull() == g) { _notice.value = "Hai già parlato alla stampa in questa giornata."; return@launch }
            val r = Relazioni.domanda(g).risposte.getOrNull(i) ?: return@launch
            val me = selectedTeam.value ?: return@launch
            dao.setStato(Stato("conf_g", g.toString()))
            dao.setStatoInt(StatoKeys.TIFOSI, dao.statoInt(StatoKeys.TIFOSI) + r.tifosi)
            dao.setStatoInt(StatoKeys.FIDUCIA, dao.statoInt(StatoKeys.FIDUCIA) + r.fiducia)
            if (r.morale != 0) dao.updatePlayers(dao.playersOnce(me.id).map { it.copy(morale = (it.morale + r.morale).coerceIn(0, 100)) })
            inc("c_conferenze")
            _notice.value = "Conferenza stampa: morale ${segno(r.morale)}, tifosi ${segno(r.tifosi)}, dirigenza ${segno(r.fiducia)}."
            aggiornaTrofei(); caricaExtra(); refreshClub()
        }
    }

    fun colloquio(playerId: Long, tono: Relazioni.Tono) {
        viewModelScope.launch(Dispatchers.IO) {
            val g = dao.statoInt(MK.GIORNATA)
            val p = dao.playerById(playerId) ?: return@launch
            if (dao.stato("col_$playerId")?.toIntOrNull() == g) { _notice.value = "${p.name} ha già parlato con te in questa giornata."; return@launch }
            val d = Relazioni.colloquio(p, tono, kotlin.random.Random.Default)
            dao.updatePlayer(p.copy(morale = (p.morale + d).coerceIn(0, 100)))
            dao.setStato(Stato("col_$playerId", g.toString()))
            inc("c_colloqui")
            _notice.value = "${p.name}: ${tono.nome.lowercase()}, morale ${segno(d)}."
            aggiornaTrofei(); caricaExtra()
        }
    }

    fun setPsicologo(l: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.setStatoInt("psicologo", l, 0, 3)
            _notice.value = if (l == 0) "Psicologo congedato." else
                "${Relazioni.PSICOLOGO[l.coerceIn(0, 3)]}: ${money(Relazioni.costoPsicologo(l))} l'anno, a fine stagione."
            caricaExtra()
        }
    }

    fun setMarketing(l: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.setStatoInt("marketing", l, 0, 3)
            _notice.value = if (l == 0) "Addetto marketing congedato." else
                "${Sponsors.MARKETING[l.coerceIn(0, 3)]}: ${money(Sponsors.costoMarketing(l))} l'anno, a fine stagione."
            caricaExtra()
        }
    }

    fun cercaSponsor() {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            refreshSponsorOffers(me.id)
            _notice.value = "Il marketing ha trovato nuove offerte: le vedi in Dirigenza."
            caricaExtra()
        }
    }

    fun setPrezzo(settore: String, pct: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val m = Abbonamenti.decode(dao.stato("prezzi")).toMutableMap()
            m[settore] = pct.coerceIn(Abbonamenti.PREZZO_MIN, Abbonamenti.PREZZO_MAX)
            dao.setStato(Stato("prezzi", Abbonamenti.encode(m)))
            caricaExtra()
        }
    }

    private suspend fun mercatoAperto(): Boolean { caricaExtra(); return _extra.value.finestra.aperta }

    /** Presta un tuo giocatore a un club della tua nazione fino a fine stagione: incassi la quota del prestito. */
    fun prestaGiocatore(p: Player) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            if (!mercatoAperto()) { _notice.value = "${_extra.value.finestra.nome}: i prestiti si fanno solo nelle finestre."; return@launch }
            if (dao.stato("ffp_blocco") == _season.value) { _notice.value = "Mercato in entrata bloccato per il fair play finanziario."; return@launch }
            if (!Prestiti.puoPrestare(p, dao.playersOnce(me.id).size)) { _notice.value = "Non puoi prestare ${p.name}: rosa troppo corta o giocatore troppo anziano."; return@launch }
            val costo = Prestiti.costo(p)
            val dest = dao.allTeams().filter { it.nazione == me.nazione && it.id != me.id && !AllStar.isAllStar(it) && it.budget >= costo }
                .randomOrNull() ?: run { _notice.value = "Nessun club può prenderlo in prestito."; return@launch }
            val t = dao.team(me.id)
            dao.updatePlayer(p.copy(teamId = dest.id, starter = false))
            dao.updateTeam(dest.copy(budget = dest.budget - costo))
            dao.updateTeam(t.copy(budget = t.budget + costo))
            registra(t.id, "Prestito ${p.name} a ${dest.name}", costo, t.budget + costo)
            val l = Prestiti.decode(dao.stato("prestiti")) + Prestiti.Prestito(p.id, t.id, dest.id, Prestiti.riscatto(p), true)
            dao.setStato(Stato("prestiti", Prestiti.encode(l)))
            inc("c_prestiti")
            _notice.value = "${p.name} in prestito a ${dest.name}: incassi ${money(costo)}. Torna a fine stagione."
            aggiornaTrofei(); caricaExtra(); refreshClub()
        }
    }

    /** Prende in prestito un giocatore di un club della tua nazione: paghi la quota, poi puoi riscattarlo. */
    fun prendiInPrestito(p: Player) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            if (!mercatoAperto()) { _notice.value = "${_extra.value.finestra.nome}: i prestiti si fanno solo nelle finestre."; return@launch }
            if (!Prestiti.puoPrendere(dao.playersOnce(me.id).size)) { _notice.value = "Rosa piena."; return@launch }
            val costo = Prestiti.costo(p)
            val t = dao.team(me.id)
            if (costo > t.budget) { _notice.value = "Budget insufficiente per il prestito (${money(costo)})."; return@launch }
            val origine = dao.team(p.teamId)
            dao.updateTeam(origine.copy(budget = origine.budget + costo))
            dao.updateTeam(t.copy(budget = t.budget - costo))
            dao.updatePlayer(p.copy(teamId = t.id, starter = false))
            registra(t.id, "Prestito ${p.name} da ${origine.name}", -costo, t.budget - costo)
            val l = Prestiti.decode(dao.stato("prestiti")) + Prestiti.Prestito(p.id, origine.id, t.id, Prestiti.riscatto(p), false)
            dao.setStato(Stato("prestiti", Prestiti.encode(l)))
            inc("c_prestiti")
            _notice.value = "${p.name} arriva in prestito da ${origine.name} (${money(costo)}). Riscatto: ${money(Prestiti.riscatto(p))}."
            aggiornaTrofei(); caricaExtra(); refreshClub()
        }
    }

    fun riscatta(playerId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val l = Prestiti.decode(dao.stato("prestiti"))
            val pr = l.firstOrNull { it.playerId == playerId && !it.inUscita } ?: return@launch
            val t = dao.team(pr.destinazione)
            if (pr.riscatto > t.budget) { _notice.value = "Budget insufficiente per il riscatto (${money(pr.riscatto)})."; return@launch }
            val origine = dao.team(pr.origine)
            dao.updateTeam(origine.copy(budget = origine.budget + pr.riscatto))
            dao.updateTeam(t.copy(budget = t.budget - pr.riscatto))
            registra(t.id, "Riscatto giocatore", -pr.riscatto, t.budget - pr.riscatto)
            dao.setStato(Stato("prestiti", Prestiti.encode(l - pr)))
            _notice.value = "Riscatto completato: ${money(pr.riscatto)}."
            caricaExtra(); refreshClub()
        }
    }

    /** A fine stagione tutti i prestiti finiscono: i giocatori tornano al club di origine. */
    private suspend fun chiudiPrestiti() {
        val l = Prestiti.decode(dao.stato("prestiti"))
        if (l.isEmpty()) return
        l.forEach { pr -> dao.playerById(pr.playerId)?.let { dao.updatePlayer(it.copy(teamId = pr.origine, starter = false)) } }
        dao.setStato(Stato("prestiti", ""))
    }

    /** Le squadre All Star esistono una volta sola: ogni decennio ha la sua, con i giocatori storpiati. */
    private suspend fun assicuraAllStar() {
        val esistenti = dao.allTeams().filter { AllStar.isAllStar(it) }.map { it.name }.toSet()
        for (d in AllStar.DECENNI) {
            if (d.nome in esistenti) continue
            val id = dao.insertTeams(listOf(AllStar.squadra(d))).first()
            dao.insertPlayers(AllStar.rosa(d, id))
        }
    }

    private suspend fun ripristinaAllStar() {
        val ids = dao.allTeams().filter { AllStar.isAllStar(it) }.map { it.id }.toSet()
        if (ids.isEmpty()) return
        val rosa = dao.allPlayers().filter { it.teamId in ids && (it.injuredRounds > 0 || it.condition < 100) }
        if (rosa.isNotEmpty()) dao.updatePlayers(rosa.map { it.copy(injuredRounds = 0, condition = 100) })
    }

    /** Infortuni extra nei livelli difficili: ogni partita c'è una piccola probabilità che un giocatore si faccia male. */
    private fun infortunioExtra(prima: List<Player>, dopo: List<Player>, giocati: Set<Long>, rng: kotlin.random.Random): List<Player> {
        val pct = -Difficolta.prevenzioneExtra(_opzioni.value.difficolta, profiloPersonalizzato())
        if (pct <= 0 || rng.nextInt(100) >= pct) return dopo
        val sani = dopo.filter { it.id in giocati && it.injuredRounds == 0 }
        val v = sani.randomOrNull(rng) ?: return dopo
        return dopo.map { if (it.id == v.id) it.copy(injuredRounds = rng.nextInt(1, 4)) else it }
    }

    // ---- Opzioni (schermata Opzioni): salvate nelle preferenze ----
    data class Opzioni(val difficolta: Int = Difficolta.NORMALE, val velocita: Int = 1)
    private val _opzioni = MutableStateFlow(Opzioni(
        prefs.getInt("difficolta5", if (prefs.contains("difficolta")) Difficolta.daVecchio(prefs.getInt("difficolta", 1)) else Difficolta.NORMALE),
        prefs.getInt("velocita_live", 1)))
    val opzioni: StateFlow<Opzioni> = _opzioni
    fun setDifficolta(d: Int) {
        prefs.edit().putInt("difficolta5", d).apply(); _opzioni.value = _opzioni.value.copy(difficolta = d)
    }

    /** Profilo "Personalizzata": budget, incassi, infortuni e dirigenza in percentuale. */
    fun profiloPersonalizzato() = Difficolta.Profilo(
        prefs.getInt("dif_budget", 100), prefs.getInt("dif_incassi", 100),
        prefs.getInt("dif_infortuni", 100), prefs.getInt("dif_dirigenza", 100)).coerente()
    private val _profilo = MutableStateFlow(profiloPersonalizzato())
    val profilo: StateFlow<Difficolta.Profilo> = _profilo
    fun setProfilo(p: Difficolta.Profilo) {
        val c = p.coerente()
        prefs.edit().putInt("dif_budget", c.budget).putInt("dif_incassi", c.incassi)
            .putInt("dif_infortuni", c.infortuni).putInt("dif_dirigenza", c.dirigenza).apply()
        _profilo.value = c
    }
    fun setVelocita(v: Int) {
        prefs.edit().putInt("velocita_live", v).apply(); _opzioni.value = _opzioni.value.copy(velocita = v)
    }

    // ---- Partita dal vivo: pausa, velocità, cambi e mentalità decisi da te ----
    /** Cosa è cambiato a un tuo giocatore dopo la partita (morale, infortunio, condizione, crescita). */
    data class EsitoGiocatore(
        val moraleDelta: Int, val infortunioTurni: Int, val condizione: Int, val miglioramenti: List<String>
    )
    data class LiveInfo(
        val m: LiveMatch, val tick: Int = 0, val paused: Boolean = false, val speed: Int = 1,
        val userHome: Boolean = true, val league: MatchResult? = null, val saved: Boolean = false,
        val meteo: String = "", val esito: Map<Long, EsitoGiocatore> = emptyMap()
    )
    private val _live = MutableStateFlow<LiveInfo?>(null)
    val live: StateFlow<LiveInfo?> = _live
    private var liveJob: kotlinx.coroutines.Job? = null

    private fun bump() { _live.value = _live.value?.let { it.copy(tick = it.tick + 1) } }

    private suspend fun buildLive(home: Team, away: Team, userHome: Boolean, league: MatchResult?): LiveInfo {
        val staffBy = dao.allStaff().groupBy { it.teamId }
        val me = if (userHome) home else away
        val squalMie = Disciplina.decode(dao.stato("squal")).keys
        val rosaCompleta = dao.playersOnce(me.id)
        val all = conPremio(rosaCompleta.filter { it.id !in squalMie })
        val nSqual = rosaCompleta.size - all.size
        if (nSqual > 0) avviso("$nSqual ${if (nSqual == 1) "squalificato" else "squalificati"}: oggi non gioca${if (nSqual == 1) "" else "no"}.")
        val res = Lineup.resolve(all, _formation.value)
        if (res.notes.isNotEmpty()) _notice.value = res.notes.joinToString("\n")
        val myBench = Medical.available(all) - res.xi.toSet()
        val opp = Medical.available(dao.playersOnce(if (userHome) away.id else home.id))
        val oppF = schemaInPartita(if (userHome) away.id else home.id, kotlin.random.Random.Default)
        val tiriMe = tiriDi(me.id)
        val msLega = dao.matchesOnce(key(me), _season.value)
        val cond = condizioni(league?.round ?: 1, msLega.filter { !Cup.isCup(it.round) }.maxOfOrNull { it.round } ?: 38, home,
            userHome, Migliorie.decodeFatte(dao.stato("migliorie_fatte")))
        val m = LiveMatch(
            home, if (userHome) res.xi else opp, away, if (userHome) opp else res.xi,
            if (userHome) _formation.value else oppF, if (userHome) oppF else _formation.value,
            Staffing.skill(staffBy, home.id, Staffing.ALLENATORE), Staffing.skill(staffBy, away.id, Staffing.ALLENATORE),
            if (userHome) _mentality.value else null, if (userHome) null else _mentality.value,
            if (userHome) myBench else null, if (userHome) null else myBench,
            manualHome = userHome, manualAway = !userHome,
            homePress = if (userHome) _pressing.value else null, awayPress = if (userHome) null else _pressing.value,
            homeLine = if (userHome) _line.value else null, awayLine = if (userHome) null else _line.value,
            homeAmp = if (userHome) _ampiezza.value else Ampiezza.NORMALE, awayAmp = if (userHome) Ampiezza.NORMALE else _ampiezza.value,
            homeStile = if (userHome) null else Staffing.stile(staffBy, home.id),
            awayStile = if (userHome) Staffing.stile(staffBy, away.id) else null,
            homeTiri = if (userHome) tiriMe else SetPieces.NONE, awayTiri = if (userHome) SetPieces.NONE else tiriMe,
            meteoGol = cond.fattoreGol() * (if (userHome) Prato.fattore(dao.statoInt(Prato.KEY)) else 1.0),
            trapH = userHome && _trappola.value, trapA = !userHome && _trappola.value
        )
        return LiveInfo(m, speed = _opzioni.value.velocita, userHome = userHome, league = league, meteo = cond.descrizione())
    }

    private fun startTicker() {
        liveJob?.cancel()
        liveJob = viewModelScope.launch {
            var cambioFatto = false
            while (true) {
                val i = _live.value ?: return@launch
                if (i.m.finished) break
                if (!i.paused) {
                    i.m.step()
                    val c = _cambioPronto.value
                    if (c > 0 && !cambioFatto && i.m.minute >= c) { cambioFatto = true; i.m.cambioProgrammato(i.userHome) }
                    bump()
                }
                kotlinx.coroutines.delay((if (i.speed >= 20) 50L else 1000L / i.speed).let { if (_risparmio.value) maxOf(it, 125L) else it })
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
    fun liveModulo(f: Formation) { val i = _live.value ?: return; i.m.setModulo(i.userHome, f); bump() }
    fun liveRitmo(r: Ritmo) { val i = _live.value ?: return; i.m.setRitmo(i.userHome, r); bump() }
    fun liveContropiede(on: Boolean) { val i = _live.value ?: return; i.m.setContropiede(i.userHome, on); bump() }
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
        var esito: Map<Long, EsitoGiocatore> = emptyMap()
        if (mr != null) {
            val r = i.m.report()
            val t = selectedTeam.value
            val prima = t?.let { dao.playersOnce(it.id) }.orEmpty().associateBy { it.id }
            // Forma: si aggiungono i voti dei giocatori della tua squadra (ultimi cinque)
            if (t != null) for (p in r.playedPlayers) if (p.id in (if (i.userHome) r.homePlayed else r.awayPlayed)) {
                val k = Forma.key(p.id)
                dao.setStato(Stato(k, Forma.aggiungi(dao.stato(k), r.playerRatings[p.id] ?: 6.0)))
            }
            val lg = mr.league
            dao.updateMatch(mr.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true))
            salvaStatistiche(_season.value, HashMap<Long, PlayerStat>().also { accumulaStat(it, _season.value, mr.homeTeamId, mr.awayTeamId, r) })
            dao.insertGoals(r.events.filter { it.type == EventType.GOAL && it.playerId != 0L }.map {
                Goal(season = _season.value, league = lg, round = mr.round,
                    teamId = if (it.home) mr.homeTeamId else mr.awayTeamId, playerId = it.playerId)
            })
            t?.let {
                registraPartita(it, dao.team(if (mr.homeTeamId == it.id) mr.awayTeamId else mr.homeTeamId), mr.homeTeamId == it.id, r, live = true)
                dopoMiaPartita(it.id, mr.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals), evento = true)
            }
            val staffBy = dao.allStaff().groupBy { it.teamId }
            val rng = kotlin.random.Random.Default
            for (id in listOf(mr.homeTeamId, mr.awayTeamId)) {
                val played = if (id == mr.homeTeamId) r.homePlayed else r.awayPlayed
                val beforeP = dao.playersOnce(id)
                val aggiornati = Medical.afterMatch(beforeP, played, Staffing.cura(staffBy, id), rng,
                    planFor(id, t?.id), Staffing.skill(staffBy, id, Staffing.ALLENATORE)).also { accrueInsurance(id, beforeP, it); registraInfortuni(id, beforeP, it) }
                val finali0 = if (id == t?.id) infortunioExtra(beforeP, aggiornati, played, rng) else aggiornati
                // Chi si è fatto male durante la partita salta qualche turno
                val finali = finali0.map { p -> if (p.id in r.infortunati && p.injuredRounds == 0) p.copy(injuredRounds = rng.nextInt(2, 6)) else p }
                dao.updatePlayers(finali.map { p -> if (p.id in played) apprendiRuolo(p) else p })
            }
            // Cosa è cambiato ai miei giocatori, per la schermata di fine partita
            if (t != null) esito = dao.playersOnce(t.id).mapNotNull { dopo ->
                val p = prima[dopo.id] ?: return@mapNotNull null
                val novita = buildList {
                    if (dopo.overall != p.overall) add("Livello ${if (dopo.overall > p.overall) "+" else ""}${dopo.overall - p.overall}")
                    if (dopo.fo != p.fo) add("Forma ${if (dopo.fo > p.fo) "+" else ""}${dopo.fo - p.fo}")
                    listOf(Triple(dopo.sec2, dopo.aff2, p.aff2 to p.sec2), Triple(dopo.sec3, dopo.aff3, p.aff3 to p.sec3),
                        Triple(dopo.sec4, dopo.aff4, p.aff4 to p.sec4)).forEach { (ruolo, aff, vecchio) ->
                        if (ruolo.isNotEmpty() && (ruolo != vecchio.second || aff > vecchio.first))
                            add("Ruolo ${TacticalRoles.label(ruolo)} al $aff%")
                    }
                }
                dopo.id to EsitoGiocatore(dopo.morale - p.morale, dopo.injuredRounds.takeIf { it > p.injuredRounds } ?: 0,
                    dopo.condition, novita)
            }.toMap()
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
        _live.value = _live.value?.copy(saved = true, tick = i.tick + 1, esito = esito)
    }

    fun closeLive() {
        val i = _live.value ?: return
        if (!i.m.finished) return
        liveJob?.cancel()
        _live.value = null
        viewModelScope.launch(Dispatchers.IO) { autoSalva() }
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

    /** Chiavi dello stato di mercato, per giocatore. */
    private object MK {
        const val GIORNATA = "giornata_mercato"     // contatore delle giornate giocate
        fun no(id: Long) = "no_$id"                 // risposte negative consecutive del giocatore (su 3)
        fun cool(id: Long) = "cool_$id"             // giornata da cui il giocatore torna a trattare
        fun pend(id: Long) = "pend_$id"             // prezzo proposto al club (0 = nessuna); dopo l'ok, prezzo concordato
        fun clubOk(id: Long) = "clubok_$id"         // 1 = il club ha accettato: si tratta con il giocatore
    }

    /**
     * Rilegge i dati solo quando le tabelle indicate cambiano (niente polling: meno CPU e batteria).
     * Emette subito il valore iniziale; le notifiche ravvicinate vengono unite.
     */
    private fun <T> osserva(vararg tabelle: String, carica: suspend () -> T): Flow<T> = callbackFlow {
        val obs = object : InvalidationTracker.Observer(arrayOf(*tabelle)) {
            override fun onInvalidated(tables: Set<String>) { trySend(Unit) }
        }
        trySend(Unit)
        db.invalidationTracker.addObserver(obs)
        awaitClose { db.invalidationTracker.removeObserver(obs) }
    }.conflate().map { carica() }.flowOn(Dispatchers.IO)

    /** Giocatori per cui il club ha già accettato (si tratta con il giocatore). */
    val clubAccepted: StateFlow<Set<Long>> = osserva("stato") {
        dao.pendingClubOffers().mapNotNull { s ->
            s.chiave.removePrefix("pend_").toLongOrNull()?.takeIf { dao.statoInt(MK.clubOk(it)) == 1 }
        }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    /** Stagione mostrata nella scheda statistiche (null = stagione in corso). */
    private val _statSeason = MutableStateFlow<String?>(null)
    val statSeasonAttiva: StateFlow<String> = combine(_statSeason, _season) { a, b -> a ?: b }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _season.value)
    val playerStats: StateFlow<List<PlayerStat>> = statSeasonAttiva.flatMapLatest { s ->
        osserva("player_stats") { dao.statsOf(s) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val statSeasons: StateFlow<List<String>> = osserva("player_stats") { dao.statsSeasons() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val clausole: StateFlow<List<Clausola>> = osserva("clausole") { dao.clausoleOnce() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val scoutMissions: StateFlow<List<ScoutMission>> = osserva("scout_missions") { dao.missionsOnce() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun pickStatSeason(s: String) { _statSeason.value = s }

    /** Convocazioni e presenze in nazionale, per la schermata Nazionali. */
    val convocazioniAll: StateFlow<List<Convocazione>> = osserva("convocazioni") { dao.allConvocazioni() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val nazionaleAll: StateFlow<List<NazionaleStat>> = osserva("nazionale_stats") { dao.allNazionaleStats() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _nazStagione = MutableStateFlow<String?>(null)
    val nazStagione: StateFlow<String> = combine(_nazStagione, _season) { a, b -> a ?: b }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _season.value)
    fun pickNazStagione(s: String) { _nazStagione.value = s }

    /** Tiri della squadra: rigoristi, punizionisti, battitori d'angolo e capitano scelto (liste in ordine di priorità). */
    suspend fun tiriDi(teamId: Long): SetPieces {
        suspend fun ids(k: String): List<Long> = dao.stato(k).orEmpty().split(",").mapNotNull { it.toLongOrNull() }
        return SetPieces(ids("rig_$teamId"), ids("pun_$teamId"), ids("ang_$teamId"), dao.stato("cap_$teamId")?.toLongOrNull())
    }

    /** Salva i tiri della squadra scelta; liste vuote o capitano vuoto = decide il capitano in campo. */
    fun salvaTiri(t: SetPieces) {
        val id = selectedTeam.value?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dao.setStato(Stato("rig_$id", t.rigori.joinToString(",")))
            dao.setStato(Stato("pun_$id", t.punizioni.joinToString(",")))
            dao.setStato(Stato("ang_$id", t.angoli.joinToString(",")))
            dao.setStato(Stato("cap_$id", t.capitano?.toString().orEmpty()))
            _notice.value = "Tiri e capitano salvati."
        }
    }

    val tiriUser: StateFlow<SetPieces> = selectedTeam.flatMapLatest { t ->
        osserva("stato") { if (t == null) SetPieces.NONE else tiriDi(t.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SetPieces.NONE)

    /** Un ruolo diverso dal naturale si impara giocandoci: dopo [PARTITE_PER_RUOLO] partite rende al 100%. */
    private suspend fun apprendiRuolo(p: Player): Player {
        val r = TacticalRoles.assigned(p)
        if (r.isEmpty() || r == p.pos) return p
        if ((r == p.sec2 && p.aff2 >= 100) || (r == p.sec3 && p.aff3 >= 100) || (r == p.sec4 && p.aff4 >= 100)) return p
        val key = "esp_${p.id}_$r"
        val n = (dao.stato(key)?.toIntOrNull() ?: 0) + 1
        dao.setStato(Stato(key, n.toString()))
        if (n < PARTITE_PER_RUOLO) return p
        return when {
            r == p.sec2 -> p.copy(aff2 = 100)
            r == p.sec3 -> p.copy(aff3 = 100)
            r == p.sec4 -> p.copy(aff4 = 100)
            p.sec2.isEmpty() -> p.copy(sec2 = r, aff2 = 100)
            else -> p.copy(sec3 = r, aff3 = 100)   // sostituisce il terzo ruolo
        }
    }

    /** Nazionale allenata dall'utente ("" = nessuna), offerta pendente e rosa convocabile. */
    val nazioneAllenata: StateFlow<String> = osserva("stato") { dao.stato("nazione_allenata").orEmpty() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val offertaNazionale: StateFlow<String> = osserva("stato") { dao.stato("offerta_nazionale").orEmpty() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val rosaNazionale: StateFlow<List<Player>> = osserva("rosa_nazionale", "players") {
        val ids = dao.rosaNazionale().map { it.playerId }.toSet()
        dao.allPlayers().filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    /** Tutti i giocatori con la nazionalità della nazionale allenata: candidati alla convocazione. */
    val giocatoriNazionalita: StateFlow<List<Player>> = nazioneAllenata.flatMapLatest { n ->
        osserva("players", "teams") {
            val asIds = dao.allTeams().filter { AllStar.isAllStar(it) }.map { it.id }.toSet()
            if (n.isEmpty()) emptyList() else dao.allPlayers().filter { it.nationality == n && it.teamId !in asIds }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun aggiungiConvocato(p: Player) {
        viewModelScope.launch(Dispatchers.IO) {
            val n = dao.stato("nazione_allenata").orEmpty()
            if (n.isEmpty() || p.nationality != n) return@launch
            if (dao.rosaNazionale().size >= Nazionali.CONVOCATI) {
                _notice.value = "La rosa è piena: ${Nazionali.CONVOCATI} convocati al massimo."
                return@launch
            }
            dao.insertRosaNazionale(RosaNazionale(p.id, n))
        }
    }

    fun togliConvocato(p: Player) {
        viewModelScope.launch(Dispatchers.IO) { dao.deleteRosaNazionale(p.id) }
    }

    /** Risposta all'offerta di una nazionale: accettando diventi ct (club e nazionale insieme). */
    fun rispondiNazionale(accetta: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val n = dao.stato("offerta_nazionale").orEmpty()
            if (n.isEmpty()) return@launch
            dao.setStato(Stato("offerta_nazionale", ""))
            if (accetta && dao.stato("nazione_allenata").isNullOrEmpty()) {
                dao.setStato(Stato("nazione_allenata", n))
                dao.clearRosaNazionale()
                _notice.value = "Sei il ct della nazionale di $n. Costruisci la rosa dalla schermata Nazionali."
            }
        }
    }

    /** Dimissioni: penalità di 15 punti di reputazione e scelta di una nuova squadra. */
    fun dimettiti() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.setStatoInt(StatoKeys.REPUTAZIONE, (dao.statoInt(StatoKeys.REPUTAZIONE) - 15).coerceAtLeast(0))
            dao.setStato(Stato("esonero", "0"))
            dao.setStato(Stato("offerte", ClubOffer.encode(emptyList())))
            prefs.edit().putBoolean("partita_iniziata", false).apply()
            _started.value = false
            _notice.value = "Ti sei dimesso (-15 di reputazione). Scegli la nuova squadra."
        }
    }

    /** Parere del vice allenatore sulla trattativa con il giocatore: monte ingaggi, durata e clausole richieste. */
    fun consiglioVice(p: Player, years: Int): String {
        val ask = wageAsk(p, years)
        return buildString {
            append(if (ask <= wageRoom()) "L'ingaggio chiesto rientra nel monte ingaggi. "
                   else "Il monte ingaggi non regge: serve spazio per ${money(ask)}. ")
            if (p.age >= 31 && years > 2) append("Oltre 2 anni chiede il 10% in più: una durata più corta costa meno. ")
            if (p.age < 24 && years < 3) append("Giovane: con 3 anni o più chiede il 10% in meno. ")
            append("Clausole richieste: bonus se 30 presenze, 20 gol, vittoria di campionato o coppa.")
        }
    }

    /** Offerta del tuo club: per un giocatore sotto contratto passa prima dal club, poi dal giocatore. */
    fun offer(p: Player, price: Long, wage: Long = wageAsk(p), years: Int = 3) {
        viewModelScope.launch {
            val buyer = selectedTeam.value ?: return@launch
            val giornata = dao.statoInt(MK.GIORNATA)
            val cool = dao.statoInt(MK.cool(p.id))
            val daAccordo = p.teamId == Market.SVINCOLATO_ID || preContratto(p) || dao.statoInt(MK.clubOk(p.id)) == 1
            when {
                dao.stato("ffp_blocco") == _season.value ->
                    _offer.value = OfferResult.Rejected("Mercato in entrata bloccato per il fair play finanziario: hai speso troppo rispetto agli incassi.")
                !daAccordo && !mercatoAperto() ->
                    _offer.value = OfferResult.Rejected("${_extra.value.finestra.nome}: i club trattano solo nelle finestre di mercato (inizio stagione e gennaio).")
                players.value.size >= Market.MAX_SQUAD ->
                    _offer.value = OfferResult.Rejected("Rosa piena (massimo ${Market.MAX_SQUAD} giocatori).")
                cool > giornata ->
                    _offer.value = OfferResult.Rejected("${p.name} è deluso e non tratta: torna disponibile tra ${cool - giornata} giornate.")
                p.teamId == Market.SVINCOLATO_ID || preContratto(p) || dao.statoInt(MK.clubOk(p.id)) == 1 ->
                    _offer.value = playerTalk(p, buyer, wage, years)
                else -> _offer.value = clubTalk(p, buyer, price, years)
            }
        }
    }

    /** Trattativa col club: la risposta non è immediata se l'offerta è valida (arriva alla prossima giornata). */
    private suspend fun clubTalk(p: Player, buyer: Team, price: Long, years: Int): OfferResult {
        // Con reputazione alta il club chiede un po' meno
        val d = Market.richiestaClub(p, dao.statoInt(StatoKeys.REPUTAZIONE))
        val squadraVenditrice = dao.playersOnce(p.teamId)
        val titolareChiave = squadraVenditrice.sortedByDescending { it.overall }.take(3).any { it.id == p.id }
        return when {
            price > buyer.budget -> OfferResult.Rejected("Budget insufficiente.")
            titolareChiave && price < (d * 1.4).toLong() ->
                OfferResult.Rejected("${p.name} è un titolare chiave: il club non lo cede sotto ${money((d * 1.4).toLong())}.")
            price < (d * 0.85).toLong() -> OfferResult.Rejected("Offerta troppo bassa: il club non tratta.")
            price < d -> OfferResult.Counter(p, d, 0L, years)
            else -> {
                dao.setStatoInt(MK.pend(p.id), price.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), 0, Int.MAX_VALUE)
                OfferResult.Pending("Il club valuta la proposta: la risposta arriva alla prossima giornata.")
            }
        }
    }

    /** Trattativa col giocatore: ingaggio e durata. Dopo tre risposte negative l'affare salta. */
    private suspend fun playerTalk(p: Player, buyer: Team, wage: Long, years: Int): OfferResult {
        val appeal = appealFor(p)
        val ask = Market.askedWage(p, appeal, years)
        return when {
            wage > wageRoom() ->
                OfferResult.Rejected("Oltre il budget ingaggi: restano ${money(wageRoom().coerceAtLeast(0))} l'anno.")
            Market.refuses(p, myStrengthNow(), appeal) ->
                negativa(p, "${p.name} rifiuta: preferisce una squadra più forte e vincente.")
            wage >= ask -> firma(p, buyer, wage, years)
            wage >= (ask * Allenatori.tolleranzaConReputazione(Market.tolleranza(p), dao.statoInt(StatoKeys.REPUTAZIONE))).toLong() ->
                OfferResult.WageCounter(p, 0L, ask, years)
            else -> negativa(p, "${p.name} chiede almeno ${money(ask)} l'anno.")
        }
    }

    /** Risposta negativa del giocatore: alla terza l'affare salta e il giocatore resta indisponibile per un periodo. */
    private suspend fun negativa(p: Player, msg: String): OfferResult {
        val n = dao.statoInt(MK.no(p.id)) + 1
        if (n < 3) {
            dao.setStatoInt(MK.no(p.id), n)
            return OfferResult.Rejected("$msg Risposta negativa $n di 3.")
        }
        val giorni = Market.cooldown(p)
        dao.setStatoInt(MK.no(p.id), 0)
        dao.setStatoInt(MK.cool(p.id), dao.statoInt(MK.GIORNATA) + giorni, 0, Int.MAX_VALUE)
        return OfferResult.Rejected("$msg Terza risposta negativa: l'affare salta. ${p.name} è deluso e non tratta per $giorni giornate.")
    }

    /** Firma: il prezzo concordato col club (zero per gli svincolati) va al club venditore. */
    /** Pre-contratto: nella finestra di gennaio un giocatore all'ultimo anno si firma senza pagare il club. */
    fun preContratto(p: Player): Boolean = p.teamId != Market.SVINCOLATO_ID && p.contract <= 1 && _extra.value.finestra == Finestre.Stato.INVERNALE

    private suspend fun firma(p: Player, buyer: Team, wage: Long, years: Int): OfferResult {
        val svincolato = p.teamId == Market.SVINCOLATO_ID || preContratto(p)
        val fee = if (svincolato) 0L else dao.statoInt(MK.pend(p.id)).toLong()
        val comm = Agenti.commissione(p, wage, years)
        // Acquisto a rate: metà subito, metà a fine stagione (nessun interesse)
        val dilazione = if (_rateMercato.value && fee >= 1_000_000L) fee / 2 else 0L
        val subito = fee - dilazione
        if (subito + comm > buyer.budget) return OfferResult.Rejected("Budget insufficiente: prezzo ${money(fee)} più commissione agente ${money(comm)}.")
        if (!svincolato) {
            val seller = dao.team(p.teamId)
            dao.updateTeam(seller.copy(budget = seller.budget + fee))
        }
        dao.updateTeam(buyer.copy(budget = buyer.budget - subito - comm))
        if (dilazione > 0L) dao.setStato(Stato("rate_mercato", ((dao.stato("rate_mercato")?.toLongOrNull() ?: 0L) + dilazione).toString()))
        dao.updatePlayer(p.copy(teamId = buyer.id, salary = wage, contract = years))
        dao.upsertClausole(listOf(Clausola(playerId = p.id, teamId = buyer.id,
            bonusPresenze = wage / 2, bonusGol = wage / 2, bonusCampionato = wage, bonusCoppa = wage / 2)))
        registra(buyer.id, "Acquisto ${p.name}" + (if (dilazione > 0L) " (metà subito)" else ""), -subito, buyer.budget - subito)
        val cart = Cartellini.decode(dao.stato(Cartellini.KEY)).filter { it.playerId != p.id }
        dao.setStato(Stato(Cartellini.KEY, Cartellini.encode(if (fee > 0L) cart + Cartellini.Cartellino(p.id, fee, years.coerceAtLeast(1)) else cart)))
        if (comm > 0) registra(buyer.id, "Commissione agente (${Agenti.nomeAgente(p)})", -comm, buyer.budget - subito - comm)
        dao.setStatoInt(MK.pend(p.id), 0)
        dao.setStatoInt(MK.clubOk(p.id), 0)
        dao.setStatoInt(MK.no(p.id), 0)
        if (buyer.id == selectedTeam.value?.id) effettiArrivo(p, buyer, wage)
        val durata = "$years ${if (years == 1) "anno" else "anni"}"
        val costo = (if (fee > 0) " e ${money(fee)} al club" + (if (dilazione > 0L) " (${money(dilazione)} a fine stagione)" else "") else "") + (if (comm > 0) ", più ${money(comm)} all'agente" else "")
        return OfferResult.Accepted("${p.name} firma: ${money(wage)} l'anno per $durata$costo.")
    }

    private fun cambiaMorale(rosa: List<Player>, delta: Map<Long, Int>): List<Player> =
        rosa.filter { it.id in delta }.map { it.copy(morale = (it.morale + (delta[it.id] ?: 0)).coerceIn(0, 100)) }

    /**
     * Arrivo di un giocatore nella tua rosa: chi gioca nel suo ruolo teme di perdere il posto, un ingaggio molto alto
     * fa gelosia, un campione (84+, o più forte di tutti nel ruolo) alza il morale; da 90 in su anche tifosi, clima e sponsor.
     */
    private suspend fun effettiArrivo(p: Player, buyer: Team, wage: Long) {
        val rosa = dao.playersOnce(buyer.id).filter { it.id != p.id }
        if (rosa.isEmpty()) return
        val delta = HashMap<Long, Int>()
        val note = ArrayList<String>()
        val rivali = rosa.filter { it.role == p.role && it.overall < p.overall }.sortedByDescending { it.overall }.take(2)
        rivali.forEach { delta[it.id] = (delta[it.id] ?: 0) - 3 }
        if (rivali.isNotEmpty()) note += "${rivali.joinToString { it.name }} temono di perdere il posto (morale -3)"
        val media = rosa.map { it.salary }.average()
        if (wage > media * 2.0) {
            val gelosi = rosa.filter { it.overall >= p.overall - 5 && it.salary * 2 < wage }.take(5)
            gelosi.forEach { delta[it.id] = (delta[it.id] ?: 0) - 2 }
            if (gelosi.isNotEmpty()) note += "${gelosi.size} compagni non gradiscono l'ingaggio di ${money(wage)} (morale -2)"
        }
        val migliore = rosa.filter { it.role == p.role }.maxOfOrNull { it.overall } ?: 0
        val bonus = when {
            p.overall >= 90 -> 4
            p.overall >= 84 -> 2
            p.overall >= migliore + 5 && migliore > 0 -> 1
            else -> 0
        }
        if (bonus > 0) {
            rosa.forEach { delta[it.id] = (delta[it.id] ?: 0) + bonus }
            note += "L'arrivo di ${p.name} dà fiducia a tutta la rosa (morale +$bonus)"
        }
        if (p.overall >= 90) {
            dao.setStatoInt(StatoKeys.TIFOSI, dao.statoInt(StatoKeys.TIFOSI) + 6)
            dao.setStatoInt(StatoKeys.CLIMA, dao.statoInt(StatoKeys.CLIMA) + 5)
            val premio = (SponsorDeal.decode(dao.stato("sponsor")).sumOf { it.annual } / 20).coerceAtLeast(100_000L)
            val tt = dao.team(buyer.id)
            dao.updateTeam(tt.copy(budget = tt.budget + premio))
            registra(tt.id, "Sponsor: effetto campione ${p.name}", premio, tt.budget + premio)
            note += "Tifosi in festa (+6), clima +5 e gli sponsor versano ${money(premio)}"
        }
        if (delta.isNotEmpty()) dao.updatePlayers(cambiaMorale(rosa, delta))
        if (note.isNotEmpty()) avviso(note.joinToString(". ") + ".")
    }

    /** Cessione: se parte un leader, un amico di qualcuno o un campione (84+), lo spogliatoio ne risente. */
    private suspend fun effettiPartenza(p: Player, me: Team) {
        val rosa = dao.playersOnce(me.id).filter { it.id != p.id }
        if (rosa.isEmpty()) return
        val delta = HashMap<Long, Int>()
        val note = ArrayList<String>()
        if (Leadership.carisma(p) >= 70) {
            rosa.forEach { delta[it.id] = (delta[it.id] ?: 0) - 3 }
            note += "${p.name} era un leader: lo spogliatoio lo rimpiange (morale -3)"
        }
        val amici = rosa.filter { it.nationality == p.nationality }.take(3)
        amici.forEach { delta[it.id] = (delta[it.id] ?: 0) - 3 }
        if (amici.isNotEmpty()) note += "${amici.joinToString { it.name }} perdono un amico (morale -3)"
        if (p.overall >= 84) {
            rosa.forEach { delta[it.id] = (delta[it.id] ?: 0) - 2 }
            dao.setStatoInt(StatoKeys.TIFOSI, dao.statoInt(StatoKeys.TIFOSI) - 4)
            note += "La cessione di un campione delude squadra e tifosi (morale -2, tifosi -4)"
        }
        if (delta.isNotEmpty()) dao.updatePlayers(cambiaMorale(rosa, delta))
        if (note.isNotEmpty()) avviso(note.joinToString(". ") + ".")
    }

    /** A ogni giornata il club risponde alle proposte della giornata prima (positiva o negativa). */
    private suspend fun risolviMercato() {
        dao.setStatoInt(MK.GIORNATA, dao.statoInt(MK.GIORNATA) + 1, 0, Int.MAX_VALUE)
        processaMissioni(kotlin.random.Random.Default)
        val rng = kotlin.random.Random.Default
        generaVoce(rng)
        for (s in dao.pendingClubOffers()) {
            val pid = s.chiave.removePrefix("pend_").toLongOrNull() ?: continue
            val prezzo = s.valore.toLongOrNull() ?: 0L
            if (prezzo <= 0L || dao.statoInt(MK.clubOk(pid)) == 1) continue
            val p = dao.playerById(pid) ?: continue
            if (rng.nextInt(100) < (if (_extra.value.ultimoGiorno) 97 else 85) && prezzo >= Market.richiestaClub(p, dao.statoInt(StatoKeys.REPUTAZIONE))) {
                dao.setStatoInt(MK.clubOk(pid), 1)
                _notice.value = "${p.name}: il club accetta. Ora puoi trattare con il giocatore."
            } else {
                dao.setStatoInt(MK.pend(pid), 0)
                _notice.value = "${p.name}: il club respinge la proposta."
            }
        }
    }

    /** Scheda di una squadra qualunque (consultazione): posizione attesa dalla forza, finanze, rosa, rivalità. */
    suspend fun infoSquadra(t: Team): InfoSquadra {
        val tutti = dao.allPlayers().groupBy { it.teamId }
        val lega = dao.allTeams().filter { it.nazione == t.nazione && it.campionato == t.campionato }
        val classifica = lega.sortedByDescending { SquadraInfo.media(tutti[it.id].orEmpty()) }
        val posizione = classifica.indexOfFirst { it.id == t.id } + 1
        val ps = tutti[t.id].orEmpty()
        return InfoSquadra(
            campionato = t.campionato,
            sponsor = t.sponsor,
            stadio = t.stadiumCapacity,
            posizioneAttesa = SquadraInfo.aspettativa(posizione, lega.size),
            media = SquadraInfo.media(ps),
            rosa = ps.size,
            budget = t.budget,
            budgetIngaggi = t.wageBudget,
            monteIngaggi = ps.sumOf { it.salary },
            ruoliCarenti = SquadraInfo.ruoliCarenti(ps),
            capitano = SquadraInfo.capitano(ps),
            rivale = Rivalita.rivaleDi(t.name)
        )
    }

    fun sell(p: Player) {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            if (players.value.size <= Market.MIN_SQUAD) {
                _offer.value = OfferResult.Rejected("Rosa troppo corta (minimo ${Market.MIN_SQUAD}).")
                return@launch
            }
            val conClausola = prefs.getBoolean("rivendita_on", false)
            val price = (Market.value(p) * 0.9).toLong().let { if (conClausola) Cartellini.prezzoConClausola(it) else it }
            // Compra un club della tua divisione che può permetterselo (prima il denaro veniva creato dal nulla)
            val buyer = teams.value.filter {
                it.id != me.id && it.nazione == me.nazione && it.campionato == me.campionato && it.budget >= price
            }.randomOrNull()
            if (buyer == null) {
                _offer.value = OfferResult.Rejected("Nessun club della tua divisione può permettersi ${p.name} a ${money(price)}.")
                return@launch
            }
            // Cessione a rate: metà subito, metà a fine stagione
            val dil = Cartellini.dilazione(price, _rateMercato.value)
            val subito = price - dil
            dao.updateTeam(me.copy(budget = me.budget + subito))
            dao.updateTeam(buyer.copy(budget = buyer.budget - price))
            dao.updatePlayer(p.copy(teamId = buyer.id, starter = false))
            registra(me.id, "Vendita ${p.name} a ${buyer.name}" + (if (dil > 0L) " (metà subito)" else ""), subito, me.budget + subito)
            if (dil > 0L) dao.setStato(Stato(Cartellini.KEY_CREDITI, ((dao.stato(Cartellini.KEY_CREDITI)?.toLongOrNull() ?: 0L) + dil).toString()))
            val carts = Cartellini.decode(dao.stato(Cartellini.KEY))
            val plus = Cartellini.plusvalenza(price, carts.firstOrNull { it.playerId == p.id })
            dao.setStato(Stato(Cartellini.KEY, Cartellini.encode(carts.filter { it.playerId != p.id })))
            dao.setStato(Stato(Cartellini.KEY_PLUS, Cartellini.aggiungiPlus(dao.stato(Cartellini.KEY_PLUS), _season.value, plus)))
            if (conClausola) dao.setStato(Stato(Cartellini.KEY_RIVENDITE, Cartellini.encodeR(
                Cartellini.decodeR(dao.stato(Cartellini.KEY_RIVENDITE)).filter { it.playerId != p.id } +
                    Cartellini.Rivendita(p.id, Cartellini.PCT_RIVENDITA, p.name))))
            caricaFinExtra()
            effettiPartenza(p, me)
            _offer.value = OfferResult.Accepted("${p.name} ceduto a ${buyer.name} per ${money(price)}" +
                (if (dil > 0L) " (${money(dil)} a fine stagione)" else "") +
                (if (plus >= 0) ". Plusvalenza ${money(plus)}" else ". Minusvalenza ${money(-plus)}") +
                (if (conClausola) ". Ti spetta il ${Cartellini.PCT_RIVENDITA}% se verrà rivenduto." else "."))
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

    /** Ragazzi dei vivai degli altri club osservati dai tuoi scout. */
    val scoutedGiovani: StateFlow<List<Youth>> =
        selectedTeam.flatMapLatest { t ->
            if (t == null) flowOf(emptyList()) else dao.scoutedYouths(t.id)
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
            val bonus = Strutture.bonusVivaio(dao.statoInt(Strutture.VIVAIO)) +
                Migliorie.effetti(Migliorie.decodeFatte(dao.stato("migliorie_fatte"))).vivaio
            val primaVolta = dao.youthsOnce(t.id).isEmpty()
            dao.insertYouths(if (primaVolta) Giovanili.iniziale(t, s, bonusVivaio = bonus) else Giovanili.nuovaLeva(t, s, bonusVivaio = bonus))
            dao.updateTeam(t.copy(youthClaimed = s))
            _notice.value = if (primaVolta) "Il settore giovanile ha ${Giovanili.iniziale(t, s).size} ragazzi dai ${Giovanili.ETA_MIN} ai ${Giovanili.ETA_MAX} anni."
                else "Sono arrivati 5 nuovi ragazzi nelle giovanili."
        }
    }

    // ---- Settore giovanile: scheda dei progressi e apparizioni in prima squadra ----
    private val _scheda = MutableStateFlow<Giovanili.Scheda?>(null)
    val scheda: StateFlow<Giovanili.Scheda?> = _scheda
    fun chiudiScheda() { _scheda.value = null }

    fun richiediScheda(y: Youth) {
        viewModelScope.launch(Dispatchers.IO) {
            val t = selectedTeam.value ?: return@launch
            val prima = dao.stato("viv_prev_${y.id}")?.toIntOrNull() ?: y.overall
            _scheda.value = Giovanili.scheda(y, prima, dao.team(t.id).youthCoachSkill)
        }
    }

    /** Il ragazzo si aggrega alla prima squadra per la prossima partita, poi torna nelle giovanili. */
    fun apparizione(y: Youth) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            if (!Giovanili.puoDebuttare(y)) { _notice.value = "${y.name} è troppo giovane: servono almeno ${Giovanili.ETA_APPARIZIONE} anni."; return@launch }
            val corrente = Giovanili.decodeApparizioni(dao.stato("apparizioni"))
            if (corrente.any { it.first == y.id }) { _notice.value = "${y.name} è già con la prima squadra."; return@launch }
            if (dao.playersOnce(me.id).size >= Market.MAX_SQUAD) { _notice.value = "Rosa piena (massimo ${Market.MAX_SQUAD})."; return@launch }
            val p = Attr.fill(Player(teamId = me.id, name = y.name, role = y.role, age = y.age, overall = y.overall, morale = 80,
                salary = 0, nationality = y.nationality, potential = y.potential, contract = 1), kotlin.random.Random)
            val ids = dao.insertPlayers(listOf(p))
            dao.setStato(Stato("apparizioni", Giovanili.encodeApparizioni(corrente + (y.id to ids.first()))))
            _notice.value = "${y.name} è con la prima squadra per la prossima partita: schieralo in Tattica."
            caricaExtra()
        }
    }

    /** Dopo la partita gli aggregati tornano nelle giovanili; chi ha giocato guadagna esperienza. */
    private suspend fun concludiApparizioni() {
        val l = Giovanili.decodeApparizioni(dao.stato("apparizioni"))
        if (l.isEmpty()) return
        val stats = dao.statsOf(_season.value).associateBy { it.playerId }
        val youths = dao.allYouths().associateBy { it.id }
        val righe = mutableListOf<String>()
        l.forEach { (yid, pid) ->
            val p = dao.playerById(pid) ?: return@forEach
            val y = youths[yid]
            val giocato = (stats[pid]?.presenze ?: 0) > 0
            if (y != null && giocato) dao.updateYouths(listOf(y.copy(overall = (y.overall + 2).coerceAtMost(y.potential))))
            dao.deletePlayer(p)
            righe += "${p.name}: " + if (giocato) "ha giocato, esperienza preziosa (+2)" else "è rimasto in panchina"
        }
        dao.setStato(Stato("apparizioni", ""))
        if (righe.isNotEmpty()) avviso("Apparizioni: " + righe.joinToString("; ") + ". Tornano nelle giovanili.")
    }

    /** Fine stagione: i giovani crescono di un anno; a 18 anni chi non è stato promosso lascia il vivaio. */
    private suspend fun cresciGiovani(me: Team): String {
        concludiApparizioni()
        val tutti = dao.allYouths()
        if (tutti.isEmpty()) return ""
        val rng = kotlin.random.Random.Default
        val coach = dao.team(me.id).youthCoachSkill
        val bonus = Strutture.bonusVivaio(dao.statoInt(Strutture.VIVAIO)) +
            Migliorie.effetti(Migliorie.decodeFatte(dao.stato("migliorie_fatte"))).vivaio
        val restano = ArrayList<Youth>(); val via = ArrayList<Youth>(); val progressi = ArrayList<Pair<Youth, Int>>()
        tutti.forEach { y ->
            dao.setStato(Stato("viv_prev_${y.id}", y.overall.toString()))
            val g = Giovanili.cresci(y, rng, coach, bonus)
            if (Giovanili.scaduto(g)) via += y else { restano += g; progressi += g to (g.overall - y.overall) }
        }
        dao.updateYouths(restano)
        via.forEach { dao.deleteYouth(it) }
        dao.cleanReportsGiovani()
        val top = progressi.sortedByDescending { it.second }.take(3).joinToString { "${it.first.name} +${it.second}" }
        return "Vivaio: tutti crescono di un anno (migliori: $top)." +
            (if (via.isNotEmpty()) " Lasciano per limiti d'età: ${via.joinToString { it.name }}." else "") +
            " Chiedi la scheda ai tuoi allenatori dalla sezione Vivaio."
    }

    fun promote(y: Youth) {
        viewModelScope.launch {
            if (players.value.size >= Market.MAX_SQUAD) {
                _notice.value = "Rosa piena (massimo ${Market.MAX_SQUAD} giocatori)."
                return@launch
            }
            val app = Giovanili.decodeApparizioni(dao.stato("apparizioni"))
            val aggregato = app.firstOrNull { it.first == y.id }
            if (aggregato != null) {
                // Già in rosa per l'apparizione: diventa un giocatore a tutti gli effetti
                dao.playerById(aggregato.second)?.let { dao.updatePlayer(it.copy(salary = 30_000, contract = 3)) }
                dao.setStato(Stato("apparizioni", Giovanili.encodeApparizioni(app - aggregato)))
            } else dao.insertPlayer(
                Attr.fill(Player(teamId = y.teamId, name = y.name, role = y.role, age = y.age,
                    overall = y.overall, morale = 75, salary = 30_000, nationality = y.nationality,
                    potential = y.potential, contract = 3), kotlin.random.Random)
            )
            dao.deleteYouth(y)
            inc("c_giovani")
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

    /** Presenze e gol di una partita, accumulati in memoria e salvati a fine giornata. */
    private fun accumulaStat(acc: MutableMap<Long, PlayerStat>, season: String, homeId: Long, awayId: Long, r: MatchReport) {
        fun riga(pid: Long, tid: Long): PlayerStat = acc[pid] ?: PlayerStat(season, pid, tid)
        r.homePlayed.forEach { pid -> riga(pid, homeId).let { acc[pid] = it.copy(presenze = it.presenze + 1) } }
        r.awayPlayed.forEach { pid -> riga(pid, awayId).let { acc[pid] = it.copy(presenze = it.presenze + 1) } }
        r.events.filter { it.type == EventType.GOAL && it.playerId != 0L }.forEach { e ->
            val pid = e.playerId
            riga(pid, if (e.home) homeId else awayId).let { acc[pid] = it.copy(gol = it.gol + 1) }
        }
    }

    /** Somma le statistiche accumulate a quelle già salvate della stagione. */
    private suspend fun salvaStatistiche(season: String, acc: MutableMap<Long, PlayerStat>) {
        if (acc.isEmpty()) return
        val gia = dao.statsOf(season).associateBy { it.playerId }
        dao.upsertStats(acc.values.map { n ->
            val vecchia = gia[n.playerId]
            if (vecchia == null) n else n.copy(presenze = vecchia.presenze + n.presenze, gol = vecchia.gol + n.gol)
        })
        acc.clear()
    }

    /** Il club ha più osservatori: la loro abilità migliore è quella usata per la scheda squadra e le scoperte. */
    private suspend fun syncScoutSkill(teamId: Long) {
        val t = dao.team(teamId)
        val migliore = dao.staffOnce(teamId).filter { it.role == Staffing.SCOUT }.maxOfOrNull { it.skill }
        dao.updateTeam(t.copy(scoutSkill = migliore ?: Staffing.VUOTO))
    }

    /** Manda un osservatore a cercare in un'area del mondo: rientra dopo alcune giornate. */
    fun inviaOsservatore(s: Staff, area: String) {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            if (dao.missionsOnce().any { it.staffId == s.id }) {
                _notice.value = "${s.name} è già in missione."
                return@launch
            }
            val giornate = Missioni.durata(s.skill)
            dao.saveMissions(listOf(ScoutMission(teamId = me.id, staffId = s.id, area = area, giornate = giornate)))
            _notice.value = "${s.name} parte per $area: rientro tra $giornate ${if (giornate == 1) "giornata" else "giornate"}."
        }
    }

    /** Ogni giornata le missioni avanzano; al rientro gli osservatori portano giocatori svincolati. */
    private suspend fun processaMissioni(rng: kotlin.random.Random) {
        val staff = dao.allStaff().associateBy { it.id }
        for (m in dao.missionsOnce()) {
            val restanti = m.giornate - 1
            if (restanti > 0) { dao.saveMissions(listOf(m.copy(giornate = restanti))); continue }
            dao.deleteMissions(listOf(m))
            val sc = staff[m.staffId] ?: continue
            val trovati = Missioni.trova(m.area, sc.skill, rng)
            if (trovati.isNotEmpty()) dao.insertPlayers(trovati)
            _notice.value = if (trovati.isEmpty()) "${sc.name} rientra da ${m.area} senza giocatori interessanti."
                else "${sc.name} rientra da ${m.area}: ${trovati.size} svincolati disponibili nel mercato."
        }
    }

    /** Nazionali con una rosa sufficiente per convocare: i giocatori raggruppati per nazionalità. */
    private suspend fun nazioniPool(): Map<String, List<Player>> {
        val asIds = dao.allTeams().filter { AllStar.isAllStar(it) }.map { it.id }.toSet()
        return dao.allPlayers().filter { it.teamId != Market.SVINCOLATO_ID && it.teamId !in asIds }.groupBy { it.nationality }
            .filter { (_, ps) -> Medical.available(ps).size >= Nazionali.MIN_ROSA }
    }

    /** Registra convocazioni, presenze e gol di una partita tra nazionali. */
    private suspend fun registraNazionale(
        season: String, competizione: String, round: Int, casa: String, trasf: String,
        convCasa: List<Player>, convTrasf: List<Player>, r: MatchReport
    ) {
        val esito = "$casa ${r.homeGoals}-${r.awayGoals} $trasf"
        dao.insertConvocazioni(
            convCasa.map { Convocazione(season = season, competizione = competizione, round = round,
                playerId = it.id, nazione = casa, risultato = esito) } +
                convTrasf.map { Convocazione(season = season, competizione = competizione, round = round,
                    playerId = it.id, nazione = trasf, risultato = esito) }
        )
        val acc = HashMap<Long, NazionaleStat>()
        val gia = dao.nazionaleStatsOf(season).associateBy { it.playerId }
        fun riga(pid: Long, naz: String): NazionaleStat = acc[pid] ?: gia[pid] ?: NazionaleStat(season, pid, naz)
        r.homePlayed.forEach { pid -> riga(pid, casa).let { acc[pid] = it.copy(presenze = it.presenze + 1) } }
        r.awayPlayed.forEach { pid -> riga(pid, trasf).let { acc[pid] = it.copy(presenze = it.presenze + 1) } }
        r.events.filter { it.type == EventType.GOAL && it.playerId != 0L }.forEach { e ->
            riga(e.playerId, if (e.home) casa else trasf).let { acc[e.playerId] = it.copy(gol = it.gol + 1) }
        }
        dao.upsertNazionaleStats(acc.values.toList())
    }

    /** Data internazionale (una giornata di campionato): le nazionali si affrontano; i convocati saltano il club. */
    private suspend fun giocaDataNazionali(season: String, round: Int) {
        if (round !in Nazionali.DATE) return
        if (dao.convocazioniData(season, round).isNotEmpty()) return
        val rng = kotlin.random.Random.Default
        val pool = nazioniPool().toMutableMap()
        val base = dao.allTeams().firstOrNull() ?: return
        // La nazionale allenata dall'utente usa la sua rosa e gioca sempre
        val mia = dao.stato("nazione_allenata").orEmpty()
        if (mia.isNotEmpty()) {
            val ids = dao.rosaNazionale().map { it.playerId }.toSet()
            val rosa = dao.allPlayers().filter { it.id in ids }
            if (Medical.available(rosa).size >= Nazionali.MIN_ROSA) pool[mia] = rosa else pool.remove(mia)
        }
        val ordine = (if (mia.isNotEmpty() && pool.containsKey(mia)) listOf(mia) else emptyList()) +
            (pool.keys - listOf(mia)).shuffled(rng)
        ordine.chunked(2).filter { it.size == 2 }.forEachIndexed { i, coppia ->
            val a = coppia[0]
            val b = coppia[1]
            val ca = Nazionali.convoca(pool.getValue(a))
            val cb = Nazionali.convoca(pool.getValue(b))
            val r = Nazionali.gioca(Nazionali.squadra(base, a, 2 * i), ca, Nazionali.squadra(base, b, 2 * i + 1), cb, rng)
            registraNazionale(season, "Amichevole internazionale", round, a, b, ca, cb, r)
        }
    }

    /** Nazionali Under 21 (solo IA): giocatori con al massimo 21 anni, calendario proprio. */
    private suspend fun poolU21(): Map<String, List<Player>> =
        nazioniPool().mapNotNull { (n, ps) ->
            val g = Nazionali.eleggibiliU21(ps)
            if (Medical.available(g).size >= Nazionali.MIN_ROSA) Nazionali.nomeU21(n) to g else null
        }.toMap()

    private suspend fun giocaDataU21(season: String, round: Int) {
        if (round !in Nazionali.DATE_U21) return
        if (dao.convocazioniData(season, round).isNotEmpty()) return
        val rng = kotlin.random.Random.Default
        val pool = poolU21()
        val base = dao.allTeams().firstOrNull() ?: return
        pool.keys.shuffled(rng).chunked(2).filter { it.size == 2 }.forEachIndexed { i, coppia ->
            val ca = Nazionali.convoca(pool.getValue(coppia[0]))
            val cb = Nazionali.convoca(pool.getValue(coppia[1]))
            val r = Nazionali.gioca(Nazionali.squadra(base, coppia[0], 2 * i), ca, Nazionali.squadra(base, coppia[1], 2 * i + 1), cb, rng)
            registraNazionale(season, "Amichevole Under 21", round, coppia[0], coppia[1], ca, cb, r)
        }
    }

    /** Eliminazione diretta: turni sorteggiati, il pareggio si decide a caso (come ai rigori). */
    /** Restituisce le nazionali uscite sotto le attese (ct esonerato): il posto in classifica di forza decide l'attesa. */
    private suspend fun eliminazione(
        season: String, nome: String, partecipanti: List<String>, pool: Map<String, List<Player>>,
        base: Team, rng: kotlin.random.Random
    ): List<String> {
        if (partecipanti.size < 2) return emptyList()
        val uscite = HashMap<String, Int>()   // nazione -> numero di squadre rimaste nel turno in cui è uscita
        var turno = partecipanti.shuffled(rng)
        while (turno.size > 1) {
            val etichetta = when (turno.size) { 16 -> "Ottavi"; 8 -> "Quarti"; 4 -> "Semifinali"; 2 -> "Finale"; else -> "Turno" }
            val prossimo = ArrayList<String>()
            turno.chunked(2).filter { it.size == 2 }.forEachIndexed { i, coppia ->
                val a = coppia[0]
                val b = coppia[1]
                val ca = Nazionali.convoca(pool.getValue(a))
                val cb = Nazionali.convoca(pool.getValue(b))
                val r = Nazionali.gioca(Nazionali.squadra(base, a, 2 * i), ca, Nazionali.squadra(base, b, 2 * i + 1), cb, rng)
                registraNazionale(season, "$nome · $etichetta", 0, a, b, ca, cb, r)
                val vince = when {
                    r.homeGoals > r.awayGoals -> a
                    r.homeGoals < r.awayGoals -> b
                    else -> if (rng.nextBoolean()) a else b
                }
                prossimo += vince
                uscite[if (vince == a) b else a] = turno.size
            }
            turno = prossimo
        }
        dao.insertAlbo(listOf(AlboEntry(season = season, nazione = if (nome == "Mondiali") "Mondo" else "Europa",
            competizione = nome, vincitore = turno.first())))
        // Attese: le prime 4 per forza devono arrivare in semifinale, le 5-8 ai quarti (o oltre)
        return uscite.filter { (n, size) ->
            val posto = partecipanti.indexOf(n) + 1
            val attesa = when { posto <= 4 -> 4; posto <= 8 -> 8; else -> 16 }
            size > attesa
        }.keys.toList()
    }

    /** Mondiali (16 squadre) ed Europei (8 europee) a fine stagione, nelle stagioni giuste. */
    /** Restituisce le nazionali i cui ct sono stati esonerati per risultati sotto le attese. */
    private suspend fun tornei(season: String, annoInizio: Int): List<String> {
        val rng = kotlin.random.Random.Default
        val pool = nazioniPool()
        val base = dao.allTeams().firstOrNull() ?: return emptyList()
        val licenziati = ArrayList<String>()
        if (Nazionali.mondiale(annoInizio)) {
            val top = pool.keys.sortedByDescending { Nazionali.forza(pool.getValue(it)) }.take(16)
            licenziati += eliminazione(season, "Mondiali", top, pool, base, rng)
        }
        if (Nazionali.europei(annoInizio)) {
            val top = pool.keys.filter { it in Nazionali.EUROPA }
                .sortedByDescending { Nazionali.forza(pool.getValue(it)) }.take(8)
            licenziati += eliminazione(season, "Europei", top, pool, base, rng)
        }
        if (Nazionali.europeoU21(annoInizio)) {
            val pu = poolU21()
            val top = pu.keys.filter { Nazionali.senzaU21(it) in Nazionali.EUROPA }
                .sortedByDescending { Nazionali.forza(pu.getValue(it)) }.take(8)
            eliminazione(season, "Europeo Under 21", top, pu, base, rng)   // i ct delle U21 non vengono esonerati
        }
        return licenziati.distinct()
    }

    /** Scheda completa di un giocatore: squadra, clausola, stagioni, nazionale e convocazioni. */
    suspend fun dettaglioGiocatore(p: Player): DettaglioGiocatore {
        val fresco = dao.playerById(p.id) ?: p
        val squadra = if (fresco.teamId == Market.SVINCOLATO_ID) "Svincolato"
            else dao.allTeams().firstOrNull { it.id == fresco.teamId }?.name ?: "—"
        return DettaglioGiocatore(
            player = fresco,
            squadra = squadra,
            clausola = dao.clausoleOnce().firstOrNull { it.playerId == p.id },
            stagioni = dao.allStats().filter { it.playerId == p.id }.sortedByDescending { it.season },
            nazionaleStagioni = dao.allNazionaleStats().filter { it.playerId == p.id }.sortedByDescending { it.season },
            convocazioni = dao.convocazioniDi(p.id),
            squadreStagioni = dao.allTeams().associate { it.id to it.name },
            votiStagioni = dao.stato("voti").orEmpty().split(";").mapNotNull { e ->
                val x = e.split(":"); if (x.size == 4 && x[1] == p.id.toString()) x[0] to ((x[2].toDoubleOrNull() ?: 0.0) / 10.0 / (x[3].toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0)) else null
            }.toMap(),
            infortuni = StoricoInfortuni.decode(dao.stato(StoricoInfortuni.KEY)).filter { it.playerId == p.id }.map { it.stagione to it.turni },
            minutiPct = if (fresco.teamId == selectedTeam.value?.id) Minutaggio.percentuale(Minutaggio.decode(dao.stato(Minutaggio.KEY))[p.id] ?: 0, dao.statoInt(Minutaggio.KEY_PARTITE)) else null,
            forma = dao.stato(Forma.key(p.id)).orEmpty().split(",").mapNotNull { it.toIntOrNull()?.let { v -> v / 10.0 } }
        )
    }

    /** Storico infortuni della tua squadra (per la scheda giocatore). */
    private suspend fun registraInfortuni(teamId: Long, before: List<Player>, after: List<Player>) {
        if (teamId != selectedTeam.value?.id) return
        val n = StoricoInfortuni.nuovi(before, after, _season.value)
        if (n.isNotEmpty()) dao.setStato(Stato(StoricoInfortuni.KEY, StoricoInfortuni.encode(StoricoInfortuni.decode(dao.stato(StoricoInfortuni.KEY)) + n)))
    }

    /** Premio partita promesso: il morale sale solo per questa gara. */
    private suspend fun conPremio(ps: List<Player>): List<Player> =
        if (dao.stato(PremioPartita.KEY) == "1") ps.map { it.copy(morale = (it.morale + PremioPartita.MORALE).coerceAtMost(100)) } else ps

    fun promettiPremio(si: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.setStato(Stato(PremioPartita.KEY, if (si) "1" else ""))
            _premio.value = si
            if (si) _notice.value = "Premio partita promesso: ${money(PremioPartita.costo(players.value.sumOf { it.salary }))} se vincete la prossima."
        }
    }
    private val _premio = MutableStateFlow(false)
    val premioPromesso: StateFlow<Boolean> = _premio

    /** Dopo una gara: accumula il rimborso per gli infortuni nuovi della tua squadra (pagato a fine stagione). */
    private fun accrueInsurance(teamId: Long, before: List<Player>, after: List<Player>) {
        val lvl = _insurance.value
        if (lvl == Insurance.NONE || teamId != selectedTeam.value?.id) return
        val prev = before.associateBy { it.id }
        val me = selectedTeam.value
        val turni = Insurance.turni(teams.value.count { me != null && it.nazione == me.nazione && it.campionato == me.campionato })
        val add = after.sumOf { a ->
            val b = prev[a.id]
            if (b != null && b.injuredRounds == 0 && a.injuredRounds > 0) Insurance.refund(a.salary, a.injuredRounds, lvl, turni) else 0L
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

    /** Missione negli altri vivai di un campionato: osserva 2 ragazzi in ciascuno di 4 club. */
    fun sendScoutGiovani(nazione: String, campionato: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val cost = Scouting.cost(me.scoutSkill)
            if (cost > me.budget) { _notice.value = "Budget insufficiente (servono ${money(cost)})."; return@launch }
            val rng = kotlin.random.Random.Default
            val club = dao.clubsOfLeague(nazione, campionato, me.id)
            if (club.isEmpty()) { _notice.value = "Nessun vivaio da osservare in questo campionato."; return@launch }
            val s = _season.value
            val vivai = HashMap<Long, List<Youth>>()
            club.shuffled(rng).take(ScoutingGiovani.CLUB_PER_MISSIONE + 2).forEach { c ->
                var l = dao.youthsOnce(c.id)
                if (l.size < ScoutingGiovani.MIN_VIVAIO_AI) {
                    dao.insertYouths(if (l.isEmpty()) Giovanili.iniziale(c, s, rng) else Giovanili.nuovaLeva(c, s, rng))
                    l = dao.youthsOnce(c.id)
                }
                vivai[c.id] = l
            }
            val picks = ScoutingGiovani.scegli(vivai, rng)
            if (picks.isEmpty()) { _notice.value = "Gli scout non hanno trovato nessuno."; return@launch }
            dao.upsertReports(picks.map {
                ScoutReport(me.id, ScoutingGiovani.chiave(it), (dao.report(me.id, ScoutingGiovani.chiave(it))?.observations ?: 0) + 1)
            })
            dao.updateTeam(me.copy(budget = me.budget - cost))
            registra(me.id, "Scouting vivai (${picks.size} ragazzi)", -cost, me.budget - cost)
            _notice.value = "Lo scout ha visto ${picks.size} ragazzi nei vivai di $campionato (${money(cost)})."
        }
    }

    fun followGiovane(y: Youth) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            if (Scouting.FOLLOW_COST > me.budget) { _notice.value = "Budget insufficiente."; return@launch }
            val k = ScoutingGiovani.chiave(y)
            dao.upsertReports(listOf(ScoutReport(me.id, k, (dao.report(me.id, k)?.observations ?: 0) + 1)))
            dao.updateTeam(me.copy(budget = me.budget - Scouting.FOLLOW_COST))
            registra(me.id, "Osservazione ${y.name} (vivaio)", -Scouting.FOLLOW_COST, me.budget - Scouting.FOLLOW_COST)
        }
    }

    /** Porta nel tuo vivaio un ragazzo di un altro club, pagando il cartellino. */
    fun ingaggiaGiovane(y: Youth) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val prezzo = ScoutingGiovani.prezzo(y)
            if (dao.youthsOnce(me.id).size >= ScoutingGiovani.MAX_MIO_VIVAIO) {
                _notice.value = "Il tuo vivaio è pieno (massimo ${ScoutingGiovani.MAX_MIO_VIVAIO} ragazzi)."; return@launch
            }
            if (prezzo > me.budget) { _notice.value = "Budget insufficiente (servono ${money(prezzo)})."; return@launch }
            dao.updateYouths(listOf(y.copy(teamId = me.id, season = _season.value)))
            dao.deleteReport(me.id, ScoutingGiovani.chiave(y))
            dao.updateTeam(me.copy(budget = me.budget - prezzo))
            registra(me.id, "Cartellino ${y.name} (vivaio)", -prezzo, me.budget - prezzo)
            _notice.value = "${y.name} entra nel tuo vivaio (${money(prezzo)})."
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
        assicuraAllStar()
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
    fun setRuoloPartita(p: Player, ruolo: String) { viewModelScope.launch { dao.updatePlayer(p.copy(ruoloPartita = ruolo, compito = "")) } }
    /** Compito di ruolo scelto per il giocatore nel ruolo in cui gioca ("" = nessuno). */
    fun setCompito(p: Player, codice: String) { viewModelScope.launch { dao.updatePlayer(p.copy(compito = codice)) } }
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

    // Senza i giocatori All Star: non si possono ingaggiare, cercare né confrontare
    val allPlayers: StateFlow<List<Player>> =
        combine(dao.allPlayersFlow(), teams) { ps, ts ->
            val asIds = ts.filter { AllStar.isAllStar(it) }.map { it.id }.toSet()
            ps.filter { it.teamId !in asIds }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---- Staff: allenatore e preparatore della tua squadra ----
    val staff: StateFlow<List<Staff>> =
        selectedTeam.flatMapLatest { t ->
            if (t == null) flowOf(emptyList()) else dao.staffOf(t.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _candidates = MutableStateFlow(Staffing.candidates(kotlin.random.Random.Default))
    val candidates: StateFlow<List<Staff>> = _candidates

    fun refreshCandidates() {
        _candidates.value = Staffing.candidates(kotlin.random.Random.Default) + Staffing.candidatiOsservatori(kotlin.random.Random.Default)
    }

    /** Assume un candidato: sostituisce chi occupava già quel ruolo. Lo stipendio si paga a fine stagione. */
    fun hire(c: Staff) {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            if (c.role == Staffing.SCOUT) {
                if (dao.staffOnce(me.id).count { it.role == Staffing.SCOUT } >= Staffing.MAX_OSSERVATORI) {
                    _notice.value = "Hai già ${Staffing.MAX_OSSERVATORI} osservatori: licenziane uno per assumerne un altro."
                    return@launch
                }
                dao.addStaff(c.copy(id = 0, teamId = me.id))
                syncScoutSkill(me.id)
                _candidates.value = _candidates.value - c
                _notice.value = "${c.name} ingaggiato come osservatore. Stipendio ${money(c.salary)} l'anno, pagato a fine stagione."
                return@launch
            }
            val old = dao.staffOnce(me.id).filter { it.role == c.role && it.specialita == c.specialita }
            if (old.isNotEmpty()) dao.deleteStaffs(old)
            dao.addStaff(c.copy(id = 0, teamId = me.id))
            syncTeamSkill(me.id, c.role, c.skill)
            _candidates.value = _candidates.value - c
            val sost = old.firstOrNull()?.let { " Sostituisce ${it.name}." } ?: ""
            _notice.value = "${c.name} ingaggiato come ${Staffing.roleName(c.role, c.specialita).lowercase()}.$sost " +
                "Stipendio ${money(c.salary)} l'anno, pagato a fine stagione."
        }
    }

    fun fire(s: Staff) {
        viewModelScope.launch {
            dao.deleteStaffs(listOf(s))
            dao.deleteMissions(dao.missionsOnce().filter { it.staffId == s.id })
            syncTeamSkill(s.teamId, s.role, Staffing.VUOTO)
            _notice.value = "${s.name} è stato licenziato."
        }
    }

    /** Vivaio e scout leggono le abilità del club: assumere le aggiorna, lasciare il ruolo vuoto le riporta a VUOTO. */
    private suspend fun syncTeamSkill(teamId: Long, role: String, skill: Int) {
        val t = dao.team(teamId)
        when (role) {
            Staffing.GIOVANILI -> dao.updateTeam(t.copy(youthCoachSkill = skill))
            Staffing.SCOUT -> syncScoutSkill(teamId)
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
            Staffing.cura(staffBy, h.id), rng,
            planFor(h.id, me.id), Staffing.skill(staffBy, h.id, Staffing.ALLENATORE))
            .also { accrueInsurance(h.id, cache[h.id] ?: emptyList(), it) }
        cache[a.id] = Medical.afterMatch(cache[a.id] ?: emptyList(), r.awayPlayed,
            Staffing.cura(staffBy, a.id), rng,
            planFor(a.id, me.id), Staffing.skill(staffBy, a.id, Staffing.ALLENATORE))
            .also { accrueInsurance(a.id, cache[a.id] ?: emptyList(), it) }
        return r
    }

    /**
     * Numeri di maglia di inizio stagione, per tutte le squadre. Chi ha già un numero lo mantiene.
     * Restituisce la nota da mostrare: chi li ha assegnati (allenatore in seconda o società).
     */
    private suspend fun assegnaNumeri(meId: Long): String {
        val ritirati = Storia.decodeBandiere(dao.stato("storia_hall")).filter { Storia.magliaRitirata(it) }.map { it.numero }.toSet()
        val updated = dao.allPlayers().groupBy { it.teamId }.flatMap { (tid, ps) ->
            val nums = ShirtNumbers.assign(ps, if (tid == meId) ritirati else emptySet())
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
            chiudiPrestiti()
            val teamsNow = dao.allTeams().filter { !AllStar.isAllStar(it) }
            val cache = HashMap(dao.allPlayers().groupBy { it.teamId })
            val byTeam = cache
            val leagues = teamsNow.groupBy { it.nazione to it.campionato }
            val formations = Formation.values()
            val staffAll = dao.allStaff()
            val staffBy = staffAll.groupBy { it.teamId }

            // 1) Tutti i campionati: genera il calendario se manca e gioca le partite rimaste
            val finals = HashMap<Pair<String, String>, List<it.androcalcio.engine.StandingRow>>()
            val statAcc = HashMap<Long, PlayerStat>()
            val goalsSeason = ArrayList<Goal>()
            for ((nc, ts) in leagues) {
                val key = "${nc.first}|${nc.second}"
                if (dao.fixtureCount(key, s) == 0) {
                    dao.insertMatches(Fixtures.generate(ts.map { it.id }, s, key))
                }
                val byId = ts.associateBy { it.id }
                // In ordine di giornata: gli infortunati saltano davvero i turni successivi
                val turniLega = dao.matchesOnce(key, s).filter { !it.played }.map { it.round }.distinct()
                turniLega.forEach { giocaDataNazionali(s, it); giocaDataU21(s, it) }
                val assentiLega: Map<Int, Set<Long>> = turniLega.associateWith { r ->
                    if (Cup.isCup(r)) emptySet() else dao.convocazioniData(s, r).map { it.playerId }.toSet()
                }
                val sims = dao.matchesOnce(key, s).filter { !it.played }.sortedBy { it.round }.mapNotNull { m ->
                    val h = byId[m.homeTeamId]
                    val a = byId[m.awayTeamId]
                    if (h == null || a == null) null else {
                        val assenti = assentiLega[m.round].orEmpty()
                        fun xi(id: Long) = cache[id].orEmpty().filter { it.id !in assenti }.let {
                            if (id == me.id) Lineup.resolve(it, _formation.value).xi else Medical.available(it)
                        }
                        fun bench(id: Long, xi: List<Player>) =
                            if (id == me.id) Medical.available(cache[id].orEmpty().filter { it.id !in assenti }) - xi.toSet() else null
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
                            Staffing.cura(staffBy, h.id), rng,
                            planFor(h.id, me.id), Staffing.skill(staffBy, h.id, Staffing.ALLENATORE))
                            .also { accrueInsurance(h.id, cache[h.id] ?: emptyList(), it) }
                        cache[a.id] = Medical.afterMatch(cache[a.id] ?: emptyList(), r.awayPlayed,
                            Staffing.cura(staffBy, a.id), rng,
                            planFor(a.id, me.id), Staffing.skill(staffBy, a.id, Staffing.ALLENATORE))
                            .also { accrueInsurance(a.id, cache[a.id] ?: emptyList(), it) }
                        accumulaStat(statAcc, s, h.id, a.id, r)
                        goalsSeason += r.events.filter { it.type == EventType.GOAL && it.playerId != 0L }.map {
                            Goal(season = s, league = m.league, round = m.round,
                                teamId = if (it.home) h.id else a.id, playerId = it.playerId)
                        }
                        m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true)
                    }
                }
                dao.updateMatches(sims)
                dao.insertGoals(goalsSeason)
                goalsSeason.clear()
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
                        accumulaStat(statAcc, s, h.id, a.id, r)
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
                        accumulaStat(statAcc, s, h.id, a.id, r)
                        m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true)
                    }
                    if (done.isEmpty()) break
                    dao.updateMatches(done)
                    finalizeKey(sk, s, rng)
                }
            }

            salvaStatistiche(s, statAcc)

            // 1d) Albo d'oro: campioni di ogni divisione, coppa e supercoppa della stagione
            val teamById = teamsNow.associateBy { it.id }
            val alboRows = ArrayList<AlboEntry>()
            val coppeVinte = HashSet<Long>()
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
                    if (label == "Coppa") coppeVinte += w
                }
            }
            dao.deleteAlboSeason(s)
            dao.insertAlbo(alboRows)
            Nazionali.DATE.forEach { giocaDataNazionali(s, it) }
            Nazionali.DATE_U21.forEach { giocaDataU21(s, it) }
            // Ct vacanti: esonerati per risultati sotto le attese, oppure dimissioni (3% a stagione per nazione)
            val vacanti = ArrayList<String>()
            vacanti += tornei(s, prefs.getInt("season_year", 2026))
            nazioniPool().keys.forEach { if (kotlin.random.Random.Default.nextDouble() < 0.03) vacanti += it }

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
                // Offerta dalla nazionale: ct vacante (esonero o dimissioni), oppure a fine stagione con reputazione alta
                val fineStagione = dao.statoInt(StatoKeys.REPUTAZIONE) >= 50 && rng.nextDouble() < 0.5
                if (dao.stato("nazione_allenata").isNullOrEmpty() && (vacanti.isNotEmpty() || fineStagione)) {
                    val nazioni = if (vacanti.isNotEmpty()) vacanti.distinct() else nazioniPool().keys.toList()
                    if (nazioni.isNotEmpty()) {
                        dao.setStato(Stato("offerta_nazionale", nazioni.random(rng)))
                        _notice.value = "Una nazionale ti ha fatto un'offerta: vedi la schermata Nazionali."
                    }
                }
            }

            // 2) Premi, promozioni e retrocessioni (divisioni ordinate per comparsa nella lista)
            val moves = HashMap<Long, String>()
            val prizes = HashMap<Long, Long>()
            val posInfo = HashMap<Long, String>()
            val promoted = HashSet<Long>()
            val relegated = HashSet<Long>()
            val serieC = { k: Pair<String, String> -> k.second.startsWith("Serie C") }
            // Playoff e playout: gare secche, in parità passa la meglio piazzata (Spareggi)
            val forzaPO = strengthOf()
            val spareggiNote = ArrayList<String>()
            val alboPO = ArrayList<AlboEntry>()
            val nomePO = { id: Long -> teamsNow.firstOrNull { it.id == id }?.name ?: "?" }
            leagues.keys.groupBy { it.first }.forEach { (_, keys) ->
                // Catena A → B (Serie C esclusa: ha i suoi gironi)
                val catena = keys.filter { !serieC(it) }.sortedBy { k -> leagues.getValue(k).minOf { it.id } }
                keys.forEach { k ->
                    val st = finals.getValue(k)
                    val size = st.size
                    val tier = if (serieC(k)) 3 else catena.indexOf(k)
                    st.forEachIndexed { pos, row ->
                        val frac = (size - 1 - pos).toDouble() / (size - 1).coerceAtLeast(1)
                        prizes[row.team.id] = Economia.premioStagione(tier, frac)   // premio di piazzamento + diritti TV
                        posInfo[row.team.id] = "${pos + 1}º su $size in ${k.second}"
                    }
                }
                catena.forEachIndexed { tier, k ->
                    if (tier + 1 < catena.size) {
                        val lk = catena[tier + 1]
                        val st = finals.getValue(k)
                        val low = finals.getValue(lk)
                        val kk = minOf(3, st.size / 4, low.size / 4)
                        st.takeLast(kk).forEach { moves[it.team.id] = lk.second; relegated += it.team.id }
                        // Promozioni: le prime kk-1 dirette, l'ultimo posto con i playoff tra le 4 successive
                        val dirette = if (kk >= 2 && low.size >= kk + 3) kk - 1 else kk
                        low.take(dirette).forEach { moves[it.team.id] = k.second; promoted += it.team.id }
                        if (dirette < kk) Spareggi.playoff(low.drop(dirette).take(4).map { it.team.id }, forzaPO, rng)?.let { (w, f) ->
                            moves[w] = k.second; promoted += w
                            spareggiNote += "Playoff ${lk.second}: ${nomePO(w)} batte ${nomePO(f)} in finale e sale in ${k.second}."
                            alboPO += AlboEntry(season = s, nazione = k.first, competizione = "Playoff ${lk.second}", vincitore = nomePO(w))
                        }
                    }
                }
                // Serie B ↔ gironi di Serie C: 4 retrocessioni dalla B (distribuite sui gironi),
                // 4 promozioni in B: la vincitrice di ogni girone più la migliore seconda
                val b = catena.getOrNull(1)
                val gironi = keys.filter { serieC(it) }.sortedBy { it.second }
                if (b != null && gironi.isNotEmpty()) {
                    val stB = finals.getValue(b)
                    val nGiu = minOf(4, stB.size / 4)
                    // Playout: le ultime 3 scendono, la quart'ultima e la quint'ultima si giocano la salvezza
                    val giu = if (nGiu == 4 && stB.size >= 10) {
                        val alto = stB[stB.size - 5].team.id; val basso = stB[stB.size - 4].team.id
                        val perde = Spareggi.playout(alto, basso, forzaPO, rng)
                        spareggiNote += "Playout ${b.second}: retrocede ${nomePO(perde)}, si salva ${nomePO(if (perde == alto) basso else alto)}."
                        stB.takeLast(3) + stB.first { it.team.id == perde }
                    } else stB.takeLast(nGiu)
                    giu.forEachIndexed { i, row ->
                        moves[row.team.id] = gironi[i % gironi.size].second
                        relegated += row.team.id
                    }
                    gironi.forEach { g ->
                        finals.getValue(g).firstOrNull()?.let { w ->
                            moves[w.team.id] = b.second
                            promoted += w.team.id
                        }
                    }
                    // Quarta promozione: playoff tra le seconde dei gironi e la migliore terza
                    val ordina = compareByDescending<it.androcalcio.engine.StandingRow> { it.pts }.thenByDescending { it.gd }
                    val seconde = gironi.mapNotNull { g -> finals.getValue(g).getOrNull(1) }.sortedWith(ordina)
                    val terza = gironi.mapNotNull { g -> finals.getValue(g).getOrNull(2) }.sortedWith(ordina).firstOrNull()
                    val seeds = (seconde + listOfNotNull(terza)).map { it.team.id }
                    Spareggi.playoff(seeds, forzaPO, rng)?.let { (w, f) ->
                        moves[w] = b.second; promoted += w
                        spareggiNote += "Playoff Serie C: ${nomePO(w)} batte ${nomePO(f)} e sale in ${b.second}."
                        alboPO += AlboEntry(season = s, nazione = b.first, competizione = "Playoff Serie C", vincitore = nomePO(w))
                    }
                }
            }
            if (alboPO.isNotEmpty()) dao.insertAlbo(alboPO)
            noteSpareggi = spareggiNote.filter { n -> n.contains(me.name) }
            // Qualificazioni: prima divisione di ogni nazione europea (Champions, Europa, Conference)
            // e di Brasile e Argentina (Copa Sudamericana)
            val europei = HashMap<Long, String>()
            val primeDivisioni = mapOf("Italia" to "Serie A", "Spagna" to "Primera", "Inghilterra" to "Premier",
                "Germania" to "Bundesliga", "Francia" to "Ligue 1", "Brasile" to "Serie A", "Argentina" to "Primera")
            primeDivisioni.forEach { (nazione, camp) ->
                val st = finals[nazione to camp] ?: return@forEach
                val ids = st.map { it.team.id }
                val posti = Europa.POSTI[nazione]
                if (posti != null) {
                    val vincitriceCoppa = Cup.finalMatch(dao.matchesOnce(Cup.key(nazione), s))
                        ?.let { Cup.winnerOfTie(listOf(it), strength) }
                    europei.putAll(Europa.assegna(ids, vincitriceCoppa, posti))
                } else {
                    europei.putAll(Sudamericana.assegna(ids))
                }
            }
            // Classifiche di fine stagione della tua nazione, con l'esito di ogni squadra
            _fine.value = FineStagione(
                stagione = s,
                divisioni = leagues.keys.filter { it.first == me.nazione }
                    .sortedBy { k -> leagues.getValue(k).minOf { it.id } }
                    .map { k ->
                        DivisioneFine(k.second, finals.getValue(k).mapIndexed { i, row ->
                            val id = row.team.id
                            val esito = when {
                                id in promoted -> "Promossa in ${moves[id]}"
                                id in relegated -> "Retrocessa in ${moves[id]}"
                                else -> europei[id].orEmpty()
                            }
                            RigaFine(i + 1, row.team.name, row.pts, esito, id == me.id)
                        })
                    }
            )
            // Fama degli allenatori delle altre squadre: il piazzamento li fa salire o scendere
            run {
                val voci = Allenatori.decode(dao.stato(Allenatori.KEY)).toMutableMap()
                for ((_, st) in finals) {
                    st.forEachIndexed { pos, row ->
                        val id = row.team.id
                        if (id == me.id) return@forEachIndexed
                        val nome = staffBy[id]?.firstOrNull { it.role == Staffing.ALLENATORE }?.name ?: return@forEachIndexed
                        val fama = voci[id]?.fama ?: Allenatori.FAMA_INIZIALE
                        val d = Allenatori.variazioneStagione(pos + 1, st.size, id in promoted, id in relegated)
                        voci[id] = Allenatori.Voce(id, nome, Allenatori.clamp(fama + d))
                    }
                }
                dao.setStato(Stato(Allenatori.KEY, Allenatori.encode(voci.values)))
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
            val effMig = Migliorie.effetti(Migliorie.decodeFatte(dao.stato("migliorie_fatte")))
            val upkeep = Strutture.manutenzione(livAll) + Strutture.manutenzione(dao.statoInt(Strutture.VIVAIO))
            var extraIncasso = 0L
            var extraPrezzi = 0L; var obBonus = 0L; var obFid = 0; var obPresente = false; var obRaggiunto = false
            var costoPsico = 0L; var costoMkt = 0L; var difAdj = 0L; var extraTotale = 0L
            val livDif = _opzioni.value.difficolta
            val profDif = profiloPersonalizzato()
            // Clausole di rendimento per tutti i giocatori (anche IA): a fine stagione il club paga i bonus raggiunti
            val campioni = finals.values.mapNotNull { it.firstOrNull()?.team?.id }.toSet()
            val statAll = dao.statsOf(s).associateBy { it.playerId }
            val clausoleGia = dao.clausoleOnce().associateBy { it.playerId }
            val clauseNuove = ArrayList<Clausola>()
            val pagaSquadra = HashMap<Long, Long>()
            for (t in teamsNow) {
                for (p in byTeam[t.id].orEmpty()) {
                    val base = maxOf(p.salary, 100_000L)
                    val c = (clausoleGia[p.id] ?: Clausola(p.id, t.id, base / 2, base / 2, base, base / 2)).copy(teamId = t.id)
                    clauseNuove += c
                    val st = statAll[p.id]
                    var pay = 0L
                    if ((st?.presenze ?: 0) >= 30) pay += c.bonusPresenze
                    if ((st?.gol ?: 0) >= 20) pay += c.bonusGol
                    if (t.id in campioni) pay += c.bonusCampionato
                    if (t.id in coppeVinte) pay += c.bonusCoppa
                    if (pay > 0) pagaSquadra[t.id] = (pagaSquadra[t.id] ?: 0L) + pay
                }
            }
            dao.upsertClausole(clauseNuove)
            val clausePay = pagaSquadra[me.id] ?: 0L
            val newTeams = teamsNow.map { t ->
                val keep = ArrayList<Player>()
                (byTeam[t.id] ?: emptyList()).forEach { p ->
                    val a = Progression.advance(p, rng, Staffing.skill(staffBy, t.id, Staffing.ALLENATORE),
                        if (t.id == me.id) Strutture.bonusAllenamento(livAll) + effMig.crescitaPct else 0,
                        mentore = Mentori.bonus(p, byTeam[t.id] ?: emptyList()))
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
                // Solo le squadre della stessa nazione: "Serie A" e "Primera" esistono in più paesi
                val lg = teamsNow.filter { it.nazione == t.nazione && it.campionato == t.campionato }.associate { x ->
                    x.id to cache[x.id].orEmpty().map { it.overall }.sortedDescending().take(11).average() }
                val rel = Economia.forzaRelativa(lg[t.id] ?: 0.0, lg.values)
                val gate = Economia.incassoStadio(t.stadiumCapacity, t.ticketPrice, rel, lg.size)
                val net = gate - wages   // monte ingaggi intero (scala reale)
                val newLeague = moves[t.id]
                if (t.id == me.id) {
                    meNet = net
                    extraIncasso = gate * effMig.incassoPct / 100
                    val posFin = finals[t.nazione to t.campionato]?.indexOfFirst { it.team.id == t.id }?.let { it + 1 } ?: 0
                    extraPrezzi = (gate * (Abbonamenti.fattore(t.stadiumCapacity, t.ticketPrice, rel, dao.statoInt(StatoKeys.TIFOSI),
                        Abbonamenti.decode(dao.stato("prezzi")), Economia.partiteInCasa(lg.size)) - 1.0)).toLong()
                    val ob = Obiettivi.decode(dao.stato("obiettivo"))
                    if (ob != null && posFin > 0) {
                        obPresente = true
                        obRaggiunto = Obiettivi.raggiunto(ob, posFin)
                        obBonus = if (obRaggiunto) ob.bonus else 0L
                        obFid = Obiettivi.fiducia(ob, posFin)
                    }
                    costoPsico = Relazioni.costoPsicologo(dao.statoInt("psicologo"))
                    costoMkt = Sponsors.costoMarketing(dao.statoInt("marketing"))
                    difAdj = Difficolta.incassi(gate + meSp + meMc, livDif, profDif) - (gate + meSp + meMc)
                    extraTotale = extraIncasso + effMig.merchAnno - effMig.manutenzione + extraPrezzi + obBonus - costoPsico - costoMkt + difAdj
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
                        (if (t.id == me.id) meSp + meMc - insPremium + insPending - coach - upkeep + extraTotale else 0L) - (pagaSquadra[t.id] ?: 0L)).coerceAtLeast(0),
                    campionato = newLeague ?: t.campionato
                )
            }
            dao.updatePlayers(updated)
            dao.deletePlayers(removed)
            val idRimasti = updated.mapTo(HashSet()) { it.id }
            dao.deleteClausole(dao.clausoleOnce().filter { c -> c.playerId !in idRimasti })
            dao.insertPlayers(added)
            // Talenti precoci: tre a stagione, in club a caso; i nomi restano da scoprire
            val talenti = it.androcalcio.engine.Talenti.genera(teamsNow, rng, 3)
            dao.insertPlayers(talenti)
            dao.updateTeams(newTeams)
            newTeams.find { it.id == me.id }?.let { nt ->
                val pr = prizes[me.id] ?: 0L
                val lines = buildList {
                    add("Bilancio di fine stagione (incassi, ingaggi)" to meNet)
                    if (pr != 0L) add("Premio piazzamento" to pr)
                    if (meSp > 0) add("Sponsor (${deals.size} contratti)" to meSp)
                    add("Ingaggio allenatore" to -coach)
                    if (clausePay > 0) add("Clausole giocatori" to -clausePay)
                    if (upkeep > 0) add("Manutenzione strutture" to -upkeep)
                    add("Merchandising" to meMc)
                    if (extraIncasso > 0) add("Migliorie stadio: incassi extra" to extraIncasso)
                    if (effMig.merchAnno > 0) add("Migliorie: store, museo e servizi" to effMig.merchAnno)
                    if (effMig.manutenzione != 0L) add("Manutenzione migliorie" to -effMig.manutenzione)
                    if (extraPrezzi != 0L) add("Prezzi per settore: effetto sugli incassi" to extraPrezzi)
                    if (obBonus > 0L) add("Obiettivo dirigenza raggiunto: bonus" to obBonus)
                    if (costoPsico > 0L) add("Psicologo" to -costoPsico)
                    if (costoMkt > 0L) add("Addetto marketing" to -costoMkt)
                    if (difAdj != 0L) add("Difficoltà: effetto sugli incassi" to difAdj)
                    if (insPremium > 0) add("Premio assicurazione" to -insPremium)
                    if (insPending > 0) add("Rimborso infortuni" to insPending)
                }
                var saldo = nt.budget - lines.sumOf { it.second }
                lines.forEach { (label, importo) ->
                    saldo += importo
                    registra(me.id, label, importo, saldo)
                }
            }

            // Obiettivo della dirigenza, contatori di carriera, premi individuali e trofei
            if (obPresente) {
                dao.setStatoInt(StatoKeys.FIDUCIA, dao.statoInt(StatoKeys.FIDUCIA) + Difficolta.fiducia(obFid, livDif, profDif))
                if (obRaggiunto) inc("c_obiettivi")
            }
            inc("c_stagioni")
            if (me.id in campioni) inc("c_titoli")
            if (me.id in coppeVinte) inc("c_coppe")
            if (me.id in promoted) inc("c_prom")
            run {
                val legaIds = teamsNow.filter { it.nazione == me.nazione && it.campionato == me.campionato }.map { it.id }.toSet()
                val giocatori = cache.values.flatten().associateBy { it.id }
                val premi = Premi.assegna(statAll.values.filter { it.teamId in legaIds }, giocatori) +
                    (finals[me.nazione to me.campionato]?.firstOrNull()?.let { Premi.Premio("Allenatore dell'anno", 0L, "Il tecnico di ${it.team.name}") }
                        ?.let { listOf(it) } ?: emptyList())
                dao.setStato(Stato("premi", Premi.encode(premi)))
            }
            dao.setStato(Stato("obiettivo_s", ""))
            ripristinaAllStar()
            aggiornaTrofei()

            // Staff: un anno di contratto in meno; chi arriva a zero lascia la squadra
            val staffNext = staffAll.map { it.copy(contract = it.contract - 1) }
            dao.updateStaffs(staffNext.filter { it.contract > 0 })
            dao.deleteStaffs(staffNext.filter { it.contract <= 0 })
            dao.deleteMissions(dao.missionsOnce().filter { m -> staffNext.none { it.id == m.staffId && it.contract > 0 } })
            staffNext.filter { it.contract <= 0 && it.teamId == me.id }.forEach {
                leavers += "${it.name} (${Staffing.roleName(it.role).lowercase()}, contratto scaduto)"
            }
            // Il club torna senza figura: vivaio e scout riprendono l'abilità "vuoto"
            staffNext.filter { it.contract <= 0 }.forEach { syncTeamSkill(it.teamId, it.role, Staffing.VUOTO) }
            val noteVivaio = cresciGiovani(me)
            dao.clearGoals()
            val y = prefs.getInt("season_year", 2026) + 1
            prefs.edit().putInt("season_year", y).apply()
            _season.value = seasonName(y)
            val numeri = assegnaNumeri(me.id)

            _notice.value = null
            chiudiStagioneSocieta(me.id, s)
            val noteSocieta = _notice.value.orEmpty()
            refreshSponsorOffers(selectedTeam.value?.id ?: me.id)
            refreshClub()
            aggiornaClassifica()
            _notice.value = "Nuova stagione ${seasonName(y)}. $report $numeri Sono emersi ${talenti.size} talenti precoci nei club." +
                (if (noteVivaio.isNotEmpty()) " $noteVivaio" else "") +
                (if (expired.isNotEmpty()) " Sponsor scaduti: ${expired.joinToString(", ")}." else "") +
                (if (leavers.isNotEmpty()) " Lasciano: ${leavers.joinToString(", ")}." else "") +
                (if (noteSocieta.isNotEmpty()) " $noteSocieta" else "")
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
            if (booked.size >= Friendlies.COUNT) { _notice.value = "Hai già fissato tutte le amichevoli."; return@launch }
            if (booked.any { it.homeTeamId == opponentId || it.awayTeamId == opponentId }) {
                _notice.value = "Squadra già impegnata in un'amichevole."; return@launch
            }
            val opp = dao.allTeams().firstOrNull { it.id == opponentId } ?: return@launch
            val strength = strengthOf()
            if (!AllStar.isAllStar(opp) && !Friendlies.accepts(strength[me.id] ?: 0.0, strength[opp.id] ?: 0.0, kotlin.random.Random.Default)) {
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
            ripristinaAllStar()
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

    /** Nome e cognome dell'allenatore (tu), scelti all'avvio della carriera. */
    private val _allenatore = MutableStateFlow(
        prefs.getString("coach_nome", null)?.takeIf { it.isNotBlank() }?.let { n ->
            "$n ${prefs.getString("coach_cognome", "").orEmpty()}".trim()
        })
    val allenatore: StateFlow<String?> = _allenatore

    /** Classifica degli allenatori: tu (fama = reputazione) e gli allenatori delle altre squadre. */
    private val _classificaAllenatori = MutableStateFlow<List<Allenatori.Riga>>(emptyList())
    val classificaAllenatori: StateFlow<List<Allenatori.Riga>> = _classificaAllenatori

    private fun avvio4() { viewModelScope.launch(Dispatchers.IO) { aggiornaClassifica() } }

    private suspend fun aggiornaClassifica() {
        val squadre = dao.allTeams().associateBy { it.id }
        val me = selectedTeam.value?.id
        val voci = Allenatori.decode(dao.stato(Allenatori.KEY))
        val righe = ArrayList<Allenatori.Riga>()
        for (staff in dao.allStaff()) {
            if (staff.role != Staffing.ALLENATORE || staff.teamId == 0L || staff.teamId == me) continue
            val squadra = squadre[staff.teamId]?.name ?: continue
            righe += Allenatori.Riga(staff.name, squadra, voci[staff.teamId]?.fama ?: Allenatori.FAMA_INIZIALE, false)
        }
        val nome = _allenatore.value
        if (nome != null && me != null) {
            righe += Allenatori.Riga(nome, squadre[me]?.name.orEmpty(), dao.statoInt(StatoKeys.REPUTAZIONE), true)
        }
        _classificaAllenatori.value = righe.sortedByDescending { it.fama }
    }

    /** Messaggio di benvenuto del presidente, mostrato una volta all'inizio della carriera. */
    private val _presidente = MutableStateFlow<String?>(null)
    val presidente: StateFlow<String?> = _presidente
    fun chiudiPresidente() { _presidente.value = null }

    /** Classifiche e esiti dell'ultima fine stagione (null finché non si chiude una stagione). */
    private val _fine = MutableStateFlow<FineStagione?>(null)
    val fine: StateFlow<FineStagione?> = _fine
    fun chiudiFine() { _fine.value = null }

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
        _mutui.value = Mutui.decode(dao.stato("mutui"))
        _debitoRate.value = dao.stato("rate_mercato")?.toLongOrNull() ?: 0L
        val year = prefs.getInt("season_year", 2026).toString()
        _club.value = ClubState(
            dao.statoInt(StatoKeys.FIDUCIA), dao.statoInt(StatoKeys.TIFOSI),
            dao.statoInt(StatoKeys.REPUTAZIONE), dao.statoInt(StatoKeys.CLIMA),
            ClubRequests.TIPI.filter { dao.stato(StatoKeys.RICHIESTA + it) == year }.toSet(),
            dao.stato("esonero") == "1", ClubOffer.decode(dao.stato("offerte")),
            SponsorDeal.decode(dao.stato("sponsor")), SponsorDeal.decode(dao.stato("sponsor_offerte")),
            coachSalary(), dao.statoInt(Strutture.ALLENAMENTO), dao.statoInt(Strutture.VIVAIO))
    }

    private fun avvio5() {
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
                tipo == "ingaggi" -> {
                    // Il presidente alza il tetto del monte ingaggi: non sono soldi in cassa
                    dao.setStato(Stato(StatoKeys.RICHIESTA + tipo, year))
                    dao.updateTeam(dao.team(me.id).let { it.copy(wageBudget = it.wageBudget + importo) })
                    _notice.value = "Accettata: monte ingaggi +${money(importo)} l'anno."
                }
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
            Sponsors.offers(active, myStrength(teamId), fama, kotlin.random.Random.Default, dao.statoInt("marketing")))))
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
        concludiApparizioni()
        val gf = if (m.homeTeamId == me) m.homeGoals else m.awayGoals
        val gs = if (m.homeTeamId == me) m.awayGoals else m.homeGoals
        // La fama dell'allenatore cresce con i risultati
        dao.setStatoInt(StatoKeys.REPUTAZIONE, Allenatori.clamp(dao.statoInt(StatoKeys.REPUTAZIONE) + Allenatori.dallaPartita(gf, gs)))
        // La fiducia della dirigenza segue ogni risultato
        dao.setStatoInt(StatoKeys.FIDUCIA, Allenatori.clamp(dao.statoInt(StatoKeys.FIDUCIA) +
            Difficolta.fiducia(Allenatori.fiduciaPartita(gf, gs), _opzioni.value.difficolta, profiloPersonalizzato())))
        aggiornaClassifica()
        val (dc, dt0) = Spogliatoio.dopoPartita(gf, gs)
        // Derby: la piazza si accende. Vittoria/sconfitta pesano di più su tifosi e dirigenza.
        val rivale = dao.team(if (m.homeTeamId == me) m.awayTeamId else m.homeTeamId).name
        val derby = Rivalita.isDerby(dao.team(me).name, rivale)
        val dt = if (derby) dt0 * 2 else dt0
        if (derby) {
            val dFid = when { gf > gs -> 2; gf < gs -> -2; else -> 0 }
            dao.setStatoInt(StatoKeys.FIDUCIA, dao.statoInt(StatoKeys.FIDUCIA) + dFid)
            _notice.value = when { gf > gs -> "Derby vinto contro $rivale: tifosi in festa."
                gf < gs -> "Derby perso contro $rivale: la piazza è furiosa."
                else -> "Derby contro $rivale finito in parità." }
        }
        val clima = (dao.statoInt(StatoKeys.CLIMA) + dc).coerceIn(0, 100)
        dao.setStatoInt(StatoKeys.CLIMA, clima)
        dao.setStatoInt(StatoKeys.TIFOSI, dao.statoInt(StatoKeys.TIFOSI) + dt)
        val spinta = Spogliatoio.spintaMorale(clima)
        val psico = dao.statoInt("psicologo")
        val rosa = dao.playersOnce(me)
        if (spinta != 0 || psico > 0) dao.updatePlayers(rosa.map {
            it.copy(morale = (it.morale + spinta + Relazioni.recuperoPsicologo(psico, it)).coerceIn(0, 100)) })
        personalitaDopoPartita(me, gf, gs)
        if (evento && _evento.value == null) {
            val prep = dao.allStaff().any { it.teamId == me && it.role == Staffing.PREPARATORE }
            _evento.value = Spogliatoio.evento(rosa, clima, dao.statoInt(StatoKeys.FIDUCIA),
                dao.statoInt(StatoKeys.TIFOSI), gf < gs, prep, kotlin.random.Random.Default)
        }
        refreshClub()
    }

    // ---- Testo e tutorial ----
    private val _testoPct = MutableStateFlow(prefs.getInt("testo_pct", 100))
    val testoPct: StateFlow<Int> = _testoPct
    fun setTesto(p: Int) { prefs.edit().putInt("testo_pct", p).apply(); _testoPct.value = p }
    private val _tutorial = MutableStateFlow(!prefs.getBoolean("tutorial_visto", false))
    val tutorial: StateFlow<Boolean> = _tutorial
    fun chiudiTutorial() { prefs.edit().putBoolean("tutorial_visto", true).apply(); _tutorial.value = false }
    fun apriTutorial() { _tutorial.value = true }
    /** Risparmio batteria: la partita dal vivo avanza al massimo 8 volte al secondo (meno CPU e schermo che si ridisegna). */
    private val _risparmio = MutableStateFlow(prefs.getBoolean("risparmio", false))
    val risparmio: StateFlow<Boolean> = _risparmio
    fun setRisparmio(b: Boolean) { prefs.edit().putBoolean("risparmio", b).apply(); _risparmio.value = b }

    // ---- Società: disciplina, personalità, scambi, fair play, storia, autosalvataggio ----
    data class SocietaStato(
        val gialli: Map<Long, Int> = emptyMap(), val squal: Map<Long, Int> = emptyMap(),
        val arbitro: Disciplina.Arbitro? = null,
        val richieste: List<Personalita.Richiesta> = emptyList(),
        val ffp: FairPlay.Esito? = null, val ffpBlocco: Boolean = false,
        val scontri: Map<String, Storia.Scontro> = emptyMap(), val record: Storia.Record = Storia.Record(),
        val hall: List<Storia.Bandiera> = emptyList(),
        val partite: List<Analisi.Partita> = emptyList(), val stat: List<PlayerStat> = emptyList(),
        val mutui: List<Mutui.Mutuo> = emptyList(),
        val prossima: String? = null, val precedenti: Storia.Scontro? = null,
        val netto: Long = 0L, val rimanenti: Int = 0, val debitoRate: Long = 0L,
        val voci: List<String> = emptyList(), val seguiti: List<Player> = emptyList(),
        val andamento: List<Int> = emptyList(), val squadreLega: Int = 0,
        val minuti: Map<Long, Int> = emptyMap(), val minutiPartite: Int = 0
    )
    private val _societa = MutableStateFlow(SocietaStato())
    val societa: StateFlow<SocietaStato> = _societa

    fun aggiornaSocieta() { viewModelScope.launch(Dispatchers.IO) { caricaSocieta() } }

    private suspend fun caricaSocieta() {
        val me = selectedTeam.value ?: return
        val t = dao.team(me.id)
        val ops = FairPlay.decode(dao.stato("ffp_perdite"))
        val tutte = dao.matchesOnce(key(t), _season.value).filter { !Cup.isCup(it.round) }
        val prossimaPartita = tutte.filter { !it.played && (it.homeTeamId == t.id || it.awayTeamId == t.id) }.minByOrNull { it.round }
        val nomeProssima = prossimaPartita?.let { m -> dao.team(if (m.homeTeamId == t.id) m.awayTeamId else m.homeTeamId).name }
        val scontri = Storia.decodeScontri(dao.stato("scontri"))
        val netto = dao.movimentiStagioneOnce(t.id, _season.value)
            .filter { Bilancio.categoria(it.label) !in setOf("Mercato", "Mutui") }.sumOf { it.importo }
        val idSeguiti = decodeIds(dao.stato("preferiti")).toSet()
        _societa.value = SocietaStato(
            gialli = Disciplina.decode(dao.stato("gialli")), squal = Disciplina.decode(dao.stato("squal")),
            arbitro = Disciplina.arbitro(dao.statoInt("c_partite") + 1, _season.value.hashCode().toLong()),
            richieste = Personalita.decode(dao.stato("richieste")),
            ffp = if (ops.isEmpty()) null else FairPlay.valuta(ops.map { -it.second }, t.budget),
            ffpBlocco = dao.stato("ffp_blocco") == _season.value,
            scontri = Storia.decodeScontri(dao.stato("scontri")), record = Storia.decodeRecord(dao.stato("storia_rec")),
            hall = Storia.decodeBandiere(dao.stato("storia_hall")),
            partite = Analisi.partiteMie(dao.matchesOnce(key(t), _season.value).filter { !Cup.isCup(it.round) }, t.id),
            stat = dao.statsOf(_season.value).filter { it.teamId == t.id },
            mutui = Mutui.decode(dao.stato("mutui")),
            prossima = nomeProssima, precedenti = nomeProssima?.let { scontri[it] },
            netto = netto, rimanenti = tutte.count { !it.played }, debitoRate = dao.stato("rate_mercato")?.toLongOrNull() ?: 0L,
            voci = dao.stato("voci").orEmpty().split("|").filter { it.isNotBlank() },
            seguiti = if (idSeguiti.isEmpty()) emptyList() else dao.allPlayers().filter { it.id in idSeguiti },
            andamento = dao.allTeams().filter { it.nazione == t.nazione && it.campionato == t.campionato }.let { lega ->
                it.androcalcio.engine.Andamento.posizioni(t.id, lega, dao.matchesOnce(key(t), _season.value).filter { !Cup.isCup(it.round) })
            },
            squadreLega = dao.allTeams().count { it.nazione == t.nazione && it.campionato == t.campionato },
            minuti = Minutaggio.decode(dao.stato(Minutaggio.KEY)), minutiPartite = dao.statoInt(Minutaggio.KEY_PARTITE)
        )
    }

    private fun decodeIds(s: String?): List<Long> = s.orEmpty().split(";").mapNotNull { it.toLongOrNull() }

    /** Preferiti: giocatori da seguire sul mercato. */
    fun togglePreferito(p: Player) {
        viewModelScope.launch(Dispatchers.IO) {
            val l = decodeIds(dao.stato("preferiti")).toMutableList()
            if (!l.remove(p.id)) l.add(p.id)
            dao.setStato(Stato("preferiti", l.joinToString(";")))
            caricaSocieta()
        }
    }
    val preferiti: StateFlow<Set<Long>> = osserva("stato") { decodeIds(dao.stato("preferiti")).toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** Capitano scelto (0 = automatico). */
    val capitanoScelto: StateFlow<Long> = osserva("stato") { dao.stato("capitano")?.toLongOrNull() ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    fun scegliCapitano(p: Player?) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = p?.id ?: 0L
            dao.setStato(Stato("capitano", if (id == 0L) "" else id.toString()))
            Leadership.scelto = id
            if (p != null) _notice.value = "${p.name} è il nuovo capitano."
        }
    }

    /** Vicecapitano (0 = nessuno). */
    val viceScelto: StateFlow<Long> = osserva("stato") { dao.stato("vicecapitano")?.toLongOrNull() ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    fun scegliVice(p: Player?) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = p?.id ?: 0L
            dao.setStato(Stato("vicecapitano", if (id == 0L) "" else id.toString()))
            Leadership.vice = id
            if (p != null) _notice.value = "${p.name} è il vicecapitano."
        }
    }

    /** Formazioni salvate: 3 posti con titolari, modulo e tattica. */
    /** Media forma per giocatore della squadra selezionata (solo chi ha giocato). */
    val formeRosa: StateFlow<Map<Long, Double>> = osserva("stato") {
        val tid = selectedTeam.value?.id ?: return@osserva emptyMap<Long, Double>()
        dao.playersOnce(tid).mapNotNull { p ->
            Forma.media(Forma.leggi(dao.stato(Forma.key(p.id))))?.let { p.id to it }
        }.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val formazioniSalvate: StateFlow<List<FormazioniSalvate.Preset?>> = osserva("stato") { FormazioniSalvate.decode(dao.stato(FormazioniSalvate.KEY)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(FormazioniSalvate.POSTI) { null })
    fun salvaFormazione(posto: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val xi = players.value.filter { it.starter }.map { it.id }
            val l = FormazioniSalvate.decode(dao.stato(FormazioniSalvate.KEY)).toMutableList()
            l[posto] = FormazioniSalvate.Preset("${_formation.value.label} · ${_mentality.value.label}", _formation.value.ordinal,
                _mentality.value.ordinal, _pressing.value.ordinal, _line.value.ordinal, xi)
            dao.setStato(Stato(FormazioniSalvate.KEY, FormazioniSalvate.encode(l)))
            _notice.value = "Formazione salvata nel posto ${posto + 1}."
        }
    }
    fun caricaFormazione(posto: Int) {
        viewModelScope.launch {
            val me = selectedTeam.value ?: return@launch
            val p = FormazioniSalvate.decode(dao.stato(FormazioniSalvate.KEY)).getOrNull(posto) ?: return@launch
            Formation.values().getOrNull(p.modulo)?.let { setFormation(it) }
            Mentality.values().getOrNull(p.mentalita)?.let { setMentality(it) }
            Pressing.values().getOrNull(p.pressing)?.let { setPressing(it) }
            DefLine.values().getOrNull(p.linea)?.let { setLine(it) }
            val ps = dao.playersOnce(me.id)
            val xi = p.titolari.filter { id -> ps.any { it.id == id } }.toSet()
            dao.updatePlayers(ps.map { it.copy(starter = it.id in xi) })
            _notice.value = if (xi.size < p.titolari.size) "Formazione caricata: ${p.titolari.size - xi.size} titolari non sono più in rosa."
                else "Formazione ${posto + 1} caricata."
        }
    }

    /** Allenamento individuale: un giocatore segue un focus diverso dalla squadra (null = come la squadra). */
    val allenamentoInd: StateFlow<Map<Long, Int>> = osserva("stato") { Training.decodeInd(dao.stato("allen_ind")).mapValues { it.value.ordinal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    fun setAllenamentoInd(p: Player, focus: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            val m = Training.decodeInd(dao.stato("allen_ind")).toMutableMap()
            if (focus == null) m.remove(p.id) else m[p.id] = TrainFocus.values()[focus]
            Training.individuali = m
            dao.setStato(Stato("allen_ind", Training.encodeInd(m)))
        }
    }

    /** All'avvio rimette in funzione capitano e allenamenti individuali salvati. */
    private fun ripristinaScelte() {
        viewModelScope.launch(Dispatchers.IO) {
            Leadership.scelto = dao.stato("capitano")?.toLongOrNull() ?: 0L
            Leadership.vice = dao.stato("vicecapitano")?.toLongOrNull() ?: 0L
            _premio.value = dao.stato(PremioPartita.KEY) == "1"
            Training.individuali = Training.decodeInd(dao.stato("allen_ind"))
        }
    }
    private fun avvio6() { ripristinaScelte() }

    /** Riposo automatico: i titolari stanchi cedono il posto a un sostituto riposato dello stesso ruolo. */
    fun riposoAutomatico() {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val rosa = dao.playersOnce(me.id)
            val squal = Disciplina.decode(dao.stato("squal")).keys
            val coppie = Analisi.riposo(rosa, squal)
            if (coppie.isEmpty()) { _notice.value = "Nessun cambio utile: nessun titolare stanco ha un sostituto riposato."; return@launch }
            coppie.forEach { (esce, entra) ->
                dao.updatePlayer(esce.copy(starter = false))
                dao.updatePlayer(entra.copy(starter = true))
            }
            _notice.value = "Riposo: " + coppie.joinToString(", ") { "${it.first.name} → ${it.second.name}" }
            caricaSocieta()
        }
    }

    /** Voce di mercato: a volte un club segue uno dei tuoi migliori giocatori. Ne restano al massimo sei. */
    private suspend fun generaVoce(rng: kotlin.random.Random) {
        if (rng.nextDouble() > 0.35) return
        val me = selectedTeam.value ?: return
        val mio = dao.playersOnce(me.id).sortedByDescending { it.overall }.take(8).ifEmpty { return }.random(rng)
        val club = dao.allTeams().filter { it.nazione == me.nazione && it.id != me.id && !AllStar.isAllStar(it) }.ifEmpty { return }.random(rng)
        val cifra = (Market.value(mio) * (0.9 + rng.nextDouble() * 0.5)).toLong()
        val g = dao.statoInt(MK.GIORNATA)
        val testo = "Giornata $g: il ${club.name} segue ${mio.name}. Si parla di ${money(cifra)}."
        val l = listOf(testo) + dao.stato("voci").orEmpty().split("|").filter { it.isNotBlank() }
        dao.setStato(Stato("voci", l.take(6).joinToString("|")))
    }

    // ---- Backup su file ----
    fun esportaSlot(n: Int, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = try { ctx.contentResolver.openOutputStream(uri)?.use { SaveSlots.esporta(ctx, n, it) } ?: false } catch (e: Exception) { false }
            _notice.value = if (ok) "Slot $n esportato nel file." else "Esportazione non riuscita (slot vuoto o file non scrivibile)."
        }
    }

    /** Riepilogo della stagione in CSV: classifica, rosa, bilancio. */
    fun esportaCsv(uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val t = selectedTeam.value ?: return@launch
            val ok = try {
                val lega = dao.allTeams().filter { it.nazione == t.nazione && it.campionato == t.campionato }
                val cl = it.androcalcio.engine.standings(lega, dao.matchesOnce(key(t), _season.value).filter { !Cup.isCup(it.round) })
                val st = dao.statsOf(_season.value).associateBy { it.playerId }
                val testo = it.androcalcio.engine.EsportaCsv.riepilogo(_season.value, t.name, cl, dao.playersOnce(t.id),
                    st.mapValues { it.value.gol }, st.mapValues { it.value.presenze },
                    Bilancio.riepilogo(dao.movimentiStagioneOnce(t.id, _season.value)))
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(("\uFEFF" + testo).toByteArray(Charsets.UTF_8)) } != null
            } catch (e: Exception) { false }
            _notice.value = if (ok) "Riepilogo esportato in CSV." else "Esportazione CSV non riuscita."
        }
    }

    fun importaSlot(n: Int, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = try { ctx.contentResolver.openInputStream(uri)?.use { SaveSlots.importa(ctx, n, it, db.openHelper.readableDatabase.version) } ?: false } catch (e: Exception) { false }
            _slots.value = SaveSlots.list(ctx)
            _notice.value = if (ok) "File importato nello slot $n. Premi Carica per giocarlo." else "File non valido o di un'altra versione dell'app: non posso importarlo."
        }
    }

    /** Dopo ogni tua partita: scontri diretti e cartellini (diffide, squalifiche, rossi). */
    private suspend fun societaDopoPartita(me: Team, opp: Team, gf: Int, gs: Int, r: MatchReport, live: Boolean) {
        dao.setStato(Stato("scontri", Storia.encodeScontri(Storia.aggiornaScontro(Storia.decodeScontri(dao.stato("scontri")), opp.name, gf, gs))))
        if (gf > gs) {
            val premio = Sponsors.premioVittoria(SponsorDeal.decode(dao.stato("sponsor")).sumOf { it.annual }, gs == 0)
            val tt = dao.team(me.id)
            dao.updateTeam(tt.copy(budget = tt.budget + premio))
            registra(tt.id, "Sponsor: premio vittoria", premio, tt.budget + premio)
        }
        val rng = kotlin.random.Random.Default
        val inCampo = r.playedPlayers.filter { it.teamId == me.id }
        if (inCampo.isEmpty()) return
        val arb = Disciplina.arbitro(dao.statoInt("c_partite"), _season.value.hashCode().toLong())
        val prima = Disciplina.Registro(Disciplina.decode(dao.stato("gialli")), Disciplina.decode(dao.stato("squal")))
        val ids = inCampo.map { it.id }.toSet()
        // Cartellini veri della partita: la doppia ammonizione vale come rosso e non conta come due gialli
        val esito = Disciplina.Esito(
            r.events.filter { it.type == EventType.YELLOW && it.playerId in ids && it.playerId !in r.espulsi }.map { it.playerId },
            r.espulsi.filter { it in ids })
        val dopo = Disciplina.applica(prima, prima.squal.keys, esito, rng)
        dao.setStato(Stato("gialli", Disciplina.encode(dopo.gialli)))
        dao.setStato(Stato("squal", Disciplina.encode(dopo.squal)))
        val nome = (dao.playersOnce(me.id).associateBy { it.id })
        val note = ArrayList<String>()
        esito.rossi.forEach { id -> nome[id]?.let { note += "Espulso ${it.name}: ${dopo.squal[id] ?: 1} giornate di squalifica." } }
        esito.gialli.filter { dopo.squal[it] != null && (prima.squal[it] ?: 0) < (dopo.squal[it] ?: 0) && it !in esito.rossi }
            .forEach { id -> nome[id]?.let { note += "${it.name} squalificato per somma di ammonizioni." } }
        Disciplina.diffidati(dopo).filter { (prima.gialli[it] ?: 0) < Disciplina.DIFFIDA }
            .forEach { id -> nome[id]?.let { note += "${it.name} è diffidato." } }
        if (note.isNotEmpty()) avviso(note.joinToString(" "))
    }

    /** Dopo la partita: la personalità cambia il morale; i mercenari chiedono aumenti, gli ambiziosi la cessione. */
    private suspend fun personalitaDopoPartita(meId: Long, gf: Int, gs: Int) {
        val rosa = dao.playersOnce(meId)
        if (rosa.isEmpty()) return
        val nessunTitolare = rosa.none { it.starter }
        dao.updatePlayers(rosa.map { p ->
            val giocato = nessunTitolare || p.starter
            p.copy(morale = (p.morale + Personalita.moraleDopo(p, gf, gs, giocato)).coerceIn(0, 100))
        })
        val esistenti = Personalita.decode(dao.stato("richieste")).filter { rq -> rosa.any { it.id == rq.playerId } }
        val nuove = Personalita.nuove(dao.playersOnce(meId), esistenti, kotlin.random.Random.Default)
        dao.setStato(Stato("richieste", Personalita.encode(esistenti + nuove)))
        if (nuove.isNotEmpty()) {
            val nomi = rosa.associateBy { it.id }
            avviso(nuove.joinToString(" ") { rq ->
                val n = nomi[rq.playerId]?.name ?: "Un giocatore"
                if (rq.tipo == Personalita.Richiesta.AUMENTO) "$n chiede un aumento (vedi Società)." else "$n chiede di essere ceduto (vedi Società)."
            })
        }
    }

    fun rispondiRichiesta(rq: Personalita.Richiesta, accetta: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val resto = Personalita.decode(dao.stato("richieste")).filter { it.playerId != rq.playerId }
            dao.setStato(Stato("richieste", Personalita.encode(resto)))
            val p = dao.playerById(rq.playerId)
            if (p == null || p.teamId != me.id) { caricaSocieta(); return@launch }
            when {
                rq.tipo == Personalita.Richiesta.AUMENTO && accetta -> {
                    dao.updatePlayer(p.copy(salary = rq.importo, morale = (p.morale + 12).coerceAtMost(100)))
                    _notice.value = "${p.name} ha accettato il nuovo ingaggio di ${money(rq.importo)} l'anno."
                }
                rq.tipo == Personalita.Richiesta.AUMENTO -> {
                    dao.updatePlayer(p.copy(morale = (p.morale - 10).coerceAtLeast(0)))
                    _notice.value = "${p.name} resta, ma non l'ha presa bene."
                }
                accetta -> { sell(p); _notice.value = "${p.name} sarà ceduto: cerchiamo un club." }
                else -> {
                    dao.updatePlayer(p.copy(morale = (p.morale - 8).coerceAtLeast(0)))
                    _notice.value = "${p.name} resta controvoglia."
                }
            }
            caricaSocieta()
        }
    }

    /** Scambio di un tuo giocatore con uno di un altro club, più un conguaglio che paghi tu. */
    fun proponiScambio(mio: Player, loro: Player, conguaglio: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            if (!mercatoAperto()) { _notice.value = "${_extra.value.finestra.nome}: gli scambi si fanno solo nelle finestre di mercato."; return@launch }
            if (dao.stato("ffp_blocco") == _season.value) { _notice.value = "Mercato in entrata bloccato per il fair play finanziario."; return@launch }
            val t = dao.team(me.id)
            if (conguaglio < 0 || conguaglio > t.budget) { _notice.value = "Conguaglio non valido o budget insufficiente."; return@launch }
            val m = dao.playerById(mio.id); val l = dao.playerById(loro.id)
            if (m == null || l == null || m.teamId != t.id || l.teamId == t.id) { _notice.value = "Scambio non più possibile."; return@launch }
            val altro = dao.team(l.teamId)
            when (val e = Scambi.valuta(m, l, conguaglio)) {
                is Scambi.Esito.Ok -> {
                    dao.updatePlayer(m.copy(teamId = altro.id, starter = false))
                    dao.updatePlayer(l.copy(teamId = t.id, starter = false))
                    if (conguaglio > 0) {
                        dao.updateTeam(altro.copy(budget = altro.budget + conguaglio))
                        dao.updateTeam(t.copy(budget = t.budget - conguaglio))
                        registra(t.id, "Acquisto (scambio) ${l.name}", -conguaglio, t.budget - conguaglio)
                    }
                    _notice.value = e.msg
                }
                is Scambi.Esito.Contro -> _notice.value = "${altro.name} vuole almeno ${money(e.conguaglio)} di conguaglio."
                is Scambi.Esito.No -> _notice.value = e.msg
            }
        }
    }

    // ---- Mutui bancari ----
    private val _mutui = MutableStateFlow<List<Mutui.Mutuo>>(emptyList())
    val mutui: StateFlow<List<Mutui.Mutuo>> = _mutui

    fun caricaMutui() { viewModelScope.launch(Dispatchers.IO) { _mutui.value = Mutui.decode(dao.stato("mutui")); caricaFinExtra() } }

    /** Dati di Finanze aggiunti in v0.6 (cartellini, rivendite, nuovo stadio, prato, proprietario). */
    data class FinExtra(
        val rivendita: Boolean = false, val crediti: Long = 0L, val plusvalenze: Long = 0L,
        val cartellini: List<Pair<String, Cartellini.Cartellino>> = emptyList(), val rivendite: List<Cartellini.Rivendita> = emptyList(),
        val stadio: NuovoStadio.Progetto? = null, val prato: Int = 80, val cura: Int = 0, val proprietario: String = "", val salvataggi: Int = 0
    )
    private val _finExtra = MutableStateFlow(FinExtra())
    val finExtra: StateFlow<FinExtra> = _finExtra

    private suspend fun caricaFinExtra() {
        val nomi = dao.allPlayers().associate { it.id to it.name }
        _finExtra.value = FinExtra(
            rivendita = prefs.getBoolean("rivendita_on", false),
            crediti = dao.stato(Cartellini.KEY_CREDITI)?.toLongOrNull() ?: 0L,
            plusvalenze = Cartellini.plusvalenzeStagione(dao.stato(Cartellini.KEY_PLUS), _season.value),
            cartellini = Cartellini.decode(dao.stato(Cartellini.KEY)).mapNotNull { c -> nomi[c.playerId]?.let { it to c } },
            rivendite = Cartellini.decodeR(dao.stato(Cartellini.KEY_RIVENDITE)),
            stadio = NuovoStadio.decode(dao.stato(NuovoStadio.KEY)),
            prato = dao.statoInt(Prato.KEY), cura = dao.statoInt(Prato.KEY_CURA),
            proprietario = dao.stato(NuovoProprietario.KEY).orEmpty(), salvataggi = dao.statoInt(NuovoProprietario.KEY_SALVATAGGI)
        )
    }

    fun setRivendita(b: Boolean) { prefs.edit().putBoolean("rivendita_on", b).apply(); viewModelScope.launch(Dispatchers.IO) { caricaFinExtra() } }
    fun setCuraPrato(l: Int) { viewModelScope.launch(Dispatchers.IO) { dao.setStatoInt(Prato.KEY_CURA, l, 0, 2); caricaFinExtra() } }

    /** Avvia il progetto del nuovo stadio: niente anticipo, una rata a ogni fine stagione per 3 stagioni. */
    fun avviaNuovoStadio(posti: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val t = selectedTeam.value ?: return@launch
            when {
                NuovoStadio.decode(dao.stato(NuovoStadio.KEY)) != null -> _notice.value = "C'è già un nuovo stadio in costruzione."
                posti <= t.stadiumCapacity -> _notice.value = "Il nuovo stadio deve essere più grande dell'attuale."
                else -> {
                    val p = NuovoStadio.nuovo(posti)
                    dao.setStato(Stato(NuovoStadio.KEY, NuovoStadio.encode(p)))
                    _notice.value = "Progetto approvato: $posti posti, ${money(NuovoStadio.costo(posti))} in ${NuovoStadio.ANNI} rate da ${money(p.rata)} a fine stagione."
                }
            }
            caricaFinExtra()
        }
    }

    /** Chiede un mutuo: l'importo entra subito nel budget mercato. */
    fun chiediMutuo(bancaId: Int, importo: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val attivi = Mutui.decode(dao.stato("mutui"))
            when (val e = Mutui.richiedi(attivi, bancaId, importo)) {
                is Mutui.Esito.No -> _notice.value = e.msg
                is Mutui.Esito.Ok -> {
                    val t = dao.team(me.id)
                    dao.updateTeam(t.copy(budget = t.budget + importo))
                    registra(t.id, "Mutuo ${Mutui.banca(bancaId)?.nome.orEmpty()}: erogazione", importo, t.budget + importo)
                    dao.setStato(Stato("mutui", Mutui.encode(e.lista)))
                    _mutui.value = e.lista
                    _notice.value = "Mutuo di ${money(importo)} firmato. Rata annuale ${money(e.mutuo.rata)} per ${e.mutuo.anni} anni."
                }
            }
        }
    }

    /** Estingue in anticipo un mutuo (residuo più penale del 2%). */
    fun estinguiMutuo(bancaId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val attivi = Mutui.decode(dao.stato("mutui"))
            val m = attivi.firstOrNull { it.banca == bancaId } ?: return@launch
            val t = dao.team(me.id)
            val costo = Mutui.costoEstinzione(m)
            if (costo > t.budget) { _notice.value = "Budget insufficiente per estinguere: servono ${money(costo)}."; return@launch }
            dao.updateTeam(t.copy(budget = t.budget - costo))
            registra(t.id, "Estinzione mutuo ${Mutui.banca(bancaId)?.nome.orEmpty()}", -costo, t.budget - costo)
            val resto = attivi.filter { it.banca != bancaId }
            dao.setStato(Stato("mutui", Mutui.encode(resto)))
            _mutui.value = resto
            _notice.value = "Mutuo estinto (${money(costo)}, penale inclusa)."
        }
    }

    private val _rateMercato = MutableStateFlow(prefs.getBoolean("rate_mercato", false))
    val rateMercato: StateFlow<Boolean> = _rateMercato
    fun setRateMercato(b: Boolean) { prefs.edit().putBoolean("rate_mercato", b).apply(); _rateMercato.value = b }
    private val _debitoRate = MutableStateFlow(0L)
    val debitoRate: StateFlow<Long> = _debitoRate

    /** Sposta un mutuo su una banca più economica (penale 2% sul residuo, pagata subito). */
    fun rifinanziaMutuo(da: Int, a: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val attivi = Mutui.decode(dao.stato("mutui"))
            when (val e = Mutui.rifinanzia(attivi, da, a)) {
                is Mutui.Esito.No -> _notice.value = e.msg
                is Mutui.Esito.Ok -> {
                    val vecchio = attivi.first { it.banca == da }
                    val penale = Mutui.penaleRifinanziamento(vecchio)
                    val t = dao.team(me.id)
                    if (penale > t.budget) { _notice.value = "Budget insufficiente per la penale: ${money(penale)}."; return@launch }
                    dao.updateTeam(t.copy(budget = t.budget - penale))
                    registra(t.id, "Estinzione mutuo ${Mutui.banca(da)?.nome.orEmpty()} (rifinanziamento)", -penale, t.budget - penale)
                    dao.setStato(Stato("mutui", Mutui.encode(e.lista)))
                    _mutui.value = e.lista
                    _notice.value = "Debito spostato su ${Mutui.banca(a)?.nome.orEmpty()} al ${"%.1f".format(e.mutuo.tasso * 100)}%. Nuova rata ${money(e.mutuo.rata)}."
                }
            }
        }
    }

    /** Fine stagione: rate dei mutui e degli acquisti a rate, poi i tagli del presidente se il debito è alto. */
    private suspend fun pagaMutui(meId: Long) {
        fineStagioneV06(meId)
        val attivi = Mutui.decode(dao.stato("mutui"))
        val rateDovute = dao.stato("rate_mercato")?.toLongOrNull() ?: 0L
        if (attivi.isEmpty() && rateDovute <= 0L) { _debitoRate.value = 0L; return }
        var t = dao.team(meId)
        val nuovi = ArrayList<Mutui.Mutuo>()
        val note = ArrayList<String>()
        for (m in attivi) {
            val nome = Mutui.banca(m.banca)?.nome.orEmpty()
            val r = Mutui.paga(m, t.budget)
            if (r.interessi > 0) { t = t.copy(budget = t.budget - r.interessi); registra(meId, "Interessi mutuo $nome", -r.interessi, t.budget) }
            if (r.capitale > 0) { t = t.copy(budget = t.budget - r.capitale); registra(meId, "Rimborso mutuo $nome", -r.capitale, t.budget) }
            if (r.mora > 0) note += "$nome: budget insufficiente, mora ${money(r.mora)}."
            else if (r.nuovo == null) note += "Mutuo $nome estinto."
            r.nuovo?.let { nuovi += it }
        }
        if (rateDovute > 0L) {
            val paga = rateDovute.coerceAtMost(t.budget.coerceAtLeast(0L))
            if (paga > 0L) { t = t.copy(budget = t.budget - paga); registra(meId, "Acquisto: saldo rata", -paga, t.budget) }
            val resto = rateDovute - paga
            dao.setStato(Stato("rate_mercato", if (resto > 0L) resto.toString() else ""))
            if (resto > 0L) note += "Rate degli acquisti non pagate per ${money(resto)}: restano da saldare."
            _debitoRate.value = resto
        }
        val taglio = Mutui.taglio(Mutui.debitoTotale(nuovi))
        if (taglio > 0L) {
            t = t.copy(budget = t.budget - taglio)
            registra(meId, "Tagli del presidente (debito alto)", -taglio, t.budget)
            note += "Il presidente taglia ${money(taglio)} dal budget: hai troppi debiti."
        }
        dao.updateTeam(t)
        dao.setStato(Stato("mutui", Mutui.encode(nuovi)))
        _mutui.value = nuovi
        note.forEach { avviso(it) }
    }

    /** A fine stagione: fair play finanziario, record, Hall of Fame. `vecchia` è il nome della stagione appena chiusa. */
    private suspend fun chiudiStagioneSocieta(meId: Long, vecchia: String) {
        pagaMutui(meId)
        // Fair play: perdita operativa degli ultimi tre anni
        val t0 = dao.team(meId)
        val ops = (FairPlay.decode(dao.stato("ffp_perdite")).filter { it.first != vecchia } +
            (vecchia to FairPlay.operativo(dao.movimentiStagioneOnce(meId, vecchia)))).takeLast(FairPlay.ANNI)
        dao.setStato(Stato("ffp_perdite", FairPlay.encode(ops)))
        val e = FairPlay.valuta(ops.map { -it.second }, t0.budget)
        if (e.sforamento > 0) {
            val multa = e.multa.coerceIn(0L, t0.budget.coerceAtLeast(0L))
            dao.updateTeam(t0.copy(budget = t0.budget - multa))
            registra(t0.id, "Multa fair play", -multa, t0.budget - multa)
            if (e.blocco) dao.setStato(Stato("ffp_blocco", _season.value))
            avviso("Fair play finanziario: perdita di ${money(e.perdita)} in tre anni, oltre la soglia di ${money(e.soglia)}. Multa ${money(multa)}" +
                (if (e.blocco) " e mercato in entrata bloccato per la nuova stagione." else "."))
        } else if (dao.stato("ffp_blocco") == vecchia) dao.setStato(Stato("ffp_blocco", ""))
        salvaDallaBancarotta(meId)
        // Record di stagione
        val ms = dao.matchesOnce(key(dao.team(meId)), vecchia).filter { it.played && !Cup.isCup(it.round) }.sortedBy { it.round }
        val ris = ms.map { m -> if (m.homeTeamId == meId) m.homeGoals to m.awayGoals else m.awayGoals to m.homeGoals }
        val vecchio = Storia.decodeRecord(dao.stato("storia_rec"))
        val nuovo = Storia.nuoviRecord(vecchio, ris, vecchia)
        dao.setStato(Stato("storia_rec", Storia.encodeRecord(nuovo)))
        if (nuovo.punti > vecchio.punti && vecchio.punti > 0) avviso("Nuovo record di punti del club: ${nuovo.punti}.")
        // Hall of Fame: anni con la squadra, media e gol in carriera
        val rosa = dao.playersOnce(meId)
        val anni = Disciplina.decode(dao.stato("storia_anni")).filterKeys { id -> rosa.any { it.id == id } }.toMutableMap()
        rosa.forEach { anni[it.id] = (anni[it.id] ?: 0) + 1 }
        dao.setStato(Stato("storia_anni", Disciplina.encode(anni)))
        val gol = HashMap<Long, Int>()
        dao.statsSeasons().forEach { st -> dao.statsOf(st).forEach { gol[it.playerId] = (gol[it.playerId] ?: 0) + it.gol } }
        val hall = Storia.decodeBandiere(dao.stato("storia_hall")).toMutableList()
        val nuovi = ArrayList<String>()
        rosa.forEach { p ->
            val a = anni[p.id] ?: 0; val g = gol[p.id] ?: 0
            if (hall.none { it.nome == p.name } && Storia.inHall(a, p.overall, g)) {
                hall += Storia.Bandiera(p.name, p.numero, a, g, p.overall); nuovi += p.name
            }
        }
        dao.setStato(Stato("storia_hall", Storia.encodeBandiere(hall)))
        if (nuovi.isNotEmpty()) avviso("Entrano nella Hall of Fame del club: ${nuovi.joinToString(", ")}.")
    }

    /** Fine stagione v0.6: crediti delle cessioni a rate, ammortamenti, rivendite, nuovo stadio, cura del prato. */
    private var noteSpareggi: List<String> = emptyList()

    private suspend fun fineStagioneV06(meId: Long) {
        var t = dao.team(meId)
        val note = ArrayList<String>(noteSpareggi).also { noteSpareggi = emptyList() }
        val cred = dao.stato(Cartellini.KEY_CREDITI)?.toLongOrNull() ?: 0L
        if (cred > 0L) {
            t = t.copy(budget = t.budget + cred); registra(meId, "Vendita: incasso rata", cred, t.budget)
            dao.setStato(Stato(Cartellini.KEY_CREDITI, "")); note += "Incassate le rate delle cessioni: ${money(cred)}."
        }
        dao.setStato(Stato(Cartellini.KEY, Cartellini.encode(Cartellini.avanza(Cartellini.decode(dao.stato(Cartellini.KEY))))))
        // Rivendite: il club che lo ha comprato può cederlo a un altro club della stessa nazione
        val rng = kotlin.random.Random.Default
        val rimaste = ArrayList<Cartellini.Rivendita>()
        for (r in Cartellini.decodeR(dao.stato(Cartellini.KEY_RIVENDITE))) {
            val p = dao.playerById(r.playerId) ?: continue
            if (p.teamId == meId) continue
            val da = dao.team(p.teamId)
            val valore = Market.value(p)
            val compratore = dao.allTeams().filter { it.id != da.id && it.id != meId && it.nazione == da.nazione && it.budget >= valore }.randomOrNull(rng)
            if (compratore == null || rng.nextDouble() >= Cartellini.probRivendita(p)) { rimaste += r; continue }
            dao.updatePlayer(p.copy(teamId = compratore.id, starter = false))
            dao.updateTeam(compratore.copy(budget = compratore.budget - valore))
            dao.updateTeam(da.copy(budget = da.budget + valore))
            val quota = valore * r.pct / 100
            t = t.copy(budget = t.budget + quota); registra(meId, "Percentuale rivendita ${p.name}", quota, t.budget)
            note += "${p.name} passa da ${da.name} a ${compratore.name} per ${money(valore)}: incassi il ${r.pct}% (${money(quota)})."
        }
        dao.setStato(Stato(Cartellini.KEY_RIVENDITE, Cartellini.encodeR(rimaste)))
        // Nuovo stadio: rata annuale, alla fine la nuova capienza
        NuovoStadio.decode(dao.stato(NuovoStadio.KEY))?.let { pr ->
            t = t.copy(budget = t.budget - pr.rata); registra(meId, "Ampliamento: nuovo stadio (rata)", -pr.rata, t.budget)
            if (pr.anniRimasti <= 1) {
                t = t.copy(stadiumCapacity = pr.posti); dao.setStato(Stato(NuovoStadio.KEY, ""))
                dao.setStatoInt(Prato.KEY, 100); note += "Il nuovo stadio è pronto: ${pr.posti} posti!"
            } else dao.setStato(Stato(NuovoStadio.KEY, NuovoStadio.encode(pr.copy(anniRimasti = pr.anniRimasti - 1))))
        }
        val cura = Prato.costoCura(dao.statoInt(Prato.KEY_CURA))
        if (cura > 0L) { t = t.copy(budget = t.budget - cura); registra(meId, "Ampliamento: cura del prato", -cura, t.budget) }
        dao.setStatoInt(Prato.KEY, (dao.statoInt(Prato.KEY) + 30).coerceAtMost(100))   // in estate il prato si rifà
        dao.setStato(Stato(Minutaggio.KEY, "")); dao.setStatoInt(Minutaggio.KEY_PARTITE, 0, 0, 999)
        dao.updateTeam(t)
        note.forEach { avviso(it) }
    }

    /** Bancarotta: se la cassa è finita e ci sono debiti, un nuovo proprietario salva la società (come nella realtà). */
    private suspend fun salvaDallaBancarotta(meId: Long) {
        val t = dao.team(meId)
        val mutui = Mutui.decode(dao.stato("mutui"))
        val rate = dao.stato("rate_mercato")?.toLongOrNull() ?: 0L
        val debiti = Mutui.debitoTotale(mutui) + rate
        val ingaggi = dao.playersOnce(meId).sumOf { it.salary } + dao.staffOnce(meId).sumOf { it.salary }
        if (!NuovoProprietario.inCrisi(t.budget, debiti, ingaggi)) return
        val div = dao.allTeams().filter { it.nazione == t.nazione }.map { it.campionato }.distinct()
            .let { l -> if (t.campionato.startsWith("Serie C")) 2 else l.indexOf(t.campionato).coerceAtLeast(0) }
        val capitale = NuovoProprietario.ricapitalizzazione(ingaggi, div)
        val nome = NuovoProprietario.nome(kotlin.random.Random.Default, dao.stato(NuovoProprietario.KEY))
        val nuovo = t.budget.coerceAtLeast(0L) + capitale
        dao.updateTeam(t.copy(budget = nuovo))
        registra(meId, "Nuovo proprietario: ricapitalizzazione", capitale, nuovo)
        dao.setStato(Stato("mutui", "")); _mutui.value = emptyList()
        dao.setStato(Stato("rate_mercato", "")); _debitoRate.value = 0L
        dao.setStato(Stato("ffp_perdite", "")); dao.setStato(Stato("ffp_blocco", ""))
        dao.setStato(Stato(NuovoProprietario.KEY, nome))
        dao.setStatoInt(NuovoProprietario.KEY_SALVATAGGI, dao.statoInt(NuovoProprietario.KEY_SALVATAGGI) + 1, 0, 999)
        dao.setStatoInt(StatoKeys.FIDUCIA, NuovoProprietario.FIDUCIA_NUOVA)
        dao.setStatoInt(StatoKeys.TIFOSI, dao.statoInt(StatoKeys.TIFOSI) + 5)
        avviso(NuovoProprietario.messaggio(nome, debiti, capitale, ::money))
    }

    /** Autosalvataggio: ogni 4 partite tue, nello slot dedicato. */
    private suspend fun autoSalva() {
        val t = selectedTeam.value ?: return
        val n = dao.statoInt("c_partite")
        if (n - dao.statoInt("auto_ultimo") < 4) return
        try {
            withContext(Dispatchers.IO) {
                SaveSlots.save(ctx, db, SaveSlots.AUTO, t.id, t.name, prefs.getInt("season_year", 2026), _season.value)
            }
            dao.setStatoInt("auto_ultimo", n, 0, Int.MAX_VALUE)
            _slots.value = SaveSlots.list(ctx)
        } catch (e: Exception) { /* l'autosalvataggio non deve mai disturbare il gioco */ }
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

    fun startGame(team: Team, nome: String, cognome: String) {
        selectTeam(team)
        prefs.edit().putBoolean("partita_iniziata", true)
            .putString("coach_nome", nome).putString("coach_cognome", cognome).apply()
        _presidente.value = Presidente.benvenuto(team.name, nome, cognome)
        _allenatore.value = "$nome $cognome"
        _started.value = true
        viewModelScope.launch(Dispatchers.IO) {
            // Difficoltà scelta nelle Opzioni: cambia il budget di mercato iniziale della nuova carriera.
            val liv = _opzioni.value.difficolta
            if (liv != Difficolta.NORMALE) {
                val t = dao.team(team.id)
                dao.updateTeam(t.copy(budget = Difficolta.budgetIniziale(t.budget, liv, profiloPersonalizzato())))
            }
            refreshSponsorOffers(team.id); refreshClub(); aggiornaClassifica()
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
    /** Salva nel primo slot libero (o nello slot 1 se sono tutti pieni), poi chiude l'app. */
    fun salvaEEsci(onFatto: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val t = selectedTeam.value
            if (t != null) {
                val libero = _slots.value.firstOrNull { it.teamName == null && it.n <= SaveSlots.COUNT }?.n ?: 1
                try {
                    SaveSlots.save(ctx, db, libero, t.id, t.name, prefs.getInt("season_year", 2026), _season.value)
                } catch (e: Exception) {
                    _notice.value = "Salvataggio non riuscito: ${e.message}"
                    return@launch
                }
                _slots.value = SaveSlots.list(ctx)
            }
            withContext(Dispatchers.Main) { onFatto() }
        }
    }

    /** Nuovo campionato: stessi club e giocatori; si ricomincia dalla scelta di squadra e allenatore. */
    fun nuovoCampionato() {
        viewModelScope.launch(Dispatchers.IO) {
            // Una nuova carriera riparte da valori neutri: niente fiducia, sponsor o esonero ereditati.
            DataIO.CHIAVI_STATO.forEach { k -> dao.setStato(Stato(k, "")) }
            dao.setStato(Stato("offerte", ""))
            listOf("gialli", "squal", "richieste", "storia_anni").forEach { dao.setStato(Stato(it, "")) }
        }
        prefs.edit().remove("team_id").putBoolean("partita_iniziata", false).apply()
        selectedId.value = null
        _started.value = false
        _fine.value = null
        _presidente.value = null
    }

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

    // Avvio: tutte le operazioni iniziali partono SOLO quando ogni proprietà è già creata.
    // Prima alcune partivano a metà costruzione e trovavano stati ancora nulli (crash all'avvio, v0.4-v0.6).
    init { avvio1(); avvio2(); avvio3(); avvio4(); avvio5(); avvio6() }
}
