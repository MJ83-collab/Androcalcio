package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import kotlin.random.Random

/**
 * Coppa nazionale a eliminazione diretta, con il formato della coppa reale del paese.
 *
 * Le partite stanno nella tabella `matches`, con league = "COPPA|<nazione>".
 * Codice turno: BASE + stadio * 10 + gamba (0 = andata o partita secca, 1 = ritorno).
 * Stadio 0 = turno preliminare (solo se il numero di squadre non è una potenza di 2), poi 1, 2, ...
 *
 * Dopo 90' in parità (partita secca, o ritorno con aggregato pari) si giocano i supplementari
 * (30', i gol si sommano al risultato) e, se serve, i rigori (homePens / awayPens; -1 = non giocati).
 */
object Cup {
    const val BASE = 1000
    private const val STEP = 10
    /** Probabilità che un rigore sia segnato. */
    private const val P_PENALTY = 0.75
    /** Gol medi di una squadra nei supplementari: circa un terzo di una partita. */
    private const val ET_LAMBDA = 0.45

    /** Stadi della supercoppa (sempre dopo gli stadi della coppa). */
    const val SUPER_STAGE = 100
    const val SUPER_FINAL_STAGE = 101
    const val SUPER_ROUND = BASE + SUPER_STAGE * STEP
    const val SUPER_FINAL_ROUND = BASE + SUPER_FINAL_STAGE * STEP

    fun key(nazione: String) = "COPPA|$nazione"
    fun superKey(nazione: String) = "SUPERCOPPA|$nazione"
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
        stage == SUPER_FINAL_STAGE -> "Finale Supercoppa"
        stage == SUPER_STAGE -> if (teamsInStage <= 2) "Supercoppa" else "Semifinali Supercoppa"
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
     * Parità dopo supplementari e rigori: passa chi ha vinto ai rigori; senza rigori, la rosa più forte.
     */
    fun nextStage(
        cup: List<MatchResult>, teams: List<Team>, strength: Map<Long, Double>,
        season: String, key: String, nazione: String, rng: Random
    ): List<MatchResult> {
        if (cup.isEmpty() || cup.any { !it.played }) return emptyList()
        val eliminated = HashSet<Long>()
        cup.filter { stageOf(it.round) < SUPER_STAGE }.groupBy { stageOf(it.round) }.values.forEach { stage ->
            stage.groupBy { setOf(it.homeTeamId, it.awayTeamId) }.values.forEach { tie ->
                val a = tie.first().homeTeamId
                val b = tie.first().awayTeamId
                val w = winnerOfTie(tie, strength)
                eliminated += if (w == a) b else a
            }
        }
        val alive = teams.map { it.id }.filter { it !in eliminated }
        if (alive.size <= 1) return emptyList()
        return pairs(alive.shuffled(rng), cup.maxOf { stageOf(it.round) } + 1, season, key, nazione)
    }

    /** Vincitore di un confronto: aggregato, poi rigori, poi rosa più forte, poi id più basso. */
    fun winnerOfTie(tie: List<MatchResult>, strength: Map<Long, Double>): Long {
        val a = tie.first().homeTeamId
        val b = tie.first().awayTeamId
        val ga = aggregate(tie, a)
        val gb = aggregate(tie, b)
        if (ga != gb) return if (ga > gb) a else b
        penaltyWinner(tie)?.let { return it }
        val sa = strength[a] ?: 0.0
        val sb = strength[b] ?: 0.0
        return if (sa != sb) (if (sa > sb) a else b) else minOf(a, b)
    }

    /** Partita della finale di coppa (l'unica partita dello stadio più alto), se giocata. */
    fun finalMatch(cup: List<MatchResult>): MatchResult? {
        if (cup.isEmpty() || cup.any { !it.played }) return null
        val last = cup.maxOf { stageOf(it.round) }
        return cup.filter { stageOf(it.round) == last }.singleOrNull()
    }

    /**
     * Supercoppa del paese, al termine della stagione. Spagna: final four (semifinali + finale).
     * Altrove: partita secca tra il campione di lega e il vincitore di coppa; se è la stessa squadra,
     * l'avversario è il secondo in campionato.
     * leagueTop: classifica della prima divisione (in ordine).
     */
    fun superFirst(nazione: String, season: String, key: String, leagueTop: List<Long>, cupWinner: Long, cupRunner: Long): List<MatchResult> {
        if (nazione == "Spagna") {
            val four = (leagueTop.take(2) + listOf(cupWinner, cupRunner) + leagueTop.drop(2)).distinct().take(4)
            if (four.size < 4) return emptyList()
            return listOf(single(season, key, SUPER_ROUND, four[0], four[3]), single(season, key, SUPER_ROUND, four[1], four[2]))
        }
        val champion = leagueTop.firstOrNull() ?: return emptyList()
        val opp = if (cupWinner != champion) cupWinner else leagueTop.getOrNull(1) ?: return emptyList()
        return listOf(single(season, key, SUPER_ROUND, champion, opp))
    }

    /** Finale della final four spagnola, tra le vincitrici delle semifinali. */
    fun superFinal(season: String, key: String, w1: Long, w2: Long): MatchResult =
        single(season, key, SUPER_FINAL_ROUND, w1, w2)

    private fun single(season: String, key: String, round: Int, home: Long, away: Long) =
        MatchResult(season = season, round = round, homeTeamId = home, awayTeamId = away,
            homeGoals = 0, awayGoals = 0, league = key)

    /**
     * Risolve una partita giocata in parità: partita secca o ritorno con aggregato pari.
     * Aggiunge i supplementari e, se serve ancora, i rigori. Idempotente: se i rigori ci sono già
     * restituisce m. Restituisce m invariata se la partita non è decisiva o non è in parità.
     */
    fun finalize(m: MatchResult, all: List<MatchResult>, rng: Random): MatchResult {
        if (!m.played || m.homePens >= 0) return m
        val stage = stageOf(m.round)
        val pair = setOf(m.homeTeamId, m.awayTeamId)
        val others = all.filter {
            it.id != m.id && stageOf(it.round) == stage && setOf(it.homeTeamId, it.awayTeamId) == pair
        }
        val secondLeg = (m.round - BASE) % STEP == 1
        if (others.isNotEmpty() && !secondLeg) return m          // andata: si decide al ritorno
        if (aggregate(others + m, m.homeTeamId) != aggregate(others + m, m.awayTeamId)) return m

        val et = m.copy(
            homeGoals = m.homeGoals + poisson(ET_LAMBDA, rng),
            awayGoals = m.awayGoals + poisson(ET_LAMBDA, rng)
        )
        if (aggregate(others + et, et.homeTeamId) != aggregate(others + et, et.awayTeamId)) return et

        val (ph, pa) = shootout(rng)
        return et.copy(homePens = ph, awayPens = pa)
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

    /** Gol totali di una squadra su tutte le gambe di un confronto. */
    private fun aggregate(tie: List<MatchResult>, team: Long): Int =
        tie.sumOf { if (it.homeTeamId == team) it.homeGoals else if (it.awayTeamId == team) it.awayGoals else 0 }

    /** Vincitore ai rigori, se la gamba decisiva li ha giocati. */
    private fun penaltyWinner(tie: List<MatchResult>): Long? {
        val pm = tie.firstOrNull { it.homePens >= 0 && it.awayPens >= 0 } ?: return null
        if (pm.homePens == pm.awayPens) return null
        return if (pm.homePens > pm.awayPens) pm.homeTeamId else pm.awayTeamId
    }

    /** Numero di gol con media lambda (Poisson, metodo di Knuth). */
    private fun poisson(lambda: Double, rng: Random): Int {
        val limit = kotlin.math.exp(-lambda)
        var k = 0
        var p = 1.0
        do {
            k++
            p *= rng.nextDouble()
        } while (p > limit)
        return k - 1
    }

    /** Rigori: 5 a testa, poi a oltranza finché il punteggio non cambia. Serie simulata senza giocatori: il Cinico non conta. */
    private fun shootout(rng: Random): Pair<Int, Int> {
        var h = 0
        var a = 0
        repeat(5) {
            if (rng.nextDouble() < P_PENALTY) h++
            if (rng.nextDouble() < P_PENALTY) a++
        }
        var rounds = 0
        while (h == a && rounds < 100) {
            if (rng.nextDouble() < P_PENALTY) h++
            if (rng.nextDouble() < P_PENALTY) a++
            rounds++
        }
        if (h == a) h++   // sicurezza: la serie non può chiudersi in parità
        return h to a
    }
}
