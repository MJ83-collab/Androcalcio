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

/**
 * Istruzioni per singolo giocatore, come in Football Manager. Quelle dello stesso gruppo si escludono.
 * Effetti piccoli, sommati su tutti gli undici: un'istruzione su un giocatore sposta la squadra di pochi decimi di punto.
 */
enum class IstrGiocatore(val label: String, val gruppo: String, val descrizione: String) {
    TIRA_DI_PIU("Tira di più", "tiro", "Cerca la conclusione più spesso."),
    TIRA_DI_MENO("Tira di meno", "tiro", "Tira solo se è libero, preferisce servire un compagno."),
    DRIBBLA_DI_PIU("Dribbla di più", "dribbling", "Punta l'uomo: rende con un buon dribbling, ma perde qualche pallone."),
    DRIBBLA_DI_MENO("Dribbla di meno", "dribbling", "Gioca semplice e tiene il possesso."),
    PASSAGGI_RISCHIOSI("Passaggi rischiosi", "passaggi", "Cerca la giocata decisiva: rende con un buon passaggio."),
    PASSAGGI_SICURI("Passaggi sicuri", "passaggi", "Passaggio facile al compagno vicino."),
    PIU_SORTITE("Più sortite in avanti", "sortite", "Si inserisce spesso: più attacco, meno copertura, più fatica."),
    RESTA_IN_POSIZIONE("Resta in posizione", "sortite", "Non lascia la sua zona: più copertura, meno attacco."),
    PRESSA_DI_PIU("Pressa di più", "pressing", "Aggredisce il portatore: più palloni recuperati, più fatica."),
    PRESSA_DI_MENO("Pressa di meno", "pressing", "Aspetta nella sua zona e risparmia energie."),
    CONTRASTI_DURI("Contrasti duri", "contrasti", "Entra deciso: difesa più solida, un po' più di fatica.");

    companion object {
        val GRUPPI = values().map { it.gruppo }.distinct()
    }
}

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
    val ruoliNonPoss: Map<Long, String> = emptyMap(),
    /** Istruzioni per singolo giocatore (id → istruzioni). */
    val istrGioc: Map<Long, Set<IstrGiocatore>> = emptyMap()
) {
    /** Moltiplicatori del piano per gli undici in campo. */
    fun effetti(xi: List<Player>): Effetti {
        var poss = 1.0; var atk = 1.0; var def = 1.0; var drain = 1.0
        poss *= 1 - 0.015 * passaggi.livello; atk *= 1 + 0.008 * passaggi.livello
        atk *= 1 + 0.013 * creativita.livello; poss *= 1 - 0.006 * creativita.livello
        poss *= 1 - 0.008 * costruzione.livello
        poss *= 1 + 0.010 * frequenza.livello; drain *= 1 + 0.06 * frequenza.livello
        def *= 1 + 0.015 * contrasti.livello; drain *= 1 + 0.03 * contrasti.livello
        if (contropressing) { poss *= 1.008; drain *= 1.04 }
        if (bloccaPortiere) poss *= 1.005
        stile?.let { atk *= 1 + 0.05 * (it.idoneita(xi) - 50) / 50.0 }
        def *= fattoreRuoliSenzaPalla(xi)
        val g = effettiGiocatori(xi)
        return Effetti(poss * g.poss, atk * g.atk, def * g.def, drain * g.drain)
    }

    /** Somma delle istruzioni dei singoli giocatori in campo, limitata a ±5% (fatica ±10%). */
    internal fun effettiGiocatori(xi: List<Player>): Effetti {
        if (istrGioc.isEmpty()) return Effetti.NEUTRO
        var poss = 0.0; var atk = 0.0; var def = 0.0; var drain = 0.0
        xi.forEach { p ->
            istrGioc[p.id].orEmpty().forEach { i ->
                when (i) {
                    IstrGiocatore.DRIBBLA_DI_PIU -> { atk += 0.006 * (p.dr - 60) / 40.0; poss -= 0.002 }
                    IstrGiocatore.DRIBBLA_DI_MENO -> { poss += 0.002; atk -= 0.002 }
                    IstrGiocatore.PASSAGGI_RISCHIOSI -> { atk += 0.008 * (p.pa - 60) / 40.0; poss -= 0.003 }
                    IstrGiocatore.PASSAGGI_SICURI -> { poss += 0.003; atk -= 0.003 }
                    IstrGiocatore.PIU_SORTITE -> { atk += 0.006; def -= 0.005; drain += 0.02 }
                    IstrGiocatore.RESTA_IN_POSIZIONE -> { def += 0.005; atk -= 0.004 }
                    IstrGiocatore.PRESSA_DI_PIU -> { poss += 0.004; drain += 0.03 }
                    IstrGiocatore.PRESSA_DI_MENO -> { poss -= 0.003; drain -= 0.03 }
                    IstrGiocatore.CONTRASTI_DURI -> { def += 0.005; drain += 0.01 }
                    IstrGiocatore.TIRA_DI_PIU, IstrGiocatore.TIRA_DI_MENO -> Unit   // cambiano chi tira, vedi pesoTiro
                }
            }
        }
        return Effetti(1 + poss.coerceIn(-0.05, 0.05), 1 + atk.coerceIn(-0.05, 0.05), 1 + def.coerceIn(-0.05, 0.05), 1 + drain.coerceIn(-0.1, 0.1))
    }

    /** Peso del giocatore nella scelta di chi tira: 1,4 con "Tira di più", 0,6 con "Tira di meno". */
    fun pesoTiro(p: Player): Double = istrGioc[p.id].orEmpty().let {
        when { IstrGiocatore.TIRA_DI_PIU in it -> 1.4; IstrGiocatore.TIRA_DI_MENO in it -> 0.6; else -> 1.0 }
    }

    /** Accende o spegne un'istruzione di un giocatore; toglie quelle dello stesso gruppo. */
    fun conIstruzione(id: Long, i: IstrGiocatore): PianoTattico {
        val ora = istrGioc[id].orEmpty()
        val nuove = if (i in ora) ora - i else ora.filter { it.gruppo != i.gruppo }.toSet() + i
        return copy(istrGioc = if (nuove.isEmpty()) istrGioc - id else istrGioc + (id to nuove))
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
        return if (v.isEmpty()) 1.0 else v.average().coerceIn(0.90, 1.10)
    }

    fun conRuoloSenzaPalla(id: Long, ruolo: String?): PianoTattico =
        copy(ruoliNonPoss = if (ruolo == null) ruoliNonPoss - id else ruoliNonPoss + (id to ruolo))

    fun codifica(): String = listOf(
        stile?.name ?: "-", passaggi.name, creativita.name, transizione.name, costruzione.name,
        frequenza.name, contrasti.name, contropressing.toString(), bloccaPortiere.toString(),
        ruoliNonPoss.entries.joinToString(";") { "${it.key}:${it.value}" }, ritmo.name,
        istrGioc.entries.joinToString(";") { e -> "${e.key}:" + e.value.joinToString(",") { it.name } }
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
                ritmo = leggi(p.getOrNull(10), Ritmo.NORMALE),
                istrGioc = p.getOrNull(11).orEmpty().split(";").mapNotNull {
                    val kv = it.split(":")
                    val id = kv.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
                    val set = kv.getOrNull(1).orEmpty().split(",").mapNotNull { n -> IstrGiocatore.values().firstOrNull { x -> x.name == n } }.toSet()
                    if (set.isEmpty()) null else id to set
                }.toMap()
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
    fun pianoCompleto(ruoliNonPoss: Map<Long, String> = emptyMap(), istrGioc: Map<Long, Set<IstrGiocatore>> = emptyMap()): PianoTattico =
        piano.copy(stile = this, ritmo = ritmo, ruoliNonPoss = ruoliNonPoss, istrGioc = istrGioc)

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

/** Punto del ruolo sul campo: profondità (0 propria porta, 1 porta avversaria) e larghezza (0 sinistra, 1 destra). */
object PostoRuolo {
    fun punto(r: String): Pair<Float, Float> = when (r) {
        "POR" -> 0.05f to 0.5f
        "TS" -> 0.25f to 0.12f; "TD" -> 0.25f to 0.88f
        "DC" -> 0.20f to 0.5f; "CS" -> 0.20f to 0.32f; "CD" -> 0.20f to 0.68f
        "MED" -> 0.33f to 0.5f; "REG" -> 0.38f to 0.5f; "INT" -> 0.45f to 0.5f
        "FS" -> 0.45f to 0.10f; "FD" -> 0.45f to 0.90f
        "MP" -> 0.58f to 0.5f; "ES" -> 0.62f to 0.15f
        "AS" -> 0.65f to 0.12f; "AD" -> 0.65f to 0.88f
        "CA" -> 0.72f to 0.5f
        else -> 0.4f to 0.5f
    }

    /** Spostamento (profondità, larghezza) da un ruolo all'altro; la larghezza non cambia se uno dei due ruoli è centrale. */
    fun spostamento(da: String, a: String): Pair<Float, Float> {
        if (da == a) return 0f to 0f
        val p = punto(da); val q = punto(a)
        val centrale = setOf("DC", "MED", "REG", "INT", "MP", "CA", "POR")
        val dy = if (da in centrale || a in centrale) 0f else q.second - p.second
        return (q.first - p.first) to dy
    }
}

/**
 * Stile delle squadre AI: le 4 squadre più storiche e forti di ogni nazione giocano uno stile da grande squadra
 * (possesso, Tiki-Taka o Gegenpress), le altre seguono lo stile del loro allenatore (attacco o difesa).
 * In ogni gruppo vince lo stile a cui la rosa si adatta meglio, quindi è sempre lo stesso finché la rosa non cambia molto.
 */
object StiliAi {
    val GRANDI = listOf(StileTattico.CONTROLLO_POSSESSO, StileTattico.TIKI_TAKA, StileTattico.TIKI_TAKA_VERTICALE, StileTattico.GEGENPRESS)
    val ATTACCO = listOf(StileTattico.GEGENPRESS, StileTattico.GIOCO_SULLE_FASCE, StileTattico.CONTROPIEDE_FLUIDO,
        StileTattico.CONTROLLO_POSSESSO, StileTattico.PALLA_LUNGA)
    val DIFESA = listOf(StileTattico.CATENACCIO, StileTattico.PARCHEGGIA_AUTOBUS, StileTattico.CONTROPIEDE_DIRETTO, StileTattico.PALLA_LUNGA)

    fun scegli(teamId: Long, coach: Stile?, rosa: List<Player>, grande: Boolean): StileTattico {
        val lista = when {
            grande -> GRANDI
            coach == Stile.ATTACCO -> ATTACCO
            coach == Stile.DIFESA -> DIFESA
            else -> ATTACCO + DIFESA
        }
        val rosaCampo = rosa.sortedByDescending { it.overall }.take(16)
        // A parità di adattamento decide la squadra (sempre lo stesso stile)
        return lista.maxBy { it.idoneita(rosaCampo) * 10 + ((teamId + it.ordinal * 7) % 7).toInt() }
    }

    /**
     * Le 4 squadre più storiche e forti di ogni nazione, prese dagli albi d'oro reali (titoli nazionali) e associate ai nomi
     * di Androcalcio. Dove due grandi reali sono nella stessa città con una sola squadra nel gioco (Lisbona, Glasgow, Istanbul),
     * entra la squadra successiva. Fonte: albi d'oro dei campionati (Wikipedia, RSSSF, ESPN), novembre 2026.
     */
    val STORICHE: Map<String, List<String>> = mapOf(
        "Italia" to listOf("Piemonte BN", "Inter NA", "Milano RN", "Partenope AZ"),          // Juventus, Inter, Milan, Napoli
        "Spagna" to listOf("Royal Madril", "Barcelos BG", "Atletic Madril", "Bilbo RJ2"),     // Real, Barcellona, Atlético, Athletic
        "Inghilterra" to listOf("Manchester Unity RD", "Liverpoole RD2", "Arsenale GN", "Manchester Citt SB"),
        "Germania" to listOf("Baviera MO", "Dortmuno GS", "Glabach FH", "Stuggart VB4"),       // Bayern, Dortmund, Gladbach, Stoccarda
        "Francia" to listOf("Parigi SG", "Marsiglia OM2", "Monaco AS2", "Lionese OL2"),        // PSG, Marsiglia, Monaco, Lione
        "Brasile" to listOf("Palmeras VD", "Flamengo MG", "Corintios TM", "San Paulo TC"),
        "Argentina" to listOf("River ML", "Boca XZ", "Racing AC", "Independencia RJ14"),
        "Olanda" to listOf("Amsterdam Vlam", "Eindhoven Licht", "Rotterdam Haven", "Alkmaar Kaas"),   // Ajax, PSV, Feyenoord, AZ
        "Portogallo" to listOf("Lisboa Sol", "Porto Ponte", "Braga Monte", "Guimaraes Castelo"),
        "Belgio" to listOf("Brussel Leeuw", "Brugge Zwaan", "Liege Acier", "Antwerpen Haven"),       // Anderlecht, Bruges, Standard, Anversa
        "Scozia" to listOf("Glasgow Clyde", "Aberdeen Granite", "Edimburgo Rock", "Dundee Tay"),
        "Turchia" to listOf("Istanbul Bosforo", "Trabzon Karadeniz", "Bursa Yesil", "Izmir Egeo"),
        "Grecia" to listOf("Atene Ulivo", "Salonicco Torre", "Larissa Pianura", "Heraklion Minoa"),
        "Giappone" to listOf("Kashima Kuro", "Yokohama Kai", "Hiroshima Momiji", "Kawasaki Taiyo")  // Kashima, Marinos, Sanfrecce, Frontale
    )

    /**
     * Le 4 squadre storiche di ogni nazione: quelle dell'elenco [STORICHE] (per nome); se una manca (rinominata nell'editor)
     * o la nazione non è nell'elenco, si completa con stadio più grande, poi budget.
     */
    fun grandi(teams: List<it.androcalcio.data.Team>): Set<Long> =
        teams.groupBy { it.nazione }.flatMap { (naz, l) ->
            val nomi = STORICHE[naz].orEmpty()
            val fisse = l.filter { it.name in nomi }
            val resto = l.filter { it !in fisse }
                .sortedWith(compareByDescending<it.androcalcio.data.Team> { it.stadiumCapacity }.thenByDescending { it.budget })
            (fisse + resto).take(4)
        }.map { it.id }.toSet()
}
