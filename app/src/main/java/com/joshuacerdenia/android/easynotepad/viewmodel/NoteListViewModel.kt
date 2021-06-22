package com.joshuacerdenia.android.easynotepad.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joshuacerdenia.android.easynotepad.data.NoteRepository
import com.joshuacerdenia.android.easynotepad.data.model.Note
import java.util.*

class NoteListViewModel : ViewModel() {

    private val repo = NoteRepository.get()

    val notesLive = repo.getNotes()

    var isManagingLive: MutableLiveData<Boolean> = MutableLiveData()
    var selectedItems = mutableListOf<Note>()

    init {
        isManagingLive.value = false
    }

    fun setIsManaging(isManaging: Boolean) {
        isManagingLive.value = isManaging
    }

    fun addNote(note: Note) {
        repo.addNote(note)
    }

    fun deleteNotesByID(ids: List<UUID>) {
        repo.deleteNotesByID(ids)
    }

    /** For Future Implementation? Edit Categories
    fun saveNote(note: Note) {
        repo.updateNote(note)
    } */
}