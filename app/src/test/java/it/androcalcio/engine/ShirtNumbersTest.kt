package it.androcalcio.engine

import it.androcalcio.data.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShirtNumbersTest {

    private fun p(id: Long, role: String, overall: Int = 60, numero: Int = 0) =
        Player(id = id, teamId = 1, name = "P$id", role = role, age = 25, overall = overall, morale = 70,
            salary = 1000, nationality = "IT", numero = numero)

    private fun rosa() = listOf(
        p(1, "P"), p(2, "D", 70), p(3, "D", 65), p(4, "D", 60), p(5, "D", 58),
        p(6, "C", 72), p(7, "C", 66), p(8, "C", 61), p(9, "C", 59),
        p(10, "A", 75), p(11, "A", 68), p(12, "A", 55), p(13, "P", 50), p(14, "D", 50)
    )

    @Test
    fun numeriUniciEValidi() {
        val nums = ShirtNumbers.assign(rosa())
        assertEquals(14, nums.size)
        assertEquals(14, nums.values.toSet().size)
        assertTrue(nums.values.all { it in 1..99 })
    }

    @Test
    fun ilPortiereHaIlNumeroUno() {
        val nums = ShirtNumbers.assign(rosa())
        assertEquals(1, nums[1L])
    }

    @Test
    fun chiHaUnNumeroLoMantiene() {
        // Il difensore con numero 9 lo tiene anche se ha un overall basso
        val ps = listOf(p(1, "P"), p(2, "D", 40, numero = 9), p(3, "A", 90))
        val nums = ShirtNumbers.assign(ps)
        assertEquals(9, nums[2L])
    }

    @Test
    fun doppioNumeroScartato() {
        val ps = listOf(p(1, "D", 80, numero = 4), p(2, "D", 50, numero = 4))
        val nums = ShirtNumbers.assign(ps)
        assertEquals(4, nums[1L])
        assertTrue(nums[2L] != 4 && nums[2L] != null)
    }

    @Test
    fun stessaRosaStessiNumeriDopoRiassegnazione() {
        val prima = ShirtNumbers.assign(rosa())
        val conNumeri = rosa().map { it.copy(numero = prima[it.id] ?: 0) }
        assertEquals(prima, ShirtNumbers.assign(conNumeri))
    }
}
