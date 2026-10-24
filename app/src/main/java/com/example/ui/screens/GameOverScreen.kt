package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.BestMoveReviewModal
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMove
import com.example.model.Player
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.ScrabbleGreenDark
import com.example.ui.theme.TripleWordRed
import com.example.viewmodel.ScrabbleViewModel

@Composable
fun GameOverScreen(
    players: List<Player>,
    winner: Player?,
    moveHistory: List<GameMove>,
    viewModel: ScrabbleViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showReviewModal by remember { mutableStateOf(false) }
    val sortedPlayers = players.sortedByDescending { it.score }
    val highestScoringMove = moveHistory.maxByOrNull { it.pointsScored }

    Scaffold(
        containerColor = Color(0xFF0D2418)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Winner Trophy Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = ScrabbleGreenDark,
                    border = androidx.compose.foundation.BorderStroke(2.5.dp, GoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(GoldAccent.copy(alpha = 0.2f), CircleShape)
                                .border(2.dp, GoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = GoldAccent,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "GAME OVER",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = GoldAccentLight
                        )

                        Text(
                            text = "${winner?.name ?: "Player"} WINS!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )

                        Text(
                            text = "Final Score: ${winner?.score ?: 0} Points",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = GoldAccent,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // High Score Word Highlight
            if (highestScoringMove != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "HIGHEST SCORING PLAY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                                Text(
                                    text = "\"${highestScoringMove.primaryWord}\" by ${highestScoringMove.playerName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "+${highestScoringMove.pointsScored} PTS",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = GoldAccentLight
                            )
                        }
                    }
                }
            }

            // Final Standings Table
            item {
                Text(
                    text = "FINAL STANDINGS",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = GoldAccent,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            itemsIndexed(sortedPlayers) { rank, player ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (rank == 0) GoldAccent else Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${rank + 1}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = if (rank == 0) GoldAccent else Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = player.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${player.score} PTS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (rank == 0) GoldAccent else Color.White
                        )
                    }
                }
            }

            // Best Move Analysis Button (VS AI or any match with recorded turn reviews)
            if (uiState.turnReviews.isNotEmpty() || uiState.gameMode == com.example.model.GameMode.VS_AI) {
                item {
                    Button(
                        onClick = { showReviewModal = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScrabbleGreenDark,
                            contentColor = GoldAccent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.turnReviews.isNotEmpty()) {
                                "Review Best Move & Analisis (${uiState.turnReviews.size} Giliran)"
                            } else {
                                "Pengaturan & Review Best Move"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo("home") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Home Menu")
                    }

                    Button(
                        onClick = {
                            viewModel.startVsAiGame()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Again", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showReviewModal) {
        BestMoveReviewModal(
            reviews = uiState.turnReviews,
            currentHint = null,
            settings = uiState.bestMoveSettings,
            isPostGame = true,
            onUpdateSettings = { viewModel.updateBestMoveSettings(it) },
            onDismiss = { showReviewModal = false }
        )
    }
}
