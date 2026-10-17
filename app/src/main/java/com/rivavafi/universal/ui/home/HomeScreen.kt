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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Bolt
import com.rivavafi.universal.ui.theme.NyseGold
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
import androidx.compose.material.icons.filled.ChevronRight
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    profileViewModel: com.rivavafi.universal.ui.profile.ProfileViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTransactionDetail: (Long) -> Unit = {},
    onNavigateToRivavaPortfolio: () -> Unit = {},
    onNavigateToCalculators: (String?) -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()
    val showDetails by viewModel.showSmsDetails.collectAsState()
    val viewModelUserName by viewModel.userName.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val viewModelProfileImageUri by viewModel.profileImageUri.collectAsState()
    val eliteConfig by viewModel.eliteConfig.collectAsState()

    val profileState by profileViewModel.profileState.collectAsState()
    val userModel = profileState.userModel

    val context = LocalContext.current
    val cachedUser = remember { com.rivavafi.universal.utils.PrefsManager(context).getUser() }
    val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

    val userName = userModel?.name?.takeIf { it.isNotBlank() }
        ?: viewModelUserName?.takeIf { it.isNotBlank() }
        ?: cachedUser?.name?.takeIf { it.isNotBlank() }
        ?: authUser?.displayName?.takeIf { it.isNotBlank() }
    val userPhone = userModel?.phone?.takeIf { it.isNotBlank() }
        ?: userModel?.phoneno?.takeIf { it.isNotBlank() }
        ?: authUser?.phoneNumber ?: ""
    val profileImageUri = userModel?.profileImage ?: viewModelProfileImageUri ?: cachedUser?.photo ?: authUser?.photoUrl?.toString()
    var showAddSheet by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showScanSmsDialog by remember { mutableStateOf(false) }

    val hasSmsPermission = remember {
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setSmsTrackingEnabled(true)
            onNavigateToAnalytics()
        } else {
            Toast.makeText(context, "SMS permission is required to auto-track transactions.", Toast.LENGTH_SHORT).show()
        }
    }

    val handleScanSmsAction = {
        if (hasSmsPermission) {
            viewModel.setSmsTrackingEnabled(true)
            onNavigateToAnalytics()
        } else {
            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    // Automatically check on Home screen load if SMS is untracked/unpermissioned and pop up prompt
    LaunchedEffect(Unit) {
        val trackingEnabledPref = context.getSharedPreferences("user_preferences", android.content.Context.MODE_PRIVATE)
            .getBoolean("sms_tracking_enabled", true)
        if (!hasSmsPermission || !trackingEnabledPref) {
            showScanSmsDialog = true
        }
    }

    val haptic = LocalHapticFeedback.current
    val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", android.content.Context.MODE_PRIVATE)

    val isPremiumPref = prefs.getBoolean("isPremium", false)
    val isPortfolioUnlocked = isPremiumUser || isPremiumPref || prefs.getBoolean("portfolio_unlocked", false)

    // Direct WhatsApp Advisor Redirect Helper (No popup)
    val onOpenWhatsAppAdvisor = { customMsg: String ->
        val msgToSend = customMsg.ifBlank {
            """
                Hello Rivava Team,

                I would like to request a 1-on-1 advisor session.

                Username: ${userName ?: "User"}
                Email: ${authUser?.email ?: userModel?.email ?: "Not provided"}
                Phone: $userPhone

                Please confirm session availability.
            """.trimIndent()
        }
        com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppWithMessage(
            context = context,
            customMessage = msgToSend
        )
    }

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
            // 1. Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RivavaGlowingLogo(size = 38.dp)
                        Column {
                            Text(
                                text = "RIVAVA",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.0.sp,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "India's First Finance Research Hub",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    letterSpacing = 0.3.sp,
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                            if (!profileImageUri.isNullOrBlank()) {
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
                            } else {
                                val initial = if (!userName.isNullOrEmpty()) userName.first().toString().uppercase() else "A"
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 2 & 3. Task 4: Rotating 4-Set Homepage Carousel (6s Interval)
            item {
                val seatsRemaining = (eliteConfig.totalSeats - eliteConfig.occupiedSeats).coerceAtLeast(0)
                val displaySeats = if (eliteConfig.occupiedSeats > 0 && seatsRemaining > 0) seatsRemaining else 93

                RotatingHomepageCarousel(
                    displaySeats = displaySeats,
                    isPortfolioUnlocked = isPortfolioUnlocked,
                    onOpenWhatsAppAdvisor = { onOpenWhatsAppAdvisor("") },
                    onNavigateToPortfolio = { onNavigateToRivavaPortfolio() },
                    onNavigateToAnalytics = { onNavigateToAnalytics() },
                    onNavigateToTransactions = { onNavigateToTransactions() },
                    onNavigateToTools = { onNavigateToCalculators(null) }
                )
            }

            // 4. Quick Actions 4-Item Row
            item {
                HomeQuickActionsSection(
                    onAddTransactionClick = { showAddSheet = true },
                    onAnalyticsClick = { onNavigateToAnalytics() },
                    onPortfolioClick = { onNavigateToRivavaPortfolio() },
                    onToolsClick = { onNavigateToCalculators(null) }
                )
            }

            // 5. Track Your Money Donut Chart and Category Breakdown
            item {
                HomeTrackMoneySection(
                    transactions = transactions,
                    onClick = { onNavigateToAnalytics() },
                    onAddTransactionClick = { showAddSheet = true },
                    onScanSmsClick = { handleScanSmsAction() },
                    isSmsTracked = hasSmsPermission
                )
            }

            // 6. Financial Tools & Calculators 8-Grid Section
            item {
                HomeFinancialToolsSection(
                    onSeeAllClick = { onNavigateToCalculators(null) },
                    onToolClick = { toolName -> onNavigateToCalculators(toolName) }
                )
            }

            // 7. Specials Section
            item {
                HomeSpecialsSection(
                    isPremium = isPortfolioUnlocked,
                    userName = userName ?: "User",
                    userPhone = userPhone,
                    onNavigateToPortfolio = { onNavigateToRivavaPortfolio() },
                    onNavigateToAddTransaction = { showAddSheet = true },
                    onOpenWhatsAppAdvisor = { onOpenWhatsAppAdvisor("Hello Rivava Team, I want to connect with an advisor regarding special offers.") }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00A3FF).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF00A3FF).copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00A3FF)
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.clickable { onNavigateToTransactions() }
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

            if (transactions.isEmpty()) {
                item {
                    EmptyState(onAddClick = { showAddSheet = true })
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

        if (showScanSmsDialog) {
            AlertDialog(
                onDismissRequest = { showScanSmsDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00A3FF).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF00A3FF).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFF00A3FF),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "Scan Your SMS & Auto-Track",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    )
                },
                text = {
                    Text(
                        text = "Automate your daily expense tracking and cash flow analytics by enabling SMS inbox scanning. Bank messages are processed locally on device.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showScanSmsDialog = false
                            handleScanSmsAction()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Scan SMS Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showScanSmsDialog = false }) {
                        Text("Later", color = Color.White.copy(alpha = 0.6f))
                    }
                },
                containerColor = Color(0xFF101524),
                shape = RoundedCornerShape(22.dp)
            )
        }
    }
}

/**
 * Task 4: Rotating Homepage Carousel supporting 4 Card Sets with 6s Auto-Rotation.
 * Cards are visually consistent in size and styling with Set 1.
 * 1-on-1 Advisor bar is independent and placed beneath hero banner.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RotatingHomepageCarousel(
    displaySeats: Int,
    isPortfolioUnlocked: Boolean,
    onOpenWhatsAppAdvisor: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pageCount = 4
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })

    // Automatic 6-second rotation interval
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            delay(6000)
            val nextPage = (pagerState.currentPage + 1) % pageCount
            pagerState.animateScrollToPage(nextPage, animationSpec = tween(450))
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(286.dp), // Unified fixed height for zero vertical jumping
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (page) {
                    0 -> {
                        // CARD SET 1: EXISTING CARDS
                        HomeEliteHeroBanner(
                            seatsRemaining = displaySeats,
                            onJoinClick = onOpenWhatsAppAdvisor
                        )
                        HomeAdvisorMicroBanner(
                            onClick = onOpenWhatsAppAdvisor
                        )
                    }
                    1 -> {
                        // CARD SET 2: RIVAVA ELITE & SPECIAL OFFER
                        HomeEliteHeroBanner(
                            seatsRemaining = displaySeats,
                            badgeText = "RIVAVA ELITE RESEARCH",
                            headlineText = "Institutional Research\n& VIP Insights",
                            subtitleText = "Access real-time stock radar, market analytics & SEBI advisor channel.",
                            buttonText = "Explore Portfolio",
                            onJoinClick = onNavigateToPortfolio
                        )
                        HomeAdvisorMicroBanner(
                            title = "1-on-1 Advisor Sessions @ ₹96",
                            subtitle = "Special limited time offer at ₹96 only (original price ₹399). Connect now.",
                            onClick = onOpenWhatsAppAdvisor
                        )
                    }
                    2 -> {
                        // CARD SET 3: FINANCIAL INSIGHTS
                        HomeEliteHeroBanner(
                            seatsRemaining = displaySeats,
                            badgeText = "SPENDING ANALYTICS",
                            headlineText = "Track Your Spending\n& Cash Flow",
                            subtitleText = "Visualize daily debits, category distribution & month-to-date income.",
                            buttonText = "Open Analytics",
                            onJoinClick = onNavigateToAnalytics
                        )
                        HomeAdvisorMicroBanner(
                            title = "Transaction Activity Radar",
                            subtitle = "Search complete transaction history, merchant tags & live bank logs.",
                            onClick = onNavigateToTransactions
                        )
                    }
                    3 -> {
                        // CARD SET 4: FINANCIAL TOOLS & RESEARCH
                        HomeEliteHeroBanner(
                            seatsRemaining = displaySeats,
                            badgeText = "SMART CALCULATORS",
                            headlineText = "Financial Tools\n& Smart Calculators",
                            subtitleText = "Plan loans, SIP, profit & loss, and interest calculations instantly.",
                            buttonText = "Open Calculators",
                            onJoinClick = onNavigateToTools
                        )
                        HomeAdvisorMicroBanner(
                            title = "Institutional Research Reports",
                            subtitle = "Read in-depth market sector reports and institutional stock coverage.",
                            onClick = onNavigateToPortfolio
                        )
                    }
                }
            }
        }

        // Page Indicator Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (isSelected) 18.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF00A3FF) else Color(0xFF26334D))
                )
            }
        }
    }
}

/**
 * Hero Banner Component matching Set 1 visual styling across all card sets.
 */
@Composable
fun HomeEliteHeroBanner(
    seatsRemaining: Int = 93,
    badgeText: String = "RIVAVA ELITE",
    headlineText: String = "1-on-1 Wealth Guidance\nwith Experts",
    subtitleText: String = "Exclusive research, in-house AI,\npersonal sessions & wealth creation tools.",
    buttonText: String = "Join Rivava Elite",
    onJoinClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
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
                .fillMaxSize()
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
            // Advisor photo on the right
            Image(
                painter = painterResource(id = R.drawable.elite_advisor_hero),
                contentDescription = "Elite Advisor",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(235.dp)
                    .align(Alignment.CenterEnd),
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd
            )

            // Horizontal dark vignette fade
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
                // Top Row: Badge & Seats Remaining
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
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.5.sp,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }

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
                        text = headlineText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.5.sp,
                            lineHeight = 23.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                // Bottom Row: Action Button + Limited Members label
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
                                text = buttonText,
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
 * Independent 1-on-1 Sessions Advisor Micro Banner placed cleanly beneath the hero card.
 */
@Composable
fun HomeAdvisorMicroBanner(
    title: String = "1 in 1 Sessions with Our Advisor",
    subtitle: String = "Get personalised market insights,\nportfolio guidance & investment strategies.",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF1E253A), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101524))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.advisor_session_avatar),
                contentDescription = "Advisor Avatar",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun HomeSpecialsSection(
    isPremium: Boolean,
    userName: String,
    userPhone: String,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onOpenWhatsAppAdvisor: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Specials",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SpecialActionCard(
                title = "Reports",
                subtitle = "Research & VIP Insights",
                icon = Icons.Default.Description,
                accentColor = Color(0xFFA855F7),
                bgColor = Color(0xFF28153D),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPortfolio
            )

            SpecialActionCard(
                title = "Habit Formation",
                subtitle = "Track daily finances",
                icon = Icons.Default.CalendarMonth,
                accentColor = Color(0xFF00E471),
                bgColor = Color(0xFF0D2D1B),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAddTransaction
            )

            SpecialActionCard(
                title = "1-on-1 Sessions",
                subtitle = "Connect with Advisor",
                icon = Icons.Default.SupportAgent,
                accentColor = Color(0xFF00C6FF),
                bgColor = Color(0xFF0D253D),
                modifier = Modifier.weight(1f),
                onClick = {
                    if (isPremium) {
                        onOpenWhatsAppAdvisor()
                    } else {
                        Toast.makeText(context, "Rivava Elite / Premium access required for 1-on-1 sessions", Toast.LENGTH_LONG).show()
                        onNavigateToPortfolio()
                    }
                }
            )
        }
    }
}

@Composable
private fun SpecialActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF1C2235), RoundedCornerShape(18.dp))
            .bounceClick { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101524))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(bgColor)
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
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
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF869AB8)
                ),
                maxLines = 1,
                softWrap = false,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionEntity, showDetails: Boolean = true, onClick: () -> Unit) {
    val isCredit = transaction.type == "CREDIT" || transaction.type == "INCOME" || transaction.type == "REWARD"
    val amountColor = if (isCredit) Color(0xFF00E471) else Color(0xFFFF4C91)

    val categoryVisual = CategoryVisuals.getCategoryVisual(transaction.category)
    val subCategoryVisual = transaction.subcategory?.let { CategoryVisuals.getSubcategoryVisual(it) }

    val visualToUse = subCategoryVisual ?: categoryVisual

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                Color(0xFF1E283D).copy(alpha = 0.65f),
                RoundedCornerShape(20.dp)
            )
            .bounceClick { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF111524), Color(0xFF090C16))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(visualToUse.color.copy(alpha = 0.16f))
                        .border(1.dp, visualToUse.color.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visualToUse.icon,
                        contentDescription = null,
                        tint = visualToUse.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (showDetails) transaction.merchantName else "Hidden",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (showDetails) visualToUse.title else "Hidden",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = visualToUse.color
                            ),
                            modifier = Modifier
                                .background(visualToUse.color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val dateString = if (showDetails) formatter.format(java.util.Date(transaction.date)) else "****"
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${if (isCredit) "+" else "-"}₹${String.format(Locale.getDefault(), "%,.0f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = amountColor,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyState(onAddClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(
                width = 1.dp,
                color = Color(0xFF1E283D).copy(alpha = 0.7f),
                shape = RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF101524), Color(0xFF090D18))
                    )
                )
                .padding(vertical = 32.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF00A3FF).copy(alpha = 0.22f), Color.Transparent)
                            )
                        )
                        .border(1.dp, Color(0xFF00A3FF).copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = Color(0xFF00A3FF),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No Transactions Recorded",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sync your bank SMS or tap below to record your first income, expense, or transfer.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 11.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00A3FF), Color(0xFF0066FF))
                            )
                        )
                        .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Add First Transaction",
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
