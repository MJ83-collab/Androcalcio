package it.androcalcio.engine

import org.junit.Assert.*
import org.junit.Test

class EconomiaTest {
    @Test fun riempimentoERangeTv() {
        assertEquals(0.45, Economia.riempimento(-3.0), 1e-9)
        assertEquals(0.90, Economia.riempimento(5.0), 1e-9)
        assertEquals(0.6, Economia.quotaTv(0.0), 1e-9)
        assertEquals(1.4, Economia.quotaTv(1.0), 1e-9)
    }

    @Test fun premioCrescenteConIlPiazzamento() {
        for (div in 0..4) {
            var prev = -1L
            for (i in 0..10) {
                val p = Economia.premioStagione(div, i / 10.0)
                assertTrue("divisione $div non monotona", p > prev)
                prev = p
            }
        }
        assertTrue(Economia.premioStagione(0, 1.0) > Economia.premioStagione(1, 1.0))
        assertTrue(Economia.premioStagione(0, 0.5) > Economia.premioStagione(1, 0.5))
    }

    @Test fun forzaRelativa() {
        assertEquals(0.5, Economia.forzaRelativa(70.0, listOf(70.0, 70.0)), 1e-9)
        assertEquals(0.5, Economia.forzaRelativa(70.0, emptyList()), 1e-9)
        assertEquals(0.0, Economia.forzaRelativa(60.0, listOf(60.0, 80.0)), 1e-9)
        assertEquals(1.0, Economia.forzaRelativa(90.0, listOf(60.0, 80.0)), 1e-9)
    }

    @Test fun partiteInCasaMinimaUno() {
        assertEquals(19, Economia.partiteInCasa(20))
        assertEquals(1, Economia.partiteInCasa(0))
    }

    @Test fun prezziBigliettoSensati() {
        for (div in 0..6) {
            val it = Economia.prezzoBiglietto("Italia", div)
            val br = Economia.prezzoBiglietto("Brasile", div)
            assertTrue(it >= 8 && br >= 5 && br <= it)
            if (div > 0) assertTrue(it <= Economia.prezzoBiglietto("Italia", div - 1))
        }
    }

    @Test fun incassoStadioMonotonoNellaForza() {
        val basso = Economia.incassoStadio(30_000, 40, 0.0, 20)
        val alto = Economia.incassoStadio(30_000, 40, 1.0, 20)
        assertTrue(alto > basso && basso > 0)
        assertEquals(1_000_000L - 1_000_000L, Economia.saldoBase(600_000, 400_000, 1_000_000))
    }

    @Test fun campionatiNuoviPiuPiatti() {
        // La prima di Olanda prende meno della prima di Serie A, l'ultima di più (ingaggi minimi e stadi piccoli)
        assertTrue(Economia.premioStagione(0, 1.0, "Olanda") < Economia.premioStagione(0, 1.0, "Italia"))
        assertTrue(Economia.premioStagione(0, 0.0, "Olanda") > Economia.premioStagione(0, 0.0, "Italia"))
        assertEquals(Economia.premioStagione(0, 0.5), Economia.premioStagione(0, 0.5, "Italia"))
        Economia.PREMI_ESTERI.keys.forEach { assertTrue(it, Economia.premioStagione(0, 0.0, it) >= 3_500_000L) }
    }
}
