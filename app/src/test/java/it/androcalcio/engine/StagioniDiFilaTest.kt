package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team
import it.androcalcio.engine.CoppeEuropee.Coppa
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tre stagioni di fila, tutte le competizioni in memoria: calendari di campionato, coppa nazionale e Supercoppa
 * per ogni nazione europea, le tre coppe europee con qualificate dalla stagione precedente e sorteggi turno per turno.
 * Controlla che i turni (Turni) non facciano mai giocare una squadra due volte nello stesso turno.
 */
class StagioniDiFilaTest {
    private var nextTeam = 1L
    private var nextMatch = 1L

    private fun mondo(): List<Team> = CoppeEuropee.PRIME_DIVISIONI.flatMap { (nazione, camp) ->
        List(if (nazione == "Scozia") 10 else 18) {
            Team(id = nextTeam++, name = "$nazione $it", shortName = "T", colorPrimary = 0, colorSecondary = 0, budget = 0,
                stadiumCapacity = 0, ticketPrice = 0, sponsor = "", nazione = nazione, campionato = camp)
        }
    }

    /** Gioca tutto con risultati a caso; [prossimo] sorteggia il turno successivo quando il precedente è finito. */
    private fun giocaTutto(
        iniziali: List<MatchResult>, prossimo: (List<MatchResult>) -> List<MatchResult>,
        finalizza: (MatchResult, List<MatchResult>) -> MatchResult, rng: Random, nome: String
    ): List<MatchResult> {
        var tutte = iniziali.map { it.copy(id = nextMatch++) }
        var giri = 0
        while (giri++ < 80) {
            val da = tutte.filter { !it.played }
            if (da.isEmpty()) {
                val nuove = prossimo(tutte)
                if (nuove.isEmpty()) break
                tutte = tutte + nuove.map { it.copy(id = nextMatch++) }
                continue
            }
            val turno = da.minOf { it.round }
            tutte = tutte.map { m ->
                if (m.round == turno && !m.played) m.copy(homeGoals = rng.nextInt(0, 4), awayGoals = rng.nextInt(0, 3), played = true) else m
            }
            val foto = tutte
            tutte = foto.map { finalizza(it, foto) }
        }
        assertTrue("$nome: non finisce mai", giri < 80)
        return tutte
    }

    private fun stadio(p: List<MatchResult>, s: Int) = p.filter { Cup.stageOf(it.round) == s }
    private fun squadreDi(p: List<MatchResult>) = p.flatMap { listOf(it.homeTeamId, it.awayTeamId) }.toSet()

    @Test fun treStagioniDiFila() {
        val sq = mondo()
        val rng = Random(11)
        val forza = sq.associate { it.id to 60 + rng.nextDouble() * 25 }
        val byId = sq.associateBy { it.id }
        val naz = sq.associate { it.id to it.nazione }
        var qualificate = CoppeEuropee.qualificateIniziali(sq, forza)
        val campioniEuropei = ArrayList<Long>()

        for (s in listOf("2026/27", "2027/28", "2028/29")) {
            // ---- qualificate europee: arrivano dalla stagione precedente
            val per = qualificate.values.groupingBy { it }.eachCount()
            assertEquals("$s Campioni", 32, per[Coppa.CAMPIONI])
            assertEquals("$s Europa", 16, per[Coppa.EUROPA])
            assertEquals("$s Coppe", 16, per[Coppa.COPPE])
            assertEquals("$s: una squadra gioca una sola coppa", qualificate.size, qualificate.keys.toSet().size)

            val tutte = ArrayList<MatchResult>()
            val nuoveQualificate = LinkedHashMap<Long, Coppa>()

            // ---- ogni nazione: campionato, coppa nazionale, Supercoppa
            CoppeEuropee.PRIME_DIVISIONI.forEach { (nazione, camp) ->
                val squadre = sq.filter { it.nazione == nazione && it.campionato == camp }
                val ids = squadre.map { it.id }
                val lega = Fixtures.generate(ids, s, "$nazione|$camp")
                assertEquals("$s $nazione campionato", ids.size * (ids.size - 1), lega.size)

                val chiave = Cup.key(nazione)
                val coppa = giocaTutto(
                    Cup.firstStage(ids, s, chiave, rng, nazione),
                    { Cup.nextStage(it, squadre, forza, s, chiave, nazione, rng) },
                    { m, a -> Cup.finalize(m, a, rng) }, rng, "$s coppa $nazione"
                )
                val finale = Cup.finalMatch(coppa)
                assertNotNull("$s $nazione: manca la finale di coppa", finale)
                val vince = Cup.winnerOfTie(listOf(finale!!), forza)
                val perde = if (vince == finale.homeTeamId) finale.awayTeamId else finale.homeTeamId

                val classifica = ids.shuffled(rng)
                val supercoppa = Cup.superFirst(nazione, s, Cup.superKey(nazione), classifica, vince, perde)
                assertTrue("$s $nazione: manca la Supercoppa", supercoppa.isNotEmpty())

                tutte += lega + coppa + supercoppa
                nuoveQualificate.putAll(CoppeEuropee.qualificate(nazione, classifica, vince, perde))
            }

            // ---- coppe europee: gironi e turni, sorteggiati man mano
            Coppa.values().forEach { c ->
                val ids = qualificate.filterValues { it == c }.keys.toList()
                val key = CoppeEuropee.key(c)
                val gironi = if (c.conGironi) CoppeEuropee.sorteggiaGironi(ids, naz, forza, rng) else emptyList()
                if (c.conGironi) {
                    assertEquals("$s Campioni: otto gironi", 8, gironi.size)
                    assertTrue("$s Campioni: gironi da quattro", gironi.all { it.size == 4 })
                    assertEquals("$s Campioni: tutte le qualificate nei gironi", ids.toSet(), gironi.flatten().toSet())
                }
                val prime = if (c.conGironi) CoppeEuropee.partiteGironi(gironi, s, key)
                else CoppeEuropee.primoTurno(ids, forza, s, key, rng)
                val p = giocaTutto(
                    prime,
                    { CoppeEuropee.prossimoTurno(c, it, gironi, byId, forza, s, rng) },
                    { m, a -> CoppeEuropee.finalizza(m, a, rng) }, rng, "$s ${c.nome}"
                )
                if (c.conGironi) assertEquals("$s ${c.nome}: partite dei gironi", 96, p.count { CoppeEuropee.inGironi(it.round) })
                val ottavi = stadio(p, CoppeEuropee.STAGE_OTTAVI); val quarti = stadio(p, CoppeEuropee.STAGE_QUARTI)
                val semi = stadio(p, CoppeEuropee.STAGE_SEMI); val fin = stadio(p, CoppeEuropee.STAGE_FINALE)
                assertEquals("$s ${c.nome} ottavi", 16, ottavi.size)
                assertEquals("$s ${c.nome} quarti", 8, quarti.size)
                assertEquals("$s ${c.nome} semifinali", 4, semi.size)
                assertEquals("$s ${c.nome} finale", 1, fin.size)
                assertEquals("$s ${c.nome}: 16 squadre agli ottavi", 16, squadreDi(ottavi).size)
                assertTrue("$s ${c.nome}: ottavi solo tra le qualificate", ids.containsAll(squadreDi(ottavi)))
                assertTrue("$s ${c.nome}: quarti dagli ottavi", squadreDi(ottavi).containsAll(squadreDi(quarti)))
                assertTrue("$s ${c.nome}: semi dai quarti", squadreDi(quarti).containsAll(squadreDi(semi)))
                assertTrue("$s ${c.nome}: finale dalle semi", squadreDi(semi).containsAll(squadreDi(fin)))
                val titolo = CoppeEuropee.finale(p, forza)
                assertNotNull("$s ${c.nome}: manca il vincitore", titolo)
                if (c == Coppa.CAMPIONI) campioniEuropei += titolo!!.first
                tutte += p
            }

            // ---- nessuna squadra gioca due volte nello stesso turno; nessun turno cade nelle date delle nazionali
            val perSquadra = HashMap<Long, MutableList<Int>>()
            tutte.forEach { m ->
                val k = Turni.chiave(m)
                perSquadra.getOrPut(m.homeTeamId) { ArrayList() }.add(k)
                perSquadra.getOrPut(m.awayTeamId) { ArrayList() }.add(k)
                if (Cup.isCup(m.round) && m.round < Cup.BASE + Cup.SUPER_STAGE * 10) {
                    assertTrue("$s ${Turni.etichetta(m)} cade in una data delle nazionali (giornata ${Turni.giornata(k)})",
                        Turni.giornata(k) !in Nazionali.DATE)
                }
            }
            perSquadra.forEach { (id, chiavi) ->
                val doppie = chiavi.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
                assertTrue("$s ${byId[id]?.name}: due partite nello stesso turno: $doppie", doppie.isEmpty())
            }

            // ---- andata prima del ritorno
            tutte.filter { Cup.isCup(it.round) }.groupBy { Triple(it.league, Cup.stageOf(it.round), setOf(it.homeTeamId, it.awayTeamId)) }
                .values.filter { it.size == 2 && !CoppeEuropee.inGironi(it[0].round) }.forEach { due ->
                    val ord = due.sortedBy { it.round }
                    assertTrue("$s andata dopo il ritorno", Turni.chiave(ord[0]) < Turni.chiave(ord[1]))
                }

            // ---- la Supercoppa è dopo tutto il resto
            val ultimaNormale = tutte.filter { !it.league.startsWith("SUPERCOPPA") }.maxOf { Turni.chiave(it) }
            tutte.filter { it.league.startsWith("SUPERCOPPA") }.forEach {
                assertTrue("$s Supercoppa prima della fine", Turni.chiave(it) > ultimaNormale)
            }

            qualificate = nuoveQualificate
        }
        assertEquals(3, campioniEuropei.size)
    }
}
