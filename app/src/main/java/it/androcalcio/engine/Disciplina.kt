package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/**
 * Cartellini, diffide e squalifiche della tua squadra. Gli stati stanno nella tabella Stato come testo "id:n;id:n".
 * Quattro gialli = diffidato; al quinto scatta una giornata di squalifica. Il rosso costa da 1 a 3 giornate.
 */
object Disciplina {
    const val DIFFIDA = 4
    const val GIALLI_SQUALIFICA = 5

    /** severita: 1 = tollerante, 2 = nella norma, 3 = severo. */
    data class Arbitro(val nome: String, val severita: Int, val capacita: Int = 2) {
        val descrizione get() = when (severita) { 1 -> "tollerante"; 3 -> "severo"; else -> "nella norma" }

        /** Tipo di arbitro: dalla combinazione di severità e capacità (1 = scarso, 3 = ottimo). */
        val profilo get() = when {
            capacita == 1 && severita == 3 -> "Severo e impreciso"
            capacita == 1 && severita == 1 -> "Distratto e permissivo"
            capacita == 1 -> "Distratto"
            capacita == 3 && severita == 3 -> "Ottimo ma severo"
            capacita == 3 && severita == 1 -> "Ottimo, lascia giocare"
            capacita == 3 -> "Ottimo"
            severita == 3 -> "Severo"
            severita == 1 -> "Permissivo"
            else -> "Equilibrato"
        }

        /** Frase per l'assistente: cosa aspettarsi da questo arbitro. */
        val consiglio get() = when {
            capacita == 1 -> "Arbitro $nome ($profilo): può sbagliare, e il VAR potrebbe non bastare."
            severita == 3 -> "Arbitro $nome ($profilo): attenzione ai falli, fischia e ammonisce molto."
            severita == 1 -> "Arbitro $nome ($profilo): lascia correre, si può giocare duro."
            else -> "Arbitro $nome ($profilo): partita sotto controllo."
        }
    }

    private val NOMI = listOf(
        "Bertoldi", "Zanchetta", "Ferraresi", "Pellizzari", "Campodonico", "Rinaldini",
        "Sorrentini", "Basalti", "Moncalvo", "Grassetti", "Tortorici", "Vanzetti"
    )

    /** L'arbitro di una giornata: fisso per stagione e giornata (stesso risultato a ogni chiamata). */
    fun arbitro(round: Int, seed: Long): Arbitro {
        val r = Random(seed * 131 + round)
        val nome = NOMI[r.nextInt(NOMI.size)]
        val severita = r.nextInt(1, 4)
        return Arbitro(nome, severita, r.nextInt(1, 4))
    }

    data class Esito(val gialli: List<Long>, val rossi: List<Long>)

    fun probGiallo(p: Player, severita: Int): Double = (0.03 + p.ag / 1500.0) * (0.7 + 0.3 * severita)
    fun probRosso(p: Player, severita: Int): Double = (0.002 + p.ag / 30000.0) * (0.7 + 0.3 * severita)

    /** Cartellini di una partita per gli 11 che sono scesi in campo. */
    fun sorteggia(inCampo: List<Player>, severita: Int, rng: Random): Esito {
        val rossi = inCampo.filter { rng.nextDouble() < probRosso(it, severita) }.map { it.id }
        val gialli = inCampo.filter { it.id !in rossi && rng.nextDouble() < probGiallo(it, severita) }.map { it.id }
        return Esito(gialli, rossi)
    }

    data class Registro(val gialli: Map<Long, Int> = emptyMap(), val squal: Map<Long, Int> = emptyMap())

    /** Dopo la partita: chi ha saltato la gara sconta una giornata, poi si sommano i nuovi cartellini. */
    fun applica(r: Registro, hannoSaltato: Set<Long>, e: Esito, rng: Random): Registro {
        val sq = r.squal.mapNotNull { (id, n) ->
            val m = if (id in hannoSaltato) n - 1 else n
            if (m > 0) id to m else null
        }.toMap().toMutableMap()
        val g = r.gialli.toMutableMap()
        e.gialli.forEach { id ->
            val n = (g[id] ?: 0) + 1
            if (n >= GIALLI_SQUALIFICA) { g[id] = 0; sq[id] = (sq[id] ?: 0) + 1 } else g[id] = n
        }
        e.rossi.forEach { id -> sq[id] = (sq[id] ?: 0) + rng.nextInt(1, 4) }
        return Registro(g.filterValues { it > 0 }, sq)
    }

    fun diffidati(r: Registro): List<Long> = r.gialli.filter { it.value >= DIFFIDA }.keys.toList()

    fun encode(m: Map<Long, Int>): String = m.entries.joinToString(";") { "${it.key}:${it.value}" }
    fun decode(s: String?): Map<Long, Int> = s.orEmpty().split(";").mapNotNull {
        val p = it.split(":")
        if (p.size == 2) (p[0].toLongOrNull() ?: return@mapNotNull null) to (p[1].toIntOrNull() ?: return@mapNotNull null) else null
    }.toMap()
}
