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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import it.androcalcio.ui.components.FlagBox
import it.androcalcio.ui.components.HeadCell
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PaperRow
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
    onInvito: (Long) -> Unit = {},
    forza: Map<Long, Int> = emptyMap()
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
        Text("PRESTAGIONE · AMICHEVOLI", color = AC.Accent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        val giocate = amichevoli.count { it.played }
        val colore = if (amichevoli.size >= 5) AC.ColGreen else if (amichevoli.size >= 3) AC.ColOrange else AC.ColEn
        Text("Fissate ${amichevoli.size} su ${Friendlies.COUNT} (giocate $giocate) · servono almeno 5 per entrare in forma",
            fontSize = 12.sp, color = colore, fontWeight = FontWeight.Bold)
        val nomi = teams.associateBy { it.id }
        PaperPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp)) {
                HeadCell("N°", AC.PaperText, Modifier.width(24.dp))
                HeadCell("AVVERSARIO", AC.PaperText, Modifier.weight(1f), TextAlign.Start)
                HeadCell("DOVE", AC.ColBlue, Modifier.width(62.dp))
                HeadCell("STATO", AC.ColGreen, Modifier.width(62.dp))
            }
            if (amichevoli.isEmpty()) PaperRow(0) { Text("Nessuna amichevole fissata.", color = AC.PaperText, fontSize = 12.sp) }
            amichevoli.sortedBy { it.round }.forEachIndexed { i, m ->
                val casa = m.homeTeamId == team.id
                val avv = nomi[if (casa) m.awayTeamId else m.homeTeamId]
                PaperRow(i) {
                    Text("${i + 1}", Modifier.width(24.dp), textAlign = TextAlign.Center, fontSize = 12.sp, color = AC.PaperText, fontWeight = FontWeight.ExtraBold)
                    if (avv != null) FlagBox(avv.nazione, Modifier.size(width = 20.dp, height = 13.dp))
                    Text("  " + (avv?.name.orEmpty()), Modifier.weight(1f), maxLines = 1, fontSize = 12.sp, color = AC.PaperText, fontWeight = FontWeight.Bold)
                    Text(if (casa) "in casa" else "trasferta", Modifier.width(62.dp), textAlign = TextAlign.Center, fontSize = 11.sp, color = AC.ColBlue)
                    Text(if (m.played) "${m.homeGoals}-${m.awayGoals}" else "da giocare", Modifier.width(62.dp), textAlign = TextAlign.Center,
                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = if (m.played) AC.ColGreen else AC.ColOrange)
                }
            }
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
        forza[team.id]?.let { Text("La tua forza: $it · verde = più debole, rosso = più forte", fontSize = 11.sp, color = AC.TextDim) }
        PaperPanel(Modifier.weight(1f).fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp)) {
                HeadCell("SQUADRA", AC.PaperText, Modifier.weight(1f), TextAlign.Start)
                HeadCell("FORZA", AC.ColGreen, Modifier.width(44.dp))
                HeadCell("STADIO", AC.ColBlue, Modifier.width(60.dp))
                HeadCell("", AC.PaperText, Modifier.width(76.dp))
            }
            LazyColumn(Modifier.fillMaxWidth()) {
                items(squadre, key = { it.id }) { t ->
                    PaperRow(squadre.indexOf(t)) {
                        FlagBox(t.nazione, Modifier.size(width = 20.dp, height = 13.dp))
                        Text("  " + t.name, Modifier.weight(1f), maxLines = 1, fontSize = 12.sp, color = AC.PaperText, fontWeight = FontWeight.Bold)
                        val f = forza[t.id]; val mia = forza[team.id]
                        Text(f?.toString() ?: "–", Modifier.width(44.dp), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (f == null || mia == null) AC.PaperText else if (f > mia + 2) AC.ColEn else if (f < mia - 2) AC.ColGreen else AC.ColOrange)
                        Text("%,d".format(t.stadiumCapacity).replace(',', '.'), Modifier.width(60.dp), textAlign = TextAlign.Center,
                            fontSize = 11.sp, color = AC.ColBlue)
                        Box(Modifier.width(76.dp), contentAlignment = Alignment.Center) {
                            when {
                                t.id in prenotate -> Text("fissata", color = AC.ColGreen, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                liberi <= 0 -> Text("esauriti", color = AC.PaperLine, fontSize = 11.sp)
                                else -> PcButton("Chiedi", { onRichiedi(t.id) }, compatto = true)
                            }
                        }
                    }
                }
            }
        }
    }
}
