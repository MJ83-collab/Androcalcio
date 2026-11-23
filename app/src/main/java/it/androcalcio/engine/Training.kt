package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/** Focus dell'allenamento settimanale: quali parametri crescono. */
enum class TrainFocus(val label: String, val info: String) {
    TECNICA("Tecnica", "QU, PA, DR, TI (portieri: GM)"),
    FISICO("Fisico", "VE, RE, AG"),
    TATTICA("Tattica", "EN, PA (portieri: GM)"),
    RIPOSO("Riposo", "Nessuna crescita, recupero +6 e meno infortuni")
}

/** Intensità: più crescita ma più fatica e più rischio di infortuni. */
enum class TrainIntensity(val label: String, val cond: Int, val injury: Double, val growth: Double) {
    BASSA("Bassa", 4, 0.8, 0.5),
    MEDIA("Media", 0, 1.0, 1.0),
    ALTA("Alta", -4, 1.3, 1.6)
}

data class TrainingPlan(val focus: TrainFocus, val intensity: TrainIntensity) {
    /** Moltiplicatore sul rischio di infortunio dopo la partita. */
    fun injuryFactor(): Double = if (focus == TrainFocus.RIPOSO) 0.7 else intensity.injury

    /** Variazione di condizione tra una partita e l'altra. */
    fun condDelta(): Int = if (focus == TrainFocus.RIPOSO) 6 else intensity.cond
}

object Training {
    /** Allenamento individuale della tua squadra: id giocatore -> focus. Letto da apply(). */
    @Volatile var individuali: Map<Long, TrainFocus> = emptyMap()

    fun encodeInd(m: Map<Long, TrainFocus>): String = m.entries.joinToString(";") { "${it.key}:${it.value.ordinal}" }
    fun decodeInd(s: String?): Map<Long, TrainFocus> = s.orEmpty().split(";").mapNotNull {
        val x = it.split(":"); val id = x.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
        val f = TrainFocus.values().getOrNull(x.getOrNull(1)?.toIntOrNull() ?: -1) ?: return@mapNotNull null
        id to f
    }.toMap()

    private const val BASE = 0.035   // probabilità per turno di +1 a un parametro (intensità media, 20-28 anni)

    private fun ageFactor(age: Int) = when {
        age <= 21 -> 1.5
        age <= 28 -> 1.0
        age <= 31 -> 0.5
        else -> 0.2
    }

    /** Parametri allenabili per focus e ruolo. */
    private fun attrs(f: TrainFocus, role: String): List<String> = when (f) {
        TrainFocus.TECNICA -> if (role == "P") listOf("gm", "qu") else listOf("qu", "pa", "dr", "ti")
        TrainFocus.FISICO -> listOf("ve", "re", "ag")
        TrainFocus.TATTICA -> if (role == "P") listOf("gm") else listOf("en", "pa")
        TrainFocus.RIPOSO -> emptyList()
    }

    private fun get(p: Player, a: String) = when (a) {
        "ve" -> p.ve; "re" -> p.re; "ag" -> p.ag; "qu" -> p.qu; "gm" -> p.gm; "en" -> p.en
        "pa" -> p.pa; "dr" -> p.dr; "ti" -> p.ti; else -> 99
    }

    private fun inc(p: Player, a: String) = when (a) {
        "ve" -> p.copy(ve = p.ve + 1); "re" -> p.copy(re = p.re + 1); "ag" -> p.copy(ag = p.ag + 1)
        "qu" -> p.copy(qu = p.qu + 1); "gm" -> p.copy(gm = p.gm + 1); "en" -> p.copy(en = p.en + 1)
        "pa" -> p.copy(pa = p.pa + 1); "dr" -> p.copy(dr = p.dr + 1); "ti" -> p.copy(ti = p.ti + 1)
        else -> p
    }

    /**
     * Effetto di una settimana di allenamento (dopo la partita).
     * Gli infortunati non si allenano. Un parametro non supera il livello + 12 né 99.
     * L'allenatore (coach 1..99) rende la crescita da 0,5× a 1,5×.
     */
    fun apply(p: Player, plan0: TrainingPlan, coach: Int, rng: Random): Player {
        if (p.injuredRounds > 0) return p
        val plan = individuali[p.id]?.let { plan0.copy(focus = it) } ?: plan0
        var out = p.copy(condition = (p.condition + plan.condDelta()).coerceIn(35, 100))
        val list = attrs(plan.focus, p.role)
        if (list.isEmpty()) return out
        val chance = BASE * plan.intensity.growth * ageFactor(p.age) * (1.0 + (coach - Staffing.NEUTRAL) / 100.0)
        if (rng.nextDouble() < chance) {
            val a = list[rng.nextInt(list.size)]
            if (get(out, a) < minOf(99, p.overall + 12)) out = inc(out, a)
        }
        return out
    }
}
