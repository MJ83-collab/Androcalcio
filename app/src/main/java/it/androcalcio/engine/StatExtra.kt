package it.androcalcio.engine

/** Passaggi, duelli e chilometri di una squadra nella partita dal vivo (la simulazione rapida non li calcola). */
class StatExtra {
    var passaggi = 0
    var passaggiRiusciti = 0
    var duelli = 0
    var duelliVinti = 0
    var km = 0.0
    val precisione: Int get() = if (passaggi == 0) 0 else passaggiRiusciti * 100 / passaggi
}

/** Atmosfera dello stadio (0..100): più è calda, più la squadra di casa spinge e tiene la palla. */
object Atmosfera {
    /** Con la tua squadra in casa contano l'umore dei tifosi e la capienza; in trasferta conta lo stadio di casa. */
    fun valore(tifosi: Int, userHome: Boolean, capienzaCasa: Int): Int {
        val pieno = (capienzaCasa / 60_000.0).coerceIn(0.0, 1.0)
        return if (userHome) (tifosi.coerceIn(0, 100) * 0.7 + pieno * 30).toInt().coerceIn(0, 100)
        else (20 + pieno * 80).toInt().coerceIn(0, 100)
    }

    fun etichetta(v: Int): String = when {
        v >= 80 -> "infuocato"
        v >= 60 -> "caldo"
        v >= 40 -> "tiepido"
        else -> "freddo"
    }

    /** Moltiplicatore sul possesso della squadra di casa: da -5% (stadio freddo) a +5% (stadio infuocato). Solo partita dal vivo. */
    fun fattore(v: Int): Double = 1.0 + (v.coerceIn(0, 100) - 50) / 50.0 * 0.05
}

/** Minuto per il tabellone e la cronaca: oltre il 90' è recupero ("90+3"). */
fun etichettaMinuto(m: Int): String = if (m > 90) "90+${m - 90}" else "$m"
