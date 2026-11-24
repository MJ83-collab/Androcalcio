package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import it.androcalcio.engine.CoppeEuropee.Coppa
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoppeEuropeeTest {
    private var nextId = 1L

    /** Un campionato di prima divisione per ognuna delle undici nazioni europee, con 18 squadre (10 la Scozia). */
    private fun mondo(): List<Team> = CoppeEuropee.PRIME_DIVISIONI.flatMap { (nazione, camp) ->
        List(if (nazione == "Scozia") 10 else 18) {
            Team(id = nextId++, name = "$nazione $it", shortName = "T", colorPrimary = 0, colorSecondary = 0, budget = 0,
                stadiumCapacity = 0, ticketPrice = 0, sponsor = "", nazione = nazione, campionato = camp)
        }
    }

    private fun forzaDi(squadre: List<Team>, rng: Random) = squadre.associate { it.id to 60 + rng.nextDouble() * 25 }

    @Test fun postiTotali() {
        val sq = mondo()
        val q = CoppeEuropee.qualificateIniziali(sq, forzaDi(sq, Random(1)))
        val per = q.values.groupingBy { it }.eachCount()
        assertEquals(32, per[Coppa.CAMPIONI])
        assertEquals(16, per[Coppa.EUROPA])
        assertEquals(16, per[Coppa.COPPE])
        assertEquals(q.size, q.keys.toSet().size)   // una squadra gioca una sola coppa
    }

    @Test fun qualificatePerNazione() {
        val classifica = (1L..18L).toList()
        val q = CoppeEuropee.qualificate("Italia", classifica, 9L, 10L)
        assertEquals(listOf(1L, 2L, 3L, 4L), q.filterValues { it == Coppa.CAMPIONI }.keys.toList())
        assertEquals(setOf(9L, 10L), q.filterValues { it == Coppa.COPPE }.keys)
        assertEquals(listOf(5L, 6L), q.filterValues { it == Coppa.EUROPA }.keys.toList())
        // Paese piccolo: un posto in Coppa Europa e uno in Coppe
        val s = CoppeEuropee.qualificate("Grecia", classifica, 7L, 8L)
        assertEquals(listOf(1L), s.filterValues { it == Coppa.CAMPIONI }.keys.toList())
        assertEquals(setOf(7L), s.filterValues { it == Coppa.COPPE }.keys)
        assertEquals(listOf(2L), s.filterValues { it == Coppa.EUROPA }.keys.toList())
    }

    @Test fun vincitriceGiaInCampioniLasciaIlPostoAllaFinalista() {
        val q = CoppeEuropee.qualificate("Spagna", (1L..18L).toList(), 2L, 12L)
        assertEquals(Coppa.CAMPIONI, q[2L])
        assertEquals(Coppa.COPPE, q[12L])
        // il secondo posto in Coppe scende in classifica, senza doppioni
        assertEquals(2, q.values.count { it == Coppa.COPPE })
        assertEquals(q.size, q.keys.toSet().size)
    }

    @Test fun coppaVintaDaUnaSerieInferiore() {
        val q = CoppeEuropee.qualificate("Italia", (1L..20L).toList(), 500L, 501L)
        assertEquals(Coppa.COPPE, q[500L]); assertEquals(Coppa.COPPE, q[501L])
        assertEquals(2, q.values.count { it == Coppa.EUROPA })
    }

    @Test fun nazioneNonEuropeaNonQualifica() {
        assertTrue(CoppeEuropee.qualificate("Brasile", (1L..10L).toList(), 1L, 2L).isEmpty())
    }

    @Test fun saveERecuperaQualificateEGironi() {
        val sq = mondo()
        val q = CoppeEuropee.qualificateIniziali(sq, forzaDi(sq, Random(2)))
        val dec = CoppeEuropee.decodeQualificate(CoppeEuropee.encodeQualificate(q))
        Coppa.values().forEach { c -> assertEquals(q.filterValues { it == c }.keys.toList(), dec.getValue(c)) }
        val g = listOf(listOf(1L, 2L, 3L, 4L), listOf(5L, 6L, 7L, 8L))
        assertEquals(g, CoppeEuropee.decodeGironi(CoppeEuropee.encodeGironi(g)))
        assertTrue(CoppeEuropee.decodeQualificate(null).values.all { it.isEmpty() })
    }

    @Test fun gironiOttoDaQuattroSenzaConnazionali() {
        val sq = mondo(); val rng = Random(3)
        val forza = forzaDi(sq, rng)
        val q = CoppeEuropee.qualificateIniziali(sq, forza)
        val ids = q.filterValues { it == Coppa.CAMPIONI }.keys.toList()
        val naz = sq.associate { it.id to it.nazione }
        val g = CoppeEuropee.sorteggiaGironi(ids, naz, forza, rng)
        assertEquals(8, g.size)
        assertTrue(g.all { it.size == 4 })
        assertEquals(ids.toSet(), g.flatten().toSet())
        assertTrue(g.all { gr -> gr.map { naz[it] }.toSet().size == 4 })
        // una squadra per urna: nei gironi ci sono 1 testa di serie ciascuno
        val teste = ids.sortedByDescending { forza[it] }.take(8).toSet()
        assertTrue(g.all { gr -> gr.count { it in teste } == 1 })
    }

    @Test fun calendarioGironi() {
        val g = listOf(listOf(1L, 2L, 3L, 4L), listOf(5L, 6L, 7L, 8L))
        val m = CoppeEuropee.partiteGironi(g, "2026/27", "EUROPA|CAMPIONI")
        assertEquals(24, m.size)   // 12 partite per girone
        assertTrue(m.all { CoppeEuropee.inGironi(it.round) && Cup.isCup(it.round) })
        assertEquals(6, m.map { it.round }.distinct().size)
        // ogni squadra gioca una volta a giornata
        m.groupBy { it.round }.values.forEach { giornata ->
            val squadre = giornata.flatMap { listOf(it.homeTeamId, it.awayTeamId) }
            assertEquals(squadre.size, squadre.toSet().size)
        }
    }

    /** Gioca tutto: risultati a caso, supplementari e rigori nei turni a eliminazione, fino al titolo. */
    private fun gioca(coppa: Coppa, ids: List<Long>, sq: List<Team>, forza: Map<Long, Double>, rng: Random): List<MatchResult> {
        val key = CoppeEuropee.key(coppa)
        val naz = sq.associate { it.id to it.nazione }
        val byId = sq.associateBy { it.id }
        val gironi = if (coppa.conGironi) CoppeEuropee.sorteggiaGironi(ids, naz, forza, rng) else emptyList()
        var tutte = if (coppa.conGironi) CoppeEuropee.partiteGironi(gironi, "2026/27", key)
            else CoppeEuropee.primoTurno(ids, forza, "2026/27", key, rng)
        tutte = tutte.mapIndexed { i, m -> m.copy(id = i + 1L) }
        var guardia = 0
        while (guardia++ < 30) {
            val daGiocare = tutte.filter { !it.played }
            if (daGiocare.isEmpty()) {
                val nuove = CoppeEuropee.prossimoTurno(coppa, tutte, gironi, byId, forza, "2026/27", rng)
                if (nuove.isEmpty()) break
                tutte = tutte + nuove.mapIndexed { i, m -> m.copy(id = tutte.size + i + 1L) }
                continue
            }
            val turno = daGiocare.minOf { it.round }
            tutte = tutte.map { m ->
                if (m.round == turno && !m.played) m.copy(homeGoals = rng.nextInt(0, 4), awayGoals = rng.nextInt(0, 3), played = true) else m
            }
            tutte = tutte.map { m -> CoppeEuropee.finalizza(m, tutte, rng) }
        }
        return tutte
    }

    @Test fun coppaDeiCampioniFinoAlTitolo() {
        val sq = mondo(); val rng = Random(4); val forza = forzaDi(sq, rng)
        val ids = CoppeEuropee.qualificateIniziali(sq, forza).filterValues { it == Coppa.CAMPIONI }.keys.toList()
        val p = gioca(Coppa.CAMPIONI, ids, sq, forza, rng)
        assertEquals(96, p.count { CoppeEuropee.inGironi(it.round) })                       // 8 gironi x 12 partite
        assertEquals(16, p.count { Cup.stageOf(it.round) == CoppeEuropee.STAGE_OTTAVI })    // 8 confronti x 2 gambe
        assertEquals(8, p.count { Cup.stageOf(it.round) == CoppeEuropee.STAGE_QUARTI })
        assertEquals(4, p.count { Cup.stageOf(it.round) == CoppeEuropee.STAGE_SEMI })
        assertEquals(1, p.count { Cup.stageOf(it.round) == CoppeEuropee.STAGE_FINALE })
        assertTrue(p.all { it.played })
        val (vince, finalista) = CoppeEuropee.finale(p, forza)!!
        assertTrue(vince != finalista && vince in ids && finalista in ids)
        // nei gironi i pareggi restano pareggi: nessun rigore
        assertTrue(p.filter { CoppeEuropee.inGironi(it.round) }.all { it.homePens < 0 })
        // la finale, se pari, ha un vincitore ai rigori
        val f = p.single { Cup.stageOf(it.round) == CoppeEuropee.STAGE_FINALE }
        if (f.homeGoals == f.awayGoals) assertTrue(f.homePens >= 0 && f.homePens != f.awayPens)
    }

    @Test fun coppaEuropaECoppeSoloEliminazione() {
        val sq = mondo(); val rng = Random(5); val forza = forzaDi(sq, rng)
        val q = CoppeEuropee.qualificateIniziali(sq, forza)
        listOf(Coppa.EUROPA, Coppa.COPPE).forEach { c ->
            val ids = q.filterValues { it == c }.keys.toList()
            val p = gioca(c, ids, sq, forza, rng)
            assertEquals(16, p.count { Cup.stageOf(it.round) == CoppeEuropee.STAGE_OTTAVI })   // 8 confronti x 2 gambe
            assertEquals(1, p.count { Cup.stageOf(it.round) == CoppeEuropee.STAGE_FINALE })
            assertTrue(p.none { CoppeEuropee.inGironi(it.round) })
            assertNotNull(CoppeEuropee.finale(p, forza))
        }
    }

    @Test fun premiCrescono() {
        val sq = mondo(); val rng = Random(6); val forza = forzaDi(sq, rng)
        val ids = CoppeEuropee.qualificateIniziali(sq, forza).filterValues { it == Coppa.CAMPIONI }.keys.toList()
        val p = gioca(Coppa.CAMPIONI, ids, sq, forza, rng)
        val premi = ids.associateWith { CoppeEuropee.premio(Coppa.CAMPIONI, p, it, forza) }
        val (vince, finalista) = CoppeEuropee.finale(p, forza)!!
        assertTrue(premi.getValue(vince) > premi.getValue(finalista))
        assertTrue(premi.values.all { it >= 4_000_000L })
        assertEquals(0L, CoppeEuropee.premio(Coppa.CAMPIONI, p, 999_999L))
        assertEquals("Vincitrice", CoppeEuropee.faseRaggiunta(p, vince, forza))
        assertEquals("Finale", CoppeEuropee.faseRaggiunta(p, finalista, forza))
    }
}
