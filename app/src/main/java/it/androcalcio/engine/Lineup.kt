package it.androcalcio.engine

import it.androcalcio.data.Player

/** Formazione titolare: scelta del giocatore o proposta dell'allenatore. */
object Lineup {

    /** Quanti giocatori servono per ruolo con un certo modulo (P, D, C, A). */
    fun needs(f: Formation): Map<String, Int> {
        val d = f.lines.first()
        val a = f.lines.last()
        return mapOf("P" to 1, "D" to d, "C" to f.lines.sum() - d - a, "A" to a)
    }

    private fun score(p: Player, role: String): Double {
        val base = when (role) {
            "P" -> Attr.gk(p); "D" -> Attr.def(p); "C" -> Attr.mid(p); else -> Attr.atk(p)
        }
        // Sotto il 75% di condizione l'allenatore comincia a preferire chi è più fresco
        return base * (0.7 + 0.3 * (p.condition.coerceAtLeast(35) / 100.0))
    }

    /** Peso, nella scelta, di un giocatore messo in un posto che non è il suo ruolo naturale né uno che conosce: resta nel suo ruolo, senza penalità in campo. */
    private const val FUORI_POSTO = 0.95
    /**
     * Affinità (%) da cui l'allenatore assegna il ruolo in campo. Sotto il 100% il motore riduce l'efficacia del giocatore
     * (vedi [TacticalRoles.factor]), quindi l'allenatore non lo fa da solo: chi vuole far imparare un ruolo lo sceglie lui.
     */
    const val AFFINITA_MIN = 100

    /** Ordine di assegnazione dei posti: prima le fasce e le ali (più rare), poi il resto. */
    private fun rarita(codice: String): Int = when (codice) {
        "FD", "FS", "AD", "AS" -> 0
        "TD", "TS", "ES", "MP" -> 1
        else -> 2
    }

    /** Quanto sta bene [p] nel posto [codice]: 1 se è il suo ruolo o non c'è un posto fisso, l'affinità se lo conosce (come il motore), altrimenti poco meno. */
    fun idoneita(p: Player, codice: String): Double {
        if (codice.isEmpty() || p.pos.isEmpty() || p.pos == codice) return 1.0
        val aff = TacticalRoles.options(p).firstOrNull { it.first == codice }?.second ?: return FUORI_POSTO
        return aff / 100.0
    }

    private fun insiemePerRiferimento(): MutableSet<Player> =
        java.util.Collections.newSetFromMap(java.util.IdentityHashMap<Player, Boolean>())

    /**
     * Sceglie un titolare per ogni posto del modulo (stesso reparto del posto) e li restituisce con il codice del posto,
     * nell'ordine di campo; i posti senza candidati sono saltati. [punteggio] dà il valore del giocatore nel reparto.
     * I giocatori si distinguono per riferimento, non per id (nei test gli id possono essere tutti 0).
     */
    fun scegliPerPosti(disponibili: List<Player>, f: Formation, punteggio: (Player, String) -> Double): List<Pair<Player, String>> {
        val codici = f.postiCompleti()
        val reparti = f.repartiPosti()
        val scelti = arrayOfNulls<Player>(codici.size)
        val usati = insiemePerRiferimento()
        for (i in codici.indices.sortedBy { rarita(codici[it]) }) {
            val best = disponibili.filter { it.role == reparti[i] && it !in usati }
                .maxByOrNull { punteggio(it, reparti[i]) * idoneita(it, codici[i]) } ?: continue
            usati += best
            scelti[i] = best
        }
        return codici.indices.mapNotNull { i -> scelti[i]?.let { it to codici[i] } }
    }

    /** Il giocatore nel posto [codice]: ruolo in campo assegnato solo se lo conosce al massimo, altrimenti il suo ruolo naturale. */
    private fun nelPosto(p: Player, codice: String): Player {
        val aff = if (codice.isEmpty() || codice == p.pos) 0 else TacticalRoles.options(p).firstOrNull { it.first == codice }?.second ?: 0
        val ruolo = if (aff >= AFFINITA_MIN) codice else ""
        return if (p.ruoloPartita == ruolo) p else p.copy(ruoloPartita = ruolo, compito = "")
    }

    /**
     * Proposta dell'allenatore: undici nell'ordine dei posti (con il ruolo in campo) e la panchina, cioè chi resta fuori.
     * Pochi giocatori in un reparto: gli undici si completano con i migliori rimasti.
     */
    fun coachPickConPanchina(players: List<Player>, f: Formation): Pair<List<Player>, List<Player>> {
        val avail = players.filter { it.injuredRounds == 0 }
        val scelti = scegliPerPosti(avail, f) { p, r -> score(p, r) }
        val dentro = insiemePerRiferimento().also { set -> scelti.forEach { set += it.first } }
        val xi = scelti.map { (p, codice) -> nelPosto(p, codice) }.toMutableList()
        if (xi.size < 11) {
            val extra = avail.filter { it !in dentro }.sortedByDescending { it.overall }.take(11 - xi.size)
            xi += extra; dentro += extra
        }
        return xi to players.filter { it !in dentro }
    }

    /** Proposta dell'allenatore: i migliori disponibili per ogni posto del modulo, nell'ordine di campo, con il ruolo in campo. */
    fun coachPick(players: List<Player>, f: Formation): List<Player> = coachPickConPanchina(players, f).first

    /**
     * Gli undici nell'ordine dei posti del modulo (portiere, difesa, centrocampo, attacco; da sinistra a destra):
     * in ogni reparto chi ha il ruolo in campo uguale al posto lo prende, gli altri riempiono i posti rimasti per valore.
     */
    fun perPosti(xi: List<Player>, f: Formation): List<Player> {
        val codici = f.postiCompleti()
        val reparti = f.repartiPosti()
        val presi = insiemePerRiferimento()
        val out = ArrayList<Player>(xi.size)
        for (rep in listOf("P", "D", "C", "A")) {
            val posti = codici.indices.filter { reparti[it] == rep }
            val gruppo = xi.filter { it.role == rep }.sortedByDescending { it.overall }.toMutableList()
            gruppo.forEach { presi += it }
            val assegnati = arrayOfNulls<Player>(posti.size)
            posti.forEachIndexed { k, i ->
                val idx = gruppo.indexOfFirst { TacticalRoles.assigned(it) == codici[i] }
                if (idx >= 0) assegnati[k] = gruppo.removeAt(idx)
            }
            for (k in assegnati.indices) if (assegnati[k] == null && gruppo.isNotEmpty()) assegnati[k] = gruppo.removeAt(0)
            out += assegnati.filterNotNull()
            out += gruppo   // più giocatori del reparto che posti: in coda
        }
        out += xi.filter { it !in presi }
        return out
    }

    data class Result(val xi: List<Player>, val notes: List<String>)

    /**
     * Gli 11 che scendono in campo: i titolari scelti, con gli infortunati sostituiti
     * dall'allenatore (stesso ruolo se possibile). Senza formazione scelta decide l'allenatore.
     */
    fun resolve(players: List<Player>, f: Formation): Result {
        val chosen = players.filter { it.starter }
        if (chosen.isEmpty()) return Result(coachPick(players, f), emptyList())
        val xi = chosen.filter { it.injuredRounds == 0 }.take(11).toMutableList()
        val notes = mutableListOf<String>()
        val bench = players.filter { it.injuredRounds == 0 && it !in xi }.toMutableList()
        chosen.filter { it.injuredRounds > 0 }.forEach { out ->
            if (xi.size >= 11) return@forEach
            val sub = bench.filter { it.role == out.role }.maxByOrNull { score(it, out.role) }
                ?: bench.maxByOrNull { it.overall }
            if (sub != null) {
                xi += sub; bench -= sub
                notes += "${out.name} è infortunato: l'allenatore fa giocare ${sub.name}."
            }
        }
        if (xi.size < 11) xi += bench.sortedByDescending { it.overall }.take(11 - xi.size)
        return Result(xi, notes)
    }
}
