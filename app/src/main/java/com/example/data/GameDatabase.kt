package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Database Entities
@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 1000,
    val totalXp: Int = 0,
    val selectedCompanionId: String = "pyro",
    val vipActive: Boolean = false,
    val dailyStreak: Int = 1,
    val lastLoginTime: Long = System.currentTimeMillis(),
    val unlockedSkins: String = "Default" // Comma-separated list of skins
)

@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelId: Int,
    val highscore: Int = 0,
    val stars: Int = 0,
    val isCompleted: Boolean = false
)

@Entity(tableName = "creature_companions")
data class CreatureCompanion(
    @PrimaryKey val companionId: String,
    val name: String,
    val element: String, // Fire, Ice, Thunder, Shadow, Solar, Cosmic
    val level: Int = 1,
    val xp: Int = 0,
    val evolutionStage: Int = 1, // 1, 2, or 3
    val isUnlocked: Boolean = false
)

// 2. Data Access Objects (DAOs)
@Dao
interface GameDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserStateFlow(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserState(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserState(progress: UserProgress)

    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllLevelsFlow(): Flow<List<LevelProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevelProgress(level: LevelProgress)

    @Query("SELECT * FROM creature_companions")
    fun getAllCompanionsFlow(): Flow<List<CreatureCompanion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCompanion(companion: CreatureCompanion)
}

// 3. Database Abstract Class
@Database(
    entities = [UserProgress::class, LevelProgress::class, CreatureCompanion::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dragonblitz_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. Repository Abstraction (Repository Pattern)
class GameRepository(private val gameDao: GameDao) {
    val userProgress: Flow<UserProgress?> = gameDao.getUserStateFlow()
    val levelsProgress: Flow<List<LevelProgress>> = gameDao.getAllLevelsFlow()
    val companions: Flow<List<CreatureCompanion>> = gameDao.getAllCompanionsFlow()

    suspend fun getUserProgressSync(): UserProgress {
        return gameDao.getUserState() ?: UserProgress()
    }

    suspend fun updateCoins(deltaCoins: Int) {
        val current = getUserProgressSync()
        val nextCoins = (current.coins + deltaCoins).coerceAtLeast(0)
        gameDao.saveUserState(current.copy(coins = nextCoins))
    }

    suspend fun updateXpAndStreak(xpGained: Int) {
        val current = getUserProgressSync()
        val nextXp = current.totalXp + xpGained
        gameDao.saveUserState(current.copy(totalXp = nextXp))
    }

    suspend fun selectCompanion(companionId: String) {
        val current = getUserProgressSync()
        gameDao.saveUserState(current.copy(selectedCompanionId = companionId))
    }

    suspend fun unlockSkin(skinName: String) {
        val current = getUserProgressSync()
        val currentSkins = current.unlockedSkins.split(",").toMutableSet()
        currentSkins.add(skinName)
        gameDao.saveUserState(current.copy(unlockedSkins = currentSkins.joinToString(",")))
    }

    suspend fun activateVip() {
        val current = getUserProgressSync()
        gameDao.saveUserState(current.copy(vipActive = true))
    }

    suspend fun saveLevelScore(levelId: Int, score: Int, stars: Int) {
        gameDao.saveLevelProgress(
            LevelProgress(levelId = levelId, highscore = score, stars = stars, isCompleted = true)
        )
    }

    suspend fun evolveCompanion(companionId: String) {
        // Query companions is easier directly with save
        // We'll update the stage
    }

    suspend fun saveCompanionDirect(companion: CreatureCompanion) {
        gameDao.saveCompanion(companion)
    }

    suspend fun checkDailyStreak() {
        val current = getUserProgressSync()
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        if (now - current.lastLoginTime > oneDayMs * 2) {
            // Streak reset
            gameDao.saveUserState(current.copy(dailyStreak = 1, lastLoginTime = now))
        } else if (now - current.lastLoginTime > oneDayMs) {
            // Increase streak
            gameDao.saveUserState(current.copy(dailyStreak = current.dailyStreak + 1, lastLoginTime = now))
        } else {
            // Just updated last login time
            gameDao.saveUserState(current.copy(lastLoginTime = now))
        }
    }

    suspend fun checkInitializeDefaultData() {
        // Initialize user record if absent
        if (gameDao.getUserState() == null) {
            gameDao.saveUserState(UserProgress())
            
            // Unlocked default companion Pyron (Level 1 fire dragon)
            gameDao.saveCompanion(CreatureCompanion("pyro", "Pyro", "Fire", level = 1, xp = 20, evolutionStage = 1, isUnlocked = true))
            
            // Other lock preview companions
            gameDao.saveCompanion(CreatureCompanion("frosty", "Frosty", "Ice", level = 1, xp = 0, evolutionStage = 1, isUnlocked = false))
            gameDao.saveCompanion(CreatureCompanion("sparky", "Sparky", "Thunder", level = 1, xp = 0, evolutionStage = 1, isUnlocked = false))
            gameDao.saveCompanion(CreatureCompanion("shadow", "Umbria", "Shadow", level = 1, xp = 0, evolutionStage = 1, isUnlocked = false))
            gameDao.saveCompanion(CreatureCompanion("serpy", "Serpy", "Golden Snake", level = 1, xp = 0, evolutionStage = 1, isUnlocked = false))
            gameDao.saveCompanion(CreatureCompanion("iris", "Iris", "Rainbow Mythic", level = 1, xp = 0, evolutionStage = 1, isUnlocked = false))

            // Unlocked levels
            for (lvl in 1..10) {
                gameDao.saveLevelProgress(LevelProgress(levelId = lvl, highscore = 0, stars = 0, isCompleted = lvl == 1))
            }
        }
    }
}
