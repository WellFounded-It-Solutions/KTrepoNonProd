package se.infomaker.livecontentui.livecontentdetailview.actionoperation.share

import android.content.Intent
import se.infomaker.livecontentui.livecontentdetailview.actionoperation.Operation

abstract class ShareBuilder {
    companion object {
        val URL_KEY = "url"
        val IMAGE_KEY = "image"
        val SUBJECT_KEY = "subject"
        val TEXT_KEY = "text"
        val FOOTER_KEY = "footer"
    }

    abstract fun createIntent(operation: Operation, callback: IntentCreatedCallback)
    abstract fun canPerform(operation: Operation): Boolean

    interface IntentCreatedCallback {
        fun onDone(intent: Intent?)
    }

    protected fun getTextList(operation: Operation): List<String> {
        val textList = listOf(
                operation.parameters[TEXT_KEY],
                operation.parameters[URL_KEY],
                operation.parameters[FOOTER_KEY])

        return textList.filterNotNull().filter {
            !it.isEmpty()
        }
    }
}