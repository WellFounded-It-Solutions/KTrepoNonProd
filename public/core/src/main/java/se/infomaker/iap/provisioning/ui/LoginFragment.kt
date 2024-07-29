package se.infomaker.iap.provisioning.ui

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import com.navigaglobal.mobile.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import se.infomaker.frt.moduleinterface.ModuleInterface
import se.infomaker.frtutilities.ktx.findActivity
import se.infomaker.iap.provisioning.ProvisioningManagerProvider
import se.infomaker.iap.provisioning.backend.FunctionResult
import se.infomaker.iap.provisioning.backend.LinkAccountResponse
import se.infomaker.iap.theme.ThemeManager
import timber.log.Timber
import java.net.SocketTimeoutException

class LoginFragment: Fragment(), ModuleInterface {

    override fun onBackPressed(): Boolean = false
    override fun onAppBarPressed() {}
    override fun shouldDisplayToolbar(): Boolean = false
    var progress: ContentLoadingProgressBar? = null
    var loginView: LoginView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(resources.getBoolean(R.bool.portrait_only)){
            (context.findActivity())?.let { activity ->
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.login_fragment, container, false)
        ThemeManager.getInstance(context).appTheme.apply(view)
        progress = view.findViewById(R.id.progress)
        loginView = view.findViewById(R.id.loginView)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        setupToolbar(toolbar)
        hideProgress()
        loginView?.onStartLoginListener = {username, password ->
            showProgress()
            context?.let {context ->
                val manager = ProvisioningManagerProvider.provide(context)
                manager.loginManager()?.login(username, password, {
                    if (manager.billingManager() == null || manager.billingManager()?.getLastPurchase() == null) {
                        activity?.finish()
                        return@login
                    }
                    var retryCount = 3
                    var link: (() -> Unit)? = null
                    link = {
                        manager.linkAccount().subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe { _: FunctionResult<LinkAccountResponse>?, error: Throwable? ->
                            if (error is SocketTimeoutException) {
                                Timber.d("Retrying linking")
                                if (retryCount-- > 0) {
                                    link?.invoke()
                                    return@subscribe
                                }
                            }
                            when {
                                error != null -> showError(error)
                                else -> {
                                    manager.loginManager()?.isLinked = true
                                    activity?.finish()
                                }
                            }
                            hideProgress()
                        }
                    }
                    link.invoke()
                }, {
                    hideProgress()
                    showError(it)
                })
            }
        }
        return view
    }

    private fun showProgress() {
        progress?.show()
        loginView?.visibility = View.GONE
    }

    private fun hideProgress() {
        progress?.hide()
        loginView?.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        loginView?.hideKeyboard()
    }

    private fun setupToolbar(toolbar: Toolbar) {
        val image = ThemeManager.getInstance(toolbar.context).appTheme.getImage(null, "provisioningCloseIcon")
        if (image == null) {
            toolbar.setNavigationIcon(R.drawable.close_button)
        } else {
            toolbar.setNavigationIcon(image.resourceId)
        }
        toolbar.setNavigationOnClickListener {
            loginView?.hideKeyboard()
            activity?.finish()
        }
    }

    private fun showError(error: Throwable) {
        context?.let {context ->
            AlertDialog.Builder(context).setMessage(error.localizedMessage)
                    .setPositiveButton(getString(android.R.string.ok)) { _, _ -> }.show()
        }
    }
}