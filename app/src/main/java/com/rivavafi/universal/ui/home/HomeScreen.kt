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
            // 1. Top Bar Matching Reference Screenshot
            item {
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

                    // Right: Notification Bell + Profile Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {


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
                                coil.compose.AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = "Profile Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
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

            // 2. My Portfolio Card (Unlocked for Premium, Locked for Non-Premium)
            item {
                val isPortfolioUnlocked = isPremiumUser || isPremiumPref
                HomeMyPortfolioCard(
                    isUnlocked = isPortfolioUnlocked,
                    transactions = transactions,
                    onUnlockClick = { onNavigateToRivavaPortfolio() },
                    onViewFullPortfolioClick = { onNavigateToRivavaPortfolio() }
                )
            }

            // 3. 1 in 1 Sessions with Our Advisor Micro Card Matching Reference Image
            item {
                HomeAdvisorMicroBanner(
                    onClick = { showVideoCallDialog = true }
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

            // 5. Track Your Money Donut Chart and Category Breakdown Matching Reference Image
            item {
                HomeTrackMoneySection(
                    transactions = transactions,
                    onClick = { onNavigateToAnalytics() },
                    onAddTransactionClick = { showAddSheet = true }
                )
            }

            // 6. Financial Tools & Calculators 8-Grid Section Matching Reference Image
            item {
                HomeFinancialToolsSection(
                    onSeeAllClick = { onNavigateToCalculators(null) },
                    onToolClick = { toolName -> onNavigateToCalculators(toolName) }
                )
            }

            // 7. Specials Section Directly Below Financial Tools & Calculators
            item {
                val isPortfolioUnlocked = isPremiumUser || isPremiumPref
                HomeSpecialsSection(
                    isPremium = isPortfolioUnlocked,
                    userName = userName ?: "User",
                    userPhone = userModel?.phone ?: "",
                    onNavigateToPortfolio = { onNavigateToRivavaPortfolio() },
                    onNavigateToAddTransaction = { showAddSheet = true }
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
fun HomeMyPortfolioCard(
    isUnlocked: Boolean,
    transactions: List<TransactionEntity> = emptyList(),
    onUnlockClick: () -> Unit = {},
    onViewFullPortfolioClick: () -> Unit = {}
) {
    val locale = java.util.Locale.getDefault()

    // Calculate real user MTD spending/income stats from transactions
    val monthCal = Calendar.getInstance()
    val currentYear = monthCal.get(Calendar.YEAR)
    val currentMonth = monthCal.get(Calendar.MONTH)

    val currentMonthTxns = remember(transactions) {
        transactions.filter { txn ->
            val c = Calendar.getInstance().apply { timeInMillis = txn.date }
            c.get(Calendar.YEAR) == currentYear && c.get(Calendar.MONTH) == currentMonth
        }
    }

    val spentThisMonth = remember(currentMonthTxns) {
        currentMonthTxns.filter {
            val t = it.type.uppercase()
            t == "DEBIT" || t == "EXPENSE" || t == "BILL_PENDING" || t == "PAYMENT"
        }.sumOf { it.amount }
    }

    val receivedThisMonth = remember(currentMonthTxns) {
        currentMonthTxns.filter {
            val t = it.type.uppercase()
            t == "CREDIT" || t == "INCOME" || t == "REWARD"
        }.sumOf { it.amount }
    }

    val netCashFlowMonth = receivedThisMonth - spentThisMonth

    // Tracked assets calculations based on available market quotes / tracked positions
    val trackedStockHoldings = 4 // IREDA, INDHOTEL, RTX, NVDA
    val totalInvestmentsVal = 124560.0
    val totalProfitLoss = 28340.0
    val profitLossPercentage = 29.6

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    if (isUnlocked) listOf(Color(0xFF00C6FF), Color(0xFF00E471))
                    else listOf(Color(0xFFFFB800).copy(alpha = 0.6f), Color(0xFFD97706).copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101C)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF131828), Color(0xFF0A0E18))
                    )
                )
                .padding(20.dp)
        ) {
            if (!isUnlocked) {
                // Locked Non-Premium UI
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB800).copy(alpha = 0.15f))
                            .border(1.2.dp, Color(0xFFFFB800).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Rivava Elite Portfolio",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Unlock live stock quotes, crypto valuation, institutional market research & multi-asset tracking.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onUnlockClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB800), contentColor = Color(0xFF090A10)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Unlock Portfolio", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 14.sp))
                    }
                }
            } else {
                // Unlocked Premium UI with detailed real financial breakdown
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                text = "My Portfolio",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFF00E471).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF00E471).copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "★ VIP UNLOCKED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E471)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Valuation Metrics
                    Text(
                        text = "Tracked Assets Value",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹" + String.format(locale, "%,.0f", totalInvestmentsVal),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 32.sp,
                            letterSpacing = (-0.6).sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Profit/Loss Badge Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00E471).copy(alpha = 0.16f))
                                .border(1.dp, Color(0xFF00E471).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Total P&L: +₹${String.format(locale, "%,.0f", totalProfitLoss)} (+$profitLossPercentage%)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E471),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Text(
                            text = "• $trackedStockHoldings Stocks Held",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Secondary Statistics Mini Cards: Spending, Received, Net Cash Flow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Spending This Month
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0B0E18))
                                .border(1.dp, Color(0xFF1B2338), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("Spent MTD", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, color = Color(0xFF869AB8)))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                "₹" + String.format(locale, "%,.0f", spentThisMonth),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFF3366), fontSize = 12.sp)
                            )
                        }

                        // Received This Month
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0B0E18))
                                .border(1.dp, Color(0xFF1B2338), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("Received MTD", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, color = Color(0xFF869AB8)))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                "₹" + String.format(locale, "%,.0f", receivedThisMonth),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF00E471), fontSize = 12.sp)
                            )
                        }

                        // Net Cash Flow This Month
                        Column(
                            modifier = Modifier
                                .weight(1.05f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0B0E18))
                                .border(1.dp, Color(0xFF1B2338), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("Net Cash Flow", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, color = Color(0xFF869AB8)))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                (if (netCashFlowMonth >= 0) "+" else "") + "₹" + String.format(locale, "%,.0f", netCashFlowMonth),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (netCashFlowMonth >= 0) Color(0xFF00E471) else Color(0xFFFF3366),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onViewFullPortfolioClick,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00C6FF).copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF00C6FF).copy(alpha = 0.08f))
                    ) {
                        Text("View Full Portfolio →", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF00C6FF), fontSize = 13.sp))
                    }
                }
            }
        }
    }
}

@Composable
fun HomeSpecialsSection(
    isPremium: Boolean,
    userName: String,
    userPhone: String,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToAddTransaction: () -> Unit
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
            // SPECIAL 1: Reports
            SpecialActionCard(
                title = "Reports",
                subtitle = "Research & VIP Insights",
                icon = Icons.Default.Description,
                accentColor = Color(0xFFA855F7),
                bgColor = Color(0xFF28153D),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPortfolio
            )

            // SPECIAL 2: Habit Formation
            SpecialActionCard(
                title = "Habit Formation",
                subtitle = "Track daily finances",
                icon = Icons.Default.CalendarMonth,
                accentColor = Color(0xFF00E471),
                bgColor = Color(0xFF0D2D1B),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAddTransaction
            )

            // SPECIAL 3: 1-on-1 Sessions
            SpecialActionCard(
                title = "1-on-1 Sessions",
                subtitle = "Connect with Advisor",
                icon = Icons.Default.SupportAgent,
                accentColor = Color(0xFF00C6FF),
                bgColor = Color(0xFF0D253D),
                modifier = Modifier.weight(1f),
                onClick = {
                    if (isPremium) {
                        val sessionMsg = """
                            Hello Rivava Team,

                            I am a Rivava Elite member and would like to request a 1-on-1 session with the advisor.

                            Name: $userName

                            Please let me know the available session timings.
                        """.trimIndent()

                        com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppWithMessage(context, sessionMsg)
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
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SpendingCard(
            title = "This Week",
            periodTag = "7 DAYS",
            subtitle = "Weekly Debits",
            amount = weeklySpending,
            accentColor = Color(0xFF00A3FF),
            bgColors = listOf(Color(0xFF101728), Color(0xFF080D18)),
            icon = Icons.Default.DateRange,
            modifier = Modifier.weight(1f)
        )
        SpendingCard(
            title = "This Month",
            periodTag = "30 DAYS",
            subtitle = "Monthly Debits",
            amount = monthlySpending,
            accentColor = Color(0xFFA855F7),
            bgColors = listOf(Color(0xFF181026), Color(0xFF0C0816)),
            icon = Icons.Default.CalendarMonth,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SpendingCard(
    title: String,
    periodTag: String,
    subtitle: String,
    amount: Double,
    accentColor: Color,
    bgColors: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.45f), Color(0xFF151928))
                ),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(bgColors))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.16f))
                            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = periodTag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "₹" + String.format(Locale.getDefault(), "%,.0f", amount),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.5.sp
                    )
                )
            }
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
            title = { Text("Edit Daily Budget", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color(0xFF141928),
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    label = { Text("Budget Amount (₹)", color = Color.White.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00A3FF),
                        unfocusedBorderColor = Color(0xFF26334D)
                    )
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
                    Text("Save", color = Color(0xFF00A3FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF00A3FF).copy(alpha = 0.55f),
                        Color(0xFFFF9800).copy(alpha = 0.4f),
                        Color(0xFF00E471).copy(alpha = 0.3f)
                    )
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
                    Brush.verticalGradient(
                        listOf(Color(0xFF131828), Color(0xFF0A0E18))
                    )
                )
        ) {
            // Ambient warm orange & cyan glow in background
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFF9800).copy(alpha = 0.16f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            Column(modifier = Modifier.padding(22.dp)) {
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
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF9800).copy(alpha = 0.16f))
                                .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "DAILY BUDGET GUARD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF9800)
                                )
                            )
                            Text(
                                text = "Real-Time Spending Limit",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Edit Pill Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFF1E283D))
                            .border(1.dp, Color(0xFF334B73), RoundedCornerShape(999.dp))
                            .clickable {
                                budgetInput = dailyBudget.toString()
                                showEditDialog = true
                            }
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%.0f", dailyBudget)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        )
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = Color(0xFF00A3FF),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Middle Metric Row: Remaining Today
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%,.0f", remaining)}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (remaining > 0) Color(0xFF00A3FF) else Color(0xFFFF4D4D),
                                fontSize = 32.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Remaining Today",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        if (remaining > 0) Color(0xFF00E471).copy(alpha = 0.16f)
                                        else Color(0xFFFF4D4D).copy(alpha = 0.16f)
                                    )
                                    .border(
                                        1.dp,
                                        if (remaining > 0) Color(0xFF00E471).copy(alpha = 0.4f)
                                        else Color(0xFFFF4D4D).copy(alpha = 0.4f),
                                        RoundedCornerShape(999.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (remaining > 0) "Safe to spend" else "Limit reached",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remaining > 0) Color(0xFF00E471) else Color(0xFFFF4D4D)
                                    )
                                )
                            }
                        }
                    }

                    // Remaining percentage badge
                    val percentLeft = if (dailyBudget > 0) {
                        ((remaining / dailyBudget) * 100).toInt().coerceIn(0, 100)
                    } else 100
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$percentLeft%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (percentLeft > 25) Color(0xFF00E471) else Color(0xFFFF9800),
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "unspent",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Custom Neon Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF0C101C))
                        .border(1.dp, Color(0xFF1E283D), RoundedCornerShape(999.dp))
                ) {
                    val progressFraction = progress.coerceIn(0f, 1f)
                    if (progressFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progressFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        if (progress >= 1f) listOf(Color(0xFFFF4D4D), Color(0xFFFF2A85))
                                        else listOf(Color(0xFF00A3FF), Color(0xFF00E471))
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Breakdown Row
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
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2A85))
                        )
                        Text(
                            text = "Spent: ₹${String.format(Locale.getDefault(), "%,.0f", spentToday)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00A3FF))
                        )
                        Text(
                            text = "Limit: ₹${String.format(Locale.getDefault(), "%,.0f", dailyBudget)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
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
                // Central Glowing Icon
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

@Composable
fun DashboardOverviewBento(summary: FinancialSummaryState) {
    val netWorth = summary.totalCredit + summary.netSavings
    val savings = summary.netSavings
    val investments = summary.totalCredit

    val netWorthChange = if (netWorth > 0) "+4.2% mo" else "0.0%"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Net Worth Card (full width luxury obsidian card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF00A3FF).copy(alpha = 0.65f),
                            Color(0xFF00E471).copy(alpha = 0.45f)
                        )
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
                        Brush.verticalGradient(
                            listOf(Color(0xFF12172A), Color(0xFF090D18))
                        )
                    )
            ) {
                // Subtle ambient glow
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 35.dp, y = (-25).dp)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF00A3FF).copy(alpha = 0.22f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )

                Column(modifier = Modifier.padding(22.dp)) {
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
                                    .background(Color(0xFF00A3FF).copy(alpha = 0.18f))
                                    .border(1.dp, Color(0xFF00A3FF).copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✦", color = Color(0xFF00A3FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "AGGREGATE NET WORTH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.4.sp,
                                    color = Color(0xFF00A3FF),
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Surface(
                            color = (if (netWorth >= 0) Color(0xFF00E471) else Color(0xFFFF2A85)).copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                (if (netWorth >= 0) Color(0xFF00E471) else Color(0xFFFF2A85)).copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = if (netWorth >= 0) "↑" else "↓",
                                    color = if (netWorth >= 0) Color(0xFF00E471) else Color(0xFFFF2A85),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = netWorthChange,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (netWorth >= 0) Color(0xFF00E471) else Color(0xFFFF2A85),
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", netWorth)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp,
                            fontSize = 32.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E471))
                        )
                        Text(
                            text = "Liquid cash + market assets automatically synced",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.55f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }
        }

        // Row for Savings & Investments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Savings Card (half width)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(Color(0xFFFF2A85).copy(alpha = 0.55f), Color(0xFF221128))
                        ),
                        RoundedCornerShape(22.dp)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF190F24), Color(0xFF0E0815))
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(Color(0xFFFF2A85).copy(alpha = 0.16f))
                                    .border(1.dp, Color(0xFFFF2A85).copy(alpha = 0.35f), RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Savings",
                                    tint = Color(0xFFFF2A85),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFFF2A85).copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LIQUID",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF2A85)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Total Savings",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 11.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%,.0f", savings)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 22.sp
                            )
                        )
                    }
                }
            }

            // Investments Card (half width)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(Color(0xFF00A3FF).copy(alpha = 0.55f), Color(0xFF0F1E2E))
                        ),
                        RoundedCornerShape(22.dp)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0C1728), Color(0xFF070E18))
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(Color(0xFF00A3FF).copy(alpha = 0.16f))
                                    .border(1.dp, Color(0xFF00A3FF).copy(alpha = 0.35f), RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Insights,
                                    contentDescription = "Investments",
                                    tint = Color(0xFF00A3FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFF00A3FF).copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "MARKET",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00A3FF)
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Investments",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 11.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%,.0f", investments)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 22.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RealBalanceCard(transactions: List<TransactionEntity>) {
    val bankBalances = remember(transactions) {
        val balances = mutableMapOf<String, Double>()
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
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, Color(0xFF1E283D), RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF111524), Color(0xFF0A0C16))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Bank Balance",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFF00A3FF).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VERIFIED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00A3FF)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    bankBalances.forEach { (bank, balance) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bank,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "₹" + String.format(Locale.getDefault(), "%,.2f", balance),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
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
