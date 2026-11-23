package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import androidx.compose.foundation.verticalScroll
import it.androcalcio.data.MatchResult
import it.androcalcio.engine.Friendlies
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.background
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

/**
 * Prossima partita: si gioca solo dal calendario. Mostra la sfida, il modulo e la mentalità da confermare
 * e il pulsante per giocarla in 2D (le altre partite della giornata si simulano da sole).
 */
@Composable
fun ProssimaPartitaScreen(
    team: Team?,
    teams: List<Team>,
    matches: List<MatchResult>,
    cupMatches: List<MatchResult>,
    friendlyMatches: List<MatchResult>,
    onPlay: () -> Unit,
    onCalendario: () -> Unit,
    mentality: Mentality = Mentality.EQUILIBRATA,
    onMentality: (Mentality) -> Unit = {},
    formation: Formation = Formation.F442,
    onFormation: (Formation) -> Unit = {}
) {
    if (team == null) return
    val tutte = matches + cupMatches
    val giornata = tutte.filter { !it.played }.minOfOrNull { it.round }
    val mia = giornata?.let { g ->
        tutte.firstOrNull { !it.played && it.round == g && (it.homeTeamId == team.id || it.awayTeamId == team.id) }
    }
    val avversario = mia?.let { m -> teams.firstOrNull { it.id == if (m.homeTeamId == team.id) m.awayTeamId else m.homeTeamId } }
    // Prima del campionato servono le amichevoli, che si giocano dal calendario
    val bloccoAmichevoli = matches.none { it.played } && !(friendlyMatches.size >= Friendlies.MIN && friendlyMatches.all { it.played })

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Prossima partita", style = MaterialTheme.typography.headlineSmall)
        when {
            bloccoAmichevoli -> {
                Text("Prima del campionato vanno giocate le amichevoli: le trovi nel calendario.", color = AC.Text)
                Azione("Vai al calendario", onCalendario)
            }
            giornata == null -> {
                Text("Non ci sono altre partite: la stagione è finita. Chiudila dal calendario.", color = AC.Text)
                Azione("Vai al calendario", onCalendario)
            }
            mia == null || avversario == null -> {
                Text("In questa giornata la tua squadra riposa.", color = AC.Text)
                Azione("Passa alla giornata successiva", onPlay)
            }
            else -> {
                ProssimaPartitaCard(team, avversario, mia, onPlay)
                SectionTitle("Il tuo modulo")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Formation.values().toList()) { f -> Choice(f.label, f == formation) { onFormation(f) } }
                }
                MentalityPicker(mentality, onMentality)
                Azione("Gioca in 2D", onPlay)
            }
        }
    }
}

@Composable
private fun Azione(testo: String, onClick: () -> Unit) {
    if (AC.pc7) PcButton(testo, onClick, Modifier.fillMaxWidth(), main = true)
    else PcAction(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)) { Text(testo) }
}

/** Colore del testo della cronaca nello stile PC 7 (su sfondo bianco). */
private fun pcEventColor(t: EventType): Color = when (t) {
    EventType.GOAL -> AC.ColEn
    EventType.YELLOW -> AC.ColOrange
    EventType.RED -> Color(0xFFC62828)
    EventType.INJURY -> AC.ColOrange
    EventType.SAVE -> AC.ColBlue
    EventType.TACTIC -> AC.ColGreen
    else -> AC.PaperText
}

/** Scelta a pulsante (stile PC 7) oppure chip Material. */
@Composable
private fun Choice(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (AC.pc7) PcButton(label, onClick, modifier, main = selected, compatto = true)
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
    // Stile notturno Androcalcio: prato a strisce, casa in ciano e ospiti in magenta con alone luminoso.
    val homeColor = AC.Neon
    val awayColor = AC.NeonPink
    val homePos = hf.coords()
    val awayPos = af.coords()
    val homeNums = numbered(homeXI)
    val awayNums = numbered(awayXI)
    val textMeasurer = rememberTextMeasurer()
    val numStyle = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
    Canvas(
        Modifier.fillMaxWidth().aspectRatio(1.6f)
            .border(2.dp, AC.Neon.copy(alpha = 0.35f), androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .padding(2.dp)
    ) {
        val w = size.width; val h = size.height
        val strisce = 12
        for (i in 0 until strisce) drawRect(if (i % 2 == 0) AC.PitchA else AC.PitchB,
            Offset(i * w / strisce, 0f), Size(w / strisce + 1f, h))
        val line = Color.White.copy(alpha = 0.75f)
        val st = Stroke(width = 2f)
        val m = h * 0.04f
        drawRect(line, Offset(m, m), Size(w - 2 * m, h - 2 * m), style = st)
        drawLine(line, Offset(w / 2, m), Offset(w / 2, h - m), 2f)
        drawCircle(line, h * 0.15f, Offset(w / 2, h / 2), style = st)
        drawCircle(line, 3f, Offset(w / 2, h / 2))
        drawRect(line, Offset(m, h * 0.22f), Size(w * 0.14f, h * 0.56f), style = st)
        drawRect(line, Offset(w - m - w * 0.14f, h * 0.22f), Size(w * 0.14f, h * 0.56f), style = st)
        drawRect(line, Offset(m, h * 0.38f), Size(w * 0.05f, h * 0.24f), style = st)
        drawRect(line, Offset(w - m - w * 0.05f, h * 0.38f), Size(w * 0.05f, h * 0.24f), style = st)
        val shift = (bx - 0.5f) * 0.2f
        val r = h * 0.038f
        fun drawNumber(n: Int, c: Offset) {
            val mm = textMeasurer.measure(n.toString(), numStyle)
            drawText(textMeasurer, n.toString(), topLeft = Offset(c.x - mm.size.width / 2f, c.y - mm.size.height / 2f), style = numStyle)
        }
        fun pallino(col: Color, c: Offset) {
            drawCircle(col.copy(alpha = 0.28f), r * 1.75f, c)
            drawCircle(AC.Night, r, c)
            drawCircle(col, r, c, style = Stroke(width = r * 0.32f))
        }
        homePos.forEachIndexed { i, (fx, fy) ->
            val c = Offset((fx + shift).coerceIn(0.03f, 0.97f) * w, fy * h)
            pallino(homeColor, c)
            homeNums.firstOrNull { it.first == i + 1 }?.let { (pos, p) -> drawNumber(if (p.numero > 0) p.numero else pos, c) }
        }
        awayPos.forEachIndexed { i, (fx, fy) ->
            val c = Offset(((1f - fx) + shift).coerceIn(0.03f, 0.97f) * w, fy * h)
            pallino(awayColor, c)
            awayNums.firstOrNull { it.first == i + 1 }?.let { (pos, p) -> drawNumber(if (p.numero > 0) p.numero else pos, c) }
        }
        val ball = Offset(bx * w, by * h)
        drawCircle(Color.White.copy(alpha = 0.3f), r * 1.2f, ball)
        drawCircle(Color.White, r * 0.7f, ball)
        drawCircle(AC.Night, r * 0.7f, ball, style = Stroke(width = 1.5f))
    }
}

/** Scelta della mentalità della tua squadra: vale per le partite e per le giornate di campionato. */
@Composable
fun MentalityPicker(current: Mentality, onPick: (Mentality) -> Unit) {
    SectionTitle("Mentalità")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Mentality.values().forEach { m ->
            Choice(m.label, m == current, Modifier.weight(1f)) { onPick(m) }
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

/** Tabellone notturno: stemmi e nomi ai lati, punteggio grande in ciano, minuto in una pillola luminosa. */
@Composable
fun Scoreboard(home: Team, away: Team, hg: Int, ag: Int, status: String) {
    it.androcalcio.ui.components.TabelloneTV(home, away, hg, ag, status)
}

/** Cronaca su carta bianca, dall'evento più recente, con il minuto in colonna. */
@Composable
fun CronacaPanel(events: List<it.androcalcio.engine.MatchEvent>) {
    PaperPanel(Modifier.fillMaxWidth()) {
        events.forEachIndexed { i, e ->
            PaperRow(i) {
                Text("${e.minute}'", Modifier.width(30.dp), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = AC.PaperText)
                Text(e.text, color = pcEventColor(e.type), fontSize = 12.sp,
                    fontWeight = if (e.type == EventType.GOAL) FontWeight.ExtraBold else FontWeight.Medium)
            }
        }
    }
}
