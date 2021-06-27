package com.joshuacerdenia.android.easynotepad.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.joshuacerdenia.android.easynotepad.data.model.Note
import java.util.*

@Dao
interface NoteDao {

    @Insert
    fun addNote(note: Note)

    @Query("SELECT * FROM note")
    fun getNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM note WHERE id = (:id)")
    fun getNote(id: UUID): LiveData<Note?>

    @Update
    fun updateNote(note: Note)

    @Delete
    fun deleteNote(note: Note)

    @Query("DELETE FROM note WHERE id IN (:ids)")
    fun deleteNotesByID(ids: List<UUID>)
}