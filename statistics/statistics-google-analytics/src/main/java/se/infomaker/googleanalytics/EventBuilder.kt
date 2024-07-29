package se.infomaker.googleanalytics

import timber.log.Timber

class EventBuilder {
    private val map = mutableMapOf<String, String?>()


    fun setCategory(value: String?): EventBuilder {
        map["ec"] = value
        return this
    }

    fun setAction(value: String?): EventBuilder {
        map["ea"] = value
        return this
    }

    fun setLabel(value: String?): EventBuilder {
        map["el"] = value
        return this
    }

    fun setValue(value: String?): EventBuilder {
        map["ev"] = value
        return this
    }

    init {
        set("t", "event")
    }

    operator fun set(key: String?, value: String): EventBuilder {
        if (key != null) {
            map[key.replace("&", "")] = value
        } else {
            Timber.d("HitBuilder.set() called with a null paramName.")
        }
        return this
    }

    fun build() :Map<String,String?> {
        return mutableMapOf<String, String?>().also {
            it.putAll(map)
        }
    }

    fun setCustomDimension(index: Int, value: String) {
        map["cd$index"] = value
    }
}