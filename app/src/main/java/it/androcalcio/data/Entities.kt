package it.androcalcio.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val shortName: String,
    val colorPrimary: Long,      // ARGB
    val colorSecondary: Long,    // ARGB
    val budget: Long,
    val stadiumCapacity: Int,
    val ticketPrice: Int,
    val sponsor: String,
    val nazione: String = "",
    val campionato: String = ""
)

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: Long,
    val name: String,
    val role: String,            // P, D, C, A
    val age: Int,
    val overall: Int,            // 1..99
    val morale: Int,             // 0..100
    val salary: Long,
    val nationality: String
)

@Entity(tableName = "matches")
data class MatchResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val season: String,
    val round: Int,
    val homeTeamId: Long,
    val awayTeamId: Long,
    val homeGoals: Int,
    val awayGoals: Int
)
