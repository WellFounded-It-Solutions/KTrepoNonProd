package se.infomaker.storagemodule.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.io.Serializable
import java.util.UUID

open class Subscription() : RealmObject(), Serializable {
    @Required var name: String? = null
    @Required var type: String? = null
    @Required var order: Int? = null
    var parameters: RealmList<KeyValue>? = null
    @Required var pushActivated: Boolean? = null
    @PrimaryKey var uuid: String? = null
    var remoteStreamId: String? = null
    val state: SubscriptionState
    get() {
        return if (remoteStreamId != null && pushActivated == true) {
            SubscriptionState.ACTIVATED
        } else if (remoteStreamId != null && pushActivated != true) {
            SubscriptionState.PENDING_DEACTIVATION
        } else if (remoteStreamId == null && pushActivated == true) {
            SubscriptionState.PENDING_ACTIVATION
        } else{
            SubscriptionState.DEACTIVATED
        }
    }

    constructor(uuid: String,
                type: String,
                order: Int,
                name: String,
                parameters: RealmList<KeyValue>,
                pushActivated: Boolean) : this() {
        this.uuid = uuid
        this.type = type
        this.order = order
        this.name = name
        this.parameters = parameters
        this.pushActivated = pushActivated
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Subscription) return false

        if (name != other.name) return false
        parameters?.forEach {
            if (other.getValue(it.key) != it.value) {
                return false
            }
        }
        if (pushActivated != other.pushActivated) return false
        if (uuid != other.uuid) return false

        return true
    }

    fun allKeys() : List<String> {
        return parameters?.map { it.key } as List<String>
    }

    fun getValue(key: String) : String? {
        parameters?.forEach { if (it.key == key) {
            return it.value}
        }
        return null
    }

    fun getDouble(key: String) : Double? {
        return getValue(key)?.toDouble()
    }

    fun getFloat(key: String) : Float? {
        return getValue(key)?.toFloat()
    }

    fun setValue(key: String, value: String) {
        parameters?.forEach {
            if (it.key == key) {
                it.value = value
                return
            }
        }
        val keyValue = KeyValue()
        keyValue.id = UUID.randomUUID().toString()
        keyValue.key = key
        keyValue.value = value
        parameters?.add(keyValue)
    }

    fun setValue(key: String, value: Double) {
        setValue(key, value.toString())
    }

    fun setDouble(key: String, value: Float) {
        setValue(key, value.toString())
    }

    fun statisticsAttributes(): Map<String, String?> {
        return mapOf("subscriptionName" to name,
                "subscriptionId" to uuid)
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        parameters?.forEach {
            result = 31 * result + (it.value?.hashCode() ?: 0)
        }
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (pushActivated?.hashCode() ?: true.hashCode())
        result = 31 * result + (uuid?.hashCode() ?: 0)
        return result
    }

    /**
     * Creates a subscription object containing only the values that differs
     * Could be used for for example animations to know what to animate, or
     * for checking if location has changed to change search question.
     */
    fun createDiffObject(other: Subscription?): Subscription? {
        if (other == null) return null

        val subscription = Subscription()
        subscription.uuid = if (uuid != other.uuid) other.uuid else null
        subscription.type = if (type != other.type) other.type else null
        subscription.name = if (name != other.name) other.name else null
        subscription.pushActivated = if (pushActivated != other.pushActivated) other.pushActivated else null

        return subscription
    }
}

enum class SubscriptionState {
    ACTIVATED,
    DEACTIVATED,
    PENDING_ACTIVATION,
    PENDING_DEACTIVATION
}