package it.androcalcio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.androcalcio.data.AndrocalcioDatabase
import it.androcalcio.data.Player
import it.androcalcio.data.SeedData
import it.androcalcio.data.Team
import it.androcalcio.data.MatchResult
import it.androcalcio.engine.Fixtures
import it.androcalcio.engine.Formation
import it.androcalcio.engine.MatchEngine
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

    private val dao = AndrocalcioDatabase.create(app).dao()

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

    private val season = "2026/27"
    private fun key(t: Team) = "${t.nazione}|${t.campionato}"

    val matches: StateFlow<List<MatchResult>> =
        selectedTeam
            .flatMapLatest { t ->
                if (t == null) flowOf(emptyList()) else dao.matches(key(t), season)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            selectedTeam.filterNotNull().collect { t ->
                val k = key(t)
                if (dao.fixtureCount(k, season) == 0) {
                    val ids = dao.teamsOfLeague(t.nazione, t.campionato).map { it.id }
                    dao.insertMatches(Fixtures.generate(ids, season, k))
                }
            }
        }
    }

    private fun playRounds(all: Boolean) {
        viewModelScope.launch {
            val t = selectedTeam.value ?: return@launch
            val ms = dao.matchesOnce(key(t), season)
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

    fun selectTeam(team: Team) {
        selectedId.value = team.id
    }
}
