package com.joshuacerdenia.android.easynotepad.extension

import com.joshuacerdenia.android.easynotepad.data.model.Note

fun List<Note>.sortedByLastModified() = this.sortedByDescending{ it.lastModified }

fun List<Note>.sortedByDateCreated() = this.sortedByDescending { it.dateCreated }

fun List<Note>.sortedByCategory() = this.sortedBy{ it.category }

fun List<Note>.sortedByTitle() = this.sortedBy{ it.title }

fun List<Note>.toMinimal() = this.map { it.toMinimal() }