package se.infomaker.livecontentui.livecontentdetailview.actionoperation.share

import android.content.Context
import se.infomaker.livecontentui.livecontentdetailview.actionoperation.Operation

class TwitterShareBuilder(val context: Context) : ShareBuilder() {
    companion object {
        private val PACKAGE_NAME = "com.twitter.android"
    }

    override fun createIntent(operation: Operation, callback: ShareBuilder.IntentCreatedCallback) {
        val text = getTextList(operation).joinToString("\n\n")

        if (operation.parameters.containsKey(IMAGE_KEY)) {
            ShareIntentUtil.createImageIntent(context, PACKAGE_NAME, text, operation.parameters[SUBJECT_KEY], operation.parameters[IMAGE_KEY] as String, callback)
        } else {
            ShareIntentUtil.createIntent(PACKAGE_NAME, text, operation.parameters[SUBJECT_KEY], callback = callback)
        }
    }

    override fun canPerform(operation: Operation): Boolean {
        if (getTextList(operation).isEmpty() && !operation.parameters.containsKey(IMAGE_KEY)) {
            return false
        }

        return ShareIntentUtil.isPackageExisted(context, PACKAGE_NAME)
    }
}