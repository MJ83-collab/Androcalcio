package it.androcalcio.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import it.androcalcio.engine.LiveMatch
import it.androcalcio.engine.MatchEvent
import it.androcalcio.engine.etichettaMinuto
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin

/** Replay di un gol: la palla ripercorre l'azione con la traiettoria blu tratteggiata. */
@Composable
fun ReplayGol(e: MatchEvent, m: LiveMatch, onChiudi: () -> Unit) {
    val prog = remember(e) { Animatable(0f) }
    var giro by remember { mutableStateOf(0) }
    LaunchedEffect(e, giro) { prog.snapTo(0f); prog.animateTo(1f, tween(4000, easing = LinearEasing)) }
    val pts = remember(e) {
        val base = e.percorso.map { Offset(it.first, it.second) }
        if (base.size >= 2) base else listOf(Offset(0.5f, 0.5f)) + base + Offset(e.x, e.y)
    }
    val n = pts.size - 1
    val pos = prog.value * n
    val i = pos.toInt().coerceIn(0, n - 1)
    val f = (pos - i).coerceIn(0f, 1f)
    val a = pts[i]; val b = pts[i + 1]
    val bx = a.x + (b.x - a.x) * f
    val by = a.y + (b.y - a.y) * f
    val scia = pts.take(i + 1) + Offset(bx, by)
    val lungo = hypot(b.x - a.x, (b.y - a.y) * 0.55f) > 0.30f
    val quota = if (lungo) sin(PI * f).toFloat() else 0f
    AlertDialog(
        onDismissRequest = onChiudi,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.97f),
        title = { Text("Replay · ${etichettaMinuto(e.minute)}' ${e.text}", fontSize = 14.sp, maxLines = 2) },
        text = {
            MangaPitch(m.home, m.away, bx, by, m.homeF, m.awayF, m.xiH.toList(), m.xiA.toList(), e.home, scia, quota, prog.value < 1f)
        },
        confirmButton = { TextButton(onClick = onChiudi) { Text("Chiudi") } },
        dismissButton = { TextButton(onClick = { giro++ }) { Text("Rivedi") } }
    )
}

/** Mappa di calore di un giocatore: 8 colonne x 5 righe, più rosso = più tempo passato in quella zona. */
@Composable
fun MappaCalore(nome: String, calore: IntArray?, casa: Boolean, onChiudi: () -> Unit) {
    AlertDialog(
        onDismissRequest = onChiudi,
        title = { Text("Mappa di calore · $nome", fontSize = 14.sp) },
        text = {
            Column {
                Canvas(Modifier.fillMaxWidth().aspectRatio(1.45f)) {
                    drawRect(Color(0xFF2F8A3E))
                    val mx = (calore?.maxOrNull() ?: 0).coerceAtLeast(1)
                    val cw = size.width / 8f; val ch = size.height / 5f
                    for (r in 0 until 5) for (c in 0 until 8) {
                        val v = calore?.getOrNull(r * 8 + c) ?: 0
                        if (v > 0) drawRect(Color(0xFFFF2D2D).copy(alpha = 0.15f + 0.7f * v / mx), Offset(c * cw, r * ch), androidx.compose.ui.geometry.Size(cw, ch))
                    }
                    val l = Color.White.copy(alpha = 0.8f)
                    drawLine(l, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 2f)
                    drawRect(l, Offset(1f, 1f), androidx.compose.ui.geometry.Size(size.width - 2f, size.height - 2f), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                }
                Text(if (casa) "La squadra di casa attacca verso destra." else "La squadra ospite attacca verso sinistra.", fontSize = 11.sp)
            }
        },
        confirmButton = { TextButton(onClick = onChiudi) { Text("Chiudi") } }
    )
}
