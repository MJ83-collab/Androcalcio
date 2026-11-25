package it.androcalcio.engine

import it.androcalcio.data.MatchResult
import java.time.LocalDate

/**
 * Calendario a turni (v0.7): il campionato si gioca la domenica, le coppe a metà settimana.
 *
 * Ogni partita ha una chiave di ordine: 2 x giornata per il campionato, 2 x giornata + 1 per un turno di coppa
 * giocato il mercoledì dopo quella giornata. Una coppa ha un posto fisso nel calendario (tabella sotto), quindi
 * nessuna squadra gioca due partite nello stesso turno:
 * - coppa nazionale: sempre dopo giornate PARI;
 * - coppe europee: sempre dopo giornate DISPARI;
 * - nessun turno cade dopo le giornate delle nazionali (vedi [Nazionali.DATE]).
 * Una squadra gioca al massimo una coppa europea e la coppa nazionale, quindi non può avere due impegni nello stesso turno.
 * La Supercoppa si gioca a fine stagione, dopo tutto il resto.
 */
object Turni {
    /** Coppa nazionale: giornata dopo cui si gioca lo stadio [k] (andata), il ritorno due giornate dopo. Tutte pari. */
    private val NAZIONALE = intArrayOf(2, 8, 14, 18, 22, 26, 32)

    /** Coppe europee: sei giornate dei gironi, poi ottavi, quarti, semifinali (andata; ritorno +2) e finale. Tutte dispari. */
    private val GIRONI = intArrayOf(3, 7, 9, 13, 15, 17)
    private const val EU_OTTAVI = 21
    private const val EU_QUARTI = 25
    private const val EU_SEMI = 31
    private const val EU_FINALE = 37

    /** Oltre questa giornata la data non cresce più: la Supercoppa cade a fine stagione. */
    private const val GIORNATA_MAX_DATA = 43

    /** Dopo quale giornata di campionato si gioca (a metà settimana) questa partita; null se è di campionato. */
    fun slot(round: Int, league: String): Int? {
        if (!Cup.isCup(round)) return null
        val stage = Cup.stageOf(round)
        val gamba = (round - Cup.BASE) % 10
        return if (CoppeEuropee.isEuropea(league) || stage >= CoppeEuropee.STAGE_GIRONI) europeo(stage, gamba)
        else nazionale(stage, gamba)
    }

    fun slot(m: MatchResult): Int? = slot(m.round, m.league)

    private fun nazionale(stage: Int, gamba: Int): Int = when {
        stage >= Cup.SUPER_STAGE -> stage                         // Supercoppa: 100, finale 101
        stage < NAZIONALE.size -> NAZIONALE[stage] + 2 * gamba
        else -> NAZIONALE.last() + 6 * (stage - NAZIONALE.size + 1) + 2 * gamba
    }

    private fun europeo(stage: Int, gamba: Int): Int = when {
        stage in CoppeEuropee.STAGE_GIRONI..CoppeEuropee.STAGE_GIRONI + 5 -> GIRONI[stage - CoppeEuropee.STAGE_GIRONI]
        stage == CoppeEuropee.STAGE_OTTAVI -> EU_OTTAVI + 2 * gamba
        stage == CoppeEuropee.STAGE_QUARTI -> EU_QUARTI + 2 * gamba
        stage == CoppeEuropee.STAGE_SEMI -> EU_SEMI + 2 * gamba
        else -> EU_FINALE
    }

    /** Chiave di ordine: campionato 2 x giornata, coppa 2 x giornata di riferimento + 1. */
    fun chiave(round: Int, league: String): Int = slot(round, league)?.let { it * 2 + 1 } ?: (round * 2)

    fun chiave(m: MatchResult): Int = chiave(m.round, m.league)

    /** Chiave del turno che segue la giornata [giornata]: tutto fino a quella giornata e al suo mercoledì. */
    fun finoAGiornata(giornata: Int): Int = giornata * 2 + 1

    fun eCampionato(chiave: Int) = chiave % 2 == 0

    /** Giornata di campionato di una chiave (per un turno di coppa, quella che lo precede). */
    fun giornata(chiave: Int): Int = chiave / 2

    /** Data del turno: domenica per il campionato, mercoledì (domenica + 3 giorni) per le coppe. */
    fun data(anno: Int, chiave: Int): LocalDate {
        val d = DateGiornate.data(anno, giornata(chiave).coerceAtMost(GIORNATA_MAX_DATA))
        return if (eCampionato(chiave)) d else d.plusDays(3)
    }

    /** Titolo e numero per il banner della partita: ("Giornata", "7"), ("Coppa", "Turno 3"), ("Europa", "Girone 2"). */
    fun banner(m: MatchResult): Pair<String, String> {
        if (!Cup.isCup(m.round)) return "Giornata" to "${m.round}"
        val stage = Cup.stageOf(m.round)
        return when {
            stage >= CoppeEuropee.STAGE_GIRONI -> "Europa" to when (stage) {
                in CoppeEuropee.STAGE_GIRONI..CoppeEuropee.STAGE_GIRONI + 5 -> "Girone ${stage - CoppeEuropee.STAGE_GIRONI + 1}"
                CoppeEuropee.STAGE_OTTAVI -> "Ottavi"
                CoppeEuropee.STAGE_QUARTI -> "Quarti"
                CoppeEuropee.STAGE_SEMI -> "Semifinale"
                else -> "Finale"
            }
            stage >= Cup.SUPER_STAGE -> "Supercoppa" to ""
            else -> "Coppa" to "Turno ${stage + 1}"
        }
    }

    /** Es. "mer 14 ott". */
    fun testo(anno: Int, chiave: Int): String {
        val d = data(anno, chiave)
        val giorno = d.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ITALIAN).take(3)
        val mese = d.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ITALIAN).take(3)
        return "$giorno ${d.dayOfMonth} $mese"
    }

    /** Nome del turno per le schermate: "giornata 7" oppure la coppa e il suo turno. */
    fun etichetta(m: MatchResult): String {
        if (!Cup.isCup(m.round)) return "giornata ${m.round}"
        val stage = Cup.stageOf(m.round)
        return if (CoppeEuropee.isEuropea(m.league) || stage >= CoppeEuropee.STAGE_GIRONI) {
            val nome = CoppeEuropee.Coppa.values().firstOrNull { CoppeEuropee.key(it) == m.league }?.nome ?: "Coppa europea"
            "$nome, ${CoppeEuropee.nomeTurno(stage).lowercase()}"
        } else when {
            stage >= Cup.SUPER_STAGE -> "Supercoppa"
            else -> "coppa nazionale, turno ${stage + 1}"
        }
    }
}
