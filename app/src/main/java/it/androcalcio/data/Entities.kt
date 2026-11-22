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
    val youthClaimed: String = "",
    @ColumnInfo(defaultValue = "0") val wageBudget: Long = 0   // monte ingaggi annuo (separato dal budget mercato)
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
    @ColumnInfo(defaultValue = "0") val injuredRounds: Int = 0,  // turni di stop; 0 = disponibile
    // Parametri come in PC Calcio (1..99): VE velocità, RE resistenza, AG aggressività, QU qualità,
    // FO stato di forma; gioco di mani, entrate, passaggio, dribbling, finalizzare, tiro, rigori.
    @ColumnInfo(defaultValue = "50") val ve: Int = 50,
    @ColumnInfo(defaultValue = "50") val re: Int = 50,
    @ColumnInfo(defaultValue = "50") val ag: Int = 50,
    @ColumnInfo(defaultValue = "50") val qu: Int = 50,
    @ColumnInfo(defaultValue = "80") val fo: Int = 80,
    @ColumnInfo(defaultValue = "20") val gm: Int = 20,
    @ColumnInfo(defaultValue = "50") val en: Int = 50,
    @ColumnInfo(defaultValue = "50") val pa: Int = 50,
    @ColumnInfo(defaultValue = "50") val dr: Int = 50,
    @ColumnInfo(defaultValue = "50") val fi: Int = 50,
    @ColumnInfo(defaultValue = "50") val ti: Int = 50,
    @ColumnInfo(defaultValue = "50") val ri: Int = 50,
    val foot: String = "D",      // D destro, S mancino, A ambidestro
    val pos: String = "",        // ruolo dettagliato: POR TD TS DC MED REG INT MP ES CA
    @ColumnInfo(defaultValue = "0") val starter: Boolean = false,  // titolare scelto dall'utente
    @ColumnInfo(defaultValue = "100") val condition: Int = 100,    // condizione fisica 0..100, tra una partita e l'altra
    // Ruoli tattici (v14): ruolo in campo ("" = naturale), primo e secondo ruolo affine con affinità in %
    @ColumnInfo(defaultValue = "") val ruoloPartita: String = "",
    @ColumnInfo(defaultValue = "") val compito: String = "",   // compito di ruolo (engine/Compiti), vuoto = nessuno
    @ColumnInfo(defaultValue = "") val sec2: String = "",
    @ColumnInfo(defaultValue = "0") val aff2: Int = 0,
    @ColumnInfo(defaultValue = "") val sec3: String = "",
    @ColumnInfo(defaultValue = "0") val aff3: Int = 0,
    // Quarto ruolo (v24): per i giocatori molto duttili, vuoto = nessuno
    @ColumnInfo(defaultValue = "") val sec4: String = "",
    @ColumnInfo(defaultValue = "0") val aff4: Int = 0,
    // Numero di maglia (v15): deciso a inizio stagione dall'allenatore in seconda; 0 = non ancora assegnato
    @ColumnInfo(defaultValue = "0") val numero: Int = 0
)

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: Long,            // 0 = candidato non ancora assunto
    val role: String,            // ALL = allenatore, PRE = preparatore
    val name: String,
    val skill: Int,              // 1..99, mostrato all'utente come giudizio
    val salary: Long,
    val contract: Int,
    // Tattica predefinita (v16): ATTACCO o DIFESA, vedi engine/Tattica.kt
    @ColumnInfo(defaultValue = "DIFESA") val stile: String = "DIFESA",
    // Specialità: preparatori (GEN, FOR, POR, ATT, CEN, DIF) e fisioterapisti (REC, PRV); vuota per gli altri ruoli
    @ColumnInfo(defaultValue = "") val specialita: String = ""
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
    val played: Boolean = false,
    @ColumnInfo(defaultValue = "-1") val homePens: Int = -1,   // rigori in coppa; -1 = non giocati
    @ColumnInfo(defaultValue = "-1") val awayPens: Int = -1
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

@Entity(tableName = "albo")
data class AlboEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val season: String,
    val nazione: String,
    val competizione: String,
    val vincitore: String
)

@Entity(tableName = "movimenti")
data class Movimento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: Long,
    val season: String,
    val label: String,
    val importo: Long,   // positivo = entrata, negativo = uscita
    val saldo: Long       // budget dopo questo movimento
)

/** Statistiche di un giocatore in una stagione: presenze e gol (campionato e coppe). */
@Entity(tableName = "player_stats", primaryKeys = ["season", "playerId"])
data class PlayerStat(
    val season: String,
    val playerId: Long,
    val teamId: Long,
    val presenze: Int = 0,
    val gol: Int = 0
)

/** Clausole di rendimento firmate con un giocatore: pagate a fine stagione se la condizione è raggiunta. */
@Entity(tableName = "clausole")
data class Clausola(
    @PrimaryKey val playerId: Long,
    val teamId: Long,
    val bonusPresenze: Long,     // 30 presenze in stagione
    val bonusGol: Long,          // 20 gol in stagione
    val bonusCampionato: Long,   // il club vince il campionato
    val bonusCoppa: Long         // il club vince la coppa nazionale
)

/** Osservatore in missione in un'area del mondo: al rientro porta giocatori svincolati. */
@Entity(tableName = "scout_missions")
data class ScoutMission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: Long,
    val staffId: Long,
    val area: String,
    val giornate: Int
)

/** Convocazione di un giocatore in nazionale: data internazionale (giornata) o torneo (giornata 0). */
@Entity(tableName = "convocazioni")
data class Convocazione(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val season: String,
    val competizione: String,   // "Amichevole internazionale", "Mondiali · Ottavi", "Europei · Finale"
    val round: Int,             // giornata di campionato della data internazionale; 0 nei tornei
    val playerId: Long,
    val nazione: String,
    val risultato: String       // es. "Italia 2-1 Spagna"
)

/** Presenze e gol in nazionale di un giocatore in una stagione. */
@Entity(tableName = "nazionale_stats", primaryKeys = ["season", "playerId"])
data class NazionaleStat(
    val season: String,
    val playerId: Long,
    val nazione: String,
    val presenze: Int = 0,
    val gol: Int = 0
)

/** Giocatori convocabili dalla nazionale allenata dall'utente (massimo 23). */
@Entity(tableName = "rosa_nazionale")
data class RosaNazionale(
    @PrimaryKey val playerId: Long,
    val nazione: String
)
