package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ElegantOrangeColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrangeContainer,
    onPrimaryContainer = OnPrimaryOrangeContainer,
    secondary = PrimaryOrangeLight,
    onSecondary = Color.White,
    secondaryContainer = PrimaryOrangeContainer,
    onSecondaryContainer = OnPrimaryOrangeContainer,
    tertiary = AccentEmerald,
    onTertiary = Color.White,
    background = AppBackgroundLight,
    onBackground = TextPrimary,
    surface = AppSurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = AppSurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = AppWhite,
    surfaceContainerHigh = AppWhite,
    surfaceContainerHighest = AppSurfaceVariantLight,
    outline = AppOutlineLight,
    outlineVariant = AppOutlineVariantLight,
    error = AccentError,
    onError = Color.White,
    errorContainer = AccentErrorContainer,
    onErrorContainer = AccentErrorText
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ElegantOrangeColorScheme,
        typography = Typography,
        content = content
    )
}
