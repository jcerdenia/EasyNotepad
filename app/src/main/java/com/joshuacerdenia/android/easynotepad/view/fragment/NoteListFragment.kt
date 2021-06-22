package com.joshuacerdenia.android.easynotepad.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.joshuacerdenia.android.easynotepad.R
import com.joshuacerdenia.android.easynotepad.data.Note
import com.joshuacerdenia.android.easynotepad.data.NotePreferences.getCategories
import com.joshuacerdenia.android.easynotepad.data.NotePreferences.getSortPreference
import com.joshuacerdenia.android.easynotepad.data.NotePreferences.setCategories
import com.joshuacerdenia.android.easynotepad.data.NotePreferences.setSortPreference
import com.joshuacerdenia.android.easynotepad.viewmodel.NoteListViewModel
import java.text.DateFormat.*
import java.util.*

private const val ARG_HAS_INTENT = "arg_has_intent"

fun List<Note>.sortedByLastModified() = this.sortedByDescending{ (_, _, _, _, date) -> date }
fun List<Note>.sortedByDateCreated() = this.sortedByDescending { (_, _, _, date) -> date }
fun List<Note>.sortedByCategory() = this.sortedBy{ (_, category) -> category }
fun List<Note>.sortedByTitle() = this.sortedBy{ (_, _, title) -> title }

class NoteListFragment : Fragment(),
    NoteListSorterFragment.Callbacks,
    ConfirmDeleteFragment.Callbacks {

    private val fragment = this@NoteListFragment
    private lateinit var toolbar: Toolbar
    private lateinit var noteRecyclerView: RecyclerView
    private var adapter: NoteAdapter? = NoteAdapter()

    private val noteListViewModel: NoteListViewModel by lazy {
        ViewModelProvider(this).get(NoteListViewModel::class.java)
    }

    private var callbacks: Callbacks? = null
    private lateinit var sortPreference: String
    private var searchTerm: String? = null

    private lateinit var messageWhenEmpty: TextView
    private lateinit var selectAllCheckBox: CheckBox
    private lateinit var closeEditButton: ImageButton
    private lateinit var addRemoveButton: FloatingActionButton

    private lateinit var categories: MutableSet<String>
    private var currentList: List<Note> = listOf()

    companion object {
        fun newInstance(hasIntent: Boolean): NoteListFragment {
            val args = Bundle().apply {
                putBoolean(ARG_HAS_INTENT, hasIntent)
            }
            return NoteListFragment().apply {
                arguments = args
            }
        }
    }

    interface Callbacks {
        fun onNoteSelected(noteID: UUID, searchTerm: String?)
        fun onNoteAddedWithIntent(noteID: UUID)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        sortPreference = getSortPreference(this.requireActivity())

        categories = getCategories(this.requireActivity()) ?: emptySet<String>().toMutableSet()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)

        toolbar = view.findViewById(R.id.toolbar) as Toolbar
        addRemoveButton = view.findViewById(R.id.add_note_button)
        messageWhenEmpty = view.findViewById(R.id.empty)
        selectAllCheckBox = view.findViewById(R.id.select_all)
        closeEditButton = view.findViewById(R.id.close_button)
        noteRecyclerView = view.findViewById(R.id.note_recycler_view)

        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

        noteRecyclerView.layoutManager = LinearLayoutManager(context)
        val divider = DividerItemDecoration(
            noteRecyclerView.context,
            (noteRecyclerView.layoutManager as LinearLayoutManager).orientation
        )
        noteRecyclerView.addItemDecoration(divider)
        noteRecyclerView.adapter = adapter

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_note_list, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        searchNotes(query)
                    }
                    searchTerm = query
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
            if (noteListViewModel.allSelected.value == true) {
                selectAllCheckBox.isChecked = true
            }
        } else {
            val hasIntent = arguments?.getBoolean(ARG_HAS_INTENT)
            if (hasIntent == true) {
                val newNote = Note()
                noteListViewModel.addNote(newNote)
                callbacks?.onNoteAddedWithIntent(newNote.id)
            }
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
        setCategories(this.activity!!, categories)
    }

    private fun searchNotes(query: String) {
        noteListViewModel.noteListLiveData.observe(viewLifecycleOwner, Observer { notes ->
                notes?.let {
                    val filteredList = mutableListOf<Note>()
                    for (note in notes) {
                        if (note.title.lowercase(Locale.ROOT)
                                .contains(query.lowercase(Locale.ROOT))
                            || note.body.lowercase(Locale.ROOT)
                                .contains(query.lowercase(Locale.ROOT))) {
                            filteredList.add(note)
                        }
                    }
                    val sortedList = sortByPreference(filteredList)
                    adapter?.submitList(sortedList)
                }
        })
    }

    private fun refreshRecyclerView() {
        noteListViewModel.noteListLiveData.observe(viewLifecycleOwner, Observer { notes ->
                notes?.let {
                    val sortedList = sortByPreference(notes)
                    adapter?.submitList(sortedList)

                    categories.clear()
                    for (note in notes) {
                        val category = note.category.trim()
                        categories.add(category)
                    }

                    searchTerm = null

                    if (noteListViewModel.editMode.value == false) {
                        if (sortedList.isEmpty()) {
                            messageWhenEmpty.visibility = View.VISIBLE
                            setHasOptionsMenu(false)
                        } else {
                            messageWhenEmpty.visibility = View.GONE
                            setHasOptionsMenu(true)
                        }
                    }
                }
        })
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
        (activity as AppCompatActivity).supportActionBar?.title = null
        setHasOptionsMenu(false)


        closeEditButton.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                exitEditMode()
            }
        }

        selectAllCheckBox.apply {
            visibility = View.VISIBLE
            isChecked = false
            setOnClickListener {
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
            }
        }

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

        refreshRecyclerView()
    }

    private fun exitEditMode() {
        noteListViewModel.editMode.value = false
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
        setHasOptionsMenu(true)

        noteListViewModel.editables.clear()
        closeEditButton.visibility = View.GONE
        selectAllCheckBox.visibility = View.GONE

        addRemoveButton.apply {
            setImageResource(R.drawable.ic_add_note)
            setOnClickListener {
                val newNote = Note()
                noteListViewModel.addNote(newNote) // an empty note is generated
                callbacks?.onNoteSelected(newNote.id, null) // and selected
            }
        }

        refreshRecyclerView()
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
                fragment.currentList = currentList

                if (note.category == "" && note.title == "" && note.body == "") {
                    noteListViewModel.deleteNote(note)
                }

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

                noteListViewModel.editMode.observe(viewLifecycleOwner, Observer {
                        if (it == true) {
                            itemView.setOnClickListener(null)
                            editCheckBox.apply {
                                visibility = View.VISIBLE
                                isChecked = noteListViewModel.editables.contains(note)

                                setOnClickListener {
                                    if (isChecked) {
                                        noteListViewModel.editables.add(note)
                                        selectAllCheckBox.isChecked =
                                            noteListViewModel.editables.size == currentList.size
                                    } else {
                                        noteListViewModel.editables.remove(note)
                                        selectAllCheckBox.isChecked = false
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
                })

                noteListViewModel.allSelected.observe(viewLifecycleOwner, Observer {
                        if (it == true) {
                            editCheckBox.isChecked = true
                        }
                })

                noteListViewModel.allDeselected.observe(viewLifecycleOwner, Observer {
                        if (it == true) {
                            editCheckBox.isChecked = false
                        }
                })
            }

            override fun onClick(v: View) {
                callbacks?.onNoteSelected(note.id, searchTerm)
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