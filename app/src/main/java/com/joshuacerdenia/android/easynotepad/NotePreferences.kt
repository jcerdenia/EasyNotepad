package com.joshuacerdenia.android.easynotepad

import android.content.Context

const val LAST_UPDATED = "last_updated"
const val DATE_CREATED = "date_created"
const val CATEGORY = "category"
const val TITLE = "title"

private const val KEY_SORT_PREF: String = "key_sort_pref"

object NotePreferences {

    fun getSortPreference(context: Context): String {
        val sharedPrefs = context.getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_SORT_PREF, LAST_UPDATED).toString()
    }

    fun setSortPreference(context: Context, sortPreference: String) {
        val sharedPrefs = context.getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString(KEY_SORT_PREF, sortPreference)
            .apply()
    }
}