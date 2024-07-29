package se.infomaker.profile.view.items.mail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import se.infomaker.frtutilities.GlobalValueManager
import se.infomaker.iap.action.display.flow.mustachify
import com.navigaglobal.mobile.profile.R

data class MessageData(val email:String, var subject:String?, var body:String?)

class MailButtonHelper(private val context: Context, private val messageData: MessageData?) {

    private val globalValueManager = GlobalValueManager.getGlobalValueManager(context)

    companion object {
        const val EMPTY_STRING = ""
    }

    private fun generateEmailSubject(subject: String?): String = (subject ?: EMPTY_STRING).mustachify(globalValueManager)

    private fun generateEmailBody(): String = (messageData?.body ?: EMPTY_STRING).mustachify(globalValueManager)

    fun createFeedbackEmail() {
       messageData?.email?.let { email ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, Array(1) { email })
                putExtra(Intent.EXTRA_SUBJECT, generateEmailSubject(messageData.subject))
                putExtra(Intent.EXTRA_TEXT, generateEmailBody())
            }

            try {
                context.startActivity (Intent.createChooser(intent, context.resources.getString(R.string.intent_chooser)))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(context, R.string.no_email_client_available, Toast.LENGTH_SHORT).show()
            }
        }
    }
}