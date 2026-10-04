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

    fun getCategoryVisual(category: String): CategoryVisual {
        return when (category.uppercase()) {
            "DEBIT" -> EXPENSE
            "CREDIT" -> INCOME
            "BILL", "BILL_PENDING" -> BILL
            "INVESTMENT" -> INVESTMENT
            "SUBSCRIPTION" -> SUBSCRIPTION
            "REWARD", "VOUCHER" -> REWARD
            "SELF_TRANSFER" -> SELF_TRANSFER
            "IGNORE" -> IGNORE
            else -> DEFAULT
        }
    }

    fun getSubcategoryVisual(subcategory: String): CategoryVisual {
        return subcategories[subcategory] ?: DEFAULT.copy(title = subcategory)
    }
}
