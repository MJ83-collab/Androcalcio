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

    @Test fun staffSponsorEReportMensile() {
        val dopo = base.copy(staffInScadenza = setOf("Preparatore Rossi"), sponsorInScadenza = setOf("Acme"), reportMese = "2026-12")
        val m = Posta.diff(base, dopo, emptyMap(), 3)
        assertEquals(3, m.size)
        assertTrue(m.any { it.testo.contains("Preparatore Rossi") && it.vai == "STAFF" })
        assertTrue(m.any { it.testo.contains("Acme") && it.vai == "COMMERCIALE" })
        assertTrue(m.any { it.testo.contains("Report mensile") && it.vai == "ROSA" })
        // stesso stato: nessun messaggio; primo valore del mese da vuoto non genera nulla se il mese non c'è
        assertTrue(Posta.diff(dopo, dopo, emptyMap(), 4).isEmpty())
        assertTrue(Posta.diff(base, base.copy(reportMese = ""), emptyMap(), 5).isEmpty())
    }

    @Test fun codificaERilettura() {
        val l = listOf(Posta.Messaggio(5, Posta.Cat.STAFF, "Ciao | mondo", "STAFF", true))
        assertEquals(l, Posta.decode(Posta.encode(l)))
    }
}
