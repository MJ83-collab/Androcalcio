package it.androcalcio.data

/** Tutto ciò che la scheda di un giocatore mostra oltre ai suoi parametri. */
data class DettaglioGiocatore(
    val player: Player,
    val squadra: String,
    val clausola: Clausola?,
    val stagioni: List<PlayerStat>,
    val nazionaleStagioni: List<NazionaleStat>,
    val convocazioni: List<Convocazione>,
    val squadreStagioni: Map<Long, String> = emptyMap(),
    val votiStagioni: Map<String, Double> = emptyMap(),   // stagione -> voto medio (solo con la tua squadra)
    val infortuni: List<Pair<String, Int>> = emptyList(), // stagione, turni di stop
    val minutiPct: Int? = null,
    val forma: List<Double> = emptyList(),   // ultimi voti, dal più vecchio
    val stima: it.androcalcio.engine.Conoscenza.Stima? = null,   // potenziale stimato da chi lo conosce
    val parereRosa: String = "",                                  // parere del vice per la prima squadra (solo i tuoi)
    val affinita: Pair<String, String>? = null,                   // affinità di carattere con la tua squadra
    val mio: Boolean = false
)
