package it.androcalcio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.engine.AvatarGen
import it.androcalcio.ui.components.AvatarSpec
import it.androcalcio.ui.theme.AC

/** Editor del volto: anteprima grande, un selettore per ogni elemento, "Casuale" e "Ripristina" (volto originale). */
@Composable
fun AvatarEditorDialog(
    nome: String, iniziale: AvatarGen.Spec, modificato: Boolean,
    onSalva: (AvatarGen.Spec) -> Unit, onRipristina: () -> Unit, onClose: () -> Unit
) {
    var spec by remember { mutableStateOf(iniziale) }
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = AC.Paper,
        title = { Text(nome, fontWeight = FontWeight.ExtraBold, color = AC.PaperText, fontSize = 16.sp) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { AvatarSpec(spec, 110.dp) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = { spec = AvatarGen.casuale(kotlin.random.Random.Default) }) { Text("Casuale") }
                    TextButton(onClick = { spec = AvatarGen.daNome(nome) }) { Text("Dal nome") }
                }
                AvatarGen.CAMPI.forEachIndexed { i, campo ->
                    Text(campo.etichetta.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.PaperText)
                    val colori = campo.colori
                    if (colori != null) {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            colori.forEachIndexed { k, c ->
                                Box(Modifier.size(30.dp).background(Color(c), CircleShape)
                                    .border(if (spec[i] == k) 3.dp else 1.dp, if (spec[i] == k) Color.Black else Color.Gray, CircleShape)
                                    .clickable { spec = spec.con(i, k) })
                            }
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { spec = spec.con(i, spec[i] - 1) }) { Text("◀") }
                            Text("${campo.nomi[spec[i]]}  (${spec[i] + 1}/${campo.n})", Modifier.weight(1f), color = AC.PaperText, fontSize = 13.sp)
                            TextButton(onClick = { spec = spec.con(i, spec[i] + 1) }) { Text("▶") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSalva(spec); onClose() }) { Text("Salva") } },
        dismissButton = {
            Row {
                if (modificato) TextButton(onClick = { onRipristina(); onClose() }) { Text("Ripristina") }
                TextButton(onClick = onClose) { Text("Annulla") }
            }
        }
    )
}
