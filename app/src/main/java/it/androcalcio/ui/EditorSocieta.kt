package it.androcalcio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.data.Player
import it.androcalcio.data.Staff
import it.androcalcio.data.Team
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.components.RuoloChip
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.theme.AC

/** Editor di tutte le società: squadre, staff e giocatori di qualsiasi club. */
@Composable
fun EditorSocieta(vm: AndrocalcioViewModel, onBack: () -> Unit) {
    val teams by vm.teams.collectAsStateWithLifecycle()
    val menteCorrente by vm.menteDelta.collectAsStateWithLifecycle()
    val sq by vm.squadraEd.collectAsStateWithLifecycle()
    var sceltaId by remember { mutableStateOf<Long?>(null) }
    var cerca by remember { mutableStateOf("") }
    var editTeam by remember { mutableStateOf<Team?>(null) }
    var editStaff by remember { mutableStateOf<Staff?>(null) }
    var editPlayer by remember { mutableStateOf<Player?>(null) }

    LaunchedEffect(sceltaId) { vm.apriSquadraEd(sceltaId) }
    DisposableEffect(Unit) { onDispose { vm.apriSquadraEd(null) } }

    val scelta = teams.firstOrNull { it.id == sceltaId }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (scelta != null) sceltaId = null else onBack() }) {
                    Text(if (scelta != null) "← Tutte le società" else "← Editor")
                }
            }
        }
        if (scelta == null) {
            item { Text("Editor di tutte le società", style = MaterialTheme.typography.headlineSmall) }
            item {
                OutlinedTextField(
                    value = cerca, onValueChange = { cerca = it }, singleLine = true,
                    label = { Text("Cerca società, campionato o nazione") }, modifier = Modifier.fillMaxWidth()
                )
            }
            val q = cerca.trim()
            items(teams.filter { t -> q.isEmpty() || listOf(t.name, t.campionato, t.nazione).any { it.contains(q, ignoreCase = true) } }.sortedBy { it.name }, key = { it.id }) { t ->
                GlassCard(Modifier.fillMaxWidth().clickable { sceltaId = t.id }) {
                    Column(Modifier.padding(10.dp)) {
                        Text(t.name, fontWeight = FontWeight.Bold)
                        Text("${t.campionato} · ${t.nazione}", fontSize = 12.sp, color = AC.TextDim)
                    }
                }
            }
        } else {
            item {
                Text(scelta.name, style = MaterialTheme.typography.headlineSmall)
                PcAction(onClick = { editTeam = scelta }, modifier = Modifier.fillMaxWidth()) { Text("Modifica società") }
            }
            item { SectionTitle("Staff (${sq.staff.size})") }
            items(sq.staff.sortedBy { it.role }, key = { "s${it.id}" }) { s ->
                GlassCard(Modifier.fillMaxWidth().clickable { editStaff = s }) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(s.name, Modifier.weight(1f))
                        Text("${s.role} · ${s.skill}", color = AC.Accent)
                    }
                }
            }
            item { SectionTitle("Giocatori (${sq.players.size})") }
            items(sq.players, key = { "p${it.id}" }) { p ->
                GlassCard(Modifier.fillMaxWidth().clickable { editPlayer = p }) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RuoloChip(p.role, p.pos)
                        Text(" ${p.name}", Modifier.weight(1f))
                        Text("${p.age}a · ${p.overall}", color = AC.Accent)
                    }
                }
            }
        }
    }

    editTeam?.let { t -> TeamEditDialog(t, onSave = { vm.saveTeam(it); editTeam = null }, onClose = { editTeam = null }) }
    editStaff?.let { s ->
        StaffEditDialog(s, onSave = { vm.salvaStaffEd(it); editStaff = null },
            onDelete = { vm.eliminaStaffEd(s); editStaff = null }, onClose = { editStaff = null })
    }
    editPlayer?.let { pl -> PlayerEditDialog(pl, vm, menteCorrente) { editPlayer = null } }
}

@Composable
private fun CampoTesto(label: String, value: String, numero: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, singleLine = true, label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = if (numero) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        onValueChange = { onChange(if (numero) it.filter(Char::isDigit).take(12) else it) }
    )
}

@Composable
private fun TeamEditDialog(t: Team, onSave: (Team) -> Unit, onClose: () -> Unit) {
    var nome by remember(t) { mutableStateOf(t.name) }
    var sigla by remember(t) { mutableStateOf(t.shortName) }
    var sponsor by remember(t) { mutableStateOf(t.sponsor) }
    var nazione by remember(t) { mutableStateOf(t.nazione) }
    var campionato by remember(t) { mutableStateOf(t.campionato) }
    var budget by remember(t) { mutableStateOf(t.budget.toString()) }
    var monte by remember(t) { mutableStateOf(t.wageBudget.toString()) }
    var stadio by remember(t) { mutableStateOf(t.stadiumCapacity.toString()) }
    var biglietto by remember(t) { mutableStateOf(t.ticketPrice.toString()) }
    var vivaio by remember(t) { mutableStateOf(t.youthCoachSkill.toString()) }
    var scout by remember(t) { mutableStateOf(t.scoutSkill.toString()) }
    var colP by remember(t) { mutableStateOf(java.lang.Long.toHexString(t.colorPrimary).uppercase()) }
    var colS by remember(t) { mutableStateOf(java.lang.Long.toHexString(t.colorSecondary).uppercase()) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Società") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CampoTesto("Nome", nome) { nome = it }
                CampoTesto("Sigla", sigla) { sigla = it.take(4) }
                CampoTesto("Sponsor", sponsor) { sponsor = it }
                CampoTesto("Nazione", nazione) { nazione = it }
                CampoTesto("Campionato", campionato) { campionato = it }
                CampoTesto("Budget mercato (€)", budget, true) { budget = it }
                CampoTesto("Monte ingaggi annuo (€)", monte, true) { monte = it }
                CampoTesto("Capienza stadio", stadio, true) { stadio = it }
                CampoTesto("Prezzo biglietto", biglietto, true) { biglietto = it }
                CampoTesto("Allenatore giovanili (1-99)", vivaio, true) { vivaio = it.take(2) }
                CampoTesto("Scout (1-99)", scout, true) { scout = it.take(2) }
                CampoTesto("Colore primario (ARGB hex)", colP) { colP = it.uppercase().filter { c -> c.isDigit() || c in 'A'..'F' }.take(8) }
                CampoTesto("Colore secondario (ARGB hex)", colS) { colS = it.uppercase().filter { c -> c.isDigit() || c in 'A'..'F' }.take(8) }
            }
        },
        confirmButton = {
            PcAction(onClick = {
                onSave(t.copy(
                    name = nome.ifBlank { t.name }, shortName = sigla.ifBlank { t.shortName }, sponsor = sponsor, nazione = nazione,
                    campionato = campionato,
                    budget = budget.toLongOrNull() ?: t.budget, wageBudget = monte.toLongOrNull() ?: t.wageBudget,
                    stadiumCapacity = stadio.toIntOrNull() ?: t.stadiumCapacity, ticketPrice = biglietto.toIntOrNull() ?: t.ticketPrice,
                    youthCoachSkill = (vivaio.toIntOrNull() ?: t.youthCoachSkill).coerceIn(1, 99),
                    scoutSkill = (scout.toIntOrNull() ?: t.scoutSkill).coerceIn(1, 99),
                    colorPrimary = colP.toLongOrNull(16) ?: t.colorPrimary, colorSecondary = colS.toLongOrNull(16) ?: t.colorSecondary
                ))
            }) { Text("Salva") }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Annulla") } }
    )
}

@Composable
private fun StaffEditDialog(s: Staff, onSave: (Staff) -> Unit, onDelete: () -> Unit, onClose: () -> Unit) {
    var nome by remember(s) { mutableStateOf(s.name) }
    var skill by remember(s) { mutableStateOf(s.skill.toString()) }
    var salario by remember(s) { mutableStateOf(s.salary.toString()) }
    var contratto by remember(s) { mutableStateOf(s.contract.toString()) }
    var stile by remember(s) { mutableStateOf(s.stile) }
    var spec by remember(s) { mutableStateOf(s.specialita) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Staff · ${s.role}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CampoTesto("Nome", nome) { nome = it }
                CampoTesto("Abilità (1-99)", skill, true) { skill = it.take(2) }
                CampoTesto("Stipendio annuo (€)", salario, true) { salario = it }
                CampoTesto("Contratto (anni)", contratto, true) { contratto = it.take(2) }
                CampoTesto("Specialità (es. GEN, REC, PRV)", spec) { spec = it.uppercase().take(4) }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ATTACCO", "DIFESA").forEach { st ->
                        PcChip(selected = stile == st, onClick = { stile = st }, label = { Text(st) })
                    }
                }
            }
        },
        confirmButton = {
            PcAction(onClick = {
                onSave(s.copy(
                    name = nome.ifBlank { s.name }, skill = (skill.toIntOrNull() ?: s.skill).coerceIn(1, 99),
                    salary = salario.toLongOrNull() ?: s.salary, contract = (contratto.toIntOrNull() ?: s.contract).coerceIn(0, 10),
                    stile = stile, specialita = spec
                ))
            }) { Text("Salva") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("Elimina") }
                TextButton(onClick = onClose) { Text("Annulla") }
            }
        }
    )
}
