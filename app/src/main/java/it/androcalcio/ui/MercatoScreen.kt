package it.androcalcio.ui

import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import it.androcalcio.engine.FiltroMercato
import it.androcalcio.engine.TacticalRoles
import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.androcalcio.ui.components.HeadCell
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.PaperPanel
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.width
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.engine.Market
import it.androcalcio.engine.OfferResult
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.StatChip
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

private val roleFilters = listOf("%" to "Tutti", "P" to "P", "D" to "D", "C" to "C", "A" to "A")

private fun roleCol(role: String): Color = when (role) {
    "P" -> AC.Warn
    "D" -> AC.Accent2
    "C" -> AC.Ok
    else -> AC.Danger
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MercatoScreen(
    team: Team?,
    teams: List<Team>,
    mySquad: List<Player>,
    market: List<Player>,
    role: String,
    result: OfferResult?,
    onRole: (String) -> Unit,
    onOffer: (Player, Long, Long, Int) -> Unit,
    wageAsk: (Player, Int) -> Long = { _, _ -> 0L },
    wageRoom: Long = 0L,
    clubAccepted: Set<Long> = emptySet(),
    advice: (Player, Int) -> String = { _, _ -> "" },
    onSell: (Player) -> Unit,
    onDismiss: () -> Unit
) {
    if (team == null) return
    val byId = teams.associateBy { it.id }
    var tab by rememberSaveable { mutableStateOf(0) }
    var affordable by rememberSaveable { mutableStateOf(true) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    // Filtri liberi: intervalli scelti da te (età, media, contratto), ruolo preciso, prezzo massimo
    var eta by remember { mutableStateOf(10f..45f) }
    var media by remember { mutableStateOf(1f..99f) }
    var contr by remember { mutableStateOf(0f..5f) }
    var posFiltro by rememberSaveable { mutableStateOf("") }
    var prezzoMaxM by remember { mutableStateOf(0f) }        // milioni, 0 = nessun limite
    var onlyFree by rememberSaveable { mutableStateOf(false) }  // solo svincolati
    var target by remember { mutableStateOf<Player?>(null) }
    var amount by remember { mutableStateOf(0L) }
    var wageOffer by remember { mutableStateOf(0L) }
    var years by remember { mutableStateOf(3) }
    var sellTarget by remember { mutableStateOf<Player?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Mercato", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip("Budget", money(team.budget), Modifier.weight(1f))
            StatChip("Rosa", "${mySquad.size} / ${Market.MAX_SQUAD}", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PcChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Acquista") })
            PcChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Vendi") })
        }

        if (tab == 0) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(roleFilters) { (code, label) ->
                    PcChip(selected = role == code, onClick = { onRole(code) }, label = { Text(label) })
                }
                item {
                    PcChip(
                        selected = affordable, onClick = { affordable = !affordable },
                        label = { Text("Alla mia portata") }
                    )
                }
                item {
                    PcChip(selected = showFilters, onClick = { showFilters = !showFilters },
                        label = { Text(if (showFilters) "Filtri ▲" else "Filtri ▼") })
                }
            }
            if (showFilters) {
                Column(Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    Text("Età ${eta.start.toInt()}–${eta.endInclusive.toInt()} anni", color = AC.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    RangeSlider(value = eta, onValueChange = { eta = it }, valueRange = 10f..45f, steps = 34)
                    Text("Media ${media.start.toInt()}–${media.endInclusive.toInt()}", color = AC.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    RangeSlider(value = media, onValueChange = { media = it }, valueRange = 1f..99f, steps = 97)
                    Text("Contratto ${contr.start.toInt()}–${contr.endInclusive.toInt()} anni", color = AC.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    RangeSlider(value = contr, onValueChange = { contr = it }, valueRange = 0f..5f, steps = 4)
                    Text(if (prezzoMaxM <= 0f) "Prezzo massimo: nessun limite" else "Prezzo massimo ${prezzoMaxM.toInt()} M",
                        color = AC.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(value = prezzoMaxM, onValueChange = { prezzoMaxM = it }, valueRange = 0f..100f, steps = 99)
                    Text("Ruolo preciso", color = AC.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { PcChip(selected = posFiltro.isEmpty(), onClick = { posFiltro = "" }, label = { Text("Tutti") }) }
                        items(TacticalRoles.NAMES.keys.toList()) { k ->
                            PcChip(selected = posFiltro == k, onClick = { posFiltro = if (posFiltro == k) "" else k }, label = { Text(k) })
                        }
                        item { PcChip(selected = onlyFree, onClick = { onlyFree = !onlyFree }, label = { Text("Svincolati") }) }
                    }
                }
            }
            val filtro = FiltroMercato(
                eta.start.toInt(), eta.endInclusive.toInt(), media.start.toInt(), media.endInclusive.toInt(),
                contr.start.toInt(), contr.endInclusive.toInt(), posFiltro, (prezzoMaxM * 1_000_000).toLong(), onlyFree
            )
            val filtrati = market.filter { p -> filtro.accetta(p, Market.value(p), p.teamId == Market.SVINCOLATO_ID) }
            val shown = (if (affordable) filtrati.filter { Market.value(it) <= team.budget } else filtrati)
            MarketTable(shown, { byId[it.teamId]?.name ?: if (it.teamId == Market.SVINCOLATO_ID) "SVINCOLATO" else "" },
                { Market.value(it) }, Modifier.weight(1f)) {
                target = it; amount = Market.value(it); years = 3; wageOffer = wageAsk(it, 3)
            }
        } else {
            MarketTable(mySquad, { team.name }, { (Market.value(it) * 0.9).toLong() }, Modifier.weight(1f)) { sellTarget = it }
        }
    }

    target?.let { p ->
        val step = maxOf(10_000L, Market.value(p) / 20)
        val libero = p.teamId == Market.SVINCOLATO_ID
        val clubOk = p.id in clubAccepted
        AlertDialog(
            onDismissRequest = { target = null },
            title = { Text(p.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${byId[p.teamId]?.name ?: if (libero) "Svincolato" else ""} · ${p.age} anni · valore ${p.overall}")
                    Text("Valore di mercato ${money(Market.value(p))}", color = AC.TextDim)
                    if (!libero && !clubOk) {
                        Text("Prezzo al club (risposta alla prossima giornata)", color = AC.TextDim, fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PcAction(onClick = { amount = (amount - step).coerceAtLeast(step) }) { Text("−") }
                            Text(money(amount), fontWeight = FontWeight.Bold, color = AC.Accent)
                            PcAction(onClick = { amount += step }) { Text("+") }
                        }
                    } else {
                        Text(if (libero) "Svincolato: trattativa diretta col giocatore."
                             else "Il club ha accettato: ora trattate con il giocatore.",
                            color = AC.Accent, fontSize = 12.sp)
                    }
                    Text("Consiglio del vice: ${advice(p, years)}", color = AC.TextDim, fontSize = 11.sp)
                    val wStep = maxOf(10_000L, wageAsk(p, years) / 20)
                    Text("Ingaggio annuo (richiesta ~${money(wageAsk(p, years))}, margine ${money(wageRoom.coerceAtLeast(0))})",
                        color = AC.TextDim, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PcAction(onClick = { wageOffer = (wageOffer - wStep).coerceAtLeast(wStep) }) { Text("−") }
                        Text(money(wageOffer), fontWeight = FontWeight.Bold, color = AC.Accent)
                        PcAction(onClick = { wageOffer += wStep }) { Text("+") }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PcAction(onClick = { years = (years - 1).coerceAtLeast(1) }) { Text("−") }
                        Text("$years ${if (years == 1) "anno" else "anni"}", fontWeight = FontWeight.Bold, color = AC.Accent)
                        PcAction(onClick = { years = (years + 1).coerceAtMost(5) }) { Text("+") }
                    }
                }
            },
            confirmButton = {
                PcAction(
                    onClick = { onOffer(p, amount, wageOffer, years); target = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)
                ) { Text("Offri") }
            },
            dismissButton = { TextButton(onClick = { target = null }) { Text("Annulla") } }
        )
    }

    sellTarget?.let { p ->
        val price = (Market.value(p) * 0.9).toLong()
        AlertDialog(
            onDismissRequest = { sellTarget = null },
            title = { Text("Vendere ${p.name}?") },
            text = { Text("Un club offre ${money(price)}.") },
            confirmButton = { PcAction(onClick = { onSell(p); sellTarget = null }) { Text("Vendi") } },
            dismissButton = { TextButton(onClick = { sellTarget = null }) { Text("Annulla") } }
        )
    }

    result?.let { r ->
        when (r) {
            is OfferResult.Counter -> AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Controfferta") },
                text = { Text("Il club chiede ${money(r.price)} per ${r.player.name}.") },
                confirmButton = { PcAction(onClick = { onOffer(r.player, r.price, r.wage, r.years) }) { Text("Accetta") } },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Rifiuta") } }
            )
            is OfferResult.WageCounter -> AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Il giocatore chiede di più") },
                text = { Text("${r.player.name} accetta per ${money(r.wage)} l'anno (${r.years} anni).") },
                confirmButton = { PcAction(onClick = { onOffer(r.player, r.price, r.wage, r.years) }) { Text("Accetta") } },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Rifiuta") } }
            )
            is OfferResult.Pending -> AlertDialog(
                onDismissRequest = onDismiss, title = { Text("In attesa del club") }, text = { Text(r.msg) },
                confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
            )
            is OfferResult.Accepted -> AlertDialog(
                onDismissRequest = onDismiss, title = { Text("Fatto") }, text = { Text(r.msg) },
                confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
            )
            is OfferResult.Rejected -> AlertDialog(
                onDismissRequest = onDismiss, title = { Text("Rifiutata") }, text = { Text(r.msg) },
                confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
            )
        }
    }
}

/** Tabella su carta bianca: ruolo, giocatore con squadra, età, media e prezzo. */
@Composable
private fun MarketTable(
    ps: List<Player>, club: (Player) -> String, price: (Player) -> Long,
    modifier: Modifier = Modifier, onClick: (Player) -> Unit
) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            HeadCell("DEM", AC.Text, Modifier.width(34.dp))
            HeadCell("GIOCATORE", AC.Text, Modifier.weight(1f), TextAlign.Start)
            HeadCell("ETÀ", AC.Text, Modifier.width(30.dp))
            HeadCell("ME", AC.ColOrange, Modifier.width(30.dp))
            HeadCell("PREZZO", AC.Text, Modifier.width(66.dp), TextAlign.End)
        }
        LazyColumn(Modifier.fillMaxWidth()) {
            itemsIndexed(ps, key = { _, p -> p.id }) { _, p ->
                val n = (p.overall / 20).coerceIn(0, 5)
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp, horizontal = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color(0xFFF2F5FF), Color(0xFFB9C6E8))))
                        .border(1.dp, Color(0xFF5A6A9A), RoundedCornerShape(10.dp))
                        .clickable { onClick(p) }.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(p.name, Modifier.weight(1f).apriScheda(p), maxLines = 1, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = AC.PaperText)
                        Text("★".repeat(n) + "☆".repeat(5 - n), color = Color(0xFFE0A800), fontSize = 12.sp)
                        Text("${p.overall}", Modifier.width(30.dp), textAlign = TextAlign.End, fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold, color = AC.ColOrange)
                        Text(when (p.role) { "P" -> "POR"; "D" -> "DIF"; "C" -> "CEN"; else -> "ATT" }, Modifier.width(36.dp),
                            textAlign = TextAlign.End, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.PaperText)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(club(p).uppercase(), Modifier.weight(1f), maxLines = 1, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = AC.ColBlue)
                        Text("${p.age} anni · ", fontSize = 10.sp, color = AC.PaperText)
                        Text(money(price(p)), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.ColGreen)
                    }
                }
            }
        }
    }
}
