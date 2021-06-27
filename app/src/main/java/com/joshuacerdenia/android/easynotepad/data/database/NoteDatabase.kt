package com.joshuacerdenia.android.easynotepad.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.joshuacerdenia.android.easynotepad.data.model.Note

@Database(entities = [Note::class], version=1)
@TypeConverters(NoteTypeConverters::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {

        private const val NAME = "note_database"

        fun build(context: Context): NoteDatabase {
            return Room
                .databaseBuilder(context, NoteDatabase::class.java, NAME)
                .build()
        }
    }
}