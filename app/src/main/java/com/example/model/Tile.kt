package com.example.model

import java.util.UUID

data class Tile(
    val letter: Char, // 'A'..'Z' or '?' for blank
    val value: Int,
    val id: String = UUID.randomUUID().toString(),
    val isBlank: Boolean = (letter == '?'),
    val assignedLetter: Char? = null // When blank is placed on board, chosen letter 'A'..'Z'
) {
    val displayLetter: Char
        get() = if (isBlank) (assignedLetter ?: '?') else letter

    companion object {
        fun letterScore(char: Char): Int {
            return when (char.uppercaseChar()) {
                '?', ' ' -> 0
                'A', 'E', 'I', 'O', 'U', 'L', 'N', 'S', 'T', 'R' -> 1
                'D', 'G' -> 2
                'B', 'C', 'M', 'P' -> 3
                'F', 'H', 'V', 'W', 'Y' -> 4
                'K' -> 5
                'J', 'X' -> 8
                'Q', 'Z' -> 10
                else -> 0
            }
        }

        fun create(letter: Char): Tile {
            val isBlank = (letter == '?' || letter == ' ')
            val char = if (isBlank) '?' else letter.uppercaseChar()
            return Tile(
                letter = char,
                value = if (isBlank) 0 else letterScore(char),
                isBlank = isBlank
            )
        }
    }
}
