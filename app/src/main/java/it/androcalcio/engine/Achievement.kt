package it.androcalcio.engine

/** Obiettivi e trofei del giocatore (achievement): si sbloccano una volta sola e restano nel salvataggio. */
object Achievement {
    /** Fotografia della carriera usata per verificare gli sblocchi. */
    data class Snapshot(
        val stagioni: Int = 0, val titoliCampionato: Int = 0, val coppe: Int = 0, val promozioni: Int = 0,
        val vittorie: Int = 0, val golSegnati: Int = 0, val derbyVinti: Int = 0, val migliorieFatte: Int = 0,
        val sponsorAttivi: Int = 0, val budget: Long = 0, val fiducia: Int = 0, val tifosi: Int = 0,
        val obiettiviRaggiunti: Int = 0, val giovaniPromossi: Int = 0, val colloqui: Int = 0, val conferenze: Int = 0,
        val prestiti: Int = 0, val partiteGiocate: Int = 0
    )

    data class Trofeo(val id: String, val nome: String, val descrizione: String, val icona: String, val ok: (Snapshot) -> Boolean)

    val TUTTI: List<Trofeo> = listOf(
        Trofeo("prima_vittoria", "Si comincia", "Vinci la prima partita", "🥇") { it.vittorie >= 1 },
        Trofeo("10_vittorie", "Dieci e lode", "10 vittorie in carriera", "🔟") { it.vittorie >= 10 },
        Trofeo("50_vittorie", "Mezzo secolo", "50 vittorie in carriera", "🏅") { it.vittorie >= 50 },
        Trofeo("100_vittorie", "Cento e non sentirli", "100 vittorie in carriera", "💯") { it.vittorie >= 100 },
        Trofeo("100_gol", "Pioggia di gol", "100 gol segnati in carriera", "⚽") { it.golSegnati >= 100 },
        Trofeo("500_gol", "Cannoniere", "500 gol segnati in carriera", "🎯") { it.golSegnati >= 500 },
        Trofeo("primo_titolo", "Campioni!", "Vinci un campionato", "🏆") { it.titoliCampionato >= 1 },
        Trofeo("tre_titoli", "Dinastia", "Vinci 3 campionati", "👑") { it.titoliCampionato >= 3 },
        Trofeo("coppa", "Coppa in bacheca", "Vinci una coppa nazionale", "🥈") { it.coppe >= 1 },
        Trofeo("promozione", "Si sale", "Ottieni una promozione", "⬆") { it.promozioni >= 1 },
        Trofeo("tre_promozioni", "Scalatore", "Ottieni 3 promozioni", "🧗") { it.promozioni >= 3 },
        Trofeo("derby", "Re del derby", "Vinci un derby", "🔥") { it.derbyVinti >= 1 },
        Trofeo("derby5", "Padroni della città", "Vinci 5 derby", "🏙") { it.derbyVinti >= 5 },
        Trofeo("costruttore", "Cantiere aperto", "Completa 5 migliorie", "🏗") { it.migliorieFatte >= 5 },
        Trofeo("architetto", "Architetto", "Completa 15 migliorie", "🏛") { it.migliorieFatte >= 15 },
        Trofeo("sponsor5", "Maglia piena", "5 sponsor attivi insieme", "💼") { it.sponsorAttivi >= 5 },
        Trofeo("sponsor10", "Maglia sold out", "10 sponsor attivi insieme", "💰") { it.sponsorAttivi >= 10 },
        Trofeo("milionario", "Cassa piena", "Budget oltre 50 milioni", "🏦") { it.budget >= 50_000_000L },
        Trofeo("fiducia", "Cocco del presidente", "Fiducia della dirigenza sopra 90", "🤝") { it.fiducia >= 90 },
        Trofeo("tifosi", "Piazza in delirio", "Umore dei tifosi sopra 90", "📣") { it.tifosi >= 90 },
        Trofeo("obiettivo", "Missione compiuta", "Raggiungi un obiettivo della dirigenza", "✅") { it.obiettiviRaggiunti >= 1 },
        Trofeo("obiettivo3", "Affidabile", "Raggiungi 3 obiettivi", "🎖") { it.obiettiviRaggiunti >= 3 },
        Trofeo("giovani", "Talent scout", "Promuovi 3 giovani dal vivaio", "🌱") { it.giovaniPromossi >= 3 },
        Trofeo("colloqui", "Uomo spogliatoio", "Fai 20 colloqui individuali", "💬") { it.colloqui >= 20 },
        Trofeo("stampa", "Volto noto", "Fai 10 conferenze stampa", "🎤") { it.conferenze >= 10 },
        Trofeo("prestito", "Mercato creativo", "Fai un prestito", "🔁") { it.prestiti >= 1 },
        Trofeo("stagioni5", "Veterano", "Allena per 5 stagioni", "📅") { it.stagioni >= 5 },
        Trofeo("stagioni10", "Una vita in panchina", "Allena per 10 stagioni", "🪑") { it.stagioni >= 10 },
        Trofeo("partite200", "Maratoneta", "200 partite giocate", "🏃") { it.partiteGiocate >= 200 }
    )

    fun sbloccati(s: Snapshot): Set<String> = TUTTI.filter { it.ok(s) }.map { it.id }.toSet()

    fun nuovi(prima: Set<String>, s: Snapshot): List<Trofeo> = TUTTI.filter { it.id !in prima && it.ok(s) }

    fun encode(ids: Set<String>) = ids.sorted().joinToString(",")
    fun decode(s: String?): Set<String> = s.orEmpty().split(",").filter { it.isNotBlank() }.toSet()
}
