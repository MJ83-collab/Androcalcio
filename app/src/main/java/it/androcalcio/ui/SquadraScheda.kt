package it.androcalcio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.unit.dp
import it.androcalcio.data.Team
import it.androcalcio.engine.InfoSquadra
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

/** Scheda di consultazione di una squadra: si apre toccando la squadra in lista. */
@Composable
fun SquadraSchedaDialog(t: Team, carica: suspend (Team) -> InfoSquadra, onClose: () -> Unit) {
    val info by produceState<InfoSquadra?>(null, t) { value = carica(t) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(t.name) },
        text = {
            val i = info
            if (i == null) {
                Text("Caricamento…")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${i.campionato} · ${t.nazione}", color = AC.TextDim)
                    Text("Aspettative: ${i.posizioneAttesa}", color = AC.Accent)
                    Text("Sponsor: ${i.sponsor} · Stadio ${i.stadio}")
                    Text("Finanze", style = MaterialTheme.typography.titleSmall)
                    Text("Budget mercato ${money(i.budget)} · Budget ingaggi ${money(i.budgetIngaggi)}")
                    Text("Monte ingaggi ${money(i.monteIngaggi)} l'anno")
                    Text("Rosa", style = MaterialTheme.typography.titleSmall)
                    Text("${i.rosa} giocatori · media ${"%.1f".format(i.media)}")
                    Text(if (i.ruoliCarenti.isEmpty()) "Nessun ruolo carente"
                         else "Ruoli carenti: ${i.ruoliCarenti.joinToString(", ")}")
                    Text("Capitano: ${i.capitano?.let { "${it.name} (${it.overall})" } ?: "—"}")
                    i.rivale?.let { Text("Derby con $it", color = AC.Danger) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Chiudi") } }
    )
}
