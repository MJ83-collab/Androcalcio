package it.androcalcio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.engine.AssistenteLive
import it.androcalcio.engine.AvatarGen
import it.androcalcio.engine.EventType
import it.androcalcio.engine.MatchEvent
import it.androcalcio.ui.components.AvatarSpec
import it.androcalcio.ui.components.LocalAvatarModificati
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.components.vignetta

private val China = Color(0xFF111111)

/** Telecronaca a fumetto: l'ultimo episodio in grassetto e, sotto, l'ultimo commento del telecronista. */
@Composable
fun TelecronacaBubble(events: List<MatchEvent>, minuto: Int) {
    val episodio = events.lastOrNull { it.type !in setOf(EventType.KICKOFF, EventType.TACTIC, EventType.COMMENTO) }
    val commento = events.lastOrNull { it.type == EventType.COMMENTO }?.takeIf { minuto - it.minute <= 6 }
    Row(Modifier.fillMaxWidth().vignetta(2.dp).background(Color.White).border(2.dp, China).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("🎙️", fontSize = 22.sp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(episodio?.let { "${it.androcalcio.engine.etichettaMinuto(it.minute)}' ${it.text}" } ?: "La partita sta per cominciare...",
                color = China, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, maxLines = 2)
            if (commento != null) Text(commento.text, color = Color(0xFF1E3FA0), fontStyle = FontStyle.Italic, fontSize = 12.sp, maxLines = 3)
        }
    }
}

/** Il vice dell'allenatore: volto, consiglio e due pulsanti (applica / ignora). */
@Composable
fun AssistenteBubble(c: AssistenteLive.Consiglio, onApplica: () -> Unit, onChiudi: () -> Unit) {
    val mappa = LocalAvatarModificati.current
    val colore = when (c.urgenza) { 3 -> Color(0xFFFF4040); 2 -> Color(0xFFFFB020); else -> Color(0xFF3DE8FF) }
    Row(Modifier.fillMaxWidth().vignetta(2.dp).background(Color.White).border(3.dp, colore).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AvatarSpec(AvatarGen.specDi("Il vice", mappa), 46.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(if (c.urgenza >= 3) "IL VICE · URGENTE" else "IL VICE", color = colore.copy(alpha = 1f).let { if (c.urgenza == 1) Color(0xFF1E3FA0) else it },
                fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
            Text(c.testo, color = China, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (c.azione != null) PcChip(true, onApplica, { Text(when (c.azione) {
                    is AssistenteLive.Azione.Cambio -> "Scegli il cambio"; else -> "Applica" }, fontSize = 12.sp) })
                PcChip(false, onChiudi, { Text("Ignora", fontSize = 12.sp, color = Color.White) })
            }
        }
    }
}
