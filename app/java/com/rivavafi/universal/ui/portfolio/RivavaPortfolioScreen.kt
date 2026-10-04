package com.rivavafi.universal.ui.portfolio

import android.content.Context
import androidx.compose.animation.animateContentSize
import android.content.Intent
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.sp
import com.rivavafi.universal.ui.components.PortfolioStockCard
import com.rivavafi.universal.ui.theme.PrimaryContainerSky
import com.rivavafi.universal.ui.components.SectionHeader
import com.rivavafi.universal.ui.theme.PremiumGradientStart
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.OpenInNew
import com.rivavafi.universal.ui.theme.glassMorphism
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import com.rivavafi.universal.ui.theme.AmoledBlack
import com.rivavafi.universal.ui.theme.TertiaryEmerald
import com.rivavafi.universal.ui.theme.SecondaryPink
import com.rivavafi.universal.ui.theme.PrimarySky
import com.rivavafi.universal.ui.theme.EmeraldGreen
import com.rivavafi.universal.ui.theme.VibrantRed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import kotlinx.coroutines.delay
import android.view.WindowManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.rivavafi.universal.data.repository.EntitlementStatus
import com.rivavafi.universal.ui.components.PremiumUnlockAnimation
import com.rivavafi.universal.ui.components.RivavaGlowingLogo
import com.rivavafi.universal.ui.theme.DarkCardBgElevated
import com.rivavafi.universal.ui.theme.DarkCardBorderHighlight
import com.rivavafi.universal.ui.theme.RivavaGoldGradient
import com.rivavafi.universal.ui.theme.RivavaBrandGradient
import com.rivavafi.universal.ui.theme.RivavaCyanGradient
import com.rivavafi.universal.ui.theme.DarkCardBg
import com.rivavafi.universal.ui.theme.DarkCardBorder
import com.rivavafi.universal.ui.theme.RivavaCyan
import com.rivavafi.universal.ui.theme.RivavaPink
import com.rivavafi.universal.ui.theme.RivavaLime
import com.rivavafi.universal.ui.theme.OnDarkSurfaceVariant
import androidx.compose.ui.graphics.Brush

data class PortfolioItem(
    val exchange: String,
    val ticker: String,
    val companyName: String,
    val marketPrice: String,
    val purchasePrice: String = "—",
    val date: String = "—"
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RivavaPortfolioScreen(
    premiumViewModel: PremiumViewModel = hiltViewModel(),
    onNavigateToDetail: (ticker: String, focus: String?) -> Unit,
    onBack: () -> Unit = {},
    viewModel: StockViewModel = hiltViewModel(),
    cryptoViewModel: CryptoViewModel = hiltViewModel(),
    profileViewModel: com.rivavafi.universal.ui.profile.ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val premiumState by premiumViewModel.premiumState.collectAsState()
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val profileState by profileViewModel.profileState.collectAsState()


    val userModel = profileState.userModel

    val userPhone = userModel?.phone?.takeIf { it.isNotBlank() } ?: userModel?.phoneno?.takeIf { it.isNotBlank() } ?: auth.currentUser?.phoneNumber ?: ""
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var premiumKeyInput by remember { mutableStateOf("") }

    val portfolioPaymentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            premiumViewModel.syncEntitlement()
            android.widget.Toast.makeText(context, "🎉 Portfolio Premium Unlocked!", android.widget.Toast.LENGTH_LONG).show()
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
                Text("Loading premium status...", color = Color.White)
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
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
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
                            RivavaGlowingLogo(size = 54.dp)
                            Box(
                                modifier = Modifier
                                    .offset(x = 6.dp, y = 6.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFFD700), Color(0xFFB8860B))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.Black,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            "Rivava Portfolio VIP",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                color = Color.White
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Private institution-grade market analytics, live multi-asset tracking, and expert advisory.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = OnDarkSurfaceVariant,
                                lineHeight = 20.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Features List with Neon Checkmarks
                        val vipFeatures = listOf(
                            "Live Real-Time Indian (NSE) & US (NYSE) Quotes",
                            "24/7 Live Crypto Rates (BTC, ETH, SOL in INR)",
                            "Global Finnhub Market News & Intelligence",
                            "Direct 1-on-1 SEBI Registered Advisor Access"
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            vipFeatures.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(RivavaLime.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", color = RivavaLime, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                    Text(
                                        text = feature,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val intent = Intent(context, PaymentActivity::class.java).apply {
                                    putExtra("plan", "portfolio_premium")
                                    putExtra("amountPaise", 39900)
                                    putExtra("title", "Rivava Portfolio Premium")
                                }
                                portfolioPaymentLauncher.launch(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
                        ) {
                            Text(
                                "Pay ₹399 & Unlock Instant Access",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppForAdvisor(
                                    context = context,
                                    username = auth.currentUser?.displayName ?: "User",
                                    email = auth.currentUser?.email ?: "",
                                    phoneNumber = userPhone,
                                    preference = "No",
                                    premiumStatus = false
                                )
                                showWhatsAppDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RivavaCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                "💬 Chat With SEBI Advisor",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RivavaCyan
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        var showSecretDialog by remember { mutableStateOf(false) }

                        TextButton(
                            onClick = { showSecretDialog = true },
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text("Have a pass? Enter Secret Key →", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }

                        if (showSecretDialog) {
                            PremiumUnlockDialog(
                                onDismiss = { showSecretDialog = false },
                                onUnlockSuccess = {
                                    premiumViewModel.syncEntitlement()
                                    showSecretDialog = false
                                },
                                onPayClick = {
                                    showSecretDialog = false
                                    val intent = Intent(context, PaymentActivity::class.java).apply {
                                        putExtra("plan", "portfolio_premium")
                                        putExtra("amountPaise", 39900)
                                        putExtra("title", "Rivava Portfolio Premium")
                                    }
                                    portfolioPaymentLauncher.launch(intent)
                                }
                            )
                        }

                        if (showWhatsAppDialog) {
                            AlertDialog(
                                properties = androidx.compose.ui.window.DialogProperties(securePolicy = androidx.compose.ui.window.SecureFlagPolicy.SecureOn),
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

    val stockStates by viewModel.stockStates.collectAsState()
    val cryptoStates by cryptoViewModel.cryptoStates.collectAsState()

    val cryptoIds = listOf("bitcoin", "ethereum", "solana")

    LaunchedEffect(Unit) {
        viewModel.startPolling(listOf("IREDA.NS", "INDHOTEL.NS", "RTX", "NVDA"))
        cryptoViewModel.startPolling(cryptoIds)
    }

    // moved down

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.systemBarsPadding()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 140.dp), // Provide enough bottom padding for the floating nav bar
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                // Custom Logo Header and Refresh
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        viewModel.refresh()
                        cryptoViewModel.refresh()
                    }) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.rivavafi.universal.R.drawable.rivava_logo),
                        contentDescription = "Rivava Logo",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }


            }



            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    var showLogsDialog by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(title = "My Portfolio")

                        val hasErrors = stockStates.values.any { it.error != null }
                        val statusColor = if (hasErrors) VibrantRed else EmeraldGreen

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showLogsDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasErrors) "Degraded" else "Live",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor
                            )
                        }
                    }

                    if (showLogsDialog) {
                        var screenshotsAllowed by remember { mutableStateOf(false) }
                        AlertDialog(
                            properties = androidx.compose.ui.window.DialogProperties(securePolicy = androidx.compose.ui.window.SecureFlagPolicy.SecureOn),
                            onDismissRequest = { showLogsDialog = false },
                            title = { Text("API Diagnostic Logs") },
                            text = {
                                LazyColumn {
                                    items(stockStates.entries.toList()) { (symbol, state) ->
                                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                            Text(text = "Symbol: $symbol", fontWeight = FontWeight.Bold)
                                            Text(text = "Source: ${state.source.name}")
                                            Text(text = "Error: ${state.error ?: "None"}", color = if (state.error != null) VibrantRed else PrimarySky)
                                            if (state.diagnostics != null) {
                                                Text(
                                                    text = "Logs:\n${state.diagnostics}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = Color.Gray,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    TextButton(onClick = { showLogsDialog = false }) {
                                        Text("Close")
                                    }
                                }
                            }
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(100)
                                visible = true
                            }

                            val nseStocks = listOf("IREDA.NS", "INDHOTEL.NS")
                            val nyseStocks = listOf("RTX", "NVDA")

                            // Helper to render a stock card
                            @Composable
                            fun RenderStockCard(symbol: String, index: Int) {
                                val normalizedSymbol = if (symbol == "IREDA") "IREDA.NS" else symbol
                                val stockState = stockStates[normalizedSymbol]
                                val quote = stockState?.data

                                val isIreda = normalizedSymbol == "IREDA.NS"
                                val isIhcl = normalizedSymbol == "INDHOTEL.NS"
                                val isRtx = normalizedSymbol == "RTX"
                                val isNvda = normalizedSymbol == "NVDA"

                                val ticker = when {
                                    isIreda -> "IREDA"
                                    isIhcl -> "INDHOTEL"
                                    isNvda -> "NVDA"
                                    else -> "RTX"
                                }
                                val companyName = when {
                                    isIreda -> "IREDA"
                                    isIhcl -> "Indian Hotels Company Limited"
                                    isNvda -> "NVIDIA Corporation"
                                    else -> "Raytheon Technologies"
                                }
                                val exchange = if (isIreda || isIhcl) "NSE" else "NYSE"
                                val currency = if (exchange == "NYSE") "$" else "₹"

                                val price = quote?.c ?: when {
                                    isIreda -> 150.0
                                    isIhcl -> 580.0
                                    isNvda -> 120.0
                                    else -> 100.0
                                }
                                val previousClose = quote?.pc ?: when {
                                    isIreda -> 148.0
                                    isIhcl -> 575.0
                                    isNvda -> 118.0
                                    else -> 99.0
                                }
                                val change = price - previousClose
                                val changePercent = if (previousClose != 0.0) (change / previousClose) * 100 else 0.0
                                val isPositive = change >= 0

                                val displayPrice = currency + String.format(Locale.getDefault(), "%.2f", price)
                                val displayAbsChange = "${if (isPositive) "+" else ""}${String.format(Locale.getDefault(), "%.2f", change)}"
                                val displayPctChange = "${if (isPositive) "+" else ""}${String.format(Locale.getDefault(), "%.2f", changePercent)}%"

                                val isDefault = stockState?.source == com.rivavafi.universal.domain.repository.QuoteSource.DEFAULT

                                AnimatedVisibility(
                                    visible = visible,
                                    enter = slideInVertically(
                                        initialOffsetY = { 50 },
                                        animationSpec = tween(durationMillis = 400, delayMillis = index * 100)
                                    ) + fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = index * 100))
                                ) {
                                    PortfolioStockCard(
                                        exchange = exchange,
                                        ticker = ticker,
                                        companyName = companyName,
                                        marketPrice = displayPrice,
                                        isPositive = isPositive,
                                        absoluteChange = displayAbsChange,
                                        percentageChange = displayPctChange,
                                        isDefault = isDefault,
                                        onValueClick = { focus ->
                                            onNavigateToDetail(ticker, focus)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // NSE Section
                            Column {
                                Text(
                                    "INDIAN MARKET",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = com.rivavafi.universal.ui.theme.RivavaLime,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    nseStocks.forEachIndexed { index, symbol ->
                                        RenderStockCard(symbol, index)
                                    }
                                }
                            }

                            // NYSE Section
                            Column {
                                Text(
                                    "US MARKET",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = com.rivavafi.universal.ui.theme.RivavaCyan,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    nyseStocks.forEachIndexed { index, symbol ->
                                        RenderStockCard(symbol, index + nseStocks.size)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Crypto Assets"
                )
                val fallbackCryptoData = mapOf(
                    "bitcoin" to CryptoData(price = 7330590.0, change24h = 3.47),
                    "ethereum" to CryptoData(price = 224359.0, change24h = 3.77),
                    "solana" to CryptoData(price = 8252.12, change24h = 3.11)
                )
                val isApiWorking = cryptoStates.isNotEmpty() && !cryptoStates.values.all { it.price <= 0.0 }
                val cryptoToDisplay = if (isApiWorking) {
                    fallbackCryptoData + cryptoStates
                } else {
                    fallbackCryptoData
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    cryptoIds.forEach { id ->
                        CryptoCard(id = id, data = cryptoToDisplay[id] ?: fallbackCryptoData.getValue(id), isApiWorking = isApiWorking)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                SectionHeader(title = "Market News")
                Spacer(modifier = Modifier.height(16.dp))
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                val indianMarket = listOf(
                    NewsItem("Business Standard", "Latest Financial News from India", "https://www.business-standard.com/", "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=600&q=80"),
                    NewsItem("The Economic Times", "Market Updates and Business News", "https://economictimes.indiatimes.com/", "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=600&q=80")
                )
                val usMarket = listOf(
                    NewsItem("The Wall Street Journal", "US Markets and Global Business", "https://www.wsj.com/", "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=600&q=80"),
                    NewsItem("Bloomberg", "Finance, Stock Market, and Business News", "https://www.bloomberg.com/", "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=600&q=80")
                )
                val internationalMarket = listOf(
                    NewsItem("Financial Times", "Global Economy and Market News", "https://www.ft.com/", "https://images.unsplash.com/photo-1444653614773-995cb1ef9efa?w=600&q=80"),
                    NewsItem("The Economist", "World News, Politics, Economics", "https://www.economist.com/", "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=600&q=80")
                )

                MarketNewsSection("🇮🇳 INDIAN MARKET", com.rivavafi.universal.ui.theme.RivavaPink, indianMarket, uriHandler)
                Spacer(modifier = Modifier.height(24.dp))
                MarketNewsSection("🇺🇸 US MARKET", com.rivavafi.universal.ui.theme.RivavaCyan, usMarket, uriHandler)
                Spacer(modifier = Modifier.height(24.dp))
                MarketNewsSection("🌍 INTERNATIONAL MARKET", com.rivavafi.universal.ui.theme.RivavaLime, internationalMarket, uriHandler)
            }


            item {
                Text(
                    text = "Live prices refresh every 30 seconds. Tap a card to cross-check on market sources.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }


}

@Composable
fun CryptoCard(id: String, data: CryptoData, isApiWorking: Boolean = true) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val isPositive = data.change24h >= 0
    val color = if (isPositive) RivavaLime else RivavaPink
    val inrFormatter = java.text.NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val (symbol, brandColor, cryptoName) = when (id.lowercase()) {
        "bitcoin" -> Triple("BTC", Color(0xFFF7931A), "Bitcoin")
        "ethereum" -> Triple("ETH", Color(0xFF627EEA), "Ethereum")
        "solana" -> Triple("SOL", Color(0xFF14F195), "Solana")
        else -> Triple(id.take(3).uppercase(Locale.getDefault()), RivavaCyan, id.replaceFirstChar { it.titlecase(Locale.getDefault()) })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(DarkCardBorderHighlight, DarkCardBorder)
                ),
                RoundedCornerShape(22.dp)
            )
            .animateContentSize(animationSpec = androidx.compose.animation.core.tween(durationMillis = 300))
            .clickable {
                val url = "https://www.google.com/search?q=$id+crypto+price"
                try {
                    uriHandler.openUri(url)
                } catch(e: Exception) {}
            },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(DarkCardBgElevated, DarkCardBg)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Crypto Icon Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brandColor.copy(alpha = 0.16f))
                        .border(1.dp, brandColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbol,
                        color = brandColor,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = cryptoName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.2.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = if (data.price > 0) inrFormatter.format(data.price) else "Live Market Rate",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Rate change pill
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "${if (isPositive) "▲ +" else "▼ "}${String.format(Locale.getDefault(), "%.2f", data.change24h)}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        ),
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(RivavaCyan.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "See rates",
                        tint = RivavaCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CuratedNewsCard(title: String, url: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(DarkCardBorderHighlight, DarkCardBorder)),
                RoundedCornerShape(20.dp)
            )
            .clickable {
                try {
                    uriHandler.openUri(url)
                } catch (e: Exception) {}
            },
        colors = CardDefaults.cardColors(containerColor = DarkCardBgElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Read News",
                tint = RivavaCyan,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun NewsCard(news: com.rivavafi.universal.domain.api.FinnhubNewsResponse) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    Card(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                Brush.verticalGradient(listOf(DarkCardBorderHighlight, DarkCardBorder)),
                RoundedCornerShape(20.dp)
            )
            .clickable {
                try {
                    uriHandler.openUri(news.url)
                } catch (e: Exception) {}
            },
        colors = CardDefaults.cardColors(containerColor = DarkCardBgElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            coil.compose.AsyncImage(
                model = if (news.image.isBlank()) "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=600&q=80" else news.image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = news.headline,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = Color.White,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = news.source,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = RivavaCyan
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

@Composable
fun MarketNewsSection(
    heading: String,
    headingColor: Color,
    items: List<NewsItem>,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            heading,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = headingColor
        )
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { news ->
                    Box(modifier = Modifier.weight(1f)) {
                        StaticNewsCard(news.source, news.title, news.url, news.imageUrl, uriHandler)
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun StaticNewsCard(source: String, title: String, url: String, imageUrl: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .animateContentSize(animationSpec = androidx.compose.animation.core.tween(durationMillis = 300))
            .clickable {
                try {
                    uriHandler.openUri(url)
                } catch (e: Exception) {}
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            coil.compose.AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 100f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = source,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF00E471) // Green accent
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
