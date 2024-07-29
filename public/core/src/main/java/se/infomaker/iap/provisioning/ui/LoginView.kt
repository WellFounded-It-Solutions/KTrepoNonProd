package se.infomaker.iap.provisioning.ui

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.navigaglobal.mobile.R
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.provisioning.config.SubscribeButtonWrapper
import se.infomaker.iap.provisioning.config.GlobalProvisioningConfig
import se.infomaker.iap.provisioning.credentials.KeychainResponse
import se.infomaker.iap.theme.view.ThemeableTextView


class LoginView  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val loginButton: MaterialButton
    private val emailEditText: TextInputEditText
    private val passwordEditText: TextInputEditText
    private val titleView: ThemeableTextView
    private val forgotPassword: ThemeableTextView
    private val alreadyCustomer: ThemeableTextView
    private val contactSupportButton: MaterialButton

    var onStartLoginListener: ((String, String) -> Unit)? = null


    init {
        LayoutInflater.from(context).inflate(R.layout.login_view, this)
        loginButton = findViewById(R.id.loginButton)
        titleView = findViewById(R.id.title)
        emailEditText = findViewById(R.id.emailTextInput)
        passwordEditText = findViewById(R.id.passwordTextInput)
        forgotPassword = findViewById(R.id.forgotPassword)
        alreadyCustomer = findViewById(R.id.alreadyCustomer)
        contactSupportButton = findViewById(R.id.contactSupportButton)

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                notifyListener()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        loginButton.setOnClickListener {
            hideKeyboard()
            notifyListener()
        }
        val config = ConfigManager.getInstance().getConfig("global", GlobalProvisioningConfig::class.java)
        val resourceManager = ResourceManager(context, "shared")
        setupTitle(resourceManager)
        setupForgotPassword(config, resourceManager)
        setupActivateAccount(config, resourceManager)
        setupContactSupportButton(config,resourceManager)
    }

    private fun setupActivateAccount(config: GlobalProvisioningConfig, resourceManager: ResourceManager) {

        val url = config.provisioning?.activateSubscriptionUrl
        if (url.isNullOrEmpty()) {
            alreadyCustomer.visibility = View.GONE
        }
        else {
            resourceManager.getString("loginAlreadyCustomer", null)?.let(alreadyCustomer::setText)
            alreadyCustomer.setOnClickListener {
                context.openCustomTab(Uri.parse(url))
            }
        }
    }

    private fun setupForgotPassword(config: GlobalProvisioningConfig, resourceManager: ResourceManager) {
        val url = config.provisioning?.forgotPasswordUrl
        if (url.isNullOrEmpty()) {
            forgotPassword.visibility = View.GONE
        }
        else {
            resourceManager.getString("loginForgotPassword", null)?.let(forgotPassword::setText)
            forgotPassword.setOnClickListener {
                context.openCustomTab(Uri.parse(url))
            }
        }
    }

    private fun setupTitle(resourceManager: ResourceManager) {
        val title = resourceManager.getString("loginTitle", null)
        when {
            title == null -> titleView.setText(R.string.default_login_title)
            title.isEmpty() -> titleView.visibility = View.GONE
            else -> titleView.text = title
        }
    }

    private fun notifyListener() {
        onStartLoginListener?.invoke(emailEditText.text.toString(), passwordEditText.text.toString())
    }

    fun clear() {
        emailEditText.text?.clear()
        passwordEditText.text?.clear()
    }

    fun showKeyboard() {
        emailEditText.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun useCredentials(credential: KeychainResponse) {
        emailEditText.setText(credential.username)
        passwordEditText.setText(credential.password)
    }

    private fun setupContactSupportButton(config: GlobalProvisioningConfig, resourceManager: ResourceManager) {
        val url = config.provisioning?.customerServiceUrl
        if (url.isNullOrEmpty()) {
            contactSupportButton.visibility = View.GONE
        }
        else {
            val customSupportButtonText = ConfigManager.getInstance(context).getConfig("core", SubscribeButtonWrapper::class.java).subscribeButtonText
            customSupportButtonText?.let {
                contactSupportButton.visibility = View.VISIBLE
                contactSupportButton.text = customSupportButtonText
                contactSupportButton.setOnClickListener {
                    hideKeyboard()
                    context.openCustomTab(Uri.parse(url))
                }
            }
        }
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}