package se.infomaker.iap.ui.action

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import se.infomaker.frtutilities.ktx.findActivity
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import se.infomaker.iap.ui.promotion.PromotionManager

class ResetPromotionsActionHandler : ActionHandler {

    override fun canPerform(context: Context, operation: Operation): Boolean = true

    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {

        // TODO ResourceManager
        // TODO Parameters to specify which promotion to reset.

        val activity = context.findActivity()
        if (activity != null) {
            activity.window.decorView.findViewById<View>(android.R.id.content)?.let {
                Snackbar.make(it, "Promotions reset", Snackbar.LENGTH_SHORT)
                        .setAction("Undo") {
                            // NOP
                        }
                        .addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(snackbar: Snackbar, @DismissEvent event: Int) {
                                if (event != DISMISS_EVENT_ACTION) {
                                    resetPromotions(context, notify = false)
                                }
                            }
                        })
                        .setActionTextColor(Color.RED)
                        .show()
            } ?: run {
                resetPromotions(context)
            }
        }
        else {
            resetPromotions(context)
        }
        onResult(Result(true))
    }

    private fun resetPromotions(context: Context, notify: Boolean = true) {
        PromotionManager.getInstance(context).clearPromoted()
        if (notify) {
            Toast.makeText(context, "Promotions reset", Toast.LENGTH_LONG).show()
        }
    }
}