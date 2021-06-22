package com.joshuacerdenia.android.easynotepad.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joshuacerdenia.android.easynotepad.R
import com.joshuacerdenia.android.easynotepad.databinding.ActivityMainBinding
import com.joshuacerdenia.android.easynotepad.view.fragment.NoteFragment
import com.joshuacerdenia.android.easynotepad.view.fragment.NoteListFragment
import java.util.*

class MainActivity : AppCompatActivity(), NoteListFragment.Callbacks {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isIntentText = intent?.type?.startsWith("text/plain") ?: false

        if (savedInstanceState == null) {
            val fragment = NoteListFragment.newInstance(isIntentText)
            supportFragmentManager
                .beginTransaction()
                .add(binding.fragmentContainer.id, fragment)
                .commit()
        }
    }

    override fun onNoteSelected(noteID: UUID, searchTerm: String?) {
        val fragment = NoteFragment.newInstance(noteID, searchTerm)
        supportFragmentManager
            .beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
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