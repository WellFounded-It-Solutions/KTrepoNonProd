package se.infomaker.livecontentui.livecontentrecyclerview.adapter

import androidx.lifecycle.LifecycleOwner
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.livecontentmanager.parser.PropertyObject
import se.infomaker.livecontentui.OnPresentationContextChangedListener
import se.infomaker.livecontentui.ViewBehaviour
import se.infomaker.livecontentui.config.LiveContentUIConfig

class ViewBehaviourFactory(
    private val resourceManager: ResourceManager,
    private val moduleIdentifier: String,
    private val lifecycleOwner: LifecycleOwner,
    private val onPresentationContextChangedListener: OnPresentationContextChangedListener
) {

    fun create(config: LiveContentUIConfig): ViewBehaviour<PropertyObject> {
        val behaviour = config.contentPresentation?.let {
            ContentPresentationBehaviour(resourceManager, it, moduleIdentifier, lifecycleOwner, onPresentationContextChangedListener)
        } ?: run {
            TemplatesBehaviour(resourceManager, config.templates, config.themeOverlayMapping)
        }

        return behaviour
    }
}