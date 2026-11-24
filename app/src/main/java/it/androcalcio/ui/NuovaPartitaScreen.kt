package it.androcalcio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import it.androcalcio.AndrocalcioViewModel.TeamInfo
import it.androcalcio.data.Team
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

/** Inizio di una nuova partita: nazione, poi campionato, poi squadra (con forza, budget e ingaggi). */
@Composable
fun NuovaPartitaScreen(teams: List<Team>, info: Map<Long, TeamInfo>, onStart: (Team, String, String) -> Unit) {
    val nations = teams.map { it.nazione }.distinct()
    var nation by rememberSaveable { mutableStateOf("") }
    if (nations.isEmpty()) return   // dati non ancora caricati
    val nat = if (nation in nations) nation else nations.first()
    val leagues = teams.filter { it.nazione == nat }.map { it.campionato }.distinct()
    var league by rememberSaveable { mutableStateOf("") }
    val lg = if (league in leagues) league else leagues.first()
    val shown = teams.filter { it.nazione == nat && it.campionato == lg }
    var pick by remember { mutableStateOf<Team?>(null) }
    var nomeAll by rememberSaveable { mutableStateOf("") }
    var cognomeAll by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize().fondoApp().statusBarsPadding()) {
        Text("Nuova partita", Modifier.padding(start = 16.dp, top = 16.dp),
            style = MaterialTheme.typography.headlineSmall, color = AC.Text)
        Text("1. Nazione  ·  2. Campionato  ·  3. Squadra", Modifier.padding(start = 16.dp, top = 2.dp),
            style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(nations) { n -> PcChip(selected = n == nat, onClick = { nation = n; league = "" }, label = { Text(n) }) }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(leagues) { l -> PcChip(selected = l == lg, onClick = { league = l }, label = { Text(l) }) }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(shown, key = { it.id }) { t ->
                val i = info[t.id]
                GlassCard(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable { pick = t }) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Crest(t, size = 48.dp)
                        Column(Modifier.padding(start = 14.dp)) {
                            Text(t.name, style = MaterialTheme.typography.titleMedium, color = AC.Text)
                            Text("Forza ${i?.strength ?: "-"}  ·  Stadio ${t.stadiumCapacity}",
                                style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
                            Text("Budget mercato ${money(t.budget)}",
                                style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
                            Text("Monte ingaggi ${money(t.wageBudget)} (rosa ${money(i?.wages ?: 0L)})",
                                style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
                        }
                    }
                }
            }
        }
    }
    pick?.let { t ->
        AlertDialog(
            onDismissRequest = { pick = null },
            title = { Text("Allenatore della ${t.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inserisci nome e cognome dell'allenatore.")
                    OutlinedTextField(value = nomeAll, onValueChange = { nomeAll = it },
                        label = { Text("Nome") }, singleLine = true)
                    OutlinedTextField(value = cognomeAll, onValueChange = { cognomeAll = it },
                        label = { Text("Cognome") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(enabled = nomeAll.isNotBlank() && cognomeAll.isNotBlank(),
                    onClick = { onStart(t, nomeAll.trim(), cognomeAll.trim()) }) { Text("Confermo") }
            },
            dismissButton = { TextButton(onClick = { pick = null }) { Text("Annulla") } }
        )
    }
}
