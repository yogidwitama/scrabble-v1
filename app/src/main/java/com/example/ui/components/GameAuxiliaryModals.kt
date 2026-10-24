package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Tile
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.ScrabbleGreenDark
import com.example.ui.theme.TileBorder
import com.example.ui.theme.TileWood

@Composable
fun ExchangeTilesModal(
    rack: List<Tile>,
    bagRemainingCount: Int,
    onExchangeConfirmed: (List<Tile>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedTileIds = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ScrabbleGreenDark,
            border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "EXCHANGE TILES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = GoldAccent
                )

                Text(
                    text = if (bagRemainingCount >= 7) {
                        "Select 1 to 7 tiles to swap with the bag. Exchanging uses your turn."
                    } else {
                        "Bag has fewer than 7 tiles remaining ($bagRemainingCount). Exchanging is not allowed per official rules."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )

                if (bagRemainingCount >= 7) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        rack.forEach { tile ->
                            val isSelected = selectedTileIds.contains(tile.id)
                            TileView(
                                tile = tile,
                                size = 42.dp,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelected) selectedTileIds.remove(tile.id)
                                    else selectedTileIds.add(tile.id)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val toSwap = rack.filter { it.id in selectedTileIds }
                                onExchangeConfirmed(toSwap)
                            },
                            enabled = selectedTileIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Swap (${selectedTileIds.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun TileBagModal(
    distributionMap: Map<Char, Int>,
    totalRemaining: Int,
    onDismiss: () -> Unit
) {
    val alphabet = ('A'..'Z').toList() + listOf('?')

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ScrabbleGreenDark,
            border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TILE BAG ($totalRemaining TILES LEFT)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = GoldAccent
                )

                Text(
                    text = "Remaining distribution of letter tiles in the bag",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(alphabet) { char ->
                        val count = distributionMap[char] ?: 0
                        val score = Tile.letterScore(char)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (count > 0) TileWood else Color.White.copy(alpha = 0.08f))
                                .border(1.dp, TileBorder, RoundedCornerShape(8.dp))
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (char == '?') "BLANK" else "$char ($score)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (count > 0) Color.Black else Color.Gray
                                )
                                Text(
                                    text = "x$count left",
                                    fontSize = 10.sp,
                                    color = if (count > 0) Color(0xFF1B5E20) else Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
