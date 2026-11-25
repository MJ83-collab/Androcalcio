package it.androcalcio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel.EuropaCoppa
import it.androcalcio.AndrocalcioViewModel.EuropaStato
import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import it.androcalcio.engine.Cup
import it.androcalcio.engine.CoppeEuropee
import it.androcalcio.engine.standings
import it.androcalcio.ui.components.FlagBox
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.TrofeoIcona
import it.androcalcio.ui.components.TipoTrofeo
import it.androcalcio.ui.theme.AC

/** Coppe europee: urna del sorteggio, gironi con maglie e bandiere, tabellone a eliminazione. */
@Composable
fun EuropaScreen(st: EuropaStato, teams: List<Team>, miaId: Long) {
    var scelta by remember(st.mia) { mutableStateOf(st.mia ?: CoppeEuropee.Coppa.CAMPIONI) }
    val byId = remember(teams) { teams.associateBy { it.id } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Coppe europee ${st.stagione}", color = AC.Accent, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(if (st.mia != null) "La tua squadra gioca: ${st.mia.nome}." else "La tua squadra quest'anno non gioca in Europa.",
            color = AC.TextDim, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CoppeEuropee.Coppa.values().forEach { c ->
                PcButton(c.nome.removePrefix("Coppa dei ").removePrefix("Coppa delle ").removePrefix("Coppa ").let {
                    if (c == CoppeEuropee.Coppa.CAMPIONI) "Campioni" else it
                }, { scelta = c }, Modifier.weight(1f), main = c == scelta)
            }
        }
        val coppa = st.coppe.firstOrNull { it.coppa == scelta }
        if (coppa == null || coppa.partite.isEmpty()) {
            Text("Sorteggio non ancora disponibile.", color = AC.Text)
        } else {
            val tipo = when (scelta) {
                CoppeEuropee.Coppa.CAMPIONI -> TipoTrofeo.CAMPIONI
                CoppeEuropee.Coppa.EUROPA -> TipoTrofeo.EUROPA
                CoppeEuropee.Coppa.COPPE -> TipoTrofeo.COPPE
            }
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Urna(Modifier.size(78.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Sorteggio · ${scelta.nome}", color = AC.Text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(if (scelta.conGironi) "${scelta.squadre} squadre, 8 gironi da 4, poi eliminazione diretta."
                            else "${scelta.squadre} squadre, eliminazione diretta andata e ritorno.",
                            color = AC.TextDim, fontSize = 12.sp)
                    }
                    TrofeoIcona(tipo, lato = 52.dp)
                }
            }
            if (scelta.conGironi && coppa.gironi.isNotEmpty()) {
                SectionTitle("Gironi")
                coppa.gironi.forEachIndexed { i, ids -> Girone(i, ids, coppa.partite, byId, miaId) }
            }
            val ko = coppa.partite.filter { !CoppeEuropee.inGironi(it.round) }.groupBy { Cup.stageOf(it.round) }.toSortedMap()
            ko.forEach { (stage, ms) ->
                SectionTitle(CoppeEuropee.nomeTurno(stage))
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ms.sortedBy { it.round }.forEach { m -> Riga(m, byId, miaId) }
                    }
                }
            }
        }
    }
}

/** Urna del sorteggio: una coppa con le palline colorate. */
@Composable
private fun Urna(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val colori = listOf(Color(0xFFE53935), Color(0xFFFFC107), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFFFFFF), Color(0xFF8E24AA))
        val sfera = Offset(w / 2, h * 0.42f)
        drawCircle(Color(0xFF1B2A5A), radius = w * 0.40f, center = sfera)
        listOf(0.30f to 0.35f, 0.55f to 0.30f, 0.42f to 0.52f, 0.62f to 0.50f, 0.28f to 0.55f, 0.48f to 0.38f).forEachIndexed { i, (x, y) ->
            drawCircle(colori[i % colori.size], radius = w * 0.07f, center = Offset(w * x, h * y))
        }
        drawCircle(Color(0x66FFFFFF), radius = w * 0.40f, center = sfera, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
        val piede = Path().apply {
            moveTo(w * 0.36f, h * 0.78f); lineTo(w * 0.64f, h * 0.78f)
            lineTo(w * 0.72f, h * 0.97f); lineTo(w * 0.28f, h * 0.97f); close()
        }
        drawPath(piede, Color(0xFF8C93A8))
        drawRect(Color(0xFFC9A227), Offset(w * 0.42f, h * 0.80f), Size(w * 0.16f, h * 0.06f))
    }
}

/** Maglia stilizzata con i colori del club. */
@Composable
private fun Maglia(t: Team?, modifier: Modifier = Modifier) {
    val c1 = Color(t?.colorPrimary ?: 0xFF888888); val c2 = Color(t?.colorSecondary ?: 0xFFFFFFFF)
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val corpo = Path().apply {
            moveTo(w * 0.28f, 0f); lineTo(w * 0.72f, 0f); lineTo(w, h * 0.28f); lineTo(w * 0.84f, h * 0.45f)
            lineTo(w * 0.76f, h * 0.36f); lineTo(w * 0.76f, h); lineTo(w * 0.24f, h); lineTo(w * 0.24f, h * 0.36f)
            lineTo(w * 0.16f, h * 0.45f); lineTo(0f, h * 0.28f); close()
        }
        drawPath(corpo, c1)
        drawRect(c2, Offset(w * 0.44f, 0f), Size(w * 0.12f, h))
        drawPath(corpo, Color(0x66000000), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))
    }
}

@Composable
private fun Girone(i: Int, ids: List<Long>, partite: List<MatchResult>, byId: Map<Long, Team>, miaId: Long) {
    val ts = ids.mapNotNull { byId[it] }
    val tab = standings(ts, partite.filter { CoppeEuropee.inGironi(it.round) && it.homeTeamId in ids })
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Girone ${CoppeEuropee.lettera(i)}", color = AC.Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            tab.forEachIndexed { pos, r ->
                val mia = r.team.id == miaId
                Row(Modifier.fillMaxWidth().background(if (mia) Color(0x33FFE23D) else Color.Transparent).padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${pos + 1}", color = if (pos < 2) AC.Ok else AC.TextDim, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(14.dp))
                    Maglia(r.team, Modifier.size(width = 18.dp, height = 16.dp))
                    FlagBox(r.team.nazione, Modifier.size(width = 20.dp, height = 13.dp))
                    Text(r.team.name, color = AC.Text, fontSize = 12.sp, fontWeight = if (mia) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1, modifier = Modifier.weight(1f))
                    Text("${r.played}", color = AC.TextDim, fontSize = 12.sp, modifier = Modifier.width(18.dp))
                    Text("${r.gf - r.ga}", color = AC.TextDim, fontSize = 12.sp, modifier = Modifier.width(26.dp))
                    Text("${r.pts}", color = AC.Text, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(22.dp))
                }
            }
            Text("Passano le prime due. Colonne: giocate, differenza reti, punti.", color = AC.TextDim, fontSize = 10.sp)
        }
    }
}

@Composable
private fun Riga(m: MatchResult, byId: Map<Long, Team>, miaId: Long) {
    val h = byId[m.homeTeamId]; val a = byId[m.awayTeamId]
    val mia = m.homeTeamId == miaId || m.awayTeamId == miaId
    val ris = if (!m.played) "–" else "${m.homeGoals}-${m.awayGoals}" + if (m.homePens >= 0) " (${m.homePens}-${m.awayPens} r.)" else ""
    Row(Modifier.fillMaxWidth().background(if (mia) Color(0x33FFE23D) else Color.Transparent).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Maglia(h, Modifier.size(width = 16.dp, height = 14.dp))
        Text(h?.name ?: "?", color = AC.Text, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
        Text(ris, color = AC.Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(a?.name ?: "?", color = AC.Text, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
        Maglia(a, Modifier.size(width = 16.dp, height = 14.dp))
    }
}
