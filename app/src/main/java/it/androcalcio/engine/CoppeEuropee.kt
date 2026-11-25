package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import kotlin.random.Random

/**
 * Coppe europee di fantasia (v0.7), per i campionati europei del gioco.
 *
 * - Coppa dei Campioni d'Europa: 32 squadre, 8 gironi da 4 (andata e ritorno), poi ottavi, quarti e semifinali
 *   con andata e ritorno e finale secca.
 * - Coppa Europa: 16 squadre, subito eliminazione diretta (andata e ritorno, finale secca).
 * - Coppa delle Coppe: 16 squadre, le vincitrici delle coppe nazionali e le finaliste dei cinque paesi maggiori.
 *
 * Le partite stanno nella tabella `matches` con league = "EUROPA|<sigla>". Il codice turno è quello della coppa
 * nazionale (Cup.BASE + stadio x 10 + gamba) con stadi alti, così si riusano supplementari e rigori di Cup:
 * gironi = stadi 200..205, ottavi 210, quarti 211, semifinali 212, finale 213.
 */
object CoppeEuropee {
    enum class Coppa(val sigla: String, val nome: String, val squadre: Int, val conGironi: Boolean) {
        CAMPIONI("CAMPIONI", "Coppa dei Campioni d'Europa", 32, true),
        EUROPA("EUROPA", "Coppa Europa", 16, false),
        COPPE("COPPE", "Coppa delle Coppe", 16, false)
    }

    const val STAGE_GIRONI = 200      // 200..205: sei giornate dei gironi
    const val STAGE_OTTAVI = 210
    const val STAGE_QUARTI = 211
    const val STAGE_SEMI = 212
    const val STAGE_FINALE = 213
    private const val STEP = 10

    fun key(c: Coppa) = "EUROPA|${c.sigla}"
    fun isEuropea(league: String) = league.startsWith("EUROPA|")
    fun round(stage: Int, leg: Int = 0) = Cup.BASE + stage * STEP + leg
    fun inGironi(round: Int) = Cup.stageOf(round) in STAGE_GIRONI..STAGE_GIRONI + 5

    /** Nome del turno per le schermate. */
    fun nomeTurno(stage: Int): String = when (stage) {
        in STAGE_GIRONI..STAGE_GIRONI + 5 -> "Girone, giornata ${stage - STAGE_GIRONI + 1}"
        STAGE_OTTAVI -> "Ottavi di finale"
        STAGE_QUARTI -> "Quarti di finale"
        STAGE_SEMI -> "Semifinali"
        STAGE_FINALE -> "Finale"
        else -> "Turno"
    }

    // ---------------------------------------------------------------------------------------------
    // Posti
    // ---------------------------------------------------------------------------------------------

    /** Prima divisione di ogni nazione europea del gioco. */
    val PRIME_DIVISIONI = mapOf(
        "Italia" to "Serie A", "Spagna" to "Primera", "Inghilterra" to "Premier", "Germania" to "Bundesliga",
        "Francia" to "Ligue 1", "Olanda" to "Eredivisie", "Portogallo" to "Primeira Liga", "Belgio" to "Pro League",
        "Turchia" to "Super Lig", "Scozia" to "Premiership", "Grecia" to "Super League"
    )
    val GRANDI = setOf("Italia", "Spagna", "Inghilterra", "Germania", "Francia")

    /** Posti in Coppa dei Campioni: 22 dai cinque paesi maggiori e 10 dagli altri, 32 in tutto. */
    val POSTI_CAMPIONI = mapOf(
        "Italia" to 4, "Germania" to 4, "Francia" to 4, "Spagna" to 5, "Inghilterra" to 5,
        "Olanda" to 2, "Portogallo" to 2, "Belgio" to 2, "Turchia" to 2, "Scozia" to 1, "Grecia" to 1
    )

    /**
     * Esito europeo di una nazione. [classifica] = id della prima divisione in ordine di piazzamento;
     * [vincitrice] e [finalista] = le squadre della finale di coppa nazionale (anche di una serie inferiore).
     * Campioni ai primi in classifica; Coppa delle Coppe a vincitrice e finalista (due posti nei paesi maggiori,
     * uno negli altri); Coppa Europa ai successivi (due posti nei paesi maggiori, uno negli altri).
     * Una squadra gioca una sola coppa: i posti non presi da vincitrice e finalista scendono in classifica.
     */
    fun qualificate(nazione: String, classifica: List<Long>, vincitrice: Long?, finalista: Long?): Map<Long, Coppa> {
        val posti = POSTI_CAMPIONI[nazione] ?: return emptyMap()
        val grande = nazione in GRANDI
        val out = LinkedHashMap<Long, Coppa>()
        classifica.take(posti).forEach { out[it] = Coppa.CAMPIONI }
        val slotCoppe = if (grande) 2 else 1
        var coppe = 0
        listOfNotNull(vincitrice, finalista).forEach { id ->
            if (coppe < slotCoppe && id !in out) { out[id] = Coppa.COPPE; coppe++ }
        }
        classifica.filter { it !in out }.take(if (grande) 2 else 1).forEach { out[it] = Coppa.EUROPA }
        classifica.filter { it !in out }.take(slotCoppe - coppe).forEach { out[it] = Coppa.COPPE }
        return out
    }

    /** Qualificate della prima stagione (nessuna classifica precedente): le più forti di ogni prima divisione. */
    fun qualificateIniziali(squadre: List<Team>, forza: Map<Long, Double>): Map<Long, Coppa> {
        val out = LinkedHashMap<Long, Coppa>()
        PRIME_DIVISIONI.forEach { (nazione, campionato) ->
            val classifica = squadre.filter { it.nazione == nazione && it.campionato == campionato }
                .sortedByDescending { forza[it.id] ?: 0.0 }.map { it.id }
            out.putAll(qualificate(nazione, classifica, null, null))
        }
        return out
    }

    /** "CAMPIONI:1,2,3;EUROPA:4,5;COPPE:6" */
    fun encodeQualificate(q: Map<Long, Coppa>): String =
        Coppa.values().joinToString(";") { c -> c.sigla + ":" + q.filterValues { it == c }.keys.joinToString(",") }

    fun decodeQualificate(s: String?): Map<Coppa, List<Long>> {
        val out = Coppa.values().associateWith { emptyList<Long>() }.toMutableMap()
        s.orEmpty().split(";").forEach { part ->
            val x = part.split(":")
            val c = Coppa.values().firstOrNull { it.sigla == x.getOrNull(0) } ?: return@forEach
            out[c] = x.getOrNull(1).orEmpty().split(",").mapNotNull { it.toLongOrNull() }
        }
        return out
    }

    // ---------------------------------------------------------------------------------------------
    // Sorteggi e calendari
    // ---------------------------------------------------------------------------------------------

    /** Lettera del girone (0 = A). */
    fun lettera(i: Int): String = ('A' + i).toString()

    /** "A:1,2,3,4;B:5,6,7,8" */
    fun encodeGironi(g: List<List<Long>>): String = g.mapIndexed { i, ids -> lettera(i) + ":" + ids.joinToString(",") }.joinToString(";")

    fun decodeGironi(s: String?): List<List<Long>> = s.orEmpty().split(";").mapNotNull { p ->
        val ids = p.substringAfter(":", "").split(",").mapNotNull { it.toLongOrNull() }
        ids.takeIf { it.isNotEmpty() }
    }

    /** Toglie le squadre più deboli perché il numero sia multiplo di [m]. */
    private fun multiplo(ids: List<Long>, forza: Map<Long, Double>, m: Int): List<Long> {
        val resto = ids.size % m
        if (resto == 0) return ids
        val fuori = ids.sortedBy { forza[it] ?: 0.0 }.take(resto).toSet()
        return ids.filter { it !in fuori }
    }

    /**
     * Sorteggio dei gironi: quattro urne per forza (le più forti nella prima), una squadra per urna in ogni girone,
     * mai due squadre della stessa nazione nello stesso girone, se possibile.
     */
    fun sorteggiaGironi(ids: List<Long>, nazione: Map<Long, String>, forza: Map<Long, Double>, rng: Random): List<List<Long>> {
        val tutte = multiplo(ids, forza, 4)
        if (tutte.size < 8) return emptyList()
        val n = tutte.size / 4
        val urne = tutte.sortedByDescending { forza[it] ?: 0.0 }.chunked(n)
        fun tenta(vincolo: Boolean): List<List<Long>>? {
            val gironi = List(n) { ArrayList<Long>() }
            for (urna in urne) {
                var scelta: List<Long>? = null
                for (t in 0 until 200) {
                    val p = urna.shuffled(rng)
                    if (!vincolo || p.indices.all { i -> gironi[i].none { nazione[it] == nazione[p[i]] } }) { scelta = p; break }
                }
                if (scelta == null) return null
                scelta.forEachIndexed { i, id -> gironi[i] += id }
            }
            return gironi
        }
        repeat(50) { tenta(true)?.let { return it } }
        return tenta(false)!!
    }

    /** Sei giornate per girone: andata e ritorno tra le quattro squadre. */
    fun partiteGironi(gironi: List<List<Long>>, season: String, key: String): List<MatchResult> =
        gironi.flatMap { ids ->
            Fixtures.generate(ids, season, key).map { it.copy(round = round(STAGE_GIRONI + it.round - 1)) }
        }

    /** Confronti a eliminazione: andata e ritorno, tranne la finale che è secca. */
    private fun confronti(coppie: List<Pair<Long, Long>>, stage: Int, season: String, key: String): List<MatchResult> =
        coppie.flatMap { (a, b) ->
            val andata = MatchResult(season = season, round = round(stage, 0), homeTeamId = a, awayTeamId = b,
                homeGoals = 0, awayGoals = 0, league = key)
            if (stage == STAGE_FINALE) listOf(andata)
            else listOf(andata, MatchResult(season = season, round = round(stage, 1), homeTeamId = b, awayTeamId = a,
                homeGoals = 0, awayGoals = 0, league = key))
        }

    /** Primo turno delle coppe senza gironi: 16 squadre agli ottavi, abbinate a caso (le più forti non si incontrano subito). */
    fun primoTurno(ids: List<Long>, forza: Map<Long, Double>, season: String, key: String, rng: Random): List<MatchResult> {
        val tutte = multiplo(ids, forza, 2)
        if (tutte.size < 4) return emptyList()
        val ordinate = tutte.sortedByDescending { forza[it] ?: 0.0 }
        val teste = ordinate.take(tutte.size / 2).shuffled(rng)
        val altre = ordinate.drop(tutte.size / 2).shuffled(rng)
        val stage = when (tutte.size) { 2 -> STAGE_FINALE; in 3..4 -> STAGE_SEMI; in 5..8 -> STAGE_QUARTI; else -> STAGE_OTTAVI }
        return confronti(teste.zip(altre), stage, season, key)
    }

    /** Ottavi dei gironi: le prime contro le seconde di un altro girone (e di un'altra nazione, se possibile). */
    private fun ottaviDaGironi(prime: List<Pair<Long, Int>>, seconde: List<Pair<Long, Int>>, nazione: Map<Long, String>,
                               rng: Random): List<Pair<Long, Long>> {
        fun valido(p: List<Pair<Long, Int>>, vincolo: Boolean) = prime.indices.all { i ->
            prime[i].second != p[i].second && (!vincolo || nazione[prime[i].first] != nazione[p[i].first])
        }
        repeat(300) { val p = seconde.shuffled(rng); if (valido(p, true)) return prime.map { it.first }.zip(p.map { it.first }) }
        repeat(300) { val p = seconde.shuffled(rng); if (valido(p, false)) return prime.map { it.first }.zip(p.map { it.first }) }
        return prime.map { it.first }.zip(seconde.shuffled(rng).map { it.first })
    }

    /** Vincitori dei confronti di uno stadio (aggregato, supplementari, rigori, poi forza). */
    fun vincitori(partite: List<MatchResult>, stage: Int, forza: Map<Long, Double>): List<Long> =
        partite.filter { Cup.stageOf(it.round) == stage }.groupBy { setOf(it.homeTeamId, it.awayTeamId) }.values
            .map { Cup.winnerOfTie(it, forza) }

    /**
     * Turno successivo, quando tutte le partite giocate sono finite. Lista vuota se un turno è ancora in corso
     * o la coppa è finita. [gironi] serve solo per la Coppa dei Campioni.
     */
    fun prossimoTurno(
        coppa: Coppa, partite: List<MatchResult>, gironi: List<List<Long>>, squadre: Map<Long, Team>,
        forza: Map<Long, Double>, season: String, rng: Random
    ): List<MatchResult> {
        if (partite.isEmpty() || partite.any { !it.played }) return emptyList()
        val key = key(coppa)
        val nazione = squadre.mapValues { it.value.nazione }
        return when (val ultimo = partite.maxOf { Cup.stageOf(it.round) }) {
            STAGE_GIRONI + 5 -> {
                if (!coppa.conGironi) return emptyList()
                val prime = ArrayList<Pair<Long, Int>>(); val seconde = ArrayList<Pair<Long, Int>>()
                gironi.forEachIndexed { gi, ids ->
                    val classifica = standings(ids.mapNotNull { squadre[it] },
                        partite.filter { inGironi(it.round) && it.homeTeamId in ids })
                    if (classifica.size >= 2) { prime += classifica[0].team.id to gi; seconde += classifica[1].team.id to gi }
                }
                confronti(ottaviDaGironi(prime, seconde, nazione, rng), STAGE_OTTAVI, season, key)
            }
            STAGE_OTTAVI, STAGE_QUARTI, STAGE_SEMI -> {
                val v = vincitori(partite, ultimo, forza).shuffled(rng)
                confronti(v.chunked(2).filter { it.size == 2 }.map { it[0] to it[1] }, ultimo + 1, season, key)
            }
            else -> emptyList()
        }
    }

    /**
     * Supplementari e rigori, solo per le fasi a eliminazione: nei gironi un pareggio resta un pareggio.
     * Restituisce la partita invariata se non serve nulla.
     */
    fun finalizza(m: MatchResult, tutte: List<MatchResult>, rng: Random, cinici: Map<Long, Int> = emptyMap()): MatchResult =
        if (Cup.stageOf(m.round) < STAGE_OTTAVI) m else Cup.finalize(m, tutte, rng, cinici)

    /** Vincitrice e finalista della coppa, a finale giocata. */
    fun finale(partite: List<MatchResult>, forza: Map<Long, Double>): Pair<Long, Long>? {
        val f = partite.filter { Cup.stageOf(it.round) == STAGE_FINALE }.singleOrNull()?.takeIf { it.played } ?: return null
        val w = Cup.winnerOfTie(listOf(f), forza)
        return w to (if (w == f.homeTeamId) f.awayTeamId else f.homeTeamId)
    }

    // ---------------------------------------------------------------------------------------------
    // Premi (per la tua squadra)
    // ---------------------------------------------------------------------------------------------

    private data class Premi(val partecipazione: Long, val vittoriaGirone: Long, val pareggioGirone: Long,
                             val turni: List<Long>, val vittoria: Long)

    /** Premi in euro: partecipazione, risultati nei gironi, ogni turno a eliminazione raggiunto (ottavi, quarti, semifinali, finale), titolo. */
    private fun tabella(c: Coppa) = when (c) {
        // Taratura 24/11: uscita nei gironi circa 4,5 M, vincitrice circa 21 M (prima era 29 M, più di un campionato vinto)
        Coppa.CAMPIONI -> Premi(3_000_000, 600_000, 200_000, listOf(1_000_000, 1_500_000, 2_500_000, 3_500_000), 6_000_000)
        Coppa.EUROPA -> Premi(1_200_000, 0, 0, listOf(500_000, 800_000, 1_200_000, 2_000_000), 3_500_000)
        Coppa.COPPE -> Premi(800_000, 0, 0, listOf(300_000, 500_000, 800_000, 1_200_000), 2_000_000)
    }

    /** Premio di una squadra a fine competizione; 0 se non ha giocato. */
    fun premio(coppa: Coppa, partite: List<MatchResult>, squadra: Long, forza: Map<Long, Double> = emptyMap()): Long {
        val mie = partite.filter { it.homeTeamId == squadra || it.awayTeamId == squadra }
        if (mie.isEmpty()) return 0L
        val t = tabella(coppa)
        var tot = t.partecipazione
        mie.filter { it.played && inGironi(it.round) }.forEach { m ->
            val gf = if (m.homeTeamId == squadra) m.homeGoals else m.awayGoals
            val gs = if (m.homeTeamId == squadra) m.awayGoals else m.homeGoals
            tot += if (gf > gs) t.vittoriaGirone else if (gf == gs) t.pareggioGirone else 0L
        }
        mie.map { Cup.stageOf(it.round) }.filter { it >= STAGE_OTTAVI }.distinct().forEach { st ->
            tot += t.turni[(st - STAGE_OTTAVI).coerceIn(0, 3)]
        }
        if (finale(partite, forza)?.first == squadra) tot += t.vittoria
        return tot
    }

    /** Fase raggiunta da una squadra, per il riepilogo. */
    fun faseRaggiunta(partite: List<MatchResult>, squadra: Long, forza: Map<Long, Double> = emptyMap()): String {
        val mie = partite.filter { it.homeTeamId == squadra || it.awayTeamId == squadra }
        if (mie.isEmpty()) return ""
        if (finale(partite, forza)?.first == squadra) return "Vincitrice"
        val massimo = mie.maxOf { Cup.stageOf(it.round) }
        return if (massimo < STAGE_OTTAVI) "Fase a gironi" else nomeTurno(massimo)
    }
}
