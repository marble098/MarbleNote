package com.marble.shamsa.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.marble.shamsa.core.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notes` (
                    `id` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `body` TEXT NOT NULL,
                    `colorArgb` INTEGER NOT NULL,
                    `pinned` INTEGER NOT NULL,
                    `createdAtMillis` INTEGER NOT NULL,
                    `updatedAtMillis` INTEGER NOT NULL,
                    `deletedAtMillis` INTEGER,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): ShamsaDatabase =
        Room.databaseBuilder(
            context,
            ShamsaDatabase::class.java,
            "shamsa.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun reminderDao(db: ShamsaDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun categoryDao(db: ShamsaDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun noteDao(db: ShamsaDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun http(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
