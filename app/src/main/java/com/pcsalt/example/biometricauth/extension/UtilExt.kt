package com.pcsalt.example.biometricauth.extension

import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

fun Activity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Activity.showAlert(
    title: String,
    message: String,
    positiveText: String,
    positiveAction: () -> Unit,
    negativeText: String,
    negativeAction: () -> Unit
) {
    AlertDialog.Builder(this).apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(positiveText) { _, _ -> positiveAction() }
        setNegativeButton(negativeText) { dialogView, _ ->
            negativeAction()
            dialogView.dismiss()
        }
    }.create().show()
}