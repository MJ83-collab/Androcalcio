package it.androcalcio.engine

import kotlin.random.Random

/**
 * Serie di rigori a scelta (coppe): quando tocca a te scegli dove tirare, quando tira l'avversario scegli dove tuffarti.
 * Lati: 0 sinistra, 1 centro, 2 destra. Prima tira la squadra di casa.
 */
class SerieRigori(val casaMia: Boolean, private val rng: Random = Random.Default) {
    data class Tiro(val casa: Boolean, val mio: Boolean, val gol: Boolean, val lato: Int, val tuffo: Int)

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

    /** La tua scelta: dove tiri (se tocca a te) o dove ti tuffi (se tira l'avversario). */
    fun scegli(lato: Int) {
        if (finita) return
        val mio = tocca
        val altro = latoCasuale()
        val tiro = if (mio) lato else altro
        val tuffo = if (mio) altro else lato
        val gol = if (tiro == tuffo) rng.nextDouble() < 0.25 else rng.nextDouble() < 0.92
        tiri += Tiro(casaTira, mio, gol, tiro, tuffo)
    }

    companion object {
        fun nomeLato(l: Int) = when (l) { 0 -> "a sinistra"; 1 -> "al centro"; else -> "a destra" }
    }
}
