package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiActionType
import com.example.ai.ScrabbleAISolver
import com.example.data.GameSessionStorage
import com.example.data.SavedAiGameSession
import com.example.dictionary.CSW24Dictionary
import com.example.dictionary.WordValidator
import com.example.model.AiDifficulty
import com.example.model.Board
import com.example.model.ChallengeState
import com.example.model.GameMode
import com.example.model.GameMove
import com.example.model.LobbyPlayer
import com.example.model.MultiplayerRoom
import com.example.model.PlacedTile
import com.example.model.Player
import com.example.model.RoomStatus
import com.example.model.Tile
import com.example.model.TileBag
import com.example.ai.CandidateMove
import com.example.audio.ScrabbleSoundEngine
import com.example.model.BestMoveReviewSettings
import com.example.model.TurnReviewRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class BingoCelebrationEvent(
    val playerName: String,
    val word: String,
    val points: Int
)

data class ScrabbleUiState(
    val currentScreen: String = "home", // "home", "lobby", "game", "game_over"
    val gameMode: GameMode = GameMode.VS_AI,
    val aiDifficulty: AiDifficulty = AiDifficulty.BEST_MOVE,
    val board: Board = Board(),
    val tileBag: TileBag = TileBag(),
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val placedTiles: List<PlacedTile> = emptyList(),
    val selectedRackTile: Tile? = null,
    val pendingBlankSquare: Pair<Int, Int>? = null,
    val challengeState: ChallengeState? = null,
    val bingoCelebration: BingoCelebrationEvent? = null,
    val moveHistory: List<GameMove> = emptyList(),
    val isGameOver: Boolean = false,
    val winner: Player? = null,
    val statusMessage: String? = null,
    val isAiThinking: Boolean = false,
    val multiplayerRoom: MultiplayerRoom? = null,
    val consecutivePasses: Int = 0,
    val hasSavedAiSession: Boolean = false,
    val savedAiSession: SavedAiGameSession? = null,
    val bestMoveSettings: BestMoveReviewSettings = BestMoveReviewSettings(),
    val turnReviews: List<TurnReviewRecord> = emptyList(),
    val currentTurnBestMoveHint: CandidateMove? = null
) {
    val currentPlayer: Player?
        get() = players.getOrNull(currentPlayerIndex)

    val isCurrentPlayerHuman: Boolean
        get() = currentPlayer?.isAi == false
}

class ScrabbleViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionStorage = GameSessionStorage(application)
    private val _uiState = MutableStateFlow(ScrabbleUiState())
    val uiState: StateFlow<ScrabbleUiState> = _uiState.asStateFlow()

    init {
        checkSavedSession()
    }

    fun checkSavedSession() {
        val hasSession = sessionStorage.hasSavedSession()
        val session = if (hasSession) sessionStorage.loadSession() else null
        _uiState.update {
            it.copy(
                hasSavedAiSession = (session != null),
                savedAiSession = session
            )
        }
    }

    fun navigateTo(screen: String) {
        _uiState.update { it.copy(currentScreen = screen) }
        checkSavedSession()
    }

    fun resumeSavedAiGame() {
        val saved = sessionStorage.loadSession() ?: return
        val hint = if (saved.currentPlayerIndex == 0) {
            ScrabbleAISolver.findBestMove(saved.board, saved.humanPlayer.rack, AiDifficulty.BEST_MOVE).move
        } else null

        _uiState.update {
            it.copy(
                currentScreen = "game",
                gameMode = GameMode.VS_AI,
                aiDifficulty = saved.difficulty,
                board = saved.board,
                tileBag = saved.tileBag,
                players = listOf(saved.humanPlayer, saved.aiPlayer),
                currentPlayerIndex = saved.currentPlayerIndex,
                moveHistory = saved.moveHistory,
                placedTiles = emptyList(),
                selectedRackTile = null,
                isGameOver = false,
                currentTurnBestMoveHint = hint,
                statusMessage = "Permainan dilanjutkan! Giliran ke-${saved.moveHistory.size + 1}."
            )
        }

        // If it was AI's turn when saved, continue AI turn
        if (saved.currentPlayerIndex == 1) {
            executeAiTurn()
        }
    }

    fun startVsAiGame(difficulty: AiDifficulty = AiDifficulty.BEST_MOVE, playerName: String = "Player 1") {
        sessionStorage.clearSession()

        val bag = TileBag()
        val humanRack = bag.drawTiles(7)
        val aiRack = bag.drawTiles(7)

        val player1 = Player(id = "p1", name = playerName, rack = humanRack, isAi = false, colorHex = 0xFF1B4D3E)
        val aiPlayer = Player(id = "ai", name = "AI (${difficulty.label.split(" ")[0]})", rack = aiRack, isAi = true, colorHex = 0xFF8D6E63)

        val initialBoard = Board()
        val players = listOf(player1, aiPlayer)
        val initialHint = ScrabbleAISolver.findBestMove(initialBoard, humanRack, AiDifficulty.BEST_MOVE).move

        _uiState.value = ScrabbleUiState(
            currentScreen = "game",
            gameMode = GameMode.VS_AI,
            aiDifficulty = difficulty,
            board = initialBoard,
            tileBag = bag,
            players = players,
            currentPlayerIndex = 0,
            hasSavedAiSession = false,
            savedAiSession = null,
            currentTurnBestMoveHint = initialHint,
            statusMessage = "Game started vs AI! Cover the center star (★) to begin."
        )

        autoSaveAiSession()
    }

    fun initMultiplayerHost(hostName: String = "Player 1") {
        val room = MultiplayerRoom(
            players = listOf(
                LobbyPlayer(name = hostName, isHost = true, avatarColor = 0xFF1B4D3E)
            )
        )
        _uiState.update {
            it.copy(
                currentScreen = "lobby",
                gameMode = GameMode.MULTIPLAYER,
                multiplayerRoom = room,
                statusMessage = "Room created! Share barcode/code to invite up to 4 players."
            )
        }
    }

    fun addPlayerToLobby(name: String, isAi: Boolean = false) {
        val room = _uiState.value.multiplayerRoom ?: return
        if (room.players.size >= room.maxPlayers) return

        val colors = listOf(0xFF1565C0, 0xFFC62828, 0xFFE65100, 0xFF4A148C)
        val newPlayer = LobbyPlayer(
            name = name,
            isHost = false,
            isAi = isAi,
            avatarColor = colors.getOrElse(room.players.size) { 0xFF004D40 }
        )
        val updatedPlayers = room.players + newPlayer
        _uiState.update {
            it.copy(multiplayerRoom = room.copy(players = updatedPlayers))
        }
    }

    fun removePlayerFromLobby(playerId: String) {
        val room = _uiState.value.multiplayerRoom ?: return
        val updated = room.players.filterNot { it.id == playerId && !it.isHost }
        _uiState.update { it.copy(multiplayerRoom = room.copy(players = updated)) }
    }

    fun startMultiplayerGame() {
        val room = _uiState.value.multiplayerRoom ?: return
        if (room.players.size < 2) return

        val bag = TileBag()
        val gamePlayers = room.players.mapIndexed { index, lp ->
            Player(
                id = lp.id,
                name = lp.name,
                rack = bag.drawTiles(7),
                isAi = lp.isAi,
                colorHex = lp.avatarColor
            )
        }

        _uiState.update {
            it.copy(
                currentScreen = "game",
                gameMode = GameMode.MULTIPLAYER,
                board = Board(),
                tileBag = bag,
                players = gamePlayers,
                currentPlayerIndex = 0,
                multiplayerRoom = room.copy(status = RoomStatus.GAME_IN_PROGRESS),
                statusMessage = "${gamePlayers[0].name}'s turn. First word must cover center star (★)!"
            )
        }
    }

    fun updateBestMoveSettings(newSettings: BestMoveReviewSettings) {
        _uiState.update { it.copy(bestMoveSettings = newSettings) }
    }

    fun selectRackTile(tile: Tile) {
        if (!_uiState.value.isCurrentPlayerHuman) return
        ScrabbleSoundEngine.playLiftSound()
        _uiState.update {
            it.copy(
                selectedRackTile = if (it.selectedRackTile?.id == tile.id) null else tile
            )
        }
    }

    fun placeTileOnBoard(row: Int, col: Int) {
        val state = _uiState.value
        val selectedTile = state.selectedRackTile ?: return
        placeTileDirectly(selectedTile, row, col)
    }

    /**
     * Direct tile placement from click or drag-and-drop
     */
    fun placeTileDirectly(tile: Tile, row: Int, col: Int) {
        val state = _uiState.value
        if (!state.isCurrentPlayerHuman) return

        // Verify square is free
        if (state.board.getSquare(row, col)?.isOccupied == true ||
            state.placedTiles.any { it.row == row && it.col == col }
        ) {
            return
        }

        ScrabbleSoundEngine.playPlaceSound()

        // If blank tile without assigned letter, open picker
        if (tile.isBlank && tile.assignedLetter == null) {
            _uiState.update {
                it.copy(
                    selectedRackTile = tile,
                    pendingBlankSquare = Pair(row, col)
                )
            }
            return
        }

        // Move tile from rack to placed tiles
        val curPlayer = state.currentPlayer ?: return
        val newRack = curPlayer.rack.filterNot { it.id == tile.id }
        val updatedPlayers = state.players.mapIndexed { idx, p ->
            if (idx == state.currentPlayerIndex) p.copy(rack = newRack) else p
        }

        _uiState.update {
            it.copy(
                placedTiles = it.placedTiles + PlacedTile(tile, row, col),
                selectedRackTile = null,
                players = updatedPlayers
            )
        }
    }

    /**
     * Moves a newly placed tile from one board square to another (dragged on board)
     */
    fun movePlacedTile(placedTile: PlacedTile, newRow: Int, newCol: Int) {
        val state = _uiState.value
        if (!state.isCurrentPlayerHuman) return

        // Destination must be unoccupied
        if (state.board.getSquare(newRow, newCol)?.isOccupied == true ||
            state.placedTiles.any { it.row == newRow && it.col == newCol && it != placedTile }
        ) {
            return
        }

        ScrabbleSoundEngine.playPlaceSound()

        val updatedPlaced = state.placedTiles.map { pt ->
            if (pt.tile.id == placedTile.tile.id) {
                pt.copy(row = newRow, col = newCol)
            } else {
                pt
            }
        }

        _uiState.update { it.copy(placedTiles = updatedPlaced) }
    }

    /**
     * Reorders rack tiles when dragged within player rack
     */
    fun reorderRackTiles(fromIndex: Int, toIndex: Int) {
        val state = _uiState.value
        if (!state.isCurrentPlayerHuman) return
        val curPlayer = state.currentPlayer ?: return
        if (fromIndex !in curPlayer.rack.indices || toIndex !in curPlayer.rack.indices) return

        ScrabbleSoundEngine.playPlaceSound()

        val mutableRack = curPlayer.rack.toMutableList()
        val tile = mutableRack.removeAt(fromIndex)
        mutableRack.add(toIndex.coerceIn(0, mutableRack.size), tile)

        val updatedPlayers = state.players.mapIndexed { idx, p ->
            if (idx == state.currentPlayerIndex) p.copy(rack = mutableRack) else p
        }

        _uiState.update { it.copy(players = updatedPlayers) }
    }

    fun chooseBlankLetter(chosenChar: Char) {
        val state = _uiState.value
        val square = state.pendingBlankSquare ?: return
        val selectedTile = state.selectedRackTile ?: return

        ScrabbleSoundEngine.playPlaceSound()

        val configuredBlank = selectedTile.copy(assignedLetter = chosenChar.uppercaseChar())
        val curPlayer = state.currentPlayer ?: return
        val newRack = curPlayer.rack.filterNot { it.id == selectedTile.id }
        val updatedPlayers = state.players.mapIndexed { idx, p ->
            if (idx == state.currentPlayerIndex) p.copy(rack = newRack) else p
        }

        _uiState.update {
            it.copy(
                placedTiles = it.placedTiles + PlacedTile(configuredBlank, square.first, square.second),
                selectedRackTile = null,
                pendingBlankSquare = null,
                players = updatedPlayers
            )
        }
    }

    fun dismissBlankPicker() {
        _uiState.update { it.copy(pendingBlankSquare = null) }
    }

    fun recallPlacedTile(placedTile: PlacedTile) {
        val state = _uiState.value
        if (!state.isCurrentPlayerHuman) return

        ScrabbleSoundEngine.playLiftSound()

        val curPlayer = state.currentPlayer ?: return
        val cleanTile = if (placedTile.tile.isBlank) placedTile.tile.copy(assignedLetter = null) else placedTile.tile
        val updatedRack = curPlayer.rack + cleanTile
        val updatedPlayers = state.players.mapIndexed { idx, p ->
            if (idx == state.currentPlayerIndex) p.copy(rack = updatedRack) else p
        }

        _uiState.update {
            it.copy(
                placedTiles = it.placedTiles.filterNot { pt -> pt.row == placedTile.row && pt.col == placedTile.col },
                players = updatedPlayers
            )
        }
    }

    fun recallAllTiles() {
        val state = _uiState.value
        val curPlayer = state.currentPlayer ?: return
        if (state.placedTiles.isEmpty()) return

        ScrabbleSoundEngine.playLiftSound()

        val returnedTiles = state.placedTiles.map {
            if (it.tile.isBlank) it.tile.copy(assignedLetter = null) else it.tile
        }
        val updatedPlayers = state.players.mapIndexed { idx, p ->
            if (idx == state.currentPlayerIndex) p.copy(rack = p.rack + returnedTiles) else p
        }

        _uiState.update {
            it.copy(
                placedTiles = emptyList(),
                selectedRackTile = null,
                players = updatedPlayers
            )
        }
    }

    fun shuffleRack() {
        val state = _uiState.value
        val curPlayer = state.currentPlayer ?: return
        val shuffled = curPlayer.rack.shuffled()
        val updatedPlayers = state.players.mapIndexed { idx, p ->
            if (idx == state.currentPlayerIndex) p.copy(rack = shuffled) else p
        }
        _uiState.update { it.copy(players = updatedPlayers) }
    }

    fun submitTurn() {
        val state = _uiState.value
        val placed = state.placedTiles
        val player = state.currentPlayer ?: return

        if (placed.isEmpty()) {
            _uiState.update { it.copy(statusMessage = "No tiles placed! Place tiles on board or Pass.") }
            return
        }

        val isMultiplayer = state.gameMode == GameMode.MULTIPLAYER

        // Validate placement
        val result = WordValidator.validateAndScore(
            board = state.board,
            placedTiles = placed,
            isMultiplayerChallengeMode = isMultiplayer
        )

        if (!result.isValid) {
            _uiState.update { it.copy(statusMessage = result.errorMessage ?: "Invalid placement.") }
            return
        }

        // In Multiplayer, enter the Challenge Phase for opponents
        if (isMultiplayer) {
            val opponents = state.players.filterNot { it.id == player.id }.map { it.id }
            _uiState.update {
                it.copy(
                    challengeState = ChallengeState(
                        playerWhoPlayed = player,
                        playedTiles = placed,
                        formedWords = result.allFormedWords,
                        points = result.totalScore,
                        isBingo = result.isBingo,
                        remainingOpponentsToRespond = opponents
                    ),
                    statusMessage = "${player.name} played \"${result.primaryWord}\" (+${result.totalScore} pts). Waiting for opponents to Challenge or Skip..."
                )
            }
            return
        }

        // In VS AI, execute play immediately
        commitSuccessfulMove(
            player = player,
            placedTiles = placed,
            words = result.allFormedWords,
            primaryWord = result.primaryWord,
            score = result.totalScore,
            isBingo = result.isBingo
        )
    }

    fun skipChallenge() {
        val state = _uiState.value
        val cs = state.challengeState ?: return

        _uiState.update { it.copy(challengeState = null) }

        commitSuccessfulMove(
            player = cs.playerWhoPlayed,
            placedTiles = cs.playedTiles,
            words = cs.formedWords,
            primaryWord = cs.formedWords.firstOrNull() ?: "",
            score = cs.points,
            isBingo = cs.isBingo
        )
    }

    fun challengeWord() {
        val state = _uiState.value
        val cs = state.challengeState ?: return

        val invalidWords = cs.formedWords.filter { !CSW24Dictionary.isValid(it) }

        _uiState.update { it.copy(challengeState = null) }

        if (invalidWords.isNotEmpty()) {
            val curPlayer = cs.playerWhoPlayed
            val recalledTiles = cs.playedTiles.map {
                if (it.tile.isBlank) it.tile.copy(assignedLetter = null) else it.tile
            }
            val updatedPlayers = state.players.map { p ->
                if (p.id == curPlayer.id) p.copy(rack = p.rack + recalledTiles) else p
            }

            _uiState.update {
                it.copy(
                    placedTiles = emptyList(),
                    players = updatedPlayers,
                    statusMessage = "Challenge SUCCESSFUL! Word(s) ${invalidWords.joinToString()} NOT in CSW24! ${curPlayer.name} loses turn."
                )
            }
            advanceTurn()
        } else {
            _uiState.update {
                it.copy(
                    statusMessage = "Challenge FAILED! Words are legal CSW24! Play stands for ${cs.playerWhoPlayed.name}."
                )
            }
            commitSuccessfulMove(
                player = cs.playerWhoPlayed,
                placedTiles = cs.playedTiles,
                words = cs.formedWords,
                primaryWord = cs.formedWords.firstOrNull() ?: "",
                score = cs.points,
                isBingo = cs.isBingo
            )
        }
    }

    private fun commitSuccessfulMove(
        player: Player,
        placedTiles: List<PlacedTile>,
        words: List<String>,
        primaryWord: String,
        score: Int,
        isBingo: Boolean
    ) {
        val state = _uiState.value
        var updatedBoard = state.board
        for (pt in placedTiles) {
            updatedBoard = updatedBoard.withTilePlaced(pt.row, pt.col, pt.tile)
        }

        // Draw replenishment tiles
        val drawnCount = placedTiles.size
        val newTiles = state.tileBag.drawTiles(drawnCount)
        val updatedRack = player.rack + newTiles

        val updatedScore = player.score + score
        val updatedPlayers = state.players.map { p ->
            if (p.id == player.id) p.copy(score = updatedScore, rack = updatedRack) else p
        }

        val move = GameMove(
            playerId = player.id,
            playerName = player.name,
            primaryWord = primaryWord,
            allWords = words,
            pointsScored = score,
            isBingo = isBingo,
            turnNumber = state.moveHistory.size + 1
        )

        if (isBingo) {
            ScrabbleSoundEngine.playBingoSound()
        }

        // Best Move Analysis for review
        var updatedTurnReviews = state.turnReviews
        if (state.gameMode == GameMode.VS_AI) {
            val shouldAnalyze = (player.isAi && state.bestMoveSettings.analyzeAiMoves) ||
                    (!player.isAi && state.bestMoveSettings.analyzePlayerMoves)
            if (shouldAnalyze) {
                val preRack = player.rack + placedTiles.map { it.tile }
                val bestDecision = ScrabbleAISolver.findBestMove(state.board, preRack, AiDifficulty.BEST_MOVE)
                val bestCandidate = bestDecision.move
                val bestPoints = bestCandidate?.score ?: 0
                val ptsDiff = (bestPoints - score).coerceAtLeast(0)

                val reviewItem = TurnReviewRecord(
                    turnNumber = state.moveHistory.size + 1,
                    playerId = player.id,
                    playerName = player.name,
                    isAi = player.isAi,
                    playedWord = primaryWord,
                    playedPoints = score,
                    playedBingo = isBingo,
                    playedTiles = placedTiles,
                    bestWord = bestCandidate?.primaryWord,
                    bestPoints = bestPoints,
                    bestBingo = bestCandidate?.isBingo ?: false,
                    bestPlacedTiles = bestCandidate?.placedTiles ?: emptyList(),
                    pointsDiff = ptsDiff,
                    boardBeforeMove = state.board
                )
                updatedTurnReviews = state.turnReviews + reviewItem
            }
        }

        val bingoEvent = if (isBingo) {
            BingoCelebrationEvent(player.name, primaryWord, score)
        } else null

        _uiState.update {
            it.copy(
                board = updatedBoard,
                placedTiles = emptyList(),
                selectedRackTile = null,
                players = updatedPlayers,
                moveHistory = it.moveHistory + move,
                bingoCelebration = bingoEvent,
                turnReviews = updatedTurnReviews,
                consecutivePasses = 0,
                statusMessage = "${player.name} scored $score pts with \"$primaryWord\"!"
            )
        }

        autoSaveAiSession()

        // Check game end conditions
        if (state.tileBag.isEmpty && updatedRack.isEmpty()) {
            endGame()
        } else {
            advanceTurn()
        }
    }

    fun passTurn() {
        recallAllTiles()
        val state = _uiState.value
        val player = state.currentPlayer ?: return
        val newPassCount = state.consecutivePasses + 1

        _uiState.update {
            it.copy(
                consecutivePasses = newPassCount,
                statusMessage = "${player.name} passed their turn."
            )
        }

        autoSaveAiSession()

        if (newPassCount >= state.players.size * 2) {
            endGame()
        } else {
            advanceTurn()
        }
    }

    fun exchangeTiles(toExchange: List<Tile>) {
        val state = _uiState.value
        val player = state.currentPlayer ?: return
        if (toExchange.isEmpty()) return

        val remainingRack = player.rack.filterNot { tile -> toExchange.any { it.id == tile.id } }
        val newDrawn = state.tileBag.swapTiles(toExchange)
        val updatedRack = remainingRack + newDrawn

        val updatedPlayers = state.players.map { p ->
            if (p.id == player.id) p.copy(rack = updatedRack) else p
        }

        _uiState.update {
            it.copy(
                players = updatedPlayers,
                consecutivePasses = 0,
                statusMessage = "${player.name} exchanged ${toExchange.size} tiles."
            )
        }

        autoSaveAiSession()
        advanceTurn()
    }

    private fun autoSaveAiSession() {
        val state = _uiState.value
        if (state.gameMode == GameMode.VS_AI && state.players.size >= 2 && !state.isGameOver) {
            val human = state.players[0]
            val ai = state.players[1]
            sessionStorage.saveSession(
                humanPlayer = human,
                aiPlayer = ai,
                board = state.board,
                tileBag = state.tileBag,
                difficulty = state.aiDifficulty,
                currentPlayerIndex = state.currentPlayerIndex,
                moveHistory = state.moveHistory
            )
            checkSavedSession()
        }
    }

    private fun advanceTurn() {
        val state = _uiState.value
        val nextIdx = (state.currentPlayerIndex + 1) % state.players.size

        val nextPlayer = state.players.getOrNull(nextIdx)
        val hint = if (state.gameMode == GameMode.VS_AI && nextPlayer?.isAi == false) {
            ScrabbleAISolver.findBestMove(state.board, nextPlayer.rack, AiDifficulty.BEST_MOVE).move
        } else null

        _uiState.update {
            it.copy(
                currentPlayerIndex = nextIdx,
                placedTiles = emptyList(),
                selectedRackTile = null,
                currentTurnBestMoveHint = hint
            )
        }

        autoSaveAiSession()

        if (nextPlayer?.isAi == true) {
            executeAiTurn()
        }
    }

    private fun executeAiTurn() {
        _uiState.update { it.copy(isAiThinking = true) }

        viewModelScope.launch {
            delay(1200)

            val state = _uiState.value
            val aiPlayer = state.currentPlayer ?: return@launch
            if (!aiPlayer.isAi) return@launch

            val decision = ScrabbleAISolver.findBestMove(
                board = state.board,
                rack = aiPlayer.rack,
                difficulty = state.aiDifficulty,
                canExchange = state.tileBag.remainingCount >= 7
            )

            _uiState.update { it.copy(isAiThinking = false) }

            when (decision.action) {
                AiActionType.PLAY -> {
                    val move = decision.move
                    if (move != null) {
                        commitSuccessfulMove(
                            player = aiPlayer,
                            placedTiles = move.placedTiles,
                            words = move.allWords,
                            primaryWord = move.primaryWord,
                            score = move.score,
                            isBingo = move.isBingo
                        )
                    } else {
                        passTurn()
                    }
                }
                AiActionType.EXCHANGE -> {
                    exchangeTiles(decision.tilesToExchange)
                }
                AiActionType.PASS -> {
                    passTurn()
                }
            }
        }
    }

    fun dismissBingoCelebration() {
        _uiState.update { it.copy(bingoCelebration = null) }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun endGame() {
        sessionStorage.clearSession()
        val state = _uiState.value
        val winner = state.players.maxByOrNull { it.score }
        _uiState.update {
            it.copy(
                isGameOver = true,
                winner = winner,
                currentScreen = "game_over",
                hasSavedAiSession = false,
                savedAiSession = null,
                statusMessage = "Game Over! ${winner?.name ?: "Someone"} wins with ${winner?.score ?: 0} points!"
            )
        }
    }
}
