package it.androcalcio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.androcalcio.engine.AssistantCoach
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.theme.AC

/** Consigli dell'allenatore in seconda: undici in forma e notizie sul prossimo avversario. */
@Composable
fun AssistantCard(advice: AssistantCoach.Advice?, onApply: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionTitle("Allenatore in seconda")
            val a = advice
            if (a == null || a.assistantName == null) {
                Text(
                    "Non hai un allenatore in seconda. Assumilo dallo staff per avere consigli su undici e avversario.",
                    color = AC.TextDim, style = MaterialTheme.typography.bodySmall
                )
                return@Column
            }
            Text(a.assistantName, style = MaterialTheme.typography.titleSmall)
            SectionTitle("Undici consigliato")
            Text(a.xi.joinToString(" · ") { it.name }, style = MaterialTheme.typography.bodySmall)
            a.xiNotes.forEach {
                Text("• $it", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
            }
            PcAction(onClick = onApply) { Text("Applica l'undici consigliato") }
            SectionTitle("Prossimo avversario: ${a.opponent ?: "nessuno in campionato"}")
            a.opponentNotes.forEach {
                Text("• $it", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
