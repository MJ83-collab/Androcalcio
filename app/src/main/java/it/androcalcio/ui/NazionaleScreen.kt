package it.androcalcio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.item
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.AlboEntry
import it.androcalcio.data.Convocazione
import it.androcalcio.data.NazionaleStat
import it.androcalcio.ui.components.PcButton

/** Nazionali: tornei (Mondiali, Europei), squadre nazionali e convocazioni della stagione scelta. */
@Composable
fun NazionaleScreen(
    stagioni: List<String>,
    stagione: String,
    onStagione: (String) -> Unit,
    convocazioni: List<Convocazione>,
    nazionale: List<NazionaleStat>,
    albo: List<AlboEntry>
) {
    val conv = convocazioni.filter { it.season == stagione }
    val naz = nazionale.filter { it.season == stagione }
    val tornei = albo.filter { it.season == stagione && (it.competizione == "Mondiali" || it.competizione == "Europei") }
    val partite = conv.groupBy { Triple(it.competizione, it.round, it.risultato) }.keys.toList()

    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item { Text("NAZIONALI", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(stagioni) { s -> PcButton(s, { onStagione(s) }, main = s == stagione) }
            }
        }
        item { Text("TORNEI", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) }
        items(listOf("Mondiali", "Europei")) { nome ->
            val vincitore = tornei.firstOrNull { it.competizione == nome }?.vincitore
            Text("$nome: " + (vincitore?.let { "vince $it" } ?: "non ancora disputati"), fontSize = 13.sp)
        }
        item { Text("NAZIONALI DELLA STAGIONE", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) }
        if (naz.isEmpty()) item { Text("Nessuna convocazione in questa stagione.", fontSize = 12.sp) }
        items(naz.groupBy { it.nazione }.toList().sortedByDescending { (_, v) -> v.sumOf { it.gol } }) { (nazione, righe) ->
            val giocatori = righe.map { it.playerId }.distinct().size
            Text("$nazione · $giocatori convocati · ${righe.sumOf { it.presenze }} presenze · ${righe.sumOf { it.gol }} gol",
                fontSize = 13.sp)
        }
        item { Text("PARTITE E CONVOCAZIONI", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) }
        if (partite.isEmpty()) item { Text("Nessuna partita internazionale.", fontSize = 12.sp) }
        items(partite) { (competizione, giornata, risultato) ->
            Column(Modifier.fillMaxWidth()) {
                Text(risultato, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(if (giornata > 0) "$competizione · giornata $giornata" else competizione, fontSize = 11.sp)
            }
        }
    }
}
