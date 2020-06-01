package com.joshuacerdenia.android.easynotepad

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.joshuacerdenia.android.easynotepad.NotePreferences.getSortPreference
import com.joshuacerdenia.android.easynotepad.NotePreferences.setSortPreference
import java.text.DateFormat.*
import java.util.*

private const val TAG = "NoteListFragment"

fun List<Note>.sortedByLastModified() = this.sortedByDescending{ (_, _, _, _, date) -> date }
fun List<Note>.sortedByDateCreated() = this.sortedByDescending { (_, _, _, date) -> date }
fun List<Note>.sortedByCategory() = this.sortedBy{ (_, category) -> category }
fun List<Note>.sortedByTitle() = this.sortedBy{ (_, _, title) -> title }

class NoteListFragment : Fragment(),
    NoteListSorterFragment.Callbacks,
    ConfirmDeleteFragment.Callbacks {

    private val fragment = this@NoteListFragment
    private val noteListViewModel: NoteListViewModel by lazy {
        ViewModelProvider(this).get(NoteListViewModel::class.java)
    }

    private lateinit var noteRecyclerView: RecyclerView
    private var adapter: NoteAdapter? = NoteAdapter()

    private var callbacks: Callbacks? = null
    private lateinit var sortPreference: String
    private var searchTerm: String? = null

    private lateinit var messageWhenEmpty: TextView
    private lateinit var addRemoveButton: FloatingActionButton
    val categories = mutableSetOf<String>()

    companion object {
        fun newInstance(): NoteListFragment {
            return NoteListFragment()
        }
    }

    interface Callbacks {
        fun onNoteSelected(noteID: UUID,
                           categories: MutableSet<String>,
                           searchTerm: String?
        )
        fun hideUpIndicator()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbacks?.hideUpIndicator()

        setHasOptionsMenu(true)
        sortPreference = getSortPreference(this.activity!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)

        addRemoveButton = view.findViewById(R.id.add_note_button)
        messageWhenEmpty = view.findViewById(R.id.empty)
        noteRecyclerView = view.findViewById(R.id.note_recycler_view)
        noteRecyclerView.layoutManager = LinearLayoutManager(context)
        noteRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_note_list, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView

        searchItem.setOnMenuItemClickListener {
            Log.d(TAG, "search clicked")
            true
        }

        searchView.apply {

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText != null) {
                        searchNotes(newText)
                    }
                    searchTerm = newText
                    return true
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit -> {
                if (noteListViewModel.editMode.value == false) {
                    enterEditMode()
                } else {
                    exitEditMode()
                }
                true
            }
            R.id.menu_sort -> {
                if (noteListViewModel.editMode.value == true) {
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
        if (noteListViewModel.editMode.value == true) {
            enterEditMode()
        } else {
            exitEditMode()
        }
    }

    override fun onSortPreferenceSelected(sortPreference: String) {
        this.sortPreference = sortPreference
        refreshRecyclerView()
    }

    override fun onDeleteConfirmed() {
        for (note in noteListViewModel.editables) {
            noteListViewModel.deleteNote(note)
        }
        exitEditMode()
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onStop() {
        super.onStop()
        setSortPreference(this.activity!!, sortPreference)
    }

    private fun searchNotes(query: String) {
        noteListViewModel.noteListLiveData.observe(
            viewLifecycleOwner, Observer { notes ->
                notes?.let {
                    val filteredList = mutableListOf<Note>()
                    for (note in notes) {
                        if (note.title.toLowerCase(Locale.ROOT)
                                .contains(query.toLowerCase(Locale.ROOT))
                            || note.body.toLowerCase(Locale.ROOT)
                                .contains(query.toLowerCase(Locale.ROOT))) {
                            filteredList.add(note)
                        }
                    }
                    val sortedList = sortByPreference(filteredList)
                    adapter?.submitList(sortedList)
                }
            }
        )
    }

    private fun refreshRecyclerView() {
        noteListViewModel.noteListLiveData.observe(
            viewLifecycleOwner, Observer { notes ->
                notes?.let {
                    val sortedList = sortByPreference(notes)
                    adapter?.submitList(sortedList)

                    categories.clear()
                    for (note in notes) {
                        val category = note.category.trim()
                        categories.add(category)
                    }

                    searchTerm = null

                    if (sortedList.isEmpty()) {
                        messageWhenEmpty.visibility = View.VISIBLE
                        setHasOptionsMenu(false)
                    } else {
                        messageWhenEmpty.visibility = View.GONE
                        setHasOptionsMenu(true)
                    }
                }
            }
        )
    }

    private fun sortByPreference(list: List<Note>): List<Note> {
        return when (sortPreference) {
            LAST_UPDATED -> list.sortedByLastModified()
            DATE_CREATED -> list.sortedByDateCreated()
            CATEGORY -> list.sortedByCategory()
            TITLE -> list.sortedByTitle()
            else -> list.sortedByLastModified()
        }
    }

    private fun enterEditMode() {
        noteListViewModel.editMode.value = true

        addRemoveButton.apply {
            setImageResource(R.drawable.ic_delete)
            setOnClickListener {
                val number = noteListViewModel.editables.size
                if (number > 0) {
                    ConfirmDeleteFragment.newInstance(number).apply {
                        show(fragment.parentFragmentManager, "sort")
                        setTargetFragment(fragment, 0)
                    }
                } else {
                    Toast.makeText(context, getString(R.string.nothing_selected),
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun exitEditMode() {
        noteListViewModel.editMode.value = false
        noteListViewModel.editables.clear()

        addRemoveButton.apply {
            setImageResource(R.drawable.ic_add_note)
            setOnClickListener {
                val newNote = Note()
                noteListViewModel.addNote(newNote) // an empty note is generated
                callbacks?.onNoteSelected(newNote.id, categories, null) // and selected
            }
        }
    }

    private inner class NoteAdapter
        : ListAdapter<Note, NoteAdapter.NoteHolder>(DiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
            val view = layoutInflater.inflate(R.layout.list_item_note, parent, false)
            return NoteHolder(view)
        }

        override fun onBindViewHolder(holder: NoteHolder, position: Int) {
            holder.bind(getItem(position))
        }

        private inner class NoteHolder(view: View)
            : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

            private lateinit var note: Note

            val titleTextView: TextView = itemView.findViewById(R.id.note_title)
            val infoTextView: TextView = itemView.findViewById(R.id.note_info)
            val editCheckBox: CheckBox = itemView.findViewById(R.id.edit_checkbox)

            init {
                itemView.setOnClickListener(this)
                itemView.setOnLongClickListener(this)
            }

            fun bind(note: Note) {
                this.note = note
                val currentList: MutableList<Note> = currentList
                
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
                        getDateInstance().format(Date()) ==
                        getDateInstance().format(note.lastModified)
                    ) {
                        getTimeInstance(SHORT).format(note.lastModified)
                    } else {
                        getDateInstance(MEDIUM).format(note.lastModified)
                    }

                infoTextView.text = getString(
                    R.string.note_list_info, dateShown, categoryShown
                )

                editCheckBox.setOnLongClickListener {
                    if (noteListViewModel.editables.size == currentList.size) {
                        noteListViewModel.allSelected.value = false
                        noteListViewModel.allDeselected.value = true
                        noteListViewModel.editables.clear()
                    } else {
                        noteListViewModel.allSelected.value = true
                        noteListViewModel.allDeselected.value = false
                        noteListViewModel.editables.clear()
                        for (item in currentList) {
                            noteListViewModel.editables.add(item)
                        }
                    }
                    true
                }

                noteListViewModel.editMode.observe(
                    viewLifecycleOwner, Observer {
                        if (it == true) {
                            itemView.setOnClickListener(null)
                            editCheckBox.apply {
                                visibility = View.VISIBLE
                                isChecked = noteListViewModel.editables.contains(note)

                                setOnClickListener {
                                    if (isChecked) {
                                        noteListViewModel.editables.add(note)
                                    } else {
                                        noteListViewModel.editables.remove(note)
                                    }

                                    noteListViewModel.allSelected.value =
                                        noteListViewModel.editables.size == currentList.size
                                    noteListViewModel.allDeselected.value =
                                        noteListViewModel.editables.size == 0
                                }
                            }
                        } else {
                            itemView.setOnClickListener(this)
                            editCheckBox.visibility = View.GONE
                        }
                    }
                )

                noteListViewModel.allSelected.observe(
                    viewLifecycleOwner, Observer {
                        if (it == true) {
                            editCheckBox.isChecked = true
                        }
                    }
                )

                noteListViewModel.allDeselected.observe(
                    viewLifecycleOwner, Observer {
                        if (it == true) {
                            editCheckBox.isChecked = false
                        }
                    }
                )
            }

            override fun onClick(v: View) {
                callbacks?.onNoteSelected(note.id, categories, searchTerm)
            }

            override fun onLongClick(v: View?): Boolean {
                if (noteListViewModel.editMode.value == false) {
                    enterEditMode()
                } else {
                    exitEditMode()
                }
                return true
            }
        }
    }

    private inner class DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}