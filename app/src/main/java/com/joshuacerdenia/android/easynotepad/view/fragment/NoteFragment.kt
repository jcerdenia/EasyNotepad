package com.joshuacerdenia.android.easynotepad.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.joshuacerdenia.android.easynotepad.data.model.Note
import com.joshuacerdenia.android.easynotepad.databinding.FragmentNoteBinding
import com.joshuacerdenia.android.easynotepad.extension.toEditable
import com.joshuacerdenia.android.easynotepad.view.OnToolbarInflated
import com.joshuacerdenia.android.easynotepad.viewmodel.NoteViewModel
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
        // Get Note ID from arguments.
        arguments?.getString(NOTE_ID)
            .run { UUID.fromString(this) }
            .run { viewModel.getNote(this) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.noteLive.observe(viewLifecycleOwner, { data ->
            data?.let { note ->
                viewModel.saveCopy()
                updateUI(note)
            }
        })
    }

    private fun updateUI(note: Note) {
        binding.categoryTextView.text = note.category.toEditable()
        binding.titleTextView.text = note.title.toEditable()
        binding.bodyTextView.text = note.body.toEditable()
        binding.lastModifiedTextView.text = note.lastModified.toString()
    }

    companion object {

        private const val NOTE_ID = "note_ID"
        private const val QUERY = "query"
        private const val ARG_INTENT = "arg_intent"

        fun newInstance(noteID: UUID, query: String?): NoteFragment {
            val args = Bundle().apply {
                putString(NOTE_ID, noteID.toString())
                putString(QUERY, query)
            }
            return NoteFragment().apply {
                arguments = args
            }
        }

        fun newInstance(noteID: UUID, intent: Intent): NoteFragment {
            val args = Bundle().apply {
                putString(NOTE_ID, noteID.toString())
                putParcelable(ARG_INTENT, intent as Parcelable)
            }
            return NoteFragment().apply {
                arguments = args
            }
        }
    }
}