package com.myApp27.vocabecho.data.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var db: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app.db"
            ).build().also { db = it }
        }
}