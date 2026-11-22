package it.androcalcio.engine

import java.time.DayOfWeek
import org.junit.Assert.*
import org.junit.Test

class DateGiornateTest {
    @Test fun domenicheCrescentiConSosta() {
        var prima = DateGiornate.data(2026, 1)
        assertEquals(DayOfWeek.SUNDAY, prima.dayOfWeek)
        assertEquals(8, prima.monthValue)
        for (g in 2..38) {
            val d = DateGiornate.data(2026, g)
            assertEquals(DayOfWeek.SUNDAY, d.dayOfWeek)
            assertTrue(d.isAfter(prima)); prima = d
            assertFalse("giornata $g nella sosta", d.monthValue == 12 && d.dayOfMonth >= 22 || d.monthValue == 1 && d.dayOfMonth <= 4)
        }
        assertEquals(2026, DateGiornate.anno("2026/27"))
        assertTrue(DateGiornate.testo(2026, 1).startsWith("dom "))
    }
}
