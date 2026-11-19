package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import kotlin.random.Random

/**
 * Coppa nazionale a eliminazione diretta, con il formato della coppa reale del paese:
 * andata e ritorno nelle fasi indicate da twoLegs (la finale è sempre secca), partita secca altrove.
 *
 * Le partite stanno nella tabella `matches`, con league = "COPPA|<nazione>".
 * Codice turno: BASE + stadio * 10 + gamba (0 = andata o partita secca, 1 = ritorno).
 * Stadio 0 = turno preliminare (solo se il numero di squadre non è una potenza di 2), poi 1, 2, ...
 *
 * Parità (dopo andata e ritorno, o in partita secca): passa la squadra con la rosa più forte
 * (media dei valori), a parità il club con id più basso. Così il risultato non cambia se
 * il calcolo viene ripetuto.
 */
object Cup {
    const val BASE = 1000
    private const val STEP = 10

    fun key(nazione: String) = "COPPA|$nazione"
    fun isCup(round: Int) = round >= BASE
    fun stageOf(round: Int) = (round - BASE) / STEP
    /**
     * Andata e ritorno secondo le regole reali della coppa del paese:
     * Italia: solo semifinali. Spagna: turno preliminare e semifinali. Brasile: dagli ottavi (16 squadre)
     * alle semifinali. Inghilterra, Germania, Francia, Argentina: sempre partita secca.
     * La finale è sempre secca (2 squadre).
     */
    fun twoLegs(nazione: String, stage: Int, teamsInStage: Int): Boolean {
        if (teamsInStage <= 2) return false
        return when (nazione) {
            "Italia" -> teamsInStage == 4
            "Spagna" -> stage == 0 || teamsInStage == 4
            "Brasile" -> teamsInStage in 4..16
            else -> false
        }
    }

    /** Nome del turno per la scheda Coppa. */
    fun stageName(stage: Int, teamsInStage: Int): String = when {
        stage == 0 -> "Turno preliminare"
        teamsInStage <= 2 -> "Finale"
        teamsInStage <= 4 -> "Semifinali"
        teamsInStage <= 8 -> "Quarti di finale"
        teamsInStage <= 16 -> "Ottavi di finale"
        else -> "Sedicesimi di finale"
    }

    /**
     * Primo turno. Se le squadre sono più della potenza di 2 inferiore, le prime
     * 2 * (n - potenza) del sorteggio giocano il turno preliminare; le altre passano direttamente.
     */
    fun firstStage(teamIds: List<Long>, season: String, key: String, rng: Random, nazione: String): List<MatchResult> {
        val n = teamIds.size
        if (n < 2) return emptyList()
        val target = Integer.highestOneBit(n)
        val prelim = 2 * (n - target)
        val shuffled = teamIds.shuffled(rng)
        return if (prelim > 0) pairs(shuffled.take(prelim), 0, season, key, nazione)
        else pairs(shuffled, 1, season, key, nazione)
    }

    /**
     * Turno successivo: se l'ultimo stadio è finito, elimina i perdenti e sorteggia i
     * qualificati. Restituisce lista vuota se un turno è ancora in corso o la coppa è finita.
     */
    fun nextStage(
        cup: List<MatchResult>, teams: List<Team>, strength: Map<Long, Double>,
        season: String, key: String, nazione: String, rng: Random
    ): List<MatchResult> {
        if (cup.isEmpty() || cup.any { !it.played }) return emptyList()
        val eliminated = HashSet<Long>()
        cup.groupBy { stageOf(it.round) }.values.forEach { stage ->
            stage.groupBy { setOf(it.homeTeamId, it.awayTeamId) }.values.forEach { tie ->
                val a = tie.first().homeTeamId
                val b = tie.first().awayTeamId
                var ga = 0; var gb = 0
                tie.forEach { m ->
                    if (m.homeTeamId == a) { ga += m.homeGoals; gb += m.awayGoals }
                    else { gb += m.homeGoals; ga += m.awayGoals }
                }
                val aWins = when {
                    ga != gb -> ga > gb
                    else -> {
                        val sa = strength[a] ?: 0.0
                        val sb = strength[b] ?: 0.0
                        if (sa != sb) sa > sb else a < b
                    }
                }
                eliminated += if (aWins) b else a
            }
        }
        val alive = teams.map { it.id }.filter { it !in eliminated }
        if (alive.size <= 1) return emptyList()
        return pairs(alive.shuffled(rng), cup.maxOf { stageOf(it.round) } + 1, season, key, nazione)
    }

    private fun pairs(pool: List<Long>, stage: Int, season: String, key: String, nazione: String): List<MatchResult> {
        val legs = if (twoLegs(nazione, stage, pool.size)) 2 else 1
        return pool.chunked(2).filter { it.size == 2 }.flatMap { (a, b) ->
            val andata = MatchResult(season = season, round = BASE + stage * STEP, homeTeamId = a,
                awayTeamId = b, homeGoals = 0, awayGoals = 0, league = key)
            if (legs == 1) listOf(andata)
            else listOf(andata, MatchResult(season = season, round = BASE + stage * STEP + 1, homeTeamId = b,
                awayTeamId = a, homeGoals = 0, awayGoals = 0, league = key))
        }
    }
}
