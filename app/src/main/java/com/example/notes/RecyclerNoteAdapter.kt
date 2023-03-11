package com.example.notes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.db.NoteDatabase
import com.example.notes.db.entities.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecyclerNoteAdapter(private val onNoteActivityResultLauncher: ActivityResultLauncher<Intent>,
                          private val context: Context, val notes: MutableList<Note>) :
    RecyclerView.Adapter<RecyclerNoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent,
            false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {

        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textViewCategory: TextView = itemView.findViewById(R.id.textViewCategory)
        private val textViewPriority: TextView = itemView.findViewById(R.id.textViewPriority)

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(note: Note) {
            textViewTitle.text = note.title

            CoroutineScope(Dispatchers.Main).launch {
                val db = NoteDatabase.getInstance(context)
                textViewCategory.text = db.categoryDao().getById(note.categoryId).name

                val priority = db.priorityDao().getById(note.priorityId)
                textViewPriority.text = priority.name

                when (priority.name) {
                    "Low" -> {
                        textViewPriority.setTextColor(Color.GREEN)
                    }
                    "Middle" -> {
                        textViewPriority.setTextColor(Color.YELLOW)
                    }
                    "High" -> {
                        textViewPriority.setTextColor(Color.RED)
                    }
                }
            }

            itemView.setOnClickListener {
                val intent = Intent(context, NoteActivity::class.java).apply {
                    putExtra("note", note as java.io.Serializable)
                }
                onNoteActivityResultLauncher.launch(intent)
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            view: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val removeAction = menu?.add(adapterPosition, 0, 0, "Remove")
            removeAction?.setOnMenuItemClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Are you sure?")
                builder.setMessage("Do you really want to perform this action?")
                builder.setPositiveButton("Yes") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = NoteDatabase.getInstance(context)
                        db.noteDao().deleteById(notes[adapterPosition].id)
                        withContext(Dispatchers.Main) {
                            addAll(db.noteDao().getAll())
                        }
                    }
                }
                builder.setNegativeButton("Cancel", null)
                val dialog = builder.create()
                dialog.show()
                true
            }

            val editAction = menu?.add(adapterPosition, 1, 1, "Edit")
            editAction?.setOnMenuItemClickListener {
                val note = notes[adapterPosition]
                val intent = Intent(context, NoteActivity::class.java).apply {
                    putExtra("note", note as java.io.Serializable)
                }
                onNoteActivityResultLauncher.launch(intent)
                true
            }
        }
    }
}