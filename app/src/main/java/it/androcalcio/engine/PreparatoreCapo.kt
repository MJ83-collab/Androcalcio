package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Youth
import java.time.LocalDate
import kotlin.random.Random

/**
 * Il preparatore atletico capo: se il club ha un preparatore generale e l'opzione è attiva, decide da solo
 * allenamenti e riposi della rosa, il piano dei reparti e la crescita del vivaio, e ogni mese scrive un report.
 */
object PreparatoreCapo {
    const val KEY_AUTO = "prep_auto"          // "0" = spento (default acceso)
    const val KEY_MESE = "prep_mese"          // ultimo mese elaborato (aaaa-mm)
    const val KEY_SNAP = "prep_snap"          // livelli della rosa a inizio mese: "id:ov;..."
    const val KEY_SNAP_VIV = "prep_snap_viv"  // livelli del vivaio a inizio mese
    const val KEY_REPORT = "prep_report"      // ultimi report separati da §
    const val MAX_REPORT = 6

    fun meseDi(d: LocalDate) = "%04d-%02d".format(d.year, d.monthValue)

    /** Cosa fa la rosa in un giorno libero: riposo il giorno prima della partita o se è stanca, altrimenti allenamento. */
    fun attivita(giorno: LocalDate, partita: LocalDate, rosa: List<Player>, skill: Int): CalendarioGiorni.Attivita {
        if (giorno.plusDays(1) == partita) return CalendarioGiorni.Attivita.RIPOSO
        val ok = rosa.filter { it.injuredRounds == 0 }
        if (ok.isEmpty()) return CalendarioGiorni.Attivita.RIPOSO
        val soglia = 72 + (skill - 50) / 8       // più è bravo, più è prudente
        val stanchi = ok.count { it.condition < 65 }
        return if (ok.map { it.condition }.average() < soglia || stanchi >= 4) CalendarioGiorni.Attivita.RIPOSO
        else CalendarioGiorni.Attivita.ALLENAMENTO
    }

    /** Attività di un singolo giocatore: il capo guarda forma, morale e quanto manca alla partita. */
    fun attivitaGiocatore(p: Player, giorno: LocalDate, partita: LocalDate, skill: Int): CalendarioGiorni.Attivita {
        if (p.injuredRounds > 0) return CalendarioGiorni.Attivita.RIPOSO
        if (giorno.plusDays(1) == partita)
            return if (p.condition < 80) CalendarioGiorni.Attivita.RIPOSO else CalendarioGiorni.Attivita.RIFINITURA
        val soglia = 60 + (skill - 50) / 8
        return if (p.condition < soglia || p.morale < 40) CalendarioGiorni.Attivita.RIPOSO else CalendarioGiorni.Attivita.ALLENAMENTO
    }

    /** Vive un giorno: ogni giocatore fa la sua attività. Restituisce la rosa aggiornata e l'attività prevalente (per diario e preparazione). */
    fun giornata(rosa: List<Player>, giorno: LocalDate, partita: LocalDate, skill: Int,
                 rng: kotlin.random.Random = kotlin.random.Random.Default): Pair<List<Player>, CalendarioGiorni.Attivita> {
        val att = rosa.map { attivitaGiocatore(it, giorno, partita, skill) }
        val nuova = rosa.mapIndexed { i, p -> CalendarioGiorni.applica(p, att[i], rng) }
        val sani = rosa.indices.filter { rosa[it].injuredRounds == 0 }.map { att[it] }
        val prevalente = sani.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: CalendarioGiorni.Attivita.RIPOSO
        return nuova to prevalente
    }

    /** Piano di squadra per la prossima partita: intensità in base alla fatica, focus a rotazione. */
    fun piano(rosa: List<Player>, skill: Int, giornata: Int): TrainingPlan {
        val ok = rosa.filter { it.injuredRounds == 0 }
        val media = if (ok.isEmpty()) 100.0 else ok.map { it.condition }.average()
        val focus = listOf(TrainFocus.TECNICA, TrainFocus.FISICO, TrainFocus.TATTICA)[giornata.mod(3)]
        return when {
            media < 62 -> TrainingPlan(TrainFocus.RIPOSO, TrainIntensity.BASSA)
            media < 78 -> TrainingPlan(focus, TrainIntensity.BASSA)
            media >= 92 && skill >= 60 -> TrainingPlan(focus, TrainIntensity.ALTA)
            else -> TrainingPlan(focus, TrainIntensity.MEDIA)
        }
    }

    private fun media(ps: List<Player>, f: (Player) -> Int) = if (ps.isEmpty()) 0.0 else ps.map(f).average()

    /** Piano per reparto: ogni reparto lavora sul gruppo di parametri più debole. Restituisce focus per giocatore e descrizione. */
    fun pianiReparti(rosa: List<Player>): Pair<Map<Long, TrainFocus>, String> {
        val out = HashMap<Long, TrainFocus>()
        val parti = ArrayList<String>()
        for ((ruolo, nome) in listOf("P" to "Portieri", "D" to "Difesa", "C" to "Centrocampo", "A" to "Attacco")) {
            val ps = rosa.filter { it.role == ruolo && it.injuredRounds == 0 }
            if (ps.isEmpty()) continue
            val f = if (ruolo == "P") TrainFocus.TECNICA else {
                val fis = media(ps) { (it.ve + it.re + it.ag) / 3 }
                val tec = media(ps) { (it.qu + it.pa + it.dr + it.ti) / 4 }
                val tat = media(ps) { (it.en + it.pa) / 2 }
                listOf(TrainFocus.FISICO to fis, TrainFocus.TECNICA to tec, TrainFocus.TATTICA to tat).minByOrNull { it.second }!!.first
            }
            ps.forEach { out[it.id] = f }
            parti += "$nome: ${f.label.lowercase()}"
        }
        return out to parti.joinToString(", ")
    }

    /** Crescita mensile extra del vivaio, guidata dal preparatore (resta sotto il potenziale). */
    fun cresciVivaio(y: Youth, skill: Int, rng: Random): Youth =
        if (y.overall < y.potential && rng.nextDouble() < 0.05 + skill / 1000.0) y.copy(overall = y.overall + 1) else y

    fun codifica(m: Map<Long, Int>) = m.entries.joinToString(";") { "${it.key}:${it.value}" }
    fun decodifica(s: String?): Map<Long, Int> = s.orEmpty().split(";").mapNotNull {
        val x = it.split(":"); val k = x.getOrNull(0)?.toLongOrNull(); val v = x.getOrNull(1)?.toIntOrNull()
        if (k != null && v != null) k to v else null
    }.toMap()

    private val MESI = listOf("gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno", "luglio", "agosto",
        "settembre", "ottobre", "novembre", "dicembre")
    fun nomeMese(m: String): String = m.split("-").getOrNull(1)?.toIntOrNull()?.let { MESI.getOrNull(it - 1) } ?: m

    /** Il report mensile del preparatore capo. */
    fun report(nome: String, mese: String, rosa: List<Player>, prima: Map<Long, Int>, viv: List<Youth>, vivPrima: Map<Long, Int>,
               allenamenti: Int, riposi: Int, pianoReparti: String, squadraB: String = ""): String {
        val salita = rosa.mapNotNull { p -> prima[p.id]?.let { p to (p.overall - it) } }
        val migliori = salita.filter { it.second > 0 }.sortedByDescending { it.second }.take(3)
        val calati = salita.filter { it.second < 0 }.sortedBy { it.second }.take(2)
        val ok = rosa.filter { it.injuredRounds == 0 }
        val cond = if (ok.isEmpty()) 0 else ok.map { it.condition }.average().toInt()
        val infortunati = rosa.count { it.injuredRounds > 0 }
        val crescitiViv = viv.mapNotNull { y -> vivPrima[y.id]?.let { y to (y.overall - it) } }.filter { it.second > 0 }
            .sortedByDescending { it.second }
        return buildString {
            append("Report di ${nomeMese(mese)} · $nome\n")
            append("Allenamenti $allenamenti, riposi $riposi. Condizione media $cond%, infortunati $infortunati.\n")
            append(if (migliori.isEmpty()) "Rosa: nessun miglioramento di livello questo mese.\n"
                else "Rosa, in crescita: " + migliori.joinToString(", ") { "${it.first.name} +${it.second}" } + ".\n")
            if (calati.isNotEmpty()) append("In calo: " + calati.joinToString(", ") { "${it.first.name} ${it.second}" } + ".\n")
            append(if (crescitiViv.isEmpty()) "Vivaio: nessun progresso rilevante.\n"
                else "Vivaio: " + crescitiViv.size + " ragazzi migliorati (" +
                    crescitiViv.take(3).joinToString(", ") { "${it.first.name} +${it.second}" } + ").\n")
            if (squadraB.isNotEmpty()) append(squadraB).append('\n')
            if (pianoReparti.isNotEmpty()) append("Piano del mese: $pianoReparti.")
        }.trim()
    }

    fun aggiungi(vecchi: String?, nuovo: String): String =
        (vecchi.orEmpty().split("§").filter { it.isNotBlank() } + nuovo).takeLast(MAX_REPORT).joinToString("§")
}
