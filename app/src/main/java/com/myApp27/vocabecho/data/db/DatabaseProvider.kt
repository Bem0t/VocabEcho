package com.myApp27.vocabecho.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    @Volatile private var db: AppDatabase? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS card_stats (
                    cardId TEXT NOT NULL PRIMARY KEY,
                    deckId TEXT NOT NULL,
                    correctCount INTEGER NOT NULL,
                    wrongCount INTEGER NOT NULL,
                    correctStreak INTEGER NOT NULL,
                    lastAnsweredEpochDay INTEGER
                )
            """.trimIndent())
        }
    }

    fun get(context: Context): AppDatabase =
        db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { db = it }
        }
}