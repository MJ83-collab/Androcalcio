package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Piano tattico a due fasi: undici stili, salvataggio, effetti piccoli e ruoli diversi per fase. */
class PianoTatticoTest {

    private var n = 0L

    private fun p(role: String, pos: String, v: Int = 65, sec2: String = "", aff2: Int = 0) =
        Player(id = ++n, teamId = 1, name = "G$n", role = role, age = 25, overall = v, morale = 70, salary = 1000,
            nationality = "IT", pos = pos, sec2 = sec2, aff2 = aff2,
            ve = v, re = v, ag = v, qu = v, fo = 80, gm = v, en = v, pa = v, dr = v, fi = v, ti = v, ri = v)

    private fun xi(v: Int = 65) = listOf(
        p("P", "POR", v), p("D", "TS", v), p("D", "DC", v), p("D", "DC", v), p("D", "TD", v),
        p("C", "MED", v), p("C", "INT", v), p("C", "INT", v), p("C", "MP", v),
        p("A", "CA", v), p("A", "ES", v)
    )

    @Test
    fun ciSonoUndiciStili() {
        assertEquals(11, StileTattico.values().size)
        // Tabula rasa: nessuna etichetta e nessuna istruzione
        assertNull(StileTattico.TABULA_RASA.intensita)
        assertTrue(StileTattico.TABULA_RASA.istruzioniInPossesso().isEmpty())
        assertTrue(StileTattico.TABULA_RASA.istruzioniSenzaPalla().isEmpty())
        // L'unico stile a intensità molto alta è il Gegenpress, l'unico a bassa il Parcheggia l'autobus
        assertEquals(listOf(StileTattico.GEGENPRESS), StileTattico.values().filter { it.intensita == Intensita.MOLTO_ALTA })
        assertEquals(listOf(StileTattico.PARCHEGGIA_AUTOBUS), StileTattico.values().filter { it.intensita == Intensita.BASSA })
    }

    @Test
    fun salvaECaricaIlPiano() {
        StileTattico.values().forEach { s ->
            val piano = s.pianoCompleto(mapOf(7L to "MED", 9L to "DC"))
            val letto = PianoTattico.decodifica(piano.codifica())
            assertEquals(s.nome, piano, letto)
        }
        assertEquals(PianoTattico(), PianoTattico.decodifica(""))
        assertEquals(PianoTattico(), PianoTattico.decodifica("testo rotto"))
    }

    @Test
    fun pianoVuotoNonCambiaNulla() {
        val e = PianoTattico().effetti(xi())
        assertEquals(1.0, e.poss, 1e-9); assertEquals(1.0, e.atk, 1e-9)
        assertEquals(1.0, e.def, 1e-9); assertEquals(1.0, e.drain, 1e-9)
    }

    @Test
    fun effettiPiccoliPerOgniStile() {
        StileTattico.values().forEach { s ->
            val e = s.pianoCompleto().effetti(xi())
            listOf(e.poss, e.atk, e.def, e.drain).forEach { v ->
                assertTrue("${s.nome}: effetto $v fuori misura", v in 0.9..1.15)
            }
        }
    }

    @Test
    fun tikiTakaDaPiuPossessoDellaPallaLunga() {
        val tiki = StileTattico.TIKI_TAKA.pianoCompleto().effetti(xi()).poss
        val lunga = StileTattico.PALLA_LUNGA.pianoCompleto().effetti(xi()).poss
        assertTrue(tiki > 1.0 && lunga < 1.0 && tiki > lunga)
    }

    @Test
    fun gegenpressStancaPiuDelCatenaccio() {
        val g = StileTattico.GEGENPRESS.pianoCompleto().effetti(xi()).drain
        val c = StileTattico.CATENACCIO.pianoCompleto().effetti(xi()).drain
        assertTrue(g > 1.0 && c < 1.0)
    }

    @Test
    fun idoneitaDipendeDallaRosa() {
        val tecnica = xi(80); val scarsa = xi(40)
        assertTrue(StileTattico.TIKI_TAKA.idoneita(tecnica) > StileTattico.TIKI_TAKA.idoneita(scarsa))
        assertEquals(50, StileTattico.TABULA_RASA.idoneita(tecnica))
        StileTattico.values().forEach { assertTrue(it.idoneita(tecnica) in 0..100) }
    }

    @Test
    fun ruoloSenzaPallaPiuComodoAiutaLaDifesa() {
        val rosa = xi()
        // Un mediano che con la palla gioca da regista al 70% e senza palla da mediano naturale al 100%
        val med = Player(id = 500, teamId = 1, name = "Mediano", role = "C", age = 25, overall = 65, morale = 70, salary = 0,
            nationality = "IT", pos = "MED", sec2 = "REG", aff2 = 70, ruoloPartita = "REG",
            ve = 65, re = 65, ag = 65, qu = 65, en = 65, pa = 65, dr = 65, fi = 65, ti = 65, ri = 65)
        val con = rosa.filter { it.role != "C" || it.pos != "MED" } + med
        val senza = PianoTattico().effetti(con).def
        val piano = PianoTattico().conRuoloSenzaPalla(med.id, "MED")
        assertTrue(piano.effetti(con).def > senza)
        // Il limite resta tra -8% e +8%
        assertTrue(piano.effetti(con).def <= 1.08 + 1e-9)
        // Togliendo il ruolo si torna al neutro
        assertEquals(senza, piano.conRuoloSenzaPalla(med.id, null).effetti(con).def, 1e-9)
    }

    @Test
    fun stelleDelRuolo() {
        val g = p("C", "REG", 80, sec2 = "MED", aff2 = 50)
        val naturale = RuoliFm.stelle(g, "REG")
        val affine = RuoliFm.stelle(g, "MED")
        assertTrue(naturale > affine)
        assertTrue(naturale in 0.5..5.0 && affine in 0.5..5.0)
        assertEquals(0.0, (naturale * 2) % 1.0, 1e-9)   // sempre a mezze stelle
    }

    // --- Partite ---

    private fun squadra(id: Long, v: Int, rng: Random): Pair<Team, List<Player>> {
        val t = Team(id = id, name = "S$id", shortName = "S$id", colorPrimary = 0, colorSecondary = 0,
            budget = 0, stadiumCapacity = 0, ticketPrice = 0, sponsor = "")
        val roles = List(2) { "P" } + List(6) { "D" } + List(6) { "C" } + List(4) { "A" }
        val ps = roles.mapIndexed { i, r ->
            Attr.fill(Player(id = id * 100 + i, teamId = id, name = "S${id}G$i", role = r, age = 26,
                overall = v + rng.nextInt(-5, 6), morale = 70, salary = 0, nationality = "ITA", potential = v, contract = 2), rng)
        }
        return t to ps
    }

    private fun mediaGol(pianoCasa: PianoTattico?, partite: Int): Double {
        val rng = Random(2026)
        var gol = 0
        repeat(partite) { k ->
            val (h, hp) = squadra(1, 70, rng); val (a, ap) = squadra(2, 70, rng)
            val r = MatchEngine.simulate(h, hp, a, ap, Formation.F442, Formation.F442, homePiano = pianoCasa, rng = Random(k.toLong()))
            gol += r.homeGoals + r.awayGoals
        }
        return gol.toDouble() / partite
    }

    @Test
    fun conUnPianoLeGolMedieRestanoRealistiche() {
        val base = mediaGol(null, 300)
        StileTattico.values().forEach { s ->
            val m = mediaGol(s.pianoCompleto(), 300)
            assertTrue("${s.nome}: $m gol a partita (base $base)", m in 1.5..4.2)
            assertTrue("${s.nome}: scarto dalla base troppo grande ($m contro $base)", Math.abs(m - base) < 0.5)
        }
        assertNotNull(base)
    }

    @Test
    fun istruzioniGiocatore() {
        var piano = PianoTattico()
        piano = piano.conIstruzione(5, IstrGiocatore.TIRA_DI_PIU)
        piano = piano.conIstruzione(5, IstrGiocatore.PRESSA_DI_PIU)
        piano = piano.conIstruzione(5, IstrGiocatore.TIRA_DI_MENO)   // stesso gruppo: sostituisce
        assertEquals(setOf(IstrGiocatore.TIRA_DI_MENO, IstrGiocatore.PRESSA_DI_PIU), piano.istrGioc[5])
        assertEquals(piano, PianoTattico.decodifica(piano.codifica()))
        piano = piano.conIstruzione(5, IstrGiocatore.TIRA_DI_MENO).conIstruzione(5, IstrGiocatore.PRESSA_DI_PIU)
        assertTrue(piano.istrGioc.isEmpty())
        val g = p("A", "CA")
        assertEquals(1.4, PianoTattico().conIstruzione(g.id, IstrGiocatore.TIRA_DI_PIU).pesoTiro(g), 1e-9)
        // Tutta la squadra con "Più sortite": effetto limitato a +5%
        val tutti = xi()
        val pieno = tutti.fold(PianoTattico()) { acc, q -> acc.conIstruzione(q.id, IstrGiocatore.PIU_SORTITE) }
        val e = pieno.effettiGiocatori(tutti)
        assertTrue(e.atk <= 1.05 + 1e-9 && e.def >= 0.95 - 1e-9 && e.atk > 1.0)
    }

    @Test
    fun stiliDelleSquadreAi() {
        val rosa = xi(75)
        assertTrue(StiliAi.scegli(1, null, rosa, grande = true) in StiliAi.GRANDI)
        assertTrue(StiliAi.scegli(1, Stile.ATTACCO, rosa, grande = false) in StiliAi.ATTACCO)
        assertTrue(StiliAi.scegli(1, Stile.DIFESA, rosa, grande = false) in StiliAi.DIFESA)
        assertEquals(StiliAi.scegli(9, Stile.DIFESA, rosa, false), StiliAi.scegli(9, Stile.DIFESA, rosa, false))
        val sq = (1L..6L).map { Team(id = it, name = "S$it", shortName = "S$it", colorPrimary = 0, colorSecondary = 0,
            budget = it * 1000, stadiumCapacity = (it * 10000).toInt(), ticketPrice = 0, sponsor = "", nazione = "Italia") }
        assertEquals(setOf(3L, 4L, 5L, 6L), StiliAi.grandi(sq))
    }

    @Test
    fun spostamentoRuolo() {
        val (dd, dw) = PostoRuolo.spostamento("MP", "MED")
        assertTrue(dd < 0f); assertEquals(0f, dw, 1e-6f)
        assertEquals(0f to 0f, PostoRuolo.spostamento("CA", "CA"))
    }
}
