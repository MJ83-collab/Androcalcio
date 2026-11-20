package it.androcalcio.engine

import it.androcalcio.data.Player

/** Scheda di una squadra: aspettative, finanze, media, ruoli carenti, capitano e rivalità. */
data class InfoSquadra(
    val campionato: String,
    val sponsor: String,
    val stadio: Int,
    val posizioneAttesa: String,
    val media: Double,
    val rosa: Int,
    val budget: Long,
    val budgetIngaggi: Long,
    val monteIngaggi: Long,
    val ruoliCarenti: List<String>,
    val capitano: Player?,
    val rivale: String?
)

object SquadraInfo {
    /** Organico ideale per ruolo (11 titolari più riserve). */
    private val ideale = listOf("P" to 2, "D" to 6, "C" to 6, "A" to 4)

    fun media(ps: List<Player>): Double = ps.map { it.overall }.average().takeIf { !it.isNaN() } ?: 0.0

    /** Aspettative del club in base alla posizione che la forza gli assegna nel campionato (1 = più forte). */
    fun aspettativa(posizione: Int, squadre: Int): String = when {
        posizione <= 3 -> "Lottare per il titolo"
        posizione <= squadre / 3 -> "Qualificazione in Europa"
        posizione > squadre - 3 -> "Evitare la retrocessione"
        else -> "Salvezza tranquilla"
    }

    /** Capitano: il più forte tra i giocatori nel pieno della carriera (24-33 anni), altrimenti il più forte. */
    fun capitano(ps: List<Player>): Player? =
        ps.filter { it.age in 24..33 }.maxByOrNull { it.overall } ?: ps.maxByOrNull { it.overall }

    /** Ruoli sotto l'organico ideale, con quanti ne ha e quanti ne vorrebbe. */
    fun ruoliCarenti(ps: List<Player>): List<String> {
        val conta = ps.groupingBy { it.role }.eachCount()
        return ideale.filter { (r, n) -> (conta[r] ?: 0) < n }
            .map { (r, n) -> "${nomeRuolo(r)} ${conta[r] ?: 0} su $n" }
    }

    private fun nomeRuolo(r: String) = when (r) { "P" -> "portieri"; "D" -> "difensori"; "C" -> "centrocampisti"; else -> "attaccanti" }
}
