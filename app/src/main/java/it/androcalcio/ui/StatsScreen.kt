package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.androcalcio.data.Clausola
import it.androcalcio.data.Goal
import it.androcalcio.data.PlayerStat
import it.androcalcio.data.MatchResult
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

/** Marcatori e portieri del tuo campionato, fino alla giornata scelta. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    team: Team?,
    teams: List<Team>,
    matches: List<MatchResult>,
    goals: List<Goal>,
    players: List<Player>,
    season: String,
    playerStats: List<PlayerStat>,
    statSeasons: List<String>,
    statSeasonAttiva: String,
    onStatSeason: (String) -> Unit,
    clausole: List<Clausola>
) {
    if (team == null) return
    var tab by rememberSaveable { mutableStateOf(0) }
    var pick by rememberSaveable { mutableStateOf(0) } // 0 = ultima giornata giocata
    val played = matches.filter { it.played }
    val lastPlayed = played.maxOfOrNull { it.round } ?: 0
    val upTo = if (pick == 0) lastPlayed else pick
    val byTeam = teams.associateBy { it.id }
    val byPlayer = players.associateBy { it.id }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("${team.campionato} $season", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PcChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Marcatori") })
            PcChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Portieri") })
            PcChip(selected = tab == 2, onClick = { tab = 2 }, label = { Text("Giocatori") })
            PcChip(selected = tab == 3, onClick = { tab = 3 }, label = { Text("Clausole") })
        }
        if (tab == 2) {
            GiocatoriPane(team, teams, players, playerStats, statSeasons, statSeasonAttiva, onStatSeason)
            return@Column
        }
        if (tab == 3) {
            ClausolePane(team, players, clausole, playerStats, statSeasonAttiva)
            return@Column
        }
        if (lastPlayed == 0) {
            Text("Nessuna giornata giocata: gioca dal Calendario.", color = AC.TextDim)
            return@Column
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items((1..lastPlayed).toList()) { r ->
                PcChip(selected = r == upTo, onClick = { pick = r }, label = { Text("$r") })
            }
        }
        Text("Fino alla giornata $upTo", style = MaterialTheme.typography.bodySmall, color = AC.TextDim)

        if (tab == 0) {
            val rows = goals.filter { it.round <= upTo }.groupBy { it.playerId }
                .map { (id, g) -> Triple(id, g.first().teamId, g.size) }
                .sortedByDescending { it.third }.take(30)
            if (rows.isEmpty()) Text("Ancora nessun gol.", color = AC.TextDim)
            Header("RETI", "GIOCATORE", "SQUADRA")
            LazyColumn(Modifier.weight(1f)) {
                itemsIndexed(rows, key = { _, r -> r.first }) { _, r ->
                    val mine = r.second == team.id
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${r.third}", Modifier.width(40.dp), fontWeight = FontWeight.Bold, color = AC.Accent)
                        Text(byPlayer[r.first]?.name ?: "?", Modifier.weight(1f),
                            color = if (mine) AC.Accent else AC.Text, maxLines = 1)
                        byTeam[r.second]?.let { Crest(it, 20.dp) }
                        Text(byTeam[r.second]?.name ?: "", Modifier.weight(1f).padding(start = 6.dp),
                            color = AC.TextDim, maxLines = 1)
                    }
                }
            }
        } else {
            // Gol subiti dalla squadra fino alla giornata scelta, col portiere titolare (il migliore).
            val rows = teams.filter { it.nazione == team.nazione && it.campionato == team.campionato }.map { t ->
                val conceded = played.filter { it.round <= upTo }.sumOf {
                    when (t.id) { it.homeTeamId -> it.awayGoals; it.awayTeamId -> it.homeGoals; else -> 0 }
                }
                val keeper = players.filter { it.teamId == t.id && it.role == "P" }.maxByOrNull { it.overall }
                Triple(t, keeper?.name ?: "-", conceded)
            }.sortedBy { it.third }
            Header("SUBITI", "PORTIERE", "SQUADRA")
            LazyColumn(Modifier.weight(1f)) {
                items(rows, key = { it.first.id }) { r ->
                    val mine = r.first.id == team.id
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${r.third}", Modifier.width(40.dp), fontWeight = FontWeight.Bold, color = AC.Accent)
                        Text(r.second, Modifier.weight(1f), color = if (mine) AC.Accent else AC.Text, maxLines = 1)
                        Crest(r.first, 20.dp)
                        Text(r.first.name, Modifier.weight(1f).padding(start = 6.dp), color = AC.TextDim, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.GiocatoriPane(
    team: Team, teams: List<Team>, players: List<Player>, stats: List<PlayerStat>,
    stagioni: List<String>, attiva: String, onStagione: (String) -> Unit
) {
    var soloMia by rememberSaveable { mutableStateOf(true) }
    val byTeam = teams.associateBy { it.id }
    val byPlayer = players.associateBy { it.id }
    val elenco = stats.filter { !soloMia || it.teamId == team.id }
        .sortedWith(compareByDescending<PlayerStat> { it.gol }.thenByDescending { it.presenze })
        .take(80)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items((stagioni + attiva).distinct().sortedDescending()) { s ->
            PcChip(selected = s == attiva, onClick = { onStagione(s) }, label = { Text(s) })
        }
    }
    PcChip(selected = soloMia, onClick = { soloMia = !soloMia },
        label = { Text(if (soloMia) "Solo la mia squadra" else "Tutti i club") })
    if (elenco.isEmpty()) Text("Nessuna statistica per questa stagione: si registra giocando.", color = AC.TextDim)
    Row(Modifier.fillMaxWidth()) {
        Text("GIOCATORE", Modifier.weight(1f), color = AC.TextDim, style = MaterialTheme.typography.labelSmall)
        Text("PRESENZE", Modifier.width(72.dp), color = AC.TextDim, style = MaterialTheme.typography.labelSmall)
        Text("GOL", Modifier.width(44.dp), color = AC.TextDim, style = MaterialTheme.typography.labelSmall)
    }
    LazyColumn(Modifier.weight(1f)) {
        items(elenco, key = { it.playerId }) { st ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(byPlayer[st.playerId]?.name ?: "Ex giocatore",
                        modifier = byPlayer[st.playerId]?.let { Modifier.apriScheda(it) } ?: Modifier,
                        color = if (st.teamId == team.id) AC.Accent else AC.Text, maxLines = 1)
                    Text(byTeam[st.teamId]?.name ?: "", color = AC.TextDim,
                        style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                Text("${st.presenze}", Modifier.width(72.dp), color = AC.Text)
                Text("${st.gol}", Modifier.width(44.dp), fontWeight = FontWeight.Bold, color = AC.Accent)
            }
        }
    }
}

@Composable
private fun ColumnScope.ClausolePane(
    team: Team, players: List<Player>, clausole: List<Clausola>, stats: List<PlayerStat>, stagione: String
) {
    val byPlayer = players.associateBy { it.id }
    val statBy = stats.associateBy { it.playerId }
    val mie = clausole.filter { it.teamId == team.id }.sortedBy { byPlayer[it.playerId]?.name ?: "" }
    Text("Stagione $stagione. Le clausole si pagano a fine stagione, se la condizione è raggiunta.",
        style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
    if (mie.isEmpty()) Text("Nessuna clausola: nasce firmando un giocatore.", color = AC.TextDim)
    LazyColumn(Modifier.weight(1f)) {
        items(mie, key = { it.playerId }) { c ->
            val pres = statBy[c.playerId]?.presenze ?: 0
            val gol = statBy[c.playerId]?.gol ?: 0
            Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(byPlayer[c.playerId]?.name ?: "Ex giocatore", style = MaterialTheme.typography.titleSmall)
                Text("Presenze $pres/30 · gol $gol/20", color = AC.Accent, style = MaterialTheme.typography.bodySmall)
                Text("30 presenze ${money(c.bonusPresenze)} · 20 gol ${money(c.bonusGol)} · " +
                    "campionato ${money(c.bonusCampionato)} · coppa ${money(c.bonusCoppa)}",
                    color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun Header(a: String, b: String, c: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(a, Modifier.width(40.dp), color = AC.TextDim, style = MaterialTheme.typography.labelSmall)
        Text(b, Modifier.weight(1f), color = AC.TextDim, style = MaterialTheme.typography.labelSmall)
        Text(c, Modifier.weight(1f).padding(start = 26.dp), color = AC.TextDim,
            style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Start)
    }
}
