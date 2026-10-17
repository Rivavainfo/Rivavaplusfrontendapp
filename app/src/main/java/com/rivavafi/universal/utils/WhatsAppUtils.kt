package com.rivavafi.universal.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder

object WhatsAppUtils {

    fun openWhatsAppWithMessage(
        context: Context,
        customMessage: String
    ) {
        try {
            val authUser = FirebaseAuth.getInstance().currentUser
            val prefsUser = PrefsManager(context).getUser()

            val username = authUser?.displayName?.takeIf { it.isNotBlank() }
                ?: prefsUser?.name?.takeIf { it.isNotBlank() }
                ?: "Rivava User"
            val email = authUser?.email?.takeIf { it.isNotBlank() }
                ?: prefsUser?.email?.takeIf { it.isNotBlank() }
                ?: "Not Provided"
            val phone = authUser?.phoneNumber?.takeIf { it.isNotBlank() }
                ?: prefsUser?.phone?.takeIf { it.isNotBlank() }
                ?: "Not Provided"

            val formattedMessage = if (!customMessage.contains("Email:", ignoreCase = true) && !customMessage.contains("Phone:", ignoreCase = true)) {
                """
                $customMessage

                User Details:
                Username: $username
                Email: $email
                Phone: $phone
                """.trimIndent()
            } else {
                customMessage
            }

            val encodedMessage = URLEncoder.encode(formattedMessage, "UTF-8")
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
        val authUser = FirebaseAuth.getInstance().currentUser
        val prefsUser = PrefsManager(context).getUser()

        val finalName = username.ifBlank { authUser?.displayName ?: prefsUser?.name ?: "Rivava User" }
        val finalEmail = email.ifBlank { authUser?.email ?: prefsUser?.email ?: "Not Provided" }
        val finalPhone = phoneNumber.ifBlank { authUser?.phoneNumber ?: prefsUser?.phone ?: "Not Provided" }

        val message = """
            Hello Rivava Team,

            A user wants to connect with an advisor.

            Details:
            Username: $finalName
            Email: $finalEmail
            Phone: $finalPhone
            Preference: $preference
            Premium Status: $premiumStatus

            Please contact me.
        """.trimIndent()
        openWhatsAppWithMessage(context, message)
    }
}
