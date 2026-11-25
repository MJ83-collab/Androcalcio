package it.androcalcio.engine

/**
 * Modulo di gioco: righe di movimento (difesa → attacco) e bonus/malus sulle tre fasi.
 *
 * [posti] dà il ruolo atteso di ogni giocatore di movimento (portiere escluso), linea per linea e da sinistra
 * a destra (come le coordinate di [coords]). Vuoto = nessun posto fisso. I posti servono all'allenatore per
 * scegliere i titolari (FD/FS esterni, AD/AS ali, terzini, centrali); il motore partita non li guarda.
 */
enum class Formation(
    val label: String,
    val lines: List<Int>,
    val atkMod: Double,
    val midMod: Double,
    val defMod: Double,
    val posti: List<String> = emptyList()
) {
    F442("4-4-2", listOf(4, 4, 2), 1.00, 1.00, 1.00,
        listOf("TS", "DC", "DC", "TD", "FS", "INT", "INT", "FD", "ES", "CA")),
    F433("4-3-3", listOf(4, 3, 3), 1.05, 0.97, 0.98,
        listOf("TS", "DC", "DC", "TD", "INT", "MED", "INT", "ES", "CA", "ES")),
    F352("3-5-2", listOf(3, 5, 2), 1.00, 1.06, 0.95,
        listOf("CS", "DC", "CD", "FS", "INT", "MED", "INT", "FD", "ES", "CA")),
    F4231("4-2-3-1", listOf(4, 2, 3, 1), 0.98, 1.04, 1.00,
        listOf("TS", "DC", "DC", "TD", "MED", "MED", "INT", "MP", "INT", "CA")),
    F532("5-3-2", listOf(5, 3, 2), 0.94, 1.00, 1.07,
        listOf("TS", "CS", "DC", "CD", "TD", "INT", "MED", "INT", "ES", "CA")),
    F451("4-5-1", listOf(4, 5, 1), 0.93, 1.06, 1.02,
        listOf("TS", "DC", "DC", "TD", "FS", "INT", "MED", "INT", "FD", "CA")),
    F343("3-4-3", listOf(3, 4, 3), 1.08, 1.00, 0.93,
        listOf("CS", "DC", "CD", "FS", "MED", "MED", "FD", "ES", "CA", "ES")),

    // Moduli con le ali (v0.7). Vanno sempre in fondo: il modulo scelto si salva come numero d'ordine.
    F433A("4-3-3 ali", listOf(4, 3, 3), 1.06, 0.97, 0.97,
        listOf("TS", "DC", "DC", "TD", "INT", "MED", "INT", "AS", "CA", "AD")),
    F4231A("4-2-3-1 ali", listOf(4, 2, 3, 1), 0.99, 1.04, 1.00,
        listOf("TS", "DC", "DC", "TD", "MED", "MED", "AS", "MP", "AD", "CA")),
    F343A("3-4-3 ali", listOf(3, 4, 3), 1.09, 1.00, 0.93,
        listOf("CS", "DC", "CD", "FS", "MED", "MED", "FD", "AS", "CA", "AD"));

    init {
        require(posti.isEmpty() || posti.size == lines.sum()) { "Posti di $label: ${posti.size} invece di ${lines.sum()}" }
    }

    /** Ruoli attesi degli 11 posti in ordine di campo: portiere, poi difesa, centrocampo, attacco (da sinistra a destra). */
    fun postiCompleti(): List<String> = listOf("POR") + posti.ifEmpty { List(lines.sum()) { "" } }

    /** Reparto (P, D, C, A) di ciascuno degli 11 posti, come in [Lineup.needs]. */
    fun repartiPosti(): List<String> {
        val out = mutableListOf("P")
        lines.forEachIndexed { i, n ->
            val r = when (i) { 0 -> "D"; lines.size - 1 -> "A"; else -> "C" }
            repeat(n) { out += r }
        }
        return out
    }

    /** Posizioni normalizzate (x lungo il campo nella propria metà, y in larghezza), portiere incluso. */
    fun coords(): List<Pair<Float, Float>> {
        val out = mutableListOf(0.05f to 0.5f)
        val n = lines.size
        lines.forEachIndexed { li, cnt ->
            val x = 0.18f + 0.28f * (if (n == 1) 0f else li.toFloat() / (n - 1))
            for (k in 0 until cnt) out += x to ((k + 1f) / (cnt + 1f))
        }
        return out
    }
}

/** Mentalità: sposta l'equilibrio tra attacco e difesa e cambia il ritmo della partita. */
enum class Mentality(val label: String, val atk: Double, val def: Double, val tempo: Double) {
    DIFENSIVA("Difensiva", 0.92, 1.08, 0.94),
    EQUILIBRATA("Equilibrata", 1.0, 1.0, 1.0),
    OFFENSIVA("Offensiva", 1.08, 0.92, 1.06)
}

/** Pressing: più alto = più palloni recuperati (possesso) ma più stanchezza. */
enum class Pressing(val label: String, val poss: Double, val drain: Double) {
    BASSO("Basso", 0.95, 0.8),
    MEDIO("Medio", 1.0, 1.0),
    ALTO("Alto", 1.06, 1.3)
}

/** Ampiezza: stretta = più possesso e più solidità; larga = più occasioni dalle fasce ma più spazi dietro. */
enum class Ampiezza(val label: String, val poss: Double, val def: Double, val atk: Double) {
    STRETTA("Stretta", 1.03, 1.03, 0.97),
    NORMALE("Normale", 1.0, 1.0, 1.0),
    LARGA("Larga", 0.98, 0.97, 1.04)
}

/** Linea difensiva: alta = squadra corta e più spinta, ma soffre gli attaccanti veloci. */
enum class DefLine(val label: String, val poss: Double, val def: Double, val atk: Double) {
    BASSA("Bassa", 0.97, 1.05, 0.97),
    MEDIA("Media", 1.0, 1.0, 1.0),
    ALTA("Alta", 1.04, 0.99, 1.02)
}

/** Schema abituale di una squadra AI: sempre lo stesso per squadra, con pesi da schemi più comuni. */
fun schemaAbituale(teamId: Long): Formation {
    val pesi = listOf(
        Formation.F442 to 35, Formation.F433 to 25, Formation.F4231 to 20, Formation.F352 to 10,
        Formation.F451 to 5, Formation.F532 to 3, Formation.F343 to 2
    )
    var r = kotlin.random.Random(teamId * 7919L + 17L).nextInt(100)
    for ((f, w) in pesi) {
        if (r < w) return f
        r -= w
    }
    return Formation.F442
}

/** Schema di una squadra AI in partita: di solito quello abituale, ogni tanto un altro. */
fun schemaInPartita(teamId: Long, rng: kotlin.random.Random): Formation =
    if (rng.nextDouble() < 0.8) schemaAbituale(teamId) else Formation.values().random(rng)
