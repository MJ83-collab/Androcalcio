package it.androcalcio.engine

/** Squadra B / Under 23 e squadra satellite: giovani della rosa (max 23 anni) che crescono più in fretta. */
object SquadraB {
    const val KEY = "squadra_b"          // "id:livello;..." con livello 0 = squadra B, 1 = satellite
    const val KEY_INVITI = "inviti_amic"  // id delle squadre che ti invitano a un'amichevole
    const val MAX = 8
    const val ETA_MAX = 23
    const val COSTO_SATELLITE = 40_000L   // all'anno, se almeno un giocatore è al satellite

    fun decode(s: String?): Map<Long, Int> = s.orEmpty().split(";").mapNotNull {
        val x = it.split(":"); val k = x.getOrNull(0)?.toLongOrNull(); val v = x.getOrNull(1)?.toIntOrNull()
        if (k != null && v != null) k to v else null
    }.toMap()

    fun encode(m: Map<Long, Int>) = m.entries.joinToString(";") { "${it.key}:${it.value}" }

    /** Probabilità mensile di +1 al livello: il satellite gioca di più e cresce di più. */
    fun probabilita(livello: Int, skillPreparatore: Int) = (if (livello == 1) 0.18 else 0.10) + skillPreparatore / 500.0
}
