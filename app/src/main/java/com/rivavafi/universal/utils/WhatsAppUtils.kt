package com.rivavafi.universal.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WhatsAppUtils {

    fun openWhatsAppWithMessage(
        context: Context,
        customMessage: String
    ) {
        try {
            val encodedMessage = URLEncoder.encode(customMessage, "UTF-8")
            val targetNumber = "918881176909"
            val url = "https://api.whatsapp.com/send?phone=$targetNumber&text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed on your device", Toast.LENGTH_LONG).show()
        }
    }

    fun openWhatsAppForAdvisor(
        context: Context,
        username: String,
        email: String,
        phoneNumber: String,
        preference: String,
        premiumStatus: Boolean
    ) {
        val message = """
            Hello Rivava Team,

            A user wants to connect with an advisor.

            Details:
            Name: $username
            Email: $email
            Phone: $phoneNumber
            Preference: $preference
            Premium Status: $premiumStatus

            Please contact me.
        """.trimIndent()
        openWhatsAppWithMessage(context, message)
    }
}
