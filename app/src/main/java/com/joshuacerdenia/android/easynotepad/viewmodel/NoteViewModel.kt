package com.joshuacerdenia.android.easynotepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.joshuacerdenia.android.easynotepad.data.NoteRepository
import com.joshuacerdenia.android.easynotepad.data.model.Note
import java.util.*

class NoteViewModel(
    private val repo: NoteRepository = NoteRepository.get()
) : ViewModel() {

    private val noteIDLive = MutableLiveData<UUID>()
    var noteLive: LiveData<Note?> = Transformations
        .switchMap(noteIDLive) { noteID -> repo.getNote(noteID) }

    fun getNote(noteID: UUID) {
        noteIDLive.value = noteID
    }

    fun saveCopy() {
        // TODO
    }

    fun updateNote(note: Note) {
        repo.updateNote(note)
    }

    fun deleteNote(note: Note) {
        repo.deleteNote(note)
    }
}