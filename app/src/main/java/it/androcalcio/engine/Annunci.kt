package it.androcalcio.engine

import it.androcalcio.data.SeedData
import it.androcalcio.data.Staff
import kotlin.random.Random

/**
 * Ricerca dello staff con annunci: il club pubblica un annuncio per un posto e i professionisti
 * interessati rispondono. Le risposte dipendono dal fascino del club e si rinnovano ogni mese.
 * Tutto salvato come testo nella tabella Stato, senza cambiare il database.
 */
object Annunci {
    const val COSTO = 25_000L
    const val KEY_ANNUNCI = "staff_annunci"
    const val KEY_CAND = "staff_cand"
    const val KEY_MESE = "staff_mese"

    data class Annuncio(val role: String, val specialita: String = "") {
        val chiave get() = "$role|$specialita"
        val nome get() = Staffing.roleName(role, specialita)
    }

    fun decodeAnnunci(s: String?): List<Annuncio> = s.orEmpty().split(";").mapNotNull {
        val p = it.split("|")
        if (p.size == 2 && p[0].isNotEmpty()) Annuncio(p[0], p[1]) else null
    }
    fun encodeAnnunci(l: List<Annuncio>) = l.joinToString(";") { it.chiave }

    fun encodeCand(l: List<Staff>) = l.joinToString(";") {
        listOf(it.role, it.specialita, it.name.replace(";", "").replace("|", ""), it.skill, it.salary, it.contract, it.stile).joinToString("|")
    }
    fun decodeCand(s: String?): List<Staff> = s.orEmpty().split(";").mapNotNull {
        val p = it.split("|")
        if (p.size != 7) null else runCatching {
            Staff(teamId = 0, role = p[0], specialita = p[1], name = p[2], skill = p[3].toInt(),
                salary = p[4].toLong(), contract = p[5].toInt(), stile = p[6])
        }.getOrNull()
    }

    /** Fascino del club 10-95: rosa forte e bilancio sano attirano professionisti migliori. */
    fun fascino(mediaRosa: Double, budget: Long): Int =
        (mediaRosa * 1.1 - 22 + if (budget > 5_000_000) 5 else 0).toInt().coerceIn(10, 95)

    /** Chi risponde a un annuncio in un dato mese: sempre le stesse persone fino al mese dopo. */
    fun risposte(teamId: Long, a: Annuncio, mese: String, fascino: Int, scala: Double = 1.0): List<Staff> {
        val rng = Random((teamId.toString() + a.chiave + mese).hashCode())
        val probabilita = 0.25 + fascino / 100.0 * 0.65
        val lista = mutableListOf<Staff>()
        // Almeno tre persone rispondono sempre; con più fascino possono essere fino a cinque
        val n = 3 + (0 until 2).count { rng.nextDouble() < probabilita }
        repeat(n) {
            val sk = (rng.nextInt(30, 56) + (fascino * 0.45).toInt() + rng.nextInt(-6, 7)).coerceIn(30, 92)
            lista += Staff(
                teamId = 0, role = a.role, specialita = a.specialita,
                name = "${SeedData.initials[rng.nextInt(SeedData.initials.length)]}. " +
                    SeedData.surnames[rng.nextInt(SeedData.surnames.size)],
                skill = sk, salary = Staffing.wage(sk, scala), contract = rng.nextInt(1, 4),
                stile = Stile.values()[rng.nextInt(Stile.values().size)].name
            )
        }
        return lista
    }

    /** Proposta di rinnovo: più anni = ingaggio un po' più basso; i più bravi chiedono di più. */
    fun nuovoIngaggio(s: Staff, anni: Int, scala: Double = 1.0): Long {
        val rialzo = if (s.skill >= 70) 1.15 else 1.05
        val sconto = 1.0 - 0.04 * (anni - 1)
        return ((Staffing.wage(s.skill, scala) * rialzo * sconto) / 1_000).toLong() * 1_000L
    }
}
