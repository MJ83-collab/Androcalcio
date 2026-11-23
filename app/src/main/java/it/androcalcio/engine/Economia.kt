package it.androcalcio.engine

/**
 * Economia di fine stagione: un solo posto dove tararla. Sono funzioni pure, quindi si provano nei test
 * (EconomiaTest simula più stagioni con le rose vere del gioco).
 *
 * Incassi di un club: biglietti (capienza x prezzo x riempimento x partite in casa x quota che resta al club),
 * premio di piazzamento e diritti TV. Uscite: monte ingaggi. Per la tua squadra si aggiungono
 * sponsor e merchandising (entrate) e allenatore, manutenzione strutture, assicurazione (uscite).
 */
object Economia {
    const val INGAGGIO_MINIMO_PER_LIVELLO = 40_000L        // (livello - 40) x 40.000 € l'anno

    /** Quota dell'incasso lordo dei biglietti che resta al club: il resto copre i costi di gestione dello stadio. */
    const val QUOTA_INCASSO_STADIO = 0.55

    /** Diritti TV per divisione (0 = massima serie; l'ultimo valore vale anche per la Serie C), scalati dal piazzamento. */
    val DIRITTI_TV = listOf(7_000_000L, 3_200_000L, 1_800_000L, 1_200_000L)

    /** Premio di piazzamento del primo classificato per divisione; il resto scende in proporzione. */
    private val PREMIO_BASE = listOf(8_000_000L, 3_000_000L, 1_500_000L)
    private const val PREMIO_BASE_MINORI = 1_000_000L
    private const val DIRITTI_TV_MINORI = 1_200_000L

    /** Riempimento dello stadio: 45% per la squadra più debole del campionato, 90% per la più forte. */
    fun riempimento(rel: Double) = 0.45 + 0.45 * rel.coerceIn(0.0, 1.0)

    /** Quota dei diritti TV: 60% in fondo alla classifica, 140% in cima. */
    fun quotaTv(frac: Double) = 0.6 + 0.8 * frac.coerceIn(0.0, 1.0)

    /** Partite in casa in un campionato a andata e ritorno con [squadre] squadre. */
    fun partiteInCasa(squadre: Int) = (squadre - 1).coerceAtLeast(1)

    fun diritti(divisione: Int): Long = DIRITTI_TV.getOrElse(divisione) { DIRITTI_TV_MINORI }

    fun premioBase(divisione: Int): Long = PREMIO_BASE.getOrElse(divisione) { PREMIO_BASE_MINORI }

    /** Premio di piazzamento più diritti TV; frac = 1 per la prima, 0 per l'ultima. */
    fun premioStagione(divisione: Int, frac: Double): Long =
        (premioBase(divisione) * frac.coerceIn(0.0, 1.0) + diritti(divisione) * quotaTv(frac)).toLong()

    /** Incasso dei biglietti di una stagione; rel = forza relativa nel campionato (0 = ultima, 1 = prima). */
    fun incassoStadio(capienza: Int, prezzo: Int, rel: Double, squadreLega: Int): Long =
        (capienza.toLong() * prezzo * riempimento(rel) * partiteInCasa(squadreLega) * QUOTA_INCASSO_STADIO).toLong()

    /** Forza relativa (0..1) di una squadra tra quelle del suo campionato; 0,5 se sono tutte uguali. */
    fun forzaRelativa(forza: Double, tutte: Collection<Double>): Double {
        val lo = tutte.minOrNull() ?: return 0.5
        val hi = tutte.maxOrNull() ?: return 0.5
        return if (hi > lo) ((forza - lo) / (hi - lo)).coerceIn(0.0, 1.0) else 0.5
    }

    /** Prezzo del biglietto di partenza: scende con la divisione; Brasile e Argentina costano meno. */
    fun prezzoBiglietto(nazione: String, divisione: Int): Int {
        val base = (45 - divisione * 8).coerceAtLeast(8)
        return if (nazione == "Brasile" || nazione == "Argentina") (base * 0.6).toInt().coerceAtLeast(5) else base
    }

    /** Bilancio di una stagione senza sponsor e extra (come per le squadre guidate dall'IA). */
    fun saldoBase(incasso: Long, premio: Long, ingaggi: Long): Long = incasso + premio - ingaggi
}
