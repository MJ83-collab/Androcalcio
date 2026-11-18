package it.androcalcio.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.androcalcio.data.Team
import it.androcalcio.engine.EventType
import it.androcalcio.engine.Formation
import it.androcalcio.engine.MatchReport
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.theme.AC
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartitaScreen(
    team: Team?,
    teams: List<Team>,
    report: MatchReport?,
    onPlay: (Team, Team, Formation) -> Unit,
    onReset: () -> Unit
) {
    if (team == null) return
    if (report == null) {
        val rivals = teams.filter { it.campionato == team.campionato && it.id != team.id }
        var awayId by rememberSaveable { mutableStateOf(-1L) }
        var formation by rememberSaveable { mutableStateOf(Formation.F442) }
        val away = rivals.firstOrNull { it.id == awayId } ?: rivals.firstOrNull()
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Partita", style = MaterialTheme.typography.headlineSmall)
            SectionTitle("Avversario (${team.campionato})")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rivals, key = { it.id }) { t ->
                    FilterChip(
                        selected = t.id == away?.id,
                        onClick = { awayId = t.id },
                        label = { Text(t.name) }
                    )
                }
            }
            SectionTitle("Il tuo modulo")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Formation.values().toList()) { f ->
                    FilterChip(
                        selected = f == formation,
                        onClick = { formation = f },
                        label = { Text(f.label) }
                    )
                }
            }
            if (away != null) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Crest(team, 56.dp); Text(team.name, textAlign = TextAlign.Center)
                        }
                        Text("vs", color = AC.TextDim)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Crest(away, 56.dp); Text(away.name, textAlign = TextAlign.Center)
                        }
                    }
                }
                Button(
                    onClick = { onPlay(team, away, formation) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AC.Accent, contentColor = AC.Bg
                    )
                ) { Text("Gioca partita (in casa)") }
            }
        }
        return
    }

    var minute by remember(report) { mutableIntStateOf(0) }
    LaunchedEffect(report) {
        minute = 0
        while (minute < 90) { delay(70); minute++ }
    }
    val seen = report.events.filter { it.minute <= minute }
    val hg = seen.count { it.type == EventType.GOAL && it.home }
    val ag = seen.count { it.type == EventType.GOAL && !it.home }
    val last = seen.lastOrNull { it.type != EventType.YELLOW }
    val bx by animateFloatAsState(last?.x ?: 0.5f, tween(300), label = "bx")
    val by by animateFloatAsState(last?.y ?: 0.5f, tween(300), label = "by")

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Crest(report.home, 44.dp)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$hg - $ag", style = MaterialTheme.typography.headlineMedium)
                        Text("$minute'", color = AC.Accent)
                    }
                    Crest(report.away, 44.dp)
                }
            }
        }
        item { Pitch(report.home, report.away, bx, by, report.homeFormation, report.awayFormation) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { minute = 90 }) { Text("Salta") }
                OutlinedButton(onClick = onReset) { Text("Nuova partita") }
            }
        }
        if (minute >= 90) item {
            Text(
                "Tiri ${report.homeShots}-${report.awayShots} · Possesso ${report.homePossession}%-${100 - report.homePossession}%",
                color = AC.TextDim, style = MaterialTheme.typography.bodySmall
            )
        }
        items(seen.reversed()) { e ->
            val c = when (e.type) {
                EventType.GOAL -> AC.Accent
                EventType.YELLOW -> AC.Warn
                EventType.SAVE -> AC.Accent2
                else -> AC.TextDim
            }
            Text("${e.minute}'  ${e.text}", color = c, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun Pitch(home: Team, away: Team, bx: Float, by: Float, hf: Formation, af: Formation) {
    val homeColor = Color(home.colorPrimary)
    val awayColor = Color(if (home.colorPrimary == away.colorPrimary) away.colorSecondary else away.colorPrimary)
    val homePos = hf.coords()
    val awayPos = af.coords()
    Canvas(Modifier.fillMaxWidth().aspectRatio(1.6f)) {
        val w = size.width; val h = size.height
        val line = Color.White.copy(alpha = 0.6f)
        val st = Stroke(width = 2f)
        drawRect(Color(0xFF1F7A3A))
        drawRect(line, style = st)
        drawLine(line, Offset(w / 2, 0f), Offset(w / 2, h), 2f)
        drawCircle(line, h * 0.15f, Offset(w / 2, h / 2), style = st)
        drawRect(line, Offset(0f, h * 0.25f), Size(w * 0.14f, h * 0.5f), style = st)
        drawRect(line, Offset(w * 0.86f, h * 0.25f), Size(w * 0.14f, h * 0.5f), style = st)
        val shift = (bx - 0.5f) * 0.2f
        val r = h * 0.03f
        homePos.forEach { (fx, fy) ->
            drawCircle(homeColor, r, Offset((fx + shift).coerceIn(0.02f, 0.98f) * w, fy * h))
        }
        awayPos.forEach { (fx, fy) ->
            drawCircle(awayColor, r, Offset(((1f - fx) + shift).coerceIn(0.02f, 0.98f) * w, fy * h))
        }
        drawCircle(Color.White, r * 0.8f, Offset(bx * w, by * h))
    }
}
