package it.androcalcio.engine

import it.androcalcio.data.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalistaConfidenzaTest {
    private fun gioc() = Player(id = 1, teamId = 1, name = "A", nationality = "IT", age = 25, role = "C", overall = 70, morale = 70, salary = 1000,
        sec2 = "FD", aff2 = 70, sec3 = "AD", aff3 = 100)

    @Test fun piuUnoSoloSottoCento() {
        val q = AnalistaPartita.confidenza(gioc())
        assertEquals(71, q.aff2)
        assertEquals(100, q.aff3)
        assertEquals(0, q.aff4)   // nessun quarto ruolo: resta 0
    }
}
