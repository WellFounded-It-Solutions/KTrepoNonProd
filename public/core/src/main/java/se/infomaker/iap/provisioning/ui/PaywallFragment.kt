package se.infomaker.iap.provisioning.ui

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.ViewFlipper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.navigaglobal.mobile.R
import se.infomaker.frtutilities.ktx.requireActivity
import se.infomaker.iap.provisioning.LoginStatus
import se.infomaker.iap.provisioning.ProvisioningManagerProvider
import se.infomaker.iap.theme.ThemeManager
import timber.log.Timber


class PaywallFragment : Fragment() {

    private lateinit var model: InlinePaywallViewModel
    var progress: ContentLoadingProgressBar? = null
    private var moduleId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        model = ViewModelProvider(this).get(InlinePaywallViewModel::class.java)

        moduleId = arguments?.getString("moduleId")
        val headerLayout = arguments?.getInt(HEADER_LAYOUT, 0) ?: 0
        container?.context?.let { context ->
            return ConstraintLayout(context).apply {
                val tv = TypedValue()
                var actionBarHeight = 0
                if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                }

                layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT).also {
                    it.setMargins(0, actionBarHeight, 0, 0)
                }
                id = R.id.constraint_layout_holder

                val paywallAnchor = LayoutInflater.from(context).inflate(R.layout.paywall_anchor, this, false)
                addView(paywallAnchor)

                val paywallHeaderView: View
                if (headerLayout != 0) {
                    paywallHeaderView = LayoutInflater.from(context).inflate(headerLayout, this, false)
                    addView(paywallHeaderView)
                } else {
                    paywallHeaderView = LayoutInflater.from(context).inflate(R.layout.empty_paywall_header, this, false)
                    addView(paywallHeaderView)
                }

                addView(ScrollView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    id = R.id.paywall
                    val paywallView = LayoutInflater.from(context).inflate(R.layout.paywall_fragment, this, false)
                    addView(paywallView)
                })

                val constraintSet = ConstraintSet()
                constraintSet.clone(this)

                constraintSet.connect(ConstraintSet.PARENT_ID, ConstraintSet.START, R.id.paywallAnchor, ConstraintSet.START, 0)
                constraintSet.connect(ConstraintSet.PARENT_ID, ConstraintSet.END, R.id.paywallAnchor, ConstraintSet.END, 0)
                constraintSet.connect(ConstraintSet.PARENT_ID, ConstraintSet.TOP, R.id.paywallAnchor, ConstraintSet.TOP, 0)

                constraintSet.connect(ConstraintSet.PARENT_ID, ConstraintSet.START, paywallHeaderView.id, ConstraintSet.START, 0)
                constraintSet.connect(ConstraintSet.PARENT_ID, ConstraintSet.END, paywallHeaderView.id, ConstraintSet.END, 0)
                constraintSet.connect(paywallHeaderView.id, ConstraintSet.TOP, R.id.paywallAnchor, ConstraintSet.BOTTOM, 0)
                constraintSet.connect(paywallHeaderView.id, ConstraintSet.BOTTOM, R.id.paywall, ConstraintSet.TOP, 0)

                constraintSet.connect(ConstraintSet.PARENT_ID, ConstraintSet.START, R.id.paywall, ConstraintSet.START, 0)
                constraintSet.connect(ConstraintSet.PARENT_ID, ConstraintSet.END, R.id.paywall, ConstraintSet.END, 0)
                constraintSet.connect(R.id.paywall, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)

                constraintSet.applyTo(this)
                ThemeManager.getInstance(context).getModuleTheme(moduleId).apply(this)
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scrollView = view.findViewById<ScrollView>(R.id.paywall)
        val flipper = view.findViewById<ViewFlipper>(R.id.flipper)
        val progressBar = view.findViewById<ContentLoadingProgressBar>(R.id.progress)
        val linkAccountView = view.findViewById<FrameLayout>(R.id.linkAccount)

        /*
         * Handle view states
         */
        model.viewState().observe(viewLifecycleOwner) {
            scrollView.visibility = View.VISIBLE
            Timber.d("Current state $it")
            when (it) {
                ViewState.PURCHASE -> {
                    if (model.loginEnabled() && model.purchasesEnabled()) {
                        val stub = view.findViewById<ViewStub>(R.id.purchaseViewStub)
                        val inflated = stub.inflate() as InlinePurchaseView
                        ThemeManager.getInstance(view.context).getModuleTheme(moduleId).apply(inflated)
                        flipper.displayedChild = flipper.indexOfChild(inflated)

                        inflated.setOnLogoutClickListener {
                            model.logout(view.context.requireActivity())
                        }
                        model.loginStatus().observe(viewLifecycleOwner) { loginStatus ->
                            inflated.isLoggedIn = loginStatus == LoginStatus.LOGGED_IN
                        }
                        inflated.onSkuSelectedListener = { skuDetails ->
                            model.startPurchaseFlow(view.context.requireActivity(), skuDetails)
                        }
                        inflated.setOnLoginClickListener {
                            ProvisioningManagerProvider.provide(view.context).loginManager()?.showLogin(view.context.requireActivity())
                        }
                        inflated.setOnLogoutClickListener { model.logout(view.context.requireActivity()) }

                        model.skuDetails().observe(viewLifecycleOwner) { availableProducts ->
                            inflated.updateAdapter(availableProducts ?: listOf())
                        }
                    } else if (!model.loginEnabled() && model.purchasesEnabled()) {
                        val stub = view.findViewById<ViewStub>(R.id.purchaseViewNoLoginStub)
                        val inflated = stub.inflate() as PurchaseViewNoLogin
                        ThemeManager.getInstance(view.context).getModuleTheme(moduleId).apply(inflated)
                        flipper.displayedChild = flipper.indexOfChild(inflated)

                        model.skuDetails().observe(viewLifecycleOwner) { availableProducts ->
                            inflated.updateAdapter(availableProducts ?: listOf())
                        }

                        inflated.onSkuSelectedListener = { skuDetails ->
                            model.startPurchaseFlow(view.context.requireActivity(), skuDetails)
                        }
                        flipper.displayedChild = flipper.indexOfChild(inflated)

                    } else if (model.loginEnabled() && !model.purchasesEnabled()) {
                        val stub = view.findViewById<ViewStub>(R.id.purchaseViewLoginOnlyStub)
                        val inflated = stub.inflate() as PurchaseViewLoginOnly
                        ThemeManager.getInstance(view.context).getModuleTheme(moduleId).apply(inflated)

                        model.loginStatus().observe(viewLifecycleOwner) { loginStatus ->
                            inflated.isLoggedIn = loginStatus == LoginStatus.LOGGED_IN
                        }

                        inflated.setOnLoginClickListener {
                            ProvisioningManagerProvider.provide(view.context).loginManager()?.showLogin(view.context.requireActivity())
                        }
                        inflated.setOnLogoutClickListener { model.logout(view.context.requireActivity()) }

                        flipper.displayedChild = flipper.indexOfChild(inflated)
                    }
                }
                ViewState.CREATE_ACCOUNT -> {
                    val stub = view.findViewById<ViewStub>(R.id.createAndLinkSubscriptionViewStub)
                    val inflated = stub.inflate() as CreateAndLinkAccountView
                    ThemeManager.getInstance(view.context).getModuleTheme(moduleId).apply(inflated)
                    flipper.displayedChild = flipper.indexOfChild(inflated)
                }
                ViewState.LINK_SUBSCRIPTION -> {
                    flipper.displayedChild = flipper.indexOfChild(linkAccountView)
                }
                else -> scrollView.visibility = View.GONE
            }
        }

        /*
         * Handle progress indicator
         */
        model.isLoading().observe(viewLifecycleOwner) {
            flipper.setVisible(it != true)
            if (it == true) progressBar.show() else progressBar.hide()
        }
    }

    companion object {
        const val HEADER_LAYOUT = "headerLayout"
    }
}

private fun View.setVisible(visible: Boolean?) {
    visibility = if (visible == true) View.VISIBLE else View.INVISIBLE
}