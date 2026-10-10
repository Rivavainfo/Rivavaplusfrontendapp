package com.rivavafi.universal.utils

import java.security.SecureRandom
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SecretKeyValidator {
    /**
     * Temporary local unlock mode flag.
     * When true, disables Firebase secret_keys Firestore verification and uses
     * the deterministic account-based formula: EmailPrefix + Month(MM) + MobilePrefix + "Riva".
     */
    const val IS_TEMPORARY_LOCAL_UNLOCK_MODE = true

    private val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()
    private val secureRandom = SecureRandom()

    /**
     * Calculates the deterministic account-based secret key according to formula:
     * KEY = EmailPrefix + Month + MobilePrefix + "Riva"
     *
     * Rules:
     * 1. EmailPrefix: First 2 letters of email's local part (before @), lowercase.
     * 2. Month: Two-digit current month number (01..12).
     * 3. MobilePrefix: First 3 digits of normalized national mobile number (removing +91, leading 0, spaces, etc.).
     * 4. Riva: Exact string "Riva".
     *
     * Example: rohit@gmail.com, 9876543210, October (10) -> ro10987Riva
     */
    fun calculateAccountSecretKey(
        email: String?,
        phone: String?,
        date: Date = Date()
    ): String? {
        if (email.isNullOrBlank()) return null
        val emailLocal = email.trim().substringBefore("@")
        if (emailLocal.length < 2) return null
        val emailPrefix = emailLocal.take(2).lowercase(Locale.ROOT)

        val cleanPhone = phone?.replace(Regex("[^0-9+]"), "") ?: ""
        var nationalPhone = cleanPhone.removePrefix("+")
        if (nationalPhone.startsWith("91") && nationalPhone.length > 10) {
            nationalPhone = nationalPhone.substring(2)
        }
        if (nationalPhone.startsWith("0") && nationalPhone.length == 11) {
            nationalPhone = nationalPhone.substring(1)
        }
        if (nationalPhone.length < 3) return null
        val mobilePrefix = nationalPhone.take(3)

        val cal = Calendar.getInstance()
        cal.time = date
        val monthNum = cal.get(Calendar.MONTH) + 1
        val monthStr = String.format(Locale.ROOT, "%02d", monthNum)

        return "${emailPrefix}${monthStr}${mobilePrefix}Riva"
    }

    /**
     * Generates a new secret key in the format: RIV-XXXX-XXXX-XXXX-XXXX
     * e.g., RIV-GUMD-CHUN-PRYE-ZV3W
     */
    fun generateSecretKey(): String {
        fun randomGroup(): String {
            val sb = StringBuilder(4)
            for (i in 0 until 4) {
                sb.append(CHARS[secureRandom.nextInt(CHARS.size)])
            }
            return sb.toString()
        }
        return "RIV-${randomGroup()}-${randomGroup()}-${randomGroup()}-${randomGroup()}"
    }

    /**
     * Regex matching RIV-XXXX-XXXX-XXXX format and legacy formats
     */
    val KEY_FORMAT_REGEX = Regex(
        "(?i)^(" +
            "RIV(-[A-Z0-9]{4}){4}|" +
            "RIV[@#\\$%&*!\\-_+]RUBICON\\d{5}|RIVRUBICON[@#\\$%&*!\\-_+]\\d{5}|" +
            "RIV[@#\\$%&*!\\-_+]\\d{5}RUBICON|RIV\\d{5}[@#\\$%&*!\\-_+]RUBICON|" +
            "RUBICON[@#\\$%&*!\\-_+]RIV\\d{5}|RUBICONRIV[@#\\$%&*!\\-_+]\\d{5}|" +
            "RUBICON[@#\\$%&*!\\-_+]\\d{5}RIV|RUBICON\\d{5}[@#\\$%&*!\\-_+]RIV|" +
            "\\d{5}[@#\\$%&*!\\-_+]RIVRUBICON|\\d{5}RIV[@#\\$%&*!\\-_+]RUBICON|" +
            "\\d{5}[@#\\$%&*!\\-_+]RUBICONRIV|\\d{5}RUBICON[@#\\$%&*!\\-_+]RIV" +
            ")$"
    )

    /**
     * Sanitizes and normalizes a secret key string.
     */
    fun normalize(key: String): String {
        return key.trim()
    }

    /**
     * Formats user input as RIV-XXXX-XXXX-XXXX automatically as typed.
     */
    fun formatAsYouType(input: String): String {
        val clean = input.replace("[^A-Za-z0-9]".toRegex(), "").uppercase()
        val builder = StringBuilder()
        for (i in clean.indices) {
            if (i > 0 && (i == 3 || i == 7 || i == 11 || i == 15)) {
                builder.append('-')
            }
            builder.append(clean[i])
        }
        return builder.toString()
    }

    /**
     * Validates structural format of a secret key.
     */
    fun isValidFormat(key: String): Boolean {
        val clean = key.trim()
        if (clean.isBlank()) return false
        if (IS_TEMPORARY_LOCAL_UNLOCK_MODE) {
            if (clean.endsWith("Riva", ignoreCase = true) && clean.length >= 8) return true
        }
        if (KEY_FORMAT_REGEX.matches(clean.uppercase())) return true
        if (clean.uppercase().matches(Regex("(?i)^RIV(-[A-Z0-9]{4}){4}$"))) return true
        return clean.length >= 8
    }

    @Deprecated("Use asynchronous verifyAndRedeemSecretKey via PremiumViewModel instead")
    fun isValid(key: String): Boolean {
        return isValidFormat(key)
    }
}
