package se.infomaker.livecontentui.section.ads;

import androidx.annotation.Nullable;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import se.infomaker.livecontentui.section.Section;
import se.infomaker.livecontentui.section.SectionItem;
import se.infomaker.livecontentui.section.SectionState;
import se.infomaker.livecontentui.section.configuration.AdsConfiguration;

public class AdsSectionWrapper implements Section {

    public static final String ARTICLE_KEY = "article";
    public static final String RELATED_KEY = "related";
    private static final Object POKE = new Object();
    private final Section section;

    private final CompositeDisposable resumedDisposable = new CompositeDisposable();
    private final BehaviorRelay<SectionState> stateRelay = BehaviorRelay.createDefault(SectionState.IDLE);
    private final BehaviorRelay<List<SectionItem>> itemRelay = BehaviorRelay.createDefault(new ArrayList<>());
    private final BehaviorRelay<Object> streamPoke = BehaviorRelay.createDefault(POKE);
    private final AdMapping adMapping;
    private List<SectionItem> currentSectionItems;

    public AdsSectionWrapper(AdsConfiguration configuration, Section section, String moduleTitle) {
        this.section = section;
        adMapping = new AdMapping(configuration, moduleTitle, () -> streamPoke.accept(POKE));
    }

    @Override
    public void reload() {
        reset();
        section.reload();
    }

    private void reset() {
        currentSectionItems = null;
        adMapping.reset();
    }

    @Override
    public void resume() {
        resumedDisposable.add(section.observeItems().subscribe(this::updateItems));
        resumedDisposable.add(section.observeState().subscribe(this::sectionStateChanged));
        section.resume();
    }

    private void sectionStateChanged(SectionState sectionState) {
        if (sectionState != SectionState.READY) {
            if (sectionState == SectionState.RELOADING && currentSectionItems == null) {
                stateRelay.accept(SectionState.LOADING);
            }
            else {
                stateRelay.accept(sectionState);
            }
        }
    }

    private void updateItems(List<SectionItem> sectionItems) {
        resumedDisposable.add(Observable.just(sectionItems)
                .map(adMapping::update)
                .subscribe(this::setCurrentSectionItems));
    }

    private void setCurrentSectionItems(List<SectionItem> currentSectionItems) {
        this.currentSectionItems = currentSectionItems;
        itemRelay.accept(currentSectionItems);
        stateRelay.accept(section.state());
    }

    @Override
    public void pause() {
        resumedDisposable.clear();
        section.pause();
    }

    @Override
    public int size() {
        return currentSectionItems != null ? currentSectionItems.size() : 0;
    }

    @Override
    public Observable<SectionState> observeState() {
        return Observable.combineLatest(stateRelay, section.observeState(), (sectionState, sectionState2) -> {
            if (sectionState.ordinal() < sectionState2.ordinal()) {
                return sectionState;
            }
            return sectionState2;
        });
    }

    @Override
    public Observable<List<SectionItem>> observeItems() {
        return Observable.combineLatest(itemRelay, streamPoke, (sectionItems, o) -> sectionItems);
    }

    @Override
    public SectionItem item(int index) {
        return currentSectionItems.get(index);
    }

    @Override
    public List<SectionItem> items() {
        return currentSectionItems != null ? currentSectionItems : Collections.emptyList();
    }

    @Override
    public Set<String> groupKeys() {
        HashSet<String> keys = new HashSet<>(section.groupKeys());
        keys.add(AdSectionItem.AD_GROUP_KEY);
        return keys;
    }

    @Nullable
    @Override
    public Date lastUpdated() {
        return section.lastUpdated();
    }

    @Override
    public SectionState state() {
        if (stateRelay.getValue().ordinal() < section.state().ordinal()) {
            return stateRelay.getValue();
        }
        return section.state();
    }
}
