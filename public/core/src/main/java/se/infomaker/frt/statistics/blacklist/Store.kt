package se.infomaker.frt.statistics.blacklist

import java.util.Date

/**
 * Stores a single value of type T
 */
interface Store<T> {
    /**
     * Get the value from the store
     */
    fun get(): T?

    /**
     * Set value in the store
     */
    fun set(value: T?)

    /**
     * Last date the set method was called
     */
    fun lastSet(): Date?
}
