package com.joshuacerdenia.android.easynotepad.extension

import android.text.Editable

fun String?.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)