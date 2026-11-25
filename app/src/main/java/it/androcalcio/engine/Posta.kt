package it.androcalcio.engine

/** Casella messaggi: eventi che fermano CONTINUA, in 4 categorie. */
object Posta {
    const val KEY = "posta"
    const val MAX = 60

    enum class Cat(val label: String) {
        PARTITA("Partita e coppe"), MERCATO("Mercato"), GIOCATORI("Giocatori e società"), STAFF("Staff e strutture")
    }

    /** `vai` = nome della schermata da aprire (es. "MERCATO"). */
    data class Messaggio(val giorno: Long, val cat: Cat, val testo: String, val vai: String, val letto: Boolean = false)

    /** Foto dello stato della società, confrontata prima e dopo ogni giorno. */
    data class Snapshot(
        val finestraAperta: Boolean,
        val infortunati: Set<Long>,
        val scadenze: Set<Long>,
        val offerte: Int,
        val richieste: Int,
        val candidati: Int,
        val cantieri: Int,
        val partiteCoppa: Int
    )

    fun diff(prima: Snapshot, dopo: Snapshot, nomi: Map<Long, String>, giorno: Long): List<Messaggio> {
        val out = mutableListOf<Messaggio>()
        fun n(id: Long) = nomi[id] ?: "Un giocatore"
        if (dopo.partiteCoppa > prima.partiteCoppa)
            out += Messaggio(giorno, Cat.PARTITA, "Sorteggio coppe: nuovi turni in calendario.", "CALENDARIO")
        if (dopo.finestraAperta && !prima.finestraAperta)
            out += Messaggio(giorno, Cat.MERCATO, "Si apre la finestra di mercato.", "MERCATO")
        if (!dopo.finestraAperta && prima.finestraAperta)
            out += Messaggio(giorno, Cat.MERCATO, "La finestra di mercato si è chiusa.", "MERCATO")
        for (id in (dopo.infortunati - prima.infortunati).sorted())
            out += Messaggio(giorno, Cat.GIOCATORI, "${n(id)} si è infortunato.", "ROSA")
        for (id in (dopo.scadenze - prima.scadenze).sorted())
            out += Messaggio(giorno, Cat.GIOCATORI, "Contratto in scadenza: ${n(id)}.", "ROSA")
        if (dopo.offerte > prima.offerte)
            out += Messaggio(giorno, Cat.GIOCATORI, "Nuova offerta per un tuo giocatore.", "MERCATO")
        if (dopo.richieste > prima.richieste)
            out += Messaggio(giorno, Cat.GIOCATORI, "Un giocatore ha fatto una richiesta (vedi Società).", "SOCIETA")
        if (dopo.candidati > prima.candidati)
            out += Messaggio(giorno, Cat.STAFF, "Nuovi candidati hanno risposto all'annuncio.", "STAFF")
        if (dopo.cantieri < prima.cantieri)
            out += Messaggio(giorno, Cat.STAFF, "Un cantiere è terminato.", "STRUTTURE")
        return out
    }

    fun encode(l: List<Messaggio>) = l.takeLast(MAX).joinToString("\n") {
        listOf(it.giorno, it.cat.name, it.vai, if (it.letto) 1 else 0, it.testo.replace("\n", " ")).joinToString("|")
    }

    fun decode(s: String?): List<Messaggio> = s.orEmpty().lines().mapNotNull { r ->
        val p = r.split("|", limit = 5)
        val g = p.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
        val c = Cat.values().firstOrNull { it.name == p.getOrNull(1) } ?: return@mapNotNull null
        if (p.size < 5) return@mapNotNull null
        Messaggio(g, c, p[4], p[2], p[3] == "1")
    }
}
