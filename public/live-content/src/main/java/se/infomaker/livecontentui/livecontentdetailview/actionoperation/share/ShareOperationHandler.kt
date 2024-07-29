package se.infomaker.livecontentui.livecontentdetailview.actionoperation.share

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.livecontentui.livecontentdetailview.actionoperation.ActionOperationHandler
import se.infomaker.livecontentui.livecontentdetailview.actionoperation.Operation
import timber.log.Timber


class ShareOperationHandler(val context: Context) : ActionOperationHandler {
    var progressDialog: ProgressDialog? = null

    override fun perform(operation: Operation): String? {
        if(!canPerform(operation)) {
            Timber.w("Operation not supported on device, you should have checked with canPerform first. $operation")
            return null
        }
        if (progressDialog == null) {
            if ("share" == operation.action) {
                val shareIntentCallback = object : ShareBuilder.IntentCreatedCallback {
                    var isCanceled = false
                    override fun onDone(intent: Intent?) {
                        progressDialog?.dismiss()
                        progressDialog = null
                        if (!isCanceled && intent != null) {
                            context.startActivity(intent)
                        }
                    }

                    fun cancel() {
                        isCanceled = true
                    }
                }

                val sharingString = context.resources.getString(R.string.sharing)
                val pleaseWaitString = context.resources.getString(R.string.please_wait)
                progressDialog = ProgressDialog.show(context, sharingString, pleaseWaitString, true)
                progressDialog?.setCancelable(true)
                progressDialog?.setOnCancelListener {
                    shareIntentCallback.cancel()
                }


                //No specific provider selected, using default picker
                if (!operation.parameters.containsKey("provider")) {
                    DefaultShareBuilder(context).createIntent(operation, shareIntentCallback)
                }
                when (operation.parameters["provider"]) {
                    "facebook" -> {
                        FacebookShareBuilder(context).createIntent(operation, shareIntentCallback)
                    }
                    "twitter" -> {
                        TwitterShareBuilder(context).createIntent(operation, shareIntentCallback)
                    }
                    "mail" -> {
                        MailShareBuilder(context).createIntent(operation, shareIntentCallback)
                    }
                }
                return null
            }
        }
        return null
    }

    override fun canPerform(operation: Operation): Boolean {
        if ("share" == operation.action) {
            // If omitted, a generic share is performed
            if (!operation.parameters.containsKey("provider")) {
                DefaultShareBuilder(context).canPerform(operation)
                return true
            }
            when (operation.parameters["provider"]) {
                "facebook" -> {
                    return FacebookShareBuilder(context).canPerform(operation)
                }
                "twitter" -> {
                    return TwitterShareBuilder(context).canPerform(operation)
                }
                "mail" -> {
                    return MailShareBuilder(context).canPerform(operation)
                }
            }
        }
        return false
    }
}