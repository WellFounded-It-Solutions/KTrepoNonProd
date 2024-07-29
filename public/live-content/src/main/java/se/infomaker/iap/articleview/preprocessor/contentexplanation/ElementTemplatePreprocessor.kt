package se.infomaker.iap.articleview.preprocessor.contentexplanation

import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.samskivert.mustache.DefaultCollector
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.MustacheException
import org.json.JSONArray
import org.json.JSONObject
import se.infomaker.frtutilities.ResourceProvider
import se.infomaker.iap.articleview.ContentStructure
import se.infomaker.iap.articleview.Preprocessor
import se.infomaker.iap.articleview.item.decorator.BackgroundColorDecorator
import se.infomaker.iap.articleview.item.decorator.DecoratorColors
import se.infomaker.iap.articleview.item.decorator.MarginDecorator
import se.infomaker.iap.articleview.item.element.ElementItem
import se.infomaker.iap.articleview.item.element.toLinkSpannable
import se.infomaker.iap.articleview.preprocessor.reproducibleUuid
import se.infomaker.iap.articleview.requirement.RequirementChecker
import timber.log.Timber

class ElementTemplatePreprocessor : Preprocessor {

    override fun process(content: ContentStructure, config: String, resourceProvider: ResourceProvider): ContentStructure {
        val gson = Gson()
        val elementTemplateConfig = gson.fromJson(config, ElementTemplateConfig::class.java)

        if (!RequirementChecker.validate(content, elementTemplateConfig.require)) {
            return content
        }

        val templateMap = elementTemplateConfig.templateMap?.let { templateMap ->
            try {
                when (templateMap) {
                    is JsonObject -> {
                        gson.fromJson(templateMap, TemplateMap::class.java)
                    }
                    else -> {
                        resourceProvider.getAsset("configuration/${templateMap.asString}", TemplateMap::class.java)
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Could not create templateMap")
                null
            }
        }

        val mustacheCompiler = Mustache.compiler().withCollector(JSONOBJECT_COLLECTOR)

        val unformattedText = if(elementTemplateConfig.keyTemplate != null && templateMap != null) {
            try {
                val key = mustacheCompiler.compile(elementTemplateConfig.keyTemplate).execute(content.properties)
                templateMap[key]
            } catch (e: Exception) {
                elementTemplateConfig.template
            }
        } else {
            elementTemplateConfig.template
        }

        unformattedText?.let {
            try {
                val text = mustacheCompiler.compile(it).execute(content.properties)
                val attributes: Map<String, String> = if (elementTemplateConfig.type != null) mapOf("type" to elementTemplateConfig.type) else mapOf("type" to "elementTemplate")
                val themeKeys = listOfNotNull(elementTemplateConfig.type, "elementTemplate", "element", "default")
                val builder = createSpannableStringBuilder(text)
                val item = ElementItem(elementTemplateConfig?.reproducibleUuid.toString(), themeKeys, attributes, builder)

                elementTemplateConfig.type?.let { type ->
                    val backgroundDecorator = BackgroundColorDecorator(DecoratorColors(listOf(type + "Background")))
                    item.decorators.add(backgroundDecorator)

                    val leftMarginKeys = listOf("${type}MarginLeft", "${type}MarginHorizontal")
                    val rightMarginKeys = listOf("${type}MarginRight", "${type}MarginHorizontal")
                    val topMarginKeys = listOf("${type}MarginTop", "${type}MarginVertical")
                    val bottomMarginKeys = listOf("${type}MarginBottom", "${type}MarginVertical")
                    val marginDecorator = MarginDecorator(
                        left = leftMarginKeys,
                        top = topMarginKeys,
                        right = rightMarginKeys,
                        bottom = bottomMarginKeys
                    )
                    item.decorators.add(marginDecorator)
                }

                content.body.items.add(item)
            } catch (e: MustacheException) {
                Timber.w(e, "Could not fetch parameter from parameters.")
            }
        }
        return content
    }

    private fun createSpannableStringBuilder(text: String): SpannableStringBuilder {
        val htmlSpanned = text.asHtmlSpanned()
        val builder = SpannableStringBuilder(htmlSpanned)
        // Replace URLSpan with LinkSpannable to handle link clicks consistently
        for (span in htmlSpanned.getSpans<URLSpan>()) {
            builder.removeSpan(span)
            val spanStart = htmlSpanned.getSpanStart(span)
            val spanEnd = htmlSpanned.getSpanEnd(span)
            builder.removeSpan(span)
            builder.setSpan(span.toLinkSpannable(), spanStart, spanEnd, 0)
        }
        return builder
    }

    companion object {
        val JSONOBJECT_FETCHER: Mustache.VariableFetcher = object : Mustache.VariableFetcher {
            @Throws(Exception::class)
            override fun get(ctx: Any, name: String): Any? {
                val map = ctx as JSONObject
                when(val value = map.opt(name)) {
                    is JSONArray -> {
                        if (value.length() > 1) {
                            return value.toList()
                        }
                        return value.opt(0)
                    }
                    else -> {
                        return value
                    }
                }
            }
            override fun toString(): String = "JSONOBJECT_FETCHER"
        }

        val JSONOBJECT_COLLECTOR = object : DefaultCollector() {
            override fun createFetcher(ctx: Any?, name: String?): Mustache.VariableFetcher {
                if (ctx is JSONObject) {
                    return JSONOBJECT_FETCHER
                }
                return super.createFetcher(ctx, name)
            }
        }

    }
}

private fun JSONArray.toList(): MutableList<Any> {
    return mutableListOf<Any>().also {
        for( i in 0 until length()) {
            it.add(get(i))
        }
    }
}

private fun String.asHtmlSpanned() = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)