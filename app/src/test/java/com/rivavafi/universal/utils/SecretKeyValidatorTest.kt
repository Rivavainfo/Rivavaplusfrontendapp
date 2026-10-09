package com.rivavafi.universal.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretKeyValidatorTest {

    @Test
    fun testAllSixComponentPermutationsValid() {
        val validPermutationKeys = listOf(
            // 1. RIV + Rubicon + Digits
            "RIV@Rubicon48291",
            "RIV#Rubicon73920",
            "RIVRubicon@48291",
            // 2. RIV + Digits + Rubicon
            "RIV$52830Rubicon",
            "RIV62840@Rubicon",
            // 3. Rubicon + RIV + Digits
            "Rubicon@RIV62840",
            "RubiconRIV#38192",
            // 4. Rubicon + Digits + RIV
            "Rubicon$52830RIV",
            "Rubicon87329#RIV",
            // 5. Digits + RIV + Rubicon
            "38192#RIVRubicon",
            "92841@RIVRubicon",
            // 6. Digits + Rubicon + RIV
            "92841@RubiconRIV",
            "02849!RubiconRIV"
        )

        for (key in validPermutationKeys) {
            assertTrue("Key should be valid: $key", SecretKeyValidator.isValidFormat(key))
        }
    }

    @Test
    fun testAllTenSupportedSymbols() {
        val symbols = listOf("@", "#", "$", "%", "&", "*", "!", "-", "_", "+")
        for (symbol in symbols) {
            val key = "RIV${symbol}Rubicon48291"
            assertTrue("Key with symbol $symbol should be valid", SecretKeyValidator.isValidFormat(key))
        }
    }

    @Test
    fun testLeadingZeroFiveDigitNumbers() {
        val leadingZeroKeys = listOf(
            "RIV@Rubicon00123",
            "00042#RubiconRIV",
            "Rubicon$08291RIV"
        )

        for (key in leadingZeroKeys) {
            assertTrue("Key with leading zero digit should be valid: $key", SecretKeyValidator.isValidFormat(key))
        }
    }

    @Test
    fun testOldHardcodedBypassPatternRejected() {
        val oldBypassKeys = listOf(
            "rivrubi@12345",
            "RIVRUBI@99999",
            "rivrubi@00000"
        )

        for (key in oldBypassKeys) {
            assertFalse("Old rivrubi bypass pattern should be rejected: $key", SecretKeyValidator.KEY_FORMAT_REGEX.matches(key))
        }
    }

    @Test
    fun testInvalidFormatKeysRejected() {
        val invalidKeys = listOf(
            "",
            "   ",
            "RIVRubicon12345", // Missing special symbol
            "RIV@Rubicon1234",  // Only 4 digits
            "RIV@Rubicon123456", // 6 digits
            "INVALID@KEY99999"
        )

        for (key in invalidKeys) {
            assertFalse("Invalid key should be rejected: $key", SecretKeyValidator.KEY_FORMAT_REGEX.matches(key))
        }
    }
}
