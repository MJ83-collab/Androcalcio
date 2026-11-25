package it.androcalcio.engine

/**
 * Posti europei per la prima divisione di ogni nazione (2026-27): in Champions i primi `champions` in classifica,
 * in Europa League `europa` posti (il secondo è della vincitrice di coppa nazionale, se non è già qualificata),
 * in Conference League `conference` posti. Il posto che non va a una squadra scende alla successiva in classifica.
 */
object Europa {
    const val CHAMPIONS = "Champions League"
    const val EUROPA = "Europa League"
    const val CONFERENCE = "Conference League"

    data class Posti(val champions: Int, val europa: Int = 2, val conference: Int = 1)

    /** Posti per nazione. Italia, Germania e Francia 4 in Champions; Spagna e Inghilterra 5 (posto extra per ranking). */
    val POSTI = mapOf(
        "Italia" to Posti(4), "Germania" to Posti(4), "Francia" to Posti(4),
        "Spagna" to Posti(5), "Inghilterra" to Posti(5)
    )

    /** classifica: id delle squadre in ordine di posizione; restituisce l'esito europeo per id. */
    fun assegna(classifica: List<Long>, vincitriceCoppa: Long?, posti: Posti): Map<Long, String> {
        val out = HashMap<Long, String>()
        classifica.take(posti.champions).forEach { out[it] = CHAMPIONS }
        // La vincitrice di coppa occupa un posto di Europa League solo se non è già qualificata
        if (vincitriceCoppa != null && vincitriceCoppa !in out) out[vincitriceCoppa] = EUROPA
        for (id in classifica) {
            if (out.values.count { it == EUROPA } >= posti.europa) break
            if (id !in out) out[id] = EUROPA
        }
        var conf = 0
        for (id in classifica) {
            if (conf >= posti.conference) break
            if (id !in out) { out[id] = CONFERENCE; conf++ }
        }
        return out
    }
}

/** Copa Sudamericana: per Brasile e Argentina, 6 posti (qui i posti dal 7° al 12° della prima divisione). */
object Sudamericana {
    const val NOME = "Copa Sudamericana"

    fun assegna(classifica: List<Long>): Map<Long, String> =
        classifica.drop(6).take(6).associateWith { NOME }
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
            "Il club conta su di lei.\n\n" +
            "Prima della 1ª giornata può giocare le amichevoli di precampionato: sono facoltative, " +
            "ma portano incassi e mettono in forma i giocatori.\n\n" +
            "• Fissa le amichevoli: apre il Calendario per sceglierle.\n" +
            "• Salta le amichevoli: si parte subito con la 1ª giornata di campionato.\n\n" +
            "Buon lavoro, $nome."
}
