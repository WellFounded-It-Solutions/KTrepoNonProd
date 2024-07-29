package se.infomaker.frt.ui.fragment

import org.json.JSONObject
import se.infomaker.livecontentmanager.query.QueryFilter
import java.net.URLEncoder

class FreeTextFilter(val value: String) : QueryFilter {

    override fun identifier() = "freetext:$value"

    override fun createStreamFilter() = JSONObject()

    override fun createSearchQuery(baseQuery: String): String =
        URLEncoder.encode("($baseQuery) AND ${value.sanitize()}", "utf-8")

    private fun String.sanitize(): String {
        return this.replace(REGEX.toRegex(), "\\\\$1")
    }

    companion object {
        private const val REGEX = "([+\\-!(){}\\[\\]^\"~*?:\\\\]|[&|]{2})"
    }
}