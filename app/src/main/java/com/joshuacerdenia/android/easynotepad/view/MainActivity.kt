package com.joshuacerdenia.android.easynotepad.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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

        val hasTextIntent = intent?.type?.startsWith("text/plain") ?: false

        if (savedInstanceState == null) {
            val fragment = NoteListFragment.newInstance(hasTextIntent)
            supportFragmentManager
                .beginTransaction()
                .add(binding.fragmentContainer.id, fragment)
                .commit()
        }
    }

    override fun onNoteSelected(noteID: UUID, query: String?) {
        val fragment = NoteFragment.newInstance(noteID, query)
        supportFragmentManager
            .beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onToolbarInflated(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}