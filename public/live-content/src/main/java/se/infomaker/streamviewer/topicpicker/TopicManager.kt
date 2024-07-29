package se.infomaker.streamviewer.topicpicker

import se.infomaker.storagemodule.Storage
import se.infomaker.storagemodule.model.Subscription
import se.infomaker.streamviewer.StatsHelper
import se.infomaker.streamviewer.stream.SubscriptionUtil
import timber.log.Timber
import java.util.UUID

object TopicManager {
    val selected = mutableMapOf<String, Topic>()

    val deleted = mutableMapOf<String, Topic>()

    /**
     * Returns whether the topic is followed or not
     */
    fun isFollowing(topic: Topic): Boolean = SubscriptionUtil.hasMatchSubscription(topic.property, topic.matching)

    /**
     * Returns whether the topic is selected or not
     */
    fun Topic.isSelected(): Boolean = selected.containsKey(this.key())

    /**
     * Cancels and clears all selections
     */
    fun cancel() {
        selected.clear()
        deleted.clear()
    }

    /**
     * Creates a unique key for the topic
     */
    fun Topic.key(): String = "$property:$matching"

    /**
     * Toggles selection of topic
     */
    fun toggleSelected(topic: Topic) {

        if (selected.containsKey(topic.key())) {
            selected.remove(topic.key())

            if(!deleted.containsKey(topic.key())) deleted[topic.key()] = topic

        } else {
            selected[topic.key()] = topic
        }

        if(topic.topics?.isNullOrEmpty() == false){
            for(child in topic.topics){
                if (selected.containsKey(topic.key())) {
                    selected[child.key()] = child
                }
                else{
                    selected.remove(child.key())
                    if(!deleted.containsKey(child.key())) deleted[child.key()] = child

                }
            }
        }


    }

    /**
     * Starts following selected topics
     */
    fun save(enablePushOnSubscription: Boolean, moduelId: String, onDone: (subscription: Subscription) -> Unit) {

        delete()

        var orderIndex = Storage.nextOrderIndex()
        selected.forEach {

            val topic = it.value


            val subscription = topic.matching?.let { it1 -> topic.property?.let { it2 ->
                Storage.getMatchSubscription(it1,
                    it2
                )
            } }
            if (subscription == null) {
                Timber.d("Creating subscription for ${topic.title}")
                val values = mutableMapOf<String, String>()
                if (topic.property != null && topic.matching != null && topic.title != null) {
                    val topicUUID = UUID.randomUUID().toString()
                    values["field"] = topic.property
                    values["value"] = topic.matching
                    values["subscriptionName"] = topic.title
                    values["subscriptionId"] = topicUUID
                    StatsHelper.logSubscriptionEvent(moduelId, StatsHelper.CREATE_STREAM_EVENT, StatsHelper.TOPIC_PICKER_VIEW, values as Map<String, Any>?)
                    Storage.addOrUpdateSubscription(topicUUID, topic.title, "match", values, orderIndex++, enablePushOnSubscription) { subscription ->
                        onDone(subscription)
                        Timber.d("Created ${subscription.name}")
                    }
                }
            }


        }
        selected.clear()

    }

    fun delete() {

        deleted.forEach {
            val topic = it.value
            val subscription = topic.matching?.let { it1 -> topic.property?.let { it2 ->
                Storage.getMatchSubscription(it1,
                    it2
                )
            } }
            if (subscription != null) {
                Timber.d("Creating subscription for ${topic.title}")
                Storage.delete(subscription)
            }
        }
        deleted.clear()

    }



}