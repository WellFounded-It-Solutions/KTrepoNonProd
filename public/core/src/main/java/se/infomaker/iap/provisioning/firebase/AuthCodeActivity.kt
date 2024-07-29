package se.infomaker.iap.provisioning.firebase

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.navigaglobal.mobile.R
import se.infomaker.iap.provisioning.ProvisioningManagerProvider
import timber.log.Timber

class AuthCodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth_code)
        intent.data?.getQueryParameter("code")?.let { code ->
            Timber.d("Got code: $code")
            ProvisioningManagerProvider.provide(this).loginManager()?.login(code, {
                Timber.d("Completed")
            }, {
                Timber.e(it, "Failed to login")
                Toast.makeText(this, "Failed to login", Toast.LENGTH_SHORT).show()
            })
        }
        StepStoneActivity.close(this)
        finish()
    }
}