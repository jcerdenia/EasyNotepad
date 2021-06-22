package com.joshuacerdenia.android.easynotepad.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joshuacerdenia.android.easynotepad.R
import java.util.*

class MainActivity : AppCompatActivity(), NoteListFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        val fragment = if (intent?.type?.startsWith("text/plain") == true) {
            NoteListFragment.newInstance(true)
        } else {
            NoteListFragment.newInstance(false)
        }

        if (currentFragment == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onNoteSelected(noteID: UUID,
                                searchTerm: String?
    ) {
        val fragment = NoteFragment.newInstance(noteID, searchTerm)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onNoteAddedWithIntent(noteID: UUID) {
        val fragment = NoteFragment.newInstanceWithIntent(noteID, intent)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}