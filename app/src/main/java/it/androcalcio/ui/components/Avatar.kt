package it.androcalcio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Avatar a fumetto in stile manga d'azione: contorni neri spessi, colori piatti, giacca e cravatta.
 * Generato dal nome (stesso nome = stessa faccia): capelli, occhi/occhiali, sopracciglia e bocca cambiano.
 */
@Composable
fun Avatar(nome: String, size: Dp = 48.dp, modifier: Modifier = Modifier) {
    val h = Math.floorMod(nome.hashCode(), 1_000_003)
    val sfondo = listOf(0xFFFFD54F, 0xFF4FC3F7, 0xFFFF8A80, 0xFFAED581, 0xFFCE93D8, 0xFFFFAB91)[h % 6]
    val pelle = listOf(0xFFFFE0C2, 0xFFF1C27D, 0xFFC68642, 0xFF8D5524)[(h / 6) % 4]
    val capelli = listOf(0xFF1B1B1B, 0xFF4E342E, 0xFFE0C068, 0xFFB0B0B0, 0xFF8D2A1A)[(h / 24) % 5]
    val taglio = (h / 120) % 5
    val occhi = (h / 600) % 4
    val bocca = (h / 2400) % 3
    val giacca = listOf(0xFF1F2333, 0xFF2E3B55, 0xFF3A2A2A)[(h / 7200) % 3]
    Canvas(modifier.size(size)) {
        val s = this.size.width
        val ink = Color(0xFF111111)
        val linea = Stroke(width = s * 0.045f, cap = StrokeCap.Round)
        drawCircle(Color(sfondo), s / 2)
        // Spalle: giacca, camicia bianca e cravatta
        val spalle = Path().apply {
            moveTo(s * 0.10f, s); quadraticBezierTo(s * 0.14f, s * 0.74f, s * 0.5f, s * 0.72f)
            quadraticBezierTo(s * 0.86f, s * 0.74f, s * 0.90f, s); close()
        }
        drawPath(spalle, Color(giacca)); drawPath(spalle, ink, style = linea)
        val camicia = Path().apply { moveTo(s * 0.40f, s * 0.73f); lineTo(s * 0.5f, s * 0.92f); lineTo(s * 0.60f, s * 0.73f); close() }
        drawPath(camicia, Color.White); drawPath(camicia, ink, style = Stroke(width = s * 0.025f))
        drawLine(Color(0xFFC62828), Offset(s * 0.5f, s * 0.76f), Offset(s * 0.5f, s * 0.92f), strokeWidth = s * 0.05f)
        // Testa
        drawOval(Color(pelle), Offset(s * 0.29f, s * 0.18f), Size(s * 0.42f, s * 0.52f))
        drawOval(ink, Offset(s * 0.29f, s * 0.18f), Size(s * 0.42f, s * 0.52f), style = linea)
        // Capelli
        val c = Color(capelli)
        when (taglio) {
            0 -> { // pettinati di lato
                val p = Path().apply {
                    moveTo(s * 0.27f, s * 0.36f); quadraticBezierTo(s * 0.30f, s * 0.12f, s * 0.55f, s * 0.13f)
                    quadraticBezierTo(s * 0.76f, s * 0.15f, s * 0.73f, s * 0.36f); quadraticBezierTo(s * 0.55f, s * 0.22f, s * 0.27f, s * 0.36f); close()
                }
                drawPath(p, c); drawPath(p, ink, style = Stroke(width = s * 0.03f))
            }
            1 -> { // a punte
                val p = Path().apply {
                    moveTo(s * 0.27f, s * 0.34f)
                    for (i in 0..5) { lineTo(s * (0.29f + i * 0.08f), s * 0.06f + (i % 2) * s * 0.06f); lineTo(s * (0.33f + i * 0.08f), s * 0.22f) }
                    lineTo(s * 0.73f, s * 0.34f); quadraticBezierTo(s * 0.5f, s * 0.22f, s * 0.27f, s * 0.34f); close()
                }
                drawPath(p, c); drawPath(p, ink, style = Stroke(width = s * 0.03f))
            }
            2 -> drawArc(c, 180f, 180f, true, Offset(s * 0.29f, s * 0.17f), Size(s * 0.42f, s * 0.26f)) // rasati
            3 -> { // pelato con lucido
                drawArc(ink, 200f, 50f, false, Offset(s * 0.36f, s * 0.22f), Size(s * 0.14f, s * 0.10f), style = Stroke(width = s * 0.02f))
            }
            else -> { // frangia lunga
                val p = Path().apply {
                    moveTo(s * 0.26f, s * 0.48f); quadraticBezierTo(s * 0.24f, s * 0.12f, s * 0.5f, s * 0.12f)
                    quadraticBezierTo(s * 0.78f, s * 0.12f, s * 0.75f, s * 0.48f); lineTo(s * 0.66f, s * 0.30f)
                    lineTo(s * 0.56f, s * 0.36f); lineTo(s * 0.46f, s * 0.29f); lineTo(s * 0.34f, s * 0.36f); close()
                }
                drawPath(p, c); drawPath(p, ink, style = Stroke(width = s * 0.03f))
            }
        }
        // Sopracciglia decise
        drawLine(ink, Offset(s * 0.36f, s * 0.37f), Offset(s * 0.46f, s * 0.395f), strokeWidth = s * 0.035f, cap = StrokeCap.Round)
        drawLine(ink, Offset(s * 0.64f, s * 0.37f), Offset(s * 0.54f, s * 0.395f), strokeWidth = s * 0.035f, cap = StrokeCap.Round)
        // Occhi
        val ys = s * 0.45f
        when (occhi) {
            0 -> { drawCircle(ink, s * 0.025f, Offset(s * 0.41f, ys)); drawCircle(ink, s * 0.025f, Offset(s * 0.59f, ys)) }
            1 -> { // occhiali da sole
                drawRoundRect(ink, Offset(s * 0.33f, ys - s * 0.045f), Size(s * 0.15f, s * 0.08f),
                    androidx.compose.ui.geometry.CornerRadius(s * 0.03f))
                drawRoundRect(ink, Offset(s * 0.52f, ys - s * 0.045f), Size(s * 0.15f, s * 0.08f),
                    androidx.compose.ui.geometry.CornerRadius(s * 0.03f))
                drawLine(ink, Offset(s * 0.48f, ys - s * 0.02f), Offset(s * 0.52f, ys - s * 0.02f), strokeWidth = s * 0.02f)
            }
            2 -> { // occhiali tondi
                drawCircle(ink, s * 0.06f, Offset(s * 0.41f, ys), style = Stroke(width = s * 0.022f))
                drawCircle(ink, s * 0.06f, Offset(s * 0.59f, ys), style = Stroke(width = s * 0.022f))
                drawCircle(ink, s * 0.018f, Offset(s * 0.41f, ys)); drawCircle(ink, s * 0.018f, Offset(s * 0.59f, ys))
            }
            else -> { // sguardo socchiuso
                drawLine(ink, Offset(s * 0.37f, ys), Offset(s * 0.45f, ys), strokeWidth = s * 0.025f, cap = StrokeCap.Round)
                drawLine(ink, Offset(s * 0.55f, ys), Offset(s * 0.63f, ys), strokeWidth = s * 0.025f, cap = StrokeCap.Round)
            }
        }
        // Bocca
        when (bocca) {
            0 -> drawLine(ink, Offset(s * 0.44f, s * 0.58f), Offset(s * 0.56f, s * 0.58f), strokeWidth = s * 0.025f, cap = StrokeCap.Round)
            1 -> drawArc(ink, 20f, 140f, false, Offset(s * 0.43f, s * 0.52f), Size(s * 0.14f, s * 0.08f),
                style = Stroke(width = s * 0.025f, cap = StrokeCap.Round))
            else -> drawLine(ink, Offset(s * 0.44f, s * 0.59f), Offset(s * 0.57f, s * 0.565f), strokeWidth = s * 0.025f, cap = StrokeCap.Round)
        }
        drawCircle(ink, s / 2 - s * 0.02f, Offset(s / 2, s / 2), style = Stroke(width = s * 0.04f))
    }
}
