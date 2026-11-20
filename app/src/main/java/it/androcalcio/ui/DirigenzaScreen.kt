package it.androcalcio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel.ClubState
import it.androcalcio.data.Team
import it.androcalcio.engine.Club
import it.androcalcio.engine.ClubRequests
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

@Composable
fun DirigenzaScreen(team: Team?, c: ClubState, onRequest: (String, Long) -> Unit) {
    if (team == null) return
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PaperPanel(Modifier.fillMaxWidth()) {
            listOf(
                "Fiducia della dirigenza" to "${c.fiducia}/100 · ${Club.fiduciaLabel(c.fiducia)}",
                "Umore dei tifosi" to "${c.tifosi}/100",
                "Reputazione allenatore" to "${c.reputazione}/100",
                "Clima di squadra" to "${c.clima}/100"
            ).forEachIndexed { i, (k, v) ->
                PaperRow(i) {
                    Text(k, Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(v, color = AC.ColBlue, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        if (c.esonero) {
            GlassCard(Modifier.fillMaxWidth()) {
                Text("La dirigenza ti ha esonerato: fiducia sotto ${Club.ESONERO_SOGLIA}.",
                    Modifier.padding(10.dp), color = AC.Danger, style = MaterialTheme.typography.bodyMedium)
            }
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionTitle("Richiesta fondi")
                Text(
                    if (Club.canRequestFunds(c.fiducia, c.tifosi))
                        "Massimo ora: ${money(Club.maxFunds(c.fiducia))}. Una richiesta per tipo e per stagione."
                    else "Servono fiducia almeno ${Club.FONDI_FIDUCIA_MIN} e tifosi almeno ${Club.FONDI_TIFOSI_MIN}.",
                    color = AC.TextDim, style = MaterialTheme.typography.bodySmall
                )
                ClubRequests.TIPI.forEach { tipo ->
                    val done = tipo in c.richieste
                    Text(tipo.replaceFirstChar { it.uppercase() } + if (done) " (già richiesto)" else "",
                        color = AC.Text, style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ClubRequests.IMPORTI.forEach { imp ->
                            PcButton(money(imp), { onRequest(tipo, imp) }, enabled = !done)
                        }
                    }
                }
            }
        }
    }
}
