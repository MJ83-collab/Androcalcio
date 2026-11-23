package it.androcalcio.engine

import kotlin.random.Random

/**
 * Salvataggio dalla bancarotta, come nella realtà: se a fine stagione la società non ha più soldi
 * e ha debiti aperti, arriva un nuovo proprietario. Ripiana i debiti (mutui e rate), mette nuovo capitale
 * e riparte con fiducia media. Nessuna penalità in classifica: l'obiettivo è non frustrare.
 */
object NuovoProprietario {
    const val KEY = "proprietario"
    const val KEY_SALVATAGGI = "salvataggi"
    const val FIDUCIA_NUOVA = 55

    val NOMI = listOf(
        "Gruppo Bellavista", "Aurora Capital", "Famiglia Brambilla", "Holding del Lario", "Ottavio Ferraris",
        "Consorzio Valpadana", "Nordest Investimenti", "Famiglia Esposito", "Atlantic Sport Fund", "Cooperativa dei Tifosi",
        "Grifone Partecipazioni", "Lorenzo Castelli", "Fondo Meridiano", "Famiglia Ruggeri", "Alpi Capital"
    )

    /** Capitale minimo messo dal nuovo proprietario, per divisione (0 = massima serie). */
    private val MINIMO = listOf(8_000_000L, 3_000_000L, 1_500_000L, 800_000L)

    /** Crisi: cassa finita, oppure cassa sotto il 5% del monte ingaggi con debiti ancora aperti. */
    fun inCrisi(budget: Long, debiti: Long, ingaggi: Long): Boolean =
        budget <= 0L || (debiti > 0L && budget * 20L < ingaggi)

    /** Nuovo capitale: 30% del monte ingaggi annuo, mai sotto il minimo della divisione. */
    fun ricapitalizzazione(ingaggi: Long, divisione: Int): Long =
        maxOf(ingaggi * 30L / 100L, MINIMO.getOrElse(divisione) { MINIMO.last() }) / 100_000L * 100_000L

    /** Nome del nuovo proprietario, diverso dal precedente. */
    fun nome(rng: Random, precedente: String?): String = NOMI.filter { it != precedente }.random(rng)

    fun messaggio(nome: String, debiti: Long, capitale: Long, money: (Long) -> String): String =
        "Bancarotta evitata: la società passa a $nome. Il nuovo proprietario ripiana debiti per ${money(debiti)} " +
            "e versa ${money(capitale)} di capitale. Si riparte senza penalità."
}
