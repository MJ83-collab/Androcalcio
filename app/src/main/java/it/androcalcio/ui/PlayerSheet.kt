package it.androcalcio.ui

import it.androcalcio.engine.Forma

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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.data.DettaglioGiocatore
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import it.androcalcio.engine.Attr
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

/** Bandiera come emoji a partire dal nome della nazione del campionato. */
fun flagOf(nazione: String): String = when (nazione) {
    "Italia" -> "🇮🇹"; "Spagna" -> "🇪🇸"; "Inghilterra" -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿"; "Germania" -> "🇩🇪"
    "Francia" -> "🇫🇷"; "Brasile" -> "🇧🇷"; "Argentina" -> "🇦🇷"; else -> "⚽"
}

/** Stelle 0..5 da un valore 1..99 (una stella ogni 20 punti). */
private fun stars(v: Int): String { val n = (v / 20).coerceIn(0, 5); return "★".repeat(n) + "☆".repeat(5 - n) }

/** Apre la scheda di un giocatore da qualunque schermo: il root dell'app la mostra. */
val LocalApriGiocatore = compositionLocalOf<(Player) -> Unit> { {} }

/** Azioni sulla scheda: preferito, capitano, allenamento individuale. Null = nascoste. */
data class AzioniGiocatore(
    val mioClub: Long, val preferiti: Set<Long>, val capitano: Long, val allenamento: Map<Long, Int>,
    val onPreferito: (Player) -> Unit, val onCapitano: (Player?) -> Unit, val onAllenamento: (Player, Int?) -> Unit,
    val vice: Long = 0L, val onVice: (Player?) -> Unit = {}
)
val LocalAzioni = compositionLocalOf<AzioniGiocatore?> { null }

/** Carica i dati completi di un giocatore (squadra, nazionale, convocazioni, stagioni). */
val LocalCaricaGiocatore = compositionLocalOf<(suspend (Player) -> DettaglioGiocatore)?> { null }

/** Rende cliccabile un nome: al tocco si apre la scheda completa. */
@Composable
fun Modifier.apriScheda(p: Player): Modifier {
    val apri = LocalApriGiocatore.current
    return this.clickable { apri(p) }
}

/** Scheda con i dati caricati dal database (nazionale, convocazioni, stagioni, clausola). */
@Composable
fun SchedaGiocatore(p: Player, team: Team?, onClose: () -> Unit, onRenew: (() -> Unit)? = null) {
    val carica = LocalCaricaGiocatore.current
    val dettaglio by produceState<DettaglioGiocatore?>(null, p.id) { value = carica?.invoke(p) }
    PlayerSheet(p, team, onClose, onRenew, dettaglio)
}

/** Scheda del giocatore come "Informazione del giocatore" del gioco originale: dati, parametri, abilità a stelle, contratto. */
@Composable
fun PlayerSheet(
    p: Player, team: Team?, onClose: () -> Unit, onRenew: (() -> Unit)? = null,
    dettaglio: DettaglioGiocatore? = null
) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = AC.Paper,
        title = null,
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth().background(AC.ColBlue).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.runtime.CompositionLocalProvider(it.androcalcio.ui.components.LocalMagliaSquadra provides
                        team?.takeIf { it.id == p.teamId }?.let { androidx.compose.ui.graphics.Color(it.colorPrimary) to androidx.compose.ui.graphics.Color(it.colorSecondary) }) {
                        it.androcalcio.ui.components.Avatar(p.name, 48.dp)
                    }
                    Text((if (p.numero > 0) "${p.numero}  " else "") + p.name.uppercase(), color = Color.White,
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.padding(start = 10.dp))
                }
                Text(Attr.posName(p).uppercase(), fontWeight = FontWeight.ExtraBold, color = AC.PaperText, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag("${p.age} anni", AC.ColGreen); Tag(p.nationality, AC.ColBlue, Modifier.apriNazione(p.nationality))
                    Tag(Attr.FOOT_NAMES[p.foot] ?: "Destro", AC.ColOrange)
                }
                val squadra = dettaglio?.squadra ?: team?.name
                if (squadra != null) Text(squadra, Modifier.apriSquadraNome(squadra), fontSize = 12.sp, color = AC.ColBlue, fontWeight = FontWeight.Bold)
                Text(if (p.injuredRounds > 0) "INFORTUNATO: ${p.injuredRounds} turni" else "NON INFORTUNATO",
                    fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (p.injuredRounds > 0) AC.ColEn else AC.ColGreen)
                Row(Modifier.fillMaxWidth().background(AC.PaperAlt).padding(6.dp)) {
                    Column(Modifier.weight(1f)) {
                        Param("VELOCITÀ", p.ve); Param("RESISTENZA", p.re); Param("AGGRESSIVITÀ", p.ag)
                        Param("QUALITÀ", p.qu); Param("STATO DI FORMA", p.fo); Param("MORALE", p.morale); Param("CARISMA", it.androcalcio.engine.Leadership.carisma(p))
                    }
                    Column(Modifier.padding(start = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MEDIA", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = AC.ColBlue)
                        Text("${p.overall}", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = AC.PaperText)
                        Text("pot. ${p.potential}", fontSize = 10.sp, color = AC.PaperText)
                    }
                }
                Column(Modifier.fillMaxWidth().background(Color(0xFF0B0B10)).padding(6.dp)) {
                    listOf("PORTIERE" to p.gm, "PASSAGGIO" to p.pa, "DRIBBLING" to p.dr,
                        "FINALIZZAZIONE" to p.fi, "ENTRATE" to p.en, "TIRO" to p.ti, "RIGORI" to p.ri).forEach { (l, v) ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(l, Modifier.weight(1f), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(stars(v), color = Color(0xFFFFE23D), fontSize = 12.sp)
                            Text("$v", Modifier.width(30.dp), color = Color(0xFFFFE23D), fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().background(AC.PaperAlt).padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("INGAGGIO ANNUO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = AC.ColBlue)
                        Text(money(p.salary), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AC.PaperText)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ANNI DI CONTRATTO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = AC.ColBlue)
                        Text("${p.contract}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AC.PaperText)
                    }
                }
                LocalAzioni.current?.let { az ->
                    Titolo("AZIONI")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        it.androcalcio.ui.components.PcChip(p.id in az.preferiti, { az.onPreferito(p) },
                            { Text(if (p.id in az.preferiti) "★ Seguito" else "☆ Segui") })
                        if (p.teamId == az.mioClub) it.androcalcio.ui.components.PcChip(p.id == az.capitano, { az.onCapitano(if (p.id == az.capitano) null else p) },
                            { Text(if (p.id == az.capitano) "© Capitano" else "Fai capitano") })
                        if (p.teamId == az.mioClub && p.id != az.capitano) it.androcalcio.ui.components.PcChip(p.id == az.vice, { az.onVice(if (p.id == az.vice) null else p) },
                            { Text(if (p.id == az.vice) "Vicecapitano" else "Fai vice") })
                    }
                    if (p.teamId == az.mioClub) {
                        Text("Allenamento individuale", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.ColBlue)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val cur = az.allenamento[p.id]
                            it.androcalcio.ui.components.PcChip(cur == null, { az.onAllenamento(p, null) }, { Text("Come la squadra") })
                            it.androcalcio.engine.TrainFocus.values().forEachIndexed { i, f ->
                                it.androcalcio.ui.components.PcChip(cur == i, { az.onAllenamento(p, i) }, { Text(f.label) })
                            }
                        }
                    }
                }
                Titolo("NAZIONALE")
                Text("Nazionale: ${flagOf(p.nationality)} ${p.nationality}", Modifier.apriNazione(p.nationality), fontSize = 12.sp, color = AC.ColBlue, fontWeight = FontWeight.Bold)
                if (dettaglio == null) {
                    Text("Caricamento…", fontSize = 11.sp, color = AC.PaperText)
                } else {
                    val presenzeNaz = dettaglio.nazionaleStagioni.sumOf { it.presenze }
                    val golNaz = dettaglio.nazionaleStagioni.sumOf { it.gol }
                    Text("Presenze in nazionale: $presenzeNaz · gol: $golNaz", fontSize = 12.sp, color = AC.PaperText)
                    if (dettaglio.convocazioni.isEmpty()) Text("Mai convocato.", fontSize = 11.sp, color = AC.PaperText)
                    dettaglio.convocazioni.take(8).forEach { c ->
                        Text("${c.season} · ${c.competizione}: ${c.risultato}", fontSize = 11.sp, color = AC.PaperText)
                    }
                    Titolo("STAGIONI")
                    if (dettaglio.stagioni.isEmpty()) Text("Nessuna partita registrata.", fontSize = 11.sp, color = AC.PaperText)
                    dettaglio.stagioni.forEach { st ->
                        val sq = dettaglio.squadreStagioni[st.teamId]?.let { " · $it" } ?: ""
                        val voto = dettaglio.votiStagioni[st.season]?.let { " · voto %.1f".format(it) } ?: ""
                        Text("${st.season}$sq: presenze ${st.presenze} · gol ${st.gol}$voto", fontSize = 12.sp, color = AC.PaperText)
                    }
                    if (dettaglio.stagioni.size > 1) Text("Totale: presenze ${dettaglio.stagioni.sumOf { it.presenze }} · gol ${dettaglio.stagioni.sumOf { it.gol }}",
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AC.PaperText)
                    dettaglio.minutiPct?.let { Text("Minuti giocati in stagione: $it%", fontSize = 12.sp, color = AC.PaperText) }
                    Titolo("FORMA E PARAMETRI")
                    Text("Forma recente: ${Forma.simbolo(Forma.media(dettaglio.forma))}" +
                        (if (dettaglio.forma.isNotEmpty()) " · ultimi voti ${dettaglio.forma.joinToString(" ") { "%.1f".format(it).replace('.', ',') }}" else ""),
                        fontSize = 12.sp, color = AC.PaperText)
                    RadarGiocatore(p)
                    Text("Rischio infortunio: ${Forma.rischio(p)} (indicativo, dipende da età, condizione e tenuta fisica)", fontSize = 12.sp, color = AC.PaperText)
                    Titolo("INFORTUNI")
                    if (dettaglio.infortuni.isEmpty()) Text("Nessun infortunio registrato.", fontSize = 11.sp, color = AC.PaperText)
                    dettaglio.infortuni.takeLast(8).reversed().forEach { (st, t) -> Text("$st: $t turni di stop", fontSize = 12.sp, color = AC.PaperText) }
                    if (dettaglio.infortuni.size >= 3) Text("Giocatore fragile: si è fatto male spesso.", fontSize = 11.sp, color = AC.ColEn)
                    dettaglio.clausola?.let { c ->
                        Titolo("CLAUSOLE")
                        Text("30 presenze ${money(c.bonusPresenze)} · 20 gol ${money(c.bonusGol)} · " +
                            "campionato ${money(c.bonusCampionato)} · coppa ${money(c.bonusCoppa)}",
                            fontSize = 11.sp, color = AC.PaperText)
                    }
                }
            }
        },
        confirmButton = { if (onRenew != null) PcAction(onClick = onRenew) { Text("Rinnova") } },
        dismissButton = { PcAction(onClick = onClose) { Text("OK") } }
    )
}

@Composable
private fun Titolo(testo: String) {
    Text(testo, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AC.ColBlue)
}

@Composable
private fun Tag(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(text, modifier.background(color).padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White,
        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
}

@Composable
private fun Param(label: String, v: Int) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f), fontSize = 11.sp, color = AC.ColBlue, fontWeight = FontWeight.ExtraBold)
        Text("$v", fontSize = 11.sp, color = if (v < 50) AC.ColEn else AC.PaperText, fontWeight = FontWeight.ExtraBold)
    }
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
