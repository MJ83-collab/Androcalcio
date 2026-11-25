package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/**
 * Fisioterapia: propensione agli infortuni nascosta (si scopre solo con un test a pagamento)
 * e operazione per accorciare lo stop. Precisione, costo ed esito dipendono dallo specialista.
 */
object Fisio {
    fun chiaveTest(id: Long) = "test_fisio_$id"
    fun chiaveOperato(id: Long) = "operato_$id"

    /** Fragilità intrinseca 0..100: fissa per giocatore, cresce dopo i 29 anni. */
    fun fragilita(p: Player): Int {
        val h = ((p.id * 2654435761L) ushr 7).toInt() and 0x7fffffff
        return (10 + h % 61 + (p.age - 29).coerceAtLeast(0) * 4).coerceIn(0, 100)
    }

    /** Peso per scegliere chi si fa male: un fragile rischia fino a 5 volte un robusto. */
    fun peso(p: Player): Double = 1.0 + fragilita(p) / 25.0

    fun <T> sceltaPesata(l: List<T>, rng: Random, peso: (T) -> Double): T {
        var r = rng.nextDouble() * l.sumOf(peso)
        for (x in l) { r -= peso(x); if (r <= 0) return x }
        return l.last()
    }

    fun livello(f: Int) = when { f >= 60 -> "ALTA"; f >= 35 -> "MEDIA"; else -> "BASSA" }

    fun costoTest(skill: Int): Long = 30_000L + skill * 1_000L
    fun errore(skill: Int): Int = ((100 - skill) / 5).coerceIn(2, 15)

    /** Esito salvato come "stima:errore". */
    fun esegui(p: Player, skill: Int, rng: Random): String {
        val e = errore(skill)
        return "${(fragilita(p) + rng.nextInt(-e, e + 1)).coerceIn(0, 100)}:$e"
    }

    fun testo(s: String?): String? {
        val (v, e) = s?.split(":")?.mapNotNull { it.toIntOrNull() }?.takeIf { it.size == 2 } ?: return null
        return "Propensione ${livello(v)} ($v/100 ±$e)"
    }

    fun costoOperazione(p: Player, turni: Int): Long = 100_000L + turni * 50_000L + p.overall * 2_000L
    fun turniRisparmiati(turni: Int, skill: Int): Int = (turni * (0.3 + skill / 200.0)).toInt().coerceIn(1, (turni - 1).coerceAtLeast(1))
    fun rischioRicaduta(skill: Int): Int = (25 - skill / 5).coerceIn(5, 20)

    /** Nuovi turni di stop dopo l'operazione; con la ricaduta si perde il vantaggio e si aggiunge un turno. */
    fun opera(turni: Int, skill: Int, rng: Random): Pair<Int, Boolean> {
        val ricaduta = rng.nextInt(100) < rischioRicaduta(skill)
        return if (ricaduta) (turni + 1) to true else (turni - turniRisparmiati(turni, skill)).coerceAtLeast(1) to false
    }
}
