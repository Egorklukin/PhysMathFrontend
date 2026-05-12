package com.example.physmath.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.physmath.data.entities.LessonEntity
import com.example.physmath.data.entities.SubjectEntity
import com.example.physmath.data.entities.TestResultEntity
import com.example.physmath.data.entities.TopicEntity

@Database(entities = [LessonEntity::class, TestResultEntity::class, SubjectEntity::class, TopicEntity::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "physmath_db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }


    }
}