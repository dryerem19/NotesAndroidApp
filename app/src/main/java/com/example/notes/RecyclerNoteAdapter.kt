package com.example.notes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import com.example.notes.db.entities.Note

class RecyclerNoteAdapter(private val context: Context, private val
    onNoteActivityResultLauncher: ActivityResultLauncher<Intent>) :
    RecyclerBaseNoteAdapter(context, onNoteActivityResultLauncher) {

    interface OnPinnedClick {
        fun onPinnedClick(note: Note)
    }

    private var onPinnedClick: OnPinnedClick? = null

    fun setOnPinnedClick(callback: OnPinnedClick) {
        onPinnedClick = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent,
            false)
        return NoteViewHolder(view)
    }

    inner class NoteViewHolder(itemView: View) :
        RecyclerBaseNoteAdapter.NoteBaseViewHolder(itemView), View.OnCreateContextMenuListener {

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            view: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            super.onCreateContextMenu(menu, view, menuInfo)

            // Pinned note
            val pinAction = menu?.add(adapterPosition, 3, 0, "Pin")
            pinAction?.setOnMenuItemClickListener {
                onPinnedClick?.onPinnedClick(getNotes()[adapterPosition])
                true
            }
        }
    }
}