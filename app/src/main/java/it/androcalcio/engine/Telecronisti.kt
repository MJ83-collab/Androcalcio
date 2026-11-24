package it.androcalcio.engine

import kotlin.random.Random

/**
 * Telecronisti di fantasia: nomi inventati, ognuno con una parlata riconoscibile ispirata a un "tipo" di commento
 * (mai nomi o frasi di persone reali). A ogni partita si estrae a caso una prima voce e una seconda voce.
 */
enum class Voce(val nome: String, val prima: Boolean, val tipo: String) {
    FERRANDI("Ottavio Ferrandi", true, "il veterano sobrio"),
    STREPPONI("Gianluca Strepponi", true, "l'urlatore"),
    BUCCHETTI("Arnaldo Bucchetti", true, "l'immaginifico"),
    LANFRANCHI("Severino Lanfranchi", true, "l'enciclopedico"),
    CONTRAFATTI("Ruggero Contrafatti", false, "il poeta tattico"),
    SBARAGLIA("Totò Sbaraglia", false, "l'ex attaccante schietto"),
    PERCENTINI("Dario Percentini", false, "lo statistico"),
    MARCOLIN("Walter Marcolin", false, "l'ex difensore tecnico"),
    SPUNTAZZO("Ciro Spuntazzo", false, "l'ironico")
}

/** Coppia della partita: chi racconta e chi commenta. */
data class Coppia(val prima: Voce, val seconda: Voce) {
    companion object {
        fun sorteggia(rng: Random): Coppia = Coppia(
            Voce.values().filter { it.prima }.let { it[rng.nextInt(it.size)] },
            Voce.values().filter { !it.prima }.let { it[rng.nextInt(it.size)] })
    }
    fun presentazione(): String =
        "In telecronaca ${prima.nome}, ${prima.tipo}; al commento ${seconda.nome}, ${seconda.tipo}."
}

object Voci {
    private fun <T> Random.uno(l: List<T>): T = l[nextInt(l.size)]

    /** Reazione della prima voce a un episodio; [g] è il cognome del protagonista, se c'è. */
    fun reazione(v: Voce, e: MatchEvent, nome: String?, rng: Random): String? {
        val g = nome ?: "lui"
        val m = e.minute
        return when (e.type) {
            EventType.GOAL -> when (v) {
                Voce.FERRANDI -> rng.uno(listOf("Rete. $g.", "$g. È gol.", "Segna $g. Semplice, pulito.", "Gol di $g. Bene così."))
                Voce.STREPPONI -> rng.uno(listOf("GOOOL! $g! $g! $g! Ma cosa ha fatto?!", "Ce l'haaa fatta! $g! Incredibile, amici, incredibile!",
                    "Rete! Rete! Rete! È di $g! Non ci posso credere!"))
                Voce.BUCCHETTI -> rng.uno(listOf("Una staffilata di $g, una sassata che gonfia la rete come una vela!",
                    "Bordata di $g! Il pallone si infila là dove nemmeno i ragni osano tessere!", "Saetta di $g, una carezza velenosa: gol!"))
                Voce.LANFRANCHI -> rng.uno(listOf("Gol di $g al minuto $m. Curiosità: in questo stadio i gol al $m° portano bene da sempre.",
                    "Segna $g. Ricordo una rete identica, stessa porta, tanti anni fa: il calcio è memoria.",
                    "Rete di $g: secondo i miei appunti è uno dei gol più belli visti qui da molte stagioni."))
                else -> null
            }
            EventType.SAVE -> when (v) {
                Voce.FERRANDI -> rng.uno(listOf("Para il portiere. Bene.", "Respinta. Attento."))
                Voce.STREPPONI -> rng.uno(listOf("NOOO! Ma come ha fatto a pararla?! Miracolo!", "Che parata! Che parata, amici!"))
                Voce.BUCCHETTI -> rng.uno(listOf("Il portiere vola come un airone e toglie il pallone dall'incrocio!", "Mano di velluto, il portiere chiude la saracinesca!"))
                Voce.LANFRANCHI -> rng.uno(listOf("Grande intervento: i portieri di questa scuola hanno sempre avuto riflessi così.", "Parata d'istinto, da manuale d'altri tempi."))
                else -> null
            }
            EventType.MISS -> when (v) {
                Voce.FERRANDI -> rng.uno(listOf("Fuori. Peccato.", "Alto. Niente."))
                Voce.STREPPONI -> rng.uno(listOf("Ahhh! Sul fondo! Ci credevamo tutti!", "Nooo, di un niente! Di un niente!"))
                Voce.BUCCHETTI -> rng.uno(listOf("Una fucilata che sibila accanto al palo!", "Il pallone accarezza il palo e se ne va, beffardo."))
                Voce.LANFRANCHI -> rng.uno(listOf("Occasione sprecata: capita anche ai migliori, la storia è piena di errori così.", "Fuori di poco: a quella distanza si segna una volta su tre."))
                else -> null
            }
            EventType.OFFSIDE -> when (v) {
                Voce.FERRANDI -> "Fuorigioco."
                Voce.STREPPONI -> "Fuorigioco! Bandierina alta! Si riparte!"
                Voce.BUCCHETTI -> "La trappola scatta come una tagliola: fuorigioco!"
                Voce.LANFRANCHI -> "Fuorigioco: la regola è cambiata tante volte, ma questo era netto."
                else -> null
            }
            EventType.RED -> when (v) {
                Voce.FERRANDI -> "Espulsione. Partita cambiata."
                Voce.STREPPONI -> "Rosso! Rosso diretto! Ora cambia tutto!"
                Voce.BUCCHETTI -> "Cartellino rosso, una lama che taglia in due la partita!"
                Voce.LANFRANCHI -> "Espulsione: in queste sfide i nervi tesi hanno sempre fatto danni."
                else -> null
            }
            else -> null
        }
    }

    /** Battuta della seconda voce dopo un episodio. */
    fun replica(v: Voce, e: MatchEvent, nome: String?, rng: Random): String? {
        val g = nome ?: "il ragazzo"
        val gol = e.type == EventType.GOAL
        return when (v) {
            Voce.CONTRAFATTI -> if (gol) rng.uno(listOf("Guardate la diagonale che apre lo spazio: è un'idea che diventa geometria, e $g la firma.",
                "Tutto nasce da una pausa, un respiro: poi la verticalità, e $g è dove il calcio voleva che fosse."))
                else rng.uno(listOf("C'è una tensione bellissima tra le linee, si sente.", "È un calcio che respira, che cerca la profondità."))
            Voce.SBARAGLIA -> if (gol) rng.uno(listOf("Mamma mia, $g! Questa la facevo io da ragazzino, eh, ma bravo!",
                "Ma che ve devo dì? $g c'ha il piede, punto. Il resto so' chiacchiere."))
                else rng.uno(listOf("Ma dai, così non si può, ragazzi. Serve cattiveria!", "Io l'avrei messa dentro, ve lo dico."))
            Voce.PERCENTINI -> if (gol) rng.uno(listOf("Dato: $g segna con il ${40 + rng.nextInt(45)}% di probabilità stimata. Freddezza.",
                "Al minuto ${e.minute}: è il ${1 + rng.nextInt(3)}° gol stagionale da questa posizione per $g."))
                else rng.uno(listOf("Statistica: in queste situazioni si segna una volta su cinque.", "Numeri alla mano, la squadra sta crescendo."))
            Voce.MARCOLIN -> if (gol) rng.uno(listOf("Guardate la linea: il centrale non scala, il terzino non stringe e $g ringrazia.",
                "Errore di posizionamento dietro: da ex difensore vi dico che lì devi accorciare."))
                else rng.uno(listOf("Bravo il difensore a temporeggiare, non bisogna mai andare giù.", "Le distanze tra i reparti sono buone."))
            Voce.SPUNTAZZO -> if (gol) rng.uno(listOf("Eh, $g ha deciso che oggi si mangia fuori. Offre lui!",
                "Il portiere l'ha vista arrivare come il treno del lunedì: in ritardo."))
                else rng.uno(listOf("Qui ci vorrebbe un miracolo, o almeno un caffè.", "Tiro così storto che l'ha preso la regia."))
            else -> null
        }
    }

    /** La seconda voce racconta l'indizio con la sua parlata. */
    fun dici(v: Voce, frase: String, q: Telecronaca.Quadro, rng: Random): String {
        val f = frase.replaceFirstChar { it.lowercase() }
        val t = when (v) {
            Voce.CONTRAFATTI -> rng.uno(listOf("C'è un disegno sottile qui: $f", "Lo dico con una certa emozione: $f")) +
                rng.uno(listOf(" È calcio che diventa geometria.", " Pensiero, poi movimento.", ""))
            Voce.SBARAGLIA -> rng.uno(listOf("Ma oh, ragazzi: $f", "Ve lo dico da ex: $f", "Sentite a me: $f")) +
                rng.uno(listOf(" Ce vo' coraggio!", " Punto.", " Io avrei già cambiato tutto."))
            Voce.PERCENTINI -> "Possesso ${q.possessoMio}%, tiri ${q.tiriMiei} a ${q.tiriLoro}. " + rng.uno(listOf("Lettura: $f", "I numeri dicono questo: $f"))
            Voce.MARCOLIN -> rng.uno(listOf("Guardate i movimenti: $f", "Da dietro si vede bene: $f", "Questione di posizioni: $f"))
            Voce.SPUNTAZZO -> rng.uno(listOf("Allora, senza offesa: $f", "Ditelo all'allenatore, io non mi permetto: $f")) +
                rng.uno(listOf(" Altrimenti si va tutti a casa presto.", " Sennò stasera i tifosi non dormono.", ""))
            else -> frase
        }
        return "${v.nome}: $t"
    }
}
