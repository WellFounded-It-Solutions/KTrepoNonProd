package se.infomaker.iap.provisioning.ui

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import com.navigaglobal.mobile.R
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.ktx.config
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.provisioning.config.SubscribeButtonWrapper
import se.infomaker.iap.provisioning.config.GlobalProvisioningConfig
import se.infomaker.iap.theme.view.ThemeableMaterialButton
import se.infomaker.iap.theme.view.ThemeableTextView

class PurchaseViewLoginOnly @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attrs, defStyleAttr) {

    private val resources by context.resources()
    private val config by config<GlobalProvisioningConfig>()

    private val customerServiceVisible: Boolean
    private val leadin: ThemeableTextView
    private val alreadySubscribedTitle: ThemeableTextView
    private val alreadySubscribedText: ThemeableTextView
    private val subscriptionExpired: ThemeableTextView
    private val subscriptionRenewalMessage: ThemeableTextView
    private val showLogin: ThemeableMaterialButton
    private val customerServiceButton: ThemeableMaterialButton
    private val logoutButton: ThemeableMaterialButton

    var isSubscriptionIn: Boolean = false
        set(value) {
            field = value

            val loginVisibility = if (!value) View.VISIBLE else View.GONE
            setDesiredVisibility(leadin, loginVisibility)
            setDesiredVisibility(alreadySubscribedTitle, loginVisibility)
            setDesiredVisibility(showLogin, loginVisibility)
            setDesiredVisibility(alreadySubscribedText, loginVisibility)

            val logoutVisibility = if (value) View.VISIBLE else View.GONE
            setDesiredVisibility(leadin, logoutVisibility)
            setDesiredVisibility(showLogin, logoutVisibility)
            setDesiredVisibility(alreadySubscribedTitle, logoutVisibility)
            setDesiredVisibility(alreadySubscribedText, logoutVisibility)

            setupBrandableText(leadin, "loginLockedArticle", R.string.provisioning_subscription_ended)
            setupBrandableText(alreadySubscribedTitle, "loginTitle", R.string.provisioning_no_subscription_contact_customer_service)
            setupBrandableText(alreadySubscribedText, "loginAlreadyCustomer", R.string.provisioning_no_subscription_contact_customer_service_body)
            setupBrandableText(showLogin, "loginButton", R.string.provisioning_customer_service_button)




            val customerServiceVisibility = if (value && customerServiceVisible) View.VISIBLE else View.GONE
            setDesiredVisibility(customerServiceButton, customerServiceVisibility)
        }



    var isLoggedIn: Boolean = false
        set(value) {
            field = value

            val loginVisibility = if (!value) View.VISIBLE else View.GONE
            setDesiredVisibility(leadin, loginVisibility)
            setDesiredVisibility(alreadySubscribedTitle, loginVisibility)
            setDesiredVisibility(showLogin, loginVisibility)
            setDesiredVisibility(alreadySubscribedText, loginVisibility)

            val logoutVisibility = if (value) View.VISIBLE else View.GONE
            setDesiredVisibility(subscriptionExpired, logoutVisibility)
            setDesiredVisibility(subscriptionRenewalMessage, logoutVisibility)
            setDesiredVisibility(logoutButton, logoutVisibility)
            var customerServiceVisibility = if (value && customerServiceVisible) View.VISIBLE else View.GONE

            val subscribeButtonText = ConfigManager.getInstance(context).getConfig("core", SubscribeButtonWrapper::class.java).subscribeButtonText
            subscribeButtonText?.let {
                customerServiceVisibility= View.VISIBLE
                customerServiceButton.text = subscribeButtonText
            }
            setDesiredVisibility(customerServiceButton, customerServiceVisibility)
        }

    init {
        val overrideLayout = resources.getLayoutIdentifier("override_inline_purchase_view_login_only")
        if (overrideLayout != 0) {
            LayoutInflater.from(context).inflate(overrideLayout, this)
        }
        else {
            LayoutInflater.from(context).inflate(R.layout.purchase_view_login_only_contents, this)

        }

        leadin = findViewById(R.id.leadin)
        alreadySubscribedTitle = findViewById(R.id.alreadySubscribedTitle)
        alreadySubscribedText = findViewById(R.id.alreadySubscribedText)
        subscriptionExpired = findViewById(R.id.subscriptionExpired)
        subscriptionRenewalMessage = findViewById(R.id.subscriptionRenewalMessage)

        showLogin = findViewById(R.id.showLogin)
        customerServiceButton = findViewById(R.id.contactCustomerServiceButton)
        logoutButton = findViewById(R.id.logoutButton)

        setupBrandableText(leadin, "loginLockedArticle", R.string.locked_article_login_only)
        setupBrandableText(alreadySubscribedTitle, "loginTitle", R.string.already_subscribed_text_login_only)
        setupBrandableText(alreadySubscribedText, "loginAlreadyCustomer", R.string.already_subscribed_text)
        setupBrandableText(subscriptionExpired, "loginSubscriptionExpired", R.string.subscription_expired)
        setupBrandableText(subscriptionRenewalMessage, "loginSubscriptionRenewalMessage", R.string.subscription_renewal_message_login_only)
        setupBrandableText(showLogin, "loginButton", R.string.login)

        val customerServiceUrl = config.provisioning?.customerServiceUrl
        customerServiceVisible = !customerServiceUrl.isNullOrEmpty()
        if (customerServiceVisible) {
            customerServiceButton.setOnClickListener { context.openCustomTab(Uri.parse(customerServiceUrl)) }
        }
    }

    private fun setupBrandableText(view: TextView, key: String, fallback: Int) {
        val text = resources.getString(key, null)
        when {
            text == null -> view.setText(fallback)
            text.isEmpty() -> {
                view.visibility = View.GONE
                view.setNoContent()
            }
            else -> view.text = text
        }
    }

    private fun setDesiredVisibility(view: View, desiredVisibility: Int) {
        if (desiredVisibility == View.VISIBLE && view.hasNoContent()) {
            // Avoid settings view which have no content and have been set to View.GONE to View.VISIBLE
            return
        }
        view.visibility = desiredVisibility
    }

    fun setOnLoginClickListener(onClick: ((View) -> Unit)) = showLogin.setOnClickListener(onClick)
    fun setOnLogoutClickListener(onClick: ((View) -> Unit)) = logoutButton.setOnClickListener(onClick)
}

private fun View.setNoContent() {
    setTag(R.id.no_content_tag, true)
}

private fun View.hasNoContent(): Boolean {
    return (getTag(R.id.no_content_tag) as? Boolean) ?: false
}