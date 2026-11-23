package it.androcalcio.engine

/** Storia del club: scontri diretti, record di stagione, Hall of Fame e maglie ritirate. Tutto come testo nella tabella Stato. */
object Storia {
    data class Scontro(val v: Int = 0, val n: Int = 0, val p: Int = 0, val gf: Int = 0, val gs: Int = 0) {
        val partite get() = v + n + p
    }

    fun aggiornaScontro(m: Map<String, Scontro>, avversario: String, gf: Int, gs: Int): Map<String, Scontro> {
        val s = m[avversario] ?: Scontro()
        val nuovo = Scontro(
            s.v + if (gf > gs) 1 else 0, s.n + if (gf == gs) 1 else 0, s.p + if (gf < gs) 1 else 0, s.gf + gf, s.gs + gs)
        return m + (avversario to nuovo)
    }

    fun encodeScontri(m: Map<String, Scontro>): String =
        m.entries.joinToString(";") { "${it.key}|${it.value.v}|${it.value.n}|${it.value.p}|${it.value.gf}|${it.value.gs}" }

    fun decodeScontri(s: String?): Map<String, Scontro> = s.orEmpty().split(";").mapNotNull {
        val p = it.split("|")
        if (p.size == 6) {
            val n = p.drop(1).map { x -> x.toIntOrNull() ?: return@mapNotNull null }
            p[0] to Scontro(n[0], n[1], n[2], n[3], n[4])
        } else null
    }.toMap()

    /** Record della stagione appena finita. */
    data class Record(val punti: Int = 0, val golFatti: Int = 0, val golSubiti: Int = 999, val serieUtili: Int = 0,
                      val stagionePunti: String = "")

    /** Risultati della tua stagione in ordine di giornata: (gol fatti, gol subiti). */
    fun nuoviRecord(vecchi: Record, risultati: List<Pair<Int, Int>>, stagione: String): Record {
        if (risultati.isEmpty()) return vecchi
        val punti = risultati.fold(0) { acc, r -> acc + (if (r.first > r.second) 3 else if (r.first == r.second) 1 else 0) }
        val gf = risultati.sumOf { it.first }; val gs = risultati.sumOf { it.second }
        var serie = 0; var best = 0
        risultati.forEach { (a, b) -> if (a >= b) { serie++; best = maxOf(best, serie) } else serie = 0 }
        return Record(
            maxOf(vecchi.punti, punti), maxOf(vecchi.golFatti, gf), minOf(vecchi.golSubiti, gs), maxOf(vecchi.serieUtili, best),
            if (punti > vecchi.punti) stagione else vecchi.stagionePunti)
    }

    fun encodeRecord(r: Record) = "${r.punti};${r.golFatti};${r.golSubiti};${r.serieUtili};${r.stagionePunti}"
    fun decodeRecord(s: String?): Record {
        val p = s.orEmpty().split(";")
        if (p.size < 5) return Record()
        return Record(p[0].toIntOrNull() ?: 0, p[1].toIntOrNull() ?: 0, p[2].toIntOrNull() ?: 999, p[3].toIntOrNull() ?: 0, p[4])
    }

    /** Un'icona del club: anni con te, partite e gol in carriera. */
    data class Bandiera(val nome: String, val numero: Int, val anni: Int, val gol: Int, val media: Int)

    const val ANNI_HALL = 5
    const val ANNI_MAGLIA = 8

    fun inHall(anni: Int, media: Int, gol: Int): Boolean = anni >= ANNI_HALL && (media >= 78 || gol >= 50)
    fun magliaRitirata(b: Bandiera): Boolean = b.anni >= ANNI_MAGLIA && b.numero > 0

    fun encodeBandiere(l: List<Bandiera>) = l.joinToString(";") { "${it.nome}|${it.numero}|${it.anni}|${it.gol}|${it.media}" }
    fun decodeBandiere(s: String?): List<Bandiera> = s.orEmpty().split(";").mapNotNull {
        val p = it.split("|")
        if (p.size == 5) Bandiera(p[0], p[1].toIntOrNull() ?: 0, p[2].toIntOrNull() ?: 0, p[3].toIntOrNull() ?: 0, p[4].toIntOrNull() ?: 0) else null
    }
}
