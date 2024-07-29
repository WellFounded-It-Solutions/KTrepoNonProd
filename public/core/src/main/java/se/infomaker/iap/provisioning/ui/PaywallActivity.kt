package se.infomaker.iap.provisioning.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.view.View
import android.widget.FrameLayout
import android.widget.ViewFlipper
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.Observer
import com.navigaglobal.mobile.R
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.ktx.applyStatusBarColor
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.credentials.CredentialManager
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.ktx.apply
import se.infomaker.iap.theme.ktx.statusBarColor
import se.infomaker.iap.theme.ktx.theme
import timber.log.Timber


class PaywallActivity : AppCompatActivity(){

    private val theme by theme()
    private val model by viewModels<PaywallViewModel>()

    private var finished: Boolean = false
    private var restartAppOnGoToContent =  true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(resources.getBoolean(R.bool.portrait_only)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val usingTransition = intent.getBooleanExtra("usingTransition", false)
        if (!usingTransition) {
            // Only force refresh when we have skipped transition
            CredentialManager.forceRefresh(this) {
                Timber.d("Credentials refreshed")
            }

        }
        if (usingTransition) {
            val fade = Fade()
            fade.excludeTarget(android.R.id.statusBarBackground, true)
            fade.excludeTarget(android.R.id.navigationBarBackground, true)

            window.enterTransition = fade
            window.exitTransition = fade

            window.returnTransition = null
        }

        restartAppOnGoToContent = intent.getBooleanExtra("restartAppOnGoToContent", true)
        val showOptOutButton = intent.getBooleanExtra("showOptOutButton", true)
        val layoutIdentifier = ResourceManager(this, "shared").getLayoutIdentifier("override_activity_paywall")
        setContentView(if (layoutIdentifier == 0) R.layout.activity_paywall else layoutIdentifier)

        theme.apply(findViewById(R.id.root))
        theme.getColor("provisioningBackground", theme.statusBarColor)?.let {
            window.applyStatusBarColor(it.get())
        }

        val flipper = findViewById<ViewFlipper>(R.id.flipper)
        val purchaseView = findViewById<PurchaseView>(R.id.purchaseView)
        val createAccountView = findViewById<CreateAndLinkAccountView>(R.id.createAndLinkSubscriptionView)
        val progressBar = findViewById<ContentLoadingProgressBar>(R.id.progress)
        val linkAccountView = findViewById<FrameLayout>(R.id.linkAccount)
        createAccountView.setOptOutVisible(showOptOutButton)
        createAccountView.onOptOutListener = {
            model.setUserOptOutFromLogin(true)
            restartApp()
        }

        createAccountView.onCreateAccountListener = { email, password ->
            model.createAndLinkAccount(email, password, onSuccess = {
                createAccountView.clear()
                if (restartAppOnGoToContent) {
                    restartApp()
                }
            }, onError = {
                showError(it.localizedMessage, getString(android.R.string.ok))
                Timber.e(it)
            })
        }

        /*
         * Setup purchase view
         */
        purchaseView.setOnLogoutClickListener {
            model.logout(this)
        }

        model.loginStatus().observe(this, Observer { loginStatus ->
            purchaseView.isLoggedIn = loginStatus == LoginStatus.LOGGED_IN
        })

        purchaseView.onSkuSelectedListener = { skuDetails ->
            model.startPurchaseFlow(this, skuDetails)
        }
        purchaseView.setOnLoginClickListener {
            model.startLogin(this)
        }
        purchaseView.setOnLogoutClickListener { model.logout(this) }

        model.skuDetails().observe(this, Observer { availableProducts ->
            purchaseView.updateAdapter(availableProducts ?: listOf())
        })

        purchaseView.credentials = CredentialManager.liveCredentials

        purchaseView.onLoginWithCredential = { credential ->
            model.loginWithCredential(this, credential)
        }

        /*
         * Handle progress indicator
         */
        model.isLoading().observe(this) {
            flipper.setVisible(it == false)
            if (it == true) progressBar.show() else progressBar.hide()
        }

        /*
         * Handle view states
         */
        model.viewState().observe(this) { viewState ->
            when (viewState) {
                ViewState.PURCHASE -> {
                    flipper.displayedChild = flipper.indexOfChild(purchaseView)
                }
                ViewState.CREATE_ACCOUNT -> {
                    flipper.displayedChild = flipper.indexOfChild(createAccountView)
                }
                ViewState.LINK_SUBSCRIPTION -> {
                    flipper.displayedChild = flipper.indexOfChild(linkAccountView)
                }
                ViewState.SHOW_CONTENT -> {
                    gotoContent()
                }
                else -> {}
            }
        }
    }

    private fun gotoContent() {
        if (!finished) {
            Timber.d("Displaying content")
            finished = true

            if (restartAppOnGoToContent) {
                baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent?.extras?.let { bundle ->
                        it.putExtras(bundle)
                    }
                    startActivity(it)
                }
            }

            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 418) {
            if (resultCode == Activity.RESULT_OK) {
                restartApp()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun View.setVisible(visible: Boolean?) {
        visibility = if (visible == true) View.VISIBLE else View.INVISIBLE
    }

    private fun showError(message: String, buttonText: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setPositiveButton(buttonText) { _, _ ->  }
        builder.show()
    }
}


fun Activity.applyTheme() {
    ThemeManager.getInstance(this).appTheme.apply(findViewById(android.R.id.content))
}