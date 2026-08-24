package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Elegant Orange (#FF5F00) Theme Palette
val PrimaryOrange = Color(0xFFFF5F00)
val PrimaryOrangeLight = Color(0xFFFF7D2E)
val PrimaryOrangeDark = Color(0xFFE04F00)
val PrimaryOrangeContainer = Color(0xFFFFF1E8)
val OnPrimaryOrangeContainer = Color(0xFF943200)
val PrimaryOrangeBorder = Color(0xFFFFCEB5)

// Gradient Brushes for Premium Visual Craft
val PrimaryOrangeGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF5F00), Color(0xFFFF7E33))
)
val PrimaryOrangeSubtleGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFF6F0), Color(0xFFFFFFFF))
)
val ActiveCardBorderBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFFF5F00), Color(0xFFFF9E66))
)

// Status & Semantic Accents
val AccentEmerald = Color(0xFF10B981)
val AccentEmeraldContainer = Color(0xFFECFDF5)
val AccentEmeraldText = Color(0xFF047857)

val AccentWarning = Color(0xFFF59E0B)
val AccentWarningContainer = Color(0xFFFFFBEB)
val AccentWarningText = Color(0xFFB45309)

val AccentError = Color(0xFFEF4444)
val AccentErrorContainer = Color(0xFFFEF2F2)
val AccentErrorText = Color(0xFFB91C1C)

// Surfaces & Backgrounds (Pure White & Subtle Warm Alabaster)
val AppWhite = Color(0xFFFFFFFF)
val AppBackgroundLight = Color(0xFFFAF7F5)
val AppSurfaceLight = Color(0xFFFFFFFF)
val AppSurfaceVariantLight = Color(0xFFF4EFEB)
val AppOutlineLight = Color(0xFFEAE2DC)
val AppOutlineVariantLight = Color(0xFFDBD1C8)

// Typography Palette (Warm Obsidian & Slate)
val TextPrimary = Color(0xFF181310)
val TextSecondary = Color(0xFF6B615A)
val TextMuted = Color(0xFF9E928A)
