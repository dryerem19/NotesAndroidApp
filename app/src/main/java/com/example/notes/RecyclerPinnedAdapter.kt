package com.example.notes

import android.content.Context
import android.content.Intent
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import com.example.notes.db.entities.Note

class RecyclerPinnedAdapter(context: Context, private val
                            onNoteActivityResultLauncher: ActivityResultLauncher<Intent>
) : RecyclerBaseNoteAdapter(context, onNoteActivityResultLauncher) {

    interface OnUnpinnedClick {
        fun onUnpinnedClick(note: Note)
    }

    private var onUnpinnedClick: OnUnpinnedClick? = null

    fun setOnUnpinnedClick(callback: OnUnpinnedClick) {
        onUnpinnedClick = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerPinnedAdapter.PinnedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent,
            false)
        return PinnedViewHolder(view)
    }

    inner class PinnedViewHolder(itemView: View) :
        RecyclerBaseNoteAdapter.NoteBaseViewHolder(itemView), View.OnCreateContextMenuListener {

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            view: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val unpinAction = menu?.add(adapterPosition, 3, 0, "Unpin")
            unpinAction?.setOnMenuItemClickListener {
                onUnpinnedClick?.onUnpinnedClick(getNotes()[adapterPosition])
                true
            }
        }
    }
}