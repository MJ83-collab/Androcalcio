package it.androcalcio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Due temi: "scuro" (design canvas Androcalcio) e "PC Calcio" (turchese, stile dei vecchi gestionali).
// I colori sono letti da uno stato: cambiando AC.pcCalcio tutte le schermate si ridisegnano.
object AC {
    var pcCalcio by mutableStateOf(false)

    val Bg get() = if (pcCalcio) Color(0xFF053A40) else Color(0xFF0A0E13)
    val Surface get() = if (pcCalcio) Color(0xFF0C5A60) else Color(0xFF131A22)
    val SurfaceHi get() = if (pcCalcio) Color(0xFF14787D) else Color(0xFF1B2530)
    val Border get() = if (pcCalcio) Color(0x80A5F2EC) else Color(0x1FFFFFFF)
    val Accent get() = if (pcCalcio) Color(0xFFFFE14A) else Color(0xFFB6FF3C)
    val Accent2 get() = if (pcCalcio) Color(0xFF5FF5E6) else Color(0xFF4FD1FF)
    val Ok get() = if (pcCalcio) Color(0xFF7CF08A) else Color(0xFF3DDC84)
    val Warn get() = Color(0xFFFFC24B)
    val Danger get() = if (pcCalcio) Color(0xFFFF6B6B) else Color(0xFFFF5C72)
    val Text get() = if (pcCalcio) Color(0xFFF2FBFA) else Color(0xFFE8EEF4)
    val TextDim get() = if (pcCalcio) Color(0xFFA9DAD6) else Color(0xFF8FA0B2)
}

private fun scheme() = darkColorScheme(
    primary = AC.Accent,
    onPrimary = AC.Bg,
    secondary = AC.Accent2,
    background = AC.Bg,
    onBackground = AC.Text,
    surface = AC.Surface,
    onSurface = AC.Text,
    surfaceVariant = AC.SurfaceHi,
    onSurfaceVariant = AC.TextDim,
    error = AC.Danger
)

private val AndrocalcioTypography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun AndrocalcioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme(),
        typography = AndrocalcioTypography,
        content = content
    )
}
