package it.androcalcio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.androcalcio.ui.components.Avatar
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel.ClubState
import it.androcalcio.AndrocalcioViewModel.ExtraStato
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.engine.Abbonamenti
import it.androcalcio.engine.Achievement
import it.androcalcio.engine.Agenti
import it.androcalcio.engine.Avanzate
import it.androcalcio.engine.Prestiti
import it.androcalcio.engine.Relazioni
import it.androcalcio.engine.SponsorDeal
import it.androcalcio.engine.Sponsors
import it.androcalcio.ui.components.GlassCard
import it.androcalcio.ui.components.PcAction
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.PcChip
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.theme.AC

/** Colonna a scorrimento con i margini standard delle schermate. */
@Composable
private fun Pagina(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) { content() }
}

@Composable
private fun Riga(sinistra: String, destra: String, evidenza: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(sinistra, color = AC.Text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(destra, color = if (evidenza) AC.Accent else AC.TextDim, style = MaterialTheme.typography.bodySmall)
    }
}

// ---------------------------------------------------------------------------------------------
// Stampa e spogliatoio
// ---------------------------------------------------------------------------------------------

@Composable
fun RelazioniScreen(
    ex: ExtraStato, rosa: List<Player>,
    onRisposta: (Int) -> Unit, onColloquio: (Long, Relazioni.Tono) -> Unit, onPsicologo: () -> Unit
) {
    Pagina {
        SectionTitle("Conferenza stampa")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val d = ex.domanda
                if (d == null) Text("Nessuna domanda per ora.", color = AC.TextDim)
                else {
                    if (d.testata.isNotEmpty()) Text("🎙 ${d.testata}" + if (d.perche.isNotEmpty()) " · ${d.perche}" else "", color = AC.TextDim, fontSize = 11.sp)
                    Text(d.testo, color = AC.Text, fontWeight = FontWeight.Bold)
                    if (ex.conferenzaFatta) Text("Hai già parlato alla stampa in questa giornata.", color = AC.TextDim, fontSize = 12.sp)
                    else d.risposte.forEachIndexed { i, r ->
                        Text("“${r.testo}”", color = AC.Text, fontSize = 13.sp)
                        Text("morale ${segnato(r.morale)} · tifosi ${segnato(r.tifosi)} · dirigenza ${segnato(r.fiducia)}" +
                            (if (d.playerId != null && r.singolo != 0) " · solo lui ${segnato(r.singolo)}" else ""),
                            color = AC.TextDim, fontSize = 11.sp)
                        PcButton("Rispondi così", { onRisposta(i) }, Modifier.fillMaxWidth(), compatto = true)
                    }
                }
            }
        }

        SectionTitle("Psicologo")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Lo psicologo è nello staff: scopre i problemi della rosa e tra i compagni, lavora sulla mentalità e scrive un report mensile.",
                    color = AC.TextDim, fontSize = 12.sp)
                PcButton("APRI LO PSICOLOGO", onPsicologo, Modifier.fillMaxWidth())
            }
        }

        val inDifficolta = rosa.count { Relazioni.haProblemi(it) }
        SectionTitle("Colloqui individuali · $inDifficolta con problemi")
        rosa.sortedBy { it.morale }.forEach { p ->
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Riga(p.name, "morale ${p.morale}" + if (Relazioni.haProblemi(p)) " ⚠" else "", Relazioni.haProblemi(p))
                    if (p.id in ex.colloquiFatti) Text("Colloquio già fatto in questa giornata.", color = AC.TextDim, fontSize = 11.sp)
                    else Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Relazioni.Tono.values().forEach { t ->
                            PcButton(t.nome, { onColloquio(p.id, t) }, Modifier.weight(1f), compatto = true)
                        }
                    }
                }
            }
        }
    }
}

private fun segnato(v: Int) = if (v > 0) "+$v" else "$v"

// ---------------------------------------------------------------------------------------------
// Marketing e prezzi
// ---------------------------------------------------------------------------------------------

@Composable
fun CommercialeScreen(
    ex: ExtraStato, c: ClubState,
    onMarketing: (Int) -> Unit, onCerca: () -> Unit, onFirma: (SponsorDeal) -> Unit, onPrezzo: (String, Int) -> Unit
) {
    Pagina {
        SectionTitle("Addetto marketing")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Cerca sponsor migliori e più numerosi. Costo annuo a fine stagione.", color = AC.TextDim, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Sponsors.MARKETING.forEachIndexed { i, _ ->
                        PcChip(selected = ex.marketing == i, onClick = { onMarketing(i) }, label = { Text(if (i == 0) "No" else "L$i", fontSize = 12.sp) })
                    }
                }
                Text("${Sponsors.MARKETING[ex.marketing.coerceIn(0, 3)]} · ${money(Sponsors.costoMarketing(ex.marketing))} l'anno · " +
                    "${Sponsors.numeroOfferte(ex.marketing)} offerte, +${Sponsors.bonusMarketingPct(ex.marketing)}% sui compensi",
                    color = AC.Accent, fontSize = 12.sp)
                PcButton("Cerca nuovi sponsor", onCerca, Modifier.fillMaxWidth(), main = true)
            }
        }

        SectionTitle("Contratti attivi (${c.sponsor.size}/${Sponsors.MAX_ATTIVI})")
        if (c.sponsor.isEmpty()) Text("Nessun contratto: cerca sponsor qui sopra.", color = AC.TextDim, fontSize = 12.sp)
        c.sponsor.forEach { d ->
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(8.dp)) {
                    Riga(d.name, money(d.annual), true)
                    Text("${Sponsors.nomeTipo(d.tipo.ifEmpty { "maglia" })} · ${d.years} ${if (d.years == 1) "anno" else "anni"}", color = AC.TextDim, fontSize = 11.sp)
                }
            }
        }

        SectionTitle("Offerte (${c.sponsorOfferte.size})")
        c.sponsorOfferte.forEach { d ->
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(d.name, color = AC.Text, fontWeight = FontWeight.Bold)
                        Text("${Sponsors.nomeTipo(d.tipo.ifEmpty { "maglia" })} · ${money(d.annual)} l'anno · ${d.years} anni", color = AC.TextDim, fontSize = 11.sp)
                    }
                    PcButton("Firma", { onFirma(d) }, compatto = true, main = true)
                }
            }
        }

        Text("I prezzi dei settori ora si trovano nella schermata Stadio (Home → Finanze → Stadio).", color = AC.TextDim, fontSize = 11.sp)
    }
}

// ---------------------------------------------------------------------------------------------
// Obiettivi e trofei
// ---------------------------------------------------------------------------------------------

@Composable
fun ObiettiviScreen(ex: ExtraStato, c: ClubState, posizione: Int? = null) {
    Pagina {
        SectionTitle("Obiettivo della dirigenza")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val o = ex.obiettivo
                if (o == null) Text("Fissato a inizio stagione.", color = AC.TextDim)
                else {
                    Text(o.tipo.nome, color = AC.Accent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text("Chiudere almeno ${o.posizioneMax}º (posizione attesa per la tua forza: ${ex.posizioneAttesa}º).", color = AC.Text, fontSize = 13.sp)
                    Text("Se riesci: bonus ${money(o.bonus)} e più fiducia. Se manchi: la fiducia scende di ${o.penale}.", color = AC.TextDim, fontSize = 12.sp)
                    Text("Fiducia attuale della dirigenza: ${c.fiducia}", color = AC.TextDim, fontSize = 12.sp)
                    if (posizione != null) {
                        val ok = posizione <= o.posizioneMax
                        Text(
                            if (ok) "In linea: sei ${posizione}º in classifica, obiettivo ${o.posizioneMax}º o meglio."
                            else "Fuori obiettivo: sei ${posizione}º, serve almeno il ${o.posizioneMax}º posto.",
                            color = if (ok) AC.Accent else AC.ColEn, fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        SectionTitle("Premi individuali (ultima stagione)")
        if (ex.premi.isEmpty()) Text("Si assegnano a fine stagione.", color = AC.TextDim, fontSize = 12.sp)
        ex.premi.forEach { p ->
            GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(8.dp)) { Riga("🏅 ${p.titolo}", p.dettaglio, true) } }
        }

        SectionTitle("Trofei sbloccati ${ex.trofei.size}/${Achievement.TUTTI.size}")
        Achievement.TUTTI.forEach { t ->
            val ok = t.id in ex.trofei
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (ok) t.icona else "🔒", fontSize = 22.sp)
                    Column(Modifier.weight(1f)) {
                        Text(t.nome, color = if (ok) AC.Accent else AC.TextDim, fontWeight = FontWeight.Bold)
                        Text(t.descrizione, color = AC.TextDim, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Giornale, meteo e statistiche avanzate
// ---------------------------------------------------------------------------------------------

@Composable
fun GiornaleScreen(ex: ExtraStato, onSceltaSfida: (Int) -> Unit = {}, onCoreo: () -> Unit = {}) {
    Pagina {
        SectionTitle("Ultras")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp)) {
                Text("Umore degli ultras: ${ex.ultras}/100" + if (ex.ultras < 25) " · in contestazione" else "", color = AC.Text, fontWeight = FontWeight.Bold)
                Text("Le vittorie li scaldano, le sconfitte li raffreddano: sotto 25 contestano e abbassano tifosi e fiducia. Una coreografia (15.000 €) carica l'atmosfera della prossima partita in casa.",
                    color = AC.TextDim, fontSize = 11.sp)
                PcAction(onClick = onCoreo, enabled = !ex.coreo) { Text(if (ex.coreo) "Coreografia pronta" else "Organizza coreografia") }
            }
        }
        SectionTitle("Prossima partita")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp)) {
                val m = ex.meteo
                Text(m?.descrizione() ?: "Nessuna partita in programma.", color = AC.Text, fontWeight = FontWeight.Bold)
                if (m != null) Text("Pioggia, neve o campo pesante riducono le occasioni da gol; il manto ibrido e il riscaldamento aiutano.",
                    color = AC.TextDim, fontSize = 11.sp)
            }
        }

        SectionTitle("Il giornale dello sport")
        val g = ex.giornale
        if (g == null) Text("Dopo la tua prossima partita esce l'edizione con titolo e pagelle.", color = AC.TextDim, fontSize = 12.sp)
        else GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(g.titolo, color = AC.Accent, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text(g.sommario, color = AC.Text, fontSize = 13.sp)
                g.pagelle.forEach { Riga(it.nome, "${"%.1f".format(it.voto)} · ${it.commento}", it.voto >= 7.0) }
            }
        }

        SectionTitle("Sfida della settimana")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val sf = ex.sfida
                Text(sf?.let { "Prossima partita: ${it.testo} (premio: +${it.fiducia} fiducia, +${it.tifosi} tifosi)" }
                    ?: "La prima sfida arriva dopo la prossima partita.", color = AC.Text, fontWeight = FontWeight.Bold)
                if (ex.sfidaEsito.isNotEmpty()) Text(ex.sfidaEsito, color = AC.TextDim, fontSize = 12.sp)
                if (ex.sfideOfferte.isNotEmpty()) {
                    Text("Scegli la sfida (più è difficile, più rende):", color = AC.TextDim, fontSize = 12.sp)
                    ex.sfideOfferte.forEach { o ->
                        PcChip(ex.sfida?.id == o.id, { onSceltaSfida(o.id) }, {
                            Text("${it.androcalcio.engine.Sfide.livello(o.id)}: ${o.testo} (+${o.fiducia} fiducia, +${o.tifosi} tifosi)", fontSize = 12.sp)
                        }, Modifier.fillMaxWidth())
                    }
                }
                Text("Sfide riuscite: ${ex.sfideVinte} · serie in corso: ${ex.sfideSerie} (ogni 3 di fila, premio extra)", color = AC.TextDim, fontSize = 12.sp)
            }
        }

        SectionTitle("Il social del club")
        if (ex.social.isEmpty()) Text("Dopo la prossima partita tifosi, giornalisti e presidente dicono la loro.", color = AC.TextDim, fontSize = 12.sp)
        else GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ex.social.forEach { Text(it, color = AC.Text, fontSize = 13.sp) }
            }
        }

        SectionTitle("Statistiche avanzate")
        val a = ex.avanzate
        if (a.partite == 0) Text("Si compilano partita dopo partita.", color = AC.TextDim, fontSize = 12.sp)
        else GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Riga("Partite", "${a.partite}")
                Riga("Gol fatti / subiti", "${a.golF} / ${a.golS}")
                Riga("Tiri fatti / subiti", "${a.tiriF} / ${a.tiriS}")
                Riga("xG (gol attesi) fatti", "%.1f".format(a.xgF), true)
                Riga("xG subiti", "%.1f".format(a.xgS), true)
                Riga("Attacco: gol rispetto agli xG", "%.2f".format(a.rendimentoAttacco))
                Riga("Difesa: gol subiti rispetto agli xG", "%.2f".format(a.rendimentoDifesa))
                Riga("Possesso medio", "${a.possessoMedio}%")
            }
        }
        if (a.partite > 0) {
            SectionTitle("Mappa di calore dei tuoi tiri")
            Text("Attacchi verso destra. Più scuro il rosso, più tiri da quella zona.", color = AC.TextDim, fontSize = 11.sp)
            Canvas(Modifier.fillMaxWidth().height(150.dp)) {
                drawRect(AC.PitchA)
                val cw = size.width / Avanzate.COLONNE
                val ch = size.height / Avanzate.RIGHE
                for (r in 0 until Avanzate.RIGHE) for (col in 0 until Avanzate.COLONNE) {
                    val n = a.zone[r * Avanzate.COLONNE + col]
                    if (n > 0) drawRect(Color(0xFFFF3D3D).copy(alpha = (0.15f + 0.75f * n / a.massimaZona).coerceIn(0f, 0.9f)),
                        Offset(col * cw, r * ch), Size(cw, ch))
                }
                drawRect(Color.White.copy(alpha = 0.6f), Offset.Zero, size, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                drawLine(Color.White.copy(alpha = 0.6f), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 2f)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Prestiti e finestre di mercato
// ---------------------------------------------------------------------------------------------

@Composable
fun PrestitiScreen(
    ex: ExtraStato, team: Team, rosa: List<Player>, altri: List<Player>, nomiClub: Map<Long, String>,
    onPresta: (Player) -> Unit, onPrendi: (Player) -> Unit, onRiscatta: (Long) -> Unit
) {
    Pagina {
        SectionTitle("Finestra di mercato")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp)) {
                Text(ex.finestra.nome + if (ex.ultimoGiorno && ex.finestra.aperta) " · ULTIMO GIORNO" else "",
                    color = if (ex.finestra.aperta) AC.Ok else AC.Danger, fontWeight = FontWeight.ExtraBold)
                Text("Le trattative con i club si aprono a inizio stagione e a metà campionato. Nell'ultimo giorno i club accettano più facilmente. " +
                    "Svincolati e offerte già accettate sono sempre possibili. Le firme costano anche la commissione dell'agente.",
                    color = AC.TextDim, fontSize = 11.sp)
                Text("Budget: ${money(ex.budget)}", color = AC.Accent, fontSize = 12.sp)
            }
        }

        SectionTitle("Prestiti in corso (${ex.prestiti.size})")
        if (ex.prestiti.isEmpty()) Text("Nessun prestito.", color = AC.TextDim, fontSize = 12.sp)
        val tutti = (rosa + altri).associateBy { it.id }
        ex.prestiti.forEach { pr ->
            val p = tutti[pr.playerId]
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p?.name ?: "Giocatore", color = AC.Text, fontWeight = FontWeight.Bold)
                        Text(if (pr.inUscita) "Ceduto a ${nomiClub[pr.destinazione] ?: "—"} fino a fine stagione"
                        else "In prestito da ${nomiClub[pr.origine] ?: "—"} · riscatto ${money(pr.riscatto)}", color = AC.TextDim, fontSize = 11.sp)
                    }
                    if (!pr.inUscita) PcButton("Riscatta", { onRiscatta(pr.playerId) }, compatto = true, main = true)
                }
            }
        }

        SectionTitle("Presta un tuo giocatore")
        rosa.filter { p -> ex.prestiti.none { it.playerId == p.id } }.sortedBy { it.overall }.take(12).forEach { p ->
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(p.name, 32.dp, Modifier.padding(end = 8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${p.name} (${p.role}, ${p.age}a, ${p.overall})", color = AC.Text, fontSize = 13.sp)
                        Text("Incassi ${money(Prestiti.costo(p))}", color = AC.TextDim, fontSize = 11.sp)
                    }
                    PcButton("Presta", { onPresta(p) }, compatto = true, enabled = Prestiti.puoPrestare(p, rosa.size))
                }
            }
        }

        SectionTitle("Prendi in prestito")
        altri.take(15).forEach { p ->
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(p.name, 32.dp, Modifier.padding(end = 8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${p.name} (${p.role}, ${p.age}a, ${p.overall})", color = AC.Text, fontSize = 13.sp)
                        Text("${nomiClub[p.teamId] ?: "—"} · prestito ${money(Prestiti.costo(p))} · riscatto ${money(Prestiti.riscatto(p))}" +
                            " · agente ${Agenti.percentuale(p)}%", color = AC.TextDim, fontSize = 11.sp)
                    }
                    PcButton("Chiedi", { onPrendi(p) }, compatto = true, enabled = Prestiti.puoPrendere(rosa.size))
                }
            }
        }
    }
}
