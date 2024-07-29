package se.infomaker.iap.action.display

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Xml
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import se.infomaker.frtutilities.JSONUtil
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.display.flow.view.ActionLinkSpannable
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.ktx.brandColor
import timber.log.Timber

fun String.actionLinkify(context: Context, moduleIdentifier: String, valueProvider: ValueProvider): SpannableStringBuilder {
    val stringBuilder = SpannableStringBuilder()
    val parser = Xml.newPullParser()
    parser.setFeature(Xml.FEATURE_RELAXED, true)
    parser.setInput("<linkify>$this</linkify>".byteInputStream(), "UTF-8")
    var done = false
    var emStart = 0
    var strongStart = 0
    var linkStart = 0
    var linkAttributes: Map<String, String>? = null
    try {
        while (!done) {
            parser.next()
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "em" -> emStart = stringBuilder.length
                        "strong" -> strongStart = stringBuilder.length
                        "a" -> {
                            linkAttributes = parser.getAttributes()
                            linkStart = stringBuilder.length
                        }
                    }

                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "linkify" -> done = true
                        "em" -> stringBuilder.setSpan(StyleSpan(Typeface.ITALIC), emStart, stringBuilder.length, 0)
                        "strong" -> stringBuilder.setSpan(StyleSpan(Typeface.BOLD), strongStart, stringBuilder.length, 0)
                        "a" -> {
                            stringBuilder.setSpan(ActionLinkSpannable(context, valueProvider,  moduleIdentifier, linkAttributes), linkStart, stringBuilder.length, 0)
                        }
                    }
                }
                XmlPullParser.TEXT -> stringBuilder.append(parser.text)
                XmlPullParser.END_DOCUMENT -> {
                    done = true
                }
            }
        }
    }
    catch (e: XmlPullParserException) {
        Timber.e(e, "Unexpected error")
    }

    return stringBuilder
}

fun Context.openCustomTab(uri: Uri) {
    val theme = ThemeManager.getInstance(this).appTheme
    val builder = CustomTabsIntent.Builder()
    val colorParams = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(theme.brandColor.get())
        .build()
    builder.setDefaultColorSchemeParams(colorParams)
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(this, uri)
}

fun Context.openUri(moduleIdentifier: String, url: String, valueProvider: ValueProvider?) {
    val uri = Uri.parse(url)
    when (uri.scheme) {
        "action" -> Operation.fromUri(uri, moduleIdentifier, valueProvider).perform(this, onResult = {})
        "http" -> openCustomTab(uri)
        "https" -> openCustomTab(uri)
        else -> {
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }
}

private fun XmlPullParser.getAttributes(): Map<String, String> {
    val attributes = mutableMapOf<String, String>()
    for (i in 0 until attributeCount) {
        attributes.put(getAttributeName(i), getAttributeValue(i))
    }
    return attributes
}

fun Operation.Companion.fromUri(uri: Uri, moduleIdentifier: String, valueProvider: ValueProvider?): Operation {
    val jsonObject = JSONObject()
    uri.queryParameterNames.forEach { keyPath ->
        JSONUtil.put(jsonObject, keyPath, uri.getQueryParameter(keyPath))
    }
    return Operation(uri.authority ?: "", moduleIdentifier, jsonObject, valueProvider)

}