package it.androcalcio.ui

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.androcalcio.AndrocalcioViewModel
import it.androcalcio.data.Player
import it.androcalcio.data.Staff
import it.androcalcio.engine.CalendarioGiorni
import it.androcalcio.engine.Fisio
import it.androcalcio.engine.Migliorie
import it.androcalcio.engine.PreparatoreCapo
import it.androcalcio.engine.Staffing
import it.androcalcio.ui.components.PaperPanel
import it.androcalcio.ui.components.RuoloChip
import it.androcalcio.ui.components.PcButton
import it.androcalcio.ui.components.SectionTitle
import it.androcalcio.ui.components.money
import it.androcalcio.ui.components.Stelle
import it.androcalcio.ui.theme.AC

/** Intestazione comune: chi ricopre il ruolo, con stelle, oppure invito ad assumerlo. */
@Composable
private fun CapoRuolo(titolo: String, s: Staff?, onStaff: () -> Unit) {
    PaperPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titolo, color = AC.PaperText, fontWeight = FontWeight.Bold)
            if (s == null) {
                Text("Ruolo scoperto: nessun effetto. Assumi una figura dallo Staff.", color = AC.PaperDim, fontSize = 12.sp)
                PcButton("Vai allo Staff", onStaff, Modifier.fillMaxWidth())
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.name, color = AC.PaperText)
                    Stelle(Staffing.mezzeStelle(s.skill))
                }
                Text("Contratto ${s.contract} anni", color = AC.PaperDim, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun Pagina(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
}

/** Capo preparatori: riepilogo del giorno, poi i giocatori raggruppati per attività con il motivo della scelta. */
@Composable
fun SchedaPreparatori(staff: List<Staff>, rosa: List<Player>, giorno: AndrocalcioViewModel.GiornoStato?, onStaff: () -> Unit) {
    val capo = staff.firstOrNull { it.role == Staffing.PREPARATORE && it.specialita == Staffing.GENERALE }
        ?: staff.firstOrNull { it.role == Staffing.PREPARATORE }
    val skill = capo?.skill ?: Staffing.VUOTO
    val partita = giorno?.prossima
    data class Riga(val p: Player, val gruppo: String, val icona: String, val motivo: String)
    val righe = rosa.map { p ->
        val att = if (giorno == null || partita == null) null else PreparatoreCapo.attivitaGiocatore(p, giorno.oggi, partita, skill)
        val (g, i) = when {
            p.injuredRounds > 0 -> "Infermeria" to "🩹"
            att == null -> "Senza programma" to "•"
            else -> att.label to att.icona
        }
        Riga(p, g, i, if (giorno == null) "" else PreparatoreCapo.motivo(p, giorno.oggi, partita, skill))
    }
    Pagina {
        CapoRuolo("Capo preparatori", capo, onStaff)
        if (giorno != null) Text(
            "Oggi: ${CalendarioGiorni.nomeGiorno(giorno.oggi)}" +
                (partita?.let { " · prossima partita: " + CalendarioGiorni.nomeGiorno(it) } ?: ""),
            color = AC.Text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        // Riepilogo: quanti giocatori per attività
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            righe.groupBy { it.gruppo }.toList().sortedByDescending { it.second.size }.forEach { (g, l) ->
                PaperPanel(Modifier.weight(1f)) {
                    Column(Modifier.padding(6.dp)) {
                        Text("${l.first().icona} ${l.size}", color = AC.PaperText, fontWeight = FontWeight.Bold)
                        Text(g, color = AC.PaperDim, fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }
        Text("Il capo decide in base a forma, morale e distanza dalla partita. Forma bassa = riposo; vigilia = rifinitura.",
            color = AC.TextDim, fontSize = 11.sp)
        righe.groupBy { it.gruppo }.toList().sortedBy { (g, _) -> if (g == "Infermeria") 0 else 1 }.forEach { (g, l) ->
            SectionTitle("${l.first().icona} $g (${l.size})")
            PaperPanel(Modifier.fillMaxWidth()) {
                l.sortedBy { it.p.condition }.forEachIndexed { idx, r ->
                    Row(Modifier.fillMaxWidth().background(if (idx % 2 == 1) AC.PaperAlt else AC.Paper).padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        RuoloChip(r.p.role, r.p.pos)
                        Column(Modifier.weight(1f).padding(start = 6.dp)) {
                            Text(r.p.name, color = AC.PaperText, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(r.motivo, color = AC.PaperDim, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Forma ${r.p.condition}", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = when { r.p.condition >= 80 -> AC.ColGreen; r.p.condition >= 60 -> AC.ColOrange; else -> AC.ColEn })
                            Text("Morale ${r.p.morale}", fontSize = 11.sp,
                                color = when { r.p.morale >= 70 -> AC.ColGreen; r.p.morale >= 45 -> AC.ColOrange; else -> AC.ColEn })
                        }
                    }
                }
            }
        }
    }
}

/** Fisioterapisti: infortunati con i turni di stop e giocatori a rischio per stanchezza. */
@Composable
fun SchedaFisio(
    staff: List<Staff>, rosa: List<Player>, onStaff: () -> Unit,
    fisio: AndrocalcioViewModel.FisioStato = AndrocalcioViewModel.FisioStato(),
    onTest: (Player) -> Unit = {}, onOpera: (Player) -> Unit = {}
) {
    fun sk(spec: String) = staff.firstOrNull { it.role == Staffing.FISIOTERAPISTA && it.specialita == spec }?.skill ?: Staffing.NEUTRAL
    val rec = sk(Staffing.RECUPERO); val prv = sk(Staffing.PREVENZIONE)
    Pagina {
        CapoRuolo("Fisioterapisti", staff.firstOrNull { it.role == Staffing.FISIOTERAPISTA }, onStaff)
        val infortunati = rosa.filter { it.injuredRounds > 0 }.sortedByDescending { it.injuredRounds }
        SectionTitle("Infermeria (${infortunati.size})")
        if (infortunati.isEmpty()) Text("Nessun infortunato.", color = AC.TextDim, fontSize = 12.sp)
        if (infortunati.isNotEmpty()) PaperPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                infortunati.forEach { p ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RuoloChip(p.role, p.pos)
                        Text(" ${p.name}", color = AC.PaperText, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("✚ ${p.injuredRounds} turni", color = AC.ColEn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    when {
                        p.id in fisio.operati -> Text("Già operato in questa stagione.", color = AC.PaperDim, fontSize = 11.sp)
                        p.injuredRounds < 2 -> Text("Stop breve: operazione non utile.", color = AC.PaperDim, fontSize = 11.sp)
                        else -> {
                            val salva = Fisio.turniRisparmiati(p.injuredRounds, rec)
                            Text("Operazione: ${money(Fisio.costoOperazione(p, p.injuredRounds))} · −$salva turni · ricaduta ${Fisio.rischioRicaduta(rec)}%",
                                color = AC.PaperText, fontSize = 11.sp)
                            PcButton("OPERA", { onOpera(p) }, compatto = true)
                        }
                    }
                }
            }
        }
        SectionTitle("Test di propensione agli infortuni")
        Text("Costo ${money(Fisio.costoTest(prv))} a giocatore · precisione ±${Fisio.errore(prv)} (dipende dal fisio della prevenzione).",
            color = AC.TextDim, fontSize = 11.sp)
        PaperPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                rosa.sortedBy { it.name }.forEach { p ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RuoloChip(p.role, p.pos)
                        Text(" ${p.name}", color = AC.PaperText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        val t = fisio.test[p.id]
                        if (t != null) Text(t, color = when {
                            t.contains("ALTA") -> AC.ColEn; t.contains("MEDIA") -> AC.PaperText; else -> AC.Ok
                        }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        else PcButton("TEST", { onTest(p) }, compatto = true)
                    }
                }
            }
        }
        val rischio = rosa.filter { it.injuredRounds == 0 && it.condition < 60 }
        SectionTitle("A rischio per stanchezza (${rischio.size})")
        rischio.forEach { Text("${it.name}: forma ${it.condition}", color = AC.Text, fontSize = 13.sp) }
    }
}

/** Giardiniere: stato del manto erboso (dalla sua abilità) e avanzamento dei cantieri. */
@Composable
fun SchedaGiardiniere(staff: List<Staff>, mig: AndrocalcioViewModel.MigliorieStato, onStaff: () -> Unit) {
    val g = staff.firstOrNull { it.role == Staffing.TUTTOFARE }
    Pagina {
        CapoRuolo("Giardiniere e tuttofare", g, onStaff)
        val v = g?.skill ?: Staffing.VUOTO
        val stato = when { v >= 75 -> "Ottimo"; v >= 60 -> "Buono"; v >= 45 -> "Discreto"; else -> "Scadente" }
        SectionTitle("Stato del campo")
        Text("$stato (curato al livello $v/99)", color = AC.Text)
        SectionTitle("Cantieri (${mig.cantieri.size})")
        if (mig.cantieri.isEmpty()) Text("Nessun cantiere aperto.", color = AC.TextDim, fontSize = 12.sp)
        mig.cantieri.forEach { (id, gg) ->
            val m = Migliorie.trova(id)
            Text("${m?.icona ?: ""} ${m?.nome ?: id}: mancano $gg giornate", color = AC.Text, fontSize = 13.sp)
        }
    }
}

/** Analista video: ultima partita (cosa ha funzionato e cosa no, chi deve migliorare) e, sotto, l'avversario che verrà. */
@Composable
fun SchedaAnalista(staff: List<Staff>, analisi: it.androcalcio.engine.AnalistaPartita.Analisi, onStaff: () -> Unit,
                   avversario: @Composable () -> Unit) {
    val a = staff.firstOrNull { it.role == Staffing.ANALISTA }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Column(Modifier.fillMaxWidth().heightIn(max = 330.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CapoRuolo("Analista video", a, onStaff)
        if (a != null) {
            SectionTitle("Ultima partita")
            if (analisi.bene.isEmpty() && analisi.male.isEmpty()) Text("Nessuna partita analizzata ancora.", color = AC.TextDim, fontSize = 12.sp)
            analisi.bene.forEach { Text("✔ $it", color = AC.Text, fontSize = 13.sp) }
            analisi.male.forEach { Text("✘ $it", color = AC.Text, fontSize = 13.sp) }
            if (analisi.migliorare.isNotEmpty()) {
                SectionTitle("Da migliorare")
                analisi.migliorare.forEach { Text("• $it", color = AC.Text, fontSize = 13.sp) }
            }
        }
      }
      SectionTitle("Prossima partita")
      Box(Modifier.weight(1f)) { avversario() }
    }
}

/**
 * Psicologo: stato dello spogliatoio, problemi rilevati con la seduta per risolverli,
 * mentalità della rosa (tocca un giocatore per lavorare su un valore) e report.
 */
@Composable
fun SchedaPsicologo(
    staff: List<Staff>, rosa: List<Player>, st: AndrocalcioViewModel.PsiStato, onStaff: () -> Unit,
    onSeduta: (String) -> Unit, onLavoro: (Player, it.androcalcio.engine.Mente.Attr) -> Unit, onReport: () -> Unit
) {
    val M = it.androcalcio.engine.Mente
    val P = it.androcalcio.engine.Psicologo
    val psi = staff.firstOrNull { s -> s.role == Staffing.PSICOLOGO }
    var scelto by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Player?>(null) }
    scelto?.let { p ->
        val v = M.valori(p, st.mente)
        val attesa = st.attesa["lav_${p.id}"] ?: 0
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { scelto = null },
            title = { Text("Lavoro mentale · ${p.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (attesa > 0) "Già seguito di recente: riprova tra $attesa giorni."
                        else "Costo ${money(P.costoLavoro(st.skill))}. Riesce più spesso con chi è professionale e sui valori bassi.",
                        fontSize = 12.sp, color = AC.TextDim)
                    M.Attr.values().forEach { a ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${a.nome} ${v[a.ordinal]}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = coloreMente(v[a.ordinal]))
                                Text(a.descrizione, fontSize = 11.sp, color = AC.TextDim)
                            }
                            if (attesa == 0) PcButton("LAVORA", { onLavoro(p, a); scelto = null }, compatto = true)
                        }
                    }
                }
            },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { scelto = null }) { Text("Chiudi") } }
        )
    }
    Pagina {
        CapoRuolo("Psicologo", psi, onStaff)
        if (psi == null) {
            Text("Senza psicologo non vedi i problemi nascosti, non puoi lavorare sulla mentalità e non ricevi il report mensile.",
                color = AC.TextDim, fontSize = 12.sp)
            return@Pagina
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(Triple("CLIMA", st.clima, "spogliatoio"), Triple("AFFIATAMENTO", st.affiatamento, "squadra"),
                Triple("PROBLEMI", st.problemi.size, "rilevati")).forEach { (t, v, sub) ->
                PaperPanel(Modifier.weight(1f)) {
                    Column(Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(t, fontSize = 10.sp, color = AC.PaperDim, fontWeight = FontWeight.Bold)
                        Text("$v", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (t == "PROBLEMI") (if (v == 0) AC.Ok else AC.ColEn) else coloreMente(v))
                        Text(sub, fontSize = 10.sp, color = AC.PaperDim)
                    }
                }
            }
        }
        SectionTitle("Problemi rilevati")
        if (st.problemi.isEmpty()) Text("Nessun problema in vista.", color = AC.TextDim, fontSize = 12.sp)
        if (st.nascosti > 0) Text("Potrebbero essercene altri ${st.nascosti}: uno psicologo più bravo li vedrebbe.", color = AC.TextDim, fontSize = 11.sp)
        st.problemi.forEach { pb ->
            PaperPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(6.dp).heightIn(min = 44.dp).background(when (pb.gravita) { 3 -> AC.ColEn; 2 -> AC.ColOrange; else -> AC.ColBlue }))
                    Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        Text("${pb.tipo.icona} ${pb.tipo.nome}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AC.PaperText)
                        Text(pb.testo, fontSize = 12.sp, color = AC.PaperText)
                        val att = st.attesa[pb.chiave] ?: 0
                        Text(if (att > 0) "Seduta già fatta: riprova tra $att giorni."
                            else "${pb.tipo.azione}: ${money(P.costoSeduta(st.skill, pb.tipo))} · riuscita ${P.probabilita(st.skill)}%",
                            fontSize = 11.sp, color = AC.PaperDim)
                    }
                    if ((st.attesa[pb.chiave] ?: 0) == 0) PcButton("AGISCI", { onSeduta(pb.chiave) }, compatto = true)
                }
            }
        }
        SectionTitle("Mentalità della rosa")
        Text("Tocca un giocatore per lavorare su un valore. DE determinazione · LD leadership · FR freddezza · PR professionalità · AU autocontrollo · SQ spirito di squadra.",
            color = AC.TextDim, fontSize = 11.sp)
        PaperPanel(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row {
                    Text("", Modifier.weight(1f))
                    M.Attr.values().forEach { a -> Text(a.sigla, Modifier.width(28.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AC.PaperDim) }
                }
                rosa.sortedBy { it.name }.forEach { p ->
                    val v = M.valori(p, st.mente)
                    Row(Modifier.fillMaxWidth().clickable { scelto = p }, verticalAlignment = Alignment.CenterVertically) {
                        RuoloChip(p.role, p.pos)
                        Text(" ${p.name}", Modifier.weight(1f), fontSize = 12.sp, color = AC.PaperText, maxLines = 1)
                        v.forEach { x -> Text("$x", Modifier.width(28.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = coloreMente(x)) }
                    }
                }
            }
        }
        SectionTitle("Report")
        PcButton("REPORT ORA", onReport, Modifier.fillMaxWidth())
        if (st.report.isNotBlank()) PaperPanel(Modifier.fillMaxWidth()) {
            Text(st.report, Modifier.padding(8.dp), fontSize = 12.sp, color = AC.PaperText)
        } else Text("Il report arriva a inizio mese (anche nella posta) o quando lo chiedi.", color = AC.TextDim, fontSize = 11.sp)
    }
}

fun coloreMente(v: Int) = when { v >= 70 -> Color(0xFF2E7D32); v >= 45 -> Color(0xFFB26A00); else -> Color(0xFFC62828) }
