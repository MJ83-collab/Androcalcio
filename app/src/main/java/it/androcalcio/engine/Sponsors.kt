package it.androcalcio.engine

import kotlin.random.Random

/** Contratto sponsor pluriennale: compenso annuo e anni rimasti (pagato a fine stagione). */
data class SponsorDeal(val name: String, val annual: Long, val years: Int, val tipo: String = "") {
    companion object {
        fun encode(l: List<SponsorDeal>) = l.joinToString(";") { "${it.name}|${it.annual}|${it.years}|${it.tipo}" }
        fun decode(s: String?): List<SponsorDeal> = s.orEmpty().split(";").mapNotNull {
            val p = it.split("|")
            if (p.size >= 3) SponsorDeal(p[0], p[1].toLongOrNull() ?: return@mapNotNull null, p[2].toIntOrNull() ?: 1,
                p.getOrNull(3).orEmpty()) else null
        }
    }
}

/**
 * Sponsor alla PC Calcio / FM: fino a 5 contratti insieme (maglia, tecnico, secondari).
 * Il compenso segue la forza della squadra (scala Market.valueAt) e la fama (tifosi + reputazione).
 * Esempio con fama media: forza 85 → maglia ~7 M l'anno, forza 70 → ~0,5 M, Serie C ~0,1 M.
 */
object Sponsors {
    const val MAX_ATTIVI = 10
    /** Peso dello slot: il primo contratto (maglia) vale di più, poi a scendere. */
    private val SLOT = listOf(1.0, 0.5, 0.35, 0.25, 0.15, 0.12, 0.10, 0.08, 0.06, 0.05)

    /** Tipi di sponsor: peso sul compenso (maglia = 1). */
    data class Tipo(val id: String, val nome: String, val peso: Double)
    val TIPI = listOf(
        Tipo("maglia", "Sponsor di maglia", 1.0), Tipo("tecnico", "Sponsor tecnico", 0.6),
        Tipo("stadio", "Naming dello stadio", 0.45), Tipo("abbigliamento", "Abbigliamento", 0.3),
        Tipo("manica", "Sponsor di manica", 0.3), Tipo("banca", "Partner finanziario", 0.28),
        Tipo("media", "Telefonia e media", 0.25), Tipo("retro", "Retro maglia", 0.22),
        Tipo("bevande", "Bevanda ufficiale", 0.2), Tipo("pantaloncini", "Pantaloncini", 0.15)
    )
    fun nomeTipo(id: String) = TIPI.firstOrNull { it.id == id }?.nome ?: "Sponsor"

    /** Addetto marketing: cerca sponsor migliori e più numerosi. Costo annuo, pagato a fine stagione. */
    val MARKETING = listOf("Nessuno", "Addetto marketing", "Responsabile marketing", "Direttore commerciale")
    fun costoMarketing(livello: Int): Long = longArrayOf(0L, 250_000L, 600_000L, 1_200_000L)[livello.coerceIn(0, 3)]
    /** Quante offerte arrivano e di quanto salgono i compensi (percentuale) con quel livello. */
    fun numeroOfferte(livello: Int) = 3 + livello.coerceIn(0, 3)
    fun bonusMarketingPct(livello: Int) = 5 * livello.coerceIn(0, 3)

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

    /** Offerte per i tipi di sponsor ancora liberi; l'addetto marketing ne porta di più e meglio pagate. */
    fun offers(active: List<SponsorDeal>, strength: Int, fama: Double, rng: Random, marketing: Int = 0): List<SponsorDeal> {
        if (active.size >= MAX_ATTIVI) return emptyList()
        val usati = active.map { it.name }.toSet()
        val tipiLiberi = TIPI.filter { t -> active.none { it.tipo == t.id } }
        if (tipiLiberi.isEmpty()) return emptyList()
        val nomi = NOMI.filter { it !in usati }.shuffled(rng)
        return tipiLiberi.shuffled(rng).take(numeroOfferte(marketing)).mapIndexed { i, t ->
            val y = rng.nextInt(1, 6)
            val noise = (0.85 + rng.nextDouble() * 0.3) * (1.0 + bonusMarketingPct(marketing) / 100.0)
            val base = 0.12 * Market.valueAt(strength) * fama * t.peso * (1.1 - 0.05 * y)
            SponsorDeal(nomi[i % nomi.size], (base * noise).toLong() / 10_000 * 10_000, y, t.id)
        }
    }
}
