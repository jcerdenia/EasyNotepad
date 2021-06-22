package com.joshuacerdenia.android.easynotepad.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.*
import com.joshuacerdenia.android.easynotepad.R
import com.joshuacerdenia.android.easynotepad.data.model.Note
import com.joshuacerdenia.android.easynotepad.databinding.FragmentNoteListBinding
import com.joshuacerdenia.android.easynotepad.extension.hide
import com.joshuacerdenia.android.easynotepad.extension.show
import com.joshuacerdenia.android.easynotepad.extension.toMinimal
import com.joshuacerdenia.android.easynotepad.view.adapter.NoteAdapter
import com.joshuacerdenia.android.easynotepad.view.dialog.*
import com.joshuacerdenia.android.easynotepad.viewmodel.NoteListViewModel
import java.text.DateFormat.*
import java.util.*

class NoteListFragment : Fragment(), NoteAdapter.EventListener {

    private var _binding: FragmentNoteListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteListViewModel by viewModels()
    private lateinit var adapter: NoteAdapter
    private var callbacks: Callbacks? = null

    private var searchTerm: String? = null

    interface Callbacks {

        fun onNoteSelected(noteID: UUID, query: String? = null)

        fun onToolbarInflated(toolbar: Toolbar)
    }

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
            val divider = DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)
            addItemDecoration(divider)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isManagingLive.observe(viewLifecycleOwner, { isManaging ->
            updateUI(isManaging)
        })

        viewModel.notesLive.observe(viewLifecycleOwner, { notes ->
            if (notes.isNotEmpty()) {
                binding.emptyTextView.hide()
                adapter.submitList(notes.toMinimal())
            } else {
                binding.emptyTextView.show()
            }
        })
    }

    private fun updateUI(isManaging: Boolean) {
        setHasOptionsMenu(!isManaging)

        if (isManaging) {
            binding.toolbar.title = null
            binding.closeButton.apply {
                show()
                setOnClickListener {
                    viewModel.setIsManaging(false)
                }
            }

            // FAB becomes Delete button.
            binding.fab.apply {
                setImageResource(R.drawable.ic_delete)
                setOnClickListener {
                    // TODO: Delete selected Items
                }
            }
        } else {
            binding.toolbar.title = getString(R.string.app_name)
            binding.closeButton.hide()

            // FAB becomes Add button.
            binding.fab.apply {
                setImageResource(R.drawable.ic_add_note)
                setOnClickListener {
                    val note = Note() // Create blank note.
                    viewModel.addNote(note)
                    callbacks?.onNoteSelected(note.id)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_note_list, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    // TODO: Search notes
                    searchTerm = query
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // TODO: Search notes
                    searchTerm = newText
                    return true
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit -> {
                viewModel.setIsManaging(true)
                true
            }
            R.id.menu_sort -> {
                NoteListSorterFragment.newInstance("CHANGE LATER").apply {
                    show(parentFragmentManager, "sort")
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNoteClicked(noteID: UUID) {
        // TODO
    }

    override fun onNoteLongClicked(noteID: UUID) {
        // TODO
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