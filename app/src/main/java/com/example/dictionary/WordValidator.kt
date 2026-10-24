package com.example.dictionary

import com.example.model.Board
import com.example.model.PlacedTile
import com.example.model.PremiumSquare
import com.example.model.Tile

data class ValidationResult(
    val isValid: Boolean,
    val primaryWord: String = "",
    val allFormedWords: List<String> = emptyList(),
    val totalScore: Int = 0,
    val isBingo: Boolean = false,
    val errorMessage: String? = null,
    val invalidWords: List<String> = emptyList()
)

object WordValidator {

    fun validateAndScore(
        board: Board,
        placedTiles: List<PlacedTile>,
        isMultiplayerChallengeMode: Boolean = false
    ): ValidationResult {
        if (placedTiles.isEmpty()) {
            return ValidationResult(isValid = false, errorMessage = "No tiles placed on the board.")
        }

        // Check bounds and overlap
        for (pt in placedTiles) {
            if (pt.row !in 0 until Board.BOARD_SIZE || pt.col !in 0 until Board.BOARD_SIZE) {
                return ValidationResult(isValid = false, errorMessage = "Tile placed outside board boundaries.")
            }
            if (board.getSquare(pt.row, pt.col)?.isOccupied == true) {
                return ValidationResult(isValid = false, errorMessage = "Square at (${pt.row}, ${pt.col}) is already occupied.")
            }
        }

        val isBoardEmpty = board.isEmpty()

        // First move must cover the center star (7, 7)
        if (isBoardEmpty) {
            val coversCenter = placedTiles.any { it.row == Board.CENTER && it.col == Board.CENTER }
            if (!coversCenter) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "First word must cover the center star (★) at row 8, column 8."
                )
            }
            if (placedTiles.size < 2) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "First word must be at least 2 letters long."
                )
            }
        }

        // Determine orientation: all tiles must be in same row or same col
        val allSameRow = placedTiles.all { it.row == placedTiles[0].row }
        val allSameCol = placedTiles.all { it.col == placedTiles[0].col }

        if (!allSameRow && !allSameCol) {
            return ValidationResult(
                isValid = false,
                errorMessage = "All placed tiles must be in the same row or column."
            )
        }

        val isHorizontal = if (placedTiles.size == 1) {
            // Check adjacent board tiles to determine likely orientation
            val r = placedTiles[0].row
            val c = placedTiles[0].col
            val hasHorizontalNeighbor = (c > 0 && board.getSquare(r, c - 1)?.isOccupied == true) ||
                    (c < Board.BOARD_SIZE - 1 && board.getSquare(r, c + 1)?.isOccupied == true)
            hasHorizontalNeighbor
        } else {
            allSameRow
        }

        // Check continuity and connectivity
        val placedMap = placedTiles.associateBy { Pair(it.row, it.col) }

        if (isHorizontal) {
            val row = placedTiles[0].row
            val minCol = placedTiles.minOf { it.col }
            val maxCol = placedTiles.maxOf { it.col }

            // Ensure no empty gaps between placed tiles
            for (c in minCol..maxCol) {
                val hasPlaced = placedMap.containsKey(Pair(row, c))
                val hasExisting = board.getSquare(row, c)?.isOccupied == true
                if (!hasPlaced && !hasExisting) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Placed tiles cannot have empty spaces between them."
                    )
                }
            }
        } else {
            val col = placedTiles[0].col
            val minRow = placedTiles.minOf { it.row }
            val maxRow = placedTiles.maxOf { it.row }

            // Ensure no empty gaps
            for (r in minRow..maxRow) {
                val hasPlaced = placedMap.containsKey(Pair(r, col))
                val hasExisting = board.getSquare(r, col)?.isOccupied == true
                if (!hasPlaced && !hasExisting) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Placed tiles cannot have empty spaces between them."
                    )
                }
            }
        }

        // Check connectivity with existing tiles (if not empty board)
        if (!isBoardEmpty) {
            var touchesExisting = false
            for (pt in placedTiles) {
                val neighbors = listOf(
                    Pair(pt.row - 1, pt.col),
                    Pair(pt.row + 1, pt.col),
                    Pair(pt.row, pt.col - 1),
                    Pair(pt.row, pt.col + 1)
                )
                if (neighbors.any { (r, c) ->
                        r in 0 until Board.BOARD_SIZE && c in 0 until Board.BOARD_SIZE &&
                                board.getSquare(r, c)?.isOccupied == true
                    }) {
                    touchesExisting = true
                    break
                }
            }

            if (!touchesExisting) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Words must connect to existing tiles on the board."
                )
            }
        }

        // Extract primary word and all cross words formed
        val formedWords = mutableListOf<String>()
        var totalPoints = 0

        // 1. Primary Word
        val (primaryWord, primaryScore) = extractAndScoreWord(
            board = board,
            placedMap = placedMap,
            startRow = placedTiles[0].row,
            startCol = placedTiles[0].col,
            isHorizontal = isHorizontal
        )

        if (primaryWord.length >= 2) {
            formedWords.add(primaryWord)
            totalPoints += primaryScore
        }

        // 2. Perpendicular Cross Words for each placed tile
        for (pt in placedTiles) {
            val (crossWord, crossScore) = extractAndScoreWord(
                board = board,
                placedMap = placedMap,
                startRow = pt.row,
                startCol = pt.col,
                isHorizontal = !isHorizontal
            )
            if (crossWord.length >= 2) {
                formedWords.add(crossWord)
                totalPoints += crossScore
            }
        }

        if (formedWords.isEmpty()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Move must form at least one valid word of 2 or more letters."
            )
        }

        // Check CSW24 validity for all formed words
        val invalidWords = formedWords.filter { !CSW24Dictionary.isValid(it) }

        // BINGO: 50 point bonus if player used all 7 rack tiles!
        val isBingo = placedTiles.size == 7
        if (isBingo) {
            totalPoints += 50
        }

        if (invalidWords.isNotEmpty() && !isMultiplayerChallengeMode) {
            // In standard vs AI or offline play, block invalid words right away
            return ValidationResult(
                isValid = false,
                primaryWord = primaryWord,
                allFormedWords = formedWords,
                totalScore = totalPoints,
                isBingo = isBingo,
                errorMessage = "Invalid word(s) in CSW24: ${invalidWords.joinToString(", ")}",
                invalidWords = invalidWords
            )
        }

        return ValidationResult(
            isValid = true,
            primaryWord = primaryWord,
            allFormedWords = formedWords,
            totalScore = totalPoints,
            isBingo = isBingo,
            invalidWords = invalidWords
        )
    }

    private fun extractAndScoreWord(
        board: Board,
        placedMap: Map<Pair<Int, Int>, PlacedTile>,
        startRow: Int,
        startCol: Int,
        isHorizontal: Boolean
    ): Pair<String, Int> {
        var rStart = startRow
        var cStart = startCol

        // Scan backwards to find word start
        if (isHorizontal) {
            while (cStart > 0 && isSquareOccupied(board, placedMap, rStart, cStart - 1)) {
                cStart--
            }
        } else {
            while (rStart > 0 && isSquareOccupied(board, placedMap, rStart - 1, cStart)) {
                rStart--
            }
        }

        // Scan forwards to build word and calculate score
        val wordChars = StringBuilder()
        var letterScoreSum = 0
        var wordMultiplier = 1

        var r = rStart
        var c = cStart

        while (r in 0 until Board.BOARD_SIZE && c in 0 until Board.BOARD_SIZE && isSquareOccupied(board, placedMap, r, c)) {
            val newlyPlaced = placedMap[Pair(r, c)]
            if (newlyPlaced != null) {
                val tile = newlyPlaced.tile
                val char = tile.displayLetter
                wordChars.append(char)

                val premium = Board.getPremium(r, c)
                val tileValue = if (tile.isBlank) 0 else tile.value

                letterScoreSum += tileValue * premium.letterMultiplier
                wordMultiplier *= premium.wordMultiplier
            } else {
                val existing = board.getSquare(r, c)?.tile
                if (existing != null) {
                    wordChars.append(existing.displayLetter)
                    val tileValue = if (existing.isBlank) 0 else existing.value
                    // Multipliers do not apply to existing tiles
                    letterScoreSum += tileValue
                }
            }

            if (isHorizontal) c++ else r++
        }

        val word = wordChars.toString()
        val score = if (word.length >= 2) letterScoreSum * wordMultiplier else 0
        return Pair(word, score)
    }

    private fun isSquareOccupied(
        board: Board,
        placedMap: Map<Pair<Int, Int>, PlacedTile>,
        row: Int,
        col: Int
    ): Boolean {
        if (row !in 0 until Board.BOARD_SIZE || col !in 0 until Board.BOARD_SIZE) return false
        return placedMap.containsKey(Pair(row, col)) || board.getSquare(row, col)?.isOccupied == true
    }
}
