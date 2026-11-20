package it.androcalcio.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.androcalcio.data.AlboEntry
import it.androcalcio.engine.Allenatori
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.theme.AC

/** Albo d'oro: campioni di campionato, coppa e supercoppa per stagione, dalla più recente. */
@Composable
fun AlboScreen(albo: List<AlboEntry>, classifica: List<Allenatori.Riga> = emptyList()) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        SectionTitle("Classifica allenatori")
        val tuo = classifica.indexOfFirst { it.mio }
        PaperPanel(Modifier.fillMaxWidth()) {
            classifica.take(30).forEachIndexed { i, r ->
                Text("${i + 1}. ${r.nome} (${r.squadra}) · fama ${r.fama}${if (r.mio) " · tu" else ""}",
                    color = AC.PaperText, modifier = Modifier.padding(vertical = 3.dp))
            }
            if (tuo >= 30) {
                val r = classifica[tuo]
                Text("${tuo + 1}. ${r.nome} (${r.squadra}) · fama ${r.fama} · tu",
                    color = AC.PaperText, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
        if (albo.isEmpty()) {
            Text("Nessuna stagione conclusa: l'albo si riempie a fine stagione.", color = AC.Text,
                modifier = Modifier.padding(8.dp))
        }
        albo.groupBy { it.season }.forEach { (season, rows) ->
            SectionTitle(season)
            PaperPanel(Modifier.fillMaxWidth()) {
                rows.forEach { r ->
                    Text("${r.nazione} · ${r.competizione}: ${r.vincitore}", color = AC.PaperText,
                        modifier = Modifier.padding(vertical = 3.dp))
                }
            }
        }
    }
}
