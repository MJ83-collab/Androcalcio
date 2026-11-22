package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.PlayerStat

/** Premi individuali di fine stagione, calcolati dalle statistiche e dai giocatori. */
object Premi {
    data class Premio(val titolo: String, val playerId: Long, val dettaglio: String)

    fun assegna(stats: List<PlayerStat>, giocatori: Map<Long, Player>): List<Premio> {
        val out = mutableListOf<Premio>()
        fun nome(id: Long) = giocatori[id]?.name ?: "—"
        stats.filter { it.gol > 0 }.maxWithOrNull(compareBy<PlayerStat> { it.gol }.thenByDescending { it.presenze })
            ?.let { out += Premio("Capocannoniere", it.playerId, "${nome(it.playerId)} · ${it.gol} gol") }
        // Miglior giovane: under 22 con più rendimento (gol e presenze)
        stats.filter { (giocatori[it.playerId]?.age ?: 99) <= 21 && it.presenze >= 5 }
            .maxByOrNull { it.gol * 3 + it.presenze + (giocatori[it.playerId]?.overall ?: 0) / 4 }
            ?.let { out += Premio("Miglior giovane", it.playerId, "${nome(it.playerId)} · ${giocatori[it.playerId]?.age} anni") }
        // Miglior portiere: portiere con più presenze e overall più alto
        stats.filter { giocatori[it.playerId]?.role == "P" && it.presenze >= 5 }
            .maxByOrNull { it.presenze + (giocatori[it.playerId]?.overall ?: 0) }
            ?.let { out += Premio("Miglior portiere", it.playerId, "${nome(it.playerId)} · ${it.presenze} presenze") }
        // Giocatore dell'anno: gol + presenze + valore
        stats.filter { it.presenze >= 5 }
            .maxByOrNull { it.gol * 2 + it.presenze / 2 + (giocatori[it.playerId]?.overall ?: 0) }
            ?.let { out += Premio("Giocatore dell'anno", it.playerId, "${nome(it.playerId)} · ${giocatori[it.playerId]?.overall} di valore") }
        return out
    }

    /** Allenatore del mese: l'allenatore con più punti nelle ultime n giornate (ids → punti). */
    fun allenatoreDelMese(punti: Map<Long, Int>): Long? = punti.maxByOrNull { it.value }?.key

    fun encode(l: List<Premio>) = l.joinToString(";") { "${it.titolo}|${it.playerId}|${it.dettaglio}" }
    fun decode(s: String?): List<Premio> = s.orEmpty().split(";").mapNotNull {
        val p = it.split("|")
        if (p.size == 3) Premio(p[0], p[1].toLongOrNull() ?: return@mapNotNull null, p[2]) else null
    }
}
