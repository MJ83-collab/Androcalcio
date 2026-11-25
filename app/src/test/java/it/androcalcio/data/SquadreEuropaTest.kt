package it.androcalcio.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Controlli sui sei campionati nuovi (v0.7): squadre inventate, dati completi, rose generate sensate. */
class SquadreEuropaTest {
    private data class Riga(val nazione: String, val campionato: String, val squadra: String, val sigla: String, val c1: String, val c2: String)

    private fun leggi(file: String): List<Riga> {
        val testo = File("src/main/assets/$file").readText()
        val regex = Regex("\"nazione\": \"([^\"]*)\",\\s*\"campionato\": \"([^\"]*)\",\\s*\"squadra\": \"([^\"]*)\",\\s*" +
            "\"sigla\": \"([^\"]*)\",\\s*\"colore1\": \"([^\"]*)\",\\s*\"colore2\": \"([^\"]*)\"")
        val r = regex.findAll(testo).map { Riga(it.groupValues[1], it.groupValues[2], it.groupValues[3], it.groupValues[4],
            it.groupValues[5], it.groupValues[6]) }.toList()
        assertEquals("Il regex deve leggere tutte le squadre di $file", testo.split("\"squadra\":").size - 1, r.size)
        return r
    }

    private val nuove by lazy { leggi("squadre_europa.json") }

    @Test fun squadrePerCampionato() {
        val attese = mapOf("Olanda" to 18, "Portogallo" to 18, "Belgio" to 18, "Scozia" to 10, "Turchia" to 18, "Grecia" to 18)
        assertEquals(attese, nuove.groupingBy { it.nazione }.eachCount())
        // un solo campionato per nazione: solo massima serie
        assertTrue(nuove.groupBy { it.nazione }.values.all { v -> v.map { it.campionato }.distinct().size == 1 })
    }

    @Test fun nomiUniciEDiversiDalleAltreSquadre() {
        val vecchi = (leggi("squadre.json") + leggi("squadre_giappone.json")).map { it.squadra }.toSet()
        assertEquals(nuove.size, nuove.map { it.squadra }.toSet().size)
        assertTrue(nuove.none { it.squadra in vecchi })
    }

    @Test fun colorieSigleValidi() {
        val hex = Regex("#[0-9A-Fa-f]{6}")
        assertTrue(nuove.all { hex.matches(it.c1) && hex.matches(it.c2) && it.c1 != it.c2 })
        assertTrue(nuove.all { it.sigla.length in 2..3 && it.sigla == it.sigla.uppercase() })
    }

    @Test fun roseGenerateComplete() {
        val squadre = nuove.map { RoseFisse.Squadra(it.nazione, it.campionato, it.squadra) }
        val righe = RoseFisse.genera(squadre).filter { it.squadra.isNotEmpty() }.groupBy { it.squadra }
        assertEquals(nuove.size, righe.size)
        righe.values.forEach { rosa ->
            val ruoli = rosa.groupingBy { it.giocatore.role }.eachCount()
            assertTrue(rosa.size >= 16)
            assertTrue((ruoli["P"] ?: 0) >= 2 && (ruoli["D"] ?: 0) >= 5 && (ruoli["C"] ?: 0) >= 5 && (ruoli["A"] ?: 0) >= 3)
        }
    }

    @Test fun primaPiuForteDellUltima() {
        val squadre = nuove.map { RoseFisse.Squadra(it.nazione, it.campionato, it.squadra) }
        val righe = RoseFisse.genera(squadre).filter { it.squadra.isNotEmpty() }.groupBy { it.squadra }
        val forza = { nome: String -> righe.getValue(nome).map { it.giocatore.overall }.sortedDescending().take(11).average() }
        nuove.groupBy { it.nazione }.values.forEach { l -> assertTrue(forza(l.first().squadra) > forza(l.last().squadra)) }
    }
}
