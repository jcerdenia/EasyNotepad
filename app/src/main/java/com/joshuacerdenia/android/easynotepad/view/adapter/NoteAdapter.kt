package com.joshuacerdenia.android.easynotepad.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joshuacerdenia.android.easynotepad.R
import com.joshuacerdenia.android.easynotepad.data.model.NoteMinimal
import com.joshuacerdenia.android.easynotepad.extension.setVisibility
import java.text.DateFormat
import java.util.*

class NoteAdapter(
    private val context: Context,
    private val listener: EventListener
) : ListAdapter<NoteMinimal, NoteAdapter.NoteHolder>(DiffCallback()) {

    interface EventListener {

        fun onNoteClicked(noteID: UUID)

        fun onNoteLongClicked(noteID: UUID)

        fun onNoteCheckBoxClicked(noteID: UUID, isChecked: Boolean)
    }

    var selectedItems: Set<UUID> = setOf()

    private val checkBoxes = mutableSetOf<CheckBox>()
    var shouldShowCheckBoxes: Boolean = false
        set(value) {
            field = value
            checkBoxes.forEach { it.setVisibility(value) }
        }

    fun toggleCheckBoxes(isChecked: Boolean) {
        checkBoxes.forEach { it.isChecked = isChecked }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.list_item_note, parent, false)
        return NoteHolder(view)
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener, View.OnLongClickListener {

        private lateinit var noteID: UUID

        private val titleTextView: TextView = itemView.findViewById(R.id.note_title)
        private val infoTextView: TextView = itemView.findViewById(R.id.note_info)
        private val editCheckBox: CheckBox = itemView.findViewById(R.id.edit_checkbox)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun bind(note: NoteMinimal) {
            noteID = note.id
            titleTextView.text = note.title

            editCheckBox.apply {
                setVisibility(shouldShowCheckBoxes)
                isChecked = selectedItems.contains(note.id)
                checkBoxes.add(this)
                setOnClickListener {
                    listener.onNoteCheckBoxClicked(note.id, this.isChecked)
                }
            }

            val categoryShown = if (note.category == "") {
                context.getString(R.string.no_category)
            } else {
                note.category
            }

            val dateShown =
                if (
                    DateFormat.getDateInstance().format(Date()) ==
                    DateFormat.getDateInstance().format(note.lastModified)
                ) {
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(note.lastModified)
                } else {
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(note.lastModified)
                }

            infoTextView.text = context.getString(R.string.note_list_info, dateShown, categoryShown)
        }

        override fun onClick(v: View) {
            listener.onNoteClicked(noteID)
        }

        override fun onLongClick(v: View?): Boolean {
            //editCheckBox.isChecked = !editCheckBox.isChecked
            listener.onNoteLongClicked(noteID)
            return true
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<NoteMinimal>() {
        override fun areItemsTheSame(oldItem: NoteMinimal, newItem: NoteMinimal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NoteMinimal, newItem: NoteMinimal): Boolean {
            return oldItem.isSameAs(newItem)
        }
    }

    companion object {
        private const val TAG = "NoteAdapter"
    }
}