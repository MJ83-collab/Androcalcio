package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/**
 * Personalità dei giocatori: non si salva nulla, il tipo nasce dal nome e dalla nazionalità (sempre lo stesso).
 * Cambia come reagiscono ai risultati e alla panchina, e chi chiede un aumento o la cessione.
 */
object Personalita {
    enum class Tipo(val nome: String, val descrizione: String) {
        LEADER("Leader", "Trascina il gruppo: i successi lo esaltano."),
        PROFESSIONISTA("Professionista", "Sempre uguale: i risultati lo toccano poco."),
        AMBIZIOSO("Ambizioso", "Vuole giocare: la panchina lo manda in crisi."),
        MERCENARIO("Mercenario", "Segue i soldi: chiede aumenti se pagato poco."),
        SENSIBILE("Sensibile", "Soffre le sconfitte e le critiche."),
        FEDELE("Fedele", "Legato alla maglia: resiste anche ai momenti bui.")
    }

    fun tipo(p: Player): Tipo {
        val h = Math.floorMod(p.name.hashCode() * 31 + p.nationality.hashCode(), Tipo.values().size)
        return Tipo.values()[h]
    }

    /** Variazione di morale dopo una partita della tua squadra. */
    fun moraleDopo(p: Player, gf: Int, gs: Int, haGiocato: Boolean): Int {
        val esito = when { gf > gs -> 1; gf < gs -> -1; else -> 0 }
        var d = esito
        when (tipo(p)) {
            Tipo.LEADER -> if (esito > 0) d += 1
            Tipo.PROFESSIONISTA -> d = 0
            Tipo.AMBIZIOSO -> d += if (haGiocato) 1 else -3
            Tipo.MERCENARIO -> d = esito / 2
            Tipo.SENSIBILE -> if (esito < 0) d -= 2 else if (esito > 0) d += 1
            Tipo.FEDELE -> if (esito < 0) d = 0
        }
        return d
    }

    data class Richiesta(val playerId: Long, val tipo: String, val importo: Long = 0L) {
        companion object {
            const val AUMENTO = "aumento"
            const val CESSIONE = "cessione"
        }
    }

    const val MAX_RICHIESTE = 3

    /** Ingaggio "giusto" per il livello e l'età del giocatore. */
    fun ingaggioGiusto(p: Player): Long = Market.wageAt(p.overall, p.age)

    /** Chi, dopo una partita, si fa avanti: aumento (mercenari pagati poco) o cessione (ambiziosi scontenti in panchina). */
    fun nuove(rosa: List<Player>, esistenti: List<Richiesta>, rng: Random): List<Richiesta> {
        if (esistenti.size >= MAX_RICHIESTE) return emptyList()
        val gia = esistenti.map { it.playerId }.toSet()
        val out = ArrayList<Richiesta>()
        for (p in rosa.shuffled(rng)) {
            if (p.id in gia || esistenti.size + out.size >= MAX_RICHIESTE) continue
            val giusto = ingaggioGiusto(p)
            when {
                tipo(p) == Tipo.MERCENARIO && p.overall >= 62 && p.salary * 10 < giusto * 7 && rng.nextInt(100) < 12 ->
                    out += Richiesta(p.id, Richiesta.AUMENTO, giusto)
                tipo(p) == Tipo.AMBIZIOSO && !p.starter && p.morale < 45 && rng.nextInt(100) < 12 ->
                    out += Richiesta(p.id, Richiesta.CESSIONE)
            }
        }
        return out
    }

    fun encode(l: List<Richiesta>): String = l.joinToString(";") { "${it.playerId}:${it.tipo}:${it.importo}" }
    fun decode(s: String?): List<Richiesta> = s.orEmpty().split(";").mapNotNull {
        val p = it.split(":")
        if (p.size == 3) Richiesta(p[0].toLongOrNull() ?: return@mapNotNull null, p[1], p[2].toLongOrNull() ?: 0L) else null
    }
}
