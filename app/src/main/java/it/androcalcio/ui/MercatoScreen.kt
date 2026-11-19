package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.foundation.background
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
    onOffer: (Player, Long) -> Unit,
    onSell: (Player) -> Unit,
    onDismiss: () -> Unit
) {
    if (team == null) return
    val byId = teams.associateBy { it.id }
    var tab by rememberSaveable { mutableStateOf(0) }
    var affordable by rememberSaveable { mutableStateOf(true) }
    var target by remember { mutableStateOf<Player?>(null) }
    var amount by remember { mutableStateOf(0L) }
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
            }
            val shown = if (affordable) market.filter { Market.value(it) <= team.budget } else market
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shown, key = { it.id }) { p ->
                    PlayerRow(p, byId[p.teamId]?.name ?: "", Market.value(p), Modifier.clickable {
                        target = p; amount = Market.value(p)
                    })
                }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mySquad, key = { it.id }) { p ->
                    PlayerRow(p, team.name, (Market.value(p) * 0.9).toLong(),
                        Modifier.clickable { sellTarget = p })
                }
            }
        }
    }

    target?.let { p ->
        val step = maxOf(10_000L, Market.value(p) / 20)
        AlertDialog(
            onDismissRequest = { target = null },
            title = { Text(p.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${byId[p.teamId]?.name ?: ""} · ${p.age} anni · valore ${p.overall}")
                    Text("Valore di mercato ${money(Market.value(p))}", color = AC.TextDim)
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PcAction(onClick = { amount = (amount - step).coerceAtLeast(step) }) { Text("−") }
                        Text(money(amount), fontWeight = FontWeight.Bold, color = AC.Accent)
                        PcAction(onClick = { amount += step }) { Text("+") }
                    }
                }
            },
            confirmButton = {
                PcAction(
                    onClick = { onOffer(p, amount); target = null },
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
                confirmButton = { PcAction(onClick = { onOffer(r.player, r.price) }) { Text("Accetta") } },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Rifiuta") } }
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

@Composable
private fun PlayerRow(p: Player, club: String, price: Long, modifier: Modifier = Modifier) {
    GlassCard(modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                    .background(roleCol(p.role).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text(p.role, color = roleCol(p.role), fontWeight = FontWeight.Bold) }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(p.name, style = MaterialTheme.typography.titleSmall)
                Text("$club · ${p.age} anni · ${p.nationality}",
                    style = MaterialTheme.typography.bodySmall, color = AC.TextDim, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${p.overall}", style = MaterialTheme.typography.titleMedium, color = AC.Accent)
                Text(money(price), style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
            }
        }
    }
}
