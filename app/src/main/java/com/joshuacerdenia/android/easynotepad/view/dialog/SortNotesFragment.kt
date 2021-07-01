package com.joshuacerdenia.android.easynotepad.view.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.joshuacerdenia.android.easynotepad.R

class SortNotesFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val currentOrder = arguments?.getInt(ORDER) ?: 0

        val options = arrayOf(
            getString(R.string.last_updated),
            getString(R.string.date_created),
            getString(R.string.category),
            getString(R.string.title)
        )

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.sort_by))
            .setCancelable(true)
            .setSingleChoiceItems(options, currentOrder) { dialog, choice ->
                setFragmentResult(ORDER, Bundle().apply {
                    putInt(ORDER, choice)
                })
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .create()

        dialog.show()
        return dialog
    }

    companion object {

        const val TAG = "SortNotesFragment"
        const val ORDER = "ORDER"

        fun newInstance(order: Int): SortNotesFragment {
            return SortNotesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ORDER, order)
                }
            }
        }
    }
}