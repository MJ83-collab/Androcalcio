package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.engine.Attr
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

/** Bandiera come emoji a partire dal nome della nazione del campionato. */
fun flagOf(nazione: String): String = when (nazione) {
    "Italia" -> "🇮🇹"; "Spagna" -> "🇪🇸"; "Inghilterra" -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿"; "Germania" -> "🇩🇪"
    "Francia" -> "🇫🇷"; "Brasile" -> "🇧🇷"; "Argentina" -> "🇦🇷"; else -> "⚽"
}

/** Scheda del giocatore con tutti i parametri, in stile gestionale classico. */
@Composable
fun PlayerSheet(p: Player, team: Team?, onClose: () -> Unit, onRenew: (() -> Unit)? = null) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(p.name) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${Attr.posName(p)} · ${p.age} anni · ${p.nationality} · ${Attr.FOOT_NAMES[p.foot] ?: "Destro"}",
                    style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
                if (team != null) Text(team.name, style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
                if (p.injuredRounds > 0) Text("Infortunato: ${p.injuredRounds} turni", color = AC.Danger,
                    style = MaterialTheme.typography.bodySmall)
                Text("Livello ${p.overall} · Potenziale ${p.potential}", color = AC.Accent, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp))
                Bar("Velocità", p.ve); Bar("Resistenza", p.re); Bar("Aggressività", p.ag)
                Bar("Qualità", p.qu); Bar("Forma", p.fo)
                if (p.role == "P") Bar("Gioco di mani", p.gm)
                Bar("Entrate", p.en); Bar("Passaggi", p.pa); Bar("Dribbling", p.dr)
                Bar("Finalizzazione", p.fi); Bar("Tiro", p.ti); Bar("Rigori", p.ri)
                Text("Morale ${p.morale} · Contratto ${p.contract} anni · ${money(p.salary)}",
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = { if (onRenew != null) PcAction(onClick = onRenew) { Text("Rinnova") } },
        dismissButton = { TextButton(onClick = onClose) { Text("Chiudi") } }
    )
}

@Composable
private fun Bar(label: String, v: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(104.dp), style = MaterialTheme.typography.bodySmall)
        LinearProgressIndicator(
            progress = { v / 99f }, modifier = Modifier.weight(1f).height(6.dp),
            color = if (v >= 75) AC.Accent else if (v >= 50) AC.Text else AC.Danger, trackColor = AC.SurfaceHi
        )
        Text("$v", Modifier.width(30.dp).padding(start = 6.dp), style = MaterialTheme.typography.bodySmall)
    }
}
