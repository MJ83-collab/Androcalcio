package it.androcalcio.engine

import it.androcalcio.data.Player

/** Scambi di giocatori con un altro club, con un conguaglio in denaro (positivo = lo paghi tu). */
object Scambi {
    sealed class Esito {
        data class Ok(val msg: String) : Esito()
        data class No(val msg: String) : Esito()
        data class Contro(val conguaglio: Long) : Esito()
    }

    private fun arrotonda(v: Long): Long = if (v >= 1_000_000L) v / 100_000L * 100_000L else v / 10_000L * 10_000L

    /** Conguaglio minimo che il club vuole: il suo giocatore vale il 5% in più, mai meno di zero. */
    fun conguaglioMinimo(mio: Player, loro: Player): Long =
        arrotonda((Market.value(loro) * 105L / 100L - Market.value(mio)).coerceAtLeast(0L))

    fun valuta(mio: Player, loro: Player, conguaglio: Long): Esito {
        if (loro.role == "P" && mio.role != "P")
            return Esito.No("${loro.name} è il loro portiere: non possono restare senza.")
        if (mio.injuredRounds > 0) return Esito.No("${mio.name} è infortunato: il club non lo vuole.")
        val min = conguaglioMinimo(mio, loro)
        return when {
            conguaglio >= min -> Esito.Ok("Scambio concluso: ${mio.name} per ${loro.name}" +
                (if (conguaglio > 0) " più un conguaglio." else "."))
            conguaglio * 100L >= min * 85L -> Esito.Contro(min)
            else -> Esito.No("Il club rifiuta: ${loro.name} vale molto più di ${mio.name}.")
        }
    }
}
