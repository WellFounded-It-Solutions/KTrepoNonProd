package se.infomaker.iap.articleview.item.embed

import android.content.Context
import android.view.View
import com.google.gson.Gson
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.OnPrepareView
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.Item

class HtmlEmbedItemPreprocessor : Preprocessor {

    private val iframeRegex: Regex = "(?:<iframe.*?>)".toRegex()
    private val schemeRegex: Regex = "(?:http(s?)://)?".toRegex()
    private val relativeUrlRegex: Regex = "(?<=[^:])(?<=[^%3a])(//)".toRegex(RegexOption.IGNORE_CASE)
    companion object {
        val SRC_REGEX: Regex = "(?<=src=\"|')(.+?)(?=\"|')".toRegex()
        val WIDTH_REGEX: Regex  = "(?:width=)(?:'|\")(\\d+)(?:'|\")".toRegex()
        val HEIGHT_REGEX: Regex = "(?:height=)(?:'|\")(\\d+)(?:'|\")".toRegex()
    }

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure
    {
        val htmlEmbedConfig = Gson().fromJson(config, HtmlEmbedItemPreprocessorConfig::class.java)

        content.body.items
                .filterIsInstance<HtmlEmbedItem>()
                .forEach { embedItem ->
                    var scheme:String?
                    embedItem.webViewPool = WebViewRecyclerPool

                    // Process baseUrl and scheme from config
                    htmlEmbedConfig.baseUrl?.let { baseUrl ->
                        scheme = schemeRegex.find(baseUrl)?.groups?.firstOrNull()?.value.toString()
                        scheme.let { scheme -> embedItem.scheme = scheme }
                        embedItem.baseUrl = baseUrl
                    }

                    htmlEmbedConfig.aspectRatio?.let { ar ->
                        embedItem.size = ar
                    }

                    htmlEmbedConfig.embed?.forEach { config ->

                        var pattern:Regex? = null
                        config.pattern?.let { pattern = it.toRegex() }

                        pattern?.let {
                        val patternMatch: MatchResult? = it.find(embedItem.data)
                            patternMatch?.let {
                                embedItem.linkType = config.method
                                embedItem.linkText = config.title
                            }
                        }
                    }

                    if(embedItem.data.isNotEmpty())
                    {
                        // Fix relative embed paths within an embed's HTML
                        embedItem.scheme?.let { embedScheme ->
                            embedItem.data = relativeUrlRegex.replace(embedItem.data, embedScheme)
                        }

                        val iframe = iframeRegex.find(embedItem.data)?.groups?.firstOrNull()?.value
                        iframe?.let {
                            embedItem.containsIframe = true
                            val src = SRC_REGEX.find(it)?.groups?.firstOrNull()?.value
                            src?.let {
                                embedItem.src = it
                            }
                        }

                        // Extract width of embed
                        val width = WIDTH_REGEX.find(embedItem.data)?.groups?.firstOrNull()?.value
                        width?.let { widthAsString ->
                            embedItem.width = "(\\d+)".toRegex().find(widthAsString)?.groups?.firstOrNull()?.value?.toInt() ?: 0
                        }
                        // Extract height of embed
                        val height = HEIGHT_REGEX.find(embedItem.data)?.groups?.firstOrNull()?.value
                        height?.let { heightAsString ->
                            embedItem.height = "(\\d+)".toRegex().find(heightAsString)?.groups?.firstOrNull()?.value?.toInt() ?: 0
                        }
                        embedItem.contentBasedAspectRatio = embedItem.width.toDouble() / embedItem.height.toDouble()

                        embedItem.data = WIDTH_REGEX.replace(embedItem.data, "width=\"100%\"")
                        embedItem.data = HEIGHT_REGEX.replace(embedItem.data, "height=\"100%\"")

                        preparePreloading(embedItem)
                    }
                }
        return content
    }

    private fun preparePreloading(item: HtmlEmbedItem)
    {
        item.listeners.add(object : OnPrepareView {
            override fun onPreHeat(item: Item, context: Context) {}

            override fun onPrepare(item: Item, view: View) {
                val embedItem = item as HtmlEmbedItem
                val embedView = view as HtmlEmbedItemView
                if (embedItem.linkType != "external") {
                    val webView = WebViewRecyclerPool.get(view.context, embedItem)
                    embedView.attachWebView(webView)
                }
            }

            override fun onPrepareCancel(item: Item, view: View) {
                if (view is HtmlEmbedItemView && item is HtmlEmbedItem) {
                    view.detachWebView()?.let { webView ->
                        WebViewRecyclerPool.recycle(webView, item)
                    }
                }
            }
        })
    }
}