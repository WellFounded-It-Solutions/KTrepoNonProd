package se.infomaker.frtutilities

import android.content.ContentResolver
import android.media.AudioAttributes
import android.net.Uri

class NotificationAudioHelper(val resourceManager: ResourceManager) {

    companion object {
        const val NOTIFICATION_RESOURCE_NAME = "snd_notify"
        const val DEFAULT_SOUND_RES = "shared"

        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
    }

    private fun String.toResourceUriOrNull(packageName: String): Uri? {
        return if (resourceManager.getRawIdentifier("${this}_${NOTIFICATION_RESOURCE_NAME}") != 0) {
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/raw/${this}_${NOTIFICATION_RESOURCE_NAME}")
        } else null
    }

    fun audioResourceUriOrNull(
        packageName: String,
        moduleId: String? = null,
        context: String? = null
    ): Uri? = context?.lowercase()?.toResourceUriOrNull(packageName) ?: run {
        moduleId?.lowercase()?.toResourceUriOrNull(packageName) ?: run {
            DEFAULT_SOUND_RES.toResourceUriOrNull(packageName)
        }
    }
}