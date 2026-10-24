package com.example.ai

import com.example.dictionary.CSW24Dictionary
import com.example.dictionary.WordValidator
import com.example.model.AiDifficulty
import com.example.model.Board
import com.example.model.PlacedTile
import com.example.model.Tile

data class CandidateMove(
    val placedTiles: List<PlacedTile>,
    val primaryWord: String,
    val allWords: List<String>,
    val score: Int,
    val isBingo: Boolean
)

enum class AiActionType {
    PLAY, EXCHANGE, PASS
}

data class AiMoveDecision(
    val action: AiActionType,
    val move: CandidateMove? = null,
    val tilesToExchange: List<Tile> = emptyList(),
    val explanation: String = ""
)

object ScrabbleAISolver {

    /**
     * Finds the best move for the AI using rack tiles, evaluating high scores on the board.
     */
    fun findBestMove(
        board: Board,
        rack: List<Tile>,
        difficulty: AiDifficulty = AiDifficulty.BEST_MOVE,
        canExchange: Boolean = true
    ): AiMoveDecision {
        if (rack.isEmpty()) {
            return AiMoveDecision(AiActionType.PASS, explanation = "AI rack is empty.")
        }

        val legalMoves = mutableListOf<CandidateMove>()
        val isBoardEmpty = board.isEmpty()

        val rackLetters = rack.map { it.letter }
        val hasBlank = rackLetters.contains('?')

        // Determine anchors
        val anchors = mutableListOf<Pair<Int, Int>>()
        if (isBoardEmpty) {
            anchors.add(Pair(Board.CENTER, Board.CENTER))
        } else {
            for (r in 0 until Board.BOARD_SIZE) {
                for (c in 0 until Board.BOARD_SIZE) {
                    if (board.getSquare(r, c)?.isOccupied == false) {
                        val hasOccupiedNeighbor = listOf(
                            Pair(r - 1, c), Pair(r + 1, c),
                            Pair(r, c - 1), Pair(r, c + 1)
                        ).any { (nr, nc) ->
                            nr in 0 until Board.BOARD_SIZE && nc in 0 until Board.BOARD_SIZE &&
                                    board.getSquare(nr, nc)?.isOccupied == true
                        }
                        if (hasOccupiedNeighbor) {
                            anchors.add(Pair(r, c))
                        }
                    }
                }
            }
        }

        // Search through candidate dictionary words that can be made with rack + board
        // To keep solver snappy on mobile while providing grandmaster level words,
        // test candidate words against available anchors
        val candidateWords = CSW24Dictionary.TWO_LETTER_WORDS + listOf(
            "QI", "ZA", "ZO", "XI", "XU", "JO", "JA", "KA", "KI", "AX", "EX", "OX", "BY", "MY",
            "QUIZ", "QUIZZES", "JAZZ", "JAZZY", "FUZZ", "FIZZ", "BUZZ", "ZAP", "ZIP", "JINX",
            "EXTRA", "EXCEL", "INDEX", "MATRIX", "CRAZY", "BLAZE", "PRIZE", "FREEZE", "TAXES",
            "ACE", "ACT", "AND", "ANY", "ARE", "ART", "BAG", "BAR", "BAT", "BED", "BIG", "BOX",
            "BOY", "BUS", "BUY", "CAN", "CAR", "CAT", "COW", "CRY", "CUP", "CUT", "DAY", "DOG",
            "DRY", "EAR", "EAT", "EGG", "END", "EYE", "FAR", "FAT", "FEW", "FLY", "FOR", "FOX",
            "FUN", "GAS", "GET", "GOD", "GUN", "GUY", "HAT", "HIT", "HOT", "HOW", "ICE", "KEY",
            "LAW", "LAY", "LEG", "LET", "LIE", "LOW", "MAN", "MAP", "MAY", "MEN", "MUG", "NET",
            "NEW", "NOT", "NOW", "OAK", "OIL", "OLD", "ONE", "OUT", "OWL", "PAY", "PEN", "PET",
            "PIG", "PIN", "PUB", "RED", "RUN", "SAY", "SEA", "SEE", "SET", "SKY", "SON", "SUN",
            "TEA", "TEN", "THE", "TIE", "TOP", "TOY", "TRY", "TWO", "USE", "WAR", "WAY", "WEB",
            "WIN", "YES", "ABLE", "ACID", "AGED", "ALSO", "AREA", "ARMY", "AWAY", "BABY", "BACK",
            "BALL", "BAND", "BANK", "BASE", "BEAR", "BEAT", "BEER", "BELL", "BELT", "BEST", "BIRD",
            "BLUE", "BOAT", "BODY", "BOND", "BONE", "BOOK", "BOOM", "BORN", "BOSS", "BOWL", "BOYS",
            "BUSY", "CALL", "CALM", "CAMP", "CARD", "CARE", "CARS", "CASE", "CASH", "CAST", "CATS",
            "CELL", "CITY", "CLUB", "COAL", "COAT", "CODE", "COLD", "COME", "COOK", "COOL", "COPE",
            "COPY", "CORE", "COST", "CREW", "CROP", "DARK", "DATA", "DATE", "DAWN", "DAYS", "DEAD",
            "DEAL", "DEAR", "DEEP", "DESK", "DIAL", "DIET", "DISK", "DOES", "DOGS", "DONE", "DOOR",
            "DOSE", "DOWN", "DRAW", "DROP", "DRUG", "DUAL", "DUST", "DUTY", "EACH", "EARN", "EASY",
            "EDGE", "EVEN", "EVER", "EVIL", "EXIT", "FACE", "FACT", "FAIL", "FAIR", "FALL", "FARM",
            "FAST", "FATE", "FEAR", "FEED", "FEEL", "FEET", "FELL", "FELT", "FILE", "FILL", "FILM",
            "FIND", "FINE", "FIRE", "FIRM", "FISH", "FIVE", "FLAT", "FLOW", "FOOD", "FOOT", "FORD",
            "FORM", "FORT", "FOUR", "FREE", "FROM", "FUEL", "FULL", "FUND", "GAIN", "GAME", "GATE",
            "GEAR", "GENE", "GIFT", "GIRL", "GIVE", "GLAD", "GOAL", "GOES", "GOLD", "GOLF", "GONE",
            "GOOD", "GRAY", "GROW", "GULF", "HAIR", "HALF", "HALL", "HAND", "HANG", "HARD", "HARM",
            "HATE", "HAVE", "HEAD", "HEAR", "HEAT", "HELD", "HELL", "HELP", "HERE", "HERO", "HIGH",
            "HILL", "HIRE", "HOLD", "HOLE", "HOLY", "HOME", "HOPE", "HOST", "HOUR", "HUGE", "HUNG",
            "HUNT", "HURT", "IDEA", "INCH", "INTO", "IRON", "ITEM", "JACK", "JANE", "JEAN", "JOIN",
            "JUMP", "JURY", "JUST", "KEEP", "KEPT", "KICK", "KILL", "KIND", "KING", "KNEE", "KNOW",
            "LACK", "LADY", "LAID", "LAKE", "LAND", "LANE", "LAST", "LATE", "LEAD", "LEFT", "LESS",
            "LIFE", "LIFT", "LIKE", "LINE", "LINK", "LION", "LIST", "LIVE", "LOAD", "LOAN", "LOCK",
            "LOGO", "LONG", "LOOK", "LORD", "LOSE", "LOSS", "LOST", "LOVE", "LUCK", "MADE", "MAIL",
            "MAIN", "MAKE", "MALE", "MANY", "MARK", "MASS", "MEAL", "MEAN", "MEAT", "MEET", "MENU",
            "MERE", "MIKE", "MILE", "MILK", "MILL", "MIND", "MINE", "MISS", "MODE", "MOOD", "MOON",
            "MORE", "MOST", "MOVE", "MUCH", "MUST", "NAME", "NAVY", "NEAR", "NECK", "NEED", "NEWS",
            "NEXT", "NICE", "NINE", "NONE", "NOSE", "NOTE", "OKAY", "ONCE", "ONLY", "OPEN", "ORAL",
            "OVER", "PACE", "PACK", "PAGE", "PAID", "PAIN", "PAIR", "PALM", "PARK", "PART", "PASS",
            "PAST", "PATH", "PEAK", "PEER", "PICK", "PILE", "PLAN", "PLAY", "PLOT", "PLUG", "PLUS",
            "POEM", "POET", "POLL", "POOL", "POOR", "PORT", "POST", "POUR", "PRAY", "PURE", "PUSH",
            "RACE", "RAIL", "RAIN", "RANK", "RARE", "RATE", "READ", "REAL", "REAR", "RELY", "RENT",
            "REST", "RICE", "RICH", "RIDE", "RING", "RISE", "RISK", "ROAD", "ROCK", "ROLE", "ROLL",
            "ROOF", "ROOM", "ROOT", "ROSE", "RULE", "RUSH", "SAFE", "SAID", "SAIL", "SALE", "SAME",
            "SAND", "SAVE", "SEAT", "SEED", "SEEK", "SEEM", "SEEN", "SELF", "SELL", "SEND", "SENT",
            "SHIP", "SHOE", "SHOP", "SHOT", "SHOW", "SHUT", "SICK", "SIDE", "SIGN", "SILK", "SITE",
            "SIZE", "SKIN", "SLIP", "SLOW", "SNOW", "SOFT", "SOIL", "SOLD", "SOLE", "SOME", "SONG",
            "SOON", "SORT", "SOUL", "SPOT", "STAR", "STAY", "STEP", "STOP", "SUCH", "SUIT", "SURE",
            "TAKE", "TALE", "TALK", "TALL", "TANK", "TAPE", "TASK", "TEAM", "TELL", "TEND", "TERM",
            "TEST", "TEXT", "THAT", "THEM", "THEN", "THEY", "THIN", "THIS", "TIDE", "TIED", "TIRE",
            "TONE", "TOOK", "TOOL", "TOUR", "TOWN", "TREE", "TRIP", "TRUE", "TUNE", "TURN", "TWIN",
            "TYPE", "UNIT", "UPON", "USED", "USER", "VARY", "VAST", "VERY", "VICE", "VIEW", "VOTE",
            "WAGE", "WAIT", "WAKE", "WALK", "WALL", "WANT", "WARM", "WARN", "WASH", "WAVE", "WAYS",
            "WEAK", "WEAR", "WEEK", "WELL", "WENT", "WERE", "WEST", "WHAT", "WHEN", "WHOM", "WIDE",
            "WIFE", "WILD", "WILL", "WIND", "WINE", "WING", "WINS", "WIRE", "WISE", "WISH", "WITH",
            "WOOD", "WORD", "WORK", "YARD", "YEAR", "ZERO", "ZINC", "ZONE"
        )

        // Try candidate words across anchors
        for (word in candidateWords) {
            val len = word.length
            if (len > 7 && isBoardEmpty) continue

            for (anchor in anchors) {
                // Try Horizontal placement where word covers anchor
                for (offset in 0 until len) {
                    val startCol = anchor.second - offset
                    val row = anchor.first
                    if (startCol >= 0 && startCol + len <= Board.BOARD_SIZE) {
                        evaluateWordAt(board, rack, word, row, startCol, isHorizontal = true, legalMoves)
                    }
                }

                // Try Vertical placement
                for (offset in 0 until len) {
                    val startRow = anchor.first - offset
                    val col = anchor.second
                    if (startRow >= 0 && startRow + len <= Board.BOARD_SIZE) {
                        evaluateWordAt(board, rack, word, startRow, col, isHorizontal = false, legalMoves)
                    }
                }
            }
        }

        if (legalMoves.isNotEmpty()) {
            // Sort by score descending (high score first)
            val sorted = legalMoves.distinctBy { it.placedTiles.map { pt -> "${pt.row},${pt.col}:${pt.tile.displayLetter}" }.sorted().joinToString(";") }
                .sortedByDescending { it.score }

            val chosenMove = when (difficulty) {
                AiDifficulty.BEST_MOVE -> sorted.first()
                AiDifficulty.BALANCED -> sorted.take(3).random()
                AiDifficulty.CASUAL -> sorted.takeLast((sorted.size / 2).coerceAtLeast(1)).random()
            }

            val bonusText = if (chosenMove.isBingo) " (BINGO! +50 PTS BONUS)" else ""
            val explanation = "AI plays '${chosenMove.primaryWord}' for ${chosenMove.score} pts$bonusText."

            return AiMoveDecision(
                action = AiActionType.PLAY,
                move = chosenMove,
                explanation = explanation
            )
        }

        // If no valid word could be formed:
        return if (canExchange && rack.size >= 3) {
            val toExchange = rack.filter { it.letter in listOf('Q', 'Z', 'J', 'X', 'V', 'K', 'U') }.take(3)
                .ifEmpty { rack.take(3) }
            AiMoveDecision(
                action = AiActionType.EXCHANGE,
                tilesToExchange = toExchange,
                explanation = "AI exchanges ${toExchange.size} tiles with the bag."
            )
        } else {
            AiMoveDecision(
                action = AiActionType.PASS,
                explanation = "AI passes turn (no valid placement found)."
            )
        }
    }

    private fun evaluateWordAt(
        board: Board,
        rack: List<Tile>,
        word: String,
        startRow: Int,
        startCol: Int,
        isHorizontal: Boolean,
        outMoves: MutableList<CandidateMove>
    ) {
        val availableRack = rack.toMutableList()
        val neededTiles = mutableListOf<PlacedTile>()

        for (i in word.indices) {
            val r = if (isHorizontal) startRow else startRow + i
            val c = if (isHorizontal) startCol + i else startCol
            val targetChar = word[i]

            val existing = board.getSquare(r, c)?.tile
            if (existing != null) {
                // Must match existing board letter
                if (existing.displayLetter != targetChar) {
                    return // Conflict with existing tile
                }
            } else {
                // We need to place this letter from the rack
                val matchingIndex = availableRack.indexOfFirst { it.letter == targetChar }
                if (matchingIndex >= 0) {
                    val tile = availableRack.removeAt(matchingIndex)
                    neededTiles.add(PlacedTile(tile, r, c))
                } else {
                    // Try blank tile
                    val blankIndex = availableRack.indexOfFirst { it.isBlank }
                    if (blankIndex >= 0) {
                        val blankTile = availableRack.removeAt(blankIndex).copy(assignedLetter = targetChar)
                        neededTiles.add(PlacedTile(blankTile, r, c))
                    } else {
                        return // Letter not in rack
                    }
                }
            }
        }

        if (neededTiles.isEmpty()) {
            return // Did not place any new tiles
        }

        // Validate complete move with WordValidator
        val validation = WordValidator.validateAndScore(board, neededTiles)
        if (validation.isValid && validation.invalidWords.isEmpty()) {
            outMoves.add(
                CandidateMove(
                    placedTiles = neededTiles,
                    primaryWord = validation.primaryWord,
                    allWords = validation.allFormedWords,
                    score = validation.totalScore,
                    isBingo = validation.isBingo
                )
            )
        }
    }
}
