package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StaffRifinituraTest {
    @Test fun almenoTreRisposteSempre() {
        val a = Annunci.Annuncio(Staffing.ALLENATORE)
        for (fascino in listOf(10, 50, 95)) for (mese in 1..24) {
            val r = Annunci.risposte(7, a, "2026-$mese", fascino)
            assertTrue("fascino $fascino mese $mese: ${r.size}", r.size in 3..5)
        }
    }

    @Test fun glIngaggiSeguonoLaCategoria() {
        val serieA = Staffing.scalaCategoria(76.0)
        val serieC = Staffing.scalaCategoria(55.0)
        assertTrue(serieC < serieA)
        assertTrue(Staffing.wage(70, serieC) < Staffing.wage(70, serieA))
        assertEquals(Staffing.wage(70), Staffing.wage(70, 1.0))
        // chi è bravo costa di più, ma sempre in proporzione alla serie
        assertTrue(Staffing.wage(80, serieC) > Staffing.wage(50, serieC))
        assertTrue(Staffing.wage(80, serieC) < Staffing.wage(80, serieA))
    }

    @Test fun corsoCostaDiPiuInSerieAltaESaleMenoSeGiaBravo() {
        assertTrue(Staffing.costoCorso(60, 1.0) > Staffing.costoCorso(60, 0.4))
        assertTrue(Staffing.guadagnoCorso(40) > Staffing.guadagnoCorso(85))
    }

    @Test fun stelleDa1A5ConMezza() {
        assertEquals(1, Staffing.mezzeStelle(0))
        assertEquals(10, Staffing.mezzeStelle(99))
        assertEquals(7, Staffing.mezzeStelle(70))   // 3 stelle e mezza
    }

    @Test fun rifinituraMigliora() {
        val rng = Random(3)
        var p = Player(teamId = 1, name = "T", role = "C", age = 25, overall = 70, morale = 70, salary = 1, nationality = "Italia",
            sec2 = "MED", aff2 = 50, pa = 60, dr = 60, ti = 60, condition = 90)
        val prima = p.pa + p.dr + p.ti
        repeat(40) { p = CalendarioGiorni.applica(p, CalendarioGiorni.Attivita.RIFINITURA, rng) }
        assertTrue(p.pa + p.dr + p.ti > prima)
        assertEquals(90, p.aff2 + 0 * 40 + 0 - 0 + 0 - 0 + 0 - 0)   // 50 + 40, fermo a 100 al massimo
        assertEquals(50, p.condition.coerceAtLeast(50))
    }
}
