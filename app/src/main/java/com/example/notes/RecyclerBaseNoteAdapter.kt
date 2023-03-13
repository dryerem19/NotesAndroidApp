package com.example.notes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.view.ContextMenu
import android.view.View
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
import java.util.Date

abstract class RecyclerBaseNoteAdapter(private val context: Context, private val
    onNoteActivityResultLauncher: ActivityResultLauncher<Intent>
)
    : RecyclerView.Adapter<RecyclerBaseNoteAdapter.NoteBaseViewHolder>() {

    private var notes = mutableListOf<Note>()

    fun getNotes(): List<Note> {
        return notes
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addNote(note: Note) {
        notes.add(note)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeNoteAt(position: Int) {
        notes.removeAt(position)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeNote(note: Note) {
        notes.remove(note)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: NoteBaseViewHolder, position: Int) {
        val note = getNotes()[position]
        holder.bind(note)
    }

    abstract inner class NoteBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {

        private val textViewTitle    = itemView.findViewById<TextView>(R.id.textViewTitle)
        private val textViewDate = itemView.findViewById<TextView>(R.id.note_date_created)

        open fun bind(note: Note) {

            textViewTitle.text = note.title

            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm")
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            val formattedDate = sdf.format(note.createDate)
            textViewDate.text = formattedDate

            CoroutineScope(Dispatchers.Main).launch {
                val db = NoteDatabase.getInstance(context)
                itemView.setOnClickListener {
                    val intent = Intent(context, NoteActivity::class.java).apply {
                        putExtra("note", note)
                    }
                    onNoteActivityResultLauncher.launch(intent)
                }
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            view: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {

            val editAction = menu?.add(adapterPosition, 0, 0, "Edit")
            editAction?.setOnMenuItemClickListener {
                val note = getNotes()[adapterPosition]
                val intent = Intent(context, NoteActivity::class.java).apply {
                    putExtra("note", note)
                }
                onNoteActivityResultLauncher.launch(intent)
                true
            }

            val removeAction = menu?.add(adapterPosition, 1, 1, "Remove")
            removeAction?.setOnMenuItemClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Are you sure?")
                builder.setMessage("Do you really want to perform this action?")
                builder.setPositiveButton("Yes") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = NoteDatabase.getInstance(context)
                        db.noteDao().deleteById(getNotes()[adapterPosition].id)
                        withContext(Dispatchers.Main) {
                            setNotes(db.noteDao().getAll())
                        }
                    }
                }
                builder.setNegativeButton("Cancel", null)
                val dialog = builder.create()
                dialog.show()
                true
            }
        }
    }
}