package se.infomaker.livecontentui.section.datasource;

import androidx.annotation.Nullable;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import se.infomaker.frtutilities.connectivity.Connectivity;
import se.infomaker.livecontentui.section.Section;
import se.infomaker.livecontentui.section.SectionItem;
import se.infomaker.livecontentui.section.SectionState;
import timber.log.Timber;

public class DataSourceSection implements Section {

    private final BehaviorRelay<SectionState> stateRelay = BehaviorRelay.createDefault(SectionState.IDLE);
    private final BehaviorRelay<List<SectionItem>> itemRelay = BehaviorRelay.create();
    private final List<SectionItem> sectionItems = new ArrayList<>();
    private final DataSource dataProvider;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private Date lastUpdated = null;
    private boolean hasResult = false;

    public DataSourceSection(DataSource dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void reload() {
        stateRelay.accept(size() > 0 ? SectionState.RELOADING : SectionState.LOADING);
        dataProvider.update();
    }

    @Override
    public void resume() {
        disposables.clear();
        disposables.add(dataProvider.observeResponse().subscribe(response -> {
            sectionItems.clear();
            SectionState state;
            if (response.error != null) {
                state = SectionState.ERROR;
                hasResult = false;
            } else {
                state = SectionState.READY;
                hasResult = true;
                sectionItems.addAll(response.items);
                lastUpdated = response.lastUpdated;
            }
            stateRelay.accept(state);
            itemRelay.accept(sectionItems);
        }, throwable -> Timber.e(throwable, "Failed to resume")));

        disposables.add(Connectivity.observable()
                .distinctUntilChanged()
                .skip(1)
                .subscribe(connected -> {
                    if (connected) {
                        dataProvider.update();
                    }
                }, Timber::e));

        if (dataProvider.resume()) {
            stateRelay.accept(hasResult ? SectionState.RELOADING : SectionState.LOADING);
        }
    }

    @Override
    public void pause() {
        dataProvider.pause();
        disposables.clear();
    }

    @Override
    public int size() {
        return sectionItems.size();
    }

    @Override
    public Observable<SectionState> observeState() {
        return stateRelay;
    }

    @Override
    public Observable<List<SectionItem>> observeItems() {
        return itemRelay;
    }

    @Override
    public SectionItem item(int index) {
        return sectionItems.get(index);
    }

    @Override
    public List<SectionItem> items() {
        return sectionItems;
    }

    @Override
    public Set<String> groupKeys() {
        return dataProvider.groupKeys();
    }

    @Nullable
    @Override
    public Date lastUpdated() {
        return lastUpdated;
    }

    @Override
    public SectionState state() {
        return stateRelay.getValue();
    }
}
