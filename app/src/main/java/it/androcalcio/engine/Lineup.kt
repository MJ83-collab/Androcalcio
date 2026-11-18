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

    /** Proposta dell'allenatore: i migliori disponibili per ogni ruolo del modulo. */
    fun coachPick(players: List<Player>, f: Formation): List<Player> {
        val avail = players.filter { it.injuredRounds == 0 }
        val xi = mutableListOf<Player>()
        needs(f).forEach { (role, n) ->
            xi += avail.filter { it.role == role && it !in xi }.sortedByDescending { score(it, role) }.take(n)
        }
        // Ruolo scoperto (pochi giocatori): completa con i migliori rimasti
        if (xi.size < 11) xi += avail.filter { it !in xi }.sortedByDescending { it.overall }.take(11 - xi.size)
        return xi
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
