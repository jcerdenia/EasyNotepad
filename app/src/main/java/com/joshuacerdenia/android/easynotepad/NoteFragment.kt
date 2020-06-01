package com.joshuacerdenia.android.easynotepad

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.io.Serializable
import java.text.DateFormat.*
import java.util.*

private const val ARG_NOTE_ID = "arg_note_ID"
private const val ARG_CATEGORIES = "categories"
private const val ARG_SEARCH_TERM = "search_term"

class NoteFragment : Fragment(), ConfirmDeleteFragment.Callbacks {

    private val fragment = this@NoteFragment
    private val noteViewModel: NoteViewModel by lazy {
        ViewModelProvider(this).get(NoteViewModel::class.java)
    }

    private var callbacks: Callbacks? = null
    private var note: Note = Note()
    private var dataIsLoaded: Boolean = false

    private lateinit var noteCategory: AutoCompleteTextView
    private lateinit var noteTitle: EditText
    private lateinit var noteBody: EditText
    private lateinit var noteDateCreated: TextView
    private lateinit var noteLastModified: TextView

    companion object {
        fun newInstance(noteID: UUID,
                        categories: MutableSet<String>,
                        searchTerm: String?
        ): NoteFragment {
            val args = Bundle().apply {
                putSerializable(ARG_NOTE_ID, noteID)
                putSerializable(ARG_CATEGORIES, categories as Serializable)
                putString(ARG_SEARCH_TERM, searchTerm)
            }
            return NoteFragment().apply {
                arguments = args
            }
        }
    }

    interface Callbacks {
        fun showUpIndicator()
        fun hideUpIndicator()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callbacks?.showUpIndicator()
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

        noteCategory = view.findViewById(R.id.note_category)
        noteTitle = view.findViewById(R.id.note_title)
        noteBody = view.findViewById(R.id.note_body)
        noteDateCreated = view.findViewById(R.id.note_date_created)
        noteLastModified = view.findViewById(R.id.note_last_modified)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = (arguments?.getSerializable(ARG_CATEGORIES) as MutableSet<*>).toList()
        var searchTerm = arguments?.getString(ARG_SEARCH_TERM)?.toLowerCase(Locale.ROOT)
        if (searchTerm == "") {
            searchTerm = null
        }

        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, categories)
        noteCategory.setAdapter(adapter)
        noteCategory.threshold = 1

        noteViewModel.noteLiveData.observe(
            viewLifecycleOwner,
            Observer { note ->
                note?.let {
                    this.note = note
                    refreshUI()
                    dataIsLoaded = true

                    findSearchTerm(note, searchTerm)

                    saveCopyOnce(note)
                    noteViewModel.notYetCopied = false
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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

    private fun findSearchTerm(note: Note, searchTerm: String?) {
        val title = note.title.toLowerCase(Locale.ROOT)
        val body = note.body.toLowerCase(Locale.ROOT)
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
                // Blank
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
        callbacks?.hideUpIndicator()
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