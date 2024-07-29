package se.infomaker.livecontentmanager.config

import java.io.Serializable

class TransformSettingsConfig: Serializable {
    var formatReplace: Map<String, String> = emptyMap()
}