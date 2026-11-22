package it.androcalcio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel.SocietaStato
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.engine.AllStar
import it.androcalcio.engine.Disciplina
import it.androcalcio.engine.Personalita
import it.androcalcio.engine.Market
import it.androcalcio.engine.Scambi
import it.androcalcio.engine.Storia
import it.androcalcio.ui.components.Avatar
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

private fun ruoloSigla(r: String) = when (r) { "P" -> "POR"; "D" -> "DIF"; "C" -> "MED"; else -> "ATT" }

@Composable
private fun RigaDato(sinistra: String, destra: String, colore: Color = AC.Text) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(sinistra, color = AC.TextDim, fontSize = 13.sp)
        Text(destra, color = colore, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun RigaGiocatore(p: Player, nota: String, modifier: Modifier = Modifier, selezionato: Boolean = false) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(p.name, 30.dp)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(p.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (selezionato) AC.Accent else AC.Text, maxLines = 1)
            Text("${ruoloSigla(p.role)} · ${p.age} anni · media ${p.overall}", fontSize = 11.sp, color = AC.TextDim, maxLines = 1)
        }
        Text(nota, fontSize = 12.sp, color = AC.Accent2, fontWeight = FontWeight.Bold)
    }
}

/** Società: disciplina, spogliatoio e personalità, scambi, fair play, storia del club e ricerca giocatori. */
@Composable
fun SocietaScreen(
    team: Team?, teams: List<Team>, rosa: List<Player>, tutti: List<Player>, s: SocietaStato,
    onRichiesta: (Personalita.Richiesta, Boolean) -> Unit,
    onScambio: (Player, Player, Long) -> Unit
) {
    if (team == null) return
    var tab by rememberSaveable { mutableStateOf(0) }
    val nomiSquadra = teams.associate { it.id to it.name }
    val titoli = listOf("Disciplina", "Spogliatoio", "Scambi", "Fair play", "Storia", "Cerca")
    Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Società", style = MaterialTheme.typography.headlineSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(titoli.indices.toList()) { i -> PcChip(selected = tab == i, onClick = { tab = i }, label = { Text(titoli[i]) }) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (tab) {
                0 -> TabDisciplina(rosa, s)
                1 -> TabSpogliatoio(rosa, s, onRichiesta)
                2 -> TabScambi(team, teams, rosa, tutti, onScambio)
                3 -> TabFairPlay(s)
                4 -> TabStoria(s)
                else -> TabCerca(teams, tutti, nomiSquadra)
            }
        }
    }
}

@Composable
private fun TabDisciplina(rosa: List<Player>, s: SocietaStato) {
    val byId = rosa.associateBy { it.id }
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            val a = s.arbitro
            if (a != null) RigaDato("Prossimo arbitro", "${a.nome} (${a.descrizione})")
            Text("Quattro gialli = diffida; al quinto scatta una giornata di squalifica. Il rosso costa da 1 a 3 giornate. " +
                "Un arbitro severo ammonisce di più.", color = AC.TextDim, fontSize = 12.sp)
        }
    }
    SectionTitle("Squalificati")
    val sq = s.squal.filter { byId.containsKey(it.key) }
    if (sq.isEmpty()) Text("Nessuno.", color = AC.TextDim)
    sq.forEach { (id, n) -> byId[id]?.let { RigaGiocatore(it, "$n ${if (n == 1) "giornata" else "giornate"}") } }
    SectionTitle("Diffidati e ammoniti")
    val gi = s.gialli.filter { byId.containsKey(it.key) }.entries.sortedByDescending { it.value }
    if (gi.isEmpty()) Text("Nessun ammonito.", color = AC.TextDim)
    gi.forEach { (id, n) -> byId[id]?.let { RigaGiocatore(it, if (n >= Disciplina.DIFFIDA) "$n · DIFFIDATO" else "$n gialli") } }
}

@Composable
private fun TabSpogliatoio(rosa: List<Player>, s: SocietaStato, onRichiesta: (Personalita.Richiesta, Boolean) -> Unit) {
    val byId = rosa.associateBy { it.id }
    SectionTitle("Richieste dei giocatori")
    val rq = s.richieste.filter { byId.containsKey(it.playerId) }
    if (rq.isEmpty()) Text("Nessuna richiesta: lo spogliatoio è tranquillo.", color = AC.TextDim)
    rq.forEach { r ->
        val p = byId[r.playerId] ?: return@forEach
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                RigaGiocatore(p, Personalita.tipo(p).nome)
                Text(
                    if (r.tipo == Personalita.Richiesta.AUMENTO)
                        "Chiede un ingaggio di ${money(r.importo)} l'anno (ora ${money(p.salary)})."
                    else "È scontento in panchina e chiede di essere ceduto.",
                    fontSize = 12.sp, color = AC.TextDim, modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PcAction(onClick = { onRichiesta(r, true) }) {
                        Text(if (r.tipo == Personalita.Richiesta.AUMENTO) "Accetta" else "Cedi", fontSize = 12.sp)
                    }
                    TextButton(onClick = { onRichiesta(r, false) }) { Text("Rifiuta", fontSize = 12.sp) }
                }
            }
        }
    }
    SectionTitle("Personalità della rosa")
    rosa.sortedByDescending { it.overall }.forEach { p ->
        val t = Personalita.tipo(p)
        Column {
            RigaGiocatore(p, t.nome, Modifier.apriScheda(p))
            Text(t.descrizione, fontSize = 11.sp, color = AC.TextDim, modifier = Modifier.padding(start = 38.dp))
        }
    }
}

@Composable
private fun TabScambi(team: Team, teams: List<Team>, rosa: List<Player>, tutti: List<Player>, onScambio: (Player, Player, Long) -> Unit) {
    var mioId by rememberSaveable { mutableStateOf(0L) }
    var clubId by rememberSaveable { mutableStateOf(0L) }
    var loroId by rememberSaveable { mutableStateOf(0L) }
    var cong by rememberSaveable { mutableStateOf(0L) }
    val mio = rosa.firstOrNull { it.id == mioId }
    val club = teams.firstOrNull { it.id == clubId }
    val loro = tutti.firstOrNull { it.id == loroId && it.teamId == clubId }
    val altri = teams.filter { it.id != team.id && it.nazione == team.nazione && !AllStar.isAllStar(it) }

    Text("Scambia un tuo giocatore con uno di un altro club, aggiungendo un conguaglio in denaro. Solo nelle finestre di mercato.",
        color = AC.TextDim, fontSize = 12.sp)
    SectionTitle("1. Il tuo giocatore")
    rosa.sortedByDescending { it.overall }.forEach { p ->
        RigaGiocatore(p, money(Market.value(p)), Modifier.clickable { mioId = p.id; cong = 0L }, p.id == mioId)
    }
    SectionTitle("2. Il club")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(altri) { t -> PcChip(selected = t.id == clubId, onClick = { clubId = t.id; loroId = 0L; cong = 0L }, label = { Text(t.name) }) }
    }
    if (club != null) {
        SectionTitle("3. Il suo giocatore")
        tutti.filter { it.teamId == club.id }.sortedByDescending { it.overall }.forEach { p ->
            RigaGiocatore(p, money(Market.value(p)), Modifier.clickable { loroId = p.id; cong = 0L }, p.id == loroId)
        }
    }
    if (mio != null && loro != null) {
        val min = Scambi.conguaglioMinimo(mio, loro)
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${mio.name} ⇄ ${loro.name}", fontWeight = FontWeight.Bold)
                RigaDato("Conguaglio minimo stimato", money(min))
                RigaDato("Tuo conguaglio", money(cong), AC.Accent)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = { cong = (cong - 500_000L).coerceAtLeast(0L) }) { Text("−500k") }
                    TextButton(onClick = { cong += 500_000L }) { Text("+500k") }
                    TextButton(onClick = { cong += 5_000_000L }) { Text("+5M") }
                    TextButton(onClick = { cong = min }) { Text("Minimo") }
                }
                PcAction(onClick = { onScambio(mio, loro, cong) }, modifier = Modifier.fillMaxWidth()) { Text("Proponi lo scambio") }
            }
        }
    }
}

@Composable
private fun TabFairPlay(s: SocietaStato) {
    val e = s.ffp
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Il fair play guarda la perdita operativa degli ultimi tre anni: mercato e stadio non contano. " +
                "Sopra la soglia c'è una multa; se lo sforamento è grande, il mercato in entrata si blocca per la stagione dopo.",
                color = AC.TextDim, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
            if (e == null) Text("Servono i conti di almeno una stagione chiusa.", color = AC.TextDim)
            else {
                val c = when (e.livello) { "Rosso" -> AC.Danger; "Giallo" -> AC.Warn; else -> AC.Ok }
                RigaDato("Semaforo", e.livello, c)
                RigaDato("Perdita ultimi 3 anni", money(e.perdita))
                RigaDato("Soglia consentita", money(e.soglia))
                if (e.sforamento > 0) RigaDato("Sforamento", money(e.sforamento), AC.Danger)
            }
            if (s.ffpBlocco) Text("MERCATO IN ENTRATA BLOCCATO per questa stagione.", color = AC.Danger,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun TabStoria(s: SocietaStato) {
    SectionTitle("Record del club")
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            val r = s.record
            if (r.punti == 0) Text("I record nascono a fine stagione.", color = AC.TextDim)
            else {
                RigaDato("Punti in una stagione", "${r.punti} (${r.stagionePunti})")
                RigaDato("Gol fatti in una stagione", "${r.golFatti}")
                RigaDato("Meno gol subiti", "${r.golSubiti}")
                RigaDato("Serie utile più lunga", "${r.serieUtili}")
            }
        }
    }
    SectionTitle("Hall of Fame")
    if (s.hall.isEmpty()) Text("Dopo 5 anni con te, chi ha media 78+ o 50 gol entra nella Hall of Fame.", color = AC.TextDim)
    s.hall.sortedByDescending { it.anni }.forEach { b ->
        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(b.nome, 34.dp)
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(b.nome, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${b.anni} anni · ${b.gol} gol · media ${b.media}", fontSize = 11.sp, color = AC.TextDim)
            }
            if (Storia.magliaRitirata(b)) Text("Maglia ${b.numero} ritirata", color = AC.Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
    SectionTitle("Scontri diretti")
    if (s.scontri.isEmpty()) Text("Gioca qualche partita: qui trovi il bilancio contro ogni avversario.", color = AC.TextDim)
    s.scontri.entries.sortedByDescending { it.value.partite }.take(25).forEach { (nome, c) ->
        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(nome, Modifier.weight(1f), fontSize = 13.sp, maxLines = 1)
            Text("${c.v}V ${c.n}N ${c.p}P · ${c.gf}-${c.gs}", fontSize = 12.sp, color = AC.Accent2)
        }
    }
}

@Composable
private fun TabCerca(teams: List<Team>, tutti: List<Player>, nomiSquadra: Map<Long, String>) {
    var q by rememberSaveable { mutableStateOf("") }
    val validi = teams.filter { !AllStar.isAllStar(it) }.map { it.id }.toSet()
    OutlinedTextField(
        value = q, onValueChange = { q = it }, label = { Text("Nome del giocatore") },
        singleLine = true, modifier = Modifier.fillMaxWidth()
    )
    val trovati = if (q.trim().length < 2) emptyList()
        else tutti.filter { it.teamId in validi && it.name.contains(q.trim(), ignoreCase = true) }.sortedByDescending { it.overall }.take(40)
    if (q.trim().length < 2) Text("Scrivi almeno due lettere: cerco in tutti i club.", color = AC.TextDim)
    else if (trovati.isEmpty()) Text("Nessun giocatore trovato.", color = AC.TextDim)
    trovati.forEach { p -> RigaGiocatore(p, nomiSquadra[p.teamId].orEmpty(), Modifier.apriScheda(p)) }
}

/** Primo avvio: cinque righe per orientarsi. Si può rivedere dalle Opzioni. */
@Composable
fun TutorialDialog(onChiudi: () -> Unit) {
    AlertDialog(
        onDismissRequest = onChiudi,
        title = { Text("Benvenuto in Androcalcio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("• Rosa e Tattica: scegli gli undici, il modulo, pressing e linea difensiva.")
                Text("• Partita: gioca la giornata, o guardala dal vivo e fai i cambi.")
                Text("• Mercato e Prestiti: si tratta solo nelle finestre (inizio stagione e gennaio). Gli scambi sono in Società.")
                Text("• Vivaio: fai crescere i ragazzi e manda gli scout a cercarne anche negli altri vivai.")
                Text("• Società: disciplina, richieste dei giocatori, fair play, storia del club e ricerca di un giocatore per nome.")
            }
        },
        confirmButton = { PcAction(onClick = onChiudi) { Text("Ho capito") } }
    )
}
