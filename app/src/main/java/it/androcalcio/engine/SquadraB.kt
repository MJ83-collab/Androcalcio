package it.androcalcio.engine

/** Squadra B / Under 23 e squadra satellite: giovani della rosa (max 23 anni) che crescono più in fretta. */
object SquadraB {
    const val KEY = "squadra_b"          // "id:livello;..." con livello 0 = squadra B, 1 = satellite
    const val KEY_INVITI = "inviti_amic"  // id delle squadre che ti invitano a un'amichevole
    const val KEY_RIS = "squadra_b_ris"   // ultimi risultati: "gf-gs:avversario|..."
    const val MAX = 8
    const val ETA_MAX = 23
    const val COSTO_SATELLITE = 40_000L   // all'anno, se almeno un giocatore è al satellite

    fun decode(s: String?): Map<Long, Int> = s.orEmpty().split(";").mapNotNull {
        val x = it.split(":"); val k = x.getOrNull(0)?.toLongOrNull(); val v = x.getOrNull(1)?.toIntOrNull()
        if (k != null && v != null) k to v else null
    }.toMap()

    fun encode(m: Map<Long, Int>) = m.entries.joinToString(";") { "${it.key}:${it.value}" }

    /** Probabilità mensile di +1 al livello: il satellite gioca di più e cresce di più. */
    fun probabilita(livello: Int, skillPreparatore: Int, bonusEsito: Double = 0.0) =
        (if (livello == 1) 0.18 else 0.10) + skillPreparatore / 500.0 + bonusEsito

    /** Partita mensile della squadra B, simulata dall'allenatore in seconda: restituisce (gol B, gol avversario). */
    fun partita(forzaB: Double, forzaAvv: Double, skillAssistente: Int, rng: kotlin.random.Random): Pair<Int, Int> {
        val diff = (forzaB - forzaAvv) / 20.0 + (skillAssistente - 50) / 200.0
        return poisson((1.2 + diff).coerceIn(0.3, 3.0), rng) to poisson((1.2 - diff).coerceIn(0.3, 3.0), rng)
    }

    private fun poisson(l: Double, rng: kotlin.random.Random): Int {
        val limite = Math.exp(-l); var k = 0; var p = 1.0
        do { k++; p *= rng.nextDouble() } while (p > limite && k < 10)
        return k - 1
    }

    /** Bonus alla crescita del mese dopo la partita: vittoria +4%, pareggio +2%. */
    fun bonusEsito(gf: Int, gs: Int): Double = if (gf > gs) 0.04 else if (gf == gs) 0.02 else 0.0

    fun aggiungiRisultato(vecchi: String?, nuovo: String, max: Int = 8): String =
        (vecchi.orEmpty().split("|").filter { it.isNotBlank() } + nuovo).takeLast(max).joinToString("|")
}
