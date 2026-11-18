package it.androcalcio.engine

import it.androcalcio.data.Player
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Allenamento: in una stagione (38 turni) l'intensità alta fa crescere più della bassa, il riposo per niente. */
class TrainingTest {

    private fun rosa(rng: Random) = (1..18).map {
        Attr.fill(Player(teamId = 1, name = "G$it", role = if (it <= 2) "P" else listOf("D", "C", "A")[it % 3],
            age = rng.nextInt(18, 34), overall = 65, morale = 70, salary = 0, nationality = "ITA",
            potential = 75, contract = 2), rng)
    }

    private fun somma(ps: List<Player>) = ps.sumOf { it.ve + it.re + it.ag + it.qu + it.gm + it.en + it.pa + it.dr + it.ti }

    private fun stagione(plan: TrainingPlan): Int {
        val rng = Random(7)
        val start = rosa(rng)
        var ps = start
        repeat(38) { ps = ps.map { Training.apply(it, plan, Staffing.NEUTRAL, rng) } }
        return somma(ps) - somma(start)
    }

    @Test
    fun crescitaPerIntensita() {
        val bassa = stagione(TrainingPlan(TrainFocus.TECNICA, TrainIntensity.BASSA))
        val media = stagione(TrainingPlan(TrainFocus.TECNICA, TrainIntensity.MEDIA))
        val alta = stagione(TrainingPlan(TrainFocus.TECNICA, TrainIntensity.ALTA))
        val riposo = stagione(TrainingPlan(TrainFocus.RIPOSO, TrainIntensity.ALTA))
        println("Punti parametro in una stagione: bassa=$bassa media=$media alta=$alta riposo=$riposo")
        assertTrue(bassa < alta)
        assertTrue(media in 10..40)
        assertTrue(riposo == 0)
    }
}
