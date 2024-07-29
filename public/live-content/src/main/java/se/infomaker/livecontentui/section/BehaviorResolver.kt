package se.infomaker.livecontentui.section

import se.infomaker.livecontentui.config.ContentTypeTemplateConfig

class BehaviorResolver(configs: Map<String, ContentTypeTemplateConfig>?) {

    private val contentTypeTemplates: Map<String, ContentTypeTemplateConfig> = configs ?: emptyMap()

    fun getTemplateKey(contentType: String): String? {
        return contentTypeTemplates[contentType]?.template
    }

    fun getBehavior(contentType: String): ContentTypeTemplateConfig.Config? {
        return contentTypeTemplates[contentType]?.config
    }
}