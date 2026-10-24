package com.example

import com.example.ai.AiActionType
import com.example.ai.ScrabbleAISolver
import com.example.dictionary.CSW24Dictionary
import com.example.dictionary.WordValidator
import com.example.model.AiDifficulty
import com.example.model.Board
import com.example.model.PlacedTile
import com.example.model.PremiumSquare
import com.example.model.Tile
import com.example.model.TileBag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrabbleGameLogicTest {

    @Test
    fun testCSW24TwoLetterWords() {
        assertTrue(CSW24Dictionary.isValid("QI"))
        assertTrue(CSW24Dictionary.isValid("ZA"))
        assertTrue(CSW24Dictionary.isValid("ZO"))
        assertTrue(CSW24Dictionary.isValid("XI"))
        assertTrue(CSW24Dictionary.isValid("XU"))
        assertTrue(CSW24Dictionary.isValid("AA"))
        assertTrue(CSW24Dictionary.isValid("CH"))
        assertTrue(CSW24Dictionary.isValid("JA"))
        assertFalse(CSW24Dictionary.isValid("ZZ"))
    }

    @Test
    fun testTileBagInitialDistribution() {
        val bag = TileBag()
        assertEquals(100, bag.remainingCount)

        val drawn = bag.drawTiles(7)
        assertEquals(7, drawn.size)
        assertEquals(93, bag.remainingCount)
    }

    @Test
    fun testBoardPremiums() {
        assertEquals(PremiumSquare.STAR, Board.getPremium(7, 7))
        assertEquals(PremiumSquare.TW, Board.getPremium(0, 0))
        assertEquals(PremiumSquare.TW, Board.getPremium(14, 14))
        assertEquals(PremiumSquare.DL, Board.getPremium(0, 3))
        assertEquals(PremiumSquare.TL, Board.getPremium(1, 5))
        assertEquals(PremiumSquare.DW, Board.getPremium(1, 1))
    }

    @Test
    fun testFirstWordMustCoverCenter() {
        val board = Board()
        // Word placed not on center
        val tilesNotOnCenter = listOf(
            PlacedTile(Tile.create('C'), 0, 0),
            PlacedTile(Tile.create('A'), 0, 1),
            PlacedTile(Tile.create('T'), 0, 2)
        )
        val resultOffCenter = WordValidator.validateAndScore(board, tilesNotOnCenter)
        assertFalse(resultOffCenter.isValid)
        assertTrue(resultOffCenter.errorMessage?.contains("center star") == true)

        // Word covering center (7, 7)
        val tilesOnCenter = listOf(
            PlacedTile(Tile.create('C'), 7, 6),
            PlacedTile(Tile.create('A'), 7, 7), // Covers center star (Double Word)
            PlacedTile(Tile.create('T'), 7, 8)
        )
        val resultOnCenter = WordValidator.validateAndScore(board, tilesOnCenter)
        assertTrue(resultOnCenter.isValid)
        assertEquals("CAT", resultOnCenter.primaryWord)
        // C(3) + A(1) + T(1) = 5 base points. Center star is DW (x2) -> 10 points!
        assertEquals(10, resultOnCenter.totalScore)
    }

    @Test
    fun testBingoBonusAwardedForSevenTiles() {
        val board = Board()
        // 7-tile word covering center star: e.g. "PLAYERS"
        val word = "PLAYERS"
        val tiles = word.mapIndexed { index, char ->
            PlacedTile(Tile.create(char), 7, 4 + index)
        }
        val result = WordValidator.validateAndScore(board, tiles)
        assertTrue(result.isValid)
        assertTrue(result.isBingo)
        // Score must include the +50 Bingo bonus
        assertTrue(result.totalScore >= 50)
    }

    @Test
    fun testAISolverGeneratesMove() {
        val board = Board()
        val rack = listOf(
            Tile.create('C'), Tile.create('A'), Tile.create('T'),
            Tile.create('S'), Tile.create('E'), Tile.create('D'), Tile.create('R')
        )
        val decision = ScrabbleAISolver.findBestMove(board, rack, difficulty = AiDifficulty.BEST_MOVE)
        assertEquals(AiActionType.PLAY, decision.action)
        assertNotNull(decision.move)
        assertTrue(decision.move!!.score > 0)
    }

    @Test
    fun testRackReordering() {
        val originalRack = mutableListOf(
            Tile.create('A'), Tile.create('B'), Tile.create('C')
        )
        // Move item at 0 ('A') to index 2
        val moved = originalRack.removeAt(0)
        originalRack.add(2, moved)
        assertEquals('B', originalRack[0].letter)
        assertEquals('C', originalRack[1].letter)
        assertEquals('A', originalRack[2].letter)
    }
}
