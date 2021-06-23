package com.joshuacerdenia.android.easynotepad.view.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.joshuacerdenia.android.easynotepad.R
import com.joshuacerdenia.android.easynotepad.data.model.Note
import com.joshuacerdenia.android.easynotepad.databinding.FragmentNoteListBinding
import com.joshuacerdenia.android.easynotepad.extension.setVisibility
import com.joshuacerdenia.android.easynotepad.view.adapter.NoteAdapter
import com.joshuacerdenia.android.easynotepad.view.dialog.*
import com.joshuacerdenia.android.easynotepad.viewmodel.NoteListViewModel
import java.text.DateFormat.*
import java.util.*

class NoteListFragment : Fragment(), NoteAdapter.EventListener {

    interface Callbacks {

        fun onNoteSelected(noteID: UUID, query: String? = null)

        fun onToolbarInflated(toolbar: Toolbar)
    }

    private var _binding: FragmentNoteListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteListViewModel by viewModels()
    private lateinit var adapter: NoteAdapter
    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        adapter = NoteAdapter(context, this)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        callbacks?.onToolbarInflated(binding.toolbar)
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@NoteListFragment.adapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        viewModel.isManagingLive.observe(viewLifecycleOwner, { isManaging ->
            updateUI(isManaging)
        })

        viewModel.notesLive.observe(viewLifecycleOwner, { notes ->
            binding.emptyTextView.setVisibility(notes.isEmpty())
            binding.recyclerView.setVisibility(notes.isNotEmpty())
            adapter.submitList(notes)
        })

        viewModel.selectedNoteIDsLive.observe(viewLifecycleOwner, { noteIDs ->
            Log.d(TAG, "Selected items: ${noteIDs.size}")
            adapter.selectedItems = noteIDs
            if (noteIDs.size < adapter.currentList.size) {
                binding.selectAllCheckbox.isChecked = false
            }
        })

        binding.selectAllCheckbox.setOnClickListener { checkBox ->
            if ((checkBox as CheckBox).isChecked) {
                val currentNoteIDs = adapter.currentList.map { it.id }
                viewModel.replaceSelectedItems(currentNoteIDs)
                adapter.toggleCheckBoxes(true)
            } else {
                viewModel.clearSelectedItems()
                adapter.toggleCheckBoxes(false)
            }
        }

        binding.fab.setOnClickListener {
            val note = Note() // Create blank note.
            note.title = "Test" // Delete later.
            viewModel.addNote(note)
            // TODO: open note
            // callbacks?.onNoteSelected(note.id)
        }
    }

    private fun updateUI(isManaging: Boolean) {
        adapter.shouldShowCheckBoxes = isManaging
        binding.selectAllCheckbox.setVisibility(isManaging)
        binding.fab.setVisibility(!isManaging)
        binding.toolbar.title = if (!isManaging) getString(R.string.app_name) else null
        updateMenuItems(binding.toolbar.menu, isManaging)
        if (!isManaging) viewModel.clearSelectedItems()
    }

    private fun updateMenuItems(menu: Menu, isManaging: Boolean) {
        menu.apply {
            findItem(R.id.menu_item_search)?.isVisible = !isManaging
            findItem(R.id.menu_item_manage)?.isVisible = !isManaging
            findItem(R.id.menu_item_sort)?.isVisible = !isManaging
            findItem(R.id.menu_item_delete)?.isVisible = isManaging
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_note_list, menu)
        updateMenuItems(menu, viewModel.isManaging())

        menu.findItem(R.id.menu_item_search).actionView?.apply {
            this as SearchView
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    // TODO: Search notes
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // TODO: Search notes
                    return true
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_manage -> {
                viewModel.setIsManaging(true)
                true
            }
            R.id.menu_item_sort -> {
                NoteListSorterFragment.newInstance("CHANGE LATER")
                    .show(parentFragmentManager, "sort")
                true
            }
            R.id.menu_item_delete -> {
                // TODO
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNoteClicked(noteID: UUID) {
        // TODO
    }

    override fun onNoteLongClicked(noteID: UUID) {
        // TODO: Select note
        viewModel.setIsManaging(!viewModel.isManaging())
        // viewModel.addSelection(noteID)
    }

    override fun onNoteCheckBoxClicked(noteID: UUID, isChecked: Boolean) {
        viewModel.apply {
            if (isChecked) addSelection(noteID) else removeSelection(noteID)
        }
    }

    fun handleBackPress(): Boolean {
        return if (viewModel.isManaging()) {
            viewModel.setIsManaging(false)
            true
        } else {
            false
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "NoteListFragment"
        private const val HAS_TEXT_INTENT = "HAS_TEXT_INTENT"

        fun newInstance(hasTextIntent: Boolean): NoteListFragment {
            return NoteListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(HAS_TEXT_INTENT, hasTextIntent)
                }
            }
        }
    }
}