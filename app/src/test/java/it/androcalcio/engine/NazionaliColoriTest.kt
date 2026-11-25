package it.androcalcio.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NazionaliColoriTest {
    private val nazioni = Nazionali.EUROPA + setOf("Brasile", "Argentina", "Stati Uniti", "Giappone", "Marocco",
        "Nigeria", "Senegal", "Egitto", "Messico", "Canada", "Corea del Sud", "Australia")

    @Test fun ognunaHaMagliaDaCasaEDaTrasfertaDiverse() {
        nazioni.forEach { n ->
            assertNotEquals(n, Nazionali.colori(n, false), Nazionali.colori(n, true))
            assertNotEquals(n, Nazionali.colori(n, false).first, Nazionali.colori(n, true).first)
        }
    }

    @Test fun coloriTipici() {
        assertEquals(0xFF1F5FBFL, Nazionali.colori("Italia", false).first)      // azzurro
        assertEquals(0xFFFF6600L, Nazionali.colori("Olanda", false).first)      // arancione
        assertEquals(0xFFFFDC02L, Nazionali.colori("Brasile", false).first)     // giallo
        assertEquals(0xFFC8102EL, Nazionali.colori("Spagna", false).first)      // rosso
    }

    @Test fun nazioneSconosciutaHaUnaMaglia() {
        assertNotEquals(Nazionali.colori("Atlantide", false), Nazionali.colori("Atlantide", true))
    }
}
