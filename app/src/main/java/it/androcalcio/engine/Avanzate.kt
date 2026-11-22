package it.androcalcio.engine

/** Statistiche avanzate di stagione: tiri, gol attesi (xG) e mappa di calore dei tiri della tua squadra. */
object Avanzate {
    const val COLONNE = 6
    const val RIGHE = 3
    /** Probabilità media che un tiro diventi gol, per stimare gli xG dai tiri. */
    const val XG_PER_TIRO = 0.11

    data class Totali(
        val partite: Int = 0, val tiriF: Int = 0, val tiriS: Int = 0, val golF: Int = 0, val golS: Int = 0,
        val possesso: Int = 0, val zone: List<Int> = List(COLONNE * RIGHE) { 0 }
    ) {
        val xgF get() = tiriF * XG_PER_TIRO
        val xgS get() = tiriS * XG_PER_TIRO
        val possessoMedio get() = if (partite == 0) 0 else possesso / partite
        val rendimentoAttacco get() = if (xgF <= 0.0) 0.0 else golF / xgF   // >1 = finalizzi meglio del previsto
        val rendimentoDifesa get() = if (xgS <= 0.0) 0.0 else golS / xgS    // <1 = subisci meno del previsto
        val massimaZona get() = zone.maxOrNull()?.coerceAtLeast(1) ?: 1
    }

    /** Indice di zona di un punto del campo (0..1); chi attacca verso sinistra viene specchiato. */
    fun zona(x: Float, y: Float, attaccaADestra: Boolean): Int {
        val xx = if (attaccaADestra) x else 1f - x
        val c = (xx * COLONNE).toInt().coerceIn(0, COLONNE - 1)
        val r = (y * RIGHE).toInt().coerceIn(0, RIGHE - 1)
        return r * COLONNE + c
    }

    /** Aggiunge una partita ai totali. eventi = (x, y, home, tipo) di tutti gli eventi della partita. */
    fun aggiungi(t: Totali, mioHome: Boolean, report: MatchReport): Totali {
        val zone = t.zone.toMutableList()
        report.events.filter { it.home == mioHome && (it.type == EventType.GOAL || it.type == EventType.SAVE || it.type == EventType.MISS) }
            .forEach { e -> zone[zona(e.x, e.y, mioHome)] += 1 }
        return Totali(
            t.partite + 1,
            t.tiriF + if (mioHome) report.homeShots else report.awayShots,
            t.tiriS + if (mioHome) report.awayShots else report.homeShots,
            t.golF + if (mioHome) report.homeGoals else report.awayGoals,
            t.golS + if (mioHome) report.awayGoals else report.homeGoals,
            t.possesso + if (mioHome) report.homePossession else 100 - report.homePossession,
            zone
        )
    }

    fun encode(t: Totali) = listOf(t.partite, t.tiriF, t.tiriS, t.golF, t.golS, t.possesso).plus(t.zone).joinToString(",")
    fun decode(s: String?): Totali {
        val v = s.orEmpty().split(",").mapNotNull { it.toIntOrNull() }
        if (v.size != 6 + COLONNE * RIGHE) return Totali()
        return Totali(v[0], v[1], v[2], v[3], v[4], v[5], v.drop(6))
    }
}
