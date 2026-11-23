package it.androcalcio.engine

import it.androcalcio.data.Movimento
import it.androcalcio.data.Player
import it.androcalcio.data.Youth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class NuoveMeccanicheTest {
    private fun p(id: Long, name: String = "G$id", ov: Int = 70, role: String = "C", salary: Long = 100_000, age: Int = 26) =
        Player(id = id, teamId = 1, name = name, role = role, age = age, overall = ov, morale = 60, salary = salary, nationality = "IT")

    // ---- Disciplina ----
    @Test fun quintoGialloSqualifica() {
        var r = Disciplina.Registro(mapOf(7L to 4))
        r = Disciplina.applica(r, emptySet(), Disciplina.Esito(listOf(7L), emptyList()), Random(1))
        assertEquals(1, r.squal[7L])
        assertFalse(r.gialli.containsKey(7L))
    }

    @Test fun squalificaScalaSoloSeSalta() {
        val r0 = Disciplina.Registro(squal = mapOf(1L to 2, 2L to 1))
        val r1 = Disciplina.applica(r0, setOf(1L, 2L), Disciplina.Esito(emptyList(), emptyList()), Random(1))
        assertEquals(1, r1.squal[1L]); assertFalse(r1.squal.containsKey(2L))
        val r2 = Disciplina.applica(r0, emptySet(), Disciplina.Esito(emptyList(), emptyList()), Random(1))
        assertEquals(2, r2.squal[1L])
    }

    @Test fun rossoDaUnoATreGiornate() {
        val r = Disciplina.applica(Disciplina.Registro(), emptySet(), Disciplina.Esito(emptyList(), listOf(5L)), Random(3))
        assertTrue(r.squal.getValue(5L) in 1..3)
    }

    @Test fun arbitroStabileEdiffidati() {
        assertEquals(Disciplina.arbitro(5, 99), Disciplina.arbitro(5, 99))
        assertTrue(Disciplina.arbitro(3, 1).severita in 1..3)
        assertEquals(listOf(9L), Disciplina.diffidati(Disciplina.Registro(mapOf(9L to 4, 8L to 2))))
    }

    @Test fun roundTripDisciplina() {
        val m = mapOf(1L to 2, 30L to 4)
        assertEquals(m, Disciplina.decode(Disciplina.encode(m)))
        assertTrue(Disciplina.decode(null).isEmpty())
    }

    @Test fun severitaAumentaCartellini() {
        val squadra = List(11) { p(it + 1L) }
        val rng1 = Random(5); val rng2 = Random(5)
        var tollerante = 0; var severo = 0
        repeat(400) {
            tollerante += Disciplina.sorteggia(squadra, 1, rng1).gialli.size
            severo += Disciplina.sorteggia(squadra, 3, rng2).gialli.size
        }
        assertTrue(severo > tollerante)
    }

    // ---- Personalità ----
    @Test fun personalitaStabileEdEffetti() {
        val x = p(1, "Mario Rossi")
        assertEquals(Personalita.tipo(x), Personalita.tipo(x.copy(id = 99)))
        val tutti = (1..60).map { Personalita.tipo(p(it.toLong(), "Nome$it")) }.toSet()
        assertTrue(tutti.size >= 4)
        // Il professionista non cambia mai, il sensibile soffre più degli altri la sconfitta
        val prof = (1..200).map { p(it.toLong(), "P$it") }.first { Personalita.tipo(it) == Personalita.Tipo.PROFESSIONISTA }
        val sens = (1..200).map { p(it.toLong(), "S$it") }.first { Personalita.tipo(it) == Personalita.Tipo.SENSIBILE }
        assertEquals(0, Personalita.moraleDopo(prof, 0, 3, true))
        assertTrue(Personalita.moraleDopo(sens, 0, 3, true) < Personalita.moraleDopo(prof, 0, 3, true))
    }

    @Test fun richiesteRispettanoIlTetto() {
        val rosa = (1..200).map { p(it.toLong(), "M$it", ov = 75, salary = 10_000L) }
        var trovate = 0
        repeat(50) { trovate += Personalita.nuove(rosa, emptyList(), Random(it)).size }
        assertTrue(trovate > 0)
        assertTrue(Personalita.nuove(rosa, List(3) { Personalita.Richiesta(it.toLong(), "aumento", 1) }, Random(1)).isEmpty())
        val l = listOf(Personalita.Richiesta(4, "aumento", 250_000), Personalita.Richiesta(8, "cessione"))
        assertEquals(l, Personalita.decode(Personalita.encode(l)))
    }

    // ---- Scambi ----
    @Test fun scambioConConguaglio() {
        val mio = p(1, ov = 70); val loro = p(2, ov = 78)
        val min = Scambi.conguaglioMinimo(mio, loro)
        assertTrue(min > 0)
        assertTrue(Scambi.valuta(mio, loro, min) is Scambi.Esito.Ok)
        assertTrue(Scambi.valuta(mio, loro, 0L) is Scambi.Esito.No || Scambi.valuta(mio, loro, 0L) is Scambi.Esito.Contro)
        assertEquals(0L, Scambi.conguaglioMinimo(loro, mio))
    }

    @Test fun nonSiScambiaIlPortiere() {
        val esito = Scambi.valuta(p(1, role = "D"), p(2, role = "P"), 5_000_000L)
        assertTrue(esito is Scambi.Esito.No)
    }

    // ---- Fair play ----
    @Test fun fairPlayEsclude_MercatoEStadio() {
        val mov = listOf(
            Movimento(teamId = 1, season = "s", label = "Bilancio", importo = -5_000_000, saldo = 0),
            Movimento(teamId = 1, season = "s", label = "Acquisto X", importo = -90_000_000, saldo = 0),
            Movimento(teamId = 1, season = "s", label = "Ampliamento stadio", importo = -20_000_000, saldo = 0),
            Movimento(teamId = 1, season = "s", label = "Multa fair play", importo = -3_000_000, saldo = 0))
        assertEquals(-5_000_000L, FairPlay.operativo(mov))
    }

    @Test fun fairPlaySoglieEBlocco() {
        val ok = FairPlay.valuta(listOf(1_000_000L, 1_000_000L, 1_000_000L), 10_000_000L)
        assertEquals("Verde", ok.livello); assertFalse(ok.blocco)
        val male = FairPlay.valuta(listOf(30_000_000L, 30_000_000L, 30_000_000L), 10_000_000L)
        assertEquals("Rosso", male.livello); assertTrue(male.blocco); assertTrue(male.multa > 0)
        assertEquals(listOf("2026/27" to 5L), FairPlay.decode(FairPlay.encode(listOf("2026/27" to 5L))))
    }

    // ---- Storia ----
    @Test fun scontriDiretti() {
        var m = Storia.aggiornaScontro(emptyMap(), "Rossi FC", 2, 1)
        m = Storia.aggiornaScontro(m, "Rossi FC", 0, 0)
        m = Storia.aggiornaScontro(m, "Rossi FC", 1, 3)
        assertEquals(Storia.Scontro(1, 1, 1, 3, 4), m["Rossi FC"])
        assertEquals(m, Storia.decodeScontri(Storia.encodeScontri(m)))
    }

    @Test fun recordDiStagione() {
        val r = Storia.nuoviRecord(Storia.Record(), listOf(2 to 0, 1 to 1, 0 to 2, 3 to 0), "2026/27")
        assertEquals(7, r.punti); assertEquals(6, r.golFatti); assertEquals(3, r.golSubiti); assertEquals(2, r.serieUtili)
        val r2 = Storia.nuoviRecord(r, listOf(0 to 1), "2027/28")
        assertEquals("2026/27", r2.stagionePunti)
        assertEquals(r, Storia.decodeRecord(Storia.encodeRecord(r)))
    }

    @Test fun hallOfFameEMaglie() {
        assertTrue(Storia.inHall(5, 80, 0)); assertTrue(Storia.inHall(6, 60, 55)); assertFalse(Storia.inHall(4, 90, 90))
        assertTrue(Storia.magliaRitirata(Storia.Bandiera("A", 10, 8, 0, 70)))
        assertFalse(Storia.magliaRitirata(Storia.Bandiera("A", 0, 9, 0, 70)))
        val l = listOf(Storia.Bandiera("Mario", 9, 6, 70, 81))
        assertEquals(l, Storia.decodeBandiere(Storia.encodeBandiere(l)))
    }

    // ---- Scouting vivai ----
    private fun y(id: Long, ov: Int, pot: Int) =
        Youth(id = id, teamId = 5, season = "s", name = "Y$id", role = "A", age = 15, overall = ov, potential = pot, nationality = "IT")

    @Test fun scoutingGiovani() {
        assertEquals(1, ScoutingGiovani.stelle(20)); assertEquals(5, ScoutingGiovani.stelle(90))
        assertEquals(-4L, ScoutingGiovani.chiave(y(4, 50, 70)))
        assertTrue(ScoutingGiovani.eGiovane(-4L)); assertFalse(ScoutingGiovani.eGiovane(4L))
        val ragazzo = y(3, 55, 80)
        val larga = ScoutingGiovani.rangeLivello(ragazzo, 40, 1); val stretta = ScoutingGiovani.rangeLivello(ragazzo, 90, 4)
        assertTrue((larga.last - larga.first) > (stretta.last - stretta.first))
        assertTrue(ScoutingGiovani.prezzo(y(1, 40, 60)) < ScoutingGiovani.prezzo(y(2, 70, 90)))
        val vivai = mapOf(1L to List(5) { y(it + 1L, 50, 70) }, 2L to emptyList(), 3L to List(5) { y(it + 10L, 50, 70) })
        val sc = ScoutingGiovani.scegli(vivai, Random(2))
        assertEquals(ScoutingGiovani.RAGAZZI_PER_CLUB * 2, sc.size)
    }
}

class MutuiTest {
    private val b = Mutui.banche[1]

    @org.junit.Test fun rataChiudeIlDebito() {
        var m = Mutui.Mutuo(b.id, 5_000_000L, b.anni, b.tasso, Mutui.rata(5_000_000L, b.tasso, b.anni))
        var anni = 0
        while (true) {
            val r = Mutui.paga(m, Long.MAX_VALUE / 4)
            anni++
            m = r.nuovo ?: break
            org.junit.Assert.assertTrue(anni < 20)
        }
        org.junit.Assert.assertEquals(b.anni, anni)
    }

    @org.junit.Test fun unSoloMutuoPerBancaETetto() {
        val Ok = Mutui.richiedi(emptyList(), b.id, 2_000_000L) as Mutui.Esito.Ok
        org.junit.Assert.assertTrue(Mutui.richiedi(Ok.lista, b.id, 1_000_000L) is Mutui.Esito.No)
        org.junit.Assert.assertTrue(Mutui.richiedi(emptyList(), b.id, b.tetto + 100_000L) is Mutui.Esito.No)
        org.junit.Assert.assertTrue(Mutui.richiedi(emptyList(), b.id, 100_000L) is Mutui.Esito.No)
    }

    @org.junit.Test fun moraSeNonCiSonoSoldi() {
        val m = Mutui.Mutuo(b.id, 4_000_000L, 3, b.tasso, Mutui.rata(4_000_000L, b.tasso, 3))
        val r = Mutui.paga(m, 0L)
        org.junit.Assert.assertTrue(r.mora > 0L)
        org.junit.Assert.assertTrue(r.nuovo!!.residuo > m.residuo)
    }

    @org.junit.Test fun codificaERientro() {
        val l = listOf(Mutui.Mutuo(0, 1_500_000L, 2, 0.04, 800_000L), Mutui.Mutuo(5, 9_000_000L, 1, 0.18, 10_620_000L))
        org.junit.Assert.assertEquals(l, Mutui.decode(Mutui.encode(l)))
    }

    @org.junit.Test fun interessiContanoNelFairPlayCapitaleNo() {
        fun mv(l: String, i: Long) = it.androcalcio.data.Movimento(teamId = 1, season = "s", label = l, importo = i, saldo = 0)
        val op = FairPlay.operativo(listOf(mv("Mutuo X: erogazione", 5_000_000L), mv("Interessi mutuo X", -300_000L), mv("Rimborso mutuo X", -1_000_000L)))
        org.junit.Assert.assertEquals(-300_000L, op)
    }
}

class AnalisiTest {
    private fun p(gf: Int, gs: Int, casa: Boolean = true) = Analisi.Partita(gf, gs, casa)

    @org.junit.Test fun formaESerie() {
        val l = listOf(p(0, 1), p(2, 0), p(1, 1), p(3, 0), p(1, 0))
        org.junit.Assert.assertEquals("SVNVV", Analisi.forma(l))
        val s = Analisi.serie(l)
        org.junit.Assert.assertEquals(2, s.vittorie)
        org.junit.Assert.assertEquals(4, s.imbattuto)
        org.junit.Assert.assertEquals(2, s.senzaSubire)
        org.junit.Assert.assertEquals(0, s.senzaVincere)
    }

    @org.junit.Test fun casaETrasferta() {
        val l = listOf(p(2, 0, true), p(0, 1, false), p(1, 1, false))
        val c = Analisi.rendimento(l, true); val t = Analisi.rendimento(l, false)
        org.junit.Assert.assertEquals(3, c.punti)
        org.junit.Assert.assertEquals(1, t.punti)
        org.junit.Assert.assertEquals(2, t.giocate)
    }

    @org.junit.Test fun nessunaPartita() {
        org.junit.Assert.assertEquals("", Analisi.forma(emptyList()))
        org.junit.Assert.assertEquals(0, Analisi.serie(emptyList()).vittorie)
        org.junit.Assert.assertEquals(0.0 to 0.0, Analisi.mediaGol(emptyList()))
    }

    @org.junit.Test fun consigliSenzaTitolari() {
        val c = Analisi.consigli(emptyList(), emptySet(), emptySet())
        org.junit.Assert.assertTrue(c.first().contains("titolari"))
    }
}

class NovitaTest {
    private fun pl(id: Long, role: String, ov: Int, cond: Int, starter: Boolean) =
        it.androcalcio.data.Player(id = id, teamId = 1, name = "G$id", role = role, age = 25, overall = ov, morale = 70,
            salary = 100_000L, nationality = "ITA", potential = ov, contract = 3, condition = cond, starter = starter)

    @org.junit.Test fun rifinanziaSoloSeIlTassoScende() {
        val caro = Mutui.Mutuo(5, 5_000_000L, 2, 0.18, Mutui.rata(5_000_000L, 0.18, 2))
        val ok = Mutui.rifinanzia(listOf(caro), 5, 1)
        org.junit.Assert.assertTrue(ok is Mutui.Esito.Ok)
        org.junit.Assert.assertEquals(0.065, (ok as Mutui.Esito.Ok).mutuo.tasso, 1e-9)
        org.junit.Assert.assertTrue(Mutui.rifinanzia(listOf(caro), 5, 5) is Mutui.Esito.No)
        val economico = Mutui.Mutuo(0, 2_000_000L, 2, 0.04, 1_000_000L)
        org.junit.Assert.assertTrue(Mutui.rifinanzia(listOf(economico), 0, 5) is Mutui.Esito.No)
    }

    @org.junit.Test fun taglioEFiducia() {
        org.junit.Assert.assertEquals(0L, Mutui.taglio(20_000_000L))
        org.junit.Assert.assertEquals(300_000L, Mutui.taglio(40_000_000L))
        org.junit.Assert.assertEquals(100, Mutui.fiducia(0L))
        org.junit.Assert.assertEquals(40, Mutui.fiducia(Mutui.TETTO_DEBITO))
    }

    @org.junit.Test fun previsione() {
        org.junit.Assert.assertEquals(1_000_000L + 100_000L * 10 - 200_000L, Analisi.previsione(1_000_000L, 500_000L, 5, 10, 200_000L))
        org.junit.Assert.assertEquals(1_000_000L, Analisi.previsione(1_000_000L, 0L, 0, 10, 0L))
    }

    @org.junit.Test fun riposoSostituisceSoloConChiHaRiposato() {
        val rosa = listOf(pl(1, "C", 70, 40, true), pl(2, "C", 65, 90, false), pl(3, "C", 60, 50, false), pl(4, "A", 80, 30, true))
        val c = Analisi.riposo(rosa, emptySet())
        org.junit.Assert.assertEquals(1, c.size)
        org.junit.Assert.assertEquals(1L to 2L, c[0].first.id to c[0].second.id)
        org.junit.Assert.assertTrue(Analisi.riposo(rosa, setOf(2L)).isEmpty())
    }

    @org.junit.Test fun allenamentoIndividualeCodifica() {
        val m = mapOf(7L to TrainFocus.FISICO, 9L to TrainFocus.RIPOSO)
        org.junit.Assert.assertEquals(m, Training.decodeInd(Training.encodeInd(m)))
        org.junit.Assert.assertTrue(Training.decodeInd("").isEmpty())
    }

    @org.junit.Test fun capitanoScelto() {
        val a = pl(1, "D", 60, 90, true); val b = pl(2, "C", 80, 90, true)
        Leadership.scelto = 0L
        org.junit.Assert.assertEquals(2L, Leadership.capitano(listOf(a, b))!!.id)
        Leadership.scelto = 1L
        org.junit.Assert.assertEquals(1L, Leadership.capitano(listOf(a, b))!!.id)
        Leadership.scelto = 99L
        org.junit.Assert.assertEquals(2L, Leadership.capitano(listOf(a, b))!!.id)
        Leadership.scelto = 0L
    }
}
