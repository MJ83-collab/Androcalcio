package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.PlayerStat
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class ExtraTest {
    private fun p(id: Long = 1, ruolo: String = "C", eta: Int = 25, ov: Int = 70, morale: Int = 50, pot: Int = 70) =
        Player(id = id, teamId = 1, name = "Giocatore $id", role = ruolo, age = eta, overall = ov, morale = morale,
            salary = 1_000_000, nationality = "Italia", potential = pot)

    // ---- Relazioni ----
    @Test fun conferenzaHaSempreTreRisposteEUnaDomandaPerSeme() {
        Relazioni.DOMANDE.forEach { assertEquals(3, it.risposte.size) }
        assertEquals(Relazioni.DOMANDE.size, Relazioni.DOMANDE.map { it.testo }.distinct().size)
        assertEquals(Relazioni.domanda(0), Relazioni.domanda(Relazioni.DOMANDE.size))
        assertNotNull(Relazioni.domanda(-3))
    }

    @Test fun colloquiHannoEffettiCoerenti() {
        val rng = Random(1)
        assertTrue(Relazioni.colloquio(p(morale = 20), Relazioni.Tono.RASSICURA, rng) >= 5)
        assertTrue(Relazioni.colloquio(p(morale = 20), Relazioni.Tono.RIMPROVERO, rng) < 0)
        assertTrue(Relazioni.colloquio(p(morale = 95), Relazioni.Tono.RIMPROVERO, rng) >= 1)
        assertTrue(Relazioni.haProblemi(p(morale = 39)))
        assertFalse(Relazioni.haProblemi(p(morale = 40)))
    }

    @Test fun psicologoAiutaSoloChiStaMale() {
        assertEquals(0, Relazioni.recuperoPsicologo(0, p(morale = 10)))
        assertEquals(0, Relazioni.recuperoPsicologo(3, p(morale = 70)))
        assertTrue(Relazioni.recuperoPsicologo(3, p(morale = 20)) > Relazioni.recuperoPsicologo(1, p(morale = 50)))
        assertTrue(Relazioni.costoPsicologo(3) > Relazioni.costoPsicologo(1))
        assertEquals(0L, Relazioni.costoPsicologo(0))
    }

    // ---- Obiettivi ----
    @Test fun obiettiviSeguonoLaForza() {
        assertEquals(Obiettivi.Tipo.TITOLO, Obiettivi.fissa(1, 20, 1_000_000).tipo)
        assertEquals(Obiettivi.Tipo.SALVEZZA, Obiettivi.fissa(20, 20, 1_000_000).tipo)
        assertTrue(Obiettivi.fissa(1, 20, 1_000_000).bonus > Obiettivi.fissa(20, 20, 1_000_000).bonus)
    }

    @Test fun obiettivoRaggiuntoOMancato() {
        val o = Obiettivi.fissa(10, 20, 1_000_000)
        assertTrue(Obiettivi.raggiunto(o, o.posizioneMax))
        assertFalse(Obiettivi.raggiunto(o, o.posizioneMax + 1))
        assertTrue(Obiettivi.fiducia(o, 1) > 0)
        assertTrue(Obiettivi.fiducia(o, 20) < 0)
        assertEquals(o, Obiettivi.decode(Obiettivi.encode(o)))
        assertNull(Obiettivi.decode("bla"))
    }

    // ---- Abbonamenti ----
    @Test fun prezziPerSettoreSalvanoERecuperano() {
        val m = Abbonamenti.SETTORI.associate { it.id to 120 }
        assertEquals(m, Abbonamenti.decode(Abbonamenti.encode(m)))
        assertTrue(Abbonamenti.decode(null).values.all { it == 100 })
        assertEquals(Abbonamenti.PREZZO_MAX, Abbonamenti.decode("curva:999").getValue("curva"))
    }

    @Test fun prezziAltiRiempionoMenoMaPossonoRenderePiu() {
        val base = Abbonamenti.SETTORI.associate { it.id to 100 }
        val alto = Abbonamenti.SETTORI.associate { it.id to 200 }
        val r100 = Abbonamenti.riempimento(0.5, 60, 100)
        val r200 = Abbonamenti.riempimento(0.5, 60, 200)
        assertTrue(r200 < r100)
        val i100 = Abbonamenti.incasso(30_000, 30, 0.5, 60, base, 19)
        val i200 = Abbonamenti.incasso(30_000, 30, 0.5, 60, alto, 19)
        assertTrue(i100 > 0 && i200 > 0)
        assertTrue(Abbonamenti.fattore(30_000, 30, 0.5, 60, base, 19) in 0.999..1.001)
        assertTrue(Abbonamenti.abbonatiPct(90, 80) > Abbonamenti.abbonatiPct(30, 150))
    }

    // ---- Premi ----
    @Test fun premiAssegnatiAlGiocatoreGiusto() {
        val g = mapOf(1L to p(1, "A", 30, 80), 2L to p(2, "A", 19, 70), 3L to p(3, "P", 30, 75), 4L to p(4, "C", 28, 82))
        val st = listOf(PlayerStat("s", 1, 1, 30, 22), PlayerStat("s", 2, 1, 20, 6), PlayerStat("s", 3, 1, 34, 0), PlayerStat("s", 4, 1, 34, 3))
        val premi = Premi.assegna(st, g)
        assertEquals(1L, premi.first { it.titolo == "Capocannoniere" }.playerId)
        assertEquals(2L, premi.first { it.titolo == "Miglior giovane" }.playerId)
        assertEquals(3L, premi.first { it.titolo == "Miglior portiere" }.playerId)
        assertEquals(premi, Premi.decode(Premi.encode(premi)))
        assertEquals(7L, Premi.allenatoreDelMese(mapOf(1L to 3, 7L to 10)))
    }

    // ---- Meteo ----
    @Test fun meteoStagionaleEFattori() {
        val rng = Random(3)
        val estate = (1..300).map { Meteo.tempo(7, rng) }
        val inverno = (1..300).map { Meteo.tempo(1, rng) }
        assertTrue(estate.none { it == Meteo.Tempo.NEVE })
        assertTrue(inverno.any { it == Meteo.Tempo.NEVE })
        assertTrue(Meteo.Condizioni(Meteo.Tempo.NEVE, Meteo.Campo.PESSIMO).fattoreGol() < 0.85)
        assertEquals(8, Meteo.meseDellaGiornata(1, 38))
        assertEquals(5, Meteo.meseDellaGiornata(38, 38))
    }

    @Test fun campoIbridoEdRiscaldamentoAiutano() {
        val rng = Random(1)
        fun media(ib: Boolean, ri: Boolean) = (1..400).map { Meteo.campo(Meteo.Tempo.NEVE, ib, ri, 1, rng).ordinal }.average()
        assertTrue(media(true, true) < media(false, false))   // ordinal minore = campo migliore
    }

    // ---- Giornale ----
    @Test fun giornaleTitoliEPagelle() {
        assertTrue(Giornale.titolo("A", "B", 3, 0, false).contains("travolge"))
        assertTrue(Giornale.titolo("A", "B", 1, 0, true).startsWith("Derby"))
        val e = Giornale.crea("A", "B", 2, 1, false, listOf("Uno" to 6.0, "Due" to 8.5, "Tre" to 4.5))
        assertEquals("Due", e.pagelle.first().nome)
        assertEquals("Immenso", e.pagelle.first().commento)
        assertEquals("Disastroso", e.pagelle.last().commento)
        assertEquals(e, Giornale.decode(Giornale.encode(e)))
        assertNull(Giornale.decode(null))
    }

    // ---- Finestre, prestiti, agenti ----
    @Test fun finestreDiMercato() {
        assertTrue(Finestre.stato(1, 38).aperta)
        assertFalse(Finestre.stato(10, 38).aperta)
        assertEquals(Finestre.Stato.INVERNALE, Finestre.stato(19, 38))
        assertFalse(Finestre.stato(30, 38).aperta)
        assertTrue(Finestre.ultimoGiorno(3, 38))
    }

    @Test fun prestitiSiSalvano() {
        val l = listOf(Prestiti.Prestito(5, 1, 2, 3_000_000, true), Prestiti.Prestito(9, 3, 1, 500_000, false))
        assertEquals(l, Prestiti.decode(Prestiti.encode(l)))
        assertTrue(Prestiti.costo(p(ov = 80)) > 0)
        assertTrue(Prestiti.riscatto(p(ov = 80)) >= Prestiti.costo(p(ov = 80)))
        assertFalse(Prestiti.puoPrestare(p(), Market.MIN_SQUAD))
        assertFalse(Prestiti.puoPrendere(Market.MAX_SQUAD))
    }

    @Test fun agentiChiedonoDiPiuPerLeStelle() {
        assertTrue(Agenti.percentuale(p(ov = 88)) > Agenti.percentuale(p(ov = 55)))
        assertTrue(Agenti.percentuale(p(eta = 19, ov = 60, pot = 80)) > Agenti.percentuale(p(eta = 28, ov = 60, pot = 60)))
        assertEquals(1_000_000L * 3 * 6 / 100 / 1000 * 1000, Agenti.commissione(p(ov = 72), 1_000_000, 3))
        assertTrue(Agenti.nomeAgente(p(7)).isNotBlank())
    }

    // ---- Sponsor ----
    @Test fun sponsorDieciTipiEMarketing() {
        assertEquals(10, Sponsors.TIPI.size)
        assertEquals(10, Sponsors.TIPI.map { it.id }.toSet().size)
        assertEquals(10, Sponsors.MAX_ATTIVI)
        val rng = Random(2)
        val senza = Sponsors.offers(emptyList(), 75, 1.0, rng, 0)
        val con = Sponsors.offers(emptyList(), 75, 1.0, Random(2), 3)
        assertEquals(3, senza.size)
        assertEquals(6, con.size)
        assertTrue(con.all { it.tipo.isNotEmpty() })
        assertTrue(Sponsors.costoMarketing(3) > Sponsors.costoMarketing(1))
        val d = SponsorDeal("X", 100_000, 2, "tecnico")
        assertEquals(listOf(d), SponsorDeal.decode(SponsorDeal.encode(listOf(d))))
        assertEquals(SponsorDeal("Y", 5, 1), SponsorDeal.decode("Y|5|1").first())   // vecchio formato
    }

    @Test fun nessunTipoDuplicatoNelleOfferte() {
        val attivi = listOf(SponsorDeal("A", 1, 1, "maglia"), SponsorDeal("B", 1, 1, "tecnico"))
        val o = Sponsors.offers(attivi, 70, 1.0, Random(5), 3)
        assertTrue(o.none { it.tipo == "maglia" || it.tipo == "tecnico" })
        assertEquals(o.size, o.map { it.tipo }.toSet().size)
    }

    // ---- All Star ----
    @Test fun allStarOttoDecenniCompleti() {
        assertEquals(8, AllStar.DECENNI.size)
        assertEquals(8, AllStar.DECENNI.map { it.nome }.toSet().size)
        AllStar.DECENNI.forEach { d ->
            assertEquals(14, d.rosa.size)
            assertEquals(2, d.rosa.count { it.ruolo == "P" })
            assertEquals(5, d.rosa.count { it.ruolo == "D" })
            assertEquals(4, d.rosa.count { it.ruolo == "C" })
            assertEquals(3, d.rosa.count { it.ruolo == "A" })
            assertEquals(14, d.rosa.map { it.nome }.toSet().size)
            val rosa = AllStar.rosa(d, 99)
            assertTrue(rosa.all { it.overall in 84..91 && it.teamId == 99L })
        }
        val tutti = AllStar.DECENNI.flatMap { d -> d.rosa.map { it.nome } }
        assertEquals("nessun nome ripetuto tra decenni", tutti.size, tutti.toSet().size)
    }

    // ---- Achievement ----
    @Test fun achievementSiSbloccano() {
        assertTrue(Achievement.TUTTI.size >= 25)
        assertEquals(Achievement.TUTTI.size, Achievement.TUTTI.map { it.id }.toSet().size)
        assertTrue(Achievement.sbloccati(Achievement.Snapshot()).isEmpty())
        val s = Achievement.Snapshot(vittorie = 12, titoliCampionato = 1)
        val ids = Achievement.sbloccati(s)
        assertTrue("prima_vittoria" in ids && "10_vittorie" in ids && "primo_titolo" in ids && "tre_titoli" !in ids)
        assertTrue(Achievement.nuovi(ids, s).isEmpty())
        assertEquals(ids, Achievement.decode(Achievement.encode(ids)))
    }

    // ---- Statistiche avanzate ----
    @Test fun zoneDelCampoSpecchiate() {
        assertEquals(Avanzate.zona(0.95f, 0.1f, true), Avanzate.zona(0.05f, 0.1f, false))
        assertEquals(Avanzate.COLONNE - 1, Avanzate.zona(1f, 0f, true))
    }

    @Test fun totaliAvanzatiSiSalvano() {
        val t = Avanzate.Totali(10, 120, 90, 14, 9, 540, List(Avanzate.COLONNE * Avanzate.RIGHE) { it })
        assertEquals(t, Avanzate.decode(Avanzate.encode(t)))
        assertEquals(Avanzate.Totali(), Avanzate.decode("1,2,3"))
        assertEquals(54, t.possessoMedio)
        assertEquals(120 * Avanzate.XG_PER_TIRO, t.xgF, 1e-9)
    }
}

class GiovaniliTest {
    private val team = it.androcalcio.data.Team(id = 1, name = "Casa", shortName = "CAS", colorPrimary = 0xFF0000FFL,
        colorSecondary = 0xFFFFFFFFL, budget = 20_000_000, stadiumCapacity = 1000, ticketPrice = 10, sponsor = "")

    @Test fun inizialeHaTutteLeEtaDa10A16() {
        val tutti = (1..40).flatMap { Giovanili.iniziale(team, "2026", kotlin.random.Random(it)) }
        assertTrue(tutti.all { it.age in Giovanili.ETA_MIN..Giovanili.ETA_MAX })
        assertEquals((Giovanili.ETA_MIN..Giovanili.ETA_MAX).toSet(), tutti.map { it.age }.toSet())
        assertEquals(14, Giovanili.iniziale(team, "2026", kotlin.random.Random(1)).size)
    }

    @Test fun iPiuPiccoliHannoOverallPiuBasso() {
        val tutti = (1..60).flatMap { Giovanili.iniziale(team, "2026", kotlin.random.Random(it)) }
        val piccoli = tutti.filter { it.age <= 11 }.map { it.overall }.average()
        val grandi = tutti.filter { it.age >= 15 }.map { it.overall }.average()
        assertTrue("piccoli=$piccoli grandi=$grandi", grandi - piccoli > 8)
        assertTrue(tutti.all { it.overall <= it.potential })
    }

    @Test fun nuovaLevaSoloPiccoli() {
        assertTrue(Giovanili.nuovaLeva(team, "2027", kotlin.random.Random(2)).all { it.age <= Giovanili.ETA_MIN + 2 })
    }

    @Test fun crescitaAnnuale() {
        val y = Giovanili.iniziale(team, "2026", kotlin.random.Random(3)).first()
        var x = y
        repeat(6) { x = Giovanili.cresci(x, kotlin.random.Random(it), 55) }
        assertEquals(y.age + 6, x.age)
        assertTrue(x.overall >= y.overall && x.overall <= x.potential)
        val bravo = Giovanili.cresci(y, kotlin.random.Random(9), 99, 10)
        val scarso = Giovanili.cresci(y, kotlin.random.Random(9), 1, 0)
        assertTrue(bravo.overall >= scarso.overall)
    }

    @Test fun ultimoAnnoEScadenza() {
        val y = Giovanili.iniziale(team, "2026", kotlin.random.Random(4)).first().copy(age = 17)
        assertFalse(Giovanili.scaduto(y))
        assertTrue(Giovanili.scaduto(Giovanili.cresci(y, kotlin.random.Random(1), 55)))
        assertTrue(Giovanili.puoDebuttare(y.copy(age = 15)))
        assertFalse(Giovanili.puoDebuttare(y.copy(age = 14)))
    }

    @Test fun schedaECoerente() {
        val y = Giovanili.iniziale(team, "2026", kotlin.random.Random(5)).first().copy(age = 16, overall = 66)
        val s = Giovanili.scheda(y, 60, 60)
        assertTrue(s.righe.any { it.contains("+6") })
        assertTrue(s.consiglio.contains("apparizione"))
        assertTrue(Giovanili.scheda(y.copy(age = 17, overall = 40), 40, 60).consiglio.contains("liberarlo"))
    }

    @Test fun apparizioniSiSalvano() {
        val l = listOf(3L to 90L, 4L to 91L)
        assertEquals(l, Giovanili.decodeApparizioni(Giovanili.encodeApparizioni(l)))
        assertTrue(Giovanili.decodeApparizioni("").isEmpty())
    }
}

class NazionaliU21Test {
    @Test fun dateU21NonCoincidonoConLeSenior() {
        assertTrue(Nazionali.DATE_U21.none { it in Nazionali.DATE })
        assertTrue(Nazionali.DATE_U21.all { it in 1..38 })
    }
    @Test fun nomiEEta() {
        assertEquals("Italia U21", Nazionali.nomeU21("Italia"))
        assertTrue(Nazionali.isU21("Italia U21"))
        assertFalse(Nazionali.isU21("Italia"))
        assertEquals("Italia", Nazionali.senzaU21("Italia U21"))
        assertTrue(Nazionali.europeoU21(2027) && !Nazionali.europeoU21(2028))
    }
    @Test fun soloGliUnder21() {
        val g = (18..30).map { Player(id = it.toLong(), teamId = 1, name = "G$it", role = "C", age = it, overall = 60, morale = 50,
            salary = 1, nationality = "Italia") }
        assertEquals(listOf(18, 19, 20, 21), Nazionali.eleggibiliU21(g).map { it.age })
    }
}
