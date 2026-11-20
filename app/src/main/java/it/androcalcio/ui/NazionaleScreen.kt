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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.AlboEntry
import it.androcalcio.data.Convocazione
import it.androcalcio.data.NazionaleStat
import it.androcalcio.data.Player
import it.androcalcio.engine.Market
import it.androcalcio.engine.Nazionali
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.money

/** Nazionali: offerta, rosa della nazionale allenata, tornei, nazionali e convocazioni della stagione scelta. */
@Composable
fun NazionaleScreen(
    stagioni: List<String>,
    stagione: String,
    onStagione: (String) -> Unit,
    convocazioni: List<Convocazione>,
    nazionale: List<NazionaleStat>,
    albo: List<AlboEntry>,
    mia: String,
    offerta: String,
    candidati: List<Player>,
    rosa: List<Player>,
    onAggiungi: (Player) -> Unit,
    onTogli: (Player) -> Unit,
    onRispondi: (Boolean) -> Unit
) {
    val conv = convocazioni.filter { it.season == stagione }
    val naz = nazionale.filter { it.season == stagione }
    val tornei = albo.filter { it.season == stagione && (it.competizione == "Mondiali" || it.competizione == "Europei") }
    val partite = conv.groupBy { Triple(it.competizione, it.round, it.risultato) }.keys.toList()
    var ruolo by rememberSaveable { mutableStateOf("Tutti") }
    var ordine by rememberSaveable { mutableStateOf("Media") }
    val rosaIds = rosa.map { it.id }.toSet()
    val filtrati = candidati
        .filter { ruolo == "Tutti" || it.role == ruolo }
        .let { l -> if (ordine == "Valore") l.sortedByDescending { Market.value(it) } else l.sortedByDescending { it.overall } }

    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item { Text("NAZIONALI", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(stagioni) { s -> PcButton(s, { onStagione(s) }, main = s == stagione) }
            }
        }
        if (mia.isEmpty() && offerta.isNotEmpty()) {
            item {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("OFFERTA DALLA NAZIONALE", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Text("La federazione di $offerta ti vuole come ct.", fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PcButton("Accetta", { onRispondi(true) }, Modifier.weight(1f), main = true)
                        PcButton("Rifiuta", { onRispondi(false) }, Modifier.weight(1f))
                    }
                }
            }
        }
        if (mia.isNotEmpty()) {
            item { Text("CT DI ${mia.uppercase()} · rosa ${rosa.size}/${Nazionali.CONVOCATI}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Tutti", "P", "D", "C", "A")) { r -> PcButton(r, { ruolo = r }, main = r == ruolo) }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PcButton("Ordina per media", { ordine = "Media" }, main = ordine == "Media")
                    PcButton("Ordina per valore", { ordine = "Valore" }, main = ordine == "Valore")
                }
            }
            items(filtrati) { p ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${p.name} (${p.role}) · media ${p.overall} · ${money(Market.value(p))}",
                        Modifier.weight(1f), fontSize = 12.sp)
                    if (p.id in rosaIds) PcButton("Togli", { onTogli(p) })
                    else PcButton("Aggiungi", { onAggiungi(p) })
                }
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
