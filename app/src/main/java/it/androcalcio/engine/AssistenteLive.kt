package it.androcalcio.engine

import it.androcalcio.data.Player

/**
 * Il vice dell'allenatore durante la partita: guarda cosa succede e ricorda cosa fare.
 * Tre livelli: ATTIVO (consiglia spesso), DISCRETO (solo le cose importanti), MUTO.
 */
object AssistenteLive {
    enum class Livello(val label: String, val minUrgenza: Int) {
        ATTIVO("Attivo", 1), DISCRETO("Discreto", 3), MUTO("Muto", 99)
    }

    /** Cosa può fare il pulsante "Applica" del consiglio. */
    sealed class Azione {
        data class Mentalita(val m: Mentality) : Azione()
        data class Contropiede(val on: Boolean) : Azione()
        data class Ritmo(val r: it.androcalcio.engine.Ritmo) : Azione()
        data class Cambio(val esce: Player) : Azione()
    }

    /** urgenza: 1 = suggerimento, 2 = importante, 3 = urgente. [id] evita di ripetere lo stesso consiglio. */
    data class Consiglio(val id: String, val testo: String, val urgenza: Int, val azione: Azione? = null, val minuto: Int = 0)

    const val PAUSA_MINUTI = 4

    /**
     * Il consiglio più urgente per ora, oppure null. [visti] sono gli id già mostrati,
     * [ultimoMinuto] il minuto dell'ultimo consiglio (per non parlare di continuo).
     */
    fun consiglia(m: LiveMatch, mioLatoCasa: Boolean, livello: Livello, visti: Set<String>, ultimoMinuto: Int): Consiglio? {
        if (livello == Livello.MUTO || m.finished) return null
        if (ultimoMinuto >= 0 && m.minute - ultimoMinuto < PAUSA_MINUTI && m.minute > 1) return null
        val mio = if (mioLatoCasa) m.xiH else m.xiA
        val loro = if (mioLatoCasa) m.xiA else m.xiH
        val golMiei = if (mioLatoCasa) m.hg else m.ag
        val golLoro = if (mioLatoCasa) m.ag else m.hg
        val ment = if (mioLatoCasa) m.mH else m.mA
        val cambiLiberi = MatchEngine.MAX_SUBS - (if (mioLatoCasa) m.subsH else m.subsA)
        val contro = if (mioLatoCasa) m.contropiedeH else m.contropiedeA
        val ritmo = if (mioLatoCasa) m.ritmoH else m.ritmoA
        val possesso = if (mioLatoCasa) m.possessoCasa else 100 - m.possessoCasa
        val c = mutableListOf<Consiglio>()
        fun add(id: String, testo: String, urg: Int, a: Azione? = null) { if (id !in visti) c += Consiglio(id, testo, urg, a, m.minute) }

        if (m.minute <= 2) add("arbitro", m.arbitro.consiglio, 1)
        if (mio.size < 11) add("dieci", "Siamo in ${mio.size}: stringi le linee e non scoprirti, meglio una mentalità difensiva.", 3,
            if (ment != Mentality.DIFENSIVA) Azione.Mentalita(Mentality.DIFENSIVA) else null)
        if (loro.size < 11 && m.minute > 5) add("loro-dieci", "Gli avversari sono in ${loro.size}: ci sono spazi, vale la pena spingere.", 2,
            if (ment != Mentality.OFFENSIVA) Azione.Mentalita(Mentality.OFFENSIVA) else null)
        if (m.minute >= 60 && golMiei < golLoro && ment != Mentality.OFFENSIVA)
            add("sotto-${m.minute / 15}", "Siamo sotto e restano ${90 - m.minute} minuti: serve coraggio, passa all'attacco.", 3, Azione.Mentalita(Mentality.OFFENSIVA))
        if (m.minute >= 75 && golMiei > golLoro && ment != Mentality.DIFENSIVA)
            add("avanti", "Siamo avanti: proteggiamo il risultato, mentalità difensiva.", 2, Azione.Mentalita(Mentality.DIFENSIVA))
        val stanco = mio.filter { it.role != "P" }.minByOrNull { m.energyOf(it) }
        if (stanco != null && cambiLiberi > 0) {
            val e = (m.energyOf(stanco) * 100).toInt()
            if (e < 70) add("stanco-${stanco.id}", "${stanco.name} è finito (${e}%): cambialo subito.", 3, Azione.Cambio(stanco))
            else if (e < 82) add("stanco2-${stanco.id}", "${stanco.name} è stanco (${e}%): pensa a un cambio.", 2, Azione.Cambio(stanco))
        }
        if (m.minute > 70 && cambiLiberi == MatchEngine.MAX_SUBS) add("nessun-cambio", "Non hai ancora fatto cambi: la panchina può dare energie fresche.", 2)
        if (possesso < 42 && m.minute in 15..80 && !contro)
            add("contro", "Loro hanno più palla: aspettiamoli e ripartiamo, attiva il contropiede.", 1, Azione.Contropiede(true))
        if (m.minute in 50..80 && golMiei == golLoro && ritmo == Ritmo.NORMALE &&
            mio.filter { it.role != "P" }.map { m.energyOf(it) }.average() > loro.filter { it.role != "P" }.map { m.energyOf(it) }.average() + 0.05)
            add("ritmo", "Siamo più freschi di loro: alza il ritmo e li mettiamo in difficoltà.", 2, Azione.Ritmo(Ritmo.VELOCE))
        val ammoniti = m.events.filter { it.type == EventType.YELLOW && it.home == mioLatoCasa }.map { it.playerId }.toSet()
        mio.firstOrNull { it.id in ammoniti && it.role in setOf("D", "C") }?.let {
            if (cambiLiberi > 0 && m.minute > 30) add("giallo-${it.id}", "${it.name} è ammonito: un altro fallo e rischiamo il rosso.", 2, Azione.Cambio(it))
        }
        if (m.minute == 45) add("intervallo", "Fine primo tempo: ${golMiei}-${golLoro}. Rivedi tattica e condizione prima di ripartire.", 1)

        return c.filter { it.urgenza >= livello.minUrgenza }.maxByOrNull { it.urgenza }
    }
}
