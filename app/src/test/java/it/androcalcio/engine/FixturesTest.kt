package it.androcalcio.engine

import org.junit.Assert.*
import org.junit.Test

class FixturesTest {
    @Test fun ognunaControOgniAltraAndataERitorno() {
        for (n in listOf(2, 3, 4, 7, 18, 20)) {
            val ids = (1L..n).toList()
            val ms = Fixtures.generate(ids, "2026", "X")
            assertEquals("n=$n", n * (n - 1), ms.size)
            val coppie = ms.map { it.homeTeamId to it.awayTeamId }
            assertEquals(coppie.size, coppie.toSet().size)
            ids.forEach { a -> ids.filter { it != a }.forEach { b -> assertTrue(a to b in coppie) } }
        }
    }

    @Test fun nessunaSquadraGiocaDueVolteNellaStessaGiornata() {
        for (n in listOf(3, 6, 19, 20)) {
            val ms = Fixtures.generate((1L..n).toList(), "2026", "X")
            ms.groupBy { it.round }.forEach { (r, g) ->
                val squadre = g.flatMap { listOf(it.homeTeamId, it.awayTeamId) }
                assertEquals("n=$n giornata $r", squadre.size, squadre.toSet().size)
            }
        }
    }

    @Test fun listaVuotaOSingola() {
        assertTrue(Fixtures.generate(emptyList(), "s", "l").isEmpty())
        assertTrue(Fixtures.generate(listOf(1L), "s", "l").isEmpty())
    }
}
