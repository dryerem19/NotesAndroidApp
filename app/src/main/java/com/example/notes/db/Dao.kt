package com.example.notes.db

import androidx.room.*
import com.example.notes.db.entities.Category
import com.example.notes.db.entities.Note
import com.example.notes.db.entities.Priority

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: Int)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    @Update
    suspend fun update(note: Note)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getById(categoryId: Int) : Category

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteById(categoryId: Int)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface PriorityDao {
    @Query("SELECT * FROM priorities")
    suspend fun getAll(): List<Priority>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(priority: Priority)

    @Query("SELECT * FROM priorities WHERE id = :priorityId")
    suspend fun getById(priorityId: Int) : Category

    @Query("DELETE FROM priorities WHERE id = :priorityId")
    suspend fun deleteById(priorityId: Int)

    @Query("DELETE FROM priorities")
    suspend fun deleteAll()
}