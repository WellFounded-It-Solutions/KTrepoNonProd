package se.infomaker.livecontentui.section.detail

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.livecontentui.OnPresentationContextChangedListener
import se.infomaker.livecontentui.section.Section
import se.infomaker.livecontentui.section.adapter.SectionViewBehaviourFactory
import se.infomaker.livecontentui.section.configuration.SectionedLiveContentUIConfig

class SectionDetailViewModelFactory(
    private val resourceManager: ResourceManager,
    private val moduleId: String,
    private val sections: List<Section>,
    private val groupKey: String,
    private val config: SectionedLiveContentUIConfig,
    private val lifecycleOwner: LifecycleOwner,
    private val onPresentationContextChangedListener: OnPresentationContextChangedListener
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewBehaviour = SectionViewBehaviourFactory(resourceManager, moduleId, lifecycleOwner, onPresentationContextChangedListener).create(config)
        return SectionDetailViewModel(sections, viewBehaviour, groupKey) as T
    }
}