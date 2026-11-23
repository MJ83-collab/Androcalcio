package it.androcalcio.engine

/**
 * Mutui bancari: la società chiede soldi a banche dai nomi comici, ognuna con tasso, tetto e durata propri.
 * Ammortamento "alla francese": rata annuale costante, pagata a fine stagione.
 * Gli interessi contano come costo operativo (fair play); capitale ed erogazione no.
 */
object Mutui {
    const val TETTO_DEBITO = 40_000_000L
    const val PENALE_ESTINZIONE = 0.02   // sul residuo, per chi estingue in anticipo
    const val MORA = 0.05                // sulla parte di rata non pagata per mancanza di budget

    data class Banca(val id: Int, val nome: String, val slogan: String, val tasso: Double, val tetto: Long, val anni: Int)

    val banche = listOf(
        Banca(0, "Cassa Rurale del Cugino Gigi", "Ti conosciamo da sempre, e anche tua madre.", 0.040, 3_000_000L, 3),
        Banca(1, "Banca Popolare dello Zio Gaetano", "Una stretta di mano vale più di un contratto.", 0.065, 8_000_000L, 5),
        Banca(2, "Credito Cooperativo Pantofola d'Oro", "Ogni mutuo, un calcio al problema.", 0.090, 15_000_000L, 4),
        Banca(3, "Monte dei Pegni Svelti", "Porta la coppa, esci coi soldi.", 0.120, 10_000_000L, 3),
        Banca(4, "Istituto Sogni d'Oro & Figli", "Paghi poco ora, tanto dopo. Tantissimo dopo.", 0.075, 20_000_000L, 8),
        Banca(5, "Banca dei Fratelli Strozzi", "Nessuna domanda. Nessuna pietà.", 0.180, 25_000_000L, 2)
    )

    /** Sopra questo debito il presidente taglia il budget a fine stagione (3% dell'eccedenza). */
    const val SOGLIA_FIDUCIA = 15_000_000L
    fun taglio(debito: Long): Long = ((debito - SOGLIA_FIDUCIA) * 3 / 100).coerceAtLeast(0L)
    /** Fiducia del presidente 0..100: scende con il debito. */
    fun fiducia(debito: Long): Int = (100 - debito * 60 / TETTO_DEBITO).toInt().coerceIn(0, 100)

    fun banca(id: Int): Banca? = banche.firstOrNull { it.id == id }

    /** Un mutuo in corso. `tasso` è fissato alla firma; `anni` sono le rate ancora da pagare. */
    data class Mutuo(val banca: Int, val residuo: Long, val anni: Int, val tasso: Double, val rata: Long)

    fun rata(capitale: Long, tasso: Double, anni: Int): Long {
        if (capitale <= 0L || anni <= 0) return 0L
        if (tasso <= 0.0) return (capitale + anni - 1) / anni
        val f = Math.pow(1.0 + tasso, -anni.toDouble())
        return Math.ceil(capitale * tasso / (1.0 - f)).toLong()
    }

    fun debitoTotale(l: List<Mutuo>): Long = l.sumOf { it.residuo }

    /** Importo massimo richiedibile a una banca: tetto della banca, tetto di debito complessivo, un solo mutuo per banca. */
    fun massimo(b: Banca, attivi: List<Mutuo>): Long =
        if (attivi.any { it.banca == b.id }) 0L
        else minOf(b.tetto, TETTO_DEBITO - debitoTotale(attivi)).coerceAtLeast(0L)

    sealed class Esito {
        data class Ok(val lista: List<Mutuo>, val mutuo: Mutuo) : Esito()
        data class No(val msg: String) : Esito()
    }

    fun richiedi(attivi: List<Mutuo>, bancaId: Int, importo: Long): Esito {
        val b = banca(bancaId) ?: return Esito.No("Banca sconosciuta.")
        if (attivi.any { it.banca == b.id }) return Esito.No("${b.nome} ti ha già prestato dei soldi: restituiscili prima.")
        val max = massimo(b, attivi)
        if (max <= 0L) return Esito.No("Hai già troppi debiti: il tetto complessivo è ${TETTO_DEBITO / 1_000_000} milioni.")
        if (importo < 500_000L) return Esito.No("Importo minimo: 500.000 €.")
        if (importo > max) return Esito.No("${b.nome} ti presta al massimo $max €.")
        val m = Mutuo(b.id, importo, b.anni, b.tasso, rata(importo, b.tasso, b.anni))
        return Esito.Ok(attivi + m, m)
    }

    fun penaleRifinanziamento(m: Mutuo): Long = Math.round(m.residuo * PENALE_ESTINZIONE)

    /** Sposta il debito residuo su un'altra banca con tasso più basso. Costo: penale del 2% sul residuo. */
    fun rifinanzia(attivi: List<Mutuo>, da: Int, a: Int): Esito {
        val m = attivi.firstOrNull { it.banca == da } ?: return Esito.No("Mutuo non trovato.")
        val b = banca(a) ?: return Esito.No("Banca sconosciuta.")
        if (a == da || attivi.any { it.banca == a }) return Esito.No("${b.nome} ti ha già prestato dei soldi.")
        if (b.tasso >= m.tasso) return Esito.No("Il tasso di ${b.nome} non è più basso.")
        if (m.residuo > b.tetto) return Esito.No("${b.nome} presta al massimo ${b.tetto} €.")
        val nuovo = Mutuo(b.id, m.residuo, b.anni, b.tasso, rata(m.residuo, b.tasso, b.anni))
        return Esito.Ok(attivi.filter { it.banca != da } + nuovo, nuovo)
    }

    /** Quota interessi della prossima rata. */
    fun interessi(m: Mutuo): Long = Math.round(m.residuo * m.tasso)

    data class Rata(val interessi: Long, val capitale: Long, val mora: Long, val nuovo: Mutuo?)

    /**
     * Rata di fine stagione. Se il budget non basta si paga il possibile; il resto resta in debito con una mora.
     * L'ultima rata chiude il mutuo.
     */
    fun paga(m: Mutuo, disponibile: Long): Rata {
        val intr = interessi(m)
        val ultima = m.anni <= 1
        val dovuta = if (ultima) m.residuo + intr else minOf(m.rata, m.residuo + intr)
        val pagata = dovuta.coerceAtMost(disponibile.coerceAtLeast(0L))
        val mancante = dovuta - pagata
        val mora = Math.round(mancante * MORA)
        val pagaInteressi = minOf(intr, pagata)
        val pagaCapitale = pagata - pagaInteressi
        // Interessi non pagati si sommano al debito
        val residuo = m.residuo - pagaCapitale + (intr - pagaInteressi) + mora
        if (residuo <= 0L) return Rata(pagaInteressi, pagaCapitale, mora, null)
        val anni = if (mancante > 0L && ultima) 1 else m.anni - 1
        return Rata(pagaInteressi, pagaCapitale, mora, m.copy(residuo = residuo, anni = anni, rata = rata(residuo, m.tasso, anni)))
    }

    /** Costo per estinguere subito: residuo più penale. */
    fun costoEstinzione(m: Mutuo): Long = m.residuo + Math.round(m.residuo * PENALE_ESTINZIONE)

    fun encode(l: List<Mutuo>): String = l.joinToString(";") { "${it.banca}:${it.residuo}:${it.anni}:${Math.round(it.tasso * 10000)}:${it.rata}" }

    fun decode(s: String?): List<Mutuo> = s.orEmpty().split(";").mapNotNull {
        val p = it.split(":")
        if (p.size != 5) return@mapNotNull null
        val b = p[0].toIntOrNull() ?: return@mapNotNull null
        val r = p[1].toLongOrNull() ?: return@mapNotNull null
        val a = p[2].toIntOrNull() ?: return@mapNotNull null
        val t = p[3].toIntOrNull() ?: return@mapNotNull null
        val rt = p[4].toLongOrNull() ?: return@mapNotNull null
        if (r <= 0L || a <= 0) null else Mutuo(b, r, a, t / 10000.0, rt)
    }
}
