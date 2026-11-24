package it.androcalcio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Player
import it.androcalcio.engine.SquadraB
import it.androcalcio.ui.components.GlassCard

/** Scheda del preparatore atletico capo: interruttore e report mensili. */
@Composable
fun PreparatoreCapoCard(auto: Boolean, assunto: Boolean, report: List<String>, onAuto: (Boolean) -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Preparatore atletico capo", fontSize = 15.sp)
                Switch(checked = auto, onCheckedChange = onAuto)
            }
            Text(
                if (!assunto) "Serve un preparatore atletico generale in staff: assumilo e gestirà da solo allenamenti, riposi, piani dei reparti e vivaio."
                else if (auto) "Gestisce da solo allenamenti e riposi della rosa, i piani dei reparti e la crescita del vivaio. Report ogni mese."
                else "Spento: allenamenti e riposi li scegli tu.", fontSize = 12.sp)
            if (assunto && auto && report.isEmpty()) Text("Il primo report arriverà a inizio del prossimo mese.", fontSize = 12.sp)
            report.take(3).forEach { Text(it, fontSize = 12.sp) }
        }
    }
}

/** Squadra B/U23 e satellite: giovani della rosa che crescono di più; li segue il preparatore capo. */
@Composable
fun SquadraBCard(rosa: List<Player>, membri: Map<Long, Int>, onSet: (Player, Int?) -> Unit, risultati: List<String> = emptyList()) {
    val giovani = rosa.filter { it.age <= SquadraB.ETA_MAX }
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Squadra B / Under 23 · ${membri.size}/${SquadraB.MAX}", fontSize = 15.sp)
            Text("Fino a 23 anni. Squadra B: crescita mensile extra. Satellite: crescita maggiore, costa 40.000 € l'anno. Restano nella rosa; li segue il preparatore capo.", fontSize = 12.sp)
            Text("Ogni mese la squadra B gioca una partita, simulata dall'allenatore in seconda (serve averlo in staff): vittoria +4% e pareggio +2% alla crescita del mese. I giovani del satellite con contratto in scadenza restano con te per 3 anni (prelazione).", fontSize = 12.sp)
            if (risultati.isNotEmpty()) Text("Ultime partite: " + risultati.takeLast(5).joinToString(" · ") { r -> r.substringBefore(":") + " " + r.substringAfter(":", "") }, fontSize = 12.sp)
            if (giovani.isEmpty()) Text("Nessun giocatore Under 23 in rosa.", fontSize = 12.sp)
            giovani.sortedByDescending { it.potential }.take(12).forEach { p ->
                val lv = membri[p.id]
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${p.name} (${p.age}) ${p.overall}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                    androidx.compose.material3.TextButton(onClick = { onSet(p, if (lv == 0) null else 0) }) { Text(if (lv == 0) "B ✓" else "B", fontSize = 12.sp) }
                    androidx.compose.material3.TextButton(onClick = { onSet(p, if (lv == 1) null else 1) }) { Text(if (lv == 1) "Sat ✓" else "Sat", fontSize = 12.sp) }
                }
            }
        }
    }
}
