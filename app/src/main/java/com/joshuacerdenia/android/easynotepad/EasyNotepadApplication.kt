package com.joshuacerdenia.android.easynotepad

import android.app.Application
import com.joshuacerdenia.android.easynotepad.data.NotePreferences
import com.joshuacerdenia.android.easynotepad.data.NoteRepository
import com.joshuacerdenia.android.easynotepad.data.database.NoteDatabase

class EasyNotepadApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotePreferences.init(this)
        val db = NoteDatabase.build(this)
        NoteRepository.init(db)
    }
}