package it.androcalcio.engine

import it.androcalcio.data.Player

/**
 * Tiri di una squadra: rigoristi, punizionisti e battitori d'angolo, in ordine di priorità, più il capitano scelto.
 * Liste vuote o senza nessuno in campo: decide il capitano in campo.
 */
data class SetPieces(
    val rigori: List<Long> = emptyList(),
    val punizioni: List<Long> = emptyList(),
    val angoli: List<Long> = emptyList(),
    val capitano: Long? = null
) {
    /** Primo della lista presente in campo; altrimenti il capitano in campo, che se esce lo sostituisce il più forte e carismatico. */
    fun tiratore(lista: List<Long>, inCampo: List<Player>): Player? =
        lista.firstNotNullOfOrNull { id -> inCampo.firstOrNull { it.id == id } }
            ?: inCampo.firstOrNull { it.id == capitano }
            ?: Leadership.capitano(inCampo)

    companion object {
        val NONE = SetPieces()
    }
}
