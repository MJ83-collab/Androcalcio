package it.androcalcio.engine

import it.androcalcio.data.Player

/** Report dell'analista video sull'avversario: più è bravo, più dettagli (e più affidabili) arrivano. */
object ReportAvversario {
    private fun ruolo(r: String) = when (r) { "P" -> "portiere"; "D" -> "difesa"; "C" -> "centrocampo"; else -> "attacco" }

    fun righe(xi: List<Player>, skill: Int): List<String> {
        if (xi.isEmpty()) return emptyList()
        val out = ArrayList<String>()
        xi.filter { it.role != "P" }.maxByOrNull { it.overall }?.let {
            out += "Uomo più pericoloso: ${it.name} (${ruolo(it.role)}, livello ${it.overall})"
        }
        val reparti = listOf("D", "C", "A").mapNotNull { r -> xi.filter { it.role == r }.takeIf { it.isNotEmpty() }?.let { r to it.map { p -> p.overall }.average() } }
        val debole = reparti.minByOrNull { it.second }
        val forte = reparti.maxByOrNull { it.second }
        if (skill >= 35 && debole != null) out += "Reparto più fragile: ${ruolo(debole.first)} (media ${debole.second.toInt()})"
        if (skill >= 50 && forte != null) out += "Reparto più forte: ${ruolo(forte.first)} (media ${forte.second.toInt()})"
        if (skill >= 55) xi.filter { it.role != "P" }.maxByOrNull { it.ti }?.let { out += "Calci piazzati: li batte probabilmente ${it.name}" }
        if (skill >= 65) {
            val stanchi = xi.count { it.condition < 75 }
            out += if (stanchi >= 3) "Condizione: $stanchi titolari non al meglio, calo atteso nella ripresa" else "Condizione: squadra in buona forma"
        }
        if (skill >= 75 && debole != null) out += "Consiglio: " + when (debole.first) {
            "D" -> "attacca con ritmo alto e cerca la profondità, la difesa soffre"
            "C" -> "pressing alto per rubare palla a centrocampo"
            else -> "linea alta e trappola del fuorigioco, l'attacco è poco incisivo"
        }
        return out
    }
}
