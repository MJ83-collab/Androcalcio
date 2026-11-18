package it.androcalcio.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Team::class, Player::class, MatchResult::class, Youth::class, ScoutReport::class, Staff::class, Goal::class],
    version = 7,
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

        fun create(context: Context): AndrocalcioDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AndrocalcioDatabase::class.java,
                "androcalcio.db"
            ).addMigrations(MIGRATION_5_6, MIGRATION_6_7).fallbackToDestructiveMigration().build()
    }
}
