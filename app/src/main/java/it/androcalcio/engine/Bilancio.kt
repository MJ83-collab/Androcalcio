package it.androcalcio.engine

import it.androcalcio.data.Movimento

/** Bilancio di stagione: raggruppa i movimenti di cassa per categoria. */
object Bilancio {
    data class Riga(val categoria: String, val entrate: Long, val uscite: Long)

    private val ordine = listOf("Gare e ingaggi", "Sponsor", "Merchandising", "Premi", "Assicurazione", "Stadio", "Mercato", "Scouting", "Altro")

    /** Categoria ricavata dall'etichetta del movimento (le etichette le scrive l'app stessa). */
    fun categoria(label: String): String = when {
        label.startsWith("Bilancio") -> "Gare e ingaggi"
        label.startsWith("Sponsor") -> "Sponsor"
        label == "Merchandising" -> "Merchandising"
        label.startsWith("Premio piazzamento") -> "Premi"
        label.startsWith("Premio assicurazione") || label.startsWith("Rimborso infortuni") -> "Assicurazione"
        label.startsWith("Ampliamento") -> "Stadio"
        label.startsWith("Acquisto") || label.startsWith("Vendita") -> "Mercato"
        label.startsWith("Scouting") || label.startsWith("Osservazione") -> "Scouting"
        else -> "Altro"
    }

    /** Entrate e uscite per categoria, nell'ordine del bilancio. Il risultato è entrate - uscite. */
    fun riepilogo(mov: List<Movimento>): List<Riga> =
        mov.groupBy { categoria(it.label) }
            .map { (c, l) ->
                Riga(c, l.filter { it.importo > 0 }.sumOf { it.importo }, -l.filter { it.importo < 0 }.sumOf { it.importo })
            }
            .sortedBy { ordine.indexOf(it.categoria).let { i -> if (i < 0) ordine.size else i } }
}
