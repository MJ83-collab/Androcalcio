package it.androcalcio.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.androcalcio.engine.AvatarGen
import it.androcalcio.engine.AvatarGen.Spec

/** Volti modificati dall'utente (nome -> volto) e azione per aprire l'editor; li fornisce il root dell'app. */
val LocalAvatarModificati = compositionLocalOf<Map<String, Spec>> { emptyMap() }
val LocalModificaAvatar = compositionLocalOf<((String) -> Unit)?> { null }

/** Colori sociali (maglia, colletto) della squadra di chi viene disegnato: se presenti, il volto indossa la maglia. */
val LocalMagliaSquadra = compositionLocalOf<Pair<Color, Color>?> { null }

/** Avatar di una persona: volto salvato se modificato, altrimenti generato dal nome. Toccandolo si apre l'editor. */
@Composable
fun Avatar(nome: String, size: Dp = 48.dp, modifier: Modifier = Modifier) {
    val spec = AvatarGen.specDi(nome, LocalAvatarModificati.current)
    val modifica = LocalModificaAvatar.current
    AvatarSpec(spec, size, if (modifica != null) modifier.clickable { modifica(nome) } else modifier, LocalMagliaSquadra.current)
}

/**
 * Disegno a fumetto in stile manga d'azione: contorni neri spessi, colori piatti.
 * Ogni campo di [Spec] cambia un elemento del volto (vedi AvatarGen.CAMPI).
 */
@Composable
fun AvatarSpec(spec: Spec, size: Dp = 48.dp, modifier: Modifier = Modifier, maglia: Pair<Color, Color>? = null) {
    Canvas(modifier.size(size)) { disegna(spec, this.size.width, maglia) }
}

/** Colore dei capelli e della pelle di una persona, per disegnarla anche sul campo. */
fun coloriPersona(nome: String, mappa: Map<String, Spec>): Pair<Color, Color> {
    val sp = AvatarGen.specDi(nome, mappa)
    return col(AvatarGen.COL_CAPELLI, sp[AvatarGen.COL_CAPELLI]) to col(AvatarGen.PELLE, sp[AvatarGen.PELLE])
}

private fun col(campo: Int, i: Int) = Color(AvatarGen.CAMPI[campo].colori!![i])

private fun DrawScope.disegna(sp: Spec, s: Float, maglia: Pair<Color, Color>? = null) {
    val ink = Color(0xFF111111)
    val linea = Stroke(width = s * 0.045f, cap = StrokeCap.Round)
    val sottile = Stroke(width = s * 0.03f, cap = StrokeCap.Round)
    val pelle = col(AvatarGen.PELLE, sp[AvatarGen.PELLE])
    val capelli = col(AvatarGen.COL_CAPELLI, sp[AvatarGen.COL_CAPELLI])
    val giacca = col(AvatarGen.GIACCA, sp[AvatarGen.GIACCA])
    fun tratto(x1: Float, y1: Float, x2: Float, y2: Float, w: Float = 0.025f) =
        drawLine(ink, Offset(s * x1, s * y1), Offset(s * x2, s * y2), strokeWidth = s * w, cap = StrokeCap.Round)
    fun riempi(p: Path, c: Color) { drawPath(p, c); drawPath(p, ink, style = sottile) }

    drawCircle(col(AvatarGen.SFONDO, sp[AvatarGen.SFONDO]), s / 2)

    // Spalle e colletto
    val spalle = Path().apply {
        moveTo(s * 0.10f, s); quadraticBezierTo(s * 0.14f, s * 0.74f, s * 0.5f, s * 0.72f)
        quadraticBezierTo(s * 0.86f, s * 0.74f, s * 0.90f, s); close()
    }
    drawPath(spalle, maglia?.first ?: giacca); drawPath(spalle, ink, style = linea)
    if (maglia != null) {
        // Maglia da calcio: colletto a V nel secondo colore e riflesso sulla spalla
        riempi(Path().apply { moveTo(s * 0.40f, s * 0.73f); lineTo(s * 0.5f, s * 0.86f); lineTo(s * 0.60f, s * 0.73f)
            lineTo(s * 0.55f, s * 0.73f); lineTo(s * 0.5f, s * 0.80f); lineTo(s * 0.45f, s * 0.73f); close() }, maglia.second)
        drawArc(Color.White.copy(alpha = 0.35f), 200f, 40f, false, Offset(s * 0.16f, s * 0.78f), Size(s * 0.30f, s * 0.20f),
            style = Stroke(width = s * 0.03f, cap = StrokeCap.Round))
    } else when (sp[AvatarGen.COLLETTO]) {
        0 -> { // camicia e cravatta
            riempi(Path().apply { moveTo(s * 0.40f, s * 0.73f); lineTo(s * 0.5f, s * 0.92f); lineTo(s * 0.60f, s * 0.73f); close() }, Color.White)
            drawLine(Color(0xFFC62828), Offset(s * 0.5f, s * 0.76f), Offset(s * 0.5f, s * 0.92f), strokeWidth = s * 0.05f)
        }
        1 -> { // papillon
            riempi(Path().apply { moveTo(s * 0.40f, s * 0.73f); lineTo(s * 0.5f, s * 0.92f); lineTo(s * 0.60f, s * 0.73f); close() }, Color.White)
            riempi(Path().apply {
                moveTo(s * 0.5f, s * 0.77f); lineTo(s * 0.41f, s * 0.73f); lineTo(s * 0.41f, s * 0.81f); close()
                moveTo(s * 0.5f, s * 0.77f); lineTo(s * 0.59f, s * 0.73f); lineTo(s * 0.59f, s * 0.81f); close()
            }, Color(0xFFC62828))
        }
        2 -> riempi(Path().apply { // camicia aperta
            moveTo(s * 0.38f, s * 0.73f); lineTo(s * 0.5f, s * 0.86f); lineTo(s * 0.62f, s * 0.73f); close() }, pelle)
        3 -> drawArc(ink, 10f, 160f, false, Offset(s * 0.38f, s * 0.64f), Size(s * 0.24f, s * 0.14f), style = sottile) // girocollo
        else -> { // polo
            riempi(Path().apply { moveTo(s * 0.38f, s * 0.73f); lineTo(s * 0.46f, s * 0.80f); lineTo(s * 0.5f, s * 0.74f)
                lineTo(s * 0.54f, s * 0.80f); lineTo(s * 0.62f, s * 0.73f); lineTo(s * 0.5f, s * 0.90f); close() }, Color.White)
        }
    }

    // Testa
    val viso = sp[AvatarGen.VISO]
    val w = floatArrayOf(0.42f, 0.50f, 0.38f, 0.46f)[viso]
    val h = floatArrayOf(0.52f, 0.48f, 0.58f, 0.52f)[viso]
    val x0 = 0.5f - w / 2; val y0 = 0.18f
    // Orecchie
    drawCircle(pelle, s * 0.045f, Offset(s * (x0 - 0.005f), s * 0.46f)); drawCircle(ink, s * 0.045f, Offset(s * (x0 - 0.005f), s * 0.46f), style = sottile)
    drawCircle(pelle, s * 0.045f, Offset(s * (x0 + w + 0.005f), s * 0.46f)); drawCircle(ink, s * 0.045f, Offset(s * (x0 + w + 0.005f), s * 0.46f), style = sottile)
    // Viso con ombra a due toni (cel shading): il lato destro è più scuro
    val faccia = Path().apply {
        if (viso == 3) addRoundRect(RoundRect(Rect(Offset(s * x0, s * y0), Size(s * w, s * h)), CornerRadius(s * 0.10f)))
        else addOval(Rect(Offset(s * x0, s * y0), Size(s * w, s * h)))
    }
    drawPath(faccia, pelle)
    clipPath(faccia) {
        drawOval(Color.Black.copy(alpha = 0.14f), Offset(s * (x0 + w * 0.62f), s * (y0 - 0.02f)), Size(s * w, s * (h + 0.04f)))
    }
    drawPath(faccia, ink, style = linea)
    val mento = y0 + h
    val bocY = mento - 0.14f
    val occY = y0 + h * 0.52f
    val dx = w * 0.22f // distanza degli occhi dal centro

    // Barba (sotto bocca e capelli)
    when (sp[AvatarGen.BARBA]) {
        1 -> tratto(0.5f - 0.09f, bocY - 0.025f, 0.5f + 0.09f, bocY - 0.025f, 0.04f) // baffi
        2 -> riempi(Path().apply { moveTo(s * 0.45f, s * (mento - 0.07f)); lineTo(s * 0.55f, s * (mento - 0.07f))
            lineTo(s * 0.5f, s * (mento + 0.01f)); close() }, capelli) // pizzetto
        3, 4 -> {
            val fondo = if (sp[AvatarGen.BARBA] == 4) 0.04f else 0.0f
            val p = Path().apply {
                moveTo(s * (x0 + 0.01f), s * (occY + 0.06f)); quadraticBezierTo(s * (x0 + 0.01f), s * (mento + fondo), s * 0.5f, s * (mento + fondo))
                quadraticBezierTo(s * (x0 + w - 0.01f), s * (mento + fondo), s * (x0 + w - 0.01f), s * (occY + 0.06f))
                quadraticBezierTo(s * (x0 + w - 0.06f), s * (bocY - 0.04f), s * 0.5f, s * (bocY - 0.045f))
                quadraticBezierTo(s * (x0 + 0.06f), s * (bocY - 0.04f), s * (x0 + 0.01f), s * (occY + 0.06f)); close()
            }
            riempi(p, capelli)
        }
        5 -> { // basette
            drawRect(capelli, Offset(s * (x0 + 0.005f), s * 0.36f), Size(s * 0.04f, s * 0.14f))
            drawRect(capelli, Offset(s * (x0 + w - 0.045f), s * 0.36f), Size(s * 0.04f, s * 0.14f))
        }
    }

    // Capelli
    val c = capelli
    when (sp[AvatarGen.TAGLIO]) {
        0 -> riempi(Path().apply {
            moveTo(s * (x0 - 0.02f), s * 0.36f); quadraticBezierTo(s * 0.30f, s * 0.12f, s * 0.55f, s * 0.13f)
            quadraticBezierTo(s * 0.76f, s * 0.15f, s * (x0 + w + 0.02f), s * 0.36f); quadraticBezierTo(s * 0.55f, s * 0.22f, s * (x0 - 0.02f), s * 0.36f); close() }, c)
        1 -> riempi(Path().apply {
            moveTo(s * 0.27f, s * 0.34f)
            for (i in 0..5) { lineTo(s * (0.29f + i * 0.08f), s * 0.06f + (i % 2) * s * 0.06f); lineTo(s * (0.33f + i * 0.08f), s * 0.22f) }
            lineTo(s * 0.73f, s * 0.34f); quadraticBezierTo(s * 0.5f, s * 0.22f, s * 0.27f, s * 0.34f); close() }, c)
        2 -> drawArc(c, 180f, 180f, true, Offset(s * x0, s * 0.17f), Size(s * w, s * 0.26f))
        3 -> drawArc(ink, 200f, 50f, false, Offset(s * 0.36f, s * 0.22f), Size(s * 0.14f, s * 0.10f), style = Stroke(width = s * 0.02f))
        4 -> riempi(Path().apply {
            moveTo(s * 0.26f, s * 0.48f); quadraticBezierTo(s * 0.24f, s * 0.12f, s * 0.5f, s * 0.12f)
            quadraticBezierTo(s * 0.78f, s * 0.12f, s * 0.75f, s * 0.48f); lineTo(s * 0.66f, s * 0.30f)
            lineTo(s * 0.56f, s * 0.36f); lineTo(s * 0.46f, s * 0.29f); lineTo(s * 0.34f, s * 0.36f); close() }, c)
        5 -> { // cresta + rasati ai lati
            drawArc(c, 180f, 180f, true, Offset(s * x0, s * 0.19f), Size(s * w, s * 0.22f))
            riempi(Path().apply { moveTo(s * 0.44f, s * 0.26f); lineTo(s * 0.47f, s * 0.04f); lineTo(s * 0.53f, s * 0.04f); lineTo(s * 0.56f, s * 0.26f); close() }, c)
        }
        6 -> { // afro
            drawCircle(c, s * 0.30f, Offset(s * 0.5f, s * 0.28f)); drawCircle(ink, s * 0.30f, Offset(s * 0.5f, s * 0.28f), style = sottile)
            drawOval(pelle, Offset(s * x0, s * 0.31f), Size(s * w, s * (h - 0.13f))); drawOval(ink, Offset(s * x0, s * 0.31f), Size(s * w, s * (h - 0.13f)), style = linea)
        }
        7 -> { // lunghi
            riempi(Path().apply {
                moveTo(s * (x0 - 0.04f), s * 0.70f); lineTo(s * (x0 - 0.04f), s * 0.34f); quadraticBezierTo(s * 0.5f, s * 0.04f, s * (x0 + w + 0.04f), s * 0.34f)
                lineTo(s * (x0 + w + 0.04f), s * 0.70f); lineTo(s * (x0 + w - 0.02f), s * 0.70f); lineTo(s * (x0 + w - 0.02f), s * 0.34f)
                quadraticBezierTo(s * 0.5f, s * 0.22f, s * (x0 + 0.02f), s * 0.34f); lineTo(s * (x0 + 0.02f), s * 0.70f); close() }, c)
        }
        8 -> { // ricci corti
            for (i in 0..4) drawCircle(c, s * 0.07f, Offset(s * (x0 + 0.02f + i * w / 4.4f), s * (0.22f - (if (i % 2 == 0) 0f else 0.02f))))
            for (i in 0..4) drawCircle(ink, s * 0.07f, Offset(s * (x0 + 0.02f + i * w / 4.4f), s * (0.22f - (if (i % 2 == 0) 0f else 0.02f))), style = Stroke(width = s * 0.02f))
        }
        9 -> riempi(Path().apply { // riga in mezzo
            moveTo(s * (x0 - 0.02f), s * 0.42f); quadraticBezierTo(s * (x0 - 0.02f), s * 0.12f, s * 0.5f, s * 0.12f)
            quadraticBezierTo(s * (x0 + w + 0.02f), s * 0.12f, s * (x0 + w + 0.02f), s * 0.42f); lineTo(s * 0.66f, s * 0.30f)
            quadraticBezierTo(s * 0.55f, s * 0.24f, s * 0.5f, s * 0.20f); quadraticBezierTo(s * 0.45f, s * 0.24f, s * 0.34f, s * 0.30f); close() }, c)
        10 -> riempi(Path().apply { // ciuffo alto
            moveTo(s * (x0 - 0.01f), s * 0.34f); quadraticBezierTo(s * 0.26f, s * 0.14f, s * 0.36f, s * 0.08f)
            quadraticBezierTo(s * 0.55f, s * 0.00f, s * 0.70f, s * 0.12f); quadraticBezierTo(s * (x0 + w + 0.02f), s * 0.20f, s * (x0 + w + 0.01f), s * 0.34f)
            quadraticBezierTo(s * 0.5f, s * 0.22f, s * (x0 - 0.01f), s * 0.34f); close() }, c)
        11 -> riempi(Path().apply { // scodella
            moveTo(s * (x0 - 0.03f), s * 0.40f); quadraticBezierTo(s * (x0 - 0.03f), s * 0.10f, s * 0.5f, s * 0.10f)
            quadraticBezierTo(s * (x0 + w + 0.03f), s * 0.10f, s * (x0 + w + 0.03f), s * 0.40f); lineTo(s * (x0 + w - 0.01f), s * 0.34f)
            lineTo(s * (x0 + 0.01f), s * 0.34f); close() }, c)
        12 -> { // treccine
            drawArc(c, 180f, 180f, true, Offset(s * (x0 - 0.01f), s * 0.15f), Size(s * (w + 0.02f), s * 0.28f))
            for (i in 0..4) tratto(x0 + 0.02f + i * (w - 0.04f) / 4f, 0.30f, x0 - 0.02f + i * (w + 0.04f) / 4f, 0.56f, 0.04f)
        }
        else -> { // stempiato: ciuffi ai lati
            riempi(Path().apply { moveTo(s * (x0 - 0.01f), s * 0.44f); quadraticBezierTo(s * (x0 - 0.02f), s * 0.18f, s * 0.38f, s * 0.17f)
                lineTo(s * 0.40f, s * 0.30f); lineTo(s * (x0 + 0.03f), s * 0.44f); close() }, c)
            riempi(Path().apply { moveTo(s * (x0 + w + 0.01f), s * 0.44f); quadraticBezierTo(s * (x0 + w + 0.02f), s * 0.18f, s * 0.62f, s * 0.17f)
                lineTo(s * 0.60f, s * 0.30f); lineTo(s * (x0 + w - 0.03f), s * 0.44f); close() }, c)
        }
    }

    // Riflesso lucido sui capelli, tipico dei disegni anime
    if (sp[AvatarGen.TAGLIO] != 3) drawArc(Color.White.copy(alpha = 0.45f), 205f, 50f, false,
        Offset(s * (x0 + 0.05f), s * 0.13f), Size(s * (w - 0.10f), s * 0.16f), style = Stroke(width = s * 0.025f, cap = StrokeCap.Round))

    // Sopracciglia
    val by = occY - 0.075f
    when (sp[AvatarGen.SOPRACCIGLIA]) {
        0 -> { tratto(0.5f - dx - 0.06f, by - 0.01f, 0.5f - dx + 0.05f, by + 0.015f, 0.035f); tratto(0.5f + dx + 0.06f, by - 0.01f, 0.5f + dx - 0.05f, by + 0.015f, 0.035f) }
        1 -> { tratto(0.5f - dx - 0.05f, by, 0.5f - dx + 0.05f, by, 0.03f); tratto(0.5f + dx - 0.05f, by, 0.5f + dx + 0.05f, by, 0.03f) }
        2 -> { tratto(0.5f - dx - 0.05f, by - 0.015f, 0.5f - dx + 0.05f, by - 0.035f, 0.03f); tratto(0.5f + dx + 0.05f, by - 0.015f, 0.5f + dx - 0.05f, by - 0.035f, 0.03f) }
        3 -> { tratto(0.5f - dx - 0.06f, by - 0.03f, 0.5f - dx + 0.06f, by + 0.03f, 0.04f); tratto(0.5f + dx + 0.06f, by - 0.03f, 0.5f + dx - 0.06f, by + 0.03f, 0.04f) }
        else -> { tratto(0.5f - dx - 0.06f, by, 0.5f - dx + 0.05f, by, 0.06f); tratto(0.5f + dx - 0.05f, by, 0.5f + dx + 0.06f, by, 0.06f) }
    }

    // Occhi
    val xs = floatArrayOf(0.5f - dx, 0.5f + dx)
    // Occhi anime: bianco a mandorla, iride colorata, pupilla, due riflessi e ciglia marcate
    val iride = listOf(Color(0xFF2F6BD8), Color(0xFF7A4A1E), Color(0xFF2E8B57), Color(0xFFC08A1E))[
        (sp[AvatarGen.TAGLIO] + sp[AvatarGen.VISO] * 3 + sp[AvatarGen.PELLE]) % 4]
    fun occhioAnime(cx: Float) {
        drawOval(Color.White, Offset(s * (cx - 0.055f), s * (occY - 0.04f)), Size(s * 0.11f, s * 0.085f))
        drawOval(iride, Offset(s * (cx - 0.033f), s * (occY - 0.038f)), Size(s * 0.066f, s * 0.08f))
        drawCircle(ink, s * 0.018f, Offset(s * cx, s * (occY + 0.002f)))
        drawCircle(Color.White, s * 0.013f, Offset(s * (cx + 0.013f), s * (occY - 0.018f)))
        drawCircle(Color.White, s * 0.006f, Offset(s * (cx - 0.014f), s * (occY + 0.02f)))
        drawArc(ink, 190f, 160f, false, Offset(s * (cx - 0.06f), s * (occY - 0.045f)), Size(s * 0.12f, s * 0.07f),
            style = Stroke(width = s * 0.03f, cap = StrokeCap.Round))
    }
    when (sp[AvatarGen.OCCHI]) {
        0 -> xs.forEach { occhioAnime(it) }
        1 -> xs.forEach { tratto(it - 0.04f, occY, it + 0.04f, occY, 0.025f) }
        2 -> xs.forEach { occhioAnime(it) }
        3 -> xs.forEach { drawArc(ink, 200f, 140f, false, Offset(s * (it - 0.04f), s * (occY - 0.025f)), Size(s * 0.08f, s * 0.06f), style = Stroke(width = s * 0.025f, cap = StrokeCap.Round)) }
        4 -> { tratto(xs[0] - 0.04f, occY - 0.015f, xs[0] + 0.04f, occY + 0.012f, 0.025f); tratto(xs[1] + 0.04f, occY - 0.015f, xs[1] - 0.04f, occY + 0.012f, 0.025f)
            xs.forEach { drawCircle(ink, s * 0.016f, Offset(s * it, s * (occY + 0.008f))) } }
        else -> xs.forEach { drawCircle(ink, s * 0.022f, Offset(s * it, s * occY)); tratto(it - 0.035f, occY + 0.04f, it + 0.035f, occY + 0.04f, 0.015f) }
    }

    // Occhiali
    val oc = sp[AvatarGen.OCCHIALI]
    if (oc == 1) { // da sole
        xs.forEach { drawRoundRect(ink, Offset(s * (it - 0.075f), s * (occY - 0.045f)), Size(s * 0.15f, s * 0.085f), CornerRadius(s * 0.03f)) }
        tratto(xs[0] + 0.075f, occY - 0.02f, xs[1] - 0.075f, occY - 0.02f, 0.02f)
    } else if (oc == 2) { // tondi
        xs.forEach { drawCircle(ink, s * 0.065f, Offset(s * it, s * occY), style = Stroke(width = s * 0.022f)) }
        tratto(xs[0] + 0.065f, occY, xs[1] - 0.065f, occY, 0.02f)
    } else if (oc == 3) { // rettangolari
        xs.forEach { drawRoundRect(ink, Offset(s * (it - 0.075f), s * (occY - 0.05f)), Size(s * 0.15f, s * 0.10f), CornerRadius(s * 0.01f), style = Stroke(width = s * 0.022f)) }
        tratto(xs[0] + 0.075f, occY - 0.02f, xs[1] - 0.075f, occY - 0.02f, 0.02f)
    } else if (oc == 4) { // visore sportivo
        drawRoundRect(Color(0xCC29B6F6), Offset(s * (x0 - 0.01f), s * (occY - 0.05f)), Size(s * (w + 0.02f), s * 0.10f), CornerRadius(s * 0.05f))
        drawRoundRect(ink, Offset(s * (x0 - 0.01f), s * (occY - 0.05f)), Size(s * (w + 0.02f), s * 0.10f), CornerRadius(s * 0.05f), style = sottile)
    }

    // Bocca
    when (sp[AvatarGen.BOCCA]) {
        0 -> tratto(0.44f, bocY, 0.56f, bocY)
        1 -> drawArc(ink, 20f, 140f, false, Offset(s * 0.42f, s * (bocY - 0.06f)), Size(s * 0.16f, s * 0.09f), style = Stroke(width = s * 0.025f, cap = StrokeCap.Round))
        2 -> tratto(0.44f, bocY + 0.01f, 0.57f, bocY - 0.015f)
        3 -> { drawOval(Color(0xFF7B1F1F), Offset(s * 0.45f, s * (bocY - 0.02f)), Size(s * 0.10f, s * 0.07f)); drawOval(ink, Offset(s * 0.45f, s * (bocY - 0.02f)), Size(s * 0.10f, s * 0.07f), style = Stroke(width = s * 0.02f)) }
        4 -> drawArc(ink, 200f, 140f, false, Offset(s * 0.43f, s * bocY), Size(s * 0.14f, s * 0.07f), style = Stroke(width = s * 0.025f, cap = StrokeCap.Round))
        else -> { drawRoundRect(Color.White, Offset(s * 0.44f, s * (bocY - 0.015f)), Size(s * 0.12f, s * 0.045f), CornerRadius(s * 0.01f)); drawRoundRect(ink, Offset(s * 0.44f, s * (bocY - 0.015f)), Size(s * 0.12f, s * 0.045f), CornerRadius(s * 0.01f), style = Stroke(width = s * 0.02f)) }
    }

    // Extra
    when (sp[AvatarGen.EXTRA]) {
        1 -> { // cerotto sul naso/guancia
            drawRoundRect(Color(0xFFF5D6A8), Offset(s * 0.56f, s * (occY + 0.05f)), Size(s * 0.10f, s * 0.035f), CornerRadius(s * 0.01f))
            drawRoundRect(ink, Offset(s * 0.56f, s * (occY + 0.05f)), Size(s * 0.10f, s * 0.035f), CornerRadius(s * 0.01f), style = Stroke(width = s * 0.012f))
        }
        2 -> { tratto(x0 + w * 0.68f, occY - 0.07f, x0 + w * 0.78f, occY + 0.10f, 0.02f); tratto(x0 + w * 0.65f, occY, x0 + w * 0.82f, occY + 0.02f, 0.012f) } // cicatrice
        3 -> drawCircle(Color(0xFFFFD54F), s * 0.022f, Offset(s * (x0 - 0.005f), s * 0.52f)) // orecchino
        4 -> { // fascia sulla fronte
            drawRect(Color(0xFFC62828), Offset(s * (x0 - 0.005f), s * (y0 + 0.07f)), Size(s * (w + 0.01f), s * 0.05f))
            drawRect(ink, Offset(s * (x0 - 0.005f), s * (y0 + 0.07f)), Size(s * (w + 0.01f), s * 0.05f), style = Stroke(width = s * 0.015f))
        }
    }
    drawCircle(ink, s / 2 - s * 0.02f, Offset(s / 2, s / 2), style = Stroke(width = s * 0.04f))
}
