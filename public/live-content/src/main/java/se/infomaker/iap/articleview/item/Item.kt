package se.infomaker.iap.articleview.item

import se.infomaker.iap.articleview.OnPrepareView
import se.infomaker.iap.articleview.item.decorator.ItemDecorator

/**
 * Represents an element in the content body
 */
abstract class Item(val uuid: String) {
    /**
     * This method takes care of checking if the match filter fits this item
     * @param query The query to match against
     * @return true if ALL filter params match, otherwise false
     */
    fun isMatching(query: Map<String, String>): Boolean {
        query.forEach { (key, value) ->
            if (matchingQuery[key] != value) return false
        }
        return true
    }

    open fun wordCount(): Int {
        return 0
    }

    protected abstract val matchingQuery: Map<String, String>

    abstract val typeIdentifier: Any

    /**
     * This is the type used in the item when querying items
     */
    abstract val selectorType: String
    val listeners = mutableSetOf<OnPrepareView>()
    val decorators = mutableListOf<ItemDecorator>()

    var sticky = false

    /**
     * Letting the compiler know that we are forcing our subclasses to
     * implement this either by being a data class or by actually
     * writing the equals method by hand.
     */
    abstract override fun equals(other: Any?): Boolean
}