package com.rivavafi.universal.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import com.google.firebase.auth.FirebaseAuth
import com.rivavafi.universal.HomeActivity
import com.rivavafi.universal.R
import com.rivavafi.universal.ui.theme.*
import androidx.compose.ui.draw.shadow
import com.rivavafi.universal.ui.theme.glassMorphism
import com.rivavafi.universal.ui.components.RivavaBrandDisplay
import com.rivavafi.universal.ui.components.RivavaGlowingLogo
import com.rivavafi.universal.ui.components.RivavaLoadingOverlay
import androidx.compose.ui.draw.alpha
import dagger.hilt.android.AndroidEntryPoint
import android.util.Patterns
import androidx.compose.foundation.shape.CircleShape
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        // We now rely on AuthViewModel's init block and LaunchedEffect(authState)
        // to handle the redirection safely after verifying backend status.

        handleIntent(intent)

        setContent {
            RivavaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmoledBlack
                ) {
                    AuthScreenContent(
                        viewModel = viewModel,
                        onLoginSuccess = { isNewUser -> goToHome(isNewUser) },
                        onNavigateToReset = { goToResetPassword() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val data = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            var mode = data.getQueryParameter("mode")
            var oobCode = data.getQueryParameter("oobCode")
            var token = data.getQueryParameter("token")
            var email = data.getQueryParameter("email")

            // Check if wrapped in dynamic link query parameter (e.g. link=...)
            val deepLink = data.getQueryParameter("link")
            if (!deepLink.isNullOrBlank()) {
                val parsedNested = android.net.Uri.parse(deepLink)
                if (mode.isNullOrBlank()) mode = parsedNested.getQueryParameter("mode")
                if (oobCode.isNullOrBlank()) oobCode = parsedNested.getQueryParameter("oobCode")
                if (token.isNullOrBlank()) token = parsedNested.getQueryParameter("token")
                if (email.isNullOrBlank()) email = parsedNested.getQueryParameter("email")
            }

            if (!token.isNullOrBlank() || !oobCode.isNullOrBlank()) {
                if (!token.isNullOrBlank() || mode == "resetPassword" || data.path?.contains("/reset") == true || data.toString().contains("resetPassword")) {
                    val resetIntent = Intent(this, SetNewPasswordActivity::class.java).apply {
                        putExtra("token", token)
                        putExtra("email", email)
                        putExtra("oobCode", oobCode)
                    }
                    startActivity(resetIntent)
                } else if (mode == "verifyEmail" || data.path?.contains("/verify") == true || data.toString().contains("verifyEmail")) {
                    if (!oobCode.isNullOrBlank()) {
                        viewModel.verifyEmailActionCode(oobCode) {
                            Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun goToHome(isNewUser: Boolean) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("isNewUser", isNewUser)
        }
        startActivity(intent)
        finish()
    }

    private fun goToResetPassword() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        startActivity(intent)
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun AuthScreenContent(
    viewModel: AuthViewModel,
    onLoginSuccess: (Boolean) -> Unit,
    onNavigateToReset: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val phoneAuthState by viewModel.phoneAuthState.collectAsState()
    val isNewUser by viewModel.isNewUser.collectAsState()
    val context = LocalContext.current

    // Automatically prompt user for all required runtime permissions on app launch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permission results received */ }

    LaunchedEffect(Unit) {
        val neededPermissions = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                android.content.pm.PackageManager.GET_PERMISSIONS
            )
            val requested = packageInfo.requestedPermissions ?: emptyArray()
            if (requested.contains(android.Manifest.permission.READ_SMS) &&
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_SMS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(android.Manifest.permission.READ_SMS)
            }
            if (requested.contains(android.Manifest.permission.RECEIVE_SMS) &&
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECEIVE_SMS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(android.Manifest.permission.RECEIVE_SMS)
            }
        } catch (e: Exception) {
            // Ignored safely
        }

        if (neededPermissions.isNotEmpty()) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        }
    }

    val googleSignInClient = remember(context) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, signInOptions)
    }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                viewModel.setErrorMessage("Google Sign-in failed: missing ID token. Please confirm the Web client ID in Firebase matches this app.")
            } else {
                viewModel.onGoogleSignInSuccess(
                    idToken = idToken,
                    name = account.displayName ?: "User",
                    email = account.email ?: account.id.orEmpty(),
                    photoUrl = account.photoUrl?.toString() ?: ""
                )
            }
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
                    "Google Sign-in was cancelled because the account chooser was closed before an account was selected."
                GoogleSignInStatusCodes.DEVELOPER_ERROR ->
                    "Google Sign-in setup error: check the SHA certificate fingerprints and Web client ID in Firebase/Google Cloud."
                CommonStatusCodes.NETWORK_ERROR ->
                    "Google Sign-in failed because the device could not reach Google. Please check your connection and try again."
                else -> "Google Sign-in failed (status ${e.statusCode}): ${e.localizedMessage ?: "Please try again."}"
            }
            viewModel.setErrorMessage(message)
        }
    }

    fun launchGoogleSignIn() {
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    BackHandler(enabled = authState == AuthState.LOADING) {
        viewModel.resetState()
    }







    LaunchedEffect(authState) {

        if (authState == AuthState.SUCCESS) {
            onLoginSuccess(isNewUser == true)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            if (it.isNotEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.setErrorMessage("") // Clear after showing
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Moving Image Carousel
            val images = listOf(
                "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&q=80"
            )
            val titles = listOf("Track smarter", "Invest better", "Grow faster")

            val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { images.size })

            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(3000)
                    val nextPage = (pagerState.currentPage + 1) % images.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }

            val cachedUser = remember { viewModel.getCachedUser() }
            val auth = FirebaseAuth.getInstance()
            val showCachedUser = auth.currentUser == null && cachedUser != null

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {
                // Background Image Pager (subtle photography)
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        coil.compose.AsyncImage(
                            model = images[page],
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.32f)
                        )
                    }
                }

                // Ambient Radial Neon Glows
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-40).dp)
                        .background(
                            Brush.radialGradient(
                                listOf(RivavaCyan.copy(alpha = 0.28f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )

                // Gradient mask to amoled black
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC08080B), AmoledBlack),
                                startY = 80f
                            )
                        )
                )

                // Foreground Branded Elements
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    RivavaGlowingLogo(size = 46.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "RIVAVA",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 3.sp,
                                color = Color.White
                            )
                        )
                        Surface(
                            color = RivavaCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RivavaCyan.copy(alpha = 0.45f)),
                            modifier = Modifier.padding(start = 5.dp)
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RivavaCyan,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = titles[pagerState.currentPage],
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Automated Wealth & Portfolio Intelligence",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RivavaCyan.copy(alpha = 0.9f),
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (showCachedUser && cachedUser != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    if (cachedUser.photo != null) {
                        coil.compose.AsyncImage(
                            model = cachedUser.photo,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Last logged in as ${cachedUser.name ?: "User"}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f))
                    )
                }
            }

            var startAnimation by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { startAnimation = true }

            // Glassmorphism Login Container with Radiant Neon Rim
            androidx.compose.animation.AnimatedVisibility(
                visible = startAnimation,
                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(1000)) +
                        androidx.compose.animation.slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = androidx.compose.animation.core.tween(1000)
                        )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    RivavaCyan.copy(alpha = 0.7f),
                                    RivavaPink.copy(alpha = 0.45f),
                                    RivavaLime.copy(alpha = 0.35f)
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF17192A), Color(0xFF0F101A))
                                )
                            )
                    ) {
                        var selectedAuthTab by remember { mutableStateOf(0) } // 0: Google, 1: Email
                        var isEmailSignUp by remember { mutableStateOf(false) }
                        var emailInput by remember { mutableStateOf("") }
                        var passwordInput by remember { mutableStateOf("") }
                        var nameInput by remember { mutableStateOf("") }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = if (selectedAuthTab == 1 && isEmailSignUp) "Create Account" else "Welcome to Rivava",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            RivavaBrandDisplay(showQuote = false)

                            // Auth Method Selector Tabs: Continue with Google & Continue with Email
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF12131E))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val authTabs = listOf(
                                    Triple("Google", Icons.Default.Bolt, 0),
                                    Triple("Email", Icons.Outlined.Email, 1)
                                )
                                authTabs.forEach { (label, icon, index) ->
                                    val isSelected = selectedAuthTab == index
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(
                                                if (isSelected) Brush.horizontalGradient(
                                                    listOf(RivavaCyan, Color(0xFF0072FF))
                                                ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                            )
                                            .clickable { selectedAuthTab = index }
                                            .padding(vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            when (selectedAuthTab) {
                                0 -> {
                                    // 1. Google Sign-in
                                    Text(
                                        text = "One-tap instant & secure sign-in",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )

                                    Button(
                                        onClick = { launchGoogleSignIn() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                        enabled = authState != AuthState.LOADING
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccountCircle,
                                                contentDescription = "Google",
                                                modifier = Modifier.size(24.dp),
                                                tint = AmoledBlack
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Continue with Google", color = AmoledBlack, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }
                                    }
                                }

                                1 -> {
                                    // 2. Email Sign In / Sign Up
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isEmailSignUp) "New User Registration" else "Sign in with password",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        TextButton(onClick = { isEmailSignUp = !isEmailSignUp }) {
                                            Text(
                                                text = if (isEmailSignUp) "Existing user? Sign In" else "New here? Sign Up",
                                                color = RivavaCyan,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }

                                    if (isEmailSignUp) {
                                        OutlinedTextField(
                                            value = nameInput,
                                            onValueChange = { nameInput = it },
                                            label = { Text("Full Name", color = Color.White.copy(0.6f)) },
                                            singleLine = true,
                                            leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = RivavaCyan) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = RivavaCyan,
                                                unfocusedBorderColor = Color.White.copy(0.2f),
                                                cursorColor = RivavaCyan,
                                                focusedLabelColor = RivavaCyan
                                            )
                                        )
                                    }

                                    OutlinedTextField(
                                        value = emailInput,
                                        onValueChange = { emailInput = it },
                                        label = { Text("Email Address", color = Color.White.copy(0.6f)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = RivavaCyan) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RivavaCyan,
                                            unfocusedBorderColor = Color.White.copy(0.2f),
                                            cursorColor = RivavaCyan,
                                            focusedLabelColor = RivavaCyan
                                        )
                                    )

                                    OutlinedTextField(
                                        value = passwordInput,
                                        onValueChange = { passwordInput = it },
                                        label = { Text("Password (min 6 chars)", color = Color.White.copy(0.6f)) },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RivavaCyan,
                                            unfocusedBorderColor = Color.White.copy(0.2f),
                                            cursorColor = RivavaCyan,
                                            focusedLabelColor = RivavaCyan
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            if (isEmailSignUp) {
                                                viewModel.onEmailRegister(emailInput.trim(), passwordInput, nameInput.trim()) {
                                                    Toast.makeText(context, "Verification email sent to $emailInput", Toast.LENGTH_LONG).show()
                                                }
                                            } else {
                                                viewModel.onEmailLogin(emailInput.trim(), passwordInput)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RivavaCyan),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                        enabled = authState != AuthState.LOADING && emailInput.isNotBlank() && passwordInput.isNotBlank()
                                    ) {
                                        Text(
                                            text = if (isEmailSignUp) "Create Account & Send Email" else "Sign In",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }

                                    if (!isEmailSignUp) {
                                        TextButton(
                                            onClick = onNavigateToReset,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "Forgot Password?",
                                                color = RivavaCyan,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        RivavaLoadingOverlay(isLoading = authState == AuthState.LOADING)
    }
}
