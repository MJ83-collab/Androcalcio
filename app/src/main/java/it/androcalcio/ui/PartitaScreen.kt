package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import it.androcalcio.engine.Pressing
import it.androcalcio.engine.DefLine
import it.androcalcio.engine.TrainFocus
import it.androcalcio.engine.TrainIntensity
import it.androcalcio.engine.TrainingPlan
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.engine.EventType
import it.androcalcio.engine.Formation
import it.androcalcio.engine.Mentality
import it.androcalcio.engine.MatchReport
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.PcButton
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
    onReset: () -> Unit,
    mentality: Mentality = Mentality.EQUILIBRATA,
    onMentality: (Mentality) -> Unit = {},
    formation: Formation = Formation.F442,
    onFormation: (Formation) -> Unit = {}
) {
    if (team == null) return
    if (report == null) {
        val rivals = teams.filter { it.campionato == team.campionato && it.id != team.id }
        var awayId by rememberSaveable { mutableStateOf(-1L) }
        val away = rivals.firstOrNull { it.id == awayId } ?: rivals.firstOrNull()
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Partita", style = MaterialTheme.typography.headlineSmall)
            SectionTitle("Avversario (${team.campionato})")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rivals, key = { it.id }) { t ->
                    Choice(t.name, t.id == away?.id) { awayId = t.id }
                }
            }
            SectionTitle("Il tuo modulo")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Formation.values().toList()) { f ->
                    Choice(f.label, f == formation) { onFormation(f) }
                }
            }
            MentalityPicker(mentality, onMentality)
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
                if (AC.pc7) {
                    PcButton("Gioca partita (in casa)", { onPlay(team, away, formation) },
                        Modifier.fillMaxWidth(), main = true)
                } else PcAction(
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
        item { Pitch(report.home, report.away, bx, by, report.homeFormation, report.awayFormation, report.homeXI, report.awayXI) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (AC.pc7) {
                    PcButton("Salta", { minute = 90 })
                    PcButton("Nuova partita", onReset)
                } else {
                    PcAction(onClick = { minute = 90 }) { Text("Salta") }
                    PcAction(onClick = onReset) { Text("Nuova partita") }
                }
            }
        }
        if (minute >= 90) item {
            Text(
                "Tiri ${report.homeShots}-${report.awayShots} · Possesso ${report.homePossession}%-${100 - report.homePossession}%",
                color = AC.TextDim, style = MaterialTheme.typography.bodySmall
            )
        }
        if (minute >= 90 && report.playerRatings.isNotEmpty()) item {
            Pagelle(report)
        }
        if (AC.pc7) item {
            PaperPanel(Modifier.fillMaxWidth()) {
                seen.reversed().forEachIndexed { i, e ->
                    PaperRow(i) {
                        Text(
                            "${e.minute}'  ${e.text}",
                            color = pcEventColor(e.type),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else items(seen.reversed()) { e ->
            val c = when (e.type) {
                EventType.GOAL -> AC.Accent
                EventType.YELLOW -> AC.Warn
                EventType.SAVE -> AC.Accent2
                EventType.TACTIC -> AC.Ok
                else -> AC.TextDim
            }
            Text("${e.minute}'  ${e.text}", color = c, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Pagelle a fine partita: chi è sceso in campo, dalla migliore alla peggiore, per squadra. */
@Composable
private fun Pagelle(report: MatchReport) {
    Column(Modifier.fillMaxWidth()) {
        SectionTitle("Pagelle")
        PagelleTeam(report.home.shortName, report.playedPlayers.filter { it.id in report.homePlayed }, report.playerRatings)
        PagelleTeam(report.away.shortName, report.playedPlayers.filter { it.id in report.awayPlayed }, report.playerRatings)
    }
}

@Composable
private fun PagelleTeam(name: String, ps: List<Player>, ratings: Map<Long, Double>) {
    Text(name, style = MaterialTheme.typography.titleSmall)
    ps.sortedByDescending { ratings[it.id] ?: 6.0 }.forEach { p ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(p.name, style = MaterialTheme.typography.bodyMedium)
            Text("%.1f".format(ratings[p.id] ?: 6.0), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Colore del testo della cronaca nello stile PC 7 (su sfondo bianco). */
private fun pcEventColor(t: EventType): Color = when (t) {
    EventType.GOAL -> AC.ColEn
    EventType.YELLOW -> AC.ColOrange
    EventType.SAVE -> AC.ColBlue
    EventType.TACTIC -> AC.ColGreen
    else -> AC.PaperText
}

/** Scelta a pulsante (stile PC 7) oppure chip Material. */
@Composable
private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) {
    if (AC.pc7) PcButton(label, onClick, main = selected)
    else PcChip(selected = selected, onClick = onClick, label = { Text(label) })
}

/**
 * Giocatori in campo nell'ordine delle posizioni (1 portiere, poi difensori, centrocampisti, attaccanti).
 * Il primo valore è la posizione; il numero mostrato è quello di maglia, se assegnato.
 */
private fun numbered(xi: List<Player>): List<Pair<Int, Player>> {
    fun rank(role: String) = when (role) { "P" -> 0; "D" -> 1; "C" -> 2; else -> 3 }
    return xi.sortedWith(compareBy<Player>({ rank(it.role) }, { -it.overall }))
        .mapIndexed { i, p -> (i + 1) to p }
}

@Composable
fun Pitch(
    home: Team, away: Team, bx: Float, by: Float, hf: Formation, af: Formation,
    homeXI: List<Player> = emptyList(), awayXI: List<Player> = emptyList()
) {
    val homeColor = Color(home.colorPrimary)
    val awayColor = Color(if (home.colorPrimary == away.colorPrimary) away.colorSecondary else away.colorPrimary)
    val homePos = hf.coords()
    val awayPos = af.coords()
    val homeNums = numbered(homeXI)
    val awayNums = numbered(awayXI)
    val textMeasurer = rememberTextMeasurer()
    val numStyle = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
        fun drawNumber(n: Int, c: Offset) {
            val m = textMeasurer.measure(n.toString(), numStyle)
            drawText(textMeasurer, n.toString(), topLeft = Offset(c.x - m.size.width / 2f, c.y - m.size.height / 2f), style = numStyle)
        }
        homePos.forEachIndexed { i, (fx, fy) ->
            val c = Offset((fx + shift).coerceIn(0.02f, 0.98f) * w, fy * h)
            drawCircle(homeColor, r, c)
            homeNums.firstOrNull { it.first == i + 1 }?.let { (pos, p) -> drawNumber(if (p.numero > 0) p.numero else pos, c) }
        }
        awayPos.forEachIndexed { i, (fx, fy) ->
            val c = Offset(((1f - fx) + shift).coerceIn(0.02f, 0.98f) * w, fy * h)
            drawCircle(awayColor, r, c)
            awayNums.firstOrNull { it.first == i + 1 }?.let { (pos, p) -> drawNumber(if (p.numero > 0) p.numero else pos, c) }
        }
        drawCircle(Color.White, r * 0.8f, Offset(bx * w, by * h))
    }
}

/** Scelta della mentalità della tua squadra: vale per le partite e per le giornate di campionato. */
@Composable
fun MentalityPicker(current: Mentality, onPick: (Mentality) -> Unit) {
    SectionTitle("Mentalità")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Mentality.values().forEach { m ->
            Choice(m.label, m == current) { onPick(m) }
        }
    }
}

/** Pressing e linea difensiva della tua squadra. */
@Composable
fun TacticsPicker(press: Pressing, line: DefLine, onPress: (Pressing) -> Unit, onLine: (DefLine) -> Unit) {
    SectionTitle("Pressing")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Pressing.values().forEach { p -> PcChip(selected = p == press, onClick = { onPress(p) }, label = { Text(p.label) }) }
    }
    SectionTitle("Linea difensiva")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DefLine.values().forEach { l -> PcChip(selected = l == line, onClick = { onLine(l) }, label = { Text(l.label) }) }
    }
}

/** Allenamento settimanale della tua squadra: si applica dopo ogni partita giocata. */
@Composable
fun TrainingPicker(plan: TrainingPlan, onChange: (TrainingPlan) -> Unit) {
    SectionTitle("Allenamento")
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TrainFocus.values().forEach { f ->
            PcChip(selected = f == plan.focus, onClick = { onChange(plan.copy(focus = f)) }, label = { Text(f.label) })
        }
    }
    Text(plan.focus.info, style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
    if (plan.focus != TrainFocus.RIPOSO) {
        SectionTitle("Intensità")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TrainIntensity.values().forEach { i ->
                PcChip(selected = i == plan.intensity, onClick = { onChange(plan.copy(intensity = i)) }, label = { Text(i.label) })
            }
        }
        val txt = when (plan.intensity) {
            TrainIntensity.BASSA -> "Crescita lenta, condizione +4, infortuni −20%"
            TrainIntensity.MEDIA -> "Crescita normale, nessun effetto su condizione e infortuni"
            TrainIntensity.ALTA -> "Crescita rapida, condizione −4, infortuni +30%"
        }
        Text(txt, style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
    }
}
