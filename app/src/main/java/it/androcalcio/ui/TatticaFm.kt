package it.androcalcio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import it.androcalcio.data.Player
import it.androcalcio.engine.Ampiezza
import it.androcalcio.engine.Compiti
import it.androcalcio.engine.Contrasti
import it.androcalcio.engine.Creativita
import it.androcalcio.engine.CostruzionePortiere
import it.androcalcio.engine.DefLine
import it.androcalcio.engine.Fasce
import it.androcalcio.engine.Formation
import it.androcalcio.engine.FrequenzaPressing
import it.androcalcio.engine.IstrGiocatore
import it.androcalcio.engine.PostoRuolo
import it.androcalcio.engine.Lineup
import it.androcalcio.engine.Passaggi
import it.androcalcio.engine.PianoTattico
import it.androcalcio.engine.Pressing
import it.androcalcio.engine.Ritmo
import it.androcalcio.engine.RuoliFm
import it.androcalcio.engine.SchemaPiazzati
import it.androcalcio.engine.StileTattico
import it.androcalcio.engine.TacticalRoles
import it.androcalcio.engine.Transizione
import it.androcalcio.ui.components.Stelle

/** Colori della schermata Tattica in stile Football Manager: sempre scura, a prescindere dal tema dell'app. */
private object Fm {
    val Sfondo = Color(0xFF0C161B)
    val Carta = Color(0xFF14242B)
    val CartaAlta = Color(0xFF1C3340)
    val Bordo = Color(0xFF2C4A57)
    val Testo = Color(0xFFE9F1F4)
    val Dim = Color(0xFF8CA3AD)
    val Verde = Color(0xFF38B26B)
    val Viola = Color(0xFF7A3FC4)
    val VerdeCampo = Color(0xFF17623A)
    val VerdeCampo2 = Color(0xFF1A6D40)
    val Portiere = Color(0xFFD9822B)
    val Difesa = Color(0xFF1D8C8A)
    val Centrocampo = Color(0xFF3E9E4F)
    val Attacco = Color(0xFF8E3BB5)
    val Rosso = Color(0xFFE5534B)
    val Oro = Color(0xFFFFC94A)
}

/** Fase della partita mostrata nel campo. */
private enum class FaseVista(val label: String) { COMBINATA("Combinata"), POSSESSO("In possesso"), SENZA("Non in possesso") }

private fun coloreLinea(codice: String): Color = when (codice) {
    "POR" -> Fm.Portiere
    "TD", "TS", "DC", "CS", "CD" -> Fm.Difesa
    "MED", "REG", "INT", "FD", "FS" -> Fm.Centrocampo
    else -> Fm.Attacco
}

private fun <T> List<T>.prossimo(corrente: T): T = this[(indexOf(corrente) + 1).mod(size)]

private fun coloreAttributo(v: Int): Color = when {
    v >= 75 -> Fm.Verde
    v >= 60 -> Color(0xFFB9D96B)
    v >= 45 -> Fm.Oro
    else -> Fm.Rosso
}

private fun mezzeStelle(p: Player, ruolo: String): Int = (RuoliFm.stelle(p, ruolo) * 2).toInt().coerceIn(1, 10)

/**
 * Tattica in stile Football Manager 27: stile in alto, campo con le fasi Combinata / In possesso / Non in possesso,
 * ruolo e compito per fase toccando un giocatore, istruzioni di squadra a due fasi.
 */
@Composable
fun TatticaFmScreen(
    players: List<Player>,
    formation: Formation, onFormation: (Formation) -> Unit,
    piano: PianoTattico, onPiano: (PianoTattico) -> Unit,
    onStile: (StileTattico) -> Unit,
    press: Pressing, onPress: (Pressing) -> Unit,
    line: DefLine, onLine: (DefLine) -> Unit,
    ampiezza: Ampiezza, onAmpiezza: (Ampiezza) -> Unit,
    fasce: Fasce, onFasce: (Fasce) -> Unit,
    schema: SchemaPiazzati, onSchema: (SchemaPiazzati) -> Unit,
    trappola: Boolean, onTrappola: (Boolean) -> Unit,
    onRoleConPalla: (Player, String) -> Unit,
    onRoleSenzaPalla: (Player, String) -> Unit,
    onCompito: (Player, String) -> Unit,
    onIstr: (Player, IstrGiocatore) -> Unit = { _, _ -> }
) {
    val xi = orderedXI(Lineup.resolve(players, formation).xi, formation)
    var tab by remember { mutableStateOf(0) }
    var fase by remember { mutableStateOf(FaseVista.COMBINATA) }
    var istrSenza by remember { mutableStateOf(false) }
    var sceltaStile by remember { mutableStateOf(false) }
    var giocatoreId by remember { mutableStateOf<Long?>(null) }

    if (sceltaStile) DialogStili(xi, piano.stile, onChiudi = { sceltaStile = false }, onUsa = { onStile(it); sceltaStile = false })
    giocatoreId?.let { id ->
        players.firstOrNull { it.id == id }?.let { p ->
            DialogRuolo(
                p, piano, if (fase == FaseVista.SENZA) 1 else 0,
                onChiudi = { giocatoreId = null },
                onRoleConPalla = onRoleConPalla, onRoleSenzaPalla = onRoleSenzaPalla, onCompito = onCompito, onIstr = onIstr
            )
        }
    }

    LazyColumn(
        Modifier.fillMaxWidth().background(Fm.Sfondo),
        contentPadding = PaddingValues(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Testata: stile, modulo, etichette
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Fm.Carta).border(1.dp, Fm.Bordo, RoundedCornerShape(8.dp)).padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("STILE", color = Fm.Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(piano.stile?.nome ?: "Nessuno stile scelto", color = Fm.Testo, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Pulsante("Cambia stile", viola = true) { sceltaStile = true }
                }
                val st = piano.stile
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Etichetta(st?.intensita?.label ?: "Intensità —")
                    Etichetta(st?.complessita?.label ?: "Complessità —")
                    val eff = st?.idoneita(xi.ifEmpty { players.take(11) }) ?: 50
                    Column(Modifier.weight(1f)) {
                        Text("EFFICACIA $eff", color = Fm.Dim, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Fm.Sfondo)) {
                            Box(Modifier.fillMaxWidth(eff / 100f).height(6.dp).background(if (eff >= 60) Fm.Verde else if (eff >= 35) Fm.Oro else Fm.Rosso))
                        }
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Formation.values().forEach { f -> Scheda(f.label, formation == f) { onFormation(f) } }
                }
            }
        }
        // Schede principali
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Scheda("Disposizione in campo", tab == 0, Modifier.weight(1f)) { tab = 0 }
                Scheda("Istruzioni squadra", tab == 1, Modifier.weight(1f)) { tab = 1 }
            }
        }
        if (tab == 0) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FaseVista.values().forEach { f -> Scheda(f.label, fase == f, Modifier.weight(1f)) { fase = f } }
                }
            }
            item { CampoFm(xi, formation, piano, line, fase) { giocatoreId = it.id } }
            item {
                Text(
                    when (fase) {
                        FaseVista.COMBINATA -> "Il ruolo con la palla e, se diverso, quello senza palla. Tocca un giocatore per cambiarli."
                        FaseVista.POSSESSO -> "Ruoli con la palla: posizione e stelle dipendono da quanto il giocatore conosce il ruolo."
                        FaseVista.SENZA -> "Ruoli senza palla: la linea si abbassa con la linea difensiva bassa. Un ruolo più congeniale rende la difesa un po' più solida."
                    },
                    color = Fm.Dim, fontSize = 12.sp
                )
            }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Scheda("In possesso", !istrSenza, Modifier.weight(1f)) { istrSenza = false }
                    Scheda("Non in possesso", istrSenza, Modifier.weight(1f)) { istrSenza = true }
                }
            }
            piano.stile?.let { st ->
                if (st.pregi.isNotEmpty()) item { ValutazioneStaff(st) }
            }
            if (!istrSenza) {
                val st = piano.stile
                item {
                    Griglia(listOf(
                        Istr("Stile dei passaggi", piano.passaggi.label, st != null && piano.passaggi != st.piano.passaggi) {
                            onPiano(piano.copy(passaggi = Passaggi.values().toList().prossimo(piano.passaggi)))
                        },
                        Istr("Ritmo di gioco", piano.ritmo.label, st != null && piano.ritmo != st.ritmo) {
                            onPiano(piano.copy(ritmo = Ritmo.values().toList().prossimo(piano.ritmo)))
                        },
                        Istr("Ampiezza in attacco", ampiezza.label, st != null && ampiezza != st.ampiezza) {
                            onAmpiezza(Ampiezza.values().toList().prossimo(ampiezza))
                        },
                        Istr("Transizione offensiva", piano.transizione.label, st != null && piano.transizione != st.piano.transizione) {
                            onPiano(piano.copy(transizione = Transizione.values().toList().prossimo(piano.transizione)))
                        },
                        Istr("Libertà di inventare", piano.creativita.label, st != null && piano.creativita != st.piano.creativita) {
                            onPiano(piano.copy(creativita = Creativita.values().toList().prossimo(piano.creativita)))
                        },
                        Istr("Costruzione portiere", piano.costruzione.label, st != null && piano.costruzione != st.piano.costruzione) {
                            onPiano(piano.copy(costruzione = CostruzionePortiere.values().toList().prossimo(piano.costruzione)))
                        },
                        Istr("Gioco sulle fasce", fasce.label, st != null && fasce != st.fasce) {
                            onFasce(Fasce.values().toList().prossimo(fasce))
                        },
                        Istr("Calci piazzati", schema.label, st != null && schema != st.schema) {
                            onSchema(SchemaPiazzati.values().toList().prossimo(schema))
                        }
                    ))
                }
                item { Text("Tocca una casella per cambiare il valore. Il punto giallo segna le modifiche rispetto allo stile.", color = Fm.Dim, fontSize = 11.sp) }
            } else {
                val st = piano.stile
                item {
                    Griglia(listOf(
                        Istr("Linea difensiva", line.label, st != null && line != st.linea) {
                            onLine(DefLine.values().toList().prossimo(line))
                        },
                        Istr("Intensità del pressing", press.label, st != null && press != st.pressing) {
                            onPress(Pressing.values().toList().prossimo(press))
                        },
                        Istr("Primo pressing", piano.frequenza.label, st != null && piano.frequenza != st.piano.frequenza) {
                            onPiano(piano.copy(frequenza = FrequenzaPressing.values().toList().prossimo(piano.frequenza)))
                        },
                        Istr("Contrasti", piano.contrasti.label, st != null && piano.contrasti != st.piano.contrasti) {
                            onPiano(piano.copy(contrasti = Contrasti.values().toList().prossimo(piano.contrasti)))
                        },
                        Istr("Contropressing", if (piano.contropressing) "Sì" else "No", st != null && piano.contropressing != st.piano.contropressing) {
                            onPiano(piano.copy(contropressing = !piano.contropressing))
                        },
                        Istr("Portiere avversario", if (piano.bloccaPortiere) "Non costruisce" else "Libero", st != null && piano.bloccaPortiere != st.piano.bloccaPortiere) {
                            onPiano(piano.copy(bloccaPortiere = !piano.bloccaPortiere))
                        },
                        Istr("Trappola fuorigioco", if (trappola) "Sì" else "No", false) { onTrappola(!trappola) }
                    ))
                }
                item {
                    Text(
                        if (trappola && line != DefLine.ALTA) "La trappola del fuorigioco funziona solo con la linea alta."
                        else "Il pressing e la linea si cambiano anche in partita, dal comando Tattica.",
                        color = Fm.Dim, fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------ pezzi piccoli

@Composable
private fun Pulsante(testo: String, viola: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(6.dp)).background(if (viola) Fm.Viola else Fm.CartaAlta)
            .border(1.dp, if (viola) Fm.Viola else Fm.Bordo, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) { Text(testo, color = Fm.Testo, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
}

@Composable
private fun Etichetta(testo: String) {
    Text(
        testo, Modifier.clip(RoundedCornerShape(4.dp)).background(Fm.CartaAlta).border(1.dp, Fm.Bordo, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        color = Fm.Testo, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1
    )
}

@Composable
private fun Scheda(testo: String, scelta: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(6.dp)).background(if (scelta) Fm.Viola else Fm.Carta)
            .border(1.dp, if (scelta) Fm.Viola else Fm.Bordo, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) { Text(testo, color = Fm.Testo, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.Center) }
}

private class Istr(val titolo: String, val valore: String, val modificata: Boolean, val onClick: () -> Unit)

/** Caselle delle istruzioni, due per riga. */
@Composable
private fun Griglia(caselle: List<Istr>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        caselle.chunked(2).forEach { riga ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                riga.forEach { c ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Fm.Carta)
                            .border(1.dp, if (c.modificata) Fm.Oro else Fm.Bordo, RoundedCornerShape(8.dp))
                            .clickable(onClick = c.onClick).padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(c.titolo.uppercase(), Modifier.weight(1f), color = Fm.Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            if (c.modificata) Box(Modifier.size(7.dp).clip(RoundedCornerShape(4.dp)).background(Fm.Oro))
                        }
                        Text(c.valore, color = Fm.Testo, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2)
                    }
                }
                if (riga.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ValutazioneStaff(st: StileTattico) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Fm.Carta).border(1.dp, Fm.Bordo, RoundedCornerShape(8.dp)).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("VALUTAZIONE STAFF", color = Fm.Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        st.pregi.forEach { Text("+  $it", color = Fm.Verde, fontSize = 13.sp) }
        st.rischi.forEach { Text("−  $it", color = Fm.Rosso, fontSize = 13.sp) }
    }
}

// ------------------------------------------------------------------ campo

@Composable
private fun CampoFm(xi: List<Player>, formation: Formation, piano: PianoTattico, line: DefLine, fase: FaseVista, onGiocatore: (Player) -> Unit) {
    val coords = formation.coords()
    // Senza palla la squadra si abbassa: tanto più quanto più bassa è la linea difensiva
    val calo = when (line) { DefLine.BASSA -> 0.09f; DefLine.MEDIA -> 0.05f; DefLine.ALTA -> 0.02f }
    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(0.78f).clip(RoundedCornerShape(8.dp)).border(1.dp, Fm.Bordo, RoundedCornerShape(8.dp))) {
        val w = maxWidth; val h = maxHeight
        Canvas(Modifier.matchParentSize()) {
            val strisce = 10
            for (i in 0 until strisce) drawRect(if (i % 2 == 0) Fm.VerdeCampo else Fm.VerdeCampo2, Offset(0f, size.height * i / strisce), Size(size.width, size.height / strisce + 1f))
            val l = Color.White.copy(alpha = 0.55f); val st = Stroke(2.5f)
            drawRect(l, Offset(size.width * 0.04f, size.height * 0.02f), Size(size.width * 0.92f, size.height * 0.96f), style = st)
            drawLine(l, Offset(size.width * 0.04f, size.height * 0.5f), Offset(size.width * 0.96f, size.height * 0.5f), 2.5f)
            drawCircle(l, size.width * 0.12f, Offset(size.width / 2, size.height * 0.5f), style = st)
            drawRect(l, Offset(size.width * 0.27f, size.height * 0.02f), Size(size.width * 0.46f, size.height * 0.15f), style = st)
            drawRect(l, Offset(size.width * 0.27f, size.height * 0.83f), Size(size.width * 0.46f, size.height * 0.15f), style = st)
        }
        xi.forEachIndexed { i, p ->
            val c = coords.getOrNull(i) ?: return@forEachIndexed
            var yf = 0.88f - (c.first - 0.05f) / 0.41f * 0.74f
            if (fase == FaseVista.SENZA && p.role != "P") yf = (yf + calo).coerceAtMost(0.80f)
            if (fase == FaseVista.POSSESSO && p.role != "P") yf = (yf - 0.02f).coerceAtLeast(0.06f)
            var xf = c.second
            val conPalla = TacticalRoles.assigned(p)
            val senza = piano.ruoliNonPoss[p.id] ?: conPalla
            if (fase == FaseVista.SENZA && senza != conPalla) {
                val (dd, dw) = PostoRuolo.spostamento(conPalla, senza)
                yf = (yf - dd * 0.8f).coerceIn(0.06f, 0.82f); xf = (xf + dw * 0.8f).coerceIn(0.08f, 0.92f)
            }
            val conIstr = piano.istrGioc[p.id].orEmpty().isNotEmpty()
            val mostrato = if (fase == FaseVista.SENZA) senza else conPalla
            val etichetta = if (fase == FaseVista.COMBINATA && senza != conPalla) "$conPalla/$senza" else mostrato
            Box(Modifier.offset(x = w * xf - 31.dp, y = h * yf - 24.dp).width(62.dp)) {
                ChipGiocatore(p, etichetta, mostrato, if (p.role == "P") coloreLinea("POR") else coloreLinea(mostrato), conIstr) { onGiocatore(p) }
            }
        }
    }
}

@Composable
private fun ChipGiocatore(p: Player, etichetta: String, ruoloStelle: String, colore: Color, conIstr: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Fm.Carta).border(1.dp, Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(etichetta + if (conIstr) " •" else "", Modifier.fillMaxWidth().background(colore).padding(vertical = 1.dp), color = Color.White, fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, maxLines = 1)
        Stelle(mezzeStelle(p, ruoloStelle), 8.sp, Fm.Oro)
        Text(p.name.substringAfterLast(' '), Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 1.dp), color = Fm.Testo, fontSize = 9.sp,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ------------------------------------------------------------------ scelta ruolo

/**
 * Ruolo di un giocatore, con la palla e senza: elenco dei ruoli che conosce con le stelle, ruoli che può imparare,
 * attributi chiave del ruolo scelto e compito (solo con la palla).
 */
@Composable
private fun DialogRuolo(
    p: Player, piano: PianoTattico, faseIniziale: Int,
    onChiudi: () -> Unit,
    onRoleConPalla: (Player, String) -> Unit,
    onRoleSenzaPalla: (Player, String) -> Unit,
    onCompito: (Player, String) -> Unit,
    onIstr: (Player, IstrGiocatore) -> Unit
) {
    var fase by remember { mutableStateOf(faseIniziale) }
    val conPalla = TacticalRoles.assigned(p)
    val senza = piano.ruoliNonPoss[p.id] ?: conPalla
    val attivo = if (fase == 0) conPalla else senza
    val conosciuti = TacticalRoles.options(p)
    val noti = conosciuti.map { it.first }.toSet()
    val daImparare = (TacticalRoles.AFFINI[p.pos].orEmpty() + TacticalRoles.DA_IMPARARE[p.pos].orEmpty()).filter { it.first !in noti }

    Dialog(onDismissRequest = onChiudi) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Fm.Sfondo).border(1.dp, Fm.Bordo, RoundedCornerShape(10.dp))
                .padding(12.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(p.name, color = Fm.Testo, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text("${TacticalRoles.label(p.pos)} naturale · età ${p.age} · media ${p.overall}", color = Fm.Dim, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Scheda("Ruolo con la palla", fase == 0, Modifier.weight(1f)) { fase = 0 }
                Scheda("Ruolo senza palla", fase == 1, Modifier.weight(1f)) { fase = 1 }
            }
            conosciuti.forEach { (r, a) ->
                RigaRuolo(TacticalRoles.label(r), "$a%", mezzeStelle(p, r), r == attivo, noto = true) {
                    if (fase == 0) onRoleConPalla(p, if (r == p.pos) "" else r)
                    else onRoleSenzaPalla(p, if (r == conPalla) "" else r)
                }
            }
            if (daImparare.isNotEmpty()) {
                Text("RUOLI DA IMPARARE (allenamento)", color = Fm.Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                daImparare.forEach { (r, a) ->
                    RigaRuolo(TacticalRoles.label(r), "fino al $a%", 1, false, noto = false) {}
                }
            }
            // Attributi chiave del ruolo scelto
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Fm.Carta).border(1.dp, Fm.Bordo, RoundedCornerShape(8.dp)).padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row {
                    Text("ATTRIBUTI CHIAVE", Modifier.weight(1f), color = Fm.Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(TacticalRoles.label(attivo).uppercase(), color = Fm.Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                RuoliFm.attributiChiave(attivo).forEach { (nome, f) ->
                    val v = f(p)
                    Row {
                        Text(nome, Modifier.weight(1f), color = Fm.Testo, fontSize = 13.sp)
                        Text("$v", color = coloreAttributo(v), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            // Compito di ruolo: solo con la palla
            if (fase == 0) {
                val compiti = Compiti.perRuolo(conPalla)
                if (compiti.isNotEmpty()) {
                    Text("COMPITO (bonus del 3% nel ruolo)", color = Fm.Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    compiti.forEach { c ->
                        val on = p.compito == c.codice
                        Text(
                            c.nome, Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(if (on) Fm.Viola else Fm.Carta)
                                .border(1.dp, if (on) Fm.Viola else Fm.Bordo, RoundedCornerShape(6.dp))
                                .clickable { onCompito(p, if (on) "" else c.codice) }.padding(horizontal = 10.dp, vertical = 7.dp),
                            color = Fm.Testo, fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Compiti.trova(p.compito)?.let { Text(it.descrizione, color = Fm.Dim, fontSize = 12.sp) }
                }
            } else {
                Text("Un ruolo senza palla più congeniale rende la difesa un po' più solida (al massimo +8%).", color = Fm.Dim, fontSize = 12.sp)
            }
            // Istruzioni per il singolo giocatore
            Text("ISTRUZIONI GIOCATORE", color = Fm.Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            val mie = piano.istrGioc[p.id].orEmpty()
            IstrGiocatore.GRUPPI.forEach { g ->
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IstrGiocatore.values().filter { it.gruppo == g }.forEach { i ->
                        val on = i in mie
                        Text(
                            i.label, Modifier.clip(RoundedCornerShape(6.dp)).background(if (on) Fm.Viola else Fm.Carta)
                                .border(1.dp, if (on) Fm.Viola else Fm.Bordo, RoundedCornerShape(6.dp))
                                .clickable { onIstr(p, i) }.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = Fm.Testo, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1
                        )
                    }
                }
            }
            mie.forEach { Text("•  ${it.label}: ${it.descrizione}", color = Fm.Dim, fontSize = 11.sp) }
            Pulsante("Chiudi", viola = true, modifier = Modifier.fillMaxWidth(), onClick = onChiudi)
        }
    }
}

@Composable
private fun RigaRuolo(nome: String, dettaglio: String, mezze: Int, scelto: Boolean, noto: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(if (scelto) Fm.CartaAlta else Fm.Carta)
            .border(1.dp, if (scelto) Fm.Verde else Fm.Bordo, RoundedCornerShape(6.dp))
            .then(if (noto) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(14.dp).clip(RoundedCornerShape(7.dp)).border(2.dp, if (scelto) Fm.Verde else Fm.Dim, RoundedCornerShape(7.dp))
            .background(if (scelto) Fm.Verde else Color.Transparent))
        Column(Modifier.weight(1f)) {
            Text(nome, color = if (noto) Fm.Testo else Fm.Dim, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(dettaglio, color = Fm.Dim, fontSize = 11.sp)
        }
        if (noto) Stelle(mezze, 12.sp, Fm.Oro)
    }
}

// ------------------------------------------------------------------ scelta stile

/** I 11 stili: etichette, pregi e rischi dello staff, istruzioni con e senza palla. I tre più adatti alla rosa hanno il pollice. */
@Composable
private fun DialogStili(xi: List<Player>, attuale: StileTattico?, onChiudi: () -> Unit, onUsa: (StileTattico) -> Unit) {
    var scelto by remember { mutableStateOf(attuale ?: StileTattico.CONTROLLO_POSSESSO) }
    val punteggi = StileTattico.values().filter { it != StileTattico.TABULA_RASA }.associateWith { it.idoneita(xi) }
    val consigliati = punteggi.entries.sortedByDescending { it.value }.take(3).map { it.key }.toSet()

    Dialog(onDismissRequest = onChiudi) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Fm.Sfondo).border(1.dp, Fm.Bordo, RoundedCornerShape(10.dp))
                .padding(12.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Crea nuova tattica: scegli uno stile", color = Fm.Testo, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            Text("Lo stile imposta un pacchetto di istruzioni. Dopo puoi cambiare tutto.", color = Fm.Dim, fontSize = 12.sp)
            StileTattico.values().forEach { s ->
                val on = s == scelto
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (on) Fm.CartaAlta else Fm.Carta)
                        .border(1.dp, if (on) Fm.Viola else Fm.Bordo, RoundedCornerShape(8.dp))
                        .clickable { scelto = s }.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(s.nome, Modifier.weight(1f), color = Fm.Testo, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        if (s in consigliati) Text("CONSIGLIATO", color = Fm.Verde, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        if (s == attuale) Text("IN USO", color = Fm.Oro, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        s.intensita?.let { Etichetta(it.label) }
                        s.complessita?.let { Etichetta(it.label) }
                        if (s != StileTattico.TABULA_RASA) Etichetta("Adatto alla rosa: ${punteggi[s] ?: 50}")
                    }
                    Text(s.sintesi, color = Fm.Dim, fontSize = 12.sp)
                    if (on && s != StileTattico.TABULA_RASA) {
                        s.pregi.forEach { Text("+  $it", color = Fm.Verde, fontSize = 12.sp) }
                        s.rischi.forEach { Text("−  $it", color = Fm.Rosso, fontSize = 12.sp) }
                        Text("IN POSSESSO", color = Fm.Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        s.istruzioniInPossesso().forEach { Text("•  $it", color = Fm.Testo, fontSize = 12.sp) }
                        Text("NON IN POSSESSO", color = Fm.Dim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        s.istruzioniSenzaPalla().forEach { Text("•  $it", color = Fm.Testo, fontSize = 12.sp) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pulsante("Annulla", modifier = Modifier.weight(1f), onClick = onChiudi)
                Pulsante("Usa questo stile", viola = true, modifier = Modifier.weight(1f)) { onUsa(scelto) }
            }
        }
    }
}
