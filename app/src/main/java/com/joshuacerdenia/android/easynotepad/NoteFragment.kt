package com.joshuacerdenia.android.easynotepad

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.text.DateFormat.*
import java.util.*

private const val TAG = "NoteFragment"
private const val ARG_NOTE_ID = "ARG_NOTE_ID"

class NoteFragment : Fragment() {

    private val noteViewModel: NoteViewModel by lazy {
        ViewModelProvider(this).get(NoteViewModel::class.java)
    }
    private var note: Note = Note()
    private var dataIsLoaded: Boolean = false

    private lateinit var noteCategory: EditText
    private lateinit var noteTitle: EditText
    private lateinit var noteBody: EditText
    private lateinit var noteDateCreated: TextView
    private lateinit var noteLastModified: TextView

    companion object {
        fun newInstance(noteID: UUID): NoteFragment {
            val args = Bundle().apply {
                putSerializable(ARG_NOTE_ID, noteID)
            }
            return NoteFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note = Note()
        val noteID: UUID = arguments?.getSerializable(ARG_NOTE_ID) as UUID
        //Log.d(TAG, "Success! Item $noteID selected.")
        noteViewModel.loadNote(noteID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note, container, false)

        noteCategory = view.findViewById(R.id.note_category)
        noteTitle = view.findViewById(R.id.note_title)
        noteBody = view.findViewById(R.id.note_body)
        noteDateCreated = view.findViewById(R.id.note_date_created)
        noteLastModified = view.findViewById(R.id.note_last_modified)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteViewModel.noteLiveData.observe(
            viewLifecycleOwner,
            Observer { note ->
                note?.let {
                    this.note = note
                    refreshUI()
                    dataIsLoaded = true
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        noteCategory.addTextChangedListener(textWatcher("category", note.category))
        noteTitle.addTextChangedListener(textWatcher("title", note.title))
        noteBody.addTextChangedListener(textWatcher("body", note.body))
    }

    private fun textWatcher(key: String, text: String): TextWatcher {

        return object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                sequence: CharSequence?, start: Int, before: Int, count: Int
            ) {
                var text = text
                text = sequence.toString()
                when (key) {
                    "category" -> note.category = text
                    "title" -> note.title = text
                    "body" -> note.body = text
                }
            }

            override fun afterTextChanged(sequence: Editable?) {
                if (dataIsLoaded) {
                    note.lastModified = Date()
                    refreshLastModified()
                }
            }
        }
    }

    private fun refreshUI() {
        noteCategory.setText(note.category)
        noteTitle.setText(note.title)
        noteBody.setText(note.body)

        val dateShown = getDateTimeInstance(MEDIUM, SHORT).format(note.dateCreated)
        noteDateCreated.text = getString(R.string.created_withDate, dateShown)
        refreshLastModified()
    }

    private fun refreshLastModified() {

        /*
        val dateShown =
            if (
                getDateInstance().format(Date()) == getDateInstance().format(note.lastModified)
            ) {
                getTimeInstance(SHORT).format(note.lastModified)
            } else {
                getDateInstance(MEDIUM).format(note.lastModified)
            }
         */

        val dateShown = getDateTimeInstance(MEDIUM, SHORT).format(note.lastModified)
        noteLastModified.text = getString(R.string.last_updated_withDate, dateShown)
    }

    override fun onStop() {
        super.onStop()
        //note.lastModified = Date()
        if (note.category == "" && note.title == "" && note.body == "") {
            noteViewModel.deleteNote(note)
        } else {
            noteViewModel.saveNote(note)
        }
    }
}