package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Board
import com.example.model.PlacedTile
import com.example.model.PremiumSquare
import com.example.model.Tile
import com.example.ui.dragdrop.DragDropController
import com.example.ui.dragdrop.DragSource
import com.example.ui.theme.BoardBackground
import com.example.ui.theme.BoardGridLine
import com.example.ui.theme.BoardSquareDefault
import com.example.ui.theme.CenterStarGold
import com.example.ui.theme.DoubleLetterCyan
import com.example.ui.theme.DoubleWordPink
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TripleLetterBlue
import com.example.ui.theme.TripleWordRed

@Composable
fun ScrabbleBoardView(
    board: Board,
    placedTiles: List<PlacedTile>,
    selectedTile: Tile?,
    onSquareClicked: (Int, Int) -> Unit,
    onPlacedTileClicked: (PlacedTile) -> Unit,
    dragController: DragDropController? = null,
    onPlaceTileOnBoard: ((Tile, Int, Int) -> Unit)? = null,
    onMoveTileOnBoard: ((PlacedTile, Int, Int) -> Unit)? = null,
    onRecallTileToRack: ((PlacedTile) -> Unit)? = null,
    onReorderRack: ((Int, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 2.5f)
        if (scale > 1f) {
            val maxOffset = (scale - 1f) * 350f
            offset = Offset(
                x = (offset.x + offsetChange.x).coerceIn(-maxOffset, maxOffset),
                y = (offset.y + offsetChange.y).coerceIn(-maxOffset, maxOffset)
            )
        } else {
            offset = Offset.Zero
        }
    }

    val placedMap = remember(placedTiles) {
        placedTiles.associateBy { Pair(it.row, it.col) }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
            .aspectRatio(1f)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(BoardBackground)
            .border(2.dp, Color(0xFF6D4C41), RoundedCornerShape(12.dp))
            .onGloballyPositioned { coordinates ->
                dragController?.boardBounds = coordinates.boundsInRoot()
            }
            .transformable(state = transformState)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        val cellSize = maxWidth / 15f

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (r in 0 until Board.BOARD_SIZE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (c in 0 until Board.BOARD_SIZE) {
                        val boardSquare = board.getSquare(r, c)
                        val newlyPlaced = placedMap[Pair(r, c)]
                        val premium = Board.getPremium(r, c)

                        val isHovered = dragController?.isDragging == true &&
                                dragController.hoveredBoardCell?.first == r &&
                                dragController.hoveredBoardCell?.second == c &&
                                boardSquare?.isOccupied == false

                        val isTileBeingDraggedFromHere = dragController?.isDragging == true &&
                                (dragController.dragSource as? DragSource.Board)?.placedTile?.let {
                                    it.row == r && it.col == c
                                } == true

                        val tileDragModifier = if (dragController != null && newlyPlaced != null) {
                            Modifier.pointerInput(newlyPlaced.tile.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { startOffset ->
                                        dragController.startDrag(
                                            DragSource.Board(newlyPlaced),
                                            startPos = startOffset
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
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(0.75.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        isHovered -> GoldAccent.copy(alpha = 0.5f)
                                        boardSquare?.isOccupied == true || newlyPlaced != null -> Color.Transparent
                                        premium == PremiumSquare.TW -> TripleWordRed
                                        premium == PremiumSquare.DW -> DoubleWordPink
                                        premium == PremiumSquare.TL -> TripleLetterBlue
                                        premium == PremiumSquare.DL -> DoubleLetterCyan
                                        premium == PremiumSquare.STAR -> CenterStarGold
                                        else -> BoardSquareDefault
                                    }
                                )
                                .border(
                                    if (isHovered) 2.dp else 0.5.dp,
                                    if (isHovered) GoldAccent else BoardGridLine,
                                    RoundedCornerShape(2.dp)
                                )
                                .then(tileDragModifier)
                                .clickable {
                                    if (newlyPlaced != null) {
                                        onPlacedTileClicked(newlyPlaced)
                                    } else if (boardSquare?.isOccupied == false) {
                                        onSquareClicked(r, c)
                                    }
                                }
                                .testTag("square_${r}_${c}"),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                // Locked tile from previous turn
                                boardSquare?.tile != null -> {
                                    TileView(
                                        tile = boardSquare.tile,
                                        size = cellSize * 0.95f
                                    )
                                }
                                // Newly placed tile in current turn
                                newlyPlaced != null -> {
                                    TileView(
                                        tile = newlyPlaced.tile,
                                        size = cellSize * 0.95f,
                                        isNewlyPlaced = true,
                                        modifier = if (isTileBeingDraggedFromHere) {
                                            Modifier.clip(RoundedCornerShape(2.dp)).background(Color.Black.copy(alpha = 0.35f))
                                        } else Modifier
                                    )
                                }
                                // Hovered drop target indicator
                                isHovered -> {
                                    Text(
                                        text = "+",
                                        color = Color.Black,
                                        fontSize = (cellSize.value * 0.6f).sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                // Center Star
                                premium == PremiumSquare.STAR -> {
                                    Text(
                                        text = "★",
                                        color = Color.White,
                                        fontSize = (cellSize.value * 0.5f).sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                // Premium square label (TW, DW, TL, DL)
                                premium != PremiumSquare.NONE -> {
                                    Text(
                                        text = premium.shortLabel,
                                        color = Color.White,
                                        fontSize = (cellSize.value * 0.32f).sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.SansSerif,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
