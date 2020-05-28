package com.joshuacerdenia.android.easynotepad

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.joshuacerdenia.android.easynotepad.NotePreferences.getSortPreference
import com.joshuacerdenia.android.easynotepad.NotePreferences.setSortPreference
import java.text.DateFormat.*
import java.util.*

private const val TAG = "NoteListFragment"

fun List<Note>.sortedByLastUpdated() = this.sortedByDescending{ (_, _, _, _, date) -> date }
fun List<Note>.sortedByDateCreated() = this.sortedByDescending { (_, _, _, date) -> date }
fun List<Note>.sortedByCategory() = this.sortedBy{ (_, category) -> category }
fun List<Note>.sortedByTitle() = this.sortedBy{ (_, _, title) -> title }

open class NoteListFragment : Fragment(), NoteListSorterFragment.Callbacks {

    private var callbacks: Callbacks? = null
    private val noteListViewModel: NoteListViewModel by lazy {
        ViewModelProvider(this).get(NoteListViewModel::class.java)
    }

    private lateinit var sortPreference: String

    private lateinit var noteRecyclerView: RecyclerView
    private var adapter: NoteAdapter? = NoteAdapter(emptyList())

    private lateinit var addRemoveButton: FloatingActionButton

    interface Callbacks {
        fun onNoteSelected(noteID: UUID)
    }

    companion object {
        fun newInstance(): NoteListFragment {
            return NoteListFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sortPreference = getSortPreference(this.activity!!)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)

        addRemoveButton = view.findViewById(R.id.add_note_button)
        noteRecyclerView = view.findViewById(R.id.note_recycler_view)
        noteRecyclerView.layoutManager = LinearLayoutManager(context)
        noteRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (noteListViewModel.editMode) {
            enterEditMode()
        } else {
            observeNoteListLiveData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_note_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment = this@NoteListFragment

        return when (item.itemId) {
            R.id.menu_edit -> {
                if (!noteListViewModel.editMode) {
                    enterEditMode()
                } else {
                    exitEditMode()
                }
                true
            }
            R.id.menu_sort -> {
                if (noteListViewModel.editMode) {
                    exitEditMode()
                }
                NoteListSorterFragment.newInstance(sortPreference).apply {
                    show(fragment.parentFragmentManager, "sort")
                    setTargetFragment(fragment, 0)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()

        addRemoveButton.setOnClickListener {
            if (!noteListViewModel.editMode) {
                val newNote = Note()
                noteListViewModel.addNote(newNote) // a fresh note is generated
                callbacks?.onNoteSelected(newNote.id) // and selected
            } else {
                for (note in noteListViewModel.editables) {
                    noteListViewModel.deleteNote(note)
                }
                exitEditMode()
            }
        }
    }

    override fun onSortPreferenceSelected(sortPreference: String) {
        this.sortPreference = sortPreference
        observeNoteListLiveData()
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onStop() {
        super.onStop()
        setSortPreference(this.activity!!, sortPreference)
    }

    private inner class NoteHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

        private lateinit var note: Note
        //private var isEditable = false

        val titleTextView: TextView = itemView.findViewById(R.id.note_title)
        val infoTextView: TextView = itemView.findViewById(R.id.note_info)
        val editCheckBox: CheckBox = itemView.findViewById(R.id.edit_checkbox)

        init {
            if (!noteListViewModel.editMode) {
                itemView.setOnClickListener(this)
                //itemView.setOnLongClickListener(this)
            }
        }

        fun bind(note: Note) {
            this.note = note

            titleTextView.text = if (note.title == "") {
                getString(R.string.no_title)
            } else {
                note.title
            }

            val categoryShown = if (note.category == "") {
                getString(R.string.no_category)
            } else {
                note.category
            }

            val dateShown =
                if (
                    getDateInstance().format(Date()) == getDateInstance().format(note.lastModified)
                ) {
                    getTimeInstance(SHORT).format(note.lastModified)
                } else {
                    getDateInstance(MEDIUM).format(note.lastModified)
                }

            infoTextView.text = getString(
                R.string.note_list_info, dateShown, categoryShown
            )

            if (noteListViewModel.editMode) {
                editCheckBox.apply{
                    visibility = View.VISIBLE
                    isChecked = noteListViewModel.editables.contains(note)
                    setOnCheckedChangeListener { _, isChecked ->
                        (if (isChecked) noteListViewModel.editables.add(note)
                        else noteListViewModel.editables.remove(note))
                        //Log.d(TAG, "Got ${noteListViewModel.editables.size} editable items")
                    }
                }
            } else {
                editCheckBox.visibility = View.GONE
            }
        }

        override fun onClick(v: View) {
            callbacks?.onNoteSelected(note.id)
        }

        override fun onLongClick(v: View?): Boolean {
            TODO("Not yet implemented")
        }
    }

    private inner class NoteAdapter(var notes: List<Note>) : RecyclerView.Adapter<NoteHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
            val view = layoutInflater.inflate(R.layout.list_item_note, parent, false)
            //Log.i(TAG, "ViewHolder created!")
            return NoteHolder(view)
        }

        override fun getItemCount(): Int = notes.size

        override fun onBindViewHolder(holder: NoteHolder, position: Int) {
            val sortedNotes = when (sortPreference) {
                LAST_UPDATED -> notes.sortedByLastUpdated()
                DATE_CREATED -> notes.sortedByDateCreated()
                CATEGORY -> notes.sortedByCategory()
                TITLE -> notes.sortedByTitle()
                else -> notes.sortedByLastUpdated() // default
            }
            val note = sortedNotes[position]
            holder.bind(note)
        }
    }

    private fun observeNoteListLiveData() {
        noteListViewModel.noteListLiveData.observe(
            viewLifecycleOwner,
            Observer { notes ->
                notes?.let {
                    //Log.i(TAG, "Got ${notes.size} notes...")
                    refreshRecyclerView(notes)
                }
            }
        )
    }

    private fun refreshRecyclerView(notes: List<Note>) {
        adapter = NoteAdapter(notes)
        noteRecyclerView.adapter = adapter
    }

    private fun enterEditMode() {
        noteListViewModel.editMode = true
        addRemoveButton.setImageResource(R.drawable.ic_delete)

        observeNoteListLiveData()
    }

    private fun exitEditMode() {
        noteListViewModel.editMode = false
        noteListViewModel.editables.clear()
        addRemoveButton.setImageResource(R.drawable.ic_add_note)

        observeNoteListLiveData()
    }
}