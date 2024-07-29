package se.infomaker.iap.provisioning.billing

import android.content.Context
import com.android.billingclient.api.Purchase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jakewharton.rxrelay2.PublishRelay
import java.util.concurrent.TimeUnit

/**
 * Notify when a new purchase happens
 */
class PurchaseNotifier(context: Context) {
    interface Listener {
        fun onAdded(purchase: Purchase)
    }

    private val relay = PublishRelay.create<String>()
    private val current = mutableMapOf<String, String>()
    private val listeners = mutableSetOf<Listener>()

    init {
        val sharedPreferences = context.applicationContext.getSharedPreferences("purchaseHistory", Context.MODE_PRIVATE)
        sharedPreferences.getString("stored", null)?.let {
            val stored : Map<String, String> = Gson().fromJson(it, object : TypeToken<Map<String, String>>() {}.type)
            current.putAll(stored)
        }
        val disposable = relay.debounce(1, TimeUnit.SECONDS).subscribe {
            sharedPreferences.edit().putString("stored", Gson().toJson(current)).apply()
        }
    }

    fun update(list: List<Purchase>) {
        list.filter {
            !current.containsKey(it.purchaseToken)
        }.forEach {
            save(it)
        }
    }

    private fun save(purchase: Purchase) {
        current[purchase.purchaseToken] = purchase.originalJson
        listeners.forEach { it.onAdded(purchase) }
        relay.accept(purchase.purchaseToken)
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}