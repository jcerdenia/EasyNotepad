package com.joshuacerdenia.android.easynotepad

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class NoteViewModel : ViewModel() {
    
    private val noteRepository = NoteRepository.get()
    private val noteIDLiveData = MutableLiveData<UUID>() // Stores and publishes Note ID pulled from database

    var noteLiveData: LiveData<Note?> =
        Transformations.switchMap(noteIDLiveData) { // Changing note ID triggers new database query
                noteID -> noteRepository.getNote(noteID)
        }

    fun loadNote(noteID: UUID) {
        noteIDLiveData.value = noteID
    }

    fun saveNote(note: Note) {
        noteRepository.updateNote(note)
    }

    fun deleteNote(note: Note) {
        noteRepository.deleteNote(note)
    }
}