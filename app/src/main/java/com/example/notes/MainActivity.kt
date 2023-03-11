package com.example.notes

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.db.NoteDatabase
import com.example.notes.db.entities.Category
import com.example.notes.db.entities.Priority
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var notesView: RecyclerView
    private lateinit var adapter: RecyclerNoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notesView = findViewById(R.id.notesList)

        adapter = RecyclerNoteAdapter(noteActivityResultLauncher, this, mutableListOf())
        notesView.adapter = adapter
        notesView.layoutManager = LinearLayoutManager(this)
        loadNotes(this)

        val addNoteButton = findViewById<FloatingActionButton>(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            noteActivityResultLauncher.launch(intent)
        }
    }

    private val noteActivityResultLauncher = registerForActivityResult(ActivityResultContracts.
        StartActivityForResult()) { _ ->
        loadNotes(this)
    }

    private fun loadNotes(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = NoteDatabase.getInstance(context)
            val noteDao = db.noteDao()
            val notes = noteDao.getAll()
            withContext(Dispatchers.Main) {
                adapter.addAll(notes)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_clear -> {
                val context = this
                CoroutineScope(Dispatchers.IO).launch {
                    val db = NoteDatabase.getInstance(context)
                    db.noteDao().deleteAll()
                }
                adapter.addAll(emptyList())
                true
            }
            R.id.menu_add_category -> {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Add Category")
                val editText = EditText(this)
                dialog.setView(editText)
                dialog.setPositiveButton("Add") { _, _ ->
                    val categoryName = editText.text.toString()

                    val context = this
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = NoteDatabase.getInstance(context)
                        val categoryDao = db.categoryDao()
                        categoryDao.insert(Category(name = categoryName))
                    }
                }
                dialog.setNegativeButton("Cancel", null)
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}