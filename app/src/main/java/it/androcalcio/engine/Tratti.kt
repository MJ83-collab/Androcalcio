package it.androcalcio.engine

import it.androcalcio.data.Player

/**
 * Tratto "Cinico" (portato al risultato): chi lo ha cerca il gol e lo trova più spesso, soprattutto nei finali di partita.
 * Una parte dei giocatori lo ha dalla nascita (deriva dal nome, come la personalità); gli altri lo possono acquisire
 * con l'allenamento tattico. Gli acquisiti sono salvati nella tabella Stato (chiave "tratti"), senza toccare il database.
 *
 * Effetti in partita (valori tarati con CinicoTaraturaTest, vedi le costanti):
 * - azione: più tiri tentati rispetto a un compagno di pari qualità ([pesoTiro]);
 * - rigori in partita e nelle serie dal vivo: meno errori ([rigore]);
 * - finale di partita in bilico: finalizza meglio ([fattoreFinale]).
 * Il tratto sposta soprattutto chi segna e un poco quando: il totale dei gol quasi non cambia.
 * Le serie di rigori simulate (Cup) non hanno giocatori, quindi lì il tratto non conta.
 */
object Tratti {
    @Volatile var acquisiti: Set<Long> = emptySet()
    @Volatile var sporco: Boolean = false

    /**
     * Peso extra tra i possibili tiratori (era 1,2, mai misurato). Misure di CinicoTaraturaTest (4560 partite):
     * con 1,10 (più rigori e finali) il Cinico segna il 9,0% in più di sé stesso non Cinico, sotto il bersaglio 10-15%;
     * con 1,15 segna il 12,2% in più (attaccanti 13,0%, centrocampisti 11,4%). Il totale dei gol cresce dello 0,5%.
     */
    const val PESO_TIRO = 1.15

    /** Rigore: punti di probabilità di segnare in più (0,85 diventa 0,89). Vale in partita e nelle serie dal vivo. */
    const val BONUS_RIGORE = 0.04
    /** Tetto della probabilità di segnare un rigore, anche per il Cinico. */
    const val RIGORE_MAX = 0.97

    /** Dal minuto FINALE_DA il Cinico finalizza meglio, se il risultato è in bilico. */
    const val FINALE_DA = 75
    /** Dal minuto FINALE_FORTE_DA (ultimi minuti e recupero) l'effetto è più forte. */
    const val FINALE_FORTE_DA = 83
    /** Probabilità di gol del tiro moltiplicata per (1 + bonus): dal 75' all'82'. */
    const val BONUS_FINALE = 0.10
    /** Dall'83' in poi, recupero compreso. */
    const val BONUS_FINALE_FORTE = 0.20
    /** Risultato in bilico: la squadra del tiratore è avanti al massimo di questi gol (pari o sotto valgono sempre). */
    const val VANTAGGIO_MAX_BILICO = 1

    fun innato(p: Player): Boolean = Math.floorMod(p.name.hashCode() * 17 + p.nationality.hashCode(), 6) == 0
    fun cinico(p: Player): Boolean = innato(p) || p.id in acquisiti

    fun acquisisci(id: Long) { if (id !in acquisiti) { acquisiti = acquisiti + id; sporco = true } }

    fun encode(): String = acquisiti.joinToString(";")
    fun decode(s: String?) { acquisiti = s.orEmpty().split(";").mapNotNull { it.toLongOrNull() }.toSet(); sporco = false }

    /** Peso nella scelta del tiratore su azione: [PESO_TIRO] per il Cinico, 1 per gli altri. */
    fun pesoTiro(p: Player): Double = if (cinico(p)) PESO_TIRO else 1.0

    /** Probabilità di segnare un rigore: quella di base, più [BONUS_RIGORE] se tira un Cinico (al massimo [RIGORE_MAX]). */
    fun rigore(base: Double, tiratore: Player?): Double =
        if (tiratore != null && cinico(tiratore)) maxOf(base, minOf(base + BONUS_RIGORE, RIGORE_MAX)) else base

    /** True se il risultato è in bilico per chi attacca: pari, sotto, o avanti di al massimo [VANTAGGIO_MAX_BILICO]. */
    fun inBilico(mio: Int, loro: Int): Boolean = mio - loro <= VANTAGGIO_MAX_BILICO

    /**
     * Moltiplicatore della probabilità di gol di un tiro su azione: 1 prima del [FINALE_DA], con risultato non in bilico
     * o per chi non è Cinico; 1 + [BONUS_FINALE] dal 75', 1 + [BONUS_FINALE_FORTE] dall'83'.
     */
    fun fattoreFinale(p: Player, minuto: Int, mio: Int, loro: Int): Double = when {
        minuto < FINALE_DA || !inBilico(mio, loro) || !cinico(p) -> 1.0
        minuto >= FINALE_FORTE_DA -> 1.0 + BONUS_FINALE_FORTE
        else -> 1.0 + BONUS_FINALE
    }
}
