package it.androcalcio.engine

import kotlin.math.abs
import kotlin.random.Random

/**
 * Tifoserie e rivalità. Ogni club ha una curva con un nome e una passione (dai tifosi e dallo stadio).
 * Le rivalità storiche sono fisse (vedi Rivalita); quelle nuove nascono dagli incontri: più si gioca contro
 * una squadra, più cresce il punteggio, soprattutto con partite equilibrate e ricche di gol.
 * Ogni partita la curva di casa prepara una coreografia da sola: più è passionale, più scalda la squadra;
 * contro una rivale la curva ospite contesta e può arrivare una multa per le pirotecniche.
 */
object Tifoserie {
    const val KEY_RIVALI = "rivali_punteggi"
    const val SOGLIA_RIVALITA = 30

    private val NOMI = listOf("Brigata", "Commando", "Fossa", "Gruppo", "Furia", "Vecchia Guardia", "Fedelissimi", "Ultras")

    fun curva(nome: String, sigla: String): String = "${NOMI[Math.floorMod(nome.hashCode(), NOMI.size)]} $sigla"

    /** Passione 20..99: nasce dai tifosi (0..100) e dalla capienza dello stadio. */
    fun passione(tifosi: Int, capienza: Int): Int = (tifosi * 0.7 + (capienza / 1000).coerceAtMost(30)).toInt().coerceIn(20, 99)

    fun decodePunteggi(s: String?): Map<String, Int> = s.orEmpty().split(";").mapNotNull {
        val kv = it.split("="); if (kv.size != 2) null else kv[0] to (kv[1].toIntOrNull() ?: return@mapNotNull null)
    }.toMap()

    fun encodePunteggi(m: Map<String, Int>): String = m.entries.joinToString(";") { "${it.key}=${it.value}" }

    /** Ogni incontro aggiunge punti alla rivalità con l'avversario: partita equilibrata e con gol valgono di più. */
    fun aggiorna(punteggi: Map<String, Int>, avversario: String, gf: Int, gs: Int): Map<String, Int> {
        var p = 10
        if (abs(gf - gs) <= 1) p += 10
        if (gf + gs >= 4) p += 5
        return punteggi + (avversario to (punteggi[avversario] ?: 0) + p)
    }

    /** Rivale attuale: la storica (intensità 100) oppure la squadra con il punteggio più alto sopra soglia. */
    fun rivale(nome: String, punteggi: Map<String, Int>): Pair<String, Int>? =
        Rivalita.rivaleDi(nome)?.let { it to 100 }
            ?: punteggi.maxByOrNull { it.value }?.takeIf { it.value >= SOGLIA_RIVALITA }?.toPair()

    data class Coreografia(val testo: String, val qualita: Int, val moraleCasa: Int, val pressioneOspiti: Int, val multa: Long)

    /** La curva prepara la coreografia da sola: il tipo dipende da derby e rivalità, la qualità da passione e caso. */
    fun coreografia(curva: String, passione: Int, rivalita: Boolean, derby: Boolean, rng: Random): Coreografia {
        val tipi = when {
            derby -> listOf("pirotecnica e striscione contro la rivale", "bandieroni e coro di derby")
            rivalita -> listOf("coro di sfida e striscioni", "torce e cori contro gli avversari")
            else -> listOf("bandieroni", "striscione di sostegno", "coreografia coi colori sociali", "coro continuo")
        }
        val tipo = tipi.random(rng)
        val q = (passione * 0.6 + rng.nextInt(0, 41)).toInt().coerceIn(10, 100)
        val morale = when { q >= 75 -> 4; q >= 50 -> 2; else -> 1 }
        val pressione = if (rivalita) (if (derby) 3 else 2) else 0
        val multa = if (derby && tipo.startsWith("pirotecnica") && rng.nextInt(100) < 30) 20_000L + passione * 200L else 0L
        val coda = if (multa > 0) " · pirotecnica: il club paga la multa di ${multa / 1000}k" else ""
        return Coreografia("$curva: $tipo (qualità $q)$coda", q, morale, pressione, multa)
    }
}
