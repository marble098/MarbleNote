package com.marble.shamsa.core.design

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconCatalog {
    val reminderKeys = listOf("event", "alarm", "work", "study", "health", "shopping", "travel", "birthday", "star")
    val categoryKeys = listOf("folder", "work", "study", "health", "shopping", "travel", "home", "favorite", "star")

    fun icon(key: String): ImageVector = when (key) {
        "alarm" -> Icons.Rounded.Alarm
        "work" -> Icons.Rounded.Work
        "study" -> Icons.Rounded.School
        "health" -> Icons.Rounded.Favorite
        "shopping" -> Icons.Rounded.ShoppingBag
        "travel" -> Icons.Rounded.Flight
        "birthday" -> Icons.Rounded.Cake
        "home" -> Icons.Rounded.Home
        "favorite" -> Icons.Rounded.Favorite
        "star" -> Icons.Rounded.Star
        "folder" -> Icons.Rounded.Folder
        else -> Icons.Rounded.Event
    }
}

object AccentPresets {
    val values = listOf(
        0xFF6D4AFF,
        0xFFFF4F9A,
        0xFF00A7C7,
        0xFF00A86B,
        0xFFFF8A00,
        0xFFE53935,
        0xFF3949AB,
        0xFF8E24AA
    )
}
