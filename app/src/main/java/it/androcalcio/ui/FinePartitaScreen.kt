package it.androcalcio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.engine.EventType
import it.androcalcio.engine.MatchReport
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.theme.AC

private val Verde = Color(0xFF2E7D32)
private val Rosso = Color(0xFFC62828)

/**
 * Schermata dopo il fischio finale: risultato, statistiche, pagelle di ogni giocatore (la tua squadra con
 * gol, minuti, morale, infortuni e crescita; l'avversario con voti, gol e minuti). Solo da qui si passa avanti.
 */
@Composable
fun FinePartitaScreen(info: AndrocalcioViewModel.LiveInfo, vm: AndrocalcioViewModel, onProssima: () -> Unit) {
    val m = info.m
    val rep = remember(info.m) { m.report() }
    val mia = if (info.userHome) m.home else m.away
    val altra = if (info.userHome) m.away else m.home
    val pronta = info.saved

    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AC.Night, AC.NightHi)))) {
        LazyColumn(
            Modifier.weight(1f), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Scoreboard(m.home, m.away, m.hg, m.ag, "Finale") }
            item { StatPanel(m.tiriCasa, m.tiriOspiti, m.possessoCasa, m.extraH, m.extraA) }
            item { Titolo("Analisi e statistiche") }
            item { AnalisiPartita(rep, info.userHome) }
            item { Titolo("Le tue pagelle · ${mia.shortName}") }
            item { Tabella(rep, info.userHome, m::minutiGiocati, info.esito, mie = true, pronta = pronta) }
            item { Titolo("Avversario · ${altra.shortName}") }
            item { Tabella(rep, !info.userHome, m::minutiGiocati, info.esito, mie = false, pronta = pronta) }
            item { Titolo("Top 11 della partita") }
            item { Top11(rep) }
            item { Titolo("Cronaca") }
            item { CronacaPanel(rep.events.reversed()) }
        }
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PcAction(
                onClick = { vm.closeLive(); onProssima() }, modifier = Modifier.fillMaxWidth(), enabled = pronta,
                colors = ButtonDefaults.buttonColors(containerColor = AC.Accent, contentColor = AC.Bg)
            ) { Text(if (pronta) "Prossima partita" else "Salvataggio in corso...") }
            TextButton(onClick = { vm.closeLive() }, enabled = pronta, modifier = Modifier.fillMaxWidth()) {
                Text("Torna al calendario")
            }
        }
    }
}

/** Le undici pagelle più alte della partita, tra le due squadre: squadra e voto. */
@Composable
private fun Top11(rep: MatchReport) {
    val top = rep.playedPlayers.sortedByDescending { rep.playerRatings[it.id] ?: 6.0 }.take(11)
    PaperPanel(Modifier.fillMaxWidth()) {
        top.forEachIndexed { i, p ->
            val casa = p.id in rep.homePlayed
            PaperRow(i) {
                Text("${i + 1}. ${p.name}", Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text((if (casa) rep.home.shortName else rep.away.shortName), color = AC.PaperLine, fontSize = 11.sp)
                Text("%.1f".format(rep.playerRatings[p.id] ?: 6.0), Modifier.width(44.dp), textAlign = TextAlign.End,
                    color = AC.ColBlue, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun Titolo(testo: String) {
    Text(testo.uppercase(), color = AC.Neon, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
}

/** Pagelle di una squadra: chi è sceso in campo, dal voto più alto; sotto il nome le statistiche personali. */
@Composable
private fun Tabella(
    rep: MatchReport, casa: Boolean, minuti: (Long) -> Int,
    esito: Map<Long, AndrocalcioViewModel.EsitoGiocatore>, mie: Boolean, pronta: Boolean
) {
    val giocati = if (casa) rep.homePlayed else rep.awayPlayed
    val ps = rep.playedPlayers.filter { it.id in giocati }.sortedByDescending { rep.playerRatings[it.id] ?: 6.0 }
    PaperPanel(Modifier.fillMaxWidth()) {
        ps.forEachIndexed { i, p ->
            val voto = rep.playerRatings[p.id] ?: 6.0
            val gol = rep.events.count { it.type == EventType.GOAL && it.playerId == p.id }
            val amm = rep.events.any { it.type == EventType.YELLOW && it.playerId == p.id }
            val ass = rep.events.count { it.type == EventType.GOAL && it.assistId == p.id }
            val esp = p.id in rep.espulsi
            val inf = p.id in rep.infortunati
            val e = if (mie) esito[p.id] else null
            PaperRow(i) {
                Column(Modifier.weight(1f)) {
                    Text("${p.pos.ifEmpty { p.role }}  ${p.name}", color = AC.PaperText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(dettagli(gol, ass, amm, esp, inf, minuti(p.id), e, mie && pronta), color = AC.PaperText.copy(alpha = 0.7f), fontSize = 11.sp)
                    e?.miglioramenti?.takeIf { it.isNotEmpty() }?.let {
                        Text("Crescita: " + it.joinToString(" · "), color = Verde, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "%.1f".format(voto), Modifier.width(44.dp), textAlign = TextAlign.End, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (voto >= 7.0) Verde else if (voto < 5.5) Rosso else AC.ColBlue
                )
            }
        }
    }
}

private fun dettagli(gol: Int, assist: Int, ammonito: Boolean, espulso: Boolean, infortunato: Boolean, minuti: Int,
                     e: AndrocalcioViewModel.EsitoGiocatore?, conEsito: Boolean): String {
    val v = mutableListOf("$minuti'")
    if (gol > 0) v += if (gol == 1) "1 gol" else "$gol gol"
    if (assist > 0) v += if (assist == 1) "1 assist" else "$assist assist"
    if (ammonito) v += "ammonito"
    if (espulso) v += "ESPULSO"
    if (infortunato) v += "uscito per infortunio"
    if (e != null && conEsito) {
        if (e.moraleDelta != 0) v += "morale ${if (e.moraleDelta > 0) "+" else ""}${e.moraleDelta}"
        if (e.infortunioTurni > 0) v += "INFORTUNIO ${e.infortunioTurni} turni"
        v += "condizione ${e.condizione}%"
    }
    return v.joinToString(" · ")
}
