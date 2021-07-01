package com.joshuacerdenia.android.easynotepad.data

import android.content.Context
import android.content.SharedPreferences

object NotePreferences {

    private const val NAME = "note_preferences"
    private lateinit var preferences: SharedPreferences

    private const val ORDER = "order"

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var order: Int
        get() = preferences.getInt(ORDER, 0)
        set(value) = preferences.edit { it.putInt(ORDER, value) }
}