package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import it.androcalcio.ui.components.Avatar
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
import it.androcalcio.data.ScoutMission
import it.androcalcio.data.Staff
import it.androcalcio.data.Team
import it.androcalcio.engine.Missioni
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
    onRefresh: () -> Unit,
    missioni: List<ScoutMission>,
    onInvia: (Staff, String) -> Unit,
    allenatore: String? = null
) {
    if (team == null) return
    var aperto by remember { mutableStateOf<Staff?>(null) }
    aperto?.let { s ->
        DettaglioStaff(
            s, candidato = s.teamId == 0L,
            onHire = { onHire(s); aperto = null },
            onFire = { onFire(s); aperto = null },
            onClose = { aperto = null },
            missione = missioni.firstOrNull { it.staffId == s.id },
            onInvia = { area -> onInvia(s, area); aperto = null }
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
                    Row(Modifier.padding(start = 12.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    val chi = if (posto.role == Staffing.ALLENATORE && allenatore != null) allenatore else s?.name
                    if (chi != null) Avatar(chi, 52.dp)
                    else Box(Modifier.size(52.dp).background(AC.Surface, CircleShape), contentAlignment = Alignment.Center) {
                        Text("?", color = AC.TextDim, fontWeight = FontWeight.Black)
                    }
                    Column(Modifier.padding(start = 0.dp, end = 12.dp, bottom = 12.dp).padding(horizontal = 12.dp)) {
                        Text(Staffing.roleName(posto.role, posto.specialita), style = MaterialTheme.typography.titleSmall)
                        if (posto.role == Staffing.ALLENATORE && allenatore != null) {
                            // Tu: la squadra che scegli la alleni tu
                            Text(allenatore, style = MaterialTheme.typography.bodyMedium, color = AC.Text)
                            Text("Tu · allenatore della squadra", color = AC.Accent, style = MaterialTheme.typography.bodySmall)
                        } else if (s == null) {
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
        }

        val osservatori = staff.filter { it.role == Staffing.SCOUT }
        item { SectionTitle("Osservatori (${osservatori.size}/${Staffing.MAX_OSSERVATORI})") }
        item {
            Text(
                "Mandali in un'area del mondo: al rientro portano giocatori svincolati, disponibili nel Mercato.",
                style = MaterialTheme.typography.bodySmall, color = AC.TextDim
            )
        }
        if (osservatori.isEmpty()) item { Text("Nessun osservatore: assumine dai candidati.", color = AC.TextDim) }
        items(osservatori) { s ->
            val m = missioni.firstOrNull { it.staffId == s.id }
            GlassCard(Modifier.fillMaxWidth().clickable { aperto = s }) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(s.name, 44.dp)
                Column(Modifier.padding(start = 12.dp)) {
                    Text("Osservatore · ${s.name}", style = MaterialTheme.typography.titleSmall)
                    Text(stelleTesto(s.skill), color = AC.Accent)
                    Text(
                        if (m == null) "Disponibile" else "In missione in ${m.area}: rientro tra ${m.giornate} giornate",
                        color = if (m == null) AC.Ok else AC.TextDim, style = MaterialTheme.typography.bodySmall
                    )
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
    s: Staff, candidato: Boolean, onHire: () -> Unit, onFire: () -> Unit, onClose: () -> Unit,
    missione: ScoutMission?, onInvia: (String) -> Unit
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
                if (s.role == Staffing.SCOUT && !candidato) {
                    Text("Missione", style = MaterialTheme.typography.titleSmall)
                    if (missione != null) {
                        Text("In missione in ${missione.area}: rientro tra ${missione.giornate} giornate", color = AC.Accent)
                    } else {
                        Text("Scegli un'area del mondo:", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                        Missioni.AREE.forEach { area ->
                            PcAction(onClick = { onInvia(area.nome) }, modifier = Modifier.fillMaxWidth()) { Text(area.nome) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (candidato) PcAction(onClick = onHire) { Text("Ingaggia") }
            else TextButton(onClick = onFire) { Text("Licenzia") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Chiudi") } }
    )
}
