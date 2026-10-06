package com.rivavafi.universal.ui.profile

import androidx.compose.foundation.background
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.layout.ContentScale
import com.rivavafi.universal.ui.home.HomeViewModel
import java.io.File
import java.io.FileOutputStream
import com.rivavafi.universal.ui.theme.EmeraldGreen
import com.rivavafi.universal.ui.theme.PremiumGradientStart
import com.rivavafi.universal.ui.theme.glassMorphism
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import com.rivavafi.universal.ui.portfolio.StockViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.rivavafi.universal.ui.theme.VibrantRed
import com.rivavafi.universal.ui.theme.PrimarySky
import com.google.firebase.auth.FirebaseAuth
import com.rivavafi.universal.utils.PrefsManager
import com.rivavafi.universal.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    premiumViewModel: com.rivavafi.universal.ui.portfolio.PremiumViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelpCenter: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    stockViewModel: StockViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val summary by viewModel.summary.collectAsState()
    val isPremiumUser by viewModel.isPremiumUser.collectAsState()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val context = LocalContext.current

    val profileState by profileViewModel.profileState.collectAsState()
    val userModel = profileState.userModel
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

    val userName = userModel?.name?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }
        ?: "User"
    val userPhone = userModel?.phone?.takeIf { it.isNotBlank() }
        ?: userModel?.phoneno?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.phoneNumber?.takeIf { it.isNotBlank() }
        ?: "No Phone Number"
    val username = userModel?.username?.takeIf { it.isNotBlank() }
        ?: userName.lowercase().replace(" ", "_")
    val profileImageUri = userModel?.profileImage?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.photoUrl?.toString()
    val userEmail = userModel?.email?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.email?.takeIf { it.isNotBlank() }
        ?: "No Email"
    val memberSince = remember(userModel?.createdAt, firebaseUser?.metadata?.creationTimestamp) {
        val ts = userModel?.createdAt ?: firebaseUser?.metadata?.creationTimestamp ?: System.currentTimeMillis()
        try {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date(ts))
        } catch (e: Exception) {
            "Active Member"
        }
    }
    val preference = userModel?.preference?.takeIf { it.isNotBlank() } ?: "No"
    val isPremiumModel = userModel?.premiumStatus == true
    val coroutineScope = rememberCoroutineScope()
    var showLogsDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var screenshotsAllowed by remember { mutableStateOf(false) }
    var showEditPhoneDialog by remember { mutableStateOf(false) }
    var showEditUsernameDialog by remember { mutableStateOf(false) }
    val stockStates by stockViewModel.stockStates.collectAsState()

    val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", android.content.Context.MODE_PRIVATE)
    val premiumState by premiumViewModel.premiumState.collectAsState()
    val isPremiumPref = premiumState.status == com.rivavafi.universal.data.repository.EntitlementStatus.UNLOCKED
    val finalIsPremium = isPremiumModel || isPremiumPref || isPremiumUser

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { selectedUri ->
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                            if (originalBitmap != null) {
                                // Resize aggressively to keep under 1MB Firestore limit
                                val maxDim = 200f
                                val scale = Math.min(maxDim / originalBitmap.width, maxDim / originalBitmap.height)
                                val resizedBitmap = if (scale < 1f) {
                                    android.graphics.Bitmap.createScaledBitmap(
                                        originalBitmap,
                                        (originalBitmap.width * scale).toInt(),
                                        (originalBitmap.height * scale).toInt(),
                                        true
                                    )
                                } else {
                                    originalBitmap
                                }

                                val outputStream = java.io.ByteArrayOutputStream()
                                resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, outputStream)
                                val bytes = outputStream.toByteArray()

                                val base64Image = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                val dataUri = "data:image/jpeg;base64,$base64Image"

                                val file = File(context.filesDir, "profile_image_${System.currentTimeMillis()}.jpg")
                                FileOutputStream(file).use { fileOut ->
                                    fileOut.write(bytes)
                                }
                                launch(kotlinx.coroutines.Dispatchers.Main) {
                                    viewModel.setProfileImageUri(file.absolutePath)
                                    profileViewModel.updateProfileImage(dataUri)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 140.dp), // Provide enough bottom padding for the floating nav bar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Component
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
            ) {
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    com.rivavafi.universal.ui.theme.RivavaCyan,
                                    com.rivavafi.universal.ui.theme.RivavaPink,
                                    com.rivavafi.universal.ui.theme.RivavaLime,
                                    com.rivavafi.universal.ui.theme.RivavaCyan
                                )
                            ),
                            CircleShape
                        )
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                com.rivavafi.universal.ui.theme.DarkCardBg,
                                CircleShape
                            )
                            .border(3.dp, Color(0xFF08080B), CircleShape),
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
                                androidx.compose.foundation.Image(
                                    bitmap = decodedBitmap.asImageBitmap(),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                coil.compose.AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "U"
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 54.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .offset(x = (-8).dp, y = (-8).dp)
                        .size(32.dp)
                        .background(com.rivavafi.universal.ui.theme.RivavaCyan, CircleShape)
                        .shadow(16.dp, CircleShape, ambientColor = com.rivavafi.universal.ui.theme.RivavaCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF08080B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    fontSize = 22.sp
                ),
                color = Color.White
            )
            if (userEmail != "No Email") {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Membership Tier Pill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (finalIsPremium) Color(0xFFFFD700).copy(alpha = 0.12f)
                        else Color(0xFF00C6FF).copy(alpha = 0.12f)
                    )
                    .border(
                        1.dp,
                        if (finalIsPremium) Color(0xFFFFD700).copy(alpha = 0.35f)
                        else Color(0xFF00C6FF).copy(alpha = 0.35f),
                        RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (finalIsPremium) Icons.Default.WorkspacePremium else Icons.Default.Verified,
                        contentDescription = null,
                        tint = if (finalIsPremium) Color(0xFFFFD700) else Color(0xFF00C6FF),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (finalIsPremium) "VIP ELITE MEMBER" else "VERIFIED INVESTOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp,
                            color = if (finalIsPremium) Color(0xFFFFD700) else Color(0xFF00C6FF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Profile Info Obsidian Card with Luxury Squircle Badges
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0E1322))
                    .border(1.dp, Color(0xFF1E283D), RoundedCornerShape(24.dp))
            ) {
                // Name Row
                ProfileInfoRow(
                    label = "NAME",
                    value = userName,
                    icon = Icons.Default.Person,
                    iconTint = Color(0xFF00C6FF),
                    onClick = { showEditNameDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = Color(0xFF1A2234))

                // Username Row
                ProfileInfoRow(
                    label = "USERNAME",
                    value = "@$username",
                    icon = Icons.Default.AlternateEmail,
                    iconTint = Color(0xFFA855F7),
                    onClick = { showEditUsernameDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = Color(0xFF1A2234))

                // Email Row
                ProfileInfoRow(
                    label = "EMAIL",
                    value = userEmail,
                    icon = Icons.Default.Email,
                    iconTint = Color(0xFF38BDF8)
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = Color(0xFF1A2234))

                // Phone Row
                ProfileInfoRow(
                    label = "PHONE",
                    value = userPhone,
                    icon = Icons.Default.Phone,
                    iconTint = Color(0xFF00E471),
                    onClick = { showEditPhoneDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = Color(0xFF1A2234))

                // Member Since Row
                ProfileInfoRow(
                    label = "JOINED",
                    value = memberSince,
                    icon = Icons.Default.CalendarMonth,
                    iconTint = Color(0xFFFFB800)
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = Color(0xFF1A2234))

                // Preference Row
                ProfileInfoRow(
                    label = "PREFERENCE",
                    value = preference,
                    icon = Icons.Default.Tune,
                    iconTint = Color(0xFF94A3B8)
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = Color(0xFF1A2234))

                // Status Row
                ProfileInfoRow(
                    label = "MEMBERSHIP STATUS",
                    value = if (finalIsPremium) "VIP Elite" else "Standard Free",
                    icon = Icons.Default.WorkspacePremium,
                    iconTint = if (finalIsPremium) Color(0xFFFFD700) else Color(0xFF64748B),
                    trailingContent = {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alphaPulse by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )
                        val statusColor = if (finalIsPremium) Color(0xFFFFD700) else Color(0xFF00E471)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .alpha(alphaPulse)
                                    .background(statusColor, CircleShape)
                            )
                            Text(
                                text = if (finalIsPremium) "Active VIP" else "Active Free",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = statusColor
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Settings Button with Modern Glass Styling
            Surface(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0E1322),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E283D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF00C6FF).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFF00C6FF).copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF00C6FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "App Settings",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Security, biometric lock & preferences",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp
                                ),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Navigate",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bento Grid Stats Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Assets Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "TOTAL ASSETS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = currencyFormatter.format(summary.totalCredit + summary.netSavings).substringBefore("."),
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = "." + currencyFormatter.format(summary.totalCredit + summary.netSavings).substringAfter(".", "00"),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = "Trending Up",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "+12.5% this month",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                // Row for Savings and Invested
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Savings Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(24.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "Savings",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "SAVINGS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormatter.format(summary.netSavings).substringBefore("."),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Invested Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(24.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Invested",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "INVESTED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormatter.format(summary.totalDebit).substringBefore("."),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Actions List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionItem(
                    icon = Icons.Default.Security,
                    title = "Security & Privacy",
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.rivava.in/privacy-policy.html"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Unable to open link", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                QuickActionItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help Center",
                    tint = MaterialTheme.colorScheme.tertiary,
                    onClick = onNavigateToHelpCenter
                )
                QuickActionItem(
                    icon = Icons.Default.OpenInNew,
                    title = "Website",
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.rivava.in"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Unable to open website", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )


                QuickActionItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Logout",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = {
                        viewModel.logout()
                        var ctx = context
                        while (ctx is android.content.ContextWrapper) {
                            if (ctx is android.app.Activity) break
                            ctx = ctx.baseContext
                        }
                        val intent = android.content.Intent(context, com.rivavafi.universal.ui.auth.AuthActivity::class.java)
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (ctx as? android.app.Activity)?.finish()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showEditNameDialog) {
            var newName by remember { mutableStateOf(userName ?: "") }
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                title = { Text("Edit Name") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true,
                        label = { Text("Your Name") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newName.isNotBlank()) {
                            profileViewModel.updateName(newName.trim())
                        }
                        showEditNameDialog = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEditUsernameDialog) {
            var newUsername by remember { mutableStateOf(username ?: "") }
            AlertDialog(
                onDismissRequest = { showEditUsernameDialog = false },
                title = { Text("Edit Username") },
                text = {
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { input ->
                            newUsername = input.lowercase().replace(Regex("[^a-z0-9_]") , "")
                        },
                        singleLine = true,
                        prefix = { Text("@") },
                        label = { Text("Username") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newUsername.isNotBlank()) {
                            profileViewModel.updateUsername(newUsername.trim('_'))
                        }
                        showEditUsernameDialog = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditUsernameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEditPhoneDialog) {
            var newPhone by remember { mutableStateOf(userPhone ?: "") }
            AlertDialog(
                onDismissRequest = { showEditPhoneDialog = false },
                title = { Text("Edit Phone") },
                text = {
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        singleLine = true,
                        label = { Text("Your Phone") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newPhone.isNotBlank()) {
                            profileViewModel.updatePhone(newPhone.trim())
                        }
                        showEditPhoneDialog = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditPhoneDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showLogsDialog) {
            AlertDialog(
                onDismissRequest = { showLogsDialog = false },
                title = { Text("API Diagnostic Logs") },
                text = {
                    LazyColumn {
                        items(stockStates.entries.toList()) { (symbol, state) ->
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                Text(text = "Symbol: $symbol", fontWeight = FontWeight.Bold)
                                Text(text = "Source: ${state.source.name}")
                                Text(text = "Error: ${state.error ?: "None"}", color = if (state.error != null) VibrantRed else PrimarySky)
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val view = androidx.compose.ui.platform.LocalView.current
                        TextButton(onClick = {
                            try {
                                val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(bitmap)
                                view.draw(canvas)
                                val file = java.io.File(context.cacheDir, "screenshot_${System.currentTimeMillis()}.png")
                                val fos = java.io.FileOutputStream(file)
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
                                fos.close()
                                android.widget.Toast.makeText(context, "Saved to ${file.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Capture failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Capture Screenshot")
                        }

                        TextButton(onClick = { showLogsDialog = false }) {
                            Text("Close")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit = { }
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(tint.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(iconTint.copy(alpha = 0.12f))
                    .border(1.dp, iconTint.copy(alpha = 0.25f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.sp
                    ),
                    color = Color(0xFF64748B)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = Color.White
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit $label",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
