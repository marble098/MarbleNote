package com.marble.shamsa.core.data

import androidx.room.withTransaction
import com.marble.shamsa.core.data.db.*
import com.marble.shamsa.core.model.*
import com.marble.shamsa.core.reminder.ReminderScheduler
import com.marble.shamsa.core.work.WorkBootstrap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val db: ShamsaDatabase,
    private val reminderDao: ReminderDao,
    private val categoryDao: CategoryDao,
    private val noteDao: NoteDao,
    private val scheduler: ReminderScheduler,
    private val work: WorkBootstrap
) {
    val reminders: Flow<List<Reminder>> =
        reminderDao.observeAll().map { list -> list.map(ReminderEntity::toModel) }

    val categories: Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map(CategoryEntity::toModel) }

    val notes: Flow<List<Note>> =
        noteDao.observeAll().map { list -> list.map(NoteEntity::toModel) }

    suspend fun saveReminder(value: Reminder) {
        val now = System.currentTimeMillis()
        val normalized = value.copy(
            id = value.id.ifBlank { UUID.randomUUID().toString() },
            createdAtMillis = value.createdAtMillis.takeIf { it > 0 } ?: now,
            updatedAtMillis = now,
            deletedAtMillis = null
        )
        reminderDao.upsert(ReminderEntity.fromModel(normalized))
        if (normalized.status == ReminderStatus.ACTIVE) {
            scheduler.schedule(normalized)
        } else {
            scheduler.cancel(normalized.id)
        }
        work.enqueueCloudSync()
        work.updateWidgets()
    }

    suspend fun complete(id: String) {
        val old = reminderDao.byId(id)?.toModel() ?: return
        saveReminder(old.copy(status = ReminderStatus.COMPLETED))
    }

    suspend fun cancel(id: String) {
        val old = reminderDao.byId(id)?.toModel() ?: return
        saveReminder(old.copy(status = ReminderStatus.CANCELED))
    }

    suspend fun delete(id: String) {
        val old = reminderDao.byId(id)?.toModel() ?: return
        val now = System.currentTimeMillis()
        reminderDao.upsert(
            ReminderEntity.fromModel(
                old.copy(updatedAtMillis = now, deletedAtMillis = now)
            )
        )
        scheduler.cancel(id)
        work.enqueueCloudSync()
        work.updateWidgets()
    }

    suspend fun snooze(id: String, minutes: Int = 10) {
        val old = reminderDao.byId(id)?.toModel() ?: return
        saveReminder(
            old.copy(
                dueAtMillis = System.currentTimeMillis() + minutes * 60_000L,
                status = ReminderStatus.ACTIVE
            )
        )
    }

    suspend fun saveCategory(value: Category) {
        val now = System.currentTimeMillis()
        val normalized = value.copy(
            id = value.id.ifBlank { UUID.randomUUID().toString() },
            createdAtMillis = value.createdAtMillis.takeIf { it > 0 } ?: now,
            updatedAtMillis = now,
            deletedAtMillis = null
        )
        categoryDao.upsert(CategoryEntity.fromModel(normalized))
        work.enqueueCloudSync()
    }

    suspend fun saveNote(value: Note) {
        val now = System.currentTimeMillis()
        val normalized = value.copy(
            id = value.id.ifBlank { UUID.randomUUID().toString() },
            createdAtMillis = value.createdAtMillis.takeIf { it > 0 } ?: now,
            updatedAtMillis = now,
            deletedAtMillis = null
        )
        noteDao.upsert(NoteEntity.fromModel(normalized))
        work.enqueueCloudSync()
    }

    suspend fun deleteNote(id: String) {
        val old = noteDao.byId(id)?.toModel() ?: return
        val now = System.currentTimeMillis()
        noteDao.upsert(
            NoteEntity.fromModel(
                old.copy(updatedAtMillis = now, deletedAtMillis = now)
            )
        )
        work.enqueueCloudSync()
    }

    suspend fun get(id: String): Reminder? = reminderDao.byId(id)?.toModel()

    suspend fun getNote(id: String): Note? = noteDao.byId(id)?.toModel()

    suspend fun upcoming(limit: Int = 8): List<Reminder> =
        reminderDao.upcoming(System.currentTimeMillis())
            .take(limit)
            .map(ReminderEntity::toModel)

    suspend fun snapshot(deviceId: String): CloudSnapshot = CloudSnapshot(
        generatedAtMillis = System.currentTimeMillis(),
        deviceId = deviceId,
        reminders = reminderDao.allForSync().map(ReminderEntity::toModel),
        categories = categoryDao.allForSync().map(CategoryEntity::toModel),
        notes = noteDao.allForSync().map(NoteEntity::toModel)
    )

    suspend fun merge(snapshot: CloudSnapshot) = db.withTransaction {
        val localReminders = reminderDao.allForSync().associateBy { it.id }
        val mergedReminders = snapshot.reminders.mapNotNull { remote ->
            val local = localReminders[remote.id]?.toModel()
            if (local == null || remote.updatedAtMillis > local.updatedAtMillis) {
                ReminderEntity.fromModel(remote)
            } else {
                null
            }
        }
        if (mergedReminders.isNotEmpty()) {
            reminderDao.upsertAll(mergedReminders)
        }

        val localCategories = categoryDao.allForSync().associateBy { it.id }
        val mergedCategories = snapshot.categories.mapNotNull { remote ->
            val local = localCategories[remote.id]?.toModel()
            if (local == null || remote.updatedAtMillis > local.updatedAtMillis) {
                CategoryEntity.fromModel(remote)
            } else {
                null
            }
        }
        if (mergedCategories.isNotEmpty()) {
            categoryDao.upsertAll(mergedCategories)
        }

        val localNotes = noteDao.allForSync().associateBy { it.id }
        val mergedNotes = snapshot.notes.mapNotNull { remote ->
            val local = localNotes[remote.id]?.toModel()
            if (local == null || remote.updatedAtMillis > local.updatedAtMillis) {
                NoteEntity.fromModel(remote)
            } else {
                null
            }
        }
        if (mergedNotes.isNotEmpty()) {
            noteDao.upsertAll(mergedNotes)
        }
    }

    suspend fun rescheduleAll() {
        upcoming(200).forEach { scheduler.schedule(it) }
    }
}
