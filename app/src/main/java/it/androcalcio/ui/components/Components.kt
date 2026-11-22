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

/** Sfumatura verticale dei pulsanti PC 7. */
val BtnGradient = Brush.verticalGradient(listOf(Color(0xFF30303C), Color(0xFF07070B)))

/** Mascotte comiche degli stemmi: scelte dal nome della squadra, sempre la stessa per lo stesso club. */
private val MASCOTTE = listOf("🐂", "🦁", "🐺", "🐍", "🦅", "🐓", "🐗", "🐴", "🐟", "🦆", "🐐", "🦊",
    "🐻", "🐝", "🐸", "🦈", "🐧", "🐷", "🦉", "🐌", "🦀", "🐊", "🦬", "🐿")

/**
 * Stemma originale e un po' comico: scudo storto con il motivo dei colori sociali (strisce, bande, metà...)
 * e una mascotte al centro. È generato dal nome: niente loghi reali dei club.
 */
@Composable
fun Crest(team: Team, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val c1 = Color(team.colorPrimary)
    val c2 = Color(team.colorSecondary)
    val h = Math.floorMod(team.name.hashCode(), 1_000_003)
    val motivo = h % 5
    val mascotte = it.androcalcio.engine.Stemmi.MASCOTTE[team.name] ?: MASCOTTE[(h / 5) % MASCOTTE.size]
    val storto = ((h / 7) % 9 - 4).toFloat()   // da -4 a +4 gradi
    val apriSquadra = it.androcalcio.ui.LocalApriSquadra.current
    Box(modifier.size(size).clickable { apriSquadra(team) }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val w = this.size.width; val hh = this.size.height
            val scudo = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.10f, hh * 0.08f)
                quadraticBezierTo(w * 0.5f, hh * 0.16f, w * 0.90f, hh * 0.08f)
                lineTo(w * 0.90f, hh * 0.50f)
                quadraticBezierTo(w * 0.88f, hh * 0.80f, w * 0.5f, hh * 0.96f)
                quadraticBezierTo(w * 0.12f, hh * 0.80f, w * 0.10f, hh * 0.50f)
                close()
            }
            rotate(storto) {
                clipPath(scudo) {
                    drawRect(c1)
                    when (motivo) {
                        0 -> for (i in 0 until 5 step 2) drawRect(c2, Offset(w * (0.1f + i * 0.16f), 0f), Size(w * 0.16f, hh))
                        1 -> drawRect(c2, Offset(w / 2, 0f), Size(w / 2, hh))
                        2 -> drawLine(c2, Offset(0f, 0f), Offset(w, hh), strokeWidth = w * 0.26f)
                        3 -> for (i in 0 until 5 step 2) drawRect(c2, Offset(0f, hh * (0.1f + i * 0.17f)), Size(w, hh * 0.17f))
                        else -> { drawRect(c2, Offset(w / 2, 0f), Size(w / 2, hh / 2)); drawRect(c2, Offset(0f, hh / 2), Size(w / 2, hh / 2)) }
                    }
                }
                drawPath(scudo, Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.09f))
                drawPath(scudo, if (AC.pc7) Color(0xFFFFD86B) else Color.White,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.04f))
            }
        }
        Text(mascotte, fontSize = (size.value * 0.40f).sp, modifier = Modifier.padding(bottom = (size.value * 0.06f).dp))
        if (size >= 40.dp) Text(
            team.shortName, Modifier.align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.75f)).padding(horizontal = 3.dp),
            color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = (size.value * 0.17f).sp, maxLines = 1
        )
    }
}

/** Card scura con bordo sottile, stile "glass" del canvas. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
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
            .clip(RoundedCornerShape(2.dp))
            .background(AC.Paper)
            .border(2.dp, AC.Btn, RoundedCornerShape(2.dp))
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
        containerColor = AC.Btn,
        contentColor = AC.Accent,
        disabledContainerColor = AC.Surface,
        disabledContentColor = AC.BtnBorder
    ),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(3.dp),
        colors = colors,
        border = BorderStroke(2.dp, if (enabled) AC.Accent else AC.BtnBorder),
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
