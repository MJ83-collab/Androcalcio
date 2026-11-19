package it.androcalcio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.engine.AssistantCoach
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.theme.AC

/** Stelle della media squadra: una stella ogni 20 punti, con la mezza stella. */
fun stelle(media: Int): String {
    val mezze = (media / 10.0).toInt().coerceIn(0, 10)
    return "★".repeat(mezze / 2) + (if (mezze % 2 == 1) "½" else "") + "☆".repeat(5 - (mezze + 1) / 2)
}

/** Schermata Avversario: undici previsto, schema abituale, media squadra e consigli dell'assistente. */
@Composable
fun AvversarioScreen(advice: AssistantCoach.Advice?) {
    LazyColumn(contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        val a = advice
        if (a == null || a.opponent == null) {
            item {
                Text("Nessuna prossima partita di campionato: non c'è un avversario da analizzare.",
                    color = AC.TextDim, style = MaterialTheme.typography.bodyMedium)
            }
            return@LazyColumn
        }
        item {
            PaperPanel(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(a.opponent, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AC.PaperText)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("MEDIA SQUADRA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.ColBlue)
                        Text(stelle(a.opponentMedia), fontSize = 16.sp, color = AC.ColOrange)
                        Text("${a.opponentMedia}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AC.PaperText)
                    }
                    a.opponentSchema?.let {
                        Text("Schema abituale: ${it.label}", fontSize = 12.sp, color = AC.PaperText)
                    }
                    a.assistantName?.let { Text("Assistente: $it", fontSize = 12.sp, color = AC.ColBlue) }
                }
            }
        }
        if (a.opponentXI.isNotEmpty()) {
            item { LineupTable(orderedXI(a.opponentXI)) }
            a.opponentSchema?.let { f -> item { FormationBoard(a.opponentXI, f, androidx.compose.ui.graphics.Color(0xFF1E3FA0)) } }
        }
        item {
            SectionTitle("Notizie dell'allenatore in seconda")
            if (a.assistantName == null) {
                Text("Assumi un allenatore in seconda dallo staff per avere le notizie sull'avversario.",
                    color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
            }
            a.opponentNotes.forEach {
                Text("• $it", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
            }
            a.tatticaNota?.let { Text("• $it", color = AC.Accent, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
