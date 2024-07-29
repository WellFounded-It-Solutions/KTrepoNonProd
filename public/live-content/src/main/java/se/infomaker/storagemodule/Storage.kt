package se.infomaker.storagemodule

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.edit
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.Sort
import se.infomaker.frtutilities.ktx.config
import se.infomaker.frtutilities.ktx.privatePreferences
import se.infomaker.storagemodule.model.KeyValue
import se.infomaker.storagemodule.model.StorageMigration
import se.infomaker.storagemodule.model.Subscription
import se.infomaker.streamviewer.config.FollowConfig
import timber.log.Timber
import java.util.UUID

@SuppressLint("StaticFieldLeak")
object Storage {

    private lateinit var context: Context
    private lateinit var defaultConfiguration: RealmConfiguration

    @JvmStatic
    fun initialize(context: Context) {
        Realm.init(context)
        Timber.d("Starting init")

        defaultConfiguration = RealmConfiguration.Builder()
            .name("default.realm")
            .schemaVersion(3)
            .migration(StorageMigration())
            .build()
        Realm.setDefaultConfiguration(defaultConfiguration)
        this.context = context.applicationContext

        val config by context.config<FollowConfig>()
        config.cleanSubscriptionStorageIdentifier?.let { identifier ->
            val storedIdentifier = context.privatePreferences().getString("subscription_storage_clean_identifier", null)
            if (identifier.isNotBlank() && identifier != storedIdentifier) {
                Realm.deleteRealm(defaultConfiguration)
                context.privatePreferences().edit {
                    putString("subscription_storage_clean_identifier", identifier)
                }
            }
        }

        Timber.d("Finished init")
    }

    @JvmStatic
    fun getMatchSubscription(value: String, field: String): Subscription? {
        getSubscriptions().forEach {
            if (it.type == "match" && value == it.getValue("value") && field == it.getValue("field")) {
                return it
            }
        }
        return null
    }

    @JvmStatic
    fun getConceptSubscription(name: String, moduleId: String): Subscription? {
        getSubscriptions().forEach {
            if (it.type == "concept" && name == it.getValue("name") && moduleId == it.getValue("moduleId")) {
                return it
            }
        }
        return null
    }

    @JvmStatic
    fun getPersistRealm(): Realm {
        return Realm.getInstance(defaultConfiguration)
    }

    @JvmStatic
    fun getRealm(): Realm {
        return Realm.getInstance(defaultConfiguration)
    }

    @JvmStatic
    fun addOrUpdateSubscription(subscription: Subscription) {
        addOrUpdateSubscription(subscription, onNewCreated = {})
    }

    /**
     * Adds subscription or updates it if UUID
     * @param subscription
     */
    @JvmStatic
    fun addOrUpdateSubscription(subscription: Subscription, onNewCreated: (Subscription) -> Unit) {
        Realm.getInstance(defaultConfiguration).use {
            it.executeTransactionAsync(Realm.Transaction { realm ->
                realm.copyToRealmOrUpdate(subscription)
            }, Realm.Transaction.OnSuccess {
                onNewCreated.invoke(getSubscription(subscription.uuid!!)!!)
            })
        }
    }

    @JvmStatic
    fun addOrUpdateSubscription(uuid: String, name: String, type: String, values: Map<String, String>, hasPushOnCreate: Boolean, onNewCreated: (Subscription) -> Unit) {
        addOrUpdateSubscription(uuid, name, type, values, nextOrderIndex(), hasPushOnCreate, onNewCreated)
    }

    @JvmStatic
    fun addOrUpdateSubscription(uuid: String, name: String, type: String, values: Map<String, String>, order: Int, hasPushOnCreate: Boolean, onNewCreated: (Subscription) -> Unit) {
        val subscription = getSubscription(uuid)

        if (subscription == null) {
            val parameters = RealmList<KeyValue>()
            values.forEach {
                val value = KeyValue()
                value.id = UUID.randomUUID().toString()
                value.key = it.key
                value.value = it.value
                parameters.add(value)
            }

            addOrUpdateSubscription(Subscription(uuid, type, order, name, parameters, hasPushOnCreate), onNewCreated)
            return
        }

        Realm.getInstance(defaultConfiguration).use {
            it.executeTransaction {
                subscription.name = name
                values.forEach { entry ->
                    subscription.setValue(entry.key, entry.value)
                }
            }
        }
    }

    @JvmStatic
    fun updateSubscriptionValues(uuid: String, values: Map<String, String>) {
        getSubscription(uuid)?.let { subscription ->
            getRealm().use {
                it.executeTransaction {
                    values.forEach { entry ->
                        subscription.setValue(entry.key, entry.value)
                    }
                }
            }
        }
    }

    @JvmStatic
    fun delete(uuid: String) {
        getSubscription(uuid)?.let { delete(it) }
    }

    @JvmStatic
    fun delete(subscription: Subscription) {
        Realm.getInstance(defaultConfiguration).use {
            it.executeTransaction {
                subscription.parameters?.deleteAllFromRealm()
                subscription.deleteFromRealm()
            }
        }
    }

    @JvmStatic
    fun getSubscriptionsAsync(): RealmResults<Subscription> {
        return getPersistRealm().where(Subscription::class.java).findAllAsync().sort("order", Sort.DESCENDING)
    }

    @JvmStatic
    fun getSubscriptions(): RealmResults<Subscription> {
        return getPersistRealm().where(Subscription::class.java).findAll().sort("order", Sort.DESCENDING)
    }

    @JvmStatic
    fun getSubscriptions(type: String): RealmResults<Subscription> {
        return getPersistRealm().where(Subscription::class.java).equalTo("type", type).findAll().sort("order", Sort.DESCENDING)
    }

    @JvmStatic
    fun getSubscription(uuid: String): Subscription? {
        return getPersistRealm().where(Subscription::class.java).equalTo("uuid", uuid).findFirst()
    }

    @JvmStatic
    fun nextOrderIndex(): Int {
        val max = getPersistRealm().where(Subscription::class.java).max("order")
        if (max != null) {
            return max.toInt() + 1
        }
        return 0
    }

    private fun updateSubscriptionOrder(uuid: String, order: Int, onComplete: () -> Unit) {
        getPersistRealm().use { realm ->
            realm.executeTransactionAsync(
                {
                    val sub = it.where(Subscription::class.java).equalTo("uuid", uuid).findFirst()
                    sub?.order = order
                },
                {
                    Timber.d("Position updated")
                    onComplete()
                },
                {
                    Timber.d("Unable to update position: ${it.message}")
                    onComplete()
                }
            )
        }
    }

    @JvmStatic
    fun reorderSubscriptions(
        ordering: MutableList<Pair<String?, Int?>>,
        onComplete: () -> Unit
    ) {
        var completedTransactions = 0
        val success = ordering.size
        ordering.reversed().forEachIndexed { index, pair ->
            pair.first?.let {
                updateSubscriptionOrder(it, index) {
                    if (++completedTransactions >= success) {
                        onComplete()
                    }
                }
            }
        }
    }
}
