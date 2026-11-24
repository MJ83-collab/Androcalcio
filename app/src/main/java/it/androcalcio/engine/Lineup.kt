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

    /** Peso di un giocatore messo in un posto che non è il suo ruolo naturale né uno che conosce (solo per la scelta). */
    private const val FUORI_POSTO = 0.85
    /** Sotto questa affinità (%) l'allenatore non assegna il ruolo in campo: il giocatore resta nel suo ruolo naturale. */
    const val AFFINITA_MIN = 70

    /** Ordine di assegnazione dei posti: prima le fasce e le ali (più rare), poi il resto. */
    private fun rarita(codice: String): Int = when (codice) {
        "FD", "FS", "AD", "AS" -> 0
        "TD", "TS", "ES", "MP" -> 1
        else -> 2
    }

    /** Quanto sta bene [p] nel posto [codice]: 1 se è il suo ruolo o non c'è un posto fisso, l'affinità se lo conosce, altrimenti poco meno. */
    fun idoneita(p: Player, codice: String): Double {
        if (codice.isEmpty() || p.pos.isEmpty() || p.pos == codice) return 1.0
        val aff = TacticalRoles.options(p).firstOrNull { it.first == codice }?.second ?: return FUORI_POSTO
        return 0.7 + 0.3 * aff / 100.0
    }

    /**
     * Sceglie un titolare per ogni posto del modulo (stesso reparto del posto) e li restituisce con il codice del posto,
     * nell'ordine di campo; i posti senza candidati sono saltati. [punteggio] dà il valore del giocatore nel reparto.
     */
    fun scegliPerPosti(disponibili: List<Player>, f: Formation, punteggio: (Player, String) -> Double): List<Pair<Player, String>> {
        val codici = f.postiCompleti()
        val reparti = f.repartiPosti()
        val scelti = arrayOfNulls<Player>(codici.size)
        val usati = HashSet<Long>()
        for (i in codici.indices.sortedBy { rarita(codici[it]) }) {
            val best = disponibili.filter { it.role == reparti[i] && it.id !in usati }
                .maxByOrNull { punteggio(it, reparti[i]) * idoneita(it, codici[i]) } ?: continue
            usati += best.id
            scelti[i] = best
        }
        return codici.indices.mapNotNull { i -> scelti[i]?.let { it to codici[i] } }
    }

    /** Il giocatore nel posto [codice]: ruolo in campo assegnato solo se lo conosce abbastanza, altrimenti il suo ruolo naturale. */
    private fun nelPosto(p: Player, codice: String): Player {
        val aff = if (codice.isEmpty() || codice == p.pos) 0 else TacticalRoles.options(p).firstOrNull { it.first == codice }?.second ?: 0
        val ruolo = if (aff >= AFFINITA_MIN) codice else ""
        return if (p.ruoloPartita == ruolo) p else p.copy(ruoloPartita = ruolo, compito = "")
    }

    /** Proposta dell'allenatore: i migliori disponibili per ogni posto del modulo, nell'ordine di campo, con il ruolo in campo. */
    fun coachPick(players: List<Player>, f: Formation): List<Player> {
        val avail = players.filter { it.injuredRounds == 0 }
        val xi = scegliPerPosti(avail, f) { p, r -> score(p, r) }.map { (p, codice) -> nelPosto(p, codice) }.toMutableList()
        // Ruolo scoperto (pochi giocatori): completa con i migliori rimasti
        if (xi.size < 11) xi += avail.filter { a -> xi.none { it.id == a.id } }.sortedByDescending { it.overall }.take(11 - xi.size)
        return xi
    }

    /**
     * Gli undici nell'ordine dei posti del modulo (portiere, difesa, centrocampo, attacco; da sinistra a destra):
     * in ogni reparto chi ha il ruolo in campo uguale al posto lo prende, gli altri riempiono i posti rimasti per valore.
     */
    fun perPosti(xi: List<Player>, f: Formation): List<Player> {
        val codici = f.postiCompleti()
        val reparti = f.repartiPosti()
        val resto = xi.toMutableList()
        val out = ArrayList<Player>(xi.size)
        for (rep in listOf("P", "D", "C", "A")) {
            val posti = codici.indices.filter { reparti[it] == rep }
            val gruppo = resto.filter { it.role == rep }.sortedByDescending { it.overall }.toMutableList()
            resto.removeAll(gruppo)
            val assegnati = arrayOfNulls<Player>(posti.size)
            posti.forEachIndexed { k, i ->
                val g = gruppo.firstOrNull { TacticalRoles.assigned(it) == codici[i] } ?: return@forEachIndexed
                assegnati[k] = g; gruppo.remove(g)
            }
            for (k in assegnati.indices) if (assegnati[k] == null && gruppo.isNotEmpty()) assegnati[k] = gruppo.removeAt(0)
            out += assegnati.filterNotNull()
            out += gruppo   // più giocatori del reparto che posti: in coda
        }
        out += resto
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
