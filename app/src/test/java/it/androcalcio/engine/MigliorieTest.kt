package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class MigliorieTest {
    @Test fun catalogoCompleto() {
        assertTrue(Migliorie.TUTTE.size >= 30)
        assertEquals(Migliorie.TUTTE.size, Migliorie.TUTTE.map { it.id }.toSet().size)
        Migliorie.TUTTE.forEach { m ->
            assertTrue(m.costo > 0 && m.giornate > 0)
            m.richiede.forEach { assertNotNull("requisito $it", Migliorie.trova(it)) }
        }
    }

    @Test fun codificaECantieri() {
        val c = mapOf("curva_n" to 8, "store" to 2)
        assertEquals(c, Migliorie.decodeCantieri(Migliorie.encodeCantieri(c)))
        val (rimasti, finite) = Migliorie.avanza(c, 3)
        assertEquals(mapOf("curva_n" to 5), rimasti)
        assertEquals(listOf("store"), finite.map { it.id })
        assertEquals(setOf("a").filter { false }.toSet(), Migliorie.decodeFatte("a,xx"))
    }

    @Test fun requisitiEBudget() {
        val anello = Migliorie.trova("anello")!!
        assertNotNull(Migliorie.avviabile(anello, emptySet(), emptyMap(), 99_000_000))
        assertNull(Migliorie.avviabile(anello, setOf("curva_n", "curva_s"), emptyMap(), 99_000_000))
        assertEquals("Budget insufficiente", Migliorie.avviabile(anello, setOf("curva_n", "curva_s"), emptyMap(), 1))
    }

    @Test fun lePiuCommercialiSiRipagano() {
        val store = Migliorie.trova("store")!!
        val r = Migliorie.ritornoAnnuo(store, 5_000_000, 200.0)
        assertTrue(r > 0)
        assertTrue(Migliorie.stagioniPerRipagare(store, r)!! < 6)
        assertEquals(EffettiMiglioria(merchAnno = 300_000, manutenzione = 36_000), Migliorie.effetti(setOf("store")))
    }

    @Test fun prevenzioneInfortuni() {
        fun p(id: Long, inj: Int) = Player(id = id, teamId = 1, name = "X", role = "D", age = 25, overall = 60,
            morale = 50, salary = 0, nationality = "Italia", injuredRounds = inj)
        val prima = listOf(p(1, 0), p(2, 3))
        val dopo = listOf(p(1, 2), p(2, 3))
        val r = Migliorie.prevenzione(prima, dopo, 100, Random(1))
        assertEquals(0, r[0].injuredRounds)
        assertEquals(3, r[1].injuredRounds)
    }
}
