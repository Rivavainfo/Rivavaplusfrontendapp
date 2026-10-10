package com.rivavafi.universal.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.rivavafi.universal.data.preferences.UserPreferencesRepository
import com.rivavafi.universal.utils.SecretKeyValidator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class EntitlementStatus { LOADING, UNLOCKED, LOCKED, ERROR }

data class PremiumState(
    val status: EntitlementStatus = EntitlementStatus.LOADING,
    val isPremium: Boolean = false,
    val source: String? = null
)

data class OrderResult(
    val success: Boolean,
    val orderId: String? = null,
    val paymentUrl: String? = null,
    val error: String? = null
)

@Singleton
class UserEntitlementRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance("asia-south1")
    private val auth = FirebaseAuth.getInstance()

    private val _premiumState = MutableStateFlow(PremiumState())
    val premiumState: StateFlow<PremiumState> = _premiumState.asStateFlow()
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    suspend fun syncEntitlement() {
        val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
        val localPremium = prefs.getBoolean("isPremium", false)
        val localPremiumSource = prefs.getString("premium_source", null)
        val hasLocalKeyUnlock = localPremium && (localPremiumSource == "access_key" || localPremiumSource == "secret_key" || localPremiumSource == "local_account_key")

        if (localPremium) {
            _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, localPremiumSource ?: "cache")
        } else {
            _premiumState.value = PremiumState(EntitlementStatus.LOADING, false, null)
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            if (hasLocalKeyUnlock) {
                _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, localPremiumSource ?: "local_account_key")
                userPreferencesRepository.setPremiumUserForCurrent(true)
            } else {
                _premiumState.value = PremiumState(EntitlementStatus.LOCKED, false, null)
                prefs.edit().putBoolean("isPremium", false).putBoolean("portfolio_unlocked", false).remove("premium_source").apply()
                userPreferencesRepository.setPremiumUserForCurrent(false)
            }
            snapshotListener?.remove()
            return
        }

        try {
            snapshotListener?.remove()
            snapshotListener = firestore.collection("therivdata").document(uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("UserEntitlement", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val isPremium = snapshot.getBoolean("premiumStatus") ?: false
                        val expiresAtDate = snapshot.getDate("premiumExpiresAt") ?: snapshot.getDate("premium_expires_at")
                        val isExpired = expiresAtDate != null && java.util.Date().after(expiresAtDate)

                        val validPremium = isPremium && !isExpired
                        val effectivePremium = validPremium || hasLocalKeyUnlock
                        val effectiveSource = if (hasLocalKeyUnlock) (localPremiumSource ?: "local_account_key") else if (validPremium) "therivdata" else null

                        _premiumState.value = if (effectivePremium) {
                            PremiumState(EntitlementStatus.UNLOCKED, true, effectiveSource)
                        } else {
                            PremiumState(EntitlementStatus.LOCKED, false, null)
                        }

                        val edit = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE).edit()
                        edit.putBoolean("isPremium", effectivePremium)
                            .putBoolean("portfolio_unlocked", effectivePremium)
                        if (effectivePremium) {
                            edit.putString("premium_source", effectiveSource)
                        } else {
                            edit.remove("premium_source")
                        }
                        edit.apply()

                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            userPreferencesRepository.setPremiumUserForCurrent(effectivePremium)
                        }
                    }
                }

            val docSnap = firestore.collection("therivdata").document(uid).get().await()
            val isPremium = docSnap.getBoolean("premiumStatus") ?: false
            val expiresAtDate = docSnap.getDate("premiumExpiresAt") ?: docSnap.getDate("premium_expires_at")
            val isExpired = expiresAtDate != null && java.util.Date().after(expiresAtDate)

            val validPremium = isPremium && !isExpired
            val effectivePremium = validPremium || hasLocalKeyUnlock
            val effectiveSource = if (hasLocalKeyUnlock) (localPremiumSource ?: "local_account_key") else if (validPremium) "therivdata" else null

            _premiumState.value = if (effectivePremium) {
                PremiumState(EntitlementStatus.UNLOCKED, true, effectiveSource)
            } else {
                PremiumState(EntitlementStatus.LOCKED, false, null)
            }

            val edit = prefs.edit()
                .putBoolean("isPremium", effectivePremium)
                .putBoolean("portfolio_unlocked", effectivePremium)
            if (effectivePremium) {
                edit.putString("premium_source", effectiveSource)
            } else {
                edit.remove("premium_source")
            }
            edit.apply()
            userPreferencesRepository.setPremiumUserForCurrent(effectivePremium)

        } catch (e: Exception) {
            Log.e("UserEntitlement", "Error syncing entitlement", e)
            _premiumState.value = PremiumState(
                if (hasLocalKeyUnlock) EntitlementStatus.UNLOCKED else EntitlementStatus.ERROR,
                hasLocalKeyUnlock || localPremium,
                localPremiumSource ?: "cache"
            )
        }
    }

    suspend fun createPaymentOrder(amountPaise: Int = 1100, plan: String = "portfolio_premium"): OrderResult {
        val uid = auth.currentUser?.uid ?: return OrderResult(false, error = "User not authenticated")
        val userEmail = auth.currentUser?.email

        return try {
            Log.i("UserEntitlement", "PAYMENT_CREATE_STARTED on Node.js REST API")
            val req = com.rivavafi.universal.data.network.CreatePaymentOrderRequest(
                userId = uid,
                userEmail = userEmail,
                plan = plan,
                amountPaise = amountPaise
            )
            val response = com.rivavafi.universal.data.network.RetrofitClient.apiService.createPaymentOrder(req)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                val orderId = data?.orderId
                val paymentUrl = data?.paymentUrl

                if (orderId != null) {
                    Log.i("UserEntitlement", "PAYMENT_CREATE_SUCCESS: $orderId")
                    OrderResult(true, orderId, paymentUrl = paymentUrl)
                } else {
                    OrderResult(false, error = "Invalid response from payment server")
                }
            } else {
                val errorMsg = response.body()?.message ?: "Failed to initialize payment with server"
                OrderResult(false, error = errorMsg)
            }
        } catch (e: Exception) {
            Log.e("UserEntitlement", "Error creating payment order via Node.js backend", e)
            OrderResult(false, error = e.message ?: "Network error connecting to payment gateway")
        }
    }

    suspend fun createUroPayOrder(amountPaise: Int): OrderResult {
        return createPaymentOrder(amountPaise)
    }

    suspend fun verifyPayment(orderId: String, paymentId: String? = null, signature: String? = null): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        return try {
            Log.i("UserEntitlement", "PAYMENT_VERIFY_STARTED on Node.js REST API for order $orderId")
            val req = com.rivavafi.universal.data.network.VerifyPaymentRequest(
                userId = uid,
                orderId = orderId,
                paymentId = paymentId,
                signature = signature
            )
            val response = com.rivavafi.universal.data.network.RetrofitClient.apiService.verifyPayment(req)

            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("UserEntitlement", "PAYMENT_VERIFY_SUCCESS on Node.js backend")
                _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, "payment_gateway")
                val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("isPremium", true)
                    .putBoolean("portfolio_unlocked", true)
                    .putString("premium_source", "payment_gateway")
                    .apply()
                userPreferencesRepository.setPremiumUserForCurrent(true)
                true
            } else {
                Log.w("UserEntitlement", "PAYMENT_VERIFY_FAILED: Server returned failure")
                _premiumState.value = PremiumState(EntitlementStatus.ERROR, false, null)
                false
            }
        } catch (e: Exception) {
            Log.e("UserEntitlement", "Failed to verify payment via Node.js REST API", e)
            _premiumState.value = PremiumState(EntitlementStatus.ERROR, false, null)
            false
        }
    }

    suspend fun verifyUroPayPayment(orderId: String): Boolean {
        return verifyPayment(orderId)
    }

    suspend fun clearEntitlement() {
        val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("isPremium", false)
            .putBoolean("portfolio_unlocked", false)
            .remove("premium_source")
            .apply()
        userPreferencesRepository.setPremiumUserForCurrent(false)
        _premiumState.value = PremiumState(EntitlementStatus.LOCKED, false, null)
    }

    suspend fun requestNewKey(): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User not logged in."))
        val userEmail = auth.currentUser?.email

        return try {
            val existing = firestore.collection("secret_keys")
                .whereEqualTo("assignedUserId", uid)
                .whereIn("status", listOf("pending", "active"))
                .get()
                .await()

            if (!existing.isEmpty) {
                val doc = existing.documents.first()
                val status = doc.getString("status") ?: "pending"
                val key = doc.getString("keyString") ?: doc.id
                return Result.success("Existing key request found ($status). Key: $key")
            }

            val newKeyString = SecretKeyValidator.generateSecretKey()
            val now = java.util.Date()
            val expiresAt = java.util.Date(now.time + 30L * 24 * 60 * 60 * 1000)

            val keyDoc = mapOf(
                "keyString" to newKeyString,
                "status" to "pending",
                "isActive" to false,
                "tier" to "portfolio_premium",
                "maxUses" to 1,
                "currentUses" to 0,
                "assignedUserId" to uid,
                "assignedEmail" to (userEmail ?: ""),
                "createdAt" to FieldValue.serverTimestamp(),
                "expiresAt" to expiresAt,
                "redeemedAt" to null,
                "redeemedBy" to null,
                "approvalStatus" to "pending",
                "generatedBy" to "system",
                "deliveryStatus" to "pending"
            )

            firestore.collection("secret_keys").document(newKeyString).set(keyDoc).await()
            Result.success("Key request submitted. Pending admin approval.")
        } catch (e: Exception) {
            Log.e("UserEntitlement", "Error requesting new key", e)
            Result.failure(Exception(e.message ?: "Failed to request key"))
        }
    }

    /**
     * Verifies and redeems secret key.
     * When IS_TEMPORARY_LOCAL_UNLOCK_MODE is true:
     * Disables Firebase secret_keys queries, calculates account-based key formula
     * and performs deterministic local UI unlock without calling Cloud Functions.
     */
    suspend fun verifyAndRedeemSecretKey(rawKey: String): Result<String> {
        val enteredKey = rawKey.trim()

        if (enteredKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a valid secret key."))
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            return Result.failure(IllegalStateException("Please log in to your account before activating a key."))
        }

        val currentUserEmail = auth.currentUser?.email

        if (SecretKeyValidator.IS_TEMPORARY_LOCAL_UNLOCK_MODE) {
            Log.i("UserEntitlement", "TEMPORARY_LOCAL_SECRET_KEY_VERIFY for user $uid")

            // Fetch stored mobile number from therivdata or UserPreferences
            var userPhone: String? = null
            try {
                val doc = firestore.collection("therivdata").document(uid).get().await()
                if (doc.exists()) {
                    userPhone = doc.getString("phone") ?: doc.getString("phoneno") ?: doc.getString("phoneNumber")
                }
            } catch (e: Exception) {
                Log.w("UserEntitlement", "Could not fetch user profile from Firestore for phone number", e)
            }

            if (userPhone.isNullOrBlank()) {
                userPhone = userPreferencesRepository.userPhoneFlow.firstOrNull()
            }

            if (userPhone.isNullOrBlank()) {
                return Result.failure(IllegalStateException("No valid saved mobile number found. Please add or verify your mobile number in your profile."))
            }

            if (currentUserEmail.isNullOrBlank()) {
                return Result.failure(IllegalStateException("No valid account email found. Please ensure you are logged in."))
            }

            val expectedKey = SecretKeyValidator.calculateAccountSecretKey(currentUserEmail, userPhone)
            if (expectedKey == null) {
                return Result.failure(IllegalStateException("Please add or verify your mobile number in your profile."))
            }

            if (enteredKey.equals(expectedKey, ignoreCase = true)) {
                val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("isPremium", true)
                    .putBoolean("portfolio_unlocked", true)
                    .putString("premium_source", "local_account_key")
                    .apply()

                userPreferencesRepository.setPremiumUserForCurrent(true)
                _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, "local_account_key")

                // Best-effort 30-day local expiry update in therivdata without failing local unlock
                try {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 30)
                    val expiresAtDate = calendar.time

                    val updateData = mapOf(
                        "isPremium" to true,
                        "premiumStatus" to true,
                        "premium_status" to "active",
                        "premiumActivatedAt" to FieldValue.serverTimestamp(),
                        "premiumExpiresAt" to expiresAtDate,
                        "premiumLastKeyId" to expectedKey
                    )
                    firestore.collection("therivdata").document(uid)
                        .set(updateData, com.google.firebase.firestore.SetOptions.merge())
                } catch (e: Exception) {
                    Log.w("UserEntitlement", "Could not sync temporary unlock state to Firestore", e)
                }

                Log.i("UserEntitlement", "TEMPORARY_LOCAL_UNLOCK_SUCCESS for user $uid")
                return Result.success("Portfolio unlocked successfully for your account!")
            } else {
                Log.w("UserEntitlement", "TEMPORARY_LOCAL_UNLOCK_FAILED: Key mismatch for user $uid")
                return Result.failure(IllegalArgumentException("Invalid Secret Key."))
            }
        }

        // --- LEGACY BACKEND FIREBASE VERIFICATION CODE (PRESERVED FOR FUTURE RESTORATION) ---
        val formattedKey = enteredKey.uppercase()
        if (!SecretKeyValidator.isValidFormat(formattedKey)) {
            return Result.failure(IllegalArgumentException("Please enter a valid secret key in RIV-XXXX-XXXX-XXXX format."))
        }

        return try {
            Log.i("UserEntitlement", "SECRET_KEY_VERIFY_STARTED for user $uid")

            var verificationSuccess = false
            var failureReason = "Invalid secret key. Please verify and try again."

            val docRef = firestore.collection("secret_keys").document(formattedKey)

            verificationSuccess = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    val keyHash = java.security.MessageDigest.getInstance("SHA-256")
                        .digest(formattedKey.toByteArray(Charsets.UTF_8))
                        .joinToString("") { "%02x".format(it) }
                    val hashRef = firestore.collection("secret_keys").document(keyHash)
                    val hashSnap = transaction.get(hashRef)
                    if (!hashSnap.exists()) {
                        failureReason = "Invalid secret key. Key not found."
                        return@runTransaction false
                    }
                }

                val targetSnap = if (snapshot.exists()) snapshot else {
                    val keyHash = java.security.MessageDigest.getInstance("SHA-256")
                        .digest(formattedKey.toByteArray(Charsets.UTF_8))
                        .joinToString("") { "%02x".format(it) }
                    transaction.get(firestore.collection("secret_keys").document(keyHash))
                }
                val targetRef = targetSnap.reference

                val status = targetSnap.getString("status") ?: "pending"
                val isActive = targetSnap.getBoolean("isActive") ?: false
                val approvalStatus = targetSnap.getString("approvalStatus") ?: "pending"

                if (approvalStatus == "pending" || status == "pending" || !isActive) {
                    failureReason = "Key is pending approval by administrator."
                    return@runTransaction false
                }

                if (status == "used" || status == "redeemed") {
                    failureReason = "This secret key has already been used and cannot be redeemed again."
                    return@runTransaction false
                }

                val maxUses = targetSnap.getLong("maxUses") ?: 1
                val currentUses = targetSnap.getLong("currentUses") ?: 0
                if (currentUses >= maxUses) {
                    failureReason = "This secret key has reached maximum usage limit."
                    return@runTransaction false
                }

                val now = java.util.Date()
                val calendar = java.util.Calendar.getInstance()
                calendar.time = now
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 30)
                val expiresAtDate = calendar.time

                transaction.update(targetRef, mapOf(
                    "status" to "used",
                    "isActive" to false,
                    "currentUses" to currentUses + 1,
                    "redeemedAt" to FieldValue.serverTimestamp(),
                    "redeemedBy" to uid
                ))

                val therivRef = firestore.collection("therivdata").document(uid)
                val userRef = firestore.collection("users").document(uid)

                val updateData = mapOf(
                    "isPremium" to true,
                    "premiumStatus" to true,
                    "premium_status" to "active",
                    "premiumTier" to "portfolio_premium",
                    "premiumActivatedAt" to FieldValue.serverTimestamp(),
                    "premiumExpiresAt" to expiresAtDate,
                    "premiumLastKeyId" to formattedKey
                )

                transaction.set(therivRef, updateData, com.google.firebase.firestore.SetOptions.merge())
                transaction.set(userRef, updateData, com.google.firebase.firestore.SetOptions.merge())

                true
            }.await()

            if (verificationSuccess) {
                val prefs = context.getSharedPreferences("RivavaPortfolioPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("isPremium", true)
                    .putBoolean("portfolio_unlocked", true)
                    .putString("premium_source", "secret_key")
                    .apply()

                userPreferencesRepository.setPremiumUserForCurrent(true)
                _premiumState.value = PremiumState(EntitlementStatus.UNLOCKED, true, "secret_key")
                Log.i("UserEntitlement", "SECRET_KEY_VERIFY_SUCCESS for user $uid")
                Result.success("Premium access activated for 30 days!")
            } else {
                Log.w("UserEntitlement", "SECRET_KEY_VERIFY_FAILED: $failureReason")
                Result.failure(Exception(failureReason))
            }
        } catch (e: Exception) {
            Log.e("UserEntitlement", "Exception during secret key verification", e)
            Result.failure(Exception(e.message ?: "Failed to verify secret key. Please try again."))
        }
    }

    suspend fun unlockWithSecretKey(key: String = ""): Boolean {
        return if (key.isNotBlank()) {
            verifyAndRedeemSecretKey(key).isSuccess
        } else {
            false
        }
    }
}
