package se.infomaker.iap.action.display.flow

import com.samskivert.mustache.DefaultCollector
import com.samskivert.mustache.Mustache
import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider
import se.infomaker.iap.action.display.flow.MustacheUtil.jsonMustacheCompiler
import se.infomaker.iap.action.display.flow.MustacheUtil.mustacheCompiler

object MustacheUtil {
    private val VALUEPROVIDER_FETCHER: Mustache.VariableFetcher = object : Mustache.VariableFetcher {
        @Throws(Exception::class)
        override fun get(ctx: Any, name: String): Any? = try {
            (ctx as? ValueProvider)?.getString(name)
        } catch (e: Exception) {
            null
        }

        override fun toString(): String = "VALUEPROVIDER_FETCHER"
    }

    private val VALUEPROVIDER_COLLECTOR = object : DefaultCollector() {
        override fun createFetcher(ctx: Any?, name: String?): Mustache.VariableFetcher {
            if (ctx is ValueProvider) {
                return VALUEPROVIDER_FETCHER
            }
            return super.createFetcher(ctx, name)
        }
    }


    val jsonMustacheCompiler = Mustache.compiler().withEscaper(JSONEscaper).nullValue("").withCollector(VALUEPROVIDER_COLLECTOR)
    val mustacheCompiler = Mustache.compiler().escapeHTML(false).nullValue("").withCollector(VALUEPROVIDER_COLLECTOR)
}

fun JSONObject.mustachify(valueProvider: ValueProvider?): JSONObject {
    return JSONObject(jsonMustacheCompiler.compile(toString()).execute(valueProvider))
}

fun String.mustachify(valueProvider: ValueProvider?): String {
    return mustacheCompiler.compile(this).execute(valueProvider)
}

object JSONEscaper : Mustache.Escaper {
    override fun escape(raw: String?): String? {
        return if (raw != null) JsonEscape.escape(raw) else null
    }
}
