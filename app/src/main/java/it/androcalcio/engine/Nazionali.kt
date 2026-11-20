package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import kotlin.random.Random

/** Nazionali: convocazioni alle date internazionali (giornate di campionato) e tornei a fine stagione. */
object Nazionali {
    /** Giornate di campionato usate come date internazionali: i convocati non giocano il club quel giorno. */
    val DATE = setOf(6, 12, 18, 24, 30, 36)
    const val CONVOCATI = 23
    const val MIN_ROSA = 11
    val EUROPA = setOf("Italia", "Spagna", "Inghilterra", "Germania", "Francia", "Portogallo", "Olanda",
        "Belgio", "Croazia", "Polonia", "Serbia")

    /** Mondiali ogni quattro anni (stagioni che iniziano nel 2026, 2030…). */
    fun mondiale(annoInizio: Int) = annoInizio % 4 == 2

    /** Europei ogni quattro anni, sfalsati di due (stagioni che iniziano nel 2028, 2032…). */
    fun europei(annoInizio: Int) = annoInizio % 4 == 0

    /** I convocati: i migliori disponibili, senza infortunati. */
    fun convoca(rosa: List<Player>): List<Player> =
        Medical.available(rosa).sortedByDescending { it.overall }.take(CONVOCATI)

    /** Forza di una nazionale: media dei convocati. */
    fun forza(rosa: List<Player>): Double = convoca(rosa).map { it.overall }.ifEmpty { listOf(0) }.average()

    /** Squadra fittizia per la partita: parametri di un club, con id e nome della nazionale. */
    fun squadra(base: Team, nazione: String, indice: Int): Team = base.copy(
        id = -(indice + 1L), name = nazione, shortName = nazione.take(3).uppercase(),
        nazione = nazione, campionato = "Nazionale"
    )

    /** Partita tra due nazionali: l'IA sceglie l'undici tra i convocati. */
    fun gioca(casa: Team, convCasa: List<Player>, trasf: Team, convTrasf: List<Player>, rng: Random): MatchReport =
        MatchEngine.simulate(casa, convCasa, trasf, convTrasf, rng = rng)
}
