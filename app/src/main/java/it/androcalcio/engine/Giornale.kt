package it.androcalcio.engine

/** Giornale sportivo: titoli e pagelle della tua ultima partita. */
object Giornale {
    data class Edizione(val titolo: String, val sommario: String, val pagelle: List<Pagella>)
    data class Pagella(val nome: String, val voto: Double, val commento: String)

    fun commento(v: Double) = when {
        v >= 8.0 -> "Immenso"
        v >= 7.0 -> "Ottimo"
        v >= 6.5 -> "Buono"
        v >= 6.0 -> "Sufficiente"
        v >= 5.0 -> "Opaco"
        else -> "Disastroso"
    }

    fun titolo(mia: String, avversario: String, gf: Int, gs: Int, derby: Boolean): String = when {
        derby && gf > gs -> "Derby nostro! $mia batte $avversario $gf-$gs"
        derby && gf < gs -> "Derby amaro: $avversario piega $mia $gs-$gf"
        gf - gs >= 3 -> "Tris e oltre: $mia travolge $avversario ($gf-$gs)"
        gf > gs -> "$mia piega $avversario, $gf-$gs"
        gf == gs && gf == 0 -> "Noia a reti bianche: $mia-$avversario 0-0"
        gf == gs -> "$mia-$avversario $gf-$gs: pari e patta"
        gs - gf >= 3 -> "Crollo: $avversario travolge $mia ($gs-$gf)"
        else -> "$avversario batte $mia $gs-$gf"
    }

    fun sommario(gf: Int, gs: Int, migliore: Pagella?): String =
        (if (gf > gs) "Tre punti pesanti. " else if (gf == gs) "Un punto a testa. " else "Serata da dimenticare. ") +
            (migliore?.let { "Il migliore: ${it.nome} (${"%.1f".format(it.voto)})." } ?: "")

    fun crea(mia: String, avversario: String, gf: Int, gs: Int, derby: Boolean, voti: List<Pair<String, Double>>): Edizione {
        val pagelle = voti.sortedByDescending { it.second }.map { Pagella(it.first, it.second, commento(it.second)) }
        return Edizione(titolo(mia, avversario, gf, gs, derby), sommario(gf, gs, pagelle.firstOrNull()), pagelle)
    }

    fun encode(e: Edizione): String = listOf(e.titolo, e.sommario,
        e.pagelle.joinToString(";") { "${it.nome}|${it.voto}|${it.commento}" }).joinToString("§")

    fun decode(s: String?): Edizione? {
        val p = s.orEmpty().split("§")
        if (p.size != 3) return null
        val pag = p[2].split(";").mapNotNull {
            val q = it.split("|")
            if (q.size == 3) Pagella(q[0], q[1].toDoubleOrNull() ?: return@mapNotNull null, q[2]) else null
        }
        return Edizione(p[0], p[1], pag)
    }
}
