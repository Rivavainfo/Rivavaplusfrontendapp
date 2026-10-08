package com.rivavafi.universal.ui.theme

import androidx.compose.ui.graphics.Color

// Core AMOLED Black & Dark Obsidian Surfaces
val AmoledBlack = Color(0xFF08080B)
val DarkSurface = Color(0xFF101016)
val DarkSurfaceVariant = Color(0xFF1A1A24)
val DarkCardBg = Color(0xFF14141D)
val DarkCardBgElevated = Color(0xFF1C1C28)
val DarkCardBorder = Color(0xFF28283C)
val DarkCardBorderHighlight = Color(0xFF3C3C58)

// Rivava Logo Tri-Color Identity (Cyan, Pink, Lime)
val RivavaCyan = Color(0xFF00A3FF)        // Electric Sky Cyan
val RivavaPink = Color(0xFFFF2A85)        // Vibrant Neon Magenta / Pink
val RivavaLime = Color(0xFF00E471)        // Electric Neon Lime / Green

// Ambient Glow Tokens (20-25% opacity)
val RivavaCyanGlow = Color(0x3300A3FF)
val RivavaPinkGlow = Color(0x33FF2A85)
val RivavaLimeGlow = Color(0x3300E471)

// Material3 & App Theme Color Bindings
val PrimarySky = RivavaCyan
val PrimaryContainerSky = Color(0xFF0066CC)
val SecondaryPink = RivavaPink
val TertiaryEmerald = RivavaLime
val VibrantRed = RivavaPink
val LightPink = Color(0xFFFFAEDB)

// Dark Theme High-Legibility Typography Colors
val OnDarkSurface = Color(0xFFFFFFFF)        // Crisp, pure readable white
val OnDarkSurfaceVariant = Color(0xFF94A3B8) // Elegant readable slate gray
val TextMuted = Color(0xFF64748B)

// Welcome / Onboarding Tokens
val WelcomePrimaryLightBlue = RivavaCyan
val WelcomePrimaryContainer = Color(0xFF0088FF)
val WelcomeSurfaceContainerLow = Color(0xFF14141A)
val WelcomeSurfaceContainerHighest = Color(0xFF242432)
val WelcomeSecondaryPink = RivavaPink
val NyseBlack = Color(0xFF121216)
val NyseGold = Color(0xFFFFD700)

val PremiumGradientStart = RivavaCyan
val PremiumGradientEnd = Color(0xFF0066CC)
val EmeraldGreen = RivavaLime
val DeepBlue = RivavaCyan
val DeepBlueVariant = Color(0xFF0055AA)

// High-Impact Luxury Gradients
val RivavaBrandGradient = androidx.compose.ui.graphics.Brush.linearGradient(
    listOf(RivavaCyan, RivavaPink, RivavaLime)
)
val RivavaHeroCardGradient = androidx.compose.ui.graphics.Brush.linearGradient(
    listOf(Color(0xFF1B1D2C), Color(0xFF12131F), Color(0xFF0B0C12))
)
val RivavaCyanGradient = androidx.compose.ui.graphics.Brush.linearGradient(
    listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
)
val RivavaPinkGradient = androidx.compose.ui.graphics.Brush.linearGradient(
    listOf(Color(0xFFFF2A85), Color(0xFFFF6584))
)
val RivavaLimeGradient = androidx.compose.ui.graphics.Brush.linearGradient(
    listOf(Color(0xFF00E471), Color(0xFF00C2A0))
)
val RivavaGoldGradient = androidx.compose.ui.graphics.Brush.linearGradient(
    listOf(Color(0xFFFFDF00), Color(0xFFD4AF37), Color(0xFF996515))
)
val RivavaSubtleRimGradient = androidx.compose.ui.graphics.Brush.linearGradient(
    listOf(RivavaCyan.copy(alpha = 0.5f), RivavaPink.copy(alpha = 0.3f), Color.White.copy(alpha = 0.1f))
)

