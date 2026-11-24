package it.androcalcio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel.StadioStato
import it.androcalcio.engine.Abbonamenti
import it.androcalcio.engine.Atmosfera
import it.androcalcio.engine.Migliorie
import it.androcalcio.engine.StadioPersonale
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

/**
 * Stadio personale: nome, colore dei seggiolini, settori con prezzi, atmosfera e strutture.
 * Le migliorie si costruiscono dalla schermata "Stadio e centro".
 */
@Composable
fun StadioScreen(
    st: StadioStato,
    onNome: (String) -> Unit,
    onColore: (Int) -> Unit,
    onPrezzo: (String, Int) -> Unit,
    onMigliorie: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val (nomeColore, argb) = StadioPersonale.colore(st.colore)
        val seggiolini = Color(argb)

        // Intestazione con la pianta dello stadio
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(st.nome, color = AC.Accent, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("${"%,d".format(st.capienza).replace(',', '.')} posti · biglietto medio ${st.prezzoBase} €",
                    color = AC.TextDim, fontSize = 12.sp)
                Pianta(st, seggiolini, Modifier.fillMaxWidth().height(170.dp))
            }
        }

        // Nome
        SectionTitle("Nome dello stadio")
        GlassCard(Modifier.fillMaxWidth()) {
            var testo by remember(st.nome) { mutableStateOf(st.nome) }
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(testo, { if (it.length <= StadioPersonale.NOME_MAX) testo = it },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                PcButton("Rinomina", { onNome(testo) }, compatto = true, main = true,
                    enabled = testo.trim() != st.nome && StadioPersonale.nomeValido(testo) != null)
            }
        }

        // Colore dei seggiolini
        SectionTitle("Colore dei seggiolini: $nomeColore")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StadioPersonale.COLORI.indices.chunked(6).forEach { riga ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        riga.forEach { i ->
                            val scelto = i == st.colore
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(Color(StadioPersonale.COLORI[i].second))
                                    .border(if (scelto) 3.dp else 1.dp, if (scelto) AC.Accent else Color.Gray, CircleShape)
                                    .clickable { onColore(i) }
                            )
                        }
                    }
                }
            }
        }

        // Settori e prezzi
        SectionTitle("Settori e prezzi")
        Text("Prezzo in % del biglietto standard. La curva è sensibile al prezzo, il VIP poco. Stime a partita, a metà classifica. Abbonati stimati: ${st.abbonatiPct}% dei posti.",
            color = AC.TextDim, fontSize = 12.sp)
        st.stime.forEach { s ->
            var v by remember(s.prezzoPct) { mutableStateOf(s.prezzoPct.toFloat()) }
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(s.settore.nome, color = AC.Text, fontWeight = FontWeight.Bold)
                        Text("${v.toInt()}% · ${s.prezzoEuro} €", color = AC.Accent, fontSize = 13.sp)
                    }
                    Text("${"%,d".format(s.posti).replace(',', '.')} posti · pieno al ${(s.riempimento * 100).toInt()}% · incasso ${money(s.incassoPartita)} a partita",
                        color = AC.TextDim, fontSize = 11.sp)
                    Slider(value = v, onValueChange = { v = (Math.round(it / 5f) * 5).toFloat() },
                        valueRange = Abbonamenti.PREZZO_MIN.toFloat()..Abbonamenti.PREZZO_MAX.toFloat(),
                        onValueChangeFinished = { onPrezzo(s.settore.id, v.toInt()) })
                }
            }
        }
        Text("Totale stimato a partita: ${money(st.stime.sumOf { it.incassoPartita })}. L'effetto dei prezzi si somma agli incassi a fine stagione.",
            color = AC.TextDim, fontSize = 11.sp)

        // Atmosfera
        SectionTitle("Atmosfera")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${st.atmosfera}/100 · ${Atmosfera.etichetta(st.atmosfera)}", color = AC.Accent,
                    fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text("Strutture: ${segno(st.bonusStrutture)} · Curva: ${segno(st.bonusCurva)}", color = AC.Text, fontSize = 13.sp)
                val eff = ((Atmosfera.fattore(st.atmosfera) - 1.0) * 100).toInt()
                Text("Nelle partite dal vivo in casa: possesso ${segno(eff)}% (da -5% a +5%).", color = AC.TextDim, fontSize = 12.sp)
                Text("Alzano l'atmosfera: copertura, tribuna coperta, illuminazione, maxischermo, anello, curva piena e tifosi contenti.",
                    color = AC.TextDim, fontSize = 11.sp)
            }
        }

        // Strutture
        SectionTitle("Strutture dello stadio")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val stadio = Migliorie.TUTTE.filter { it.area == Migliorie.STADIO }
                val fatte = stadio.filter { it.id in st.fatte }
                val inCorso = stadio.filter { it.id in st.cantieri }
                Text("Costruite: ${fatte.size} su ${stadio.size}", color = AC.Text, fontWeight = FontWeight.Bold)
                if (fatte.isNotEmpty()) Text(fatte.joinToString(" · ") { "${it.icona} ${it.nome}" }, color = AC.TextDim, fontSize = 12.sp)
                inCorso.forEach { Text("🚧 ${it.nome}: ${st.cantieri[it.id]} giornate", color = AC.Text, fontSize = 12.sp) }
                Text("Manutenzione: ${money(st.manutenzioneAnno)} l'anno", color = AC.TextDim, fontSize = 12.sp)
                Text("Negozi del club: ${StadioPersonale.negozi(st.fatte)} · sito web livello ${StadioPersonale.livelloSito(st.fatte)}/3",
                    color = AC.TextDim, fontSize = 12.sp)
                PcButton("Migliorie e cantieri", onMigliorie, compatto = true, main = true)
            }
        }
    }
}

private fun segno(v: Int) = if (v > 0) "+$v" else "$v"

/** Pianta vista dall'alto: campo al centro, quattro settori con il colore dei seggiolini; più pieni, più accesi. */
@Composable
private fun Pianta(st: StadioStato, seggiolini: Color, modifier: Modifier) {
    val pieno = st.stime.associate { it.settore.id to it.riempimento.toFloat() }
    val coperto = "copertura" in st.fatte
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val m = 0.17f
        fun settore(id: String) = seggiolini.copy(alpha = 0.35f + 0.65f * (pieno[id] ?: 0.6f))
        // Tribune: curve ai lati corti, tribuna e distinti ai lati lunghi, VIP al centro della tribuna
        drawRect(settore("curva"), Offset(0f, h * m), Size(w * m, h * (1 - 2 * m)))
        drawRect(settore("curva"), Offset(w * (1 - m), h * m), Size(w * m, h * (1 - 2 * m)))
        drawRect(settore("distinti"), Offset(w * m, 0f), Size(w * (1 - 2 * m), h * m))
        drawRect(settore("tribuna"), Offset(w * m, h * (1 - m)), Size(w * (1 - 2 * m), h * m))
        drawRect(settore("vip"), Offset(w * 0.4f, h * (1 - m)), Size(w * 0.2f, h * m))
        // Campo
        val f0 = Offset(w * m + 6f, h * m + 6f); val fs = Size(w * (1 - 2 * m) - 12f, h * (1 - 2 * m) - 12f)
        drawRect(Color(0xFF2E7D32), f0, fs)
        drawRect(Color.White, f0, fs, style = Stroke(2f))
        drawLine(Color.White, Offset(w / 2, f0.y), Offset(w / 2, f0.y + fs.height), 2f)
        drawCircle(Color.White, fs.height * 0.18f, Offset(w / 2, h / 2), style = Stroke(2f))
        if (coperto) drawRect(Color.White.copy(alpha = 0.6f), Offset(2f, 2f), Size(w - 4f, h - 4f), style = Stroke(3f))
        if ("faretti" in st.fatte || "led" in st.fatte) {
            listOf(Offset(4f, 4f), Offset(w - 4f, 4f), Offset(4f, h - 4f), Offset(w - 4f, h - 4f))
                .forEach { drawCircle(Color(0xFFFFF59D), 6f, it) }
        }
    }
}
