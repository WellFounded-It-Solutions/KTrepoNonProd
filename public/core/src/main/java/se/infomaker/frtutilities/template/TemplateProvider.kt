package se.infomaker.frtutilities.template

import com.samskivert.mustache.Escapers
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Template

object TemplateProvider {
    private val cache = mutableMapOf<String, Template>()

    private val compiler = Mustache.compiler().withEscaper(Escapers.NONE)
    private val dummy = compiler.compile("")

    fun getMustache(template: String, defaultMissingValue: String): Template {
        cache["$template|$defaultMissingValue"].let{
            return@let it
        }

        return try {
            val mustache = compiler.defaultValue(defaultMissingValue).compile(template)
            cache[template] = mustache
            mustache
        }
        catch (e: StringIndexOutOfBoundsException) {
            dummy
        }
    }
}