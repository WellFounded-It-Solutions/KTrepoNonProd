package se.infomaker.livecontentui.section.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.combineLatest
import io.reactivex.schedulers.Schedulers
import se.infomaker.livecontentui.extensions.allItems
import se.infomaker.livecontentui.section.Section
import se.infomaker.livecontentui.section.SectionItem
import se.infomaker.livecontentui.section.SectionState
import se.infomaker.livecontentui.section.adapter.SectionViewBehaviour
import se.infomaker.livecontentui.section.ktx.hasExternalLink
import timber.log.Timber
import java.util.Date

class SectionDetailViewModel(private val sections: List<Section>, private val viewBehaviour: SectionViewBehaviour, private val groupKey: String) : ViewModel() {

    private val garbage = CompositeDisposable()
    private val groupPredicate: ((SectionItem) -> Boolean) = { groupKey == it.groupKey() }
    private val minStateOperation: ((SectionState, SectionState) -> SectionState) = { first, second -> if (first.ordinal < second.ordinal) first else second }
    private val _viewState by lazy { MutableLiveData(SectionDetailState(sections.minState, sections.validItems))}

    val viewState: LiveData<SectionDetailState>
        get() = _viewState

    val lastUpdated: Date?
        get() = sections.mapNotNull { it.lastUpdated() }
                .maxByOrNull { it.time }

    init {
        Observable.fromIterable(sections)
                .map { it.observeState() }
                .toList().blockingGet()
                .combineLatest { it.min }
                .distinctUntilChanged()
                .subscribe({ updateState() }, { Timber.e(it, "Failed when observing section states.") })
                .addTo(garbage)

        Observable.fromIterable(sections)
                .subscribeOn(Schedulers.computation())
                .map { it.observeItems() }
                .toList().blockingGet()
                .combineLatest { it.valid }
                .filter { it.isUpdated() }
                .subscribe({ updateState() }, { Timber.e(it, "Failed when observing section items.") })
                .addTo(garbage)
    }

    private fun updateState() {
        _viewState.postValue(SectionDetailState(sections.minState, sections.validItems))
    }

    override fun onCleared() {
        super.onCleared()
        garbage.clear()
    }

    private fun List<SectionItem>.withViewBehaviour(): List<SectionItem> {
        viewBehaviour.update(this)
        return this
    }

    private fun List<SectionItem>.isUpdated(): Boolean {
        _viewState.value?.items?.let {
            if (size == it.size) {
                it.forEachIndexed { index, current ->
                    val new = get(index)
                    if (!current.areContentsTheSame(new)) {
                        return true
                    }
                }
                return false
            }
        }
        return true
    }

    private val List<Section>.minState: SectionState
        get() = map { it.state() }.min

    private val List<SectionState>.min: SectionState
        get() = reduceOrNull(minStateOperation) ?: SectionState.IDLE

    private val List<Section>.validItems: List<SectionItem>
        get() = flatMap { it.items().allItems.filter(groupPredicate).filterNot { item -> item.hasExternalLink } }.withViewBehaviour()

    private val List<List<SectionItem>>.valid: List<SectionItem>
        get() = flatten().allItems.filter(groupPredicate).filterNot { item -> item.hasExternalLink }.withViewBehaviour()
}