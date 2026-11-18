package it.androcalcio.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
        shape = RoundedCornerShape(18.dp),
        color = AC.Surface,
        border = BorderStroke(1.dp, AC.Border),
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
