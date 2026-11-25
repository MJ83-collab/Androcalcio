package it.androcalcio.engine

import kotlin.random.Random
import org.junit.Assert.assertTrue
import org.junit.Test

/** Il Cinico conta anche nelle serie di rigori simulate (Cup.finalize). */
class CinicoRigoriSimulatiTest {
    private fun quotaCasa(cinH: Int, cinA: Int): Double {
        val rng = Random(7)
        val par = Cup.superFinal("2026", "k", 1L, 2L).copy(played = true)
        var casa = 0
        var tot = 0
        repeat(40000) {
            val r = Cup.finalize(par, emptyList(), rng, mapOf(1L to cinH, 2L to cinA))
            if (r.homePens >= 0) {
                tot++
                if (r.homePens > r.awayPens) casa++
            }
        }
        assertTrue("servono serie di rigori", tot > 5000)
        return casa.toDouble() / tot
    }

    @Test fun senzaCiniciEquilibrio() {
        val q = quotaCasa(0, 0)
        assertTrue("quota $q", q in 0.47..0.53)
    }

    @Test fun ciniciVinconoDiPiu() {
        assertTrue(quotaCasa(5, 0) > 0.53)
        assertTrue(quotaCasa(0, 5) < 0.47)
    }
}
