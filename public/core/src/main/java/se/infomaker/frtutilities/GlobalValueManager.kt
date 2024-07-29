package se.infomaker.frtutilities

import android.content.Context
import io.reactivex.Observable
import org.json.JSONObject
import se.infomaker.frtutilities.meta.ValueProvider

object GlobalValueManager {
    private val valueMap: MutableMap<String, JSONObject> = mutableMapOf()
    private val onChangeListeners = mutableSetOf<OnValueChangeListener>()

    fun put(key: String, values: JSONObject ) {
        valueMap.put(key, values)
        val iterator = onChangeListeners.iterator()
        while(iterator.hasNext()) {
            if (!iterator.next().onValueChanged()) {
                iterator.remove()
            }
        }
    }

    fun remove (key: String) {
        valueMap.remove(key)
    }

    fun getGlobalValueManager(context: Context) : ValueProvider {
        val valueProvider = ConfigManager.getInstance(context).valueProvider
        return object : ValueProvider {
            override fun observeString(keyPath: String): Observable<String>? {
                return Observable.merge(valueProvider.observeString(keyPath), observeGlobal(keyPath))
            }

            override fun getStrings(keyPath: String): MutableList<String> {
                getString(keyPath)?.let {
                    return mutableListOf(it)
                }
                return mutableListOf()
            }

            override fun getString(keyPath: String): String? {
                return valueProvider?.getString(keyPath) ?: globalValue(keyPath)
            }
        }
    }

    private fun observeGlobal(keyPath: String): Observable<String> {
        return Observable.create { emitter ->
            var last = globalValue(keyPath)
            if (last != null) {
                emitter.onNext(last)
            }
            val changeListener = object : OnValueChangeListener {
                override fun onValueChanged(): Boolean {
                    if (emitter.isDisposed) {
                        return false
                    }
                    val next = globalValue(keyPath)
                    if (last != next) {
                        last = next
                        // TODO consider representing null in other manner
                        emitter.onNext(last ?: "")
                    }
                    return !emitter.isDisposed
                }
            }
            onChangeListeners.add(changeListener)

            emitter.setCancellable {
                onChangeListeners.remove(changeListener)
            }
        }
    }

    private fun globalValue(keyPath: String): String? {
        val index = keyPath.indexOf(".")
        if (index == -1) {
            return null
        }
        val first = keyPath.substring(0, index)
        val second = keyPath.substring(index+1)
        valueMap[first]?.let {
            return JSONUtil.getString(it, second)
        }
        return null
    }
}