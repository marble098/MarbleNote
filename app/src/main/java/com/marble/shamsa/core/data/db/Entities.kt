package com.marble.shamsa.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.marble.shamsa.core.model.*

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String,
    val dueAtMillis: Long,
    val priority: Int,
    val categoryId: String?,
    val icon: String,
    val colorArgb: Long,
    val status: String,
    val popupEnabled: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val deletedAtMillis: Long?
) {
    fun toModel() = Reminder(id, title, notes, dueAtMillis, ReminderPriority.entries.first { it.level == priority }, categoryId, icon, colorArgb, ReminderStatus.valueOf(status), popupEnabled, createdAtMillis, updatedAtMillis, deletedAtMillis)
    companion object {
        fun fromModel(v: Reminder) = ReminderEntity(v.id, v.title, v.notes, v.dueAtMillis, v.priority.level, v.categoryId, v.icon, v.colorArgb, v.status.name, v.popupEnabled, v.createdAtMillis, v.updatedAtMillis, v.deletedAtMillis)
    }
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val colorArgb: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val deletedAtMillis: Long?
) {
    fun toModel() = Category(id, name, icon, colorArgb, createdAtMillis, updatedAtMillis, deletedAtMillis)
    companion object { fun fromModel(v: Category) = CategoryEntity(v.id, v.name, v.icon, v.colorArgb, v.createdAtMillis, v.updatedAtMillis, v.deletedAtMillis) }
}
