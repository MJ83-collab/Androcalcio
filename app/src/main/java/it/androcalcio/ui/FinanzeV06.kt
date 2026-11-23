package it.androcalcio.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.androcalcio.AndrocalcioViewModel.FinExtra
import it.androcalcio.data.Team
import it.androcalcio.engine.Cartellini
import it.androcalcio.engine.NuovoStadio
import it.androcalcio.engine.Prato
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

/** Finanze v0.6: proprietà, cessioni (rate e % di rivendita), plusvalenze e ammortamenti, nuovo stadio, prato. */
@Composable
fun FinanzeV06(team: Team, e: FinExtra, onRivendita: (Boolean) -> Unit, onNuovoStadio: (Int) -> Unit, onCura: (Int) -> Unit) {
    @Composable fun piccolo(t: String, c: androidx.compose.ui.graphics.Color = AC.TextDim) =
        Text(t, color = c, style = MaterialTheme.typography.bodySmall)
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (e.proprietario.isNotEmpty()) {
                SectionTitle("Proprietà")
                piccolo("Proprietario: ${e.proprietario}" + if (e.salvataggi > 1) " (società salvata ${e.salvataggi} volte)" else "", AC.Text)
            }
            SectionTitle("Cessioni")
            piccolo("Con \"A rate\" attivo, anche le cessioni sopra 1 milione si incassano metà subito e metà a fine stagione." +
                if (e.crediti > 0) " Da incassare: ${money(e.crediti)}." else "")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PcChip(!e.rivendita, { onRivendita(false) }, { Text("Cessione secca") })
                PcChip(e.rivendita, { onRivendita(true) }, { Text("Con ${Cartellini.PCT_RIVENDITA}% rivendita") })
            }
            piccolo("Con la percentuale incassi il ${Cartellini.SCONTO_RIVENDITA}% in meno subito, ma il ${Cartellini.PCT_RIVENDITA}% del prezzo se il club lo rivende.")
            e.rivendite.forEach { piccolo("• ${it.nome}: ${it.pct}% sulla rivendita", AC.Text) }
            piccolo("Plusvalenze della stagione: ${money(e.plusvalenze)}", if (e.plusvalenze < 0) AC.Danger else AC.Text)

            SectionTitle("Ammortamenti")
            piccolo("Il prezzo di un acquisto si divide sugli anni di contratto. La plusvalenza è il prezzo di vendita meno il valore ancora da ammortizzare.")
            if (e.cartellini.isEmpty()) piccolo("Nessun cartellino da ammortizzare.")
            e.cartellini.sortedByDescending { it.second.residuo }.forEach { (n, c) ->
                piccolo("• $n: ${money(c.quota)} l'anno · a bilancio ${money(c.residuo)} (${c.anni - c.fatti} anni)", AC.Text)
            }
            if (e.cartellini.isNotEmpty()) piccolo("Ammortamento annuo totale: ${money(Cartellini.ammortamentoAnnuo(e.cartellini.map { it.second }))}", AC.Text)

            SectionTitle("Nuovo stadio")
            val pr = e.stadio
            if (pr != null) piccolo("In costruzione: ${pr.posti} posti. Mancano ${pr.anniRimasti} rate da ${money(pr.rata)} (a fine stagione).", AC.ColOrange)
            else {
                val opz = NuovoStadio.opzioni(team.stadiumCapacity)
                if (opz.isEmpty()) piccolo("Il tuo stadio è già tra i più grandi.")
                else {
                    piccolo("Progetto di ${NuovoStadio.ANNI} stagioni, ${money(NuovoStadio.COSTO_POSTO)} a posto, pagato a rate a fine stagione.")
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        opz.forEach { p -> PcChip(false, { onNuovoStadio(p) }, { Text("$p posti · ${money(NuovoStadio.costo(p))}") }) }
                    }
                }
            }

            SectionTitle("Prato")
            piccolo("Stato: ${e.prato}/100 (${Prato.etichetta(e.prato)}). Sotto 50 la tua squadra crea meno occasioni in casa.",
                if (e.prato < 50) AC.Danger else AC.Text)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Prato.CURE.forEachIndexed { i, n ->
                    PcChip(e.cura == i, { onCura(i) }, { Text(if (i == 0) n else "$n · ${money(Prato.costoCura(i))}") })
                }
            }
        }
    }
}
