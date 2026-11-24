package it.androcalcio.engine

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Taratura v0.6: economia meno punitiva, sponsor più generosi, salvataggio dalla bancarotta. */
class EconomiaV06Test {

    /** Stagione tipo della tua squadra (rose vere del gioco): incassi + premi + 3 sponsor + merch - ingaggi - staff - allenatore. */
    private fun saldi(campionato: String, divisione: Int): List<Long> {
        val righe = File("src/main/assets/rose_fisse.csv").readLines().drop(1).map { it.split("|") }
        val json = File("src/main/assets/squadre.json").readText()
        val nomi = Regex("\"nazione\": \"Italia\",\\s*\"campionato\": \"$campionato\",\\s*\"squadra\": \"([^\"]*)\"")
            .findAll(json).map { it.groupValues[1] }.toList()
        val forza = nomi.associateWith { n -> righe.filter { it[0] == n }.map { it[3].toInt() }.sortedDescending().take(11).average() }
        return nomi.map { n ->
            val rel = Economia.forzaRelativa(forza.getValue(n), forza.values)
            val cap = 50_000 - divisione * 9_000 + 2_000
            val gate = Economia.incassoStadio(cap, Economia.prezzoBiglietto("Italia", divisione), rel, nomi.size)
            val premio = Economia.premioStagione(divisione, rel)
            val ingaggi = righe.filter { it[0] == n }.sumOf { it[5].toLong() }
            val staff = 10L * ((55 - divisione * 6 - 30).coerceAtLeast(5) * 12_000L)
            val sponsor = listOf(1.0, 0.6, 0.45).sumOf { Sponsors.QUOTA_SPONSOR * Market.valueAt(forza.getValue(n).toInt()) * it * 0.95 }.toLong()
            gate + premio + sponsor + Sponsorship.merch(cap) - ingaggi - staff - 800_000L
        }
    }

    @Test fun squadraMediaInPariOUtile() {
        val a = saldi("Serie A", 0).sorted(); val b = saldi("Serie B", 1).sorted()
        assertTrue("Serie A mediana ${a[a.size / 2]}", a[a.size / 2] > 0L)
        assertTrue("Serie B mediana ${b[b.size / 2]}", b[b.size / 2] > -500_000L)
        assertTrue("Nessuna di A perde più di 8 M: ${a.first()}", a.first() > -8_000_000L)
    }

    @Test fun sondaMedianeTEMP() {
        val a = saldi("Serie A", 0).sorted(); val b = saldi("Serie B", 1).sorted()
        val c = listOf("Serie C Girone A", "Serie C Girone B", "Serie C Girone C").flatMap { saldi(it, 3) }.sorted()
        org.junit.Assert.fail("SONDA A med=${a[a.size / 2]} min=${a.first()} max=${a.last()} | B med=${b[b.size / 2]} min=${b.first()} | C med=${c[c.size / 2]} min=${c.first()} n=${c.size}")
    }

    @Test fun premioVittoriaSponsor() {
        assertEquals(60_000L, Sponsors.premioVittoria(0L, false))
        assertEquals(75_000L, Sponsors.premioVittoria(0L, true))
        assertEquals(200_000L, Sponsors.premioVittoria(10_000_000L, false))
    }

    @Test fun nuovoProprietarioSalvaLaSocieta() {
        assertTrue(NuovoProprietario.inCrisi(0L, 0L, 10_000_000L))
        assertTrue(NuovoProprietario.inCrisi(400_000L, 5_000_000L, 10_000_000L))
        assertFalse(NuovoProprietario.inCrisi(600_000L, 5_000_000L, 10_000_000L))
        assertFalse(NuovoProprietario.inCrisi(100_000L, 0L, 10_000_000L))
        assertEquals(8_000_000L, NuovoProprietario.ricapitalizzazione(1_000_000L, 0))
        assertEquals(15_000_000L, NuovoProprietario.ricapitalizzazione(50_000_000L, 0))
        assertEquals(800_000L, NuovoProprietario.ricapitalizzazione(0L, 7))
        val r = kotlin.random.Random(1)
        repeat(20) { assertTrue(NuovoProprietario.nome(r, "Alpi Capital") != "Alpi Capital") }
    }

    @Test fun debitoMenoPunitivo() {
        assertEquals(0L, Mutui.taglio(25_000_000L))
        assertEquals(200_000L, Mutui.taglio(35_000_000L))
    }
}
