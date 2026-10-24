package com.example.model

import java.util.UUID

data class LobbyPlayer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isHost: Boolean = false,
    val isAi: Boolean = false,
    val avatarColor: Long = 0xFF1B4D3E
)

data class MultiplayerRoom(
    val roomCode: String = generateRoomCode(),
    val maxPlayers: Int = 4,
    val players: List<LobbyPlayer> = listOf(
        LobbyPlayer(name = "Host (You)", isHost = true, avatarColor = 0xFF1B4D3E)
    ),
    val status: RoomStatus = RoomStatus.WAITING_FOR_PLAYERS
) {
    val canStart: Boolean get() = players.size in 2..4

    companion object {
        fun generateRoomCode(): String {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            val randomStr = (1..6).map { chars.random() }.joinToString("")
            return "CSW-$randomStr"
        }
    }
}

enum class RoomStatus {
    WAITING_FOR_PLAYERS,
    GAME_IN_PROGRESS,
    FINISHED
}
