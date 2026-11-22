package it.androcalcio.engine

import org.junit.Assert.*
import org.junit.Test

class DifficoltaTest {
    @Test fun budgetDecrescenteConLaDifficolta() {
        val b = 10_000_000L
        assertEquals(b, Difficolta.budgetIniziale(b, Difficolta.NORMALE))
        assertTrue(Difficolta.budgetIniziale(b, 0) > b)
        assertTrue(Difficolta.budgetIniziale(b, 2) < b)
        assertEquals(Difficolta.budgetIniziale(b, 2), Difficolta.budgetIniziale(b, 99))
        assertEquals(Difficolta.budgetIniziale(b, 0), Difficolta.budgetIniziale(b, -5))
    }
}
