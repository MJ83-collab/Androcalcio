package it.androcalcio.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Team::class, Player::class, MatchResult::class, Youth::class, ScoutReport::class, Staff::class, Goal::class, Movimento::class, AlboEntry::class],
    version = 13,
    exportSchema = false
)
abstract class AndrocalcioDatabase : RoomDatabase() {

    abstract fun dao(): AndrocalcioDao

    companion object {
        /** 5 -> 6: aggiunge lo staff e lo stato infortunio dei giocatori, senza perdere i dati. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE players ADD COLUMN injuredRounds INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `staff` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`teamId` INTEGER NOT NULL, " +
                        "`role` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`skill` INTEGER NOT NULL, " +
                        "`salary` INTEGER NOT NULL, " +
                        "`contract` INTEGER NOT NULL)"
                )
            }
        }

        /** 6 -> 7: tabella dei gol (marcatori e portieri). */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `goals` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`season` TEXT NOT NULL, `league` TEXT NOT NULL, " +
                        "`round` INTEGER NOT NULL, `teamId` INTEGER NOT NULL, " +
                        "`playerId` INTEGER NOT NULL)"
                )
            }
        }

        /** 7 -> 8: parametri dei giocatori, ricavati dal livello attuale (variazione deterministica). */
        /** 8 -> 9: formazione titolare scelta dall'utente, senza perdere i dati. */
        /** 9 -> 10: condizione fisica tra una partita e l'altra, senza perdere i dati. */
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE players ADD COLUMN condition INTEGER NOT NULL DEFAULT 100")
            }
        }

        /** 10 -> 11: registro dei movimenti di cassa della tua squadra, senza perdere i dati. */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `albo` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`season` TEXT NOT NULL, `nazione` TEXT NOT NULL, " +
                        "`competizione` TEXT NOT NULL, `vincitore` TEXT NOT NULL)"
                )
            }
        }
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `movimenti` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`teamId` INTEGER NOT NULL, `season` TEXT NOT NULL, " +
                        "`label` TEXT NOT NULL, `importo` INTEGER NOT NULL, `saldo` INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE matches ADD COLUMN homePens INTEGER NOT NULL DEFAULT -1")
                db.execSQL("ALTER TABLE matches ADD COLUMN awayPens INTEGER NOT NULL DEFAULT -1")
            }
        }
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE players ADD COLUMN starter INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val ints = mapOf("ve" to 50, "re" to 50, "ag" to 50, "qu" to 50, "fo" to 80, "gm" to 20,
                    "en" to 50, "pa" to 50, "dr" to 50, "fi" to 50, "ti" to 50, "ri" to 50)
                ints.forEach { (c, d) ->
                    db.execSQL("ALTER TABLE players ADD COLUMN $c INTEGER NOT NULL DEFAULT $d")
                }
                db.execSQL("ALTER TABLE players ADD COLUMN foot TEXT NOT NULL DEFAULT 'D'")
                db.execSQL("ALTER TABLE players ADD COLUMN pos TEXT NOT NULL DEFAULT ''")
                // bias per ruolo: P, D, C, A (come in engine/Attributes.kt)
                fun f(col: String, mul: Int, p: Int, d: Int, c: Int, a: Int) = db.execSQL(
                    "UPDATE players SET $col = MIN(99, MAX(25, overall + CASE role WHEN 'P' THEN $p " +
                        "WHEN 'D' THEN $d WHEN 'C' THEN $c ELSE $a END + ((id * $mul) % 11) - 5))"
                )
                f("ve", 7, -8, 0, 0, 4); f("re", 13, 0, 3, 4, -3); f("ag", 5, -5, 4, 0, -2)
                f("qu", 3, 0, 0, 3, 0); f("en", 17, -25, 6, 0, -15); f("pa", 19, -10, -3, 6, -3)
                f("dr", 23, -25, -10, 0, 4); f("fi", 29, -35, -22, -8, 8); f("ti", 31, -30, -15, -3, 5)
                f("ri", 37, -20, -12, -5, 2)
                db.execSQL("UPDATE players SET gm = CASE role WHEN 'P' THEN MIN(99, overall + 8) ELSE 8 + (id % 22) END")
                db.execSQL("UPDATE players SET fo = 60 + ((id * 3) % 40)")
                db.execSQL("UPDATE players SET foot = CASE (id % 10) WHEN 0 THEN 'S' WHEN 1 THEN 'S' WHEN 2 THEN 'A' ELSE 'D' END")
                db.execSQL(
                    "UPDATE players SET pos = CASE role WHEN 'P' THEN 'POR' " +
                        "WHEN 'D' THEN CASE (id % 4) WHEN 0 THEN 'TD' WHEN 1 THEN 'TS' ELSE 'DC' END " +
                        "WHEN 'C' THEN CASE (id % 4) WHEN 0 THEN 'MED' WHEN 1 THEN 'REG' WHEN 2 THEN 'INT' ELSE 'MP' END " +
                        "ELSE CASE (id % 3) WHEN 0 THEN 'ES' ELSE 'CA' END END"
                )
            }
        }

        fun create(context: Context): AndrocalcioDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AndrocalcioDatabase::class.java,
                "androcalcio.db"
            ).addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13).fallbackToDestructiveMigration().build()
    }
}
