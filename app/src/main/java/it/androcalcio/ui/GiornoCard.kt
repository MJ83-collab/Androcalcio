package it.androcalcio.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.engine.CalendarioGiorni
import it.androcalcio.engine.CalendarioGiorni.Attivita
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.theme.AC
import java.time.DayOfWeek

/** Il giorno corrente del calendario: data, prossima partita, settimana, preparazione e scelta dell'attività. */
@Composable
fun GiornoCard(g: AndrocalcioViewModel.GiornoStato, onAttivita: (Attivita) -> Unit, onSalta: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("📅 " + CalendarioGiorni.nomeGiorno(g.oggi), color = AC.Accent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            val prossima = g.prossima
            if (prossima != null) {
                val vs = g.avversario?.let { (if (g.casa) "in casa contro " else "in trasferta contro ") + it } ?: "la tua squadra riposa"
                Text(
                    if (g.giornoPartita) "⚽ Oggi si gioca: giornata ${g.giornata}, $vs."
                    else "Prossima partita: ${CalendarioGiorni.nomeGiorno(prossima)}, tra ${g.giorniMancanti} " +
                        (if (g.giorniMancanti == 1) "giorno" else "giorni") + ". Giornata ${g.giornata}, $vs.",
                    color = AC.Text, fontSize = 12.sp
                )
            } else {
                Text("Nessuna partita in programma: la stagione è finita.", color = AC.Text, fontSize = 12.sp)
            }
            Settimana(g)
            Text(
                "Preparazione " + "★".repeat(g.prep) + "☆".repeat(CalendarioGiorni.PREP_MAX - g.prep) +
                    "  ·  morale +${g.prep * 2} alla prossima partita",
                color = AC.TextDim, fontSize = 11.sp
            )
            if (prossima != null && !g.giornoPartita) {
                Text("COSA FAI OGGI?", color = AC.Text, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                Attivita.values().filter { it != Attivita.RITIRO || g.oggi.monthValue in 7..8 }.chunked(2).forEach { riga ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        riga.forEach { a ->
                            if (AC.pc7) NastroTesto("${a.icona} ${a.label}", a.info, ColoriNastri[a.ordinal % ColoriNastri.size], Modifier.weight(1f)) { onAttivita(a) }
                            else PcChip(false, { onAttivita(a) }, {
                                Column {
                                    Text("${a.icona} ${a.label}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                    Text(a.info, fontSize = 10.sp)
                                }
                            }, Modifier.weight(1f))
                        }
                    }
                }
                if (g.giorniMancanti > 1) {
                    PcButton("Salta alla partita (${g.giorniMancanti} giorni automatici)", onSalta, Modifier.fillMaxWidth(), compatto = true)
                }
            }
        }
    }
}

/** Settimana corrente da lunedì a domenica: oggi evidenziato, partita col pallone, giorni passati con l'attività svolta. */
@Composable
private fun Settimana(g: AndrocalcioViewModel.GiornoStato) {
    val lunedi = g.oggi.with(DayOfWeek.MONDAY)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 0..6) {
            val d = lunedi.plusDays(i.toLong())
            val oggi = d == g.oggi
            val attivita = g.diario[d.toEpochDay()]
            Column(
                Modifier.weight(1f).border(if (oggi) 2.dp else 1.dp, if (oggi) AC.Accent else AC.Border).padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(CalendarioGiorni.giornoBreve(d), fontSize = 10.sp, color = AC.TextDim)
                Text("${d.dayOfMonth}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AC.Text)
                Text(
                    when {
                        d == g.prossima -> "⚽"
                        attivita != null -> attivita.icona
                        d.isBefore(g.oggi) -> "·"
                        else -> " "
                    }, fontSize = 13.sp
                )
            }
        }
    }
}
