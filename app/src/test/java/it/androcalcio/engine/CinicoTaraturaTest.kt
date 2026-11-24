package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Taratura del tratto Cinico: gli stessi campionati giocati due volte, senza Cinici e con un Cinico ogni sei giocatori
 * (un difensore, un centrocampista e un attaccante per squadra). Stesse rose e stesso seme per ogni partita, così la
 * differenza misura l'effetto del tratto. Nessun giocatore ha il Cinico innato (nomi scelti apposta).
 */
class CinicoTaraturaTest {

    private val roles = List(2) { "P" } + List(6) { "D" } + List(6) { "C" } + List(4) { "A" }
    /** Posti dei Cinici: un difensore, un centrocampista e un attaccante, non tra i più forti della rosa. */
    private val postiCinici = setOf(3, 9, 15)

    @After fun pulisci() { Tratti.acquisiti = emptySet() }

    private fun nomeNonInnato(base: String): String {
        var n = base
        while (Tratti.innato(Player(teamId = 0, name = n, role = "A", age = 25, overall = 60, morale = 50, salary = 0, nationality = "ITA"))) n += "x"
        return n
    }

    private fun squadra(id: Long, tier: Int, rng: Random): Pair<Team, List<Player>> {
        val t = Team(id = id, name = "S$id", shortName = "S$id", colorPrimary = 0, colorSecondary = 0,
            budget = 0, stadiumCapacity = 0, ticketPrice = 0, sponsor = "")
        val base = 82 - tier * 9 + rng.nextInt(-4, 5)
        val ps = roles.mapIndexed { slot, role ->
            val age = rng.nextInt(17, 37)
            val star = if (slot == 2 || slot == 8 || slot == 14 || slot == 0) 4 else 0
            val ov = (base + star + rng.nextInt(-9, 9) - if (age < 21) 6 else 0).coerceIn(35, 95)
            Attr.fill(Player(id = id * 100 + slot, teamId = id, name = nomeNonInnato("T${id}S$slot"), role = role, age = age,
                overall = ov, morale = 70, salary = 0, nationality = "ITA", potential = ov, contract = 2), rng)
        }
        return t to ps
    }

    private class Stat {
        var n = 0; var gol = 0; var golTardi = 0
        val golCinici = HashMap<String, Int>()          // per ruolo, gol dei giocatori nei posti dei Cinici
        var rigori = 0; var rigoriGol = 0; var rigoriCinici = 0; var rigoriCiniciGol = 0
    }

    /** Un campionato a 20 squadre, andata e ritorno. Con cinici = true i giocatori dei postiCinici hanno il tratto. */
    private fun campionato(tier: Int, seme: Long, cinici: Boolean, st: Stat) {
        val rng = Random(seme)
        val sq = (1L..20L).map { squadra(it, tier, rng) }
        val tutti = sq.flatMap { it.second }
        val marcati = tutti.filter { (it.id % 100).toInt() in postiCinici }
        val ruolo = marcati.associate { it.id to it.role }
        val perNome = tutti.associateBy { it.name }
        Tratti.acquisiti = if (cinici) marcati.map { it.id }.toSet() else emptySet()
        var k = 0L
        for (h in sq) for (a in sq) if (h !== a) {
            val rm = Random(seme * 1_000_003L + k++)   // seme per partita: le due versioni restano allineate
            val r = MatchEngine.simulate(h.first, h.second, a.first, a.second,
                Formation.values().random(rm), Formation.values().random(rm), rng = rm)
            st.n++; st.gol += r.homeGoals + r.awayGoals
            for (e in r.events) {
                val rigoreGol = e.type == EventType.GOAL && e.text.startsWith("Gol su Rigore")
                val rigoreNo = (e.type == EventType.SAVE || e.type == EventType.MISS) && e.text.startsWith("Rigore di ")
                if (e.type == EventType.GOAL) {
                    if (e.minute > 75) st.golTardi++
                    ruolo[e.playerId]?.let { st.golCinici[it] = (st.golCinici[it] ?: 0) + 1 }
                }
                if (rigoreGol || rigoreNo) {
                    st.rigori++
                    if (rigoreGol) st.rigoriGol++
                    val chi = if (rigoreGol) tutti.firstOrNull { it.id == e.playerId }
                        else perNome[e.text.removePrefix("Rigore di ").substringBefore(",")]
                    if (chi != null && Tratti.cinico(chi)) { st.rigoriCinici++; if (rigoreGol) st.rigoriCiniciGol++ }
                }
            }
        }
    }

    @Test
    fun taraturaCinico() {
        val base = Stat(); val cin = Stat()
        for (tier in 0..2) for (s in 0 until 4) {
            val seme = 7000L + tier * 100 + s
            campionato(tier, seme, false, base)
            campionato(tier, seme, true, cin)
        }
        Tratti.acquisiti = emptySet()
        val loc = java.util.Locale.ITALY
        fun g(st: Stat) = st.gol.toDouble() / st.n
        fun tardi(st: Stat) = 100.0 * st.golTardi / st.gol
        fun conv(st: Stat) = 100.0 * st.rigoriGol / st.rigori
        val rapporto = cin.golCinici.values.sum().toDouble() / base.golCinici.values.sum()
        println(String.format(loc, "TARATURA cinico: partite %d, gol/partita senza %.3f, con %.3f (%+.2f%%)",
            base.n, g(base), g(cin), 100.0 * (g(cin) / g(base) - 1)))
        println(String.format(loc, "TARATURA cinico: gol dei Cinici %d contro %d da non Cinici, rapporto %.3f",
            cin.golCinici.values.sum(), base.golCinici.values.sum(), rapporto))
        for (r in listOf("A", "C", "D")) println(String.format(loc, "TARATURA cinico ruolo %s: %d contro %d, rapporto %.3f",
            r, cin.golCinici[r] ?: 0, base.golCinici[r] ?: 0, (cin.golCinici[r] ?: 0).toDouble() / (base.golCinici[r] ?: 1)))
        println(String.format(loc, "TARATURA cinico: gol dopo il 75' senza %.1f%%, con %.1f%%", tardi(base), tardi(cin)))
        println(String.format(loc, "TARATURA cinico: rigori senza %d, segnati %.1f%%; con %d, segnati %.1f%%; tirati da Cinici %d, segnati %.1f%%",
            base.rigori, conv(base), cin.rigori, conv(cin), cin.rigoriCinici, 100.0 * cin.rigoriCiniciGol / cin.rigoriCinici.coerceAtLeast(1)))

        assertTrue("Gol/partita senza Cinici ${g(base)} fuori da 2,4-3,0", g(base) in 2.4..3.0)
        assertTrue("Gol/partita con Cinici ${g(cin)} fuori da 2,4-3,0", g(cin) in 2.4..3.0)
        assertTrue("Il Cinico non deve creare molti gol in più: ${g(cin)} contro ${g(base)}", g(cin) / g(base) in 0.98..1.025)
        assertTrue("Il Cinico deve segnare il 10-15% in più (margine 6-20%): rapporto $rapporto", rapporto in 1.06..1.20)
        assertTrue("Quota di gol dopo il 75' spostata troppo: ${tardi(base)} -> ${tardi(cin)}", tardi(cin) - tardi(base) in -0.5..2.0)
        assertTrue("Conversione rigori ${conv(cin)}% fuori da 72-90%", conv(cin) in 72.0..90.0)
    }

    // --- Regole semplici ---

    private fun gioc(id: Long, nome: String, role: String = "A", fi: Int = 70) =
        Player(id = id, teamId = 1, name = nomeNonInnato(nome), role = role, age = 25, overall = 70, morale = 50,
            salary = 0, nationality = "ITA", fi = fi)

    @Test
    fun rigoreCinicoSegnaDiPiu() {
        val c = gioc(42, "Cinico"); val n = gioc(43, "Normale")
        Tratti.acquisiti = setOf(42L)
        val base = 0.70 + 0.2 * 70 / 99.0
        assertTrue(Tratti.rigore(base, c) > Tratti.rigore(base, n))
        assertEquals(base, Tratti.rigore(base, n), 1e-9)
        assertEquals(base, Tratti.rigore(base, null), 1e-9)
        assertEquals(base + Tratti.BONUS_RIGORE, Tratti.rigore(base, c), 1e-9)
        assertTrue(Tratti.rigore(0.96, c) <= Tratti.RIGORE_MAX)
        assertTrue(Tratti.pesoTiro(c) > Tratti.pesoTiro(n))
    }

    @Test
    fun finaleSoloNelFinaleEInBilico() {
        val c = gioc(42, "Cinico"); val n = gioc(43, "Normale")
        Tratti.acquisiti = setOf(42L)
        assertEquals(1.0, Tratti.fattoreFinale(c, Tratti.FINALE_DA - 1, 0, 0), 1e-9)   // prima della soglia
        assertEquals(1.0, Tratti.fattoreFinale(c, 88, 3, 1), 1e-9)                     // avanti di 2: non in bilico
        assertEquals(1.0, Tratti.fattoreFinale(n, 88, 0, 1), 1e-9)                     // non Cinico
        assertEquals(1.0 + Tratti.BONUS_FINALE, Tratti.fattoreFinale(c, Tratti.FINALE_DA, 1, 0), 1e-9)
        assertEquals(1.0 + Tratti.BONUS_FINALE_FORTE, Tratti.fattoreFinale(c, 90, 0, 3), 1e-9)   // sotto conta sempre
        assertTrue(Tratti.fattoreFinale(c, Tratti.FINALE_FORTE_DA, 0, 0) > Tratti.fattoreFinale(c, Tratti.FINALE_DA, 0, 0))
    }

    @Test
    fun serieRigoriCinicoSegnaDiPiu() {
        val c = gioc(42, "Cinico"); val n = gioc(43, "Normale")
        Tratti.acquisiti = setOf(42L)
        var golC = 0; var golN = 0
        for (seme in 0 until 3000) {
            val sc = SerieRigori(true, Random(seme), rigoristiCasa = listOf(c))
            val sn = SerieRigori(true, Random(seme), rigoristiCasa = listOf(n))
            sc.scegli(seme % 3); sn.scegli(seme % 3)
            if (sc.tiri[0].gol) golC++
            if (sn.tiri[0].gol) golN++
            assertTrue(!sn.tiri[0].gol || sc.tiri[0].gol)   // stesso seme: il Cinico segna almeno quando segna l'altro
        }
        assertTrue("Cinico $golC, non Cinico $golN", golC > golN)
        // Senza rigoristi la serie funziona come prima, senza nomi
        val s = SerieRigori(false, Random(1))
        s.scegli(0)
        assertEquals("", s.tiri[0].chi)
    }

    @Test
    fun serieRigoriTiratoriAGiro() {
        val a = gioc(1, "Primo"); val b = gioc(2, "Secondo")
        val s = SerieRigori(true, Random(5), rigoristiCasa = listOf(a, b), rigoristiOspiti = listOf(b))
        assertTrue(s.prossimoTiratore() === a)
        s.scegli(0)          // casa: a
        assertTrue(s.prossimoTiratore() === b)
        s.scegli(0)          // ospiti: b
        assertTrue(s.prossimoTiratore() === b)
        s.scegli(0)          // casa: b
        assertTrue(s.prossimoTiratore() === b)
        s.scegli(0)          // ospiti: b
        assertTrue(s.prossimoTiratore() === a)
        assertEquals(a.name, s.tiri[0].chi)
        assertEquals(b.name, s.tiri[2].chi)
    }
}
