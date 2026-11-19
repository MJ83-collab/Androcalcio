package it.androcalcio.engine

import it.androcalcio.data.Team
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class YouthsTest {

    private fun team(skill: Int) = Team(id = 1, name = "Casa", shortName = "CAS", colorPrimary = 0xFF0000FFL,
        colorSecondary = 0xFFFFFFFFL, budget = 20_000_000, stadiumCapacity = 1000, ticketPrice = 10, sponsor = "",
        youthCoachSkill = skill)

    private fun potenzaMedia(skill: Int): Double =
        (1..300).flatMap { Youths.generate(team(skill), "2026", Random(it)) }.map { it.potential }.average()

    @Test
    fun allenatoreMigliorePotenzialePiuAlto() {
        val bassa = potenzaMedia(1)
        val alta = potenzaMedia(99)
        assertTrue("alta=$alta bassa=$bassa", alta - bassa > 3.0)
        assertTrue("differenza troppo ampia", alta - bassa < 10.0)
    }
}
