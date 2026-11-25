package it.androcalcio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Sigle brevi dei ruoli in stile PC Calcio 7, colorate per reparto: POR arancio, difesa blu, centrocampo verde, attacco rosso. */
object Ruoli {
    private val DIFESA = setOf("D", "DIF", "DC", "CS", "CD", "TS", "TD")
    private val CENTRO = setOf("C", "MED", "REG", "INT", "MP", "FS", "FD", "CC", "ES", "ED", "TRQ")
    private val PORTA = setOf("P", "POR")

    /** Reparto (P, D, C, A) di una sigla qualsiasi. */
    fun reparto(sigla: String): String = when (sigla.uppercase()) {
        in PORTA -> "P"; in DIFESA -> "D"; in CENTRO -> "C"; else -> "A"
    }

    /** Sigla da mostrare: ruolo dettagliato se c'è, altrimenti quella del reparto. */
    fun sigla(reparto: String, dettaglio: String = ""): String = dettaglio.ifEmpty {
        when (reparto) { "P" -> "POR"; "D" -> "DIF"; "C" -> "CEN"; else -> "ATT" }
    }

    fun colore(sigla: String): Color = when (reparto(sigla)) {
        "P" -> Color(0xFFD96A00); "D" -> Color(0xFF1E5FC8); "C" -> Color(0xFF2E8B3A); else -> Color(0xFFC62828)
    }
}

/** Pastiglia colorata con la sigla del ruolo (es. "DC", "INT", "CA"). */
@Composable
fun RuoloChip(reparto: String, dettaglio: String = "", modifier: Modifier = Modifier) {
    val s = Ruoli.sigla(reparto, dettaglio)
    Text(s, modifier.clip(RoundedCornerShape(3.dp)).background(Ruoli.colore(s)).widthIn(min = 30.dp)
        .padding(horizontal = 3.dp, vertical = 1.dp),
        color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, maxLines = 1)
}
