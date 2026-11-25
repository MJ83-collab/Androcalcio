package it.androcalcio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import it.androcalcio.data.Team
import it.androcalcio.ui.components.Crest
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Player
import it.androcalcio.engine.Market
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

/** Colore di un valore 0..99 nelle tabelle: verde alto, nero medio, arancio basso, rosso molto basso. */
fun coloreValore(v: Int): Color = when {
    v >= 80 -> AC.ColGreen
    v >= 65 -> AC.PaperText
    v >= 50 -> AC.ColOrange
    else -> AC.ColEn
}

/** Colore degli anni di contratto rimasti: 1 anno o meno rosso, 2 arancio, di più verde. */
fun coloreContratto(anni: Int): Color = when {
    anni <= 1 -> AC.ColEn
    anni == 2 -> AC.ColOrange
    else -> AC.ColGreen
}

fun etichettaContratto(anni: Int): String = when {
    anni <= 0 -> "SCADUTO"
    anni == 1 -> "IN SCADENZA"
    else -> "$anni ANNI"
}

/** Riquadro contratto della scheda giocatore: stato, ingaggio, anni, valore di mercato. */
@Composable
fun ContrattoBlock(p: Player) {
    val valore = Market.value(p)
    val pct = if (valore > 0) (p.salary * 100 / valore).toInt() else 0
    Column(Modifier.fillMaxWidth().background(AC.PaperAlt).padding(6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("CONTRATTO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = AC.ColBlue)
            Text(etichettaContratto(p.contract), Modifier.background(coloreContratto(p.contract)).padding(horizontal = 6.dp, vertical = 1.dp),
                color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Cella("INGAGGIO ANNUO", money(p.salary))
            Cella("MESE", money(p.salary / 12))
            Cella("VALORE", money(valore))
        }
        Text("L'ingaggio è il $pct% del valore di mercato.", fontSize = 10.sp, color = AC.PaperText)
    }
}

@Composable
private fun Cella(titolo: String, valore: String) {
    Column {
        Text(titolo, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = AC.ColBlue)
        Text(valore, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AC.PaperText)
    }
}

/** Intestazione di una partita come nel menu di PC Calcio 7: stemma e nome a sinistra, riquadro bianco al centro, nome e stemma a destra. */
@Composable
fun BannerPartita(casa: Team, ospite: Team, titolo: String, numero: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF8FB0E0), Color(0xFF4A6BA8))))
            .border(2.dp, Color(0xFF1C2D52), RoundedCornerShape(4.dp)).padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Crest(casa, 40.dp)
            Text(casa.name, Modifier.weight(1f).padding(horizontal = 4.dp).background(Color(0xFF0E1A33)).padding(horizontal = 4.dp, vertical = 2.dp),
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
        }
        Column(
            Modifier.width(78.dp).clip(RoundedCornerShape(3.dp)).background(Color.White).border(1.dp, Color(0xFF1C2D52)).padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(titolo, color = Color(0xFFC62828), fontWeight = FontWeight.Black, fontSize = 11.sp, maxLines = 1)
            Text(numero, color = Color(0xFF111111), fontWeight = FontWeight.Black, fontSize = 16.sp, maxLines = 1)
        }
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(ospite.name, Modifier.weight(1f).padding(horizontal = 4.dp).background(Color(0xFF0E1A33)).padding(horizontal = 4.dp, vertical = 2.dp),
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, textAlign = TextAlign.End)
            Crest(ospite, 40.dp)
        }
    }
}

/** Colori dei nastri delle attività del calendario. */
val ColoriNastri = listOf(Color(0xFF4E8A55), Color(0xFF4F6E9E), Color(0xFF9C5A7E), Color(0xFF9A7A55), Color(0xFFC0742B))

/** Pulsante a nastro con due righe di testo (titolo e spiegazione), come i pulsanti del menu di PC Calcio 7. */
@Composable
fun NastroTesto(titolo: String, info: String, colore: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.height(52.dp).clickable(onClick = onClick), contentAlignment = Alignment.CenterStart) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height; val sl = h * 0.3f
            val p = Path().apply { moveTo(sl, 0f); lineTo(w, 0f); lineTo(w - sl, h); lineTo(0f, h); close() }
            drawPath(p, Brush.verticalGradient(listOf(
                Color((colore.red + 0.25f).coerceAtMost(1f), (colore.green + 0.25f).coerceAtMost(1f), (colore.blue + 0.25f).coerceAtMost(1f)),
                colore, Color(colore.red * 0.6f, colore.green * 0.6f, colore.blue * 0.6f))))
            drawPath(p, Color(0xFF0E1A33), style = Stroke(width = 3f))
            drawLine(Color.White.copy(alpha = 0.5f), Offset(sl + 4f, 3f), Offset(w - 6f, 3f), strokeWidth = 2f)
        }
        Column(Modifier.padding(start = 16.dp, end = 14.dp)) {
            Text(titolo.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1,
                style = TextStyle(shadow = Shadow(Color.Black, Offset(1.5f, 1.5f), 1f)))
            Text(info, color = Color.White, fontSize = 9.sp, maxLines = 2, lineHeight = 10.sp,
                style = TextStyle(shadow = Shadow(Color.Black, Offset(1f, 1f), 1f)))
        }
    }
}
