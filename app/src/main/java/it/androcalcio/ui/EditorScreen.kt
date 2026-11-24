package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.theme.AC

@Composable
private fun TextF(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        singleLine = true, modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NumF(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = { onChange(it.filter(Char::isDigit).take(10)) },
        label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    vm: AndrocalcioViewModel,
    team: Team?,
    players: List<Player>,
    notice: String?
) {
    if (team == null) return
    val export = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) vm.exportTo(uri) }
    val import = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) vm.importFrom(uri) }

    var name by remember(team) { mutableStateOf(team.name) }
    var sigla by remember(team) { mutableStateOf(team.shortName) }
    var sponsor by remember(team) { mutableStateOf(team.sponsor) }
    var budget by remember(team) { mutableStateOf(team.budget.toString()) }
    var stadium by remember(team) { mutableStateOf(team.stadiumCapacity.toString()) }
    var ticket by remember(team) { mutableStateOf(team.ticketPrice.toString()) }
    var coach by remember(team) { mutableStateOf(team.youthCoachSkill.toString()) }
    var scout by remember(team) { mutableStateOf(team.scoutSkill.toString()) }
    var editing by remember { mutableStateOf<Player?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Editor dati", style = MaterialTheme.typography.headlineSmall) }
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dati e salvataggio", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Le modifiche sono già nel database. \"Salva come personalizzati\" ne tiene una copia: " +
                            "all'avvio potrai scegliere tra personalizzati, predefiniti o continuare. " +
                            "Caricare personalizzati o predefiniti azzera calendario, risultati e giovani.",
                        style = MaterialTheme.typography.bodySmall, color = AC.TextDim
                    )
                    PcAction(
                        onClick = { vm.saveCustom() }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)
                    ) { Text("Salva come personalizzati") }
                    Text("Database predefinito: le tue modifiche (squadre, giocatori, volti) diventano la base di ogni nuova carriera e di \"Ripristina\". Tocca un volto qui sotto per modificarlo.",
                        style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PcAction(onClick = { vm.salvaComePredefinito() }) { Text("Usa come predefinito") }
                        PcAction(onClick = { vm.tornaAlPredefinitoOriginale() }) { Text("Torna all'originale") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PcAction(onClick = { export.launch("androcalcio-dati.json") }) { Text("Esporta JSON") }
                        PcAction(onClick = { import.launch(arrayOf("*/*")) }) { Text("Importa JSON") }
                    }
                    TextButton(onClick = { confirmReset = true }) { Text("Ripristina dati predefiniti") }
                }
            }
        }
        item { SectionTitle("Squadra") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextF("Nome", name) { name = it }
                TextF("Sigla (3 lettere)", sigla) { sigla = it.take(3) }
                TextF("Sponsor", sponsor) { sponsor = it }
                NumF("Budget (€)", budget) { budget = it }
                NumF("Capienza stadio", stadium) { stadium = it }
                NumF("Prezzo biglietto (€)", ticket) { ticket = it }
                NumF("Abilità allenatore giovanili (1-99)", coach) { coach = it }
                NumF("Abilità scout (1-99)", scout) { scout = it }
                PcAction(onClick = {
                    vm.saveTeam(team.copy(
                        name = name.ifBlank { team.name },
                        shortName = sigla.uppercase().ifBlank { team.shortName },
                        sponsor = sponsor,
                        budget = budget.toLongOrNull() ?: team.budget,
                        stadiumCapacity = stadium.toIntOrNull() ?: team.stadiumCapacity,
                        ticketPrice = ticket.toIntOrNull() ?: team.ticketPrice,
                        youthCoachSkill = (coach.toIntOrNull() ?: team.youthCoachSkill).coerceIn(1, 99),
                        scoutSkill = (scout.toIntOrNull() ?: team.scoutSkill).coerceIn(1, 99)
                    ))
                }) { Text("Salva squadra") }
            }
        }
        item { SectionTitle("Giocatori (tocca per modificare)") }
        item { PcAction(onClick = { vm.addPlayer() }) { Text("Aggiungi giocatore") } }
        items(players, key = { it.id }) { p ->
            GlassCard(Modifier.fillMaxWidth().clickable { editing = p }) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    androidx.compose.runtime.CompositionLocalProvider(it.androcalcio.ui.components.LocalEditorAttivo provides true) {
                        it.androcalcio.ui.components.Avatar(p.name, 40.dp)   // tocca il volto per modificarlo
                    }
                    Text("${p.role}  ${p.name}", Modifier.weight(1f).padding(start = 8.dp))
                    Text("${p.overall}", color = AC.Accent)
                }
            }
        }
    }

    editing?.let { p ->
        var pn by remember(p) { mutableStateOf(p.name) }
        var role by remember(p) { mutableStateOf(p.role) }
        var age by remember(p) { mutableStateOf(p.age.toString()) }
        var ov by remember(p) { mutableStateOf(p.overall.toString()) }
        var pot by remember(p) { mutableStateOf(p.potential.toString()) }
        var mo by remember(p) { mutableStateOf(p.morale.toString()) }
        var sal by remember(p) { mutableStateOf(p.salary.toString()) }
        var nat by remember(p) { mutableStateOf(p.nationality) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Modifica giocatore") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextF("Nome", pn) { pn = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("P", "D", "C", "A").forEach { r ->
                            PcChip(selected = role == r, onClick = { role = r }, label = { Text(r) })
                        }
                    }
                    NumF("Età", age) { age = it }
                    NumF("Valore (1-99)", ov) { ov = it }
                    NumF("Potenziale (1-99, non sotto il valore)", pot) { pot = it }
                    NumF("Morale (0-100)", mo) { mo = it }
                    NumF("Ingaggio annuo (€)", sal) { sal = it }
                    TextF("Nazionalità", nat) { nat = it }
                }
            },
            confirmButton = {
                PcAction(onClick = {
                    val newOv = (ov.toIntOrNull() ?: p.overall).coerceIn(1, 99)
                    vm.savePlayer(p.copy(
                        name = pn.ifBlank { p.name }, role = role,
                        age = (age.toIntOrNull() ?: p.age).coerceIn(15, 45),
                        overall = newOv,
                        potential = (pot.toIntOrNull() ?: p.potential).coerceIn(newOv, 99),
                        morale = (mo.toIntOrNull() ?: p.morale).coerceIn(0, 100),
                        salary = sal.toLongOrNull() ?: p.salary,
                        nationality = nat.ifBlank { p.nationality }
                    ))
                    editing = null
                }) { Text("Salva") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { vm.deletePlayer(p); editing = null }) { Text("Elimina") }
                    TextButton(onClick = { editing = null }) { Text("Annulla") }
                }
            }
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Ripristinare i predefiniti?") },
            text = { Text("Le squadre tornano ai dati iniziali e calendario, risultati e giovani vengono azzerati. La copia personalizzata salvata non viene toccata.") },
            confirmButton = { PcAction(onClick = { vm.resetDefaults(); confirmReset = false }) { Text("Ripristina") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Annulla") } }
        )
    }

    notice?.let {
        AlertDialog(
            onDismissRequest = { vm.clearNotice() }, title = { Text("Editor") }, text = { Text(it) },
            confirmButton = { TextButton(onClick = { vm.clearNotice() }) { Text("OK") } }
        )
    }
}
