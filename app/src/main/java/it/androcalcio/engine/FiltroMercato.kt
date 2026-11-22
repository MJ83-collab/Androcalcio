package it.androcalcio.engine

import it.androcalcio.data.Player

/** Ricerca libera nel mercato: età, media, anni di contratto, ruolo preciso, prezzo massimo, solo svincolati. */
data class FiltroMercato(
    val etaMin: Int = 10, val etaMax: Int = 45,
    val mediaMin: Int = 1, val mediaMax: Int = 99,
    val contrattoMin: Int = 0, val contrattoMax: Int = 5,
    val pos: String = "",            // ruolo preciso (TD, DC, MED...), vuoto = tutti
    val prezzoMax: Long = 0L,        // 0 = nessun limite
    val soloSvincolati: Boolean = false
) {
    fun accetta(p: Player, prezzo: Long, svincolato: Boolean): Boolean =
        p.age in etaMin..etaMax && p.overall in mediaMin..mediaMax && p.contract in contrattoMin..contrattoMax &&
            (pos.isEmpty() || p.pos == pos || p.sec2 == pos || p.sec3 == pos || p.sec4 == pos) &&
            (prezzoMax <= 0L || prezzo <= prezzoMax) && (!soloSvincolati || svincolato)
}
