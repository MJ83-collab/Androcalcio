package it.androcalcio.data

/** Tutto ciò che la scheda di un giocatore mostra oltre ai suoi parametri. */
data class DettaglioGiocatore(
    val player: Player,
    val squadra: String,
    val clausola: Clausola?,
    val stagioni: List<PlayerStat>,
    val nazionaleStagioni: List<NazionaleStat>,
    val convocazioni: List<Convocazione>
)
