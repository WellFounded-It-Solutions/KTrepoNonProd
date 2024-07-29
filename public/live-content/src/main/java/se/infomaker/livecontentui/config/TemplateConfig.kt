package se.infomaker.livecontentui.config

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class TemplateConfig(
    @SerializedName("name") private val _name: String? = null
) {
    val name: String
        get() = _name ?: throw InvalidConfigurationException("A template needs a name.")
    var require = emptyList<String>()
    var bindingOverrides = emptyList<BindingOverride>()
}

data class ContentTypeTemplateConfig(val template: String, val config: Config?) {

    data class Config(val moduleId: String, val actions: Map<String, Action>?)

    data class Action(val action: String, val parameters: JsonObject?, val moduleId: String?)
}
