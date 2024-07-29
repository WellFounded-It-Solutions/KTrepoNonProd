package se.infomaker.livecontentui.section;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.Date;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;

/**
 * Section providing section items
 */
public interface Section extends LifecycleObserver {

    /**
     * Make the section reload its content
     */
    void reload();

    /**
     * Make sure the section has loaded data and keeping it live
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void resume();

    /**
     * Pause the section from being live
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void pause();

    /**
     * total number of items in list, this includes headers/footers
     *
     * @return total number of items
     */
    int size();

    /**
     *
     * @return Observable providing section state information
     */
    Observable<SectionState> observeState();

    /**
     *
     * @return Observable providing section items
     */
    Observable<List<SectionItem>> observeItems();

    /**
     * item for index
     *
     * @param index
     * @return item
     */
    SectionItem item(int index);

    /**
     * All section items in the list
     *
     * @return all items
     */
    List<SectionItem> items();

    /**
     *
     * @return All group keys items can belong to
     */
    Set<String> groupKeys();

    /**
     *
     * @return Date when this section was last successfully updated.
     */
    @Nullable
    Date lastUpdated();

    /**
     *
     * @return Current section state information of this section.
     */
    SectionState state();
}
