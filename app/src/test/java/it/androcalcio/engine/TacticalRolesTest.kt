package it.androcalcio.engine

import it.androcalcio.data.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class TacticalRolesTest {

    private fun player(pos: String, ruolo: String = "", sec2: String = "", aff2: Int = 0, sec3: String = "", aff3: Int = 0) =
        Player(
            teamId = 1, name = "Test", role = "C", age = 25, overall = 70, morale = 70, salary = 1000,
            nationality = "IT", pos = pos, ruoloPartita = ruolo, sec2 = sec2, aff2 = aff2, sec3 = sec3, aff3 = aff3
        )

    @Test
    fun ruoloNaturaleHaEfficaciaPiena() {
        val p = player("MED", sec2 = "REG", aff2 = 70)
        assertEquals(1.0, TacticalRoles.factor(p), 1e-9)
    }

    @Test
    fun ruoloAffineUsaLAffinita() {
        val p = player("MED", ruolo = "REG", sec2 = "REG", aff2 = 70, sec3 = "INT", aff3 = 50)
        assertEquals(0.7, TacticalRoles.factor(p), 1e-9)
    }

    @Test
    fun terzoRuoloUsaLaSuaAffinita() {
        val p = player("MED", ruolo = "INT", sec2 = "REG", aff2 = 70, sec3 = "INT", aff3 = 50)
        assertEquals(0.5, TacticalRoles.factor(p), 1e-9)
    }

    @Test
    fun ruoloNonAffineNonPenalizza() {
        // Ruolo non presente tra naturale e affini: nessuna penalità (fallback sicuro)
        val p = player("MED", ruolo = "CA", sec2 = "REG", aff2 = 70)
        assertEquals(1.0, TacticalRoles.factor(p), 1e-9)
    }

    @Test
    fun opzioniPartonoDalNaturale() {
        val p = player("TD", sec2 = "TS", aff2 = 70, sec3 = "DC", aff3 = 40)
        assertEquals(listOf("TD" to 100, "TS" to 70, "DC" to 40), TacticalRoles.options(p))
    }

    @Test
    fun affiniDiTuttiISeiRuoliStannoNellaStessaLinea() {
        TacticalRoles.AFFINI.forEach { (pos, affini) ->
            val lineaDi = { r: String -> when (r) { "TD", "TS", "DC" -> "D"; "MED", "REG", "INT", "MP" -> "C"; else -> "A" } }
            affini.forEach { (r, _) -> assertEquals("$pos -> $r", lineaDi(pos), lineaDi(r)) }
        }
    }
}
