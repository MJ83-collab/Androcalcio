package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.androcalcio.data.Staff
import it.androcalcio.data.Team
import it.androcalcio.engine.Staffing
import it.androcalcio.engine.Stile
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC
import java.util.Locale

/** Cinque stelle per un'abilità 1-99 (scala 0-10), con mezze stelle. */
private fun stelleTesto(skill: Int): String {
    val s = Staffing.stelle(skill)
    val piene = s.toInt()
    val mezza = s - piene >= 0.5
    return "★".repeat(piene) + (if (mezza) "◐" else "") + "☆".repeat(5 - piene - (if (mezza) 1 else 0))
}

private fun punteggio(skill: Int): String = String.format(Locale.ITALY, "%.1f/10", skill / 10.0)

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
    var aperto by remember { mutableStateOf<Staff?>(null) }
    aperto?.let { s ->
        DettaglioStaff(
            s, candidato = s.teamId == 0L,
            onHire = { onHire(s); aperto = null },
            onFire = { onFire(s); aperto = null },
            onClose = { aperto = null }
        )
    }

    val gruppi = listOf(
        "Staff tecnico" to Staffing.POSTI.filter { it.role in listOf(Staffing.ALLENATORE, Staffing.ASSISTENTE, Staffing.GIOVANILI) },
        "Preparatori atletici (6)" to Staffing.POSTI.filter { it.role == Staffing.PREPARATORE },
        "Fisioterapisti (2)" to Staffing.POSTI.filter { it.role == Staffing.FISIOTERAPISTA }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Text("Staff", style = MaterialTheme.typography.headlineSmall) }
        item {
            Text(
                "Tocca una figura per vedere bonus e malus. Lo stipendio si paga a fine stagione, " +
                    "insieme agli ingaggi. Assumere una figura sostituisce quella in carica dello stesso posto.",
                style = MaterialTheme.typography.bodySmall, color = AC.TextDim
            )
        }
        gruppi.forEach { (titolo, posti) ->
            item { SectionTitle(titolo) }
            items(posti) { posto ->
                val s = staff.firstOrNull { Staffing.stessoPosto(it, posto) }
                GlassCard(Modifier.fillMaxWidth().then(if (s != null) Modifier.clickable { aperto = s } else Modifier)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(Staffing.roleName(posto.role, posto.specialita), style = MaterialTheme.typography.titleSmall)
                        if (s == null) {
                            Text("vacante", color = AC.Accent, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stelleTesto(s.skill), color = AC.Accent)
                                Text("${punteggio(s.skill)} · ${s.name}", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
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
            GlassCard(Modifier.fillMaxWidth().clickable { aperto = c }) {
                Column(Modifier.padding(12.dp)) {
                    Text("${Staffing.roleName(c.role, c.specialita)}: ${c.name}", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${stelleTesto(c.skill)} · ${punteggio(c.skill)} · ${money(c.salary)} l'anno · contratto ${c.contract} anni",
                        style = MaterialTheme.typography.bodySmall, color = AC.TextDim
                    )
                }
            }
        }
    }
}

@Composable
private fun DettaglioStaff(
    s: Staff, candidato: Boolean, onHire: () -> Unit, onFire: () -> Unit, onClose: () -> Unit
) {
    val (bonus, malus) = Staffing.effetti(s)
    val stile = Stile.values().firstOrNull { it.name == s.stile } ?: Stile.DIFESA
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(Staffing.roleName(s.role, s.specialita)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${s.name} · ${stelleTesto(s.skill)} · ${punteggio(s.skill)}")
                Text("${money(s.salary)} l'anno · contratto ${s.contract} anni", color = AC.TextDim)
                if (s.role == Staffing.ALLENATORE) Text(stile.descrizione(), color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                Text("Bonus", style = MaterialTheme.typography.titleSmall)
                if (bonus.isEmpty()) Text("Nessun bonus", color = AC.TextDim)
                bonus.forEach { Text("+ $it", color = AC.Ok) }
                Text("Malus", style = MaterialTheme.typography.titleSmall)
                if (malus.isEmpty()) Text("Nessun malus", color = AC.TextDim)
                malus.forEach { Text("− $it", color = AC.Danger) }
            }
        },
        confirmButton = {
            if (candidato) PcAction(onClick = onHire) { Text("Ingaggia") }
            else TextButton(onClick = onFire) { Text("Licenzia") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Chiudi") } }
    )
}
