package com.explorify.explorifyapp.data.remote.room

import androidx.room.Database
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AuthToken::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authTokenDao(): AuthTokenDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "explorify_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
