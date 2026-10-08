package com.rivavafi.universal.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Psychology

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.widget.Toast



import androidx.hilt.navigation.compose.hiltViewModel
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.rivavafi.universal.ui.theme.glassMorphism
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import com.rivavafi.universal.R

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Shield

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    profileViewModel: com.rivavafi.universal.ui.profile.ProfileViewModel = hiltViewModel(),
    onRestartApp: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val isLoading by viewModel.isLoading.collectAsState()
    val banksDetected by viewModel.banksDetected.collectAsState()
    val showSmsDetails by viewModel.showSmsDetails.collectAsState()
    val terminologyMode by viewModel.terminologyMode.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()

    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userModel = profileState.userModel

    val displayName = userModel?.name?.takeIf { it.isNotBlank() } ?: currentUser?.displayName ?: "Rivava User"
    val userEmail = userModel?.email?.takeIf { it.isNotBlank() } ?: currentUser?.email ?: "Not connected"
    val isGoogleOnly = currentUser?.providerData?.any { it.providerId == "google.com" } == true && currentUser.providerData.none { it.providerId == "password" }

    val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", android.content.Context.MODE_PRIVATE)
    val isPremium = prefs.getBoolean("isPremium", false)

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showSmsRationaleDialog by remember { mutableStateOf(false) }
    var showSmsSettingsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountStep1Dialog by remember { mutableStateOf(false) }
    var showDeleteAccountStep2Dialog by remember { mutableStateOf(false) }
    var deleteConfirmationText by remember { mutableStateOf("") }
    var isDeleting by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_SMS] == true && permissions[Manifest.permission.RECEIVE_SMS] == true
        if (!granted) {
            viewModel.setSmsTrackingMode("OFF")
            showSmsSettingsDialog = true
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportCsv(context, uri) { result ->
                result.onSuccess { path ->
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

            // ACCOUNT SECTION
            SettingsGroupCard(title = "ACCOUNT") {
                SettingsRowItem(
                    icon = Icons.Default.Person,
                    title = "Account Information",
                    subtitle = "View connected email & authentication type",
                    onClick = {
                        Toast.makeText(context, "Logged in as $userEmail", Toast.LENGTH_SHORT).show()
                    }
                )

                if (!isGoogleOnly) {
                    SettingsRowItem(
                        icon = Icons.Default.Lock,
                        title = "Password Settings",
                        subtitle = "Request password reset link",
                        onClick = {
                            if (!currentUser?.email.isNullOrBlank()) {
                                auth.sendPasswordResetEmail(currentUser!!.email!!)
                                Toast.makeText(context, "Password reset email sent to ${currentUser.email}", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
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
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
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
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://therivava.com/privacy"))
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
    if (showSmsRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showSmsRationaleDialog = false },
            title = { Text("SMS Permissions Needed") },
            text = { Text("To automatically read and track your transactions from SMS messages, Rivava needs access to read and receive SMS.") },
            confirmButton = {
                TextButton(onClick = {
                    showSmsRationaleDialog = false
                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS))
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showSmsSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSmsSettingsDialog = false },
            title = { Text("Permission Denied") },
            text = { Text("It looks like SMS permissions are permanently denied. Please go to App Settings and enable SMS permissions manually.") },
            confirmButton = {
                TextButton(onClick = {
                    showSmsSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmsSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
