package it.androcalcio.engine

import it.androcalcio.data.Player

/** Finestre di mercato, prestiti e agenti. */
object Finestre {
    enum class Stato(val nome: String, val aperta: Boolean) {
        ESTIVA("Mercato estivo aperto", true), INVERNALE("Mercato invernale aperto", true), CHIUSO("Mercato chiuso", false)
    }

    /** giornata = prossima da giocare (1 = non è partito il campionato); totali = giornate di campionato. */
    fun stato(giornata: Int, totali: Int): Stato = when {
        giornata <= 3 -> Stato.ESTIVA
        giornata in (totali / 2 - 1)..(totali / 2 + 2) -> Stato.INVERNALE
        else -> Stato.CHIUSO
    }

    /** Ultima giornata utile della finestra: "ultimo giorno", i club accettano con più facilità. */
    fun ultimoGiorno(giornata: Int, totali: Int): Boolean =
        giornata == 3 || giornata == totali / 2 + 2

    fun prossimaApertura(giornata: Int, totali: Int): Int =
        if (giornata < totali / 2 - 1) totali / 2 - 1 else 0   // 0 = alla prossima estate
}

/** Prestiti: giocatori dati o presi in prestito fino a fine stagione, con diritto di riscatto. */
object Prestiti {
    data class Prestito(val playerId: Long, val origine: Long, val destinazione: Long, val riscatto: Long, val inUscita: Boolean)

    fun encode(l: List<Prestito>) = l.joinToString(";") {
        "${it.playerId},${it.origine},${it.destinazione},${it.riscatto},${if (it.inUscita) 1 else 0}"
    }
    fun decode(s: String?): List<Prestito> = s.orEmpty().split(";").mapNotNull {
        val p = it.split(",")
        if (p.size != 5) return@mapNotNull null
        Prestito(p[0].toLongOrNull() ?: return@mapNotNull null, p[1].toLongOrNull() ?: return@mapNotNull null,
            p[2].toLongOrNull() ?: return@mapNotNull null, p[3].toLongOrNull() ?: return@mapNotNull null, p[4] == "1")
    }

    /** Costo del prestito: 12% del valore di mercato (quanto incassa chi presta o paga chi prende). */
    fun costo(p: Player): Long = (Market.value(p) * 12 / 100) / 10_000L * 10_000L

    /** Diritto di riscatto: valore di mercato, un po' meno se il giocatore è maturo. */
    fun riscatto(p: Player): Long = (Market.value(p) * (if (p.age >= 30) 85 else 100) / 100) / 10_000L * 10_000L

    fun puoPrestare(p: Player, rosa: Int) = rosa > Market.MIN_SQUAD && p.age <= 33
    fun puoPrendere(rosa: Int) = rosa < Market.MAX_SQUAD
}

/** Agenti: commissione sulle firme e sui rinnovi; gli agenti più potenti chiedono di più. */
object Agenti {
    /** Percentuale della commissione in base al valore del giocatore: 3% sopra i 20 M, fino al 10% per i giovani promettenti. */
    fun percentuale(p: Player): Int = when {
        p.overall >= 85 -> 10
        p.overall >= 78 -> 8
        p.overall >= 70 -> 6
        p.overall >= 60 -> 4
        else -> 2
    } + (if (p.age < 22 && p.potential - p.overall >= 12) 2 else 0)

    /** Commissione su una firma: percentuale dell'ingaggio annuo moltiplicato per gli anni di contratto (almeno 1). */
    fun commissione(p: Player, ingaggio: Long, anni: Int): Long =
        (ingaggio * anni.coerceAtLeast(1) * percentuale(p) / 100) / 1_000L * 1_000L

    private val NOMI = listOf("Gino Procuratti", "Fausto Commissioni", "Tony Percentuale", "Lele Clausolo", "Pippo Pacchetto", "Sandro Sottobanco")
    fun nomeAgente(p: Player): String = NOMI[Math.floorMod(p.id.hashCode(), NOMI.size)]
}
