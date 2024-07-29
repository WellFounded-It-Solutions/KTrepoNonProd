package se.infomaker.livecontentui.livecontentdetailview.actionoperation.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import se.infomaker.livecontentui.livecontentdetailview.actionoperation.Operation
import kotlin.concurrent.thread

class MailShareBuilder(val context: Context) : ShareBuilder() {
    companion object {
        private val INTENT_TYPE = "text/rfc822"
    }

    private fun Intent.mailify() {
        this.putExtra(Intent.EXTRA_EMAIL, "")
        this.data = Uri.parse("mailto:")
        this.action = Intent.ACTION_SENDTO
    }

    override fun createIntent(operation: Operation, callback: IntentCreatedCallback) {
        val text = getTextList(operation).joinToString("\n\n")

        val subject = operation.parameters[SUBJECT_KEY]
        if (operation.parameters.containsKey(IMAGE_KEY)) {
            val mailCallback = object : IntentCreatedCallback {
                override fun onDone(intent: Intent?) {
                    intent?.mailify()
                    callback.onDone(intent)
                }
            }
            ShareIntentUtil.createImageIntent(context, null, text, subject, operation.parameters[IMAGE_KEY] as String, mailCallback)
        } else {
            thread {
                val intent = ShareIntentUtil.createIntent(null, text, subject, INTENT_TYPE)
                intent.mailify()
                callback.onDone(intent)
            }
        }
    }

    /**
     * Checks if mail client is present
     */
    override fun canPerform(operation: Operation): Boolean {
        if (getTextList(operation).isEmpty() && !operation.parameters.containsKey(IMAGE_KEY)) {
            return false
        }

        val packageManager = context.packageManager
        val intent = ShareIntentUtil.createIntent(null, "", null, INTENT_TYPE)
        intent.mailify()
        val list = packageManager.queryIntentActivities(intent, 0)

        return list.size != 0
    }
}