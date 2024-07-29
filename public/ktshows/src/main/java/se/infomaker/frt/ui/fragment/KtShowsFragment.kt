package se.infomaker.frt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.appbar.AppBarLayout
import com.navigaglobal.mobile.ktshows.R
import kotlinx.coroutines.FlowPreview
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import se.infomaker.frt.moduleinterface.BaseModule
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import timber.log.Timber

import javax.inject.Inject


class KtShowsFragment : BaseModule() {

    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_kt_shows, container, false)

        // Initialize WebView
        webView = rootView.findViewById(R.id.webView)
        webView?.webViewClient = WebViewClient()
        webView?.settings?.javaScriptEnabled = true

        // Load the URL
        val url = "https://www.khaleejtimes.com/kt-shows-app" // Replace with your desired URL
        webView?.loadUrl(url)

        // Log analytics event
        StatisticsManager.getInstance().logEvent(
            StatisticsEvent.Builder()
                .viewShow()
                .moduleId(moduleIdentifier)
                .moduleName(moduleName)
                .moduleTitle(moduleTitle)
                .viewName("kt_shows")
                .build()
        )

        return rootView
    }

    override fun shouldDisplayToolbar(): Boolean = true

    override fun onBackPressed(): Boolean {
        // Handle onBackPressed for WebView
        if (webView?.canGoBack() == true) {
            webView?.goBack()
            return true
        }
        return false
    }

    override fun onAppBarPressed() {
        // Handle onAppBarPressed for WebView
        // You can implement custom behavior here, if needed
    }
}