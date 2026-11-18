package it.androcalcio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Token dal design canvas "Androcalcio - Grafica 2026"
private val BgDark = Color(0xFF0A0E13)
private val SurfaceDark = Color(0xFF131A22)
private val SurfaceVariantDark = Color(0xFF1B2530)
private val AccentLime = Color(0xFFB6FF3C)
private val AccentCyan = Color(0xFF4FD1FF)
private val TextMain = Color(0xFFE8EEF4)
private val DangerRed = Color(0xFFFF5C72)

private val AndrocalcioDark = darkColorScheme(
    primary = AccentLime,
    onPrimary = BgDark,
    secondary = AccentCyan,
    background = BgDark,
    onBackground = TextMain,
    surface = SurfaceDark,
    onSurface = TextMain,
    surfaceVariant = SurfaceVariantDark,
    error = DangerRed
)

@Composable
fun AndrocalcioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AndrocalcioDark, content = content)
}
