package it.androcalcio.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Team
import it.androcalcio.ui.theme.AC
import it.androcalcio.ui.theme.PcTitle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import java.util.Locale


/** Effetto rilievo dei vecchi gestionali: luce in alto a sinistra, ombra in basso a destra (invertiti se premuto). */
fun Modifier.bevel(raised: Boolean = true, w: Dp = 2.dp): Modifier = this.drawBehind {
    val t = w.toPx()
    val light = Color.White.copy(alpha = 0.55f)
    val dark = Color.Black.copy(alpha = 0.55f)
    val (tl, br) = if (raised) light to dark else dark to light
    drawRect(tl, topLeft = Offset(0f, 0f), size = Size(size.width, t))
    drawRect(tl, topLeft = Offset(0f, 0f), size = Size(t, size.height))
    drawRect(br, topLeft = Offset(0f, size.height - t), size = Size(size.width, t))
    drawRect(br, topLeft = Offset(size.width - t, 0f), size = Size(t, size.height))
}

/** Ombra netta da vignetta di fumetto (tema manga): rettangolo nero spostato in basso a destra. */
fun Modifier.vignetta(d: Dp = 3.dp): Modifier = this.drawBehind {
    val o = d.toPx()
    drawRect(Color.Black, Offset(o, o), size)
}

/** Sfumatura verticale dei pulsanti PC 7. */
val BtnGradient = Brush.verticalGradient(listOf(Color(0xFF30303C), Color(0xFF07070B)))

/**
 * Stemma originale in stile realistico, generato dal nome (niente loghi reali): 5 sagome (scudo classico, scudo
 * appuntito, tondo con anello, ovale, scudo arrotondato), 9 motivi con i colori sociali, un simbolo disegnato
 * (stella, corona, torre, pallone, fulmine, onde) o il monogramma, bordo oro o scuro, effetto lucido e nastro col nome.
 */
@Composable
fun Crest(team: Team, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val c1 = Color(team.colorPrimary)
    val c2 = Color(team.colorSecondary).let { if (it == c1) Color.White else it }
    val h = Math.floorMod(team.name.hashCode(), 1_000_003)
    val forma = h % 5
    val motivo = (h / 5) % 9
    val simbolo = (h / 45) % 8
    val oro = (h / 360) % 3 == 0
    val sigla = team.shortName.filter { it.isLetter() }.take(if (simbolo == 7) 1 else 2).uppercase()
    val apriSquadra = it.androcalcio.ui.LocalApriSquadra.current
    Box(modifier.size(size).clickable { apriSquadra(team) }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) { stemma(c1, c2, forma, motivo, simbolo, oro) }
        if (simbolo >= 6) Text(
            sigla, color = if (c1.luminanceApprox() > 0.6f) Color(0xFF111111) else Color.White,
            fontWeight = FontWeight.Black, fontSize = (size.value * (if (sigla.length > 1) 0.30f else 0.40f)).sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
            style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.6f), Offset(1.5f, 1.5f), 2f))
        )
        if (size >= 40.dp) Text(
            team.shortName.uppercase(),
            Modifier.align(Alignment.BottomCenter).offset(y = (size.value * 0.04f).dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (oro) Color(0xFFB8902A) else Color(0xFF15151C))
                .border(1.dp, if (oro) Color(0xFFFFE9A0) else c2, RoundedCornerShape(2.dp))
                .padding(horizontal = 3.dp),
            color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = (size.value * 0.15f).sp, maxLines = 1
        )
    }
}

private fun Color.luminanceApprox(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

private fun sagomaStemma(forma: Int, w: Float, h: Float) = androidx.compose.ui.graphics.Path().apply {
    when (forma) {
        0 -> { moveTo(w * 0.12f, h * 0.07f); lineTo(w * 0.88f, h * 0.07f); lineTo(w * 0.88f, h * 0.52f)
            cubicTo(w * 0.88f, h * 0.78f, w * 0.66f, h * 0.90f, w * 0.5f, h * 0.97f)
            cubicTo(w * 0.34f, h * 0.90f, w * 0.12f, h * 0.78f, w * 0.12f, h * 0.52f); close() }
        1 -> { moveTo(w * 0.10f, h * 0.05f); quadraticBezierTo(w * 0.5f, h * 0.16f, w * 0.90f, h * 0.05f)
            lineTo(w * 0.90f, h * 0.55f); lineTo(w * 0.5f, h * 0.97f); lineTo(w * 0.10f, h * 0.55f); close() }
        2 -> addOval(androidx.compose.ui.geometry.Rect(w * 0.05f, h * 0.05f, w * 0.95f, h * 0.95f))
        3 -> addOval(androidx.compose.ui.geometry.Rect(w * 0.15f, h * 0.03f, w * 0.85f, h * 0.97f))
        else -> { moveTo(w * 0.12f, h * 0.30f); cubicTo(w * 0.12f, h * 0.04f, w * 0.88f, h * 0.04f, w * 0.88f, h * 0.30f)
            lineTo(w * 0.88f, h * 0.58f); quadraticBezierTo(w * 0.86f, h * 0.86f, w * 0.5f, h * 0.97f)
            quadraticBezierTo(w * 0.14f, h * 0.86f, w * 0.12f, h * 0.58f); close() }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.stemma(c1: Color, c2: Color, forma: Int, motivo: Int, simbolo: Int, oro: Boolean) {
    val w = size.width; val h = size.height
    val sag = sagomaStemma(forma, w, h)
    val bordo = if (oro) Color(0xFFC9A227) else Color(0xFF16161D)
    val ink = Color(0xFF111111)
    translate(w * 0.035f, h * 0.035f) { drawPath(sag, Color.Black.copy(alpha = 0.35f)) }
    clipPath(sag) {
        drawRect(c1)
        when (motivo) {
            0 -> for (i in 0 until 7 step 2) drawRect(c2, Offset(w * i / 7f, 0f), Size(w / 7f, h))
            1 -> for (i in 0 until 7 step 2) drawRect(c2, Offset(0f, h * i / 7f), Size(w, h / 7f))
            2 -> drawLine(c2, Offset(0f, 0f), Offset(w, h), strokeWidth = w * 0.30f)
            3 -> drawRect(c2, Offset(w / 2, 0f), Size(w / 2, h))
            4 -> { drawRect(c2, Offset(w / 2, 0f), Size(w / 2, h / 2)); drawRect(c2, Offset(0f, h / 2), Size(w / 2, h / 2)) }
            5 -> { drawRect(c2, Offset(w * 0.40f, 0f), Size(w * 0.20f, h)); drawRect(c2, Offset(0f, h * 0.38f), Size(w, h * 0.18f)) }
            6 -> drawPath(androidx.compose.ui.graphics.Path().apply { moveTo(0f, h * 0.85f); lineTo(w / 2, h * 0.35f); lineTo(w, h * 0.85f) },
                c2, style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.20f))
            7 -> drawPath(sag, c2, style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.24f))
            else -> drawRect(c2, Offset(0f, 0f), Size(w, h * 0.42f))
        }
        // Effetto lucido: luce in alto, ombra in basso
        drawRect(androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.38f), Color.Transparent, Color.Black.copy(alpha = 0.28f))))
        drawOval(Color.White.copy(alpha = 0.16f), Offset(w * 0.18f, h * 0.06f), Size(w * 0.64f, h * 0.26f))
    }
    if (forma == 2) {   // tondo: anello esterno con filetto
        drawCircle(bordo, w * 0.43f, Offset(w / 2, h / 2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.09f))
        drawCircle(if (oro) Color(0xFFFFE9A0) else c2, w * 0.37f, Offset(w / 2, h / 2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.02f))
    }
    drawPath(sag, bordo, style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.075f))
    drawPath(sag, if (oro) Color(0xFFFFE9A0) else Color.White.copy(alpha = 0.7f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.018f))
    // Simbolo al centro (bianco o oro, contorno scuro)
    val cx = w / 2; val cy = h * 0.47f; val r = w * 0.17f
    val fill = if (oro) Color(0xFFF2CF5B) else Color.White
    val tratto = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.03f, join = androidx.compose.ui.graphics.StrokeJoin.Round)
    fun simb(p: androidx.compose.ui.graphics.Path) { drawPath(p, fill); drawPath(p, ink, style = tratto) }
    when (simbolo) {
        0 -> simb(androidx.compose.ui.graphics.Path().apply {   // stella
            for (k in 0 until 10) {
                val a = Math.PI * (-0.5 + k / 5.0); val rr = if (k % 2 == 0) r * 1.15f else r * 0.48f
                val x = cx + (rr * Math.cos(a)).toFloat(); val y = cy + (rr * Math.sin(a)).toFloat()
                if (k == 0) moveTo(x, y) else lineTo(x, y)
            }; close() })
        1 -> simb(androidx.compose.ui.graphics.Path().apply {   // corona
            moveTo(cx - r, cy + r * 0.6f); lineTo(cx - r * 1.1f, cy - r * 0.6f); lineTo(cx - r * 0.5f, cy - r * 0.05f)
            lineTo(cx, cy - r * 0.9f); lineTo(cx + r * 0.5f, cy - r * 0.05f); lineTo(cx + r * 1.1f, cy - r * 0.6f)
            lineTo(cx + r, cy + r * 0.6f); close() })
        2 -> simb(androidx.compose.ui.graphics.Path().apply {   // torre merlata
            moveTo(cx - r * 0.8f, cy + r); lineTo(cx - r * 0.8f, cy - r * 0.8f); lineTo(cx - r * 0.45f, cy - r * 0.8f)
            lineTo(cx - r * 0.45f, cy - r * 0.45f); lineTo(cx - r * 0.15f, cy - r * 0.45f); lineTo(cx - r * 0.15f, cy - r * 0.8f)
            lineTo(cx + r * 0.15f, cy - r * 0.8f); lineTo(cx + r * 0.15f, cy - r * 0.45f); lineTo(cx + r * 0.45f, cy - r * 0.45f)
            lineTo(cx + r * 0.45f, cy - r * 0.8f); lineTo(cx + r * 0.8f, cy - r * 0.8f); lineTo(cx + r * 0.8f, cy + r); close() })
        3 -> {   // pallone
            drawCircle(Color.White, r * 1.05f, Offset(cx, cy)); drawCircle(ink, r * 1.05f, Offset(cx, cy), style = tratto)
            drawPath(androidx.compose.ui.graphics.Path().apply {
                for (k in 0 until 5) { val a = Math.PI * (-0.5 + k * 0.4)
                    val x = cx + (r * 0.42f * Math.cos(a)).toFloat(); val y = cy + (r * 0.42f * Math.sin(a)).toFloat()
                    if (k == 0) moveTo(x, y) else lineTo(x, y) }; close() }, ink)
        }
        4 -> simb(androidx.compose.ui.graphics.Path().apply {   // fulmine
            moveTo(cx + r * 0.3f, cy - r * 1.1f); lineTo(cx - r * 0.6f, cy + r * 0.15f); lineTo(cx - r * 0.05f, cy + r * 0.15f)
            lineTo(cx - r * 0.35f, cy + r * 1.1f); lineTo(cx + r * 0.65f, cy - r * 0.2f); lineTo(cx + r * 0.1f, cy - r * 0.2f); close() })
        5 -> for (k in 0..2) {   // onde
            val y = cy - r * 0.6f + k * r * 0.6f
            drawPath(androidx.compose.ui.graphics.Path().apply { moveTo(cx - r, y)
                quadraticBezierTo(cx - r / 2, y - r * 0.35f, cx, y); quadraticBezierTo(cx + r / 2, y + r * 0.35f, cx + r, y) },
                fill, style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.06f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        else -> drawCircle(Color.Black.copy(alpha = 0.25f), r * 1.15f, Offset(cx, cy))   // fondo del monogramma
    }
}

/** Card scura con bordo sottile, stile "glass" del canvas. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (AC.manga) {
        Box(modifier.vignetta().background(AC.Surface).border(2.dp, Color.White)) {
            CompositionLocalProvider(LocalContentColor provides AC.Text) { content() }
        }
        return
    }
    if (AC.pc7) {
        Box(
            modifier.clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(AC.NavyTop, AC.NavyBottom)))
                .border(2.dp, AC.NavyBorder, RoundedCornerShape(2.dp))
                .bevel(w = 1.dp)
        ) { CompositionLocalProvider(LocalContentColor provides AC.Text) { content() } }
        return
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (AC.pc7) 3.dp else 18.dp),
        color = AC.Surface,
        border = BorderStroke(if (AC.pc7) 2.dp else 1.dp, AC.Border),
        content = content
    )
}

/** Pulsante nero a spigoli vivi, testo maiuscolo bianco o giallo (azione principale). */
@Composable
fun PcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    main: Boolean = false,
    enabled: Boolean = true,
    compatto: Boolean = false
) {
    if (AC.manga) {
        Box(
            modifier.vignetta(2.dp)
                .background(if (!enabled) AC.SurfaceHi else if (main) AC.Accent else AC.Surface)
                .border(2.dp, Color.White)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = if (compatto) 4.dp else 14.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text.uppercase(Locale.ITALY), color = if (!enabled) AC.TextDim else Color.White,
                fontWeight = FontWeight.ExtraBold, fontSize = if (compatto) 12.sp else 13.sp, letterSpacing = 0.8.sp, maxLines = 1)
        }
        return
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(BtnGradient)
            .border(2.dp, if (main) AC.Accent else AC.BtnBorder, RoundedCornerShape(3.dp))
            .bevel()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (compatto) 4.dp else 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text.uppercase(Locale.ITALY),
            color = if (!enabled) AC.BtnBorder else if (main) AC.Accent else Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (compatto) 12.sp else 13.sp,
            letterSpacing = 0.8.sp,
            maxLines = 1
        )
    }
}

/** Pannello bianco che contiene una tabella (bordo scuro, spigoli vivi). */
@Composable
fun PaperPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .then(if (AC.manga) Modifier.vignetta() else Modifier)
            .clip(RoundedCornerShape(2.dp))
            .background(AC.Paper)
            .border(if (AC.manga) 3.dp else 2.dp, AC.Btn, RoundedCornerShape(2.dp))
            .bevel(w = 1.dp),
        content = content
    )
}

/** Cella di intestazione di tabella: testo piccolo in grassetto con il colore della colonna. */
@Composable
fun HeadCell(text: String, color: Color, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Center) {
    Text(
        text, modifier = modifier, color = color, textAlign = align,
        fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, maxLines = 1
    )
}

/** Riga di tabella con fondo alternato (indice pari chiaro, dispari bluastro). */
@Composable
fun PaperRow(index: Int, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth()
            .background(if (index % 2 == 0) AC.Paper else AC.PaperAlt)
            .drawBehind { drawRect(AC.PaperLine, Offset(0f, size.height - 1f), Size(size.width, 1f)) }
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = AC.TextDim)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = AC.Accent
            )
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    if (AC.manga) {
        Box(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp)) {
            Text(
                text.uppercase(Locale.ITALY),
                Modifier.vignetta(2.dp).background(AC.Accent).border(2.dp, Color.White).padding(horizontal = 10.dp, vertical = 3.dp),
                color = Color.White, fontFamily = PcTitle, fontSize = 13.sp, letterSpacing = 1.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        return
    }
    if (AC.pc7) {
        Box(
            modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(AC.SectionTop, AC.SectionBottom)))
                .border(1.dp, Color(0xFF0B1240), RoundedCornerShape(2.dp))
                .bevel(w = 1.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text.uppercase(Locale.ITALY), color = Color.White, fontFamily = PcTitle,
                fontSize = 12.sp, letterSpacing = 1.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall.copy(shadow = Shadow(Color(0xFF0B1240), Offset(1f, 1f), 2f))
            )
        }
        return
    }
    Text(
        text.uppercase(Locale.ITALY),
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = AC.TextDim,
        letterSpacing = 1.2.sp
    )
}

/** Bottone PC 7 per le azioni: nero, spigoli vivi, bordo giallo. Sostituisce i Button Material. */
@Composable
fun PcAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (AC.manga) AC.Accent else AC.Btn,
        contentColor = if (AC.manga) Color.White else AC.Accent,
        disabledContainerColor = AC.Surface,
        disabledContentColor = AC.BtnBorder
    ),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = if (AC.manga) modifier.vignetta(2.dp) else modifier,
        enabled = enabled,
        shape = RoundedCornerShape(if (AC.manga) 0.dp else 3.dp),
        colors = colors,
        border = BorderStroke(2.dp, if (AC.manga) Color.White else if (enabled) AC.Accent else AC.BtnBorder),
        content = content
    )
}

/** Scheda PC 7 (selezionabile): quadrato nero se attiva, grigio se no. Sostituisce i FilterChip. */
@Composable
fun PcChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    if (AC.manga) {
        Box(
            modifier.then(if (selected) Modifier.vignetta(2.dp) else Modifier)
                .background(if (selected) AC.Accent else AC.Surface)
                .border(2.dp, if (selected) Color.White else Color.White.copy(alpha = 0.5f))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides Color.White) { label() }
        }
        return
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (selected) BtnGradient else Brush.verticalGradient(listOf(AC.SurfaceHi, AC.Surface)))
            .border(2.dp, if (selected) AC.Accent else AC.BtnBorder, RoundedCornerShape(3.dp))
            .bevel(raised = !selected)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides if (selected) AC.Accent else AC.Text) {
            label()
        }
    }
}

fun money(v: Long): String =
    if (v >= 1_000_000) String.format(Locale.ITALY, "€ %.1f M", v / 1_000_000.0)
    else String.format(Locale.ITALY, "€ %,d", v)


/** Riquadro icona PC 7: quadratino in rilievo col colore della sezione e il pittogramma bianco. */
@Composable
fun PcIconBadge(icon: ImageVector, color: Color, size: Dp = 34.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(3.dp))
            .background(Brush.linearGradient(listOf(color.copy(alpha = 1f), Color(
                (color.red * 0.45f), (color.green * 0.45f), (color.blue * 0.45f)))))
            .border(1.dp, Color.Black.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
            .bevel(w = 1.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.62f))
    }
}

/** Bandierina disegnata (niente emoji): strisce verticali o orizzontali per le nazioni del gioco. */
@Composable
fun FlagBox(nazione: String, modifier: Modifier = Modifier) {
    val v: List<Color>? = when (nazione) {
        "Italia" -> listOf(Color(0xFF009246), Color.White, Color(0xFFCE2B37))
        "Francia" -> listOf(Color(0xFF0055A4), Color.White, Color(0xFFEF4135))
        else -> null
    }
    val h: List<Color>? = when (nazione) {
        "Germania" -> listOf(Color.Black, Color(0xFFDD0000), Color(0xFFFFCE00))
        "Spagna" -> listOf(Color(0xFFAA151B), Color(0xFFF1BF00), Color(0xFFF1BF00), Color(0xFFAA151B))
        "Argentina" -> listOf(Color(0xFF74ACDF), Color.White, Color(0xFF74ACDF))
        "Giappone" -> listOf(Color.White, Color(0xFFBC002D), Color.White)
        else -> null
    }
    val apriNazione = it.androcalcio.ui.LocalApriNazione.current
    Canvas(modifier.width(18.dp).height(12.dp).border(1.dp, Color.Black.copy(alpha = 0.6f)).clickable { apriNazione(nazione) }) {
        when {
            v != null -> v.forEachIndexed { i, c ->
                drawRect(c, Offset(size.width / v.size * i, 0f), Size(size.width / v.size + 1f, size.height)) }
            h != null -> h.forEachIndexed { i, c ->
                drawRect(c, Offset(0f, size.height / h.size * i), Size(size.width, size.height / h.size + 1f)) }
            nazione == "Inghilterra" -> {
                drawRect(Color.White)
                drawRect(Color(0xFFCE1124), Offset(size.width * 0.42f, 0f), Size(size.width * 0.16f, size.height))
                drawRect(Color(0xFFCE1124), Offset(0f, size.height * 0.38f), Size(size.width, size.height * 0.24f))
            }
            nazione == "Brasile" -> {
                drawRect(Color(0xFF009C3B))
                drawCircle(Color(0xFFFFDF00), radius = size.height * 0.42f)
                drawCircle(Color(0xFF002776), radius = size.height * 0.24f)
            }
            else -> drawRect(Color(0xFF8899CC))
        }
    }
}
