package com.example.ui.dragdrop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.PlacedTile
import com.example.model.Tile
import com.example.ui.components.TileView
import com.example.ui.theme.GoldAccent
import kotlin.math.roundToInt

sealed class DragSource {
    data class Rack(val tile: Tile, val index: Int) : DragSource()
    data class Board(val placedTile: PlacedTile) : DragSource()
}

class DragDropController {
    var isDragging by mutableStateOf(false)
    var dragSource by mutableStateOf<DragSource?>(null)
    var dragPosition by mutableStateOf(Offset.Zero)
    var hoveredBoardCell by mutableStateOf<Pair<Int, Int>?>(null)
    var hoveredRackIndex by mutableStateOf<Int?>(null)

    var boardBounds by mutableStateOf<Rect?>(null)
    var rackBounds by mutableStateOf<Rect?>(null)

    fun startDrag(source: DragSource, startPos: Offset) {
        dragSource = source
        dragPosition = startPos
        isDragging = true
        com.example.audio.ScrabbleSoundEngine.playLiftSound()
        updateHoverTargets(startPos)
    }

    fun onDrag(dragAmount: Offset) {
        dragPosition += dragAmount
        com.example.audio.ScrabbleSoundEngine.playDragSound()
        updateHoverTargets(dragPosition)
    }

    private fun updateHoverTargets(pos: Offset) {
        val bBounds = boardBounds
        if (bBounds != null && bBounds.contains(pos)) {
            val cellW = bBounds.width / 15f
            val cellH = bBounds.height / 15f
            val c = ((pos.x - bBounds.left) / cellW).toInt().coerceIn(0, 14)
            val r = ((pos.y - bBounds.top) / cellH).toInt().coerceIn(0, 14)
            hoveredBoardCell = Pair(r, c)
            hoveredRackIndex = null
            return
        } else {
            hoveredBoardCell = null
        }

        val rBounds = rackBounds
        if (rBounds != null && rBounds.contains(pos)) {
            val slotW = rBounds.width / 7f
            val idx = ((pos.x - rBounds.left) / slotW).toInt().coerceIn(0, 6)
            hoveredRackIndex = idx
            return
        } else {
            hoveredRackIndex = null
        }
    }

    fun endDrag(
        onPlaceTileOnBoard: (Tile, Int, Int) -> Unit,
        onMoveTileOnBoard: (PlacedTile, Int, Int) -> Unit,
        onRecallTileToRack: (PlacedTile) -> Unit,
        onReorderRack: (Int, Int) -> Unit
    ) {
        val src = dragSource
        val targetCell = hoveredBoardCell
        val targetRack = hoveredRackIndex

        if (src != null) {
            com.example.audio.ScrabbleSoundEngine.playPlaceSound()
            when (src) {
                is DragSource.Rack -> {
                    if (targetCell != null) {
                        onPlaceTileOnBoard(src.tile, targetCell.first, targetCell.second)
                    } else if (targetRack != null && targetRack != src.index) {
                        onReorderRack(src.index, targetRack)
                    }
                }
                is DragSource.Board -> {
                    if (targetCell != null) {
                        if (targetCell.first != src.placedTile.row || targetCell.second != src.placedTile.col) {
                            onMoveTileOnBoard(src.placedTile, targetCell.first, targetCell.second)
                        }
                    } else if (targetRack != null || (rackBounds != null && dragPosition.y >= (rackBounds?.top ?: 0f) - 60f)) {
                        onRecallTileToRack(src.placedTile)
                    }
                }
            }
        }

        isDragging = false
        dragSource = null
        hoveredBoardCell = null
        hoveredRackIndex = null
    }

    fun cancelDrag() {
        isDragging = false
        dragSource = null
        hoveredBoardCell = null
        hoveredRackIndex = null
    }
}

@Composable
fun rememberDragDropController(): DragDropController {
    return remember { DragDropController() }
}

@Composable
fun FloatingDraggedTile(
    controller: DragDropController
) {
    if (!controller.isDragging) return
    val src = controller.dragSource ?: return

    val tile = when (src) {
        is DragSource.Rack -> src.tile
        is DragSource.Board -> src.placedTile.tile
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (controller.dragPosition.x - 24.dp.toPx()).roundToInt(),
                    y = (controller.dragPosition.y - 24.dp.toPx()).roundToInt()
                )
            }
            .size(48.dp)
            .shadow(16.dp, RoundedCornerShape(8.dp))
            .rotate(4f)
            .border(2.dp, GoldAccent, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        TileView(
            tile = tile,
            size = 48.dp,
            isSelected = true
        )
    }
}
