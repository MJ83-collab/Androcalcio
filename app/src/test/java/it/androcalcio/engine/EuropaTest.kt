package it.androcalcio.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class EuropaTest {
    private val classifica = (1L..20L).toList()

    @Test
    fun primiQuattroInChampionsQuintaEVincitriceInEuropa() {
        val e = Europa.assegna(classifica, vincitriceCoppa = 12L)
        assertEquals(Europa.CHAMPIONS, e[1L]); assertEquals(Europa.CHAMPIONS, e[4L])
        assertEquals(Europa.EUROPA, e[5L]); assertEquals(Europa.EUROPA, e[12L])
    }

    @Test
    fun sestaInConferenceSenzaVincitriceFuoriTop() {
        val e = Europa.assegna(classifica, vincitriceCoppa = 12L)
        assertEquals(Europa.CONFERENCE, e[6L])
        assertEquals(null, e[7L])
    }

    @Test
    fun vincitriceGiaQualificataNonOccupaUnPostoInPiu() {
        val e = Europa.assegna(classifica, vincitriceCoppa = 2L)
        assertEquals(Europa.CHAMPIONS, e[2L])
        assertEquals(Europa.CONFERENCE, e[6L])
        assertEquals(1, e.values.count { it == Europa.EUROPA })
    }

    @Test
    fun vincitriceSestaPassaLaConferenceAlSettimo() {
        val e = Europa.assegna(classifica, vincitriceCoppa = 6L)
        assertEquals(Europa.EUROPA, e[6L])
        assertEquals(Europa.CONFERENCE, e[7L])
    }
}
