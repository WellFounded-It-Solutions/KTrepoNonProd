package se.infomaker.streamviewer.topicpicker

import java.io.Serializable

data class Topic(val title: String?, val matching: String?, val property: String?, val topics: List<Topic>?) : Serializable