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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Color as AndroidColor
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
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

/**
 * Speedometer Gauge Icon for Credit Score Card
 */
@Composable
fun SpeedometerGaugeIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val stroke = size.width * 0.12f
        val r = (size.width - stroke) / 2
        val center = Offset(size.width / 2f, size.height * 0.55f)

        // Background track arc
        drawArc(
            color = color.copy(alpha = 0.35f),
            startAngle = 145f,
            sweepAngle = 250f,
            useCenter = false,
            topLeft = Offset(center.x - r, center.y - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Active progress arc
        drawArc(
            color = color,
            startAngle = 145f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x - r, center.y - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Needle
        val angleRad = Math.toRadians(310.0)
        val needleEnd = Offset(
            center.x + (r * 0.68f) * kotlin.math.cos(angleRad).toFloat(),
            center.y + (r * 0.68f) * kotlin.math.sin(angleRad).toFloat()
        )
        drawLine(
            color = color,
            start = center,
            end = needleEnd,
            strokeWidth = stroke * 0.85f,
            cap = StrokeCap.Round
        )
        drawCircle(color = color, radius = stroke * 0.9f, center = center)
    }
}

/**
 * 1. Rivava Elite Hero Banner Matching Reference Image Exactly
 */
@Composable
fun HomeEliteHeroBanner(
    seatsRemaining: Int = 93,
    onJoinClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFFD700).copy(alpha = 0.45f),
                            Color(0xFF8B6914).copy(alpha = 0.25f),
                            Color(0xFFFFD700).copy(alpha = 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .clickable { onJoinClick() },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0A06)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(218.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF16130B),
                                Color(0xFF0F0C07),
                                Color(0xFF080603)
                            )
                        )
                    )
            ) {
                // Advisor photo with golden crown on the right
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.rivavafi.universal.R.drawable.elite_advisor_hero),
                    contentDescription = "Elite Advisor",
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(235.dp)
                        .align(Alignment.CenterEnd),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    alignment = Alignment.CenterEnd
                )

                // Smooth horizontal fade from solid dark on left to transparent on right
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0C0A06),
                                    Color(0xFF0C0A06).copy(alpha = 0.98f),
                                    Color(0xFF0C0A06).copy(alpha = 0.82f),
                                    Color(0xFF0C0A06).copy(alpha = 0.40f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = 620f
                            )
                        )
                )

                // Foreground Content Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row: RIVAVA ELITE on Left, 93/100 Seats badge on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("👑", fontSize = 15.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "RIVAVA ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.3.sp
                                    )
                                )
                                Text(
                                    text = "ELITE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.3.sp
                                    )
                                )
                            }
                        }

                        // Seats Badge: "93/100 Seats"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFF261D07).copy(alpha = 0.95f))
                                .border(
                                    1.dp,
                                    Color(0xFFFFD700).copy(alpha = 0.55f),
                                    RoundedCornerShape(999.dp)
                                )
                                .padding(horizontal = 9.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${if (seatsRemaining > 0) seatsRemaining else 93}/100 Seats",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Headline + Subtitle
                    Column {
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                withStyle(
                                    androidx.compose.ui.text.SpanStyle(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        letterSpacing = (-0.3).sp
                                    )
                                ) {
                                    append("1-on-1 Wealth Guidance\nwith ")
                                }
                                withStyle(
                                    androidx.compose.ui.text.SpanStyle(
                                        color = Color(0xFFFFC83B),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        letterSpacing = (-0.3).sp
                                    )
                                ) {
                                    append("Experts")
                                }
                            },
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "Exclusive research, in-house AI,\npersonal sessions & wealth creation tools.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.72f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    // Bottom Row: "Join Rivava Elite →" Button + "Limited to 100 Members"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Surface(
                            onClick = onJoinClick,
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFC83B)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "Join Rivava Elite",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F0B00),
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = "→",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F0B00),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Text under advisor pointing hand
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 4.dp, bottom = 2.dp)
                        ) {
                            Text(
                                text = "Limited to",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFFD700).copy(alpha = 0.85f),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "100 Members",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFFD700),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }

/**
 * 2. 1 in 1 Sessions with Our Advisor Micro Card Matching Reference Image
 */
@Composable
fun HomeAdvisorMicroBanner(
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF1E253A), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101524))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: Circular 3D cartoon advisor avatar
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.rivavafi.universal.R.drawable.advisor_session_avatar),
                contentDescription = "Advisor Avatar",
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            // Middle: Headline and Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "1 in 1 Sessions with Our Advisor",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Get personalised market insights,\nportfolio guidance & investment strategies.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.62f),
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp
                    )
                )
            }

            // Right: Chevron >
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 3. Quick Actions 4-Card Grid
 */
@Composable
fun HomeQuickActionsSection(
    onAddTransactionClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onPortfolioClick: () -> Unit = {},
    onToolsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Action Cards in an evenly aligned grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            // Card 1: Add Transaction
            HomeActionCard(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF00E575)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF042817),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = "Add Transaction",
                subtitle = "Debit & Credit",
                subtitleColor = Color(0xFF00E575),
                onClick = onAddTransactionClick
            )

            // Card 2: Analytics
            HomeActionCard(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF9333EA), Color(0xFF7E22CE))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = "Analytics",
                subtitle = "Charts & Insights",
                subtitleColor = Color.White.copy(alpha = 0.55f),
                onClick = onAnalyticsClick
            )

            // Card 3: Portfolio
            HomeActionCard(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFE11D48), Color(0xFFBE123C))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = "Portfolio",
                subtitle = "Stocks & Crypto",
                subtitleColor = Color.White.copy(alpha = 0.55f),
                onClick = onPortfolioClick
            )

            // Card 4: Tools
            HomeActionCard(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF00A3FF), Color(0xFF0284C7))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = "Tools",
                subtitle = "Calculators",
                subtitleColor = Color.White.copy(alpha = 0.55f),
                onClick = onToolsClick
            )
        }
    }
}

/**
 * Phone container with bolt icon for My UPI
 */
@Composable
fun UpiPhoneBoltIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stroke = 1.8.dp.toPx()

            // Phone rounded rect outline
            val rectWidth = w * 0.62f
            val rectHeight = h * 0.88f
            val left = (w - rectWidth) / 2f
            val top = (h - rectHeight) / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(rectWidth, rectHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = stroke)
            )
        }
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(15.dp)
        )
    }
}

/**
 * Diagonal Opposing Arrows Icon for R&E Calculator
 */
@Composable
fun DiagonalOpposingArrowsIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF34D399)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.2.dp.toPx()

        // 1. Top-right pointing arrow ↗
        val a1Start = Offset(w * 0.40f, h * 0.50f)
        val a1End = Offset(w * 0.72f, h * 0.18f)
        drawLine(color = color, start = a1Start, end = a1End, strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = color, start = a1End, end = Offset(w * 0.52f, h * 0.18f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = color, start = a1End, end = Offset(w * 0.72f, h * 0.38f), strokeWidth = stroke, cap = StrokeCap.Round)

        // 2. Bottom-left pointing arrow ↙
        val a2Start = Offset(w * 0.60f, h * 0.50f)
        val a2End = Offset(w * 0.28f, h * 0.82f)
        drawLine(color = color, start = a2Start, end = a2End, strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = color, start = a2End, end = Offset(w * 0.48f, h * 0.82f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color = color, start = a2End, end = Offset(w * 0.28f, h * 0.62f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun HomeActionCard(
    iconContent: @Composable () -> Unit,
    title: String,
    subtitle: String,
    subtitleColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF1C2235), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101524))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            iconContent()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                maxLines = 1,
                softWrap = false,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.8.sp,
                    fontWeight = FontWeight.Medium,
                    color = subtitleColor
                ),
                maxLines = 1,
                softWrap = false,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

enum class TrackPeriod(val label: String, val daysCount: Int) {
    TODAY("Today", 1),
    LAST_7_DAYS("Last 7 Days", 7),
    LAST_30_DAYS("Last 30 Days", 30)
}

data class ChartBarData(
    val label: String,
    val debitAmount: Double,
    val creditAmount: Double
)

/**
 * Redesigned Track Your Money Section with Time-Period Selector, Detailed Statistics & Interactive Trends Chart
 */
@Composable
fun HomeTrackMoneySection(
    transactions: List<com.rivavafi.universal.data.local.TransactionEntity> = emptyList(),
    onClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(TrackPeriod.LAST_7_DAYS) }
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    val locale = java.util.Locale.getDefault()

    // Calculate start timestamp for selected period in local timezone
    val (periodStartTime, periodDaysCount) = remember(selectedPeriod) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)

        when (selectedPeriod) {
            TrackPeriod.TODAY -> Pair(cal.timeInMillis, 1)
            TrackPeriod.LAST_7_DAYS -> {
                cal.add(java.util.Calendar.DAY_OF_YEAR, -6)
                Pair(cal.timeInMillis, 7)
            }
            TrackPeriod.LAST_30_DAYS -> {
                cal.add(java.util.Calendar.DAY_OF_YEAR, -29)
                Pair(cal.timeInMillis, 30)
            }
        }
    }

    // Filter transactions for selected period
    val periodTransactions = remember(transactions, periodStartTime) {
        transactions.filter { it.date >= periodStartTime }
    }

    // Statistics calculations
    val totalSpent = remember(periodTransactions) {
        periodTransactions.filter { txn ->
            val t = txn.type.uppercase()
            t == "DEBIT" || t == "EXPENSE" || t == "BILL_PENDING" || t == "PAYMENT"
        }.sumOf { it.amount }
    }

    val totalReceived = remember(periodTransactions) {
        periodTransactions.filter { txn ->
            val t = txn.type.uppercase()
            t == "CREDIT" || t == "INCOME" || t == "REWARD"
        }.sumOf { it.amount }
    }

    val netCashFlow = totalReceived - totalSpent
    val transactionCount = periodTransactions.size

    val topCategory = remember(periodTransactions) {
        val debits = periodTransactions.filter { txn ->
            val t = txn.type.uppercase()
            t == "DEBIT" || t == "EXPENSE" || t == "BILL_PENDING" || t == "PAYMENT"
        }
        if (debits.isEmpty()) "N/A"
        else debits.groupBy { it.category.ifBlank { "General" } }
            .maxByOrNull { entry -> entry.value.sumOf { it.amount } }?.key ?: "N/A"
    }

    val avgDailySpending = totalSpent / periodDaysCount

    // Grouping for Chart Data
    val chartData = remember(selectedPeriod, periodTransactions) {
        when (selectedPeriod) {
            TrackPeriod.TODAY -> {
                // 6 buckets of 4 hours: 00-04, 04-08, 08-12, 12-16, 16-20, 20-24
                val buckets = listOf("00:00", "04:00", "08:00", "12:00", "16:00", "20:00")
                val debitSums = DoubleArray(6)
                val creditSums = DoubleArray(6)

                periodTransactions.forEach { txn ->
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = txn.date }
                    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                    val idx = (hour / 4).coerceIn(0, 5)
                    val t = txn.type.uppercase()
                    if (t == "CREDIT" || t == "INCOME" || t == "REWARD") {
                        creditSums[idx] += txn.amount
                    } else {
                        debitSums[idx] += txn.amount
                    }
                }

                buckets.mapIndexed { i, label -> ChartBarData(label, debitSums[i], creditSums[i]) }
            }

            TrackPeriod.LAST_7_DAYS -> {
                // 7 days
                val dayFormat = java.text.SimpleDateFormat("EEE", locale)
                val cal = java.util.Calendar.getInstance()
                val list = mutableListOf<ChartBarData>()

                for (i in 6 downTo 0) {
                    val targetCal = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, -i)
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    val startTime = targetCal.timeInMillis
                    targetCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    val endTime = targetCal.timeInMillis

                    val dayTxns = periodTransactions.filter { it.date in startTime until endTime }
                    val debits = dayTxns.filter {
                        val t = it.type.uppercase()
                        t == "DEBIT" || t == "EXPENSE" || t == "BILL_PENDING" || t == "PAYMENT"
                    }.sumOf { it.amount }
                    val credits = dayTxns.filter {
                        val t = it.type.uppercase()
                        t == "CREDIT" || t == "INCOME" || t == "REWARD"
                    }.sumOf { it.amount }

                    val label = dayFormat.format(java.util.Date(startTime))
                    list.add(ChartBarData(label, debits, credits))
                }
                list
            }

            TrackPeriod.LAST_30_DAYS -> {
                // 6 grouped 5-day intervals or 10 3-day intervals. Let's do 6 intervals for clean x-axis
                val dateFormat = java.text.SimpleDateFormat("dd MMM", locale)
                val list = mutableListOf<ChartBarData>()

                for (i in 5 downTo 0) {
                    val startCal = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, -(i * 5 + 4))
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    val startTime = startCal.timeInMillis

                    val endCal = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, -(i * 5))
                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                        set(java.util.Calendar.MINUTE, 59)
                        set(java.util.Calendar.SECOND, 59)
                        set(java.util.Calendar.MILLISECOND, 999)
                    }
                    val endTime = endCal.timeInMillis

                    val rangeTxns = periodTransactions.filter { it.date in startTime..endTime }
                    val debits = rangeTxns.filter {
                        val t = it.type.uppercase()
                        t == "DEBIT" || t == "EXPENSE" || t == "BILL_PENDING" || t == "PAYMENT"
                    }.sumOf { it.amount }
                    val credits = rangeTxns.filter {
                        val t = it.type.uppercase()
                        t == "CREDIT" || t == "INCOME" || t == "REWARD"
                    }.sumOf { it.amount }

                    val label = dateFormat.format(java.util.Date(startTime))
                    list.add(ChartBarData(label, debits, credits))
                }
                list
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header with Chevron >
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Track Your Money",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time-Period Selector Segmented Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF101524))
                .border(1.dp, Color(0xFF1E283D), RoundedCornerShape(14.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TrackPeriod.values().forEach { period ->
                val isSelected = period == selectedPeriod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF00A3FF), Color(0xFF0066FF))
                                    )
                                )
                            } else Modifier
                        )
                        .clickable {
                            selectedPeriod = period
                            selectedBarIndex = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 11.5.sp,
                            color = if (isSelected) Color.White else Color(0xFF869AB8)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Financial Overview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, Color(0xFF1C2235), RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101524))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {

                if (periodTransactions.isEmpty()) {
                    // Clean Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00A3FF).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF00A3FF).copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = Color(0xFF00A3FF),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "No Transactions For ${selectedPeriod.label}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Record a debit or credit transaction to view real-time statistics & trends.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF869AB8),
                                fontSize = 11.5.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onAddTransactionClick,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3FF)),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("Add Transaction", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            }
                        }
                    }
                } else {
                    // Key Financial Indicators Grid (3x2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrackStatCard(
                            title = "Money Spent",
                            value = "₹" + String.format(locale, "%,.0f", totalSpent),
                            isNegative = true,
                            modifier = Modifier.weight(1f)
                        )
                        TrackStatCard(
                            title = "Money Received",
                            value = "₹" + String.format(locale, "%,.0f", totalReceived),
                            isPositive = true,
                            modifier = Modifier.weight(1f)
                        )
                        TrackStatCard(
                            title = "Net Cash Flow",
                            value = (if (netCashFlow >= 0) "+" else "") + "₹" + String.format(locale, "%,.0f", netCashFlow),
                            isPositive = netCashFlow >= 0,
                            isNegative = netCashFlow < 0,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrackStatCard(
                            title = "Transactions",
                            value = "$transactionCount",
                            subtitle = "logged",
                            modifier = Modifier.weight(1f)
                        )
                        TrackStatCard(
                            title = "Top Category",
                            value = topCategory,
                            modifier = Modifier.weight(1f)
                        )
                        TrackStatCard(
                            title = "Avg Daily Spend",
                            value = "₹" + String.format(locale, "%,.0f", avgDailySpending),
                            subtitle = "/ day",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Donut Chart Section
                    Text(
                        text = "Category Expenses Breakdown",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Group debit/expense transactions by category for Donut Chart
                    val categoryBreakdown = remember(periodTransactions) {
                        val debits = periodTransactions.filter { txn ->
                            val t = txn.type.uppercase()
                            t == "DEBIT" || t == "EXPENSE" || t == "BILL_PENDING" || t == "PAYMENT"
                        }
                        debits.groupBy { it.category.ifBlank { "General" } }
                            .map { (category, list) ->
                                CategoryVisuals.getCategoryVisual(category) to list.sumOf { it.amount }
                            }
                            .sortedByDescending { it.second }
                    }

                    if (categoryBreakdown.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expense data for Donut Chart in this period",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF869AB8)
                            )
                        }
                    } else {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            factory = { context ->
                                PieChart(context).apply {
                                    description.isEnabled = false
                                    isDrawHoleEnabled = true
                                    setHoleColor(AndroidColor.TRANSPARENT)
                                    setTransparentCircleColor(AndroidColor.TRANSPARENT)
                                    holeRadius = 58f
                                    transparentCircleRadius = 63f
                                    setDrawCenterText(true)
                                    centerText = "Expenses"
                                    setCenterTextSize(14f)
                                    setCenterTextColor(AndroidColor.WHITE)
                                    rotationAngle = 0f
                                    isRotationEnabled = true
                                    legend.isEnabled = true
                                    legend.textColor = AndroidColor.WHITE
                                    legend.isWordWrapEnabled = true
                                    setEntryLabelColor(AndroidColor.WHITE)
                                    setEntryLabelTextSize(10f)
                                }
                            },
                            update = { pieChart ->
                                val entries = categoryBreakdown.take(6).map { (visual, amount) ->
                                    PieEntry(amount.toFloat(), visual.title)
                                }
                                val colors = categoryBreakdown.take(6).map { (visual, _) ->
                                    visual.color.toArgb()
                                }
                                val dataSet = PieDataSet(entries, "").apply {
                                    sliceSpace = 2f
                                    selectionShift = 6f
                                    this.colors = colors
                                    valueTextColor = AndroidColor.WHITE
                                    valueTextSize = 10f
                                }
                                pieChart.data = PieData(dataSet)
                                pieChart.invalidate()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    isPositive: Boolean = false,
    isNegative: Boolean = false,
    modifier: Modifier = Modifier
) {
    val valColor = when {
        isPositive -> Color(0xFF00E471)
        isNegative -> Color(0xFFFF3366)
        else -> Color.White
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0B0E18))
            .border(1.dp, Color(0xFF1B2338), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF869AB8)
            ),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.5.sp,
                color = valColor
            ),
            maxLines = 1
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    color = Color(0xFF64748B)
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ExpenseCategoryRow(
    color: Color,
    name: String,
    amount: String,
    percentage: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color Rounded Square / Pill
        Box(
            modifier = Modifier
                .size(width = 8.5.dp, height = 6.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(7.dp))
        // Category Name
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.88f)
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        // Amount
        Text(
            text = amount,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        // Percentage
        Text(
            text = percentage,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.55f)
            ),
            modifier = Modifier.width(28.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

/**
 * 5. Financial Tools & Calculators 8-Item Grid Matching Reference Image
 */
@Composable
fun HomeFinancialToolsSection(
    onSeeAllClick: () -> Unit = {},
    onToolClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Financial Tools & Calculators",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSeeAllClick() },
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                )
                Text("→", fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 1 (4 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            // 1. Percentage Calculator
            CalculatorGridItem(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF2E174D))
                            .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.45f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC084FC))
                    }
                },
                title = "Percentage\nCalculator",
                onClick = { onToolClick("PERCENTAGE") }
            )

            // 2. R&E Calculator with diagonal opposing arrows
            CalculatorGridItem(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF0C2B20))
                            .border(1.dp, Color(0xFF059669).copy(alpha = 0.45f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        DiagonalOpposingArrowsIcon(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF34D399)
                        )
                    }
                },
                title = "R&E\nCalculator",
                onClick = { onToolClick("EQUIVALENCE") }
            )

            // 3. MDR Calculator
            CalculatorGridItem(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF0B293F))
                            .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.45f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                title = "MDR\nCalculator",
                onClick = { onToolClick("MDR") }
            )

            // 4. Loan EMI Calculator
            CalculatorGridItem(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF3B1527))
                            .border(1.dp, Color(0xFFBE123C).copy(alpha = 0.45f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color(0xFFFB7185),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                title = "Loan EMI\nCalculator",
                onClick = { onToolClick("EMI") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2 (4 items)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            // 5. Profit & Loss Calculator
            CalculatorGridItem(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF351F0D))
                            .border(1.dp, Color(0xFFD97706).copy(alpha = 0.45f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                title = "Profit & Loss\nCalculator",
                onClick = { onToolClick("PROFIT_LOSS") }
            )

            // 6. Compound Calculator
            CalculatorGridItem(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF0A2B35))
                            .border(1.dp, Color(0xFF0891B2).copy(alpha = 0.45f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = Color(0xFF22D3EE),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                title = "Compound\nCalculator",
                onClick = { onToolClick("COMPOUND_INTEREST") }
            )

            // 7. SIP Calculator
            CalculatorGridItem(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF342B0D))
                            .border(1.dp, Color(0xFFCA8A04).copy(alpha = 0.45f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = Color(0xFFFACC15),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                title = "SIP\nCalculator",
                onClick = { onToolClick("SIP") }
            )

            // 8. Equivalence Calculator
            CalculatorGridItem(
                modifier = Modifier.weight(1f),
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF2C1645))
                            .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.45f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Balance,
                            contentDescription = null,
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                title = "Equivalence\nCalculator",
                onClick = { onToolClick("EQUIVALENCE") }
            )
        }
    }
}

@Composable
private fun CalculatorGridItem(
    iconContent: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF1C2235), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101524))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            iconContent()
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 13.sp
                ),
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

