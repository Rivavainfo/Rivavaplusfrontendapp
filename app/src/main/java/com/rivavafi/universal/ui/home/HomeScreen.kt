package com.rivavafi.universal.ui.home

import java.util.Calendar
import java.util.Locale

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.rivavafi.universal.R
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivavafi.universal.data.local.TransactionEntity
import com.rivavafi.universal.domain.usecase.FinancialSummaryState
import com.rivavafi.universal.ui.add.AddTransactionBottomSheet
import com.rivavafi.universal.ui.theme.CategoryVisuals
import com.rivavafi.universal.ui.theme.bounceClick
import com.rivavafi.universal.ui.theme.glassMorphism
import com.rivavafi.universal.ui.theme.glowEffect
import com.rivavafi.universal.ui.theme.RivavaCyan
import com.rivavafi.universal.ui.theme.RivavaPink
import com.rivavafi.universal.ui.theme.RivavaLime
import com.rivavafi.universal.ui.theme.DarkCardBg
import com.rivavafi.universal.ui.theme.DarkCardBorder
import com.rivavafi.universal.ui.theme.OnDarkSurface
import com.rivavafi.universal.ui.theme.OnDarkSurfaceVariant
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Star
import com.rivavafi.universal.ui.components.RivavaGlowingLogo
import com.rivavafi.universal.ui.theme.RivavaBrandGradient
import com.rivavafi.universal.ui.theme.RivavaCyanGradient
import com.rivavafi.universal.ui.theme.RivavaGoldGradient
import com.rivavafi.universal.ui.theme.DarkCardBgElevated
import com.rivavafi.universal.ui.theme.DarkCardBorderHighlight
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Lock
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.rivavafi.universal.ui.portfolio.PasswordDialog
import androidx.compose.material.icons.filled.Person

import com.rivavafi.universal.ui.components.PremiumCard
import com.rivavafi.universal.ui.components.SectionHeader
import androidx.compose.ui.layout.ContentScale

import android.Manifest
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.graphics.asImageBitmap

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.automirrored.filled.TrendingUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    profileViewModel: com.rivavafi.universal.ui.profile.ProfileViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTransactionDetail: (Long) -> Unit = {},
    onNavigateToRivavaPortfolio: () -> Unit = {},
    onNavigateToCalculators: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {}
) {
    val summary by viewModel.summary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val dailyBudget by viewModel.dailyBudget.collectAsState()
    val layoutPreset by viewModel.homeLayoutPreset.collectAsState()
    val showDetails by viewModel.showSmsDetails.collectAsState()
    val viewModelUserName by viewModel.userName.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val viewModelProfileImageUri by viewModel.profileImageUri.collectAsState()
    val eliteConfig by viewModel.eliteConfig.collectAsState()
    val eliteSubscription by viewModel.eliteSubscription.collectAsState()

    val profileState by profileViewModel.profileState.collectAsState()
    val userModel = profileState.userModel

    val context = LocalContext.current
    val cachedUser = remember { com.rivavafi.universal.utils.PrefsManager(context).getUser() }
    val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

    val userName = userModel?.name?.takeIf { it.isNotBlank() }
        ?: viewModelUserName?.takeIf { it.isNotBlank() }
        ?: cachedUser?.name?.takeIf { it.isNotBlank() }
        ?: authUser?.displayName?.takeIf { it.isNotBlank() }
    val profileImageUri = userModel?.profileImage ?: viewModelProfileImageUri ?: cachedUser?.photo ?: authUser?.photoUrl?.toString()
    var showAddSheet by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showVideoCallDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", android.content.Context.MODE_PRIVATE)

    // Removed showEliteBottomSheet logic as we now navigate directly to EliteLandingActivity
    val isPremiumPref = prefs.getBoolean("isPremium", false)

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {},
        containerColor = Color(0xFF06070B)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Bar: Glowing Logo on Left, Search + Bell with badge + Profile Avatar on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Logo + Branding
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RivavaGlowingLogo(size = 38.dp)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "RIVAVA",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 2.2.sp,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "+",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF00A3FF)
                                        ),
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                                Text(
                                    text = "FINANCIAL INTELLIGENCE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.5.sp,
                                        letterSpacing = 1.2.sp,
                                        color = Color.White.copy(alpha = 0.55f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        // Right: Search Button + Notification Bell + Profile Avatar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Circular Search Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF141724))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                    .clickable { onNavigateToTransactions() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Circular Notification Bell Button with Red Badge Dot
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF141724))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                    .clickable {
                                        Toast.makeText(context, "No new notifications", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    // Red badge dot
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .offset(x = 1.dp, y = (-2).dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF3B30))
                                    )
                                }
                            }

                            // Profile Avatar with Neon Ring
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(DarkCardBgElevated)
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.sweepGradient(listOf(RivavaCyan, RivavaPink, RivavaLime, RivavaCyan)),
                                        shape = CircleShape
                                    )
                                    .clickable { onNavigateToProfile() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileImageUri != null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        val initial = if (!userName.isNullOrEmpty()) userName.first().toString().uppercase() else ""
                                        if (initial.isNotEmpty()) {
                                            Text(
                                                text = initial,
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Profile Avatar",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        val decodedBitmap = remember(profileImageUri) {
                                            if (profileImageUri.startsWith("data:image")) {
                                                try {
                                                    val base64String = profileImageUri.substringAfter("base64,")
                                                    val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                                                    android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            } else null
                                        }

                                        if (decodedBitmap != null) {
                                            Image(
                                                bitmap = decodedBitmap.asImageBitmap(),
                                                contentDescription = "Profile Avatar",
                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            coil.compose.AsyncImage(
                                                model = profileImageUri,
                                                contentDescription = "Profile Avatar",
                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.rivava_logo),
                                        contentDescription = "Profile Avatar",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Hero Section: "Welcome to Rivava+" with Neon 3D Cubes Graphic
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome to",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Rivava",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 34.sp,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        color = Color(0xFF00A3FF),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 34.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Smart financial tracking, analytics\n& portfolio intelligence.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp
                                )
                            )
                        }

                        // 3D Isometric Neon Glass Cubes Graphic
                        IsometricNeonCubesGraphic(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(width = 145.dp, height = 115.dp)
                        )
                    }
                }
            }

            // Ultra-Luxury Obsidian Wealth Master Card
            item {
                var selectedTimeframe by remember { mutableStateOf("1M") }
                val totalEstimatedWealth = summary.totalCredit + summary.netSavings
                val wealthFormatted = if (totalEstimatedWealth > 0) {
                    "₹" + String.format(Locale.getDefault(), "%,.0f", totalEstimatedWealth)
                } else {
                    "₹2,48,500"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF00A3FF).copy(alpha = 0.65f),
                                    Color(0xFFA855F7).copy(alpha = 0.35f),
                                    Color(0xFF00E471).copy(alpha = 0.25f)
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .clickable { onNavigateToRivavaPortfolio() },
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF131627),
                                        Color(0xFF0C0E1A),
                                        Color(0xFF070810)
                                    )
                                )
                            )
                    ) {
                        // Ambient cyan radial glow in top right
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 40.dp, y = (-40).dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFF00A3FF).copy(alpha = 0.22f), Color.Transparent)
                                    ),
                                    CircleShape
                                )
                        )

                        Column(modifier = Modifier.padding(20.dp)) {
                            // Top Row: Sparkle Star + RIVAVA WEALTH CARD + LIVE + Timeframe Pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFFFFD700), Color(0xFFB8860B))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✦", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "RIVAVA WEALTH CARD",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.0.sp,
                                            fontSize = 9.5.sp,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    )
                                    Surface(
                                        color = Color(0xFF0A2B1D),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E471).copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF00E471))
                                            )
                                            Text(
                                                text = "LIVE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF00E471),
                                                    fontSize = 8.5.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                // Timeframe filter pills: 1D, 1W, 1M, 1Y, ALL
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("1D", "1W", "1M", "1Y", "ALL").forEach { tf ->
                                        val isSelected = selectedTimeframe == tf
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isSelected) Color(0xFF0E3255) else Color.Transparent
                                                )
                                                .border(
                                                    width = if (isSelected) 1.dp else 0.dp,
                                                    color = if (isSelected) Color(0xFF00A3FF).copy(alpha = 0.8f) else Color.Transparent,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable { selectedTimeframe = tf }
                                                .padding(horizontal = 5.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tf,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color(0xFF00A3FF) else Color.White.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Middle Row: Total Wealth (Left) & Glowing Neon Wave Chart with ₹2.48L Tooltip (Right)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.05f)) {
                                    Text(
                                        text = "Total Estimated Wealth",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.5.sp,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = wealthFormatted,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            letterSpacing = (-0.5).sp,
                                            fontSize = 30.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(Color(0xFF0A2B1D))
                                            .border(1.dp, Color(0xFF00E471).copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("↑", color = Color(0xFF00E471), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "+14.8% this month",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF00E471),
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                NeonWaveChartWithTooltip(
                                    modifier = Modifier
                                        .weight(0.95f)
                                        .height(82.dp),
                                    tooltipValue = "₹2.48L"
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Sub-metrics row: Invested, Total Returns, XIRR
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Invested",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.55f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹1,86,200",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Total Returns",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.55f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹62,300",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF00E471),
                                            fontSize = 15.sp
                                        )
                                    )
                                }

                                Column {
                                    Text(
                                        text = "XIRR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.55f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "18.4%",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF00E471),
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action Buttons Row: Explore Portfolio → & + Add Account
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onNavigateToRivavaPortfolio() },
                                    modifier = Modifier
                                        .weight(1.15f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF0066FF), Color(0xFF0099FF))
                                                ),
                                                shape = RoundedCornerShape(14.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BarChart,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color.White
                                            )
                                            Text(
                                                text = "Explore Portfolio",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            Text("→", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }

                                Surface(
                                    onClick = { showAddSheet = true },
                                    modifier = Modifier
                                        .weight(0.85f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddCircleOutline,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color.White
                                            )
                                            Text(
                                                text = "Add Account",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 13.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5 Quick-Action Icon Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    QuickActionItem(
                        title = "Portfolio\nTracker",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = Color(0xFFA855F7),
                        bgColor = Color(0xFF1E1333),
                        onClick = { onNavigateToRivavaPortfolio() },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionItem(
                        title = "Market\nInsights",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        accentColor = Color(0xFF00E471),
                        bgColor = Color(0xFF0E251E),
                        onClick = { onNavigateToAnalytics() },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionItem(
                        title = "Financial\nCalculator",
                        icon = Icons.Default.PieChart,
                        accentColor = Color(0xFF00A3FF),
                        bgColor = Color(0xFF0E2038),
                        onClick = { onNavigateToCalculators() },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionItem(
                        title = "Research\nReports",
                        icon = Icons.Default.Description,
                        accentColor = Color(0xFFFF2A85),
                        bgColor = Color(0xFF281122),
                        onClick = { onNavigateToTransactions() },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionItem(
                        title = "Learn\n& Grow",
                        icon = Icons.Default.School,
                        accentColor = Color(0xFFFF9800),
                        bgColor = Color(0xFF2C1E0C),
                        onClick = { onNavigateToHelpCenter() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Rivava Elite Luxury Card
            item {
                val seatsRemaining = (eliteConfig.totalSeats - eliteConfig.occupiedSeats).coerceAtLeast(0)
                val isFull = seatsRemaining == 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .border(
                            width = 1.3.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFFD700).copy(alpha = 0.85f),
                                    Color(0xFFB8860B).copy(alpha = 0.45f),
                                    Color(0xFFFFD700).copy(alpha = 0.9f)
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .clickable {
                            if (eliteSubscription.isElite) {
                                context.startActivity(Intent(context, com.rivavafi.universal.ui.elite.EliteDashboardActivity::class.java))
                            } else {
                                val intent = Intent(context, com.rivavafi.universal.ui.elite.EliteLandingActivity::class.java)
                                val options = android.app.ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out)
                                context.startActivity(intent, options.toBundle())
                            }
                        },
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF1E170A),
                                        Color(0xFF100D06),
                                        Color(0xFF080703)
                                    )
                                )
                            )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Header Row: Crown + RIVAVA ELITE + 100/100
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("👑", fontSize = 16.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "RIVAVA ",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.6.sp,
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "ELITE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.6.sp,
                                                fontSize = 11.sp,
                                                color = Color(0xFFFFD700)
                                            )
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xFF221A08))
                                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                                        .padding(horizontal = 9.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isFull) "FULL" else "${if (seatsRemaining > 0) seatsRemaining else 100}/100",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD700),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Middle Row: Title (Left) and Golden Crown Graphic (Right)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Private Wealth Guidance\nfor Serious Investors",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        lineHeight = 23.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                GoldenCrownGraphic(
                                    modifier = Modifier
                                        .size(width = 95.dp, height = 75.dp)
                                        .padding(start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3 Feature Chips Row spanning full width
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Video Consults
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VideoCall,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = "Video\nConsults",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            lineHeight = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                // 2. Priority Support
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SupportAgent,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = "Priority\nSupport",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            lineHeight = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                // 3. Elite Research Access
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Text(
                                        text = "Elite\nResearch Access",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            lineHeight = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Bottom Row: Price (Left) + Gold Unlock Button (Right)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "₹399",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 22.sp,
                                                color = Color(0xFFFFD700)
                                            )
                                        )
                                        Text(
                                            text = " / month",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White.copy(alpha = 0.65f),
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "+ 1 Free Live Session",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E471),
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (eliteSubscription.isElite) {
                                            context.startActivity(Intent(context, com.rivavafi.universal.ui.elite.EliteDashboardActivity::class.java))
                                        } else {
                                            val intent = Intent(context, com.rivavafi.universal.ui.elite.EliteLandingActivity::class.java)
                                            val options = android.app.ActivityOptions.makeCustomAnimation(context, android.R.anim.fade_in, android.R.anim.fade_out)
                                            context.startActivity(intent, options.toBundle())
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.Black
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                                ),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .padding(horizontal = 22.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = if (eliteSubscription.isElite) "Open Dashboard" else "Unlock Elite",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.Black,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            Text("→", fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Financial Tools & Calculators Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onNavigateToCalculators() }
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF00A3FF).copy(alpha = 0.6f), Color(0xFF00E471).copy(alpha = 0.35f))
                            ),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF141728), Color(0xFF0D0F1C))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF0066FF))
                                        .border(1.dp, Color(0xFF00A3FF).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Financial Tools & Calculators",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "SIP • EMI • P&L • MDR • Compounding",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.55f)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.clickable { onNavigateToCalculators() }
                            ) {
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF00A3FF)
                                )
                                Text("→", color = Color(0xFF00A3FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Talk to Rivava",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Brush.horizontalGradient(listOf(DarkCardBorderHighlight, Color.Transparent)))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Call Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.95f)
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(RivavaCyan.copy(alpha = 0.5f), DarkCardBorder)
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+918881176909"))
                                context.startActivity(intent)
                            },
                        colors = CardDefaults.cardColors(containerColor = DarkCardBgElevated),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(RivavaCyan.copy(alpha = 0.16f), CircleShape)
                                        .border(1.dp, RivavaCyan.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(24.dp), tint = RivavaCyan)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Call", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("Direct Line", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = OnDarkSurfaceVariant)
                            }
                        }
                    }

                    // Video Call Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.95f)
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(RivavaPink.copy(alpha = 0.5f), DarkCardBorder)
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .clickable { showVideoCallDialog = true },
                        colors = CardDefaults.cardColors(containerColor = DarkCardBgElevated),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(RivavaPink.copy(alpha = 0.16f), CircleShape)
                                        .border(1.dp, RivavaPink.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(26.dp), tint = RivavaPink)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Video Call", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("1-on-1 VIP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = OnDarkSurfaceVariant)
                            }
                        }
                    }

                    // Chat Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.95f)
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(RivavaLime.copy(alpha = 0.5f), DarkCardBorder)
                                ),
                                RoundedCornerShape(22.dp)
                            )
                            .clickable { showChatDialog = true },
                        colors = CardDefaults.cardColors(containerColor = DarkCardBgElevated),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(RivavaLime.copy(alpha = 0.16f), CircleShape)
                                        .border(1.dp, RivavaLime.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(24.dp), tint = RivavaLime)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Chat", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text("Instant AI", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = OnDarkSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }



            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dashboard Overview",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .bounceClick {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showAddSheet = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                DashboardOverviewBento(summary = summary)
            }

            when (layoutPreset) {
                "Minimal" -> {
                    item {
                        RealBalanceCard(transactions = transactions)
                    }
                }
                "Analytics" -> {
                    item {
                        SpendingSummaryCards(transactions = transactions)
                    }
                    item {
                        RealBalanceCard(transactions = transactions)
                    }
                }
                "Daily Tracker" -> {
                    item {
                        DailyBudgetCard(
                            transactions = transactions,
                            dailyBudget = dailyBudget,
                            onBudgetUpdate = { newBudget -> viewModel.updateDailyBudget(newBudget) }
                        )
                    }
                    item {
                        SpendingSummaryCards(transactions = transactions)
                    }
                }
                "Subscription View" -> {
                    item {
                        com.rivavafi.universal.ui.analytics.SubscriptionTrackerCard(transactions = transactions)
                    }
                }
                else -> {
                    item {
                        DailyBudgetCard(
                            transactions = transactions,
                            dailyBudget = dailyBudget,
                            onBudgetUpdate = { newBudget -> viewModel.updateDailyBudget(newBudget) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(transactions.take(5), key = { it.id }) { transaction ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(500)) + expandVertically(
                            animationSpec = spring(
                                dampingRatio = 0.5f,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        exit = fadeOut(animationSpec = tween(500)) + shrinkVertically(
                            animationSpec = spring(
                                dampingRatio = 0.5f,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        TransactionItem(transaction, showDetails = showDetails, onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToTransactionDetail(transaction.id)
                        })
                    }
                }
            }
        }

        if (showVideoCallDialog) {
            var name by remember { mutableStateOf("") }
            var preferredTime by remember { mutableStateOf("") }
            var contactInfo by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showVideoCallDialog = false },
                title = { Text("Schedule a Video Call") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Fill out the details below to book a consultation via WhatsApp.")
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        OutlinedTextField(
                            value = preferredTime,
                            onValueChange = { preferredTime = it },
                            label = { Text("Preferred Time (e.g. 2:00 PM)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        OutlinedTextField(
                            value = contactInfo,
                            onValueChange = { contactInfo = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (name.isNotBlank() && preferredTime.isNotBlank() && contactInfo.isNotBlank()) {
                            showVideoCallDialog = false
                            val message = "Hi, I want to schedule a video call.\nName: $name\nPreferred Time: $preferredTime\nContact: $contactInfo"
                            val encodedMessage = java.net.URLEncoder.encode(message, "UTF-8")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918881176909?text=$encodedMessage"))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp not found", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVideoCallDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }


        if (showChatDialog) {
            var message by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showChatDialog = false },
                title = { Text("Chat with Rivava") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Send us a message and our support team will get back to you.")
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Your Message") },
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showChatDialog = false
                        Toast.makeText(context, "Message sent!", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Send")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChatDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAddSheet) {
            val dynamicCategories by viewModel.categories.collectAsState()
            AddTransactionBottomSheet(
                categories = dynamicCategories.map { it.name }.ifEmpty { listOf("General") },
                onDismiss = { showAddSheet = false },
                onSave = { title, amount, type, category, subcategory, date ->
                    viewModel.addTransaction(title, amount, type, category, subcategory, date)
                    showAddSheet = false
                },
                onAddCategory = { newCategoryName, type ->
                    viewModel.addCategory(newCategoryName, type)
                }
            )
        }

        if (showPasswordDialog) {
            PasswordDialog(
                onDismiss = { showPasswordDialog = false },
                onUnlock = { password ->
                    if (password.trim() == userName?.trim()) {
                        showPasswordDialog = false
                        viewModel.setPremiumUser(true)
                        Toast.makeText(context, "Premium Unlocked!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun SpendingSummaryCards(transactions: List<TransactionEntity>) {
    val weekStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val monthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    var weeklySpending = 0.0
    var monthlySpending = 0.0

    transactions.forEach {
        if (it.type == "DEBIT" || it.type == "EXPENSE" || it.type == "BILL_PENDING") {
            if (it.date >= weekStart.toLong()) weeklySpending += it.amount
            if (it.date >= monthStart.toLong()) monthlySpending += it.amount
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SpendingCard("This Week", weeklySpending, Modifier.weight(1f))
        SpendingCard("This Month", monthlySpending, Modifier.weight(1f))
    }
}

@Composable
fun SpendingCard(title: String, amount: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.glassMorphism(cornerRadius = 24f, alpha = 0.15f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹" + String.format(java.util.Locale.getDefault(), "%.0f", amount),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}

@Composable
fun DailyBudgetCard(
    transactions: List<TransactionEntity>,
    dailyBudget: Double,
    onBudgetUpdate: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf(dailyBudget.toString()) }

    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    var spentToday = 0.0
    transactions.forEach {
        if ((it.type == "DEBIT" || it.type == "EXPENSE" || it.type == "BILL_PENDING") && it.date >= todayStart) {
            spentToday += it.amount
        }
    }

    val remaining = (dailyBudget - spentToday).coerceAtLeast(0.0)
    val progress = if (dailyBudget > 0) (spentToday / dailyBudget).toFloat().coerceIn(0f, 1f) else 1f

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Daily Budget") },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    label = { Text("Budget Amount") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newBudget = budgetInput.toDoubleOrNull()
                        if (newBudget != null && newBudget >= 0) {
                            onBudgetUpdate(newBudget)
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .glassMorphism(cornerRadius = 28f, alpha = 0.15f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daily Budget", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${String.format(Locale.getDefault(), "%.0f", dailyBudget)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            budgetInput = dailyBudget.toString()
                            showEditDialog = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "₹${String.format(Locale.getDefault(), "%.0f", remaining)}",
                color = if (remaining > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text("Remaining Today", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Spent: ₹${String.format(Locale.getDefault(), "%.0f", spentToday)}",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to add your first credit or debit",
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun DashboardOverviewBento(summary: FinancialSummaryState) {
    val netWorth = summary.totalCredit + summary.netSavings
    val savings = summary.netSavings
    val investments = summary.totalCredit

    val netWorthChange = if (netWorth > 0) "+4.2% mo" else "0.0%"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Net Worth Card (full width luxury obsidian card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(RivavaCyan.copy(alpha = 0.8f), RivavaLime.copy(alpha = 0.6f))
                    ),
                    shape = RoundedCornerShape(26.dp)
                ),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF161828), Color(0xFF0E0F1A))
                        )
                    )
            ) {
                // Subtle ambient glow
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 30.dp, y = (-20).dp)
                        .background(
                            Brush.radialGradient(
                                listOf(RivavaCyan.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )

                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AGGREGATE NET WORTH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = RivavaCyan
                            )
                        )
                        Surface(
                            color = (if (netWorth >= 0) RivavaLime else RivavaPink).copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                (if (netWorth >= 0) RivavaLime else RivavaPink).copy(alpha = 0.35f)
                            )
                        ) {
                            Text(
                                text = netWorthChange,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (netWorth >= 0) RivavaLime else RivavaPink
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", netWorth)}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Liquid cash + market assets automatically synced",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnDarkSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Row for Savings & Investments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Savings Card (half width)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(RivavaPink.copy(alpha = 0.4f), DarkCardBorder)
                        ),
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBgElevated),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(RivavaPink.copy(alpha = 0.16f), CircleShape)
                            .border(1.dp, RivavaPink.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Savings",
                            tint = RivavaPink,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Total Savings",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = OnDarkSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", savings)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }
            }

            // Investments Card (half width)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(RivavaCyan.copy(alpha = 0.4f), DarkCardBorder)
                        ),
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBgElevated),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(RivavaCyan.copy(alpha = 0.16f), CircleShape)
                            .border(1.dp, RivavaCyan.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Investments",
                            tint = RivavaCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Investments",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = OnDarkSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", investments)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RealBalanceCard(transactions: List<TransactionEntity>) {
    val bankBalances = remember(transactions) {
        val balances = mutableMapOf<String, Double>()
        // transactions are already ordered by timestamp desc, so first found is latest
        transactions.forEach { t ->
            if (t.bankName != null && t.availableBalance != null) {
                if (!balances.containsKey(t.bankName)) {
                    balances[t.bankName] = t.availableBalance
                }
            }
        }
        balances
    }

    if (bankBalances.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .glassMorphism(cornerRadius = 28f, alpha = 0.2f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Estimated Bank Balance",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                bankBalances.forEach { (bank, balance) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = bank,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "₹" + String.format(java.util.Locale.getDefault(), "%.2f", balance),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionEntity, showDetails: Boolean = true, onClick: () -> Unit) {
    val isCredit = transaction.type == "CREDIT" || transaction.type == "INCOME" || transaction.type == "REWARD"
    val amountColor = if (isCredit) RivavaLime else RivavaPink

    val categoryVisual = CategoryVisuals.getCategoryVisual(transaction.category)
    val subCategoryVisual = transaction.subcategory?.let { CategoryVisuals.getSubcategoryVisual(it) }

    val visualToUse = subCategoryVisual ?: categoryVisual

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .glassMorphism(cornerRadius = 24f, alpha = 0.15f)
            .bounceClick { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(visualToUse.color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = visualToUse.icon,
                contentDescription = null,
                tint = visualToUse.color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (showDetails) transaction.merchantName else "Hidden",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (showDetails) visualToUse.title else "Hidden",
                    style = MaterialTheme.typography.bodyMedium,
                    color = visualToUse.color,
                    modifier = Modifier
                        .background(visualToUse.color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                val dateString = if (showDetails) formatter.format(java.util.Date(transaction.date)) else "****"
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showDetails && !transaction.bankName.isNullOrBlank()) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = transaction.bankName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Text(
            text = "${if (isCredit) "+" else "-"}₹${String.format(java.util.Locale.getDefault(), "%.0f", transaction.amount)}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = amountColor
        )
    }
}
