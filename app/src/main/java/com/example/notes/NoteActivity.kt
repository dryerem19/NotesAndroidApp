package com.example.notes

import com.example.notes.db.NoteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.notes.db.entities.Category
import com.example.notes.db.entities.Note
import com.example.notes.db.entities.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class NoteActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var editTextNote: EditText

    private lateinit var buttonCancel: Button
    private lateinit var buttonSave: Button

    private lateinit var categories: List<Category>
    private lateinit var priorities: List<Priority>

    private var currentNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        editTextTitle = findViewById(R.id.edit_text_title)
        spinnerCategory = findViewById(R.id.spinner_category)
        editTextNote = findViewById(R.id.edit_text_note)

        buttonCancel = findViewById(R.id.button_cancel)
        buttonSave = findViewById(R.id.button_save)

        // Get data from database
        categories = emptyList()
        priorities = emptyList()
        CoroutineScope(Dispatchers.Main).launch {
            loadCategories()
        }

        // Load note for edit if it exist
        currentNote = intent.getParcelableExtra("note")
        if (currentNote != null) {
            editTextTitle.setText(currentNote!!.title)
            spinnerCategory.setSelection(categories.indexOfFirst {
                it.id == currentNote!!.categoryId
            })
            editTextNote.setText(currentNote!!.text)
        }

        buttonSave.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                saveNote()
            }
        }

        buttonCancel.setOnClickListener {
            showCancelConfirmationDialog()
        }
    }

    private suspend fun loadCategories() {
        val categoryDao = NoteDatabase.getInstance(this).categoryDao()
        categories =  categoryDao.getAll()
        spinnerCategory.apply {
            adapter = CategorySpinnerAdapter(categories, this@NoteActivity)
            setSelection(0)
        }

    }

    private suspend fun saveNote() {
        val title = editTextTitle.text.toString()
        val category = spinnerCategory.selectedItem as? Category
        val text = editTextNote.text.toString()

        val noteDao = NoteDatabase.getInstance(this).noteDao()
        if (title.isEmpty() || category == null || text.isEmpty()) {
            runOnUiThread {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            val note = Note(
                id = currentNote?.id ?: 0,
                title = title,
                categoryId = category.id,
                createDate = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)).time,
                text = text
            )
            if (currentNote != null) {
                var note: Note = currentNote as Note
                note.title = title
                note.categoryId = category.id
                note.text = text
                noteDao.update(note)
            } else {
                noteDao.insert(note)
            }
            finish()
        }
    }

    private fun showCancelConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cancel")
            .setMessage("Are you sure you want to cancel?")
            .setPositiveButton("Yes") { _, _ ->
                setResult(RESULT_CANCELED)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}