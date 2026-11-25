package it.androcalcio.engine

import it.androcalcio.data.Player

/**
 * Ruoli tattici. Ogni giocatore ha un ruolo naturale (pos) con affinità 100%
 * e fino a due ruoli affini (sec2, sec3) con la loro affinità in %.
 * Il ruolo in cui gioca in campo (ruoloPartita) moltiplica la sua efficacia:
 * nel ruolo naturale 100%, in un ruolo affine l'affinità.
 */
object TacticalRoles {

    val NAMES = mapOf(
        "POR" to "Portiere", "TD" to "Terzino destro", "TS" to "Terzino sinistro",
        "DC" to "Centrale", "CS" to "Centrale sinistro", "CD" to "Centrale destro", "MED" to "Mediano", "REG" to "Regista", "INT" to "Interno",
        "MP" to "Mezzapunta", "ES" to "Estremo", "CA" to "Centravanti",
        "FD" to "Esterno destro", "FS" to "Esterno sinistro", "AD" to "Ala destra", "AS" to "Ala sinistra"
    )

    /** Ruoli affini per ruolo naturale, sempre nella stessa linea (difesa, centrocampo, attacco). */
    val AFFINI: Map<String, List<Pair<String, Int>>> = mapOf(
        "TD" to listOf("TS" to 70, "DC" to 40),
        "TS" to listOf("TD" to 70, "DC" to 40),
        "DC" to listOf("CS" to 90, "CD" to 90),
        "CS" to listOf("CD" to 90, "DC" to 80),
        "CD" to listOf("CS" to 90, "DC" to 80),
        "POR" to listOf("DC" to 100),
        "MED" to listOf("REG" to 70, "INT" to 50),
        "REG" to listOf("MED" to 70, "INT" to 40),
        "INT" to listOf("MP" to 70, "MED" to 40),
        "MP" to listOf("INT" to 70, "REG" to 40),
        "ES" to listOf("CA" to 50),
        "CA" to listOf("ES" to 40)
    )

    /** Ruoli di fascia (destra/sinistra) che si imparano solo con l'allenamento: non entrano nei ruoli di partenza. */
    val DA_IMPARARE: Map<String, List<Pair<String, Int>>> = mapOf(
        "TD" to listOf("FD" to 60), "TS" to listOf("FS" to 60),
        "INT" to listOf("FD" to 50, "FS" to 50),
        "MP" to listOf("AD" to 60, "AS" to 60),
        "ES" to listOf("AD" to 80, "AS" to 80),
        "CA" to listOf("AD" to 50, "AS" to 50)
    )

    /** Imparare con l'allenamento: un nuovo ruolo affine (parte da metà dell'affinità) o più affinità in uno già noto. */
    fun impara(p: Player, rng: kotlin.random.Random): Player {
        val noti = setOf(p.sec2, p.sec3, p.sec4)
        val nuovi = (AFFINI[p.pos].orEmpty() + DA_IMPARARE[p.pos].orEmpty()).filter { it.first !in noti }
        val liberi = listOf(p.sec2, p.sec3, p.sec4).count { it.isEmpty() }
        if (nuovi.isNotEmpty() && liberi > 0 && rng.nextInt(100) < 40) {
            val (r, a) = nuovi[rng.nextInt(nuovi.size)]
            val aff = (a / 2).coerceAtLeast(25)
            return when {
                p.sec2.isEmpty() -> p.copy(sec2 = r, aff2 = aff)
                p.sec3.isEmpty() -> p.copy(sec3 = r, aff3 = aff)
                else -> p.copy(sec4 = r, aff4 = aff)
            }
        }
        val su = buildList { if (p.sec2.isNotEmpty() && p.aff2 < 100) add(2); if (p.sec3.isNotEmpty() && p.aff3 < 100) add(3); if (p.sec4.isNotEmpty() && p.aff4 < 100) add(4) }
        if (su.isEmpty()) return p
        return when (su[rng.nextInt(su.size)]) {
            2 -> p.copy(aff2 = (p.aff2 + 4).coerceAtMost(100))
            3 -> p.copy(aff3 = (p.aff3 + 4).coerceAtMost(100))
            else -> p.copy(aff4 = (p.aff4 + 4).coerceAtMost(100))
        }
    }

    fun label(role: String): String = NAMES[role] ?: role

    /** Ruolo affine di default in posizione i (0 = primo, 1 = secondo) per un ruolo naturale: (ruolo, affinità). */
    fun affine(pos: String, i: Int): Pair<String, Int> = AFFINI[pos]?.getOrNull(i) ?: ("" to 0)

    /** Ruolo in cui il giocatore scende in campo: quello scelto, altrimenti il naturale. */
    fun assigned(p: Player): String = p.ruoloPartita.ifEmpty { p.pos }

    /** Efficacia in campo: 1.0 nel ruolo naturale, l'affinità % in un ruolo affine. */
    fun factor(p: Player): Double {
        val r = assigned(p)
        return Compiti.effetto(p.compito, r) * when {
            r == p.pos -> 1.0
            r == p.sec2 -> p.aff2 / 100.0
            r == p.sec3 -> p.aff3 / 100.0
            r == p.sec4 -> p.aff4 / 100.0
            else -> 1.0
        }
    }

    /** Ruoli che il giocatore può scegliere, con affinità: prima il naturale (100%). */
    fun options(p: Player): List<Pair<String, Int>> = listOfNotNull(
        p.pos.takeIf { it.isNotEmpty() }?.let { it to 100 },
        p.sec2.takeIf { it.isNotEmpty() }?.let { it to p.aff2 },
        p.sec3.takeIf { it.isNotEmpty() }?.let { it to p.aff3 },
        p.sec4.takeIf { it.isNotEmpty() }?.let { it to p.aff4 }
    )
}
