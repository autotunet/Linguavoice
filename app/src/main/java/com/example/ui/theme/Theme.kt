package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DuoGreen,
    onPrimary = Color.White,
    primaryContainer = DuoDarkSurface,
    onPrimaryContainer = DuoGreenLight,
    secondary = DuoBlue,
    onSecondary = Color.White,
    secondaryContainer = DuoDarkBorder,
    onSecondaryContainer = DuoBlueLight,
    tertiary = DuoOrange,
    background = DuoDarkBg,
    surface = DuoDarkSurface,
    onBackground = DuoDarkText,
    onSurface = DuoDarkText,
    error = DuoRed,
    outline = DuoDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = DuoGreen,
    onPrimary = Color.White,
    primaryContainer = DuoGreenLight,
    onPrimaryContainer = DuoGreenDark,
    secondary = DuoBlue,
    onSecondary = Color.White,
    secondaryContainer = DuoBlueLight,
    onSecondaryContainer = DuoBlueDark,
    tertiary = DuoOrange,
    background = DuoBackground,
    surface = Color.White,
    onBackground = DuoTextPrimary,
    onSurface = DuoTextPrimary,
    error = DuoRed,
    outline = DuoCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
