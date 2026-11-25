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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import it.androcalcio.R
import androidx.compose.ui.unit.sp

// Due temi: "scuro" (design canvas Androcalcio) e "PC Calcio" (turchese, stile dei vecchi gestionali).
// I colori sono letti da uno stato: cambiando AC.pcCalcio tutte le schermate si ridisegnano.
object AC {
    /** 0 = scuro, 1 = turchese PC Calcio, 2 = blu navy PC Calcio, 3 = PC Calcio 7 (viola-blu, tabelle bianche). */
    var tema by mutableIntStateOf(0)

    /** Il tema "PC Calcio 7" ha forme squadrate, pulsanti neri e tabelle su carta bianca. */
    val pc7 get() = tema == 3 || tema == 4 || tema == 5

    /** 5 = chiaro: sfondi e riquadri chiari, testo scuro. I pannelli navy e i pulsanti neri restano scuri. */
    val chiaro get() = tema == 5

    /** 6 = manga: blu notte, rosso shōnen, bordi a china e ombre da vignetta di fumetto. */
    val manga get() = tema == 6

    private fun pick(a: Long, b: Long, c: Long, d: Long = c, e: Long = d, f: Long = d, g: Long = a) =
        Color(when (tema) { 1 -> b; 2 -> c; 3 -> d; 4 -> e; 5 -> f; 6 -> g; else -> a })

    /** Da preferenza (0 = blu PC Calcio 7, 1 = nero, 2 = chiaro, 3 = manga) a tema interno. */
    fun daPreferenza(v: Int) = when (v) { 1 -> 4; 2 -> 5; 3 -> 6; else -> 3 }

    /** Preferenza predefinita: blu PC Calcio 7 (v0.7: prima era manga). */
    const val PREFERENZA_PREDEFINITA = 0

    val Bg get() = pick(0xFF0A0E13, 0xFF053A40, 0xFF0A1740, 0xFF2A3A8C, 0xFF000000, 0xFFEEF1F7, g = 0xFF12152B)
    val Surface get() = pick(0xFF131A22, 0xFF0C5A60, 0xFF14307A, 0xFF27358A, 0xFF0A0A0F, 0xFFFFFFFF, g = 0xFF1D2245)
    val SurfaceHi get() = pick(0xFF1B2530, 0xFF14787D, 0xFF2243A0, 0xFF3B4FA6, 0xFF171A2B, 0xFFDDE3F0, g = 0xFF2A3166)
    val Border get() = pick(0x1FFFFFFF, 0x80A5F2EC, 0x80B4C8FF, 0xFFA9B9EA, 0xFFA9B9EA, 0xFF9AA4C4, g = 0xFFFFFFFF)
    val Accent get() = pick(0xFFB6FF3C, 0xFFFFE14A, 0xFFFFD21F, 0xFFFFE23D, 0xFFFFE23D, 0xFFC27A00, g = 0xFFFF4040)
    val Accent2 get() = pick(0xFF4FD1FF, 0xFF5FF5E6, 0xFF7FC4FF, 0xFF9CCBFF, 0xFF9CCBFF, 0xFF1565C0, g = 0xFF46C2FF)
    val Ok get() = pick(0xFF3DDC84, 0xFF7CF08A, 0xFF7CF08A, 0xFF7CF08A, 0xFF7CF08A, 0xFF2E7D32, g = 0xFF5BE37D)
    val Warn get() = Color(0xFFFFC24B)
    val Danger get() = pick(0xFFFF5C72, 0xFFFF6B6B, 0xFFFF7A5C, 0xFFFF6B5C, 0xFFFF6B5C, 0xFFC62828, g = 0xFFFF5C5C)
    val Text get() = pick(0xFFE8EEF4, 0xFFF2FBFA, 0xFFF4F7FF, 0xFFF6F8FF, 0xFFF6F8FF, 0xFF101418, g = 0xFFFFFFFF)
    val TextDim get() = pick(0xFF8FA0B2, 0xFFA9DAD6, 0xFFB4C4EE, 0xFFD9E1FF, 0xFFD9E1FF, 0xFF4A5468, g = 0xFFC8D0F0)

    // Tabelle su carta bianca (stile PC Calcio 7): valgono per tutti i temi, le usano le tabelle dei passi successivi.
    val Paper = Color(0xFFFFFFFF)
    val PaperAlt = Color(0xFFE6EAF6)
    val PaperText = Color(0xFF101418)
    val PaperDim = Color(0xFF4A5468)
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
    /** Pannelli e pulsanti navy PC 7 (menu, riquadri), bordo azzurro e testo secondario ad alto contrasto. */
    val NavyTop = Color(0xFF26338A)
    val NavyBottom = Color(0xFF0E1550)
    val NavyBorder = Color(0xFF9DB4F2)
    val NavyText = Color(0xFFD9E1FF)
    /** Banda delle intestazioni di sezione ("GIOCATORI CONVOCATI"). */
    val SectionTop = Color(0xFF5C7BD6)
    val SectionBottom = Color(0xFF223A98)

    // Stile "Androcalcio": menu e tabelle PC Calcio 7, partita dal vivo in notturna con luci neon.
    // Ciano = la squadra di casa (e i tuoi dati in evidenza), magenta = gli ospiti. Stessi colori in tutta l'app.
    val Night = Color(0xFF060B24)
    val NightHi = Color(0xFF0F1A45)
    val Neon = Color(0xFF3DE8FF)
    val NeonPink = Color(0xFFFF3D8B)
    val PitchA = Color(0xFF2F7D3C)
    val PitchB = Color(0xFF29703A)
    val GoalBanner = Color(0xFFFFE23D)
}

private fun scheme() = (if (AC.chiaro) androidx.compose.material3.lightColorScheme(
    primary = AC.Accent, onPrimary = AC.Surface, secondary = AC.Accent2, background = AC.Bg, onBackground = AC.Text,
    surface = AC.Surface, onSurface = AC.Text, surfaceVariant = AC.SurfaceHi, onSurfaceVariant = AC.TextDim, error = AC.Danger
) else darkColorScheme(
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
))

/** Caratteri stile PC Calcio 7: corpo stretto e squadrato (Roboto Condensed), titoli pesanti (Archivo Black). */
val PcBody = FontFamily(
    Font(R.font.pc_body, FontWeight.Normal),
    Font(R.font.pc_body, FontWeight.Medium),
    Font(R.font.pc_body_bold, FontWeight.SemiBold),
    Font(R.font.pc_body_bold, FontWeight.Bold),
    Font(R.font.pc_body_black, FontWeight.ExtraBold),
    Font(R.font.pc_body_black, FontWeight.Black)
)
val PcTitle = FontFamily(Font(R.font.pc_title, FontWeight.Normal))

private val AndrocalcioTypography = Typography(
    headlineMedium = TextStyle(fontFamily = PcTitle, fontSize = 24.sp),
    headlineSmall = TextStyle(fontFamily = PcTitle, fontSize = 19.sp),
    titleLarge = TextStyle(fontFamily = PcTitle, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = PcBody, fontSize = 16.sp, fontWeight = FontWeight.Bold),
    titleSmall = TextStyle(fontFamily = PcBody, fontSize = 14.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontFamily = PcBody, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = PcBody, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = PcBody, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = PcBody, fontSize = 14.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontFamily = PcBody, fontSize = 12.sp, fontWeight = FontWeight.Bold),
    labelSmall = TextStyle(fontFamily = PcBody, fontSize = 11.sp, fontWeight = FontWeight.Bold)
)

@Composable
fun AndrocalcioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme(),
        typography = AndrocalcioTypography,
        content = content
    )
}
