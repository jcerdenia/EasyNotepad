package com.joshuacerdenia.android.easynotepad.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.joshuacerdenia.android.easynotepad.data.database.NoteDatabase
import com.joshuacerdenia.android.easynotepad.data.model.Note
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "note-database"

class NoteRepository private constructor(context: Context) {

    private val database: NoteDatabase = Room.databaseBuilder(
        context.applicationContext,
        NoteDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val dao = database.noteDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getNotes(): LiveData<List<Note>> = dao.getNotes()
    
    fun getNote(id: UUID): LiveData<Note?> = dao.getNote(id)

    fun updateNote(note: Note) {
        executor.execute {
            dao.updateNote(note)
        }
    }

    fun addNote(note: Note) {
        executor.execute {
            dao.addNote(note)
        }
    }
    
    fun deleteNote(note: Note) {
        executor.execute {
            dao.deleteNote(note)
        }
    }

    fun deleteNotesByID(ids: List<UUID>) {
        executor.execute {
            dao.deleteNotesByID(ids)
        }
    }

    companion object {
        private var INSTANCE: NoteRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = NoteRepository(context)
            }
        }

        fun get(): NoteRepository {
            return INSTANCE ?: throw IllegalStateException("NoteRepository must be initialized!")
        }
    }
}