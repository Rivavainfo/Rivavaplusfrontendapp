package com.rivavafi.universal.ui.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeCardCarouselSection(
    seatsRemaining: Int = 93,
    isPortfolioUnlocked: Boolean = false,
    onNavigateToPortfolio: () -> Unit = {},
    onBookSessionClick: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToTools: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalPageCount = 4
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { totalPageCount })
    val coroutineScope = rememberCoroutineScope()
    var isUserInteracting by remember { mutableStateOf(false) }
    var isLifecycleActive by remember { mutableStateOf(true) }

    // Lifecycle observer to pause timer when app is in background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isLifecycleActive = event.targetState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Fixed 6-second auto-rotation timer with smooth slide transition
    LaunchedEffect(pagerState.currentPage, isUserInteracting, isLifecycleActive) {
        if (!isUserInteracting && isLifecycleActive) {
            delay(6000)
            val nextPage = (pagerState.currentPage + 1) % totalPageCount
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(durationMillis = 450)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isUserInteracting = true },
                    onDragEnd = { isUserInteracting = false },
                    onDragCancel = { isUserInteracting = false },
                    onDrag = { _, _ -> }
                )
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(218.dp)
        ) { page ->
            when (page) {
                0 -> {
                    // CARD SET 1 — EXISTING HERO BANNER
                    HomeEliteHeroBanner(
                        seatsRemaining = seatsRemaining,
                        onJoinClick = onBookSessionClick
                    )
                }
                1 -> {
                    // CARD SET 2 — RIVAVA ELITE
                    CarouselCardSetContainer(
                        title = "RIVAVA ELITE & RESEARCH",
                        badgeText = "PREMIUM SERVICES",
                        badgeColor = Color(0xFFFFD700),
                        gradientColors = listOf(Color(0xFF16130B), Color(0xFF0F0C07), Color(0xFF080603)),
                        cardA = CarouselCardItem(
                            title = "Rivava Elite",
                            subtitle = "Explore Premium Research",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            accentColor = Color(0xFFFFD700),
                            actionText = "Open Portfolio",
                            onClick = onNavigateToPortfolio
                        ),
                        cardB = CarouselCardItem(
                            title = "1-on-1 Advisor Sessions",
                            subtitle = "Connect With Your Advisor",
                            icon = Icons.AutoMirrored.Filled.Chat,
                            accentColor = Color(0xFF00C6FF),
                            actionText = "Book Session",
                            onClick = onBookSessionClick
                        )
                    )
                }
                2 -> {
                    // CARD SET 3 — FINANCIAL INSIGHTS
                    CarouselCardSetContainer(
                        title = "FINANCIAL INSIGHTS & ACTIVITY",
                        badgeText = "ANALYTICS & HISTORY",
                        badgeColor = Color(0xFF00E471),
                        gradientColors = listOf(Color(0xFF0C1914), Color(0xFF08120E), Color(0xFF050A08)),
                        cardA = CarouselCardItem(
                            title = "Track Your Spending",
                            subtitle = "Understand Where Your Money Goes",
                            icon = Icons.Default.BarChart,
                            accentColor = Color(0xFF00E471),
                            actionText = "Open Analytics",
                            onClick = onNavigateToAnalytics
                        ),
                        cardB = CarouselCardItem(
                            title = "Transaction History",
                            subtitle = "Explore Your Daily Money Activity",
                            icon = Icons.Default.ReceiptLong,
                            accentColor = Color(0xFF00A3FF),
                            actionText = "Open Transactions",
                            onClick = onNavigateToTransactions
                        )
                    )
                }
                3 -> {
                    // CARD SET 4 — FINANCIAL TOOLS & RESEARCH
                    CarouselCardSetContainer(
                        title = "FINANCIAL TOOLS & RESEARCH",
                        badgeText = "SMART PLANNING",
                        badgeColor = Color(0xFFA855F7),
                        gradientColors = listOf(Color(0xFF191028), Color(0xFF100B1A), Color(0xFF08050E)),
                        cardA = CarouselCardItem(
                            title = "Financial Calculators",
                            subtitle = "Plan and Calculate Smarter",
                            icon = Icons.Default.Calculate,
                            accentColor = Color(0xFFA855F7),
                            actionText = "Open Tools",
                            onClick = onNavigateToTools
                        ),
                        cardB = CarouselCardItem(
                            title = "Research Reports",
                            subtitle = "Explore Rivava Research",
                            icon = Icons.Default.Description,
                            accentColor = Color(0xFFFF3366),
                            actionText = "Open Reports",
                            onClick = onNavigateToPortfolio
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Small Carousel Page Indicator Dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalPageCount) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(6.dp)
                        .width(if (isSelected) 18.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFF00A3FF) else Color.White.copy(alpha = 0.25f)
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index, animationSpec = tween(350))
                            }
                        }
                )
            }
        }
    }
}

data class CarouselCardItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val actionText: String,
    val onClick: () -> Unit
)

@Composable
private fun CarouselCardSetContainer(
    title: String,
    badgeText: String,
    badgeColor: Color,
    gradientColors: List<Color>,
    cardA: CarouselCardItem,
    cardB: CarouselCardItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(218.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFF1E283D), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color.White,
                            letterSpacing = 1.0.sp
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = badgeColor
                            )
                        )
                    }
                }

                // Two Side-by-Side Action Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CarouselSubCard(
                        item = cardA,
                        modifier = Modifier.weight(1f)
                    )

                    CarouselSubCard(
                        item = cardB,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CarouselSubCard(
    item: CarouselCardItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF1F293D), RoundedCornerShape(16.dp))
            .clickable(onClick = item.onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101524))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(item.accentColor.copy(alpha = 0.16f))
                        .border(1.dp, item.accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = item.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Color.White
                    ),
                    maxLines = 2,
                    lineHeight = 15.sp
                )
            }

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.5.sp,
                    color = Color(0xFF869AB8),
                    lineHeight = 13.5.sp
                ),
                maxLines = 2
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.actionText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.5.sp,
                        color = item.accentColor
                    )
                )
                Text(
                    text = "→",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = item.accentColor
                )
            }
        }
    }
}
