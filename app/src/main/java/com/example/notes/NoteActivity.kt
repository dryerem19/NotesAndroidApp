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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class NoteActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerPriority: Spinner
    private lateinit var editTextNote: EditText

    private lateinit var buttonCancel: Button
    private lateinit var buttonSave: Button

    private lateinit var categories: List<Category>
    private lateinit var priorities: List<Priority>

    private var currentNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        editTextTitle = findViewById(R.id.editTextTitle)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        editTextNote = findViewById(R.id.editTextNote)

        buttonCancel = findViewById(R.id.buttonCancel)
        buttonSave = findViewById(R.id.buttonSave)

        // Get data from database
        categories = emptyList()
        priorities = emptyList()
        CoroutineScope(Dispatchers.Main).launch {
            loadCategories()
            loadPriorities()
        }

        // Load note for edit if it exist
        val noteString = intent.getStringExtra("note")
        if (noteString != null) {
            val currentNote = Json.decodeFromString<Note>(noteString)
            editTextTitle.setText(currentNote.title)
            spinnerCategory.setSelection(categories.indexOfFirst {
                it.id == currentNote.categoryId
            })
            spinnerPriority.setSelection(priorities.indexOfFirst {
                it.id == currentNote.priorityId
            })
            editTextNote.setText(currentNote.text)
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

    private suspend fun loadPriorities() {
        val priorityDao = NoteDatabase.getInstance(this).priorityDao()
        priorities = priorityDao.getAll()
        spinnerPriority.apply {
            adapter = ArrayAdapter(this@NoteActivity,
                android.R.layout.simple_spinner_item, priorities).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(0)
        }
    }

    private suspend fun saveNote() {
        val title = editTextTitle.text.toString()
        val category = spinnerCategory.selectedItem as? Category
        val priority = spinnerPriority.selectedItem as? Priority
        val text = editTextNote.text.toString()

        val noteDao = NoteDatabase.getInstance(this).noteDao()
        if (title.isEmpty() || category == null || priority == null || text.isEmpty()) {
            runOnUiThread {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            val note = Note(
                id = currentNote?.id ?: 0,
                title = title,
                categoryId = category.id,
                priorityId = priority.id,
                text = text
            )
            if (currentNote != null) {
                var note: Note = currentNote as Note
                note.title = title
                note.categoryId = category.id
                note.priorityId = priority.id
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