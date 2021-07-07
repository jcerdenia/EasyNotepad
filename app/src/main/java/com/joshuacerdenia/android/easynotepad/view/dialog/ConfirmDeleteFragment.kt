package com.joshuacerdenia.android.easynotepad.view.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.joshuacerdenia.android.easynotepad.R

class ConfirmDeleteFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val number = arguments?.getInt(NUMBER) ?: 1
        val whatToDelete = resources.getQuantityString(R.plurals.notes, number, number)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete, whatToDelete))
            .setMessage(getString(R.string.no_undo))
            .setCancelable(true)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                setFragmentResult(CONFIRM_DELETE, Bundle().apply {
                    putBoolean(CONFIRM_DELETE, true)
                })
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
            .create()

        dialog.show()
        return dialog
    }

    companion object {

        const val TAG = "ConfirmDeleteFragment"
        const val CONFIRM_DELETE = "confirm_delete"
        private const val NUMBER = "number"

        fun newInstance(number: Int = 1): ConfirmDeleteFragment {
            return ConfirmDeleteFragment().apply {
                arguments = Bundle().apply {
                    putInt(NUMBER, number)
                }
            }
        }
    }
}