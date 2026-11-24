package it.androcalcio.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StadioPersonaleTest {
    private val prezziStd = Abbonamenti.SETTORI.associate { it.id to 100 }

    @Test fun senzaMigliorieQuoteComePrima() {
        val s = StadioPersonale.settori(emptySet())
        Abbonamenti.SETTORI.zip(s).forEach { (a, b) -> assertEquals(a.quota, b.quota, 1e-9) }
    }

    @Test fun quoteSempreSommanoUno() {
        val tutte = Migliorie.TUTTE.map { it.id }.toSet()
        assertEquals(1.0, StadioPersonale.settori(tutte).sumOf { it.quota }, 1e-9)
        val conSkybox = StadioPersonale.settori(setOf("skybox", "hospitality")).first { it.id == "vip" }.quota
        assertTrue(conSkybox > 0.10)
    }

    @Test fun prezziStandardNonCambianoGliIncassi() {
        // Nessuna squadra deve guadagnare meno di prima con i prezzi di partenza, qualunque stadio abbia
        val tutte = Migliorie.TUTTE.map { it.id }.toSet()
        listOf(emptySet(), setOf("curva_n", "curva_s"), tutte).forEach { f ->
            val r = Abbonamenti.fattore(30_000, 30, 0.5, 60, prezziStd, 19, StadioPersonale.settori(f))
            assertTrue(r in 0.999..1.001)
        }
    }

    @Test fun curvaPiuSensibileDelVip() {
        val curva = Abbonamenti.SETTORI.first { it.id == "curva" }
        val vip = Abbonamenti.SETTORI.first { it.id == "vip" }
        val calo = { s: Abbonamenti.Settore -> Abbonamenti.riempimento(0.5, 60, 150, s.elasticita) / Abbonamenti.riempimento(0.5, 60, 100, s.elasticita) }
        assertTrue(calo(curva) < calo(vip))
    }

    @Test fun atmosferaSaleConLeStrutture() {
        val senza = StadioPersonale.atmosfera(50, emptySet(), 0.7)
        val con = StadioPersonale.atmosfera(50, setOf("copertura", "tribuna", "faretti", "maxischermo"), 0.7)
        assertEquals(50, senza)
        assertTrue(con > senza)
        assertTrue(StadioPersonale.atmosfera(50, emptySet(), 1.0) > StadioPersonale.atmosfera(50, emptySet(), 0.3))
        assertEquals(100, StadioPersonale.atmosfera(99, Migliorie.TUTTE.map { it.id }.toSet(), 1.0))
    }

    @Test fun vantaggioCasaPiccolo() {
        assertEquals(1.05, Atmosfera.fattore(100), 1e-9)
        assertEquals(0.95, Atmosfera.fattore(0), 1e-9)
    }

    @Test fun nomeEColori() {
        assertEquals("Stadio Nuovo", StadioPersonale.nomeValido("  Stadio   Nuovo "))
        assertNull(StadioPersonale.nomeValido("ab"))
        assertNull(StadioPersonale.nomeValido("x".repeat(40)))
        assertEquals(12, StadioPersonale.COLORI.size)
        assertEquals(StadioPersonale.COLORI.last(), StadioPersonale.colore(99))
    }

    @Test fun sitoEStoreAPassi() {
        val sito = Migliorie.trova("sito")!!
        assertEquals("Serve prima: Store ufficiale", Migliorie.avviabile(sito, emptySet(), emptyMap(), 10_000_000))
        assertNull(Migliorie.avviabile(sito, setOf("store"), emptyMap(), 10_000_000))
        assertEquals(2, StadioPersonale.negozi(setOf("store", "store2")))
        assertEquals(3, StadioPersonale.livelloSito(setOf("sito", "sito2", "sito3")))
    }

    @Test fun stimeCoerenti() {
        val st = StadioPersonale.stime(30_000, 30, 0.5, 60, prezziStd, emptySet())
        assertEquals(4, st.size)
        assertTrue(st.all { it.incassoPartita > 0 && it.riempimento in 0.05..1.0 })
        assertTrue(st.first { it.settore.id == "vip" }.prezzoEuro > st.first { it.settore.id == "curva" }.prezzoEuro)
    }
}
