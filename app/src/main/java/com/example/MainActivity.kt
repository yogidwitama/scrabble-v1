package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MultiplayerLobbyScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ScrabbleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScrabbleApp()
                }
            }
        }
    }
}

@Composable
fun ScrabbleApp(
    viewModel: ScrabbleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.currentScreen) {
        "home" -> {
            HomeScreen(viewModel = viewModel)
        }
        "lobby" -> {
            BackHandler {
                viewModel.navigateTo("home")
            }
            uiState.multiplayerRoom?.let { room ->
                MultiplayerLobbyScreen(
                    room = room,
                    viewModel = viewModel
                )
            } ?: HomeScreen(viewModel = viewModel)
        }
        "game" -> {
            BackHandler {
                viewModel.navigateTo("home")
            }
            GameScreen(viewModel = viewModel)
        }
        "game_over" -> {
            BackHandler {
                viewModel.navigateTo("home")
            }
            GameOverScreen(
                players = uiState.players,
                winner = uiState.winner,
                moveHistory = uiState.moveHistory,
                viewModel = viewModel
            )
        }
    }
}
