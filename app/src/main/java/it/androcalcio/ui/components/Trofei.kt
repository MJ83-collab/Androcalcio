package it.androcalcio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Tutti i trofei del gioco: disegnati a mano, nessun logo o coppa reale. */
enum class TipoTrofeo(val nome: String) {
    SCUDETTO("Campionato"), COPPA_NAZIONALE("Coppa nazionale"), SUPERCOPPA("Supercoppa"),
    CAMPIONI("Coppa dei Campioni d'Europa"), EUROPA("Coppa Europa"), COPPE("Coppa delle Coppe"),
    MONDIALE("Mondiali"), EUROPEO("Europei"), AMICHEVOLE("Amichevole")
}

/** Trofeo dal nome della competizione scritto nell'albo d'oro. */
fun trofeoDa(competizione: String): TipoTrofeo = when {
    competizione.startsWith("Campionato") || competizione.startsWith("Playoff") -> TipoTrofeo.SCUDETTO
    competizione == "Supercoppa" -> TipoTrofeo.SUPERCOPPA
    competizione.startsWith("Coppa dei Campioni") -> TipoTrofeo.CAMPIONI
    competizione == "Coppa Europa" -> TipoTrofeo.EUROPA
    competizione == "Coppa delle Coppe" -> TipoTrofeo.COPPE
    competizione == "Mondiali" -> TipoTrofeo.MONDIALE
    competizione.startsWith("Europei") || competizione.startsWith("Europeo") -> TipoTrofeo.EUROPEO
    else -> TipoTrofeo.COPPA_NAZIONALE
}

@Composable
fun TrofeoIcona(tipo: TipoTrofeo, modifier: Modifier = Modifier, nazione: String = "", lato: Dp = 40.dp,
                manica: Color = Color(0xFF2E7D32)) {
    Canvas(modifier.size(lato)) { disegnaTrofeo(tipo, nazione, manica) }
}

fun DrawScope.disegnaTrofeo(tipo: TipoTrofeo, nazione: String = "", manica: Color = Color(0xFF2E7D32)) = when (tipo) {
    TipoTrofeo.SCUDETTO -> disegnaScudetto(nazione)
    TipoTrofeo.COPPA_NAZIONALE -> disegnaCoppa()
    TipoTrofeo.SUPERCOPPA -> disegnaSupercoppa()
    TipoTrofeo.CAMPIONI -> disegnaCampioni()
    TipoTrofeo.EUROPA -> disegnaCoppaEuropa()
    TipoTrofeo.COPPE -> disegnaCoppaCoppe()
    TipoTrofeo.MONDIALE -> disegnaMondiale()
    TipoTrofeo.EUROPEO -> disegnaEuropeo()
    TipoTrofeo.AMICHEVOLE -> disegnaStrettaDiMano(manica, Color(0xFF8A94A6))
}

private val ARGENTO = listOf(Color(0xFF8E9AA8), Color(0xFFF2F5F8), Color(0xFFA9B3BF), Color(0xFF6E7884))
private val ORO = listOf(Color(0xFF9C7A1E), Color(0xFFFFE89A), Color(0xFFD4AF37), Color(0xFF8A6A12))
private val BORDO = Color(0xFF2B3440)

private fun DrawScope.piedistallo(brush: Brush, y: Float, larg: Float, alt: Float) {
    val w = size.width; val h = size.height
    drawRect(brush, Offset(w * (0.5f - larg / 2), h * y), Size(w * larg, h * alt))
    drawRect(BORDO, Offset(w * (0.5f - larg / 2), h * y), Size(w * larg, h * alt), style = Stroke(1.5f))
}

/** Coppa nazionale: argento, due anse piccole. */
fun DrawScope.disegnaCoppa() {
    val w = size.width; val h = size.height
    val arg = Brush.horizontalGradient(ARGENTO)
    val coppa = Path().apply { moveTo(w * 0.22f, h * 0.06f); lineTo(w * 0.78f, h * 0.06f)
        quadraticBezierTo(w * 0.76f, h * 0.55f, w * 0.54f, h * 0.62f); lineTo(w * 0.54f, h * 0.78f); lineTo(w * 0.46f, h * 0.78f)
        lineTo(w * 0.46f, h * 0.62f); quadraticBezierTo(w * 0.24f, h * 0.55f, w * 0.22f, h * 0.06f); close() }
    drawPath(coppa, arg)
    drawArc(Color(0xFFB7C0CB), 90f, 180f, false, Offset(w * 0.06f, h * 0.12f), Size(w * 0.24f, h * 0.30f), style = Stroke(width = 5f))
    drawArc(Color(0xFFB7C0CB), 270f, 180f, false, Offset(w * 0.70f, h * 0.12f), Size(w * 0.24f, h * 0.30f), style = Stroke(width = 5f))
    piedistallo(arg, 0.80f, 0.40f, 0.14f)
    drawPath(coppa, BORDO, style = Stroke(width = 2f))
}

/** Supercoppa: piatto d'argento rotondo su un supporto. */
fun DrawScope.disegnaSupercoppa() {
    val w = size.width; val h = size.height
    val arg = Brush.radialGradient(listOf(Color(0xFFF7F9FB), Color(0xFFAAB4C0), Color(0xFF6E7884)), Offset(w * 0.42f, h * 0.36f), w * 0.5f)
    drawCircle(arg, w * 0.34f, Offset(w * 0.5f, h * 0.42f))
    drawCircle(BORDO, w * 0.34f, Offset(w * 0.5f, h * 0.42f), style = Stroke(2f))
    drawCircle(Color(0xFFD4AF37), w * 0.22f, Offset(w * 0.5f, h * 0.42f), style = Stroke(3f))
    drawCircle(Color(0xFFD4AF37), w * 0.06f, Offset(w * 0.5f, h * 0.42f))
    piedistallo(Brush.horizontalGradient(ARGENTO), 0.80f, 0.36f, 0.14f)
    drawRect(Brush.horizontalGradient(ARGENTO), Offset(w * 0.46f, h * 0.74f), Size(w * 0.08f, h * 0.08f))
}

/** Coppa dei Campioni: argento, grande, con anse larghe che scendono fino alla base. */
fun DrawScope.disegnaCampioni() {
    val w = size.width; val h = size.height
    val arg = Brush.horizontalGradient(ARGENTO)
    val corpo = Path().apply { moveTo(w * 0.30f, h * 0.06f); lineTo(w * 0.70f, h * 0.06f)
        quadraticBezierTo(w * 0.70f, h * 0.50f, w * 0.54f, h * 0.58f); lineTo(w * 0.56f, h * 0.80f); lineTo(w * 0.44f, h * 0.80f)
        lineTo(w * 0.46f, h * 0.58f); quadraticBezierTo(w * 0.30f, h * 0.50f, w * 0.30f, h * 0.06f); close() }
    // Anse "a orecchie": dal bordo fino al piede
    val ansaSx = Path().apply { moveTo(w * 0.31f, h * 0.12f); cubicTo(w * 0.0f, h * 0.05f, w * 0.0f, h * 0.70f, w * 0.44f, h * 0.70f) }
    val ansaDx = Path().apply { moveTo(w * 0.69f, h * 0.12f); cubicTo(w * 1.0f, h * 0.05f, w * 1.0f, h * 0.70f, w * 0.56f, h * 0.70f) }
    drawPath(ansaSx, Color(0xFFB7C0CB), style = Stroke(width = w * 0.07f))
    drawPath(ansaDx, Color(0xFFB7C0CB), style = Stroke(width = w * 0.07f))
    drawPath(ansaSx, BORDO, style = Stroke(width = 1.2f)); drawPath(ansaDx, BORDO, style = Stroke(width = 1.2f))
    drawPath(corpo, arg); drawPath(corpo, BORDO, style = Stroke(width = 2f))
    piedistallo(arg, 0.80f, 0.42f, 0.15f)
}

/** Coppa Europa: vaso slanciato d'argento su un alto piedistallo dorato. */
fun DrawScope.disegnaCoppaEuropa() {
    val w = size.width; val h = size.height
    val arg = Brush.horizontalGradient(ARGENTO)
    val vaso = Path().apply { moveTo(w * 0.36f, h * 0.04f); lineTo(w * 0.64f, h * 0.04f)
        quadraticBezierTo(w * 0.58f, h * 0.18f, w * 0.66f, h * 0.34f); quadraticBezierTo(w * 0.66f, h * 0.52f, w * 0.53f, h * 0.56f)
        lineTo(w * 0.53f, h * 0.64f); lineTo(w * 0.47f, h * 0.64f); lineTo(w * 0.47f, h * 0.56f)
        quadraticBezierTo(w * 0.34f, h * 0.52f, w * 0.34f, h * 0.34f); quadraticBezierTo(w * 0.42f, h * 0.18f, w * 0.36f, h * 0.04f); close() }
    drawPath(vaso, arg); drawPath(vaso, BORDO, style = Stroke(width = 2f))
    piedistallo(Brush.horizontalGradient(ORO), 0.64f, 0.30f, 0.30f)
    drawLine(BORDO, Offset(w * 0.35f, h * 0.79f), Offset(w * 0.65f, h * 0.79f), 1.5f)
}

/** Coppa delle Coppe: coppa d'oro piccola con tre stelle sopra. */
fun DrawScope.disegnaCoppaCoppe() {
    val w = size.width; val h = size.height
    val oro = Brush.horizontalGradient(ORO)
    val coppa = Path().apply { moveTo(w * 0.28f, h * 0.28f); lineTo(w * 0.72f, h * 0.28f)
        quadraticBezierTo(w * 0.70f, h * 0.64f, w * 0.54f, h * 0.68f); lineTo(w * 0.54f, h * 0.80f); lineTo(w * 0.46f, h * 0.80f)
        lineTo(w * 0.46f, h * 0.68f); quadraticBezierTo(w * 0.30f, h * 0.64f, w * 0.28f, h * 0.28f); close() }
    drawArc(Color(0xFFD4AF37), 90f, 180f, false, Offset(w * 0.14f, h * 0.32f), Size(w * 0.20f, h * 0.22f), style = Stroke(width = 4f))
    drawArc(Color(0xFFD4AF37), 270f, 180f, false, Offset(w * 0.66f, h * 0.32f), Size(w * 0.20f, h * 0.22f), style = Stroke(width = 4f))
    drawPath(coppa, oro); drawPath(coppa, BORDO, style = Stroke(width = 2f))
    piedistallo(oro, 0.80f, 0.36f, 0.14f)
    listOf(0.30f, 0.50f, 0.70f).forEach { stella(Offset(w * it, h * (if (it == 0.50f) 0.10f else 0.15f)), w * 0.07f, Color(0xFFFFE89A)) }
}

/** Mondiali: globo dorato su una colonna a spirale e una base con fascia verde. */
fun DrawScope.disegnaMondiale() {
    val w = size.width; val h = size.height
    val oro = Brush.horizontalGradient(ORO)
    val colonna = Path().apply { moveTo(w * 0.38f, h * 0.76f); quadraticBezierTo(w * 0.30f, h * 0.50f, w * 0.40f, h * 0.38f)
        lineTo(w * 0.60f, h * 0.38f); quadraticBezierTo(w * 0.70f, h * 0.50f, w * 0.62f, h * 0.76f); close() }
    drawPath(colonna, oro); drawPath(colonna, BORDO, style = Stroke(width = 1.5f))
    // spirale
    for (k in 0..2) drawArc(Color(0xFF8A6A12), 200f, 140f, false, Offset(w * 0.36f, h * (0.44f + k * 0.10f)), Size(w * 0.28f, h * 0.10f), style = Stroke(1.5f))
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF3C4), Color(0xFFD4AF37), Color(0xFF8A6A12)), Offset(w * 0.44f, h * 0.18f), w * 0.26f),
        w * 0.19f, Offset(w * 0.5f, h * 0.24f))
    drawCircle(BORDO, w * 0.19f, Offset(w * 0.5f, h * 0.24f), style = Stroke(1.5f))
    drawArc(Color(0xFF8A6A12), 0f, 180f, false, Offset(w * 0.31f, h * 0.16f), Size(w * 0.38f, h * 0.10f), style = Stroke(1.2f))
    drawLine(Color(0xFF8A6A12), Offset(w * 0.5f, h * 0.05f), Offset(w * 0.5f, h * 0.43f), 1.2f)
    piedistallo(oro, 0.76f, 0.42f, 0.18f)
    drawRect(Color(0xFF2E7D32), Offset(w * 0.29f, h * 0.81f), Size(w * 0.42f, h * 0.06f))
}

/** Europei: coppa d'argento affusolata con una fascia blu e un cerchio di stelle. */
fun DrawScope.disegnaEuropeo() {
    val w = size.width; val h = size.height
    val arg = Brush.horizontalGradient(ARGENTO)
    val coppa = Path().apply { moveTo(w * 0.30f, h * 0.06f); lineTo(w * 0.70f, h * 0.06f)
        lineTo(w * 0.58f, h * 0.62f); lineTo(w * 0.56f, h * 0.78f); lineTo(w * 0.44f, h * 0.78f); lineTo(w * 0.42f, h * 0.62f); close() }
    drawPath(coppa, arg); drawPath(coppa, BORDO, style = Stroke(width = 2f))
    drawRect(Color(0xFF1E3A8A), Offset(w * 0.34f, h * 0.24f), Size(w * 0.32f, h * 0.12f))
    for (k in 0 until 5) stella(Offset(w * (0.38f + k * 0.06f), h * 0.30f), w * 0.025f, Color(0xFFFFD54F))
    piedistallo(arg, 0.80f, 0.40f, 0.14f)
}

private fun DrawScope.stella(c: Offset, r: Float, colore: Color) {
    val p = Path()
    for (i in 0 until 10) {
        val ang = Math.PI / 2 + i * Math.PI / 5
        val rr = if (i % 2 == 0) r else r * 0.45f
        val x = c.x + (rr * Math.cos(ang)).toFloat(); val y = c.y - (rr * Math.sin(ang)).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close(); drawPath(p, colore)
}

/** Colori dello scudetto: quelli della bandiera della nazione (true = strisce verticali). */
fun coloriScudetto(nazione: String): Pair<List<Color>, Boolean> = when (nazione) {
    "Italia" -> listOf(Color(0xFF009246), Color.White, Color(0xFFCE2B37)) to true
    "Francia" -> listOf(Color(0xFF0055A4), Color.White, Color(0xFFEF4135)) to true
    "Germania" -> listOf(Color.Black, Color(0xFFDD0000), Color(0xFFFFCE00)) to false
    "Spagna" -> listOf(Color(0xFFAA151B), Color(0xFFF1BF00), Color(0xFFAA151B)) to false
    "Argentina" -> listOf(Color(0xFF74ACDF), Color.White, Color(0xFF74ACDF)) to false
    "Inghilterra" -> listOf(Color.White, Color(0xFFCE1124), Color.White) to true
    "Brasile" -> listOf(Color(0xFF009C3B), Color(0xFFFFDF00), Color(0xFF009C3B)) to true
    "Giappone" -> listOf(Color.White, Color(0xFFBC002D), Color.White) to true
    "Olanda" -> listOf(Color(0xFFAE1C28), Color.White, Color(0xFF21468B)) to false
    "Portogallo" -> listOf(Color(0xFF006600), Color(0xFFFF0000), Color(0xFFFF0000)) to true
    "Belgio" -> listOf(Color.Black, Color(0xFFFAE042), Color(0xFFED2939)) to true
    "Scozia" -> listOf(Color(0xFF005EB8), Color.White, Color(0xFF005EB8)) to false
    "Turchia" -> listOf(Color(0xFFE30A17), Color.White, Color(0xFFE30A17)) to false
    "Grecia" -> listOf(Color(0xFF0D5EAF), Color.White, Color(0xFF0D5EAF), Color.White, Color(0xFF0D5EAF)) to false
    else -> listOf(Color(0xFF3F6FD8), Color.White, Color(0xFF3F6FD8)) to true
}

/** Scudetto con i colori della bandiera: bordo dorato, punta in basso. */
fun DrawScope.disegnaScudetto(nazione: String) {
    val w = size.width; val h = size.height
    val l = w * 0.18f; val r = w * 0.82f; val t = h * 0.08f; val b = h * 0.94f
    val forma = Path().apply {
        moveTo(l, t); lineTo(w * 0.42f, t); lineTo(w * 0.5f, t + h * 0.07f); lineTo(w * 0.58f, t); lineTo(r, t)
        lineTo(r, h * 0.52f); quadraticBezierTo(r, h * 0.80f, w * 0.5f, b); quadraticBezierTo(l, h * 0.80f, l, h * 0.52f); close()
    }
    val (colori, verticale) = coloriScudetto(nazione)
    clipPath(forma) {
        colori.forEachIndexed { i, c ->
            if (verticale) drawRect(c, Offset(l + (r - l) / colori.size * i, t), Size((r - l) / colori.size + 1f, b - t))
            else drawRect(c, Offset(l, t + (b - t) / colori.size * i), Size(r - l, (b - t) / colori.size + 1f))
        }
        drawRect(Color.White.copy(alpha = 0.18f), Offset(l, t), Size((r - l) * 0.35f, b - t))
    }
    drawPath(forma, Color(0xFFD4AF37), style = Stroke(width = 5f))
    drawPath(forma, Color(0xFF5C4A12), style = Stroke(width = 1.5f))
}

/** Stretta di mano: due braccia (manica della tua squadra e dell'avversario) che si incontrano al centro. */
fun DrawScope.disegnaStrettaDiMano(manicaSx: Color, manicaDx: Color) {
    val w = size.width; val h = size.height
    val pelle = Color(0xFFF1C27D); val ombra = Color(0xFFC6955A); val bordo = Color(0xFF3B2A18)
    fun tratto(x0: Float, y0: Float, x1: Float, y1: Float, c: Color, spessore: Float) =
        drawLine(c, Offset(w * x0, h * y0), Offset(w * x1, h * y1), w * spessore, cap = StrokeCap.Round)
    tratto(0.02f, 0.92f, 0.30f, 0.62f, manicaSx, 0.20f)
    tratto(0.98f, 0.92f, 0.70f, 0.62f, manicaDx, 0.20f)
    tratto(0.27f, 0.65f, 0.33f, 0.59f, Color(0xFFF5F5F5), 0.21f)
    tratto(0.73f, 0.65f, 0.67f, 0.59f, Color(0xFFF5F5F5), 0.21f)
    tratto(0.36f, 0.56f, 0.60f, 0.50f, ombra, 0.20f)
    tratto(0.64f, 0.56f, 0.42f, 0.48f, pelle, 0.17f)
    for (k in 0..3) {
        val x = 0.40f + k * 0.06f
        tratto(x, 0.43f, x + 0.015f, 0.56f, pelle, 0.07f)
        if (k > 0) drawLine(bordo, Offset(w * (x - 0.03f), h * 0.44f), Offset(w * (x - 0.02f), h * 0.54f), 1.5f)
    }
    tratto(0.40f, 0.47f, 0.52f, 0.40f, pelle, 0.07f)
    drawLine(bordo, Offset(w * 0.40f, h * 0.44f), Offset(w * 0.52f, h * 0.37f), 1.5f)
}
