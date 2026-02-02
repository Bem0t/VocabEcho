package com.myApp27.vocabecho.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CardProgressEntity::class,
        CardStatsEntity::class,
        UserDeckEntity::class,
        UserCardEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardProgressDao(): CardProgressDao
    abstract fun cardStatsDao(): CardStatsDao
    abstract fun userDeckDao(): UserDeckDao
    abstract fun userCardDao(): UserCardDao
}