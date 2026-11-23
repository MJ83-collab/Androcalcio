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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel.ClubState
import it.androcalcio.data.Team
import it.androcalcio.engine.Club
import it.androcalcio.engine.ClubOffer
import it.androcalcio.engine.ClubOffers
import it.androcalcio.engine.ClubRequests
import it.androcalcio.engine.SponsorDeal
import it.androcalcio.engine.Sponsors
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

@Composable
fun DirigenzaScreen(team: Team?, c: ClubState, teams: List<Team>, onRequest: (String, Long) -> Unit,
                    onOffer: (ClubOffer, Boolean) -> Unit, onSponsor: (SponsorDeal) -> Unit = {}, onRejectSponsors: () -> Unit = {},
                    onDimettiti: () -> Unit = {}) {
    var confermaDimissioni by remember { mutableStateOf(false) }
    if (team == null) return
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PcButton("Dimettiti e cambia squadra", { confermaDimissioni = true }, Modifier.fillMaxWidth())
        if (confermaDimissioni) {
            AlertDialog(
                onDismissRequest = { confermaDimissioni = false },
                title = { Text("Dimissioni") },
                text = { Text("Ti dimetti dalla panchina: la reputazione scende di 15 punti e potrai scegliere una nuova squadra.") },
                confirmButton = { TextButton(onClick = { confermaDimissioni = false; onDimettiti() }) { Text("Dimettiti") } },
                dismissButton = { TextButton(onClick = { confermaDimissioni = false }) { Text("Annulla") } }
            )
        }
        PaperPanel(Modifier.fillMaxWidth()) {
            listOf(
                "Fiducia della dirigenza" to "${c.fiducia}/100 · ${Club.fiduciaLabel(c.fiducia)}",
                "Umore dei tifosi" to "${c.tifosi}/100",
                "Reputazione allenatore" to "${c.reputazione}/100",
                "Clima di squadra" to "${c.clima}/100",
                "Tuo ingaggio (annuo)" to money(c.stipendioAllenatore)
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
                    Text((if (tipo == "ingaggi") "Monte ingaggi (in più ogni anno)" else tipo.replaceFirstChar { it.uppercase() }) + if (done) " (già richiesto)" else "",
                        color = AC.Text, style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ClubRequests.importi(tipo, c.reputazione, c.tifosi).forEach { imp ->
                            PcButton(money(imp), { onRequest(tipo, imp) }, enabled = !done)
                        }
                    }
                }
            }
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionTitle("Sponsor (${c.sponsor.size}/${Sponsors.MAX_ATTIVI})")
                if (c.sponsor.isEmpty()) Text("Nessun contratto attivo.", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                c.sponsor.forEachIndexed { i, d ->
                    Text("${if (i == 0) "Maglia · " else ""}${d.name}: ${money(d.annual)} l'anno, ${d.years} ${if (d.years == 1) "anno" else "anni"} rimasti",
                        color = AC.Text, style = MaterialTheme.typography.bodySmall)
                }
                if (c.sponsorOfferte.isNotEmpty()) {
                    Text("Offerte per il prossimo contratto (più anni = un po' meno all'anno; vittorie e tifosi alzano le offerte):",
                        color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                    c.sponsorOfferte.forEach { d ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${d.name}\n${money(d.annual)} × ${d.years}", Modifier.weight(1f),
                                color = AC.Text, style = MaterialTheme.typography.bodySmall)
                            PcButton("Firma", { onSponsor(d) }, main = true)
                        }
                    }
                    PcButton("Rifiuta tutte", onRejectSponsors)
                }
            }
        }
        if (c.offerte.isNotEmpty()) {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionTitle("Offerte di panchina")
                    c.offerte.forEach { o ->
                        val t = teams.firstOrNull { it.id == o.teamId } ?: return@forEach
                        Text(t.name, color = AC.Text, style = MaterialTheme.typography.titleSmall)
                        Text("Garanzia ${money(o.garanzia)} · ingaggio ${money(o.stipendio)} l'anno",
                            color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PcButton("Accetta", { onOffer(o, false) }, main = true)
                            PcButton("Chiedi +50%", { onOffer(o, true) })
                        }
                    }
                    Text("Chiedere più garanzie funziona solo con reputazione ${ClubOffers.RICHIESTA_GARANZIE_REP_MIN}+, altrimenti il club ritira l'offerta.",
                        color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
