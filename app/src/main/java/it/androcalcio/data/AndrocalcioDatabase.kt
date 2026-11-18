package it.androcalcio.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Team::class, Player::class, MatchResult::class],
    version = 2,
    exportSchema = false
)
abstract class AndrocalcioDatabase : RoomDatabase() {

    abstract fun dao(): AndrocalcioDao

    companion object {
        fun create(context: Context): AndrocalcioDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AndrocalcioDatabase::class.java,
                "androcalcio.db"
            ).fallbackToDestructiveMigration().build()
    }
}
