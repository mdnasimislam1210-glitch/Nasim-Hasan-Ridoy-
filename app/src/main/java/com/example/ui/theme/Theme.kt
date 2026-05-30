package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DragonBlitzColorScheme = darkColorScheme(
    primary = NeonRedRune,
    onPrimary = TextWhite,
    secondary = AmberGold,
    onSecondary = ObsidianBlack,
    tertiary = FireOrange,
    background = ObsidianBlack,
    onBackground = TextWhite,
    surface = PanelGray,
    onSurface = TextWhite,
    surfaceVariant = DarkLava,
    onSurfaceVariant = TextWhite,
    outline = BloodCrimson
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // We force the game to use our ultra-cute, dark-fantasy color scheme
    // to preserve design requirements.
    MaterialTheme(
        colorScheme = DragonBlitzColorScheme,
        typography = Typography,
        content = content
    )
}
