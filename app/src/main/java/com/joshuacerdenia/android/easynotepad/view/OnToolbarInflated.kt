package com.joshuacerdenia.android.easynotepad.view

import androidx.appcompat.widget.Toolbar

interface OnToolbarInflated {

    fun onToolbarInflated(toolbar: Toolbar, isNavigableUp: Boolean = false)
}