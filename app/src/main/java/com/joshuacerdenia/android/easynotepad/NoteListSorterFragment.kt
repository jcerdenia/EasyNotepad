package com.joshuacerdenia.android.easynotepad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

private const val ARG_SORT_PREF = "arg_sort_pref"

class NoteListSorterFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(sortPreference: String): NoteListSorterFragment {
            val args = Bundle().apply {
                putString(ARG_SORT_PREF, sortPreference)
            }
            return NoteListSorterFragment().apply {
                arguments = args
            }
        }
    }

    /*
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder
            .setMessage(R.string.sort_by)
            .setCancelable(true)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val dialog = dialogBuilder.create()
        dialog.show()

        return dialog
        }
     */

    private lateinit var lastUpdatedButton: RadioButton
    private lateinit var dateCreatedButton: RadioButton
    private lateinit var categoryButton: RadioButton
    private lateinit var titleButton: RadioButton
    private lateinit var okButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            inflater.inflate(R.layout.fragment_note_list_sorter, container, false)

        lastUpdatedButton = view.findViewById(R.id.radioButton_sortBy_lastUpdated)
        dateCreatedButton = view.findViewById(R.id.radioButton_sortBy_dateCreated)
        categoryButton = view.findViewById(R.id.radioButton_sortBy_category)
        titleButton = view.findViewById(R.id.radioButton_sortBy_title)
        okButton = view.findViewById(R.id.ok_button)
        cancelButton = view.findViewById(R.id.cancel_button)

        return view
    }

    override fun onStart() {
        super.onStart()

        var sortPreference = arguments?.getString(ARG_SORT_PREF)

        when (sortPreference) {
            LAST_UPDATED -> lastUpdatedButton.isChecked = true
            DATE_CREATED -> dateCreatedButton.isChecked = true
            CATEGORY -> categoryButton.isChecked = true
            TITLE -> titleButton.isChecked = true
        }

        lastUpdatedButton.setOnClickListener {
            sortPreference = LAST_UPDATED
        }

        dateCreatedButton.setOnClickListener {
            sortPreference = DATE_CREATED
        }

        categoryButton.setOnClickListener {
            sortPreference = CATEGORY
        }

        titleButton.setOnClickListener {
            sortPreference = TITLE
        }

        okButton.setOnClickListener() {
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onSortPreferenceSelected(sortPreference!!)
            }
            dismiss()
        }

        cancelButton.setOnClickListener() {
            dismiss()
        }
    }

    interface Callbacks {
        fun onSortPreferenceSelected(sortPreference: String)
    }
}