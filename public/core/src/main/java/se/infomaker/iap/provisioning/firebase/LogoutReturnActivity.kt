package se.infomaker.iap.provisioning.firebase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import se.infomaker.iap.provisioning.ProvisioningManagerProvider

class LogoutReturnActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProvisioningManagerProvider.provide(this).loginManager()?.completeLogout()
        finish()
    }
}