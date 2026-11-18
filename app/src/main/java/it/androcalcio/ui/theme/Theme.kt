package it.androcalcio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Token dal design canvas "Androcalcio - Grafica 2026"
object AC {
    val Bg = Color(0xFF0A0E13)
    val Surface = Color(0xFF131A22)
    val SurfaceHi = Color(0xFF1B2530)
    val Border = Color(0x1FFFFFFF)
    val Accent = Color(0xFFB6FF3C)
    val Accent2 = Color(0xFF4FD1FF)
    val Ok = Color(0xFF3DDC84)
    val Warn = Color(0xFFFFC24B)
    val Danger = Color(0xFFFF5C72)
    val Text = Color(0xFFE8EEF4)
    val TextDim = Color(0xFF8FA0B2)
}

private val AndrocalcioDark = darkColorScheme(
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
        colorScheme = AndrocalcioDark,
        typography = AndrocalcioTypography,
        content = content
    )
}
