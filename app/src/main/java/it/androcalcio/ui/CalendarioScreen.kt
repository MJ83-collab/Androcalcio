package it.androcalcio.ui

import androidx.compose.runtime.remember
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import it.androcalcio.engine.Mentality
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
import it.androcalcio.engine.Cup
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.theme.AC
import it.androcalcio.ui.components.HeadCell
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PcButton
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    team: Team?,
    teams: List<Team>,
    matches: List<MatchResult>,
    onNext: () -> Unit,
    onSeason: () -> Unit,
    season: String,
    onClose: () -> Unit,
    mentality: Mentality = Mentality.EQUILIBRATA,
    onMentality: (Mentality) -> Unit = {},
    onNextLive: () -> Unit = onNext,
    cupMatches: List<MatchResult> = emptyList()
) {
    var askNext by remember { mutableStateOf(false) }
    if (askNext) AlertDialog(
        onDismissRequest = { askNext = false },
        title = { Text("Prossima giornata") },
        text = { Text("Vuoi guardare la tua partita dal vivo (pausa, cambi, mentalità) o avere solo il risultato?") },
        confirmButton = { Button(onClick = { askNext = false; onNextLive() }) { Text("Guarda la partita") } },
        dismissButton = { TextButton(onClick = { askNext = false; onNext() }) { Text("Solo risultato") } }
    )
    if (team == null) return
    val byId = teams.associateBy { it.id }
    var tab by rememberSaveable { mutableStateOf(0) }
    var round by rememberSaveable { mutableStateOf(0) } // 0 = automatica
    val nextRound = matches.filter { !it.played }.minOfOrNull { it.round }
    val cupPending = cupMatches.any { !it.played }
    val pending = nextRound != null || cupPending
    val lastRound = matches.maxOfOrNull { it.round } ?: 1
    val shown = if (round == 0) (nextRound ?: lastRound) else round

    Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PcButton("Calendario", { tab = 0 }, Modifier.weight(1f), main = tab == 0)
            PcButton("Classifica", { tab = 1 }, Modifier.weight(1f), main = tab == 1)
            PcButton("Coppa", { tab = 2 }, Modifier.weight(1f), main = tab == 2)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PcButton("Prossima giornata", { round = 0; askNext = true }, Modifier.weight(1f), main = true, enabled = pending)
            PcButton("Fine stagione", { round = 0; onSeason() }, Modifier.weight(1f), enabled = pending)
        }

        if (pending) MentalityPicker(mentality, onMentality)

        if (!pending && matches.isNotEmpty()) {
            PcButton("Chiudi stagione e passa alla successiva", { round = 0; onClose() }, Modifier.fillMaxWidth(), main = true)
        }

        if (tab == 2) {
            Text("Coppa ${team.nazione} · $season", color = AC.Text,
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (cupMatches.isEmpty()) {
                    Text("Coppa non ancora sorteggiata.", color = AC.Text)
                }
                cupMatches.groupBy { Cup.stageOf(it.round) }.toSortedMap().forEach { (stage, ms) ->
                    val teamsIn = ms.flatMap { listOf(it.homeTeamId, it.awayTeamId) }.toSet().size
                    SectionTitle(Cup.stageName(stage, teamsIn))
                    PaperPanel(Modifier.fillMaxWidth()) {
                        ms.sortedBy { it.round }.forEachIndexed { i, m -> MatchLine(m, byId, team.id, i) }
                    }
                }
            }
        } else if (tab == 0) {
            Text("Giornata $shown di $lastRound · ${team.campionato} $season", color = AC.Text,
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items((1..lastRound).toList()) { r ->
                    FilterChip(selected = r == shown, onClick = { round = r }, label = { Text("$r") })
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                PaperPanel(Modifier.fillMaxWidth()) {
                    matches.filter { it.round == shown }.forEachIndexed { i, m -> MatchLine(m, byId, team.id, i) }
                }
            }
        } else {
            Text("Giornata $shown di $lastRound · ${team.campionato} $season", color = AC.Text,
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items((1..lastRound).toList()) { r ->
                    FilterChip(selected = r == shown, onClick = { round = r }, label = { Text("$r") })
                }
            }
            val league = teams.filter { it.nazione == team.nazione && it.campionato == team.campionato }
            // Divisioni della nazione in ordine: la prima è la massima serie
            val tiers = teams.filter { it.nazione == team.nazione }.groupBy { it.campionato }
                .entries.sortedBy { e -> e.value.minOf { it.id } }.map { it.key }
            val tier = tiers.indexOf(team.campionato)
            val kk = minOf(3, league.size / 4)
            val rows = standings(league, matches.filter { it.round <= shown })
            val heads = listOf("G", "V", "N", "P", "GF", "GS", "DR", "PT")
            val headColor = listOf(AC.ColBlue, AC.ColGreen, AC.ColBlue, AC.ColEn, AC.ColBlue, AC.ColBlue, AC.ColBlue, AC.ColOrange)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                PaperPanel(Modifier.fillMaxWidth()) {
                    Column(Modifier.horizontalScroll(rememberScrollState())) {
                        Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            HeadCell("", AC.PaperText, Modifier.width(14.dp))
                            HeadCell("N", AC.PaperText, Modifier.width(22.dp))
                            HeadCell("SQUADRA", AC.PaperText, Modifier.width(128.dp), TextAlign.Start)
                            heads.forEachIndexed { k, h -> HeadCell(h, headColor[k], Modifier.width(if (k >= 6) 32.dp else 26.dp)) }
                        }
                        rows.forEachIndexed { i, r ->
                            val mine = r.team.id == team.id
                            val c = if (mine) AC.ColBlue else AC.PaperText
                            val zone = when {
                                tier > 0 && i < kk -> "▲"
                                tier in 0 until tiers.lastIndex && i >= rows.size - kk -> "▼"
                                else -> ""
                            }
                            PaperRow(i, Modifier.then(if (mine) Modifier.background(AC.Mine) else Modifier)) {
                                Text(zone, Modifier.width(14.dp), fontSize = 10.sp,
                                    color = if (zone == "▲") AC.ColGreen else AC.ColEn)
                                Text("${i + 1}", Modifier.width(22.dp), textAlign = TextAlign.Center, fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold, color = AC.PaperText)
                                Row(Modifier.width(128.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Crest(r.team, 18.dp)
                                    Text(r.team.name, Modifier.padding(start = 6.dp), color = c, maxLines = 1,
                                        fontSize = 12.sp, fontWeight = if (mine) FontWeight.ExtraBold else FontWeight.Medium)
                                }
                                val cells = listOf(r.played, r.won, r.drawn, r.lost, r.gf, r.ga, r.gd, r.pts)
                                cells.forEachIndexed { k, v ->
                                    Text("$v", Modifier.width(if (k >= 6) 32.dp else 26.dp), textAlign = TextAlign.Center,
                                        fontSize = 12.sp, color = c,
                                        fontWeight = if (k == 7) FontWeight.ExtraBold else FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Riga di una partita (casa - trasferta, risultato in riquadro nero), evidenziata se è della tua squadra. */
@Composable
private fun MatchLine(m: MatchResult, byId: Map<Long, Team>, teamId: Long, i: Int) {
    val h = byId[m.homeTeamId]
    val a = byId[m.awayTeamId]
    val mine = m.homeTeamId == teamId || m.awayTeamId == teamId
    val c = if (mine) AC.ColBlue else AC.PaperText
    val w = if (mine) FontWeight.ExtraBold else FontWeight.Medium
    PaperRow(i, Modifier.then(if (mine) Modifier.background(AC.Mine) else Modifier)) {
        Text(h?.name ?: "?", Modifier.weight(1f), color = c, fontWeight = w,
            textAlign = TextAlign.End, fontSize = 12.sp, maxLines = 1)
        Box(
            Modifier.padding(horizontal = 6.dp).width(54.dp).background(AC.Btn).padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(if (m.played) "${m.homeGoals} - ${m.awayGoals}" else "-", color = Color.White,
                fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
        Text(a?.name ?: "?", Modifier.weight(1f), color = c, fontWeight = w, fontSize = 12.sp, maxLines = 1)
    }
}
