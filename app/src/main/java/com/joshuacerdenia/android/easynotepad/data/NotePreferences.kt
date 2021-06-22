package com.joshuacerdenia.android.easynotepad.data

import android.content.Context
import com.joshuacerdenia.android.easynotepad.LAST_UPDATED

private const val SHARED_PREFS = "shared_prefs"
private const val KEY_SORT_PREF: String = "key_sort_pref"
private const val KEY_CATEGORIES = "categories"

object NotePreferences {

    fun getSortPreference(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_SORT_PREF, LAST_UPDATED).toString()
    }

    fun setSortPreference(context: Context, sortPreference: String) {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString(KEY_SORT_PREF, sortPreference)
            .apply()
    }

    fun getCategories(context: Context): MutableSet<String>? {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(KEY_CATEGORIES, null)
    }

    fun setCategories(context: Context, categories: MutableSet<String>) {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putStringSet(KEY_CATEGORIES, categories)
            .apply()
    }
}