package it.androcalcio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.engine.Formation
import it.androcalcio.ui.orderedXI
import it.androcalcio.ui.theme.AC

private val BluTv = Color(0xFF2347D6)
private val BluTvScuro = Color(0xFF1B38B0)
private val CianoTv = Color(0xFF7FE3F5)

/**
 * Tabellone televisivo: linguetta con la competizione, fascia blu con le due squadre, punteggio su parallelogramma
 * bianco e, sotto, il minuto. Disegnato con forme semplici: nessun logo reale.
 */
@Composable
fun TabelloneTV(
    home: Team, away: Team, hg: Int, ag: Int, tempo: String,
    titolo: String = "CALCIO", modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            Modifier.clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(BluTv)
                .padding(horizontal = 26.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(titolo.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1)
            Row(Modifier.width(56.dp).height(2.dp)) {
                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF2E9E4F)))
                Box(Modifier.weight(1f).fillMaxHeight().background(Color.White))
                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFD32F2F)))
            }
        }
        Row(Modifier.fillMaxWidth().height(64.dp).background(androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(Color(0xFF3A5BD9), BluTvScuro, Color(0xFF0B1240)))), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Crest(home, 50.dp)
                Text(home.name.uppercase(), Modifier.weight(1f).padding(horizontal = 6.dp), color = Color.White,
                    fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1, textAlign = TextAlign.Center)
            }
            Box(Modifier.width(112.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Canvas(Modifier.matchParentSize()) {
                    val sl = size.width * 0.12f
                    val p = Path().apply {
                        moveTo(sl, 0f); lineTo(size.width, 0f); lineTo(size.width - sl, size.height); lineTo(0f, size.height); close()
                    }
                    drawPath(p, Color.White)
                    drawLine(Color(0xFFD32F2F), Offset(sl, 0f), Offset(0f, size.height), strokeWidth = 6f)
                    drawLine(Color(0xFF111111), Offset(sl * 1.7f, 0f), Offset(sl * 0.7f, size.height), strokeWidth = 4f)
                    drawLine(Color(0xFF111111), Offset(size.width - sl * 0.3f, 0f), Offset(size.width - sl * 1.3f, size.height), strokeWidth = 4f)
                }
                Text("$hg - $ag", color = Color(0xFF111111), fontWeight = FontWeight.Black, fontSize = 30.sp, maxLines = 1)
            }
            Row(Modifier.weight(1f).padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(away.name.uppercase(), Modifier.weight(1f).padding(horizontal = 6.dp), color = Color.White,
                    fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1, textAlign = TextAlign.Center)
                Crest(away, 50.dp)
            }
        }
        Text(
            tempo,
            Modifier.clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)).background(CianoTv)
                .padding(horizontal = 24.dp, vertical = 2.dp),
            color = Color(0xFF0B1A3A), fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1
        )
    }
}

private fun cognome(nome: String): String = nome.trim().split(" ").lastOrNull().orEmpty()

/**
 * Formazione su campo verticale come nei tabelloni delle formazioni ufficiali: attacco in alto, portiere in basso,
 * ogni giocatore con il suo volto e il cognome in una targhetta.
 */
@Composable
fun FormazioneAvatar(
    xi: List<Player>, formation: Formation, modifier: Modifier = Modifier,
    squadra: Team? = null, titolo: String = "FORMAZIONE"
) {
    val ordered = orderedXI(xi, formation)
    val coords = formation.coords()
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().background(AC.Night).padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(titolo.uppercase() + " · ${formation.label}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
            if (squadra != null) Crest(squadra, 30.dp)
        }
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(0.82f).background(AC.PitchB)) {
            val w = maxWidth
            val h = maxHeight
            Canvas(Modifier.matchParentSize()) {
                // Campo in prospettiva come nei tabelloni delle formazioni ufficiali: più stretto in alto
                val W = size.width; val H = size.height
                drawRect(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF1B1F2A), Color(0xFF3B4152))))
                val inA = W * 0.14f; val y0 = H * 0.02f; val y1 = H * 0.98f
                fun x(fx: Float, fy: Float): Float { val m = inA * (1f - fy); return m + (W - 2 * m) * fx }
                fun pt(fx: Float, fy: Float) = Offset(x(fx, fy), y0 + (y1 - y0) * fy)
                val campo = Path().apply { moveTo(pt(0f, 0f).x, pt(0f, 0f).y); lineTo(pt(1f, 0f).x, pt(1f, 0f).y)
                    lineTo(pt(1f, 1f).x, pt(1f, 1f).y); lineTo(pt(0f, 1f).x, pt(0f, 1f).y); close() }
                drawPath(campo, AC.PitchB)
                for (k in 0 until 10 step 2) {
                    val a = k / 10f; val b = (k + 1) / 10f
                    drawPath(Path().apply { moveTo(pt(0f, a).x, pt(0f, a).y); lineTo(pt(1f, a).x, pt(1f, a).y)
                        lineTo(pt(1f, b).x, pt(1f, b).y); lineTo(pt(0f, b).x, pt(0f, b).y); close() }, AC.PitchA)
                }
                drawPath(campo, androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent, Color.White.copy(alpha = 0.08f))))
                val line = Color.White.copy(alpha = 0.85f); val st = Stroke(2.5f)
                drawPath(campo, line, style = st)
                drawLine(line, pt(0f, 0.5f), pt(1f, 0.5f), 2.5f)
                drawOval(line, Offset(W / 2 - W * 0.12f, pt(0.5f, 0.5f).y - H * 0.055f), Size(W * 0.24f, H * 0.11f), style = st)
                fun area(fa: Float, fb: Float, top: Boolean) {
                    val (ya, yb) = if (top) 0f to fb else (1f - fb) to 1f
                    drawPath(Path().apply { moveTo(pt(fa, ya).x, pt(fa, ya).y); lineTo(pt(1 - fa, ya).x, pt(1 - fa, ya).y)
                        lineTo(pt(1 - fa, yb).x, pt(1 - fa, yb).y); lineTo(pt(fa, yb).x, pt(fa, yb).y); close() }, line, style = st)
                }
                area(0.22f, 0.15f, true); area(0.36f, 0.06f, true); area(0.22f, 0.15f, false); area(0.36f, 0.06f, false)
            }
            val cardW = 70.dp
            ordered.forEachIndexed { i, p ->
                val c = coords.getOrNull(i) ?: return@forEachIndexed
                val norm = ((c.first - 0.05f) / 0.41f).coerceIn(0f, 1f)
                val fx = 0.12f + c.second * 0.76f
                val fy = 0.86f - norm * 0.72f
                Column(
                    Modifier.offset(x = w * fx - cardW / 2, y = h * fy - 28.dp).width(cardW),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Avatar(p.name, 40.dp)
                    Text(
                        cognome(p.name).uppercase(),
                        Modifier.clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.92f)).padding(horizontal = 4.dp),
                        color = Color(0xFF111111), fontWeight = FontWeight.Black, fontSize = 10.sp, maxLines = 1
                    )
                }
            }
        }
    }
}
