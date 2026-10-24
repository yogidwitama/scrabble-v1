package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tile
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TileBorder
import com.example.ui.theme.TileIvory
import com.example.ui.theme.TileText
import com.example.ui.theme.TileWood

@Composable
fun TileView(
    tile: Tile,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    isSelected: Boolean = false,
    isNewlyPlaced: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val cornerRadius = (size.value * 0.15f).dp
    val letterFontSize = (size.value * 0.48f).sp
    val scoreFontSize = (size.value * 0.22f).sp

    val backgroundColor = if (tile.isBlank && tile.assignedLetter == null) {
        TileIvory
    } else {
        TileWood
    }

    val borderModifier = when {
        isSelected -> Modifier.border(2.5.dp, GoldAccent, RoundedCornerShape(cornerRadius))
        isNewlyPlaced -> Modifier.border(2.dp, Color(0xFF2E7D32), RoundedCornerShape(cornerRadius))
        else -> Modifier.border(1.dp, TileBorder, RoundedCornerShape(cornerRadius))
    }

    Box(
        modifier = modifier
            .size(size)
            .then(if (isSelected) Modifier.offset(y = (-4).dp) else Modifier)
            .shadow(
                elevation = if (isSelected) 8.dp else 2.5.dp,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    listOf(
                        backgroundColor,
                        Color(0xFFE8D4AC)
                    )
                )
            )
            .then(borderModifier)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        // Main Letter
        Text(
            text = tile.displayLetter.toString(),
            fontSize = letterFontSize,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            color = if (tile.isBlank) Color(0xFF1565C0) else TileText
        )

        // Subscript Score (bottom-right)
        if (!tile.isBlank || tile.assignedLetter != null) {
            Text(
                text = if (tile.isBlank) "0" else tile.value.toString(),
                fontSize = scoreFontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = TileText.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = (size.value * 0.05f).dp, end = (size.value * 0.08f).dp)
            )
        }
    }
}
