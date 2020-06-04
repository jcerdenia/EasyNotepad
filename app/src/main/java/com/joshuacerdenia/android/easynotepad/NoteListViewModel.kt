package com.joshuacerdenia.android.easynotepad

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NoteListViewModel : ViewModel() {

    private val noteRepository = NoteRepository.get()
    val noteListLiveData = noteRepository.getNotes()

    var editMode: MutableLiveData<Boolean> = MutableLiveData()
    var allSelected: MutableLiveData<Boolean> = MutableLiveData()
    var allDeselected: MutableLiveData<Boolean> = MutableLiveData()
    var editables = mutableListOf<Note>()

    init {
        editMode.value = false
        allSelected.value = false
        allDeselected.value = false
    }

    fun addNote(note: Note) {
        noteRepository.addNote(note)
    }

    fun deleteNote(note: Note) {
        noteRepository.deleteNote(note)
    }

    /** For Future Implementation? Edit Categories
    fun saveNote(note: Note) {
        noteRepository.updateNote(note)
    } */
}