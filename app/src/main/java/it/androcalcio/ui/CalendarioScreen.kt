package it.androcalcio.ui

import it.androcalcio.engine.DateGiornate
import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.runtime.remember
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import it.androcalcio.engine.Friendlies
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import it.androcalcio.engine.Mentality
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import it.androcalcio.engine.standings
import it.androcalcio.engine.Cup
import it.androcalcio.engine.Turni
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.theme.AC
import it.androcalcio.ui.components.HeadCell
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PcButton
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    team: Team?,
    teams: List<Team>,
    matches: List<MatchResult>,
    onNext: () -> Unit,
    season: String,
    onClose: () -> Unit,
    mentality: Mentality = Mentality.EQUILIBRATA,
    onMentality: (Mentality) -> Unit = {},
    onNextLive: () -> Unit = onNext,
    cupMatches: List<MatchResult> = emptyList(),
    friendlyMatches: List<MatchResult> = emptyList(),
    onRequestFriendly: (Long) -> Unit = {},
    onPlayFriendlies: () -> Unit = {},
    inviti: List<Long> = emptyList(), onInvito: (Long) -> Unit = {},
    onSimulaFino: (Int) -> Unit = {},
    giorno: it.androcalcio.AndrocalcioViewModel.GiornoStato? = null,
    onAttivita: (it.androcalcio.engine.CalendarioGiorni.Attivita) -> Unit = {},
    onSalta: () -> Unit = {},
    onMercato: () -> Unit = {}
) {
    var askNext by remember { mutableStateOf(false) }
    var amichevoliAperte by rememberSaveable { mutableStateOf(false) }
    if (askNext) AlertDialog(
        onDismissRequest = { askNext = false },
        title = { Text("La tua partita") },
        text = { Text("Vuoi giocare la tua partita in 2D (pausa, cambi, mentalità) o simularla?") },
        confirmButton = { PcAction(onClick = { askNext = false; onNextLive() }) { Text("Gioca in 2D") } },
        dismissButton = { TextButton(onClick = { askNext = false; onNext() }) { Text("Simula") } }
    )
    if (team == null) return
    if (amichevoliAperte) {
        AmichevoliScreen(team, teams, friendlyMatches, { onRequestFriendly(it) }, onPlayFriendlies, { amichevoliAperte = false }, inviti, onInvito)
        return
    }
    val byId = teams.associateBy { it.id }
    var tab by rememberSaveable { mutableStateOf(0) }
    var round by rememberSaveable { mutableStateOf(0) } // 0 = automatica
    val nextRound = matches.filter { !it.played }.minOfOrNull { it.round }
    val cupPending = cupMatches.any { !it.played }
    val pending = nextRound != null || cupPending
    val lastRound = matches.maxOfOrNull { it.round } ?: 1
    val shown = if (round == 0) (nextRound ?: lastRound) else round
    val tutte = matches + cupMatches
    // Il turno successivo che ti riguarda: i turni di coppa in cui non giochi passano da soli
    val miaProssima = tutte.any { !it.played && (it.homeTeamId == team.id || it.awayTeamId == team.id) }
    // Prima del campionato servono almeno cinque amichevoli fissate e giocate
    val amichevoliPronte = friendlyMatches.size >= Friendlies.MIN && friendlyMatches.all { it.played }
    val bloccoAmichevoli = false   // le amichevoli non sono obbligatorie

    Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PcButton("Calendario", { tab = 0 }, Modifier.weight(1f), main = tab == 0)
            PcButton("Campionato", { tab = 1 }, Modifier.weight(1f), main = tab == 1)
            PcButton("Coppa", { tab = 2 }, Modifier.weight(1f), main = tab == 2)
            PcButton("Tutte", { tab = 3 }, Modifier.weight(1f), main = tab == 3)
        }
        if (giorno != null) GiornoCard(giorno, { a ->
            onAttivita(a)
            if (a == it.androcalcio.engine.CalendarioGiorni.Attivita.AMICHEVOLE) amichevoliAperte = true
        }, onSalta)
        val giornoOk = giorno == null || giorno.prossima == null || giorno.giornoPartita
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Chiede 2D o simulazione solo se la tua squadra gioca nella prossima giornata
            PcButton("Prossima giornata", {
                round = 0
                if (miaProssima) { askNext = true } else { onNext() }
            }, Modifier.fillMaxWidth(), main = true, enabled = pending && !bloccoAmichevoli)
        }
        // Simula fino a: 5 giornate, apertura del mercato invernale, vigilia del prossimo derby
        if (nextRound != null && !bloccoAmichevoli) {
            val totali = lastRound
            val mercato = totali / 2 - 2
            val derby = matches.filter { !it.played && (it.homeTeamId == team.id || it.awayTeamId == team.id) }.sortedBy { it.round }
                .firstOrNull { m -> val o = byId[if (m.homeTeamId == team.id) m.awayTeamId else m.homeTeamId]; o != null && it.androcalcio.engine.Rivalita.isDerby(team.name, o.name) }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Simula fino a:", color = AC.Text, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterVertically))
                PcChip(false, { round = 0; onSimulaFino(nextRound + 4) }, { Text("+5 giornate") })
                if (nextRound <= mercato) PcChip(false, { round = 0; onSimulaFino(mercato) }, { Text("Mercato invernale") })
                if (derby != null && derby.round > nextRound) PcChip(false, { round = 0; onSimulaFino(derby.round - 1) }, { Text("Vigilia del derby (g. ${derby.round})") })
            }
        }


        if (!pending && matches.isNotEmpty()) {
            PcButton("Chiudi stagione e passa alla successiva", { round = 0; onClose() }, Modifier.fillMaxWidth(), main = true)
        }

        if (tab == 2) {
            Text("Coppa ${team.nazione} · $season", color = AC.Text,
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                SectionTitle("Amichevoli di precampionato")
                PcButton("Amichevoli: ${friendlyMatches.size}/${Friendlies.COUNT} fissate", { amichevoliAperte = true },
                    Modifier.fillMaxWidth())
                if (cupMatches.isEmpty()) {
                    Text("Coppa non ancora sorteggiata.", color = AC.Text)
                }
                cupMatches.groupBy { it.league.startsWith("EUROPA|") to Cup.stageOf(it.round) }
                    .toSortedMap(compareBy({ it.first }, { it.second })).forEach { (chiave, ms) ->
                    val stage = chiave.second
                    val teamsIn = ms.flatMap { listOf(it.homeTeamId, it.awayTeamId) }.toSet().size
                    SectionTitle(if (chiave.first) {
                        val c = it.androcalcio.engine.CoppeEuropee.Coppa.values().firstOrNull { c -> "EUROPA|${c.sigla}" == ms.first().league }
                        "${c?.nome ?: "Coppa europea"} · ${it.androcalcio.engine.CoppeEuropee.nomeTurno(stage)}"
                    } else Cup.stageName(stage, teamsIn))
                    PaperPanel(Modifier.fillMaxWidth()) {
                        ms.sortedBy { it.round }.forEachIndexed { i, m -> MatchLine(m, byId, team.id, i) }
                    }
                }
            }
        } else if (tab == 3) {
            Text("Tutte le tue partite · $season", color = AC.Text,
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                fun mine(m: MatchResult) = m.homeTeamId == team.id || m.awayTeamId == team.id
                val league = matches.filter(::mine).sortedBy { it.round }
                val cups = cupMatches.filter(::mine).sortedBy { Turni.chiave(it) }
                val friends = friendlyMatches.sortedBy { it.round }
                if (league.isNotEmpty()) {
                    SectionTitle("Campionato")
                    PaperPanel(Modifier.fillMaxWidth()) {
                        league.forEachIndexed { i, m -> MatchLine(m, byId, team.id, i) }
                    }
                }
                if (cups.isNotEmpty()) {
                    SectionTitle("Coppa e supercoppa")
                    PaperPanel(Modifier.fillMaxWidth()) {
                        cups.forEachIndexed { i, m -> MatchLine(m, byId, team.id, i, Turni.testo(DateGiornate.anno(season), Turni.chiave(m))) }
                    }
                }
                if (friends.isNotEmpty()) {
                    SectionTitle("Amichevoli")
                    PaperPanel(Modifier.fillMaxWidth()) {
                        friends.forEachIndexed { i, m -> MatchLine(m, byId, team.id, i) }
                    }
                }
                if (league.isEmpty() && cups.isEmpty() && friends.isEmpty()) {
                    Text("Nessuna partita in programma.", color = AC.Text)
                }
            }
        } else if (tab == 0) {
            Text("Giornata $shown di $lastRound · ${DateGiornate.testo(DateGiornate.anno(season), shown)} · ${team.campionato}", color = AC.Text,
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items((1..lastRound).toList()) { r ->
                    PcChip(selected = r == shown, onClick = { round = r }, label = { Text("$r") })
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                PaperPanel(Modifier.fillMaxWidth()) {
                    matches.filter { it.round == shown }.forEachIndexed { i, m -> MatchLine(m, byId, team.id, i) }
                }
            }
        } else {
            Text("Giornata $shown di $lastRound · ${team.campionato} $season", color = AC.Text,
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items((1..lastRound).toList()) { r ->
                    PcChip(selected = r == shown, onClick = { round = r }, label = { Text("$r") })
                }
            }
            val league = teams.filter { it.nazione == team.nazione && it.campionato == team.campionato }
            // Divisioni della nazione in ordine: la prima è la massima serie
            val tiers = teams.filter { it.nazione == team.nazione }.groupBy { it.campionato }
                .entries.sortedBy { e -> e.value.minOf { it.id } }.map { it.key }
            val tier = tiers.indexOf(team.campionato)
            val kk = minOf(3, league.size / 4)
            var lato by rememberSaveable { mutableStateOf(0) }   // 0 totale, 1 casa, 2 trasferta
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Totale", "Casa", "Trasferta").forEachIndexed { i, n -> PcChip(lato == i, { lato = i }, { Text(n) }) }
            }
            val fino = matches.filter { it.round <= shown }
            val rows = when (lato) {
                1 -> it.androcalcio.engine.ClassificaExtra.lato(league, fino, true)
                2 -> it.androcalcio.engine.ClassificaExtra.lato(league, fino, false)
                else -> standings(league, fino)
            }
            // Playoff: nelle serie minori l'ultimo posto promozione si gioca tra le 4 successive
            val dirette = if (kk >= 2 && league.size >= kk + 3) kk - 1 else kk
            val heads = listOf("G", "V", "N", "P", "GF", "GS", "DR", "PT")
            val headColor = listOf(AC.ColBlue, AC.ColGreen, AC.ColBlue, AC.ColEn, AC.ColBlue, AC.ColBlue, AC.ColBlue, AC.ColOrange)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                PaperPanel(Modifier.fillMaxWidth()) {
                    Column(Modifier.horizontalScroll(rememberScrollState())) {
                        Row(Modifier.background(androidx.compose.ui.graphics.Color(0xFF0A1A6B)).padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            HeadCell("", androidx.compose.ui.graphics.Color.White, Modifier.width(14.dp))
                            HeadCell("N", androidx.compose.ui.graphics.Color.White, Modifier.width(22.dp))
                            HeadCell("SQUADRA", androidx.compose.ui.graphics.Color.White, Modifier.width(128.dp), TextAlign.Start)
                            heads.forEach { h -> HeadCell(h, androidx.compose.ui.graphics.Color(0xFF8FB4FF), Modifier.width(if (h == "DR" || h == "PT") 32.dp else 26.dp)) }
                            HeadCell("FORMA", androidx.compose.ui.graphics.Color(0xFF8FB4FF), Modifier.width(70.dp))
                        }
                        rows.forEachIndexed { i, r ->
                            val mine = r.team.id == team.id
                            val c = if (mine) AC.ColBlue else AC.PaperText
                            val zone = when {
                                lato == 0 && tier > 0 && i < dirette -> "▲"
                                lato == 0 && tier > 0 && i < dirette + 4 && dirette < kk -> "◆"
                                lato == 0 && tier in 0 until tiers.lastIndex && i >= rows.size - kk -> "▼"
                                else -> ""
                            }
                            PaperRow(i, Modifier.then(if (mine) Modifier.background(AC.Mine) else Modifier)) {
                                Text(zone, Modifier.width(14.dp), fontSize = 10.sp,
                                    color = when (zone) { "▲" -> AC.ColGreen; "◆" -> AC.ColOrange; else -> AC.ColEn })
                                Text("${i + 1}", Modifier.width(22.dp).background(androidx.compose.ui.graphics.Color(0xFF0A1A6B)),
                                    textAlign = TextAlign.Center, fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold, color = androidx.compose.ui.graphics.Color.White)
                                Row(Modifier.width(128.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Crest(r.team, 18.dp)
                                    Text(r.team.name, Modifier.padding(start = 6.dp).apriSquadra(r.team), color = c, maxLines = 1,
                                        fontSize = 12.sp, fontWeight = if (mine) FontWeight.ExtraBold else FontWeight.Medium)
                                }
                                val cells = listOf(r.played, r.won, r.drawn, r.lost, r.gf, r.ga, r.gd, r.pts)
                                cells.forEachIndexed { k, v ->
                                    Text("$v", Modifier.width(if (k >= 6) 32.dp else 26.dp), textAlign = TextAlign.Center,
                                        fontSize = 12.sp, color = c,
                                        fontWeight = if (k == 7) FontWeight.ExtraBold else FontWeight.Medium)
                                }
                                val forma = it.androcalcio.engine.ClassificaExtra.forma(r.team.id, fino)
                                Row(Modifier.width(70.dp), horizontalArrangement = Arrangement.Center) {
                                    forma.forEach { ch ->
                                        Text("$ch", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(1.dp).width(12.dp).background(when (ch) { 'V' -> AC.ColGreen; 'N' -> AC.ColOrange; else -> AC.ColEn }))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Riga di una partita (casa - trasferta, risultato in riquadro nero), evidenziata se è della tua squadra. */
@Composable
private fun MatchLine(m: MatchResult, byId: Map<Long, Team>, teamId: Long, i: Int, data: String = "") {
    val h = byId[m.homeTeamId]
    val a = byId[m.awayTeamId]
    val mine = m.homeTeamId == teamId || m.awayTeamId == teamId
    val c = if (mine) AC.ColBlue else AC.PaperText
    val w = if (mine) FontWeight.ExtraBold else FontWeight.Medium
    PaperRow(i, Modifier.then(if (mine) Modifier.background(AC.Mine) else Modifier)) {
        if (data.isNotEmpty()) Text(data, Modifier.padding(end = 4.dp), fontSize = 10.sp, color = AC.PaperText)
        IconaCompetizione(m, byId[teamId]?.nazione ?: "")
        Text(h?.name ?: "?", Modifier.weight(1f).let { m -> if (h != null) m.apriSquadra(h) else m }, color = c, fontWeight = w,
            textAlign = TextAlign.End, fontSize = 12.sp, maxLines = 1)
        Box(
            Modifier.padding(horizontal = 6.dp).widthIn(min = 54.dp).background(AC.Btn).padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            val pens = if (m.homePens >= 0) "  rig ${m.homePens}-${m.awayPens}" else ""
            Text(if (m.played) "${m.homeGoals} - ${m.awayGoals}$pens" else "-", color = Color.White,
                fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
        Text(a?.name ?: "?", Modifier.weight(1f).let { m -> if (a != null) m.apriSquadra(a) else m }, color = c, fontWeight = w, fontSize = 12.sp, maxLines = 1)
    }
}

/** Icona a sinistra della partita: bandiera della nazione per il campionato, icona diversa per coppa, supercoppa e amichevole. */
@Composable
private fun IconaCompetizione(m: MatchResult, nazione: String) {
    Box(Modifier.width(26.dp), contentAlignment = Alignment.Center) {
        when {
            m.league.startsWith("AMICHEVOLE") -> Text("🤝", fontSize = 13.sp)
            m.league.startsWith("COPPA") ->
                Text(if (m.round >= Cup.SUPER_ROUND) "🥇" else "🏆", fontSize = 13.sp)
            else -> Bandiera(nazione)
        }
    }
}

/** Gagliardetto con i colori della nazione (tricolore per l'Italia). */
@Composable
private fun Bandiera(nazione: String) {
    val colori = when (nazione) {
        "Italia" -> listOf(Color(0xFF009246), Color.White, Color(0xFFCE2B37))
        "Spagna" -> listOf(Color(0xFFAA151B), Color(0xFFF1BF00), Color(0xFFAA151B))
        "Inghilterra" -> listOf(Color.White, Color(0xFFCF142B), Color.White)
        "Germania" -> listOf(Color.Black, Color(0xFFDD0000), Color(0xFFFFCE00))
        "Francia" -> listOf(Color(0xFF002395), Color.White, Color(0xFFED2939))
        "Brasile" -> listOf(Color(0xFF009C3B), Color(0xFFFFDF00), Color(0xFF009C3B))
        "Argentina" -> listOf(Color(0xFF74ACDF), Color.White, Color(0xFF74ACDF))
        "Giappone" -> listOf(Color.White, Color(0xFFBC002D), Color.White)
        "Olanda" -> listOf(Color(0xFFAE1C28), Color.White, Color(0xFF21468B))
        "Portogallo" -> listOf(Color(0xFF006600), Color(0xFFFF0000), Color(0xFFFF0000))
        "Belgio" -> listOf(Color.Black, Color(0xFFFAE042), Color(0xFFED2939))
        "Scozia" -> listOf(Color(0xFF005EB8), Color.White, Color(0xFF005EB8))
        "Turchia" -> listOf(Color(0xFFE30A17), Color.White, Color(0xFFE30A17))
        "Grecia" -> listOf(Color(0xFF0D5EAF), Color.White, Color(0xFF0D5EAF))
        else -> listOf(Color.Gray, Color.LightGray, Color.Gray)
    }
    Row(Modifier.width(18.dp).height(12.dp).clip(RoundedCornerShape(2.dp))) {
        colori.forEach { c -> Box(Modifier.weight(1f).fillMaxHeight().background(c)) }
    }
}
