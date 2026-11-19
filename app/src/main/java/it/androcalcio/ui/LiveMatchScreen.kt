package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.data.Player
import it.androcalcio.engine.EventType
import it.androcalcio.engine.MatchEngine
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.theme.AC

/** La tua partita dal vivo: pausa, velocità, cambi, mentalità, salta alla fine. */
@Composable
fun LiveMatchScreen(info: AndrocalcioViewModel.LiveInfo, vm: AndrocalcioViewModel) {
    val m = info.m
    @Suppress("UNUSED_VARIABLE") val t = info.tick   // ricomposizione a ogni minuto
    val events = m.events.toList()
    val last = events.lastOrNull { it.type !in setOf(EventType.YELLOW, EventType.SUB, EventType.TACTIC) }
    val bx by animateFloatAsState(last?.x ?: 0.5f, tween(300), label = "bx")
    val by by animateFloatAsState(last?.y ?: 0.5f, tween(300), label = "by")
    var subOpen by remember { mutableStateOf(false) }
    val mySubs = if (info.userHome) m.subsH else m.subsA

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Crest(m.home, 44.dp)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${m.hg} - ${m.ag}", style = MaterialTheme.typography.headlineMedium)
                        Text(if (m.finished) "Finale" else "${m.minute}'" + if (info.paused) " · pausa" else "",
                            color = AC.Accent)
                    }
                    Crest(m.away, 44.dp)
                }
            }
        }
        item { Pitch(m.home, m.away, bx, by, m.homeF, m.awayF) }
        if (!m.finished) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PcAction(onClick = { vm.setLivePaused(!info.paused) },
                        colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)) {
                        Text(if (info.paused) "Riprendi" else "Pausa")
                    }
                    PcAction(onClick = { vm.setLivePaused(true); subOpen = true },
                        enabled = mySubs < MatchEngine.MAX_SUBS) {
                        Text("Cambi ($mySubs/${MatchEngine.MAX_SUBS})")
                    }
                    PcAction(onClick = { vm.liveSkip() }) { Text("Fine") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1, 2, 4, 20).forEach { x ->
                        PcChip(selected = info.speed == x, onClick = { vm.setLiveSpeed(x) }, label = { Text("×$x") })
                    }
                }
            }
            item { MentalityPicker(if (info.userHome) m.mH else m.mA) { vm.liveMentality(it) } }
            item {
                Column {
                    TacticsPicker(
                        if (info.userHome) m.pressH else m.pressA, if (info.userHome) m.lineH else m.lineA,
                        { vm.livePressing(it) }, { vm.liveLine(it) }
                    )
                }
            }
        } else {
            item {
                PcAction(onClick = { vm.closeLive() }, modifier = Modifier.fillMaxWidth(), enabled = info.saved || info.league == null,
                    colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)) {
                    Text(if (info.league != null) "Torna al calendario" else "Chiudi")
                }
            }
            item {
                val r = m.report()
                Text("Tiri ${r.homeShots}-${r.awayShots} · Possesso ${r.homePossession}%-${100 - r.homePossession}%",
                    color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
            }
        }
        items(events.reversed()) { e ->
            val c = when (e.type) {
                EventType.GOAL -> AC.Accent
                EventType.YELLOW -> AC.Warn
                EventType.SAVE -> AC.Accent2
                EventType.TACTIC, EventType.SUB -> AC.Ok
                else -> AC.TextDim
            }
            Text("${e.minute}'  ${e.text}", color = c, style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (subOpen) SubDialog(
        xi = if (info.userHome) m.xiH.toList() else m.xiA.toList(),
        bench = if (info.userHome) m.benchH.toList() else m.benchA.toList(),
        energy = { (m.energyOf(it) * 100).toInt() },
        onDone = { out, inP -> if (out != null && inP != null) vm.liveSub(out, inP); subOpen = false }
    )
}

/** Scelta del cambio: prima chi esce (con l'energia), poi chi entra dalla panchina. */
@Composable
private fun SubDialog(
    xi: List<Player>, bench: List<Player>, energy: (Player) -> Int,
    onDone: (Player?, Player?) -> Unit
) {
    var out by remember { mutableStateOf<Player?>(null) }
    var inP by remember { mutableStateOf<Player?>(null) }
    AlertDialog(
        onDismissRequest = { onDone(null, null) },
        title = { Text(if (out == null) "Chi esce?" else "Chi entra al posto di ${out!!.name}?") },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (out == null) items(xi) { p ->
                    val e = energy(p)
                    Text("${p.role}  ${p.name}   energia $e%",
                        Modifier.fillMaxWidth().clickable { out = p }.padding(vertical = 8.dp),
                        color = if (e < 85) AC.Danger else if (e < 93) AC.Warn else AC.Text)
                } else items(bench.sortedByDescending { it.role == out!!.role }) { p ->
                    Text("${p.role}  ${p.name}   LIV ${p.overall}",
                        Modifier.fillMaxWidth().clickable { inP = p }.padding(vertical = 8.dp),
                        color = if (p == inP) AC.Accent else if (p.role == out!!.role) AC.Text else AC.TextDim)
                }
            }
        },
        confirmButton = {
            PcAction(onClick = { onDone(out, inP) }, enabled = out != null && inP != null) { Text("Conferma cambio") }
        },
        dismissButton = {
            TextButton(onClick = { if (out != null) { out = null; inP = null } else onDone(null, null) }) {
                Text(if (out != null) "Indietro" else "Annulla")
            }
        }
    )
}
