package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantCoachTest {

    private val team = Team(id = 1, name = "Casa", shortName = "CAS", colorPrimary = 0xFF0000FFL,
        colorSecondary = 0xFFFFFFFFL, budget = 0, stadiumCapacity = 1000, ticketPrice = 10, sponsor = "")

    private fun p(id: Long, role: String, overall: Int = 65, condition: Int = 100, injured: Int = 0, name: String = "G$id") =
        Player(id = id, teamId = 1, name = name, role = role, age = 25, overall = overall, morale = 70,
            salary = 1000, nationality = "IT", condition = condition, injuredRounds = injured)

    private fun rosa(): List<Player> {
        val ruoli = listOf("P") + List(5) { "D" } + List(5) { "C" } + List(4) { "A" }
        return ruoli.mapIndexed { i, r -> p(i + 1L, r, overall = 60 + i % 7) }
    }

    @Test
    fun undiciSenzaInfortunati() {
        val ps = rosa().mapIndexed { i, x -> if (i == 3) x.copy(injuredRounds = 2) else x }
        val xi = AssistantCoach.startingXI(ps, Formation.F442)
        assertEquals(11, xi.size)
        assertTrue(xi.none { it.injuredRounds > 0 })
    }

    @Test
    fun stanchiSegnalatiConSostituto() {
        val ps = rosa().map { if (it.id == 6L) it.copy(condition = 40, overall = 85, name = "Stanco") else it }
        val xi = AssistantCoach.startingXI(ps, Formation.F442)
        val notes = AssistantCoach.xiNotes(ps, xi)
        if (xi.any { it.id == 6L }) {
            assertTrue(notes.any { it.contains("Stanco") && it.contains("stanco") })
        }
    }

    @Test
    fun fuoriPerInfortunioNegliAvvisi() {
        val ps = rosa().mapIndexed { i, x -> if (i == 0) x.copy(injuredRounds = 3, name = "Infortunato") else x }
        val notes = AssistantCoach.xiNotes(ps, AssistantCoach.startingXI(ps, Formation.F442))
        assertTrue(notes.any { it.contains("Infortunato") })
    }

    @Test
    fun notizieSullAvversario() {
        val avversario = rosa().map { it.copy(teamId = 2, id = it.id + 100, name = "Av${it.id}") }
        val notes = AssistantCoach.opponentNotes(team.copy(id = 2, shortName = "AVV"), avversario, rosa(), 50, 50, Formation.F442)
        assertTrue(notes.isNotEmpty())
        assertTrue(notes.any { it.contains("uomo più pericoloso") })
        assertTrue(notes.any { it.startsWith("Di solito") && it.contains("gioca con il") })
    }

    @Test
    fun loSchemaAbitualeEStabile() {
        for (id in 1L..50L) assertEquals(schemaAbituale(id), schemaAbituale(id))
    }

    @Test
    fun laPartitaUsaSpessoLoSchemaAbituale() {
        val rng = kotlin.random.Random(3)
        val abituale = schemaAbituale(9L)
        val uguali = (1..1000).count { schemaInPartita(9L, rng) == abituale }
        assertTrue("schema abituale usato solo $uguali volte su 1000", uguali in 780..920)
    }

    @Test
    fun tatticaAttaccoSeSiamoPiuForti() {
        val mine = rosa().map { it.copy(overall = 80) }
        val loro = rosa().map { it.copy(overall = 50) }
        val r = AssistantCoach.tattica(loro, mine, 99L, 50, 50, Formation.F442, Stile.DIFESA)
        assertEquals(Stile.ATTACCO, r?.first)
    }

    @Test
    fun tatticaDifesaSeSiamoPiuDeboli() {
        val mine = rosa().map { it.copy(overall = 50) }
        val loro = rosa().map { it.copy(overall = 80) }
        val r = AssistantCoach.tattica(loro, mine, 99L, 50, 50, Formation.F442, Stile.ATTACCO)
        assertEquals(Stile.DIFESA, r?.first)
    }

    @Test
    fun tatticaNessunConsiglioSenzaAllenatore() {
        assertEquals(null, AssistantCoach.tattica(rosa(), rosa(), 99L, 50, 50, Formation.F442, null))
    }
}
