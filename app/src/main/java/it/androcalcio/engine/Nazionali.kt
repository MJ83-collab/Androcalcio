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
        "Belgio", "Croazia", "Polonia", "Serbia", "Scozia", "Turchia", "Grecia")

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
    fun squadra(base: Team, nazione: String, indice: Int): Team {
        // Indice pari = squadra di casa (maglia da casa), dispari = ospite (maglia da trasferta)
        val (c1, c2) = colori(senzaU21(nazione), trasferta = indice % 2 == 1)
        return base.copy(
            id = -(indice + 1L), name = nazione, shortName = nazione.take(3).uppercase(),
            nazione = nazione, campionato = "Nazionale", colorPrimary = c1, colorSecondary = c2
        )
    }

    /** Maglie tipiche: (colore principale, secondo colore) da casa e da trasferta. */
    private val KIT: Map<String, Pair<Pair<Long, Long>, Pair<Long, Long>>> = mapOf(
        "Italia" to ((0xFF1F5FBFL to 0xFFFFFFFFL) to (0xFFF5F5F5L to 0xFF1F5FBFL)),
        "Spagna" to ((0xFFC8102EL to 0xFFFFC400L) to (0xFFF5F5F5L to 0xFFC8102EL)),
        "Inghilterra" to ((0xFFF5F5F5L to 0xFF14215CL) to (0xFFC8102EL to 0xFFF5F5F5L)),
        "Germania" to ((0xFFF5F5F5L to 0xFF1A1A1AL) to (0xFF1A1A1AL to 0xFFDD0000L)),
        "Francia" to ((0xFF1B3C8CL to 0xFFF5F5F5L) to (0xFFF5F5F5L to 0xFF1B3C8CL)),
        "Brasile" to ((0xFFFFDC02L to 0xFF009C3BL) to (0xFF1D3F94L to 0xFFF5F5F5L)),
        "Argentina" to ((0xFF75AADBL to 0xFFF5F5F5L) to (0xFF1B2A6BL to 0xFF75AADBL)),
        "Portogallo" to ((0xFFC8102EL to 0xFF006600L) to (0xFFF5F5F5L to 0xFFC8102EL)),
        "Olanda" to ((0xFFFF6600L to 0xFFF5F5F5L) to (0xFF1B3C8CL to 0xFFFF6600L)),
        "Belgio" to ((0xFFC8102EL to 0xFF1A1A1AL) to (0xFFF5F5F5L to 0xFFC8102EL)),
        "Croazia" to ((0xFFC8102EL to 0xFFF5F5F5L) to (0xFF1B2A6BL to 0xFFF5F5F5L)),
        "Polonia" to ((0xFFF5F5F5L to 0xFFC8102EL) to (0xFFC8102EL to 0xFFF5F5F5L)),
        "Serbia" to ((0xFFC6363CL to 0xFF0C4076L) to (0xFFF5F5F5L to 0xFF0C4076L)),
        "Stati Uniti" to ((0xFFF5F5F5L to 0xFF14215CL) to (0xFF14215CL to 0xFFC8102EL)),
        "Giappone" to ((0xFF1F3C88L to 0xFFF5F5F5L) to (0xFFF5F5F5L to 0xFF1F3C88L)),
        "Marocco" to ((0xFFC1272DL to 0xFF006233L) to (0xFFF5F5F5L to 0xFF006233L)),
        "Scozia" to ((0xFF0B2A5BL to 0xFFF5F5F5L) to (0xFFF5F5F5L to 0xFF0B2A5BL)),
        "Turchia" to ((0xFFE30A17L to 0xFFF5F5F5L) to (0xFFF5F5F5L to 0xFFE30A17L)),
        "Grecia" to ((0xFF0D5EAFL to 0xFFF5F5F5L) to (0xFFF5F5F5L to 0xFF0D5EAFL)),
        "Nigeria" to ((0xFF008751L to 0xFFF5F5F5L) to (0xFFF5F5F5L to 0xFF008751L)),
        "Senegal" to ((0xFFF5F5F5L to 0xFF00853FL) to (0xFF00853FL to 0xFFFDEF42L)),
        "Egitto" to ((0xFFCE1126L to 0xFFF5F5F5L) to (0xFFF5F5F5L to 0xFFCE1126L)),
        "Messico" to ((0xFF006847L to 0xFFF5F5F5L) to (0xFFF5F5F5L to 0xFF006847L)),
        "Canada" to ((0xFFD52B1EL to 0xFFF5F5F5L) to (0xFFF5F5F5L to 0xFFD52B1EL)),
        "Corea del Sud" to ((0xFFCD2E3AL to 0xFF0047A0L) to (0xFFF5F5F5L to 0xFFCD2E3AL)),
        "Australia" to ((0xFFFFCD00L to 0xFF00843DL) to (0xFF00843DL to 0xFFFFCD00L))
    )

    /** Colori di maglia di una nazionale; per nazioni senza maglia tipica: bianco in casa, nero in trasferta. */
    fun colori(nazione: String, trasferta: Boolean): Pair<Long, Long> {
        val k = KIT[nazione] ?: return if (trasferta) 0xFF1A1A1AL to 0xFFF5F5F5L else 0xFFF5F5F5L to 0xFF1A1A1AL
        return if (trasferta) k.second else k.first
    }

    private fun simili(a: Long, b: Long): Boolean {
        fun ch(c: Long, s: Int) = ((c shr s) and 0xFF).toInt()
        val d = Math.abs(ch(a, 16) - ch(b, 16)) + Math.abs(ch(a, 8) - ch(b, 8)) + Math.abs(ch(a, 0) - ch(b, 0))
        return d < 150
    }

    /** Partita tra due nazionali: l'IA sceglie l'undici tra i convocati. */
    fun gioca(casa: Team, convCasa: List<Player>, trasf: Team, convTrasf: List<Player>, rng: Random): MatchReport {
        // Se le due maglie si somigliano, l'ospite scambia i due colori della sua maglia da trasferta
        val ospite = if (simili(casa.colorPrimary, trasf.colorPrimary))
            trasf.copy(colorPrimary = trasf.colorSecondary, colorSecondary = trasf.colorPrimary) else trasf
        return MatchEngine.simulate(casa, convCasa, ospite, convTrasf, rng = rng)
    }
}
