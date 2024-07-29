package se.infomaker.frt.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import com.navigaglobal.mobile.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import se.infomaker.frt.ui.activity.FragmentHelper
import se.infomaker.frt.ui.activity.MainActivity
import se.infomaker.frtutilities.MainMenuItem
import se.infomaker.frtutilities.ktx.moduleInfo
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.provisioning.ProvisioningManagerProvider
import se.infomaker.iap.provisioning.ui.dp2px
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor

class PaywallWrapperFragment: androidx.fragment.app.Fragment() {

    private val garbage = CompositeDisposable()

    private val module by moduleInfo()
    private val resources by resources()
    private val requiresPermission by lazy { arguments?.getString(REQUIRES_PERMISSION_KEY) }
    private var extras: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extras = arguments?.getBundle("extras")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.paywall_wrapper, container, false)
        context?.let {
            val provisioningManager = ProvisioningManagerProvider.provide(it)

            var layoutIdentifier = resources.getLayoutIdentifier("paywall_header")
            if (layoutIdentifier == 0) {
                layoutIdentifier = R.layout.default_paywall_header
            }

            val contentContainer = view.findViewById<FrameLayout>(R.id.contentContainer)
            val paywallContainer = view.findViewById<FrameLayout>(R.id.paywallContainer)
            val progress = view.findViewById<MaterialProgressBar>(R.id.progress)
            val theme = ThemeManager.getInstance(context).getModuleTheme(module.identifier)

            if (hasBottomNavigation()) {
                paywallContainer.updatePadding(bottom = 56.dp2px())
            }

            val paywallFragment = provisioningManager.createPaywallFragment(requireActivity(), layoutIdentifier)
            val contentFragment = FragmentHelper.createModuleFragment(activity, module, extras)
            childFragmentManager.beginTransaction()
                    .replace(R.id.paywallContainer, paywallFragment)
                    .replace(R.id.contentContainer, contentFragment, CONTENT_FRAGMENT_TAG)
                    .commit()

            progress.supportProgressTintList = ColorStateList.valueOf(theme.getColor("primaryColor", ThemeColor.DKGRAY).get())
            provisioningManager.canDisplayContentWithPermission(requiresPermission ?: "premium")
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { showContent ->
                        contentContainer.visibility = View.VISIBLE
                        progress.visibility = View.INVISIBLE
                        if (showContent) {
                            paywallContainer.visibility = View.INVISIBLE
                            paywallContainer.setOnTouchListener(null)
                        } else {
                            paywallContainer.visibility = View.VISIBLE
                            paywallContainer.setOnTouchListener { _, _ ->
                                return@setOnTouchListener true

                            }
                        }
            }.addTo(garbage)
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        garbage.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        childFragmentManager.findFragmentByTag(CONTENT_FRAGMENT_TAG)?.onActivityResult(requestCode, resultCode, data)
    }

    private fun hasBottomNavigation(): Boolean {
        (activity as? MainActivity)?.let { mainActivity ->
            return !mainActivity.mainMenuConfig.mainMenuItems
                    .filter { it.menuLocation != "none" }
                    .any { it?.menuLocation == MainMenuItem.MENU_LOCATION_DRAWER }
        }
        return false
    }

    companion object {

        private const val REQUIRES_PERMISSION_KEY = "requiresPermission"
        private const val CONTENT_FRAGMENT_TAG = "currentContentFragment"

        @JvmStatic
        fun create(menuItem: MainMenuItem, extras: Bundle?) : PaywallWrapperFragment {
            val bundle = Bundle()
            bundle.putString("moduleId", menuItem.id)
            bundle.putString("moduleTitle", menuItem.title)
            bundle.putString("moduleName", menuItem.moduleName)
            bundle.putString("modulePromotion", menuItem.promotion)
            bundle.putString(REQUIRES_PERMISSION_KEY, menuItem.requiresPermission)
            bundle.putBundle("extras", extras)
            val fragment = PaywallWrapperFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}

private fun Disposable.addTo(composite: CompositeDisposable) {
    composite.add(this)
}