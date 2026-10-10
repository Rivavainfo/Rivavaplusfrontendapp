package com.rivavafi.universal.utils

import java.security.SecureRandom

object SecretKeyValidator {
    private val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()
    private val secureRandom = SecureRandom()

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
        return key.trim().uppercase()
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
        val clean = key.trim().uppercase()
        if (clean.isBlank()) return false
        if (KEY_FORMAT_REGEX.matches(clean)) return true
        if (clean.matches(Regex("(?i)^RIV(-[A-Z0-9]{4}){4}$"))) return true
        return clean.length >= 8
    }

    @Deprecated("Use asynchronous verifyAndRedeemSecretKey via PremiumViewModel instead")
    fun isValid(key: String): Boolean {
        return isValidFormat(key)
    }
}
