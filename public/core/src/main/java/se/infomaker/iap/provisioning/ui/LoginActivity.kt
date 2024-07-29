package se.infomaker.iap.provisioning.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.navigaglobal.mobile.R
import com.navigaglobal.mobile.databinding.ActivityLoginBinding
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.applyStatusBarColor
import se.infomaker.iap.provisioning.config.GlobalProvisioningConfig
import se.infomaker.iap.provisioning.credentials.SmartLockManager
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.ktx.apply
import se.infomaker.iap.theme.ktx.statusBarColor
import se.infomaker.iap.theme.ktx.theme
import timber.log.Timber


class LoginActivity: AppCompatActivity() {

    private val theme by theme()

    private lateinit var binding: ActivityLoginBinding
    private val model: LoginViewModel by viewModels()
    private lateinit var smartLockManager: SmartLockManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smartLockManager = SmartLockManager(this)
        if(resources.getBoolean(R.bool.portrait_only)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        theme.apply(binding.root)
        theme.getColor("provisioningBackground", theme.statusBarColor)?.let {
            window.applyStatusBarColor(it.get())
        }
        setupToolbar()

        trySmartlockLogin()

        binding.loginView.onStartLoginListener = { username, password ->
            model.login(username, password, onSuccess = {
                setResult(Activity.RESULT_OK)
                smartLockManager.setSmartLockCredentials(username, password) {
                    finish()
                }
            }, onError = {
                showError(it.message ?: "Wrong username or password", "Try again")
                Timber.e(it)
            })
        }
        model.isLoading().observe(this, Observer {
            binding.loginView.visibility =  if(model.isLoggedIn() || it == true) View.GONE else View.VISIBLE
            if (it == true) {
                binding.progress.show()
            }
            else {
                binding.progress.hide()
            }
        })
        if (savedInstanceState == null) {
            trySmartlockLogin()
        }
    }

    private fun trySmartlockLogin() {
        val config = ConfigManager.getInstance().getConfig("global", GlobalProvisioningConfig::class.java)
        val domain = config?.provisioning?.sso?.domain
        if (domain != null) {
            smartLockManager.getSmartLockCredentials(domain) { credential ->
                if (credential?.username != null && credential.password != null) {
                    binding.loginView.hideKeyboard()
                    binding.loginView.useCredentials(credential)
                }
            }
        }
        else {
            binding.loginView.showKeyboard()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!smartLockManager.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onStart() {
        super.onStart()
        smartLockManager.onStart()
    }

    override fun onStop() {
        super.onStop()
        smartLockManager.onStop()
    }
    override fun onPause() {
        super.onPause()
        binding.loginView.hideKeyboard()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val image = ThemeManager.getInstance(this).appTheme.getImage(null, "provisioningCloseIcon")
        if (image == null) {
            supportActionBar?.setHomeAsUpIndicator(R.drawable.close_button)
        } else {
            supportActionBar?.setHomeAsUpIndicator(image.resourceId)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            binding.loginView.hideKeyboard()
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private var finished: Boolean = false

    private fun showError(message: String, buttonText: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setPositiveButton(buttonText) { _, _ ->  }
        builder.show()
    }
}

fun Activity.restartApp() {
    baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)?.let {
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        it.putExtra("isRestart", true)
        intent?.extras?.let { bundle ->
            it.putExtras(bundle)
        }
        startActivity(it)
        overridePendingTransition(0, 0)
    }

    finish()
}