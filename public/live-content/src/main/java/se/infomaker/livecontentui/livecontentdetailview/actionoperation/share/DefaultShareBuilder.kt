package se.infomaker.livecontentui.livecontentdetailview.actionoperation.share

import android.content.Context
import se.infomaker.livecontentui.livecontentdetailview.actionoperation.Operation

class DefaultShareBuilder(val context: Context) : ShareBuilder() {
    override fun createIntent(operation: Operation, callback: IntentCreatedCallback) {
        var text = operation.parameters["text"] as String
        var subject = operation.parameters[SUBJECT_KEY]
        val image = operation.parameters[IMAGE_KEY]

        if (operation.parameters.containsKey(URL_KEY)) {
            text += "\n\n" + operation.parameters[URL_KEY]
        }

        if (image != null) {
            ShareIntentUtil.createImageIntent(context, null, text, subject, image, callback)
        } else {
            ShareIntentUtil.createIntent(null, text, subject, callback = callback)
        }
    }

    override fun canPerform(operation: Operation): Boolean {
        return true
    }
}