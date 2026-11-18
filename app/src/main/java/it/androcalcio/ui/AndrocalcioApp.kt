package it.androcalcio.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.StatChip
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

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
    EDITOR("Editor", Icons.Filled.Build, "Modifica i dati")
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

    BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

    Scaffold(
        containerColor = AC.Bg,
        bottomBar = {
            NavigationBar(containerColor = AC.Surface) {
                bottomTabs.forEach { t ->
                    NavigationBarItem(
                        selected = screen == t,
                        onClick = { screen = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AC.Bg,
                            selectedTextColor = AC.Accent,
                            indicatorColor = AC.Accent,
                            unselectedIconColor = AC.TextDim,
                            unselectedTextColor = AC.TextDim
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (screen) {
                Screen.HOME -> HomeScreen(team, players) { screen = it }
                Screen.ROSA -> RosaScreen(team, players)
                Screen.SQUADRE -> TeamList(teams, team?.id) {
                    vm.selectTeam(it)
                    screen = Screen.HOME
                }
                Screen.PARTITA -> PartitaScreen(team, teams, report, { h, a, f -> vm.play(h, a, f) }, { vm.resetMatch() })
                Screen.CALENDARIO -> CalendarioScreen(team, teams, matches, { vm.playNextRound() }, { vm.playSeason() })
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
                        team, teams, players, youths, scouted, reports, notice,
                        { vm.generateYouths() }, { vm.promote(it) }, { vm.discard(it) },
                        { n, c -> vm.sendScout(n, c) }, { vm.followPlayer(it) }, { vm.clearNotice() }
                    )
                }
                Screen.FINANZE -> FinanzeScreen(team, players)
                else -> ComingSoon(screen)
            }
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

        SectionTitle("Menu")
        val tiles = listOf(
            Screen.ROSA, Screen.PARTITA, Screen.MERCATO, Screen.VIVAIO,
            Screen.CALENDARIO, Screen.FINANZE, Screen.STAFF, Screen.EDITOR,
            Screen.SQUADRE
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

@Composable
fun RosaScreen(team: Team?, players: List<Player>) {
    if (team == null) return
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Crest(team, size = 40.dp)
                Text(
                    team.name,
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        items(players, key = { it.id }) { p ->
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                            .background(roleColor(p.role).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(p.role, color = roleColor(p.role), fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(p.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${p.age} anni · ${p.nationality} · morale ${p.morale}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AC.TextDim
                        )
                        LinearProgressIndicator(
                            progress = { p.overall / 99f },
                            modifier = Modifier.padding(top = 6.dp).fillMaxWidth().height(4.dp),
                            color = AC.Accent,
                            trackColor = AC.SurfaceHi
                        )
                    }
                    Text(
                        "${p.overall}",
                        modifier = Modifier.padding(start = 12.dp).width(32.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = AC.Accent
                    )
                }
            }
        }
    }
}

@Composable
fun FinanzeScreen(team: Team?, players: List<Player>) {
    if (team == null) return
    val wages = players.sumOf { it.salary }
    val gate = (team.stadiumCapacity * team.ticketPrice * 0.7).toLong()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Finanze", style = MaterialTheme.typography.headlineSmall)
        StatChip("Budget disponibile", money(team.budget), Modifier.fillMaxWidth())
        StatChip("Monte ingaggi (annuo)", money(wages), Modifier.fillMaxWidth())
        StatChip(
            "Incasso stimato a partita (70% stadio)",
            money(gate),
            Modifier.fillMaxWidth()
        )
        StatChip("Prezzo biglietto", "€ ${team.ticketPrice}", Modifier.fillMaxWidth())
        StatChip("Sponsor principale", team.sponsor, Modifier.fillMaxWidth())
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
