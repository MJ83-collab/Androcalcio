package it.androcalcio.engine

/**
 * Stadio personalizzabile (v0.7): nome, colore dei seggiolini, settori che cambiano con le migliorie
 * e atmosfera che dipende dalle strutture. Stato salvato in Stato: "stadio_nome" e "stadio_colore".
 */
object StadioPersonale {
    const val KEY_NOME = "stadio_nome"
    const val KEY_COLORE = "stadio_colore"
    const val NOME_MIN = 3
    const val NOME_MAX = 32

    /** Colori dei seggiolini: nome e ARGB. */
    val COLORI: List<Pair<String, Long>> = listOf(
        "Rosso" to 0xFFC62828, "Blu" to 0xFF1565C0, "Azzurro" to 0xFF42A5F5, "Verde" to 0xFF2E7D32,
        "Giallo" to 0xFFF9A825, "Arancio" to 0xFFEF6C00, "Viola" to 0xFF6A1B9A, "Granata" to 0xFF7B1E2B,
        "Nero" to 0xFF212121, "Bianco" to 0xFFF5F5F5, "Grigio" to 0xFF757575, "Celeste" to 0xFF80DEEA
    )

    fun nomePredefinito(squadra: String) = "Stadio Comunale $squadra"

    /** Nome accettato: spazi ripuliti, da 3 a 32 caratteri; altrimenti null. */
    fun nomeValido(s: String): String? = s.trim().replace(Regex("\\s+"), " ").takeIf { it.length in NOME_MIN..NOME_MAX }

    fun colore(indice: Int): Pair<String, Long> = COLORI[indice.coerceIn(0, COLORI.size - 1)]

    /**
     * Settori con le quote di posti adattate alle migliorie costruite.
     * Partenza: curva 35%, distinti 25%, tribuna 30%, VIP 10% (come prima della v0.7).
     */
    fun settori(fatte: Set<String>): List<Abbonamenti.Settore> {
        val extra = mutableMapOf("curva" to 0.0, "distinti" to 0.0, "tribuna" to 0.0, "vip" to 0.0)
        fun add(id: String, k: String, v: Double) { if (id in fatte) extra[k] = extra.getValue(k) + v }
        add("curva_n", "curva", 0.03); add("curva_s", "curva", 0.03)
        add("anello", "distinti", 0.04); add("famiglie", "distinti", 0.02)
        add("tribuna", "tribuna", 0.02)
        add("skybox", "vip", 0.03); add("hospitality", "vip", 0.02)
        val grezze = Abbonamenti.SETTORI.map { it to it.quota + extra.getValue(it.id) }
        val tot = grezze.sumOf { it.second }
        return grezze.map { (s, q) -> s.copy(quota = q / tot) }
    }

    /** Punti di atmosfera dati dalle strutture dello stadio (0..14). */
    fun bonusStrutture(fatte: Set<String>): Int {
        var b = 0
        if ("copertura" in fatte) b += 5
        if ("tribuna" in fatte) b += 2
        if ("faretti" in fatte) b += 3
        if ("led" in fatte) b += 1
        if ("maxischermo" in fatte) b += 2
        if ("anello" in fatte) b += 1
        return b
    }

    /** Curva piena scalda lo stadio: da -6 (curva quasi vuota) a +6 (curva esaurita). */
    fun bonusCurva(riempimentoCurva: Double): Int = ((riempimentoCurva.coerceIn(0.0, 1.0) - 0.7) * 20).toInt().coerceIn(-6, 6)

    /** Atmosfera finale nelle partite in casa della tua squadra. */
    fun atmosfera(base: Int, fatte: Set<String>, riempimentoCurva: Double): Int =
        (base + bonusStrutture(fatte) + bonusCurva(riempimentoCurva)).coerceIn(0, 100)

    /** Stima per partita di un settore: posti, riempimento e incasso che resta al club. */
    data class StimaSettore(val settore: Abbonamenti.Settore, val prezzoPct: Int, val posti: Int, val riempimento: Double,
                            val prezzoEuro: Int, val incassoPartita: Long)

    fun stime(capienza: Int, prezzoBase: Int, relativo: Double, tifosi: Int, prezzi: Map<String, Int>, fatte: Set<String>): List<StimaSettore> {
        val set = settori(fatte)
        val somma = set.sumOf { it.quota * it.peso }.takeIf { it > 0 } ?: 1.0
        return set.map { s ->
            val pct = prezzi[s.id] ?: 100
            val posti = (capienza * s.quota).toInt()
            val r = Abbonamenti.riempimento(relativo, tifosi, pct, s.elasticita)
            val euro = prezzoBase * s.peso / somma * pct / 100.0
            StimaSettore(s, pct, posti, r, euro.toInt(), (posti * r * euro * Economia.QUOTA_INCASSO_STADIO).toLong())
        }
    }

    /** Livello del sito web (0..3) e numero di store del club (0..4): serviranno per le vendite delle maglie. */
    fun livelloSito(fatte: Set<String>): Int = listOf("sito", "sito2", "sito3").count { it in fatte }
    fun negozi(fatte: Set<String>): Int = listOf("store", "store2", "store3", "store4").count { it in fatte }
}
