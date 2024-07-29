package se.infomaker.iap.articleview.extensions.ifragasatt

import android.content.Context
import android.net.Uri
import android.view.View
import com.google.gson.Gson
import com.samskivert.mustache.Mustache
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.OnPrepareView
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.extensions.ifragasatt.api.CommentCountRequest
import se.infomaker.iap.articleview.extensions.ifragasatt.api.IfragasattApiProvider
import se.infomaker.iap.articleview.extensions.ifragasatt.api.IfragasattUrlBuilder
import se.infomaker.iap.articleview.item.Item
import se.infomaker.iap.articleview.preprocessor.contentexplanation.ElementTemplatePreprocessor
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.inject.Singleton

class IfragasattPreprocessor @Inject constructor(
    private val ifragasattApiProvider: IfragasattApiProvider
) : Preprocessor{
    private val formatters = mutableMapOf<String,DateFormat>()

    override fun process(content: ContentStructure, jsonConfig: String, resourceProvider: ResourceProvider): ContentStructure {
        val config = Gson().fromJson(jsonConfig, IfragasattConfig::class.java)
        if (content.shouldHideComments(config)) {
            return content
        }

        val articleId = content.getArticleId(config)
        val customerId = config.customerId
        if (articleId == null || customerId == null) {
            return content
        }

        val parameters = if (config.queryParameters != null) extractQueryParameters(content.properties, config.queryParameters) else mutableMapOf()
        content.createImageUrl(config).let { articleImage ->
            parameters["articleImage"] = articleImage
        }
        parameters["customLogo"] = "hidden"
        parameters["articleId"] = articleId
        parameters["customerId"] = customerId.toString()
        getArticlePublishedTime(config, content)?.let {
            parameters["articlePublishedTime"] = it
        }

        val ifragasattItem = IfragasattItem(articleId, buildUrl(config, parameters))

        ifragasattItem.listeners.add(object : OnPrepareView{
            var disposable: Disposable? = null
            var onSuccess: (() -> Unit?)? = null

            override fun onPreHeat(item: Item, context: Context) {
                updateCommentCount()
            }

            fun updateCommentCount() {
                disposable?.let {
                    if(!it.isDisposed) {
                        it.dispose()
                    }
                }
                val api = ifragasattApiProvider.getApi(config.baseUrl ?: "https://api.ifragasatt.se")
                disposable = api.commentCount(CommentCountRequest(customerId, listOf(articleId)))
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe { result, error ->
                            result?.body()?.let {
                                ifragasattItem.commentCount = it.firstOrNull()?.commentCount
                                onSuccess?.invoke()
                            }
                            error?.let {
                                Timber.w(it, "Failed to update comment count")
                            }
                            disposable?.dispose()
                            disposable = null
                            onSuccess = null
                        }
            }

            override fun onPrepare(item: Item, view: View) {
                // Schedule a new update  if needed
                (item as? IfragasattItem)?.let {
                    if (it.commentCount == null && disposable == null) {
                        updateCommentCount()
                        return
                    }
                }
                // Postpone bind if data is not yet available
                if (ifragasattItem.commentCount == null) {
                    onSuccess = {
                        (view as? IfragasattItemView)?.bind(item as IfragasattItem)
                    }
                }
            }

            override fun onPrepareCancel(item:  Item, view: View) {
                onSuccess = null
                disposable?.dispose()
                disposable = null
            }
        })

        content.body.items.add(content.body.items.size, ifragasattItem)

        return content
    }

    private fun buildUrl(config: IfragasattConfig, parameters: MutableMap<String, String>) : String{
        val builder = Uri.parse(config.baseUrl ?: "https://comment.ifragasatt.se").buildUpon().appendPath("load")
        parameters.forEach { (key, value) ->
            builder.appendQueryParameter(key, value)
        }
        return builder.build().toString()
    }

    private fun getArticlePublishedTime(config: IfragasattConfig, content: ContentStructure): String? {
        config.articlePublishedTime?.let {
            it.property?.let { property ->
                val unformattedDate = content.properties.firstStringOrNull(property)
                if (unformattedDate != null) {
                    val inputFormat = getFormatter(it.inputFormat ?: "yyyy-MM-dd'T'HH:mm:ssX")
                    val outputFormat = getFormatter(it.outputFormat ?: "yyyy-MM-dd HH:mm:ss")
                    return outputFormat.format(inputFormat.parse(unformattedDate))
                }
            }
        }
        return null
    }

    private fun getFormatter(format: String) : DateFormat {
        return formatters[format] ?: SimpleDateFormat(format).also {
            formatters[format] = it
        }
    }

    private fun extractQueryParameters(properties: JSONObject, queryParameters: Map<String, String>): MutableMap<String, String> {
        return mutableMapOf<String,String>().apply {
            val mustacheCompiler = Mustache.compiler().withCollector(ElementTemplatePreprocessor.JSONOBJECT_COLLECTOR)
            for((key, template) in queryParameters) {
                put(key, mustacheCompiler.compile(template).execute(properties))
            }
        }
    }
}

private fun ContentStructure.shouldHideComments(config: IfragasattConfig): Boolean {
    if (config.hideCommentsProperty == null) {
        return false
    }
    return properties.firstStringOrNull(config.hideCommentsProperty)?.toLowerCase() == "true"
}

private fun ContentStructure.getArticleId(config: IfragasattConfig): String? {
    config.articleIdProperties?.forEach { property ->
        properties.firstStringOrNull(property)?.let {
            return it
        }
    }
    return null
}

fun JSONObject.firstStringOrNull(key: String): String?{
    optJSONArray(key)?.let {
        if (it.length() > 0) {
            return it.optString(0, null)
        }
    }
    return null
}

private fun ContentStructure.createImageUrl(config: IfragasattConfig): String {
    return IfragasattUrlBuilder(config.imageUrlProvider, properties).imageUrl.toString()
}

