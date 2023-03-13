package com.example.notes.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notes.db.entities.Category
import com.example.notes.db.entities.Note
import com.example.notes.db.entities.PinnedNote
import com.example.notes.db.entities.Priority

@Database(entities = [Note::class, Category::class,
                     PinnedNote::class], version = 5)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val DATABASE_NAME = "note_database"

        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase {
            Log.d("com.example.notes.Db.NoteDatabase", "Context: $context")
            Log.d("com.example.notes.Db.NoteDatabase", "Database name: $DATABASE_NAME")

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java, DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}