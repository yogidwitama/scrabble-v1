package com.example.model

data class BoardSquare(
    val row: Int,
    val col: Int,
    val premium: PremiumSquare,
    val tile: Tile? = null
) {
    val isOccupied: Boolean get() = tile != null
}

data class Board(
    val squares: List<List<BoardSquare>> = createDefaultBoard()
) {
    fun getSquare(row: Int, col: Int): BoardSquare? {
        if (row !in 0 until BOARD_SIZE || col !in 0 until BOARD_SIZE) return null
        return squares[row][col]
    }

    fun isCenterOccupied(): Boolean {
        return squares[CENTER][CENTER].isOccupied
    }

    fun isEmpty(): Boolean {
        return squares.all { row -> row.all { !it.isOccupied } }
    }

    fun withTilePlaced(row: Int, col: Int, tile: Tile): Board {
        val updated = squares.mapIndexed { r, rowList ->
            if (r == row) {
                rowList.mapIndexed { c, square ->
                    if (c == col) square.copy(tile = tile) else square
                }
            } else {
                rowList
            }
        }
        return Board(updated)
    }

    companion object {
        const val BOARD_SIZE = 15
        const val CENTER = 7

        fun getPremium(row: Int, col: Int): PremiumSquare {
            if (row == CENTER && col == CENTER) return PremiumSquare.STAR

            // Triple Word (TW)
            val twCoords = setOf(
                Pair(0, 0), Pair(0, 7), Pair(0, 14),
                Pair(7, 0), Pair(7, 14),
                Pair(14, 0), Pair(14, 7), Pair(14, 14)
            )
            if (Pair(row, col) in twCoords) return PremiumSquare.TW

            // Double Word (DW)
            val dwCoords = setOf(
                Pair(1, 1), Pair(2, 2), Pair(3, 3), Pair(4, 4),
                Pair(1, 13), Pair(2, 12), Pair(3, 11), Pair(4, 10),
                Pair(13, 1), Pair(12, 2), Pair(11, 3), Pair(10, 4),
                Pair(13, 13), Pair(12, 12), Pair(11, 11), Pair(10, 10)
            )
            if (Pair(row, col) in dwCoords) return PremiumSquare.DW

            // Triple Letter (TL)
            val tlCoords = setOf(
                Pair(1, 5), Pair(1, 9),
                Pair(5, 1), Pair(5, 5), Pair(5, 9), Pair(5, 13),
                Pair(9, 1), Pair(9, 5), Pair(9, 9), Pair(9, 13),
                Pair(13, 5), Pair(13, 9)
            )
            if (Pair(row, col) in tlCoords) return PremiumSquare.TL

            // Double Letter (DL)
            val dlCoords = setOf(
                Pair(0, 3), Pair(0, 11),
                Pair(2, 6), Pair(2, 8),
                Pair(3, 0), Pair(3, 7), Pair(3, 14),
                Pair(6, 2), Pair(6, 6), Pair(6, 8), Pair(6, 12),
                Pair(7, 3), Pair(7, 11),
                Pair(8, 2), Pair(8, 6), Pair(8, 8), Pair(8, 12),
                Pair(11, 0), Pair(11, 7), Pair(11, 14),
                Pair(12, 6), Pair(12, 8),
                Pair(14, 3), Pair(14, 11)
            )
            if (Pair(row, col) in dlCoords) return PremiumSquare.DL

            return PremiumSquare.NONE
        }

        private fun createDefaultBoard(): List<List<BoardSquare>> {
            return (0 until BOARD_SIZE).map { r ->
                (0 until BOARD_SIZE).map { c ->
                    BoardSquare(
                        row = r,
                        col = c,
                        premium = getPremium(r, c),
                        tile = null
                    )
                }
            }
        }
    }
}
