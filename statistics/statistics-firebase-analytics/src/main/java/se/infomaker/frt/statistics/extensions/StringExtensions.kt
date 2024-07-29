package se.infomaker.frt.statistics.extensions

import se.infomaker.frtutilities.template.TemplateProvider
import java.io.StringWriter

internal fun String.resolveMustache(context: Any) : String {
    val writer = StringWriter()
    TemplateProvider.getMustache(this, "").execute(context, writer)
    return writer.toString()
}