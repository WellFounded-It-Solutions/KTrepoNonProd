package se.infomaker.livecontentmanager.parser

import org.json.JSONArray
import org.json.JSONObject
import java.util.regex.Pattern


/**
 * Simplified version of mustache performing simple search and replace
 */
object CheapMustache {
    private var pattern = Pattern.compile("\\{\\{([^<]*)\\}\\}")

    fun resolve(template: String, values: JSONObject): String {
        val matcher = pattern.matcher(template)
        val sb = StringBuffer(template.length)
        while (matcher.find()) {
            val match = matcher.group(1)
            values.opt(match)?.let { matchKey ->
                (matchKey as? JSONArray)?.let {array ->
                    val replacement = toList(array).joinToString { it.toString() }
                    matcher.appendReplacement(sb, replacement)
                }
            }
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    private fun toList(array: JSONArray) : List<Any> {
        val list = mutableListOf<Any>()
        for (i in 0 until array.length()) {
            list.add(array[i])
        }
        return list
    }
}