package it.androcalcio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.data.Player
import it.androcalcio.data.Team

enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Squadre", Icons.Filled.Home),
    ROSA("Rosa", Icons.Filled.Person),
    PARTITA("Partita", Icons.Filled.Star),
    MERCATO("Mercato", Icons.Filled.Settings),
    VIVAIO("Vivaio", Icons.Filled.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndrocalcioApp(vm: AndrocalcioViewModel = viewModel()) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    val teams by vm.teams.collectAsState()
    val selected by vm.selectedTeam.collectAsState()
    val players by vm.players.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                Tab.HOME -> TeamList(teams, selected?.id) { vm.selectTeam(it) }
                Tab.ROSA -> RosaScreen(selected, players)
                Tab.PARTITA -> Placeholder("Motore partita 2D: da implementare")
                Tab.MERCATO -> Placeholder("Mercato: da implementare")
                Tab.VIVAIO -> Placeholder("Vivaio: da implementare")
            }
        }
    }
}

@Composable
fun TeamList(teams: List<Team>, selectedId: Long?, onSelect: (Team) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(teams, key = { it.id }) { team ->
            Card(Modifier.fillMaxWidth().clickable { onSelect(team) }) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        team.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (team.id == selectedId) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Sponsor: ${team.sponsor} · Stadio: ${team.stadiumCapacity}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun RosaScreen(team: Team?, players: List<Player>) {
    if (team == null) {
        Placeholder("Seleziona una squadra dalla scheda Squadre")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Text(team.name, style = MaterialTheme.typography.headlineSmall)
        }
        items(players, key = { it.id }) { p ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${p.role}  ${p.name}")
                Text("${p.overall}", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun Placeholder(text: String) {
    Box(
        Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
