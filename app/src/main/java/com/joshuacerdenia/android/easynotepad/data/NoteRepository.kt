package com.joshuacerdenia.android.easynotepad.data

import androidx.lifecycle.LiveData
import com.joshuacerdenia.android.easynotepad.data.database.NoteDatabase
import com.joshuacerdenia.android.easynotepad.data.model.Note
import java.util.*
import java.util.concurrent.Executors

class NoteRepository private constructor(db: NoteDatabase) {

    private val dao = db.noteDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getNotes(): LiveData<List<Note>> = dao.getNotes()
    
    fun getNote(id: UUID): LiveData<Note?> = dao.getNote(id)

    fun updateNote(note: Note) {
        executor.execute { dao.updateNote(note) }
    }

    fun addNote(note: Note) {
        executor.execute { dao.addNote(note) }
    }

    fun deleteNotesByID(vararg noteID: UUID) {
        executor.execute { dao.deleteNotesByID(*noteID) }
    }

    companion object {

        private var INSTANCE: NoteRepository? = null

        fun init(db: NoteDatabase) {
            if (INSTANCE == null) {
                INSTANCE = NoteRepository(db)
            }
        }

        fun get(): NoteRepository {
            return INSTANCE ?: throw IllegalStateException("Repo must be initialized!")
        }
    }
}