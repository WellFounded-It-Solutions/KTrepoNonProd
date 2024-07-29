package se.infomaker.iap.provisioning.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.constraintlayout.widget.Barrier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.navigaglobal.mobile.R
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.iap.provisioning.credentials.Credential
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.util.UI
import se.infomaker.iap.theme.view.ThemeableMaterialButton
import se.infomaker.iap.theme.view.ThemeableTextView


class PurchaseView  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attrs, defStyleAttr) {
    private val container: ConstraintLayout
    private var loginButton: ThemeableMaterialButton?
    private var loginLink: ThemeableTextView?
    private var credentialList: RecyclerView
    private val alreadySubscribedTitleView: ThemeableTextView?
    private val alreadySubscribedText: ThemeableTextView?
    private val alreadySubscribedLeadIn: ThemeableTextView?
    private val credentialTitle: ThemeableTextView?
    private val loginContainer: ViewGroup
    private val logoutButton: ThemeableMaterialButton
    private val orGroup: Group
    private val loggedInGroup: Group
    private val loggedOutGroup: Group
    private val purchaseBarrier: Barrier

    var onSkuSelectedListener: ((SkuDetails) -> Unit)? = null
    private var adapter : ProductItemAdapter? = null

    private var credentialAdapter: CredentialAdapter? = null
    var onLoginWithCredential: ((Credential) -> Unit)? = null
    set(value) {
        field = value
        credentialAdapter?.onCredentialSelected = value
    }
    var credentials: LiveData<List<Credential>>? = null
    set(value) {
        field = value
        if (value != null && !value.value.isNullOrEmpty()) {
            credentialAdapter = CredentialAdapter(value, onLoginWithCredential)
            credentialList.adapter = credentialAdapter
            credentialList.visibility = View.VISIBLE
            credentialTitle?.visibility = View.VISIBLE
            loginLink?.visibility = View.VISIBLE
            loginButton?.visibility = View.GONE
        }
        else {
            credentialList.visibility = View.INVISIBLE
            credentialTitle?.visibility = View.GONE
            loginLink?.visibility = View.GONE
            loginButton?.visibility = View.VISIBLE
        }
    }

    var isLoggedIn: Boolean = false
        set(value) {
            field = value


            val set = ConstraintSet()
            set.clone(container)
            set.clear(R.id.or, ConstraintSet.TOP)

            val target = if(value) R.id.productList else R.id.alreadySubscribedText

            set.connect(R.id.or, ConstraintSet.TOP, target, ConstraintSet.BOTTOM, UI.dp2px(24F).toInt())
            purchaseBarrier.referencedIds = intArrayOf(if(value) R.id.subscriptionExpired else R.id.or)

            set.clear(R.id.productList, ConstraintSet.TOP)
            val aboveProductList = if(value) R.id.subscriptionExpired else R.id.or
            set.connect(R.id.productList, ConstraintSet.TOP, aboveProductList, ConstraintSet.BOTTOM, 0)

            set.applyTo(container)

            val loginVisible = if (!value) View.VISIBLE else View.GONE
            loggedOutGroup.visibility = loginVisible

            val logoutVisible = if (value) View.VISIBLE else View.GONE
            loggedInGroup.visibility = logoutVisible
        }

    init {
        val overrideLayout = ResourceManager(context, "shared").getLayoutIdentifier("override_appstart_purchase_view")
        if (overrideLayout != 0) {
            LayoutInflater.from(context).inflate(overrideLayout, this)
        }
        else {
            LayoutInflater.from(context).inflate(R.layout.appstart_purchase_view, this)
        }
        container = findViewById(R.id.container)
        loginContainer = findViewById(R.id.showLogin)
        loginButton = findViewById(R.id.showLoginButton)
        loginLink = findViewById(R.id.showLoginLink)
        logoutButton = findViewById(R.id.logoutButton)
        credentialTitle = findViewById(R.id.credentialTitle)
        alreadySubscribedTitleView = findViewById(R.id.alreadySubscribedTitle)
        alreadySubscribedText = findViewById(R.id.alreadySubscribedText)
        alreadySubscribedLeadIn = findViewById(R.id.leadin)
        orGroup = findViewById(R.id.orGroup)
        loggedInGroup = findViewById(R.id.loggedInGroup)
        loggedOutGroup = findViewById(R.id.loggedOutGroup)
        credentialList = findViewById(R.id.credentialList)
        purchaseBarrier = findViewById(R.id.purchaseBarrier)

        orGroup.visibility = View.GONE


        val recyclerView = findViewById<RecyclerView>(R.id.productList)

        recyclerView.layoutManager = LinearLayoutManager(context)
        ThemeManager.getInstance(context).appTheme.getColor("purchaseSubscriptionDivider", null)?.let { color ->
            val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
            dividerItemDecoration.setDrawable(ColorDrawable(color.get()))
            recyclerView.addItemDecoration(dividerItemDecoration)
        }

        adapter = ProductItemAdapter(ThemeManager.getInstance(context).appTheme) { skuDetails ->
            onSkuSelectedListener?.invoke(skuDetails)
        }.also {
            recyclerView.adapter = it
        }
        setupTitle()
        setupText()
        setupLeadin()
        setupCredentialList()
    }

    private fun setupCredentialList() {
        credentialList.layoutManager = LinearLayoutManager(context)
    }

    private fun setupLeadin() {
        val title = ResourceManager(context, "shared").getString("provisioningLeadin", null)
        when {
            title == null -> alreadySubscribedLeadIn?.setText(R.string.purchase_leadin)
            title.isEmpty() -> {
                alreadySubscribedLeadIn?.text = ""
                alreadySubscribedLeadIn?.visibility = View.GONE
            }
            else -> alreadySubscribedLeadIn?.text = title
        }
    }

    private fun setupTitle() {
        val title = ResourceManager(context, "shared").getString("provisioningTitle", null)
        when {
            title == null -> alreadySubscribedTitleView?.setText(R.string.default_purchase_title)
            title.isEmpty() -> alreadySubscribedTitleView?.visibility = View.GONE
            else -> alreadySubscribedTitleView?.text = title
        }
    }

    private fun setupText(){
        alreadySubscribedText?.movementMethod = LinkMovementMethod.getInstance()
        val resourceManager = ResourceManager(context, "shared")
        val text = resourceManager.getString("provisioningText", null)
        val textIdentifier = resourceManager.getStringIdentifier("provisioningText")

        when {
            text == null -> alreadySubscribedText?.setText(R.string.already_subscribed_text)
            text.isEmpty() -> {
                alreadySubscribedText?.text = ""
                alreadySubscribedText?.visibility = View.GONE
            }
            else -> {
                val charSequence = alreadySubscribedText?.resources?.getText(textIdentifier)
                alreadySubscribedText?.text = charSequence
            }
        }
    }

    fun setOnLoginClickListener(onClick: ((View) -> Unit)) {
        for (i in 0 until loginContainer.childCount) {
            loginContainer.getChildAt(i).setOnClickListener(onClick)
        }
    }

    fun setOnLogoutClickListener(onClick: ((View) -> Unit)) = logoutButton.setOnClickListener(onClick)

    fun updateAdapter(list: List<SkuDetails>)  {
        if (list.isNotEmpty()) {
            orGroup.visibility = View.VISIBLE
        }
        else {
            orGroup.visibility = View.GONE
        }
        adapter?.update(list)
    }
}