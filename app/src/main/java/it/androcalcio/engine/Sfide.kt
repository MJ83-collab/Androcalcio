package it.androcalcio.engine

import kotlin.random.Random

/** Sfida della settimana: un obiettivo extra per la prossima partita, con premio in fiducia e tifosi. */
object Sfide {
    data class Sfida(val id: Int, val testo: String, val fiducia: Int, val tifosi: Int)

    val TUTTE = listOf(
        Sfida(0, "Vinci la partita", 3, 2),
        Sfida(1, "Non subire gol", 3, 1),
        Sfida(2, "Segna almeno due gol", 2, 3),
        Sfida(3, "Vinci con due gol di scarto", 4, 4),
        Sfida(4, "Non perdere", 2, 1),
        Sfida(5, "Segna e subisci: serve spettacolo (almeno 3 gol totali)", 1, 5),
        Sfida(6, "Vinci senza subire gol", 5, 3),
        Sfida(7, "Fai felice la curva: vinci con tre gol o più", 3, 6)
    )

    // Livelli: tre sfide proposte a ogni partita, una per livello; più è difficile, più rende
    private val FACILI = listOf(0, 4)
    private val MEDIE = listOf(1, 2, 5)
    private val DIFFICILI = listOf(3, 6, 7)
    fun offerte(rng: Random): List<Sfida> = listOf(FACILI, MEDIE, DIFFICILI).map { l -> TUTTE[l[rng.nextInt(l.size)]] }
    fun livello(id: Int): String = when (id) { in FACILI -> "Facile"; in MEDIE -> "Media"; else -> "Difficile" }
    fun codice(l: List<Sfida>): String = l.joinToString(",") { it.id.toString() }
    fun decodeOfferte(s: String?): List<Sfida> = s.orEmpty().split(",").mapNotNull { it.toIntOrNull() }.mapNotNull { da(it) }

    fun genera(rng: Random): Sfida = TUTTE[rng.nextInt(TUTTE.size)]
    fun da(id: Int?): Sfida? = TUTTE.firstOrNull { it.id == id }

    fun riuscita(s: Sfida, gf: Int, gs: Int): Boolean = when (s.id) {
        0 -> gf > gs
        1 -> gs == 0
        2 -> gf >= 2
        3 -> gf - gs >= 2
        4 -> gf >= gs
        5 -> gf + gs >= 3
        6 -> gf > gs && gs == 0
        else -> gf >= 3 && gf > gs
    }
}
