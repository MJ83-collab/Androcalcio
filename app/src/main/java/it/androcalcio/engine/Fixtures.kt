package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import it.androcalcio.data.Team

object Fixtures {
    /** Calendario all'italiana andata e ritorno con il metodo del cerchio. */
    fun generate(ids: List<Long>, season: String, league: String): List<MatchResult> {
        val arr = ArrayList<Long?>(ids)
        if (arr.size % 2 == 1) arr.add(null)
        val n = arr.size
        val rounds = n - 1
        val out = mutableListOf<MatchResult>()
        for (r in 0 until rounds) {
            for (i in 0 until n / 2) {
                val a = arr[i]
                val b = arr[n - 1 - i]
                if (a != null && b != null) {
                    val flip = (r + i) % 2 == 1
                    val h = if (flip) b else a
                    val v = if (flip) a else b
                    out += MatchResult(season = season, round = r + 1, homeTeamId = h,
                        awayTeamId = v, homeGoals = 0, awayGoals = 0, league = league)
                    out += MatchResult(season = season, round = r + 1 + rounds, homeTeamId = v,
                        awayTeamId = h, homeGoals = 0, awayGoals = 0, league = league)
                }
            }
            val last = arr.removeAt(n - 1)
            arr.add(1, last)
        }
        return out
    }
}

data class StandingRow(
    val team: Team, val played: Int, val won: Int, val drawn: Int,
    val lost: Int, val gf: Int, val ga: Int
) {
    val pts get() = won * 3 + drawn
    val gd get() = gf - ga
}

fun standings(teams: List<Team>, ms: List<MatchResult>): List<StandingRow> {
    // per squadra: v, n, p, gf, gs
    val acc = teams.associate { it.id to IntArray(5) }
    ms.filter { it.played }.forEach { m ->
        val h = acc[m.homeTeamId] ?: return@forEach
        val a = acc[m.awayTeamId] ?: return@forEach
        h[3] += m.homeGoals; h[4] += m.awayGoals
        a[3] += m.awayGoals; a[4] += m.homeGoals
        when {
            m.homeGoals > m.awayGoals -> { h[0]++; a[2]++ }
            m.homeGoals < m.awayGoals -> { a[0]++; h[2]++ }
            else -> { h[1]++; a[1]++ }
        }
    }
    return teams.map { t ->
        val x = acc.getValue(t.id)
        StandingRow(t, x[0] + x[1] + x[2], x[0], x[1], x[2], x[3], x[4])
    }.sortedWith(
        compareByDescending<StandingRow> { it.pts }.thenByDescending { it.gd }.thenByDescending { it.gf }
    )
}
