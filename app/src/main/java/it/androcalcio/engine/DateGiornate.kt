package it.androcalcio.engine

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Date delle giornate di campionato: si parte l'ultima domenica di agosto, una giornata a settimana, sosta di Natale. */
object DateGiornate {
    private val MESI = listOf("gen", "feb", "mar", "apr", "mag", "giu", "lug", "ago", "set", "ott", "nov", "dic")
    private val GIORNI = listOf("lun", "mar", "mer", "gio", "ven", "sab", "dom")

    /** Data della giornata [giornata] (da 1) nella stagione che inizia nell'anno [anno]. */
    fun data(anno: Int, giornata: Int): LocalDate {
        val inizio = LocalDate.of(anno, 8, 31).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        var d = inizio.plusWeeks((giornata - 1).coerceAtLeast(0).toLong())
        // Sosta natalizia: le giornate che cadrebbero tra il 22 dicembre e il 5 gennaio slittano di due settimane
        val sosta = LocalDate.of(anno, 12, 22)
        if (!d.isBefore(sosta)) d = d.plusWeeks(2)
        return d
    }

    /** Es. "dom 30 ago 2026". */
    fun testo(anno: Int, giornata: Int): String {
        val d = data(anno, giornata)
        return "${GIORNI[d.dayOfWeek.value - 1]} ${d.dayOfMonth} ${MESI[d.monthValue - 1]} ${d.year}"
    }

    /** Anno di inizio da una stagione come "2026/27"; 2026 se non leggibile. */
    fun anno(stagione: String): Int = stagione.take(4).toIntOrNull() ?: 2026
}
