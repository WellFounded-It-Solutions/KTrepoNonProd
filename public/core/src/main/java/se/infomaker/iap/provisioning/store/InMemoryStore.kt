package se.infomaker.iap.provisioning.store

/**
 * In memory key value store
 */
class InMemoryStore : KeyValueStore {
    override fun putBoolean(key: String, value: Boolean?): KeyValueStore {
        (transaction?: values).let {
            if (value != null) {
                it.put(key, value)
            }
            else {
                it.remove(key)
            }
        }
        return this
    }

    override fun getBoolean(key: String, fallback: Boolean): Boolean = values[key] as? Boolean ?: fallback

    private val values = mutableMapOf<String, Any>()
    private var transaction: MutableMap<String, Any>? = null

    override fun put(key: String, value: String?): KeyValueStore {
        (transaction?: values).let {
            if (value != null) {
                it.put(key, value)
            }
            else {
                it.remove(key)
            }
        }
        return this
    }

    override fun get(key: String): String? = values[key] as? String

    override fun beginTransaction(): KeyValueStore {
        transaction = mutableMapOf()
        return this
    }

    override fun endTransaction() {
        transaction?.let {
            values.putAll(it)
        }
        transaction = null
    }

    override fun rollback() {
        transaction = null
    }

    override fun clear() {
        transaction = null
        values.clear()
    }
}