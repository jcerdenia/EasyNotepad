package com.joshuacerdenia.android.easynotepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joshuacerdenia.android.easynotepad.data.NotePreferences
import com.joshuacerdenia.android.easynotepad.data.NoteRepository
import com.joshuacerdenia.android.easynotepad.data.model.Note
import com.joshuacerdenia.android.easynotepad.data.model.NoteMinimal
import com.joshuacerdenia.android.easynotepad.extension.queriedBy
import com.joshuacerdenia.android.easynotepad.extension.sortedBy
import java.util.*

class NoteListViewModel(
    private val repo: NoteRepository = NoteRepository.get()
) : ViewModel() {

    private val notesDbLive = repo.getNotes()
    val notesLive = MediatorLiveData<List<NoteMinimal>>()

    private val _isManagingLive = MutableLiveData(false)
    val isManagingLive: LiveData<Boolean> get() = _isManagingLive

    private var selectedNoteIDs = mutableSetOf<UUID>()
    private val _selectedNoteIDsLive = MutableLiveData<Set<UUID>>()
    val selectedNoteIDsLive: LiveData<Set<UUID>> get() = _selectedNoteIDsLive
    val selectedItemCount get() = selectedNoteIDs.size

    var order = 0
        get() = NotePreferences.order
        private set

    private var query = ""

    init {
        notesLive.addSource(notesDbLive) { notes ->
            updateNoteList(notes)
        }
    }

    private fun updateNoteList(base: List<Note>) {
        notesLive.value = base
            .queriedBy(query)
            .sortedBy(order)
            .map { it.toMinimal() }
    }

    fun isManaging(): Boolean = _isManagingLive.value ?: false

    fun setIsManaging(isManaging: Boolean) {
        _isManagingLive.value = isManaging
        if (!isManaging) clearSelectedItems()
    }

    fun sortNotes(order: Int) {
        NotePreferences.order = order
        notesDbLive.value?.let { updateNoteList(it) }
    }

    fun submitQuery(query: String) {
        this.query = query
        notesDbLive.value?.let { updateNoteList(it) }
    }

    private fun MutableLiveData<Set<UUID>>.update() {
        this.value = selectedNoteIDs
    }

    fun replaceSelectedItems(noteIDs: List<UUID>) {
        selectedNoteIDs.clear()
        selectedNoteIDs.addAll(noteIDs)
        _selectedNoteIDsLive.update()
    }

    fun addSelection(noteID: UUID) {
        selectedNoteIDs.add(noteID)
        _selectedNoteIDsLive.update()
    }

    fun removeSelection(noteID: UUID) {
        selectedNoteIDs.remove(noteID)
        _selectedNoteIDsLive.update()
    }

    fun clearSelectedItems() {
        selectedNoteIDs.clear()
        _selectedNoteIDsLive.update()
    }

    fun addNote(note: Note) {
        repo.addNote(note)
    }

    fun deleteSelectedItems() {
        selectedNoteIDs
            .toTypedArray()
            .run { repo.deleteNotesByID(*this) }
        clearSelectedItems()
    }
}