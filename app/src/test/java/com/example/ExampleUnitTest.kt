package com.example

import com.example.ai.ScrabbleAISolver
import com.example.audio.ScrabbleSoundEngine
import com.example.dictionary.CSW24Dictionary
import com.example.dictionary.WordValidator
import com.example.model.AiDifficulty
import com.example.model.BestMoveReviewSettings
import com.example.model.Board
import com.example.model.PlacedTile
import com.example.model.Tile
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCSW24OfficialTwoLetterWords() {
        assertTrue("QI must be valid in CSW24", CSW24Dictionary.isValid("QI"))
        assertTrue("ZA must be valid in CSW24", CSW24Dictionary.isValid("ZA"))
        assertTrue("ZO must be valid in CSW24", CSW24Dictionary.isValid("ZO"))
        assertTrue("AA must be valid in CSW24", CSW24Dictionary.isValid("AA"))
        assertTrue("EX must be valid in CSW24", CSW24Dictionary.isValid("EX"))
        assertFalse("QQ must be invalid", CSW24Dictionary.isValid("QQ"))
        assertFalse("XZ must be invalid", CSW24Dictionary.isValid("XZ"))
    }

    @Test
    fun testBingoBonusAwardedForSevenTiles() {
        val board = Board()
        // Word "QUIZZES" placed on center row 7 across columns 7..13 (7 letters = Bingo)
        val tiles = "QUIZZES".mapIndexed { idx, ch ->
            PlacedTile(Tile.create(ch), row = 7, col = 7 + idx)
        }
        val result = WordValidator.validateAndScore(board, tiles)
        assertTrue("Word QUIZZES must be valid in CSW24", result.isValid)
        assertTrue("Result must be marked as Bingo", result.isBingo)
        // Check that points include 50 bingo bonus
        assertTrue("Points must be >= 50", result.totalScore >= 50)
    }

    @Test
    fun testBestMoveSolverProducesValidMove() {
        val board = Board()
        val rack = listOf(
            Tile.create('Q'),
            Tile.create('I'),
            Tile.create('A'),
            Tile.create('T'),
            Tile.create('S'),
            Tile.create('E'),
            Tile.create('N')
        )
        val decision = ScrabbleAISolver.findBestMove(board, rack, AiDifficulty.BEST_MOVE)
        assertNotNull("AI decision should not be null", decision)
        assertNotNull("AI move should be generated", decision.move)
        assertTrue("Best move must score points", decision.move!!.score > 0)
        assertTrue("Best move word must be valid CSW24", CSW24Dictionary.isValid(decision.move!!.primaryWord))
    }

    @Test
    fun testBestMoveReviewSettingsCheckboxes() {
        // Test default settings: all true
        val defaultSettings = BestMoveReviewSettings()
        assertTrue(defaultSettings.reviewInGame)
        assertTrue(defaultSettings.reviewPostGame)
        assertTrue(defaultSettings.analyzePlayerMoves)
        assertTrue(defaultSettings.analyzeAiMoves)

        // Test one or both or any combination can be enabled
        val playerOnly = defaultSettings.copy(analyzePlayerMoves = true, analyzeAiMoves = false)
        assertTrue(playerOnly.analyzePlayerMoves)
        assertFalse(playerOnly.analyzeAiMoves)

        val aiOnly = defaultSettings.copy(analyzePlayerMoves = false, analyzeAiMoves = true)
        assertFalse(aiOnly.analyzePlayerMoves)
        assertTrue(aiOnly.analyzeAiMoves)

        val postGameOnly = defaultSettings.copy(reviewInGame = false, reviewPostGame = true)
        assertFalse(postGameOnly.reviewInGame)
        assertTrue(postGameOnly.reviewPostGame)
    }

    @Test
    fun testScrabbleSoundEngineMuteToggle() {
        ScrabbleSoundEngine.setMuted(true)
        assertTrue("Sound engine should be muted", ScrabbleSoundEngine.isMuted())
        // Should not throw or crash when called while muted
        ScrabbleSoundEngine.playPlaceSound()
        ScrabbleSoundEngine.playBingoSound()

        ScrabbleSoundEngine.setMuted(false)
        assertFalse("Sound engine should not be muted", ScrabbleSoundEngine.isMuted())
    }
}
