package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team

/**
 * Consigli dell'allenatore in seconda: undici titolari in base alla forma di oggi e notizie
 * sul prossimo avversario. Il modulo degli avversari non si conosce in anticipo: lo sceglie
 * la squadra al momento della partita, quindi i consigli parlano di forza, non di schema.
 */
object AssistantCoach {

    /** Consiglio completo: undici, avvisi sull'undici e notizie sull'avversario. */
    data class Advice(
        val assistantName: String?,          // null = nessun allenatore in seconda
        val xi: List<Player>,
        val xiNotes: List<String>,
        val opponent: String?,               // null = nessuna prossima partita di campionato
        val opponentNotes: List<String>,
        val tattica: Stile? = null,          // stile consigliato; null = nessun consiglio
        val tatticaNota: String? = null
    )

    /**
     * Stile consigliato contro l'avversario: attacco se siamo più forti, difesa se siamo più deboli.
     * A forze pari resta la tattica del nostro allenatore. Stessa forza usata per le notizie.
     */
    fun tattica(
        theirs: List<Player>, mine: List<Player>, oppId: Long,
        oppCoach: Int, myCoach: Int, f: Formation, mioStile: Stile?
    ): Pair<Stile, String>? {
        val t = theirs.filter { it.injuredRounds == 0 }
        val m = mine.filter { it.injuredRounds == 0 }
        if (t.isEmpty() || m.isEmpty() || mioStile == null) return null
        val o = MatchEngine.ratings(t, schemaAbituale(oppId), oppCoach)
        val me = MatchEngine.ratings(m, f, myCoach)
        val q = (me.atk + me.mid + me.def) / (o.atk + o.mid + o.def)
        return when {
            q > 1.05 -> Stile.ATTACCO to "Siamo più forti: tattica d'attacco, alziamo la squadra e pressiamo."
            q < 0.95 -> Stile.DIFESA to "Sono più forti: tattica di difesa, linea bassa e ripartenze."
            else -> mioStile to "Forze pari: resta sulla tattica del tuo allenatore (${mioStile.label.lowercase()})."
        }
    }

    /** Punteggio per la scelta dei titolari: qualità del ruolo, morale, ruolo in campo e condizione di oggi. */
    fun score(p: Player): Double {
        val base = when (p.role) { "P" -> Attr.gk(p); "D" -> Attr.def(p); "C" -> Attr.mid(p); else -> Attr.atk(p) }
        val condizione = 0.7 + 0.3 * p.condition.coerceIn(0, 100) / 100.0
        return base * TacticalRoles.factor(p) * MatchEngine.moraleFactor(p) * condizione
    }

    /** Undici per il modulo: infortunati esclusi, poi per ruolo; chi è in forma passa avanti. */
    fun startingXI(players: List<Player>, f: Formation): List<Player> {
        val avail = players.filter { it.injuredRounds == 0 }
        val xi = mutableListOf<Player>()
        Lineup.needs(f).forEach { (role, n) ->
            xi += avail.filter { it.role == role && it !in xi }.sortedByDescending { score(it) }.take(n)
        }
        if (xi.size < 11) xi += avail.filter { it !in xi }.sortedByDescending { score(it) }.take(11 - xi.size)
        return xi
    }

    /** Avvisi sull'undici: per ogni titolare stanco, il sostituto consigliato dello stesso ruolo. */
    fun xiNotes(players: List<Player>, xi: List<Player>): List<String> {
        val out = mutableListOf<String>()
        xi.filter { it.condition < 60 }.forEach { p ->
            val sost = players.filter { it.injuredRounds == 0 && it !in xi && it.role == p.role }
                .maxByOrNull { score(it) }
            out += if (sost != null)
                "${p.name} è stanco (condizione ${p.condition}): meglio ${sost.name} dall'inizio."
            else "${p.name} è stanco (condizione ${p.condition}), ma non ci sono alternative del suo ruolo."
        }
        val fuori = players.filter { it.injuredRounds > 0 }
        if (fuori.isNotEmpty()) out += "Fuori per infortunio: ${fuori.joinToString { it.name }}."
        return out
    }

    /** Notizie sull'avversario, confrontate con la nostra squadra. Usa la stessa forza che usa il motore. */
    fun opponentNotes(
        opp: Team, theirs: List<Player>, mine: List<Player>,
        oppCoach: Int, myCoach: Int, f: Formation
    ): List<String> {
        val out = mutableListOf<String>()
        val t = theirs.filter { it.injuredRounds == 0 }
        val m = mine.filter { it.injuredRounds == 0 }
        if (t.isEmpty() || m.isEmpty()) return listOf("Non ci sono abbastanza giocatori per un confronto.")

        val schema = schemaAbituale(opp.id)
        val o = MatchEngine.ratings(t, schema, oppCoach)
        val me = MatchEngine.ratings(m, f, myCoach)

        t.filter { it.role == "A" }.maxByOrNull { Attr.atk(it) }?.let {
            out += "Il loro uomo più pericoloso è ${it.name} (overall ${it.overall})."
        }
        t.filter { it.role == "P" }.maxByOrNull { Attr.gk(it) }?.let {
            out += "Il portiere è ${it.name} (overall ${it.overall})."
        }
        if (o.atk > me.def * 1.05) out += "Il loro attacco è superiore alla tua difesa: tieni la linea compatta e marca stretto."
        if (o.mid > me.mid * 1.05) out += "Il loro centrocampo domina: con una mentalità Equilibrata o Difensiva eviti di esporti."
        if (me.mid > o.mid * 1.05) out += "Il tuo centrocampo è superiore: puoi alzare il pressing per recuperare palloni."
        if (o.stamina < me.stamina - 5) out += "Hanno meno fiato di te: spingi dopo il 60'."
        else if (o.stamina > me.stamina + 5) out += "Hanno più resistenza di te: evita ritmi alti negli ultimi venti minuti."
        val formaMedia = t.map { it.fo }.average()
        if (formaMedia < 75) out += "Arrivano con una forma piuttosto bassa (media ${formaMedia.toInt()})."
        val infortunati = theirs.count { it.injuredRounds > 0 }
        if (infortunati > 0) out += "Hanno $infortunati infortunati."
        out += "Di solito ${opp.shortName} gioca con il ${schema.label}, ma può cambiare modulo durante la partita."
        out += schemaTips(schema)
        return out
    }

    /** Consigli in base allo schema abituale dell'avversario. */
    private fun schemaTips(s: Formation): List<String> {
        val out = mutableListOf<String>()
        if (s.lines.size == 3 && s.lines.last() >= 3) out += "Con tre punte attaccano sulle ripartenze: copri bene le fasce."
        if (s.lines.size == 3 && s.lines[1] >= 5) out += "Hanno cinque a centrocampo: allarga il gioco sulle fasce."
        if (s.lines.size == 3 && s.lines[0] == 5) out += "Con cinque in difesa si chiudono: cerca il tiro da fuori area."
        if (s.lines.size == 3 && s.lines[0] == 3) out += "Difesa a tre: attenzione alle sovrapposizioni sulle fasce."
        if (s.lines.size == 4) out += "Con il doppio mediano coprono bene il centro: cerca gli inserimenti dei trequartisti."
        return out
    }
}
