package com.joshuacerdenia.android.easynotepad.data

import android.content.Context
import android.content.SharedPreferences
import com.joshuacerdenia.android.easynotepad.view.dialog.LAST_UPDATED

private const val SHARED_PREFS = "shared_prefs"
private const val KEY_SORT_PREF: String = "key_sort_pref"
private const val KEY_CATEGORIES = "categories"

object NotePreferences {

    private lateinit var pref: SharedPreferences

    fun init(context: Context) {
        pref = context.getSharedPreferences("note_preferences", Context.MODE_PRIVATE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    @Deprecated("Deprecated")
    fun getSortPreference(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_SORT_PREF, LAST_UPDATED).toString()
    }

    @Deprecated("Deprecated")
    fun setSortPreference(context: Context, sortPreference: String) {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString(KEY_SORT_PREF, sortPreference)
            .apply()
    }

    @Deprecated("Deprecated")
    fun getCategories(context: Context): MutableSet<String>? {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(KEY_CATEGORIES, null)
    }

    @Deprecated("Deprecated")
    fun setCategories(context: Context, categories: MutableSet<String>) {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putStringSet(KEY_CATEGORIES, categories)
            .apply()
    }
}