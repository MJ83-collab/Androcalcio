package it.androcalcio.engine

import org.junit.Assert.*
import org.junit.Test

class DifficoltaTest {
    @Test fun budgetDecrescenteConLaDifficolta() {
        val b = 10_000_000L
        assertEquals(b, Difficolta.budgetIniziale(b, Difficolta.NORMALE))
        assertTrue(Difficolta.budgetIniziale(b, 0) > Difficolta.budgetIniziale(b, 1))
        assertTrue(Difficolta.budgetIniziale(b, 1) > b)
        assertTrue(Difficolta.budgetIniziale(b, 3) < b)
        assertTrue(Difficolta.budgetIniziale(b, 4) < Difficolta.budgetIniziale(b, 3))
        assertEquals(Difficolta.budgetIniziale(b, 4), Difficolta.budgetIniziale(b, 99))
        assertEquals(Difficolta.budgetIniziale(b, 0), Difficolta.budgetIniziale(b, -5))
    }

    @Test fun profiloPersonalizzatoEControllato() {
        val p = Difficolta.Profilo(budget = 5, incassi = 500, infortuni = 100, dirigenza = 100)
        assertEquals(30, Difficolta.profilo(Difficolta.PERSONALIZZATA, p).budget)
        assertEquals(200, Difficolta.profilo(Difficolta.PERSONALIZZATA, p).incassi)
        assertEquals(300L, Difficolta.budgetIniziale(1_000L, Difficolta.PERSONALIZZATA, p))
    }

    @Test fun vecchiIndiciSiMigrano() {
        assertEquals(1, Difficolta.daVecchio(0))   // Facile
        assertEquals(Difficolta.NORMALE, Difficolta.daVecchio(1))
        assertEquals(3, Difficolta.daVecchio(2))   // Difficile
    }

    @Test fun fiduciaPesaDiPiuNelleSconfitteSeDifficile() {
        assertEquals(-2, Difficolta.fiducia(-2, Difficolta.NORMALE))
        assertTrue(Difficolta.fiducia(-4, 4) < -4)
        assertTrue(Difficolta.fiducia(4, 4) < 4)
        assertTrue(Difficolta.prevenzioneExtra(4) < 0)
        assertTrue(Difficolta.prevenzioneExtra(0) > 0)
    }

    @Test fun incassiSeguonoIlLivello() {
        assertTrue(Difficolta.incassi(1_000_000L, 0) > Difficolta.incassi(1_000_000L, 4))
    }
}
