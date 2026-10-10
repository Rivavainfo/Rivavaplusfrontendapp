package com.rivavafi.universal.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SecretKeyValidatorTest {

    @Test
    fun testAccountSecretKeyFormulaExamples() {
        val calOct = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.OCTOBER)
            set(Calendar.DAY_OF_MONTH, 15)
        }

        // Example 1: rohit@gmail.com, 9876543210, October (10) -> ro10987Riva
        val key1 = SecretKeyValidator.calculateAccountSecretKey(
            email = "rohit@gmail.com",
            phone = "9876543210",
            date = calOct.time
        )
        assertEquals("ro10987Riva", key1)

        // Example 2: aditya@gmail.com, 9123456780, October (10) -> ad10912Riva
        val key2 = SecretKeyValidator.calculateAccountSecretKey(
            email = "aditya@gmail.com",
            phone = "+91 91234 56780",
            date = calOct.time
        )
        assertEquals("ad10912Riva", key2)
    }

    @Test
    fun testAllMonthsFormatting() {
        val email = "testuser@gmail.com"
        val phone = "+919876543210"

        val expectedMonths = listOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")

        for (monthIndex in 0..11) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2024)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, 10)
            }
            val key = SecretKeyValidator.calculateAccountSecretKey(email, phone, cal.time)
            val expected = "te${expectedMonths[monthIndex]}987Riva"
            assertEquals("Month ${monthIndex + 1} key mismatch", expected, key)
        }
    }

    @Test
    fun testPhoneNormalizationAndCountryCode() {
        val calOct = Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.OCTOBER)
        }.time

        val phoneWithPlus91 = SecretKeyValidator.calculateAccountSecretKey("john@domain.com", "+919876543210", calOct)
        val phoneWithSpaces = SecretKeyValidator.calculateAccountSecretKey("john@domain.com", "+91 98765 43210", calOct)
        val phoneDirect = SecretKeyValidator.calculateAccountSecretKey("john@domain.com", "9876543210", calOct)

        assertEquals("jo10987Riva", phoneWithPlus91)
        assertEquals("jo10987Riva", phoneWithSpaces)
        assertEquals("jo10987Riva", phoneDirect)
    }

    @Test
    fun testMissingOrShortAccountDetails() {
        val cal = Calendar.getInstance().time

        assertNull("Missing email should return null", SecretKeyValidator.calculateAccountSecretKey(null, "9876543210", cal))
        assertNull("Empty email should return null", SecretKeyValidator.calculateAccountSecretKey("", "9876543210", cal))
        assertNull("Short email prefix should return null", SecretKeyValidator.calculateAccountSecretKey("a@b.com", "9876543210", cal))

        assertNull("Missing phone should return null", SecretKeyValidator.calculateAccountSecretKey("rohit@gmail.com", null, cal))
        assertNull("Empty phone should return null", SecretKeyValidator.calculateAccountSecretKey("rohit@gmail.com", "", cal))
        assertNull("Short phone should return null", SecretKeyValidator.calculateAccountSecretKey("rohit@gmail.com", "12", cal))
    }

    @Test
    fun testTemporaryModeIsValidFormat() {
        assertTrue("Account-based key ending with Riva should be valid format in temporary mode", SecretKeyValidator.isValidFormat("ro10987Riva"))
        assertTrue("Case-insensitive Riva check", SecretKeyValidator.isValidFormat("ad10912riva"))
        assertFalse("Key without Riva or minimum length should be invalid", SecretKeyValidator.isValidFormat("ro1098"))
    }
}
