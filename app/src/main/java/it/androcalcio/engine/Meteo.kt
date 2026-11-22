package it.androcalcio.engine

import kotlin.random.Random

/** Meteo e campo: cambiano la voglia di segnare, la fatica e il rischio di brutte partite. */
object Meteo {
    enum class Tempo(val nome: String, val icona: String, val gol: Double, val fatica: Double) {
        SOLE("Sole", "☀", 1.00, 1.00),
        NUVOLOSO("Nuvoloso", "☁", 1.00, 1.00),
        PIOGGIA("Pioggia", "🌧", 0.94, 1.10),
        NEBBIA("Nebbia", "🌫", 0.96, 1.00),
        VENTO("Vento", "💨", 0.95, 1.05),
        NEVE("Neve", "❄", 0.88, 1.25),
        CALDO("Caldo torrido", "🔥", 0.97, 1.30)
    }

    enum class Campo(val nome: String, val gol: Double) {
        OTTIMO("Ottimo", 1.03), BUONO("Buono", 1.0), DISCRETO("Discreto", 0.98), PESANTE("Pesante", 0.94), PESSIMO("Pessimo", 0.90)
    }

    data class Condizioni(val tempo: Tempo, val campo: Campo) {
        /** Moltiplicatore sui gol attesi. */
        fun fattoreGol() = tempo.gol * campo.gol
        fun descrizione() = "${tempo.icona} ${tempo.nome} · campo ${campo.nome.lowercase()}"
    }

    /** mese 1..12 (agosto = 8). Estate calda, inverno piovoso e nevoso. */
    fun tempo(mese: Int, rng: Random): Tempo {
        val r = rng.nextDouble()
        return when (mese) {
            6, 7, 8 -> if (r < 0.35) Tempo.CALDO else if (r < 0.80) Tempo.SOLE else if (r < 0.92) Tempo.NUVOLOSO else Tempo.PIOGGIA
            12, 1, 2 -> if (r < 0.15) Tempo.NEVE else if (r < 0.40) Tempo.PIOGGIA else if (r < 0.55) Tempo.NEBBIA else if (r < 0.80) Tempo.NUVOLOSO else Tempo.SOLE
            3, 4, 5, 9, 10, 11 -> if (r < 0.30) Tempo.PIOGGIA else if (r < 0.40) Tempo.VENTO else if (r < 0.50) Tempo.NEBBIA else if (r < 0.75) Tempo.NUVOLOSO else Tempo.SOLE
            else -> Tempo.NUVOLOSO
        }
    }

    /**
     * Stato del campo: manto ibrido e riscaldamento (migliorie) lo tengono bene sotto pioggia e neve;
     * la manutenzione dipende dal livello del curatore (0..3).
     */
    fun campo(tempo: Tempo, ibrido: Boolean, riscaldamento: Boolean, manutenzione: Int, rng: Random): Campo {
        var punti = 3 + manutenzione.coerceIn(0, 3) / 2 + (if (ibrido) 1 else 0)
        if (tempo == Tempo.PIOGGIA) punti -= if (ibrido) 1 else 2
        if (tempo == Tempo.NEVE) punti -= if (riscaldamento) 1 else 3
        if (tempo == Tempo.CALDO) punti -= 1
        punti += rng.nextInt(-1, 2)
        return when {
            punti >= 5 -> Campo.OTTIMO
            punti == 4 -> Campo.BUONO
            punti == 3 -> Campo.DISCRETO
            punti == 2 -> Campo.PESANTE
            else -> Campo.PESSIMO
        }
    }

    /** Il mese di una giornata: da agosto a maggio, in proporzione. */
    fun meseDellaGiornata(giornata: Int, totali: Int): Int {
        val idx = ((giornata - 1) * 10 / totali.coerceAtLeast(1)).coerceIn(0, 9)
        return intArrayOf(8, 9, 10, 11, 12, 1, 2, 3, 4, 5)[idx]
    }
}
