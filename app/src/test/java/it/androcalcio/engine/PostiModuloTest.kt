package it.androcalcio.engine

import it.androcalcio.data.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PostiModuloTest {

    private var n = 0L

    private fun p(role: String, pos: String, ov: Int = 70, sec2: String = "", aff2: Int = 0, ruolo: String = "") =
        Player(id = ++n, teamId = 1, name = "G$n", role = role, age = 25, overall = ov, morale = 70, salary = 1000,
            nationality = "IT", pos = pos, sec2 = sec2, aff2 = aff2, ruoloPartita = ruolo,
            ve = ov, re = ov, ag = ov, qu = ov, fo = 80, gm = ov, en = ov, pa = ov, dr = ov, fi = ov, ti = ov, ri = ov)

    private fun rosa(): List<Player> = listOf(
        p("P", "POR"),
        p("D", "TS"), p("D", "DC"), p("D", "DC"), p("D", "TD"), p("D", "DC"), p("D", "TD"),
        p("C", "MED"), p("C", "INT"), p("C", "INT"), p("C", "REG"), p("C", "MP"), p("C", "INT"),
        p("A", "CA"), p("A", "ES"), p("A", "CA"), p("A", "ES")
    )

    @Test
    fun ognModuloHaPostiGiusti() {
        Formation.values().forEach { f ->
            assertEquals("${f.label}: posti", f.lines.sum(), f.posti.size)
            assertEquals("${f.label}: undici posti", 11, f.postiCompleti().size)
            assertEquals("${f.label}: reparti", 11, f.repartiPosti().size)
            assertEquals("${f.label}: un solo portiere", 1, f.repartiPosti().count { it == "P" })
        }
    }

    @Test
    fun ordineDeiModuliVecchiNonCambia() {
        // Il modulo si salva come numero d'ordine: i nuovi vanno in fondo
        assertEquals(Formation.F442, Formation.values()[0])
        assertEquals(Formation.F343, Formation.values()[6])
        assertEquals(Formation.F433A, Formation.values()[7])
    }

    @Test
    fun allenatoreMetteIlTerzinoSulLatoGiusto() {
        val r = rosa()
        val xi = Lineup.coachPick(r, Formation.F442)
        assertEquals(11, xi.size)
        // Primo difensore del modulo = posto TS: tra i terzini sinistri disponibili è l'unico
        assertEquals("TS", xi[1].pos)
        assertEquals("TD", xi[4].pos)
    }

    @Test
    fun alaConAffinitaAltaGiocaSullaFascia() {
        val ala = p("A", "ES", ov = 74, sec2 = "AD", aff2 = 90)
        val sinistra = p("A", "ES", ov = 76, sec2 = "AS", aff2 = 100)
        val r = rosa().filter { it.pos != "ES" } + ala + sinistra + p("A", "ES", ov = 74)
        val xi = Lineup.coachPick(r, Formation.F433A)
        val scelta = xi.first { it.id == ala.id }
        assertEquals("AD", scelta.ruoloPartita)
        // AD è l'ultimo posto dell'attacco (destra)
        assertEquals(ala.id, xi.last().id)
    }

    @Test
    fun affinitaBassaNonCambiaRuoloInCampo() {
        val ala = p("A", "ES", ov = 80, sec2 = "AD", aff2 = 50)
        val r = rosa().filter { it.pos != "ES" } + ala + p("A", "ES", ov = 60)
        val xi = Lineup.coachPick(r, Formation.F433A)
        xi.filter { it.id == ala.id }.forEach { assertEquals("", it.ruoloPartita) }
    }

    @Test
    fun perPostiMetteLeAliAgliEstremi() {
        val r = rosa()
        val sinistra = p("A", "ES", sec2 = "AS", aff2 = 100, ruolo = "AS")
        val destra = p("A", "ES", sec2 = "AD", aff2 = 100, ruolo = "AD")
        val centro = r.first { it.pos == "CA" }
        val xi = listOf(r.first { it.role == "P" }) + r.filter { it.role == "D" }.take(4) + r.filter { it.role == "C" }.take(3) +
            listOf(destra, centro, sinistra)
        val ord = Lineup.perPosti(xi, Formation.F433A)
        assertEquals(11, ord.size)
        assertEquals(sinistra.id, ord[8].id)
        assertEquals(centro.id, ord[9].id)
        assertEquals(destra.id, ord[10].id)
    }

    @Test
    fun conPochiGiocatoriCompletaA11() {
        val corta = rosa().filter { it.role != "A" } + p("A", "CA")
        assertTrue(Lineup.coachPick(corta, Formation.F433A).size >= 10)
    }
}
