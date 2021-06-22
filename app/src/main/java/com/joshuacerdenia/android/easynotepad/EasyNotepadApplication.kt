package com.joshuacerdenia.android.easynotepad

import android.app.Application
import com.joshuacerdenia.android.easynotepad.data.NoteRepository

class EasyNotepadApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NoteRepository.initialize(this)
    }
}