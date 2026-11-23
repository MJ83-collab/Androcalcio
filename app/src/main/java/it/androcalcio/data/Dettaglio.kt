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
    val minutiPct: Int? = null
)
