package it.androcalcio.engine

import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

/**
 * Maglie legate allo sponsor tecnico (v0.7).
 *
 * - 8 marchi di fantasia, ognuno con i suoi stili e un dettaglio di firma.
 * - Tre divise: casa (colori del club), trasferta e terza.
 * - A ogni stagione si possono tenere o cambiare, una volta sola; con un nuovo marchio si cambiano per forza.
 * - Le proposte sono sempre tre, tra cui scegliere.
 * - Merchandising: vendite secondo tifosi, risultati, giocatori famosi, novità, bellezza, negozi e prezzo.
 *
 * Stato: "maglie" (divisa attuale), "maglie_stagione" (stagione della scelta), "maglie_novita" (stagione del cambio),
 * "maglie_proposte" (proposte della stagione), "maglie_prezzo" (prezzo in euro, 0 = standard).
 */
object Maglie {
    const val KEY = "maglie"
    const val KEY_STAGIONE = "maglie_stagione"
    const val KEY_NOVITA = "maglie_novita"
    const val KEY_PROPOSTE = "maglie_proposte"
    const val KEY_PREZZO = "maglie_prezzo"

    enum class Stile(val nome: String, val pregio: Int) {
        TINTA("Tinta unita", 0), STRISCE("Strisce verticali", 8), STRISCE_LARGHE("Bande larghe", 6),
        CERCHI("Strisce orizzontali", 6), META("Metà e metà", 5), BANDA("Banda diagonale", 9),
        SPALLE("Spalle a contrasto", 4), FASCIA("Fascia sul petto", 5), GESSATO("Gessato", 7),
        QUARTI("Quarti", 4), SFUMATO("Sfumato", 3), CHEVRON("Chevron", 6), CROCE("Croce", 5), MANICHE("Maniche a contrasto", 3)
    }

    enum class Colletto(val nome: String) { GIRO("Girocollo"), V("Collo a V"), POLO("Colletto polo") }

    /** Marchio tecnico: stili della casa, colletti preferiti e dettaglio di firma (bordino sulle maniche). */
    data class Marchio(val nome: String, val motto: String, val stili: List<Stile>, val colletti: List<Colletto>, val bordino: Boolean)

    val MARCHI = listOf(
        Marchio("Fulmia", "Linee veloci, tagli diagonali", listOf(Stile.BANDA, Stile.CHEVRON, Stile.SFUMATO, Stile.TINTA), listOf(Colletto.V, Colletto.GIRO), true),
        Marchio("Korvo", "Sobrietà nordica", listOf(Stile.TINTA, Stile.SPALLE, Stile.FASCIA, Stile.CROCE), listOf(Colletto.GIRO), false),
        Marchio("Ardena", "Tradizione italiana", listOf(Stile.STRISCE, Stile.GESSATO, Stile.TINTA, Stile.MANICHE), listOf(Colletto.POLO, Colletto.V), true),
        Marchio("Taurex", "Forza e contrasto", listOf(Stile.META, Stile.QUARTI, Stile.STRISCE_LARGHE, Stile.BANDA), listOf(Colletto.V, Colletto.GIRO), false),
        Marchio("Velka", "Tecnica e leggerezza", listOf(Stile.SFUMATO, Stile.FASCIA, Stile.TINTA, Stile.CHEVRON), listOf(Colletto.GIRO, Colletto.V), true),
        Marchio("Montano Sport", "Il fascino del vintage", listOf(Stile.CERCHI, Stile.STRISCE, Stile.CROCE, Stile.MANICHE), listOf(Colletto.POLO), true),
        Marchio("Zefiro", "Colori di mare", listOf(Stile.CERCHI, Stile.SFUMATO, Stile.SPALLE, Stile.TINTA), listOf(Colletto.GIRO, Colletto.POLO), false),
        Marchio("Gryp", "Grafica di strada", listOf(Stile.QUARTI, Stile.CHEVRON, Stile.META, Stile.GESSATO), listOf(Colletto.V), true)
    )

    /** Senza sponsor tecnico le maglie le fa il club: pochi stili, nessun dettaglio. */
    val CLUB = Marchio("Produzione del club", "Semplice e onesta", listOf(Stile.TINTA, Stile.STRISCE, Stile.SPALLE), listOf(Colletto.GIRO), false)

    /** Marchio di uno sponsor tecnico: se il nome non è uno degli 8 (salvataggi vecchi) se ne sceglie uno fisso dal nome. */
    fun marchioDi(nomeSponsor: String?): Marchio {
        if (nomeSponsor.isNullOrBlank()) return CLUB
        return MARCHI.firstOrNull { it.nome == nomeSponsor } ?: MARCHI[Math.floorMod(nomeSponsor.hashCode(), MARCHI.size)]
    }

    // ---------------------------------------------------------------------------------------------
    // Divise
    // ---------------------------------------------------------------------------------------------

    data class Kit(val stile: Stile, val c1: Long, val c2: Long, val c3: Long, val colletto: Colletto, val bordino: Boolean) {
        fun encode() = listOf(stile.name, c1.toString(16), c2.toString(16), c3.toString(16), colletto.name, if (bordino) "1" else "0").joinToString(",")
        companion object {
            fun decode(s: String): Kit? {
                val p = s.split(",")
                if (p.size < 6) return null
                return Kit(
                    Stile.values().firstOrNull { it.name == p[0] } ?: return null,
                    p[1].toLongOrNull(16) ?: return null, p[2].toLongOrNull(16) ?: return null, p[3].toLongOrNull(16) ?: return null,
                    Colletto.values().firstOrNull { it.name == p[4] } ?: Colletto.GIRO, p[5] == "1"
                )
            }
        }
    }

    data class Divisa(val marchio: String, val casa: Kit, val trasferta: Kit, val terza: Kit) {
        val kit get() = listOf(casa, trasferta, terza)
        fun encode() = marchio + "#" + kit.joinToString("#") { it.encode() }
        companion object {
            fun decode(s: String?): Divisa? {
                val p = s.orEmpty().split("#")
                if (p.size < 4) return null
                return Divisa(p[0], Kit.decode(p[1]) ?: return null, Kit.decode(p[2]) ?: return null, Kit.decode(p[3]) ?: return null)
            }
        }
    }

    fun encodeProposte(l: List<Divisa>) = l.joinToString("§") { it.encode() }
    fun decodeProposte(s: String?): List<Divisa> = s.orEmpty().split("§").mapNotNull { Divisa.decode(it) }

    /** Colori per trasferta e terza. */
    val PALETTE = listOf(
        "Bianco" to 0xFFF5F5F5L, "Nero" to 0xFF1A1A1AL, "Blu notte" to 0xFF14215CL, "Rosso" to 0xFFC62828L,
        "Giallo" to 0xFFF9D71CL, "Verde" to 0xFF2E7D32L, "Celeste" to 0xFF6EC6F0L, "Arancio" to 0xFFEF6C00L,
        "Viola" to 0xFF6A1B9AL, "Grigio" to 0xFF8A8F98L, "Oro" to 0xFFC9A227L, "Granata" to 0xFF7B1F2EL,
        "Turchese" to 0xFF00A0A0L, "Rosa" to 0xFFE48FB5L
    )

    private fun lum(c: Long): Double {
        val r = (c shr 16 and 0xFF) / 255.0; val g = (c shr 8 and 0xFF) / 255.0; val b = (c and 0xFF) / 255.0
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun diversi(a: Long, b: Long) = abs(lum(a) - lum(b)) > 0.18 || tinta(a) != tinta(b)

    /** Tonalità grossolana (0..5) o -1 per i grigi: serve solo a dire se due colori sono "dello stesso tipo". */
    private fun tinta(c: Long): Int {
        val r = (c shr 16 and 0xFF); val g = (c shr 8 and 0xFF); val b = (c and 0xFF)
        val mx = maxOf(r, g, b); val mn = minOf(r, g, b)
        if (mx - mn < 40) return -1
        return when (mx) { r -> if (g > b + 60) 1 else 0; g -> if (b > r + 40) 3 else 2; else -> if (r > g + 40) 5 else 4 }
    }

    private fun contrasto(base: Long): Long = if (lum(base) > 0.55) 0xFF1A1A1AL else 0xFFF5F5F5L

    private fun kit(m: Marchio, stile: Stile, c1: Long, c2: Long, c3: Long, rng: Random) =
        Kit(stile, c1 or 0xFF000000L, c2 or 0xFF000000L, c3 or 0xFF000000L, m.colletti.random(rng), m.bordino)

    /**
     * Una divisa completa: casa con i colori del club; trasferta chiara o scura in contrasto con la casa;
     * terza di un colore della palette diverso dalle altre due.
     */
    fun genera(m: Marchio, colore1: Long, colore2: Long, rng: Random): Divisa {
        val c1 = colore1 and 0xFFFFFFFFL; val c2 = colore2 and 0xFFFFFFFFL
        val casa = kit(m, m.stili.random(rng), c1, if (diversi(c1, c2)) c2 else contrasto(c1), if (rng.nextBoolean()) c2 else contrasto(c1), rng)
        // Trasferta: il secondo colore del club come base se si distingue dal primo, altrimenti bianco o nero
        val baseT = if (diversi(c1, c2) && rng.nextInt(100) < 55) c2 else if (lum(c1) > 0.5) 0xFF1A1A1AL else 0xFFF5F5F5L
        val trasferta = kit(m, m.stili.random(rng), baseT, if (diversi(baseT, c1)) c1 else contrasto(baseT), c1, rng)
        val usati = listOf(c1, c2, baseT)
        val opzioni = PALETTE.map { it.second }.filter { p -> usati.all { diversi(it, p) } }.ifEmpty { PALETTE.map { it.second } }
        val baseX = opzioni.random(rng)
        val secondo = listOf(c1, c2, contrasto(baseX)).firstOrNull { diversi(it, baseX) } ?: contrasto(baseX)
        val terza = kit(m, m.stili.random(rng), baseX, secondo, contrasto(baseX), rng)
        return Divisa(m.nome, casa, trasferta, terza)
    }

    /** Tre proposte del marchio, sempre le stesse per stagione e squadra. */
    fun proposte(m: Marchio, colore1: Long, colore2: Long, seme: Long): List<Divisa> {
        val rng = Random(seme)
        val out = ArrayList<Divisa>()
        repeat(12) {
            if (out.size < 3) {
                val d = genera(m, colore1, colore2, rng)
                if (out.none { it.casa.stile == d.casa.stile && it.terza.c1 == d.terza.c1 }) out += d
            }
        }
        while (out.size < 3) out += genera(m, colore1, colore2, rng)
        return out
    }

    fun seme(teamId: Long, stagione: String, marchio: String) = teamId * 31 + stagione.hashCode() * 7L + marchio.hashCode()

    // ---------------------------------------------------------------------------------------------
    // Bellezza
    // ---------------------------------------------------------------------------------------------

    /** Bellezza di una maglia, 0..100: contrasto leggibile, pregio dello stile, dettagli. */
    fun bellezza(k: Kit): Int {
        val diff = abs(lum(k.c1) - lum(k.c2))
        val contr = when {
            diff < 0.08 && tinta(k.c1) == tinta(k.c2) -> 5      // colori quasi uguali: si legge male
            diff > 0.6 -> 22
            else -> 14 + (diff * 15).toInt()
        }
        val stile = 30 + k.stile.pregio * 3
        val det = (if (k.bordino) 6 else 0) + (if (k.colletto == Colletto.POLO) 4 else 0)
        return (stile + contr + det).coerceIn(0, 100)
    }

    /** Bellezza della divisa: conta di più la maglia di casa. */
    fun bellezza(d: Divisa): Int = (bellezza(d.casa) * 0.5 + bellezza(d.trasferta) * 0.3 + bellezza(d.terza) * 0.2).toInt()

    fun stelle(b: Int) = (b / 20).coerceIn(1, 5)

    // ---------------------------------------------------------------------------------------------
    // Merchandising
    // ---------------------------------------------------------------------------------------------

    /** Prezzo standard di una maglia in euro, secondo la serie (0 = massima). */
    fun prezzoStandard(divisione: Int): Int = when (divisione) { 0 -> 90; 1 -> 65; 2 -> 50; else -> 45 }

    const val PREZZO_MIN = 20
    const val PREZZO_MAX = 200

    /** Costo di produzione: 45% del prezzo standard. Il resto è il margine del club. */
    fun costo(divisione: Int) = prezzoStandard(divisione) * 0.45

    /** Sensibilità al prezzo: in Serie A i tifosi pagano di più, nelle serie minori un prezzo alto svuota i negozi. */
    fun elasticita(divisione: Int) = when (divisione) { 0 -> 1.6; 1 -> 2.0; else -> 2.6 }

    data class Fattori(val tifosi: Double, val risultati: Double, val famosi: Double, val novita: Double,
                       val bellezza: Double, val negozi: Double, val prezzo: Double) {
        val totale get() = tifosi * risultati * famosi * novita * bellezza * negozi * prezzo
    }

    data class Vendite(val maglie: Int, val prezzo: Int, val ricavo: Long, val fattori: Fattori)

    /**
     * Vendite di una stagione. Con valori medi (tifosi 50, metà classifica, nessun campione, maglia già vista,
     * bellezza 50, nessun negozio) e prezzo standard il margine è uguale al vecchio merchandising (25.000 € ogni 1.000 posti).
     * [frac] = 1 per la prima in classifica, 0 per l'ultima. [famosi] = giocatori 84+. [novita] = maglie nuove quest'anno.
     */
    fun vendite(capienza: Int, divisione: Int, prezzo: Int, tifosi: Int, frac: Double, famosi: Int, novita: Boolean,
                bellezza: Int, negozi: Int, sito: Int): Vendite {
        val std = prezzoStandard(divisione)
        val p = (if (prezzo <= 0) std else prezzo).coerceIn(PREZZO_MIN, PREZZO_MAX)
        val f = Fattori(
            tifosi = 0.7 + 0.6 * tifosi.coerceIn(0, 100) / 100.0,
            risultati = 0.8 + 0.4 * frac.coerceIn(0.0, 1.0),
            famosi = 1.0 + 0.08 * famosi.coerceIn(0, 4),
            novita = if (novita) 1.2 else 1.0,
            bellezza = 0.85 + 0.3 * bellezza.coerceIn(0, 100) / 100.0,
            negozi = 1.0 + 0.05 * negozi.coerceIn(0, 4) + 0.08 * sito.coerceIn(0, 3),
            prezzo = (std.toDouble() / p).pow(elasticita(divisione))
        )
        val margineStd = std - costo(divisione)
        val base = Sponsorship.merch(capienza) / margineStd          // maglie vendute a prezzo standard e fattori medi
        val maglie = (base * f.totale).toInt().coerceAtLeast(0)
        val ricavo = (maglie * (p - costo(divisione))).toLong()
        return Vendite(maglie, p, ricavo / 1_000L * 1_000L, f)
    }
}
