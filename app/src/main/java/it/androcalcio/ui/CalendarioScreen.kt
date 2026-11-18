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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import it.androcalcio.engine.standings
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.theme.AC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    team: Team?,
    teams: List<Team>,
    matches: List<MatchResult>,
    onNext: () -> Unit,
    onSeason: () -> Unit
) {
    if (team == null) return
    val byId = teams.associateBy { it.id }
    var tab by rememberSaveable { mutableStateOf(0) }
    var round by rememberSaveable { mutableStateOf(0) } // 0 = automatica
    val nextRound = matches.filter { !it.played }.minOfOrNull { it.round }
    val lastRound = matches.maxOfOrNull { it.round } ?: 1
    val shown = if (round == 0) (nextRound ?: lastRound) else round

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("${team.campionato} 2026/27", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Calendario") })
            FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Classifica") })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { round = 0; onNext() }, enabled = nextRound != null,
                colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)
            ) { Text("Prossima giornata") }
            OutlinedButton(onClick = { round = 0; onSeason() }, enabled = nextRound != null) {
                Text("Fine stagione")
            }
        }

        if (tab == 0) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items((1..lastRound).toList()) { r ->
                    FilterChip(selected = r == shown, onClick = { round = r }, label = { Text("$r") })
                }
            }
            LazyColumn(Modifier.weight(1f)) {
                items(matches.filter { it.round == shown }, key = { it.id }) { m ->
                    val h = byId[m.homeTeamId]
                    val a = byId[m.awayTeamId]
                    val mine = m.homeTeamId == team.id || m.awayTeamId == team.id
                    val c = if (mine) AC.Accent else AC.Text
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(h?.name ?: "?", Modifier.weight(1f), color = c, textAlign = TextAlign.End)
                        Text(
                            if (m.played) "${m.homeGoals} - ${m.awayGoals}" else "-",
                            Modifier.width(60.dp), color = AC.Accent2, textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Text(a?.name ?: "?", Modifier.weight(1f), color = c)
                    }
                }
            }
        } else {
            val league = teams.filter { it.nazione == team.nazione && it.campionato == team.campionato }
            val rows = standings(league, matches)
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("#", Modifier.width(28.dp), color = AC.TextDim)
                Text("Squadra", Modifier.weight(1f), color = AC.TextDim)
                Text("G", Modifier.width(30.dp), color = AC.TextDim, textAlign = TextAlign.End)
                Text("DR", Modifier.width(38.dp), color = AC.TextDim, textAlign = TextAlign.End)
                Text("Pt", Modifier.width(36.dp), color = AC.TextDim, textAlign = TextAlign.End)
            }
            LazyColumn(Modifier.weight(1f)) {
                itemsIndexed(rows, key = { _, r -> r.team.id }) { i, r ->
                    val c = if (r.team.id == team.id) AC.Accent else AC.Text
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${i + 1}", Modifier.width(28.dp), color = AC.TextDim)
                        Crest(r.team, 22.dp)
                        Text(r.team.name, Modifier.weight(1f).padding(start = 8.dp), color = c, maxLines = 1)
                        Text("${r.played}", Modifier.width(30.dp), textAlign = TextAlign.End, color = c)
                        Text("${r.gd}", Modifier.width(38.dp), textAlign = TextAlign.End, color = c)
                        Text("${r.pts}", Modifier.width(36.dp), textAlign = TextAlign.End,
                            color = c, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
