package it.androcalcio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import it.androcalcio.engine.Friendlies
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.theme.AC

/**
 * Amichevoli di precampionato in tre passi: nazione, serie, squadra.
 * Servono almeno cinque amichevoli per entrare in forma prima del campionato.
 */
@Composable
fun AmichevoliScreen(
    team: Team,
    teams: List<Team>,
    amichevoli: List<MatchResult>,
    onRichiedi: (Long) -> Unit,
    onGioca: () -> Unit,
    onChiudi: () -> Unit,
    inviti: List<Long> = emptyList(),
    onInvito: (Long) -> Unit = {}
) {
    val nazioni = teams.map { it.nazione }.distinct()
    var nazione by rememberSaveable { mutableStateOf(team.nazione) }
    val nat = if (nazione in nazioni) nazione else team.nazione
    val serie = teams.filter { it.nazione == nat }.map { it.campionato }.distinct()
    var campionato by rememberSaveable { mutableStateOf(team.campionato) }
    val lg = if (campionato in serie) campionato else serie.firstOrNull().orEmpty()

    val prenotate = amichevoli.flatMap { listOf(it.homeTeamId, it.awayTeamId) }.filter { it != team.id }.toSet()
    val liberi = Friendlies.COUNT - amichevoli.size
    val squadre = teams.filter { it.nazione == nat && it.campionato == lg && it.id != team.id }
        .sortedBy { it.name }

    Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PcButton("Torna al calendario", onChiudi, Modifier.fillMaxWidth())
        Text("Amichevoli di precampionato", style = MaterialTheme.typography.titleMedium, color = AC.Text)
        Text(
            "Fissate ${amichevoli.size} su ${Friendlies.COUNT} · servono almeno 5 per entrare in forma",
            style = MaterialTheme.typography.bodySmall, color = AC.TextDim
        )
        val nomi = teams.associateBy { it.id }
        amichevoli.sortedBy { it.round }.forEach { m ->
            val casa = m.homeTeamId == team.id
            val avversario = nomi[if (casa) m.awayTeamId else m.homeTeamId]?.name.orEmpty()
            Text("${if (casa) "in casa" else "in trasferta"} · $avversario${if (m.played) " · giocata" else ""}",
                style = MaterialTheme.typography.bodySmall, color = AC.Text)
        }
        if (amichevoli.any { !it.played }) PcButton("Gioca le amichevoli fissate", onGioca, Modifier.fillMaxWidth())
        val daInvito = inviti.mapNotNull { id -> teams.firstOrNull { it.id == id } }.filter { o -> amichevoli.none { it.homeTeamId == o.id || it.awayTeamId == o.id } }
        if (daInvito.isNotEmpty()) {
            Text("Inviti ricevuti questo mese", style = MaterialTheme.typography.labelLarge, color = AC.Text)
            daInvito.forEach { o -> PcButton("Accetta l'invito di ${o.name}", { onInvito(o.id) }, Modifier.fillMaxWidth()) }
        }

        Text("1. Nazione", style = MaterialTheme.typography.labelLarge, color = AC.Text)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(nazioni) { n -> PcChip(selected = n == nat, onClick = { nazione = n; campionato = "" }, label = { Text(n) }) }
        }

        Text("2. Serie", style = MaterialTheme.typography.labelLarge, color = AC.Text)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(serie) { c -> PcChip(selected = c == lg, onClick = { campionato = c }, label = { Text(c) }) }
        }

        Text("3. Squadra · $liberi posto/i libero/i", style = MaterialTheme.typography.labelLarge, color = AC.Text)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(squadre, key = { it.id }) { t ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(t.name, color = AC.Text, style = MaterialTheme.typography.bodyMedium)
                    when {
                        t.id in prenotate -> Text("fissata", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                        liberi <= 0 -> Text("posti esauriti", color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                        else -> PcButton("Chiedi", { onRichiedi(t.id) })
                    }
                }
            }
        }
    }
}
