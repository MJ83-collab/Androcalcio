package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import it.androcalcio.data.Player
import it.androcalcio.data.Team
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V06Test {
    private fun team(id: Long) = Team(id = id, name = "T$id", shortName = "T$id", colorPrimary = 0, colorSecondary = 0,
        budget = 0, stadiumCapacity = 10_000, ticketPrice = 20, sponsor = "")
    private fun pl(id: Long, ov: Int = 70, age: Int = 25, inj: Int = 0) =
        Player(id = id, teamId = 1, name = "P$id", role = "C", age = age, overall = ov, morale = 50, salary = 1_000_000, nationality = "ITA", injuredRounds = inj)
    private fun m(r: Int, h: Long, a: Long, hg: Int, ag: Int) =
        MatchResult(league = "L", season = "S", round = r, homeTeamId = h, awayTeamId = a, homeGoals = hg, awayGoals = ag, played = true)

    @Test fun playoffEPlayout() {
        val forza = mapOf(1L to 80.0, 2L to 70.0, 3L to 65.0, 4L to 60.0)
        val conta = HashMap<Long, Int>()
        val rng = Random(7)
        repeat(2000) { Spareggi.playoff(listOf(1L, 2L, 3L, 4L), forza, rng)!!.first.let { conta[it] = (conta[it] ?: 0) + 1 } }
        assertTrue("la più forte vince più spesso", (conta[1L] ?: 0) > (conta[4L] ?: 0))
        val perde = Spareggi.playout(1L, 4L, forza, Random(1))
        assertTrue(perde == 1L || perde == 4L)
        assertTrue(Spareggi.probPassa(60.0, 60.0) > 0.5)   // in parità passa la meglio piazzata
    }

    @Test fun playoffGiocabile() {
        val turni = ArrayList<String>()
        // La peggio piazzata vince sempre: semifinali 4-1 e 3-2, finale tra 4 e 3
        val r = Spareggi.playoffCon(listOf(1L, 2L, 3L, 4L)) { a, b, turno -> turni += turno; maxOf(a, b) }!!
        assertEquals(listOf("Semifinale", "Semifinale", "Finale"), turni)
        assertEquals(4L to 3L, r)
        assertEquals(null, Spareggi.playoffCon(listOf(1L)) { a, _, _ -> a })
    }

    @Test fun regoleRealiPlayoff() {
        // Parità: passa la meglio piazzata, salvo rigori
        assertEquals(1L, Spareggi.passaTurno(1L, 2L, 2, 2))
        assertEquals(2L, Spareggi.passaTurno(1L, 2L, 2, 2, 3, 4))
        assertEquals(2L, Spareggi.passaTurno(1L, 2L, 1, 3))
        // Finale di B: a pari punti supplementari e rigori
        assertEquals(Spareggi.Regola.SUPPL_RIGORI, Spareggi.B_FINALE.regolaPer(true))
        assertEquals(Spareggi.Regola.PIAZZATA, Spareggi.B_FINALE.regolaPer(false))
        // Tabellone B a 6: preliminari 5-8 e 6-7, poi semifinali con 3ª e 4ª
        val gare = ArrayList<String>()
        val r = kotlinx.coroutines.runBlocking { Spareggi.tabelloneB(listOf(3L, 4L, 5L, 6L, 7L, 8L)) { a, b, t -> gare += "${t.nome}:$a-$b"; a } }!!
        assertEquals(listOf("Turno preliminare:5-8", "Turno preliminare:6-7", "Semifinale:3-6", "Semifinale:4-5", "Finale:3-4"), gare)
        assertEquals(3L to 4L, r)
        val forza = mapOf(1L to 75.0, 2L to 60.0)
        var vinceAlto = 0
        repeat(1000) { if (Spareggi.simulaTurno(1L, 2L, 2, Spareggi.Regola.PIAZZATA, forza, Random(it)) == 1L) vinceAlto++ }
        assertTrue("la più forte e meglio piazzata passa più spesso: $vinceAlto", vinceAlto > 600)
    }

    @Test fun classificaCasaTrasfertaEForma() {
        val ts = listOf(team(1), team(2))
        val ms = listOf(m(1, 1, 2, 2, 0), m(2, 2, 1, 1, 1), m(3, 1, 2, 0, 1))
        val casa = ClassificaExtra.lato(ts, ms, true)
        assertEquals(3, casa.first { it.team.id == 1L }.pts)
        assertEquals(1, casa.first { it.team.id == 2L }.pts)
        assertEquals("VNP", ClassificaExtra.forma(1L, ms))
        assertEquals(listOf(1, 1, 1), Andamento.posizioni(1L, ts, ms))
    }

    @Test fun cartelliniAmmortamentoEPlusvalenza() {
        val c = Cartellini.Cartellino(1, 9_000_000, 3)
        assertEquals(3_000_000, c.quota)
        val dopo = Cartellini.avanza(listOf(c))
        assertEquals(6_000_000, dopo.first().residuo)
        assertEquals(4_000_000, Cartellini.plusvalenza(10_000_000, dopo.first()))
        assertEquals(10_000_000, Cartellini.plusvalenza(10_000_000, null))
        assertTrue(Cartellini.avanza(Cartellini.avanza(dopo)).isEmpty())
        assertEquals(0L, Cartellini.dilazione(900_000, true))
        assertEquals(1_000_000L, Cartellini.dilazione(2_000_000, true))
        assertEquals(c, Cartellini.decode(Cartellini.encode(listOf(c))).first())
        val r = Cartellini.Rivendita(5, 15, "Mario Rossi")
        assertEquals(r, Cartellini.decodeR(Cartellini.encodeR(listOf(r))).first())
        val s = Cartellini.aggiungiPlus(Cartellini.aggiungiPlus(null, "2026/27", 100), "2026/27", 50)
        assertEquals(150L, Cartellini.plusvalenzeStagione(s, "2026/27"))
    }

    @Test fun stadioEPrato() {
        val p = NuovoStadio.nuovo(60_000)
        assertEquals(90_000_000L, NuovoStadio.costo(60_000))
        assertEquals(30_000_000L, p.rata)
        assertEquals(p, NuovoStadio.decode(NuovoStadio.encode(p)))
        assertEquals(listOf(60_000, 80_000), NuovoStadio.opzioni(50_000))
        assertEquals(1.0, Prato.fattore(80), 1e-9)
        assertTrue(Prato.fattore(10) < Prato.fattore(40))
        assertTrue(Prato.dopoPartita(80, 2, false) > Prato.dopoPartita(80, 0, true))
    }

    @Test fun minutaggioEInfortuni() {
        val m = Minutaggio.aggiungi(emptyMap(), setOf(1L), setOf(1L, 2L))
        assertEquals(90, m[1L]); assertEquals(30, m[2L])
        assertEquals(50, Minutaggio.percentuale(45, 1))
        val rosa = (1L..15L).map { pl(it, 60 + it.toInt()) }
        assertTrue(Minutaggio.scontenti(rosa, emptyMap(), 3).isEmpty())
        assertTrue(Minutaggio.scontenti(rosa, emptyMap(), 6).isNotEmpty())
        val n = StoricoInfortuni.nuovi(listOf(pl(1)), listOf(pl(1, inj = 3)), "S")
        assertEquals(listOf(StoricoInfortuni.Voce(1, "S", 3)), n)
        assertEquals(n, StoricoInfortuni.decode(StoricoInfortuni.encode(n)))
    }

    @Test fun formazioniECaratteristiche() {
        val l = listOf(FormazioniSalvate.Preset("4-4-2 · Equilibrata", 0, 1, 1, 2, listOf(1L, 2L)), null, null)
        assertEquals(l, FormazioniSalvate.decode(FormazioniSalvate.encode(l)))
        assertEquals(3, FormazioniSalvate.decode("").size)
        val veloce = pl(1).copy(ve = 85); val lento = pl(2).copy(ve = 50)
        val f = FiltroMercato(caratteristica = "VELOCE")
        assertTrue(f.accetta(veloce, 0, false)); assertFalse(f.accetta(lento, 0, false))
        assertEquals(20_000L, PremioPartita.costo(0))
        assertEquals(200_000L, PremioPartita.costo(10_000_000))
    }

    @Test fun csvConIntestazioni() {
        val csv = EsportaCsv.riepilogo("2026/27", "Squadra; con punto e virgola", emptyList(), listOf(pl(1)), mapOf(1L to 3), mapOf(1L to 10), emptyList())
        assertTrue(csv.contains("\"Squadra; con punto e virgola\""))
        assertTrue(csv.contains("P1;C;25;70;10;3;1000000"))
        assertNotNull(Insurance.turni(18).takeIf { it == 34 })
    }
}
