package com.joshuacerdenia.android.easynotepad.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.joshuacerdenia.android.easynotepad.data.model.Note
import java.util.*

@Dao
interface NoteDao {

    @Insert
    fun addNote(note: Note)

    @Query("SELECT * FROM note")
    fun getNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM note WHERE id = (:noteID)")
    fun getNote(noteID: UUID): LiveData<Note?>

    @Update
    fun updateNote(note: Note)

    @Query("DELETE FROM note WHERE id IN (:noteID)")
    fun deleteNotesByID(vararg noteID: UUID)
}