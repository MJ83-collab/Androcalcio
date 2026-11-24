package it.androcalcio.engine

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelecronistiTest {
    @Test fun coppiaCasualeConUnaPrimaEUnaSecondaVoce() {
        val viste = HashSet<Voce>()
        repeat(300) { val c = Coppia.sorteggia(Random(it)); assertTrue(c.prima.prima); assertTrue(!c.seconda.prima); viste += c.prima; viste += c.seconda }
        assertEquals(9, viste.size)   // tutte e nove le voci escono prima o poi
    }

    @Test fun ogniVoceHaLaSuaParlata() {
        val gol = MatchEvent(30, EventType.GOAL, true, "", 0.5f, 0.5f, playerId = 7)
        Voce.values().filter { it.prima }.forEach { v ->
            val t = Voci.reazione(v, gol, "Rossi", Random(1))!!
            assertTrue("$v: $t", t.contains("Rossi"))
        }
        val r = Telecronaca.reazione(gol, "Rossi", Coppia(Voce.STREPPONI, Voce.SBARAGLIA), Random(2))
        assertTrue(r.first().startsWith("Gianluca Strepponi: "))
        assertTrue(r.size == 2 && r[1].startsWith("Totò Sbaraglia: "))
    }
}
