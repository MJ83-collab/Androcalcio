package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/**
 * Serie di rigori a scelta (coppe): quando tocca a te scegli dove tirare, quando tira l'avversario scegli dove tuffarti.
 * Lati: 0 sinistra, 1 centro, 2 destra. Prima tira la squadra di casa.
 */
class SerieRigori(
    val casaMia: Boolean,
    private val rng: Random = Random.Default,
    /** Rigoristi in ordine di tiro (vedi LiveMatch.rigoristi); vuoto = nessun nome e nessun effetto del Cinico. */
    private val rigoristiCasa: List<Player> = emptyList(),
    private val rigoristiOspiti: List<Player> = emptyList()
) {
    data class Tiro(val casa: Boolean, val mio: Boolean, val gol: Boolean, val lato: Int, val tuffo: Int, val chi: String = "")

    val tiri = mutableListOf<Tiro>()
    val golCasa get() = tiri.count { it.casa && it.gol }
    val golOspiti get() = tiri.count { !it.casa && it.gol }
    val casaTira get() = tiri.size % 2 == 0
    /** True se il prossimo tiro è tuo. */
    val tocca get() = casaTira == casaMia

    val finita: Boolean get() {
        val tc = tiri.count { it.casa }
        val to = tiri.size - tc
        if (tc <= 5 && to <= 5) return golCasa > golOspiti + (5 - to) || golOspiti > golCasa + (5 - tc)
        return tc == to && golCasa != golOspiti
    }

    fun risultato() = golCasa to golOspiti

    private fun latoCasuale(): Int { val r = rng.nextDouble(); return if (r < 0.4) 0 else if (r < 0.6) 1 else 2 }

    /** Chi tira il prossimo rigore (a giro sulla lista), null se la lista è vuota. */
    fun prossimoTiratore(): Player? {
        val lista = if (casaTira) rigoristiCasa else rigoristiOspiti
        if (lista.isEmpty()) return null
        return lista[tiri.count { it.casa == casaTira } % lista.size]
    }

    /** La tua scelta: dove tiri (se tocca a te) o dove ti tuffi (se tira l'avversario). Il Cinico sbaglia meno (Tratti.rigore). */
    fun scegli(lato: Int) {
        if (finita) return
        val mio = tocca
        val tiratore = prossimoTiratore()
        val altro = latoCasuale()
        val tiro = if (mio) lato else altro
        val tuffo = if (mio) altro else lato
        val base = if (tiro == tuffo) P_GOL_INDOVINATO else P_GOL_SPIAZZATO
        val gol = rng.nextDouble() < Tratti.rigore(base, tiratore)
        tiri += Tiro(casaTira, mio, gol, tiro, tuffo, tiratore?.name.orEmpty())
    }

    companion object {
        /** Gol se il portiere indovina il lato. */
        const val P_GOL_INDOVINATO = 0.25
        /** Gol se il portiere si tuffa dall'altra parte. */
        const val P_GOL_SPIAZZATO = 0.92
        fun nomeLato(l: Int) = when (l) { 0 -> "a sinistra"; 1 -> "al centro"; else -> "a destra" }
    }
}
