package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import kotlin.random.Random

// Migliorie v0.6: logica pura (si prova nei test), lo stato sta nella tabella Stato come testo.

/** Playoff e playout di fine stagione: gare secche, in parità passa chi era meglio piazzato. */
object Spareggi {
    /** Probabilità che passi [a] (meglio piazzata) contro [b], data la forza dei due undici. */
    fun probPassa(fa: Double, fb: Double): Double = (0.58 + (fa - fb) * 0.03).coerceIn(0.2, 0.85)

    fun passa(a: Long, b: Long, forza: Map<Long, Double>, rng: Random): Long =
        if (rng.nextDouble() < probPassa(forza[a] ?: 60.0, forza[b] ?: 60.0)) a else b

    /**
     * Playoff a 4 (in ordine di classifica): semifinali 1ª-4ª e 2ª-3ª, poi finale. Restituisce (vincitrice, finalista).
     * [gioca] riceve (meglio piazzata, peggio piazzata, turno) e restituisce chi passa: può essere una partita vera.
     */
    inline fun playoffCon(seeds: List<Long>, gioca: (Long, Long, String) -> Long): Pair<Long, Long>? {
        if (seeds.size < 2) return null
        if (seeds.size < 4) return gioca(seeds[0], seeds[1], "Finale").let { w -> w to (if (w == seeds[0]) seeds[1] else seeds[0]) }
        val s1 = gioca(seeds[0], seeds[3], "Semifinale")
        val s2 = gioca(seeds[1], seeds[2], "Semifinale")
        val alto = if (seeds.indexOf(s1) < seeds.indexOf(s2)) s1 else s2
        val basso = if (alto == s1) s2 else s1
        val w = gioca(alto, basso, "Finale")
        return w to (if (w == alto) basso else alto)
    }

    /** Come [playoffCon], tutto simulato con la forza delle rose. */
    fun playoff(seeds: List<Long>, forza: Map<Long, Double>, rng: Random): Pair<Long, Long>? =
        playoffCon(seeds) { a, b, _ -> passa(a, b, forza, rng) }

    /** Playout: la meglio piazzata [alto] contro [basso]; restituisce chi retrocede. */
    fun playout(alto: Long, basso: Long, forza: Map<Long, Double>, rng: Random): Long =
        if (passa(alto, basso, forza, rng) == alto) basso else alto

    // ---- Regole reali (regolamenti Lega B e Lega Pro 2025/26) ----

    /** Come si decide una parità alla fine del turno. */
    enum class Regola(val testo: String) {
        PIAZZATA("in parità passa la meglio piazzata, senza supplementari"),
        SUPPL_PIAZZATA("in parità supplementari, poi passa la meglio piazzata (niente rigori)"),
        SUPPL_RIGORI("in parità supplementari e rigori")
    }

    /** Turno a una o due gare; [seParimerito] vale se le due squadre hanno chiuso il campionato a pari punti. */
    data class Turno(val nome: String, val gare: Int, val regola: Regola, val seParimerito: Regola? = null) {
        fun regolaPer(parimerito: Boolean) = if (parimerito && seParimerito != null) seParimerito else regola
    }

    // Serie B: turno preliminare secco (5ª-8ª, 6ª-7ª), semifinali e finale andata e ritorno; playout andata e ritorno
    val B_PRELIMINARE = Turno("Turno preliminare", 1, Regola.SUPPL_PIAZZATA)
    val B_SEMIFINALE = Turno("Semifinale", 2, Regola.PIAZZATA)
    val B_FINALE = Turno("Finale", 2, Regola.PIAZZATA, seParimerito = Regola.SUPPL_RIGORI)
    val B_PLAYOUT = Turno("Playout", 2, Regola.PIAZZATA)
    // Serie C (fase finale): semifinali a parità meglio piazzata, finale con supplementari e rigori
    val C_SEMIFINALE = Turno("Semifinale", 2, Regola.PIAZZATA)
    val C_FINALE = Turno("Finale", 2, Regola.SUPPL_RIGORI)
    // Inghilterra: semifinali andata e ritorno, finale secca; sempre supplementari e rigori
    val ENG_SEMIFINALE = Turno("Semifinale", 2, Regola.SUPPL_RIGORI)
    val ENG_FINALE = Turno("Finale", 1, Regola.SUPPL_RIGORI)
    // Spagna: andata e ritorno, in parità supplementari e poi la meglio piazzata
    val ESP_TURNO = Turno("Semifinale", 2, Regola.SUPPL_PIAZZATA)
    val ESP_FINALE = Turno("Finale", 2, Regola.SUPPL_PIAZZATA)

    const val ET_LAMBDA = 0.35

    fun poisson(l: Double, rng: Random): Int {
        val limite = Math.exp(-l); var k = 0; var p = 1.0
        do { k++; p *= rng.nextDouble() } while (p > limite && k < 10)
        return k - 1
    }

    /** Chi passa: [golAlto]/[golBasso] sono i totali (supplementari compresi), rigori -1 se non tirati. */
    fun passaTurno(alto: Long, basso: Long, golAlto: Int, golBasso: Int, rigAlto: Int = -1, rigBasso: Int = -1): Long = when {
        golAlto > golBasso -> alto
        golBasso > golAlto -> basso
        rigAlto >= 0 && rigBasso >= 0 -> if (rigBasso > rigAlto) basso else alto
        else -> alto
    }

    /** Gol di una gara simulata: chi gioca in casa ha un piccolo vantaggio. */
    fun golGara(fCasa: Double, fOspite: Double, rng: Random): Pair<Int, Int> {
        val d = (fCasa - fOspite) * 0.04
        return poisson((1.35 + d).coerceIn(0.3, 3.0), rng) to poisson((1.05 - d).coerceIn(0.3, 3.0), rng)
    }

    /** Turno intero simulato con le regole reali (andata in casa della peggio piazzata, ritorno della meglio piazzata). */
    fun simulaTurno(alto: Long, basso: Long, gare: Int, regola: Regola, forza: Map<Long, Double>, rng: Random): Long {
        val fa = forza[alto] ?: 60.0; val fb = forza[basso] ?: 60.0
        var ga = 0; var gb = 0
        if (gare == 2) { val (c, o) = golGara(fb, fa, rng); gb += c; ga += o }
        val (c, o) = golGara(fa, fb, rng); ga += c; gb += o
        if (ga == gb && regola != Regola.PIAZZATA) { ga += poisson(ET_LAMBDA, rng); gb += poisson(ET_LAMBDA, rng) }
        if (ga == gb && regola == Regola.SUPPL_RIGORI) return if (rng.nextDouble() < 0.5) alto else basso
        return passaTurno(alto, basso, ga, gb)
    }

    /** Playoff di Serie B a 6 (in ordine di classifica): preliminari 5ª-8ª e 6ª-7ª, poi semifinali con 3ª e 4ª, poi finale. */
    inline fun tabelloneB(seeds: List<Long>, gioca: (Long, Long, Turno) -> Long): Pair<Long, Long>? {
        if (seeds.size < 6) return tabellone4(seeds, B_SEMIFINALE, B_FINALE, gioca)
        val p1 = gioca(seeds[2], seeds[5], B_PRELIMINARE)
        val p2 = gioca(seeds[3], seeds[4], B_PRELIMINARE)
        val (pAlto, pBasso) = if (seeds.indexOf(p1) < seeds.indexOf(p2)) p1 to p2 else p2 to p1
        return tabellone4(listOf(seeds[0], seeds[1], pAlto, pBasso), B_SEMIFINALE, B_FINALE, gioca)
    }

    /** Playoff a 4: semifinali 1ª-4ª e 2ª-3ª, poi finale; la meglio piazzata è sempre il primo argomento. */
    inline fun tabellone4(seeds: List<Long>, semi: Turno, finale: Turno, gioca: (Long, Long, Turno) -> Long): Pair<Long, Long>? {
        if (seeds.size < 2) return null
        if (seeds.size < 4) return gioca(seeds[0], seeds[1], finale).let { w -> w to (if (w == seeds[0]) seeds[1] else seeds[0]) }
        val s1 = gioca(seeds[0], seeds[3], semi)
        val s2 = gioca(seeds[1], seeds[2], semi)
        val alto = if (seeds.indexOf(s1) < seeds.indexOf(s2)) s1 else s2
        val basso = if (alto == s1) s2 else s1
        val w = gioca(alto, basso, finale)
        return w to (if (w == alto) basso else alto)
    }

    /** Tabellone dei playoff di promozione secondo la nazione. */
    inline fun playoffNazione(nazione: String, seeds: List<Long>, gioca: (Long, Long, Turno) -> Long): Pair<Long, Long>? = when (nazione) {
        "Italia" -> tabelloneB(seeds, gioca)
        "Spagna" -> tabellone4(seeds, ESP_TURNO, ESP_FINALE, gioca)
        else -> tabellone4(seeds, ENG_SEMIFINALE, ENG_FINALE, gioca)
    }
}

/** Classifica solo casa o solo trasferta e forma delle ultime partite. */
object ClassificaExtra {
    fun lato(teams: List<Team>, ms: List<MatchResult>, casa: Boolean): List<StandingRow> {
        val acc = teams.associate { it.id to IntArray(5) }
        ms.filter { it.played }.forEach { m ->
            val id = if (casa) m.homeTeamId else m.awayTeamId
            val x = acc[id] ?: return@forEach
            val gf = if (casa) m.homeGoals else m.awayGoals
            val gs = if (casa) m.awayGoals else m.homeGoals
            x[3] += gf; x[4] += gs
            when { gf > gs -> x[0]++; gf == gs -> x[1]++; else -> x[2]++ }
        }
        return teams.map { t -> val x = acc.getValue(t.id); StandingRow(t, x[0] + x[1] + x[2], x[0], x[1], x[2], x[3], x[4]) }
            .sortedWith(compareByDescending<StandingRow> { it.pts }.thenByDescending { it.gd }.thenByDescending { it.gf })
    }

    /** Ultimi [n] risultati (V, N, P), dal più vecchio al più recente. */
    fun forma(teamId: Long, ms: List<MatchResult>, n: Int = 5): String =
        ms.filter { it.played && (it.homeTeamId == teamId || it.awayTeamId == teamId) }.sortedBy { it.round }.takeLast(n)
            .joinToString("") { m ->
                val (f, s) = if (m.homeTeamId == teamId) m.homeGoals to m.awayGoals else m.awayGoals to m.homeGoals
                when { f > s -> "V"; f == s -> "N"; else -> "P" }
            }
}

/** Posizione in classifica giornata per giornata (per il grafico). */
object Andamento {
    fun posizioni(teamId: Long, teams: List<Team>, ms: List<MatchResult>): List<Int> {
        val last = ms.filter { it.played }.maxOfOrNull { it.round } ?: return emptyList()
        return (1..last).map { r -> standings(teams, ms.filter { it.round <= r }).indexOfFirst { it.team.id == teamId } + 1 }
    }
}

/** Minuti giocati: chi gioca poco (ed è tra i migliori della rosa) perde morale. */
object Minutaggio {
    const val KEY = "minuti"
    const val KEY_PARTITE = "minuti_partite"
    fun encode(m: Map<Long, Int>) = m.entries.joinToString(";") { "${it.key}:${it.value}" }
    fun decode(s: String?): Map<Long, Int> = s.orEmpty().split(";").mapNotNull {
        val p = it.split(":"); if (p.size == 2) (p[0].toLongOrNull() ?: return@mapNotNull null) to (p[1].toIntOrNull() ?: 0) else null
    }.toMap()

    /** Minuti in una partita: 90 ai titolari rimasti in campo, 30 a chi è entrato o uscito. */
    fun aggiungi(m: Map<Long, Int>, titolari: Set<Long>, inCampo: Set<Long>): Map<Long, Int> {
        val out = m.toMutableMap()
        inCampo.forEach { id -> out[id] = (out[id] ?: 0) + if (id in titolari) 90 else 30 }
        return out
    }

    fun percentuale(minuti: Int, partite: Int) = if (partite <= 0) 0 else (minuti * 100 / (partite * 90)).coerceIn(0, 100)

    /** Scontenti per poco spazio: tra i 14 migliori, sotto il 30% dei minuti dopo almeno 6 partite. */
    fun scontenti(rosa: List<Player>, m: Map<Long, Int>, partite: Int): List<Player> {
        if (partite < 6) return emptyList()
        val top = rosa.sortedByDescending { it.overall }.take(14).map { it.id }.toSet()
        return rosa.filter { it.id in top && it.injuredRounds == 0 && percentuale(m[it.id] ?: 0, partite) < 30 }
    }
}

/** Cartellini: costo d'acquisto, ammortamento sugli anni di contratto, plusvalenze, percentuali di rivendita, cessioni a rate. */
object Cartellini {
    const val KEY = "cartellini"
    const val KEY_RIVENDITE = "rivendite"
    const val KEY_CREDITI = "crediti_mercato"
    const val KEY_PLUS = "plusvalenze"
    const val PCT_RIVENDITA = 15
    const val SCONTO_RIVENDITA = 5

    data class Cartellino(val playerId: Long, val costo: Long, val anni: Int, val fatti: Int = 0) {
        val quota get() = if (anni <= 0) costo else costo / anni
        val residuo get() = (costo - quota * fatti).coerceAtLeast(0L)
    }
    data class Rivendita(val playerId: Long, val pct: Int, val nome: String)

    fun encode(l: List<Cartellino>) = l.joinToString(";") { "${it.playerId}:${it.costo}:${it.anni}:${it.fatti}" }
    fun decode(s: String?): List<Cartellino> = s.orEmpty().split(";").mapNotNull {
        val p = it.split(":"); if (p.size == 4) Cartellino(p[0].toLongOrNull() ?: return@mapNotNull null,
            p[1].toLongOrNull() ?: 0L, p[2].toIntOrNull() ?: 1, p[3].toIntOrNull() ?: 0) else null
    }
    fun encodeR(l: List<Rivendita>) = l.joinToString(";") { "${it.playerId}:${it.pct}:${it.nome.replace(";", " ").replace(":", " ")}" }
    fun decodeR(s: String?): List<Rivendita> = s.orEmpty().split(";").mapNotNull {
        val p = it.split(":"); if (p.size == 3) Rivendita(p[0].toLongOrNull() ?: return@mapNotNull null, p[1].toIntOrNull() ?: 0, p[2]) else null
    }

    /** Plusvalenza: prezzo meno valore ancora a bilancio (zero per vivaio e svincolati). */
    fun plusvalenza(prezzo: Long, c: Cartellino?): Long = prezzo - (c?.residuo ?: 0L)

    /** Fine stagione: un anno in più di ammortamento; i cartellini finiti escono. */
    fun avanza(l: List<Cartellino>): List<Cartellino> = l.map { it.copy(fatti = it.fatti + 1) }.filter { it.fatti < it.anni }

    fun ammortamentoAnnuo(l: List<Cartellino>) = l.sumOf { it.quota }

    /** Cessione a rate: metà a fine stagione (solo sopra 1 milione). */
    fun dilazione(prezzo: Long, aRate: Boolean) = if (aRate && prezzo >= 1_000_000L) prezzo / 2 else 0L

    fun prezzoConClausola(prezzo: Long) = prezzo * (100 - SCONTO_RIVENDITA) / 100

    /** Probabilità che il club che lo ha comprato lo rivenda in una stagione: più alta per i giovani forti. */
    fun probRivendita(p: Player): Double = when {
        p.age <= 23 && p.overall >= 75 -> 0.35
        p.age <= 26 -> 0.25
        p.age <= 30 -> 0.15
        else -> 0.05
    }

    fun plusvalenzeStagione(s: String?, stagione: String): Long =
        s.orEmpty().split(";").firstOrNull { it.startsWith("$stagione=") }?.substringAfter("=")?.toLongOrNull() ?: 0L
    fun aggiungiPlus(s: String?, stagione: String, v: Long): String {
        val m = s.orEmpty().split(";").filter { it.contains("=") }.associate { it.substringBefore("=") to (it.substringAfter("=").toLongOrNull() ?: 0L) }.toMutableMap()
        m[stagione] = (m[stagione] ?: 0L) + v
        return m.entries.toList().takeLast(10).joinToString(";") { "${it.key}=${it.value}" }
    }
}

/** Nuovo stadio: progetto di 3 stagioni, pagato a rate a fine stagione; alla fine cambia la capienza. */
object NuovoStadio {
    const val KEY = "nuovo_stadio"
    const val ANNI = 3
    const val COSTO_POSTO = 1_500L
    val CAPIENZE = listOf(30_000, 45_000, 60_000, 80_000)

    data class Progetto(val posti: Int, val anniRimasti: Int, val rata: Long)
    fun costo(posti: Int) = posti * COSTO_POSTO
    fun nuovo(posti: Int) = Progetto(posti, ANNI, costo(posti) / ANNI)
    fun encode(p: Progetto?) = p?.let { "${it.posti}:${it.anniRimasti}:${it.rata}" } ?: ""
    fun decode(s: String?): Progetto? = s.orEmpty().split(":").takeIf { it.size == 3 }?.let {
        Progetto(it[0].toIntOrNull() ?: return null, it[1].toIntOrNull() ?: return null, it[2].toLongOrNull() ?: return null)
    }
    /** Capienze proponibili: solo più grandi di quella attuale. */
    fun opzioni(attuale: Int) = CAPIENZE.filter { it > attuale }
}

/** Stato del prato: le partite in casa e la pioggia lo rovinano, la manutenzione lo ripara. Sotto 50 si segna meno. */
object Prato {
    const val KEY = "prato"
    const val KEY_CURA = "prato_cura"
    val CURE = listOf("Ordinaria", "Curata", "Professionale")
    fun costoCura(l: Int) = longArrayOf(0L, 250_000L, 700_000L)[l.coerceIn(0, 2)]
    private fun recupero(l: Int) = intArrayOf(1, 3, 5)[l.coerceIn(0, 2)]

    /** Dopo una partita in casa (pioggia o neve = più danni). */
    fun dopoPartita(prato: Int, cura: Int, maltempo: Boolean): Int =
        (prato - (if (maltempo) 7 else 3) + recupero(cura)).coerceIn(10, 100)

    /** Fattore sulle azioni pericolose in casa: 1 con prato buono, fino a 0,88 se è pessimo. */
    fun fattore(prato: Int): Double = if (prato >= 50) 1.0 else 0.88 + 0.12 * (prato - 10).coerceAtLeast(0) / 40.0

    fun etichetta(prato: Int) = when { prato >= 85 -> "Perfetto"; prato >= 65 -> "Buono"; prato >= 50 -> "Discreto"; prato >= 30 -> "Rovinato"; else -> "Pessimo" }
}

/** Premio partita promesso alla squadra: alza il morale prima della gara, si paga solo se si vince. */
object PremioPartita {
    const val KEY = "premio_partita"
    const val MORALE = 6
    /** 2% del monte ingaggi annuo della rosa (minimo 20.000 €). */
    fun costo(ingaggi: Long) = maxOf(20_000L, ingaggi * 2 / 100) / 1_000L * 1_000L
}

/** Storico degli infortuni: stagione e turni di stop di ogni infortunio. */
object StoricoInfortuni {
    const val KEY = "storia_inf"
    data class Voce(val playerId: Long, val stagione: String, val turni: Int)
    fun encode(l: List<Voce>) = l.takeLast(400).joinToString(";") { "${it.playerId}:${it.stagione}:${it.turni}" }
    fun decode(s: String?): List<Voce> = s.orEmpty().split(";").mapNotNull {
        val p = it.split(":"); if (p.size == 3) Voce(p[0].toLongOrNull() ?: return@mapNotNull null, p[1], p[2].toIntOrNull() ?: 0) else null
    }
    /** Infortuni nuovi: chi era sano prima e ora ha turni di stop. */
    fun nuovi(prima: List<Player>, dopo: List<Player>, stagione: String): List<Voce> {
        val p = prima.associateBy { it.id }
        return dopo.filter { d -> p[d.id]?.injuredRounds == 0 && d.injuredRounds > 0 }.map { Voce(it.id, stagione, it.injuredRounds) }
    }
    fun fragile(l: List<Voce>, id: Long) = l.count { it.playerId == id } >= 3
}

/** Formazioni salvate: undici titolari, modulo e tattica in 3 posti. */
object FormazioniSalvate {
    const val KEY = "formazioni_salvate"
    const val POSTI = 3
    data class Preset(val nome: String, val modulo: Int, val mentalita: Int, val pressing: Int, val linea: Int, val titolari: List<Long>)
    fun encode(l: List<Preset?>) = l.joinToString(";") { p ->
        p?.let { "${it.nome.replace(Regex("[;|,]"), " ")}|${it.modulo}|${it.mentalita}|${it.pressing}|${it.linea}|${it.titolari.joinToString(",")}" } ?: ""
    }
    fun decode(s: String?): List<Preset?> {
        val parti = s.orEmpty().split(";")
        return List(POSTI) { i ->
            val p = parti.getOrNull(i).orEmpty().split("|")
            if (p.size == 6) Preset(p[0], p[1].toIntOrNull() ?: 0, p[2].toIntOrNull() ?: 1, p[3].toIntOrNull() ?: 1,
                p[4].toIntOrNull() ?: 1, p[5].split(",").mapNotNull { it.toLongOrNull() }) else null
        }
    }
}

/** Ricerca per caratteristica nel mercato. */
enum class Caratteristica(val etichetta: String, val valore: (Player) -> Int) {
    VELOCE("Veloce", { it.ve }), RESISTENTE("Resistente", { it.re }), FISICO("Fisico", { it.fo }),
    TECNICO("Tecnico", { it.qu }), DRIBBLATORE("Dribblatore", { it.dr }), REGISTA("Regista", { it.pa }),
    FINALIZZATORE("Finalizzatore", { it.fi }), TIRATORE("Tiro da fuori", { it.ti }), MARCATORE("Marcatore", { it.en }),
    RIGORISTA("Rigorista", { it.ri });
    companion object { const val SOGLIA = 78 }
}

/** Esportazione del riepilogo di stagione in CSV (separatore ;). */
object EsportaCsv {
    private fun c(s: String) = if (s.contains(';') || s.contains('"')) "\"" + s.replace("\"", "\"\"") + "\"" else s
    fun riepilogo(stagione: String, squadra: String, classifica: List<StandingRow>, rosa: List<Player>,
                  gol: Map<Long, Int>, presenze: Map<Long, Int>, bilancio: List<Bilancio.Riga>): String = buildString {
        appendLine("Androcalcio;Riepilogo stagione ${c(stagione)};${c(squadra)}")
        appendLine(); appendLine("CLASSIFICA"); appendLine("Pos;Squadra;G;V;N;P;GF;GS;PT")
        classifica.forEachIndexed { i, r -> appendLine("${i + 1};${c(r.team.name)};${r.played};${r.won};${r.drawn};${r.lost};${r.gf};${r.ga};${r.pts}") }
        appendLine(); appendLine("ROSA"); appendLine("Nome;Ruolo;Età;Media;Presenze;Gol;Ingaggio")
        rosa.sortedBy { "PDCA".indexOf(it.role) }.forEach { p ->
            appendLine("${c(p.name)};${p.role};${p.age};${p.overall};${presenze[p.id] ?: 0};${gol[p.id] ?: 0};${p.salary}")
        }
        appendLine(); appendLine("BILANCIO"); appendLine("Categoria;Entrate;Uscite")
        bilancio.forEach { appendLine("${c(it.categoria)};${it.entrate};${it.uscite}") }
    }
}
