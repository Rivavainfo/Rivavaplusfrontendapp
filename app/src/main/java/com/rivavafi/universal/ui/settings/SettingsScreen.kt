package com.rivavafi.universal.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.rivavafi.universal.R
import com.rivavafi.universal.sms.SmsScanResult
import com.rivavafi.universal.ui.theme.glassMorphism
import java.text.SimpleDateFormat
import java.util.*

enum class SmsPermissionState {
    GRANTED,
    NEVER_REQUESTED,
    DENIED_REQUESTABLE,
    BLOCKED_PERMANENTLY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    profileViewModel: com.rivavafi.universal.ui.profile.ProfileViewModel = hiltViewModel(),
    onRestartApp: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val isLoading by viewModel.isLoading.collectAsState()
    val showSmsDetails by viewModel.showSmsDetails.collectAsState()
    val terminologyMode by viewModel.terminologyMode.collectAsState()
    val smsTrackingMode by viewModel.smsTrackingMode.collectAsState()
    val lastScanResult by viewModel.lastScanResult.collectAsState()
    val lastSmsScanDate by viewModel.lastSmsScanDate.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()

    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userModel = profileState.userModel

    val displayName = userModel?.name?.takeIf { it.isNotBlank() } ?: currentUser?.displayName ?: "Rivava User"
    val userEmail = userModel?.email?.takeIf { it.isNotBlank() } ?: currentUser?.email ?: "Not connected"

    val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
    val isPremium = prefs.getBoolean("isPremium", false)

    // Permission state calculation
    fun checkPermissionState(): SmsPermissionState {
        val hasRead = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val hasReceive = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        if (hasRead && hasReceive) {
            return SmsPermissionState.GRANTED
        }

        val requestedBefore = prefs.getBoolean("sms_permission_requested_before", false)
        if (!requestedBefore) {
            return SmsPermissionState.NEVER_REQUESTED
        }

        val showRationaleRead = activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_SMS) } ?: false
        val showRationaleReceive = activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.RECEIVE_SMS) } ?: false

        return if (showRationaleRead || showRationaleReceive) {
            SmsPermissionState.DENIED_REQUESTABLE
        } else {
            SmsPermissionState.BLOCKED_PERMANENTLY
        }
    }

    var permissionState by remember { mutableStateOf(checkPermissionState()) }

    // Dialog Flow States
    var showExplanationStepDialog by remember { mutableStateOf(false) }
    var showConfirmationStepDialog by remember { mutableStateOf(false) }
    var showModeSelectionStepDialog by remember { mutableStateOf(false) }
    var showBlockedSettingsDialog by remember { mutableStateOf(false) }
    var showScanResultDialog by remember { mutableStateOf<SmsScanResult?>(null) }
    var pendingSelectedMode by remember { mutableStateOf("BOTH") }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDeleteAccountStep1Dialog by remember { mutableStateOf(false) }
    var showDeleteAccountStep2Dialog by remember { mutableStateOf(false) }
    var deleteConfirmationText by remember { mutableStateOf("") }
    var isDeleting by remember { mutableStateOf(false) }

    // Recheck permission state on Resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionState = checkPermissionState()
                if (permissionState == SmsPermissionState.GRANTED && smsTrackingMode == "OFF" && showBlockedSettingsDialog) {
                    showBlockedSettingsDialog = false
                    showConfirmationStepDialog = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        prefs.edit().putBoolean("sms_permission_requested_before", true).apply()
        val granted = permissions[Manifest.permission.READ_SMS] == true && permissions[Manifest.permission.RECEIVE_SMS] == true
        permissionState = checkPermissionState()

        if (granted) {
            showConfirmationStepDialog = true
        } else {
            viewModel.setSmsTrackingMode("OFF")
            if (permissionState == SmsPermissionState.BLOCKED_PERMANENTLY) {
                showBlockedSettingsDialog = true
            } else {
                Toast.makeText(context, "SMS permission is required to enable automated tracking.", Toast.LENGTH_LONG).show()
            }
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportCsv(context, uri) { result ->
                result.onSuccess {
                    Toast.makeText(context, "Exported successfully", Toast.LENGTH_LONG).show()
                }.onFailure { e ->
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.importCsv(context, uri) { result ->
                result.onSuccess { count ->
                    Toast.makeText(context, "Successfully imported $count transactions", Toast.LENGTH_LONG).show()
                }.onFailure { e ->
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun startEnableSmsFlow(targetMode: String) {
        pendingSelectedMode = targetMode
        permissionState = checkPermissionState()

        if (permissionState == SmsPermissionState.GRANTED) {
            showConfirmationStepDialog = true
        } else if (permissionState == SmsPermissionState.BLOCKED_PERMANENTLY) {
            showBlockedSettingsDialog = true
        } else {
            showExplanationStepDialog = true
        }
    }

    Scaffold(
        containerColor = Color(0xFF06070B),
        modifier = Modifier.systemBarsPadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Title
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 28.sp
                )
            )

            // TOP SECTION: User Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, Color(0xFF1E283D), RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101524))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val initial = displayName.firstOrNull()?.uppercase() ?: "U"
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00A3FF), Color(0xFF0066FF))
                                )
                            )
                            .border(1.5.dp, Color(0xFF60A5FA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 22.sp
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 17.sp
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF869AB8),
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (isPremium) Color(0xFF00E471).copy(alpha = 0.16f)
                                else Color(0xFF00A3FF).copy(alpha = 0.16f)
                            )
                            .border(
                                1.dp,
                                if (isPremium) Color(0xFF00E471).copy(alpha = 0.4f)
                                else Color(0xFF00A3FF).copy(alpha = 0.4f),
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isPremium) "ELITE VIP" else "STANDARD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = if (isPremium) Color(0xFF00E471) else Color(0xFF00A3FF)
                            )
                        )
                    }
                }
            }

            // FINANCIAL TRACKING SECTION
            SettingsGroupCard(title = "FINANCIAL TRACKING") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Header & Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(Color(0xFF1B2338)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = null,
                                    tint = Color(0xFF00A3FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    "SMS Tracking Options",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Detect debit/credit transactions automatically from SMS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF869AB8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = smsTrackingMode != "OFF",
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    startEnableSmsFlow("BOTH")
                                } else {
                                    viewModel.setSmsTrackingMode("OFF")
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00A3FF)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Permission Status Badge Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B2338).copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Permission Status",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF869AB8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        val statusLabel: String
                        val statusBg: Color
                        val statusText: Color

                        when (permissionState) {
                            SmsPermissionState.GRANTED -> {
                                statusLabel = "Permission Granted"
                                statusBg = Color(0xFF00E471).copy(alpha = 0.15f)
                                statusText = Color(0xFF00E471)
                            }
                            SmsPermissionState.NEVER_REQUESTED, SmsPermissionState.DENIED_REQUESTABLE -> {
                                statusLabel = "Permission Required"
                                statusBg = Color(0xFFFF9800).copy(alpha = 0.15f)
                                statusText = Color(0xFFFF9800)
                            }
                            SmsPermissionState.BLOCKED_PERMANENTLY -> {
                                statusLabel = "Blocked — Open Settings"
                                statusBg = Color(0xFFFF3366).copy(alpha = 0.15f)
                                statusText = Color(0xFFFF3366)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusBg)
                                .clickable(enabled = permissionState == SmsPermissionState.BLOCKED_PERMANENTLY) {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = statusText
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Track Transaction Types",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00A3FF),
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val trackingModes = listOf(
                        "BOTH" to "Both (Credit & Debit)",
                        "CREDIT_ONLY" to "Credit Only",
                        "DEBIT_ONLY" to "Debit Only",
                        "OFF" to "Off"
                    )

                    trackingModes.forEach { (modeKey, label) ->
                        val isSelected = smsTrackingMode == modeKey || (smsTrackingMode == "ALL" && modeKey == "BOTH")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(0xFF00A3FF).copy(alpha = 0.18f)
                                    else Color(0xFF1B2338).copy(alpha = 0.6f)
                                )
                                .clickable {
                                    if (modeKey != "OFF") {
                                        if (permissionState == SmsPermissionState.GRANTED) {
                                            viewModel.setSmsTrackingMode(modeKey)
                                            viewModel.rescanSms(context, modeKey) { res ->
                                                showScanResultDialog = res
                                            }
                                        } else {
                                            startEnableSmsFlow(modeKey)
                                        }
                                    } else {
                                        viewModel.setSmsTrackingMode("OFF")
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (modeKey != "OFF") {
                                        if (permissionState == SmsPermissionState.GRANTED) {
                                            viewModel.setSmsTrackingMode(modeKey)
                                            viewModel.rescanSms(context, modeKey) { res ->
                                                showScanResultDialog = res
                                            }
                                        } else {
                                            startEnableSmsFlow(modeKey)
                                        }
                                    } else {
                                        viewModel.setSmsTrackingMode("OFF")
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A3FF))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) Color.White else Color(0xFFC0CCDC),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scan Existing SMS Button & Timestamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Last Successful Scan",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF869AB8),
                                    fontSize = 11.sp
                                )
                            )
                            val scanText = if (lastSmsScanDate > 0) {
                                SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(lastSmsScanDate))
                            } else {
                                "Never scanned"
                            }
                            Text(
                                text = scanText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Button(
                            onClick = {
                                if (permissionState == SmsPermissionState.GRANTED && smsTrackingMode != "OFF") {
                                    viewModel.rescanSms(context) { res ->
                                        showScanResultDialog = res
                                    }
                                } else {
                                    startEnableSmsFlow(if (smsTrackingMode != "OFF") smsTrackingMode else "BOTH")
                                }
                            },
                            enabled = permissionState == SmsPermissionState.GRANTED && smsTrackingMode != "OFF",
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A3FF))
                        ) {
                            Text("Scan Existing SMS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tracking Info
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF00A3FF).copy(alpha = 0.08f))
                            .border(1.dp, Color(0xFF00A3FF).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Rivava parses financial transaction messages locally on your device to log expenses and income. Private SMS contents, OTPs, and personal messages are never stored or uploaded.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }
            }

            // PREFERENCES SECTION
            SettingsGroupCard(title = "PREFERENCES") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Transaction Details", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp))
                        Text("Display merchant, category & date", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF869AB8), fontSize = 11.sp))
                    }
                    Switch(
                        checked = showSmsDetails,
                        onCheckedChange = { viewModel.setShowSmsDetails(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF00A3FF))
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Credit/Debit Terminology", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp))
                        Text("Toggle formal banking terminology", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF869AB8), fontSize = 11.sp))
                    }
                    Switch(
                        checked = terminologyMode == "CREDIT_DEBIT",
                        onCheckedChange = { viewModel.toggleTerminology() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF00A3FF))
                    )
                }

                SettingsRowItem(
                    icon = Icons.Default.Download,
                    title = "Export Transactions (CSV)",
                    subtitle = "Offline backup of all logged transactions",
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                        csvExportLauncher.launch("Rivava_Backup_$timestamp.csv")
                    }
                )

                SettingsRowItem(
                    icon = Icons.Default.Upload,
                    title = "Import Transactions (CSV)",
                    subtitle = "Restore transactions from a backup CSV file",
                    onClick = {
                        csvImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "text/plain", "application/vnd.ms-excel", "*/*"))
                    }
                )
            }

            // SUPPORT SECTION
            SettingsGroupCard(title = "SUPPORT & LEGAL") {
                SettingsRowItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    title = "Chat with Advisor",
                    subtitle = "Connect directly with Rivava financial team on WhatsApp",
                    onClick = {
                        com.rivavafi.universal.utils.WhatsAppUtils.openWhatsAppWithMessage(
                            context = context,
                            customMessage = "Hello Rivava Team, I need assistance regarding my Rivava app."
                        )
                    }
                )

                SettingsRowItem(
                    icon = Icons.Default.HelpOutline,
                    title = "Help & Support Center",
                    subtitle = "FAQs & direct chat support",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://therivava.com/help"))
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )

                SettingsRowItem(
                    icon = Icons.Default.Shield,
                    title = "Privacy Policy",
                    subtitle = "Read about data protection & encryption",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.rivava.in/privacy-policy.html"))
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )

                SettingsRowItem(
                    icon = Icons.Default.Article,
                    title = "Terms & Conditions",
                    subtitle = "App usage & subscription terms",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://therivava.com/terms"))
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )
            }

            // DANGER ZONE & LOGOUT
            SettingsGroupCard(title = "ACCOUNT ACTIONS") {
                SettingsRowItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Logout",
                    subtitle = "Sign out from this device safely",
                    titleColor = Color(0xFFFF9800),
                    onClick = {
                        viewModel.logout()
                        onRestartApp()
                    }
                )

                SettingsRowItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Clear Transaction History",
                    subtitle = "Permanently clear logged transaction records",
                    titleColor = Color(0xFFFF3366),
                    onClick = { showClearDataDialog = true }
                )
            }

            // APP VERSION FOOTER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rivava Finance • Version ${com.rivavafi.universal.BuildConfig.VERSION_NAME} (${com.rivavafi.universal.BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                )
                Text(
                    text = "Encrypted Local Storage & Bank-Grade Security",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF475569), fontSize = 10.sp)
                )
            }
        }
    }

    // --- STEP 1: EXPLANATION DIALOG ---
    if (showExplanationStepDialog) {
        AlertDialog(
            onDismissRequest = { showExplanationStepDialog = false },
            title = { Text("Enable SMS Money Tracking?") },
            text = { Text("Allow Rivava to detect eligible financial transactions from bank and UPI SMS messages to automatically update your expense and income tracker.") },
            confirmButton = {
                TextButton(onClick = {
                    showExplanationStepDialog = false
                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS))
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExplanationStepDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    // --- STEP 3: MANDATORY CONFIRMATION DIALOG ---
    if (showConfirmationStepDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationStepDialog = false },
            title = { Text("Enable Automatic SMS Tracking?") },
            text = { Text("Rivava will scan eligible banking and payment SMS messages to detect your transactions and automatically update Track Your Money. Do you want to enable this feature?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmationStepDialog = false
                    showModeSelectionStepDialog = true
                }) {
                    Text("Enable Tracking")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationStepDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- STEP 4: CHOOSE TRANSACTION TYPE DIALOG ---
    if (showModeSelectionStepDialog) {
        var selectedModeInternal by remember { mutableStateOf(pendingSelectedMode) }
        AlertDialog(
            onDismissRequest = { showModeSelectionStepDialog = false },
            title = { Text("Select Transaction Types") },
            text = {
                Column {
                    Text("Choose which types of financial transactions to track:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    val modes = listOf(
                        "BOTH" to "Both Debit and Credit (Recommended)",
                        "DEBIT_ONLY" to "Debit Only (Expenses & UPI)",
                        "CREDIT_ONLY" to "Credit Only (Income & Deposits)"
                    )

                    modes.forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedModeInternal = mode }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedModeInternal == mode,
                                onClick = { selectedModeInternal = mode }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showModeSelectionStepDialog = false
                    viewModel.setSmsTrackingMode(selectedModeInternal)
                    viewModel.rescanSms(context, selectedModeInternal) { res ->
                        showScanResultDialog = res
                    }
                }) {
                    Text("Confirm & Scan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModeSelectionStepDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- BLOCKED SETTINGS DIALOG ---
    if (showBlockedSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedSettingsDialog = false },
            title = { Text("Permission Denied") },
            text = { Text("It looks like SMS permissions are permanently denied. Please go to App Settings and enable SMS permissions manually.") },
            confirmButton = {
                TextButton(onClick = {
                    showBlockedSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockedSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- SCAN RESULT COMPLETION DIALOG ---
    showScanResultDialog?.let { result ->
        AlertDialog(
            onDismissRequest = { showScanResultDialog = null },
            title = { Text("SMS Tracking Enabled") },
            text = {
                Column {
                    Text("Scan Summary:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Messages Examined: ${result.messagesExamined}", fontSize = 13.sp)
                    Text("• Eligible Detected: ${result.transactionsDetected}", fontSize = 13.sp)
                    Text("• New Transactions Imported: ${result.newTransactionsAdded}", fontSize = 13.sp)
                    Text("• Duplicates Skipped: ${result.duplicatesSkipped}", fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showScanResultDialog = null }) {
                    Text("Done")
                }
            }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Transaction History?") },
            text = { Text("This will permanently delete all your logged transactions. Your profile and portfolio access will remain intact.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearTransactionHistory()
                        showClearDataDialog = false
                        Toast.makeText(context, "Transaction history cleared.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteAccountStep1Dialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountStep1Dialog = false },
            title = { Text("Delete Account?") },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone and will delete all your local and cloud data, including your premium status.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountStep1Dialog = false
                        deleteConfirmationText = ""
                        showDeleteAccountStep2Dialog = true
                    }
                ) {
                    Text("Next", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountStep1Dialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    com.rivavafi.universal.ui.components.RivavaLoadingOverlay(isLoading = isLoading)

    if (showDeleteAccountStep2Dialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountStep2Dialog = false },
            title = { Text("Final Confirmation") },
            text = {
                Column {
                    Text("Type 'DELETE' to confirm.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deleteConfirmationText,
                        onValueChange = { deleteConfirmationText = it },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (deleteConfirmationText == "DELETE") {
                            isDeleting = true
                            viewModel.deleteAccount {
                                showDeleteAccountStep2Dialog = false
                                isDeleting = false
                                onRestartApp()
                            }
                        }
                    },
                    enabled = deleteConfirmationText == "DELETE" && !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Confirm", color = if (deleteConfirmationText == "DELETE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountStep2Dialog = false },
                    enabled = !isDeleting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsGroupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = Color(0xFF00A3FF),
                letterSpacing = 1.2.sp
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF1E283D), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101524))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0xFF1B2338)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (titleColor != Color.White) titleColor else Color(0xFF00A3FF),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF869AB8),
                    fontSize = 11.sp
                )
            )
        }

        Text(
            text = "→",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Bold
            )
        )
    }
}
