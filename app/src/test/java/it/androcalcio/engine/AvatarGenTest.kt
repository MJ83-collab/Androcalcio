package it.androcalcio.engine

import org.junit.Assert.*
import org.junit.Test

class AvatarGenTest {
    @Test fun stessoNomeStessoVolto() = assertEquals(AvatarGen.daNome("Mario Rossi"), AvatarGen.daNome("Mario Rossi"))

    @Test fun indiciSempreValidi() {
        for (i in 0 until 500) {
            val s = AvatarGen.daNome("Giocatore $i")
            assertEquals(AvatarGen.CAMPI.size, s.v.size)
            s.v.forEachIndexed { k, x -> assertTrue(x in 0 until AvatarGen.CAMPI[k].n) }
        }
    }

    @Test fun molteVarieta() {
        val volti = (0 until 1000).map { AvatarGen.daNome("Nome $it").encode() }.toSet()
        assertTrue("volti diversi: ${volti.size}", volti.size > 990)
    }

    @Test fun barbaERara() {
        val senza = (0 until 1000).count { AvatarGen.daNome("N$it")[AvatarGen.BARBA] == 0 }
        assertTrue(senza in 500..750)
    }

    @Test fun encodeDecodeEMappa() {
        val s = AvatarGen.daNome("Luca").con(AvatarGen.TAGLIO, 13).con(AvatarGen.BARBA, -1)
        assertEquals(s, AvatarGen.decode(s.encode()))
        assertEquals(AvatarGen.CAMPI[AvatarGen.BARBA].n - 1, s[AvatarGen.BARBA])
        val m = mapOf("Luca" to s, "Anna Maria" to AvatarGen.daNome("x"))
        assertEquals(m, AvatarGen.decodeMappa(AvatarGen.encodeMappa(m)))
        assertNull(AvatarGen.decode("1.2.3")); assertNull(AvatarGen.decode(null))
        assertEquals(s, AvatarGen.specDi("Luca", m)); assertEquals(AvatarGen.daNome("Z"), AvatarGen.specDi("Z", m))
    }
}
