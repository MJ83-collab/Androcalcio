package it.androcalcio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import it.androcalcio.data.Goal
import it.androcalcio.data.MatchResult
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.ui.components.Crest
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
    season: String
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
            FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Marcatori") })
            FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Portieri") })
        }
        if (lastPlayed == 0) {
            Text("Nessuna giornata giocata: gioca dal Calendario.", color = AC.TextDim)
            return@Column
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items((1..lastPlayed).toList()) { r ->
                FilterChip(selected = r == upTo, onClick = { pick = r }, label = { Text("$r") })
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
private fun Header(a: String, b: String, c: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(a, Modifier.width(40.dp), color = AC.TextDim, style = MaterialTheme.typography.labelSmall)
        Text(b, Modifier.weight(1f), color = AC.TextDim, style = MaterialTheme.typography.labelSmall)
        Text(c, Modifier.weight(1f).padding(start = 26.dp), color = AC.TextDim,
            style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Start)
    }
}
