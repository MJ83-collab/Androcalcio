package it.androcalcio.engine

import kotlin.random.Random

/**
 * Telecronaca: racconta la partita e, tra le righe, dà indizi su come vincerla.
 * Il testo dipende dallo stato (possesso, tiri, fatica, punteggio, arbitro), mai dal caso puro.
 */
object Telecronaca {
    /** Situazione vista dalla squadra dell'utente. */
    data class Quadro(
        val minuto: Int, val golMiei: Int, val golLoro: Int, val possessoMio: Int,
        val tiriMiei: Int, val tiriLoro: Int, val energiaMedia: Double, val energiaAvversari: Double,
        val inDieci: Boolean, val avversariInDieci: Boolean, val arbitro: Disciplina.Arbitro,
        val mentalita: Mentality, val cambiLiberi: Int
    )

    private fun <T> Random.scegli(l: List<T>): T = l[nextInt(l.size)]

    /** Commento di fondo della seconda voce, con la sua parlata. */
    fun commento(q: Quadro, rng: Random, c: Coppia): String? = commento(q, rng)?.let { Voci.dici(c.seconda, it, q, rng) }

    /** Un commento di fondo, con un indizio se la situazione lo permette. Null = niente da dire. */
    fun commento(q: Quadro, rng: Random): String? {
        val indizi = mutableListOf<String>()
        if (q.minuto >= 60 && q.golMiei < q.golLoro && q.mentalita != Mentality.OFFENSIVA)
            indizi += "Siamo sotto e il tempo passa: servirebbe più coraggio in avanti, magari una mentalità offensiva."
        if (q.minuto >= 70 && q.golMiei > q.golLoro && q.mentalita == Mentality.OFFENSIVA)
            indizi += "Il vantaggio è prezioso: chiudere gli spazi e proteggerlo potrebbe bastare."
        if (q.possessoMio < 42 && q.tiriMiei >= q.tiriLoro - 1)
            indizi += "Gli avversari tengono la palla ma noi pungiamo in ripartenza: il contropiede sembra la strada giusta."
        if (q.possessoMio > 58 && q.tiriMiei < q.tiriLoro)
            indizi += "Tanto palleggio ma poche conclusioni: serve più verticalità, meno giri palla."
        if (q.energiaMedia < 0.84 && q.cambiLiberi > 0)
            indizi += "Le gambe iniziano a pesare: un cambio fresco potrebbe dare nuova linfa."
        if (q.energiaAvversari < q.energiaMedia - 0.06 && q.minuto > 55)
            indizi += "Gli avversari sono più stanchi di noi: aumentare il ritmo adesso può fare la differenza."
        if (q.inDieci) indizi += "In dieci bisogna stringere le linee e non scoprirsi."
        if (q.avversariInDieci) indizi += "Avversari in dieci: gli spazi aumentano, vale la pena spingere."
        if (q.arbitro.capacita == 1 && q.minuto in 10..80 && rng.nextInt(4) == 0)
            indizi += "L'arbitro sta faticando a tenere in mano la gara: attenzione alle decisioni dubbie."
        if (q.arbitro.severita == 3 && rng.nextInt(4) == 0)
            indizi += "Arbitro severo: ogni intervento duro rischia il giallo."
        if (indizi.isNotEmpty() && rng.nextInt(3) != 0) return rng.scegli(indizi)

        val generici = mutableListOf(
            "Gioco spezzettato a centrocampo, la partita si decide sui dettagli.",
            "Il pubblico spinge: l'atmosfera è elettrica.",
            "Squadre lunghe, ci sono spazi da sfruttare.",
            "Ritmo alto, nessuno vuole lasciare il pallone all'altro.",
            "Si gioca con grande intensità, ogni duello conta."
        )
        if (q.golMiei == q.golLoro) generici += "Il risultato è in equilibrio: il prossimo episodio può cambiare tutto."
        if (q.minuto > 80) generici += "Finale di partita: ogni pallone pesa doppio."
        return rng.scegli(generici)
    }

    /** Reazione a caldo della prima voce (col nome del giocatore) e, a volte, la battuta della seconda voce. */
    fun reazione(e: MatchEvent, nome: String?, c: Coppia, rng: Random): List<String> {
        val prima = Voci.reazione(c.prima, e, nome, rng) ?: return emptyList()
        val seconda = if (e.type == EventType.GOAL || rng.nextInt(3) == 0) Voci.replica(c.seconda, e, nome, rng) else null
        return listOfNotNull("${c.prima.nome}: $prima", seconda?.let { "${c.seconda.nome}: $it" })
    }

    /** Commento a caldo su un evento già successo. */
    fun reazione(e: MatchEvent, rng: Random): String? = when (e.type) {
        EventType.GOAL -> rng.scegli(listOf("Che azione! Il pallone finisce in rete!", "Esplode lo stadio, è gol!", "Perfetta la conclusione, il portiere non può nulla."))
        EventType.SAVE -> rng.scegli(listOf("Gran parata! Il portiere è decisivo.", "Riflessi da campione, il pallone è fuori."))
        EventType.MISS -> rng.scegli(listOf("Che occasione sprecata!", "Di poco a lato, brividi per la difesa."))
        EventType.OFFSIDE -> "Bravi i difensori a salire insieme: fuorigioco."
        EventType.RED -> "Decisione pesante: la partita cambia volto."
        else -> null
    }
}
