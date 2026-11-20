package it.androcalcio.engine

/**
 * Fama degli allenatori (0..100). La tua fama è la reputazione di allenatore; quella degli allenatori
 * delle altre squadre viene aggiornata a fine stagione in base al piazzamento.
 */
object Allenatori {
    const val KEY = "fama_allenatori"
    const val FAMA_INIZIALE = 30

    /** Variazione di fama per una partita: vittoria +2, pareggio +1, sconfitta -1. */
    fun dallaPartita(gf: Int, gs: Int): Int = when {
        gf > gs -> 2
        gf == gs -> 1
        else -> -1
    }

    /** Fiducia della dirigenza per partita: vittoria +1, pareggio 0, sconfitta -1. */
    fun fiduciaPartita(gf: Int, gs: Int): Int = when {
        gf > gs -> 1
        gf == gs -> 0
        else -> -1
    }

    /** Sconto sul prezzo del club con reputazione alta: fino al 10% con reputazione 100. */
    fun scontoClub(reputazione: Int): Double = 1.0 - 0.10 * reputazione.coerceIn(0, 100) / 100.0

    /** Tolleranza sull'ingaggio del giocatore: con reputazione alta accetta una quota un po' più bassa (fino a +5 punti). */
    fun tolleranzaConReputazione(base: Double, reputazione: Int): Double =
        (base + 0.05 * reputazione.coerceIn(0, 100) / 100.0).coerceAtMost(1.0)

    /** Variazione di fama a fine stagione: buon piazzamento su, zona bassa giù, promozione e retrocessione pesano. */
    fun variazioneStagione(posizione: Int, squadre: Int, promossa: Boolean, retrocessa: Boolean): Int {
        var d = when {
            posizione <= 3 -> 5
            posizione <= squadre / 3 -> 2
            posizione > squadre - squadre / 4 -> -2
            else -> 0
        }
        if (promossa) d += 4
        if (retrocessa) d -= 4
        return d
    }

    fun clamp(v: Int): Int = v.coerceIn(0, 100)

    /** Voce salvata: id della squadra, nome dell'allenatore, fama. */
    data class Voce(val teamId: Long, val nome: String, val fama: Int)

    /** Riga della classifica degli allenatori. */
    data class Riga(val nome: String, val squadra: String, val fama: Int, val mio: Boolean)

    fun encode(voci: Collection<Voce>): String =
        voci.joinToString("\n") { "${it.teamId}|${it.nome}|${it.fama}" }

    fun decode(s: String?): Map<Long, Voce> {
        if (s.isNullOrBlank()) return emptyMap()
        return s.lines().mapNotNull { riga ->
            val p = riga.split("|")
            if (p.size != 3) return@mapNotNull null
            val id = p[0].toLongOrNull() ?: return@mapNotNull null
            val fama = p[2].toIntOrNull() ?: return@mapNotNull null
            id to Voce(id, p[1], fama)
        }.toMap()
    }
}
