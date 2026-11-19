package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.androcalcio.data.Staff
import it.androcalcio.data.Team
import it.androcalcio.engine.Staffing
import it.androcalcio.engine.Youths
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.StatChip
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC
import java.util.Locale
import kotlin.math.roundToInt

/** Effetto dichiarato in chiaro, così si capisce cosa cambia assumendo questa figura. */
private fun effetto(s: Staff): String = when (s.role) {
    Staffing.ALLENATORE -> {
        val p = ((Staffing.coachFactor(s.skill) - 1.0) * 100).roundToInt()
        "Partita: ${if (p >= 0) "+" else ""}$p% · crescita dei giocatori fino a 28 anni"
    }
    Staffing.PREPARATORE -> String.format(
        Locale.ITALY, "Rischio infortunio per partita: %.1f%%", Staffing.injuryChance(s.skill) * 100
    )
    Staffing.GIOVANILI -> "Giudizi del vivaio sui giovani: più precisi con abilità alta"
    Staffing.ASSISTENTE -> "Assegna i numeri di maglia a inizio stagione"
    else -> "Osservazioni dello scout: costo e precisione dipendono dall'abilità"
}

@Composable
fun StaffScreen(
    team: Team?,
    staff: List<Staff>,
    candidates: List<Staff>,
    onHire: (Staff) -> Unit,
    onFire: (Staff) -> Unit,
    onRefresh: () -> Unit
) {
    if (team == null) return
    val coach = staff.firstOrNull { it.role == Staffing.ALLENATORE }
    val prep = staff.firstOrNull { it.role == Staffing.PREPARATORE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Text("Staff", style = MaterialTheme.typography.headlineSmall) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("Allenatore", coach?.let { Youths.judge(it.skill) } ?: "vacante", Modifier.weight(1f))
                StatChip("Preparatore", prep?.let { Youths.judge(it.skill) } ?: "vacante", Modifier.weight(1f))
            }
        }
        item {
            Text(
                "Lo stipendio di ogni figura si paga a fine stagione, insieme agli ingaggi. " +
                    "Assumere una figura sostituisce quella in carica dello stesso ruolo.",
                style = MaterialTheme.typography.bodySmall, color = AC.TextDim
            )
        }

        item { SectionTitle("In organico") }
        if (staff.isEmpty()) {
            item {
                Text(
                    "Nessun allenatore né preparatore: la squadra gioca con effetti neutri.",
                    color = AC.TextDim
                )
            }
        }
        items(staff) { s ->
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${Staffing.roleName(s.role)}: ${s.name}", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Livello: ${Youths.judge(s.skill)} · ${money(s.salary)} l'anno · contratto ${s.contract} anni",
                        style = MaterialTheme.typography.bodySmall, color = AC.TextDim
                    )
                    Text(effetto(s), modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodySmall, color = AC.Accent)
                    TextButton(onClick = { onFire(s) }) { Text("Licenzia") }
                }
            }
        }

        item { SectionTitle("Candidati") }
        item {
            PcAction(
                onClick = onRefresh, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)
            ) { Text("Nuovi candidati") }
        }
        // Niente key: i candidati non assunti hanno tutti id 0 e le chiavi dovrebbero essere uniche.
        items(candidates) { c ->
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${Staffing.roleName(c.role)}: ${c.name}", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Livello: ${Youths.judge(c.skill)} · ${money(c.salary)} l'anno · contratto ${c.contract} anni",
                        style = MaterialTheme.typography.bodySmall, color = AC.TextDim
                    )
                    Text(effetto(c), modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodySmall, color = AC.Accent)
                    PcAction(onClick = { onHire(c) }, modifier = Modifier.padding(top = 6.dp)) { Text("Ingaggia") }
                }
            }
        }
    }
}
