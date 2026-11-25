package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TurniTest {

    private fun m(round: Int, league: String) =
        MatchResult(season = "2026/27", round = round, homeTeamId = 1, awayTeamId = 2, homeGoals = 0, awayGoals = 0, league = league)

    private val naz = "COPPA|Italia"
    private val camp = CoppeEuropee.key(CoppeEuropee.Coppa.CAMPIONI)
    private val eur = CoppeEuropee.key(CoppeEuropee.Coppa.EUROPA)

    @Test
    fun campionatoNonHaSlot() {
        assertEquals(null, Turni.slot(7, "Serie A"))
        assertEquals(14, Turni.chiave(7, "Serie A"))
    }

    @Test
    fun coppaNazionaleSempreDopoGiornatePariEEuropeaDopoDispari() {
        for (stage in 0..9) for (gamba in 0..1) {
            val s = Turni.slot(Cup.BASE + stage * 10 + gamba, naz)!!
            assertEquals("nazionale stadio $stage gamba $gamba: giornata $s", 0, s % 2)
        }
        val europei = listOf(
            CoppeEuropee.STAGE_GIRONI to 0, CoppeEuropee.STAGE_GIRONI + 1 to 0, CoppeEuropee.STAGE_GIRONI + 2 to 0,
            CoppeEuropee.STAGE_GIRONI + 3 to 0, CoppeEuropee.STAGE_GIRONI + 4 to 0, CoppeEuropee.STAGE_GIRONI + 5 to 0,
            CoppeEuropee.STAGE_OTTAVI to 0, CoppeEuropee.STAGE_OTTAVI to 1, CoppeEuropee.STAGE_QUARTI to 0, CoppeEuropee.STAGE_QUARTI to 1,
            CoppeEuropee.STAGE_SEMI to 0, CoppeEuropee.STAGE_SEMI to 1, CoppeEuropee.STAGE_FINALE to 0
        )
        europei.forEach { (stage, gamba) ->
            val s = Turni.slot(CoppeEuropee.round(stage, gamba), camp)!!
            assertEquals("europeo stadio $stage gamba $gamba: giornata $s", 1, s % 2)
        }
    }

    @Test
    fun nessunaCoppaDopoLeGiornateDelleNazionali() {
        val tutti = mutableListOf<Int>()
        for (stage in 0..9) for (gamba in 0..1) tutti += Turni.slot(Cup.BASE + stage * 10 + gamba, naz)!!
        for (stage in listOf(200, 201, 202, 203, 204, 205, 210, 211, 212, 213)) for (gamba in 0..1) tutti += Turni.slot(CoppeEuropee.round(stage, gamba), eur)!!
        tutti.forEach { assertFalse("giornata $it è una data delle nazionali", it in Nazionali.DATE) }
    }

    @Test
    fun ordineDeiTurniDiUnaCoppaCresce() {
        val nazionale = (0..8).flatMap { s -> listOf(0, 1).map { g -> Turni.slot(Cup.BASE + s * 10 + g, naz)!! } }
        // andata e ritorno dello stesso stadio sono turni diversi, e lo stadio dopo viene dopo
        for (s in 0..7) assertTrue(Turni.slot(Cup.BASE + (s + 1) * 10, naz)!! > Turni.slot(Cup.BASE + s * 10 + 1, naz)!!)
        assertTrue(nazionale.isNotEmpty())
        val stadi = listOf(200, 201, 202, 203, 204, 205, 210, 211, 212, 213)
        val eu = stadi.map { Turni.slot(CoppeEuropee.round(it, 0), camp)!! }
        for (i in 1 until eu.size) assertTrue("europeo: ${eu[i]} dopo ${eu[i - 1]}", eu[i] > eu[i - 1])
        assertTrue(Turni.slot(CoppeEuropee.round(CoppeEuropee.STAGE_OTTAVI, 1), camp)!! < Turni.slot(CoppeEuropee.round(CoppeEuropee.STAGE_QUARTI, 0), camp)!!)
        assertTrue(Turni.slot(CoppeEuropee.round(CoppeEuropee.STAGE_SEMI, 1), camp)!! < Turni.slot(CoppeEuropee.round(CoppeEuropee.STAGE_FINALE, 0), camp)!!)
    }

    @Test
    fun coppaInSettimanaStaTraDueGiornate() {
        val coppa = Turni.chiave(Cup.BASE + 10, naz)       // stadio 1: dopo la giornata 8
        assertTrue(coppa > Turni.chiave(8, "Serie A"))
        assertTrue(coppa < Turni.chiave(9, "Serie A"))
        assertFalse(Turni.eCampionato(coppa))
        assertEquals(8, Turni.giornata(coppa))
    }

    @Test
    fun supercoppaDopoTuttoIlResto() {
        val super1 = Turni.chiave(Cup.SUPER_ROUND, "SUPERCOPPA|Italia")
        assertTrue(super1 > Turni.chiave(38, "Serie A"))
        assertTrue(super1 > Turni.chiave(CoppeEuropee.round(CoppeEuropee.STAGE_FINALE), eur))
        assertTrue(Turni.chiave(Cup.SUPER_FINAL_ROUND, "SUPERCOPPA|Italia") > super1)
    }

    @Test
    fun dateDomenicaPerCampionatoMercolediPerCoppa() {
        val domenica = Turni.data(2026, Turni.chiave(8, "Serie A"))
        val mercoledi = Turni.data(2026, Turni.chiave(Cup.BASE + 10, naz))
        assertEquals(java.time.DayOfWeek.SUNDAY, domenica.dayOfWeek)
        assertEquals(java.time.DayOfWeek.WEDNESDAY, mercoledi.dayOfWeek)
        assertEquals(domenica, mercoledi.minusDays(3))
        // la Supercoppa non finisce anni dopo
        assertTrue(Turni.data(2026, Turni.chiave(Cup.SUPER_ROUND, "SUPERCOPPA|Italia")).year <= 2027)
    }

    @Test
    fun etichetteLeggibili() {
        assertEquals("giornata 5", Turni.etichetta(m(5, "Serie A")))
        assertTrue(Turni.etichetta(m(CoppeEuropee.round(CoppeEuropee.STAGE_GIRONI + 1), camp)).contains("Coppa dei Campioni"))
        assertEquals("Europa" to "Girone 2", Turni.banner(m(CoppeEuropee.round(201), camp)))
        assertEquals("Giornata" to "5", Turni.banner(m(5, "Serie A")))
    }
}
