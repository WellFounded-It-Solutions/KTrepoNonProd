package se.infomaker.livecontentui.livecontentrecyclerview.adapter

import org.json.JSONObject
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.ViewBehaviour
import se.infomaker.livecontentui.config.BindingOverride
import se.infomaker.livecontentui.config.TemplateConfig
import se.infomaker.livecontentui.config.ThemeOverlayConfig
import se.infomaker.livecontentui.extensions.hasAll
import se.infomaker.livecontentui.extensions.isRelated

class TemplatesBehaviour(private val resourceManager: ResourceManager, private val templates: Map<String, TemplateConfig>, private val themeOverlayMapping: ThemeOverlayConfig?) : ViewBehaviour<PropertyObject> {

    private val viewTypeMap = mutableMapOf<String?, Int>()
    private val viewTypeReverseMap = mutableMapOf<Int, String?>()
    private val defaultTemplateName by lazy { templates[DEFAULT_TEMPLATE_KEY]?.name ?: DEFAULT_TEMPLATE_NAME }

    private var nextViewTypeId = 0

    override fun viewTypeForKey(key: PropertyObject): Int {
        val templateKey = key.getTemplateKey(templates)
        viewTypeMap[templateKey]?.let {
            return it
        }
        return assignViewTypeId(templateKey)
    }

    /**
     * Assigns a viewtype id if none exists
     *
     * @param templateKey the assigned view type id
     * @return
     */
    @Synchronized
    private fun assignViewTypeId(templateKey: String?): Int {
        viewTypeMap[templateKey]?.let {
            return it
        }
        return nextViewTypeId.also {
            viewTypeMap[templateKey] = it
            viewTypeReverseMap[it] = templateKey
            nextViewTypeId++
        }
    }

    override fun layoutResourceForViewType(viewType: Int): Int {
        val key = keyForViewType(viewType)
        val layoutName = templates[key]?.name ?: defaultTemplateName
        return resourceManager.getLayoutIdentifier(layoutName)
    }

    private fun keyForViewType(viewType: Int) = viewTypeReverseMap[viewType] ?: DEFAULT_TEMPLATE_KEY

    override fun bindingOverridesForViewType(viewType: Int): List<BindingOverride>? {
        val key = keyForViewType(viewType)
        return templates[key]?.bindingOverrides
    }

    override fun themesForKey(key: PropertyObject) = themeOverlayMapping?.getOverlayThemeFile(key)?.let { listOf(it) }

    override fun presentationContextForKey(key: PropertyObject): JSONObject? = null

    companion object {
        private const val DEFAULT_TEMPLATE_KEY = "default"
        private const val DEFAULT_TEMPLATE_NAME = "standard_default_teaser"
    }
}

private fun PropertyObject.getTemplateKey(templates: Map<String, TemplateConfig>): String? {
    val templateKeyPrefix = if (isRelated) "related-" else ""
    optString("priority", null)?.let { priority ->
        val templateKey = "$templateKeyPrefix$priority"
        templates[templateKey]?.let {
            if (properties.hasAll(it.require)) return templateKey
        }
    }
    return "${templateKeyPrefix}default"
}