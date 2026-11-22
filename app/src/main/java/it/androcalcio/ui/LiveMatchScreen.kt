package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PaperRow
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

/** Pannello della partita dal vivo aperto dalla barra in basso. */
private enum class LivePanel { NESSUNO, TATTICA, STAT }

/** La tua partita dal vivo, in notturna: tabellone, possesso, campo, gol in evidenza e barra comandi. */
@Composable
fun LiveMatchScreen(info: AndrocalcioViewModel.LiveInfo, vm: AndrocalcioViewModel) {
    val m = info.m
    @Suppress("UNUSED_VARIABLE") val t = info.tick   // ricomposizione a ogni minuto
    val events = m.events.toList()
    val last = events.lastOrNull { it.type !in setOf(EventType.YELLOW, EventType.SUB, EventType.TACTIC) }
    val lastGoal = events.lastOrNull { it.type == EventType.GOAL }
    val bx by animateFloatAsState(last?.x ?: 0.5f, tween(300), label = "bx")
    val by by animateFloatAsState(last?.y ?: 0.5f, tween(300), label = "by")
    var subOpen by remember { mutableStateOf(false) }
    var panel by remember { mutableStateOf(LivePanel.NESSUNO) }
    val mySubs = if (info.userHome) m.subsH else m.subsA

    LazyColumn(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AC.Night, AC.NightHi))),
        contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Scoreboard(m.home, m.away, m.hg, m.ag, if (m.finished) "Finale" else "${m.minute}'" + if (info.paused) " · pausa" else "") }
        item { PossessoBar(m.possessoCasa) }
        if (lastGoal != null && m.minute - lastGoal.minute <= 3) item { GolBanner("GOL! " + lastGoal.text, lastGoal.minute) }
        item { Pitch(m.home, m.away, bx, by, m.homeF, m.awayF, m.xiH.toList(), m.xiA.toList()) }
        if (!m.finished) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NeonTile("Tattica", Icons.Filled.Edit, panel == LivePanel.TATTICA, Modifier.weight(1f)) {
                        panel = if (panel == LivePanel.TATTICA) LivePanel.NESSUNO else LivePanel.TATTICA
                    }
                    NeonTile("Cambio $mySubs/${MatchEngine.MAX_SUBS}", Icons.Filled.Refresh, false, Modifier.weight(1f),
                        enabled = mySubs < MatchEngine.MAX_SUBS) { vm.setLivePaused(true); subOpen = true }
                    NeonTile("Stat", Icons.Filled.List, panel == LivePanel.STAT, Modifier.weight(1f)) {
                        panel = if (panel == LivePanel.STAT) LivePanel.NESSUNO else LivePanel.STAT
                    }
                    NeonTile(if (info.paused) "Riprendi" else "Pausa", Icons.Filled.PlayArrow, info.paused, Modifier.weight(1f)) {
                        vm.setLivePaused(!info.paused)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("VELOCITÀ", color = AC.NavyText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    listOf(1, 2, 4, 20).forEach { x ->
                        PcChip(selected = info.speed == x, onClick = { vm.setLiveSpeed(x) }, label = { Text("×$x") })
                    }
                }
            }
            if (panel == LivePanel.TATTICA) {
                item { MentalityPicker(if (info.userHome) m.mH else m.mA) { vm.liveMentality(it) } }
                item {
                    Column {
                        TacticsPicker(
                            if (info.userHome) m.pressH else m.pressA, if (info.userHome) m.lineH else m.lineA,
                            { vm.livePressing(it) }, { vm.liveLine(it) }
                        )
                    }
                }
            }
            if (panel == LivePanel.STAT) item { StatPanel(m.tiriCasa, m.tiriOspiti, m.possessoCasa) }
            item { PcAction(onClick = { vm.liveSkip() }, modifier = Modifier.fillMaxWidth()) { Text("Termina partita") } }
        } else {
            item {
                PcAction(onClick = { vm.closeLive() }, modifier = Modifier.fillMaxWidth(), enabled = info.saved || info.league == null,
                    colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)) {
                    Text(if (info.league != null) "Torna al calendario" else "Chiudi")
                }
            }
            item { StatPanel(m.tiriCasa, m.tiriOspiti, m.possessoCasa) }
        }
        item { CronacaPanel(events.reversed()) }
    }

    if (subOpen) SubDialog(
        xi = if (info.userHome) m.xiH.toList() else m.xiA.toList(),
        bench = if (info.userHome) m.benchH.toList() else m.benchA.toList(),
        energy = { (m.energyOf(it) * 100).toInt() },
        onDone = { out, inP -> if (out != null && inP != null) vm.liveSub(out, inP); subOpen = false }
    )
}

/** Barra del possesso: ciano per la casa, magenta per gli ospiti. */
@Composable
private fun PossessoBar(casa: Int) {
    val c = casa.coerceIn(1, 99)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$c%", color = AC.Neon, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, modifier = Modifier.width(40.dp))
        Row(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))) {
            Box(Modifier.weight(c.toFloat()).fillMaxHeight().background(AC.Neon))
            Box(Modifier.weight((100 - c).toFloat()).fillMaxHeight().background(AC.NeonPink))
        }
        Text("${100 - c}%", color = AC.NeonPink, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp,
            textAlign = TextAlign.End, modifier = Modifier.width(40.dp))
    }
}

/** Fascia gialla del gol, come in TV. */
@Composable
private fun GolBanner(testo: String, minuto: Int) {
    Box(
        Modifier.fillMaxWidth().background(AC.GoalBanner, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("$testo ($minuto')", color = AC.Night, fontWeight = FontWeight.Black, fontSize = 15.sp,
            maxLines = 2, textAlign = TextAlign.Center)
    }
}

/** Pulsante della barra comandi: icona sopra, scritta sotto, bordo ciano se attivo. */
@Composable
private fun NeonTile(label: String, icon: ImageVector, on: Boolean, modifier: Modifier = Modifier,
                     enabled: Boolean = true, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    val col = if (!enabled) AC.TextDim else if (on) AC.Neon else Color.White
    Column(
        modifier.clip(shape).background(Brush.verticalGradient(listOf(AC.NightHi, AC.Night)))
            .border(1.5.dp, if (on) AC.Neon else AC.Neon.copy(alpha = 0.3f), shape)
            .clickable(enabled = enabled, onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = col)
        Text(label.uppercase(), color = col, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

/** Statistiche della partita su carta bianca. */
@Composable
private fun StatPanel(tiriCasa: Int, tiriOspiti: Int, possesso: Int) {
    PaperPanel(Modifier.fillMaxWidth()) {
        listOf("Tiri" to "$tiriCasa - $tiriOspiti", "Possesso" to "$possesso% - ${100 - possesso}%").forEachIndexed { i, (k, v) ->
            PaperRow(i) {
                Text(k, Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(v, color = AC.ColBlue, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
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
