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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Team
import it.androcalcio.ui.theme.AC
import java.util.Locale

/** Stemma generato dai colori della squadra: due tinte in diagonale e la sigla. */
@Composable
fun Crest(team: Team, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val c1 = Color(team.colorPrimary)
    val c2 = Color(team.colorSecondary)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(0f to c1, 0.5f to c1, 0.5f to c2, 1f to c2)
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            team.shortName,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value * 0.3f).sp,
            style = MaterialTheme.typography.labelSmall.copy(
                shadow = Shadow(Color.Black, blurRadius = 6f)
            )
        )
    }
}

/** Card scura con bordo sottile, stile "glass" del canvas. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
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
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(AC.Btn)
            .border(2.dp, if (main) AC.Accent else AC.BtnBorder, RoundedCornerShape(3.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text.uppercase(Locale.ITALY),
            color = if (!enabled) AC.BtnBorder else if (main) AC.Accent else Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
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
            .border(2.dp, AC.Btn, RoundedCornerShape(2.dp)),
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
    Text(
        text.uppercase(Locale.ITALY),
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = AC.TextDim,
        letterSpacing = 1.2.sp
    )
}

fun money(v: Long): String =
    if (v >= 1_000_000) String.format(Locale.ITALY, "€ %.1f M", v / 1_000_000.0)
    else String.format(Locale.ITALY, "€ %,d", v)
