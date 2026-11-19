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
    /** 0 = scuro, 1 = turchese PC Calcio, 2 = blu navy PC Calcio, 3 = PC Calcio 7 (viola-blu, tabelle bianche). */
    var tema by mutableIntStateOf(0)

    /** Il tema "PC Calcio 7" ha forme squadrate, pulsanti neri e tabelle su carta bianca. */
    val pc7 get() = tema == 3

    private fun pick(a: Long, b: Long, c: Long, d: Long = c) = Color(when (tema) { 1 -> b; 2 -> c; 3 -> d; else -> a })

    val Bg get() = pick(0xFF0A0E13, 0xFF053A40, 0xFF0A1740, 0xFF5A4C9E)
    val Surface get() = pick(0xFF131A22, 0xFF0C5A60, 0xFF14307A, 0xFF27358A)
    val SurfaceHi get() = pick(0xFF1B2530, 0xFF14787D, 0xFF2243A0, 0xFF3B4FA6)
    val Border get() = pick(0x1FFFFFFF, 0x80A5F2EC, 0x80B4C8FF, 0xFFA9B9EA)
    val Accent get() = pick(0xFFB6FF3C, 0xFFFFE14A, 0xFFFFD21F, 0xFFFFE23D)
    val Accent2 get() = pick(0xFF4FD1FF, 0xFF5FF5E6, 0xFF7FC4FF, 0xFF9CCBFF)
    val Ok get() = pick(0xFF3DDC84, 0xFF7CF08A, 0xFF7CF08A, 0xFF7CF08A)
    val Warn get() = Color(0xFFFFC24B)
    val Danger get() = pick(0xFFFF5C72, 0xFFFF6B6B, 0xFFFF7A5C, 0xFFFF6B5C)
    val Text get() = pick(0xFFE8EEF4, 0xFFF2FBFA, 0xFFF4F7FF, 0xFFF6F8FF)
    val TextDim get() = pick(0xFF8FA0B2, 0xFFA9DAD6, 0xFFB4C4EE, 0xFFCDD5F3)

    // Tabelle su carta bianca (stile PC Calcio 7): valgono per tutti i temi, le usano le tabelle dei passi successivi.
    val Paper = Color(0xFFFFFFFF)
    val PaperAlt = Color(0xFFE6EAF6)
    val PaperText = Color(0xFF101418)
    val PaperLine = Color(0xFF9AA4C4)
    val Mine = Color(0xFFFFF0A8)   // riga della tua squadra
    /** Colori delle intestazioni: EN rosso, VE/RE/AG/QU blu, SF/MO verde, ME arancio. */
    val ColEn = Color(0xFFC62828)
    val ColBlue = Color(0xFF1E3FA0)
    val ColGreen = Color(0xFF2E7D32)
    val ColOrange = Color(0xFFD96A00)
    /** Pulsanti neri e banda del titolo. */
    val Btn = Color(0xFF0B0B10)
    val BtnBorder = Color(0xFF6B6F80)
    val BannerTop = Color(0xFFA6C0F0)
    val BannerBottom = Color(0xFF4F6FB8)
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
