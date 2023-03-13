package com.example.notes

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.db.NoteDatabase
import com.example.notes.db.entities.Category
import com.example.notes.db.entities.Note
import com.example.notes.db.entities.PinnedNote
import com.example.notes.db.entities.Priority
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var noteAdapter: RecyclerNoteAdapter
    private lateinit var pinnedAdapter: RecyclerPinnedAdapter
    private lateinit var categoryAdapter: RecyclerCategoryAdapter

    private lateinit var scrollviewNotes: ScrollView
    private lateinit var emptyNotesTextview: TextView
    private lateinit var categoryRecycleView: RecyclerView
    private lateinit var noteRecycleView: RecyclerView
    private lateinit var notesHeader: TextView
    private lateinit var pinnedRecycleView: RecyclerView
    private lateinit var pinnedNotesHeader: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val context = this

        // Initialize
        setupAdapters(this)
        updateCategories(this)
        updateNotes(this)

        // Handler for filter by category
        categoryAdapter.setOnItemClickListener(object : RecyclerCategoryAdapter.OnItemClickListener {
            override fun onItemClicked(category: Category) {
                updateNotes(context)
            }
        })

        // Handler for unpinned context menu click
        pinnedAdapter.setOnUnpinnedClick(object : RecyclerPinnedAdapter.OnUnpinnedClick {
            override fun onUnpinnedClick(note: Note) {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = NoteDatabase.getInstance(context)
                    db.noteDao().unpinNote(note.id)
                    withContext(Dispatchers.Main) {
                        updateNotes(context)
                    }
                }
            }
        })

        // Handler for pinned context menu click
        noteAdapter.setOnPinnedClick(object : RecyclerNoteAdapter.OnPinnedClick {
            override fun onPinnedClick(note: Note) {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = NoteDatabase.getInstance(context)
                    db.noteDao().insertPinned(PinnedNote(noteId = note.id))
                    withContext(Dispatchers.Main) {
                        updateNotes(context)
                    }
                }
            }
        })

        // Button click for jump detail page
        val addNoteButton = findViewById<FloatingActionButton>(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            noteActivityResultLauncher.launch(intent)
        }
    }

    private val noteActivityResultLauncher = registerForActivityResult(ActivityResultContracts.
        StartActivityForResult()) { _ ->
        updateNotes(this)
    }

    private fun setupAdapters(context: Context) {

        emptyNotesTextview = findViewById(R.id.empty_notes_textview)
        scrollviewNotes = findViewById(R.id.scrollview_notes)

        // Setup categories adapter
        categoryRecycleView = findViewById(R.id.categories)
        categoryRecycleView.apply {
            categoryAdapter = RecyclerCategoryAdapter()
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false)
        }

        // Setup pinned notes adapter
        pinnedNotesHeader = findViewById(R.id.pinnedNotesHeader)
        pinnedRecycleView = findViewById(R.id.pinned_notes)
        pinnedRecycleView.apply {
            pinnedAdapter = RecyclerPinnedAdapter(context, noteActivityResultLauncher)
            adapter = pinnedAdapter
            layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false)
        }

        // Setup other notes adapter
        notesHeader = findViewById(R.id.otherNotesHeader)
        noteRecycleView = findViewById(R.id.other_notes_recycler_view)
        noteRecycleView.apply {
            noteAdapter = RecyclerNoteAdapter(context, noteActivityResultLauncher)
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun updateCategories(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            // Load the data from database
            val db = NoteDatabase.getInstance(context)
            val categories = mutableListOf<Category>()
            categories.addAll(db.categoryDao().getAll())

            // Add 'all' entry
            categories.add(0, Category(0, "All"))

            // Load the data into adapter
            withContext(Dispatchers.Main) {
                categoryAdapter.addAll(categories)
            }
        }
    }

    fun updateNotes(context: Context) {
        val selectedCategory: Category? = categoryAdapter.getSelectedCategory()
        CoroutineScope(Dispatchers.IO).launch {
            val db = NoteDatabase.getInstance(context)

            // Get the actual data
            val notes = mutableListOf<Note>()
            val pinned = mutableListOf<Note>()
            if ((selectedCategory != null) && (selectedCategory.name != "All")) {
                notes.addAll(db.noteDao().getByCategoryId(selectedCategory.id))
                pinned.addAll(db.noteDao().getPinnedByCategoryId(selectedCategory.id))
            } else {
                notes.addAll(db.noteDao().getAll())
                pinned.addAll(db.noteDao().getPinnedNotes())
            }

            // Setup the actual data to adapters
            withContext(Dispatchers.Main) {
                noteAdapter.setNotes(notes)
                pinnedAdapter.setNotes(pinned)

                scrollviewNotes.setBackgroundColor(Color.parseColor("#EFE2DC"))
                emptyNotesTextview.visibility = View.GONE

                if (notes.isEmpty()) {
                    noteRecycleView.visibility = View.GONE
                    notesHeader.visibility = View.GONE
                } else {
                    noteRecycleView.visibility = View.VISIBLE
                    notesHeader.visibility = View.VISIBLE
                }

                if (pinned.isEmpty()) {
                    pinnedRecycleView.visibility = View.GONE
                    pinnedNotesHeader.visibility = View.GONE
                } else {
                    pinnedRecycleView.visibility = View.VISIBLE
                    pinnedNotesHeader.visibility = View.VISIBLE
                }

                if (notes.isEmpty() && pinned.isEmpty()) {
                    scrollviewNotes.setBackgroundColor(Color.TRANSPARENT)
                    emptyNotesTextview.visibility = View.VISIBLE
                }
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
                noteAdapter.setNotes(emptyList())
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