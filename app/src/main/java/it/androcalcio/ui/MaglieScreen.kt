package it.androcalcio.ui

import it.androcalcio.ui.components.Stelle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel.MaglieStato
import it.androcalcio.engine.Maglie
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

/** Maglie: divisa della stagione, tre proposte del marchio tecnico, prezzo e stima delle vendite. */
@Composable
fun MaglieScreen(st: MaglieStato, onTieni: () -> Unit, onScegli: (Int) -> Unit, onPrezzo: (Int) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Sponsor tecnico: ${st.marchio.nome}", color = AC.Accent, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text(st.marchio.motto + " · stili: " + st.marchio.stili.joinToString(", ") { it.nome.lowercase() },
                    color = AC.TextDim, fontSize = 12.sp)
            }
        }

        st.divisa?.let { d ->
            SectionTitle("Maglie della stagione (${d.marchio})")
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TreMaglie(d, 74)
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("Bellezza: ", color = AC.Text, fontSize = 13.sp)
                        Stelle(Maglie.mezzeStelle(Maglie.bellezza(d)), 13.sp)
                        Text(" (${Maglie.bellezza(d)}/100)" + if (st.novita) " · nuove quest'anno: +20% vendite" else "",
                            color = AC.Text, fontSize = 13.sp)
                    }
                }
            }
        }

        // Scelta della stagione
        when {
            st.obbligo -> Text("Nuovo sponsor tecnico: devi scegliere maglie ${st.marchio.nome} tra le tre proposte.",
                color = AC.Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            st.puoiCambiare -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nuova stagione: tieni le maglie o scegline di nuove.", color = AC.Text, fontSize = 13.sp, modifier = Modifier.weight(1f))
                PcButton("Tieni", onTieni, compatto = true)
            }
            else -> Text("Maglie già scelte per questa stagione. Si cambiano dalla prossima.", color = AC.TextDim, fontSize = 12.sp)
        }
        if (st.puoiCambiare) {
            SectionTitle("Proposte ${st.marchio.nome}")
            st.proposte.forEachIndexed { i, p ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Proposta ${i + 1} · casa ${p.casa.stile.nome.lowercase()}, ${p.casa.colletto.nome.lowercase()}",
                            color = AC.Text, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        TreMaglie(p, 56)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Bellezza ${Maglie.bellezza(p)}/100", color = AC.TextDim, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            PcButton("Scegli", { onScegli(i) }, compatto = true, main = true)
                        }
                    }
                }
            }
        }

        // Merchandising
        SectionTitle("Vendita delle maglie")
        val v = st.vendite
        var prezzo by remember(st.prezzo, st.prezzoStandard) { mutableStateOf((if (st.prezzo > 0) st.prezzo else st.prezzoStandard).toFloat()) }
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Prezzo di una maglia", color = AC.Text, fontWeight = FontWeight.Bold)
                    Text("${prezzo.toInt()} € (standard ${st.prezzoStandard} €)", color = AC.Accent, fontSize = 13.sp)
                }
                Slider(value = prezzo, onValueChange = { prezzo = (Math.round(it / 5f) * 5).toFloat() },
                    valueRange = Maglie.PREZZO_MIN.toFloat()..Maglie.PREZZO_MAX.toFloat(),
                    onValueChangeFinished = { onPrezzo(prezzo.toInt()) })
                if (v != null) {
                    Text("Stima a metà classifica: ${"%,d".format(v.maglie).replace(',', '.')} maglie, guadagno ${money(v.ricavo)} a stagione.",
                        color = AC.Text, fontSize = 13.sp)
                    val f = v.fattori
                    Text("Tifosi ${pct(f.tifosi)} · risultati ${pct(f.risultati)} · campioni ${pct(f.famosi)} · novità ${pct(f.novita)} · " +
                        "bellezza ${pct(f.bellezza)} · negozi e sito ${pct(f.negozi)} · prezzo ${pct(f.prezzo)}",
                        color = AC.TextDim, fontSize = 11.sp)
                }
                Text(if (st.divisione == 0) "In massima serie i tifosi accettano prezzi alti." else
                    "Nelle serie minori un prezzo alto fa crollare le vendite.", color = AC.TextDim, fontSize = 11.sp)
                Text("Il guadagno arriva a fine stagione. Sotto il costo di produzione (${(st.prezzoStandard * 0.45).toInt()} €) si perde.",
                    color = AC.TextDim, fontSize = 11.sp)
            }
        }
    }
}

private fun pct(x: Double): String { val p = ((x - 1.0) * 100).toInt(); return if (p >= 0) "+$p%" else "$p%" }

@Composable
private fun TreMaglie(d: Maglie.Divisa, lato: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf("Casa" to d.casa, "Trasferta" to d.trasferta, "Terza" to d.terza).forEach { (n, k) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MagliaKit(k, Modifier.size(lato.dp, (lato * 0.9f).dp))
                Text(n, color = AC.TextDim, fontSize = 11.sp)
            }
        }
    }
}

/** Disegna una maglia: corpo, motivo dello stile, colletto e bordino sulle maniche. */
@Composable
fun MagliaKit(k: Maglie.Kit, modifier: Modifier = Modifier) {
    val c1 = Color(k.c1); val c2 = Color(k.c2); val c3 = Color(k.c3)
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val corpo = Path().apply {
            moveTo(w * 0.30f, 0f); lineTo(w * 0.70f, 0f); lineTo(w, h * 0.22f); lineTo(w * 0.86f, h * 0.42f)
            lineTo(w * 0.77f, h * 0.35f); lineTo(w * 0.77f, h); lineTo(w * 0.23f, h); lineTo(w * 0.23f, h * 0.35f)
            lineTo(w * 0.14f, h * 0.42f); lineTo(0f, h * 0.22f); close()
        }
        clipPath(corpo) {
            drawRect(c1)
            when (k.stile) {
                Maglie.Stile.TINTA -> {}
                Maglie.Stile.STRISCE -> { var x = w * 0.05f; while (x < w) { drawRect(c2, Offset(x, 0f), Size(w * 0.075f, h)); x += w * 0.15f } }
                Maglie.Stile.STRISCE_LARGHE -> { drawRect(c2, Offset(w * 0.23f, 0f), Size(w * 0.18f, h)); drawRect(c2, Offset(w * 0.59f, 0f), Size(w * 0.18f, h)) }
                Maglie.Stile.CERCHI -> { var y = h * 0.1f; while (y < h) { drawRect(c2, Offset(0f, y), Size(w, h * 0.1f)); y += h * 0.2f } }
                Maglie.Stile.META -> drawRect(c2, Offset(w * 0.5f, 0f), Size(w * 0.5f, h))
                Maglie.Stile.BANDA -> drawPath(Path().apply {
                    moveTo(w * 0.15f, 0f); lineTo(w * 0.38f, 0f); lineTo(w * 0.9f, h); lineTo(w * 0.67f, h); close() }, c2)
                Maglie.Stile.SPALLE -> { drawRect(c2, Offset(0f, 0f), Size(w, h * 0.2f)) }
                Maglie.Stile.FASCIA -> drawRect(c2, Offset(0f, h * 0.36f), Size(w, h * 0.16f))
                Maglie.Stile.GESSATO -> { var x = w * 0.08f; while (x < w) { drawRect(c2, Offset(x, 0f), Size(w * 0.012f, h)); x += w * 0.07f } }
                Maglie.Stile.QUARTI -> { drawRect(c2, Offset(w * 0.5f, 0f), Size(w * 0.5f, h * 0.5f)); drawRect(c2, Offset(0f, h * 0.5f), Size(w * 0.5f, h * 0.5f)) }
                Maglie.Stile.SFUMATO -> drawRect(Brush.verticalGradient(listOf(c1, c2)))
                Maglie.Stile.CHEVRON -> drawPath(Path().apply {
                    moveTo(0f, h * 0.25f); lineTo(w * 0.5f, h * 0.48f); lineTo(w, h * 0.25f); lineTo(w, h * 0.38f)
                    lineTo(w * 0.5f, h * 0.61f); lineTo(0f, h * 0.38f); close() }, c2)
                Maglie.Stile.CROCE -> { drawRect(c2, Offset(w * 0.44f, 0f), Size(w * 0.12f, h)); drawRect(c2, Offset(0f, h * 0.32f), Size(w, h * 0.12f)) }
                Maglie.Stile.MANICHE -> {
                    drawRect(c2, Offset(0f, 0f), Size(w * 0.23f, h)); drawRect(c2, Offset(w * 0.77f, 0f), Size(w * 0.23f, h))
                }
            }
            if (k.bordino) {
                drawLine(c3, Offset(0f, h * 0.22f), Offset(w * 0.14f, h * 0.42f), strokeWidth = w * 0.04f)
                drawLine(c3, Offset(w, h * 0.22f), Offset(w * 0.86f, h * 0.42f), strokeWidth = w * 0.04f)
            }
        }
        // Colletto
        when (k.colletto) {
            Maglie.Colletto.GIRO -> drawArc(c3, 0f, 180f, false, Offset(w * 0.38f, -h * 0.08f), Size(w * 0.24f, h * 0.16f), style = Stroke(w * 0.035f))
            Maglie.Colletto.V -> drawPath(Path().apply { moveTo(w * 0.38f, 0f); lineTo(w * 0.5f, h * 0.15f); lineTo(w * 0.62f, 0f) },
                c3, style = Stroke(w * 0.035f))
            Maglie.Colletto.POLO -> {
                drawPath(Path().apply { moveTo(w * 0.34f, 0f); lineTo(w * 0.5f, h * 0.1f); lineTo(w * 0.42f, h * 0.16f); close() }, c3)
                drawPath(Path().apply { moveTo(w * 0.66f, 0f); lineTo(w * 0.5f, h * 0.1f); lineTo(w * 0.58f, h * 0.16f); close() }, c3)
            }
        }
        drawPath(corpo, Color(0x66000000), style = Stroke(width = 1.5f))
    }
}
