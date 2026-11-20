package it.androcalcio.engine

import kotlin.random.Random

/** Contratto sponsor pluriennale: compenso annuo e anni rimasti (pagato a fine stagione). */
data class SponsorDeal(val name: String, val annual: Long, val years: Int) {
    companion object {
        fun encode(l: List<SponsorDeal>) = l.joinToString(";") { "${it.name}|${it.annual}|${it.years}" }
        fun decode(s: String?): List<SponsorDeal> = s.orEmpty().split(";").mapNotNull {
            val p = it.split("|")
            if (p.size == 3) SponsorDeal(p[0], p[1].toLongOrNull() ?: return@mapNotNull null, p[2].toIntOrNull() ?: 1) else null
        }
    }
}

/**
 * Sponsor alla PC Calcio / FM: fino a 5 contratti insieme (maglia, tecnico, secondari).
 * Il compenso segue la forza della squadra (scala Market.valueAt) e la fama (tifosi + reputazione).
 * Esempio con fama media: forza 85 → maglia ~7 M l'anno, forza 70 → ~0,5 M, Serie C ~0,1 M.
 */
object Sponsors {
    const val MAX_ATTIVI = 5
    /** Peso dello slot: il primo contratto (maglia) vale di più, poi a scendere. */
    private val SLOT = listOf(1.0, 0.5, 0.35, 0.25, 0.15)

    private val NOMI = listOf(
        "Acqua Monteverde", "Autoscuole Lagunari", "Cantina Torre Alta", "Banca Padania Unita", "Telefonia Stella",
        "Pneumatici Rombo", "Caffè Valdora", "Assicurazioni Leone d'Oro", "Pasta Granfiore", "Energia Solaris",
        "Moto Fulmine", "Birra Ghiacciaio", "Elettro Brivio", "Gelati Polare", "Volo Azzurro",
        "Orologi Meridiana", "Supermercati Ortolano", "Sportivo Kappaflex", "Costruzioni Granito", "Web Aquilone"
    )

    /** Fama 0,6..1,4: tifosi e reputazione alti alzano le offerte. */
    fun fama(tifosi: Int, reputazione: Int) = 0.6 + tifosi / 250.0 + reputazione / 250.0

    /** Compenso annuo per lo slot n (0 = maglia), anni di durata: contratti lunghi = un po' meno all'anno. */
    fun annual(strength: Int, slot: Int, fama: Double, years: Int): Long {
        val base = 0.12 * Market.valueAt(strength) * fama * SLOT[slot.coerceIn(0, SLOT.size - 1)]
        val v = base * (1.1 - 0.05 * years)
        return (v / 10_000).toLong() * 10_000L
    }

    /** Tre offerte per il prossimo slot libero, con nomi non già sotto contratto. */
    fun offers(active: List<SponsorDeal>, strength: Int, fama: Double, rng: Random): List<SponsorDeal> {
        if (active.size >= MAX_ATTIVI) return emptyList()
        val used = active.map { it.name }.toSet()
        return NOMI.filter { it !in used }.shuffled(rng).take(3).map { n ->
            val y = rng.nextInt(1, 6)
            val noise = 0.85 + rng.nextDouble() * 0.3
            SponsorDeal(n, (annual(strength, active.size, fama, y) * noise).toLong() / 10_000 * 10_000, y)
        }
    }
}
