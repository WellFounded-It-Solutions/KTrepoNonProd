package se.infomaker.livecontentui.livecontentdetailview.actionoperation.share

import android.content.Context
import se.infomaker.livecontentui.livecontentdetailview.actionoperation.Operation

class FacebookShareBuilder(val context: Context) : ShareBuilder() {
    companion object {
        private val PACKAGE_NAME = "com.facebook.katana"
    }

    override fun createIntent(operation: Operation, callback: ShareBuilder.IntentCreatedCallback) {
        var text = ""

        //Facebook only supports url OR image, we prioritize url
        if (operation.parameters.containsKey(URL_KEY)) {
            text += operation.parameters[URL_KEY]
            ShareIntentUtil.createIntent(PACKAGE_NAME, text, operation.parameters[SUBJECT_KEY], callback = callback)
        } else if (operation.parameters.containsKey(IMAGE_KEY)) {
            ShareIntentUtil.createImageIntent(context, PACKAGE_NAME, text, operation.parameters[SUBJECT_KEY], operation.parameters[IMAGE_KEY] as String, callback)
        } else {
            callback.onDone(null)
        }
    }

    override fun canPerform(operation: Operation): Boolean {
        return ShareIntentUtil.isPackageExisted(context, PACKAGE_NAME)
                && (operation.parameters.containsKey(URL_KEY)
                || operation.parameters.containsKey(IMAGE_KEY))
    }
}