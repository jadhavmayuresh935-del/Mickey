package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Instagram Sunset & Modern SaaS Palette
val IgPink = Color(0xFFE1306C)
val IgPurple = Color(0xFF833AB4)
val IgOrange = Color(0xFFF77737)
val IgYellow = Color(0xFFFFDC80)
val IgDarkViolet = Color(0xFF405DE6)

// Primary & Secondary Brand Tokens
val BrandPrimary = Color(0xFFC13584)
val BrandSecondary = Color(0xFFF56040)
val BrandTertiary = Color(0xFF833AB4)
val BrandAccent = Color(0xFFFCAF45)

// Light Palette
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F3F5)
val LightOnBackground = Color(0xFF191C1E)
val LightOnSurface = Color(0xFF191C1E)
val LightOutline = Color(0xFFE2E4E8)

// Dark Palette
val DarkBackground = Color(0xFF0F0E17)
val DarkSurface = Color(0xFF1A1826)
val DarkSurfaceVariant = Color(0xFF262438)
val DarkOnBackground = Color(0xFFF1F1F6)
val DarkOnSurface = Color(0xFFF1F1F6)
val DarkOutline = Color(0xFF37344D)

// Status & Metric Colors
val SuccessGreen = Color(0xFF00C853)
val WarningAmber = Color(0xFFFFAB00)
val InfoBlue = Color(0xFF2979FF)
val DraftGray = Color(0xFF8E8E93)

// Instagram Gradient Brush Helper
val InstagramGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF833AB4), // Deep Purple
        Color(0xFFC13584), // Fuchsia
        Color(0xFFE1306C), // Vibrant Pink
        Color(0xFFF77737), // Sunset Orange
        Color(0xFFFCAF45)  // Warm Gold
    )
)
