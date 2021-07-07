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

    val noteLive: LiveData<Note?> = Transformations
        .switchMap(noteIDLive) { noteID -> repo.getNote(noteID) }

    private val noteBeforeUpdate get() = noteLive.value

    fun getNoteByID(noteID: UUID) {
        noteIDLive.value = noteID
    }

    fun submitChanges(category: String, title: String, body: String) {
        noteBeforeUpdate?.let { note ->
            if (note.isContentChanged(category, title, body)) {
                note.update(category, title, body)
                if (note.isEmpty()) {
                    repo.deleteNotesByID(note.id)
                } else {
                    repo.updateNote(note)
                }
            }
        }
    }

    fun deleteCurrentNote() {
        noteIDLive.value?.let { noteID ->
            repo.deleteNotesByID(noteID)
        }
    }
}