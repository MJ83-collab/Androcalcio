package it.androcalcio.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AndrocalcioDao {

    @Query("SELECT * FROM teams ORDER BY name")
    fun teams(): Flow<List<Team>>

    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY overall DESC")
    fun playersOf(teamId: Long): Flow<List<Player>>

    @Query("SELECT COUNT(*) FROM teams")
    suspend fun teamCount(): Int

    @Insert
    suspend fun insertTeams(teams: List<Team>): List<Long>

    @Insert
    suspend fun insertPlayers(players: List<Player>)
}
