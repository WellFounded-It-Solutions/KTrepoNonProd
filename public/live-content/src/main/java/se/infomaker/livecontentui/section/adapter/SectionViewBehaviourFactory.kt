package se.infomaker.livecontentui.section.adapter

import androidx.lifecycle.LifecycleOwner
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.livecontentui.OnPresentationContextChangedListener
import se.infomaker.livecontentui.config.ContentPresentationConfig
import se.infomaker.livecontentui.config.LiveContentUIConfig
import se.infomaker.livecontentui.config.TemplateConfig
import se.infomaker.livecontentui.config.ThemeOverlayConfig

class SectionViewBehaviourFactory(
    private val resourceManager: ResourceManager,
    private val moduleIdentifier: String,
    private val lifecycleOwner: LifecycleOwner,
    private val onPresentationContextChangedListener: OnPresentationContextChangedListener
) {

    fun create(config: LiveContentUIConfig): SectionViewBehaviour {
        return create(config.contentPresentation, config.templates, config.themeOverlayMapping)
    }

    fun create(presentationConfig: ContentPresentationConfig?, templates: Map<String, TemplateConfig>, overlayConfig: ThemeOverlayConfig?): SectionViewBehaviour {
        return presentationConfig?.let {
            SectionContentPresentationBehaviour(resourceManager, it, moduleIdentifier, lifecycleOwner, onPresentationContextChangedListener)
        } ?: run {
            SectionTemplatesViewBehaviour(overlayConfig, templates)
        }
    }
}