package com.marble.shamsa.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReminderEntity::class, CategoryEntity::class], version = 1, exportSchema = true)
abstract class ShamsaDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun categoryDao(): CategoryDao
}
