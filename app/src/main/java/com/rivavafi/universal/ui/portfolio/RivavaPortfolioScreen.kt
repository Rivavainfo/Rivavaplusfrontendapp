package com.rivavafi.universal.ui.portfolio

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rivavafi.universal.R
import com.rivavafi.universal.data.local.TransactionEntity
import com.rivavafi.universal.data.repository.EntitlementStatus
import com.rivavafi.universal.ui.components.RivavaGlowingLogo
import com.rivavafi.universal.ui.theme.AmoledBlack
import com.rivavafi.universal.ui.theme.CategoryVisuals
import com.rivavafi.universal.ui.theme.EmeraldGreen
import com.rivavafi.universal.ui.theme.RivavaCyan
import com.rivavafi.universal.ui.theme.RivavaLime
import com.rivavafi.universal.ui.theme.RivavaPink
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

private fun formatInrVal(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return "₹" + formatter.format(amount)
}

@Composable
fun RivavaPortfolioScreen(
    onBack: () -> Unit = {},
    onNavigateToDetail: (ticker: String, focus: String) -> Unit = { _, _ -> },
    onNavigateToProfile: () -> Unit = {},
    premiumViewModel: PremiumViewModel = hiltViewModel(),
    viewModel: StockViewModel = hiltViewModel(),
    cryptoViewModel: CryptoViewModel = hiltViewModel(),
    profileViewModel: com.rivavafi.universal.ui.profile.ProfileViewModel = hiltViewModel(),
    homeViewModel: com.rivavafi.universal.ui.home.HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val premiumState by premiumViewModel.premiumState.collectAsState()
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val profileState by profileViewModel.profileState.collectAsState()
    val transactions by homeViewModel.transactions.collectAsState()

    val userModel = profileState.userModel
    val userPhone = userModel?.phone?.takeIf { it.isNotBlank() }
        ?: userModel?.phoneno?.takeIf { it.isNotBlank() }
        ?: auth.currentUser?.phoneNumber ?: ""
    var showWhatsAppDialog by remember { mutableStateOf(false) }

    val portfolioPaymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            premiumViewModel.syncEntitlement()
            android.widget.Toast.makeText(context, "Portfolio Premium Unlocked!", android.widget.Toast.LENGTH_LONG).show()
        } else {
            val error = result.data?.getStringExtra("error") ?: "Payment was cancelled."
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (premiumState.status == EntitlementStatus.LOADING) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = EmeraldGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading portfolio intelligence...", color = Color.White)
            }
        }
        return
    }

    if (premiumState.status != EntitlementStatus.UNLOCKED) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack)
                .systemBarsPadding()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                RivavaCyan.copy(alpha = 0.8f),
                                RivavaPink.copy(alpha = 0.6f),
                                RivavaLime.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1B1D2E),
                                    Color(0xFF12131F),
                                    Color(0xFF090A10)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Glowing Brand Logo & Lock Badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            RivavaGlowingLogo(size = 56.dp)
                            Box(
                                modifier = Modifier
                                    .offset(x = 6.dp, y = 6.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFFB800), Color(0xFFD97706))
                                        )
                                    )
                                    .border(1.5.dp, Color(0xFF090A10), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFF090A10),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Luxury VIP Pill Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFF00C6FF).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFF00C6FF).copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "★ INSTITUTIONAL INTELLIGENCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.4.sp,
                                    fontSize = 10.sp,
                                    color = Color(0xFF00C6FF)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "Rivava Portfolio VIP",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.6).sp,
                                fontSize = 24.sp,
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Private institution-grade market analytics, live multi-asset tracking, and expert advisory.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                lineHeight = 18.sp,
                                fontSize = 12.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Features List
                        val vipFeatures = listOf(
                            Triple("Live NSE & NYSE Quotes", "Real-time tick stream with zero latency", Icons.AutoMirrored.Filled.TrendingUp),
                            Triple("24/7 Multi-Asset Tracking", "Live INR valuation for BTC, ETH & SOL", Icons.Default.ShowChart),
                            Triple("Global Finnhub Market News", "Wall St & Dalal Street institutional radar", Icons.Outlined.Public),
                            Triple("Direct SEBI Advisor Access", "1-on-1 private advisory consultation", Icons.Default.VerifiedUser)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF131724), RoundedCornerShape(18.dp))
                                .border(1.dp, Color(0xFF232D42), RoundedCornerShape(18.dp))
                                .padding(14.dp)
                        ) {
                            vipFeatures.forEach { (title, subtitle, icon) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF00C6FF).copy(alpha = 0.12f))
                                            .border(1.dp, Color(0xFF00C6FF).copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color(0xFF00C6FF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        )
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00E471),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        var showSecretDialog by remember { mutableStateOf(false) }

                        // Chat with Advisor Button
                        Button(
                            onClick = {
                                val advisorMsg = "Hello Rivava Team, I would like to speak with an advisor regarding Rivava Elite Portfolio access. Please guide me through the activation process."
                                com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppWithMessage(
                                    context = context,
                                    customMessage = advisorMsg
                                )
                                showWhatsAppDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF00C6FF)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00C6FF),
                                contentColor = Color(0xFF0A0F1D)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = Color(0xFF0A0F1D),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Chat with Advisor",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0A0F1D)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Unlock with Secret Key Button
                        OutlinedButton(
                            onClick = { showSecretDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFB800)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFFFB800).copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Key",
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Unlock with Secret Key",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB800)
                                    )
                                )
                            }
                        }

                        if (showSecretDialog) {
                            PremiumUnlockDialog(
                                onDismiss = { showSecretDialog = false },
                                onUnlockSuccess = {
                                    premiumViewModel.syncEntitlement()
                                    showSecretDialog = false
                                }
                            )
                        }

                        if (showWhatsAppDialog) {
                            AlertDialog(
                                onDismissRequest = { showWhatsAppDialog = false },
                                title = { Text("Contact Advisor", fontWeight = FontWeight.Bold) },
                                text = { Text("Did you connect with the advisor successfully?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showWhatsAppDialog = false
                                        com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppForAdvisor(
                                            context = context,
                                            username = auth.currentUser?.displayName ?: "User",
                                            email = auth.currentUser?.email ?: "",
                                            phoneNumber = userPhone,
                                            preference = "No",
                                            premiumStatus = false
                                        )
                                    }) {
                                        Text("Contact Again", color = Color(0xFFD4AF37))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showWhatsAppDialog = false }) {
                                        Text("Close", color = Color.Gray)
                                    }
                                },
                                containerColor = Color(0xFF1E1E1E),
                                titleContentColor = Color.White,
                                textContentColor = Color.White
                            )
                        }
                    }
                }
            }
        }
        return
    }

    // Unlocked / Development Mode: Render Full Portfolio Section
    val stockStates by viewModel.stockStates.collectAsState()
    val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()
    val cryptoIds = listOf("bitcoin", "ethereum", "solana")

    LaunchedEffect(Unit) {
        viewModel.startPolling(listOf("IREDA.NS", "INDHOTEL.NS", "RTX", "NVDA"))
        cryptoViewModel.startPolling(cryptoIds)
    }

    var selectedTimeframe by remember { mutableStateOf("1D") }
    var topStocksMarket by remember { mutableStateOf("Indian") } // "Indian" or "US"

    // Calculate real stock investments value dynamically from live market quotes
    val iredaQuotePrice = stockStates["IREDA.NS"]?.data?.c ?: 107.33
    val indhQuotePrice = stockStates["INDHOTEL.NS"]?.data?.c ?: 731.30
    val rtxQuotePrice = stockStates["RTX"]?.data?.c ?: 183.29
    val nvdaQuotePrice = stockStates["NVDA"]?.data?.c ?: 239.24

    // Fixed realistic stock quantities for tracked holdings (IREDA: 500, INDHOTEL: 80, RTX: 10, NVDA: 15)
    val totalStockInvestmentsVal = remember(iredaQuotePrice, indhQuotePrice, rtxQuotePrice, nvdaQuotePrice) {
        (500 * iredaQuotePrice) + (80 * indhQuotePrice) + (10 * rtxQuotePrice * 83.5) + (15 * nvdaQuotePrice * 83.5)
    }

    Scaffold(
        containerColor = Color(0xFF060913),
        modifier = Modifier.systemBarsPadding()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // 1. Top Brand Header Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RivavaGlowingLogo(size = 36.dp)
                        Column {
                            Text(
                                text = "RIVAVA",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 17.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "India's First Finance Research Hub",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // User Initial Avatar
                        val initial = auth.currentUser?.displayName?.firstOrNull()?.uppercase() ?: "A"
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                                    )
                                )
                                .border(1.5.dp, Color(0xFF00C6FF).copy(alpha = 0.5f), CircleShape)
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // 2. "My Portfolio" Card calculated directly from user transaction history
            item {
                MyPortfolioTransactionCard(
                    transactions = transactions,
                    trackedStockValue = totalStockInvestmentsVal
                )
            }

            // 3. "Top Stocks" Section with Indian / US switcher
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF2A6D).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFFFF2A6D),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Top Stocks",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                ),
                                color = Color.White
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Indian / US Toggle
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFF101626))
                                    .border(1.dp, Color(0xFF1E283D), RoundedCornerShape(999.dp))
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (topStocksMarket == "Indian") Color(0xFF0077EE) else Color.Transparent)
                                        .clickable { topStocksMarket = "Indian" }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Indian",
                                        color = if (topStocksMarket == "Indian") Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (topStocksMarket == "US") Color(0xFF0077EE) else Color.Transparent)
                                        .clickable { topStocksMarket = "US" }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "US",
                                        color = if (topStocksMarket == "US") Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "See All →",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.clickable { onNavigateToDetail("IREDA", "overview") }
                            )
                        }
                    }

                    if (topStocksMarket == "Indian") {
                        // IREDA
                        val iredaQuote = stockStates["IREDA.NS"]?.data
                        val iredaPrice = iredaQuote?.c?.let { "₹" + String.format(Locale.getDefault(), "%.2f", it) } ?: "₹107.33"
                        PortfolioAssetRow(
                            badgeText = "IRED",
                            badgeBgColor = Color(0xFF0D3328),
                            badgeTextColor = Color(0xFF14F195),
                            title = "IREDA",
                            subtitle = "Indian Renewable Energy Development",
                            price = iredaPrice,
                            pctChange = "-1.53%",
                            absChange = "-1.67",
                            isPositive = false,
                            sparklinePositive = true,
                            onClick = { onNavigateToDetail("IREDA", "overview") }
                        )

                        // INDHOTEL
                        val indhQuote = stockStates["INDHOTEL.NS"]?.data
                        val indhPrice = indhQuote?.c?.let { "₹" + String.format(Locale.getDefault(), "%.2f", it) } ?: "₹731.30"
                        PortfolioAssetRow(
                            badgeText = "INDH",
                            badgeBgColor = Color(0xFF381216),
                            badgeTextColor = Color(0xFFFF3366),
                            title = "INDHOTEL",
                            subtitle = "Indian Hotels Co. Ltd.",
                            price = indhPrice,
                            pctChange = "-0.64%",
                            absChange = "-4.70",
                            isPositive = false,
                            sparklinePositive = false,
                            onClick = { onNavigateToDetail("INDHOTEL", "overview") }
                        )
                    } else {
                        // US Options inside Top Stocks
                        val rtxQuote = stockStates["RTX"]?.data
                        val rtxPrice = rtxQuote?.c?.let { "$" + String.format(Locale.getDefault(), "%.2f", it) } ?: "$183.29"
                        PortfolioAssetRow(
                            badgeText = "RTX",
                            badgeBgColor = Color(0xFF122852),
                            badgeTextColor = Color(0xFF60A5FA),
                            title = "RTX",
                            subtitle = "Raytheon Technologies",
                            price = rtxPrice,
                            pctChange = "-0.56%",
                            absChange = "-1.04",
                            isPositive = false,
                            sparklinePositive = true,
                            onClick = { onNavigateToDetail("RTX", "overview") }
                        )

                        val nvdaQuote = stockStates["NVDA"]?.data
                        val nvdaPrice = nvdaQuote?.c?.let { "$" + String.format(Locale.getDefault(), "%.2f", it) } ?: "$239.24"
                        PortfolioAssetRow(
                            badgeText = "NVDA",
                            badgeBgColor = Color(0xFF1B3813),
                            badgeTextColor = Color(0xFF84CC16),
                            title = "NVIDIA",
                            subtitle = "NVIDIA Corporation",
                            price = nvdaPrice,
                            pctChange = "+0.14%",
                            absChange = "+0.34",
                            isPositive = true,
                            sparklinePositive = true,
                            onClick = { onNavigateToDetail("NVDA", "overview") }
                        )
                    }
                }
            }

            // 4. "US Stocks" Dedicated Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF00C6FF).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Public,
                                    contentDescription = null,
                                    tint = Color(0xFF00C6FF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "US Stocks",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                ),
                                color = Color.White
                            )
                        }

                        Text(
                            text = "See All →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.clickable { onNavigateToDetail("NVDA", "overview") }
                        )
                    }

                    // RTX
                    val rtxQuote = stockStates["RTX"]?.data
                    val rtxPrice = rtxQuote?.c?.let { "$" + String.format(Locale.getDefault(), "%.2f", it) } ?: "$183.29"
                    PortfolioAssetRow(
                        badgeText = "RTX",
                        badgeBgColor = Color(0xFF122852),
                        badgeTextColor = Color(0xFF60A5FA),
                        title = "RTX",
                        subtitle = "Raytheon Technologies",
                        price = rtxPrice,
                        pctChange = "-0.56%",
                        absChange = "-1.04",
                        isPositive = false,
                        sparklinePositive = true,
                        onClick = { onNavigateToDetail("RTX", "overview") }
                    )

                    // NVIDIA
                    val nvdaQuote = stockStates["NVDA"]?.data
                    val nvdaPrice = nvdaQuote?.c?.let { "$" + String.format(Locale.getDefault(), "%.2f", it) } ?: "$239.24"
                    PortfolioAssetRow(
                        badgeText = "NVDA",
                        badgeBgColor = Color(0xFF1B3813),
                        badgeTextColor = Color(0xFF84CC16),
                        title = "NVIDIA",
                        subtitle = "NVIDIA Corporation",
                        price = nvdaPrice,
                        pctChange = "+0.14%",
                        absChange = "+0.34",
                        isPositive = true,
                        sparklinePositive = true,
                        onClick = { onNavigateToDetail("NVDA", "overview") }
                    )
                }
            }

            // 5. "Crypto Assets" Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF7931A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₿",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = "Crypto Assets",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                ),
                                color = Color.White
                            )
                        }

                        Text(
                            text = "See All →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.clickable {
                                try {
                                    uriHandler.openUri("https://coinmarketcap.com/")
                                } catch (_: Exception) {}
                            }
                        )
                    }

                    // BTC
                    val btcData = cryptoStates["bitcoin"]
                    val btcChange = btcData?.change24h ?: -2.64
                    PortfolioAssetRow(
                        badgeText = "₿",
                        badgeBgColor = Color(0xFFF7931A),
                        badgeTextColor = Color.White,
                        title = "BTC",
                        subtitle = "Bitcoin",
                        titleColor = Color(0xFFF7931A),
                        sparklinePositive = btcChange >= 0,
                        onClick = {
                            try {
                                uriHandler.openUri("https://www.google.com/search?q=bitcoin+price+inr")
                            } catch (_: Exception) {}
                        }
                    )

                    // ETH
                    val ethData = cryptoStates["ethereum"]
                    val ethChange = ethData?.change24h ?: -4.98
                    PortfolioAssetRow(
                        badgeText = "♦",
                        badgeBgColor = Color(0xFF38467A),
                        badgeTextColor = Color(0xFF818CF8),
                        title = "ETH",
                        subtitle = "Ethereum",
                        titleColor = Color(0xFF818CF8),
                        sparklinePositive = ethChange >= 0,
                        onClick = {
                            try {
                                uriHandler.openUri("https://www.google.com/search?q=ethereum+price+inr")
                            } catch (_: Exception) {}
                        }
                    )

                    // SOL
                    val solData = cryptoStates["solana"]
                    val solChange = solData?.change24h ?: -2.73
                    PortfolioAssetRow(
                        badgeText = "≡",
                        badgeBgColor = Color(0xFF0F3B36),
                        badgeTextColor = Color(0xFF14F195),
                        title = "SOL",
                        subtitle = "Solana",
                        titleColor = Color(0xFF14F195),
                        sparklinePositive = solChange >= 0,
                        onClick = {
                            try {
                                uriHandler.openUri("https://www.google.com/search?q=solana+price+inr")
                            } catch (_: Exception) {}
                        }
                    )
                }
            }

            // 6. "Market News" Section with High-Res Image Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFA855F7).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Feed,
                                    contentDescription = null,
                                    tint = Color(0xFFA855F7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Market News",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                ),
                                color = Color.White
                            )
                        }

                        Text(
                            text = "See All →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.clickable {
                                try {
                                    uriHandler.openUri("https://www.business-standard.com/")
                                } catch (_: Exception) {}
                            }
                        )
                    }

                    // Side-by-side 2 News Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Indian Market
                        NewsImageCard(
                            imageRes = R.drawable.news_indian_market,
                            flagEmoji = "🇮🇳",
                            tagTitle = "INDIAN MARKET",
                            tagColor = Color(0xFFFF3366),
                            headline = "Latest Financial News from India",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                try {
                                    uriHandler.openUri("https://www.business-standard.com/")
                                } catch (_: Exception) {}
                            }
                        )

                        // Card 2: US Market
                        NewsImageCard(
                            imageRes = R.drawable.news_us_market,
                            flagEmoji = "🇺🇸",
                            tagTitle = "US MARKET",
                            tagColor = Color(0xFF00C6FF),
                            headline = "US Markets and Global Business",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                try {
                                    uriHandler.openUri("https://www.wsj.com/")
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }

            // Bottom Footnote
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Live prices refresh automatically. Tap any card for institutional research.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color(0xFF64748B),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Task 1: "My Portfolio" Card displaying real financial metrics calculated directly from user transaction history.
 */
@Composable
fun MyPortfolioTransactionCard(
    transactions: List<TransactionEntity>,
    trackedStockValue: Double = 124560.0
) {
    var showMoreDetails by remember { mutableStateOf(false) }

    // Helper functions for transaction filtering
    fun isDebitTxn(type: String): Boolean {
        val t = type.trim().uppercase()
        return t == "DEBIT" || t == "EXPENSE" || t == "BILL" || t == "BILL_PENDING" || t == "PAYMENT" ||
                (t != "CREDIT" && t != "INCOME" && t != "REWARD" && t != "SELF_TRANSFER" && t != "TRANSFER" && t != "IGNORE")
    }

    fun isCreditTxn(type: String): Boolean {
        val t = type.trim().uppercase()
        return t == "CREDIT" || t == "INCOME" || t == "REWARD"
    }

    // Calculations based on saved user transactions
    val totalMoneySpent = remember(transactions) {
        transactions.filter { isDebitTxn(it.type) }.sumOf { it.amount }
    }

    val totalMoneyReceived = remember(transactions) {
        transactions.filter { isCreditTxn(it.type) }.sumOf { it.amount }
    }

    val netCashFlow = totalMoneyReceived - totalMoneySpent

    val totalTxnCount = transactions.size
    val debitCount = remember(transactions) { transactions.count { isDebitTxn(it.type) } }
    val creditCount = remember(transactions) { transactions.count { isCreditTxn(it.type) } }

    // Today's values (resets at local midnight)
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todaySpent = remember(transactions, todayStart) {
        transactions.filter { it.date >= todayStart && isDebitTxn(it.type) }.sumOf { it.amount }
    }

    val todayReceived = remember(transactions, todayStart) {
        transactions.filter { it.date >= todayStart && isCreditTxn(it.type) }.sumOf { it.amount }
    }

    // Month's values (current calendar month)
    val monthStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val monthSpent = remember(transactions, monthStart) {
        transactions.filter { it.date >= monthStart && isDebitTxn(it.type) }.sumOf { it.amount }
    }

    val monthReceived = remember(transactions, monthStart) {
        transactions.filter { it.date >= monthStart && isCreditTxn(it.type) }.sumOf { it.amount }
    }

    // Largest Expense & Top Spending Category
    val largestExpense = remember(transactions) {
        transactions.filter { isDebitTxn(it.type) }.maxByOrNull { it.amount }?.amount ?: 0.0
    }

    val topCategory = remember(transactions) {
        val debitTxns = transactions.filter { isDebitTxn(it.type) }
        if (debitTxns.isEmpty()) "None"
        else {
            debitTxns.groupBy { txn ->
                CategoryVisuals.resolveCategoryVisual(txn.category, txn.subcategory).title
            }.maxByOrNull { entry -> entry.value.sumOf { it.amount } }?.key ?: "None"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFF1B2338), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101C))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF00C6FF).copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF00C6FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "MY PORTFOLIO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFF00E471), CircleShape)
                    )
                    Text(
                        text = "Live Data",
                        color = Color(0xFF00E471),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Money Spent vs Money Received
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Money Spent",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatInrVal(totalMoneySpent),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFFFF3366)
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Money Received",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatInrVal(totalMoneyReceived),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF00E471)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Net Cash Flow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF101524))
                    .border(1.dp, Color(0xFF1B2438), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Cash Flow",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = (if (netCashFlow >= 0) "+" else "") + formatInrVal(netCashFlow),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (netCashFlow >= 0) Color(0xFF00E471) else Color(0xFFFF3366)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = Color(0xFF1B2338), thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Today & Month Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B0E18))
                        .border(1.dp, Color(0xFF1B2338), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text("Today Spent: " + formatInrVal(todaySpent), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = Color(0xFFFF3366), fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Today Received: " + formatInrVal(todayReceived), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = Color(0xFF00E471), fontWeight = FontWeight.Bold))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B0E18))
                        .border(1.dp, Color(0xFF1B2338), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text("This Month Spent: " + formatInrVal(monthSpent), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = Color(0xFFFF3366), fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("This Month Received: " + formatInrVal(monthReceived), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = Color(0xFF00E471), fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable "More Details" Toggle Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMoreDetails = !showMoreDetails }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showMoreDetails) "Hide Details" else "More Details",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00C6FF)
                    )
                )
                Icon(
                    imageVector = if (showMoreDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF00C6FF),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Animated Secondary Information
            AnimatedVisibility(visible = showMoreDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = Color(0xFF1B2338), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Transactions: $totalTxnCount", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium))
                        Text("Debits: $debitCount", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFFFF3366), fontWeight = FontWeight.Bold))
                        Text("Credits: $creditCount", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF00E471), fontWeight = FontWeight.Bold))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Largest Expense:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF94A3B8)))
                        Text(formatInrVal(largestExpense), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Top Spending Category:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF94A3B8)))
                        Text(topCategory, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF00C6FF), fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

/**
 * Reusable asset row perfectly mirroring the reference UI design.
 */
@Composable
fun PortfolioAssetRow(
    badgeText: String,
    badgeBgColor: Color,
    badgeTextColor: Color,
    title: String,
    subtitle: String,
    price: String? = null,
    pctChange: String? = null,
    absChange: String? = null,
    isPositive: Boolean = true,
    sparklinePositive: Boolean = isPositive,
    titleColor: Color = Color.White,
    onClick: () -> Unit = {},
    onOptionsClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF1B2338), RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101C))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Squircle Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(badgeBgColor)
                    .border(1.dp, badgeTextColor.copy(alpha = 0.35f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = badgeTextColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Subtitle
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    ),
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Mini Sparkline
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(30.dp)
                    .padding(horizontal = 2.dp)
            ) {
                MiniSparkline(
                    isPositive = sparklinePositive,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (price != null || pctChange != null) {
                Spacer(modifier = Modifier.width(10.dp))

                // Price & Change Badge
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (price != null) {
                        Text(
                            text = price,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    }

                    if (pctChange != null) {
                        val badgeBg = if (isPositive) Color(0xFF00E471).copy(alpha = 0.15f) else Color(0xFFFF3366).copy(alpha = 0.15f)
                        val badgeBorder = if (isPositive) Color(0xFF00E471).copy(alpha = 0.35f) else Color(0xFFFF3366).copy(alpha = 0.35f)
                        val badgeColor = if (isPositive) Color(0xFF00E471) else Color(0xFFFF3366)

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(badgeBg)
                                .border(0.8.dp, badgeBorder, RoundedCornerShape(7.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${if (isPositive) "▲" else "▼"} $pctChange",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                ),
                                color = badgeColor
                            )
                            if (absChange != null) {
                                Text(
                                    text = absChange,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 9.sp
                                    ),
                                    color = badgeColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * High-res visual news card matching the reference image.
 */
@Composable
fun NewsImageCard(
    imageRes: Int,
    flagEmoji: String,
    tagTitle: String,
    tagColor: Color,
    headline: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(138.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF1B2338), RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101C))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = headline,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Deep dark vignette overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.65f),
                                Color(0xFF060913).copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Micro Category Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(text = flagEmoji, fontSize = 12.sp)
                    Text(
                        text = tagTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = tagColor
                    )
                }

                // Headline + Arrow Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draws the smooth glowing green mountain/area chart on the hero card.
 */
@Composable
fun PortfolioAreaChart(
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF00E471)
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val points = listOf(
            Offset(0f, height * 0.85f),
            Offset(width * 0.12f, height * 0.80f),
            Offset(width * 0.22f, height * 0.65f),
            Offset(width * 0.35f, height * 0.70f),
            Offset(width * 0.48f, height * 0.50f),
            Offset(width * 0.60f, height * 0.55f),
            Offset(width * 0.72f, height * 0.35f),
            Offset(width * 0.85f, height * 0.40f),
            Offset(width * 1.0f, height * 0.15f)
        )

        val strokePath = Path()
        strokePath.moveTo(points.first().x, points.first().y)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val midX = (p0.x + p1.x) / 2
            strokePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // Fill with vertical gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.38f),
                    lineColor.copy(alpha = 0.08f),
                    Color.Transparent
                )
            )
        )

        // Draw crisp stroke
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Endpoint glowing dot
        val last = points.last()
        drawCircle(
            color = lineColor.copy(alpha = 0.3f),
            radius = 6.dp.toPx(),
            center = last
        )
        drawCircle(
            color = lineColor,
            radius = 2.5.dp.toPx(),
            center = last
        )
    }
}

/**
 * Draws the mini-sparkline curve for stock/crypto rows.
 */
@Composable
fun MiniSparkline(
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isPositive) Color(0xFF00E471) else Color(0xFFFF3366)
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val path = Path()
        if (isPositive) {
            path.moveTo(0f, height * 0.8f)
            path.cubicTo(width * 0.25f, height * 0.65f, width * 0.35f, height * 0.9f, width * 0.55f, height * 0.5f)
            path.cubicTo(width * 0.75f, height * 0.2f, width * 0.85f, height * 0.45f, width, height * 0.15f)
        } else {
            path.moveTo(0f, height * 0.2f)
            path.cubicTo(width * 0.25f, height * 0.35f, width * 0.35f, height * 0.1f, width * 0.55f, height * 0.5f)
            path.cubicTo(width * 0.75f, height * 0.8f, width * 0.85f, height * 0.55f, width, height * 0.85f)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun NewsCard(news: com.rivavafi.universal.domain.api.FinnhubNewsResponse) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    Card(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable {
                try {
                    uriHandler.openUri(news.url)
                } catch (_: Exception) {}
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            coil.compose.AsyncImage(
                model = if (news.image.isBlank()) "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=600&q=80" else news.image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = news.headline,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = news.source,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class NewsItem(
    val source: String,
    val title: String,
    val url: String,
    val imageUrl: String
)
