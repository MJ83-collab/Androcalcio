package it.androcalcio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import it.androcalcio.engine.FineStagione
import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.theme.AC

/** Fine stagione: classifica di ogni divisione della tua nazione, con promozioni, retrocessioni e coppe europee. */
@Composable
fun FineStagioneDialog(fine: FineStagione, onSalvaEsci: () -> Unit, onNuovo: () -> Unit, onChiudi: () -> Unit) {
    Dialog(onDismissRequest = onChiudi, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(AC.Bg).padding(16.dp)) {
            Text("Fine stagione ${fine.stagione}", style = MaterialTheme.typography.headlineSmall, color = AC.Text)
            Text("Classifiche finali, promozioni, retrocessioni e qualificazioni europee.",
                style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
            LazyColumn(Modifier.weight(1f).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                fine.divisioni.forEach { d ->
                    item {
                        Text(d.nome, Modifier.padding(top = 10.dp),
                            style = MaterialTheme.typography.titleMedium, color = AC.Text)
                    }
                    items(d.righe) { r ->
                        val esito = if (r.esito.isBlank()) "" else "  ·  ${r.esito}"
                        Text("${r.posizione}. ${r.squadra}  ${r.punti} pt$esito",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (r.mia) AC.Text else AC.TextDim,
                            fontWeight = if (r.mia) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            PcAction(onClick = onSalvaEsci, modifier = Modifier.fillMaxWidth()) { Text("Salva ed esci") }
            PcAction(onClick = onNuovo, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) { Text("Nuovo campionato") }
            TextButton(onClick = onChiudi, modifier = Modifier.fillMaxWidth()) { Text("Chiudi") }
        }
    }
}
