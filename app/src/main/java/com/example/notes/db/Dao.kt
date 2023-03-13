package com.example.notes.db

import androidx.room.*
import com.example.notes.db.entities.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE id NOT IN (SELECT noteId FROM pinned_notes)")
    suspend fun getAll(): List<Note>

    @Query("SELECT * FROM notes WHERE id IN (SELECT noteId FROM pinned_notes)")
    fun getPinnedNotes(): List<Note>

    @Query("DELETE FROM pinned_notes WHERE noteId = :noteId")
    fun unpinNote(noteId: Int)

    @Query("SELECT * FROM notes WHERE category_id = :id")
    suspend fun getByCategoryId(id: Int) : List<Note>

    @Query("SELECT * FROM (SELECT * FROM notes WHERE id IN (SELECT noteId FROM pinned_notes)) WHERE category_id = :id")
    suspend fun getPinnedByCategoryId(id: Int) : List<Note>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getById(noteId: Int) : Note

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPinned(pinnedNote: PinnedNote): Long

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
