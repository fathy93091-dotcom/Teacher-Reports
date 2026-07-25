package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.Black,
    secondary = SecondaryDark,
    onSecondary = Color.Black,
    background = BackgroundDark,
    onBackground = IceWhiteText,
    surface = SurfaceDark,
    onSurface = IceWhiteText,
    surfaceVariant = DeepEmeraldCharcoal,
    onSurfaceVariant = CoolGrayText,
    outline = CoolGrayText
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.White,
    secondary = SandGold,
    onSecondary = Color.White,
    background = SageGrayBg,
    onBackground = TextCharcoal,
    surface = CardWhite,
    onSurface = TextCharcoal,
    surfaceVariant = EmeraldLight,
    onSurfaceVariant = TextSecondaryGray,
    outline = TextSecondaryGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
