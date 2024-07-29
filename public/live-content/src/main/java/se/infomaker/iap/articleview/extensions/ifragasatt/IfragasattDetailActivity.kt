package se.infomaker.iap.articleview.extensions.ifragasatt

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import com.google.android.material.appbar.CollapsingToolbarLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.material.internal.ContextUtils
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ktx.isDebuggable
import se.infomaker.iap.theme.Theme
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.color.ThemeColor

class IfragasattDetailActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var toolbar: Toolbar? = null
    private val theme: Theme? = null
    private var toolbarLayout: CollapsingToolbarLayout? = null

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ifragasatt_detail_activity)
        val articleId = intent.getStringExtra("articleId")
        val commentUrl = intent.getStringExtra("commentUrl")

        webView = findViewById(R.id.web_view)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val theme = ThemeManager.getInstance(this).appTheme
        initToolbar()
        setStatusBarColor(theme)

        if (isDebuggable) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView?.apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
            val html = buildPage(articleId, commentUrl)
            settings.javaScriptEnabled = true
            loadDataWithBaseURL("https://comment.ifragasatt.se", html, "text/html", "UTF-8", null)
        }
    }

    private fun buildPage(articleId: String?, commentUrl: String?) : String {
        return "<div width=\"700px\" style=\"text-align: center;\" id=\"ifragasatt-$articleId\"></div>\n" +
                "        <script>\n" +
                "        // function which gets a script from Ifrågasätt servers.\n" +
                "        (function() {\n" +
                "        var d = document;\n" +
                "        var s = d.createElement('script');\n" +
                "        s.src = \"$commentUrl\";\n" +
                "            (d.head || d.body).appendChild(s);\n" +
                "        })();\n" +
                "        </script>"
    }

    private fun setStatusBarColor(theme: Theme){
        val statusbarColor = theme.getColor("statusbarColor", ThemeColor(-0x1000000))
        if (statusbarColor != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = statusbarColor.get()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowTitleEnabled(false)
            val up = resources.getDrawable(R.drawable.close_button)
            DrawableCompat.setTint(up, Color.DKGRAY)
            toolbar?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            toolbarLayout?.contentScrim = ColorDrawable(Color.WHITE)
            toolbarLayout?.setContentScrimColor(Color.WHITE)

            it.setHomeAsUpIndicator(up)
        }
    }
}