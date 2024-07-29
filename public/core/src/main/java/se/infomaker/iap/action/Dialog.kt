package se.infomaker.iap.action

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun presentMessageDialog(context: Context, message: String, confirmAction: () -> Unit = {}) {
    AlertDialog.Builder(context)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok) { p0, _ ->
                p0.dismiss()
                confirmAction()
            }
            .setOnCancelListener {
                confirmAction()
            }
            .create().show()
}

fun presentConfirm(context: Context, confirm: String?, proceed: Runnable) {
    AlertDialog.Builder(context)
            .setMessage(confirm)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok) { p0, _ ->
                p0.dismiss()
                proceed.run()
            }
            .setNegativeButton(android.R.string.cancel) { p0, _ -> p0.dismiss() }
            .create().show()
}