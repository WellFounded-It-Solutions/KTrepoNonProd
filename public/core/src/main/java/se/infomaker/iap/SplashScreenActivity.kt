package se.infomaker.iap

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.navigaglobal.mobile.R
import dagger.hilt.android.AndroidEntryPoint
import io.hansel.hanselsdk.Hansel
import se.infomaker.frt.ui.activity.MainActivity

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    private val splashTimeout: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        //Netcore
        Hansel.pairTestDevice(intent.dataString)

        // Using a Handler to delay the opening of the MainActivity
        findViewById<View>(android.R.id.content).postDelayed({
            // Start MainActivity after the splashTimeout
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Close the SplashScreen activity
            finish()
        }, splashTimeout)
    }


}