package it.androcalcio.engine

import it.androcalcio.data.Player
import kotlin.random.Random

/** Conferenze stampa, colloqui individuali e psicologo: tutto ciò che muove morale, tifosi e fiducia a parole. */
object Relazioni {

    /** [singolo] = morale in più o in meno al solo giocatore della domanda (se c'è). */
    data class Risposta(val testo: String, val morale: Int, val tifosi: Int, val fiducia: Int, val singolo: Int = 0)
    data class Domanda(val id: Int, val testo: String, val risposte: List<Risposta>,
                       val playerId: Long? = null, val testata: String = "", val perche: String = "")

    /** Quello che sa la stampa oggi: serve a fare domande vere, mai su partite che non hai giocato. */
    data class Contesto(
        val squadra: String,
        val ultima: Triple<String, Int, Int>? = null,   // avversario, gol fatti, gol subiti (null = nessuna partita giocata)
        val prossimo: String? = null,
        val divario: Int = 0,                           // forza avversario − nostra (punti media)
        val migliore: Pair<Player, Double>? = null,     // miglior voto dell'ultima partita
        val peggiore: Pair<Player, Double>? = null,     // peggior voto dell'ultima partita
        val giovane: String? = null,                    // giovane del vivaio più promettente
        val posizione: Int? = null, val squadre: Int = 20, val attesa: Int? = null,
        val finestraAperta: Boolean = false,
        val infortunati: List<String> = emptyList(),
        val moraleMedio: Int = 70
    )

    private val TESTATE = listOf("Gazzetta del Pallone", "Corriere della Panchina", "Tuttocalcio Express", "Radio Spogliatoio",
        "Il Fuorigioco Quotidiano", "SportClick 24", "Calcio & Clic", "Il Tifoso Digitale")

    /**
     * Domanda del giorno costruita sui fatti: risultato vero dell'ultima partita, nomi veri dei giocatori,
     * prossimo avversario, classifica, mercato, infermeria. Le domande generiche restano solo come riserva.
     */
    fun domandaDelGiorno(c: Contesto, seme: Int): Domanda {
        val pool = mutableListOf<Domanda>()
        val testata = TESTATE[Math.floorMod(seme * 7, TESTATE.size)]
        c.ultima?.let { (avv, gf, gs) ->
            val esito = when { gf > gs -> "la vittoria"; gf < gs -> "la sconfitta"; else -> "il pareggio" }
            pool += Domanda(100, "Dopo $esito $gf-$gs contro $avv: che giudizio dà alla squadra?", when {
                gf > gs -> listOf(Risposta("Grande prova, ma restiamo con i piedi per terra.", 2, 1, 1),
                    Risposta("Bene, però abbiamo concesso troppo.", 0, 0, 2),
                    Risposta("Siamo i più forti, lo sanno tutti.", 1, 3, -2))
                gf < gs -> listOf(Risposta("La colpa è mia, non dei ragazzi.", 2, 0, -1),
                    Risposta("Così non va: voglio una reazione subito.", -2, 1, 2),
                    Risposta("L'arbitro ha deciso la partita.", 1, 2, -2))
                else -> listOf(Risposta("Un punto che fa morale.", 1, 0, 0),
                    Risposta("Potevamo vincerla, peccato.", 0, 1, 1),
                    Risposta("Pareggio giusto, nient'altro da dire.", 0, -1, 0))
            }, testata = testata, perche = "Ultima partita: $gf-$gs contro $avv")
            c.peggiore?.takeIf { it.second < 5.8 }?.let { (p, v) ->
                pool += Domanda(101, "${p.name} ha preso ${"%.1f".format(v)} in pagella ed è finito nel mirino dei social. Lo difende?", listOf(
                    Risposta("Lo difendo: è un professionista serio.", 0, 0, 0, 6),
                    Risposta("Le critiche fanno crescere, deve reagire.", 0, 0, 1, -1),
                    Risposta("Ha sbagliato e lo sa.", 0, 1, 0, -6)),
                    playerId = p.id, testata = testata, perche = "Voto basso nell'ultima partita")
            }
            c.migliore?.takeIf { it.second >= 7.0 }?.let { (p, v) ->
                pool += Domanda(102, "${p.name} è stato il migliore (${"%.1f".format(v)}). Si parla già di grandi club interessati: lo blinda?", listOf(
                    Risposta("Resta qui, è il cuore di questa squadra.", 0, 2, -1, 5),
                    Risposta("Deve pensare solo a lavorare.", 0, 0, 1, -1),
                    Risposta("Il mercato non lo commento.", 0, -1, 1, 0)),
                    playerId = p.id, testata = testata, perche = "Miglior voto nell'ultima partita")
            }
        }
        c.prossimo?.let { avv ->
            pool += if (c.divario >= 4) Domanda(103, "Arriva il $avv, sulla carta più forte di voi. Teme la sfida?", listOf(
                Risposta("Nessun timore: ce la giochiamo.", 2, 2, -1),
                Risposta("Rispetto per loro, ma abbiamo i nostri punti di forza.", 1, 1, 1),
                Risposta("Siamo sfavoriti, inutile negarlo.", -2, -1, 1)), testata = testata, perche = "Prossimo avversario più forte")
            else if (c.divario <= -4) Domanda(104, "Contro il $avv siete favoriti. Rischio di sottovalutarli?", listOf(
                Risposta("Nessuna partita è facile, massima concentrazione.", 1, 0, 2),
                Risposta("Dobbiamo vincere e basta.", 0, 2, 0),
                Risposta("Faremo turnover per far rifiatare qualcuno.", 1, -1, 0)), testata = testata, perche = "Prossimo avversario più debole")
            else Domanda(105, "Sfida equilibrata con il $avv. Cambierà qualcosa?", listOf(
                Risposta("Squadra che vince non si cambia.", 1, 0, 1),
                Risposta("Ho in mente qualche sorpresa.", 1, 1, 0),
                Risposta("Lo vedrete in campo.", 0, -1, 0)), testata = testata, perche = "Prossimo avversario alla pari")
        }
        if (c.posizione != null && c.attesa != null) {
            pool += if (c.posizione > c.attesa + 3) Domanda(106, "Siete ${c.posizione}° ma la società si aspettava di più. Si sente in discussione?", listOf(
                Risposta("Ho la fiducia della società, ne usciremo.", 1, 0, 1),
                Risposta("Giudicatemi a fine stagione.", 0, -1, 0),
                Risposta("Con questa rosa non si può fare miracoli.", -2, 1, -3)), testata = testata, perche = "Classifica sotto le attese")
            else if (c.posizione < c.attesa - 2) Domanda(107, "Siete ${c.posizione}°, oltre le attese. Si può sognare?", listOf(
                Risposta("Sognare è gratis: ci proviamo.", 2, 3, -1),
                Risposta("Testa bassa, l'obiettivo resta quello.", 1, 0, 2),
                Risposta("È presto per i bilanci.", 0, 0, 1)), testata = testata, perche = "Classifica sopra le attese")
            else Domanda(108, "Classifica in linea con le attese (${c.posizione}°). Soddisfatto?", listOf(
                Risposta("Sì, ma voglio di più.", 1, 1, 1),
                Risposta("Stiamo lavorando bene.", 1, 0, 1),
                Risposta("Mai soddisfatto.", 0, 1, 0)), testata = testata, perche = "Classifica in linea")
        }
        if (c.finestraAperta) pool += DOMANDE[1].copy(testo = "Mercato aperto: i tifosi chiedono rinforzi. Arriverà qualcuno?", testata = testata, perche = "Finestra di mercato aperta")
        if (c.infortunati.size >= 3) pool += Domanda(109, "Infermeria piena (${c.infortunati.take(3).joinToString(", ")}…): colpa della preparazione?", listOf(
            Risposta("Il mio staff è il migliore, è solo sfortuna.", 1, 0, 0),
            Risposta("Faremo verifiche sui carichi di lavoro.", 0, 0, 2),
            Risposta("Giochiamo troppo, il calendario è assurdo.", 1, 1, -1)), testata = testata, perche = "Molti infortunati")
        c.giovane?.let { g ->
            pool += Domanda(110, "Dal vivaio si parla molto di $g. Lo vedremo in prima squadra?", listOf(
                Risposta("Se continua così, avrà la sua occasione.", 1, 2, 0),
                Risposta("È ancora presto, deve crescere.", -1, 0, 1),
                Risposta("Il campo premia chi se lo merita.", 1, 1, 0)), testata = testata, perche = "Giovane promettente nel vivaio")
        }
        if (c.moraleMedio < 50) pool += DOMANDE[2].copy(testata = testata, perche = "Morale della squadra basso")
        if (c.ultima == null) pool += DOMANDE[3].copy(testo = "Stagione al via per il ${c.squadra}: qual è l'obiettivo?", testata = testata, perche = "Inizio stagione")
        if (pool.isEmpty()) pool += DOMANDE[6].copy(testata = testata)
        return pool[Math.floorMod(seme, pool.size)]
    }

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
