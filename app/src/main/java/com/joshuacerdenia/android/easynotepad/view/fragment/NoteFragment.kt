package com.joshuacerdenia.android.easynotepad.view.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.joshuacerdenia.android.easynotepad.R
import com.joshuacerdenia.android.easynotepad.data.Note
import com.joshuacerdenia.android.easynotepad.data.NotePreferences
import com.joshuacerdenia.android.easynotepad.view.dialog.ConfirmDeleteFragment
import com.joshuacerdenia.android.easynotepad.viewmodel.NoteViewModel
import java.text.DateFormat.*
import java.util.*

private const val ARG_NOTE_ID = "arg_note_ID"
private const val ARG_SEARCH_TERM = "search_term"
private const val ARG_INTENT = "arg_intent"

class NoteFragment : Fragment(), ConfirmDeleteFragment.Callbacks {

    private val fragment = this@NoteFragment
    private lateinit var toolbar: Toolbar
    private val noteViewModel: NoteViewModel by lazy {
        ViewModelProvider(this).get(NoteViewModel::class.java)
    }

    private var note: Note = Note()
    private var dataIsLoaded: Boolean = false

    private lateinit var noteCategory: AutoCompleteTextView
    private lateinit var noteTitle: EditText
    private lateinit var noteBody: EditText
    private lateinit var noteDateCreated: TextView
    private lateinit var noteLastModified: TextView

    companion object {
        fun newInstance(noteID: UUID, searchTerm: String?): NoteFragment {
            val args = Bundle().apply {
                putSerializable(ARG_NOTE_ID, noteID)
                putString(ARG_SEARCH_TERM, searchTerm)
            }
            return NoteFragment().apply {
                arguments = args
            }
        }

        fun newInstanceWithIntent(noteID: UUID, intent: Intent): NoteFragment {
            val args = Bundle().apply {
                putSerializable(ARG_NOTE_ID, noteID)
                putParcelable(ARG_INTENT, intent as Parcelable)
            }
            return NoteFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        note = Note()
        val noteID: UUID = arguments?.getSerializable(ARG_NOTE_ID) as UUID
        noteViewModel.loadNote(noteID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        noteCategory = view.findViewById(R.id.note_category)
        noteTitle = view.findViewById(R.id.note_title)
        noteBody = view.findViewById(R.id.note_body)
        noteDateCreated = view.findViewById(R.id.note_date_created)
        noteLastModified = view.findViewById(R.id.note_last_modified)

        val activity = (activity as? AppCompatActivity)
        activity?.setSupportActionBar(toolbar)
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var searchTerm = arguments?.getString(ARG_SEARCH_TERM)?.lowercase(Locale.ROOT)
        if (searchTerm == "") {
            searchTerm = null
        }

        val categories = NotePreferences.getCategories(requireContext())?.toList() as List<*>
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        noteCategory.setAdapter(adapter)
        noteCategory.threshold = 1

        noteViewModel.noteLiveData.observe(viewLifecycleOwner, Observer { note ->
            note.let {
                this.note = note!!
                refreshUI()
                dataIsLoaded = true

                loadTextFromIntent()
                findSearchTerm(note, searchTerm)

                saveCopyOnce(note)
                noteViewModel.notYetCopied = false
            }
        })
    }

    private fun loadTextFromIntent() {
        val intent = arguments?.getParcelable<Intent>(ARG_INTENT)
        if (intent !== null) {
            note.title = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
            note.body = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            refreshUI()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            /** For future implementation?
             *
            R.id.menu_undo -> {
                restoreOldCopy(note)
                noteViewModel.notYetCopied = true
                refreshUI()
                saveCopyOnce(note)
                noteViewModel.notYetCopied = false
                when (true) {
                    noteCategory.isFocused ->
                        noteCategory.setSelection(note.category.lastIndex + 1)
                    noteTitle.isFocused -> noteTitle.setSelection(note.title.lastIndex + 1)
                    noteBody.isFocused -> noteBody.setSelection(note.body.lastIndex + 1)
                }
                true
            }
            R.id.menu_save -> {
                if (isNoteModified(note)) {
                    noteViewModel.notYetCopied = true
                    saveCopyOnce(note)
                    noteViewModel.notYetCopied = false
                    note.lastModified = Date()

                    val dateShown = getDateTimeInstance(MEDIUM, SHORT).format(note.lastModified)
                    noteLastModified.text = getString(R.string.last_updated_withDate, dateShown)
                    Toast.makeText(context, getString(R.string.note_saved),
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.nothing_to_save),
                        Toast.LENGTH_SHORT).show()
                }
                true
            } */

            R.id.menu_share -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, note.body)
                    putExtra(
                        Intent.EXTRA_SUBJECT, "${note.category}: ${note.title}"
                    )
                }.also { intent ->
                    val chooserIntent = Intent.createChooser(intent, getString(R.string.send_note))
                    startActivity(chooserIntent)
                }
                true
            }
            R.id.menu_delete -> {
                ConfirmDeleteFragment.newInstance(1).apply {
                    show(fragment.parentFragmentManager, "sort")
                    setTargetFragment(fragment, 0)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveCopyOnce(note: Note) {
        if (noteViewModel.notYetCopied) {
            noteViewModel.noteBeforeChanged.category = note.category
            noteViewModel.noteBeforeChanged.title = note.title
            noteViewModel.noteBeforeChanged.dateCreated = note.dateCreated
            noteViewModel.noteBeforeChanged.lastModified = note.lastModified
            noteViewModel.noteBeforeChanged.body = note.body
        }
    }

    private fun isNoteModified(note: Note): Boolean {
        return !(noteViewModel.noteBeforeChanged.category == note.category
                && noteViewModel.noteBeforeChanged.title == note.title
                && noteViewModel.noteBeforeChanged.body == note.body)
    }

    /** For Future Implementation?
    private fun restoreOldCopy(note: Note) {
        note.category = noteViewModel.noteBeforeChanged.category
        note.title = noteViewModel.noteBeforeChanged.title
        note.body = noteViewModel.noteBeforeChanged.body
        note.lastModified = noteViewModel.noteBeforeChanged.lastModified
    } */

    private fun findSearchTerm(note: Note, searchTerm: String?) {
        val title = note.title.lowercase(Locale.ROOT)
        val body = note.body.lowercase(Locale.ROOT)
        if (searchTerm !== null) {
            if (title.contains(searchTerm)) {
                val index = title.indexOf(searchTerm)
                noteTitle.requestFocus()
                noteTitle.setSelection(index)
            } else {
                val index = body.indexOf(searchTerm)
                noteBody.requestFocus()
                noteBody.setSelection(index)
            }
        }
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
                // Blank
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
                    val icon = if (isNoteModified(note)) {
                        R.drawable.ic_done
                    } else {
                        0
                    }
                    (activity as? AppCompatActivity)?.supportActionBar?.setHomeAsUpIndicator(icon)
                }
            }
        }
    }

    private fun refreshUI() {
        noteCategory.setText(note.category)
        noteTitle.setText(note.title)
        noteBody.setText(note.body)

        val createdShown = getDateTimeInstance(MEDIUM, SHORT).format(note.dateCreated)
        noteDateCreated.text = getString(R.string.created_withDate, createdShown)

        val lastUpdatedShown = getDateTimeInstance(MEDIUM, SHORT).format(note.lastModified)
        noteLastModified.text = getString(R.string.last_updated_withDate, lastUpdatedShown)
    }

    override fun onDeleteConfirmed() {
        noteViewModel.deleteNote(note)
        parentFragmentManager.popBackStack()
    }

    override fun onStop() {
        super.onStop()

        if (noteViewModel.noteBeforeChanged.category == note.category &&
            noteViewModel.noteBeforeChanged.title == note.title &&
            noteViewModel.noteBeforeChanged.dateCreated == note.dateCreated &&
            noteViewModel.noteBeforeChanged.lastModified == note.lastModified &&
            noteViewModel.noteBeforeChanged.body == note.body) {
            note.lastModified = note.lastModified
        } else {
            note.lastModified = Date()
        }

        if (note.category == "" && note.title == "" && note.body == "") {
            noteViewModel.deleteNote(note)
        } else {
            noteViewModel.saveNote(note)
        }
    }
}