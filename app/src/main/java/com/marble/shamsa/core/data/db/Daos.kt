package com.marble.shamsa.core.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE deletedAtMillis IS NULL ORDER BY dueAtMillis ASC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE deletedAtMillis IS NULL AND status = 'ACTIVE' AND dueAtMillis >= :now ORDER BY dueAtMillis ASC")
    suspend fun upcoming(now: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders")
    suspend fun allForSync(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): ReminderEntity?

    @Upsert
    suspend fun upsert(entity: ReminderEntity)

    @Upsert
    suspend fun upsertAll(entities: List<ReminderEntity>)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE deletedAtMillis IS NULL ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun allForSync(): List<CategoryEntity>

    @Upsert
    suspend fun upsert(entity: CategoryEntity)

    @Upsert
    suspend fun upsertAll(entities: List<CategoryEntity>)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE deletedAtMillis IS NULL ORDER BY pinned DESC, updatedAtMillis DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes")
    suspend fun allForSync(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): NoteEntity?

    @Upsert
    suspend fun upsert(entity: NoteEntity)

    @Upsert
    suspend fun upsertAll(entities: List<NoteEntity>)
}
