package com.example.game

import kotlin.math.abs
import kotlin.random.Random

enum class CreatureType(
    val idName: String,
    val displayName: String,
    val colorPrimary: Long,
    val colorSecondary: Long
) {
    FIRE("fire", "Fire Dragon", 0xFFFF2A2A, 0xFFFFA235),      // Pyro Red
    ICE("ice", "Ice Kitten", 0xFF00C3FF, 0xFF0073FF),        // Frosty Blue
    THUNDER("thunder", "Volt Wolf", 0xFFFFEB3B, 0xFFFF9800),  // Sparky Yellow
    SHADOW("shadow", "Shadow Imp", 0xFF9C27B0, 0xFF3F51B5),   // Umbria Purple
    GOLDEN_SNAKE("snake", "Golden Snake", 0xFFFFD700, 0xFFFF8F00), // Gold
    RAINBOW("rainbow", "Rainbow Phoenix", 0xFFFF0055, 0xFF44FF44) // Wild Card Multicolored
}

enum class SpecialType {
    NONE,
    LINE_ROW,       // Clears full row (Match 4 Horizontal)
    LINE_COLUMN,    // Clears full column (Match 4 Vertical)
    EXPLOSIVE,      // Clears 3x3 surrounding (Match T- or L-shaped)
    MYTHIC_RAINBOW  // Clears all of a selected color (Match 5)
}

data class MatchPiece(
    val id: Long = Random.nextLong(),
    var row: Int,
    var col: Int,
    val type: CreatureType,
    val special: SpecialType = SpecialType.NONE,
    var isMatched: Boolean = false,
    var displayOffsetRow: Float = 0f, // For smooth falling / swipe movement interpolation
    var displayOffsetCol: Float = 0f,
    var scale: Float = 1f // For spawn/pop animation scaling
)

class MatchThreeEngine(
    val rows: Int = 6,
    val cols: Int = 6
) {
    var board: Array<Array<MatchPiece>> = Array(rows) { r ->
        Array(cols) { c ->
            MatchPiece(row = r, col = c, type = CreatureType.values()[0])
        }
    }
    
    init {
        generateStableBoard()
    }

    // Generate a starting board that contains NO pre-formed match-3 paths!
    fun generateStableBoard() {
        var attempts = 0
        do {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val allowedTypes = CreatureType.values().toMutableList()
                    // Remove types that would trigger a Match-3 immediately
                    if (r >= 2 && board[r - 1][c].type == board[r - 2][c].type) {
                        allowedTypes.remove(board[r - 1][c].type)
                    }
                    if (c >= 2 && board[r][c - 1].type == board[r][c - 2].type) {
                        allowedTypes.remove(board[r][c - 1].type)
                    }
                    // Avoid full rainbow pieces in generation
                    allowedTypes.remove(CreatureType.RAINBOW)

                    val chosenType = allowedTypes[Random.nextInt(allowedTypes.size)]
                    board[r][c] = MatchPiece(row = r, col = c, type = chosenType)
                }
            }
            attempts++
        } while (hasPossibleMatches() == false && attempts < 100)
    }

    // Helper to evaluate if any swap yields a Match-3 (to check for "No More Moves" or ensure playable)
    private fun hasPossibleMatches(): Boolean {
        // Evaluate horizontal and vertical mock swaps for matches
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // Swap with right
                if (c + 1 < cols) {
                    if (testSwapAndCheck(r, c, r, c + 1)) return true
                }
                // Swap with down
                if (r + 1 < rows) {
                    if (testSwapAndCheck(r, c, r + 1, c)) return true
                }
            }
        }
        return false
    }

    private fun testSwapAndCheck(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        // Save
        val t1 = board[r1][c1].type
        val t2 = board[r2][c2].type
        // Mock swap
        board[r1][c1] = board[r1][c1].copy(type = t2)
        board[r2][c2] = board[r2][c2].copy(type = t1)
        val hasMatch = checkBoardForMatchPatterns(markMatched = false).isNotEmpty()
        // Restore
        board[r1][c1] = board[r1][c1].copy(type = t1)
        board[r2][c2] = board[r2][c2].copy(type = t2)
        return hasMatch
    }

    // Core board evaluation logic
    // Returns list of pieces that should match and trigger scoring
    fun checkBoardForMatchPatterns(markMatched: Boolean = false): List<MatchPiece> {
        val matchedPieces = mutableSetOf<MatchPiece>()

        // 1. Check Horizontal Matches
        for (r in 0 until rows) {
            var matchLen = 1
            var matchStartCol = 0
            for (c in 1 until cols) {
                if (board[r][c].type == board[r][c - 1].type && board[r][c].type != CreatureType.RAINBOW) {
                    matchLen++
                } else {
                    if (matchLen >= 3) {
                        for (i in matchStartCol until matchStartCol + matchLen) {
                            matchedPieces.add(board[r][i])
                        }
                    }
                    matchLen = 1
                    matchStartCol = c
                }
            }
            if (matchLen >= 3) {
                for (i in matchStartCol until matchStartCol + matchLen) {
                    matchedPieces.add(board[r][i])
                }
            }
        }

        // 2. Check Vertical Matches
        for (c in 0 until cols) {
            var matchLen = 1
            var matchStartRow = 0
            for (r in 1 until rows) {
                if (board[r][c].type == board[r - 1][c].type && board[r][c].type != CreatureType.RAINBOW) {
                    matchLen++
                } else {
                    if (matchLen >= 3) {
                        for (i in matchStartRow until matchStartRow + matchLen) {
                            matchedPieces.add(board[i][c])
                        }
                    }
                    matchLen = 1
                    matchStartRow = r
                }
            }
            if (matchLen >= 3) {
                for (i in matchStartRow until matchStartRow + matchLen) {
                    matchedPieces.add(board[i][c])
                }
            }
        }

        if (markMatched) {
            matchedPieces.forEach {
                board[it.row][it.col].isMatched = true
            }
        }

        return matchedPieces.toList()
    }

    // Swap adjacent cells with check and rollback if no matches
    // Returns pair: (list of matches created, true if valid match swipe occurred)
    fun attemptSwap(r1: Int, c1: Int, r2: Int, c2: Int): Pair<List<MatchPiece>, Boolean> {
        // Verify adjacency
        if (abs(r1 - r2) + abs(c1 - c2) != 1) return Pair(emptyList(), false)

        val p1 = board[r1][c1]
        val p2 = board[r2][c2]

        // Rainbow swap clear logic
        if (p1.type == CreatureType.RAINBOW || p2.type == CreatureType.RAINBOW) {
            val matches = mutableListOf<MatchPiece>()
            if (p1.type == CreatureType.RAINBOW && p2.type != CreatureType.RAINBOW) {
                matches.addAll(clearAllOfType(p2.type))
                matches.add(p1)
            } else if (p2.type == CreatureType.RAINBOW && p1.type != CreatureType.RAINBOW) {
                matches.addAll(clearAllOfType(p1.type))
                matches.add(p2)
            } else {
                // Dual rainbow swap clears entire board!
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        matches.add(board[r][c])
                    }
                }
            }
            matches.forEach { board[it.row][it.col].isMatched = true }
            return Pair(matches, true)
        }

        // Standard Swap
        board[r1][c1] = p2.copy(row = r1, col = c1)
        board[r2][c2] = p1.copy(row = r2, col = c2)

        val matched = checkBoardForMatchPatterns(markMatched = true)
        if (matched.isNotEmpty()) {
            // Apply special trigger checks before returning
            applySpecialCreators(matched, r1, c1, r2, c2)
            return Pair(matched, true)
        } else {
            // Rollback swap
            board[r1][c1] = p1
            board[r2][c2] = p2
            return Pair(emptyList(), false)
        }
    }

    // Checks matches geometry to convert a specific tile to a Special Ball (Horizontal/Vertical blaster, exploder, rainbow)
    private fun applySpecialCreators(matched: List<MatchPiece>, r1: Int, c1: Int, r2: Int, c2: Int) {
        // Group by row & col
        val groupedByRow = matched.groupBy { it.row }
        val groupedByCol = matched.groupBy { it.col }

        // Find intersection swap coordinate
        val targetRow = if (board[r1][c1].isMatched) r1 else r2
        val targetCol = if (board[r1][c1].isMatched) c1 else c2

        var specCreated = false

        // Match 5 -> Rainbow Ball Spark
        for ((col, list) in groupedByCol) {
            if (list.size >= 5 && !specCreated) {
                val tType = list.first().type
                board[targetRow][targetCol] = MatchPiece(
                    row = targetRow, col = targetCol,
                    type = CreatureType.RAINBOW,
                    special = SpecialType.MYTHIC_RAINBOW
                )
                specCreated = true
            }
        }
        for ((row, list) in groupedByRow) {
            if (list.size >= 5 && !specCreated) {
                board[targetRow][targetCol] = MatchPiece(
                    row = targetRow, col = targetCol,
                    type = CreatureType.RAINBOW,
                    special = SpecialType.MYTHIC_RAINBOW
                )
                specCreated = true
            }
        }

        // Match 4 Vertical -> Column Blast
        if (!specCreated) {
            for ((col, list) in groupedByCol) {
                if (list.size == 4) {
                    val type = list.first().type
                    board[targetRow][col] = MatchPiece(
                        row = targetRow, col = col,
                        type = type,
                        special = SpecialType.LINE_COLUMN
                    )
                    specCreated = true
                }
            }
        }

        // Match 4 Horizontal -> Row Blast
        if (!specCreated) {
            for ((row, list) in groupedByRow) {
                if (list.size == 4) {
                    val type = list.first().type
                    board[row][targetCol] = MatchPiece(
                        row = row, col = targetCol,
                        type = type,
                        special = SpecialType.LINE_ROW
                    )
                    specCreated = true
                }
            }
        }

        // T / L matches -> Explosive Bomb Blast
        if (!specCreated && matched.size >= 5) {
            // Check cross patterns
            val hasRowN = groupedByRow.values.any { it.size >= 3 }
            val hasColN = groupedByCol.values.any { it.size >= 3 }
            if (hasRowN && hasColN) {
                val type = matched.first().type
                board[targetRow][targetCol] = MatchPiece(
                    row = targetRow, col = targetCol,
                    type = type,
                    special = SpecialType.EXPLOSIVE
                )
            }
        }
    }

    private fun clearAllOfType(type: CreatureType): List<MatchPiece> {
        val list = mutableListOf<MatchPiece>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c].type == type) {
                    list.add(board[r][c])
                }
            }
        }
        return list
    }

    // Processes standard matches and chain specials
    // Returns full consolidated list of blasted pieces
    fun processMatchedTriggers(): List<MatchPiece> {
        val fullyBlasted = mutableSetOf<MatchPiece>()
        val queue = mutableListOf<MatchPiece>()

        // Add all base matched items to queue
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c].isMatched) {
                    queue.add(board[r][c])
                }
            }
        }

        // Resolve special chain blasts
        while (queue.isNotEmpty()) {
            val curr = queue.removeAt(0)
            if (curr in fullyBlasted) continue
            fullyBlasted.add(curr)

            // Trigger Special powers!
            when (curr.special) {
                SpecialType.LINE_ROW -> {
                    // Blast entire row
                    for (c in 0 until cols) {
                        val victim = board[curr.row][c]
                        if (victim !in fullyBlasted) {
                            queue.add(victim)
                        }
                    }
                }
                SpecialType.LINE_COLUMN -> {
                    // Blast entire column
                    for (r in 0 until rows) {
                        val victim = board[r][curr.col]
                        if (victim !in fullyBlasted) {
                            queue.add(victim)
                        }
                    }
                }
                SpecialType.EXPLOSIVE -> {
                    // Blast 3x3 surrounding cells
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = curr.row + dr
                            val nc = curr.col + dc
                            if (nr in 0 until rows && nc in 0 until cols) {
                                val victim = board[nr][nc]
                                if (victim !in fullyBlasted) {
                                    queue.add(victim)
                                }
                            }
                        }
                    }
                }
                SpecialType.MYTHIC_RAINBOW -> {
                    // Wild random color cascade
                    val randomTarget = CreatureType.values()[Random.nextInt(CreatureType.values().size - 1)]
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val victim = board[r][c]
                            if (victim.type == randomTarget && victim !in fullyBlasted) {
                                queue.add(victim)
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        // Clean matched states
        fullyBlasted.forEach {
            board[it.row][it.col] = board[it.row][it.col].copy(isMatched = true)
        }

        return fullyBlasted.toList()
    }

    // Shift elements down (cascade gravity)
    // Returns index details of drops, needed for spawning slide animations
    // Triple: (id, fromRow, toRow)
    fun applyGravity(): List<Triple<Long, Int, Int>> {
        val movements = mutableListOf<Triple<Long, Int, Int>>()

        for (c in 0 until cols) {
            // Read from bottom to top
            var emptySlots = 0
            for (r in rows - 1 downTo 0) {
                if (board[r][c].isMatched) {
                    emptySlots++
                } else if (emptySlots > 0) {
                    // Shift piece down by emptySlots counts
                    val sourcePiece = board[r][c]
                    val targetRow = r + emptySlots
                    
                    movements.add(Triple(sourcePiece.id, r, targetRow))
                    
                    // Move in physical array data
                    board[targetRow][c] = sourcePiece.copy(row = targetRow, displayOffsetRow = -emptySlots.toFloat())
                    board[r][c] = MatchPiece(row = r, col = c, type = CreatureType.values()[0], isMatched = true) // Temp empty/invalid
                }
            }
        }
        return movements
    }

    // Refill top empty cells with fresh adorable creature balls!
    // Returns lists of newly spawned assets for animations
    fun refillEmptyTopSlots(): List<MatchPiece> {
        val spawns = mutableListOf<MatchPiece>()
        for (c in 0 until cols) {
            var refillCount = 0
            for (r in 0 until rows) {
                if (board[r][c].isMatched) {
                    refillCount++
                }
            }
            // Spawn them from top (out of bounds row negative offsets)
            var currentRefillIdx = 1
            for (r in 0 until rows) {
                if (board[r][c].isMatched) {
                    // Pick random cute creature element
                    val excludedArray = CreatureType.values().toMutableList()
                    excludedArray.remove(CreatureType.RAINBOW) // Spawns standard
                    val rndType = excludedArray[Random.nextInt(excludedArray.size)]
                    
                    val newPiece = MatchPiece(
                        row = r, col = c,
                        type = rndType,
                        displayOffsetRow = -currentRefillIdx.toFloat(), // Slides down visually
                        scale = 0f // Pop in visually
                    )
                    board[r][c] = newPiece
                    spawns.add(newPiece)
                    currentRefillIdx++
                }
            }
        }
        return spawns
    }

    // Active boosters / Dragon Power actions
    fun boosterPowerClearSingle(r: Int, c: Int): List<MatchPiece> {
        val p = board[r][c]
        board[r][c] = p.copy(isMatched = true)
        val cleared = processMatchedTriggers()
        return cleared
    }

    fun boosterPowerShuffle(): List<MatchPiece> {
        // Collect current tiles and shuffle types
        val types = mutableListOf<CreatureType>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c].type != CreatureType.RAINBOW) {
                    types.add(board[r][c].type)
                }
            }
        }
        types.shuffle()
        
        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c].type != CreatureType.RAINBOW) {
                    board[r][c] = board[r][c].copy(type = types[idx], isMatched = false)
                    idx++
                }
            }
        }
        return emptyList()
    }
}
