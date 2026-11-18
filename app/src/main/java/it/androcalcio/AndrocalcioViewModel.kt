package it.androcalcio

import android.app.Application
import android.net.Uri
import it.androcalcio.data.DataIO
import kotlinx.coroutines.Dispatchers
import java.io.File
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.androcalcio.data.AndrocalcioDatabase
import it.androcalcio.data.Player
import it.androcalcio.data.SeedData
import it.androcalcio.data.Team
import it.androcalcio.data.MatchResult
import it.androcalcio.engine.Fixtures
import it.androcalcio.engine.Formation
import it.androcalcio.data.ScoutReport
import it.androcalcio.data.Youth
import it.androcalcio.engine.MatchEngine
import it.androcalcio.engine.Progression
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
    private val dao = AndrocalcioDatabase.create(app).dao()
    private val customFile = File(app.filesDir, "custom.json")

    val teams: StateFlow<List<Team>> =
        dao.teams().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val selectedId = MutableStateFlow<Long?>(null)

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
            val hp = dao.playersOnce(home.id)
            val ap = dao.playersOnce(away.id)
            _report.value = MatchEngine.simulate(home, hp, away, ap, formation, Formation.values().random())
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

    private fun playRounds(all: Boolean) {
        viewModelScope.launch {
            val t = selectedTeam.value ?: return@launch
            val ms = dao.matchesOnce(key(t), _season.value)
            val teamsById = dao.teamsOfLeague(t.nazione, t.campionato).associateBy { it.id }
            val cache = HashMap<Long, List<Player>>()
            val formations = Formation.values()
            val rounds = ms.filter { !it.played }.map { it.round }.distinct().sorted()
                .let { if (all) it else it.take(1) }
            for (m in ms.filter { !it.played && it.round in rounds }) {
                val h = teamsById[m.homeTeamId] ?: continue
                val a = teamsById[m.awayTeamId] ?: continue
                val hp = cache.getOrPut(h.id) { dao.playersOnce(h.id) }
                val ap = cache.getOrPut(a.id) { dao.playersOnce(a.id) }
                val r = MatchEngine.simulate(h, hp, a, ap, formations.random(), formations.random())
                dao.updateMatch(m.copy(homeGoals = r.homeGoals, awayGoals = r.awayGoals, played = true))
            }
        }
    }

    fun playNextRound() = playRounds(false)
    fun playSeason() = playRounds(true)

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
                Player(teamId = y.teamId, name = y.name, role = y.role, age = y.age,
                    overall = y.overall, morale = 75, salary = 30_000, nationality = y.nationality,
                    potential = y.potential, contract = 3)
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
            dao.insertPlayer(Player(teamId = t.id, name = "Nuovo giocatore", role = "C", age = 22,
                overall = 60, morale = 70, salary = 50_000, nationality = "ITA"))
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

    /** Fine stagione: età, crescita, ritiri, contratti, bilancio; nuova stagione. */
    fun closeSeason() {
        viewModelScope.launch(Dispatchers.IO) {
            val me = selectedTeam.value ?: return@launch
            val rng = kotlin.random.Random.Default
            val byTeam = dao.allPlayers().groupBy { it.teamId }
            val updated = ArrayList<Player>()
            val removed = ArrayList<Player>()
            val added = ArrayList<Player>()
            val leavers = ArrayList<String>()
            var report = ""
            val newTeams = dao.allTeams().map { t ->
                val keep = ArrayList<Player>()
                (byTeam[t.id] ?: emptyList()).forEach { p ->
                    val a = Progression.advance(p, rng)
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
                val wages = keep.sumOf { it.salary }
                val gate = t.stadiumCapacity.toLong() * t.ticketPrice * 7 / 10 * 19
                val net = gate - wages * 4 / 10
                if (t.id == me.id) report = "Bilancio stagione: ${money(net)}."
                t.copy(budget = (t.budget + net).coerceAtLeast(0))
            }
            dao.updatePlayers(updated)
            dao.deletePlayers(removed)
            dao.insertPlayers(added)
            dao.updateTeams(newTeams)
            dao.clearYouths()
            val y = prefs.getInt("season_year", 2026) + 1
            prefs.edit().putInt("season_year", y).apply()
            _season.value = seasonName(y)
            _notice.value = "Nuova stagione ${seasonName(y)}. $report" +
                (if (leavers.isNotEmpty()) " Lasciano: ${leavers.joinToString(", ")}." else "")
        }
    }

    fun selectTeam(team: Team) {
        selectedId.value = team.id
    }
}
