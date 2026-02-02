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

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS user_decks (
                    id TEXT NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    createdAtEpochDay INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS user_cards (
                    id TEXT NOT NULL PRIMARY KEY,
                    deckId TEXT NOT NULL,
                    front TEXT NOT NULL,
                    back TEXT NOT NULL,
                    createdAtEpochDay INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_cards_deckId ON user_cards(deckId)")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE user_decks ADD COLUMN imageUri TEXT")
        }
    }

    /**
     * Migration 4->5: Add card type support for Anki-like card modes.
     * Adds columns: type, clozeText, clozeAnswer, clozeHint
     * Existing cards default to BASIC type.
     */
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add type column with default BASIC for existing rows
            db.execSQL("ALTER TABLE user_cards ADD COLUMN type TEXT DEFAULT 'BASIC'")
            // Add cloze-related columns (nullable)
            db.execSQL("ALTER TABLE user_cards ADD COLUMN clozeText TEXT")
            db.execSQL("ALTER TABLE user_cards ADD COLUMN clozeAnswer TEXT")
            db.execSQL("ALTER TABLE user_cards ADD COLUMN clozeHint TEXT")
        }
    }

    fun get(context: Context): AppDatabase =
        db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                .also { db = it }
        }
}