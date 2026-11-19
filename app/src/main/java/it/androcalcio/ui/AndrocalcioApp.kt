package it.androcalcio.ui

import it.androcalcio.engine.DefLine
import it.androcalcio.engine.Pressing
import it.androcalcio.engine.TrainingPlan
import it.androcalcio.engine.Formation
import it.androcalcio.engine.Lineup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.data.Movimento
import it.androcalcio.engine.Stadium
import it.androcalcio.engine.Sponsorship
import it.androcalcio.engine.Insurance
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.engine.Progression
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.HeadCell
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.StatChip
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC
import androidx.compose.foundation.layout.Spacer
import it.androcalcio.engine.Bilancio

enum class Screen(val label: String, val icon: ImageVector, val desc: String) {
    HOME("Home", Icons.Filled.Home, ""),
    ROSA("Rosa", Icons.Filled.Person, "Giocatori e valori"),
    PARTITA("Partita", Icons.Filled.Star, "Simulazione 2D"),
    MERCATO("Mercato", Icons.Filled.ShoppingCart, "Acquisti e cessioni"),
    VIVAIO("Vivaio", Icons.Filled.Face, "Giovani e scouting"),
    SQUADRE("Squadre", Icons.Filled.List, "Cambia squadra"),
    CALENDARIO("Calendario", Icons.Filled.DateRange, "Giornate e risultati"),
    FINANZE("Finanze", Icons.Filled.AccountBox, "Bilancio e sponsor"),
    STAFF("Staff", Icons.Filled.Settings, "Allenatori e preparatori"),
    STATISTICHE("Statistiche", Icons.Filled.Info, "Marcatori e portieri"),
    EDITOR("Editor", Icons.Filled.Build, "Modifica i dati"),
    SALVATAGGI("Salvataggi", Icons.Filled.Done, "Salva e carica la partita")
}

private val bottomTabs = listOf(
    Screen.HOME, Screen.ROSA, Screen.PARTITA, Screen.MERCATO, Screen.VIVAIO
)

@Composable
fun AndrocalcioApp(vm: AndrocalcioViewModel = viewModel()) {
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    val teams by vm.teams.collectAsState()
    val team by vm.selectedTeam.collectAsState()
    val players by vm.players.collectAsState()
    val report by vm.report.collectAsState()
    val matches by vm.matches.collectAsState()
    val season by vm.season.collectAsState()
    val globalNotice by vm.notice.collectAsState()

    val ask by vm.askStartup.collectAsState()
    if (ask) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Quali dati vuoi usare?") },
            text = { Text("Hai una copia personalizzata. Nessuna copia viene sovrascritta.") },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { vm.chooseStartup(0) }, modifier = Modifier.fillMaxWidth()) { Text("Personalizzati") }
                    Button(onClick = { vm.chooseStartup(1) }, modifier = Modifier.fillMaxWidth()) { Text("Predefiniti") }
                    TextButton(onClick = { vm.chooseStartup(2) }, modifier = Modifier.fillMaxWidth()) { Text("Continua come ora") }
                }
            }
        )
    }

    if (globalNotice != null && screen != Screen.VIVAIO && screen != Screen.EDITOR) {
        AlertDialog(
            onDismissRequest = { vm.clearNotice() },
            title = { Text("Androcalcio") },
            text = { Text(globalNotice ?: "") },
            confirmButton = { TextButton(onClick = { vm.clearNotice() }) { Text("OK") } }
        )
    }

    BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

    Scaffold(
        containerColor = AC.Bg,
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().background(
                androidx.compose.ui.graphics.Brush.verticalGradient(listOf(AC.Bg, AC.Surface))
            )
        ) {
        HeaderBar(team, season, screen)
        Row(Modifier.weight(1f)) {
        Box(Modifier.weight(1f).fillMaxSize()) {
            val liveInfo by vm.live.collectAsState()
            val li = liveInfo
            if (li != null) LiveMatchScreen(li, vm) else when (screen) {
                Screen.HOME -> HomeScreen(team, players) { screen = it }
                Screen.ROSA -> {
                    val modulo by vm.formation.collectAsState()
                    val press by vm.pressing.collectAsState()
                    val linea by vm.line.collectAsState()
                    val allen by vm.training.collectAsState()
                    RosaScreen(team, players, { p, y -> vm.renew(p, y) },
                        modulo, { vm.setFormation(it) }, { vm.toggleStarter(it) }, { vm.coachLineup() },
                        press, linea, { vm.setPressing(it) }, { vm.setLine(it) },
                        allen, { vm.setTraining(it) })
                }
                Screen.SQUADRE -> TeamList(teams, team?.id) {
                    vm.selectTeam(it)
                    screen = Screen.HOME
                }
                Screen.PARTITA -> {
                    val ment by vm.mentality.collectAsState()
                    val modulo by vm.formation.collectAsState()
                    PartitaScreen(team, teams, report, { h, a, f -> vm.play(h, a, f) }, { vm.resetMatch() },
                        ment, { vm.setMentality(it) }, modulo, { vm.setFormation(it) })
                }
                Screen.CALENDARIO -> {
                    val ment by vm.mentality.collectAsState()
                    CalendarioScreen(team, teams, matches, { vm.playNextRound() }, { vm.playSeason() }, season,
                        { vm.closeSeason() }, ment, { vm.setMentality(it) }, { vm.playNextRoundLive() })
                }
                Screen.MERCATO -> {
                    val market by vm.market.collectAsState()
                    val role by vm.marketRole.collectAsState()
                    val offer by vm.offerResult.collectAsState()
                    MercatoScreen(
                        team, teams, players, market, role, offer,
                        { vm.setMarketRole(it) }, { p, price -> vm.offer(p, price) },
                        { vm.sell(it) }, { vm.clearOffer() }
                    )
                }
                Screen.VIVAIO -> {
                    val youths by vm.youths.collectAsState()
                    val scouted by vm.scouted.collectAsState()
                    val reports by vm.reports.collectAsState()
                    val notice by vm.notice.collectAsState()
                    VivaioScreen(
                        team, teams, players, youths, scouted, reports, notice, season,
                        { vm.generateYouths() }, { vm.promote(it) }, { vm.discard(it) },
                        { n, c -> vm.sendScout(n, c) }, { vm.followPlayer(it) }, { vm.clearNotice() }
                    )
                }
                Screen.EDITOR -> {
                    val notice by vm.notice.collectAsState()
                    EditorScreen(vm, team, players, notice)
                }
                Screen.FINANZE -> {
                    val movimenti by vm.movimenti.collectAsState()
                    val movStagione by vm.movimentiStagione.collectAsState()
                    val stagione by vm.season.collectAsState()
                    val assicurazione by vm.insurance.collectAsState()
                    FinanzeScreen(team, players, movimenti, { vm.expandStadium() }, { vm.setSponsor(it) },
                        assicurazione, { vm.setInsurance(it) }, movStagione, stagione)
                }
                Screen.STATISTICHE -> {
                    val goals by vm.goals.collectAsState()
                    val everyone by vm.allPlayers.collectAsState()
                    StatsScreen(team, teams, matches, goals, everyone, season)
                }
                Screen.STAFF -> {
                    val staffList by vm.staff.collectAsState()
                    val cands by vm.candidates.collectAsState()
                    StaffScreen(
                        team, staffList, cands,
                        { vm.hire(it) }, { vm.fire(it) }, { vm.refreshCandidates() }
                    )
                }
                Screen.SALVATAGGI -> {
                    val slots by vm.slots.collectAsState()
                    SalvataggiScreen(slots, team?.name, season,
                        { vm.saveSlot(it) }, { vm.loadSlot(it) }, { vm.deleteSlot(it) })
                }
                else -> ComingSoon(screen)
            }
        }
        SideMenu(screen) { screen = it }
        }
        }
    }
}

/** Barra in alto: squadra e stagione a sinistra, titolo della schermata a destra. */
@Composable
fun HeaderBar(team: Team?, season: String, screen: Screen) {
    if (AC.pc7) { HeaderBarPc7(team, season, screen); return }
    Row(
        Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (team != null) Crest(team, size = 34.dp)
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
            Text(team?.name ?: "Androcalcio", style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text("${flagOf(team?.nazione ?: "")} ${team?.campionato ?: ""} · Stagione $season",
                style = MaterialTheme.typography.bodySmall, color = AC.TextDim, maxLines = 1)
        }
        Text(
            if (screen == Screen.HOME) "ANDROCALCIO" else screen.label.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AC.Accent
        )
    }
}

/** Barra in alto stile PC Calcio 7: riquadri squadra a sinistra, banda del titolo al centro, cartellino data a destra. */
@Composable
fun HeaderBarPc7(team: Team?, season: String, screen: Screen) {
    val shape = RoundedCornerShape(3.dp)
    Row(
        Modifier.fillMaxWidth().background(AC.Surface).border(1.dp, AC.Border)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(Modifier.weight(1.15f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                Modifier.fillMaxWidth().clip(shape).background(Color(0xFFD5DEF5)).border(1.dp, AC.Btn, shape)
                    .padding(horizontal = 5.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (team != null) { Crest(team, size = 20.dp); Spacer(Modifier.width(5.dp)) }
                Text(team?.name ?: "Androcalcio", color = AC.PaperText, fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold, maxLines = 1)
            }
            Text(
                "${flagOf(team?.nazione ?: "")} ${team?.campionato ?: ""}",
                Modifier.fillMaxWidth().clip(shape).background(Color(0xFF2E3F8F)).border(1.dp, AC.Btn, shape)
                    .padding(horizontal = 5.dp, vertical = 3.dp),
                color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1
            )
        }
        Box(
            Modifier.weight(1.2f).height(40.dp).clip(shape)
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(AC.BannerTop, AC.BannerBottom)))
                .border(2.dp, Color(0xFF2B3A7A), shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (screen == Screen.HOME) "ANDROCALCIO" else screen.label.uppercase(),
                color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp, maxLines = 1,
                style = MaterialTheme.typography.titleMedium.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(Color(0xFF1B2A66), blurRadius = 4f)
                )
            )
        }
        Column(
            Modifier.weight(0.8f).clip(shape).background(Color.White).border(1.dp, AC.Btn, shape)
                .padding(vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Stagione", color = AC.PaperText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(season, color = Color(0xFF1E3FA0), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

/** Menu laterale a elenco di testo, come nei gestionali di una volta. */
@Composable
fun SideMenu(current: Screen, onSelect: (Screen) -> Unit) {
    Column(
        Modifier.width(84.dp).fillMaxHeight().background(Color.Black.copy(alpha = 0.3f))
            .verticalScroll(rememberScrollState()).padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Screen.values().forEach { sc ->
            val on = sc == current
            Text(
                sc.label,
                modifier = Modifier.fillMaxWidth()
                    .background(if (AC.pc7) (if (on) AC.Btn else AC.Surface) else if (on) AC.Accent.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelect(sc) }
                    .padding(horizontal = 10.dp, vertical = 11.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (on) AC.Accent else AC.Text
            )
        }
    }
}

@Composable
fun HomeScreen(team: Team?, players: List<Player>, go: (Screen) -> Unit) {
    if (team == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Caricamento...", color = AC.TextDim)
        }
        return
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Crest(team, size = 64.dp)
                Column(Modifier.padding(start = 16.dp)) {
                    Text(team.name, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Sponsor: ${team.sponsor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AC.TextDim
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip("Budget", money(team.budget), Modifier.weight(1f))
            StatChip("Stadio", "${team.stadiumCapacity}", Modifier.weight(1f))
            StatChip("Rosa", "${players.size}", Modifier.weight(1f))
        }

        val ctx = androidx.compose.ui.platform.LocalContext.current
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Tema", style = MaterialTheme.typography.titleSmall)
            listOf("Scuro", "Turchese", "Blu", "PC 7").forEachIndexed { i, n ->
                androidx.compose.material3.FilterChip(
                    selected = AC.tema == i,
                    onClick = {
                        AC.tema = i
                        ctx.getSharedPreferences("androcalcio", 0).edit().putInt("tema", i).apply()
                    },
                    label = { Text(n) }
                )
            }
        }

        SectionTitle("Menu")
        val tiles = listOf(
            Screen.ROSA, Screen.PARTITA, Screen.MERCATO, Screen.VIVAIO,
            Screen.CALENDARIO, Screen.FINANZE, Screen.STAFF, Screen.STATISTICHE, Screen.EDITOR,
            Screen.SQUADRE, Screen.SALVATAGGI
        )
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { s -> MenuTile(s, Modifier.weight(1f)) { go(s) } }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MenuTile(s: Screen, modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassCard(modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(AC.Accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(s.icon, contentDescription = s.label, tint = AC.Accent)
            }
            Text(
                s.label,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Text(s.desc, style = MaterialTheme.typography.bodySmall, color = AC.TextDim)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamList(teams: List<Team>, selectedId: Long?, onSelect: (Team) -> Unit) {
    val leagues = teams.map { "${it.nazione} · ${it.campionato}" }.distinct()
    val current = teams.firstOrNull { it.id == selectedId }
    var league by rememberSaveable {
        mutableStateOf(current?.let { "${it.nazione} · ${it.campionato}" } ?: "")
    }
    val active = if (league in leagues) league else leagues.firstOrNull().orEmpty()
    val shown = teams.filter { "${it.nazione} · ${it.campionato}" == active }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Scegli la squadra",
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            style = MaterialTheme.typography.headlineSmall
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leagues) { l ->
                FilterChip(
                    selected = l == active,
                    onClick = { league = l },
                    label = { Text(l) }
                )
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shown, key = { it.id }) { t ->
                GlassCard(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .clickable { onSelect(t) }
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Crest(t, size = 48.dp)
                        Column(Modifier.padding(start = 14.dp)) {
                            Text(
                                t.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (t.id == selectedId) AC.Accent else AC.Text
                            )
                            Text(
                                "${t.sponsor} · Stadio ${t.stadiumCapacity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AC.TextDim
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun roleColor(role: String): Color = when (role) {
    "P" -> AC.Warn
    "D" -> AC.Accent2
    "C" -> AC.Ok
    else -> AC.Danger
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosaScreen(
    team: Team?, players: List<Player>, onRenew: (Player, Int) -> Unit,
    formation: Formation, onFormation: (Formation) -> Unit,
    onToggleStarter: (Player) -> Unit, onCoach: () -> Unit,
    press: Pressing, line: DefLine, onPress: (Pressing) -> Unit, onLine: (DefLine) -> Unit,
    training: TrainingPlan, onTraining: (TrainingPlan) -> Unit
) {
    if (team == null) return
    var renew by remember { mutableStateOf<Player?>(null) }
    var sheet by remember { mutableStateOf<Player?>(null) }
    sheet?.let { p -> PlayerSheet(p, team, onClose = { sheet = null }, onRenew = { renew = p; sheet = null }) }
    renew?.let { p ->
        var years by remember(p) { mutableStateOf(3) }
        AlertDialog(
            onDismissRequest = { renew = null },
            title = { Text("Rinnovo ${p.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Contratto attuale: ${p.contract} anni · ${money(p.salary)}")
                    Text("Richiesta: ${money(Progression.wage(p))} l'anno", color = AC.Accent)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { y ->
                            FilterChip(selected = years == y, onClick = { years = y }, label = { Text("$y") })
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { onRenew(p, years); renew = null }) { Text("Rinnova") } },
            dismissButton = { TextButton(onClick = { renew = null }) { Text("Annulla") } }
        )
    }
    LazyColumn(
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { RosaTable(players, onToggleStarter) { sheet = it } }
        item { FormazioneCard(players, formation, onFormation, onCoach) }
        item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { TacticsPicker(press, line, onPress, onLine) } } }
        item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { TrainingPicker(training, onTraining) } } }
    }
}

private fun roleName(r: String) = when (r) { "P" -> "POR"; "D" -> "DIF"; "C" -> "MED"; else -> "ATT" }

/** Rosa a tabella su carta bianca (stile PC Calcio 7). Scorre in orizzontale per mostrare tutte le colonne. */
@Composable
private fun RosaTable(players: List<Player>, onToggleStarter: (Player) -> Unit, onOpen: (Player) -> Unit) {
    val stats = listOf("EN", "VE", "RE", "AG", "QU", "FO", "CF", "LIV")
    val statColor = listOf(AC.ColEn, AC.ColBlue, AC.ColBlue, AC.ColBlue, AC.ColBlue, AC.ColGreen, AC.ColGreen, AC.ColOrange)
    PaperPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                HeadCell("T", AC.PaperText, Modifier.width(24.dp))
                HeadCell("GIOCATORE", AC.PaperText, Modifier.width(118.dp), TextAlign.Start)
                stats.forEachIndexed { i, h -> HeadCell(h, statColor[i], Modifier.width(30.dp)) }
                HeadCell("POS", AC.PaperText, Modifier.width(38.dp))
            }
            players.forEachIndexed { i, p ->
                PaperRow(i, Modifier.clickable { onOpen(p) }) {
                    Text(if (p.starter) "●" else "○",
                        Modifier.width(24.dp).clickable { onToggleStarter(p) },
                        color = if (p.starter) AC.ColGreen else AC.PaperLine,
                        textAlign = TextAlign.Center, fontSize = 14.sp)
                    Text(p.name + if (p.injuredRounds > 0) " ✚" else "", Modifier.width(118.dp), maxLines = 1,
                        color = if (p.injuredRounds > 0) AC.ColEn else AC.PaperText,
                        fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    val values = listOf(p.en, p.ve, p.re, p.ag, p.qu, p.fo, p.condition, p.overall)
                    values.forEachIndexed { k, v ->
                        Text("$v", Modifier.width(30.dp), textAlign = TextAlign.Center, fontSize = 12.sp,
                            fontWeight = if (k == 7) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (k == 6 && v < 60) AC.ColEn else if (k == 6 && v < 80) AC.ColOrange else AC.PaperText)
                    }
                    Text(roleName(p.role), Modifier.width(38.dp), textAlign = TextAlign.Center,
                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.PaperText)
                }
            }
        }
    }
}

@Composable
fun FinanzeScreen(
    team: Team?, players: List<Player>, movimenti: List<Movimento> = emptyList(),
    onExpand: () -> Unit = {}, onSponsor: (String) -> Unit = {},
    insurance: Int = 0, onInsurance: (Int) -> Unit = {},
    bilancioStagione: List<Movimento> = emptyList(), stagione: String = ""
) {
    if (team == null) return
    val wages = players.sumOf { it.salary }
    val gate = (team.stadiumCapacity * team.ticketPrice * 0.7).toLong()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Situazione: tabella a due colonne
        PaperPanel(Modifier.fillMaxWidth()) {
            listOf(
                "Budget disponibile" to money(team.budget),
                "Monte ingaggi (annuo)" to money(wages),
                "Incasso stimato a partita" to money(gate),
                "Prezzo biglietto" to "€ ${team.ticketPrice}",
                "Stadio" to "${team.stadiumCapacity} posti",
                "Sponsor" to team.sponsor
            ).forEachIndexed { i, (k, v) ->
                PaperRow(i) {
                    Text(k, Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(v, color = if (i == 0) AC.ColGreen else AC.ColBlue, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionTitle("Stadio")
                Text(
                    "+${Stadium.STEP} posti per ${money(Stadium.blockCost)}. Più posti, più incasso. Tetto ${Stadium.MAX_CAPACITY}.",
                    color = AC.TextDim, style = MaterialTheme.typography.bodySmall
                )
                PcButton("Amplia stadio", onExpand, main = true,
                    enabled = Stadium.canExpand(team.stadiumCapacity) && team.budget >= Stadium.blockCost)

                SectionTitle("Sponsor e merchandising")
                Text(
                    "Incassati a fine stagione. Merchandising: ${money(Sponsorship.merch(team.stadiumCapacity))} l'anno.",
                    color = AC.TextDim, style = MaterialTheme.typography.bodySmall
                )
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Sponsorship.offers.forEach { o ->
                        PcButton("${o.name} · ${money(o.annual)}", { onSponsor(o.name) }, main = o.name == team.sponsor)
                    }
                }

                SectionTitle("Assicurazione infortuni")
                Text(
                    "Premio a fine stagione; rimborsa una quota dell'ingaggio per ogni turno di stop.",
                    color = AC.TextDim, style = MaterialTheme.typography.bodySmall
                )
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        Insurance.NONE to "Nessuna",
                        Insurance.BASE to "Base · ${money(Insurance.premium(Insurance.BASE))} (50%)",
                        Insurance.FULL to "Completa · ${money(Insurance.premium(Insurance.FULL))} (100%)"
                    ).forEach { (lvl, label) -> PcButton(label, { onInsurance(lvl) }, main = insurance == lvl) }
                }
            }
        }

        // Bilancio di stagione
        val righe = remember(bilancioStagione) { Bilancio.riepilogo(bilancioStagione) }
        val entrate = righe.sumOf { it.entrate }
        val uscite = righe.sumOf { it.uscite }
        SectionTitle("Bilancio di stagione $stagione")
        PaperPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp)) {
                HeadCell("CATEGORIA", AC.PaperText, Modifier.weight(1f), TextAlign.Start)
                HeadCell("ENTRATE", AC.ColGreen, Modifier.width(78.dp), TextAlign.End)
                HeadCell("USCITE", AC.ColEn, Modifier.width(78.dp), TextAlign.End)
            }
            if (righe.isEmpty()) {
                PaperRow(0) { Text("Nessun movimento in questa stagione.", color = AC.PaperText, fontSize = 12.sp) }
            }
            righe.forEachIndexed { i, r ->
                PaperRow(i) {
                    Text(r.categoria, Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(if (r.entrate > 0) money(r.entrate) else "-", Modifier.width(78.dp), textAlign = TextAlign.End,
                        color = AC.PaperText, fontSize = 12.sp)
                    Text(if (r.uscite > 0) money(r.uscite) else "-", Modifier.width(78.dp), textAlign = TextAlign.End,
                        color = AC.PaperText, fontSize = 12.sp)
                }
            }
            if (righe.isNotEmpty()) {
                val risultato = entrate - uscite
                PaperRow(righe.size) {
                    Text("TOTALE", Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Text(money(entrate), Modifier.width(78.dp), textAlign = TextAlign.End, color = AC.ColGreen,
                        fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Text(money(uscite), Modifier.width(78.dp), textAlign = TextAlign.End, color = AC.ColEn,
                        fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
                PaperRow(righe.size + 1) {
                    Text("RISULTATO", Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Text((if (risultato >= 0) "+" else "") + money(risultato), color = if (risultato >= 0) AC.ColGreen else AC.ColEn,
                        fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Movimenti di cassa
        SectionTitle("Movimenti di cassa")
        PaperPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp)) {
                HeadCell("VOCE", AC.PaperText, Modifier.weight(1f), TextAlign.Start)
                HeadCell("IMPORTO", AC.ColBlue, Modifier.width(76.dp), TextAlign.End)
                HeadCell("SALDO", AC.ColOrange, Modifier.width(76.dp), TextAlign.End)
            }
            if (movimenti.isEmpty()) {
                PaperRow(0) { Text("Nessun movimento registrato.", color = AC.PaperText, fontSize = 12.sp) }
            }
            movimenti.forEachIndexed { i, m ->
                PaperRow(i) {
                    Column(Modifier.weight(1f)) {
                        Text(m.label, color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(m.season, color = AC.PaperLine, fontSize = 10.sp)
                    }
                    Text((if (m.importo >= 0) "+" else "") + money(m.importo), Modifier.width(76.dp), textAlign = TextAlign.End,
                        color = if (m.importo >= 0) AC.ColGreen else AC.ColEn, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Text(money(m.saldo), Modifier.width(76.dp), textAlign = TextAlign.End, color = AC.PaperText, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ComingSoon(s: Screen) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        GlassCard {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(s.icon, contentDescription = s.label, tint = AC.Accent)
                Text(
                    s.label,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "In arrivo nei prossimi passi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AC.TextDim
                )
            }
        }
    }
}

/** Modulo, conteggio dei titolari per ruolo e proposta dell'allenatore. */
@Composable
private fun FormazioneCard(
    players: List<Player>, formation: Formation,
    onFormation: (Formation) -> Unit, onCoach: () -> Unit
) {
    val xi = players.filter { it.starter }
    val need = Lineup.needs(formation)
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Formazione", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(Formation.values().toList()) { f ->
                    FilterChip(selected = f == formation, onClick = { onFormation(f) }, label = { Text(f.label) })
                }
            }
            val ok = xi.size == 11 && need.all { (r, n) -> xi.count { it.role == r } == n }
            Text(
                "Titolari ${xi.size}/11 · " + need.entries.joinToString("  ") { (r, n) -> "$r ${xi.count { it.role == r }}/$n" },
                color = if (ok) AC.Ok else AC.Warn, style = MaterialTheme.typography.bodySmall
            )
            if (xi.any { it.injuredRounds > 0 }) Text(
                "Hai titolari infortunati: in partita l'allenatore li sostituisce.",
                color = AC.Danger, style = MaterialTheme.typography.bodySmall
            )
            if (xi.isEmpty()) Text("Nessun titolare scelto: decide l'allenatore.",
                color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
            Button(onClick = onCoach) { Text("Chiedi all'allenatore") }
            Text("Tocca ● / ○ per mettere o togliere un titolare.",
                color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
        }
    }
}
