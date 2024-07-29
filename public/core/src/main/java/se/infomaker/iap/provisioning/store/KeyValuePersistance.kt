package se.infomaker.iap.provisioning.store

/**
 * Simple key value persistence layer to avoid using android classes as it is not
 * available during unit tests
 */
interface KeyValueStore {
    /**
     * Store key value pair
     *
     * If put is called after beginTransaction the values are persisted first when end transaction is called
     * else the value is persisted immediately
     *
     * @param key key to store value under
     * @param value value to store, if null the value is removed
     * @return self to allow chaining of put calls
     */
    fun put(key: String, value: String?): KeyValueStore

    /**
     * Get value for key or null if value is not set
     */
    fun get(key: String): String?

    /**
     * Get value for key or null if value is not set
     */
    fun getBoolean(key: String, fallback: Boolean): Boolean

    /**
     * Begin making changes
     * @return self to allow chaining of put calls
     */
    fun beginTransaction(): KeyValueStore

    /**
     * Persist all operations since beginTransaction was called
     */
    fun endTransaction()

    /**
     * Abort current transaction
     */
    fun rollback()

    /**
     * Remove all keys/values
     */
    fun clear()

    fun putBoolean(key: String, value: Boolean?): KeyValueStore
}