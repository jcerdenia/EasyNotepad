package com.joshuacerdenia.android.easynotepad

import androidx.lifecycle.ViewModel

class NoteListViewModel : ViewModel() {

    private val noteRepository = NoteRepository.get()
    val noteListLiveData = noteRepository.getNotes()

    var editMode = false
    var editables = mutableListOf<Note>()
    var selectAll = true

    fun addNote(note: Note) {
        noteRepository.addNote(note)
    }

    fun deleteNote(note: Note) {
        noteRepository.deleteNote(note)
    }

    // for editing categories
    fun saveNote(note: Note) {
        noteRepository.updateNote(note)
    }
}