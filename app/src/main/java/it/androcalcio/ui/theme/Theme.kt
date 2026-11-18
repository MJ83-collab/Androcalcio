package it.androcalcio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Due temi: "scuro" (design canvas Androcalcio) e "PC Calcio" (turchese, stile dei vecchi gestionali).
// I colori sono letti da uno stato: cambiando AC.pcCalcio tutte le schermate si ridisegnano.
object AC {
    /** 0 = scuro, 1 = turchese PC Calcio, 2 = blu navy PC Calcio. */
    var tema by mutableIntStateOf(0)

    private fun pick(a: Long, b: Long, c: Long) = Color(when (tema) { 1 -> b; 2 -> c; else -> a })

    val Bg get() = pick(0xFF0A0E13, 0xFF053A40, 0xFF0A1740)
    val Surface get() = pick(0xFF131A22, 0xFF0C5A60, 0xFF14307A)
    val SurfaceHi get() = pick(0xFF1B2530, 0xFF14787D, 0xFF2243A0)
    val Border get() = pick(0x1FFFFFFF, 0x80A5F2EC, 0x80B4C8FF)
    val Accent get() = pick(0xFFB6FF3C, 0xFFFFE14A, 0xFFFFD21F)
    val Accent2 get() = pick(0xFF4FD1FF, 0xFF5FF5E6, 0xFF7FC4FF)
    val Ok get() = pick(0xFF3DDC84, 0xFF7CF08A, 0xFF7CF08A)
    val Warn get() = Color(0xFFFFC24B)
    val Danger get() = pick(0xFFFF5C72, 0xFFFF6B6B, 0xFFFF7A5C)
    val Text get() = pick(0xFFE8EEF4, 0xFFF2FBFA, 0xFFF4F7FF)
    val TextDim get() = pick(0xFF8FA0B2, 0xFFA9DAD6, 0xFFB4C4EE)
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
