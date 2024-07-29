package se.infomaker.livecontentui.livecontentdetailview.frequency

import android.content.Context

object FrequencyManagerProvider {

    private var manager: FrequencyManager? = null

    fun provide(context: Context): FrequencyManager {

        manager?.let {
            return it
        }
        manager = FrequencyManager(context)
        return manager!!
    }
}