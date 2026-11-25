package it.androcalcio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import it.androcalcio.engine.SerieRigori

/** Dialogo dei rigori a scelta: tiri e parate, un tiro alla volta. */
@Composable
fun RigoriDialog(s: SerieRigori, nomeCasa: String, nomeOspiti: String, tick: Int, onScegli: (Int) -> Unit, onChiudi: () -> Unit) {
    @Suppress("UNUSED_VARIABLE") val t = tick
    val finita = s.finita
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text("Rigori: $nomeCasa ${s.golCasa} - ${s.golOspiti} $nomeOspiti") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                s.tiri.takeLast(6).forEachIndexed { _, x ->
                    val chi = (if (x.casa) nomeCasa else nomeOspiti) + if (x.chi.isNotEmpty()) " (${x.chi})" else ""
                    val esito = if (x.gol) "GOL" else if (x.lato == x.tuffo) "PARATO" else "FUORI"
                    Text("$chi: $esito (tiro ${SerieRigori.nomeLato(x.lato)}, portiere ${SerieRigori.nomeLato(x.tuffo)})", fontSize = 12.sp)
                }
                if (!finita) {
                    Text(if (s.tocca) "Tocca a te: dove tiri?" else "Tira l'avversario: dove ti tuffi?", fontSize = 14.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Sinistra", "Centro", "Destra").forEachIndexed { i, n ->
                            Button(onClick = { onScegli(i) }) { Text(n, fontSize = 12.sp) }
                        }
                    }
                } else {
                    val miaVince = if (s.casaMia) s.golCasa > s.golOspiti else s.golOspiti > s.golCasa
                    Text(if (miaVince) "Hai vinto ai rigori!" else "Hai perso ai rigori.", fontSize = 15.sp)
                }
            }
        },
        confirmButton = { if (finita) Button(onClick = onChiudi) { Text("Continua") } }
    )
}
