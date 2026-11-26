package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import it.androcalcio.data.SeedData
import it.androcalcio.data.Team
import it.androcalcio.engine.Nazionali
import it.androcalcio.engine.TacticalRoles
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

/** Posizioni naturali ammesse per ruolo (P, D, C, A). */
private val POS_PER_RUOLO = mapOf(
    "P" to listOf("POR"), "D" to listOf("TD", "TS", "DC", "CS", "CD"),
    "C" to listOf("MED", "REG", "INT", "MP"), "A" to listOf("ES", "CA")
)
private val PIEDI = listOf("D" to "Destro", "S" to "Mancino", "A" to "Ambidestro")

/** Menu a tendina: il pulsante mostra la scelta attuale, [opzioni] sono coppie (codice, nome). */
@Composable
private fun Scelta(titolo: String, valore: String, opzioni: List<Pair<String, String>>, onScelta: (String) -> Unit) {
    var aperto by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { aperto = true }, modifier = Modifier.fillMaxWidth()) {
            Text(titolo + ": " + (opzioni.firstOrNull { o -> o.first == valore }?.second ?: valore.ifEmpty { "—" }))
        }
        DropdownMenu(expanded = aperto, onDismissRequest = { aperto = false }) {
            opzioni.forEach { (codice, nome) ->
                DropdownMenuItem(text = { Text(nome) }, onClick = { onScelta(codice); aperto = false })
            }
        }
    }
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
    val menteCorrente by vm.menteDelta.collectAsStateWithLifecycle()
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
        var foot by remember(p) { mutableStateOf(p.foot) }
        var pos by remember(p) { mutableStateOf(p.pos) }
        var s2 by remember(p) { mutableStateOf(p.sec2) }
        var a2 by remember(p) { mutableStateOf(p.aff2.toString()) }
        var s3 by remember(p) { mutableStateOf(p.sec3) }
        var a3 by remember(p) { mutableStateOf(p.aff3.toString()) }
        var s4 by remember(p) { mutableStateOf(p.sec4) }
        var a4 by remember(p) { mutableStateOf(p.aff4.toString()) }
        // Caratteristiche: sigla -> valore in testo (tutti i parametri del giocatore)
        val num = remember(p) { androidx.compose.runtime.mutableStateMapOf(
            "VE" to p.ve.toString(), "RE" to p.re.toString(), "AG" to p.ag.toString(), "QU" to p.qu.toString(), "FO" to p.fo.toString(),
            "GM" to p.gm.toString(), "EN" to p.en.toString(), "PA" to p.pa.toString(), "DR" to p.dr.toString(), "FI" to p.fi.toString(),
            "TI" to p.ti.toString(), "RI" to p.ri.toString(), "CO" to p.condition.toString(), "CT" to p.contract.toString(),
            "NU" to p.numero.toString(), "IN" to p.injuredRounds.toString()) }
        val menteIniz = it.androcalcio.engine.Mente.valori(p, menteCorrente)
        val mente = remember(p, menteCorrente) { androidx.compose.runtime.mutableStateListOf(*menteIniz.map { v -> v.toString() }.toTypedArray()) }
        var scheda by remember(p) { mutableStateOf(0) }
        val nazioni = (SeedData.codes.keys + Nazionali.EUROPA + p.nationality).toSortedSet().map { n -> n to n }
        fun daPosizione(nuova: String) {   // ruoli affini di partenza per la posizione scelta
            pos = nuova
            val (r2, f2) = TacticalRoles.affine(nuova, 0); val (r3, f3) = TacticalRoles.affine(nuova, 1)
            s2 = r2; a2 = f2.toString(); s3 = r3; a3 = f3.toString(); s4 = ""; a4 = "0"
        }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Modifica giocatore") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SCHEDE_EDITOR.forEachIndexed { k, nome -> PcChip(selected = scheda == k, onClick = { scheda = k }, label = { Text(nome, fontSize = 12.sp) }) }
                }
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  when (scheda) {
                  0 -> {
                    TextF("Nome", pn) { pn = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("P", "D", "C", "A").forEach { r ->
                            PcChip(selected = role == r, onClick = {
                                role = r
                                if (pos !in POS_PER_RUOLO.getValue(r)) daPosizione(POS_PER_RUOLO.getValue(r).first())
                            }, label = { Text(r) })
                        }
                    }
                    Text("Posizione preferita", style = MaterialTheme.typography.labelMedium, color = AC.TextDim)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        POS_PER_RUOLO.getValue(role).forEach { c ->
                            PcChip(selected = pos == c, onClick = { daPosizione(c) }, label = { Text(c) })
                        }
                    }
                    Text("Piede preferito", style = MaterialTheme.typography.labelMedium, color = AC.TextDim)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PIEDI.forEach { (c, nome) ->
                            PcChip(selected = foot == c, onClick = { foot = c }, label = { Text(nome) })
                        }
                    }
                    NumF("Età", age) { age = it }
                    Scelta("Nazionale", nat, nazioni) { nat = it }
                    NumF("Valore (1-99)", ov) { ov = it }
                    NumF("Potenziale (1-99, non sotto il valore)", pot) { pot = it }
                  }
                  1, 2 -> CAMPI_EDITOR.filter { c -> c.scheda == scheda }.forEach { c ->
                      Caratteristica(c.sigla, c.nome, c.descrizione, num[c.sigla].orEmpty()) { v -> num[c.sigla] = v }
                  }
                  3 -> {
                    Caratteristica("MO", "Morale", "Umore del giocatore (0-100): sotto 45 rende meno e crea problemi.", mo) { mo = it }
                    it.androcalcio.engine.Mente.Attr.values().forEach { a ->
                        Caratteristica(a.sigla, a.nome, a.descrizione, mente[a.ordinal]) { v -> mente[a.ordinal] = v }
                    }
                    Text("Carattere: ${it.androcalcio.engine.Personalita.tipo(pn, nat).nome} (dipende da nome e nazionalità).",
                        style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
                  }
                  4 -> {
                    NumF("Ingaggio annuo (€)", sal) { sal = it }
                    CAMPI_EDITOR.filter { c -> c.scheda == 4 }.forEach { c ->
                        Caratteristica(c.sigla, c.nome, c.descrizione, num[c.sigla].orEmpty()) { v -> num[c.sigla] = v }
                    }
                  }
                  else -> {
                    Text("Ruoli affini (con l'affinità in %). Cambiando la posizione si ripartono i ruoli di default.",
                        style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
                    CodiceRuolo("Ruolo 2", s2, pos) { s2 = it; if (it.isEmpty()) a2 = "0" else if (a2 == "0") a2 = "50" }
                    if (s2.isNotEmpty()) NumF("Affinità ruolo 2 (0-100)", a2) { a2 = it }
                    CodiceRuolo("Ruolo 3", s3, pos) { s3 = it; if (it.isEmpty()) a3 = "0" else if (a3 == "0") a3 = "50" }
                    if (s3.isNotEmpty()) NumF("Affinità ruolo 3 (0-100)", a3) { a3 = it }
                    CodiceRuolo("Ruolo 4", s4, pos) { s4 = it; if (it.isEmpty()) a4 = "0" else if (a4 == "0") a4 = "50" }
                    if (s4.isNotEmpty()) NumF("Affinità ruolo 4 (0-100)", a4) { a4 = it }
                  }
                  }
                }
                }
            },
            confirmButton = {
                PcAction(onClick = {
                    fun nv(k: String, d: Int) = (num[k]?.toIntOrNull() ?: d).coerceIn(1, 99)
                    val parametriCambiati = listOf("VE" to p.ve, "RE" to p.re, "AG" to p.ag, "QU" to p.qu, "EN" to p.en,
                        "PA" to p.pa, "DR" to p.dr, "FI" to p.fi, "TI" to p.ti, "RI" to p.ri).any { (k, v) -> nv(k, v) != v }
                    val valoreDigitato = ov.toIntOrNull()?.takeIf { it != p.overall }
                    val newOv = valoreDigitato?.coerceIn(1, 99)
                        ?: if (parametriCambiati) it.androcalcio.engine.Attr.overallDa(role, nv("VE", p.ve), nv("RE", p.re), nv("AG", p.ag),
                            nv("QU", p.qu), nv("EN", p.en), nv("PA", p.pa), nv("DR", p.dr), nv("FI", p.fi), nv("TI", p.ti), nv("RI", p.ri))
                        else p.overall
                    // Ruoli affini: mai uguali alla posizione o tra loro; affinità 0 se il ruolo è vuoto
                    val usati = mutableSetOf(pos)
                    fun ruolo(r: String) = if (r.isNotEmpty() && usati.add(r)) r else ""
                    fun aff(r: String, a: String) = if (r.isEmpty()) 0 else (a.toIntOrNull() ?: 50).coerceIn(0, 100)
                    val r2 = ruolo(s2); val r3 = ruolo(s3); val r4 = ruolo(s4)
                    fun n(k: String, d: Int) = (num[k]?.toIntOrNull() ?: d).coerceIn(1, 99)
                    vm.savePlayer(p.copy(
                        name = pn.ifBlank { p.name }, role = role,
                        age = (age.toIntOrNull() ?: p.age).coerceIn(15, 45),
                        overall = newOv,
                        potential = (pot.toIntOrNull() ?: p.potential).coerceIn(newOv, 99),
                        morale = (mo.toIntOrNull() ?: p.morale).coerceIn(0, 100),
                        salary = sal.toLongOrNull() ?: p.salary,
                        nationality = nat.ifBlank { p.nationality },
                        foot = foot, pos = pos,
                        sec2 = r2, aff2 = aff(r2, a2), sec3 = r3, aff3 = aff(r3, a3), sec4 = r4, aff4 = aff(r4, a4),
                        // il ruolo scelto per la partita deve restare uno dei suoi ruoli
                        ruoloPartita = if (p.ruoloPartita in setOf(pos, r2, r3, r4)) p.ruoloPartita else "",
                        compito = if (p.ruoloPartita in setOf(pos, r2, r3, r4)) p.compito else "",
                        ve = n("VE", p.ve), re = n("RE", p.re), ag = n("AG", p.ag), qu = n("QU", p.qu), fo = n("FO", p.fo),
                        gm = n("GM", p.gm), en = n("EN", p.en), pa = n("PA", p.pa), dr = n("DR", p.dr), fi = n("FI", p.fi),
                        ti = n("TI", p.ti), ri = n("RI", p.ri),
                        condition = (num["CO"]?.toIntOrNull() ?: p.condition).coerceIn(0, 100),
                        contract = (num["CT"]?.toIntOrNull() ?: p.contract).coerceIn(0, 10),
                        numero = (num["NU"]?.toIntOrNull() ?: p.numero).coerceIn(0, 99),
                        injuredRounds = (num["IN"]?.toIntOrNull() ?: p.injuredRounds).coerceIn(0, 60)
                    ))
                    val nuoviMente = IntArray(mente.size) { k -> mente[k].toIntOrNull() ?: menteIniz[k] }
                    if (!nuoviMente.contentEquals(menteIniz)) vm.salvaMente(p, nuoviMente)
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

private val SCHEDE_EDITOR = listOf("Anagrafica", "Fisico", "Tecnica", "Mentale", "Contratto", "Ruoli")

private data class CampoEditor(val sigla: String, val nome: String, val descrizione: String, val scheda: Int)

private val CAMPI_EDITOR = listOf(
    CampoEditor("VE", "Velocità", "Scatto e corsa: pesa negli inserimenti e nei recuperi.", 1),
    CampoEditor("RE", "Resistenza", "Quanto regge i 90 minuti: cala meno la condizione.", 1),
    CampoEditor("AG", "Aggressività", "Grinta nei contrasti: più palloni recuperati, più cartellini.", 1),
    CampoEditor("FO", "Forma", "Stato di forma del momento (40-99).", 1),
    CampoEditor("CO", "Condizione", "Energia di oggi (0-100): sotto 60 rischia infortuni.", 1),
    CampoEditor("QU", "Qualità", "Tecnica generale e visione di gioco.", 2),
    CampoEditor("PA", "Passaggio", "Precisione nei passaggi corti e lunghi.", 2),
    CampoEditor("DR", "Dribbling", "Saltare l'uomo palla al piede.", 2),
    CampoEditor("EN", "Entrate", "Tackle e anticipo in difesa.", 2),
    CampoEditor("FI", "Finalizzazione", "Freddezza sotto porta.", 2),
    CampoEditor("TI", "Tiro", "Potenza e precisione da fuori.", 2),
    CampoEditor("RI", "Rigori", "Precisione dal dischetto.", 2),
    CampoEditor("GM", "Gioco di mani", "Parate e uscite: conta soprattutto per i portieri.", 2),
    CampoEditor("CT", "Contratto", "Anni di contratto rimasti (0-10).", 4),
    CampoEditor("NU", "Numero di maglia", "0 = lo sceglie l'allenatore in seconda.", 4),
    CampoEditor("IN", "Turni di infortunio", "0 = disponibile.", 4)
)

/** Una caratteristica: nome con sigla, descrizione breve e valore modificabile. */
@Composable
private fun Caratteristica(sigla: String, nome: String, descrizione: String, valore: String, onChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f)) {
            Text("$sigla · $nome", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(descrizione, style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
        }
        OutlinedTextField(
            value = valore, onValueChange = { v -> onChange(v.filter(Char::isDigit).take(3)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(76.dp)
        )
    }
}

/** Codice ruolo libero (1-4 lettere) con suggerimenti toccabili dai ruoli tattici noti. */
@Composable
private fun CodiceRuolo(label: String, valore: String, posizione: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = valore, singleLine = true, label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
            onValueChange = { v -> onChange(v.uppercase().filter { it.isLetter() }.take(4)) },
            supportingText = { Text(TacticalRoles.NAMES[valore] ?: if (valore.isEmpty()) "Vuoto: nessun ruolo" else "Ruolo personalizzato") }
        )
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TacticalRoles.NAMES.keys.filter { it != posizione }.forEach { c ->
                PcChip(selected = valore == c, onClick = { onChange(c) }, label = { Text(c, fontSize = 12.sp) })
            }
        }
    }
}
