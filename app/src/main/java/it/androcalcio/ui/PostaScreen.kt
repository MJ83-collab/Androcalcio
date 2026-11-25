package it.androcalcio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.engine.CalendarioGiorni
import it.androcalcio.engine.Posta
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.theme.AC
import java.time.LocalDate

/** Casella messaggi: eventi che hanno fermato CONTINUA, dal più recente. Un tocco apre la schermata giusta. */
@Composable
fun PostaScreen(messaggi: List<Posta.Messaggio>, onApri: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        SectionTitle("Messaggi")
        if (messaggi.isEmpty()) {
            Text("Nessun messaggio. Premi CONTINUA: ti fermo quando succede qualcosa.", color = AC.TextDim, fontSize = 12.sp)
        }
        // Prima i non letti, poi per categoria; ogni categoria ha il suo colore (come in FM27)
        val nonLetti = messaggi.filter { !it.letto }.asReversed()
        if (nonLetti.isNotEmpty()) {
            Text("DA LEGGERE (${nonLetti.size})", Modifier.padding(top = 6.dp, bottom = 4.dp), color = AC.Warn, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            nonLetti.forEach { m -> Riga(m, onApri) }
        }
        Posta.Cat.values().forEach { cat ->
            val l = messaggi.filter { it.cat == cat && it.letto }.asReversed()
            if (l.isNotEmpty()) {
                Text(cat.label.uppercase(), Modifier.padding(top = 10.dp, bottom = 4.dp), color = coloreCategoria(cat), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                l.forEach { m -> Riga(m, onApri) }
            }
        }
    }
}

@Composable
private fun Riga(m: Posta.Messaggio, onApri: (String) -> Unit) {
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth().padding(bottom = 6.dp).clickable { onApri(m.vai) }) {
        androidx.compose.foundation.layout.Box(Modifier.width(6.dp).height(52.dp).background(coloreCategoria(m.cat)))
        GlassCard(Modifier.weight(1f)) {
            Column(Modifier.padding(10.dp)) {
                Text(CalendarioGiorni.nomeGiorno(LocalDate.ofEpochDay(m.giorno)) + " · " + m.cat.label, color = AC.TextDim, fontSize = 10.sp)
                Text(m.testo, color = AC.Text, fontWeight = if (m.letto) FontWeight.Normal else FontWeight.Bold)
            }
        }
    }
}
