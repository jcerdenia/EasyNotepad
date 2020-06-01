package com.joshuacerdenia.android.easynotepad

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

private const val ARG_NUMBER = "arg_number"

class ConfirmDeleteFragment : DialogFragment() {

    companion object {
        fun newInstance(number: Int): ConfirmDeleteFragment {
            val args = Bundle().apply {
                putInt(ARG_NUMBER, number)
            }
            return ConfirmDeleteFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val number = arguments?.getInt(ARG_NUMBER)
        val whatToDelete = if (number == 1) {
            "note"
        } else {
            "$number notes"
        }

        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder
            .setTitle(getString(R.string.confirm_delete, whatToDelete))
            .setMessage(getString(R.string.no_undo))
            .setCancelable(true)
            .setPositiveButton("Yes") { dialog, _ ->
                targetFragment?.let { fragment ->
                    (fragment as Callbacks).onDeleteConfirmed()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }

        val dialog = dialogBuilder.create()
        dialog.show()

        return dialog
    }

    interface Callbacks {
        fun onDeleteConfirmed()
    }
}