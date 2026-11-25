package it.androcalcio.engine

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SquadraBFedeltaTest {
    @Test fun partitaSquadraBPiuForteVinceDiPiu() {
        val rng = Random(3)
        var forte = 0; var debole = 0
        repeat(2000) {
            val (a, b) = SquadraB.partita(70.0, 60.0, 80, rng); if (a > b) forte++
            val (c, d) = SquadraB.partita(60.0, 70.0, 20, rng); if (c > d) debole++
        }
        assertTrue("forte $forte debole $debole", forte > debole)
    }

    @Test fun bonusEsitoEStoricoRisultati() {
        assertEquals(0.04, SquadraB.bonusEsito(2, 0), 1e-9)
        assertEquals(0.02, SquadraB.bonusEsito(1, 1), 1e-9)
        assertEquals(0.0, SquadraB.bonusEsito(0, 3), 1e-9)
        var s: String? = null
        repeat(12) { s = SquadraB.aggiungiRisultato(s, "$it-0:X") }
        assertEquals(8, s!!.split("|").size)
        assertTrue(s!!.endsWith("11-0:X"))
    }

    @Test fun fedeltaPerIdNonConfondeIOmonimi() {
        val v = mapOf("Rossi" to 2)   // vecchia chiave a nome: vale una volta sola
        val n1 = Fedelta.avanzaId(v, listOf(1L to "Rossi", 2L to "Rossi"))
        assertEquals(3, n1["1"]); assertEquals(3, n1["2"])
        val n2 = Fedelta.avanzaId(n1, listOf(1L to "Rossi"))
        assertEquals(4, n2["1"]); assertEquals(null, n2["2"])
    }
}
