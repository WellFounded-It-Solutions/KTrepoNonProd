package se.infomaker.frt.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import se.infomaker.iap.SpringBoardManager

class LaunchSchemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isTaskRoot) {

            SpringBoardManager.route(this, intent) {
                finish()
            }
        }
        else {
            finish()
        }
    }

}