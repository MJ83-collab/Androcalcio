package it.androcalcio.engine

import it.androcalcio.data.Player

/**
 * Tratto "Cinico" (portato al risultato): chi lo ha cerca il gol e lo trova più spesso, soprattutto nei finali di partita.
 * Una parte dei giocatori lo ha dalla nascita (deriva dal nome, come la personalità); gli altri lo possono acquisire
 * con l'allenamento tattico. Gli acquisiti sono salvati nella tabella Stato (chiave "tratti"), senza toccare il database.
 */
object Tratti {
    @Volatile var acquisiti: Set<Long> = emptySet()
    @Volatile var sporco: Boolean = false

    fun innato(p: Player): Boolean = Math.floorMod(p.name.hashCode() * 17 + p.nationality.hashCode(), 6) == 0
    fun cinico(p: Player): Boolean = innato(p) || p.id in acquisiti

    fun acquisisci(id: Long) { if (id !in acquisiti) { acquisiti = acquisiti + id; sporco = true } }

    fun encode(): String = acquisiti.joinToString(";")
    fun decode(s: String?) { acquisiti = s.orEmpty().split(";").mapNotNull { it.toLongOrNull() }.toSet(); sporco = false }

    /** Peso extra tra i possibili tiratori: +20% di tiri tentati rispetto a un compagno di pari qualità. */
    fun pesoTiro(p: Player): Double = if (cinico(p)) 1.2 else 1.0
}
