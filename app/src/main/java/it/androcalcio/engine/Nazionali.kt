package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import kotlin.random.Random

/** Nazionali: convocazioni alle date internazionali (giornate di campionato) e tornei a fine stagione. */
object Nazionali {
    /**
     * Giornate di campionato usate come date internazionali (due per finestra FIFA 2026-27):
     * 21 set-6 ott 2026 → 5, 6 · 9-17 nov 2026 → 11, 12 · 22-30 mar 2027 → 29, 30 · 7-15 giu 2027 → 35, 36.
     * I convocati non giocano il club in quella giornata.
     */
    val DATE = setOf(5, 6, 11, 12, 29, 30, 35, 36)
    /** Date dell'Under 21 (giornate diverse dalle senior): amichevoli di preparazione e qualificazioni. */
    val DATE_U21 = setOf(8, 14, 32)
    const val ETA_U21 = 21
    const val SUFFISSO_U21 = " U21"
    const val CONVOCATI = 23
    const val MIN_ROSA = 11
    val EUROPA = setOf("Italia", "Spagna", "Inghilterra", "Germania", "Francia", "Portogallo", "Olanda",
        "Belgio", "Croazia", "Polonia", "Serbia")

    /** Europeo Under 21 ogni due anni (stagioni che iniziano in anni dispari). */
    fun europeoU21(annoInizio: Int) = annoInizio % 2 == 1

    fun nomeU21(nazione: String) = nazione + SUFFISSO_U21
    fun isU21(nome: String) = nome.endsWith(SUFFISSO_U21)
    fun senzaU21(nome: String) = nome.removeSuffix(SUFFISSO_U21)

    /** Giocatori di una nazione utili all'Under 21: massimo 21 anni. */
    fun eleggibiliU21(rosa: List<Player>): List<Player> = rosa.filter { it.age <= ETA_U21 }

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
