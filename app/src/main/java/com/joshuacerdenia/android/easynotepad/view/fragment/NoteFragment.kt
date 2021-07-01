package com.joshuacerdenia.android.easynotepad.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.joshuacerdenia.android.easynotepad.R
import com.joshuacerdenia.android.easynotepad.data.model.Note
import com.joshuacerdenia.android.easynotepad.databinding.FragmentNoteBinding
import com.joshuacerdenia.android.easynotepad.extension.toEditable
import com.joshuacerdenia.android.easynotepad.view.OnToolbarInflated
import com.joshuacerdenia.android.easynotepad.viewmodel.NoteViewModel
import java.text.DateFormat
import java.util.*

class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteViewModel by viewModels()
    private var callbacks: OnToolbarInflated? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as OnToolbarInflated?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // Get Note ID from arguments and
        // use it to retrieve note contents.
        arguments?.getString(NOTE_ID)
            .run { UUID.fromString(this) }
            .run { viewModel.getNoteByID(this) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        callbacks?.onToolbarInflated(binding.toolbar, true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.noteLive.observe(viewLifecycleOwner, { note ->
            note?.let { updateUI(it) }
        })
    }

    private fun updateUI(note: Note) {
        binding.categoryEditText.text = note.category.toEditable()
        binding.titleEditText.text = note.title.toEditable()
        binding.bodyEditText.text = note.body.toEditable()

        binding.lastModifiedTextView.text = getString(R.string.last_updated_withDate, DateFormat
            .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(note.lastModified))

        binding.createdTextView.text = getString(R.string.created_withDate, DateFormat
            .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(note.dateCreated))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_note, menu)
    }

    override fun onStop() {
        val category = binding.categoryEditText.text.toString()
        val title = binding.titleEditText.text.toString()
        val body = binding.bodyEditText.text.toString()
        viewModel.save(category, title, body)
        super.onStop()
    }

    companion object {

        private const val NOTE_ID = "note_ID"
        private const val QUERY = "query"
        private const val INTENT = "intent"

        private const val CATEGORY = "category"
        private const val TITLE = "title"
        private const val BODY = "body"

        fun newInstance(noteID: UUID, query: String?): NoteFragment {
            return NoteFragment().apply {
                arguments = Bundle().apply {
                    putString(NOTE_ID, noteID.toString())
                    putString(QUERY, query)
                }
            }
        }

        fun newInstance(noteID: UUID, intent: Intent): NoteFragment {
            return NoteFragment().apply {
                arguments = Bundle().apply {
                    putString(NOTE_ID, noteID.toString())
                    putParcelable(INTENT, intent as Parcelable)
                }
            }
        }
    }
}