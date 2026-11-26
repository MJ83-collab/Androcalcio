package it.androcalcio.engine

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Calendario a giorni: ogni giornata (campionato e coppa, stessa numerazione) ha una data reale.
 * Tra una partita e l'altra l'allenatore sceglie cosa fare ogni giorno.
 */
object CalendarioGiorni {
    enum class Attivita(val label: String, val icona: String, val info: String) {
        ALLENAMENTO("Allenamento", "🏃", "Condizione −1, preparazione +1"),
        RIPOSO("Riposo", "🛌", "Condizione +6, morale +1, preparazione −1"),
        AMICHEVOLE("Amichevole", "🤝", "Condizione −4, preparazione +2; apre le amichevoli"),
        RIFINITURA("Rifinitura", "🎯", "Condizione −1, preparazione +1; migliora un po' la tecnica e l'affinità con i ruoli"),
        RITIRO("Ritiro", "🏕️", "Solo luglio e agosto. Costa 20.000 €: condizione −2, preparazione +2, morale +2")
    }

    const val COSTO_RITIRO = 20_000L   // al giorno

    const val KEY_OGGI = "cal_oggi_"      // + stagione: giorno corrente (epochDay)
    const val KEY_DIARIO = "cal_diario_"  // + stagione: "epochDay:ATTIVITA;..."
    const val KEY_PREP = "cal_prep"       // preparazione accumulata (0..4): morale in più alla partita
    const val PREP_MAX = 4

    fun anno(stagione: String): Int = stagione.take(4).toIntOrNull() ?: 2026

    /** Primo giorno della stagione: 1 luglio, inizio del ritiro. */
    fun inizio(stagione: String): LocalDate = LocalDate.of(anno(stagione), 7, 1)

    /** Data di un turno ([Turni.chiave]): domenica per il campionato, mercoledì per le coppe; la stessa fonte della lista partite. */
    fun data(stagione: String, chiave: Int): LocalDate = Turni.data(anno(stagione), chiave)

    /** Giorno corrente: quello salvato, ma mai prima del giorno dopo l'ultimo turno giocato. */
    fun oggi(stagione: String, salvato: Long?, ultimoTurno: Int?): LocalDate {
        val minimo = ultimoTurno?.let { data(stagione, it).plusDays(1) } ?: inizio(stagione)
        val s = salvato?.let { LocalDate.ofEpochDay(it) }
        return if (s == null || s.isBefore(minimo)) minimo else s
    }

    fun nomeGiorno(d: LocalDate): String =
        d.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ITALIAN).replaceFirstChar { it.uppercase() } + " " +
            d.dayOfMonth + " " + d.month.getDisplayName(TextStyle.FULL, Locale.ITALIAN) + " " + d.year

    fun giornoBreve(d: LocalDate): String = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ITALIAN).take(3)

    fun leggiDiario(s: String?): Map<Long, Attivita> = s.orEmpty().split(";").mapNotNull { e ->
        val x = e.split(":")
        if (x.size != 2) null else {
            val k = x[0].toLongOrNull()
            val a = Attivita.values().firstOrNull { it.name == x[1] }
            if (k != null && a != null) k to a else null
        }
    }.toMap()

    /** Tiene gli ultimi 120 giorni, per non far crescere il salvataggio. */
    fun scriviDiario(m: Map<Long, Attivita>): String =
        m.entries.sortedBy { it.key }.takeLast(120).joinToString(";") { "${it.key}:${it.value.name}" }

    /** Attività automatica per i giorni saltati: allenamento, riposo ogni tre giorni e il giorno prima della partita. */
    fun automatica(giorno: LocalDate, partita: LocalDate): Attivita =
        if (giorno.plusDays(1) == partita || giorno.toEpochDay() % 3 == 0L) Attivita.RIPOSO else Attivita.ALLENAMENTO

    /** Effetto di un'attività sulla preparazione accumulata. */
    fun prep(attuale: Int, a: Attivita): Int = when (a) {
        Attivita.ALLENAMENTO -> attuale + 1
        Attivita.AMICHEVOLE -> attuale + 2
        Attivita.RIPOSO -> attuale - 1
        Attivita.RIFINITURA -> attuale + 1
        Attivita.RITIRO -> attuale + 2
    }.coerceIn(0, PREP_MAX)

    /** Effetto di un'attività su condizione e morale di un giocatore non infortunato. */
    fun applica(p: it.androcalcio.data.Player, a: Attivita, rng: kotlin.random.Random = kotlin.random.Random.Default): it.androcalcio.data.Player = if (p.injuredRounds > 0) p else when (a) {
        Attivita.ALLENAMENTO -> p.copy(condition = (p.condition - cala(p, 1)).coerceAtLeast(50))
        Attivita.RIPOSO -> p.copy(condition = (p.condition + 4 + p.re / 25).coerceAtMost(100), morale = (p.morale + 1).coerceAtMost(99))
        Attivita.AMICHEVOLE -> p.copy(condition = (p.condition - cala(p, 4)).coerceAtLeast(50))
        Attivita.RIFINITURA -> rifinisci(p.copy(condition = (p.condition - cala(p, 1)).coerceAtLeast(50)), rng)
        Attivita.RITIRO -> p.copy(condition = (p.condition - cala(p, 2)).coerceAtLeast(50), morale = (p.morale + 2).coerceAtMost(99))
    }

    /** Fatica di un'attività: chi ha resistenza alta cala meno, chi è sopra i 30 anni un po' di più. */
    fun cala(p: it.androcalcio.data.Player, base: Int): Int =
        Math.round(base * (1.6 - p.re / 100.0) + (p.age - 30).coerceAtLeast(0) * 0.15).toInt().coerceAtLeast(0)

    /**
     * Rifinitura: prove tecniche e schemi. Con il 30% di probabilità uno tra passaggio, dribbling e tiro sale di 1
     * (fino a 90); ogni ruolo secondario sotto il 100% guadagna 1 punto di affinità.
     */
    fun rifinisci(p: it.androcalcio.data.Player, rng: kotlin.random.Random): it.androcalcio.data.Player {
        var q = p
        if (rng.nextInt(100) < 30) {
            q = when (rng.nextInt(3)) {
                0 -> if (q.pa < 90) q.copy(pa = q.pa + 1) else q
                1 -> if (q.dr < 90) q.copy(dr = q.dr + 1) else q
                else -> if (q.ti < 90) q.copy(ti = q.ti + 1) else q
            }
        }
        return q.copy(
            aff2 = if (q.sec2.isNotEmpty()) (q.aff2 + 1).coerceAtMost(100) else q.aff2,
            aff3 = if (q.sec3.isNotEmpty()) (q.aff3 + 1).coerceAtMost(100) else q.aff3,
            aff4 = if (q.sec4.isNotEmpty()) (q.aff4 + 1).coerceAtMost(100) else q.aff4
        )
    }
}
