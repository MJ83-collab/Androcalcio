package it.androcalcio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
