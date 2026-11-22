package it.androcalcio.data

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Informazioni mostrate per uno slot; null nei campi se lo slot è vuoto. */
data class SlotInfo(
    val n: Int,
    val teamName: String? = null,
    val season: String? = null,
    val date: String? = null
) {
    val empty get() = teamName == null
}

/**
 * Salvataggi completi: ogni slot è una copia dell'intero database
 * (squadre, rose, calendario, risultati, marcatori, staff, vivaio, scouting, infortuni)
 * più un file con stagione e squadra scelta.
 */
object SaveSlots {
    const val COUNT = 3
    /** Autosalvataggio: uno slot in più, riscritto dall'app ogni poche partite. */
    const val AUTO = 4
    const val DB_NAME = "androcalcio.db"

    private fun dir(ctx: Context) = File(ctx.filesDir, "salvataggi").apply { mkdirs() }
    private fun dbFile(ctx: Context, n: Int) = File(dir(ctx), "slot$n.db")
    private fun metaFile(ctx: Context, n: Int) = File(dir(ctx), "slot$n.json")

    fun list(ctx: Context): List<SlotInfo> = (1..AUTO).map { n ->
        val m = metaFile(ctx, n)
        if (!m.exists() || !dbFile(ctx, n).exists()) SlotInfo(n) else try {
            val j = JSONObject(m.readText())
            SlotInfo(n, j.getString("team"), j.getString("season"), j.getString("date"))
        } catch (e: Exception) { SlotInfo(n) }
    }

    /** Copia il database nello slot (prima svuota il WAL, così il file è completo). */
    fun save(ctx: Context, db: AndrocalcioDatabase, n: Int, teamId: Long, teamName: String, year: Int, season: String) {
        db.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)")).use { it.moveToFirst() }
        val tmp = File(dir(ctx), "slot$n.tmp")
        ctx.getDatabasePath(DB_NAME).copyTo(tmp, overwrite = true)
        tmp.renameTo(dbFile(ctx, n).also { it.delete() })
        metaFile(ctx, n).writeText(
            JSONObject().put("teamId", teamId).put("team", teamName)
                .put("year", year).put("season", season)
                .put("date", SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date()))
                .toString()
        )
    }

    fun delete(ctx: Context, n: Int) {
        dbFile(ctx, n).delete()
        metaFile(ctx, n).delete()
    }

    /**
     * Carica lo slot: sostituisce il file del database e ripristina stagione e squadra.
     * Dopo questa chiamata l'app va riavviata. Restituisce false se lo slot è vuoto.
     */
    fun load(ctx: Context, n: Int): Boolean {
        val src = dbFile(ctx, n)
        val meta = metaFile(ctx, n)
        if (!src.exists() || !meta.exists()) return false
        val j = JSONObject(meta.readText())
        // Copia accanto e poi rinomina: il database aperto non viene toccato fino al riavvio
        val target = ctx.getDatabasePath(DB_NAME)
        val tmp = File(target.path + ".load")
        src.copyTo(tmp, overwrite = true)
        File(target.path + "-wal").delete()
        File(target.path + "-shm").delete()
        if (!tmp.renameTo(target)) return false
        ctx.getSharedPreferences("androcalcio", 0).edit()
            .putInt("season_year", j.getInt("year"))
            .putLong("team_id", j.getLong("teamId"))
            .putBoolean("partita_iniziata", true)
            .commit()
        return true
    }
}
