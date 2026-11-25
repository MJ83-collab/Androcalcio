package it.androcalcio.engine

/** Analista video: cosa ha funzionato e cosa no nell'ultima partita, e dove i giocatori devono migliorare. */
object AnalistaPartita {
    const val KEY = "ana_ultima"

    data class Analisi(val bene: List<String>, val male: List<String>, val migliorare: List<String>)

    fun analizza(r: MatchReport, mioHome: Boolean, mioId: Long, skill: Int): Analisi {
        val bene = ArrayList<String>(); val male = ArrayList<String>(); val mig = ArrayList<String>()
        val gf = if (mioHome) r.homeGoals else r.awayGoals
        val gs = if (mioHome) r.awayGoals else r.homeGoals
        val tiri = if (mioHome) r.homeShots else r.awayShots
        val tiriAvv = if (mioHome) r.awayShots else r.homeShots
        val poss = if (mioHome) r.homePossession else 100 - r.homePossession
        val miei = r.events.filter { it.home == mioHome }
        val sprechi = miei.count { it.type == EventType.MISS }
        val ammon = miei.count { it.type == EventType.YELLOW || it.type == EventType.RED }
        val subiti = r.events.filter { it.type == EventType.GOAL && it.home != mioHome }

        if (tiri > tiriAvv + 2) bene += "Tiri: $tiri contro $tiriAvv, la squadra ha creato più dell'avversario"
        if (tiriAvv > tiri + 2) male += "Tiri subiti: $tiriAvv contro $tiri, il filtro a centrocampo non ha retto"
        if (poss >= 55) bene += "Possesso palla ${poss}%: il fraseggio ha funzionato"
        if (poss <= 45) male += "Possesso palla solo ${poss}%: troppe palle perse in uscita"
        if (skill >= 40 && sprechi >= 3) male += "$sprechi occasioni sprecate: serve più freddezza sotto porta"
        if (skill >= 40 && gf >= 2) bene += "Attacco in forma: $gf gol segnati"
        if (skill >= 50 && gs == 0) bene += "Difesa e portiere: porta inviolata"
        if (skill >= 50 && subiti.any { it.minute > 75 }) male += "Gol subito nel finale: calo di concentrazione e di fiato"
        if (skill >= 60 && ammon >= 3) male += "$ammon cartellini: troppa irruenza, rischio squalifiche"

        val mieiGioc = r.playedPlayers.filter { it.teamId == mioId }
        val voti = mieiGioc.mapNotNull { p -> r.playerRatings[p.id]?.let { p to it } }
        voti.maxByOrNull { it.second }?.let { (p, v) -> if (skill >= 45) bene += "Migliore in campo: ${p.name} (${"%.1f".format(v)})" }
        if (skill >= 55) voti.sortedBy { it.second }.take(2).forEach { (p, v) ->
            mig += "${p.name} (${"%.1f".format(v)}): " + debolezza(p)
        }
        if (bene.isEmpty()) bene += "Nessun aspetto davvero positivo da segnalare"
        if (male.isEmpty()) male += "Nessun errore grave da segnalare"
        return Analisi(bene, male, mig)
    }

    private fun debolezza(p: it.androcalcio.data.Player): String {
        val m = listOf("passaggio" to p.pa, "dribbling" to p.dr, "tiro" to p.ti, "resistenza" to p.re, "velocità" to p.ve)
        val peggio = m.minByOrNull { it.second }!!
        return "lavorare su ${peggio.first} (${peggio.second}) in allenamento"
    }

    /** Confidenza tattica: dopo ogni partita analizzata ogni ruolo secondario sotto il 100% guadagna 1 punto di affinità. */
    fun confidenza(p: it.androcalcio.data.Player): it.androcalcio.data.Player = p.copy(
        aff2 = if (p.sec2.isNotEmpty()) (p.aff2 + 1).coerceAtMost(100) else p.aff2,
        aff3 = if (p.sec3.isNotEmpty()) (p.aff3 + 1).coerceAtMost(100) else p.aff3,
        aff4 = if (p.sec4.isNotEmpty()) (p.aff4 + 1).coerceAtMost(100) else p.aff4
    )

    fun encode(a: Analisi) = (a.bene.map { "B:$it" } + a.male.map { "M:$it" } + a.migliorare.map { "G:$it" }).joinToString("\n")

    fun decode(s: String?): Analisi {
        val l = s.orEmpty().lines().filter { it.length > 2 }
        fun f(c: Char) = l.filter { it[0] == c }.map { it.substring(2) }
        return Analisi(f('B'), f('M'), f('G'))
    }
}
