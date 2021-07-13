package com.joshuacerdenia.android.easynotepad.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joshuacerdenia.android.easynotepad.R
import com.joshuacerdenia.android.easynotepad.data.model.NoteMinimal
import com.joshuacerdenia.android.easynotepad.databinding.ListItemNoteBinding
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
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemNoteBinding.inflate(inflater, parent, false)
        checkBoxes.add(binding.editCheckBox)
        return NoteHolder(context, binding, listener)
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        val note = getItem(position)
        val isChecked = selectedItems.contains(note.id)
        holder.bind(note, shouldShowCheckBoxes, isChecked)
    }

    class NoteHolder(
        private val context: Context,
        private val binding: ListItemNoteBinding,
        private val listener: EventListener
    ) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener,
        View.OnLongClickListener {

        private lateinit var noteID: UUID

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun bind(note: NoteMinimal, isCheckBoxVisible: Boolean, isChecked: Boolean) {
            noteID = note.id

            binding.titleTextView.text = note.title.run {
                if (this.isNotEmpty()) this else context.getString(R.string.no_title)
            }

            binding.categoryTextView.text = note.category.run {
                if (this.isNotEmpty()) this else context.getString(R.string.no_category)
            }

            binding.dateTimeTextView.text = note.lastModified.run {
                val dateNow = DateFormat.getDateInstance().format(Date())
                val dateModified = DateFormat.getDateInstance().format(this)
                if (dateNow == dateModified) {
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(this)
                } else {
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(this)
                }
            }

            binding.editCheckBox.apply {
                setVisibility(isCheckBoxVisible)
                this.isChecked = isChecked
                setOnClickListener {
                    listener.onNoteCheckBoxClicked(note.id, this.isChecked)
                }
            }
        }

        override fun onClick(v: View) {
            listener.onNoteClicked(noteID)
        }

        override fun onLongClick(v: View?): Boolean {
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
}