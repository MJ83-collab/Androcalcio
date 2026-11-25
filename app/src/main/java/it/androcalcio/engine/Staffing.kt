package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.SeedData
import it.androcalcio.data.Staff
import kotlin.random.Random

/**
 * Staff della prima squadra: allenatore, allenatore in seconda, allenatore delle giovanili,
 * sei preparatori atletici (morale e parametri, forma, portieri, attaccanti, centrocampisti, difensori)
 * e due fisioterapisti (recupero e prevenzione). Abilità 1-99, mostrata come 0-10 con stelle.
 */
object Staffing {
    const val ALLENATORE = "ALL"
    const val ASSISTENTE = "ASS"
    const val GIOVANILI = "GIO"
    const val PREPARATORE = "PRE"
    const val FISIOTERAPISTA = "FIS"
    const val TUTTOFARE = "GIA"      // giardiniere e tuttofare: cura il manto erboso e lo stadio
    const val ANALISTA = "ANA"       // analista video: report dettagliato sull'avversario
    const val SCOUT = "SCO"          // osservatori: più di uno, fino a MAX_OSSERVATORI, fuori dai POSTI
    const val MAX_OSSERVATORI = 5
    const val NEUTRAL = 50          // squadre AI e posti vuoti: effetto nullo
    const val VUOTO = 40            // abilità del club quando il ruolo resta scoperto

    // Specialità dei preparatori atletici
    const val GENERALE = "GEN"
    const val FORMA = "FOR"
    const val PORTIERI = "POR"
    const val ATTACCANTI = "ATT"
    const val CENTROCAMPISTI = "CEN"
    const val DIFENSORI = "DIF"
    // Specialità dei fisioterapisti
    const val RECUPERO = "REC"
    const val PREVENZIONE = "PRV"

    val ROLES = listOf(ALLENATORE, ASSISTENTE, GIOVANILI, PREPARATORE, FISIOTERAPISTA, SCOUT, TUTTOFARE, ANALISTA)
    private val SPEC_PREPARATORI = listOf(GENERALE, FORMA, PORTIERI, ATTACCANTI, CENTROCAMPISTI, DIFENSORI)
    private val SPEC_FISIO = listOf(RECUPERO, PREVENZIONE)

    /** Gli undici posti dello staff: ruolo più specialità dove serve. */
    data class Posto(val role: String, val specialita: String = "")

    val POSTI: List<Posto> = listOf(Posto(ALLENATORE), Posto(ASSISTENTE), Posto(GIOVANILI)) +
        SPEC_PREPARATORI.map { Posto(PREPARATORE, it) } + SPEC_FISIO.map { Posto(FISIOTERAPISTA, it) } + Posto(TUTTOFARE) + Posto(ANALISTA)

    fun roleName(role: String, specialita: String = ""): String = when (role) {
        ALLENATORE -> "Allenatore"
        ASSISTENTE -> "Allenatore in seconda"
        GIOVANILI -> "Allenatore delle giovanili"
        PREPARATORE -> "Preparatore atletico · ${specialitaNome(specialita)}"
        FISIOTERAPISTA -> "Fisioterapista · ${specialitaNome(specialita)}"
        SCOUT -> "Osservatore"
        TUTTOFARE -> "Giardiniere e tuttofare"
        ANALISTA -> "Analista video"
        else -> ""
    }

    fun specialitaNome(s: String): String = when (s) {
        GENERALE -> "morale e parametri"
        FORMA -> "forma"
        PORTIERI -> "portieri"
        ATTACCANTI -> "attaccanti"
        CENTROCAMPISTI -> "centrocampisti"
        DIFENSORI -> "difensori"
        RECUPERO -> "recupero"
        PREVENZIONE -> "prevenzione"
        else -> ""
    }

    /** Stessa coppia ruolo + specialità: stesso posto. */
    fun stessoPosto(s: Staff, p: Posto): Boolean = s.role == p.role && s.specialita == p.specialita

    /** Abilità di un ruolo (allenatore, vice, giovanili); 50 se il posto è vuoto. */
    fun skill(by: Map<Long, List<Staff>>, teamId: Long, role: String): Int =
        by[teamId]?.firstOrNull { it.role == role }?.skill ?: NEUTRAL

    /** Tattica predefinita dell'allenatore della squadra; null se la squadra non ha allenatore. */
    fun stile(by: Map<Long, List<Staff>>, teamId: Long): Stile? =
        by[teamId]?.firstOrNull { it.role == ALLENATORE }?.stile
            ?.let { s -> Stile.values().firstOrNull { it.name == s } }

    /** Effetti dello staff medico e atletico, letti una volta per partita. */
    data class Cura(
        val generale: Int, val forma: Int, val recupero: Int, val prevenzione: Int,
        val ruoli: Map<String, Int>,   // preparatore del reparto per P, D, C, A
        val campo: Int = NEUTRAL       // giardiniere: stato del manto erboso
    ) {
        /** Abilità del preparatore del ruolo dato; neutra se manca. */
        fun perRuolo(role: String): Int = ruoli[role] ?: NEUTRAL

        companion object {
            val NEUTRO = Cura(NEUTRAL, NEUTRAL, NEUTRAL, NEUTRAL, emptyMap())
        }
    }

    /** Lo staff medico e atletico di una squadra. */
    fun cura(by: Map<Long, List<Staff>>, teamId: Long): Cura {
        val lista = by[teamId].orEmpty()
        fun prep(s: String) = lista.firstOrNull { it.role == PREPARATORE && it.specialita == s }?.skill ?: NEUTRAL
        fun fis(s: String) = lista.firstOrNull { it.role == FISIOTERAPISTA && it.specialita == s }?.skill ?: NEUTRAL
        return Cura(
            generale = prep(GENERALE), forma = prep(FORMA),
            recupero = fis(RECUPERO),
            // un buon giardiniere tiene il prato in ordine: meno infortuni muscolari (pesa il 20% della prevenzione)
            prevenzione = (fis(PREVENZIONE) * 0.8 + (lista.firstOrNull { it.role == TUTTOFARE }?.skill ?: NEUTRAL) * 0.2).toInt(),
            ruoli = mapOf("P" to prep(PORTIERI), "D" to prep(DIFENSORI), "C" to prep(CENTROCAMPISTI), "A" to prep(ATTACCANTI)),
            campo = lista.firstOrNull { it.role == TUTTOFARE }?.skill ?: NEUTRAL
        )
    }

    /** Moltiplicatore sul motore partita: da circa -10% (skill 1) a +10% (skill 99). */
    fun coachFactor(skill: Int): Double = 1.0 + (skill - NEUTRAL) / 500.0

    /** Probabilità per partita che una squadra abbia un infortunato: circa 2.5% (skill 99) - 7.5% (skill 1). */
    fun injuryChance(prevenzione: Int): Double = 0.05 * (1.0 - (prevenzione - NEUTRAL) / 100.0)

    /** Ingaggio annuo in base all'abilità. */
    fun wage(skill: Int, scala: Double = 1.0): Long = (((skill - 30).coerceAtLeast(5) * 12_000L * scala).toLong() / 1_000L) * 1_000L

    /**
     * Scala degli stipendi in base alla categoria del club (dalla forza media della rosa): massima serie ~0,9-1,
     * Serie B ~0,6, Serie C ~0,4. Un professionista bravo costa in proporzione alla serie, non a un valore unico.
     */
    fun scalaCategoria(mediaRosa: Double): Double = ((mediaRosa - 40.0) / 40.0).coerceIn(0.3, 1.0)

    /** Costo di un corso di formazione: cresce con l'abilità, in proporzione alla categoria. */
    fun costoCorso(skill: Int, scala: Double): Long = (((15_000 + skill * 500L) * (0.4 + 0.6 * scala)).toLong() / 1_000L) * 1_000L

    /** Quanto sale l'abilità con un corso: di più per chi parte da meno. */
    fun guadagnoCorso(skill: Int): Int = if (skill < 60) 3 else if (skill < 80) 2 else 1

    /** Mezze stelle da 1 a 10 (cioè da 1 a 5 stelle) per un valore 0-100. */
    fun mezzeStelle(valore: Int): Int = Math.round(valore / 10.0).toInt().coerceIn(1, 10)

    /**
     * Staff di una squadra AI per i posti mancanti.
     * Abilità legata alla forza media della rosa: massima serie ~57, seconda ~48 (±10).
     */
    fun aiStaff(teamId: Long, avgOverall: Double, missing: List<Posto>, rng: Random): List<Staff> =
        missing.map { posto ->
            val sk = (avgOverall - 25 + rng.nextInt(-10, 11)).toInt().coerceIn(30, 90)
            Staff(
                teamId = teamId, role = posto.role, specialita = posto.specialita,
                name = "${SeedData.initials[rng.nextInt(SeedData.initials.length)]}. " +
                    SeedData.surnames[rng.nextInt(SeedData.surnames.size)],
                skill = sk, salary = wage(sk, scalaCategoria(avgOverall)), contract = rng.nextInt(1, 5),
                stile = Stile.values()[rng.nextInt(Stile.values().size)].name
            )
        }

    /** Candidati da assumere: n per ogni posto, con abilità e contratto casuali. */
    fun candidates(rng: Random, n: Int = 2, scala: Double = 1.0): List<Staff> = POSTI.flatMap { posto ->
        (1..n).map {
            val sk = rng.nextInt(35, 91)
            Staff(
                teamId = 0, role = posto.role, specialita = posto.specialita,
                name = "${SeedData.initials[rng.nextInt(SeedData.initials.length)]}. " +
                    SeedData.surnames[rng.nextInt(SeedData.surnames.size)],
                skill = sk, salary = wage(sk, scala), contract = rng.nextInt(1, 5),
                stile = Stile.values()[rng.nextInt(Stile.values().size)].name
            )
        }
    }

    /** Osservatori candidati: non sono in POSTI, il club ne può assumere fino a MAX_OSSERVATORI. */
    fun candidatiOsservatori(rng: Random, n: Int = 3, scala: Double = 1.0): List<Staff> = List(n) {
        val sk = rng.nextInt(35, 91)
        Staff(
            teamId = 0, role = SCOUT, specialita = "",
            name = "${SeedData.initials[rng.nextInt(SeedData.initials.length)]}. " +
                SeedData.surnames[rng.nextInt(SeedData.surnames.size)],
            skill = sk, salary = wage(sk, scala), contract = rng.nextInt(1, 5),
            stile = Stile.values()[rng.nextInt(Stile.values().size)].name
        )
    }

    /** Stelle da 0 a 5 per un'abilità 1-99 (scala 0-10, mezze stelle). */
    fun stelle(skill: Int): Double = ((skill / 10.0) / 2.0 * 2).let { Math.round(it) / 2.0 }

    /** Bonus e malus di un membro dello staff, dedotti dalla sua abilità. */
    fun effetti(s: Staff): Pair<List<String>, List<String>> {
        val bonus = mutableListOf<String>()
        val malus = mutableListOf<String>()
        val k = s.skill
        val su = k >= 55
        val giu = k < 45
        val pct = ((coachFactor(k) - 1) * 100).let { "%.1f".format(it) }
        val gradi = (k - NEUTRAL) / 25
        when (s.role) {
            ALLENATORE -> {
                if (su) bonus += "In partita: rendimento della squadra +$pct%"
                if (giu) malus += "In partita: rendimento della squadra $pct%"
            }
            ASSISTENTE -> {
                if (su) bonus += "Consigli tattici sull'avversario e sull'undici più affidabili"
                if (giu) malus += "Consigli tattici incerti: undici e avversario meno precisi"
            }
            ANALISTA -> {
                if (su) bonus += "Report sull'avversario più ricco e preciso"
                if (giu) malus += "Report sull'avversario scarno e poco affidabile"
            }
            TUTTOFARE -> {
                if (su) bonus += "Prato curato: infortuni muscolari ridotti e campo sempre in ordine"
                if (giu) malus += "Prato rovinato: più infortuni muscolari, buche e zolle che saltano"
                bonus += "Fa anche piccoli lavori: lampadine, porte, spogliatoi (e il caffè)"
            }
            GIOVANILI -> {
                if (su) bonus += "Vivaio: giovani con potenziale più alto e giudizi più precisi"
                if (giu) malus += "Vivaio: giudizi sui giovani meno precisi"
            }
            PREPARATORE -> when (s.specialita) {
                GENERALE -> {
                    if (su) bonus += "Morale della rosa e parametri generali migliori"
                    if (giu) malus += "Morale della rosa in calo, recupero più lento"
                }
                FORMA -> {
                    if (su) bonus += "Forma dei giocatori più stabile e alta"
                    if (giu) malus += "Forma dei giocatori instabile"
                }
                else -> {
                    val ruolo = specialitaNome(s.specialita)
                    if (su) bonus += "Allenamento ${ruolo}: crescita più rapida"
                    if (giu) malus += "Allenamento ${ruolo}: crescita più lenta"
                }
            }
            FISIOTERAPISTA -> when (s.specialita) {
                RECUPERO -> {
                    if (su) bonus += "Infortuni: fino a ${gradi.coerceAtLeast(0)} turno/i di recupero in meno"
                    if (giu) malus += "Infortuni: recupero più lungo"
                }
                else -> {
                    if (su) bonus += "Prevenzione: rischio infortuni ridotto del ${(k - NEUTRAL)}%"
                    if (giu) malus += "Prevenzione: rischio infortuni aumentato del ${(NEUTRAL - k)}%"
                }
            }
        }
        return bonus to malus
    }
}

/** Infortuni e condizione fisica: dopo ogni partita una squadra può perdere un giocatore per 1-3 turni. */
object Medical {
    /** I giocatori disponibili: gli infortunati non scendono in campo. */
    fun available(ps: List<Player>): List<Player> = ps.filter { it.injuredRounds == 0 }

    /** Calo di condizione per chi ha giocato: più alto per chi ha poca resistenza (RE) o è più anziano. */
    private fun conditionDrop(p: Player): Int = (14 - p.re / 12 + (p.age - 24).coerceAtLeast(0) / 4).coerceIn(6, 20)

    /** Recupero per chi non ha giocato: un buon preparatore generale lo accelera. */
    private fun conditionGain(generale: Int): Int = (10 + (generale - Staffing.NEUTRAL) / 6).coerceIn(6, 18)

    /**
     * Dopo una partita: chi è sceso in campo (playedIds) perde condizione, gli altri recuperano;
     * gli infortunati recuperano più in fretta con un buon fisioterapista del recupero, poi può esserci un nuovo infortunio
     * (più raro con un buon fisioterapista della prevenzione).
     * Con un piano di allenamento (solo la tua squadra) si applicano crescita, fatica e rischio extra.
     */
    fun afterMatch(
        ps: List<Player>, playedIds: Set<Long>, cura: Staffing.Cura, rng: Random,
        plan: TrainingPlan? = null, coach: Int = Staffing.NEUTRAL
    ): List<Player> {
        val healed0 = ps.map {
            // la forma oscilla di partita in partita; il preparatore della forma la stabilizza
            val fo = (it.fo + rng.nextInt(-4, 5) + (cura.forma - Staffing.NEUTRAL) / 25).coerceIn(40, 99)
            val cond = if (it.id in playedIds) (it.condition - conditionDrop(it)).coerceAtLeast(35)
                else (it.condition + conditionGain(cura.generale)).coerceAtMost(100)
            val base = it.copy(fo = fo, condition = cond)
            if (it.injuredRounds > 0) base.copy(injuredRounds = it.injuredRounds - 1) else base
        }
        val healed = if (plan == null) healed0 else healed0.map {
            // il preparatore del reparto alza o abbassa l'effetto dell'allenamento per il ruolo del giocatore
            Training.apply(it, plan, coach + (cura.perRuolo(it.role) - Staffing.NEUTRAL) / 3, rng)
        }
        if (rng.nextDouble() >= Staffing.injuryChance(cura.prevenzione) * (plan?.injuryFactor() ?: 1.0)) return healed
        val ok = healed.indices.filter { healed[it].injuredRounds == 0 }
        if (ok.isEmpty()) return healed
        val i = ok[rng.nextInt(ok.size)]
        val turni = (1 + rng.nextInt(3) - (cura.recupero - Staffing.NEUTRAL) / 25).coerceAtLeast(1)
        return healed.mapIndexed { j, p -> if (j == i) p.copy(injuredRounds = turni) else p }
    }
}
