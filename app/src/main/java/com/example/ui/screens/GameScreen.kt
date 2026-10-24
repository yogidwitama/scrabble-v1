package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapVerticalCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.ui.components.BingoCelebrationOverlay
import com.example.ui.components.BlankTilePickerModal
import com.example.ui.components.ChallengeDialog
import com.example.ui.components.DictionaryLookupModal
import com.example.ui.components.ExchangeTilesModal
import com.example.ui.components.RulesModal
import com.example.ui.components.ScrabbleBoardView
import com.example.ui.components.TileBagModal
import com.example.ui.components.TileRackView
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.ScrabbleGreenDark
import com.example.ui.theme.ScrabbleGreenMid
import com.example.ui.theme.TripleWordRed
import com.example.viewmodel.ScrabbleViewModel

import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import com.example.ui.components.BestMoveReviewModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: ScrabbleViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showTileBagModal by remember { mutableStateOf(false) }
    var showExchangeModal by remember { mutableStateOf(false) }
    var showDictionaryModal by remember { mutableStateOf(false) }
    var showRulesModal by remember { mutableStateOf(false) }
    var showBestMoveModal by remember { mutableStateOf(false) }
    var showMenuDropdown by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(com.example.audio.ScrabbleSoundEngine.isMuted()) }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissStatusMessage()
        }
    }

    val dragController = com.example.ui.dragdrop.rememberDragDropController()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (uiState.gameMode == GameMode.VS_AI) "VS AI (${uiState.aiDifficulty.label.split(" ")[0]})" else "MULTIPLAYER",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = GoldAccent
                                )
                                if (uiState.gameMode == GameMode.MULTIPLAYER) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = uiState.multiplayerRoom?.roomCode ?: "",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Tiles left in bag: ${uiState.tileBag.remainingCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateTo("home") }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home", tint = Color.White)
                        }
                    },
                    actions = {
                        if (uiState.gameMode == GameMode.VS_AI) {
                            IconButton(onClick = { showBestMoveModal = true }) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Best Move Review", tint = GoldAccent)
                            }
                        }
                        IconButton(onClick = {
                            isMuted = !isMuted
                            com.example.audio.ScrabbleSoundEngine.setMuted(isMuted)
                        }) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Sound Toggle",
                                tint = if (isMuted) Color.Gray else GoldAccent
                            )
                        }
                        IconButton(onClick = { showDictionaryModal = true }) {
                            Icon(Icons.Default.Book, contentDescription = "CSW24 Lookup", tint = GoldAccent)
                        }
                        IconButton(onClick = { showTileBagModal = true }) {
                            Icon(Icons.Default.Inventory2, contentDescription = "Tile Bag", tint = Color.White)
                        }
                        IconButton(onClick = { showMenuDropdown = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                        }

                        DropdownMenu(
                            expanded = showMenuDropdown,
                            onDismissRequest = { showMenuDropdown = false },
                            modifier = Modifier.background(ScrabbleGreenDark)
                        ) {
                            if (uiState.gameMode == GameMode.VS_AI) {
                                DropdownMenuItem(
                                    text = { Text("Best Move & Ceklis", color = GoldAccent) },
                                    onClick = {
                                        showMenuDropdown = false
                                        showBestMoveModal = true
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Scrabble Rules", color = Color.White) },
                                onClick = {
                                    showMenuDropdown = false
                                    showRulesModal = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Resign / Quit Match", color = TripleWordRed) },
                                onClick = {
                                    showMenuDropdown = false
                                    viewModel.navigateTo("home")
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = ScrabbleGreenDark)
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFF0A1B12)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF0A1B12)),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Players & Scores Strip
                PlayerScoreStrip(
                    players = uiState.players,
                    currentIndex = uiState.currentPlayerIndex,
                    isAiThinking = uiState.isAiThinking
                )

                // 2. 15x15 Scrabble Board
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ScrabbleBoardView(
                        board = uiState.board,
                        placedTiles = uiState.placedTiles,
                        selectedTile = uiState.selectedRackTile,
                        onSquareClicked = { r, c ->
                            viewModel.placeTileOnBoard(r, c)
                        },
                        onPlacedTileClicked = { pt ->
                            viewModel.recallPlacedTile(pt)
                        },
                        dragController = dragController,
                        onPlaceTileOnBoard = { tile, r, c ->
                            viewModel.placeTileDirectly(tile, r, c)
                        },
                        onMoveTileOnBoard = { pt, newR, newC ->
                            viewModel.movePlacedTile(pt, newR, newC)
                        },
                        onRecallTileToRack = { pt ->
                            viewModel.recallPlacedTile(pt)
                        },
                        onReorderRack = { fromIdx, toIdx ->
                            viewModel.reorderRackTiles(fromIdx, toIdx)
                        }
                    )
                }

                // 3. Move Preview / Info Banner
                MovePreviewBanner(
                    placedTilesCount = uiState.placedTiles.size,
                    isHumanTurn = uiState.isCurrentPlayerHuman,
                    isAiThinking = uiState.isAiThinking,
                    activePlayerName = uiState.currentPlayer?.name ?: "",
                    bestMoveHint = uiState.currentTurnBestMoveHint,
                    isBestMoveInGameEnabled = uiState.bestMoveSettings.reviewInGame,
                    onOpenBestMove = { showBestMoveModal = true }
                )

                // 4. Rack View
                val currentRack = uiState.currentPlayer?.rack ?: emptyList()
                TileRackView(
                    rack = currentRack,
                    selectedTileId = uiState.selectedRackTile?.id,
                    onTileSelected = { viewModel.selectRackTile(it) },
                    onShuffle = { viewModel.shuffleRack() },
                    onRecall = { viewModel.recallAllTiles() },
                    canRecall = uiState.placedTiles.isNotEmpty(),
                    dragController = dragController,
                    onPlaceTileOnBoard = { tile, r, c ->
                        viewModel.placeTileDirectly(tile, r, c)
                    },
                    onMoveTileOnBoard = { pt, newR, newC ->
                        viewModel.movePlacedTile(pt, newR, newC)
                    },
                    onRecallTileToRack = { pt ->
                        viewModel.recallPlacedTile(pt)
                    },
                    onReorderRack = { fromIdx, toIdx ->
                        viewModel.reorderRackTiles(fromIdx, toIdx)
                    }
                )

                // 5. Action Buttons Bar
                GameActionButtons(
                    isHumanTurn = uiState.isCurrentPlayerHuman,
                    hasPlacedTiles = uiState.placedTiles.isNotEmpty(),
                    canExchange = uiState.tileBag.remainingCount >= 7,
                    onSubmitPlay = { viewModel.submitTurn() },
                    onPassTurn = { viewModel.passTurn() },
                    onOpenExchange = { showExchangeModal = true }
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Top-level Floating Dragged Tile Overlay
        com.example.ui.dragdrop.FloatingDraggedTile(controller = dragController)
    }

    // Overlays & Modals
    uiState.bingoCelebration?.let { bingo ->
        BingoCelebrationOverlay(
            playerName = bingo.playerName,
            primaryWord = bingo.word,
            totalPoints = bingo.points,
            onDismiss = { viewModel.dismissBingoCelebration() }
        )
    }

    uiState.challengeState?.let { cs ->
        ChallengeDialog(
            challengeState = cs,
            onSkipChallenge = { viewModel.skipChallenge() },
            onChallengeWord = { viewModel.challengeWord() }
        )
    }

    if (uiState.pendingBlankSquare != null) {
        BlankTilePickerModal(
            onLetterChosen = { viewModel.chooseBlankLetter(it) },
            onDismiss = { viewModel.dismissBlankPicker() }
        )
    }

    if (showExchangeModal) {
        ExchangeTilesModal(
            rack = uiState.currentPlayer?.rack ?: emptyList(),
            bagRemainingCount = uiState.tileBag.remainingCount,
            onExchangeConfirmed = {
                showExchangeModal = false
                viewModel.exchangeTiles(it)
            },
            onDismiss = { showExchangeModal = false }
        )
    }

    if (showTileBagModal) {
        TileBagModal(
            distributionMap = uiState.tileBag.getDistributionMap(),
            totalRemaining = uiState.tileBag.remainingCount,
            onDismiss = { showTileBagModal = false }
        )
    }

    if (showDictionaryModal) {
        DictionaryLookupModal(onDismiss = { showDictionaryModal = false })
    }

    if (showRulesModal) {
        RulesModal(onDismiss = { showRulesModal = false })
    }

    if (showBestMoveModal) {
        BestMoveReviewModal(
            reviews = uiState.turnReviews,
            currentHint = uiState.currentTurnBestMoveHint,
            settings = uiState.bestMoveSettings,
            isPostGame = false,
            onUpdateSettings = { viewModel.updateBestMoveSettings(it) },
            onDismiss = { showBestMoveModal = false }
        )
    }
}

@Composable
private fun PlayerScoreStrip(
    players: List<com.example.model.Player>,
    currentIndex: Int,
    isAiThinking: Boolean
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F261B))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(players) { idx, player ->
            val isCurrentTurn = idx == currentIndex
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentTurn) ScrabbleGreenMid else Color.White.copy(alpha = 0.05f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isCurrentTurn) 1.5.dp else 0.5.dp,
                    color = if (isCurrentTurn) GoldAccent else Color.White.copy(alpha = 0.15f)
                ),
                modifier = Modifier.width(135.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(player.colorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (player.isAi) Icons.Default.SmartToy else Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = player.name,
                                fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isCurrentTurn) GoldAccent else Color.White
                            )
                            if (isCurrentTurn && isAiThinking && player.isAi) {
                                Spacer(modifier = Modifier.width(4.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 1.5.dp,
                                    color = GoldAccent
                                )
                            }
                        }
                        Text(
                            text = "${player.score} PTS",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MovePreviewBanner(
    placedTilesCount: Int,
    isHumanTurn: Boolean,
    isAiThinking: Boolean,
    activePlayerName: String,
    bestMoveHint: com.example.ai.CandidateMove?,
    isBestMoveInGameEnabled: Boolean,
    onOpenBestMove: () -> Unit
) {
    Surface(
        color = Color(0xFF132B1E),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isAiThinking) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI is computing best move...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            } else if (!isHumanTurn) {
                Text(
                    text = "$activePlayerName's turn",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            } else if (placedTilesCount > 0) {
                Text(
                    text = "$placedTilesCount tile(s) placed • Tap Play to score",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            } else {
                Text(
                    text = "Drag or tap tile to place on board",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // In-game Best Move hint pill (if enabled and on human turn)
            if (isHumanTurn && isBestMoveInGameEnabled && bestMoveHint != null && !isAiThinking) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent.copy(alpha = 0.18f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onOpenBestMove() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Hint",
                        tint = GoldAccent,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Best: \"${bestMoveHint.primaryWord}\" (+${bestMoveHint.score}p)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccentLight
                    )
                }
            }
        }
    }
}

@Composable
private fun GameActionButtons(
    isHumanTurn: Boolean,
    hasPlacedTiles: Boolean,
    canExchange: Boolean,
    onSubmitPlay: () -> Unit,
    onPassTurn: () -> Unit,
    onOpenExchange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Exchange Button
        OutlinedButton(
            onClick = onOpenExchange,
            enabled = isHumanTurn && !hasPlacedTiles,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("Exchange", fontSize = 11.sp)
        }

        // Pass Button
        OutlinedButton(
            onClick = onPassTurn,
            enabled = isHumanTurn,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("Pass", fontSize = 11.sp)
        }

        // PLAY / SUBMIT WORD Button
        Button(
            onClick = onSubmitPlay,
            enabled = isHumanTurn && hasPlacedTiles,
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldAccent,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1.3f)
                .testTag("play_word_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("PLAY WORD", fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
}
