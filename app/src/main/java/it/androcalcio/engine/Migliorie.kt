package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/** Effetti di una miglioria: si sommano a quelli delle altre già costruite. */
data class EffettiMiglioria(
    val posti: Int = 0,            // posti in più allo stadio (applicati a fine lavori)
    val incassoPct: Int = 0,       // % in più sugli incassi dei biglietti a fine stagione
    val merchAnno: Long = 0,       // merchandising e servizi in più all'anno
    val tifosi: Int = 0,           // umore tifosi, una volta a fine lavori
    val morale: Int = 0,           // morale della rosa, una volta a fine lavori
    val crescitaPct: Int = 0,      // % in più di crescita dei giocatori a fine stagione
    val infortuniPct: Int = 0,     // % di infortuni evitati dopo ogni partita
    val vivaio: Int = 0,           // punti di potenziale in più ai giovani
    val manutenzione: Long = 0     // costo (o risparmio, se negativo) all'anno
) {
    operator fun plus(o: EffettiMiglioria) = EffettiMiglioria(
        posti + o.posti, incassoPct + o.incassoPct, merchAnno + o.merchAnno, tifosi + o.tifosi, morale + o.morale,
        crescitaPct + o.crescitaPct, infortuniPct + o.infortuniPct, vivaio + o.vivaio, manutenzione + o.manutenzione)
}

data class Miglioria(
    val id: String, val area: String, val nome: String, val icona: String,
    val costo: Long, val giornate: Int, val effetti: EffettiMiglioria, val richiede: List<String> = emptyList()
)

/**
 * Migliorie di stadio e centro sportivo: costo, giornate di lavoro, effetti e ritorno economico stimato.
 * Stato salvato come testo: costruite "id,id" e cantieri "id:giornate;id:giornate".
 */
object Migliorie {
    const val STADIO = "Stadio"
    const val CENTRO = "Centro sportivo"
    const val MAX_CANTIERI = 3

    private fun m(id: String, area: String, nome: String, icona: String, costoK: Int, gg: Int, e: EffettiMiglioria,
                  vararg req: String) = Miglioria(id, area, nome, icona, costoK * 1_000L, gg,
        e.copy(manutenzione = if (e.manutenzione != 0L) e.manutenzione else costoK * 30L), req.toList())

    val TUTTE: List<Miglioria> = listOf(
        m("curva_n", STADIO, "Curva nord ampliata", "🏟️", 2_400, 8, EffettiMiglioria(posti = 3_000)),
        m("curva_s", STADIO, "Curva sud ampliata", "🏟️", 2_400, 8, EffettiMiglioria(posti = 3_000), "curva_n"),
        m("anello", STADIO, "Anello superiore", "🏗️", 9_000, 20, EffettiMiglioria(posti = 8_000, tifosi = 5), "curva_n", "curva_s"),
        m("seggiolini", STADIO, "Seggiolini nuovi", "💺", 800, 3, EffettiMiglioria(incassoPct = 3)),
        m("tribuna", STADIO, "Tribuna coperta", "⛱️", 3_000, 10, EffettiMiglioria(incassoPct = 6, tifosi = 3)),
        m("copertura", STADIO, "Copertura totale", "🏛️", 6_000, 16, EffettiMiglioria(incassoPct = 5, tifosi = 3), "tribuna"),
        m("maxischermo", STADIO, "Maxischermo", "📺", 1_500, 4, EffettiMiglioria(incassoPct = 4, merchAnno = 80_000)),
        m("faretti", STADIO, "Impianto di illuminazione", "🔦", 2_000, 6, EffettiMiglioria(incassoPct = 5, tifosi = 2)),
        m("led", STADIO, "Illuminazione LED", "💡", 1_200, 4, EffettiMiglioria(incassoPct = 2, manutenzione = -40_000)),
        m("skybox", STADIO, "Skybox VIP", "🥂", 4_500, 12, EffettiMiglioria(incassoPct = 10)),
        m("hospitality", STADIO, "Area hospitality", "🍽️", 2_200, 6, EffettiMiglioria(incassoPct = 5, merchAnno = 100_000), "skybox"),
        m("museo", STADIO, "Museo del club", "🏆", 1_800, 8, EffettiMiglioria(merchAnno = 250_000, tifosi = 2)),
        m("tour", STADIO, "Tour dello stadio", "🎟️", 400, 2, EffettiMiglioria(merchAnno = 150_000), "museo"),
        m("store", STADIO, "Store ufficiale", "👕", 1_200, 4, EffettiMiglioria(merchAnno = 300_000)),
        m("sito", STADIO, "Sito web e shop online", "🌐", 500, 2, EffettiMiglioria(merchAnno = 180_000), "store"),
        m("sito2", STADIO, "Shop online con spedizioni in tutto il mondo", "🌍", 900, 3, EffettiMiglioria(merchAnno = 220_000), "sito"),
        m("sito3", STADIO, "App ufficiale del club", "📲", 1_400, 4, EffettiMiglioria(merchAnno = 260_000, tifosi = 2), "sito2"),
        m("store2", STADIO, "Secondo store in città", "🛍️", 1_000, 4, EffettiMiglioria(merchAnno = 200_000), "store"),
        m("store3", STADIO, "Store in un'altra città", "🏬", 1_500, 5, EffettiMiglioria(merchAnno = 230_000), "store2"),
        m("store4", STADIO, "Store all'estero", "✈️", 2_500, 6, EffettiMiglioria(merchAnno = 320_000), "store3"),
        m("bar", STADIO, "Bar e ristoranti", "🍕", 1_000, 4, EffettiMiglioria(incassoPct = 4)),
        m("parcheggi", STADIO, "Parcheggi", "🅿️", 900, 5, EffettiMiglioria(incassoPct = 3, tifosi = 1)),
        m("famiglie", STADIO, "Settore famiglie", "👨‍👩‍👧", 600, 3, EffettiMiglioria(incassoPct = 2, tifosi = 4)),
        m("wifi", STADIO, "Wi-Fi e app dello stadio", "📱", 700, 3, EffettiMiglioria(incassoPct = 2, merchAnno = 120_000)),
        m("solare", STADIO, "Pannelli solari", "☀️", 1_600, 6, EffettiMiglioria(manutenzione = -90_000)),
        m("ibrido", STADIO, "Manto erboso ibrido", "🌱", 1_100, 3, EffettiMiglioria(infortuniPct = 8)),
        m("riscaldamento", STADIO, "Riscaldamento del campo", "🔥", 700, 3, EffettiMiglioria(infortuniPct = 4)),
        m("irrigazione", STADIO, "Irrigazione automatica del campo", "💧", 650, 3, EffettiMiglioria(infortuniPct = 4), "ibrido"),
        m("palestra", CENTRO, "Palestra moderna", "🏋️", 1_200, 5, EffettiMiglioria(crescitaPct = 5)),
        m("piscina", CENTRO, "Piscina riabilitativa", "🏊", 1_500, 6, EffettiMiglioria(infortuniPct = 10)),
        m("medico", CENTRO, "Centro medico", "🩺", 2_500, 8, EffettiMiglioria(infortuniPct = 12)),
        m("campi", CENTRO, "Campi di allenamento extra", "⚽", 900, 4, EffettiMiglioria(crescitaPct = 4)),
        m("video", CENTRO, "Sala analisi video", "🎬", 800, 3, EffettiMiglioria(crescitaPct = 4)),
        m("gps", CENTRO, "Laboratorio GPS e dati", "📡", 1_800, 6, EffettiMiglioria(crescitaPct = 5), "video"),
        m("crio", CENTRO, "Criosauna e recupero", "❄️", 1_000, 4, EffettiMiglioria(infortuniPct = 6, morale = 3)),
        m("foresteria", CENTRO, "Foresteria giocatori", "🏨", 1_400, 6, EffettiMiglioria(morale = 5)),
        m("mensa", CENTRO, "Mensa con nutrizionista", "🥗", 600, 3, EffettiMiglioria(crescitaPct = 3, infortuniPct = 4)),
        m("relax", CENTRO, "Sala relax", "🎮", 500, 2, EffettiMiglioria(morale = 4)),
        m("coperto", CENTRO, "Campo coperto invernale", "🏠", 2_200, 8, EffettiMiglioria(crescitaPct = 3, infortuniPct = 3)),
        m("convitto", CENTRO, "Convitto del vivaio", "🎒", 2_000, 8, EffettiMiglioria(vivaio = 2)),
    )

    private val BY_ID = TUTTE.associateBy { it.id }
    fun trova(id: String) = BY_ID[id]

    fun decodeFatte(s: String?): Set<String> = s.orEmpty().split(",").filter { it in BY_ID }.toSet()
    fun encodeFatte(ids: Set<String>) = ids.sorted().joinToString(",")
    fun decodeCantieri(s: String?): Map<String, Int> = s.orEmpty().split(";").mapNotNull { x ->
        val p = x.split(":"); val g = p.getOrNull(1)?.toIntOrNull()
        if (p[0] in BY_ID && g != null) p[0] to g else null
    }.toMap()
    fun encodeCantieri(c: Map<String, Int>) = c.entries.joinToString(";") { "${it.key}:${it.value}" }

    /** Si può avviare: non già fatta o in corso, requisiti costruiti, cantieri liberi, budget sufficiente. */
    fun avviabile(m: Miglioria, fatte: Set<String>, cantieri: Map<String, Int>, budget: Long): String? = when {
        m.id in fatte -> "Già costruita"
        m.id in cantieri -> "Lavori in corso"
        m.richiede.any { it !in fatte } -> "Serve prima: " + m.richiede.filter { it !in fatte }.joinToString { trova(it)?.nome ?: it }
        cantieri.size >= MAX_CANTIERI -> "Massimo $MAX_CANTIERI cantieri insieme"
        budget < m.costo -> "Budget insufficiente"
        else -> null
    }

    /** Avanza i cantieri di [giornate]: ritorna i cantieri rimasti e le migliorie finite. */
    fun avanza(cantieri: Map<String, Int>, giornate: Int): Pair<Map<String, Int>, List<Miglioria>> {
        val rimasti = cantieri.mapValues { it.value - giornate }
        return rimasti.filterValues { it > 0 } to rimasti.filterValues { it <= 0 }.keys.mapNotNull { trova(it) }
    }

    fun effetti(fatte: Set<String>): EffettiMiglioria = fatte.mapNotNull { trova(it)?.effetti }.fold(EffettiMiglioria()) { a, b -> a + b }

    /**
     * Ritorno economico annuo stimato: incasso extra (posti e %), merchandising, meno la manutenzione.
     * [incassoStagione] = incasso biglietti attuale a stagione; [incassoPerPosto] = quanto rende un posto in una stagione.
     */
    fun ritornoAnnuo(m: Miglioria, incassoStagione: Long, incassoPerPosto: Double): Long {
        val e = m.effetti
        return (incassoStagione * e.incassoPct / 100.0 + e.posti * incassoPerPosto).toLong() + e.merchAnno - e.manutenzione
    }

    /** Stagioni per ripagare la spesa; null se non si ripaga (migliorie sportive). */
    fun stagioniPerRipagare(m: Miglioria, ritorno: Long): Double? = if (ritorno > 0) m.costo.toDouble() / ritorno else null

    /** Prevenzione: dei giocatori appena infortunati, il [pct]% guarisce subito (strutture mediche). */
    fun prevenzione(prima: List<Player>, dopo: List<Player>, pct: Int, rng: Random): List<Player> {
        if (pct <= 0) return dopo
        val sani = prima.filter { it.injuredRounds == 0 }.mapTo(HashSet()) { it.id }
        return dopo.map { p -> if (p.injuredRounds > 0 && p.id in sani && rng.nextInt(100) < pct) p.copy(injuredRounds = 0) else p }
    }
}
