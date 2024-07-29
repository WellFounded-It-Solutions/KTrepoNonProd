package se.infomaker.livecontentui.extensions

import se.infomaker.livecontentmanager.parser.PropertyObject

private const val RELATED_KEY = "INTERNAL-related"
private const val FIRST_KEY = "INTERNAL-first"

internal fun PropertyObject.optLinkedConcepts() = optPropertyObjects("linkedConcepts")

internal fun PropertyObject.optRelatedArticles() = optPropertyObjects("relatedArticles")

internal fun MutableList<PropertyObject>.sort(order: List<String>?) {
    if (order == null) {
        return
    }

    sortWith(compareBy { order.indexOf(it.id) })
}

internal fun PropertyObject.optListObjects() = optPropertyObjects("articles", "packages", "lists")

internal fun PropertyObject.optSortedListObjects(): List<PropertyObject>? {
    val objects = optListObjects()?.toMutableList() ?: return null
    return objects.also { it.sort(optStringList("contentOrder")) }
}

internal var PropertyObject.isRelated: Boolean
    get() {
        optString(RELATED_KEY)?.let {
            return it.toBoolean()
        }
        return false
    }
    set(value) = putString(RELATED_KEY, value.toString())

internal var PropertyObject.isFirst: Boolean
    get() {
        optString(FIRST_KEY)?.let {
            return it.toBoolean()
        }
        return false
    }
    set(value) = putString(FIRST_KEY, value.toString())