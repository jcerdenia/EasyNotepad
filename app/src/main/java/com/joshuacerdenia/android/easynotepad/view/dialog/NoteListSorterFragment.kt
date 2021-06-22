package com.joshuacerdenia.android.easynotepad.view.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

const val LAST_UPDATED = "Last updated"
const val DATE_CREATED = "Date created"
const val CATEGORY = "Category"
const val TITLE = "Title"

private const val ARG_SORT_PREF = "arg_sort_pref"

class NoteListSorterFragment : DialogFragment() {

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val options = arrayOf(LAST_UPDATED, DATE_CREATED, CATEGORY, TITLE)

        val optionsMap = mutableMapOf<String, Int>()
        var i = 0
        for (option in options) {
            optionsMap[option] = i
            i += 1
        }

        val sortPreference = arguments?.getString(ARG_SORT_PREF) as String
        val currentChoice = optionsMap[sortPreference]

        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setTitle("Sort by")
            .setCancelable(true)
            .setSingleChoiceItems(options, currentChoice!!) { dialog, choice ->
                val newChoice: String = getKeyFromMap(optionsMap, choice)
                targetFragment?.let { fragment ->
                    (fragment as Callbacks).onSortPreferenceSelected(newChoice)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val dialog = dialogBuilder.create()
        dialog.show()

        return dialog
    }

    private fun getKeyFromMap(map: Map<String, Int>, value: Int): String {
        val list: List<Pair<String, Int>> = map.toList()
        lateinit var key: String
        for (pair in list) {
            if (pair.second == value) {
                key = pair.first
            }
        }
        return key
    }

    interface Callbacks {
        fun onSortPreferenceSelected(sortPreference: String)
    }
}