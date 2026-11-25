package it.androcalcio.engine

import it.androcalcio.data.Player

/**
 * Tattica a due fasi, come in Football Manager: istruzioni IN POSSESSO e NON IN POSSESSO, 11 stili che le impostano
 * in un colpo e un ruolo diverso per fase. Pressing, linea, ampiezza, ritmo, fasce e schema dei piazzati restano
 * le impostazioni che c'erano già (le scrive lo stile); qui ci sono quelle nuove.
 * Tutti gli effetti sono piccoli (pochi punti percento) e valgono solo per la squadra dell'utente: le squadre AI non cambiano.
 */
enum class Passaggi(val label: String, val livello: Int) {
    MOLTO_CORTI("Molto corti", -2), CORTI("Corti", -1), STANDARD("Standard", 0), DIRETTI("Diretti", 1), MOLTO_DIRETTI("Molto diretti", 2)
}

enum class Creativita(val label: String, val livello: Int) {
    DISCIPLINATO("Disciplinato", -1), EQUILIBRATO("Equilibrata", 0), CREATIVO("Creativa", 1)
}

enum class Transizione(val label: String) { MANTIENI("Mantieni assetto"), CONTROPIEDE("Contropiede") }

/** Come riparte il portiere: dai centrali (corta), mista o con rinvii lunghi. */
enum class CostruzionePortiere(val label: String, val livello: Int) {
    CORTA("Dai centrali", -1), MISTA("Mista", 0), LUNGA("Rinvii lunghi", 1)
}

enum class FrequenzaPressing(val label: String, val livello: Int) {
    MENO("Meno frequente", -1), NORMALE("Normale", 0), PIU("Più frequente", 1)
}

enum class Contrasti(val label: String, val livello: Int) {
    PRUDENTI("Senza azzardi", -1), NORMALI("Normali", 0), AGGRESSIVI("Aggressivi", 1)
}

enum class Intensita(val label: String) { BASSA("Bassa intensità"), MEDIA("Intensità media"), ALTA("Alta intensità"), MOLTO_ALTA("Intensità molto alta") }
enum class Complessita(val label: String) { BASSA("Bassa complessità"), MEDIA("Media complessità"), ALTA("Alta complessità") }

/** Moltiplicatori che un piano dà alla squadra: possesso, attacco, difesa e consumo di energia. */
data class Effetti(val poss: Double = 1.0, val atk: Double = 1.0, val def: Double = 1.0, val drain: Double = 1.0) {
    companion object { val NEUTRO = Effetti() }
}

/** Le istruzioni di squadra nuove (le altre sono già in [Pressing], [DefLine], [Ampiezza], [Ritmo]) e i ruoli senza palla. */
data class PianoTattico(
    val stile: StileTattico? = null,
    // In possesso
    val passaggi: Passaggi = Passaggi.STANDARD,
    val creativita: Creativita = Creativita.EQUILIBRATO,
    val transizione: Transizione = Transizione.MANTIENI,
    val costruzione: CostruzionePortiere = CostruzionePortiere.MISTA,
    // Non in possesso
    val frequenza: FrequenzaPressing = FrequenzaPressing.NORMALE,
    val contrasti: Contrasti = Contrasti.NORMALI,
    val contropressing: Boolean = false,
    val bloccaPortiere: Boolean = false,
    /** Ritmo di partenza della partita (in partita lo cambi con il comando Ritmo). */
    val ritmo: Ritmo = Ritmo.NORMALE,
    /** Ruolo senza palla di un giocatore (id → codice ruolo, uno dei suoi ruoli noti). Assente = lo stesso della fase di possesso. */
    val ruoliNonPoss: Map<Long, String> = emptyMap()
) {
    /** Moltiplicatori del piano per gli undici in campo. */
    fun effetti(xi: List<Player>): Effetti {
        var poss = 1.0; var atk = 1.0; var def = 1.0; var drain = 1.0
        poss *= 1 - 0.012 * passaggi.livello; atk *= 1 + 0.006 * passaggi.livello
        atk *= 1 + 0.010 * creativita.livello; poss *= 1 - 0.005 * creativita.livello
        poss *= 1 - 0.006 * costruzione.livello
        poss *= 1 + 0.008 * frequenza.livello; drain *= 1 + 0.06 * frequenza.livello
        def *= 1 + 0.012 * contrasti.livello; drain *= 1 + 0.03 * contrasti.livello
        if (contropressing) { poss *= 1.006; drain *= 1.04 }
        if (bloccaPortiere) poss *= 1.004
        stile?.let { atk *= 1 + 0.04 * (it.idoneita(xi) - 50) / 50.0 }
        def *= fattoreRuoliSenzaPalla(xi)
        return Effetti(poss, atk, def, drain)
    }

    /**
     * Media di (affinità nel ruolo senza palla / affinità nel ruolo con palla) dei giocatori di movimento, tra 0,92 e 1,08.
     * Senza ruoli scelti vale 1: un giocatore più a suo agio senza palla che con la palla rende di più in difesa.
     */
    internal fun fattoreRuoliSenzaPalla(xi: List<Player>): Double {
        if (ruoliNonPoss.isEmpty()) return 1.0
        val v = xi.filter { it.role != "P" }.map { p ->
            val r = ruoliNonPoss[p.id]
            if (r == null) 1.0 else affinita(p, r) / TacticalRoles.factor(p).coerceAtLeast(0.3)
        }
        return if (v.isEmpty()) 1.0 else v.average().coerceIn(0.92, 1.08)
    }

    fun conRuoloSenzaPalla(id: Long, ruolo: String?): PianoTattico =
        copy(ruoliNonPoss = if (ruolo == null) ruoliNonPoss - id else ruoliNonPoss + (id to ruolo))

    fun codifica(): String = listOf(
        stile?.name ?: "-", passaggi.name, creativita.name, transizione.name, costruzione.name,
        frequenza.name, contrasti.name, contropressing.toString(), bloccaPortiere.toString(),
        ruoliNonPoss.entries.joinToString(";") { "${it.key}:${it.value}" }, ritmo.name
    ).joinToString("|")

    companion object {
        /** Affinità (0..1) di un giocatore in un ruolo: 1 il naturale, la sua % negli affini, 0,5 in un ruolo che non conosce. */
        fun affinita(p: Player, ruolo: String): Double = when {
            ruolo == p.pos -> 1.0
            ruolo == p.sec2 -> p.aff2 / 100.0
            ruolo == p.sec3 -> p.aff3 / 100.0
            ruolo == p.sec4 -> p.aff4 / 100.0
            else -> 0.5
        }

        private inline fun <reified E : Enum<E>> leggi(s: String?, def: E): E =
            enumValues<E>().firstOrNull { it.name == s } ?: def

        fun decodifica(s: String?): PianoTattico {
            if (s.isNullOrBlank()) return PianoTattico()
            val p = s.split("|")
            if (p.size < 9) return PianoTattico()
            val ruoli = p.getOrNull(9).orEmpty().split(";").mapNotNull {
                val kv = it.split(":")
                val id = kv.getOrNull(0)?.toLongOrNull()
                val r = kv.getOrNull(1)
                if (id != null && !r.isNullOrEmpty()) id to r else null
            }.toMap()
            return PianoTattico(
                stile = StileTattico.values().firstOrNull { it.name == p[0] },
                passaggi = leggi(p[1], Passaggi.STANDARD), creativita = leggi(p[2], Creativita.EQUILIBRATO),
                transizione = leggi(p[3], Transizione.MANTIENI), costruzione = leggi(p[4], CostruzionePortiere.MISTA),
                frequenza = leggi(p[5], FrequenzaPressing.NORMALE), contrasti = leggi(p[6], Contrasti.NORMALI),
                contropressing = p[7] == "true", bloccaPortiere = p[8] == "true", ruoliNonPoss = ruoli,
                ritmo = leggi(p.getOrNull(10), Ritmo.NORMALE)
            )
        }
    }
}

/**
 * Gli 11 stili del gioco (primo passaggio di "Crea nuova tattica" in FM27). Ogni stile imposta un pacchetto di istruzioni
 * con la palla e senza, ma non lega le mani: dopo si può cambiare tutto. Pregi e rischi sono quelli dichiarati dallo staff.
 */
enum class StileTattico(
    val nome: String,
    val sintesi: String,
    val intensita: Intensita?,
    val complessita: Complessita?,
    val pregi: List<String>,
    val rischi: List<String>,
    val piano: PianoTattico,
    val pressing: Pressing, val linea: DefLine, val ampiezza: Ampiezza, val ritmo: Ritmo,
    val fasce: Fasce = Fasce.MISTO, val schema: SchemaPiazzati = SchemaPiazzati.NORMALE
) {
    CONTROLLO_POSSESSO(
        "Controllo del possesso", "Tenere il pallone e riconquistarlo in fretta, con pazienza e passaggi corti.",
        Intensita.ALTA, Complessita.MEDIA,
        listOf("Possesso", "Iniziativa"), listOf("Uscire palla a terra dalla difesa può costare caro", "Attacchi più lenti"),
        PianoTattico(passaggi = Passaggi.CORTI, costruzione = CostruzionePortiere.CORTA, frequenza = FrequenzaPressing.PIU,
            contropressing = true, bloccaPortiere = true),
        Pressing.ALTO, DefLine.ALTA, Ampiezza.NORMALE, Ritmo.LENTO
    ),
    TIKI_TAKA(
        "Tiki-Taka", "La versione estrema del Controllo del possesso: pressing intenso e attacco molto paziente.",
        Intensita.ALTA, Complessita.ALTA,
        listOf("Dominio del possesso", "Palloni recuperati in alto"), listOf("Attacchi lenti", "Servono qualità tecniche molto elevate"),
        PianoTattico(passaggi = Passaggi.MOLTO_CORTI, costruzione = CostruzionePortiere.CORTA, frequenza = FrequenzaPressing.PIU,
            contropressing = true, bloccaPortiere = true),
        Pressing.ALTO, DefLine.ALTA, Ampiezza.NORMALE, Ritmo.LENTO
    ),
    TIKI_TAKA_VERTICALE(
        "Tiki-Taka verticale", "Stesso pressing e pazienza del Tiki-Taka, ma squadra stretta per uno-due rapidi.",
        Intensita.ALTA, Complessita.ALTA,
        listOf("Possesso", "Più combinazioni"), listOf("L'attacco può intasarsi negli spazi stretti", "Servono qualità tecniche molto elevate"),
        PianoTattico(passaggi = Passaggi.CORTI, creativita = Creativita.CREATIVO, transizione = Transizione.CONTROPIEDE,
            costruzione = CostruzionePortiere.CORTA, frequenza = FrequenzaPressing.PIU, contropressing = true, bloccaPortiere = true),
        Pressing.ALTO, DefLine.ALTA, Ampiezza.STRETTA, Ritmo.LENTO
    ),
    GEGENPRESS(
        "Gegenpress", "Tutti lavorano e appena si perde palla si pressa subito l'avversario.",
        Intensita.MOLTO_ALTA, Complessita.ALTA,
        listOf("Palloni conquistati in zone avanzate", "Avversario destabilizzato dall'intensità"), listOf("Molto faticoso", "Meno controllo del gioco"),
        PianoTattico(transizione = Transizione.CONTROPIEDE, costruzione = CostruzionePortiere.CORTA,
            frequenza = FrequenzaPressing.PIU, contropressing = true, bloccaPortiere = true),
        Pressing.ALTO, DefLine.ALTA, Ampiezza.STRETTA, Ritmo.VELOCE
    ),
    GIOCO_SULLE_FASCE(
        "Gioco sulle fasce", "Tutto per arrivare al cross nelle condizioni migliori, con sovrapposizioni frequenti.",
        Intensita.ALTA, Complessita.BASSA,
        listOf("Evita le zone centrali intasate", "Il cross diventa una fonte affidabile di occasioni"), listOf("Più palloni persi in attacco", "Poco efficace contro difese forti di testa"),
        PianoTattico(passaggi = Passaggi.DIRETTI, transizione = Transizione.CONTROPIEDE, frequenza = FrequenzaPressing.PIU),
        Pressing.MEDIO, DefLine.MEDIA, Ampiezza.LARGA, Ritmo.VELOCE, fasce = Fasce.LATERALE
    ),
    PALLA_LUNGA(
        "Palla lunga", "Portare il pallone nella metà campo avversaria il prima possibile, con lanci verso gli attaccanti.",
        Intensita.ALTA, Complessita.BASSA,
        listOf("Meno palle perse vicino alla propria porta", "Pressione immediata sulla difesa avversaria"), listOf("Meno possesso", "Dipende dalla superiorità fisica davanti"),
        PianoTattico(passaggi = Passaggi.MOLTO_DIRETTI, creativita = Creativita.DISCIPLINATO, transizione = Transizione.CONTROPIEDE,
            costruzione = CostruzionePortiere.LUNGA),
        Pressing.MEDIO, DefLine.MEDIA, Ampiezza.NORMALE, Ritmo.VELOCE, schema = SchemaPiazzati.CROSS
    ),
    CONTROPIEDE_FLUIDO(
        "Contropiede fluido", "In ripartenza i giocatori sono creativi: combinano tra loro e giocano tra le linee.",
        Intensita.ALTA, Complessita.MEDIA,
        listOf("Espone l'avversario", "Contropiede imprevedibile"), listOf("Rende meno contro chi si chiude", "Concede il possesso"),
        PianoTattico(passaggi = Passaggi.CORTI, transizione = Transizione.CONTROPIEDE, frequenza = FrequenzaPressing.PIU,
            contrasti = Contrasti.AGGRESSIVI, contropressing = true),
        Pressing.MEDIO, DefLine.BASSA, Ampiezza.NORMALE, Ritmo.VELOCE
    ),
    CONTROPIEDE_DIRETTO(
        "Contropiede diretto", "Qui conta la velocità: il pallone va portato avanti subito, contro una difesa fuori posizione.",
        Intensita.ALTA, Complessita.BASSA,
        listOf("Espone l'avversario con attacchi veloci e diretti"), listOf("Rende meno contro chi si chiude", "Concede il possesso"),
        PianoTattico(passaggi = Passaggi.DIRETTI, transizione = Transizione.CONTROPIEDE, frequenza = FrequenzaPressing.PIU,
            contrasti = Contrasti.AGGRESSIVI),
        Pressing.MEDIO, DefLine.BASSA, Ampiezza.NORMALE, Ritmo.VELOCE
    ),
    CATENACCIO(
        "Catenaccio", "Il primo obiettivo è negare all'avversario le occasioni. Si difende con 3 centrali, per più copertura.",
        Intensita.MEDIA, Complessita.MEDIA,
        listOf("Riduce al minimo lo spazio per attaccare", "Espone poco ai contropiedi"), listOf("Meno occasioni", "Molto meno possesso"),
        PianoTattico(passaggi = Passaggi.DIRETTI, creativita = Creativita.DISCIPLINATO, transizione = Transizione.CONTROPIEDE,
            frequenza = FrequenzaPressing.MENO, contrasti = Contrasti.PRUDENTI),
        Pressing.BASSO, DefLine.BASSA, Ampiezza.STRETTA, Ritmo.LENTO, schema = SchemaPiazzati.CROSS
    ),
    PARCHEGGIA_AUTOBUS(
        "Parcheggia l'autobus", "Si rinuncia a gran parte del possesso e si cercano gol in contropiede e sui calci piazzati.",
        Intensita.BASSA, Complessita.MEDIA,
        listOf("Riduce al minimo lo spazio per attaccare", "Espone poco ai contropiedi"), listOf("Meno occasioni", "Molto meno possesso"),
        PianoTattico(passaggi = Passaggi.DIRETTI, creativita = Creativita.DISCIPLINATO, transizione = Transizione.CONTROPIEDE,
            costruzione = CostruzionePortiere.LUNGA, frequenza = FrequenzaPressing.MENO, contrasti = Contrasti.AGGRESSIVI),
        Pressing.BASSO, DefLine.BASSA, Ampiezza.STRETTA, Ritmo.LENTO, schema = SchemaPiazzati.CROSS
    ),
    TABULA_RASA(
        "Tabula rasa", "Nessuna etichetta e nessuna istruzione: si parte da zero. Consigliata solo ad allenatori esperti.",
        null, null, emptyList(), emptyList(), PianoTattico(),
        Pressing.MEDIO, DefLine.MEDIA, Ampiezza.NORMALE, Ritmo.NORMALE
    );

    /** Piano di questo stile con lo stile segnato (i ruoli senza palla non si toccano: vedi [PianoTattico.conRuoloSenzaPalla]). */
    fun pianoCompleto(ruoliNonPoss: Map<Long, String> = emptyMap()): PianoTattico = piano.copy(stile = this, ritmo = ritmo, ruoliNonPoss = ruoliNonPoss)

    /** Istruzioni in possesso che lo stile imposta, a parole, per la scheda dello stile. */
    fun istruzioniInPossesso(): List<String> = if (this == TABULA_RASA) emptyList() else buildList {
        if (piano.passaggi != Passaggi.STANDARD) add("Passaggi: ${piano.passaggi.label.lowercase()}")
        add("Ritmo: ${ritmo.label.lowercase()}")
        add("Ampiezza: ${ampiezza.label.lowercase()}")
        if (piano.creativita != Creativita.EQUILIBRATO) add("Libertà di inventare: ${piano.creativita.label.lowercase()}")
        if (piano.transizione == Transizione.CONTROPIEDE) add("Transizione offensiva: contropiede")
        if (piano.costruzione != CostruzionePortiere.MISTA) add("Portiere: ${piano.costruzione.label.lowercase()}")
        if (fasce != Fasce.MISTO) add("Gioco: ${fasce.label.lowercase()}")
        if (schema != SchemaPiazzati.NORMALE) add("Calci piazzati: ${schema.label.lowercase()}")
    }

    /** Istruzioni senza palla che lo stile imposta, a parole. */
    fun istruzioniSenzaPalla(): List<String> = if (this == TABULA_RASA) emptyList() else buildList {
        add("Linea difensiva: ${linea.label.lowercase()}")
        add("Pressing: ${pressing.label.lowercase()}")
        if (piano.frequenza != FrequenzaPressing.NORMALE) add("Primo pressing: ${piano.frequenza.label.lowercase()}")
        if (piano.contrasti != Contrasti.NORMALI) add("Contrasti: ${piano.contrasti.label.lowercase()}")
        if (piano.contropressing) add("Contropressing")
        if (piano.bloccaPortiere) add("Il portiere avversario non costruisce dal basso")
    }

    /**
     * Quanto la rosa si adatta allo stile, da 0 a 100 (50 = nella media): è l'"Efficacia" della schermata.
     * Tiki-Taka e Controllo del possesso vogliono tecnica, il Gegenpress fiato e corsa, la Palla lunga attaccanti forti
     * e resistenti, i contropiedi velocità, il Catenaccio difensori che sanno entrare.
     */
    fun idoneita(xi: List<Player>): Int {
        val gioc = xi.filter { it.role != "P" }
        if (gioc.isEmpty() || this == TABULA_RASA) return 50
        fun m(ps: List<Player>, f: (Player) -> Int): Double = ps.ifEmpty { gioc }.map(f).average()
        val media = when (this) {
            CONTROLLO_POSSESSO, TIKI_TAKA, TIKI_TAKA_VERTICALE -> m(gioc) { (it.pa + it.qu + it.dr) / 3 }
            GEGENPRESS -> m(gioc) { (it.re + it.ve + it.ag) / 3 }
            GIOCO_SULLE_FASCE -> m(gioc.filter { TacticalRoles.assigned(it) in LATERALI }) { (it.ve + it.qu) / 2 }
            PALLA_LUNGA -> m(gioc.filter { it.role == "A" }) { (it.ag + it.re) / 2 }
            CONTROPIEDE_FLUIDO, CONTROPIEDE_DIRETTO -> m(gioc) { (it.ve + it.re) / 2 }
            CATENACCIO, PARCHEGGIA_AUTOBUS -> m(gioc.filter { it.role == "D" }) { (it.en + it.re) / 2 }
            TABULA_RASA -> 60.0
        }
        return ((media - 45.0) / 30.0 * 100.0).toInt().coerceIn(0, 100)
    }

    companion object {
        private val LATERALI = setOf("TD", "TS", "ES", "FD", "FS", "AD", "AS")
    }
}

/** Valutazioni dei ruoli per la scelta ruolo: stelle da 0,5 a 5 dall'affinità, e attributi chiave di ogni ruolo. */
object RuoliFm {
    /** Stelle (con mezze) dall'affinità 0..100 e dalla media degli attributi chiave. */
    fun stelle(p: Player, ruolo: String): Double {
        val aff = PianoTattico.affinita(p, ruolo)
        val q = attributiChiave(ruolo).map { it.second(p) }.average().let { if (it.isNaN()) 50.0 else it }
        val punteggio = (q / 99.0) * 5.0 * aff
        return (Math.round(punteggio * 2) / 2.0).coerceIn(0.5, 5.0)
    }

    /** Attributi chiave del ruolo: nome e lettura dal giocatore (scala 1..99). */
    fun attributiChiave(ruolo: String): List<Pair<String, (Player) -> Int>> = when (ruolo) {
        "POR" -> listOf("Gioco di mani" to { p: Player -> p.gm }, "Passaggio" to { p: Player -> p.pa }, "Posizione" to { p: Player -> p.qu })
        "DC", "CS", "CD" -> listOf("Entrate" to { p: Player -> p.en }, "Aggressività" to { p: Player -> p.ag }, "Velocità" to { p: Player -> p.ve }, "Qualità" to { p: Player -> p.qu })
        "TD", "TS" -> listOf("Entrate" to { p: Player -> p.en }, "Velocità" to { p: Player -> p.ve }, "Resistenza" to { p: Player -> p.re }, "Passaggio" to { p: Player -> p.pa })
        "MED" -> listOf("Entrate" to { p: Player -> p.en }, "Passaggio" to { p: Player -> p.pa }, "Resistenza" to { p: Player -> p.re }, "Aggressività" to { p: Player -> p.ag })
        "REG" -> listOf("Passaggio" to { p: Player -> p.pa }, "Qualità" to { p: Player -> p.qu }, "Dribbling" to { p: Player -> p.dr })
        "INT" -> listOf("Passaggio" to { p: Player -> p.pa }, "Resistenza" to { p: Player -> p.re }, "Qualità" to { p: Player -> p.qu }, "Tiro" to { p: Player -> p.ti })
        "MP" -> listOf("Passaggio" to { p: Player -> p.pa }, "Dribbling" to { p: Player -> p.dr }, "Qualità" to { p: Player -> p.qu }, "Tiro" to { p: Player -> p.ti })
        "FD", "FS" -> listOf("Resistenza" to { p: Player -> p.re }, "Velocità" to { p: Player -> p.ve }, "Passaggio" to { p: Player -> p.pa }, "Dribbling" to { p: Player -> p.dr })
        "ES", "AD", "AS" -> listOf("Velocità" to { p: Player -> p.ve }, "Dribbling" to { p: Player -> p.dr }, "Qualità" to { p: Player -> p.qu }, "Finalizzazione" to { p: Player -> p.fi })
        "CA" -> listOf("Finalizzazione" to { p: Player -> p.fi }, "Tiro" to { p: Player -> p.ti }, "Aggressività" to { p: Player -> p.ag }, "Qualità" to { p: Player -> p.qu })
        else -> listOf("Qualità" to { p: Player -> p.qu })
    }
}
