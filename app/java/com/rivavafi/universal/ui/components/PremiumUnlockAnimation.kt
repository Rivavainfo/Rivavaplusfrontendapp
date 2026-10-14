package com.rivavafi.universal.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PremiumUnlockAnimation(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationStep by remember { mutableStateOf(0) }
    var isFadingOut by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Step 1: Display centered lock icon on slightly dimmed background (0 - 250ms)
        animationStep = 0
        delay(250)

        // Step 2: Animate lock opening with subtle scale and glow (250ms - 600ms)
        animationStep = 1
        delay(350)

        // Step 3: Animate text "Portfolio Unlocked" & "Welcome to Rivava Elite" (600ms - 1000ms)
        animationStep = 2
        delay(400)

        // Step 4: Smooth fade out and reveal unlocked UI (1000ms - 1150ms)
        isFadingOut = true
        delay(150)
        onAnimationComplete()
    }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isFadingOut) 0f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "overlayAlpha"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (animationStep >= 1) 1.15f else 1.0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "iconScale"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (animationStep >= 2) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "textAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "unlockGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(overlayAlpha)
            .background(Color(0xFF06070B).copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(iconScale)
        ) {
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                // Subtle ambient radial glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color(0xFF00E471).copy(alpha = if (animationStep >= 1) glowAlpha * 0.35f else 0.1f),
                            CircleShape
                        )
                )

                // Step 1: Centered closed lock
                if (animationStep == 0) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Step 2: Open lock with glow
                if (animationStep >= 1) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Unlocked",
                        tint = Color(0xFF00E471),
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 3: Subtle fade in for text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha)
            ) {
                Text(
                    text = "Portfolio Unlocked",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 24.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Welcome to Rivava Elite",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color(0xFF00E471),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
