package se.infomaker.livecontentmanager.config

import com.google.gson.annotations.SerializedName

data class SublistConfig(
    val behaviour: PresentationBehaviour = PresentationBehaviour.DEFAULT,
    @SerializedName("defaultNumVisibleItems") val _defaultNumVisibleItems: Int? = null,
    val collapseIfMoreThan: Int? = null,
    val collapsible: Boolean = true
) {
    val defaultNumVisibleItems: Int?
        get() = _defaultNumVisibleItems ?: collapseIfMoreThan

    val expandable: Boolean
        get() = collapseIfMoreThan != null
}


