package com.joshuacerdenia.android.easynotepad.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Note(
    @PrimaryKey var id: UUID = UUID.randomUUID(),
    var category: String = "",
    var title: String = "",
    val dateCreated: Date = Date(),
    var lastModified: Date = Date(),
    var body: String = ""
)