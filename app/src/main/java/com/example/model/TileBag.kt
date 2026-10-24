package com.example.model

class TileBag(initialTiles: List<Tile>? = null) {
    private val tiles = mutableListOf<Tile>()

    init {
        if (initialTiles != null) {
            tiles.addAll(initialTiles)
        } else {
            reset()
        }
    }

    val remainingCount: Int
        get() = tiles.size

    val isEmpty: Boolean
        get() = tiles.isEmpty()

    fun reset() {
        tiles.clear()
        OFFICIAL_DISTRIBUTION.forEach { (char, count) ->
            repeat(count) {
                tiles.add(Tile.create(char))
            }
        }
        tiles.shuffle()
    }

    fun drawTiles(count: Int): List<Tile> {
        val drawn = mutableListOf<Tile>()
        val actualCount = count.coerceAtMost(tiles.size)
        repeat(actualCount) {
            drawn.add(tiles.removeAt(0))
        }
        return drawn
    }

    fun swapTiles(toSwap: List<Tile>): List<Tile> {
        if (tiles.size < 7) {
            // Cannot swap if fewer than 7 tiles remain in bag (official rule)
            return emptyList()
        }
        val count = toSwap.size.coerceAtMost(tiles.size)
        val newTiles = drawTiles(count)
        tiles.addAll(toSwap)
        tiles.shuffle()
        return newTiles
    }

    fun getDistributionMap(): Map<Char, Int> {
        val counts = mutableMapOf<Char, Int>()
        tiles.forEach { tile ->
            val key = tile.letter
            counts[key] = (counts[key] ?: 0) + 1
        }
        return counts
    }

    companion object {
        val OFFICIAL_DISTRIBUTION: Map<Char, Int> = mapOf(
            'A' to 9, 'B' to 2, 'C' to 2, 'D' to 4, 'E' to 12,
            'F' to 2, 'G' to 3, 'H' to 2, 'I' to 9, 'J' to 1,
            'K' to 1, 'L' to 4, 'M' to 2, 'N' to 6, 'O' to 8,
            'P' to 2, 'Q' to 1, 'R' to 6, 'S' to 4, 'T' to 6,
            'U' to 4, 'V' to 2, 'W' to 2, 'X' to 1, 'Y' to 2,
            'Z' to 1, '?' to 2 // 2 Blanks
        )
    }
}
