package it.androcalcio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.ui.theme.AC

/**
 * Collegamenti tra le schede: toccando una squadra (stemma o nome), una bandiera/nazionalità o un giocatore
 * si apre la scheda relativa, da qualunque schermata. Il root dell'app mostra i dialoghi.
 */
val LocalApriSquadra = compositionLocalOf<(Team) -> Unit> { {} }
val LocalApriSquadraPerNome = compositionLocalOf<(String) -> Unit> { {} }
val LocalApriNazione = compositionLocalOf<(String) -> Unit> { {} }

@Composable
fun Modifier.apriSquadra(t: Team): Modifier {
    val apri = LocalApriSquadra.current
    return this.clickable { apri(t) }
}

@Composable
fun Modifier.apriSquadraNome(nome: String): Modifier {
    val apri = LocalApriSquadraPerNome.current
    return this.clickable { apri(nome) }
}

@Composable
fun Modifier.apriNazione(nazione: String): Modifier {
    val apri = LocalApriNazione.current
    return this.clickable { apri(nazione) }
}

/** Scheda di una nazione: bandiera, club e i giocatori migliori con quella nazionalità. Ogni nome si apre. */
@Composable
fun NazioneSchedaDialog(nazione: String, giocatori: List<Player>, squadre: List<Team>, onClose: () -> Unit) {
    val dellaNazione = giocatori.filter { it.nationality == nazione }.sortedByDescending { it.overall }
    val club = squadre.filter { it.nazione == nazione }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("${flagOf(nazione)} $nazione") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${dellaNazione.size} giocatori · ${club.size} club in campionato", color = AC.TextDim, fontSize = 12.sp)
                if (club.isNotEmpty()) {
                    Text("Club", color = AC.Accent, fontSize = 12.sp)
                    club.take(8).forEach { t -> Text(t.name, Modifier.apriSquadra(t), fontSize = 13.sp) }
                }
                Text("Migliori giocatori", color = AC.Accent, fontSize = 12.sp)
                dellaNazione.take(15).forEach { p ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp).apriScheda(p), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${p.name} (${p.role}, ${p.age}a)", fontSize = 13.sp)
                        Text("${p.overall}", fontSize = 13.sp, color = AC.Accent)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Chiudi") } }
    )
}
