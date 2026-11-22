package it.androcalcio.engine

import it.androcalcio.data.Player
import org.junit.Assert.*
import org.junit.Test

class FiltroMercatoTest {
    private fun p(age: Int, ovr: Int, contr: Int = 2, pos: String = "DC", sec2: String = "") =
        Player(teamId = 1, name = "X", role = "D", age = age, overall = ovr, morale = 50, salary = 0, nationality = "Italia", contract = contr, pos = pos, sec2 = sec2)

    @Test fun rangeLiberi() {
        val f = FiltroMercato(etaMin = 10, etaMax = 19)
        assertTrue(f.accetta(p(15, 50), 0, false))
        assertFalse(f.accetta(p(20, 50), 0, false))
        assertFalse(FiltroMercato(mediaMin = 70).accetta(p(25, 69), 0, false))
        assertFalse(FiltroMercato(contrattoMax = 1).accetta(p(25, 69, contr = 3), 0, false))
        assertTrue(FiltroMercato(pos = "TS").accetta(p(25, 69, sec2 = "TS"), 0, false))
        assertFalse(FiltroMercato(prezzoMax = 1000).accetta(p(25, 69), 2000, false))
        assertFalse(FiltroMercato(soloSvincolati = true).accetta(p(25, 69), 0, false))
    }
}
