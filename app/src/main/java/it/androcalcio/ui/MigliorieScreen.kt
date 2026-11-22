package it.androcalcio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.data.Team
import it.androcalcio.engine.EffettiMiglioria
import it.androcalcio.engine.Miglioria
import it.androcalcio.engine.Migliorie
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.PaperRow
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC
import it.androcalcio.ui.theme.PcTitle

/** Stadio e centro sportivo: disegno che cresce con i lavori, cantieri in corso e catalogo delle migliorie. */
@Composable
fun MigliorieScreen(team: Team?, st: AndrocalcioViewModel.MigliorieStato, onAvvia: (String) -> Unit) {
    if (team == null) return
    var area by rememberSaveable { mutableStateOf(Migliorie.STADIO) }
    val tot = Migliorie.effetti(st.fatte)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { StadioDisegno(st.fatte, st.cantieri.keys, team) }
        item { Riepilogo(team, tot, st.fatte.size) }
        if (st.cantieri.isNotEmpty()) {
            item { SectionTitle("Cantieri aperti (${st.cantieri.size}/${Migliorie.MAX_CANTIERI})") }
            items(st.cantieri.entries.toList()) { (id, gg) ->
                val m = Migliorie.trova(id) ?: return@items
                Cantiere(m, gg)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(Migliorie.STADIO, Migliorie.CENTRO).forEach { a ->
                    PcChip(selected = area == a, onClick = { area = a }, label = { Text(a) })
                }
            }
        }
        items(Migliorie.TUTTE.filter { it.area == area }) { m ->
            val motivo = Migliorie.avviabile(m, st.fatte, st.cantieri, team.budget)
            val ritorno = Migliorie.ritornoAnnuo(m, st.incassoStagione, st.incassoPerPosto)
            SchedaMiglioria(m, motivo, m.id in st.fatte, ritorno) { onAvvia(m.id) }
        }
    }
}

/** Stadio visto dall'alto: le tribune compaiono e si colorano man mano che le costruisci; i cantieri lampeggiano in giallo. */
@Composable
private fun StadioDisegno(fatte: Set<String>, inCorso: Set<String>, team: Team) {
    val club = Color(team.colorPrimary)
    fun colore(id: String) = when (id) { in fatte -> club; in inCorso -> AC.GoalBanner; else -> Color.White.copy(alpha = 0.10f) }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Brush.verticalGradient(listOf(AC.NightHi, AC.Night)))
        .border(1.5.dp, AC.Neon.copy(alpha = 0.5f), RoundedCornerShape(10.dp)).padding(10.dp)) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(1.9f)) {
            val w = size.width; val h = size.height
            val cr = CornerRadius(h * 0.25f)
            // anello superiore, tribune e curve
            drawRoundRect(colore("anello"), Offset(0f, 0f), Size(w, h), cr)
            drawRoundRect(AC.Night, Offset(w * 0.04f, h * 0.07f), Size(w * 0.92f, h * 0.86f), cr)
            drawRect(colore("tribuna"), Offset(w * 0.2f, h * 0.08f), Size(w * 0.6f, h * 0.14f))
            drawRect(colore("famiglie"), Offset(w * 0.2f, h * 0.78f), Size(w * 0.6f, h * 0.14f))
            drawRoundRect(colore("curva_n"), Offset(w * 0.05f, h * 0.12f), Size(w * 0.13f, h * 0.76f), CornerRadius(h * 0.2f))
            drawRoundRect(colore("curva_s"), Offset(w * 0.82f, h * 0.12f), Size(w * 0.13f, h * 0.76f), CornerRadius(h * 0.2f))
            if ("copertura" in fatte) drawRoundRect(Color.White.copy(alpha = 0.18f), Offset(w * 0.04f, h * 0.07f), Size(w * 0.92f, h * 0.86f), cr)
            // campo
            val campo = if ("ibrido" in fatte) AC.PitchA else AC.PitchB
            drawRect(campo, Offset(w * 0.21f, h * 0.25f), Size(w * 0.58f, h * 0.5f))
            drawRect(Color.White.copy(alpha = 0.7f), Offset(w * 0.21f, h * 0.25f), Size(w * 0.58f, h * 0.5f), style = Stroke(2f))
            drawLine(Color.White.copy(alpha = 0.7f), Offset(w * 0.5f, h * 0.25f), Offset(w * 0.5f, h * 0.75f), 2f)
            drawCircle(Color.White.copy(alpha = 0.7f), h * 0.08f, Offset(w * 0.5f, h * 0.5f), style = Stroke(2f))
            // maxischermo, luci, skybox
            drawRect(colore("maxischermo"), Offset(w * 0.42f, h * 0.0f), Size(w * 0.16f, h * 0.05f))
            listOf(Offset(w * 0.03f, h * 0.05f), Offset(w * 0.97f, h * 0.05f), Offset(w * 0.03f, h * 0.95f), Offset(w * 0.97f, h * 0.95f)).forEach {
                drawCircle(if ("led" in fatte) Color(0xFFFFF59D) else Color.White.copy(alpha = 0.15f), h * 0.035f, it)
            }
            drawRect(colore("skybox"), Offset(w * 0.35f, h * 0.22f), Size(w * 0.3f, h * 0.025f))
        }
    }
}

@Composable
private fun Riepilogo(team: Team, e: EffettiMiglioria, n: Int) {
    val righe = listOf(
        "Posti" to "${team.stadiumCapacity}",
        "Migliorie costruite" to "$n / ${Migliorie.TUTTE.size}",
        "Incassi extra" to "+${e.incassoPct}%",
        "Store e servizi" to money(e.merchAnno) + " l'anno",
        "Crescita giocatori" to "+${e.crescitaPct}%",
        "Infortuni evitati" to "${e.infortuniPct}%",
        "Manutenzione" to money(e.manutenzione) + " l'anno"
    )
    PaperPanel(Modifier.fillMaxWidth()) {
        righe.forEachIndexed { i, (k, v) ->
            PaperRow(i) {
                Text(k, Modifier.weight(1f), color = AC.PaperText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(v, color = AC.ColBlue, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun Cantiere(m: Miglioria, rimaste: Int) {
    val fatto = 1f - rimaste.toFloat() / m.giornate
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(AC.NightHi).padding(8.dp)) {
        Text("${m.icona} ${m.nome} · mancano $rimaste giornate", color = AC.GoalBanner, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        Box(Modifier.fillMaxWidth().height(8.dp).padding(top = 2.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.15f))) {
            Box(Modifier.fillMaxWidth(fatto.coerceIn(0.02f, 1f)).height(8.dp).background(AC.GoalBanner))
        }
    }
}

@Composable
private fun SchedaMiglioria(m: Miglioria, motivo: String?, fatta: Boolean, ritorno: Long, onAvvia: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape)
            .background(Brush.verticalGradient(listOf(AC.NavyTop, AC.NavyBottom)))
            .border(1.5.dp, if (fatta) AC.Ok else AC.NavyBorder, shape).padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(m.icona, fontSize = 28.sp, modifier = Modifier.width(44.dp))
            Column(Modifier.weight(1f)) {
                Text(m.nome, color = Color.White, fontFamily = PcTitle, fontSize = 15.sp)
                Text("${money(m.costo)} · ${m.giornate} giornate di lavori", color = AC.NavyText, fontSize = 12.sp)
            }
        }
        Text(effettiTesto(m.effetti), color = AC.Neon, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        val rip = Migliorie.stagioniPerRipagare(m, ritorno)
        Text(
            if (rip != null) "Ritorno stimato ${money(ritorno)} l'anno · si ripaga in ${"%.1f".format(rip)} stagioni"
            else "Investimento sportivo: rende in risultati, non in incassi",
            color = if (rip != null && rip <= 5) AC.Ok else AC.TextDim, fontSize = 11.sp
        )
        if (m.richiede.isNotEmpty()) Text("Richiede: " + m.richiede.joinToString { Migliorie.trova(it)?.nome ?: it },
            color = AC.TextDim, fontSize = 11.sp)
        if (fatta) Text("✔ COSTRUITA", color = AC.Ok, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        else Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            PcButton("Avvia lavori", onAvvia, main = true, enabled = motivo == null)
            if (motivo != null) Text(motivo, color = AC.Warn, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

private fun effettiTesto(e: EffettiMiglioria): String = buildList {
    if (e.posti > 0) add("+${e.posti} posti")
    if (e.incassoPct > 0) add("incassi +${e.incassoPct}%")
    if (e.merchAnno > 0) add("+${money(e.merchAnno)} l'anno")
    if (e.tifosi > 0) add("tifosi +${e.tifosi}")
    if (e.morale > 0) add("morale +${e.morale}")
    if (e.crescitaPct > 0) add("crescita +${e.crescitaPct}%")
    if (e.infortuniPct > 0) add("infortuni −${e.infortuniPct}%")
    if (e.vivaio > 0) add("giovani +${e.vivaio} potenziale")
    if (e.manutenzione < 0) add("risparmio ${money(-e.manutenzione)} l'anno")
}.joinToString(" · ")
