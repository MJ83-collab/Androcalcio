package it.androcalcio.ui

import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcChip

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PaperRow
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.flow.first
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.data.Player
import it.androcalcio.engine.EventType
import it.androcalcio.engine.MatchEngine
import it.androcalcio.engine.TacticalRoles
import it.androcalcio.ui.components.Crest
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.theme.AC

/** Pannello della partita dal vivo aperto dalla barra in basso. */
private enum class LivePanel { NESSUNO, TATTICA, STAT, ASSISTENTE }

/** Partita dal vivo: tabellone in alto, campo grande al centro con telecronaca e vice, fascia comandi in basso. */
@Composable
fun LiveMatchScreen(info: AndrocalcioViewModel.LiveInfo, vm: AndrocalcioViewModel, onProssima: () -> Unit = {}) {
    val m = info.m
    @Suppress("UNUSED_VARIABLE") val t = info.tick   // ricomposizione a ogni minuto
    val tutti = m.events.toList()
    // Telecronaca e cronaca restano sincronizzate con il campo: il testo di un'azione compare quando la palla ha finito il tragitto
    var mostrati by remember { mutableStateOf(tutti.size) }
    val events = tutti.take(mostrati.coerceAtMost(tutti.size))
    val ignorati = setOf(EventType.YELLOW, EventType.SUB, EventType.TACTIC, EventType.RED, EventType.INJURY,
        EventType.COMMENTO, EventType.VAR)
    val last = events.lastOrNull { it.type !in ignorati }
    val lastGoal = events.lastOrNull { it.type == EventType.GOAL }
    val lento = info.tempo == it.androcalcio.engine.Meteo.Tempo.PIOGGIA || info.tempo == it.androcalcio.engine.Meteo.Tempo.NEVE ||
        info.prato == it.androcalcio.engine.Meteo.Campo.PESANTE || info.prato == it.androcalcio.engine.Meteo.Campo.PESSIMO
    val palla = rememberPalla(tutti, m.minute, m.possessoCasa, info.speed >= 4, m.homeF, m.awayF, if (lento) 1.25f else 1f)
    val pallaCorrente = androidx.compose.runtime.rememberUpdatedState(palla)
    androidx.compose.runtime.LaunchedEffect(tutti.size) {
        if (tutti.size < mostrati) mostrati = tutti.size
        if (tutti.size > mostrati) {
            val nuovi = tutti.subList(mostrati, tutti.size)
            if (info.speed < 4 && nuovi.any { it.percorso.isNotEmpty() }) {
                val partita = kotlinx.coroutines.withTimeoutOrNull(900) { androidx.compose.runtime.snapshotFlow { pallaCorrente.value.inVolo }.first { it } }
                if (partita != null) kotlinx.coroutines.withTimeoutOrNull(7000) { androidx.compose.runtime.snapshotFlow { pallaCorrente.value.inVolo }.first { !it } }
                kotlinx.coroutines.delay(200)
            }
            mostrati = tutti.size
        }
    }
    var replay by remember { mutableStateOf<it.androcalcio.engine.MatchEvent?>(null) }
    var subOpen by remember { mutableStateOf(false) }
    var pausaPrima by remember { mutableStateOf(false) }   // per riprendere da sola dopo il cambio
    var panel by remember { mutableStateOf(LivePanel.NESSUNO) }
    val mySubs = if (info.userHome) m.subsH else m.subsA
    val consiglio by vm.consiglio.collectAsState()
    val livello by vm.assistenteLivello.collectAsState()
    // Fischio finale: prima voti e statistiche di ogni giocatore, poi si passa alla partita successiva
    if (m.finished) { FinePartitaScreen(info, vm, onProssima); return }

    // Momenti chiave a tutto schermo: gol, espulsioni, rigori, VAR (non a velocità ×20)
    var momento by remember { mutableStateOf<it.androcalcio.engine.MatchEvent?>(null) }
    var visti by remember { mutableStateOf(events.size) }
    androidx.compose.runtime.LaunchedEffect(events.size) {
        if (events.size > visti) {
            val nuovi = events.subList(visti, events.size)
            visti = events.size
            if (info.speed < 20) {
                val ev = nuovi.lastOrNull { it.type == EventType.VAR && titoloMomento(it) != null }
                    ?: nuovi.lastOrNull { titoloMomento(it) != null }
                if (ev != null) momento = ev
            }
        }
    }

    Box(Modifier.fillMaxSize().fondoApp(Brush.verticalGradient(listOf(AC.Night, AC.NightHi)))) {
    Column(Modifier.fillMaxSize()) {
        val larga = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 720
        val colA: androidx.compose.foundation.lazy.LazyListScope.() -> Unit = {
            item { Scoreboard(m.home, m.away, if (mostrati >= tutti.size) m.hg else events.count { it.type == EventType.GOAL && it.home }, if (mostrati >= tutti.size) m.ag else events.count { it.type == EventType.GOAL && !it.home }, "${it.androcalcio.engine.etichettaMinuto(m.minute)}'" + if (info.paused) " · pausa" else "") }
            item { PossessoBar(m.possessoCasa) }
        }
        val colB: androidx.compose.foundation.lazy.LazyListScope.() -> Unit = {
            consiglio?.let { c ->
                item {
                    AssistenteBubble(c, onApplica = {
                        val az = vm.applicaConsiglio()
                        if (az is it.androcalcio.engine.AssistenteLive.Azione.Cambio) { pausaPrima = info.paused; vm.setLivePaused(true); subOpen = true }
                    }, onChiudi = { vm.chiudiConsiglio() })
                }
            }
            item { Text("Arbitro: ${m.arbitro.nome} · ${m.arbitro.profilo}" + (if (m.varAttivo) " · VAR" else "") +
                (if (info.atmosfera.isNotEmpty()) " · Stadio ${info.atmosfera}" else ""),
                color = AC.NavyText, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            if (lastGoal != null && m.minute - lastGoal.minute <= 3) {
                item { GolBanner("GOL! " + lastGoal.text, lastGoal.minute) }
                item { PcChip(selected = false, onClick = { vm.setLivePaused(true); replay = lastGoal }, label = { Text("▶ Replay del gol") }) }
            }
        }
        val colC: androidx.compose.foundation.lazy.LazyListScope.() -> Unit = {
            item { MangaPitch(m.home, m.away, palla.x, palla.y, m.homeF, m.awayF, m.xiH.toList(), m.xiA.toList(), palla.attacco, palla.scia, palla.quota, palla.inVolo,
                info.tempo, info.prato, m.lineH, m.lineA, palla.sim,
                m.homePiano?.ruoliNonPoss.orEmpty(), m.awayPiano?.ruoliNonPoss.orEmpty()) }
            item { TelecronacaBubble(events, m.minute) }
        }
        val colD: androidx.compose.foundation.lazy.LazyListScope.() -> Unit = {
            when (panel) {
                LivePanel.TATTICA -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("MODULO", color = AC.NavyText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val attuale = if (info.userHome) m.homeF else m.awayF
                                listOf(it.androcalcio.engine.Formation.F442, it.androcalcio.engine.Formation.F433,
                                    it.androcalcio.engine.Formation.F352, it.androcalcio.engine.Formation.F451,
                                    it.androcalcio.engine.Formation.F433A, it.androcalcio.engine.Formation.F4231A).forEach { f ->
                                    PcChip(selected = f == attuale, onClick = { vm.liveModulo(f) }, label = { Text(f.label) })
                                }
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("RITMO", color = AC.NavyText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val attuale = if (info.userHome) m.ritmoH else m.ritmoA
                                it.androcalcio.engine.Ritmo.values().forEach { r ->
                                    PcChip(selected = r == attuale, onClick = { vm.liveRitmo(r) }, label = { Text(r.label) })
                                }
                            }
                        }
                    }
                    item {
                        val contro = if (info.userHome) m.contropiedeH else m.contropiedeA
                        PcChip(selected = contro, onClick = { vm.liveContropiede(!contro) },
                            label = { Text(if (contro) "Contropiede: attivo" else "Contropiede: off") })
                    }
                    item { MentalityPicker(if (info.userHome) m.mH else m.mA) { vm.liveMentality(it) } }
                    item {
                        TacticsPicker(
                            if (info.userHome) m.pressH else m.pressA, if (info.userHome) m.lineH else m.lineA,
                            { vm.livePressing(it) }, { vm.liveLine(it) }
                        )
                    }
                }
                LivePanel.STAT -> item { StatPanel(m.tiriCasa, m.tiriOspiti, m.possessoCasa, m.extraH, m.extraA) }
                LivePanel.ASSISTENTE -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("IL TUO VICE", color = AC.NavyText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Attivo", "Discreto", "Muto").forEachIndexed { i, l ->
                                PcChip(selected = livello == i, onClick = { vm.setAssistenteLivello(i) }, label = { Text(l) })
                            }
                        }
                        Text(when (livello) {
                            0 -> "Ti ricorda tutto: cambi, tattica, arbitro."
                            1 -> "Parla solo per le cose importanti e ferma la partita."
                            else -> "Non parla. Decidi tutto tu."
                        }, color = Color.White, fontSize = 12.sp)
                    }
                }
                LivePanel.NESSUNO -> item { CronacaPanel(events.filter { it.type != EventType.COMMENTO }.reversed().take(12)) }
            }
            item { PcAction(onClick = { vm.liveSkip() }, modifier = Modifier.fillMaxWidth()) { Text("Termina partita") } }
        }
        if (larga) Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LazyColumn(Modifier.weight(1.2f).fillMaxHeight(), contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { colA(); colC() }
            LazyColumn(Modifier.weight(1f).fillMaxHeight(), contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { colB(); colD() }
        } else LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            colA(); colB(); colC(); colD()
        }
        // Fascia comandi fissa in basso
        Column(Modifier.fillMaxWidth().background(AC.Night).padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(1, 2, 4, 20).forEach { x ->
                    PcChip(selected = info.speed == x, onClick = { vm.setLiveSpeed(x) }, label = { Text("×$x", fontSize = 12.sp) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NeonTile("Tattica", Icons.Filled.Edit, panel == LivePanel.TATTICA, Modifier.weight(1f)) {
                    panel = if (panel == LivePanel.TATTICA) LivePanel.NESSUNO else LivePanel.TATTICA
                }
                NeonTile("Cambi $mySubs/${MatchEngine.MAX_SUBS}", Icons.Filled.Refresh, false, Modifier.weight(1f),
                    enabled = mySubs < MatchEngine.MAX_SUBS) { pausaPrima = info.paused; vm.setLivePaused(true); subOpen = true }
                NeonTile(when (livello) { 0 -> "Vice"; 1 -> "Vice·"; else -> "Muto" }, Icons.Filled.Person,
                    panel == LivePanel.ASSISTENTE, Modifier.weight(1f)) {
                    panel = if (panel == LivePanel.ASSISTENTE) LivePanel.NESSUNO else LivePanel.ASSISTENTE
                }
                NeonTile("Stat", Icons.Filled.List, panel == LivePanel.STAT, Modifier.weight(1f)) {
                    panel = if (panel == LivePanel.STAT) LivePanel.NESSUNO else LivePanel.STAT
                }
                NeonTile(if (info.paused) "Riprendi" else "Pausa", Icons.Filled.PlayArrow, info.paused, Modifier.weight(1f),
                    glifo = if (info.paused) null else "❚❚", colore = if (info.paused) AC.Ok else null) {
                    vm.setLivePaused(!info.paused)
                }
            }
        }
    }
    replay?.let { ev ->
        it.androcalcio.ui.ReplayGol(ev, m) { replay = null; vm.setLivePaused(false) }
    }
    momento?.let { ev ->
        val tutti = m.xiH + m.xiA + m.benchH + m.benchA
        MomentoChiave(titoloMomento(ev) ?: "", ev.text, if (ev.home) m.home else m.away,
            tutti.firstOrNull { it.id == ev.playerId }, { momento = null }, scenaVar = ev.type == EventType.VAR)
    }
    }

    if (subOpen) SubDialog(
        xi = if (info.userHome) m.xiH.toList() else m.xiA.toList(),
        bench = if (info.userHome) m.benchH.toList() else m.benchA.toList(),
        energy = { (m.energyOf(it) * 100).toInt() },
        ammoniti = events.filter { it.type == EventType.YELLOW }.map { it.playerId }.toSet(),
        onDone = { out, inP -> if (out != null && inP != null) vm.liveSub(out, inP); subOpen = false; if (!pausaPrima) vm.setLivePaused(false) }
    )
}

/** Titolo del momento chiave per un evento, oppure null se l'evento non merita la scena a tutto schermo. */
private fun titoloMomento(e: it.androcalcio.engine.MatchEvent): String? {
    val rigore = e.text.contains("rigore", ignoreCase = true)
    return when {
        e.type == EventType.VAR -> when {
            e.text.contains("annullat", true) -> if (rigore) "RIGORE ANNULLATO!" else "GOL ANNULLATO!"
            e.text.contains("valid", true) || e.text.contains("conferm", true) -> "GOL VALIDO!"
            else -> "CONTROLLO VAR"
        }
        e.type == EventType.GOAL -> if (rigore) "GOL SU RIGORE!" else "GOOOL!"
        e.type == EventType.RED -> "ESPULSO!"
        e.type == EventType.INJURY -> "INFORTUNIO!"
        e.type == EventType.SAVE && rigore -> "RIGORE PARATO!"
        e.type == EventType.MISS && rigore -> "RIGORE SBAGLIATO!"
        else -> null
    }
}

/** Barra del possesso: ciano per la casa, magenta per gli ospiti. */
@Composable
private fun PossessoBar(casa: Int) {
    val c = casa.coerceIn(1, 99)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$c%", color = AC.Neon, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, modifier = Modifier.width(40.dp))
        Row(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))) {
            Box(Modifier.weight(c.toFloat()).fillMaxHeight().background(AC.Neon))
            Box(Modifier.weight((100 - c).toFloat()).fillMaxHeight().background(AC.NeonPink))
        }
        Text("${100 - c}%", color = AC.NeonPink, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp,
            textAlign = TextAlign.End, modifier = Modifier.width(40.dp))
    }
}

/** Fascia gialla del gol, come in TV. */
@Composable
private fun GolBanner(testo: String, minuto: Int) {
    Box(
        Modifier.fillMaxWidth().background(AC.GoalBanner, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("$testo (${it.androcalcio.engine.etichettaMinuto(minuto)}')", color = AC.Night, fontWeight = FontWeight.Black, fontSize = 15.sp,
            maxLines = 2, textAlign = TextAlign.Center)
    }
}

/** Pulsante della barra comandi: icona sopra, scritta sotto, bordo ciano se attivo. */
@Composable
private fun NeonTile(label: String, icon: ImageVector, on: Boolean, modifier: Modifier = Modifier,
                     enabled: Boolean = true, glifo: String? = null, colore: Color? = null, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    val col = colore ?: if (!enabled) AC.TextDim else if (on) AC.Neon else Color.White
    Column(
        modifier.clip(shape).background(Brush.verticalGradient(listOf(AC.NightHi, AC.Night)))
            .border(1.5.dp, if (on) AC.Neon else AC.Neon.copy(alpha = 0.3f), shape)
            .clickable(enabled = enabled, onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (glifo != null) Text(glifo, color = col, fontSize = 18.sp, fontWeight = FontWeight.Black)
        else Icon(icon, contentDescription = label, tint = col)
        Text(label.uppercase(), color = col, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

/** Statistiche della partita su carta bianca. */
@Composable
internal fun StatPanel(
    tiriCasa: Int, tiriOspiti: Int, possesso: Int,
    extraCasa: it.androcalcio.engine.StatExtra? = null, extraOspiti: it.androcalcio.engine.StatExtra? = null
) {
    PaperPanel(Modifier.fillMaxWidth()) {
        val righe = buildList {
            add("Tiri" to "$tiriCasa - $tiriOspiti")
            add("Possesso" to "$possesso% - ${100 - possesso}%")
            if (extraCasa != null && extraOspiti != null) {
                add("Passaggi" to "${extraCasa.passaggi} - ${extraOspiti.passaggi}")
                add("Precisione" to "${extraCasa.precisione}% - ${extraOspiti.precisione}%")
                add("Duelli vinti" to "${extraCasa.duelliVinti} - ${extraOspiti.duelliVinti}")
                add("Km percorsi" to "%.1f - %.1f".format(extraCasa.km, extraOspiti.km))
            }
        }
        righe.forEachIndexed { i, (k, v) ->
            PaperRow(i) {
                Text(k, Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(v, color = AC.ColBlue, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

/** Ruolo dettagliato del giocatore (DC, TS, MED...) o, se manca, la lettera del reparto. */
private fun posTxt(p: Player) = p.pos.ifEmpty { p.role }

/** Affinità % del giocatore con un ruolo dettagliato; null se non è tra i suoi ruoli. */
private fun affinita(p: Player, ruolo: String): Int? = when {
    ruolo.isEmpty() -> null
    p.pos == ruolo -> 100
    p.sec2 == ruolo -> p.aff2
    p.sec3 == ruolo -> p.aff3
    p.sec4 == ruolo -> p.aff4
    else -> null
}

private fun reparto(r: String) = when (r) { "P" -> "portiere"; "D" -> "difensore"; "C" -> "centrocampista"; else -> "attaccante" }

/**
 * Cambi in un colpo d'occhio: a sinistra chi è in campo (energia a barra), a destra la panchina.
 * In alto il cambio consigliato (il più stanco con il miglior sostituto) applicabile con un tocco.
 * Toccando chi esce, la panchina si ordina e si colora per compatibilità col suo ruolo.
 */
@Composable
private fun SubDialog(
    xi: List<Player>, bench: List<Player>, energy: (Player) -> Int, ammoniti: Set<Long>,
    onDone: (Player?, Player?) -> Unit
) {
    var out by remember { mutableStateOf<Player?>(null) }
    var inP by remember { mutableStateOf<Player?>(null) }
    fun compat(p: Player, o: Player): Int = affinita(p, TacticalRoles.assigned(o)) ?: if (p.role == o.role) 60 else 0
    fun ordina(o: Player) = bench.sortedWith(compareByDescending<Player> { compat(it, o) }.thenByDescending { it.overall })
    // Consiglio del vice: il più stanco (non portiere) e il suo miglior sostituto compatibile
    val stanco = xi.filter { it.role != "P" }.minByOrNull { energy(it) - if (it.id in ammoniti) 8 else 0 }
    val sostituto = stanco?.let { s -> ordina(s).firstOrNull { compat(it, s) >= 60 } }
    val o = out
    AlertDialog(
        onDismissRequest = { onDone(null, null) },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.96f),
        title = { Text("CAMBI · la partita è in pausa", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (stanco != null && sostituto != null && o == null) {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(AC.Accent.copy(alpha = 0.18f))
                        .clickable { onDone(stanco, sostituto) }.padding(8.dp)) {
                        Text("⚡ CAMBIO CONSIGLIATO (tocca per farlo)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.Accent)
                        Text("Esce ${stanco.name} (${energy(stanco)}%) → entra ${sostituto.name} (${posTxt(sostituto)}, ${compat(sostituto, stanco)}%)",
                            fontSize = 13.sp, color = AC.Text, fontWeight = FontWeight.Bold)
                    }
                }
                Text(if (o == null) "1) Tocca chi esce a sinistra" else "2) Tocca chi entra a destra: verde = adatto al ruolo di ${o.name}",
                    fontSize = 12.sp, color = AC.TextDim)
                Row(Modifier.fillMaxWidth().heightIn(max = 400.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        item { Text("IN CAMPO", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.Ok) }
                        items(xi) { p ->
                            val e = energy(p)
                            val sel = p == o
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                                .background(if (sel) AC.Danger.copy(alpha = 0.35f) else AC.Surface)
                                .clickable { out = if (sel) null else p; inP = null }.padding(5.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    it.androcalcio.ui.components.RuoloChip(p.role, posTxt(p))
                                    Text(" ${p.name}", fontSize = 12.sp, color = AC.Text, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                Row(Modifier.fillMaxWidth().padding(top = 2.dp).height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color(0x33FFFFFF))) {
                                    Box(Modifier.weight(e.coerceIn(1, 100).toFloat()).fillMaxHeight()
                                        .background(if (e < 80) AC.Danger else if (e < 90) AC.Warn else AC.Ok))
                                    if (e < 100) Box(Modifier.weight((100 - e).toFloat()).fillMaxHeight())
                                }
                                Text("energia $e%" + if (p.id in ammoniti) " · 🟨" else "", fontSize = 10.sp, color = AC.TextDim)
                            }
                        }
                    }
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        item { Text("PANCHINA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.Warn) }
                        items(if (o == null) bench.sortedBy { it.role } else ordina(o)) { p ->
                            val c = o?.let { compat(p, it) }
                            val sel = p == inP
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                                .background(when { sel -> AC.Ok.copy(alpha = 0.4f); c != null && c >= 80 -> AC.Ok.copy(alpha = 0.15f); else -> AC.Surface })
                                .clickable(enabled = o != null) { inP = p }.padding(5.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    it.androcalcio.ui.components.RuoloChip(p.role, posTxt(p))
                                    Text(" ${p.name}", fontSize = 12.sp, color = AC.Text, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                Text("liv ${p.overall} · cond ${p.condition}%" + (c?.let { " · ${it}%" } ?: ""), fontSize = 10.sp,
                                    color = when { c == null -> AC.TextDim; c >= 80 -> AC.Ok; c >= 50 -> AC.Warn; else -> AC.Danger })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            PcAction(onClick = { onDone(out, inP) }, enabled = out != null && inP != null) {
                Text(if (out != null && inP != null) "⇄ ${out!!.name} → ${inP!!.name}" else "Conferma cambio", maxLines = 1)
            }
        },
        dismissButton = { TextButton(onClick = { onDone(null, null) }) { Text("Chiudi") } }
    )
}
