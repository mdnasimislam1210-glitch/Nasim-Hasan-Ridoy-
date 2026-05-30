package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = GameRepository(db.gameDao())

    // Observables from DB
    val userProgress: StateFlow<UserProgress?> = repository.userProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val levelsProgress: StateFlow<List<LevelProgress>> = repository.levelsProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val companions: StateFlow<List<CreatureCompanion>> = repository.companions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Match-3 Grid State
    private val _boardState = MutableStateFlow<Array<Array<MatchPiece>>>(emptyArray())
    val boardState: StateFlow<Array<Array<MatchPiece>>> = _boardState.asStateFlow()

    // Game Session states
    var currentLevelId = MutableStateFlow(1)
    val currentScore = MutableStateFlow(0)
    val movesLeft = MutableStateFlow(30)
    val comboMultiplier = MutableStateFlow(1)
    val isAnimating = MutableStateFlow(false)
    val gameplayActive = MutableStateFlow(false)

    // Target States
    val targetType = MutableStateFlow(CreatureType.FIRE)
    val targetCountRequired = MutableStateFlow(20)
    val targetClearedCount = MutableStateFlow(0)

    // Boss Battle states
    val isBossLevel = MutableStateFlow(false)
    val bossHp = MutableStateFlow(0)
    val bossMaxHp = MutableStateFlow(5000)
    val bossName = MutableStateFlow("Lava Tyrant")
    val isBossAttackActive = MutableStateFlow(false)

    // Active companion chosen for boost
    val activeCompanion = MutableStateFlow<CreatureCompanion?>(null)

    // Current screen popup trigger states
    val isLevelCleared = MutableStateFlow(false)
    val isLevelFailed = MutableStateFlow(false)
    val rewardCoinsEarned = MutableStateFlow(0)
    val rewardStarsEarned = MutableStateFlow(0)

    // Lucky wheel state
    val isWheelSpinning = MutableStateFlow(false)
    val wheelSelectionDegrees = MutableStateFlow(0f)

    private var matchEngine = MatchThreeEngine()

    init {
        viewModelScope.launch {
            repository.checkInitializeDefaultData()
            repository.checkDailyStreak()
            // Pull initial values
            observeActiveCompanion()
        }
    }

    private fun observeActiveCompanion() {
        viewModelScope.launch {
            // Find currently selected companion
            combine(companions, userProgress) { list, user ->
                if (user != null && list.isNotEmpty()) {
                    list.find { it.companionId == user.selectedCompanionId }
                } else null
            }.collect { companion ->
                activeCompanion.value = companion
            }
        }
    }

    // Prepare match-3 board for playing a selected level
    fun startLevel(levelId: Int) {
        currentLevelId.value = levelId
        currentScore.value = 0
        movesLeft.value = when (levelId) {
            in 1..3 -> 30
            in 4..6 -> 25
            else -> 20
        }
        comboMultiplier.value = 1
        gameplayActive.value = true
        isLevelCleared.value = false
        isLevelFailed.value = false
        targetClearedCount.value = 0

        // Set levels targets
        when (levelId) {
            1 -> {
                isBossLevel.value = false
                targetType.value = CreatureType.FIRE
                targetCountRequired.value = 15
            }
            2 -> {
                isBossLevel.value = false
                targetType.value = CreatureType.ICE
                targetCountRequired.value = 20
            }
            3 -> {
                isBossLevel.value = false
                targetType.value = CreatureType.THUNDER
                targetCountRequired.value = 20
            }
            4 -> {
                isBossLevel.value = false
                targetType.value = CreatureType.SHADOW
                targetCountRequired.value = 25
            }
            5 -> {
                // Boss level!
                isBossLevel.value = true
                bossMaxHp.value = 4000
                bossHp.value = 4000
                bossName.value = "VALKOR THE LAVA WYRM"
                targetType.value = CreatureType.FIRE
                targetCountRequired.value = 30
            }
            6 -> {
                isBossLevel.value = false
                targetType.value = CreatureType.GOLDEN_SNAKE
                targetCountRequired.value = 15
            }
            7 -> {
                isBossLevel.value = false
                targetType.value = CreatureType.THUNDER
                targetCountRequired.value = 25
            }
            8 -> {
                isBossLevel.value = false
                targetType.value = CreatureType.SHADOW
                targetCountRequired.value = 30
            }
            9 -> {
                isBossLevel.value = false
                targetType.value = CreatureType.FIRE
                targetCountRequired.value = 35
            }
            10 -> {
                // final shadow hydra boss
                isBossLevel.value = true
                bossMaxHp.value = 8000
                bossHp.value = 8000
                bossName.value = "MALAKAR SHADOW OVERLORD"
                targetType.value = CreatureType.SHADOW
                targetCountRequired.value = 40
            }
        }

        // Configure Match engine board size matching rows/cols
        matchEngine = MatchThreeEngine(6, 6)
        _boardState.value = matchEngine.board
    }

    // Swap adjacent creature blocks!
    fun swapPieces(r1: Int, c1: Int, r2: Int, c2: Int) {
        if (isAnimating.value || !gameplayActive.value) return

        viewModelScope.launch {
            isAnimating.value = true
            
            // 1. Perform Swap attempt
            val (matched, success) = matchEngine.attemptSwap(r1, c1, r2, c2)
            _boardState.value = copyBoard(matchEngine.board)

            if (success) {
                // Decrement moves
                movesLeft.value -= 1
                
                // 2. Cascade Loop for chain actions
                var matchesToProcess = matched
                while (matchesToProcess.isNotEmpty()) {
                    // Score current matches
                    val baseScore = matchesToProcess.size * 10 * comboMultiplier.value
                    
                    // Count target counts
                    val targetMatches = matchesToProcess.filter { it.type == targetType.value }
                    if (targetMatches.isNotEmpty()) {
                        // Level companions can multiply damage or increase drop rates
                        val companionMultiplier = activeCompanion.value?.level ?: 1
                        targetClearedCount.value += targetMatches.size * companionMultiplier
                    }

                    // Score calculation
                    currentScore.value += baseScore

                    // Boss Combat
                    if (isBossLevel.value) {
                        // Elements matched deal specific boss damage
                        val totalDmg = matchesToProcess.sumOf {
                            val dmgBase = when (it.type) {
                                CreatureType.FIRE -> 80
                                CreatureType.ICE -> 50
                                CreatureType.THUNDER -> 60
                                CreatureType.SHADOW -> 40
                                CreatureType.GOLDEN_SNAKE -> 100
                                else -> 150
                            }
                            dmgBase * (activeCompanion.value?.evolutionStage ?: 1)
                        }
                        bossHp.value = (bossHp.value - totalDmg).coerceAtLeast(0)
                    }

                    // Process trigger chains (Blasters, Special items)
                    val fullyBlasted = matchEngine.processMatchedTriggers()
                    _boardState.value = copyBoard(matchEngine.board)
                    delay(300)

                    // Apply gravity
                    matchEngine.applyGravity()
                    _boardState.value = copyBoard(matchEngine.board)
                    delay(250)

                    // Refill missing slots
                    matchEngine.refillEmptyTopSlots()
                    _boardState.value = copyBoard(matchEngine.board)
                    delay(200)

                    // Search for cascading matches created by physical drop!
                    comboMultiplier.value += 1
                    matchesToProcess = matchEngine.checkBoardForMatchPatterns(markMatched = true)
                    _boardState.value = copyBoard(matchEngine.board)
                }

                // Reset Combo Multiplier for next swipe
                comboMultiplier.value = 1

                // Check win boundaries
                checkGameEndConditions()
            } else {
                // Non-matching swap: visual feedback swap back
                delay(150)
            }
            
            isAnimating.value = false
        }
    }

    private fun checkGameEndConditions() {
        // Star rewards
        val reqTargetMet = targetClearedCount.value >= targetCountRequired.value
        val bossKilled = isBossLevel.value && bossHp.value <= 0
        val targetMet = if (isBossLevel.value) reqTargetMet && bossKilled else reqTargetMet

        if (targetMet) {
            // Level Cleared Success!
            isLevelCleared.value = true
            gameplayActive.value = false

            // Calculate score rewards
            val earnedCoins = 150 + (currentScore.value / 10)
            val stars = when {
                currentScore.value >= 2500 -> 3
                currentScore.value >= 1500 -> 2
                else -> 1
            }
            rewardCoinsEarned.value = earnedCoins
            rewardStarsEarned.value = stars

            viewModelScope.launch {
                // Save progress
                repository.updateCoins(earnedCoins)
                repository.saveLevelScore(currentLevelId.value, currentScore.value, stars)
                
                // Double XP to the companion!
                activeCompanion.value?.let { comp ->
                    val increasedXp = comp.xp + 50
                    var nextLevel = comp.level
                    var isUnlocked = comp.isUnlocked
                    if (increasedXp >= 100) {
                        nextLevel += 1
                    }
                    repository.saveCompanionDirect(comp.copy(xp = increasedXp % 100, level = nextLevel))
                }

                // Auto-unlock next level
                if (currentLevelId.value < 10) {
                    repository.saveLevelScore(currentLevelId.value + 1, 0, 0)
                }
            }
        } else if (movesLeft.value <= 0) {
            // Level Failed Out of Moves!
            isLevelFailed.value = true
            gameplayActive.value = false
        }
    }

    // Evolution System -> Level Up & Evolve Creature companion using coins
    fun evolveCompanion(companionId: String, currentCompanions: List<CreatureCompanion>) {
        val companion = currentCompanions.find { it.companionId == companionId } ?: return
        val currentCoins = userProgress.value?.coins ?: 0
        
        // Evolve cost: stage * 300 coins
        val cost = companion.evolutionStage * 400
        if (currentCoins >= cost) {
            viewModelScope.launch {
                repository.updateCoins(-cost)
                val nextStage = (companion.evolutionStage + 1).coerceAtMost(3)
                val newName = when (companionId) {
                    "pyro" -> if (nextStage == 2) "Pyrosaur" else "Pyrogon Elder"
                    "frosty" -> if (nextStage == 2) "Froststrike" else "Cosmic Ice Panther"
                    "sparky" -> if (nextStage == 2) "Voltgriff" else "Lightning Storm Gryphon"
                    "shadow" -> if (nextStage == 2) "Umbrialisk" else "Void Dreadfiend"
                    "serpy" -> if (nextStage == 2) "Gilded Viper" else "Basilisk Hydra"
                    "iris" -> if (nextStage == 2) "Seraphic Chick" else "Nebula Phoenix"
                    else -> companion.name
                }
                repository.saveCompanionDirect(
                    companion.copy(
                        name = newName,
                        evolutionStage = nextStage,
                        level = companion.level + 1
                    )
                )
            }
        }
    }

    // Hatch standard egg using 250 coins
    fun hatchEgg(companionId: String) {
        val userVal = userProgress.value ?: return
        if (userVal.coins >= 250) {
            viewModelScope.launch {
                repository.updateCoins(-250)
                companions.value.find { it.companionId == companionId }?.let { comp ->
                    repository.saveCompanionDirect(comp.copy(isUnlocked = true))
                }
            }
        }
    }

    // Boosters / Powerups clear mechanics within board
    fun useShuffleBooster() {
        val userVal = userProgress.value ?: return
        if (userVal.coins >= 100) {
            viewModelScope.launch {
                repository.updateCoins(-100)
                matchEngine.boosterPowerShuffle()
                _boardState.value = copyBoard(matchEngine.board)
            }
        }
    }

    fun useRainbowStrikeBooster() {
        val userVal = userProgress.value ?: return
        if (userVal.coins >= 200) {
            viewModelScope.launch {
                repository.updateCoins(-200)
                // Force turn random cell into Rainbow Ball
                val r = Random.nextInt(matchEngine.rows)
                val c = Random.nextInt(matchEngine.cols)
                matchEngine.board[r][c] = MatchPiece(
                    row = r, col = c,
                    type = CreatureType.RAINBOW,
                    special = SpecialType.MYTHIC_RAINBOW
                )
                _boardState.value = copyBoard(matchEngine.board)
            }
        }
    }

    // Store settings - VIP card purchases
    fun buyShopItem(coinsGained: Int, costDollars: Double) {
        viewModelScope.launch {
            repository.updateCoins(coinsGained)
        }
    }

    fun buyVipPass() {
        viewModelScope.launch {
            repository.activateVip()
            repository.updateCoins(2000) // VIP gift
        }
    }

    // Claim loyalty rewards
    fun claimDailyReward(streak: Int) {
        viewModelScope.launch {
            val coinsReward = streak * 100
            repository.updateCoins(coinsReward)
            // Save updated last login so timer does not duplicate
            val current = repository.getUserProgressSync()
            db.gameDao().saveUserState(current.copy(lastLoginTime = System.currentTimeMillis() - 86400000L * 2)) // advance
        }
    }

    // Spin Lucky Wheel Game
    fun spinLuckyWheel() {
        if (isWheelSpinning.value) return
        viewModelScope.launch {
            isWheelSpinning.value = true
            // Spin visual angle
            val targetDegrees = 1080f + Random.nextInt(360)
            wheelSelectionDegrees.value = targetDegrees
            delay(2800) // Anim duration
            
            // Evaluate sector
            val sector = ((targetDegrees % 360) / 60).toInt()
            val rewardCoins = when (sector) {
                0 -> 50
                1 -> 150
                2 -> 300
                3 -> 0 // Try Again
                4 -> 500
                else -> 100
            }
            if (rewardCoins > 0) {
                repository.updateCoins(rewardCoins)
            }
            isWheelSpinning.value = false
        }
    }

    fun selectCompanionCompanion(id: String) {
        viewModelScope.launch {
            repository.selectCompanion(id)
        }
    }

    fun earnCoinsFromAd(amount: Int) {
        viewModelScope.launch {
            repository.updateCoins(amount)
        }
    }

    fun wipeAndResetAllProgress() {
        viewModelScope.launch {
            db.clearAllTables()
            repository.checkInitializeDefaultData()
        }
    }

    // Reset gameplay sequence helper
    private fun copyBoard(src: Array<Array<MatchPiece>>): Array<Array<MatchPiece>> {
        return Array(src.size) { r ->
            Array(src[r].size) { c ->
                src[r][c].copy()
            }
        }
    }
}
