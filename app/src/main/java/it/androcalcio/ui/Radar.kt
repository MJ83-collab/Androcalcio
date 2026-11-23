package it.androcalcio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Player
import it.androcalcio.ui.theme.AC
import kotlin.math.cos
import kotlin.math.sin

/** Radar dei sei parametri principali del giocatore (1..99), su un poligono a sei lati. */
@Composable
fun RadarGiocatore(p: Player) {
    val assi = listOf("VE" to p.ve, "RE" to p.re, "QU" to p.qu, "PA" to p.pa, "TI" to p.ti, "FI" to p.fi)
    val n = assi.size
    Canvas(Modifier.fillMaxWidth().height(190.dp)) {
        val c = Offset(size.width / 2, size.height / 2)
        val r = minOf(size.width, size.height) / 2.8f
        fun punto(i: Int, k: Float): Offset {
            val a = -Math.PI / 2 + 2 * Math.PI * i / n
            return Offset(c.x + (r * k * cos(a)).toFloat(), c.y + (r * k * sin(a)).toFloat())
        }
        for (anello in 1..4) {
            val path = Path()
            for (i in 0 until n) {
                val q = punto(i, anello / 4f)
                if (i == 0) path.moveTo(q.x, q.y) else path.lineTo(q.x, q.y)
            }
            path.close()
            drawPath(path, Color.Gray.copy(alpha = 0.45f), style = Stroke(width = 1f))
        }
        val area = Path()
        for (i in 0 until n) {
            val q = punto(i, assi[i].second.coerceIn(1, 99) / 99f)
            if (i == 0) area.moveTo(q.x, q.y) else area.lineTo(q.x, q.y)
        }
        area.close()
        drawPath(area, AC.ColBlue.copy(alpha = 0.35f))
        drawPath(area, AC.ColBlue, style = Stroke(width = 3f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        assi.forEach { (nome, v) -> Text("$nome $v", fontSize = 11.sp, color = AC.PaperText) }
    }
}
