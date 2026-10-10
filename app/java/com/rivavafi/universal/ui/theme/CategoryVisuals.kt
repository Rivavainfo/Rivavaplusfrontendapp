package com.rivavafi.universal.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryVisual(
    val title: String,
    val color: Color,
    val icon: ImageVector
)

object CategoryVisuals {
    val EXPENSE = CategoryVisual("Debit", RivavaPink, Icons.Outlined.TrendingDown)
    val INCOME = CategoryVisual("Credit", RivavaLime, Icons.Outlined.TrendingUp)
    val BILL = CategoryVisual("Bill", Color(0xFFA855F7), Icons.Outlined.Receipt)
    val INVESTMENT = CategoryVisual("Investment", RivavaCyan, Icons.Outlined.Savings)
    val SUBSCRIPTION = CategoryVisual("Subscription", Color(0xFF6366F1), Icons.Outlined.Autorenew)
    val REWARD = CategoryVisual("Reward", Color(0xFFFBBF24), Icons.Outlined.CardGiftcard)
    val SELF_TRANSFER = CategoryVisual("Transfer", Color(0xFF94A3B8), Icons.Outlined.SyncAlt)
    val IGNORE = CategoryVisual("Ignore", Color(0xFF64748B), Icons.Outlined.Block)
    val DEFAULT = CategoryVisual("Other", Color(0xFF64748B), Icons.Outlined.HelpOutline)

    val subcategories = mapOf(
        "Food" to CategoryVisual("Food", Color(0xFFFB923C), Icons.Outlined.Restaurant),
        "Groceries" to CategoryVisual("Groceries", RivavaLime, Icons.Outlined.LocalGroceryStore),
        "Transport" to CategoryVisual("Transport", RivavaCyan, Icons.Outlined.DirectionsCar),
        "Bills" to CategoryVisual("Bills", Color(0xFFA855F7), Icons.Outlined.ReceiptLong),
        "Shopping" to CategoryVisual("Shopping", RivavaPink, Icons.Outlined.ShoppingBag),
        "Recharge" to CategoryVisual("Recharge", RivavaCyan, Icons.Outlined.PhoneAndroid),
        "Entertainment" to CategoryVisual("Entertainment", Color(0xFFF44336), Icons.Outlined.Movie),
        "Travel" to CategoryVisual("Travel", Color(0xFF03A9F4), Icons.Outlined.Flight),
        "Loan Given" to CategoryVisual("Loan Given", Color(0xFF795548), Icons.Outlined.Handshake),
        "Loan Payment" to CategoryVisual("Loan Payment", Color(0xFF8D6E63), Icons.Outlined.Payments),
        "Health" to CategoryVisual("Health", Color(0xFFD32F2F), Icons.Outlined.MedicalServices),
        "Education" to CategoryVisual("Education", Color(0xFF3F51B5), Icons.Outlined.School),
        "Utilities" to CategoryVisual("Utilities", Color(0xFF673AB7), Icons.Outlined.Lightbulb),
        "Others" to CategoryVisual("Others", Color(0xFF607D8B), Icons.Outlined.MoreHoriz)
    )

    val FOOD = CategoryVisual("Food", Color(0xFFFB923C), Icons.Outlined.Restaurant)
    val SHOPPING = CategoryVisual("Shopping", Color(0xFFEC4899), Icons.Outlined.ShoppingBag)
    val TRANSPORT = CategoryVisual("Transport", RivavaCyan, Icons.Outlined.DirectionsCar)
    val BILLS = CategoryVisual("Bills", Color(0xFFA855F7), Icons.Outlined.ReceiptLong)
    val ENTERTAINMENT = CategoryVisual("Entertainment", Color(0xFFF43F5E), Icons.Outlined.Movie)
    val HEALTHCARE = CategoryVisual("Healthcare", Color(0xFFEF4444), Icons.Outlined.MedicalServices)
    val INVESTMENTS = CategoryVisual("Investments", Color(0xFF10B981), Icons.Outlined.Savings)

    fun resolveCategoryVisual(category: String?, subcategory: String? = null): CategoryVisual {
        val catUpper = (category ?: "").trim().uppercase()
        val subUpper = (subcategory ?: "").trim().uppercase()
        val combined = "$catUpper $subUpper"

        if (subUpper.isNotBlank() && subcategories.containsKey(subcategory)) {
            return subcategories[subcategory]!!
        }

        return when {
            combined.contains("FOOD") || combined.contains("DINING") || combined.contains("RESTAURANT") || combined.contains("GROCER") -> FOOD
            combined.contains("SHOP") || combined.contains("CLOTH") || combined.contains("STORE") -> SHOPPING
            combined.contains("TRANSPORT") || combined.contains("TRAVEL") || combined.contains("FUEL") || combined.contains("CAB") || combined.contains("UBER") || combined.contains("OLA") -> TRANSPORT
            combined.contains("BILL") || combined.contains("UTILITY") || combined.contains("RECHARGE") || combined.contains("ELECTRIC") || combined.contains("WATER") -> BILLS
            combined.contains("ENTERTAIN") || combined.contains("MOVIE") || combined.contains("GAME") || combined.contains("CINEMA") -> ENTERTAINMENT
            combined.contains("HEALTH") || combined.contains("MEDIC") || combined.contains("HOSPITAL") || combined.contains("DOCTOR") || combined.contains("PHARM") -> HEALTHCARE
            combined.contains("INVEST") || combined.contains("MUTUAL") || combined.contains("STOCK") || combined.contains("SIP") || combined.contains("SAVING") -> INVESTMENTS
            combined.contains("SUBSCRIPT") || combined.contains("NETFLIX") || combined.contains("SPOTIFY") -> SUBSCRIPTION
            combined.contains("REWARD") || combined.contains("CASHBACK") -> REWARD
            combined.contains("TRANSFER") -> SELF_TRANSFER
            catUpper == "DEBIT" -> EXPENSE
            catUpper == "CREDIT" -> INCOME
            else -> DEFAULT.copy(title = if (category.isNullOrBlank()) "Other" else category)
        }
    }

    fun getCategoryVisual(category: String): CategoryVisual {
        return resolveCategoryVisual(category)
    }

    fun getSubcategoryVisual(subcategory: String): CategoryVisual {
        return subcategories[subcategory] ?: resolveCategoryVisual(subcategory)
    }
}
