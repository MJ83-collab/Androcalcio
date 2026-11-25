package it.androcalcio.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class MezzeStelleTest {
    @Test fun scoutingDa1a10() {
        assertEquals(1, ScoutingGiovani.mezzeStelle(0))
        assertEquals(2, ScoutingGiovani.mezzeStelle(30))    // 30 → una stella
        assertEquals(10, ScoutingGiovani.mezzeStelle(85))   // 85+ → cinque stelle
        assertEquals(10, ScoutingGiovani.mezzeStelle(99))
        assertEquals(ScoutingGiovani.stelle(60), (ScoutingGiovani.mezzeStelle(60) + 1) / 2)
    }

    @Test fun maglieDa1a10() {
        assertEquals(1, Maglie.mezzeStelle(0))
        assertEquals(5, Maglie.mezzeStelle(55))
        assertEquals(10, Maglie.mezzeStelle(100))
    }
}
