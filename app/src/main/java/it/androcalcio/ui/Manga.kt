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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
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
    homeXI: List<Player>, awayXI: List<Player>, attaccoCasa: Boolean?,
    scia: List<Offset> = emptyList()   // percorso della palla nell'azione, in coordinate 0..1 (traiettoria blu tratteggiata)
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
        val piede = Offset(0f, r * 0.85f)   // la palla sta ai piedi della figurina

        // Traiettoria blu tratteggiata dell'azione (sotto giocatori e pallone)
        if (scia.size >= 2) {
            val tratteggio = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(r * 0.7f, r * 0.6f))
            val percorso = androidx.compose.ui.graphics.Path()
            scia.forEachIndexed { i, o ->
                val q = p(o.x, o.y) + piede
                if (i == 0) percorso.moveTo(q.x, q.y) else percorso.lineTo(q.x, q.y)
            }
            drawPath(percorso, Color.White.copy(alpha = 0.55f), style = Stroke(width = r * 0.34f, cap = StrokeCap.Round, pathEffect = tratteggio))
            drawPath(percorso, Color(0xFF1E5BFF), style = Stroke(width = r * 0.2f, cap = StrokeCap.Round, pathEffect = tratteggio))
        }

        // I giocatori vicini alla palla le vanno incontro (fino al 45% della distanza)
        fun verso(nx: Float, ny: Float): Offset {
            val d = hypot(bx - nx, (by - ny) * 0.55f)
            val k = (0.45f * (1f - d / 0.2f)).coerceAtLeast(0f)
            return p(nx + (bx - nx) * k, ny + (by - ny) * k)
        }
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
                figurina(pl, verso((fx + shift).coerceIn(0.03f, 0.97f), fy + (by - fy) * 0.08f), mH, if (pl.numero > 0) pl.numero else pos, true)
            }
        }
        af.coords().forEachIndexed { i, (fx, fy) ->
            awayNums.firstOrNull { it.first == i + 1 }?.let { (pos, pl) ->
                figurina(pl, verso(((1f - fx) + shift).coerceIn(0.03f, 0.97f), fy + (by - fy) * 0.08f), mA, if (pl.numero > 0) pl.numero else pos, false)
            }
        }

        // Arbitro: maglia gialla, segue l'azione a debita distanza
        val arb = p((bx - 0.05f).coerceIn(0.05f, 0.95f), (by + 0.10f).coerceIn(0.08f, 0.92f))
        drawOval(Color.Black.copy(alpha = 0.3f), Offset(arb.x - r * 0.8f, arb.y + r * 0.85f), Size(r * 1.6f, r * 0.5f))
        drawRoundRect(Color(0xFFFFE23D), Offset(arb.x - r * 0.65f, arb.y - r * 0.15f), Size(r * 1.3f, r * 1.1f), CornerRadius(r * 0.4f))
        drawRoundRect(Inchiostro, Offset(arb.x - r * 0.65f, arb.y - r * 0.15f), Size(r * 1.3f, r * 1.1f), CornerRadius(r * 0.4f), style = Stroke(width = r * 0.16f))
        drawCircle(Color(0xFFF1C9A0), r * 0.48f, Offset(arb.x, arb.y - r * 0.55f))
        drawCircle(Inchiostro, r * 0.48f, Offset(arb.x, arb.y - r * 0.55f), style = Stroke(width = r * 0.14f))

        // Pallone con linee di velocità verso la porta attaccata
        val ball = p(bx, by) + piede
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
fun MomentoChiave(titolo: String, testo: String, squadra: Team, giocatore: Player?, onFine: () -> Unit, scenaVar: Boolean = false) {
    val anim = remember(titolo, testo) { Animatable(0f) }
    LaunchedEffect(titolo, testo) { anim.animateTo(1f, tween(350, easing = FastOutSlowInEasing)) }
    LaunchedEffect(titolo, testo) { delay(2000); onFine() }
    val c1 = if (scenaVar) Color(0xFF0B3D91) else Color(squadra.colorPrimary)
    val c2 = if (scenaVar) Color(0xFF00E5FF) else Color(squadra.colorSecondary)
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
            if (scenaVar) {
                Box(Modifier.background(Inchiostro).border(4.dp, Color(0xFF00E5FF)).padding(horizontal = 28.dp, vertical = 18.dp)) {
                    Text("VAR", color = Color(0xFF00E5FF), fontFamily = PcTitle, fontSize = 56.sp)
                }
            } else if (giocatore != null) {
                AvatarSpec(AvatarGen.specDi(giocatore.name, mappa), 150.dp,
                    Modifier.background(Color.White, androidx.compose.foundation.shape.CircleShape), maglia = c1 to c2)
            }
            val onomatopea = if (scenaVar) "BIP BIP..." else when {
                titolo.contains("GOOOL") || titolo.contains("RIGORE") -> listOf("ZUUUM!", "BAAAM!", "SHOOT!", "DOOON!")[Math.floorMod(testo.hashCode(), 4)]
                titolo.contains("ESPULSO") -> "FIIIISCHIO!"
                else -> "..."
            }
            Box(Modifier.graphicsLayer { rotationZ = 8f }) {
                val os = TextStyle(fontFamily = PcTitle, fontSize = 30.sp)
                Text(onomatopea, style = os.copy(color = Inchiostro, drawStyle = Stroke(width = 9f)))
                Text(onomatopea, style = os.copy(color = c2))
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

/**
 * Posizione animata della palla: passa da giocatore a giocatore lungo il percorso degli episodi e, negli altri
 * minuti, con un passaggio verso un compagno della squadra che ha il possesso. `scia` è il tragitto da disegnare.
 */
class PallaAnimata(val x: Float, val y: Float, val attacco: Boolean?, val scia: List<Offset> = emptyList())

/**
 * Posti dei giocatori di movimento (portiere escluso) dove la palla "sta ai piedi": è il punto fisso della posizione
 * disegnata in MangaPitch, che sposta la squadra di (x - 0.5) * 0.2 al muoversi della palla.
 */
private fun postiGiocatori(f: it.androcalcio.engine.Formation, casa: Boolean): List<Pair<Float, Float>> =
    f.coords().drop(1).map { (fx, fy) ->
        val base = if (casa) fx else 1f - fx
        ((base - 0.1f) / 0.8f).coerceIn(0.03f, 0.97f) to fy
    }

/** Il giocatore più vicino al punto indicato (diverso da `escludi`, per non passarsi la palla da soli). */
private fun alPiedeDi(posti: List<Pair<Float, Float>>, da: Pair<Float, Float>, escludi: Pair<Float, Float>?): Pair<Float, Float> {
    if (posti.isEmpty()) return da
    val ord = posti.sortedBy { hypot(it.first - da.first, (it.second - da.second) * 0.55f) }
    return ord.firstOrNull { it != escludi } ?: ord.first()
}

private suspend fun muoviPalla(ax: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>, ay: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>, px: Float, py: Float, ms: Int) {
    kotlinx.coroutines.coroutineScope {
        launch { ax.animateTo(px, tween(ms)) }
        launch { ay.animateTo(py, tween(ms)) }
    }
}

@Composable
fun rememberPalla(
    events: List<it.androcalcio.engine.MatchEvent>, minuto: Int, possessoCasa: Int, veloce: Boolean,
    hf: it.androcalcio.engine.Formation, af: it.androcalcio.engine.Formation
): PallaAnimata {
    val ax = remember { Animatable(0.5f) }
    val ay = remember { Animatable(0.5f) }
    val scia = remember { androidx.compose.runtime.mutableStateListOf<Offset>() }
    var attacco by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Boolean?>(null) }
    var inAzione by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var inVolo by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }   // la palla sta viaggiando: la scia arriva fino a lei
    val idx = events.indexOfLast { it.percorso.isNotEmpty() }
    val ultimo = events.getOrNull(idx)
    val durata = if (veloce) 30 else 260

    LaunchedEffect(idx) {
        val e = ultimo ?: return@LaunchedEffect
        inAzione = true; attacco = e.home
        val posti = postiGiocatori(if (e.home) hf else af, e.home)
        // Le tappe intermedie diventano passaggi a un compagno; l'ultima resta il punto del tiro
        var prec: Pair<Float, Float>? = null
        val tappe = e.percorso.mapIndexed { i, pt ->
            if (i == e.percorso.lastIndex) pt else alPiedeDi(posti, pt, prec).also { prec = it }
        }
        scia.clear(); scia.add(Offset(ax.value, ay.value))
        inVolo = true
        for ((px, py) in tappe) {
            muoviPalla(ax, ay, px, py, durata)
            scia.add(Offset(px, py))
        }
        inVolo = false; attacco = null
        delay(if (veloce) 300L else 1500L)   // la traiettoria resta visibile un attimo dopo il tiro
        scia.clear()
        inAzione = false
    }
    LaunchedEffect(minuto) {
        if (inAzione || (ultimo != null && ultimo.minute == minuto)) return@LaunchedEffect
        val rnd = java.util.Random(minuto * 131L + possessoCasa)
        val casaPalla = rnd.nextInt(100) < possessoCasa
        val mira = (0.5f + (possessoCasa - 50) / 100f * 0.5f + (rnd.nextFloat() - 0.5f) * 0.5f).coerceIn(0.1f, 0.9f) to
            (0.15f + rnd.nextFloat() * 0.7f)
        val (tx, ty) = alPiedeDi(postiGiocatori(if (casaPalla) hf else af, casaPalla), mira, null)
        scia.clear(); scia.add(Offset(ax.value, ay.value))
        inVolo = true
        muoviPalla(ax, ay, tx, ty, if (veloce) 30 else 700)
        scia.add(Offset(tx, ty))
        inVolo = false
        delay(if (veloce) 100L else 500L)
        if (!inAzione) scia.clear()
    }
    val tragitto = if (inVolo) scia.toList() + Offset(ax.value, ay.value) else scia.toList()
    return PallaAnimata(ax.value, ay.value, attacco, tragitto)
}
