package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF0F111A),
    secondary = TealAccent,
    onSecondary = Color(0xFF0F111A),
    tertiary = GoldPremium,
    onTertiary = Color(0xFF0F111A),
    background = BankBackground,
    onBackground = OnBackgroundWhite,
    surface = BankSurface,
    onSurface = OnBackgroundWhite,
    surfaceVariant = BankSurfaceVariant,
    onSurfaceVariant = TextSecondaryMuted,
    error = CoralDeduction,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for modern banking design
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful branded design
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
