package se.infomaker.iap.articleview.item.embed

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.constraintlayout.widget.ConstraintLayout
import com.navigaglobal.mobile.livecontent.R
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.iap.articleview.ArticleViewProviderWrapper
import se.infomaker.iap.theme.view.ThemeableImageView
import se.infomaker.iap.theme.view.ThemeableTextView
import timber.log.Timber


class HtmlEmbedItemView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var attachedWebView: WebView? = null
    private val clickInfoContainer: ViewGroup
    private val linkText: ThemeableTextView
    private val offlineText: ThemeableTextView
    private val gradient: View
    private val separator: View
    private var  hideGradientForHTMLView=false
    private var customHeight: Int? = null
        set(value) {
            field = value
            attachedWebView?.layoutParams?.height = value
            attachedWebView?.invalidate()
        }

    var moduleId: String? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.html_embed, this, true)
        clickInfoContainer = findViewById(R.id.click_to_view_container)
        linkText = findViewById(R.id.link_text)
        offlineText = findViewById(R.id.offline_text)
        gradient = findViewById(R.id.gradient)
        separator = findViewById(R.id.separator)

        hideGradientForHTMLView = ConfigManager.getInstance(context).getConfig("core", ArticleViewProviderWrapper::class.java).hideGradientForHTMLView
            if(hideGradientForHTMLView) {
                gradient.background=null
            }


    }

    fun enableOfflineMode() {
        offlineText.visibility = View.VISIBLE
        linkText.visibility = View.INVISIBLE
        clickInfoContainer.visibility = View.INVISIBLE
        gradient.visibility = View.INVISIBLE
        separator.visibility = View.INVISIBLE
        attachedWebView?.visibility = View.INVISIBLE
        attachedWebView?.loadUrl("about:blank")
    }

    fun enableOnlineMode() {
        offlineText.visibility = View.INVISIBLE
        linkText.visibility = View.VISIBLE
        clickInfoContainer.visibility = View.VISIBLE
        gradient.visibility = View.VISIBLE
        separator.visibility = View.VISIBLE

    }

    fun attachWebView(webView: WebView) {
        if (attachedWebView != null) {
            Timber.d("Scrapping WebView (no recycle!)")
            attachedWebView?.visibility = View.INVISIBLE
            (attachedWebView?.parent as? ViewGroup)?.removeView(attachedWebView)
            removeView(attachedWebView)
            attachedWebView = null
        }
        addView(webView.apply {
            this.layoutParams = HtmlEmbedItemViewFactory.LAYOUT_PARAMS_MATCH_MATCH
        }, 0)
        attachedWebView = webView
        attachedWebView?.visibility = View.VISIBLE
    }

    fun detachWebView(): WebView? {
        attachedWebView?.let {
            attachedWebView?.visibility = View.INVISIBLE
            removeView(it)
            attachedWebView = null
            return it
        }
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    fun updateTouchHandler(interactive: String, embedLinkSettings: EmbedLinkSettings? = null) {
        when (interactive) {
            "internal" -> {
                attachedWebView?.setOnTouchListener(null)
                clickInfoContainer.visibility = View.INVISIBLE
                return
            }
            "internalLink" -> {
                clickInfoContainer.visibility = View.VISIBLE
                attachedWebView?.setOnClickListener {
                    embedLinkSettings?.let {
                        onClick(it)
                    }
                }
                attachedWebView?.setOnTouchListener { v, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_UP -> {
                            embedLinkSettings?.let {
                                onClick(it)
                            }
                        }
                    }
                    true
                }
            }
        }
    }

    fun setCustomHeight(height: Int) {
        customHeight = height
    }

    fun hide() {
        attachedWebView?.visibility = View.GONE
        clickInfoContainer.visibility = View.GONE
        gradient.visibility = View.GONE
        separator.visibility = View.GONE
    }

    fun show() {
        attachedWebView?.visibility = View.VISIBLE
        clickInfoContainer.visibility = View.VISIBLE
        gradient.visibility = View.VISIBLE
        separator.visibility = View.VISIBLE
    }

    private fun onClick(embedLinkSettings: EmbedLinkSettings) {
        context.startActivity(FullscreenEmbedActivity.createIntent(context,
                embedLinkSettings.baseUrl,
                embedLinkSettings.data ?: "",
                embedLinkSettings.mime,
                embedLinkSettings.encoding,
                moduleId))
    }

    fun loadExternalLink(embedLinkSettings: EmbedLinkSettings) {
        attachedWebView?.visibility = View.GONE
        linkText.text = embedLinkSettings.linkText
        linkText.setOnClickListener { onClick(embedLinkSettings) }
    }
}