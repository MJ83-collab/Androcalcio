package it.androcalcio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import it.androcalcio.engine.Difficolta
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC
import it.androcalcio.ui.theme.PcTitle

/** Testata della home: logo Androcalcio in giallo su notte, stagione e budget in una pillola. */
@Composable
fun HomeBanner(team: Team, season: String) {
    val shape = RoundedCornerShape(4.dp)
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(AC.NavyTop, AC.Night)), shape)
            .border(2.dp, AC.NavyBorder, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text("ANDROCALCIO", color = AC.Accent, fontFamily = PcTitle, fontSize = 30.sp, letterSpacing = 1.sp,
            style = MaterialTheme.typography.headlineMedium.copy(shadow = Shadow(Color.Black, Offset(2f, 3f), 4f)))
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (season.isEmpty()) team.campionato else "Stagione $season · ${team.campionato}",
                Modifier.weight(1f), color = AC.NavyText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(money(team.budget), color = AC.Night, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.background(AC.Accent, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 3.dp))
        }
    }
}

/** Prossima partita in stile notturno: casa in ciano, ospiti in magenta; tocco = vai alla partita. */
@Composable
fun ProssimaPartitaCard(me: Team, avversario: Team, m: MatchResult, onGioca: () -> Unit) {
    val home = if (m.homeTeamId == me.id) me else avversario
    val away = if (m.homeTeamId == me.id) avversario else me
    val shape = RoundedCornerShape(10.dp)
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(AC.NightHi, AC.Night)), shape)
            .border(1.5.dp, AC.Neon.copy(alpha = 0.6f), shape)
            .clickable(onClick = onGioca).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PROSSIMA PARTITA · GIORNATA ${m.round}", color = AC.Neon, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Crest(home, 44.dp)
                Text(home.name, color = AC.Neon, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, textAlign = TextAlign.Center)
            }
            Text("VS", color = Color.White, fontFamily = PcTitle, fontSize = 20.sp)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Crest(away, 44.dp)
                Text(away.name, color = AC.NeonPink, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, textAlign = TextAlign.Center)
            }
        }
        Text("Tocca per andare alla partita", color = AC.TextDim, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

/** Opzioni come in PC Calcio 7: difficoltà (vale per la prossima carriera) e velocità della partita dal vivo. */
@Composable
fun OpzioniScreen(op: AndrocalcioViewModel.Opzioni, onDifficolta: (Int) -> Unit, onVelocita: (Int) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionTitle("Difficoltà")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Difficolta.NOMI.forEachIndexed { i, nome ->
                PcChip(selected = op.difficolta == i, onClick = { onDifficolta(i) }, label = { Text(nome) })
            }
        }
        DifficoltaBarra(op.difficolta)
        SectionTitle("Velocità partita dal vivo")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 2, 4, 20).forEach { x -> PcChip(selected = op.velocita == x, onClick = { onVelocita(x) }, label = { Text("×$x") }) }
        }
        SectionTitle("Informazione")
        PaperPanel(Modifier.fillMaxWidth()) {
            listOf(
                "Difficoltà" to Difficolta.descrizione(op.difficolta),
                "Quando vale" to "Dalla prossima nuova carriera (scelta della squadra).",
                "Velocità" to "Velocità di partenza delle partite dal vivo; durante la gara puoi cambiarla."
            ).forEachIndexed { i, (k, v) ->
                PaperRow(i) {
                    Text(k, Modifier.padding(end = 8.dp), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Text(v, Modifier.weight(1f), color = AC.ColBlue, fontSize = 12.sp)
                }
            }
        }
    }
}

/** Barra a tacche verde-rossa come nella schermata opzioni originale. */
@Composable
private fun DifficoltaBarra(livello: Int) {
    val tacche = 12
    val accese = (livello + 1) * tacche / Difficolta.NOMI.size
    Row(Modifier.fillMaxWidth().background(AC.Night, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (i in 0 until tacche) {
            val col = if (i < tacche / 2) AC.Ok else if (i < tacche * 5 / 6) AC.Warn else AC.Danger
            Text("█", color = if (i < accese) col else col.copy(alpha = 0.2f), fontSize = 12.sp)
        }
    }
}
