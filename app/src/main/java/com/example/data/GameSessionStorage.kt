package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AiDifficulty
import com.example.model.Board
import com.example.model.BoardSquare
import com.example.model.GameMove
import com.example.model.Player
import com.example.model.Tile
import com.example.model.TileBag
import org.json.JSONArray
import org.json.JSONObject

data class SavedAiGameSession(
    val humanPlayer: Player,
    val aiPlayer: Player,
    val board: Board,
    val tileBag: TileBag,
    val difficulty: AiDifficulty,
    val currentPlayerIndex: Int,
    val moveHistory: List<GameMove>,
    val timestamp: Long
)

class GameSessionStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("scrabble_ai_session_prefs", Context.MODE_PRIVATE)

    fun hasSavedSession(): Boolean {
        return prefs.contains(KEY_SESSION_DATA)
    }

    fun saveSession(
        humanPlayer: Player,
        aiPlayer: Player,
        board: Board,
        tileBag: TileBag,
        difficulty: AiDifficulty,
        currentPlayerIndex: Int,
        moveHistory: List<GameMove>
    ) {
        val root = JSONObject()
        root.put("timestamp", System.currentTimeMillis())
        root.put("difficulty", difficulty.name)
        root.put("currentPlayerIndex", currentPlayerIndex)

        // Human player
        root.put("humanPlayer", serializePlayer(humanPlayer))
        // AI player
        root.put("aiPlayer", serializePlayer(aiPlayer))

        // Board occupied squares
        val boardArr = JSONArray()
        for (r in 0 until Board.BOARD_SIZE) {
            for (c in 0 until Board.BOARD_SIZE) {
                val sq = board.getSquare(r, c)
                if (sq?.isOccupied == true && sq.tile != null) {
                    val tileObj = JSONObject().apply {
                        put("r", r)
                        put("c", c)
                        put("letter", sq.tile.letter.toString())
                        put("value", sq.tile.value)
                        put("isBlank", sq.tile.isBlank)
                        if (sq.tile.assignedLetter != null) {
                            put("assigned", sq.tile.assignedLetter.toString())
                        }
                    }
                    boardArr.put(tileObj)
                }
            }
        }
        root.put("board", boardArr)

        // Remaining tiles in bag
        val bagDist = tileBag.getDistributionMap()
        val bagObj = JSONObject()
        bagDist.forEach { (char, count) ->
            bagObj.put(char.toString(), count)
        }
        root.put("bagDistribution", bagObj)

        // Move history
        val movesArr = JSONArray()
        moveHistory.forEach { m ->
            val mObj = JSONObject().apply {
                put("playerId", m.playerId)
                put("playerName", m.playerName)
                put("primaryWord", m.primaryWord)
                val wordsArr = JSONArray()
                m.allWords.forEach { wordsArr.put(it) }
                put("allWords", wordsArr)
                put("points", m.pointsScored)
                put("isBingo", m.isBingo)
                put("turn", m.turnNumber)
                put("time", m.timestamp)
            }
            movesArr.put(mObj)
        }
        root.put("moveHistory", movesArr)

        prefs.edit().putString(KEY_SESSION_DATA, root.toString()).apply()
    }

    fun loadSession(): SavedAiGameSession? {
        val jsonStr = prefs.getString(KEY_SESSION_DATA, null) ?: return null
        return try {
            val root = JSONObject(jsonStr)
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())
            val diffStr = root.optString("difficulty", AiDifficulty.BEST_MOVE.name)
            val difficulty = try { AiDifficulty.valueOf(diffStr) } catch (_: Exception) { AiDifficulty.BEST_MOVE }
            val curIndex = root.optInt("currentPlayerIndex", 0)

            val humanPlayer = deserializePlayer(root.getJSONObject("humanPlayer"))
            val aiPlayer = deserializePlayer(root.getJSONObject("aiPlayer"))

            // Restore Board
            var board = Board()
            val boardArr = root.getJSONArray("board")
            for (i in 0 until boardArr.length()) {
                val item = boardArr.getJSONObject(i)
                val r = item.getInt("r")
                val c = item.getInt("c")
                val letter = item.getString("letter")[0]
                val value = item.getInt("value")
                val isBlank = item.getBoolean("isBlank")
                val assigned = if (item.has("assigned")) item.getString("assigned")[0] else null
                val tile = Tile(letter = letter, value = value, isBlank = isBlank, assignedLetter = assigned)
                board = board.withTilePlaced(r, c, tile)
            }

            // Restore TileBag
            val bagDistObj = root.getJSONObject("bagDistribution")
            val restoredBagTiles = mutableListOf<Tile>()
            val keys = bagDistObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val char = key[0]
                val count = bagDistObj.getInt(key)
                repeat(count) {
                    restoredBagTiles.add(Tile.create(char))
                }
            }
            val tileBag = TileBag(restoredBagTiles)

            // Restore Move History
            val movesArr = root.getJSONArray("moveHistory")
            val moveHistory = mutableListOf<GameMove>()
            for (i in 0 until movesArr.length()) {
                val mObj = movesArr.getJSONObject(i)
                val allWordsList = mutableListOf<String>()
                val aw = mObj.getJSONArray("allWords")
                for (j in 0 until aw.length()) {
                    allWordsList.add(aw.getString(j))
                }
                moveHistory.add(
                    GameMove(
                        playerId = mObj.getString("playerId"),
                        playerName = mObj.getString("playerName"),
                        primaryWord = mObj.getString("primaryWord"),
                        allWords = allWordsList,
                        pointsScored = mObj.getInt("points"),
                        isBingo = mObj.getBoolean("isBingo"),
                        turnNumber = mObj.getInt("turn"),
                        timestamp = mObj.optLong("time", System.currentTimeMillis())
                    )
                )
            }

            SavedAiGameSession(
                humanPlayer = humanPlayer,
                aiPlayer = aiPlayer,
                board = board,
                tileBag = tileBag,
                difficulty = difficulty,
                currentPlayerIndex = curIndex,
                moveHistory = moveHistory,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearSession() {
        prefs.edit().remove(KEY_SESSION_DATA).apply()
    }

    private fun serializePlayer(player: Player): JSONObject {
        val obj = JSONObject()
        obj.put("id", player.id)
        obj.put("name", player.name)
        obj.put("score", player.score)
        obj.put("isAi", player.isAi)
        obj.put("colorHex", player.colorHex)

        val rackArr = JSONArray()
        player.rack.forEach { tile ->
            val tObj = JSONObject().apply {
                put("letter", tile.letter.toString())
                put("value", tile.value)
                put("isBlank", tile.isBlank)
                if (tile.assignedLetter != null) {
                    put("assigned", tile.assignedLetter.toString())
                }
            }
            rackArr.put(tObj)
        }
        obj.put("rack", rackArr)
        return obj
    }

    private fun deserializePlayer(obj: JSONObject): Player {
        val id = obj.getString("id")
        val name = obj.getString("name")
        val score = obj.getInt("score")
        val isAi = obj.getBoolean("isAi")
        val colorHex = obj.getLong("colorHex")

        val rackList = mutableListOf<Tile>()
        val rackArr = obj.getJSONArray("rack")
        for (i in 0 until rackArr.length()) {
            val tObj = rackArr.getJSONObject(i)
            val letter = tObj.getString("letter")[0]
            val value = tObj.getInt("value")
            val isBlank = tObj.getBoolean("isBlank")
            val assigned = if (tObj.has("assigned")) tObj.getString("assigned")[0] else null
            rackList.add(Tile(letter = letter, value = value, isBlank = isBlank, assignedLetter = assigned))
        }

        return Player(
            id = id,
            name = name,
            score = score,
            rack = rackList,
            isAi = isAi,
            colorHex = colorHex
        )
    }

    companion object {
        private const val KEY_SESSION_DATA = "saved_scrabble_ai_session_json"
    }
}
