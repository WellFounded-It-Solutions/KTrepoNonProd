package se.infomaker.iap.push.huawei

data class HuaweiPushConfig(private val pushTopic: String?) {

    val topicName: String?
        get() = pushTopic?.substringAfterLast(":")
}