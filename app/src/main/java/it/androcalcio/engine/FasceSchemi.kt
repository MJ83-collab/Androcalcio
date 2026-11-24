package it.androcalcio.engine

import it.androcalcio.data.Player

/**
 * Dove costruisce il gioco la squadra: sulle fasce (terzini ed estremi) o per vie centrali (centrocampo e centravanti).
 * Rende in base alla qualità dei giocatori di quella zona: con ottimi esterni conviene la fascia, con un bel centrocampo il centro.
 * MISTO non cambia nulla.
 */
enum class Fasce(val label: String, val descrizione: String) {
    CENTRALE("Centrale", "Palla al centro: pesano passaggio e qualità di mediani, registi, mezzepunte e centravanti."),
    MISTO("Misto", "Nessun effetto."),
    LATERALE("Sulle fasce", "Palla agli esterni: pesano velocità e qualità di terzini ed estremi.");

    /** Moltiplicatore dell'attacco, da -8% a +8% in base ai giocatori della zona scelta. */
    fun fattore(xi: List<Player>): Double {
        if (this == MISTO) return 1.0
        val laterale = this == LATERALE
        val zona = xi.filter {
            val r = TacticalRoles.assigned(it)
            if (laterale) r == "TD" || r == "TS" || r == "ES" || r == "FD" || r == "FS" || r == "AD" || r == "AS" else r == "MED" || r == "REG" || r == "INT" || r == "MP" || r == "CA"
        }
        val q = if (zona.isEmpty()) 55.0 else zona.map { if (laterale) (it.ve + it.qu) / 2.0 else (it.pa + it.qu) / 2.0 }.average()
        return 1.0 + 0.08 * ((q - 55.0) / 45.0).coerceIn(-1.0, 1.0)
    }
}

/** Schema sui calci piazzati: cambia la probabilità di gol di angoli e punizioni. NORMALE non cambia nulla. */
enum class SchemaPiazzati(val label: String, val descrizione: String, val angoli: Double, val punizioni: Double) {
    NORMALE("Normale", "Nessun effetto.", 1.0, 1.0),
    CROSS("Palloni in area", "Angoli +25%, punizioni -10%.", 1.25, 0.9),
    DIRETTO("Tiro diretto", "Punizioni +30%, angoli -10%.", 0.9, 1.3),
    CORTO("Schema corto", "Angoli e punizioni +10%.", 1.1, 1.1)
}
