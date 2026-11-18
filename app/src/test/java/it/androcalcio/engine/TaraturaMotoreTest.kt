package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Taratura del motore: simula campionati interi con rose generate come nel gioco
 * e stampa media gol, pareggi e vittorie in casa.
 */
class TaraturaMotoreTest {

    private val roles = List(2) { "P" } + List(6) { "D" } + List(6) { "C" } + List(4) { "A" }

    private fun squadra(id: Long, tier: Int, rng: Random): Pair<Team, List<Player>> {
        val t = Team(id = id, name = "S$id", shortName = "S$id", colorPrimary = 0, colorSecondary = 0,
            budget = 0, stadiumCapacity = 0, ticketPrice = 0, sponsor = "")
        val base = 82 - tier * 9 + rng.nextInt(-4, 5)
        val ps = roles.mapIndexed { slot, role ->
            val age = rng.nextInt(17, 37)
            val star = if (slot == 2 || slot == 8 || slot == 14 || slot == 0) 4 else 0
            val ov = (base + star + rng.nextInt(-9, 9) - if (age < 21) 6 else 0).coerceIn(35, 95)
            Attr.fill(Player(teamId = id, name = "G$slot", role = role, age = age, overall = ov,
                morale = 70, salary = 0, nationality = "ITA", potential = ov, contract = 2), rng)
        }
        return t to ps
    }

    private data class Stat(var n: Int = 0, var gol: Int = 0, var x: Int = 0, var casa: Int = 0, var zero: Int = 0, var cambi: Int = 0)

    private fun campionato(tier: Int, stagioni: Int, rng: Random): Stat {
        val st = Stat()
        repeat(stagioni) {
            val sq = (1L..20L).map { squadra(it, tier, rng) }
            for (h in sq) for (a in sq) if (h !== a) {
                val r = MatchEngine.simulate(h.first, h.second, a.first, a.second,
                    Formation.values().random(rng), Formation.values().random(rng), rng = rng)
                st.n++; st.gol += r.homeGoals + r.awayGoals
                if (r.homeGoals == r.awayGoals) st.x++
                if (r.homeGoals > r.awayGoals) st.casa++
                if (r.homeGoals + r.awayGoals == 0) st.zero++
                st.cambi += r.events.count { it.type == EventType.SUB }
            }
        }
        return st
    }

    @Test
    fun stampaStatistiche() {
        val rng = Random(2026)
        for (tier in 0..2) {
            val s = campionato(tier, 3, rng)
            println(String.format(java.util.Locale.ITALY,
                "TARATURA divisione %d: partite %d, gol/partita %.2f, pareggi %.1f%%, vittorie casa %.1f%%, 0-0 %.1f%%, cambi per squadra %.1f",
                tier + 1, s.n, s.gol.toDouble() / s.n, 100.0 * s.x / s.n, 100.0 * s.casa / s.n, 100.0 * s.zero / s.n, s.cambi / (2.0 * s.n)))
            // Intervalli realistici: se una modifica li rompe, la build fallisce
            val gol = s.gol.toDouble() / s.n
            val x = 100.0 * s.x / s.n
            val casa = 100.0 * s.casa / s.n
            assertTrue("Divisione ${tier + 1}: gol/partita $gol fuori da 2,4-3,0", gol in 2.4..3.0)
            assertTrue("Divisione ${tier + 1}: pareggi $x% fuori da 22-30%", x in 22.0..30.0)
            assertTrue("Divisione ${tier + 1}: vittorie in casa $casa% fuori da 42-49%", casa in 42.0..49.0)
        }
    }
}
