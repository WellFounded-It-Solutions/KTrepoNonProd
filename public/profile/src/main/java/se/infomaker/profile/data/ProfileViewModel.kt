package se.infomaker.profile.data

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import se.infomaker.frtutilities.ConfigManager
import se.infomaker.frtutilities.GlobalValueManager
import se.infomaker.frtutilities.ktx.resources
import se.infomaker.iap.action.display.flow.condition.ConditionFactory
import se.infomaker.iap.action.display.flow.condition.view.shouldShowIf
import se.infomaker.iap.theme.ktx.theme
import se.infomaker.utilities.mustachio
import timber.log.Timber
import java.util.UUID


@FlowPreview
class ProfileViewModel(app: Application, val moduleIdentifier: String?) : AndroidViewModel(app) {

    val resources by getApplication<Application>().applicationContext.resources { moduleIdentifier }
    val theme by getApplication<Application>().applicationContext.theme { moduleIdentifier }
    var state by mutableStateOf(listOf<ProfileItem>())
        private set
    var indent by mutableStateOf(14.dp)

    private var items by mutableStateOf(mutableListOf<ProfileItem>())
    private val conditionFactory = ConditionFactory()
    private val globalValueManager =
        GlobalValueManager.getGlobalValueManager(getApplication<Application>().applicationContext)

    init {
        snapshotFlow { items }
            .onEach {
                state = it.filterNot { item -> item.invisible }
            }.map { list ->
                list.filterIsInstance<MutableVisibility>()
                    .map { it.showIf?.key?.let { keyPath -> observeString(keyPath, it) } }
            }.launchIn(viewModelScope)
    }

    private fun observeString(keyPath: String, item: MutableVisibility) = viewModelScope.launch {
        globalValueManager.observeString(keyPath.mustachio)
            ?.asFlow()
            ?.onEach {
                item.visibility.value = shouldShowIf(
                    globalValueManager,
                    conditionFactory.create(item.showIf as ShowIf)
                )
            }
            ?.collect {
                val section = items.filter { (item as? ProfileItem)?.sectionIdentifier == it.sectionIdentifier }
                removeHeaderAndFooterIfSectionIsEmpty(section)
                section.filterNot { item -> item.invisible }.also { updateSectionState(it) }
            }
    }

    // Handy for debugging
    /*private fun <T> T.logItem(): T {
        (this as? ProfileItem)?.let {
            print("name=${it.name}, ")
        }
        (this as? MutableVisibility)?.let {
            println("visibility=${it.visibility.value}")
        }
        return this
    }*/

    private fun removeHeaderAndFooterIfSectionIsEmpty(section: List<ProfileItem>) {

        val isSectionEmpty = section
            .filterNot { it is SectionFooterItem || it is SectionHeaderItem }
            .filterNot { it.invisible }
            .isEmpty()

        if (isSectionEmpty) {
            // Hide empty SectionHeader and SectionFooter items
            section.map { (it as? MutableVisibility)?.visibility?.value = false }
        } else {
            // Show SectionHeader and SectionFooter items
            section.filter { it is SectionFooterItem || it is SectionHeaderItem }
                .map { (it as? MutableVisibility)?.visibility?.value = true }
        }
    }

    private fun updateSectionState(sectionItems: List<ProfileItem>?): List<ProfileItem>? =
        sectionItems?.mapIndexed { index, profileItem ->
            profileItem.position.value = updateSectionPosition(index, sectionItems.size)
            profileItem
        }

    private fun buildProfileItemForConfigAsync(
        profileItemConfig: ProfileItemConfig,
        index: Int, sectionSize: Int, sectionIdentifier: String,
    ): Deferred<ProfileItem?> {
        val factory = ProfileItemFactory(theme, resources)
        return viewModelScope.async {
            factory.buildItemForConfig(profileItemConfig,
                updateSectionPosition(index, sectionSize),
                sectionIdentifier,
                moduleIdentifier)
        }
    }

    fun configUpdated(event: ProfileListEvent) {
        when (event) {
            is ProfileListEvent.UpdateConfig -> {
                viewModelScope.launch {
                    val updatedItems: List<ProfileItem> = try {
                        event.config.sections?.flatMap { section: Section ->
                            val sectionId = UUID.randomUUID().toString()
                            section.profileItemConfigs
                                ?.mapIndexed { index, profileItemConfig ->
                                    buildProfileItemForConfigAsync(profileItemConfig,
                                        index,
                                        section.profileItemConfigs.size,
                                        sectionId)
                                }
                                ?.awaitAll()
                                ?.filterNotNull()
                                ?.map {
                                    if (it.image != -1) {
                                        if(!ConfigManager
                                                .getInstance(getApplication<Application>()
                                                    .applicationContext)
                                                .getConfig("core", ProfileIndentWrapper::class.java)
                                                .skipIndentingProfileViews)
                                                        indent = 72.dp
                                        else
                                            indent = 24.dp
                                    }
                                    it
                                } ?: emptyList()
                        } ?: emptyList()
                    } catch (e: Exception) {
                        Timber.e(e)
                        emptyList()
                    }
                    items = updatedItems.toMutableList()
                }
            }
        }
    }

    private fun updateSectionPosition(index: Int, size: Int) = when {
        index == 0 && size == 1 -> SectionPosition.BOTH
        index == size - 1 -> SectionPosition.END
        index == 0 -> SectionPosition.START
        else -> SectionPosition.MIDDLE
    }
}
