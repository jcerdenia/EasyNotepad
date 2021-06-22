package com.joshuacerdenia.android.easynotepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.joshuacerdenia.android.easynotepad.data.Note
import com.joshuacerdenia.android.easynotepad.data.NoteRepository
import java.util.*

class NoteViewModel : ViewModel() {
    
    private val noteRepository = NoteRepository.get()
    private val noteIDLiveData = MutableLiveData<UUID>() // Stores and publishes noteID from DB

    var noteLiveData: LiveData<Note?> =
        Transformations.switchMap(noteIDLiveData) { // Changing noteID triggers new DB query
                noteID -> noteRepository.getNote(noteID)
        }

    var noteBeforeChanged: Note = Note()
    var notYetCopied: Boolean = true

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