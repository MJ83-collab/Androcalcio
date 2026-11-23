package it.androcalcio.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Dati della partita dell'utente (fiducia, tifosi, reputazione...): stanno nel DB, così i salvataggi li copiano. */
@Entity(tableName = "stato")
data class Stato(@PrimaryKey val chiave: String, val valore: String)

object StatoKeys {
    const val FIDUCIA = "fiducia"            // dirigenza 0..100
    const val TIFOSI = "tifosi"              // umore tifosi 0..100
    const val REPUTAZIONE = "reputazione"    // allenatore 0..100
    const val CLIMA = "clima"                // spogliatoio 0..100
    const val RICHIESTA = "richiesta_"       // + tipo (stadio, allenamento, vivaio) = anno dell'ultima richiesta

    val DEFAULTS = mapOf(FIDUCIA to 60, TIFOSI to 60, REPUTAZIONE to 30, CLIMA to 60,
        "liv_allenamento" to 1, "liv_vivaio" to 1, "prato" to 80)
}

suspend fun AndrocalcioDao.statoInt(chiave: String): Int =
    stato(chiave)?.toIntOrNull() ?: StatoKeys.DEFAULTS[chiave] ?: 0

suspend fun AndrocalcioDao.setStatoInt(chiave: String, v: Int, min: Int = 0, max: Int = 100) =
    setStato(Stato(chiave, v.coerceIn(min, max).toString()))
