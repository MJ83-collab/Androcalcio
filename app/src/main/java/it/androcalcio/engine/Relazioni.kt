package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/** Conferenze stampa, colloqui individuali e psicologo: tutto ciò che muove morale, tifosi e fiducia a parole. */
object Relazioni {

    data class Risposta(val testo: String, val morale: Int, val tifosi: Int, val fiducia: Int)
    data class Domanda(val id: Int, val testo: String, val risposte: List<Risposta>)

    val DOMANDE: List<Domanda> = listOf(
        Domanda(0, "Dopo l'ultima partita: come giudica la prestazione?", listOf(
            Risposta("Siamo stati bravi, i ragazzi meritano un applauso.", 2, 1, 0),
            Risposta("Dobbiamo fare molto di più, non basta.", -1, 0, 2),
            Risposta("Parlerà il campo, non ho altro da dire.", 0, -1, 0))),
        Domanda(1, "I tifosi chiedono rinforzi sul mercato. Cosa risponde?", listOf(
            Risposta("La società ci sta lavorando, arriveranno.", 0, 2, -1),
            Risposta("Questa rosa è forte così, ci credo.", 2, 0, 1),
            Risposta("Il mercato è affare della dirigenza.", -1, -1, 2))),
        Domanda(2, "Si parla di tensioni nello spogliatoio. È vero?", listOf(
            Risposta("Falso: siamo un gruppo unito.", 2, 1, 0),
            Risposta("Ne parleremo dentro lo spogliatoio, non qui.", 1, 0, 1),
            Risposta("Qualche scontro c'è, ma fa parte del gioco.", -2, 0, -1))),
        Domanda(3, "Qual è l'obiettivo stagionale?", listOf(
            Risposta("Puntiamo in alto, senza paura.", 1, 3, -1),
            Risposta("Una partita alla volta, con umiltà.", 1, 0, 2),
            Risposta("Salvarci il prima possibile.", -2, -1, 1))),
        Domanda(3 + 1, "Un titolare è stato criticato dalla stampa. La sua opinione?", listOf(
            Risposta("Lo difendo: è un professionista serio.", 3, 0, 0),
            Risposta("Le critiche fanno crescere.", -1, 0, 1),
            Risposta("Ha sbagliato e lo sa.", -3, 1, 0))),
        Domanda(5, "Il prossimo avversario è molto più forte. Teme la sfida?", listOf(
            Risposta("Nessun timore: ce la giochiamo.", 2, 2, -1),
            Risposta("Rispetto per loro, ma abbiamo i nostri punti di forza.", 1, 1, 1),
            Risposta("Sulla carta siamo sfavoriti.", -2, -1, 0))),
        Domanda(6, "Come sta il rapporto con la dirigenza?", listOf(
            Risposta("Ottimo, lavoriamo nella stessa direzione.", 0, 0, 3),
            Risposta("Ci confrontiamo, ognuno col suo ruolo.", 0, 0, 1),
            Risposta("Servirebbe più sostegno.", 1, 1, -3))),
        Domanda(7, "Un giovane del vivaio si sta mettendo in luce. Lo vedremo in campo?", listOf(
            Risposta("Se continua così, avrà la sua occasione.", 1, 2, 0),
            Risposta("È ancora presto, deve crescere.", -1, 0, 1),
            Risposta("Il campo premia chi se lo merita.", 1, 1, 0)))
    )

    /** Domanda della conferenza: cambia a ogni giornata (e con il seme). */
    fun domanda(seme: Int): Domanda = DOMANDE[Math.floorMod(seme, DOMANDE.size)]

    // ---- Colloqui individuali ----
    enum class Tono(val nome: String, val descrizione: String) {
        ELOGIO("Elogio", "Funziona meglio con chi ha il morale basso."),
        RASSICURA("Rassicura", "Aiuta chi è giù o chi gioca poco."),
        RIMPROVERO("Rimprovero", "Scuote chi si sente troppo sicuro; fa male a chi è già giù.")
    }

    /** Variazione di morale dopo un colloquio. Casuale in piccola parte, ma prevedibile nei segni. */
    fun colloquio(p: Player, tono: Tono, rng: Random): Int {
        val m = p.morale
        val base = when (tono) {
            Tono.ELOGIO -> if (m < 50) 8 else if (m < 80) 5 else 2
            Tono.RASSICURA -> if (m < 40) 10 else if (m < 60) 6 else 1
            Tono.RIMPROVERO -> if (m > 80) 3 else if (m > 55) 0 else -7
        }
        return base + rng.nextInt(-1, 2)
    }

    /** Un giocatore "ha un problema" quando il morale è molto basso. */
    fun haProblemi(p: Player) = p.morale < 40

    // ---- Psicologo ----
    val PSICOLOGO = listOf("Nessuno", "Psicologo junior", "Psicologo esperto", "Mental coach di fama")
    /** Costo annuo, pagato a fine stagione. */
    fun costoPsicologo(livello: Int): Long = longArrayOf(0L, 300_000L, 700_000L, 1_500_000L)[livello.coerceIn(0, 3)]

    /** Punti di morale recuperati a giornata da chi ha il morale sotto 60; chi sta bene non cambia. */
    fun recuperoPsicologo(livello: Int, p: Player): Int {
        if (livello <= 0 || p.morale >= 60) return 0
        return livello + (if (p.morale < 35) 1 else 0)
    }
}
