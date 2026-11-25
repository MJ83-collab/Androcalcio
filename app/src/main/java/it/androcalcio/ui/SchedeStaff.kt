package it.androcalcio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.data.Player
import it.androcalcio.data.Staff
import it.androcalcio.engine.Migliorie
import it.androcalcio.engine.Staffing
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.Stelle
import it.androcalcio.ui.theme.AC

/** Intestazione comune: chi ricopre il ruolo, con stelle, oppure invito ad assumerlo. */
@Composable
private fun CapoRuolo(titolo: String, s: Staff?, onStaff: () -> Unit) {
    PaperPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titolo, color = AC.Text, fontWeight = FontWeight.Bold)
            if (s == null) {
                Text("Ruolo scoperto: nessun effetto. Assumi una figura dallo Staff.", color = AC.TextDim, fontSize = 12.sp)
                PcButton("Vai allo Staff", onStaff, Modifier.fillMaxWidth())
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.name, color = AC.Text)
                    Stelle(Staffing.mezzeStelle(s.skill))
                }
                Text("Contratto ${s.contract} anni", color = AC.TextDim, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun Pagina(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
}

/** Capo preparatori: stato di ogni giocatore e attività consigliata per oggi. */
@Composable
fun SchedaPreparatori(staff: List<Staff>, rosa: List<Player>, onStaff: () -> Unit) {
    Pagina {
        CapoRuolo("Capo preparatori", staff.firstOrNull { it.role == Staffing.PREPARATORE }, onStaff)
        SectionTitle("Attività consigliata oggi")
        rosa.sortedBy { it.condition }.forEach { p ->
            val att = when {
                p.injuredRounds > 0 -> "Recupero in infermeria"
                p.condition < 65 -> "Riposo"
                p.morale < 45 -> "Svago e colloquio"
                p.condition < 85 -> "Carico leggero"
                else -> "Allenamento mirato"
            }
            PaperPanel(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("${p.name} (${p.role})", color = AC.Text, fontSize = 13.sp)
                        Text("Forma ${p.condition} · Morale ${p.morale}", color = AC.TextDim, fontSize = 11.sp)
                    }
                    Text(att, color = AC.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Fisioterapisti: infortunati con i turni di stop e giocatori a rischio per stanchezza. */
@Composable
fun SchedaFisio(staff: List<Staff>, rosa: List<Player>, onStaff: () -> Unit) {
    Pagina {
        CapoRuolo("Fisioterapisti", staff.firstOrNull { it.role == Staffing.FISIOTERAPISTA }, onStaff)
        val infortunati = rosa.filter { it.injuredRounds > 0 }.sortedByDescending { it.injuredRounds }
        SectionTitle("Infermeria (${infortunati.size})")
        if (infortunati.isEmpty()) Text("Nessun infortunato.", color = AC.TextDim, fontSize = 12.sp)
        infortunati.forEach { Text("${it.name}: ancora ${it.injuredRounds} turni di stop", color = AC.Text, fontSize = 13.sp) }
        val rischio = rosa.filter { it.injuredRounds == 0 && it.condition < 60 }
        SectionTitle("A rischio per stanchezza (${rischio.size})")
        rischio.forEach { Text("${it.name}: forma ${it.condition}", color = AC.Text, fontSize = 13.sp) }
    }
}

/** Giardiniere: stato del manto erboso (dalla sua abilità) e avanzamento dei cantieri. */
@Composable
fun SchedaGiardiniere(staff: List<Staff>, mig: AndrocalcioViewModel.MigliorieStato, onStaff: () -> Unit) {
    val g = staff.firstOrNull { it.role == Staffing.TUTTOFARE }
    Pagina {
        CapoRuolo("Giardiniere e tuttofare", g, onStaff)
        val v = g?.skill ?: Staffing.VUOTO
        val stato = when { v >= 75 -> "Ottimo"; v >= 60 -> "Buono"; v >= 45 -> "Discreto"; else -> "Scadente" }
        SectionTitle("Stato del campo")
        Text("$stato (curato al livello $v/99)", color = AC.Text)
        SectionTitle("Cantieri (${mig.cantieri.size})")
        if (mig.cantieri.isEmpty()) Text("Nessun cantiere aperto.", color = AC.TextDim, fontSize = 12.sp)
        mig.cantieri.forEach { (id, gg) ->
            val m = Migliorie.trova(id)
            Text("${m?.icona ?: ""} ${m?.nome ?: id}: mancano $gg giornate", color = AC.Text, fontSize = 13.sp)
        }
    }
}
