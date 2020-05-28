package com.joshuacerdenia.android.easynotepad.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.joshuacerdenia.android.easynotepad.Note

@Database(entities = [ Note::class ], version=1)
@TypeConverters(NoteTypeConverters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}