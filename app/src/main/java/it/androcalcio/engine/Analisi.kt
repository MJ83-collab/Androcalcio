package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import it.androcalcio.data.Player
import it.androcalcio.data.PlayerStat

/**
 * Analisi della rosa e dei risultati: funzioni pure, nessun dato salvato.
 * Alimentano la scheda "Analisi" della Società.
 */
object Analisi {
    /** Un risultato visto dalla tua squadra. */
    data class Partita(val gf: Int, val gs: Int, val incasa: Boolean) {
        val esito get() = when { gf > gs -> 'V'; gf == gs -> 'N'; else -> 'S' }
        val punti get() = when { gf > gs -> 3; gf == gs -> 1; else -> 0 }
    }

    fun partiteMie(ms: List<MatchResult>, meId: Long): List<Partita> =
        ms.filter { it.played && (it.homeTeamId == meId || it.awayTeamId == meId) }
            .map { if (it.homeTeamId == meId) Partita(it.homeGoals, it.awayGoals, true) else Partita(it.awayGoals, it.homeGoals, false) }

    // 1. Contratti in scadenza (entro un anno), dai più forti
    fun inScadenza(rosa: List<Player>): List<Player> = rosa.filter { it.contract <= 1 }.sortedByDescending { it.overall }

    // 2. Infermeria
    fun infermeria(rosa: List<Player>): List<Player> = rosa.filter { it.injuredRounds > 0 }.sortedByDescending { it.injuredRounds }

    // 3. Stanchi: sotto il 60% di condizione e non infortunati
    fun stanchi(rosa: List<Player>): List<Player> = rosa.filter { it.injuredRounds == 0 && it.condition < 60 }.sortedBy { it.condition }

    // 4-5. Reparti: ingaggi, età media, media e valore
    data class Reparto(val ruolo: String, val n: Int, val ingaggi: Long, val etaMedia: Double, val overallMedio: Double, val valore: Long)

    fun reparti(rosa: List<Player>): List<Reparto> = listOf("P", "D", "C", "A").mapNotNull { r ->
        val l = rosa.filter { it.role == r }
        if (l.isEmpty()) null
        else Reparto(r, l.size, l.sumOf { it.salary }, l.map { it.age }.average(), l.map { it.overall }.average(), l.sumOf { Market.value(it) })
    }

    // 6. Giovani promesse: fino a 21 anni con margine di crescita
    fun promesse(rosa: List<Player>): List<Player> =
        rosa.filter { it.age <= 21 && it.potential - it.overall >= 6 }.sortedByDescending { it.potential }

    // 7. Forma recente: ultime cinque, dalla più vecchia alla più nuova
    fun forma(p: List<Partita>, n: Int = 5): String = p.takeLast(n).joinToString("") { it.esito.toString() }

    // 8. Serie in corso (dalla partita più recente all'indietro)
    data class Serie(val vittorie: Int, val imbattuto: Int, val senzaSubire: Int, val senzaVincere: Int)

    fun serie(p: List<Partita>): Serie {
        val r = p.asReversed()
        return Serie(
            r.takeWhile { it.gf > it.gs }.size,
            r.takeWhile { it.gf >= it.gs }.size,
            r.takeWhile { it.gs == 0 }.size,
            r.takeWhile { it.gf <= it.gs }.size
        )
    }

    // 9. Casa e trasferta
    data class Rendimento(val giocate: Int, val punti: Int, val gf: Int, val gs: Int)

    fun rendimento(p: List<Partita>, incasa: Boolean): Rendimento {
        val l = p.filter { it.incasa == incasa }
        return Rendimento(l.size, l.sumOf { it.punti }, l.sumOf { it.gf }, l.sumOf { it.gs })
    }

    // 10. Marcatori del club
    fun marcatori(stat: List<PlayerStat>, rosa: List<Player>, n: Int = 5): List<Pair<Player, Int>> {
        val byId = rosa.associateBy { it.id }
        return stat.filter { it.gol > 0 && byId.containsKey(it.playerId) }
            .sortedByDescending { it.gol }.take(n).mapNotNull { s -> byId[s.playerId]?.let { it to s.gol } }
    }

    // 15. Media gol fatti e subiti a partita (centesimi per evitare i decimali nei test)
    fun mediaGol(p: List<Partita>): Pair<Double, Double> =
        if (p.isEmpty()) 0.0 to 0.0 else p.sumOf { it.gf }.toDouble() / p.size to p.sumOf { it.gs }.toDouble() / p.size

    // 16. Capitano suggerito: esperienza e qualità, morale alto
    fun capitanoSuggerito(rosa: List<Player>): Player? =
        rosa.filter { it.injuredRounds == 0 && it.role != "P" }.maxByOrNull { it.overall * 2 + it.age.coerceAtMost(34) + it.morale / 4 }

    // 17. Scontenti: morale sotto 40
    fun scontenti(rosa: List<Player>): List<Player> = rosa.filter { it.morale < 40 }.sortedBy { it.morale }

    // 18. Attaccanti a secco: almeno 5 presenze e nessun gol
    fun attaccantiASecco(stat: List<PlayerStat>, rosa: List<Player>): List<Player> {
        val byId = rosa.associateBy { it.id }
        return stat.filter { it.presenze >= 5 && it.gol == 0 }.mapNotNull { byId[it.playerId] }.filter { it.role == "A" }
    }

    // 19. Valore complessivo della rosa
    fun valoreRosa(rosa: List<Player>): Long = rosa.sumOf { Market.value(it) }

    // 20. Rinnovi consigliati: contratto in scadenza e media tra i migliori della rosa
    fun rinnoviConsigliati(rosa: List<Player>): List<Player> {
        if (rosa.isEmpty()) return emptyList()
        val soglia = rosa.map { it.overall }.sorted()[rosa.size * 2 / 3]
        return inScadenza(rosa).filter { it.overall >= soglia }
    }

    // 11. Consigli del vice per la prossima partita
    fun consigli(rosa: List<Player>, squal: Set<Long>, diffidati: Set<Long>): List<String> {
        val titolari = rosa.filter { it.starter }
        val l = ArrayList<String>()
        titolari.filter { it.id in squal }.forEach { l += "${it.name} è squalificato: sostituiscilo." }
        titolari.filter { it.injuredRounds > 0 }.forEach { l += "${it.name} è infortunato: non può giocare." }
        titolari.filter { it.injuredRounds == 0 && it.condition < 60 }.forEach { l += "${it.name} è stanco (${it.condition}%): valuta il turnover." }
        titolari.filter { it.id in diffidati && it.id !in squal }.forEach { l += "${it.name} è diffidato: un giallo e salta la prossima." }
        titolari.filter { it.morale < 40 }.forEach { l += "${it.name} è scontento (morale ${it.morale}): parlagli." }
        if (titolari.isEmpty()) l += "Non hai scelto i titolari: ci pensa l'allenatore in seconda."
        if (l.isEmpty()) l += "Nessun problema: la formazione è in ordine."
        return l
    }

    // 12. Salute finanziaria: peso degli ingaggi sul budget ingaggi, in percentuale
    fun pesoIngaggi(rosa: List<Player>, budgetIngaggi: Long): Int =
        if (budgetIngaggi <= 0L) 100 else (rosa.sumOf { it.salary } * 100L / budgetIngaggi).toInt()

    // 4. Bilancio previsto a fine stagione: budget + media netta per partita × partite rimaste − rate
    fun previsione(budget: Long, netto: Long, giocate: Int, rimanenti: Int, rate: Long): Long =
        budget + (if (giocate <= 0) 0L else netto / giocate * rimanenti) - rate

    /** Riposo automatico: coppie (esce, entra) per i titolari sotto il 60% con un sostituto riposato dello stesso ruolo. */
    fun riposo(rosa: List<Player>, squal: Set<Long>): List<Pair<Player, Player>> {
        val usati = HashSet<Long>()
        val out = ArrayList<Pair<Player, Player>>()
        for (t in rosa.filter { it.starter && it.injuredRounds == 0 && it.condition < 60 }.sortedBy { it.condition }) {
            val c = rosa.filter { !it.starter && it.injuredRounds == 0 && it.id !in squal && it.id !in usati && it.role == t.role && it.condition >= 70 }
                .maxByOrNull { it.overall } ?: continue
            usati += c.id; out += t to c
        }
        return out
    }

    // 13. Prossima rata dei mutui rispetto al budget
    fun rataInArrivo(mutui: List<Mutui.Mutuo>): Long = mutui.sumOf { minOf(it.rata, it.residuo + Mutui.interessi(it)) }
}
