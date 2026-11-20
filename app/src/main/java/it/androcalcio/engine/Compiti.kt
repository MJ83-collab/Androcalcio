package it.androcalcio.engine

/**
 * Compiti di ruolo: una scelta in più sopra al ruolo del giocatore (es. Ala Introversa su ES).
 * Un compito dà un bonus di rendimento quando il giocatore gioca nel suo ruolo.
 */
object Compiti {
    data class Compito(val codice: String, val nome: String, val descrizione: String, val ruoli: List<String>)

    /** Bonus di rendimento di un compito scelto (3%). */
    const val BONUS = 0.03

    val TUTTI = listOf(
        Compito("portiere_classico", "Portiere Classico", "Ruolo standard di difesa della porta.", listOf("POR")),
        Compito("portiere_uscitore", "Portiere Uscitore", "Esce dalla area per intercettare palle alte o palloni giocati.", listOf("POR")),
        Compito("portiere_libero", "Portiere Libero", "Esce molto per coprire spazi dietro la difesa.", listOf("POR")),
        Compito("portiere_di_basso", "Portiere di Basso", "Rimanendo basso, serve da libero per lanci lunghi.", listOf("POR")),
        Compito("difensore_centrale", "Difensore Centrale", "Marcare, contrastare e conquistare il pallone.", listOf("DC", "CS", "CD")),
        Compito("difensore_centrale_costruttore_di_gioco", "Difensore Centrale Costruttore di Gioco", "Rompe le linee con passaggi dalla difesa.", listOf("DC", "CS", "CD")),
        Compito("difensore_centrale_non_convenzionale", "Difensore Centrale Non Convenzionale", "Pulisce il pericolo senza cercare costruzioni complesse.", listOf("DC", "CS", "CD")),
        Compito("difensore_centrale_avanzato", "Difensore Centrale Avanzato", "Si spinge in mediana per aggiungere un uomo in fase di possesso.", listOf("DC", "CS", "CD")),
        Compito("difensore_centrale_largo", "Difensore Centrale Largo", "In un trequarti, si sparge per supportare le fasce.", listOf("DC", "CS", "CD")),
        Compito("difensore_centrale_che_si_sovrappone", "Difensore Centrale che si sovrappone", "Corre lungo la fascia per creare superiorità numerica.", listOf("DC", "CS", "CD")),
        Compito("difensore_centrale_che_ferma", "Difensore Centrale che Ferma", "Esce incontro all'attaccante per vincere il pallone anticipandolo.", listOf("DC", "CS", "CD")),
        Compito("difensore_centrale_che_copre", "Difensore Centrale che Copre", "Si ritira per coprire i vuoti dietro la linea difensiva.", listOf("DC", "CS", "CD")),
        Compito("terzino", "Terzino", "Bilancia difesa e supporto offensivo.", listOf("TD", "TS")),
        Compito("terzino_interno", "Terzino Interno", "Si inserisce tra i difensori centrali in possesso per dare opzioni.", listOf("TD", "TS")),
        Compito("terzino_che_pressa", "Terzino che Pressa", "Chiude l'ala avversaria alta sul campo.", listOf("TD", "TS")),
        Compito("terzino_trattenuto", "Terzino Trattenuto", "Mantiene la posizione in difesa senza pressare alto.", listOf("TD", "TS")),
        Compito("mediano", "Mediano", "Si posiziona davanti alla difesa per recuperare e passare semplice.", listOf("MED")),
        Compito("regista", "Regista", "Imposta il gioco dal basso e serve i passaggi decisivi.", listOf("REG")),
        Compito("mezzala_half_back", "Mezzala (Half-Back)", "Si inserisce tra i difensori centrali per formare un trequarti.", listOf("INT")),
        Compito("mediano_che_si_ritira", "Mediano che si Ritira", "Scende nella linea difensiva in fase di non possesso.", listOf("MED")),
        Compito("mediano_schermante", "Mediano Schermante", "Blocca gli spazi davanti ai difensori centrali.", listOf("MED")),
        Compito("mediano_che_pressa", "Mediano che Pressa", "Esce dal muro per pressare il centrocampista avversario.", listOf("MED")),
        Compito("mediano_che_copre_largo", "Mediano che Copre Largo", "Si sposta sulla fascia per aiutare il terzino.", listOf("MED")),
        Compito("centrocampista_centrale", "Centrocampista Centrale", "Collega gioco e supporta entrambe le fasi.", listOf("MED", "INT")),
        Compito("regista_in_movimento_midfield_playmaker", "Regista in Movimento (Midfield Playmaker)", "Crea gioco nel terzo medio.", listOf("REG")),
        Compito("tutto_campo_box_to_box", "Tutto Campo (Box-to-Box)", "Copre tutto il campo, arrivando tardi in area per tirare.", listOf("MED", "INT")),
        Compito("regista_tutto_campo", "Regista Tutto Campo", "Corridore che cerca anche di creare gioco quando sale.", listOf("REG")),
        Compito("centrocampista_di_spazio", "Centrocampista di Spazio", "Corre negli spazi tra terzino e difensore centrale.", listOf("MED", "INT")),
        Compito("centrocampista_che_pressa", "Centrocampista che Pressa", "Esce per pressare il portatore di palla.", listOf("MED", "INT")),
        Compito("centrocampista_schermante", "Centrocampista Schermante", "Protegge la difesa senza pressare.", listOf("MED", "INT")),
        Compito("centrocampista_che_copre_largo", "Centrocampista che Copre Largo", "Aiuta sulla fascia in difesa.", listOf("MED", "INT")),
        Compito("trequartista", "Trequartista", "Gioca tra le linee, collega con la punta e cerca il tiro.", listOf("MP")),
        Compito("regista_avanzato", "Regista Avanzato", "Principale creatore nel terzo offensivo.", listOf("MP")),
        Compito("ruolo_libero", "Ruolo Libero", "Si muove dove trova spazio.", listOf("MP")),
        Compito("trequartista_che_segue", "Trequartista che Segue", "Ritorna indietro per marcare il mediano avversario.", listOf("MP")),
        Compito("trequartista_uscita_centrale", "Trequartista Uscita Centrale", "Resta alto e centrale pronto al contropiede.", listOf("MP")),
        Compito("trequartista_uscita_divisa", "Trequartista Uscita Divisa", "Resta tra i difensori per rompere la linea in contropiede.", listOf("MP")),
        Compito("ala", "Ala", "Resta largo, batte il terzino e serve il cross.", listOf("ES")),
        Compito("ala_introversa", "Ala Introversa", "Parte largo ma taglia dentro per tirare o passare.", listOf("ES")),
        Compito("ala_di_spazio", "Ala di Spazio", "Gioca nel corridoio tra fascia e centro.", listOf("ES")),
        Compito("regista_largo", "Regista Largo", "Riceve largo e si insinua per creare.", listOf("ES")),
        Compito("ampia_wide_forward", "Ampia (Wide Forward)", "Corre dietro la difesa per segnare più che crossare.", listOf("ES")),
        Compito("introversa_inside_forward", "Introversa (Inside Forward)", "Taglia dentro per attaccare l'area di rigore.", listOf("ES")),
        Compito("ala_che_segue", "Ala che Segue", "Ripiega con il terzino avversario in difesa.", listOf("ES")),
        Compito("ala_uscita_largo", "Ala Uscita Largo", "Resta alto sulla fascia per allargare il contropiede.", listOf("ES")),
        Compito("ala_uscita_introversa", "Ala Uscita Introversa", "Resta alto e stretto per attaccare gli spazi interni.", listOf("ES")),
        Compito("centravanti", "Centravanti", "Completo, segna, porta palla e corre dietro.", listOf("CA")),
        Compito("punta_bersaglio", "Punta Bersaglio", "Punto di riferimento per il gioco aereo e palle in profondità.", listOf("CA")),
        Compito("trequartista_poacher", "Trequartista (Poacher)", "Vive a ridosso dell'ultimo uomo e nell'area piccola.", listOf("CA")),
        Compito("attaccante_arretrato_deep_lying_forward", "Attaccante Arretrato (Deep-Lying Forward)", "Ritorna corto per collegare e portare palla.", listOf("CA")),
        Compito("falso_nove_false_nine", "Falso Nove (False Nine)", "Scende in mediana per trascinare il difensore e creare spazio.", listOf("CA")),
        Compito("attaccante_di_fascia_channel_forward", "Attaccante di Fascia (Channel Forward)", "Corre nelle fasce interne per allargare la difesa.", listOf("CA")),
        Compito("seconda_punta", "Seconda Punta", "Gioca dietro una punta e arriva in area in ritardo.", listOf("CA")),
        Compito("attaccante_di_spazio", "Attaccante di Spazio", "Si muove negli spazi laterali del centro per ricevere.", listOf("CA")),
        Compito("attaccante_centrale_che_segue", "Attaccante Centrale che Segue", "Ripiega per marcare il mediano avversario.", listOf("CA")),
        Compito("attaccante_centrale_uscita", "Attaccante Centrale Uscita", "Resta alto e centrale per il contropiede.", listOf("CA")),
        Compito("attaccante_centrale_uscita_divisa", "Attaccante Centrale Uscita Divisa", "Resta tra i difensori per rompere la linea in contropiede.", listOf("CA")),
    )

    /** Compiti disponibili per un ruolo (codice di ruolo, es. "ES", "DC"). */
    fun perRuolo(ruolo: String): List<Compito> = TUTTI.filter { ruolo in it.ruoli }

    fun trova(codice: String): Compito? = TUTTI.firstOrNull { it.codice == codice }

    /** Moltiplicatore di rendimento: 1.0 senza compito o con un compito di un altro ruolo. */
    fun effetto(codice: String, ruolo: String): Double =
        if (codice.isNotEmpty() && trova(codice)?.ruoli?.contains(ruolo) == true) 1.0 + BONUS else 1.0
}
