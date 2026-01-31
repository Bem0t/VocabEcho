package com.myApp27.vocabecho.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CardProgressEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardProgressDao(): CardProgressDao
}