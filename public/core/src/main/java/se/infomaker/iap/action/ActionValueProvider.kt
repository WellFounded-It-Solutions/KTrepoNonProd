package se.infomaker.iap.action

import io.reactivex.Observable
import se.infomaker.frtutilities.meta.ValueProvider

/**
 * This is a ValueProvider to use for actions.
 * Currently we flatMap the keys before we even put them in here:
 *  "myAction.myFirstKey.mySubKey" to "myValue"
 *  "myAction.mySecondKey.mySubKey" to "mySecondValue"
 * As soon as we need support for actual lists we need to do a new implementation of this.
 */
data class ActionValueProvider(private val parent: ValueProvider?, private val properties: Map<String, String>) : ValueProvider {
    override fun observeString(keyPath: String): Observable<String>? {
        // TODO Need other way to propagate updated properties 
        return Observable.just(getString(keyPath))
    }

    override fun getString(keyPath: String): String? {
        return properties[keyPath] ?: parent?.getString(keyPath)
    }

    override fun getStrings(keyPath: String): MutableList<String>? {
        return properties[keyPath]?.let { return@let mutableListOf(it) } ?: parent?.getStrings(keyPath)
    }
}