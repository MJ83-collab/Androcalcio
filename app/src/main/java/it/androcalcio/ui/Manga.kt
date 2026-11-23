package it.androcalcio.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.engine.AvatarGen
import it.androcalcio.engine.Formation
import it.androcalcio.ui.components.AvatarSpec
import it.androcalcio.ui.components.LocalAvatarModificati
import it.androcalcio.ui.components.coloriPersona
import it.androcalcio.ui.theme.PcTitle
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private val Inchiostro = Color(0xFF111111)

private fun luminanza(c: Color) = 0.299f * c.red + 0.587f * c.green + 0.114f * c.blue
private fun simili(a: Color, b: Color) = abs(a.red - b.red) + abs(a.green - b.green) + abs(a.blue - b.blue) < 0.45f

/** Colori delle due squadre in campo: se le maglie si somigliano, gli ospiti usano il secondo colore o una maglia neutra. */
private fun maglie(home: Team, away: Team): Pair<Pair<Color, Color>, Pair<Color, Color>> {
    val h = Color(home.colorPrimary) to Color(home.colorSecondary)
    var a = Color(away.colorPrimary) to Color(away.colorSecondary)
    if (simili(h.first, a.first)) a = a.second to a.first
    if (simili(h.first, a.first)) a = (if (luminanza(h.first) > 0.5f) Color(0xFF1A1A2E) else Color.White) to a.second
    return h to a
}

/**
 * Campo realistico in stile anime: tribune a retino, prato tagliato a strisce con luce dei riflettori,
 * porte con la rete, giocatori come figurine (maglia, capelli, ombra) e linee di velocità dietro il pallone.
 */
@Composable
fun MangaPitch(
    home: Team, away: Team, bx: Float, by: Float, hf: Formation, af: Formation,
    homeXI: List<Player>, awayXI: List<Player>, attaccoCasa: Boolean?
) {
    val mappa = LocalAvatarModificati.current
    val (mH, mA) = remember(home.id, away.id) { maglie(home, away) }
    val homeNums = numbered(homeXI)
    val awayNums = numbered(awayXI)
    val colori = remember(homeXI, awayXI, mappa) { (homeXI + awayXI).associate { it.id to coloriPersona(it.name, mappa) } }
    val textMeasurer = rememberTextMeasurer()

    Canvas(Modifier.fillMaxWidth().aspectRatio(1.45f).border(3.dp, Inchiostro)) {
        val w = size.width; val h = size.height
        val top = h * 0.10f; val bottom = h * 0.90f; val ph = bottom - top
        fun p(fx: Float, fy: Float) = Offset(fx * w, top + fy * ph)

        tribuna(0f, top, mH.first, mA.first)
        tribuna(bottom, h - bottom, mA.first, mH.first)

        // Prato: strisce del tosaerba, luce dei riflettori dall'alto, bordi più scuri
        val strisce = 14
        for (i in 0 until strisce) drawRect(if (i % 2 == 0) Color(0xFF3B9A47) else Color(0xFF33893F),
            Offset(i * w / strisce, top), Size(w / strisce + 1f, ph))
        drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.Transparent, Color.Black.copy(alpha = 0.14f)), top, bottom),
            Offset(0f, top), Size(w, ph))
        drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.30f)), Offset(w / 2, top + ph / 2), w * 0.75f),
            Offset(0f, top), Size(w, ph))

        // Linee del campo
        val bianco = Color.White.copy(alpha = 0.88f)
        val st = Stroke(width = 2.5f)
        val m = 0.035f
        drawRect(bianco, p(m, m), Size(w * (1 - 2 * m), ph * (1 - 2 * m)), style = st)
        drawLine(bianco, p(0.5f, m), p(0.5f, 1 - m), 2.5f)
        drawCircle(bianco, ph * 0.15f, p(0.5f, 0.5f), style = st)
        drawCircle(bianco, 3.5f, p(0.5f, 0.5f))
        for (lato in 0..1) {
            val x0 = if (lato == 0) m else 1 - m - 0.14f
            drawRect(bianco, p(x0, 0.22f), Size(w * 0.14f, ph * 0.56f), style = st)
            val x1 = if (lato == 0) m else 1 - m - 0.05f
            drawRect(bianco, p(x1, 0.38f), Size(w * 0.05f, ph * 0.24f), style = st)
            val dischetto = if (lato == 0) m + 0.10f else 1 - m - 0.10f
            drawCircle(bianco, 3f, p(dischetto, 0.5f))
            // Lunetta dell'area
            val cx = p(dischetto, 0.5f)
            drawArc(bianco, if (lato == 0) -55f else 125f, 110f, false,
                Offset(cx.x - ph * 0.15f, cx.y - ph * 0.15f), Size(ph * 0.30f, ph * 0.30f), style = st)
            // Porta con la rete
            val gx = if (lato == 0) m - 0.022f else 1 - m
            val g0 = p(gx, 0.44f)
            val gs = Size(w * 0.022f, ph * 0.12f)
            drawRect(Color.White.copy(alpha = 0.22f), g0, gs)
            for (k in 1..3) drawLine(Color.White.copy(alpha = 0.5f), Offset(g0.x, g0.y + gs.height * k / 4f), Offset(g0.x + gs.width, g0.y + gs.height * k / 4f), 1f)
            for (k in 1..2) drawLine(Color.White.copy(alpha = 0.5f), Offset(g0.x + gs.width * k / 3f, g0.y), Offset(g0.x + gs.width * k / 3f, g0.y + gs.height), 1f)
            drawRect(Color.White, g0, gs, style = Stroke(width = 3f))
        }

        // Giocatori
        val shift = (bx - 0.5f) * 0.2f
        val r = ph * 0.042f
        fun figurina(pl: Player, c: Offset, maglia: Pair<Color, Color>, numero: Int, casa: Boolean) {
            val portiere = pl.role == "P"
            val col = if (portiere) (if (casa) Color(0xFFF4D03F) else Color(0xFF7CD957)) else maglia.first
            val (capelli, pelle) = colori[pl.id] ?: (Color(0xFF3A2A1A) to Color(0xFFF1C9A0))
            drawOval(Color.Black.copy(alpha = 0.32f), Offset(c.x - r * 0.95f, c.y + r * 0.95f), Size(r * 1.9f, r * 0.6f))
            val corpo = Offset(c.x - r * 0.75f, c.y - r * 0.15f)
            val cs = Size(r * 1.5f, r * 1.3f)
            drawRoundRect(col, corpo, cs, CornerRadius(r * 0.45f))
            if (!portiere) drawRect(maglia.second, Offset(c.x - r * 0.16f, corpo.y + r * 0.05f), Size(r * 0.32f, cs.height - r * 0.1f))
            drawRoundRect(Inchiostro, corpo, cs, CornerRadius(r * 0.45f), style = Stroke(width = r * 0.18f))
            val testa = Offset(c.x, c.y - r * 0.62f)
            drawCircle(pelle, r * 0.55f, testa)
            drawArc(capelli, 180f, 180f, true, Offset(testa.x - r * 0.58f, testa.y - r * 0.62f), Size(r * 1.16f, r * 1.0f))
            drawCircle(Inchiostro, r * 0.55f, testa, style = Stroke(width = r * 0.16f))
            val stile = TextStyle(color = if (luminanza(col) > 0.6f) Inchiostro else Color.White, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
            val mm = textMeasurer.measure(numero.toString(), stile)
            drawText(textMeasurer, numero.toString(), topLeft = Offset(c.x - mm.size.width / 2f, corpo.y + cs.height / 2 - mm.size.height / 2f), style = stile)
        }
        hf.coords().forEachIndexed { i, (fx, fy) ->
            homeNums.firstOrNull { it.first == i + 1 }?.let { (pos, pl) ->
                figurina(pl, p((fx + shift).coerceIn(0.03f, 0.97f), fy), mH, if (pl.numero > 0) pl.numero else pos, true)
            }
        }
        af.coords().forEachIndexed { i, (fx, fy) ->
            awayNums.firstOrNull { it.first == i + 1 }?.let { (pos, pl) ->
                figurina(pl, p(((1f - fx) + shift).coerceIn(0.03f, 0.97f), fy), mA, if (pl.numero > 0) pl.numero else pos, false)
            }
        }

        // Pallone con linee di velocità verso la porta attaccata
        val ball = p(bx, by)
        if (attaccoCasa != null) {
            val dir = if (attaccoCasa) 1f else -1f
            for (i in 0..4) {
                val dy = (i - 2) * r * 0.35f
                val lung = r * (2.4f + (i % 2) * 1.3f)
                drawLine(Color.White.copy(alpha = 0.75f), Offset(ball.x - dir * r * 0.9f, ball.y + dy),
                    Offset(ball.x - dir * (r * 0.9f + lung), ball.y + dy), 2.5f, StrokeCap.Round)
            }
        }
        drawOval(Color.Black.copy(alpha = 0.3f), Offset(ball.x - r * 0.5f, ball.y + r * 0.45f), Size(r, r * 0.35f))
        drawCircle(Color.White, r * 0.55f, ball)
        drawCircle(Inchiostro, r * 0.2f, ball)
        drawCircle(Inchiostro, r * 0.55f, ball, style = Stroke(width = 2f))
    }
}

/** Tribuna con il pubblico disegnato a retino: puntini nei colori delle due squadre e bianchi. */
private fun DrawScope.tribuna(y: Float, alt: Float, c1: Color, c2: Color) {
    drawRect(Color(0xFF1B1F3A), Offset(0f, y), Size(size.width, alt))
    val passo = size.width / 70f
    var riga = 0
    var yy = y + passo / 2
    while (yy < y + alt) {
        var x = passo / 2 + (riga % 2) * passo / 2
        var k = riga * 7
        while (x < size.width) {
            val c = when ((k * 31 + riga * 17) % 5) { 0, 1 -> c1; 2 -> c2; 3 -> Color.White; else -> Color(0xFF8A93C9) }
            drawCircle(c.copy(alpha = 0.75f), passo * 0.32f, Offset(x, yy))
            x += passo; k++
        }
        yy += passo * 0.9f; riga++
    }
    drawLine(Inchiostro, Offset(0f, if (y == 0f) alt else y), Offset(size.width, if (y == 0f) alt else y), 3f)
}

/**
 * Momento chiave a tutto schermo, come in un fumetto sportivo: linee di concentrazione, banda nei colori
 * della squadra, volto del protagonista e titolo con il contorno. Si chiude da solo o con un tocco.
 */
@Composable
fun MomentoChiave(titolo: String, testo: String, squadra: Team, giocatore: Player?, onFine: () -> Unit) {
    val anim = remember(titolo, testo) { Animatable(0f) }
    LaunchedEffect(titolo, testo) { anim.animateTo(1f, tween(350, easing = FastOutSlowInEasing)) }
    LaunchedEffect(titolo, testo) { delay(2000); onFine() }
    val c1 = Color(squadra.colorPrimary)
    val c2 = Color(squadra.colorSecondary)
    val mappa = LocalAvatarModificati.current

    Box(Modifier.fillMaxSize().background(Color.White).clickable { onFine() }) {
        Canvas(Modifier.fillMaxSize()) {
            val ctr = Offset(size.width / 2, size.height * 0.40f)
            val raggio = hypot(size.width, size.height)
            val rnd = java.util.Random(testo.hashCode().toLong())
            val n = 96
            for (i in 0 until n) {
                val a = i * (2 * PI / n) + rnd.nextDouble() * 0.03
                val dentro = raggio * (0.20f + rnd.nextFloat() * 0.12f)
                drawLine(Inchiostro, Offset(ctr.x + (cos(a) * dentro).toFloat(), ctr.y + (sin(a) * dentro).toFloat()),
                    Offset(ctr.x + (cos(a) * raggio).toFloat(), ctr.y + (sin(a) * raggio).toFloat()), 2f + rnd.nextFloat() * 6f)
            }
            rotate(-12f, ctr) {
                val y0 = ctr.y + size.height * 0.12f
                drawRect(Inchiostro, Offset(-size.width, y0 - 6f), Size(size.width * 3, size.height * 0.17f + 12f))
                drawRect(c1, Offset(-size.width, y0), Size(size.width * 3, size.height * 0.12f))
                drawRect(c2, Offset(-size.width, y0 + size.height * 0.12f), Size(size.width * 3, size.height * 0.05f))
            }
        }
        Column(
            Modifier.align(Alignment.Center).padding(16.dp).graphicsLayer {
                val k = 0.6f + 0.4f * anim.value
                scaleX = k; scaleY = k; alpha = anim.value
            },
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (giocatore != null) {
                AvatarSpec(AvatarGen.specDi(giocatore.name, mappa), 150.dp,
                    Modifier.background(Color.White, androidx.compose.foundation.shape.CircleShape), maglia = c1 to c2)
            }
            Box(Modifier.graphicsLayer { rotationZ = -6f }) {
                val base = TextStyle(fontFamily = PcTitle, fontSize = 48.sp, textAlign = TextAlign.Center)
                Text(titolo, style = base.copy(color = Inchiostro, drawStyle = Stroke(width = 14f)))
                Text(titolo, style = base.copy(color = Color(0xFFFFE23D)))
            }
            Text(
                testo, Modifier.background(Color.White).border(3.dp, Inchiostro).padding(horizontal = 12.dp, vertical = 8.dp),
                color = Inchiostro, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, textAlign = TextAlign.Center
            )
        }
    }
}
