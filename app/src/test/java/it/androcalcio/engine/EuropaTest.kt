package it.androcalcio.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EuropaTest {
    private val classifica = (1L..20L).toList()
    private val italia = Europa.Posti(4)

    @Test
    fun primiQuattroInChampionsQuintaESestaInEuropaConCoppa() {
        val e = Europa.assegna(classifica, vincitriceCoppa = 12L, posti = italia)
        assertEquals(Europa.CHAMPIONS, e[1L]); assertEquals(Europa.CHAMPIONS, e[4L])
        assertEquals(Europa.EUROPA, e[5L]); assertEquals(Europa.EUROPA, e[12L])
        assertEquals(Europa.CONFERENCE, e[6L])
        assertNull(e[7L])
    }

    @Test
    fun vincitriceGiaQualificataSpostaLaEuropaAlSesto() {
        val e = Europa.assegna(classifica, vincitriceCoppa = 2L, posti = italia)
        assertEquals(Europa.CHAMPIONS, e[2L])
        assertEquals(Europa.EUROPA, e[5L]); assertEquals(Europa.EUROPA, e[6L])
        assertEquals(Europa.CONFERENCE, e[7L])
    }

    @Test
    fun vincitriceQuintaNonOccupaDueVolte() {
        val e = Europa.assegna(classifica, vincitriceCoppa = 5L, posti = italia)
        assertEquals(2, e.values.count { it == Europa.EUROPA })
        assertEquals(Europa.CONFERENCE, e[7L])
    }

    @Test
    fun spagnaEInghilterraHannoUnPostoInChampionsInPiu() {
        val e = Europa.assegna(classifica, vincitriceCoppa = null, posti = Europa.POSTI.getValue("Spagna"))
        assertEquals(Europa.CHAMPIONS, e[5L])
        assertEquals(Europa.EUROPA, e[6L]); assertEquals(Europa.EUROPA, e[7L])
        assertEquals(Europa.CONFERENCE, e[8L])
    }

    @Test
    fun sudamericanaDalSettimoAlDodicesimo() {
        val e = Sudamericana.assegna(classifica)
        assertEquals(6, e.size)
        assertEquals(Sudamericana.NOME, e[7L]); assertEquals(Sudamericana.NOME, e[12L])
        assertNull(e[6L]); assertNull(e[13L])
    }
}
