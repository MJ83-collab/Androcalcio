package it.androcalcio.data

import androidx.room.ColumnInfo
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
    val campionato: String = "",
    val youthCoachSkill: Int = 55,
    val scoutSkill: Int = 55,
    val youthClaimed: String = ""
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
    val nationality: String,
    val potential: Int = 60,
    val contract: Int = 3,
    @ColumnInfo(defaultValue = "0") val injuredRounds: Int = 0   // turni di stop; 0 = disponibile
)

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: Long,            // 0 = candidato non ancora assunto
    val role: String,            // ALL = allenatore, PRE = preparatore
    val name: String,
    val skill: Int,              // 1..99, mostrato all'utente come giudizio
    val salary: Long,
    val contract: Int
)

@Entity(tableName = "matches")
data class MatchResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val season: String,
    val round: Int,
    val homeTeamId: Long,
    val awayTeamId: Long,
    val homeGoals: Int,
    val awayGoals: Int,
    val league: String = "",
    val played: Boolean = false
)

@Entity(tableName = "youths")
data class Youth(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: Long,
    val season: String,
    val name: String,
    val role: String,
    val age: Int,
    val overall: Int,
    val potential: Int,      // nascosto: l'utente vede solo il parere dell'allenatore
    val nationality: String
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val season: String,
    val league: String,
    val round: Int,
    val teamId: Long,
    val playerId: Long
)

@Entity(tableName = "scout_reports", primaryKeys = ["teamId", "playerId"])
data class ScoutReport(
    val teamId: Long,
    val playerId: Long,
    val observations: Int
)
