package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CenterStarGold
import com.example.ui.theme.DoubleLetterCyan
import com.example.ui.theme.DoubleWordPink
import com.example.ui.theme.TripleLetterBlue
import com.example.ui.theme.TripleWordRed

enum class PremiumSquare(
    val label: String,
    val shortLabel: String,
    val letterMultiplier: Int,
    val wordMultiplier: Int,
    val badgeColor: Color
) {
    NONE("", "", 1, 1, Color.Transparent),
    DL("DOUBLE LETTER", "DL", 2, 1, DoubleLetterCyan),
    TL("TRIPLE LETTER", "TL", 3, 1, TripleLetterBlue),
    DW("DOUBLE WORD", "DW", 1, 2, DoubleWordPink),
    TW("TRIPLE WORD", "TW", 1, 3, TripleWordRed),
    STAR("CENTER STAR", "★", 1, 2, CenterStarGold)
}
