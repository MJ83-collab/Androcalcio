package it.androcalcio.engine

import it.androcalcio.data.Youth
import kotlin.random.Random

/**
 * Scouting nei vivai degli altri club. Le osservazioni stanno in `scout_reports` con playerId NEGATIVO
 * (= -id del ragazzo), così non serve nessuna migrazione del database.
 */
object ScoutingGiovani {
    const val MIN_VIVAIO_AI = 8        // sotto questa soglia il vivaio di un club AI riceve una nuova leva
    const val MAX_MIO_VIVAIO = 30      // posti massimi nel tuo vivaio
    const val CLUB_PER_MISSIONE = 4
    const val RAGAZZI_PER_CLUB = 2

    fun chiave(y: Youth): Long = -y.id
    fun eGiovane(playerId: Long): Boolean = playerId < 0

    /** Da 1 a 5 stelle: 30 → 1 stella, 85+ → 5 stelle. */
    fun stelle(v: Int): Int = ((v - 16) / 14).coerceIn(1, 5)

    /** Mezze stelle da 1 a 10 (2 = una stella intera): ogni mezza stella vale 7 punti, 30 → 2 mezze, 85+ → 10. */
    fun mezzeStelle(v: Int): Int = ((v - 16) / 7).coerceIn(1, 10)

    fun stelleTesto(v: Int): String = "★".repeat(stelle(v)) + "☆".repeat(5 - stelle(v))

    /** Stima del livello attuale: la forbice si stringe con le osservazioni e con lo scout bravo. */
    fun rangeLivello(y: Youth, skill: Int, osservazioni: Int): IntRange {
        val half = (12 - skill / 12 - osservazioni * 2).coerceAtLeast(2)
        val shift = Random(y.id * 29 + osservazioni).nextInt(-half / 2, half / 2 + 1)
        val c = y.overall + shift
        return (c - half).coerceAtLeast(1)..(c + half).coerceAtMost(99)
    }

    /** Potenziale percepito: più incerto del livello, e cambia a ogni osservazione. */
    fun potenzialeStimato(y: Youth, skill: Int, osservazioni: Int): Int {
        val amp = ((100 - skill) / 5 + 4 - osservazioni * 3).coerceAtLeast(1)
        val noise = Random(y.id * 17 + osservazioni).nextInt(-amp, amp + 1)
        return (y.potential + noise).coerceIn(30, 99)
    }

    /** Cartellino del ragazzo: costo di acquisto dal club (più alto con potenziale e livello). */
    fun prezzo(y: Youth): Long = (y.overall * y.potential * 110L).coerceIn(80_000L, 2_500_000L)

    /** Sceglie i club e i ragazzi osservati in una missione. */
    fun scegli(vivai: Map<Long, List<Youth>>, rng: Random): List<Youth> {
        val club = vivai.filter { it.value.isNotEmpty() }.keys.shuffled(rng).take(CLUB_PER_MISSIONE)
        return club.flatMap { vivai.getValue(it).shuffled(rng).take(RAGAZZI_PER_CLUB) }
    }
}
