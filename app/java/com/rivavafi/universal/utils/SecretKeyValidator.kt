package com.rivavafi.universal.utils

object SecretKeyValidator {
    /**
     * Regex matching all 6 permutations of RIV, Rubicon, and 5-digit numbers
     * with exactly one special symbol separator from [@#\$%&*!\-_+] at either component boundary.
     */
    val KEY_FORMAT_REGEX = Regex(
        "(?i)^(" +
            // RIV + Rubicon + Digits
            "RIV[@#\\$%&*!\\-_+]RUBICON\\d{5}|RIVRUBICON[@#\\$%&*!\\-_+]\\d{5}|" +
            // RIV + Digits + Rubicon
            "RIV[@#\\$%&*!\\-_+]\\d{5}RUBICON|RIV\\d{5}[@#\\$%&*!\\-_+]RUBICON|" +
            // Rubicon + RIV + Digits
            "RUBICON[@#\\$%&*!\\-_+]RIV\\d{5}|RUBICONRIV[@#\\$%&*!\\-_+]\\d{5}|" +
            // Rubicon + Digits + RIV
            "RUBICON[@#\\$%&*!\\-_+]\\d{5}RIV|RUBICON\\d{5}[@#\\$%&*!\\-_+]RIV|" +
            // Digits + RIV + Rubicon
            "\\d{5}[@#\\$%&*!\\-_+]RIVRUBICON|\\d{5}RIV[@#\\$%&*!\\-_+]RUBICON|" +
            // Digits + Rubicon + RIV
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
     * Formats user input as RIV-XXXX-XXXX-XXXX automatically if legacy key is typed.
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
     * Validates minimum structural length or randomized key format before sending to backend.
     */
    fun isValidFormat(key: String): Boolean {
        val clean = key.trim()
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
