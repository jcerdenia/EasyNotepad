package com.joshuacerdenia.android.easynotepad.data.model

import java.util.*

// Note without dateCreated and body.
data class NoteMinimal(
    val id: UUID,
    val category: String,
    val title: String,
    val lastModified: Date,
) {

    fun isSameAs(note: NoteMinimal): Boolean {
        return category == note.category
                && title == note.title
                && lastModified == note.lastModified
    }
}