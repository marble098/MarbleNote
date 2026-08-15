package com.marble.shamsa.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReminderStatus { ACTIVE, COMPLETED }

@Serializable
enum class ReminderPriority(val level: Int) { LOW(0), NORMAL(1), HIGH(2), URGENT(3) }

@Serializable
enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Serializable
enum class DisplayMode { COMPACT, CARDS, FOCUS }

@Serializable
enum class ReminderFilter { ALL, TODAY, UPCOMING, COMPLETED }

@Serializable
enum class ReminderSort { DUE, PRIORITY, CREATED }

@Serializable
data class Reminder(
    val id: String,
    val title: String,
    val notes: String = "",
    val dueAtMillis: Long,
    val priority: ReminderPriority = ReminderPriority.NORMAL,
    val categoryId: String? = null,
    val icon: String = "event",
    val colorArgb: Long = 0xFF7C4DFF,
    val status: ReminderStatus = ReminderStatus.ACTIVE,
    val popupEnabled: Boolean = true,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val deletedAtMillis: Long? = null
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val icon: String = "folder",
    val colorArgb: Long = 0xFF00B8D9,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val deletedAtMillis: Long? = null
)

@Serializable
data class CloudSnapshot(
    val schemaVersion: Int = 1,
    val generatedAtMillis: Long,
    val deviceId: String,
    val reminders: List<Reminder>,
    val categories: List<Category>
)
