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

    @Query("SELECT * FROM players WHERE teamId != :teamId AND role LIKE :role AND teamId NOT IN (SELECT id FROM teams WHERE nazione = 'All Star') ORDER BY overall DESC")
    fun market(teamId: Long, role: String): Flow<List<Player>>

    @Update
    suspend fun updatePlayer(p: Player)

    @Update
    suspend fun updateTeam(t: Team)

    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun team(id: Long): Team

    @Insert
    suspend fun insertYouths(list: List<Youth>)

    @Query("SELECT * FROM youths WHERE teamId = :teamId")
    suspend fun youthsOnce(teamId: Long): List<Youth>

    @Query("SELECT * FROM youths")
    suspend fun allYouths(): List<Youth>

    @Update
    suspend fun updateYouths(list: List<Youth>)

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

    @Query("SELECT y.* FROM youths y JOIN scout_reports r ON r.playerId = -y.id WHERE r.teamId = :teamId")
    fun scoutedYouths(teamId: Long): Flow<List<Youth>>

    @Query("DELETE FROM scout_reports WHERE teamId = :teamId AND playerId = :playerId")
    suspend fun deleteReport(teamId: Long, playerId: Long)

    @Query("DELETE FROM scout_reports WHERE playerId < 0 AND -playerId NOT IN (SELECT id FROM youths)")
    suspend fun cleanReportsGiovani()

    @Query("SELECT * FROM teams WHERE nazione = :nazione AND campionato = :campionato AND id != :me")
    suspend fun clubsOfLeague(nazione: String, campionato: String, me: Long): List<Team>

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

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun match(id: Long): MatchResult?

    @Query("SELECT valore FROM stato WHERE chiave = :chiave")
    suspend fun stato(chiave: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setStato(s: Stato)

    @Query("DELETE FROM stato") suspend fun clearStato()

    /** Trattative in attesa di risposta del club (chiave "pend_<id giocatore>", valore = prezzo proposto). */
    @Query("SELECT * FROM stato WHERE chiave LIKE 'pend\\_%' ESCAPE '\\'")
    suspend fun pendingClubOffers(): List<Stato>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun playerById(id: Long): Player?

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
    suspend fun insertPlayers(players: List<Player>): List<Long>

    @Query("SELECT * FROM staff WHERE teamId = :teamId ORDER BY role")
    fun staffOf(teamId: Long): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE teamId = :teamId")
    suspend fun staffOnce(teamId: Long): List<Staff>

    @Query("SELECT * FROM staff")
    suspend fun allStaff(): List<Staff>

    @Insert
    suspend fun addStaff(s: Staff)

    @Insert
    suspend fun insertStaffs(list: List<Staff>): List<Long>

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

    @Insert
    suspend fun insertMovimento(m: Movimento)

    @Query("SELECT * FROM movimenti WHERE teamId = :teamId ORDER BY id DESC LIMIT :n")
    fun movimenti(teamId: Long, n: Int = 60): Flow<List<Movimento>>

    @Query("DELETE FROM movimenti") suspend fun clearMovimenti()

    @Insert suspend fun insertAlbo(list: List<AlboEntry>)

    @Query("DELETE FROM albo WHERE season = :season") suspend fun deleteAlboSeason(season: String)
    @Query("DELETE FROM albo") suspend fun clearAlbo()

    @Query("SELECT * FROM albo ORDER BY season DESC, id")
    fun albo(): Flow<List<AlboEntry>>

    @Query("SELECT * FROM movimenti WHERE teamId = :teamId AND season = :season")
    suspend fun movimentiStagioneOnce(teamId: Long, season: String): List<Movimento>

    @Query("SELECT * FROM movimenti WHERE teamId = :teamId AND season = :season ORDER BY id")
    fun movimentiStagione(teamId: Long, season: String): Flow<List<Movimento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(list: List<PlayerStat>)

    @Query("SELECT * FROM player_stats")
    suspend fun allStats(): List<PlayerStat>

    @Query("SELECT * FROM player_stats WHERE season = :season")
    suspend fun statsOf(season: String): List<PlayerStat>

    @Query("SELECT DISTINCT season FROM player_stats ORDER BY season DESC")
    suspend fun statsSeasons(): List<String>

    @Query("DELETE FROM player_stats") suspend fun clearStats()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClausole(list: List<Clausola>)

    @Query("SELECT * FROM clausole")
    suspend fun clausoleOnce(): List<Clausola>

    @Delete suspend fun deleteClausole(list: List<Clausola>)

    @Query("DELETE FROM clausole") suspend fun clearClausole()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMissions(list: List<ScoutMission>)

    @Query("SELECT * FROM scout_missions")
    suspend fun missionsOnce(): List<ScoutMission>

    @Delete suspend fun deleteMissions(list: List<ScoutMission>)

    @Query("DELETE FROM scout_missions") suspend fun clearMissions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConvocazioni(list: List<Convocazione>)

    @Query("SELECT * FROM convocazioni WHERE season = :season AND round = :round")
    suspend fun convocazioniData(season: String, round: Int): List<Convocazione>

    @Query("SELECT * FROM convocazioni WHERE playerId = :playerId ORDER BY id DESC")
    suspend fun convocazioniDi(playerId: Long): List<Convocazione>

    @Query("SELECT * FROM convocazioni")
    suspend fun allConvocazioni(): List<Convocazione>

    @Query("DELETE FROM convocazioni") suspend fun clearConvocazioni()

    @Query("SELECT * FROM nazionale_stats")
    suspend fun allNazionaleStats(): List<NazionaleStat>

    @Query("SELECT * FROM nazionale_stats WHERE season = :season")
    suspend fun nazionaleStatsOf(season: String): List<NazionaleStat>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNazionaleStats(list: List<NazionaleStat>)

    @Query("DELETE FROM nazionale_stats") suspend fun clearNazionaleStats()

    @Query("SELECT * FROM rosa_nazionale")
    suspend fun rosaNazionale(): List<RosaNazionale>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRosaNazionale(r: RosaNazionale)

    @Query("DELETE FROM rosa_nazionale WHERE playerId = :playerId") suspend fun deleteRosaNazionale(playerId: Long)

    @Query("DELETE FROM rosa_nazionale") suspend fun clearRosaNazionale()
}

