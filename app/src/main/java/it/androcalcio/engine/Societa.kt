package it.androcalcio.engine

/**
 * Qualificazioni europee della Serie A (regole 2026-27 secondo Sky Sport):
 * 1°-4° in Champions League, 5° e vincitrice della Coppa Italia in Europa League, 6° in Conference League.
 * Semplificazione: se la vincitrice di coppa è già nei primi cinque, il posto extra non passa al sesto.
 */
object Europa {
    const val CHAMPIONS = "Champions League"
    const val EUROPA = "Europa League"
    const val CONFERENCE = "Conference League"

    /** classifica: id delle squadre in ordine di posizione; restituisce l'esito europeo per id. */
    fun assegna(classifica: List<Long>, vincitriceCoppa: Long?): Map<Long, String> {
        val out = HashMap<Long, String>()
        classifica.take(4).forEach { out[it] = CHAMPIONS }
        classifica.getOrNull(4)?.let { out[it] = EUROPA }
        if (vincitriceCoppa != null && vincitriceCoppa !in out) out[vincitriceCoppa] = EUROPA
        classifica.firstOrNull { it !in out }?.let { out[it] = CONFERENCE }
        return out
    }
}

/** Una riga della classifica di fine stagione. */
data class RigaFine(val posizione: Int, val squadra: String, val punti: Int, val esito: String, val mia: Boolean)

/** Una divisione (es. Serie A, Serie C Girone A) con la sua classifica finale. */
data class DivisioneFine(val nome: String, val righe: List<RigaFine>)

/** Fine stagione della nazione della squadra allenata. */
data class FineStagione(val stagione: String, val divisioni: List<DivisioneFine>)

object Presidente {
    /** Messaggio di benvenuto del presidente all'inizio della carriera. */
    fun benvenuto(squadra: String, nome: String, cognome: String): String =
        "Buongiorno mister $cognome, benvenuto alla $squadra. " +
            "Il club conta su di lei. Prima dell'inizio del campionato fissi le amichevoli di precampionato " +
            "dal Calendario: sono facoltative, ma aiutano gli incassi e la forma dei giocatori. " +
            "Buon lavoro, $nome."
}
