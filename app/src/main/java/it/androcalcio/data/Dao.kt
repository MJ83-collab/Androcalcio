package it.androcalcio.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AndrocalcioDao {

    @Query("SELECT * FROM teams ORDER BY id")
    fun teams(): Flow<List<Team>>

    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY overall DESC")
    fun playersOf(teamId: Long): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE teamId = :teamId")
    suspend fun playersOnce(teamId: Long): List<Player>

    @Query("SELECT COUNT(*) FROM matches WHERE league = :league AND season = :season")
    suspend fun fixtureCount(league: String, season: String): Int

    @Insert
    suspend fun insertMatches(list: List<MatchResult>)

    @Update
    suspend fun updateMatch(match: MatchResult)

    @Query("SELECT * FROM matches WHERE league = :league AND season = :season ORDER BY round, id")
    fun matches(league: String, season: String): Flow<List<MatchResult>>

    @Query("SELECT * FROM matches WHERE league = :league AND season = :season ORDER BY round, id")
    suspend fun matchesOnce(league: String, season: String): List<MatchResult>

    @Query("SELECT * FROM teams WHERE nazione = :nazione AND campionato = :campionato ORDER BY id")
    suspend fun teamsOfLeague(nazione: String, campionato: String): List<Team>

    @Query("SELECT COUNT(*) FROM teams")
    suspend fun teamCount(): Int

    @Query("SELECT * FROM players WHERE teamId != :teamId AND role LIKE :role ORDER BY overall DESC")
    fun market(teamId: Long, role: String): Flow<List<Player>>

    @Update
    suspend fun updatePlayer(p: Player)

    @Update
    suspend fun updateTeam(t: Team)

    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun team(id: Long): Team

    @Insert
    suspend fun insertYouths(list: List<Youth>)

    @Query("SELECT * FROM youths WHERE teamId = :teamId ORDER BY potential DESC")
    fun youths(teamId: Long): Flow<List<Youth>>

    @Delete
    suspend fun deleteYouth(y: Youth)

    @Insert
    suspend fun insertPlayer(p: Player)

    @Query("SELECT * FROM scout_reports WHERE teamId = :teamId")
    fun reports(teamId: Long): Flow<List<ScoutReport>>

    @Query("SELECT p.* FROM players p JOIN scout_reports r ON r.playerId = p.id WHERE r.teamId = :teamId")
    fun scouted(teamId: Long): Flow<List<Player>>

    @Query("SELECT * FROM scout_reports WHERE teamId = :teamId AND playerId = :playerId")
    suspend fun report(teamId: Long, playerId: Long): ScoutReport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReports(list: List<ScoutReport>)

    @Query("SELECT * FROM players WHERE teamId != :me AND teamId IN (SELECT id FROM teams WHERE nazione = :nazione AND campionato = :campionato)")
    suspend fun leaguePlayers(nazione: String, campionato: String, me: Long): List<Player>

    @Query("SELECT * FROM teams ORDER BY id")
    suspend fun allTeams(): List<Team>

    @Query("SELECT * FROM players")
    suspend fun allPlayers(): List<Player>

    @Delete
    suspend fun deletePlayer(p: Player)

    @Query("DELETE FROM matches") suspend fun clearMatches()
    @Query("DELETE FROM youths") suspend fun clearYouths()
    @Query("DELETE FROM scout_reports") suspend fun clearReports()
    @Query("DELETE FROM players") suspend fun clearPlayers()
    @Query("DELETE FROM teams") suspend fun clearTeams()

    @Update
    suspend fun updateMatches(list: List<MatchResult>)

    @Update
    suspend fun updatePlayers(list: List<Player>)

    @Update
    suspend fun updateTeams(list: List<Team>)

    @Delete
    suspend fun deletePlayers(list: List<Player>)

    @Insert
    suspend fun insertTeams(teams: List<Team>): List<Long>

    @Insert
    suspend fun insertPlayers(players: List<Player>)

    @Query("SELECT * FROM staff WHERE teamId = :teamId ORDER BY role")
    fun staffOf(teamId: Long): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE teamId = :teamId")
    suspend fun staffOnce(teamId: Long): List<Staff>

    @Query("SELECT * FROM staff")
    suspend fun allStaff(): List<Staff>

    @Insert
    suspend fun addStaff(s: Staff)

    @Insert
    suspend fun insertStaffs(list: List<Staff>)

    @Update
    suspend fun updateStaffs(list: List<Staff>)

    @Delete
    suspend fun deleteStaffs(list: List<Staff>)

    @Query("DELETE FROM staff") suspend fun clearStaff()

    @Insert
    suspend fun insertGoals(list: List<Goal>)

    @Query("SELECT * FROM goals WHERE league = :league AND season = :season")
    fun goals(league: String, season: String): Flow<List<Goal>>

    @Query("DELETE FROM goals") suspend fun clearGoals()

    @Query("SELECT * FROM players")
    fun allPlayersFlow(): Flow<List<Player>>
}
