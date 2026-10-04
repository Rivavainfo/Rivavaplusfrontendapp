package com.rivavafi.universal.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivavafi.universal.R
import com.rivavafi.universal.ui.theme.*

@Composable
fun RivavaGlowingLogo(
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                // Multi-color ambient neon glow behind logo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            RivavaCyan.copy(alpha = pulseAlpha * 0.5f),
                            RivavaPink.copy(alpha = pulseAlpha * 0.3f),
                            Color.Transparent
                        ),
                        radius = size.toPx() * 0.9f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(size * 0.28f))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            RivavaCyan.copy(alpha = 0.7f),
                            RivavaPink.copy(alpha = 0.5f),
                            RivavaLime.copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(size * 0.28f)
                ),
            color = Color(0xFF0C0D14),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 0.16f),
                contentAlignment = Alignment.Center
            ) {
                // Rivava Tri-Color Identity Geometry
                // Left column: Top Pink square, Bottom Lime square
                // Right column: Cyan vertical rectangle
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(size * 0.05f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(size * 0.05f)
                    ) {
                        // Top Pink square
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(size * 0.06f))
                                .background(RivavaPink)
                        )
                        // Bottom Lime square
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(size * 0.06f))
                                .background(RivavaLime)
                        )
                    }
                    // Right Cyan rectangle
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight(0.9f)
                            .clip(RoundedCornerShape(size * 0.07f))
                            .background(RivavaCyan)
                    )
                }
            }
        }
    }
}

@Composable
fun RivavaBrandDisplay(
    showQuote: Boolean = true,
    logoSize: Dp = 72.dp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        RivavaGlowingLogo(size = logoSize)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RIVAVA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    color = Color.White
                )
            )
            Surface(
                color = RivavaCyan.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RivavaCyan.copy(alpha = 0.4f)),
                modifier = Modifier.padding(start = 6.dp)
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = RivavaCyan
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (showQuote) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "SMART FINANCIAL INTELLIGENCE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = RivavaCyan.copy(alpha = 0.85f),
                    letterSpacing = 2.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Be a master of your money",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Normal,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 0.3.sp
                )
            )
        }
    }
}

