package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = Color.Black,
    primaryContainer = ScrabbleGreenMid,
    onPrimaryContainer = GoldAccentLight,
    secondary = ScrabbleGreenLight,
    onSecondary = Color.White,
    tertiary = TripleWordRed,
    background = SurfaceDark,
    surface = Color(0xFF1B2820),
    onBackground = Color(0xFFEFEFEF),
    onSurface = Color(0xFFEFEFEF)
)

private val LightColorScheme = lightColorScheme(
    primary = ScrabbleGreenDark,
    onPrimary = Color.White,
    primaryContainer = ScrabbleGreenLight,
    onPrimaryContainer = Color.White,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    tertiary = TripleWordRed,
    background = SurfaceCream,
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve authentic Scrabble theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
