package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MeccanicheTest {

    private val home = Team(id = 1, name = "Casa", shortName = "CAS", colorPrimary = 0xFF0000FFL,
        colorSecondary = 0xFFFFFFFFL, budget = 0, stadiumCapacity = 1000, ticketPrice = 10, sponsor = "")
    private val away = Team(id = 2, name = "Fuori", shortName = "FUO", colorPrimary = 0xFFFF0000L,
        colorSecondary = 0xFF000000L, budget = 0, stadiumCapacity = 1000, ticketPrice = 10, sponsor = "")

    private fun player(morale: Int) =
        Player(teamId = 1, name = "T", role = "C", age = 25, overall = 70, morale = morale, salary = 1000, nationality = "IT")

    private fun squad(base: Long, teamId: Long, morale: Int = 70): List<Player> {
        val roles = listOf("P") + List(4) { "D" } + List(4) { "C" } + List(2) { "A" }
        return roles.mapIndexed { i, r ->
            Player(id = base + i, teamId = teamId, name = "$teamId-$i", role = r, age = 25, overall = 60 + i,
                morale = morale, salary = 1000, nationality = "IT")
        }
    }

    private fun play(seed: Int): MatchReport {
        val m = LiveMatch(home, squad(1, 1), away, squad(101, 2), Formation.F442, Formation.F442,
            50, 50, null, null, null, null, Random(seed))
        m.runToEnd()
        return m.report()
    }

    @Test
    fun moraleAgliEstremi() {
        assertEquals(0.95, MatchEngine.moraleFactor(player(0)), 1e-9)
        assertEquals(1.00, MatchEngine.moraleFactor(player(50)), 1e-9)
        assertEquals(1.05, MatchEngine.moraleFactor(player(100)), 1e-9)
    }

    @Test
    fun pagelleRestanoNeiLimiti() {
        for (seed in 1..30) {
            val r = play(seed)
            assertTrue("pagelle mancanti (seed $seed)", r.playerRatings.isNotEmpty())
            r.playerRatings.values.forEach { v ->
                assertTrue("pagella $v fuori da 3..10 (seed $seed)", v in 3.0..10.0)
                assertEquals("pagella $v non a un decimale", v * 10, Math.round(v * 10).toDouble(), 1e-9)
            }
        }
    }

    @Test
    fun pagelleSoloPerChiHaGiocato() {
        val r = play(7)
        val played = r.playedPlayers.map { it.id }.toSet()
        assertTrue("servono almeno 11 giocatori in campo", played.size >= 11)
        assertTrue("pagelle per chi non ha giocato", r.playerRatings.keys.all { it in played })
    }

    @Test
    fun ilNumeroDiGoalCoincideConGliEventi() {
        for (seed in 1..30) {
            val r = play(seed)
            val goalEvents = r.events.count { it.type == EventType.GOAL }
            assertEquals("gol e eventi non coincidono (seed $seed)", r.homeGoals + r.awayGoals, goalEvents)
        }
    }

    @Test
    fun calciPiazzatiCompaionoNelleSimulazioni() {
        val piazzati = (1..100).sumOf { seed ->
            play(seed).events.count { it.text.startsWith("Rigore") || it.text.startsWith("Gol su Rigore") ||
                it.text.contains("Punizione") || it.text.contains("Gol su Punizione") }
        }
        assertTrue("nessun calcio piazzato in 100 partite", piazzati > 0)
    }
}
