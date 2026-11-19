package it.androcalcio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.Player
import it.androcalcio.engine.AssistantCoach
import it.androcalcio.engine.DefLine
import it.androcalcio.engine.Formation
import it.androcalcio.engine.Lineup
import it.androcalcio.engine.Pressing
import it.androcalcio.engine.TacticalRoles
import it.androcalcio.ui.components.HeadCell
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.theme.AC

/** Undici ordinato per linee (portiere, difesa, centrocampo, attacco), come le posizioni del modulo. */
fun orderedXI(xi: List<Player>): List<Player> =
    xi.sortedBy { when (it.role) { "P" -> 0; "D" -> 1; "C" -> 2; else -> 3 } }

private fun dem(role: String) = when (role) { "P" -> "POR"; "D" -> "DIF"; "C" -> "MED"; else -> "ATT" }

/** Posizione (x lungo il campo, y in larghezza, 0..1) che il ruolo occupa nell'icona del mini-campo. */
private fun roleSpot(code: String, role: String): Offset = when (code) {
    "POR" -> Offset(0.10f, 0.5f)
    "TD" -> Offset(0.30f, 0.85f)
    "TS" -> Offset(0.30f, 0.15f)
    "DC" -> Offset(0.28f, 0.5f)
    "MED" -> Offset(0.45f, 0.5f)
    "REG" -> Offset(0.50f, 0.5f)
    "INT" -> Offset(0.55f, 0.28f)
    "MP" -> Offset(0.70f, 0.5f)
    "ES" -> Offset(0.70f, 0.14f)
    "CA" -> Offset(0.88f, 0.5f)
    else -> when (role) {
        "P" -> Offset(0.10f, 0.5f); "D" -> Offset(0.28f, 0.5f); "C" -> Offset(0.50f, 0.5f); else -> Offset(0.85f, 0.5f)
    }
}

/** Mini-campo verde con un pallino bianco nella posizione del ruolo (come l'icona originale). */
@Composable
private fun RoleIcon(code: String, role: String) {
    Canvas(Modifier.size(width = 30.dp, height = 18.dp)) {
        drawRect(Color(0xFF2E7D32))
        drawRect(Color.White, style = Stroke(1.2f))
        drawLine(Color.White, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1f)
        val s = roleSpot(code, role)
        drawCircle(Color.White, size.height * 0.2f, Offset(s.x * size.width, s.y * size.height))
        drawCircle(Color.Black, size.height * 0.2f, Offset(s.x * size.width, s.y * size.height), style = Stroke(1f))
    }
}

/** Colore della cella in base al valore: rosso basso, arancio medio, nero alto. */
private fun cellColor(v: Int) = when { v < 50 -> AC.ColEn; v < 70 -> AC.ColOrange; else -> AC.PaperText }

/**
 * Tabella dei titolari come nella schermata Tattica originale: N°, giocatore,
 * EN VE RE AG QU SF MO ME, ruolo con mini-campo e demarcazione (POR/DIF/MED/ATT).
 */
@Composable
fun LineupTable(xi: List<Player>, onRole: ((Player) -> Unit)? = null, onOpen: ((Player) -> Unit)? = null) {
    val heads = listOf("EN", "VE", "RE", "AG", "QU", "SF", "MO", "ME")
    val colors = listOf(AC.ColEn, AC.ColBlue, AC.ColBlue, AC.ColBlue, AC.ColBlue, AC.ColBlue, AC.ColGreen, AC.ColOrange)
    PaperPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.horizontalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                HeadCell("N°", AC.PaperText, Modifier.width(26.dp))
                HeadCell("GIOCATORE", AC.PaperText, Modifier.width(112.dp), TextAlign.Start)
                heads.forEachIndexed { i, h -> HeadCell(h, colors[i], Modifier.width(30.dp)) }
                HeadCell("RUOLO", AC.PaperText, Modifier.width(120.dp))
                HeadCell("DEM", AC.PaperText, Modifier.width(36.dp))
            }
            xi.forEachIndexed { i, p ->
                PaperRow(i, Modifier.clickable(enabled = onOpen != null) { onOpen?.invoke(p) }) {
                    Text(if (p.numero > 0) "${p.numero}" else "${i + 1}", Modifier.width(26.dp),
                        textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = AC.PaperText)
                    Text(p.name + if (p.condition < 60) " ↓" else "", Modifier.width(112.dp), maxLines = 1,
                        fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AC.PaperText)
                    listOf(p.en, p.ve, p.re, p.ag, p.qu, p.fo, p.morale, p.overall).forEachIndexed { k, v ->
                        Text("$v", Modifier.width(30.dp), textAlign = TextAlign.Center, fontSize = 12.sp,
                            fontWeight = if (k == 7) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (k == 0 && p.en < 60) AC.ColEn else cellColor(v))
                    }
                    val code = TacticalRoles.assigned(p)
                    Row(
                        Modifier.width(120.dp).clickable(enabled = onRole != null) { onRole?.invoke(p) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoleIcon(code, p.role)
                        Text(TacticalRoles.label(code).uppercase(), Modifier.padding(start = 4.dp), maxLines = 1,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AC.ColBlue)
                    }
                    Text(dem(p.role), Modifier.width(36.dp), textAlign = TextAlign.Center, fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold, color = AC.PaperText)
                }
            }
        }
    }
}

/** Campo orizzontale con i giocatori numerati e le frecce di avanzamento di centrocampisti e attaccanti. */
@Composable
fun TacticPitch(xi: List<Player>, formation: Formation, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val ordered = orderedXI(xi)
    val coords = formation.coords()
    Canvas(modifier.fillMaxWidth().aspectRatio(1.7f).background(Color(0xFF1B5E20))) {
        // strisce del prato
        val stripe = size.width / 12f
        for (k in 0 until 12 step 2) drawRect(Color(0xFF2E7D32), Offset(k * stripe, 0f), Size(stripe, size.height))
        val line = Color.White.copy(alpha = 0.85f)
        val st = Stroke(2f)
        drawRect(line, Offset(6f, 6f), Size(size.width - 12f, size.height - 12f), style = st)
        drawLine(line, Offset(size.width / 2, 6f), Offset(size.width / 2, size.height - 6f), 2f)
        drawCircle(line, size.height * 0.16f, Offset(size.width / 2, size.height / 2), style = st)
        val boxW = size.width * 0.14f; val boxH = size.height * 0.56f
        drawRect(line, Offset(6f, (size.height - boxH) / 2), Size(boxW, boxH), style = st)
        drawRect(line, Offset(size.width - 6f - boxW, (size.height - boxH) / 2), Size(boxW, boxH), style = st)
        val r = size.height * 0.062f
        ordered.forEachIndexed { i, p ->
            val c = coords.getOrNull(i) ?: return@forEachIndexed
            // la propria metà campo si estende su quasi tutta la larghezza, portiere a sinistra
            val x = size.width * (0.05f + (c.first - 0.05f) / 0.41f * 0.78f)
            val y = size.height * (0.08f + c.second * 0.84f)
            if (p.role == "C" || p.role == "A") arrow(Offset(x + r, y), r, if (p.role == "A") Color(0xFFFFE23D) else Color.White)
            drawCircle(if (p.role == "P") Color(0xFFFFE23D) else Color(0xFF66BB6A), r, Offset(x, y))
            drawCircle(Color.Black, r, Offset(x, y), style = Stroke(1.5f))
            val label = if (p.numero > 0) "${p.numero}" else "${i + 1}"
            val tl = measurer.measure(label, TextStyle(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black))
            drawText(tl, topLeft = Offset(x - tl.size.width / 2f, y - tl.size.height / 2f))
        }
    }
}

private fun DrawScope.arrow(from: Offset, r: Float, color: Color) {
    val path = Path().apply {
        moveTo(from.x + r * 0.2f, from.y - r * 0.55f)
        lineTo(from.x + r * 1.5f, from.y)
        lineTo(from.x + r * 0.2f, from.y + r * 0.55f)
        close()
    }
    drawPath(path, color)
}

/** Schermata Tattica: undici, campo, schemi e parametri come nel gioco originale. */
@Composable
fun TatticaScreen(
    players: List<Player>,
    formation: Formation, onFormation: (Formation) -> Unit,
    onCoach: () -> Unit,
    press: Pressing, line: DefLine, onPress: (Pressing) -> Unit, onLine: (DefLine) -> Unit,
    onRole: (Player, String) -> Unit,
    advice: AssistantCoach.Advice?, onApplyXI: () -> Unit, onApplyTattica: () -> Unit,
    onOpenAvversario: () -> Unit
) {
    val xi = orderedXI(Lineup.resolve(players, formation).xi)
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
            confirmButton = { PcButton("Chiudi", { roleFor = null }, main = true) }
        )
    }
    LazyColumn(contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { LineupTable(xi, onRole = { roleFor = it }) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).border2()) { TacticPitch(xi, formation) }
                Column(Modifier.width(120.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PcButton("Allenatore", onCoach, Modifier.fillMaxWidth())
                    PcButton("Consigliato", onApplyXI, Modifier.fillMaxWidth(), enabled = advice?.assistantName != null)
                    PcButton("Tattica", onApplyTattica, Modifier.fillMaxWidth(), enabled = advice?.tattica != null)
                    PcButton("Ver rivale", onOpenAvversario, Modifier.fillMaxWidth(), main = true)
                }
            }
        }
        item {
            SectionTitle("Tattiche predefinite")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Formation.values().forEach { f -> PcChip(formation == f, { onFormation(f) }, { Text(f.label) }) }
            }
        }
        item {
            SectionTitle("Pressing")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pressing.values().forEach { p -> PcChip(press == p, { onPress(p) }, { Text(p.label) }) }
            }
            SectionTitle("Linea difensiva")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DefLine.values().forEach { l -> PcChip(line == l, { onLine(l) }, { Text(l.label) }) }
            }
        }
    }
}

private fun Modifier.border2(): Modifier =
    this.then(Modifier.background(AC.Btn).padding(2.dp))
