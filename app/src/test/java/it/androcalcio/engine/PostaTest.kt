package it.androcalcio.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PostaTest {
    private val base = Posta.Snapshot(false, emptySet(), emptySet(), 0, 0, 0, 2, 3)

    @Test fun nessunCambioNessunMessaggio() {
        assertTrue(Posta.diff(base, base, emptyMap(), 1).isEmpty())
    }

    @Test fun eventiNelleQuattroCategorie() {
        val dopo = base.copy(finestraAperta = true, infortunati = setOf(7L), offerte = 1, candidati = 3, cantieri = 1, partiteCoppa = 5)
        val m = Posta.diff(base, dopo, mapOf(7L to "Rossi"), 10)
        assertEquals(setOf(Posta.Cat.PARTITA, Posta.Cat.MERCATO, Posta.Cat.GIOCATORI, Posta.Cat.STAFF), m.map { it.cat }.toSet())
        assertTrue(m.any { it.testo.contains("Rossi") })
    }

    @Test fun codificaERilettura() {
        val l = listOf(Posta.Messaggio(5, Posta.Cat.STAFF, "Ciao | mondo", "STAFF", true))
        assertEquals(l, Posta.decode(Posta.encode(l)))
    }
}
