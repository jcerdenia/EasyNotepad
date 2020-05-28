package com.joshuacerdenia.android.easynotepad

import android.app.Application

class EasyNotepadApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NoteRepository.initialize(this)
    }
}