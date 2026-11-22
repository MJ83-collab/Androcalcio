package it.androcalcio.ui

import android.app.Activity

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.bevel
import it.androcalcio.ui.components.PcChip

import it.androcalcio.engine.DefLine
import it.androcalcio.engine.Pressing
import it.androcalcio.engine.TacticalRoles
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccountCircle
import it.androcalcio.ui.components.PcIconBadge
import it.androcalcio.ui.components.FlagBox
import it.androcalcio.ui.theme.PcTitle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
import it.androcalcio.engine.AllStar
import it.androcalcio.engine.Strutture
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

enum class Screen(val label: String, val icon: ImageVector, val desc: String, val tint: Color) {
    HOME("Home", Icons.Filled.Home, "", Color(0xFF3F6FD8)),
    ROSA("Rosa", Icons.Filled.Person, "Giocatori e valori", Color(0xFF2F8F4E)),
    TATTICA("Tattica", Icons.Filled.Place, "Undici, ruoli e campo", Color(0xFF3AA35B)),
    AVVERSARIO("Avversario", Icons.Filled.Search, "Prossimo rivale e assistente", Color(0xFFC0392B)),
    PARTITA("Partita", Icons.Filled.PlayArrow, "Simulazione 2D", Color(0xFFE08A12)),
    MERCATO("Mercato", Icons.Filled.ShoppingCart, "Acquisti e cessioni", Color(0xFFD4A017)),
    VIVAIO("Vivaio", Icons.Filled.Face, "Giovani e scouting", Color(0xFF4CAF50)),
    SQUADRE("Squadre", Icons.Filled.Refresh, "Elenco squadre", Color(0xFF5B6BC8)),
    CALENDARIO("Calendario", Icons.Filled.DateRange, "Giornate e risultati", Color(0xFF2E86C1)),
    FINANZE("Finanze", Icons.Filled.AccountBox, "Bilancio e sponsor", Color(0xFF1E9E7A)),
    DIRIGENZA("Dirigenza", Icons.Filled.AccountBox, "Fiducia, tifosi, fondi", Color(0xFFB9770E)),
    STAFF("Staff", Icons.Filled.AccountCircle, "Allenatori e preparatori", Color(0xFF8E5BC8)),
    STATISTICHE("Statistiche", Icons.Filled.List, "Marcatori e portieri", Color(0xFF2874A6)),
    NAZIONALI("Nazionali", Icons.Filled.Star, "Convocazioni e tornei", Color(0xFF8E44AD)),
    EDITOR("Editor", Icons.Filled.Edit, "Modifica i dati", Color(0xFF7F8C8D)),
    SALVATAGGI("Salvataggi", Icons.Filled.Done, "Salva e carica la partita", Color(0xFF16A085)),
    ALBO("Albo d'oro", Icons.Filled.Star, "Campioni per stagione", Color(0xFFC9A227)),
    STRUTTURE("Stadio e centro", Icons.Filled.Build, "Migliorie, cantieri, ritorni", Color(0xFF00897B)),
    RELAZIONI("Stampa", Icons.Filled.Person, "Conferenze, colloqui, psicologo", Color(0xFFAD1457)),
    COMMERCIALE("Marketing", Icons.Filled.ShoppingCart, "Sponsor e prezzi", Color(0xFFEF6C00)),
    OBIETTIVI("Obiettivi", Icons.Filled.Done, "Dirigenza, premi, trofei", Color(0xFF6A1B9A)),
    GIORNALE("Giornale", Icons.Filled.List, "Pagelle, meteo, xG", Color(0xFF455A64)),
    PRESTITI("Prestiti", Icons.Filled.Refresh, "Finestre e prestiti", Color(0xFF1565C0)),
    SOCIETA("Società", Icons.Filled.Person, "Disciplina, scambi, storia", Color(0xFF00838F)),
    OPZIONI("Opzioni", Icons.Filled.Settings, "Difficoltà e velocità", Color(0xFF607D8B))
}

private val bottomTabs = listOf(
    Screen.HOME, Screen.ROSA, Screen.PARTITA, Screen.MERCATO, Screen.VIVAIO
)

@Composable
private fun AndrocalcioContenuto(vm: AndrocalcioViewModel) {
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var menuAperto by rememberSaveable { mutableStateOf(false) }
    val teams by vm.teams.collectAsState()
    val team by vm.selectedTeam.collectAsState()
    val players by vm.players.collectAsState()
    val report by vm.report.collectAsState()
    val matches by vm.matches.collectAsState()
    val cupMatches by vm.cupMatches.collectAsState()
    val friendlyMatches by vm.friendlyMatches.collectAsState()
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
                    PcAction(onClick = { vm.chooseStartup(0) }, modifier = Modifier.fillMaxWidth()) { Text("Personalizzati") }
                    PcAction(onClick = { vm.chooseStartup(1) }, modifier = Modifier.fillMaxWidth()) { Text("Predefiniti") }
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

    val presidente by vm.presidente.collectAsState()
    presidente?.let { msg ->
        AlertDialog(
            onDismissRequest = { vm.chiudiPresidente() },
            title = { Text("Il presidente") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { vm.chiudiPresidente(); screen = Screen.CALENDARIO }) { Text("Inizia la stagione") }
            },
            dismissButton = { TextButton(onClick = { vm.chiudiPresidente() }) { Text("Più tardi") } }
        )
    }
    val fineStagione by vm.fine.collectAsState()
    val activity = LocalContext.current as? Activity
    fineStagione?.let { f ->
        FineStagioneDialog(f,
            onSalvaEsci = { vm.salvaEEsci { activity?.finishAffinity() } },
            onNuovo = { vm.nuovoCampionato() }) { vm.chiudiFine() }
    }

    val clubNow by vm.club.collectAsState()
    if (clubNow.esonero && screen != Screen.DIRIGENZA) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Esonerato") },
            text = { Text("La dirigenza ti ha esonerato. Scegli una delle offerte di panchina.") },
            confirmButton = { TextButton(onClick = { screen = Screen.DIRIGENZA }) { Text("Vedi offerte") } }
        )
    }
    val ev by vm.evento.collectAsState()
    ev?.let { e ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text(e.titolo) },
            text = { Text(e.testo) },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    e.scelte.forEach { c -> PcAction(onClick = { vm.scegli(e, c) }, modifier = Modifier.fillMaxWidth()) { Text(c.label) } }
                }
            }
        )
    }
    val started by vm.started.collectAsState()
    if (!started && teams.isNotEmpty()) {
        val info by vm.teamInfo.collectAsState()
        NuovaPartitaScreen(teams, info) { t, nome, cognome -> vm.startGame(t, nome, cognome); screen = Screen.HOME }
        return
    }

    BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

    Scaffold(
        containerColor = AC.Bg,
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().background(
                if (AC.pc7) androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(Color(0xFF4652B8), Color(0xFF2A3A8C), Color(0xFF1B2461)))
                else androidx.compose.ui.graphics.Brush.verticalGradient(listOf(AC.Bg, AC.Surface))
            )
        ) {
        HeaderBar(team, season, screen)
        Row(Modifier.weight(1f)) {
        Box(Modifier.weight(1f).fillMaxSize()) {
            val liveInfo by vm.live.collectAsState()
            val li = liveInfo
            if (li != null) LiveMatchScreen(li, vm) else when (screen) {
                Screen.HOME -> {
                    val tm = team
                    val next = if (tm == null) null else matches
                        .filter { !it.played && (it.homeTeamId == tm.id || it.awayTeamId == tm.id) }.minByOrNull { it.round }
                    val oppId = next?.let { if (it.homeTeamId == tm?.id) it.awayTeamId else it.homeTeamId }
                    HomeScreen(team, players, season, next, teams.firstOrNull { it.id == oppId }) { screen = it }
                }
                Screen.STRUTTURE -> {
                    val mig by vm.migliorie.collectAsState()
                    LaunchedEffect(team?.id) { vm.aggiornaMigliorie() }
                    MigliorieScreen(team, mig) { vm.avviaMiglioria(it) }
                }
                Screen.SOCIETA -> {
                    val soc by vm.societa.collectAsState()
                    val tuttiG by vm.allPlayers.collectAsState()
                    LaunchedEffect(team?.id, players.size) { vm.aggiornaSocieta() }
                    SocietaScreen(team, teams, players, tuttiG, soc, { r, ok -> vm.rispondiRichiesta(r, ok) }, { m, l, c -> vm.proponiScambio(m, l, c) })
                }
                Screen.OPZIONI -> {
                    val op by vm.opzioni.collectAsState()
                    val prof by vm.profilo.collectAsState()
                    val testo by vm.testoPct.collectAsState()
                    OpzioniScreen(op, { vm.setDifficolta(it) }, { vm.setVelocita(it) }, prof, { vm.setProfilo(it) },
                        testo, { vm.setTesto(it) }, { vm.apriTutorial() })
                }
                Screen.RELAZIONI -> {
                    val ex by vm.extra.collectAsState()
                    LaunchedEffect(team?.id, players.size) { vm.aggiornaExtra() }
                    RelazioniScreen(ex, players, { vm.rispondiConferenza(it) }, { id, t -> vm.colloquio(id, t) }, { vm.setPsicologo(it) })
                }
                Screen.COMMERCIALE -> {
                    val ex by vm.extra.collectAsState()
                    val cl by vm.club.collectAsState()
                    LaunchedEffect(team?.id) { vm.aggiornaExtra() }
                    CommercialeScreen(ex, cl, { vm.setMarketing(it) }, { vm.cercaSponsor() }, { vm.acceptSponsor(it) }, { s, p -> vm.setPrezzo(s, p) })
                }
                Screen.OBIETTIVI -> {
                    val ex by vm.extra.collectAsState()
                    val cl by vm.club.collectAsState()
                    LaunchedEffect(team?.id) { vm.aggiornaExtra() }
                    ObiettiviScreen(ex, cl)
                }
                Screen.GIORNALE -> {
                    val ex by vm.extra.collectAsState()
                    LaunchedEffect(team?.id) { vm.aggiornaExtra() }
                    GiornaleScreen(ex)
                }
                Screen.PRESTITI -> {
                    val ex by vm.extra.collectAsState()
                    val tutti by vm.allPlayers.collectAsState()
                    LaunchedEffect(team?.id) { vm.aggiornaExtra() }
                    val tm = team
                    if (tm != null) {
                        val nomi = teams.associate { it.id to it.name }
                        val altri = tutti.filter { p -> p.teamId != tm.id && teams.any { t -> t.id == p.teamId && t.nazione == tm.nazione } }
                            .sortedByDescending { it.overall }.filter { it.overall <= 80 }
                        PrestitiScreen(ex, tm, players, altri, nomi, { vm.prestaGiocatore(it) }, { vm.prendiInPrestito(it) }, { vm.riscatta(it) })
                    }
                }
                Screen.ROSA -> {
                    val modulo by vm.formation.collectAsState()
                    val press by vm.pressing.collectAsState()
                    val linea by vm.line.collectAsState()
                    val allen by vm.training.collectAsState()
                    val consigli by vm.assistantAdvice.collectAsState()
                    RosaScreen(team, players, { p, y -> vm.renew(p, y) },
                        modulo, { vm.setFormation(it) }, { vm.toggleStarter(it) }, { vm.coachLineup() },
                        press, linea, { vm.setPressing(it) }, { vm.setLine(it) },
                        allen, { vm.setTraining(it) }, { p, r -> vm.setRuoloPartita(p, r) },
                        consigli, { vm.applyAssistantXI() }, { vm.applyAssistantTattica() })
                }
                Screen.SQUADRE -> {   // solo consultazione: la panchina cambia con le offerte
                    var scheda by remember { mutableStateOf<Team?>(null) }
                    TeamList(teams, team?.id) { scheda = it }
                    scheda?.let { t -> SquadraSchedaDialog(t, { x -> vm.infoSquadra(x) }, { scheda = null }) }
                }
                Screen.PARTITA -> {
                    val ment by vm.mentality.collectAsState()
                    val modulo by vm.formation.collectAsState()
                    PartitaScreen(team, teams, report, { h, a, f -> vm.play(h, a, f) }, { vm.resetMatch() },
                        ment, { vm.setMentality(it) }, modulo, { vm.setFormation(it) })
                }
                Screen.CALENDARIO -> {
                    val ment by vm.mentality.collectAsState()
                    CalendarioScreen(team, teams, matches, { vm.playNextRound() }, season,
                        { vm.closeSeason() }, ment, { vm.setMentality(it) }, { vm.playNextRoundLive() },
                        cupMatches = cupMatches,
                        friendlyMatches = friendlyMatches,
                        onRequestFriendly = { vm.requestFriendly(it) },
                        onPlayFriendlies = { vm.playFriendlies() })
                }
                Screen.MERCATO -> {
                    val market by vm.market.collectAsState()
                    val role by vm.marketRole.collectAsState()
                    val offer by vm.offerResult.collectAsState()
                    val clubOk by vm.clubAccepted.collectAsState()
                    MercatoScreen(
                        team, teams, players, market, role, offer,
                        { vm.setMarketRole(it) }, { p, price, w, y -> vm.offer(p, price, w, y) },
                        { p, y -> vm.wageAsk(p, y) }, vm.wageRoom(),
                        clubOk, { p, y -> vm.consiglioVice(p, y) },
                        { vm.sell(it) }, { vm.clearOffer() }
                    )
                }
                Screen.VIVAIO -> {
                    val youths by vm.youths.collectAsState()
                    val scouted by vm.scouted.collectAsState()
                    val scoutedGio by vm.scoutedGiovani.collectAsState()
                    val reports by vm.reports.collectAsState()
                    val notice by vm.notice.collectAsState()
                    val exViv by vm.extra.collectAsState()
                    val schedaViv by vm.scheda.collectAsState()
                    LaunchedEffect(team?.id) { vm.aggiornaExtra() }
                    VivaioScreen(
                        team, teams, players, youths, scouted, reports, notice, season,
                        { vm.generateYouths() }, { vm.promote(it) }, { vm.discard(it) },
                        { n, c -> vm.sendScout(n, c) }, { vm.followPlayer(it) }, { vm.clearNotice() },
                        exViv.inPrima, schedaViv, { vm.richiediScheda(it) }, { vm.apparizione(it) }, { vm.chiudiScheda() },
                        scoutedGio, { n, c -> vm.sendScoutGiovani(n, c) }, { vm.followGiovane(it) }, { vm.ingaggiaGiovane(it) }
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
                    val cl by vm.club.collectAsState()
                    FinanzeScreen(team, players, movimenti, { vm.expandStadium() }, { vm.setSponsor(it) },
                        cl.livAllenamento, cl.livVivaio, { vm.upgrade(it) },
                        assicurazione, { vm.setInsurance(it) }, movStagione, stagione)
                }
                Screen.NAZIONALI -> {
                    val conv by vm.convocazioniAll.collectAsState()
                    val nazStats by vm.nazionaleAll.collectAsState()
                    val alboNaz by vm.albo.collectAsState()
                    val nazStagione by vm.nazStagione.collectAsState()
                    val stagioniNaz by vm.statSeasons.collectAsState()
                    val mia by vm.nazioneAllenata.collectAsState()
                    val offertaN by vm.offertaNazionale.collectAsState()
                    val candidatiN by vm.giocatoriNazionalita.collectAsState()
                    val rosaN by vm.rosaNazionale.collectAsState()
                    NazionaleScreen((stagioniNaz + season).distinct().sorted(), nazStagione,
                        { vm.pickNazStagione(it) }, conv, nazStats, alboNaz,
                        mia, offertaN, candidatiN, rosaN,
                        { vm.aggiungiConvocato(it) }, { vm.togliConvocato(it) }, { vm.rispondiNazionale(it) })
                }
                Screen.STATISTICHE -> {
                    val goals by vm.goals.collectAsState()
                    val everyone by vm.allPlayers.collectAsState()
                    val statStagioni by vm.statSeasons.collectAsState()
                    val statAttiva by vm.statSeasonAttiva.collectAsState()
                    val statGiocatori by vm.playerStats.collectAsState()
                    val clausoleAll by vm.clausole.collectAsState()
                    StatsScreen(team, teams, matches, goals, everyone, season, statGiocatori, statStagioni,
                        statAttiva, { vm.pickStatSeason(it) }, clausoleAll)
                }
                Screen.DIRIGENZA -> {
                    val c by vm.club.collectAsState()
                    DirigenzaScreen(team, c, teams, { tipo, importo -> vm.requestFunds(tipo, importo) }, { o, ask -> vm.acceptOffer(o, ask) },
                        { vm.acceptSponsor(it) }, { vm.rejectSponsors() }, { vm.dimettiti() })
                }
                Screen.STAFF -> {
                    val staffList by vm.staff.collectAsState()
                    val cands by vm.candidates.collectAsState()
                    val missioni by vm.scoutMissions.collectAsState()
                    val allenatoreNome by vm.allenatore.collectAsState()
                    StaffScreen(
                        team, staffList, cands,
                        { vm.hire(it) }, { vm.fire(it) }, { vm.refreshCandidates() },
                        missioni, { s, area -> vm.inviaOsservatore(s, area) },
                        allenatore = allenatoreNome
                    )
                }
                Screen.TATTICA -> {
                    val modulo by vm.formation.collectAsState()
                    val press by vm.pressing.collectAsState()
                    val linea by vm.line.collectAsState()
                    val consigli by vm.assistantAdvice.collectAsState()
                    val tiriU by vm.tiriUser.collectAsState()
                    TatticaScreen(players, modulo, { vm.setFormation(it) }, { vm.coachLineup() },
                        press, linea, { vm.setPressing(it) }, { vm.setLine(it) },
                        { p, r -> vm.setRuoloPartita(p, r) },
                        consigli, { vm.applyAssistantXI() }, { vm.applyAssistantTattica() },
                        { screen = Screen.AVVERSARIO }, tiriU, { vm.salvaTiri(it) }, { p, c -> vm.setCompito(p, c) })
                }
                Screen.AVVERSARIO -> {
                    val consigli by vm.assistantAdvice.collectAsState()
                    AvversarioScreen(consigli)
                }
                Screen.ALBO -> {
                    val albo by vm.albo.collectAsState()
                    val classifica by vm.classificaAllenatori.collectAsState()
                    AlboScreen(albo, classifica)
                }
                Screen.SALVATAGGI -> {
                    val slots by vm.slots.collectAsState()
                    SalvataggiScreen(slots, team?.name, season,
                        { vm.saveSlot(it) }, { vm.loadSlot(it) }, { vm.deleteSlot(it) })
                }
                else -> ComingSoon(screen)
            }
        }
        // Menu laterale a scomparsa: chiuso resta solo una linguetta, così le schermate hanno tutto lo spazio
        MenuLinguetta(menuAperto) { menuAperto = !menuAperto }
        if (menuAperto) SideMenu(screen) { screen = it; menuAperto = false }
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
                Modifier.fillMaxWidth().clip(shape).background(Color(0xFFD5DEF5)).border(1.dp, AC.Btn, shape).bevel(w = 1.dp)
                    .padding(horizontal = 5.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (team != null) { Crest(team, size = 20.dp); Spacer(Modifier.width(5.dp)) }
                Text(team?.name ?: "Androcalcio", color = AC.PaperText, fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold, maxLines = 1)
            }
            Row(
                Modifier.fillMaxWidth().clip(shape).background(Color(0xFF2E3F8F)).border(1.dp, AC.Btn, shape).bevel(w = 1.dp)
                    .padding(horizontal = 5.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlagBox(team?.nazione ?: "")
                Spacer(Modifier.width(5.dp))
                Text(team?.campionato ?: "", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Box(
            Modifier.weight(1.2f).height(40.dp).clip(shape)
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(AC.BannerTop, AC.BannerBottom)))
                .border(2.dp, Color(0xFF2B3A7A), shape).bevel(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (screen == Screen.HOME) "ANDROCALCIO" else screen.label.uppercase(),
                color = Color.White, fontSize = 13.sp, fontFamily = PcTitle,
                letterSpacing = 1.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(Color(0xFF1B2A66), blurRadius = 4f)
                )
            )
        }
        Column(
            Modifier.weight(0.8f).clip(shape).background(Color.White).border(1.dp, AC.Btn, shape).bevel(w = 1.dp)
                .padding(vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Stagione", color = AC.PaperText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(season, color = Color(0xFF1E3FA0), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

/** Menu laterale: in PC 7 colonna di pulsanti navy in rilievo con icona e voce su una riga sola. */
/** Linguetta del menu laterale: ☰ per aprirlo, › per chiuderlo. */
@Composable
private fun MenuLinguetta(aperto: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.width(26.dp).fillMaxHeight()
            .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(AC.NavyTop, AC.NavyBottom)))
            .border(1.dp, AC.NavyBorder)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(if (aperto) "›" else "☰", color = AC.Accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun SideMenu(current: Screen, onSelect: (Screen) -> Unit) {
    if (AC.pc7) {
        Column(
            Modifier.width(104.dp).fillMaxHeight()
                .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF141B5C), Color(0xFF0A0F3A))))
                .border(1.dp, Color(0xFF060A2A))
                .verticalScroll(rememberScrollState()).padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Screen.values().forEach { sc ->
                val on = sc == current
                val shape = RoundedCornerShape(2.dp)
                Row(
                    Modifier.fillMaxWidth().clip(shape)
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                            if (on) listOf(Color(0xFF0A0A12), Color(0xFF23233A)) else listOf(AC.NavyTop, AC.NavyBottom)))
                        .border(1.dp, if (on) AC.Accent else AC.NavyBorder, shape)
                        .bevel(raised = !on, w = 1.dp)
                        .clickable { onSelect(sc) }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PcIconBadge(sc.icon, sc.tint, size = 20.dp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        sc.label.uppercase(), color = if (on) AC.Accent else Color.White,
                        fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.2.sp,
                        maxLines = 1, softWrap = false, overflow = TextOverflow.Clip
                    )
                }
            }
        }
        return
    }
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
                    .background(if (on) AC.Accent.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onSelect(sc) }
                    .padding(horizontal = 10.dp, vertical = 11.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (on) AC.Accent else AC.Text,
                maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HomeScreen(
    team: Team?, players: List<Player>, season: String = "",
    prossima: it.androcalcio.data.MatchResult? = null, avversario: Team? = null, go: (Screen) -> Unit
) {
    if (team == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Caricamento...", color = AC.TextDim)
        }
        return
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(if (AC.pc7) 10.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (AC.pc7) 10.dp else 12.dp)
    ) {
        HomeBanner(team, season)
        if (prossima != null && avversario != null) ProssimaPartitaCard(team, avversario, prossima) { go(Screen.PARTITA) }
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

        if (AC.pc7) {
            val media = if (players.isEmpty()) 0 else players.sortedByDescending { it.overall }.take(11).map { it.overall }.average().toInt()
            PaperPanel(Modifier.fillMaxWidth()) {
                listOf(
                    "Budget" to money(team.budget),
                    "Stadio" to "${team.stadiumCapacity} posti",
                    "Giocatori in rosa" to "${players.size}",
                    "Media dei titolari" to "$media"
                ).forEachIndexed { i, (k, v) ->
                    PaperRow(i) {
                        Text(k, Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(v, color = if (i == 0) AC.ColGreen else AC.ColBlue, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip("Budget", money(team.budget), Modifier.weight(1f))
            StatChip("Stadio", "${team.stadiumCapacity}", Modifier.weight(1f))
            StatChip("Rosa", "${players.size}", Modifier.weight(1f))
        }

        SectionTitle("Menu")
        val tiles = listOf(
            Screen.ROSA, Screen.TATTICA, Screen.PARTITA, Screen.AVVERSARIO, Screen.MERCATO, Screen.VIVAIO,
            Screen.CALENDARIO, Screen.FINANZE, Screen.DIRIGENZA, Screen.STAFF, Screen.STATISTICHE, Screen.NAZIONALI, Screen.EDITOR,
            Screen.STRUTTURE, Screen.RELAZIONI, Screen.COMMERCIALE, Screen.OBIETTIVI, Screen.GIORNALE, Screen.PRESTITI,
            Screen.SOCIETA, Screen.SQUADRE, Screen.SALVATAGGI, Screen.ALBO, Screen.OPZIONI
        )
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(if (AC.pc7) 8.dp else 12.dp)) {
                row.forEach { s -> MenuTile(s, Modifier.weight(1f)) { go(s) } }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MenuTile(s: Screen, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (AC.pc7) {
        // Pulsante navy in rilievo: icona colorata in alto, voce in maiuscolo e descrizione ben leggibile
        val shape = RoundedCornerShape(2.dp)
        Column(
            modifier.height(112.dp).clip(shape)
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(AC.NavyTop, AC.NavyBottom)))
                .border(2.dp, AC.NavyBorder, shape).bevel(w = 1.5.dp)
                .clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PcIconBadge(s.icon, s.tint, size = 38.dp)
            Text(s.label.uppercase(), Modifier.padding(top = 6.dp), color = Color.White, fontSize = 13.sp,
                fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, maxLines = 1, softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black, androidx.compose.ui.geometry.Offset(1f, 1f), 2f)))
            Text(s.desc, color = AC.NavyText, fontSize = 11.sp, maxLines = 2, lineHeight = 13.sp,
                textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis)
        }
        return
    }
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
            "Squadre (consultazione)",
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            style = MaterialTheme.typography.headlineSmall
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(leagues) { l ->
                PcChip(
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
    training: TrainingPlan, onTraining: (TrainingPlan) -> Unit,
    onRole: (Player, String) -> Unit = { _, _ -> },
    advice: it.androcalcio.engine.AssistantCoach.Advice? = null, onApplyXI: () -> Unit = {},
    onApplyTattica: () -> Unit = {}
) {
    if (team == null) return
    var renew by remember { mutableStateOf<Player?>(null) }
    var sheet by remember { mutableStateOf<Player?>(null) }
    sheet?.let { p -> SchedaGiocatore(p, team, onClose = { sheet = null }, onRenew = { renew = p; sheet = null }) }
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
                            PcChip(selected = years == y, onClick = { years = y }, label = { Text("$y") })
                        }
                    }
                }
            },
            confirmButton = { PcAction(onClick = { onRenew(p, years); renew = null }) { Text("Rinnova") } },
            dismissButton = { TextButton(onClick = { renew = null }) { Text("Annulla") } }
        )
    }
    LazyColumn(
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { MediaSquadraBar(team, players) }
        item { AssistantCard(advice, onApplyXI, onApplyTattica) }
        item { RosaTable(players, onToggleStarter, { sheet = it }, onRole) }
        item { FormazioneCard(players, formation, onFormation, onCoach) }
        item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { TacticsPicker(press, line, onPress, onLine) } } }
        item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { TrainingPicker(training, onTraining) } } }
    }
}

/** Testata della rosa come in PC Calcio: stemma, nome e media dei titolari a stelle. */
@Composable
private fun MediaSquadraBar(team: Team, players: List<Player>) {
    val media = if (players.isEmpty()) 0 else players.sortedByDescending { it.overall }.take(11).map { it.overall }.average().toInt()
    val shape = RoundedCornerShape(2.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape)
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(AC.NavyTop, AC.NavyBottom)))
            .border(2.dp, AC.NavyBorder, shape).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Crest(team, 36.dp)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(team.name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp, maxLines = 1)
            Text("MEDIA SQUADRA", color = AC.NavyText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(stelle(media), color = AC.Accent, fontSize = 16.sp)
        Text(" $media", color = AC.Neon, fontWeight = FontWeight.Black, fontSize = 18.sp)
    }
}

private fun roleName(r: String) = when (r) { "P" -> "POR"; "D" -> "DIF"; "C" -> "MED"; else -> "ATT" }

/** Ordine dei gruppi di ruolo in rosa: portieri, difensori, centrocampisti, attaccanti. */
private fun ordineRuolo(r: String) = when (r) { "P" -> 0; "D" -> 1; "C" -> 2; else -> 3 }
private fun ruoloTitolo(r: String) = when (r) { "P" -> "PORTIERI"; "D" -> "DIFENSORI"; "C" -> "CENTROCAMPISTI"; else -> "ATTACCANTI" }
/** Sigla di ruolo accanto al nome: P, D, C, A. */
private fun ruoloTag(r: String) = "[$r]"

/** Rosa a tabella su carta bianca (stile PC Calcio 7). Scorre in orizzontale per mostrare tutte le colonne. */
@Composable
private fun RosaTable(
    players: List<Player>, onToggleStarter: (Player) -> Unit, onOpen: (Player) -> Unit,
    onRole: (Player, String) -> Unit
) {
    val stats = listOf("EN", "VE", "RE", "AG", "QU", "SF", "MO", "ME")
    val statColor = listOf(AC.ColEn, AC.ColBlue, AC.ColBlue, AC.ColBlue, AC.ColBlue, AC.ColBlue, AC.ColGreen, AC.ColOrange)
    var roleFor by remember { mutableStateOf<Player?>(null) }
    roleFor?.let { rp ->
        AlertDialog(
            onDismissRequest = { roleFor = null },
            title = { Text("Ruolo di ${rp.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ruolo naturale al 100%. Gli affini pesano con la loro affinità.",
                        color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                    TacticalRoles.options(rp).forEach { (r, a) ->
                        PcChip(
                            selected = TacticalRoles.assigned(rp) == r,
                            onClick = { onRole(rp, if (r == rp.pos) "" else r); roleFor = null },
                            label = { Text("${TacticalRoles.label(r)} · $a%") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = { PcAction(onClick = { roleFor = null }) { Text("Chiudi") } }
        )
    }
    PaperPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                HeadCell("T", AC.PaperText, Modifier.width(24.dp))
                HeadCell("N°", AC.PaperText, Modifier.width(26.dp))
                HeadCell("", AC.PaperText, Modifier.width(25.dp))
                HeadCell("GIOCATORE", AC.PaperText, Modifier.width(118.dp), TextAlign.Start)
                HeadCell("", AC.PaperText, Modifier.width(62.dp))
                stats.forEachIndexed { i, h -> HeadCell(h, statColor[i], Modifier.width(30.dp)) }
                HeadCell("RUOLO", AC.PaperText, Modifier.width(74.dp))
            }
            // Sezioni come nell'originale: titolari, convocati (panchina), non convocati (infortunati)
            val tit = players.filter { it.starter && it.injuredRounds == 0 }
            val conv = players.filter { !it.starter && it.injuredRounds == 0 }
            val fuori = players.filter { it.injuredRounds > 0 }
            val sezioni = listOf("TITOLARI" to tit, "GIOCATORI CONVOCATI" to conv, "GIOCATORI NON CONVOCATI" to fuori)
            var idx = 0
            sezioni.forEach { (titolo, lista) -> if (lista.isNotEmpty()) {
                Text("⚽  $titolo", Modifier.fillMaxWidth().background(AC.PaperAlt).padding(vertical = 4.dp),
                    textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = AC.ColBlue)
            var ruoloCorrente = ""
            lista.sortedBy { ordineRuolo(it.role) }.forEach { p ->
                if (p.role != ruoloCorrente) {   // intestazione per ogni gruppo di ruolo
                    ruoloCorrente = p.role
                    Text("— ${ruoloTitolo(p.role)} —", Modifier.fillMaxWidth().background(AC.PaperLine).padding(vertical = 2.dp),
                        textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.PaperText)
                }
                val i = idx++
                PaperRow(i, Modifier.clickable { onOpen(p) }) {
                    Text(if (p.starter) "●" else "○",
                        Modifier.width(24.dp).clickable { onToggleStarter(p) },
                        color = if (p.starter) AC.ColGreen else AC.PaperLine,
                        textAlign = TextAlign.Center, fontSize = 14.sp)
                    Text(if (p.numero > 0) "${p.numero}" else "-", Modifier.width(26.dp), textAlign = TextAlign.Center,
                        fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = AC.PaperText)
                    it.androcalcio.ui.components.Avatar(p.name, 22.dp, Modifier.padding(end = 3.dp))
                    Text(ruoloTag(p.role) + "  " + p.name + if (p.injuredRounds > 0) " ✚" else "", Modifier.width(118.dp), maxLines = 1,
                        color = if (p.injuredRounds > 0) AC.ColEn else AC.PaperText,
                        fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    val st = (p.overall / 20).coerceIn(0, 5)
                    Text("★".repeat(st) + "☆".repeat(5 - st), Modifier.width(62.dp), fontSize = 11.sp, color = Color(0xFFE0A800))
                    val values = listOf(p.condition, p.ve, p.re, p.ag, p.qu, p.fo, p.morale, p.overall)
                    values.forEachIndexed { k, v ->
                        Text("$v", Modifier.width(30.dp), textAlign = TextAlign.Center, fontSize = 12.sp,
                            fontWeight = if (k == 7) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (k == 0 && v < 60) AC.ColEn else if (k == 0 && v < 80) AC.ColOrange else AC.PaperText)
                    }
                    // Ruolo in campo con l'affinità: tocca per passare al ruolo affine successivo
                    val opts = TacticalRoles.options(p)
                    val cur = TacticalRoles.assigned(p)
                    val affCur = opts.firstOrNull { it.first == cur }?.second ?: 100
                    Text(
                        if (p.pos.isEmpty()) roleName(p.role) else "$cur $affCur%",
                        Modifier.width(74.dp).clickable(enabled = opts.size > 1) { roleFor = p },
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                        color = if (affCur < 100) AC.ColOrange else AC.PaperText
                    )
                }
            }
            } }
        }
    }
}

@Composable
fun FinanzeScreen(
    team: Team?, players: List<Player>, movimenti: List<Movimento> = emptyList(),
    onExpand: () -> Unit = {}, onSponsor: (String) -> Unit = {},
    livAllenamento: Int = 1, livVivaio: Int = 1, onUpgrade: (String) -> Unit = {},
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
                "Budget mercato" to money(team.budget),
                "Budget ingaggi (annuo)" to money(team.wageBudget),
                "Ingaggi della rosa (annui)" to money(wages),
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

                SectionTitle("Strutture")
                listOf(Strutture.ALLENAMENTO to livAllenamento, Strutture.VIVAIO to livVivaio).forEach { (tipo, liv) ->
                    Text("${if (tipo == Strutture.ALLENAMENTO) "Centro di allenamento" else "Vivaio"} · livello $liv/${Strutture.MAX}",
                        color = AC.Text, style = MaterialTheme.typography.titleSmall)
                    Text(Strutture.descrizione(tipo, liv) + " · manutenzione " + money(Strutture.manutenzione(liv)) + " l'anno",
                        color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
                    PcButton(if (liv >= Strutture.MAX) "Livello massimo" else "Migliora · ${money(Strutture.costo(liv))}",
                        { onUpgrade(tipo) }, main = true, enabled = liv < Strutture.MAX && team.budget >= Strutture.costo(liv))
                }

                SectionTitle("Sponsor e merchandising")
                Text(
                    "Incassati a fine stagione. Merchandising: ${money(Sponsorship.merch(team.stadiumCapacity))} l'anno.",
                    color = AC.TextDim, style = MaterialTheme.typography.bodySmall
                )
                Text("Gli sponsor si gestiscono in Dirigenza (fino a 5 contratti pluriennali).",
                    color = AC.TextDim, style = MaterialTheme.typography.bodySmall)

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
                    PcChip(selected = f == formation, onClick = { onFormation(f) }, label = { Text(f.label) })
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
            PcAction(onClick = onCoach) { Text("Chiedi all'allenatore") }
            Text("Tocca ● / ○ per mettere o togliere un titolare.",
                color = AC.TextDim, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Root: rende disponibili da ovunque la scheda giocatore e il caricamento dei suoi dati. */
@Composable
fun AndrocalcioApp(vm: AndrocalcioViewModel = viewModel()) {
    var schedaAperta by remember { mutableStateOf<Player?>(null) }
    var squadraAperta by remember { mutableStateOf<Team?>(null) }
    var nazioneAperta by remember { mutableStateOf<String?>(null) }
    var avatarAperto by remember { mutableStateOf<String?>(null) }
    val avatarMod by vm.avatar.collectAsState()
    val squadre by vm.teams.collectAsState()
    val tutti by vm.allPlayers.collectAsState()
    val testoPct by vm.testoPct.collectAsState()
    val tutorial by vm.tutorial.collectAsState()
    val avviata by vm.started.collectAsState()
    val dens = androidx.compose.ui.platform.LocalDensity.current
    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(dens.density, dens.fontScale * testoPct / 100f),
        LocalApriGiocatore provides { schedaAperta = it },
        LocalCaricaGiocatore provides { vm.dettaglioGiocatore(it) },
        LocalApriSquadra provides { squadraAperta = it },
        LocalApriSquadraPerNome provides { n -> squadre.firstOrNull { it.name == n }?.let { squadraAperta = it } },
        LocalApriNazione provides { nazioneAperta = it },
        it.androcalcio.ui.components.LocalAvatarModificati provides avatarMod,
        it.androcalcio.ui.components.LocalModificaAvatar provides { n -> avatarAperto = n }
    ) {
        AndrocalcioContenuto(vm)
        if (tutorial && avviata) TutorialDialog { vm.chiudiTutorial() }
        schedaAperta?.let { p -> SchedaGiocatore(p, null, onClose = { schedaAperta = null }) }
        squadraAperta?.let { t -> SquadraSchedaDialog(t, { x -> vm.infoSquadra(x) }, { squadraAperta = null }) }
        avatarAperto?.let { n ->
            AvatarEditorDialog(n, it.androcalcio.engine.AvatarGen.specDi(n, avatarMod), avatarMod.containsKey(n),
                { sp -> vm.salvaAvatar(n, sp) }, { vm.salvaAvatar(n, null) }, { avatarAperto = null })
        }
        nazioneAperta?.let { n ->
            val asIds = squadre.filter { it.nazione == AllStar.NAZIONE }.map { it.id }.toSet()
            NazioneSchedaDialog(n, tutti.filter { it.teamId !in asIds }, squadre, { nazioneAperta = null })
        }
    }
}
