package it.androcalcio.engine

/**
 * Avatar a fumetto parametrici: ogni volto è una lista di indici (uno per campo). Lo stesso nome dà sempre lo
 * stesso volto, ma ogni campo si può cambiare dall'editor; le modifiche si salvano come testo "a.b.c...".
 */
object AvatarGen {
    class Campo(val etichetta: String, val nomi: List<String>, val colori: List<Long>? = null) {
        val n: Int get() = colori?.size ?: nomi.size
    }

    private val SFONDI = listOf(0xFFFFD54F, 0xFF4FC3F7, 0xFFFF8A80, 0xFFAED581, 0xFFCE93D8, 0xFFFFAB91,
        0xFF80CBC4, 0xFFB0BEC5, 0xFFFFF176, 0xFF7986CB)
    private val PELLI = listOf(0xFFFFE0C2, 0xFFF8D2B0, 0xFFF1C27D, 0xFFE0AC69, 0xFFC68642, 0xFFA0652F, 0xFF8D5524, 0xFF5C3A1E)
    private val CAPELLI = listOf(0xFF1B1B1B, 0xFF3B2A20, 0xFF6D4C41, 0xFF9A5B2E, 0xFFE0C068, 0xFFF3E9B8, 0xFFB0B0B0,
        0xFFF5F5F5, 0xFF8D2A1A, 0xFF1E88E5)
    private val GIACCHE = listOf(0xFF1F2333, 0xFF2E3B55, 0xFF3A2A2A, 0xFF1B5E20, 0xFF880E4F, 0xFF4E342E, 0xFF37474F,
        0xFFB71C1C, 0xFFF9A825, 0xFF4A148C)

    val CAMPI: List<Campo> = listOf(
        Campo("Sfondo", emptyList(), SFONDI),
        Campo("Pelle", emptyList(), PELLI),
        Campo("Colore capelli", emptyList(), CAPELLI),
        Campo("Taglio", listOf("Pettinati", "A punte", "Rasati", "Pelato", "Frangia lunga", "Cresta", "Afro",
            "Capelli lunghi", "Ricci corti", "Riga in mezzo", "Ciuffo alto", "A scodella", "Treccine", "Stempiato")),
        Campo("Viso", listOf("Ovale", "Largo", "Lungo", "Squadrato")),
        Campo("Occhi", listOf("Puntini", "Socchiusi", "Grandi", "Sorridenti", "Cattivi", "Stanchi")),
        Campo("Sopracciglia", listOf("Decise", "Dritte", "Alzate", "Arrabbiate", "Folte")),
        Campo("Bocca", listOf("Dritta", "Sorriso", "Ghigno", "Aperta", "Triste", "Denti")),
        Campo("Barba", listOf("Nessuna", "Baffi", "Pizzetto", "Corta", "Piena", "Basette")),
        Campo("Occhiali", listOf("Nessuno", "Da sole", "Tondi", "Rettangolari", "Visore")),
        Campo("Extra", listOf("Nessuno", "Cerotto", "Cicatrice", "Orecchino", "Fascia")),
        Campo("Giacca", emptyList(), GIACCHE),
        Campo("Colletto", listOf("Cravatta", "Papillon", "Camicia aperta", "Girocollo", "Polo"))
    )

    const val SFONDO = 0; const val PELLE = 1; const val COL_CAPELLI = 2; const val TAGLIO = 3; const val VISO = 4
    const val OCCHI = 5; const val SOPRACCIGLIA = 6; const val BOCCA = 7; const val BARBA = 8; const val OCCHIALI = 9
    const val EXTRA = 10; const val GIACCA = 11; const val COLLETTO = 12

    data class Spec(val v: List<Int>) {
        operator fun get(campo: Int): Int = v[campo]
        fun con(campo: Int, valore: Int): Spec {
            val n = CAMPI[campo].n
            return Spec(v.toMutableList().also { it[campo] = Math.floorMod(valore, n) })
        }
        fun encode(): String = v.joinToString(".")
    }

    fun decode(s: String?): Spec? {
        val p = s?.split(".")?.mapNotNull { it.toIntOrNull() } ?: return null
        if (p.size != CAMPI.size) return null
        return Spec(p.mapIndexed { i, x -> Math.floorMod(x, CAMPI[i].n) })
    }

    /** Volto ricavato dal nome: sempre uguale per lo stesso nome. Barba, occhiali ed extra sono più rari. */
    fun daNome(nome: String): Spec {
        var seed = nome.hashCode().toLong() * 0x9E3779B97F4A7C15uL.toLong() + 12345L
        return Spec(CAMPI.mapIndexed { i, c ->
            seed = seed * 6364136223846793005L + 1442695040888963407L
            val r = ((seed ushr 33) % 1_000_000L).toInt()
            if (i == BARBA || i == OCCHIALI || i == EXTRA) { if (r % 100 < 62) 0 else 1 + (r / 100) % (c.n - 1) }
            else r % c.n
        })
    }

    fun casuale(rnd: kotlin.random.Random): Spec = Spec(CAMPI.map { rnd.nextInt(it.n) })

    // ---- Modifiche salvate: un testo "nome<TAB>spec" per riga ----
    fun decodeMappa(s: String?): Map<String, Spec> =
        s.orEmpty().lines().mapNotNull { l ->
            val p = l.split("\t"); if (p.size != 2) null else decode(p[1])?.let { p[0] to it }
        }.toMap()

    fun encodeMappa(m: Map<String, Spec>): String = m.entries.joinToString("\n") { "${it.key}\t${it.value.encode()}" }

    fun specDi(nome: String, mappa: Map<String, Spec>): Spec = mappa[nome] ?: daNome(nome)
}
