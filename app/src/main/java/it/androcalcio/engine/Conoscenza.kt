package it.androcalcio.engine

import it.androcalcio.data.Player
import it.androcalcio.data.Team
import it.androcalcio.data.Youth
import kotlin.random.Random

/**
 * Quanto conosci un giocatore (stile FM): il potenziale e il carattere non si vedono finché qualcuno dello staff
 * non li valuta. Tuoi giocatori: allenatore in seconda. Tuo vivaio: allenatore delle giovanili. Altri: scout.
 * La stima è una forbice che si stringe con la conoscenza e con la bravura di chi valuta.
 */
object Conoscenza {
    /** Sotto questa soglia il potenziale resta "?". */
    const val MIN_POT = 25
    /** Da questa soglia lo scout riferisce anche il carattere. */
    const val MIN_CARATTERE = 50

    /** Conoscenza di un giocatore di un altro club: ogni osservazione aggiunge, di più con uno scout bravo. */
    fun daScout(osservazioni: Int, scoutSkill: Int): Int =
        if (osservazioni <= 0) 0 else (osservazioni * (18 + scoutSkill / 5)).coerceIn(0, 100)

    /** Conoscenza dei tuoi: dipende solo da chi valuta (vice o allenatore delle giovanili). */
    fun daStaff(skill: Int): Int = (45 + skill / 2).coerceIn(45, 95)

    data class Stima(val chi: String, val conoscenza: Int, val forbice: IntRange?, val carattere: String?) {
        val giudizio: String get() = forbice?.let { Youths.judge((it.first + it.last) / 2) } ?: "Sconosciuto"
        val testo: String get() = forbice?.let { "${it.first}–${it.last} ($giudizio)" } ?: "? (serve una valutazione)"
    }

    /** Forbice del potenziale: larga con poca conoscenza, sempre attorno al valore vero ma con un errore fisso per giocatore. */
    fun forbice(potenziale: Int, livello: Int, conoscenza: Int, seme: Long): IntRange? {
        if (conoscenza < MIN_POT) return null
        val half = ((100 - conoscenza) / 6 + 2).coerceIn(2, 14)
        val errore = Random(seme * 37 + conoscenza / 10).nextInt(-half / 2, half / 2 + 1)
        val c = potenziale + errore
        return (c - half).coerceAtLeast(livello).coerceAtMost(99)..(c + half).coerceIn(livello, 99)
    }

    fun stima(p: Player, chi: String, conoscenza: Int): Stima = Stima(
        chi, conoscenza, forbice(p.potential, p.overall, conoscenza, p.id),
        if (conoscenza >= MIN_CARATTERE) Personalita.tipo(p).nome else null
    )

    fun stimaGiovane(y: Youth, chi: String, conoscenza: Int): Stima = Stima(
        chi, conoscenza, forbice(y.potential, y.overall, conoscenza, -y.id), null
    )

    /** Parere del vice per la prima squadra: che ruolo avrebbe oggi e domani nella rosa. */
    fun ruoloInRosa(p: Player, rosa: List<Player>, stimaPot: IntRange?): String {
        val pari = rosa.filter { it.role == p.role && it.id != p.id }.map { it.overall }.sortedDescending()
        val posti = when (p.role) { "P" -> 1; "D" -> 4; "C" -> 4; else -> 2 }
        val soglia = pari.getOrNull(posti - 1) ?: 0
        val oggi = when {
            p.overall >= soglia + 3 -> "titolare"
            p.overall >= soglia - 2 -> "da rotazione"
            else -> "riserva"
        }
        val domani = stimaPot?.let { if ((it.first + it.last) / 2 >= soglia + 5 && p.age <= 23) ", può diventare un titolare fisso" else "" } ?: ""
        return "Oggi $oggi$domani."
    }

    /**
     * Affinità di carattere con la tua squadra: guarda chi c'è già nello spogliatoio.
     * Restituisce livello (Alta, Media, Bassa) e motivo.
     */
    fun affinita(p: Player, rosa: List<Player>, ricca: Boolean): Pair<String, String> {
        val tipi = rosa.filter { it.id != p.id }.groupingBy { Personalita.tipo(it) }.eachCount()
        val moraleMedio = rosa.map { it.morale }.ifEmpty { listOf(70) }.average()
        val pari = rosa.filter { it.role == p.role && it.id != p.id }.map { it.overall }.sortedDescending()
        val titolare = p.overall >= (pari.getOrNull(1) ?: 0)
        return when (Personalita.tipo(p)) {
            Personalita.Tipo.LEADER -> when (tipi[Personalita.Tipo.LEADER] ?: 0) {
                0, 1 -> "Alta" to "Alla squadra serve un leader: lo diventerebbe."
                2 -> "Media" to "Ci sono già due leader: dovrà guadagnarsi lo spazio."
                else -> "Bassa" to "Troppi leader nello spogliatoio: rischio di attriti."
            }
            Personalita.Tipo.PROFESSIONISTA -> "Alta" to "Si adatta a qualsiasi gruppo."
            Personalita.Tipo.FEDELE -> "Alta" to "Si lega in fretta alla maglia."
            Personalita.Tipo.AMBIZIOSO -> if (titolare) "Alta" to "Sarebbe titolare: è ciò che vuole."
                else "Bassa" to "Partirebbe dalla panchina: vorrà andarsene presto."
            Personalita.Tipo.MERCENARIO -> if (ricca) "Media" to "Contento finché paghi bene."
                else "Bassa" to "Club non ricco: chiederà aumenti continui."
            Personalita.Tipo.SENSIBILE -> if (moraleMedio >= 65) "Media" to "Gruppo sereno: può rendere bene."
                else "Bassa" to "Spogliatoio giù di morale: rischia di crollare."
        }
    }
}

/**
 * Vivai diversi per ogni club: qualità (da 1 a 5 stelle) e specialità fissa del settore giovanile.
 * Qualità da allenatore delle giovanili, ricchezza e stadio; la specialità nasce dal nome del club (sempre la stessa).
 */
object VivaioClub {
    enum class Specialita(val nome: String, val ruoli: List<String>, val estero: Int) {
        DIFENSORI("Fucina di difensori", listOf("D", "D", "D", "C", "P", "A"), 10),
        PORTIERI("Scuola di portieri", listOf("P", "P", "D", "C", "A"), 10),
        CENTROCAMPO("Scuola di registi", listOf("C", "C", "C", "D", "A"), 10),
        ATTACCANTI("Talenti offensivi", listOf("A", "A", "A", "C", "D"), 10),
        MONDO("Rete di osservatori nel mondo", listOf("D", "C", "A", "C", "D", "P"), 45),
        TERRITORIO("Solo ragazzi del territorio", listOf("D", "C", "A", "C", "D", "P"), 0)
    }

    fun specialita(t: Team): Specialita = Specialita.values()[Math.floorMod(t.name.hashCode() * 17, Specialita.values().size)]

    /** Mezze stelle da 2 a 10. */
    fun qualita(t: Team): Int {
        val soldi = (t.budget / 5_000_000L).toInt().coerceIn(0, 10)
        val stadio = (t.stadiumCapacity / 8_000).coerceIn(0, 8)
        val v = t.youthCoachSkill / 10 + soldi / 2 + stadio / 2
        return (v / 2).coerceIn(2, 10)
    }

    /** Punti di potenziale in più o in meno per i ragazzi di questo vivaio. */
    fun bonusPotenziale(t: Team): Int = (qualita(t) - 5) * 2
}
