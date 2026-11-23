package it.androcalcio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.engine.EventType
import it.androcalcio.engine.MatchReport
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.theme.AC

/** Analisi post-partita: poche frasi sui fatti della partita e la tabella delle statistiche avanzate. */
@Composable
fun AnalisiPartita(rep: MatchReport, userHome: Boolean) {
    fun n(home: Boolean, t: EventType) = rep.events.count { it.home == home && it.type == t }
    fun tiriPorta(home: Boolean) = n(home, EventType.GOAL) + n(home, EventType.SAVE)
    fun pct(gol: Int, tiri: Int) = if (tiri == 0) "—" else "${gol * 100 / tiri}%"

    val possCasa = rep.homePossession
    val possOsp = 100 - rep.homePossession
    val golMiei = if (userHome) rep.homeGoals else rep.awayGoals
    val golAvv = if (userHome) rep.awayGoals else rep.homeGoals
    val tiriMiei = if (userHome) rep.homeShots else rep.awayShots
    val possMia = if (userHome) possCasa else possOsp
    val fuorigiocoMiei = n(userHome, EventType.OFFSIDE)
    val espulsiMiei = n(userHome, EventType.RED)

    val analisi = buildList {
        if (golMiei > golAvv && possMia < 45) add("Hai vinto con meno palla: le ripartenze hanno funzionato.")
        if (golMiei < golAvv && possMia > 55) add("Hai avuto più palla ma non l'hai trasformata in gol: serve più movimento in area.")
        if (tiriMiei >= 5 && golMiei * 100 / tiriMiei < 10) add("Poche conclusioni vincenti: $golMiei gol su $tiriMiei tiri.")
        if (fuorigiocoMiei >= 3) add("Troppi fuorigioco ($fuorigiocoMiei): valuta una linea difensiva meno alta.")
        if (espulsiMiei > 0) add("Hai giocato in inferiorità numerica.")
        if (isEmpty()) add("Partita equilibrata: il risultato dipende soprattutto da tiri e parate.")
    }

    val righe = listOf(
        Triple("Tiri", rep.homeShots.toString(), rep.awayShots.toString()),
        Triple("Tiri in porta", tiriPorta(true).toString(), tiriPorta(false).toString()),
        Triple("Gol", rep.homeGoals.toString(), rep.awayGoals.toString()),
        Triple("Efficacia", pct(rep.homeGoals, rep.homeShots), pct(rep.awayGoals, rep.awayShots)),
        Triple("Possesso", "$possCasa%", "$possOsp%"),
        Triple("Fuorigioco", n(true, EventType.OFFSIDE).toString(), n(false, EventType.OFFSIDE).toString()),
        Triple("Ammonizioni", n(true, EventType.YELLOW).toString(), n(false, EventType.YELLOW).toString()),
        Triple("Espulsioni", n(true, EventType.RED).toString(), n(false, EventType.RED).toString()),
        Triple("Infortuni", n(true, EventType.INJURY).toString(), n(false, EventType.INJURY).toString()),
        Triple("Cambi", n(true, EventType.SUB).toString(), n(false, EventType.SUB).toString())
    )

    PaperPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("COSA DICE LA PARTITA", color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            analisi.forEach { Text("• $it", color = AC.PaperText, fontSize = 12.sp) }
            Text("STATISTICHE AVANZATE", color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Row {
                Text("", Modifier.weight(1f))
                Text("Casa", Modifier.width(64.dp), textAlign = TextAlign.Center, color = AC.PaperText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                Text("Ospiti", Modifier.width(64.dp), textAlign = TextAlign.Center, color = AC.PaperText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
            righe.forEach { (voce, casa, ospiti) ->
                Row {
                    Text(voce, Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp)
                    Text(casa, Modifier.width(64.dp), textAlign = TextAlign.Center, color = AC.PaperText, fontSize = 12.sp)
                    Text(ospiti, Modifier.width(64.dp), textAlign = TextAlign.Center, color = AC.PaperText, fontSize = 12.sp)
                }
            }
        }
    }
}
