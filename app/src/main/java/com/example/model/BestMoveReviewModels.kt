package com.example.model

data class BestMoveReviewSettings(
    val reviewInGame: Boolean = true,       // "saat bermain"
    val reviewPostGame: Boolean = true,     // "setelah bermain"
    val analyzePlayerMoves: Boolean = true, // "catatan move pemain"
    val analyzeAiMoves: Boolean = true      // "catatan move ai"
)

data class TurnReviewRecord(
    val turnNumber: Int,
    val playerId: String,
    val playerName: String,
    val isAi: Boolean,
    val playedWord: String,
    val playedPoints: Int,
    val playedBingo: Boolean,
    val playedTiles: List<PlacedTile>,
    // Best move theoretical calculation
    val bestWord: String?,
    val bestPoints: Int?,
    val bestBingo: Boolean,
    val bestPlacedTiles: List<PlacedTile>,
    val pointsDiff: Int, // (bestPoints - playedPoints). If <= 0 -> Optimal!
    val boardBeforeMove: Board,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isOptimal: Boolean get() = pointsDiff <= 0
}
