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
        MERCATO("Mercato", "💼", "Giornata leggera, condizione +2; apre il mercato")
    }

    const val KEY_OGGI = "cal_oggi_"      // + stagione: giorno corrente (epochDay)
    const val KEY_DIARIO = "cal_diario_"  // + stagione: "epochDay:ATTIVITA;..."
    const val KEY_PREP = "cal_prep"       // preparazione accumulata (0..4): morale in più alla partita
    const val PREP_MAX = 4

    fun anno(stagione: String): Int = stagione.take(4).toIntOrNull() ?: 2026

    /** Primo giorno della stagione: 1 luglio, inizio del ritiro. */
    fun inizio(stagione: String): LocalDate = LocalDate.of(anno(stagione), 7, 1)

    /** Prima domenica dal 23 agosto: prima giornata. */
    private fun primaGiornata(stagione: String): LocalDate {
        var d = LocalDate.of(anno(stagione), 8, 23)
        while (d.dayOfWeek != DayOfWeek.SUNDAY) d = d.plusDays(1)
        return d
    }

    /** Data della giornata (1 = prima): una a settimana, con due settimane di sosta a Natale. */
    fun data(stagione: String, giornata: Int): LocalDate {
        var d = primaGiornata(stagione).plusWeeks((giornata - 1).coerceAtLeast(0).toLong())
        if (!d.isBefore(LocalDate.of(anno(stagione), 12, 22))) d = d.plusWeeks(2)
        return d
    }

    /** Giorno corrente: quello salvato, ma mai prima del giorno dopo l'ultima giornata giocata. */
    fun oggi(stagione: String, salvato: Long?, ultimaGiocata: Int?): LocalDate {
        val minimo = ultimaGiocata?.let { data(stagione, it).plusDays(1) } ?: inizio(stagione)
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
        Attivita.MERCATO -> attuale
    }.coerceIn(0, PREP_MAX)

    /** Effetto di un'attività su condizione e morale di un giocatore non infortunato. */
    fun applica(p: it.androcalcio.data.Player, a: Attivita): it.androcalcio.data.Player = if (p.injuredRounds > 0) p else when (a) {
        Attivita.ALLENAMENTO -> p.copy(condition = (p.condition - 1).coerceAtLeast(50))
        Attivita.RIPOSO -> p.copy(condition = (p.condition + 6).coerceAtMost(100), morale = (p.morale + 1).coerceAtMost(99))
        Attivita.AMICHEVOLE -> p.copy(condition = (p.condition - 4).coerceAtLeast(50))
        Attivita.MERCATO -> p.copy(condition = (p.condition + 2).coerceAtMost(100))
    }
}
