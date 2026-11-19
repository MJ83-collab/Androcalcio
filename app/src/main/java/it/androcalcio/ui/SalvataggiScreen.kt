package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.androcalcio.data.SlotInfo
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.theme.AC

private enum class SlotAction { SALVA, CARICA, CANCELLA }

/** Tre slot di salvataggio: salva, carica (con riavvio dell'app) e cancella, sempre con conferma. */
@Composable
fun SalvataggiScreen(
    slots: List<SlotInfo>,
    teamName: String?,
    season: String,
    onSave: (Int) -> Unit,
    onLoad: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    var pending by remember { mutableStateOf<Pair<SlotAction, SlotInfo>?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Salvataggi", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Partita in corso: ${teamName ?: "-"}, stagione $season. " +
                "Ogni slot conserva tutto: rose, calendario, risultati, statistiche, staff, vivaio e infortuni.",
            color = AC.TextDim, style = MaterialTheme.typography.bodySmall
        )
        slots.forEach { s ->
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Slot ${s.n}", color = AC.Accent, style = MaterialTheme.typography.titleMedium)
                    if (s.empty) Text("Vuoto", color = AC.TextDim)
                    else Text("${s.teamName} · ${s.season}\nSalvato il ${s.date}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PcAction(onClick = {
                            if (s.empty) onSave(s.n) else pending = SlotAction.SALVA to s
                        }) { Text("Salva") }
                        if (!s.empty) {
                            PcAction(onClick = { pending = SlotAction.CARICA to s }) { Text("Carica") }
                            TextButton(onClick = { pending = SlotAction.CANCELLA to s }) {
                                Text("Cancella", color = AC.Danger)
                            }
                        }
                    }
                }
            }
        }
    }

    pending?.let { (action, s) ->
        val (title, body) = when (action) {
            SlotAction.SALVA -> "Sovrascrivere lo slot ${s.n}?" to
                "Il salvataggio di ${s.teamName} (${s.season}) verrà sostituito dalla partita in corso."
            SlotAction.CARICA -> "Caricare lo slot ${s.n}?" to
                "La partita in corso non salvata andrà persa. L'app si riavvierà da sola su ${s.teamName} (${s.season})."
            SlotAction.CANCELLA -> "Cancellare lo slot ${s.n}?" to
                "Il salvataggio di ${s.teamName} (${s.season}) verrà eliminato definitivamente."
        }
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                PcAction(onClick = {
                    when (action) {
                        SlotAction.SALVA -> onSave(s.n)
                        SlotAction.CARICA -> onLoad(s.n)
                        SlotAction.CANCELLA -> onDelete(s.n)
                    }
                    pending = null
                }) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Annulla") } }
        )
    }
}
