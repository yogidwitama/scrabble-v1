package com.example.model

data class PlacedTile(
    val tile: Tile,
    val row: Int,
    val col: Int
)

data class Player(
    val id: String,
    val name: String,
    val score: Int = 0,
    val rack: List<Tile> = emptyList(),
    val isAi: Boolean = false,
    val colorHex: Long = 0xFF1B4D3E,
    val skippedTurnsCount: Int = 0
)

enum class GameMode(val title: String, val subtitle: String) {
    VS_AI("Play vs AI", "Challenge computer with high-score Best Move calculation"),
    MULTIPLAYER("Multiplayer (2-4 Players)", "Invite opponents via Barcode / QR or Room Code")
}

enum class AiDifficulty(val label: String, val description: String) {
    BEST_MOVE("Best Move (Grandmaster)", "Calculates highest-scoring legal move each turn"),
    BALANCED("Challenger (Medium)", "Plays solid tournament words with high accuracy"),
    CASUAL("Casual (Standard)", "Friendly practice level playing everyday words")
}

data class GameMove(
    val playerId: String,
    val playerName: String,
    val primaryWord: String,
    val allWords: List<String>,
    val pointsScored: Int,
    val isBingo: Boolean,
    val turnNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChallengeState(
    val playerWhoPlayed: Player,
    val playedTiles: List<PlacedTile>,
    val formedWords: List<String>,
    val points: Int,
    val isBingo: Boolean,
    val remainingOpponentsToRespond: List<String> // Player IDs of opponents who can challenge or skip
)
