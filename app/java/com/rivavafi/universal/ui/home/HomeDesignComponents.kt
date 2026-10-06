package com.rivavafi.universal.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rivavafi.universal.ui.theme.*

/**
 * 3D Isometric Neon Glass Cubes Illustration with floating +14.8% Return Badge
 */
@Composable
fun IsometricNeonCubesGraphic(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "cubesGlow")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    Box(
        modifier = modifier
            .size(width = 145.dp, height = 115.dp)
            .offset(y = floatOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background ambient purple & cyan glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.35f),
                        Color(0xFF00A3FF).copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.52f, h * 0.48f),
                    radius = w * 0.55f
                ),
                center = Offset(w * 0.52f, h * 0.48f),
                radius = w * 0.55f
            )

            // Function to draw isometric cube
            fun drawIsoCube(
                cx: Float,
                cy: Float,
                radius: Float,
                topColors: List<Color>,
                leftColors: List<Color>,
                rightColors: List<Color>,
                edgeColor: Color
            ) {
                val dx = radius * 0.866f // cos(30°)
                val dy = radius * 0.5f   // sin(30°)

                // Top Face
                val topPath = Path().apply {
                    moveTo(cx, cy - radius)
                    lineTo(cx + dx, cy - dy)
                    lineTo(cx, cy)
                    lineTo(cx - dx, cy - dy)
                    close()
                }
                drawPath(
                    path = topPath,
                    brush = Brush.linearGradient(
                        colors = topColors,
                        start = Offset(cx - dx, cy - radius),
                        end = Offset(cx + dx, cy)
                    )
                )
                drawPath(
                    path = topPath,
                    color = edgeColor.copy(alpha = 0.85f),
                    style = Stroke(width = 2f)
                )

                // Left Face
                val leftPath = Path().apply {
                    moveTo(cx - dx, cy - dy)
                    lineTo(cx, cy)
                    lineTo(cx, cy + radius)
                    lineTo(cx - dx, cy + dy)
                    close()
                }
                drawPath(
                    path = leftPath,
                    brush = Brush.linearGradient(
                        colors = leftColors,
                        start = Offset(cx - dx, cy - dy),
                        end = Offset(cx, cy + radius)
                    )
                )
                drawPath(
                    path = leftPath,
                    color = edgeColor.copy(alpha = 0.75f),
                    style = Stroke(width = 2f)
                )

                // Right Face
                val rightPath = Path().apply {
                    moveTo(cx, cy)
                    lineTo(cx + dx, cy - dy)
                    lineTo(cx + dx, cy + dy)
                    lineTo(cx, cy + radius)
                    close()
                }
                drawPath(
                    path = rightPath,
                    brush = Brush.linearGradient(
                        colors = rightColors,
                        start = Offset(cx, cy),
                        end = Offset(cx + dx, cy + radius)
                    )
                )
                drawPath(
                    path = rightPath,
                    color = edgeColor.copy(alpha = 0.7f),
                    style = Stroke(width = 2f)
                )
            }

            // Draw 3 Overlapping Cubes:
            // 1. Bottom-Left Lime/Green Glowing Glass Cube
            drawIsoCube(
                cx = w * 0.30f,
                cy = h * 0.70f,
                radius = w * 0.17f,
                topColors = listOf(Color(0xFF00FF87).copy(alpha = 0.85f), Color(0xFF00D2FF).copy(alpha = 0.65f)),
                leftColors = listOf(Color(0xFF00E471).copy(alpha = 0.5f), Color(0xFF032219).copy(alpha = 0.85f)),
                rightColors = listOf(Color(0xFF00B4D8).copy(alpha = 0.45f), Color(0xFF021727).copy(alpha = 0.9f)),
                edgeColor = Color(0xFF00FF87)
            )

            // 2. Top-Center Magenta/Violet Translucent Cube
            drawIsoCube(
                cx = w * 0.54f,
                cy = h * 0.38f,
                radius = w * 0.20f,
                topColors = listOf(Color(0xFFE024C3).copy(alpha = 0.9f), Color(0xFF7C3AED).copy(alpha = 0.7f)),
                leftColors = listOf(Color(0xFF9333EA).copy(alpha = 0.55f), Color(0xFF140D2E).copy(alpha = 0.9f)),
                rightColors = listOf(Color(0xFF3B82F6).copy(alpha = 0.45f), Color(0xFF0A0E2A).copy(alpha = 0.9f)),
                edgeColor = Color(0xFF00E5FF)
            )

            // 3. Right Electric Cyan Wireframe Glass Cube
            drawIsoCube(
                cx = w * 0.76f,
                cy = h * 0.62f,
                radius = w * 0.18f,
                topColors = listOf(Color(0xFF00D2FF).copy(alpha = 0.5f), Color(0xFF2563EB).copy(alpha = 0.3f)),
                leftColors = listOf(Color(0xFF00A3FF).copy(alpha = 0.4f), Color(0xFF05122B).copy(alpha = 0.85f)),
                rightColors = listOf(Color(0xFF1D4ED8).copy(alpha = 0.45f), Color(0xFF040A1F).copy(alpha = 0.9f)),
                edgeColor = Color(0xFF00D2FF)
            )

            // Connecting Neon Trend Line to Badge
            val trailPath = Path().apply {
                moveTo(w * 0.76f, h * 0.62f)
                cubicTo(w * 0.82f, h * 0.50f, w * 0.85f, h * 0.38f, w * 0.88f, h * 0.22f)
            }
            drawPath(
                path = trailPath,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF00D2FF), Color(0xFF00FF87))
                ),
                style = Stroke(width = 2.4f, cap = StrokeCap.Round)
            )

            // Glowing Indicator Dot
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF00E5FF), Color.Transparent),
                    center = Offset(w * 0.88f, h * 0.22f),
                    radius = 12f
                ),
                center = Offset(w * 0.88f, h * 0.22f),
                radius = 12f
            )
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = Offset(w * 0.88f, h * 0.22f)
            )
        }

        // Floating +14.8% Capsule Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 2.dp, y = (-2).dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFF08201B).copy(alpha = 0.95f))
                .border(
                    width = 1.dp,
                    color = Color(0xFF00E471).copy(alpha = 0.75f),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+14.8%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    color = Color(0xFF00E471)
                )
            )
        }
    }
}

/**
 * Interactive Glowing Wave Line Chart for Rivava Wealth Card
 */
@Composable
fun NeonWaveChart(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Define smooth points for wave curve
        val p0 = Offset(0f, h * 0.78f)
        val p1 = Offset(w * 0.18f, h * 0.72f)
        val p2 = Offset(w * 0.35f, h * 0.58f)
        val p3 = Offset(w * 0.52f, h * 0.70f)
        val p4 = Offset(w * 0.68f, h * 0.45f)
        val p5 = Offset(w * 0.82f, h * 0.52f)
        val pPeak = Offset(w * 0.94f, h * 0.24f) // Peak dot

        val linePath = Path().apply {
            moveTo(p0.x, p0.y)
            cubicTo(w * 0.08f, h * 0.75f, w * 0.12f, h * 0.73f, p1.x, p1.y)
            cubicTo(w * 0.24f, h * 0.70f, w * 0.28f, h * 0.56f, p2.x, p2.y)
            cubicTo(w * 0.40f, h * 0.60f, w * 0.46f, h * 0.72f, p3.x, p3.y)
            cubicTo(w * 0.58f, h * 0.68f, w * 0.62f, h * 0.44f, p4.x, p4.y)
            cubicTo(w * 0.72f, h * 0.46f, w * 0.76f, h * 0.54f, p5.x, p5.y)
            cubicTo(w * 0.86f, h * 0.50f, w * 0.90f, h * 0.26f, pPeak.x, pPeak.y)
        }

        // Fill path under wave
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(pPeak.x, h)
            lineTo(0f, h)
            close()
        }

        // 1. Draw gradient area fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF00A3FF).copy(alpha = 0.28f),
                    Color(0xFF8B5CF6).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                startY = h * 0.2f,
                endY = h
            )
        )

        // 2. Draw subtle vertical dashed / light indicator line under peak
        drawLine(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF00A3FF).copy(alpha = 0.6f), Color.Transparent)
            ),
            start = pPeak,
            end = Offset(pPeak.x, h),
            strokeWidth = 1.2f
        )

        // 3. Draw ambient glow around wave line
        drawPath(
            path = linePath,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF00A3FF).copy(alpha = 0.4f),
                    Color(0xFF8B5CF6).copy(alpha = 0.5f),
                    Color(0xFF00A3FF).copy(alpha = 0.6f)
                )
            ),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // 4. Draw sharp foreground wave line
        drawPath(
            path = linePath,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF0072FF),
                    Color(0xFF00A3FF),
                    Color(0xFFA855F7),
                    Color(0xFF00E471)
                )
            ),
            style = Stroke(width = 2.8f, cap = StrokeCap.Round)
        )

        // 5. Draw glowing marker circle on peak
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF00A3FF).copy(alpha = 0.8f), Color.Transparent),
                center = pPeak,
                radius = 16f
            ),
            center = pPeak,
            radius = 16f
        )
        drawCircle(
            color = Color(0xFF00A3FF),
            radius = 6f,
            center = pPeak
        )
        drawCircle(
            color = Color.White,
            radius = 3.5f,
            center = pPeak
        )
    }
}

/**
 * 3D Golden Crown Graphic for Rivava Elite Card
 */
@Composable
fun GoldenCrownGraphic(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "crownAura")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    Box(
        modifier = modifier.size(110.dp, 90.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Golden Radial Ambient Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = auraAlpha * 0.45f),
                        Color(0xFFFFA500).copy(alpha = auraAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.55f
                )
            )

            // Base glowing pedestal ellipse
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = 0.5f),
                        Color(0xFFB8860B).copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.80f),
                    radius = w * 0.4f
                ),
                topLeft = Offset(w * 0.1f, h * 0.72f),
                size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.18f)
            )

            // 2. Crown Base Arc
            val baseLeft = Offset(w * 0.18f, h * 0.72f)
            val baseRight = Offset(w * 0.82f, h * 0.72f)
            val baseBottom = Offset(w * 0.5f, h * 0.78f)

            val baseBand = Path().apply {
                moveTo(baseLeft.x, baseLeft.y)
                quadraticBezierTo(baseBottom.x, baseBottom.y, baseRight.x, baseRight.y)
                lineTo(baseRight.x + 2f, baseRight.y + 10f)
                quadraticBezierTo(baseBottom.x, baseBottom.y + 10f, baseLeft.x - 2f, baseLeft.y + 10f)
                close()
            }
            drawPath(
                path = baseBand,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFB8860B), Color(0xFFFFD700), Color(0xFFFFF2A8), Color(0xFFFFD700), Color(0xFFB8860B))
                )
            )

            // 3. Crown Body with 5 Peaks
            val p1 = Offset(w * 0.20f, h * 0.38f) // Left-outer peak
            val v1 = Offset(w * 0.32f, h * 0.60f) // Valley 1
            val p2 = Offset(w * 0.38f, h * 0.26f) // Left-inner peak
            val v2 = Offset(w * 0.46f, h * 0.55f) // Valley 2
            val pCenter = Offset(w * 0.50f, h * 0.15f) // Tallest center peak
            val v3 = Offset(w * 0.54f, h * 0.55f) // Valley 3
            val p3 = Offset(w * 0.62f, h * 0.26f) // Right-inner peak
            val v4 = Offset(w * 0.68f, h * 0.60f) // Valley 4
            val p4 = Offset(w * 0.80f, h * 0.38f) // Right-outer peak

            val crownBody = Path().apply {
                moveTo(baseLeft.x, baseLeft.y)
                lineTo(p1.x, p1.y)
                lineTo(v1.x, v1.y)
                lineTo(p2.x, p2.y)
                lineTo(v2.x, v2.y)
                lineTo(pCenter.x, pCenter.y)
                lineTo(v3.x, v3.y)
                lineTo(p3.x, p3.y)
                lineTo(v4.x, v4.y)
                lineTo(p4.x, p4.y)
                lineTo(baseRight.x, baseRight.y)
                quadraticBezierTo(baseBottom.x, baseBottom.y, baseLeft.x, baseLeft.y)
                close()
            }

            drawPath(
                path = crownBody,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3B0),
                        Color(0xFFFFD700),
                        Color(0xFFE6A800),
                        Color(0xFF996500)
                    ),
                    startY = pCenter.y,
                    endY = baseBottom.y
                )
            )

            // Crown outline highlight
            drawPath(
                path = crownBody,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.9f), Color(0xFFFFD700), Color(0xFFB8860B))
                ),
                style = Stroke(width = 1.5f)
            )

            // Peak Pearls / Jewels
            fun drawJewel(pt: Offset, r: Float) {
                drawCircle(color = Color(0xFFFFD700), radius = r + 1f, center = pt)
                drawCircle(color = Color.White, radius = r * 0.6f, center = pt)
            }
            drawJewel(p1, 4.5f)
            drawJewel(p2, 5.5f)
            drawJewel(pCenter, 7.5f)
            drawJewel(p3, 5.5f)
            drawJewel(p4, 4.5f)

            // Sparkle Stars
            fun drawSparkle(center: Offset, size: Float) {
                val p = Path().apply {
                    moveTo(center.x, center.y - size)
                    quadraticBezierTo(center.x, center.y, center.x + size, center.y)
                    quadraticBezierTo(center.x, center.y, center.x, center.y + size)
                    quadraticBezierTo(center.x, center.y, center.x - size, center.y)
                    quadraticBezierTo(center.x, center.y, center.x, center.y - size)
                    close()
                }
                drawPath(p, color = Color.White)
            }
            drawSparkle(Offset(w * 0.16f, h * 0.22f), 6f)
            drawSparkle(Offset(w * 0.86f, h * 0.18f), 8f)
            drawSparkle(Offset(w * 0.65f, h * 0.12f), 5f)
        }
    }
}

/**
 * Neon Wave Chart with Top-Right Tooltip Pill (e.g. ₹2.48L)
 */
@Composable
fun NeonWaveChartWithTooltip(
    modifier: Modifier = Modifier,
    tooltipValue: String = "₹2.48L"
) {
    Box(modifier = modifier) {
        NeonWaveChart(modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-2).dp, y = (-2).dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0E253F).copy(alpha = 0.95f))
                .border(1.dp, Color(0xFF00A3FF).copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = tooltipValue,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Quick Action Item Card with Squircle Icon and 2-line Label
 */
@Composable
fun QuickActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 13.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2
        )
    }
}
