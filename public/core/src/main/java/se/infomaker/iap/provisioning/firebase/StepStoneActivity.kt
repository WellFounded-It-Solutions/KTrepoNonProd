package se.infomaker.iap.provisioning.firebase

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import se.infomaker.iap.provisioning.ProvisioningManagerProvider
import se.infomaker.iap.provisioning.ui.openCustomTab


/**
 * Step in and out of auth flows
 */
class StepStoneActivity : AppCompatActivity() {
    companion object {
        fun open(context: Context, url: String) {
            val intent = Intent(context, StepStoneActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
        }

        fun close(context: Context) {
            val intent = Intent(context, StepStoneActivity::class.java)
            intent.putExtra("finish", true)
            intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }

    private var paused: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("finish", false)) {
            finish()
        }
        else {
            intent.getStringExtra("url")?.let { url ->
                openCustomTab(Uri.parse(url))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (paused) {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }
}