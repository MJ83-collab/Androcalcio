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
import it.androcalcio.engine.Annunci
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
    allenatore: String? = null,
    annunci: List<Annunci.Annuncio> = emptyList(),
    risposte: List<Staff> = emptyList(),
    onPubblica: (String, String) -> Unit = { _, _ -> },
    onRitira: (Annunci.Annuncio) -> Unit = {},
    onRinnova: (Staff, Int) -> Unit = { _, _ -> },
    focus: Staffing.Posto? = null,
    onFocusUsato: () -> Unit = {}
) {
    if (team == null) return
    var aperto by remember { mutableStateOf<Staff?>(null) }
    var rinnovo by remember { mutableStateOf<Staff?>(null) }
    focus?.let { f ->
        val gia = annunci.any { it.role == f.role && it.specialita == f.specialita }
        AlertDialog(
            onDismissRequest = onFocusUsato,
            title = { Text("Cerca: ${Staffing.roleName(f.role, f.specialita)}") },
            text = { Text(if (gia) "Hai già un annuncio attivo: guarda le risposte più in basso, in Annunci e candidati."
                else "Pubblica un annuncio (${money(Annunci.COSTO)}): i professionisti interessati rispondono subito. Le risposte cambiano ogni mese.") },
            confirmButton = { if (!gia) PcAction(onClick = { onPubblica(f.role, f.specialita); onFocusUsato() }) { Text("Pubblica annuncio") }
                else TextButton(onClick = onFocusUsato) { Text("Ok") } },
            dismissButton = { if (!gia) TextButton(onClick = onFocusUsato) { Text("Non ora") } }
        )
    }
    rinnovo?.let { s ->
        AlertDialog(
            onDismissRequest = { rinnovo = null },
            title = { Text("Rinnovo di ${s.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Contratto attuale: ${s.contract} ${if (s.contract == 1) "anno" else "anni"}, ${money(s.salary)} l'anno.", color = AC.TextDim)
                    Text("Più anni firmi, meno chiede all'anno:", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                    (1..3).forEach { a ->
                        PcAction(onClick = { onRinnova(s, a); rinnovo = null }, modifier = Modifier.fillMaxWidth()) {
                            Text("$a ${if (a == 1) "anno" else "anni"} · ${money(Annunci.nuovoIngaggio(s, a))}")
                        }
                    }
                }
            },
            confirmButton = {}, dismissButton = { TextButton(onClick = { rinnovo = null }) { Text("Annulla") } }
        )
    }
    aperto?.let { s ->
        DettaglioStaff(
            s, candidato = s.teamId == 0L,
            onHire = { onHire(s); aperto = null },
            onFire = { onFire(s); aperto = null },
            onRinnova = { rinnovo = s; aperto = null },
            onClose = { aperto = null },
            missione = missioni.firstOrNull { it.staffId == s.id },
            onInvia = { area -> onInvia(s, area); aperto = null }
        )
    }

    val gruppi = listOf(
        "Staff tecnico" to Staffing.POSTI.filter { it.role in listOf(Staffing.ALLENATORE, Staffing.ASSISTENTE, Staffing.GIOVANILI) },
        "Preparatori atletici (6)" to Staffing.POSTI.filter { it.role == Staffing.PREPARATORE },
        "Fisioterapisti (2)" to Staffing.POSTI.filter { it.role == Staffing.FISIOTERAPISTA },
        "Manutenzione" to Staffing.POSTI.filter { it.role == Staffing.TUTTOFARE },
        "Analisi" to Staffing.POSTI.filter { it.role == Staffing.ANALISTA }
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
                GlassCard(Modifier.fillMaxWidth().clickable { if (s != null) aperto = s else onPubblica(posto.role, posto.specialita) }) {
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
                            Text("vacante · tocca per cercare", color = AC.Accent, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stelleTesto(s.skill), color = AC.Accent)
                                Text("${punteggio(s.skill)} · ${s.name}", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(if (s.contract <= 1) "⚠ contratto in scadenza: tocca per rinnovare" else "contratto ${s.contract} anni · ${money(s.salary)} l'anno",
                                color = if (s.contract <= 1) AC.Warn else AC.TextDim, style = MaterialTheme.typography.bodySmall)
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

        item { SectionTitle("Annunci e candidati") }
        item {
            Text("Pubblica un annuncio (${money(Annunci.COSTO)}) per un posto: rispondono i professionisti interessati, " +
                "più numerosi e bravi se il club è forte. Le risposte cambiano ogni mese.",
                style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
        }
        val posti = Staffing.POSTI + Staffing.Posto(Staffing.SCOUT)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Pubblica un nuovo annuncio", style = MaterialTheme.typography.titleSmall)
                posti.filter { p -> annunci.none { it.role == p.role && it.specialita == p.specialita } }.chunked(2).forEach { riga ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        riga.forEach { p ->
                            PcAction(onClick = { onPubblica(p.role, p.specialita) }, modifier = Modifier.weight(1f)) {
                                Text(Staffing.roleName(p.role, p.specialita).replace("Preparatore atletico", "Prep.").replace("Fisioterapista", "Fisio"),
                                    style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
        if (annunci.isEmpty()) item { Text("Nessun annuncio attivo.", color = AC.TextDim) }
        annunci.forEach { a ->
            val r = risposte.filter { it.role == a.role && it.specialita == a.specialita }
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📢 ${a.nome}", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                            TextButton(onClick = { onRitira(a) }) { Text("Ritira") }
                        }
                        if (r.isEmpty()) Text("Nessuna risposta questo mese.", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                        r.forEach { c ->
                            Row(Modifier.fillMaxWidth().clickable { aperto = c }, verticalAlignment = Alignment.CenterVertically) {
                                Avatar(c.name, 36.dp)
                                Column(Modifier.padding(start = 10.dp)) {
                                    Text(c.name, style = MaterialTheme.typography.bodyMedium)
                                    Text("${stelleTesto(c.skill)} · ${money(c.salary)} l'anno · ${c.contract} anni",
                                        color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DettaglioStaff(
    s: Staff, candidato: Boolean, onHire: () -> Unit, onFire: () -> Unit, onRinnova: () -> Unit = {}, onClose: () -> Unit,
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
            else Row { PcAction(onClick = onRinnova) { Text("Rinnova") }; TextButton(onClick = onFire) { Text("Licenzia") } }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Chiudi") } }
    )
}
