package se.infomaker.livecontentui.livecontentdetailview.frequency

import se.infomaker.datastore.FrequencyRecord

interface DatabaseCallback {
    fun onFrequencyLoaded(frequencyRecord: List<FrequencyRecord>)

    fun onDataNotAvailable()

    fun onSuccess()
}