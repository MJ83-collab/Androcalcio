package it.androcalcio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import it.androcalcio.ui.components.TipoTrofeo
import it.androcalcio.ui.components.disegnaTrofeo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import it.androcalcio.ui.components.Crest

/** Colori dei quattro blocchi, come nel menu principale dei manageriali anni '90. */
private val ColCampionato = Color(0xFF9C5A7E)
private val ColAllenatore = Color(0xFF4F6E9E)
private val ColMercato = Color(0xFF4E8A55)
private val ColFinanze = Color(0xFF9A7A55)
private val Fondo = Brush.verticalGradient(listOf(Color(0xFF5C7FB8), Color(0xFF3E5E96), Color(0xFF2B4472)))

/**
 * Home in stile PC Calcio 7, adattata al telefono: intestazione con la tua squadra contro la prossima avversaria,
 * quattro blocchi (Campionato, Allenatore, Mercato, Finanze) con pulsanti a nastro, trofeo con la prossima
 * competizione, le altre voci in fondo e la barra Salva / Notizie / Continua.
 */
@Composable
fun HomePc(team: Team, season: String, prossima: MatchResult?, avversario: Team?, go: (Screen) -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Fondo).verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Intestazione(team, season, prossima, avversario)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Blocco("CAMPIONATO", ColCampionato, Modifier.weight(1f), listOf(
                Triple("Risultati", Screen.GIORNALE, Screen.GIORNALE.icon),
                Triple("Classifica", Screen.CALENDARIO, Screen.STATISTICHE.icon),
                Triple("Calendario", Screen.CALENDARIO, Screen.CALENDARIO.icon)), go)
            Blocco("ALLENATORE", ColAllenatore, Modifier.weight(1f), listOf(
                Triple("Formazione", Screen.TATTICA, Screen.TATTICA.icon),
                Triple("Tattiche", Screen.TATTICA, Screen.TATTICA.icon),
                Triple("Avversario", Screen.AVVERSARIO, Screen.AVVERSARIO.icon)), go)
        }
        Trofeo(prossima, team)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Blocco("MERCATO", ColMercato, Modifier.weight(1f), listOf(
                Triple("Acquisti", Screen.MERCATO, Screen.MERCATO.icon),
                Triple("Collaboratori", Screen.STAFF, Screen.STAFF.icon),
                Triple("Rosa", Screen.ROSA, Screen.ROSA.icon)), go)
            Blocco("FINANZE", ColFinanze, Modifier.weight(1f), listOf(
                Triple("Cassa", Screen.FINANZE, Screen.FINANZE.icon),
                Triple("Decisioni", Screen.DIRIGENZA, Screen.DIRIGENZA.icon),
                Triple("Stadio", Screen.STADIO, Screen.STRUTTURE.icon)), go)
        }
        // Altre voci: piccole targhe in due colonne
        val altre = listOf(Screen.VIVAIO, Screen.PRESTITI, Screen.COMMERCIALE, Screen.MAGLIE, Screen.RELAZIONI, Screen.OBIETTIVI,
            Screen.SOCIETA, Screen.NAZIONALI, Screen.SQUADRE, Screen.ALBO, Screen.STATISTICHE, Screen.EDITOR, Screen.OPZIONI)
        altre.chunked(2).forEach { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                r.forEach { s -> Targa(s.label, s.tint, Modifier.weight(1f)) { go(s) } }
            }
        }
        BarraInferiore(go)
    }
}

@Composable
private fun Intestazione(team: Team, season: String, prossima: MatchResult?, avversario: Team?) {
    val home = prossima == null || avversario == null || prossima.homeTeamId == team.id
    val (a, b) = if (avversario == null) team to null else if (home) team to avversario else avversario to team
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF8FB0E0), Color(0xFF4A6BA8))))
            .border(2.dp, Color(0xFF1C2D52), RoundedCornerShape(4.dp)).padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Lato(a, Modifier.weight(1f), true)
        Column(
            Modifier.width(78.dp).clip(RoundedCornerShape(3.dp)).background(Color.White).border(1.dp, Color(0xFF1C2D52))
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(season, color = Color(0xFF111111), fontWeight = FontWeight.Black, fontSize = 11.sp)
            Text(if (prossima == null) "—" else if (tipoGara(prossima) == TipoGara.AMICHEVOLE) "Amichevole"
                else if (it.androcalcio.engine.Cup.isCup(prossima.round)) "COPPA" else "Giornata",
                color = Color(0xFFC62828), fontWeight = FontWeight.Black, fontSize = 11.sp)
            if (prossima != null && tipoGara(prossima) == TipoGara.CAMPIONATO)
                Text("${prossima.round}", color = Color(0xFF111111), fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        if (b != null) Lato(b, Modifier.weight(1f), false) else Box(Modifier.weight(1f))
    }
}

@Composable
private fun Lato(t: Team, modifier: Modifier, sinistra: Boolean) {
    Row(modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (sinistra) Arrangement.Start else Arrangement.End) {
        if (sinistra) Crest(t, 40.dp)
        Text(t.name, Modifier.weight(1f).padding(horizontal = 4.dp)
            .background(Color(0xFF0E1A33)).padding(horizontal = 4.dp, vertical = 2.dp),
            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1,
            textAlign = if (sinistra) TextAlign.Start else TextAlign.End)
        if (!sinistra) Crest(t, 40.dp)
    }
}

/** Blocco con l'etichetta verticale sul lato e tre pulsanti a nastro. */
@Composable
private fun Blocco(titolo: String, colore: Color, modifier: Modifier,
                   voci: List<Triple<String, Screen, androidx.compose.ui.graphics.vector.ImageVector>>, go: (Screen) -> Unit) {
    Row(modifier.clip(RoundedCornerShape(4.dp)).background(Color(0x33000000)).border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(4.dp))) {
        Box(Modifier.width(18.dp).height(150.dp).background(Color(0xFF0E1A33)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                titolo.forEach { c -> Text(c.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 9.sp, lineHeight = 10.sp) }
            }
        }
        Column(Modifier.weight(1f).padding(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            voci.forEach { (n, s, icona) -> Nastro(n, colore, icona) { go(s) } }
        }
    }
}

/** Pulsante a nastro inclinato con riflesso e icona a sinistra. */
@Composable
private fun Nastro(testo: String, colore: Color, icona: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(40.dp).clickable(onClick = onClick), contentAlignment = Alignment.CenterStart) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height; val sl = h * 0.35f
            val p = Path().apply { moveTo(sl, 0f); lineTo(w, 0f); lineTo(w - sl, h); lineTo(0f, h); close() }
            drawPath(p, Brush.verticalGradient(listOf(colore.copy(alpha = 1f).let { Color(
                (it.red + 0.25f).coerceAtMost(1f), (it.green + 0.25f).coerceAtMost(1f), (it.blue + 0.25f).coerceAtMost(1f)) }, colore,
                Color(colore.red * 0.6f, colore.green * 0.6f, colore.blue * 0.6f))))
            drawPath(p, Color(0xFF0E1A33), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
            drawLine(Color.White.copy(alpha = 0.5f), Offset(sl + 4f, 3f), Offset(w - 6f, 3f), strokeWidth = 2f)
        }
        Row(Modifier.padding(start = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF3EBD8)).border(1.dp, Color(0xFF0E1A33), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center) { Icon(icona, null, tint = colore, modifier = Modifier.size(20.dp)) }
            Text(testo.uppercase(), Modifier.weight(1f).padding(start = 6.dp), color = Color.White, fontWeight = FontWeight.Black,
                fontSize = 12.sp, maxLines = 1, style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black, Offset(1.5f, 1.5f), 1f)))
        }
    }
}

/** Coppa d'argento al centro con la prossima competizione. */
private enum class TipoGara { CAMPIONATO, COPPA, AMICHEVOLE }

private fun tipoGara(m: MatchResult): TipoGara = when {
    m.league.startsWith("AMICHEVOLE|") -> TipoGara.AMICHEVOLE
    it.androcalcio.engine.Cup.isCup(m.round) -> TipoGara.COPPA
    else -> TipoGara.CAMPIONATO
}

/** Centro della Home: coppa (gare di coppa), scudetto della nazione (campionato), stretta di mano (amichevole). */
@Composable
private fun Trofeo(prossima: MatchResult?, team: Team) {
    val tipo = prossima?.let { tipoGara(it) }
    Row(Modifier.fillMaxWidth().height(70.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(when {
            tipo == TipoGara.AMICHEVOLE -> "Precampionato"
            tipo == TipoGara.COPPA && prossima!!.league.startsWith("SUPERCOPPA") -> "Supercoppa"
            tipo == TipoGara.COPPA && prossima!!.league.startsWith("EUROPA|") ->
                it.androcalcio.engine.CoppeEuropee.Coppa.values().firstOrNull { c -> "EUROPA|${c.sigla}" == prossima.league }?.nome ?: "Coppa europea"
            tipo == TipoGara.COPPA -> "Coppa nazionale"
            else -> team.campionato
        }, Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp, textAlign = TextAlign.End)
        Canvas(Modifier.width(70.dp).fillMaxHeight()) {
            when {
                tipo == TipoGara.COPPA && prossima!!.league.startsWith("SUPERCOPPA") -> disegnaTrofeo(TipoTrofeo.SUPERCOPPA)
                tipo == TipoGara.COPPA && prossima!!.league.startsWith("EUROPA|") ->
                    disegnaTrofeo(when (prossima.league) {
                        "EUROPA|CAMPIONI" -> TipoTrofeo.CAMPIONI
                        "EUROPA|EUROPA" -> TipoTrofeo.EUROPA
                        else -> TipoTrofeo.COPPE
                    })
                tipo == TipoGara.COPPA -> disegnaTrofeo(TipoTrofeo.COPPA_NAZIONALE)
                tipo == TipoGara.AMICHEVOLE -> disegnaTrofeo(TipoTrofeo.AMICHEVOLE, manica = Color(team.colorPrimary))
                else -> disegnaTrofeo(TipoTrofeo.SCUDETTO, team.nazione)
            }
        }
        Text(when {
            prossima == null -> "Fine stagione"
            tipo == TipoGara.AMICHEVOLE -> "Amichevole"
            tipo == TipoGara.COPPA -> "Turno di coppa"
            else -> "Giornata ${prossima.round}"
        }, Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}

@Composable
private fun Targa(testo: String, colore: Color, modifier: Modifier, onClick: () -> Unit) {
    Text(testo.uppercase(), modifier.clip(RoundedCornerShape(3.dp))
        .background(Brush.verticalGradient(listOf(colore, Color(colore.red * 0.55f, colore.green * 0.55f, colore.blue * 0.55f))))
        .border(1.dp, Color(0xFF0E1A33), RoundedCornerShape(3.dp)).clickable(onClick = onClick).padding(vertical = 8.dp),
        color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 1)
}

@Composable
private fun BarraInferiore(go: (Screen) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color(0xFF0E1A33)).padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("Salvare" to Screen.SALVATAGGI, "Notizie" to Screen.GIORNALE, "Continua" to Screen.PARTITA).forEach { (t, s) ->
            Row(Modifier.clickable { go(s) }.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(s.icon, null, tint = if (s == Screen.PARTITA) Color(0xFF5BD16B) else Color(0xFF9FC1FF), modifier = Modifier.size(20.dp))
                Text(t.uppercase(), Modifier.padding(start = 4.dp), color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}
