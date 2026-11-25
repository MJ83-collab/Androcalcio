package it.androcalcio.engine

import it.androcalcio.data.Team
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RicambioGiovaniTest {
    private val squadra = Team(id = 7, name = "Test", shortName = "T", colorPrimary = 0, colorSecondary = 0, budget = 0,
        stadiumCapacity = 0, ticketPrice = 0, sponsor = "", nazione = "Italia", campionato = "Serie A")

    @Test fun ilGiovaneHaIlRuoloRichiestoEPuoPrendereUnIdVecchio() {
        val rng = Random(5)
        listOf("P", "D", "C", "A").forEach { ruolo ->
            val g = Progression.newYoung(squadra, rng, false, ruolo).copy(id = 4242)
            assertEquals(ruolo, g.role)
            assertEquals(4242L, g.id)
            assertEquals(squadra.id, g.teamId)
            assertTrue("giovane: ${g.age}", g.age in 17..19)
            assertTrue(g.name.isNotBlank())
        }
    }

    @Test fun caratteristicheCasuali() {
        val rng = Random(9)
        val v = List(40) { Progression.newYoung(squadra, rng, false, "C") }
        assertTrue(v.map { it.name }.toSet().size > 10)
        assertTrue(v.map { it.overall }.toSet().size > 5)
        assertTrue(v.map { it.ve }.toSet().size > 5)
    }
}
