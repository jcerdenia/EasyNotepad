package com.joshuacerdenia.android.easynotepad

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity(),
    NoteListFragment.Callbacks,
    NoteFragment.Callbacks {

    private var actionBar: ActionBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        actionBar = this.supportActionBar!!

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = NoteListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onNoteSelected(noteID: UUID,
                                categories: MutableSet<String>,
                                searchTerm: String?
    ) {
        val fragment = NoteFragment.newInstance(noteID, categories, searchTerm)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun showUpIndicator() {
        actionBar?.title = getString(R.string.edit_note)
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun hideUpIndicator() {
        actionBar?.title = getString(R.string.app_name)
        actionBar?.setHomeButtonEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        hideUpIndicator()
        super.onBackPressed()
    }
}