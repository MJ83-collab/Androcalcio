package it.androcalcio.data

import androidx.room.Dao
import androidx.room.Insert
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

    @Insert
    suspend fun insertTeams(teams: List<Team>): List<Long>

    @Insert
    suspend fun insertPlayers(players: List<Player>)
}
