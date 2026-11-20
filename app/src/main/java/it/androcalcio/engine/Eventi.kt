package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/** Effetto di una scelta: morale dei giocatori coinvolti, clima, fiducia dirigenza, tifosi, condizione rosa. */
data class Scelta(val label: String, val morale: Int = 0, val clima: Int = 0, val fiducia: Int = 0,
                  val tifosi: Int = 0, val condizione: Int = 0, val esito: String = "")

data class Evento(val titolo: String, val testo: String, val giocatori: List<Long>, val scelte: List<Scelta>)

/**
 * Spogliatoio e interazioni alla PC Calcio/FM.
 * Ogni giornata: clima e tifosi si muovono col risultato; il clima sposta il morale di tutti;
 * con probabilità ~35% arriva un evento con 2-3 scelte.
 */
object Spogliatoio {
    /** Variazioni dopo una partita della tua squadra: gf/gs = gol fatti/subiti. */
    fun dopoPartita(gf: Int, gs: Int): Pair<Int, Int> = when {   // (clima, tifosi)
        gf > gs -> 2 to (if (gf - gs >= 3) 3 else 2)
        gf < gs -> -2 to (if (gs - gf >= 3) -4 else -2)
        else -> 0 to -1
    }

    /** Spinta del clima sul morale di tutti: clima 80 = +1 a giornata, 30 = -2. */
    fun spintaMorale(clima: Int) = (clima - 60) / 15

    fun evento(rosa: List<Player>, clima: Int, fiducia: Int, tifosi: Int, perso: Boolean,
               haPreparatore: Boolean, rng: Random): Evento? {
        if (rosa.size < 4 || rng.nextInt(100) >= 35) return null
        val panchina = rosa.filter { !it.starter && it.overall >= rosa.maxOf { p -> p.overall } - 8 && it.injuredRounds == 0 }
        val tipi = buildList {
            if (panchina.isNotEmpty()) add(0)
            add(1); add(2)
            if (haPreparatore && rosa.count { it.condition < 75 } >= 3) add(3)
            if (perso && tifosi < 50) add(4)
            if (fiducia < 40) add(5)
            if (clima < 45) add(6)
        }
        val a = rosa.random(rng)
        val b = (rosa - a).random(rng)
        return when (tipi.random(rng)) {
            0 -> panchina.random(rng).let { p -> Evento("Spazio in squadra",
                "${p.name} (${p.overall}) chiede di giocare di più.", listOf(p.id), listOf(
                Scelta("Gli prometto spazio", morale = 12, clima = 1, esito = "${p.name} è motivato."),
                Scelta("Deve meritarselo", morale = -8, clima = 1, esito = "Il gruppo apprezza la fermezza."),
                Scelta("Può cercarsi un'altra squadra", morale = -20, clima = -3, esito = "${p.name} è furioso."))) }
            1 -> Evento("Lite in allenamento", "${a.name} e ${b.name} sono venuti alle mani.", listOf(a.id, b.id), listOf(
                Scelta("Multa a entrambi", morale = -6, clima = 3, esito = "Disciplina ristabilita."),
                Scelta("Li faccio chiarire", morale = 3, clima = 1, esito = "Si sono stretti la mano."),
                Scelta("Lascio correre", clima = -5, esito = "Lo spogliatoio si divide.")))
            2 -> Evento("Intesa in campo", "${a.name} e ${b.name} si trovano a meraviglia.", listOf(a.id, b.id), listOf(
                Scelta("Complimenti pubblici", morale = 6, clima = 2, esito = "Morale alle stelle."),
                Scelta("Restiamo concentrati", morale = 2, clima = 1, esito = "Ricevuto, mister.")))
            3 -> Evento("Il preparatore avverte", "Molti giocatori sono stanchi: propone un giorno di riposo.", emptyList(), listOf(
                Scelta("Giorno di riposo", condizione = 10, clima = 2, esito = "La squadra recupera."),
                Scelta("Si lavora lo stesso", condizione = -3, clima = -2, esito = "Qualcuno brontola.")))
            4 -> Evento("Contestazione", "I tifosi contestano dopo la sconfitta.", emptyList(), listOf(
                Scelta("Vado a parlarci", tifosi = 6, fiducia = 1, esito = "Applausi alla fine."),
                Scelta("Difendo i giocatori", tifosi = -2, clima = 4, esito = "La squadra si compatta."),
                Scelta("Nessun commento", tifosi = -5, esito = "La curva fischia.")))
            5 -> Evento("Il presidente chiama", "La dirigenza è preoccupata dai risultati.", emptyList(), listOf(
                Scelta("Prometto una svolta", fiducia = 5, clima = -1, esito = "Ti danno tempo, ma ora devi vincere."),
                Scelta("Chiedo pazienza", fiducia = -2, clima = 2, esito = "Il presidente non è convinto.")))
            else -> Evento("Spogliatoio teso", "Il capitano dice che il gruppo è sfiduciato.", emptyList(), listOf(
                Scelta("Cena di squadra", morale = 4, clima = 6, esito = "Serata riuscita."),
                Scelta("Allenamenti più duri", clima = -3, condizione = -2, esito = "Musi lunghi."),
                Scelta("Ci parla il vice", clima = 3, esito = "Il clima migliora un po'.")))
        }
    }
}
