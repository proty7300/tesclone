package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CloneEntity::class], version = 1, exportSchema = false)
abstract class CloneDatabase : RoomDatabase() {
    abstract fun cloneDao(): CloneDao

    companion object {
        @Volatile
        private var INSTANCE: CloneDatabase? = null

        fun getDatabase(context: Context): CloneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CloneDatabase::class.java,
                    "clone_space_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
