package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Bottle::class,
        SubBottle::class,
        Review::class,
        Blind::class,
        BlindReveal::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BourbonDatabase : RoomDatabase() {
    abstract fun bourbonDao(): BourbonDao

    companion object {
        @Volatile
        private var INSTANCE: BourbonDatabase? = null

        fun getDatabase(context: Context): BourbonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BourbonDatabase::class.java,
                    "bourbon_journal_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
