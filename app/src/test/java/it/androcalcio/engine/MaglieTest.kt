package it.androcalcio.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MaglieTest {
    private val rosso = 0xFFC62828L
    private val bianco = 0xFFF5F5F5L

    @Test fun ottoMarchiConStiliPropri() {
        assertEquals(8, Maglie.MARCHI.size)
        assertEquals(8, Maglie.MARCHI.map { it.nome }.toSet().size)
        Maglie.MARCHI.forEach { assertTrue(it.nome, it.stili.size >= 4) }
        // Salvataggi vecchi: un nome qualsiasi diventa sempre lo stesso marchio
        assertEquals(Maglie.marchioDi("Sportivo Kappaflex"), Maglie.marchioDi("Sportivo Kappaflex"))
        assertEquals(Maglie.CLUB, Maglie.marchioDi(null))
        assertEquals("Velka", Maglie.marchioDi("Velka").nome)
    }

    @Test fun divisaSiSalvaESiRilegge() {
        val d = Maglie.genera(Maglie.MARCHI[2], rosso, bianco, Random(1))
        assertEquals(d, Maglie.Divisa.decode(d.encode()))
        val p = Maglie.proposte(Maglie.MARCHI[0], rosso, bianco, 42L)
        assertEquals(p, Maglie.decodeProposte(Maglie.encodeProposte(p)))
    }

    @Test fun treProposteStabiliEDelMarchio() {
        val m = Maglie.MARCHI[3]
        val a = Maglie.proposte(m, rosso, bianco, 7L)
        assertEquals(3, a.size)
        assertEquals(a, Maglie.proposte(m, rosso, bianco, 7L))
        a.forEach { d ->
            assertEquals(m.nome, d.marchio)
            d.kit.forEach { assertTrue(it.stile in m.stili) }
            assertEquals(rosso, d.casa.c1)            // casa sempre col colore del club
            assertNotEquals(d.casa.c1, d.terza.c1)    // terza di un altro colore
        }
    }

    @Test fun venditeMedieComeIlVecchioMerchandising() {
        val v = Maglie.vendite(40_000, 0, 0, 50, 0.5, 0, false, 50, 0, 0)
        val vecchio = Sponsorship.merch(40_000)
        assertTrue("${v.ricavo} vs $vecchio", kotlin.math.abs(v.ricavo - vecchio) <= 2_000)
    }

    @Test fun prezzoEsageratoPesaDiPiuNelleSerieMinori() {
        fun quota(div: Int) = Maglie.vendite(20_000, div, Maglie.prezzoStandard(div) * 2, 50, 0.5, 0, false, 50, 0, 0).ricavo.toDouble() /
            Maglie.vendite(20_000, div, 0, 50, 0.5, 0, false, 50, 0, 0).ricavo
        assertTrue(quota(3) < quota(0))
        assertTrue(quota(3) < 0.6)
    }

    @Test fun piuTifosiCampioniENovitaVendonoDiPiu() {
        val base = Maglie.vendite(30_000, 0, 0, 50, 0.5, 0, false, 50, 0, 0).maglie
        assertTrue(Maglie.vendite(30_000, 0, 0, 90, 0.5, 0, false, 50, 0, 0).maglie > base)
        assertTrue(Maglie.vendite(30_000, 0, 0, 50, 1.0, 0, false, 50, 0, 0).maglie > base)
        assertTrue(Maglie.vendite(30_000, 0, 0, 50, 0.5, 2, false, 50, 0, 0).maglie > base)
        assertTrue(Maglie.vendite(30_000, 0, 0, 50, 0.5, 0, true, 50, 0, 0).maglie > base)
        assertTrue(Maglie.vendite(30_000, 0, 0, 50, 0.5, 0, false, 90, 0, 0).maglie > base)
        assertTrue(Maglie.vendite(30_000, 0, 0, 50, 0.5, 0, false, 50, 2, 1).maglie > base)
    }

    @Test fun sponsorTecnicoEUnMarchio() {
        val nomi = Maglie.MARCHI.map { it.nome }.toSet()
        repeat(20) { i ->
            Sponsors.offers(emptyList(), 75, 1.0, Random(i), 3).filter { it.tipo == "tecnico" }.forEach { assertTrue(it.name in nomi) }
        }
    }
}
