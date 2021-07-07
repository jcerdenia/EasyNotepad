package com.joshuacerdenia.android.easynotepad.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.joshuacerdenia.android.easynotepad.R
import com.joshuacerdenia.android.easynotepad.databinding.ActivityMainBinding
import com.joshuacerdenia.android.easynotepad.view.fragment.NoteFragment
import com.joshuacerdenia.android.easynotepad.view.fragment.NoteListFragment
import java.util.*

class MainActivity : AppCompatActivity(),
    NoteListFragment.Callbacks,
    NoteFragment.Callbacks {

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

    override fun onShareNotePressed(subject: String, text: String) {
        Intent(Intent.ACTION_SEND)
            .apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            .run { Intent.createChooser(this, getString(R.string.send_note)) }
            .run { startActivity(this) }
    }

    override fun onNoteDeleted() {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.fragmentContainer.id, NoteListFragment.newInstance() )
            .commit()
    }

    override fun onToolbarInflated(toolbar: Toolbar, isNavigableUp: Boolean) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(isNavigableUp)
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)
        if (fragment is OnBackPressed) {
            val isHandled = fragment.handleBackPress()
            if (isHandled) super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}