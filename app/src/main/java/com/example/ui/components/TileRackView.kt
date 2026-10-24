package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Tile
import com.example.ui.dragdrop.DragDropController
import com.example.ui.dragdrop.DragSource
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.WoodRackDark
import com.example.ui.theme.WoodRackLight

@Composable
fun TileRackView(
    rack: List<Tile>,
    selectedTileId: String?,
    onTileSelected: (Tile) -> Unit,
    onShuffle: () -> Unit,
    onRecall: () -> Unit,
    canRecall: Boolean,
    dragController: DragDropController? = null,
    onPlaceTileOnBoard: ((Tile, Int, Int) -> Unit)? = null,
    onMoveTileOnBoard: ((com.example.model.PlacedTile, Int, Int) -> Unit)? = null,
    onRecallTileToRack: ((com.example.model.PlacedTile) -> Unit)? = null,
    onReorderRack: ((Int, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Wood Rack Shelf Container
        Surface(
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    dragController?.rackBounds = coordinates.boundsInRoot()
                }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                WoodRackLight,
                                WoodRackDark,
                                Color(0xFF3E2723)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFF4E342E), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Shuffle Button
                    IconButton(
                        onClick = onShuffle,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("shuffle_rack_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Shuffle Rack",
                            tint = GoldAccent
                        )
                    }

                    // 7-Tile Rack Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rack.forEachIndexed { index, tile ->
                            val isHovered = dragController?.isDragging == true &&
                                    dragController.hoveredRackIndex == index
                            val isBeingDragged = dragController?.isDragging == true &&
                                    (dragController.dragSource as? DragSource.Rack)?.tile?.id == tile.id

                            val dragModifier = if (dragController != null) {
                                Modifier.pointerInput(tile.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { offset ->
                                            dragController.startDrag(
                                                DragSource.Rack(tile, index),
                                                startPos = offset
                                            )
                                        },
                                        onDrag = { _, dragAmount ->
                                            dragController.onDrag(dragAmount)
                                        },
                                        onDragEnd = {
                                            if (onPlaceTileOnBoard != null && onMoveTileOnBoard != null && onRecallTileToRack != null && onReorderRack != null) {
                                                dragController.endDrag(
                                                    onPlaceTileOnBoard = onPlaceTileOnBoard,
                                                    onMoveTileOnBoard = onMoveTileOnBoard,
                                                    onRecallTileToRack = onRecallTileToRack,
                                                    onReorderRack = onReorderRack
                                                )
                                            } else {
                                                dragController.cancelDrag()
                                            }
                                        },
                                        onDragCancel = {
                                            dragController.cancelDrag()
                                        }
                                    )
                                }
                            } else Modifier

                            Box(
                                modifier = Modifier
                                    .then(if (isHovered) Modifier.border(2.dp, GoldAccent, RoundedCornerShape(6.dp)) else Modifier)
                                    .then(dragModifier),
                                contentAlignment = Alignment.Center
                            ) {
                                TileView(
                                    tile = tile,
                                    size = 42.dp,
                                    isSelected = (tile.id == selectedTileId),
                                    modifier = if (isBeingDragged) Modifier.clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.35f)) else Modifier,
                                    onClick = { onTileSelected(tile) }
                                )
                            }
                        }

                        // Empty slots if rack has fewer than 7 tiles
                        val emptySlotsCount = (7 - rack.size).coerceAtLeast(0)
                        repeat(emptySlotsCount) { emptyIdx ->
                            val slotIndex = rack.size + emptyIdx
                            val isHovered = dragController?.isDragging == true &&
                                    dragController.hoveredRackIndex == slotIndex

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isHovered) GoldAccent.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.2f))
                                    .border(
                                        if (isHovered) 2.dp else 1.dp,
                                        if (isHovered) GoldAccent else Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                            )
                        }
                    }

                    // Recall Button
                    IconButton(
                        onClick = onRecall,
                        enabled = canRecall,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("recall_tiles_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Recall Placed Tiles",
                            tint = if (canRecall) GoldAccent else Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
