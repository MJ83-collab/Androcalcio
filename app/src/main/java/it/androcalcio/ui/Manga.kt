package it.androcalcio.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
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
import it.androcalcio.engine.Meteo
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
    scia: List<Offset> = emptyList(),   // percorso della palla nell'azione, in coordinate 0..1 (traiettoria blu tratteggiata)
    quota: Float = 0f,                  // altezza della palla 0..1 (0 = rasoterra)
    inMoto: Boolean = true,             // la palla sta viaggiando (linee di velocità)
    meteo: it.androcalcio.engine.Meteo.Tempo? = null, prato: it.androcalcio.engine.Meteo.Campo? = null,
    lineaCasa: it.androcalcio.engine.DefLine? = null, lineaOspiti: it.androcalcio.engine.DefLine? = null,
    sim: PallonePista? = null,          // se presente, la palla è simulata sui giocatori (portata e passata) invece di essere disegnata dove dice bx,by
    ruoliSenzaCasa: Map<Long, String> = emptyMap(),   // ruoli senza palla (piano tattico): ci si sposta lì quando si difende
    ruoliSenzaOspiti: Map<Long, String> = emptyMap(),
    mioCasa: Boolean? = null            // la squadra dell'utente: true = casa, false = ospiti (alone giallo e etichetta)
) {
    val animato = meteo == it.androcalcio.engine.Meteo.Tempo.PIOGGIA || meteo == it.androcalcio.engine.Meteo.Tempo.NEVE ||
        meteo == it.androcalcio.engine.Meteo.Tempo.VENTO
    val fase = if (animato) androidx.compose.animation.core.rememberInfiniteTransition(label = "meteo").animateFloat(
        0f, 1f, androidx.compose.animation.core.infiniteRepeatable(
            tween(if (meteo == it.androcalcio.engine.Meteo.Tempo.NEVE) 4000 else 1100, easing = androidx.compose.animation.core.LinearEasing)),
        label = "fase").value else 0f
    val spinta = androidx.compose.animation.core.animateFloatAsState(
        when (attaccoCasa) { null -> 0f; true -> 1f; false -> -1f }, tween(800), label = "spinta")
    // I giocatori seguono la palla con un leggero ritardo (molla): niente scatti, movimento continuo
    val molla = androidx.compose.animation.core.spring<Float>(dampingRatio = 1f, stiffness = 45f)
    val pbxS = androidx.compose.animation.core.animateFloatAsState(bx, molla, label = "pbx")
    val pbyS = androidx.compose.animation.core.animateFloatAsState(by, molla, label = "pby")
    val mappa = LocalAvatarModificati.current
    val (mH, mA) = remember(home.id, away.id) { maglie(home, away) }
    val homeNums = numbered(homeXI, hf)
    val awayNums = numbered(awayXI, af)
    val colori = remember(homeXI, awayXI, mappa) { (homeXI + awayXI).associate { it.id to coloriPersona(it.name, mappa) } }
    val textMeasurer = rememberTextMeasurer()

    // Ogni giocatore ha una posizione sua che si aggiorna a ogni fotogramma: accelera e frena dolcemente verso il bersaglio
    // (formazione + spinta dell'azione + attrazione della palla), con velocità massima. Niente salti, come in Football Manager.
    val pbx = pbxS.value; val pby = pbyS.value
    val sp = spinta.value
    // Il blocco squadra scorre verso la palla lungo il campo (più compatto quando difende)
    val shift = (pbx - 0.5f) * 0.30f
    // profondita = distanza dalla propria porta (0 = porta, 0.5 = metà campo), uguale per casa e ospiti
    fun dove(profondita: Float, ny: Float, casa: Boolean, portiere: Boolean): Offset {
        val segno = if (casa) 1f else -1f
        val att = kotlin.math.max(segno * sp, 0f)
        val dif = kotlin.math.max(-segno * sp, 0f)
        val linea = if (casa) lineaCasa else lineaOspiti
        val extraLinea = if (portiere || profondita >= 0.3f) 0f else when (linea) {
            it.androcalcio.engine.DefLine.ALTA -> 0.05f; it.androcalcio.engine.DefLine.BASSA -> -0.04f; else -> 0f }
        // In attacco la squadra sale (gli attaccanti più dei difensori); in difesa scende e si compatta
        val rel = if (portiere) profondita + 0.02f * att
            else profondita + (0.04f + 0.35f * profondita) * att - 0.10f * profondita * dif + extraLinea
        val xRel = if (casa) rel else 1f - rel
        val x0 = (xRel + shift * (if (portiere) 0.15f else 1f)).coerceIn(0.04f, 0.96f)
        // Lateralmente: chi difende stringe verso la palla più di chi attacca
        val y0 = ny + (pby - ny) * (if (portiere) 0.25f else 0.12f + 0.20f * dif)
        val d = hypot(pbx - x0, (pby - y0) * 0.55f)
        val k = 0.8f * (1f - d / 0.18f).coerceAtLeast(0f).let { it * it }
        return Offset(x0 + (pbx - x0) * k, y0 + (pby - y0) * k)
    }
    val bersagli = Array<Offset?>(22) { null }
    // Senza palla ogni giocatore scivola verso il suo ruolo di non possesso, tanto più quanto più la squadra difende
    fun spostaFase(nums: List<Pair<Int, Player>>, ruoli: Map<Long, String>, i: Int, casa: Boolean): Pair<Float, Float> {
        val p = nums.getOrNull(i)?.second ?: return 0f to 0f
        val r = ruoli[p.id] ?: return 0f to 0f
        val dif = kotlin.math.max(-(if (casa) 1f else -1f) * sp, 0f)
        val (dd, dw) = it.androcalcio.engine.PostoRuolo.spostamento(it.androcalcio.engine.TacticalRoles.assigned(p), r)
        return dd * 0.5f * dif to dw * 0.8f * dif
    }
    hf.coords().forEachIndexed { i, (fx, fy) -> if (i < 11) {
        val (dd, dw) = spostaFase(homeNums, ruoliSenzaCasa, i, true)
        bersagli[i] = dove(fx + dd, fy + dw + (pby - fy) * 0.08f, true, i == 0)
    } }
    af.coords().forEachIndexed { i, (fx, fy) -> if (i < 11) {
        val (dd, dw) = spostaFase(awayNums, ruoliSenzaOspiti, i, false)
        bersagli[11 + i] = dove(fx + dd, fy + dw + (pby - fy) * 0.08f, false, i == 0)
    } }
    val bersagliOra = androidx.compose.runtime.rememberUpdatedState(bersagli)
    val posiz = remember { androidx.compose.runtime.mutableStateListOf<Offset>().also { l -> repeat(22) { l.add(Offset(0.5f, 0.5f)) } } }
    val velocita = remember { FloatArray(44) }
    val partiti = remember { BooleanArray(22) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        var prima = 0L
        while (true) androidx.compose.runtime.withFrameNanos { ns ->
            val dt = if (prima == 0L) 0.016f else ((ns - prima) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            prima = ns
            val tg = bersagliOra.value
            for (i in 0 until 22) {
                val g = sim?.forzato(i) ?: tg[i] ?: continue
                if (!partiti[i]) { posiz[i] = g; partiti[i] = true; continue }
                val c = posiz[i]
                var vx = (g.x - c.x) * 2.4f; var vy = (g.y - c.y) * 2.4f
                val m = hypot(vx, vy); val vmax = 0.16f
                if (m > vmax) { vx *= vmax / m; vy *= vmax / m }
                val a = kotlin.math.min(1f, dt * 4.5f)
                velocita[2 * i] += (vx - velocita[2 * i]) * a
                velocita[2 * i + 1] += (vy - velocita[2 * i + 1]) * a
                posiz[i] = Offset(c.x + velocita[2 * i] * dt, c.y + velocita[2 * i + 1] * dt)
            }
            sim?.avanza(dt, posiz)
        }
    }

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
        val r = ph * 0.042f
        val piede = Offset(r * 0.95f, r * 0.45f)   // la palla sta accanto al pallino di chi la gioca

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

        // Giocatori come pallini numerati (stile Football Manager). Si muovono con l'azione: chi attacca sale,
        // chi difende arretra e si stringe verso la palla, e i vicini alla palla le vanno incontro.
        val bTerra = p(bx, by)
        fun pallino(pl: Player, c: Offset, maglia: Pair<Color, Color>, numero: Int, casa: Boolean) {
            val portiere = pl.role == "P"
            val col = if (portiere) (if (casa) Color(0xFFF4D03F) else Color(0xFF7CD957)) else maglia.first
            val rp = r * 0.95f
            // La mia squadra: alone giallo sotto il pallino, così si riconosce subito
            if (mioCasa != null && casa == mioCasa) drawCircle(Color(0xFFFFE23D).copy(alpha = 0.55f), rp * 1.45f, c)
            drawCircle(Color.Black.copy(alpha = 0.3f), rp, Offset(c.x + r * 0.15f, c.y + r * 0.22f))
            drawCircle(col, rp, c)
            drawCircle(if (portiere) Inchiostro else maglia.second, rp, c, style = Stroke(width = r * 0.2f))
            // Chi ha la palla ha un anello giallo
            if (hypot(c.x - bTerra.x, c.y - bTerra.y) < rp * 2.3f) drawCircle(Color(0xFFFFE23D), rp * 1.28f, c, style = Stroke(width = r * 0.16f))
            val stile = TextStyle(color = if (luminanza(col) > 0.6f) Inchiostro else Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            val mm = textMeasurer.measure(numero.toString(), stile)
            drawText(textMeasurer, numero.toString(), topLeft = Offset(c.x - mm.size.width / 2f, c.y - mm.size.height / 2f), style = stile)
        }
        var lineaX = if (attaccoCasa == true) Float.MAX_VALUE else Float.MIN_VALUE   // ultimo difensore di chi non ha la palla
        hf.coords().forEachIndexed { i, _ ->
            homeNums.firstOrNull { it.first == i + 1 }?.let { (pos, pl) ->
                val n = posiz.getOrNull(i) ?: return@let
                val c = p(n.x, n.y)
                pallino(pl, c, mH, if (pl.numero > 0) pl.numero else pos, true)
                if (i > 0 && attaccoCasa == false && c.x > lineaX) lineaX = c.x
            }
        }
        af.coords().forEachIndexed { i, _ ->
            awayNums.firstOrNull { it.first == i + 1 }?.let { (pos, pl) ->
                val n = posiz.getOrNull(11 + i) ?: return@let
                val c = p(n.x, n.y)
                pallino(pl, c, mA, if (pl.numero > 0) pl.numero else pos, false)
                if (i > 0 && attaccoCasa == true && c.x < lineaX) lineaX = c.x
            }
        }
        // Linea del fuorigioco: tratteggio rosso all'altezza dell'ultimo difensore di chi non ha la palla
        if (attaccoCasa != null && lineaX != Float.MAX_VALUE && lineaX != Float.MIN_VALUE) {
            drawLine(Color(0xFFFF3B6B).copy(alpha = 0.8f), Offset(lineaX, top), Offset(lineaX, bottom), 2.5f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 9f)))
        }

        // Arbitro: maglia gialla, segue l'azione a debita distanza
        val arb = p((bx - 0.05f).coerceIn(0.05f, 0.95f), (by + 0.10f).coerceIn(0.08f, 0.92f))
        drawOval(Color.Black.copy(alpha = 0.3f), Offset(arb.x - r * 0.8f, arb.y + r * 0.85f), Size(r * 1.6f, r * 0.5f))
        drawRoundRect(Color(0xFFFFE23D), Offset(arb.x - r * 0.65f, arb.y - r * 0.15f), Size(r * 1.3f, r * 1.1f), CornerRadius(r * 0.4f))
        drawRoundRect(Inchiostro, Offset(arb.x - r * 0.65f, arb.y - r * 0.15f), Size(r * 1.3f, r * 1.1f), CornerRadius(r * 0.4f), style = Stroke(width = r * 0.16f))
        drawCircle(Color(0xFFF1C9A0), r * 0.48f, Offset(arb.x, arb.y - r * 0.55f))
        drawCircle(Inchiostro, r * 0.48f, Offset(arb.x, arb.y - r * 0.55f), style = Stroke(width = r * 0.14f))

        // Pallone: gira mentre rotola; se è alto (pallonetto, lancio, tiro alto) si ingrandisce e l'ombra resta a terra
        val ball = bTerra + piede
        val q = quota.coerceIn(0f, 1f)
        val bc = Offset(ball.x, ball.y - q * r * 1.6f)
        val rb = r * 0.55f * (1f + 0.5f * q)
        if (attaccoCasa != null && inMoto) {
            val dir = if (attaccoCasa) 1f else -1f
            for (i in 0..4) {
                val dy = (i - 2) * r * 0.35f
                val lung = r * (2.4f + (i % 2) * 1.3f)
                drawLine(Color.White.copy(alpha = 0.75f), Offset(bc.x - dir * r * 0.9f, bc.y + dy),
                    Offset(bc.x - dir * (r * 0.9f + lung), bc.y + dy), 2.5f, StrokeCap.Round)
            }
        }
        val om = 1f - 0.3f * q
        drawOval(Color.Black.copy(alpha = 0.3f * (1f - 0.4f * q)), Offset(ball.x - r * 0.5f * om, ball.y + r * 0.45f), Size(r * om, r * 0.35f * om))
        drawCircle(Color.White, rb, bc)
        rotate(bx * 1800f + by * 900f, bc) {
            for (k in 0..2) {
                val an = (k * 120.0 + 20.0) * PI / 180.0
                drawCircle(Inchiostro, rb * 0.28f, Offset(bc.x + (cos(an) * rb * 0.58).toFloat(), bc.y + (sin(an) * rb * 0.58).toFloat()))
            }
            drawCircle(Inchiostro, rb * 0.22f, bc)
            drawLine(Inchiostro, bc, Offset(bc.x + rb, bc.y), 2f)
        }
        drawCircle(Inchiostro, rb, bc, style = Stroke(width = 2f))

        // Meteo e prato sul campo
        when (meteo) {
            Meteo.Tempo.PIOGGIA -> for (k in 0 until 70) {
                val x = ((k * 97) % 100) / 100f * w
                val y = top + ((((k * 53) % 100) / 100f + fase) % 1f) * ph
                drawLine(Color(0xFFBFD8FF).copy(alpha = 0.5f), Offset(x, y), Offset(x - r * 0.3f, y + r * 0.9f), 2f)
            }
            Meteo.Tempo.NEVE -> for (k in 0 until 60) {
                val x = ((k * 97) % 100) / 100f * w + sin(fase * 2 * PI + k).toFloat() * r * 0.6f
                val y = top + ((((k * 53) % 100) / 100f + fase) % 1f) * ph
                drawCircle(Color.White.copy(alpha = 0.85f), 2.5f + (k % 3), Offset(x, y))
            }
            Meteo.Tempo.NEBBIA -> drawRect(Color.White.copy(alpha = 0.22f), Offset(0f, top), Size(w, ph))
            Meteo.Tempo.VENTO -> for (k in 0 until 14) {
                val y = top + ((k * 71) % 100) / 100f * ph
                val x = ((((k * 37) % 100) / 100f + fase) % 1f) * w
                drawLine(Color.White.copy(alpha = 0.35f), Offset(x, y), Offset(x + r * 2.5f, y), 2f, StrokeCap.Round)
            }
            Meteo.Tempo.CALDO -> drawRect(Color(0xFFFF8A00).copy(alpha = 0.10f), Offset(0f, top), Size(w, ph))
            else -> {}
        }
        if (prato == Meteo.Campo.PESANTE || prato == Meteo.Campo.PESSIMO) {
            drawRect(Color(0xFF5A3B1E).copy(alpha = if (prato == Meteo.Campo.PESSIMO) 0.18f else 0.10f), Offset(0f, top), Size(w, ph))
        }
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
 * Pallone con fisica semplice: sta ai piedi di un giocatore (che lo porta correndo), oppure viaggia da un
 * compagno all'altro a velocità costante verso la posizione ATTUALE del ricevente, oppure è libero dopo un tiro.
 * Le posizioni sono quelle disegnate dei 22 giocatori (0..10 casa, 11..21 ospiti; 0 e 11 portieri).
 * I comandi `passa`, `porta` e `tira` sono sospesi finché il gesto non è finito.
 */
class PallonePista {
    var x by androidx.compose.runtime.mutableFloatStateOf(0.5f)
    var y by androidx.compose.runtime.mutableFloatStateOf(0.5f)
    var z by androidx.compose.runtime.mutableFloatStateOf(0f)
    var possessore = -1
        private set
    private var pos: List<Offset> = emptyList()
    private var volo = false
    private var ricevente = -1
    private var mira = Offset.Zero
    private var vel = 1f
    private var picco = 0f
    private var dist0 = 1f
    private var portando = false
    private var raggio = 0.05f
    private var scadenza = 0f
    private var tempo = 0f
    private var fine: kotlinx.coroutines.CompletableDeferred<Unit>? = null

    private fun nuovo(): kotlinx.coroutines.CompletableDeferred<Unit> {
        fine?.cancel()
        return kotlinx.coroutines.CompletableDeferred<Unit>().also { fine = it }
    }
    private fun chiudi() { fine?.complete(Unit); fine = null }
    private fun distanza(a: Offset, b: Offset) = hypot(a.x - b.x, (a.y - b.y) * 0.55f)

    /** Se il giocatore `i` deve andare in un punto preciso (chi porta palla, chi sta per ricevere). */
    fun forzato(i: Int): Offset? = when {
        portando && i == possessore -> mira
        volo && i == ricevente -> mira
        else -> null
    }

    /** Passaggio al compagno di `squadra` più vicino al punto `verso`; `picco` > 0 = palla alta. */
    suspend fun passa(squadra: IntRange, verso: Offset, velocita: Float, alto: Float = 0f) {
        val r = squadra.filter { it % 11 != 0 && it != possessore && it < pos.size }
            .minByOrNull { distanza(pos[it], verso) }
        if (r == null) { x = verso.x; y = verso.y; z = 0f; return }
        val att = nuovo()
        portando = false; possessore = -1; ricevente = r; mira = verso
        vel = velocita; picco = alto; dist0 = maxOf(distanza(pos[r], Offset(x, y)), 0.01f); volo = true
        att.await()
    }

    /** Il giocatore con la palla corre verso `meta` (si ferma entro `r` o dopo `massimo` secondi). */
    suspend fun porta(meta: Offset, r: Float, massimo: Float) {
        if (possessore < 0 || volo) return
        val att = nuovo()
        mira = meta; raggio = r; scadenza = massimo; tempo = 0f; portando = true
        att.await()
    }

    /** Tiro: la palla parte dal punto in cui si trova e resta libera nel punto `verso`. */
    suspend fun tira(verso: Offset, velocita: Float, alto: Float = 0f) {
        val att = nuovo()
        portando = false; possessore = -1; ricevente = -1; mira = verso
        vel = velocita; picco = alto; dist0 = maxOf(distanza(verso, Offset(x, y)), 0.01f); volo = true
        att.await()
    }

    /** Un passo di simulazione, chiamato a ogni fotogramma dopo il movimento dei giocatori. */
    fun avanza(dt: Float, p: List<Offset>) {
        pos = p
        if (volo) {
            val d = p.getOrNull(ricevente) ?: mira
            val dd = distanza(d, Offset(x, y))
            val passo = vel * dt
            val t = (1f - dd / dist0).coerceIn(0f, 1f)
            z = picco * 4f * t * (1f - t)
            if (dd <= passo) {
                x = d.x; y = d.y; z = 0f; volo = false
                possessore = ricevente; ricevente = -1
                chiudi()
            } else {
                val f = passo / dd
                x += (d.x - x) * f; y += (d.y - y) * f
            }
        } else if (possessore >= 0) {
            val h = p.getOrNull(possessore) ?: return
            x = h.x; y = h.y; z = 0f
            if (portando) {
                tempo += dt
                if (distanza(h, mira) < raggio || tempo >= scadenza) { portando = false; chiudi() }
            }
        }
    }
}

/**
 * Palla animata: la porta e se la passa la squadra in possesso. Negli episodi segue il percorso dell'azione
 * (passaggi ai compagni, conduzione, tiro); negli altri minuti c'è una conduzione e un passaggio.
 * `scia` è il tragitto da disegnare, `quota` l'altezza (0 rasoterra, 1 alta), `inVolo` dice se la palla sta viaggiando.
 */
class PallaAnimata(
    val x: Float, val y: Float, val attacco: Boolean?, val scia: List<Offset> = emptyList(),
    val quota: Float = 0f, val inVolo: Boolean = false, val sim: PallonePista? = null
)

@Composable
fun rememberPalla(
    events: List<it.androcalcio.engine.MatchEvent>, minuto: Int, possessoCasa: Int, veloce: Boolean,
    hf: it.androcalcio.engine.Formation, af: it.androcalcio.engine.Formation,
    fattoreTempo: Float = 1f   // > 1 con pioggia, neve o prato pesante: la palla corre meno
): PallaAnimata {
    val sim = remember { PallonePista() }
    val scia = remember { androidx.compose.runtime.mutableStateListOf<Offset>() }
    var attacco by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Boolean?>(null) }
    var inAzione by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var inVolo by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }   // la palla sta viaggiando: la scia arriva fino a lei
    val idx = events.indexOfLast { it.percorso.isNotEmpty() }
    val ultimo = events.getOrNull(idx)
    val ritmo = if (veloce) 10f else 1f / fattoreTempo

    LaunchedEffect(idx) {
        val e = ultimo ?: return@LaunchedEffect
        inAzione = true; attacco = e.home
        val sq = if (e.home) 0..10 else 11..21
        val rnd = java.util.Random(e.minute * 17L + idx)
        scia.clear(); scia.add(Offset(sim.x, sim.y))
        inVolo = true
        if (sim.possessore !in sq) { sim.passa(sq, Offset(sim.x, sim.y), 1.1f * ritmo); scia.add(Offset(sim.x, sim.y)) }
        e.percorso.forEachIndexed { i, (px, py) ->
            val pt = Offset(px, py)
            if (i < e.percorso.lastIndex) {
                val lungo = hypot(pt.x - sim.x, (pt.y - sim.y) * 0.55f) > 0.30f
                sim.passa(sq, pt, 1.1f * ritmo, if (lungo) 1f else 0f)
            } else {
                if (!veloce) sim.porta(pt, 0.10f, 1.4f)
                sim.tira(pt, 2.0f * ritmo, if (rnd.nextFloat() < 0.35f) 0.8f else 0f)
            }
            scia.add(Offset(sim.x, sim.y))
        }
        inVolo = false
        delay(if (veloce) 300L else 1500L)   // la traiettoria resta visibile un attimo dopo il tiro; la squadra resta alta
        scia.clear()
        attacco = null
        inAzione = false
    }
    LaunchedEffect(minuto) {
        if (inAzione || (ultimo != null && ultimo.minute == minuto)) return@LaunchedEffect
        val rnd = java.util.Random(minuto * 131L + possessoCasa)
        val casaPalla = rnd.nextInt(100) < possessoCasa
        val sq = if (casaPalla) 0..10 else 11..21
        val mira = Offset((0.5f + (possessoCasa - 50) / 100f * 0.5f + (rnd.nextFloat() - 0.5f) * 0.5f).coerceIn(0.1f, 0.9f),
            0.15f + rnd.nextFloat() * 0.7f)
        scia.clear(); scia.add(Offset(sim.x, sim.y))
        inVolo = true
        if (sim.possessore !in sq) sim.passa(sq, Offset(sim.x, sim.y), 0.9f * ritmo)
        if (!veloce) sim.porta(mira, 0.06f, 1.2f)
        val lungo = hypot(mira.x - sim.x, (mira.y - sim.y) * 0.55f) > 0.30f
        sim.passa(sq, mira, 0.9f * ritmo, if (lungo) 0.6f else 0f)
        scia.add(Offset(sim.x, sim.y))
        inVolo = false
        delay(if (veloce) 100L else 500L)
        if (!inAzione) scia.clear()
    }
    val tragitto = if (inVolo) scia.toList() + Offset(sim.x, sim.y) else scia.toList()
    return PallaAnimata(sim.x, sim.y, attacco, tragitto, sim.z, inVolo, sim)
}
