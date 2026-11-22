package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Player
import it.androcalcio.data.ScoutReport
import it.androcalcio.data.Team
import it.androcalcio.data.Youth
import it.androcalcio.engine.Market
import it.androcalcio.engine.Scouting
import it.androcalcio.engine.ScoutingGiovani
import it.androcalcio.ui.components.Avatar
import it.androcalcio.engine.Youths
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.StatChip
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

private fun rc(role: String): Color = when (role) {
    "P" -> AC.Warn
    "D" -> AC.Accent2
    "C" -> AC.Ok
    else -> AC.Danger
}

@Composable
private fun RoleBadge(role: String) {
    Box(
        Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(rc(role).copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) { Text(role, color = rc(role), fontWeight = FontWeight.Bold) }
}

/** Filtro per ruolo usato negli elenchi di scouting. */
@Composable
private fun FiltroRuolo(scelto: String, onScelto: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(listOf("" to "Tutti", "P" to "POR", "D" to "DIF", "C" to "MED", "A" to "ATT")) { (cod, nome) ->
            PcChip(selected = scelto == cod, onClick = { onScelto(cod) }, label = { Text(nome) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VivaioScreen(
    team: Team?,
    teams: List<Team>,
    squad: List<Player>,
    youths: List<Youth>,
    scouted: List<Player>,
    reports: List<ScoutReport>,
    notice: String?,
    season: String,
    onGenerate: () -> Unit,
    onPromote: (Youth) -> Unit,
    onDiscard: (Youth) -> Unit,
    onScout: (String, String) -> Unit,
    onFollow: (Player) -> Unit,
    onDismiss: () -> Unit,
    inPrima: Set<Long> = emptySet(),
    scheda: it.androcalcio.engine.Giovanili.Scheda? = null,
    onScheda: (Youth) -> Unit = {},
    onApparizione: (Youth) -> Unit = {},
    onChiudiScheda: () -> Unit = {},
    scoutedGiovani: List<Youth> = emptyList(),
    onScoutGiovani: (String, String) -> Unit = { _, _ -> },
    onFollowGiovane: (Youth) -> Unit = {},
    onIngaggia: (Youth) -> Unit = {}
) {
    if (team == null) return
    var tab by rememberSaveable { mutableStateOf(0) }
    val byId = teams.associateBy { it.id }
    val leagues = teams.map { "${it.nazione}|${it.campionato}" }.distinct()
    var league by rememberSaveable { mutableStateOf("") }
    var ruolo by rememberSaveable { mutableStateOf("") }
    val active = if (league in leagues) league else "${team.nazione}|${team.campionato}"

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Vivaio e scouting", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip("Allenatore giovanili", "${team.youthCoachSkill}", Modifier.weight(1f))
            StatChip("Scout", "${team.scoutSkill}", Modifier.weight(1f))
            StatChip("Budget", money(team.budget), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PcChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Giovani") })
            PcChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Scouting") })
            PcChip(selected = tab == 2, onClick = { tab = 2 }, label = { Text("Vivai") })
        }

        if (tab == 0) {
            if (team.youthClaimed != season) {
                PcAction(
                    onClick = onGenerate, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)
                ) { Text("Nuova leva $season") }
            }
            if (youths.isEmpty() && team.youthClaimed == season) {
                Text("Nessun giovane da valutare per questa stagione.", color = AC.TextDim)
            }
            LazyColumn(
                Modifier.weight(1f), contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(youths, key = { it.id }) { y ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(y.name, 44.dp)
                                Box(Modifier.padding(start = 8.dp)) { RoleBadge(y.role) }
                                Column(Modifier.padding(start = 12.dp)) {
                                    Text(y.name, style = MaterialTheme.typography.titleSmall)
                                    Text("${y.age} anni · ${y.nationality}" + if (y.id in inPrima) " · con la prima squadra" else "",
                                        style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
                                }
                            }
                            Text(
                                "Livello: ${Youths.judge(y.overall)} · Potenziale: " +
                                    Youths.judge(Youths.perceived(y, team.youthCoachSkill)),
                                modifier = Modifier.padding(top = 8.dp), color = AC.Accent
                            )
                            Text(
                                "Liv. ${ScoutingGiovani.stelleTesto(y.overall)}   Pot. " +
                                    ScoutingGiovani.stelleTesto(Youths.perceived(y, team.youthCoachSkill)),
                                color = Color(0xFFFFC24B), fontSize = 13.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PcAction(onClick = { onScheda(y) }) { Text("Scheda", fontSize = 12.sp) }
                                if (y.age >= it.androcalcio.engine.Giovanili.ETA_APPARIZIONE && y.id !in inPrima)
                                    PcAction(onClick = { onApparizione(y) }) { Text("Prova", fontSize = 12.sp) }
                                PcAction(onClick = { onPromote(y) }) { Text("Promuovi", fontSize = 12.sp) }
                                TextButton(onClick = { onDiscard(y) }) { Text("Libera", fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        } else if (tab == 2) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(leagues) { l ->
                    PcChip(selected = l == active, onClick = { league = l }, label = { Text(l.replace("|", " · ")) })
                }
            }
            PcAction(
                onClick = { val (n, c) = active.split("|"); onScoutGiovani(n, c) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)
            ) { Text("Missione nei vivai (${money(Scouting.cost(team.scoutSkill))})") }
            FiltroRuolo(ruolo) { ruolo = it }
            val obsG = reports.associate { it.playerId to it.observations }
            if (scoutedGiovani.isEmpty()) Text("Nessun ragazzo osservato: manda gli scout nei vivai di un campionato.", color = AC.TextDim)
            LazyColumn(
                Modifier.weight(1f), contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scoutedGiovani.filter { ruolo.isEmpty() || it.role == ruolo }, key = { it.id }) { y ->
                    val n = obsG[ScoutingGiovani.chiave(y)] ?: 1
                    val r = ScoutingGiovani.rangeLivello(y, team.scoutSkill, n)
                    val pot = ScoutingGiovani.potenzialeStimato(y, team.scoutSkill, n)
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(y.name, 44.dp)
                                Box(Modifier.padding(start = 8.dp)) { RoleBadge(y.role) }
                                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                    Text(y.name, style = MaterialTheme.typography.titleSmall)
                                    Text("${byId[y.teamId]?.name ?: ""} · ${y.age} anni · ${y.nationality}",
                                        style = MaterialTheme.typography.bodySmall, color = AC.TextDim, maxLines = 1)
                                }
                                Text("${r.first}–${r.last}", style = MaterialTheme.typography.titleMedium, color = AC.Accent)
                            }
                            Text(
                                "Liv. ${ScoutingGiovani.stelleTesto((r.first + r.last) / 2)}   Pot. ${ScoutingGiovani.stelleTesto(pot)} " +
                                    "(${Youths.judge(pot)}) · osservazioni: $n",
                                modifier = Modifier.padding(top = 6.dp), color = Color(0xFFFFC24B), fontSize = 13.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = { onFollowGiovane(y) }) {
                                    Text("Osserva ancora (${money(Scouting.FOLLOW_COST)})", fontSize = 12.sp)
                                }
                                PcAction(onClick = { onIngaggia(y) }) {
                                    Text("Ingaggia (${money(ScoutingGiovani.prezzo(y))})", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(leagues) { l ->
                    PcChip(
                        selected = l == active, onClick = { league = l },
                        label = { Text(l.replace("|", " · ")) }
                    )
                }
            }
            PcAction(
                onClick = { val (n, c) = active.split("|"); onScout(n, c) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)
            ) { Text("Invia scout (${money(Scouting.cost(team.scoutSkill))})") }
            FiltroRuolo(ruolo) { ruolo = it }
            val obs = reports.associate { it.playerId to it.observations }
            LazyColumn(
                Modifier.weight(1f), contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(scouted.filter { ruolo.isEmpty() || it.role == ruolo }, key = { it.id }) { p ->
                    val n = obs[p.id] ?: 1
                    val r = Scouting.range(p, team.scoutSkill, n)
                    val mid = (r.first + r.last) / 2
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(p.name, 44.dp)
                                Box(Modifier.padding(start = 8.dp)) { RoleBadge(p.role) }
                                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                    Text(p.name, style = MaterialTheme.typography.titleSmall)
                                    Text("${byId[p.teamId]?.name ?: ""} · ${p.age} anni · ${p.nationality}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AC.TextDim, maxLines = 1)
                                }
                                Text("${r.first}–${r.last}",
                                    style = MaterialTheme.typography.titleMedium, color = AC.Accent)
                            }
                            Text(
                                "${Scouting.fit(mid, p.role, squad)} · osservazioni: $n",
                                modifier = Modifier.padding(top = 6.dp),
                                style = MaterialTheme.typography.bodySmall, color = AC.Accent2
                            )
                            TextButton(onClick = { onFollow(p) }) {
                                Text("Osserva ancora (${money(Scouting.FOLLOW_COST)})")
                            }
                        }
                    }
                }
            }
        }
    }

    scheda?.let { sc ->
        AlertDialog(
            onDismissRequest = onChiudiScheda,
            title = { Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(sc.titolo.substringBefore(" ·"), 48.dp)
                Text(sc.titolo, Modifier.padding(start = 10.dp))
            } },
            text = { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                sc.righe.forEach { Text(it) }
                Text(sc.consiglio, color = AC.Accent, fontWeight = FontWeight.Bold)
            } },
            confirmButton = { TextButton(onClick = onChiudiScheda) { Text("OK") } }
        )
    }

    notice?.let {
        AlertDialog(
            onDismissRequest = onDismiss, title = { Text("Vivaio e scouting") }, text = { Text(it) },
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
        )
    }
}
