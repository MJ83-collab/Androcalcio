package it.androcalcio.ui

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

/** Scheda del giocatore come "Informazione del giocatore" del gioco originale: dati, parametri, abilità a stelle, contratto. */
@Composable
fun PlayerSheet(p: Player, team: Team?, onClose: () -> Unit, onRenew: (() -> Unit)? = null) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = AC.Paper,
        title = null,
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.fillMaxWidth().background(AC.ColBlue).padding(8.dp)) {
                    Text((if (p.numero > 0) "${p.numero}  " else "") + p.name.uppercase(), color = Color.White,
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Text(Attr.posName(p).uppercase(), fontWeight = FontWeight.ExtraBold, color = AC.PaperText, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag("${p.age} anni", AC.ColGreen); Tag(p.nationality, AC.ColBlue)
                    Tag(Attr.FOOT_NAMES[p.foot] ?: "Destro", AC.ColOrange)
                }
                if (team != null) Text(team.name, fontSize = 12.sp, color = AC.PaperText)
                Text(if (p.injuredRounds > 0) "INFORTUNATO: ${p.injuredRounds} turni" else "NON INFORTUNATO",
                    fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (p.injuredRounds > 0) AC.ColEn else AC.ColGreen)
                Row(Modifier.fillMaxWidth().background(AC.PaperAlt).padding(6.dp)) {
                    Column(Modifier.weight(1f)) {
                        Param("VELOCITÀ", p.ve); Param("RESISTENZA", p.re); Param("AGGRESSIVITÀ", p.ag)
                        Param("QUALITÀ", p.qu); Param("STATO DI FORMA", p.fo); Param("MORALE", p.morale)
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
            }
        },
        confirmButton = { if (onRenew != null) PcAction(onClick = onRenew) { Text("Rinnova") } },
        dismissButton = { PcAction(onClick = onClose) { Text("OK") } }
    )
}

@Composable
private fun Tag(text: String, color: Color) {
    Text(text, Modifier.background(color).padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White,
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
