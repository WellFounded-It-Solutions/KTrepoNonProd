package se.infomaker.livecontentui.section;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Set;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.articleview.item.author.DividerDecorationConfig;
import se.infomaker.iap.theme.Theme;
import se.infomaker.livecontentui.impressions.ContentTracker;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder;

public interface SectionItem {
    DividerDecorationConfig NO_DIVIDER_CONFIG = new DividerDecorationConfig(null, null, "", false, false);

    /**
     *
     * @return Consistent unique id for this item
     */
    String getId();

    /**
     *
     * @return Consistent unique identifier for the items section
     */
    String sectionIdentifier();

    /**
     * Bind the section item to provided view holder
     *
     * @param binder
     * @param viewHolder
     */
    @Nullable
    Set<LiveBinding> bind(@NonNull PropertyBinder binder, @NonNull SectionItemViewHolder viewHolder, @NonNull ResourceManager resourceManager, @NonNull Theme theme);

    /**
     * Used to check if an item is the same (not equals) to another item.
     * Example if an item has been updated it is still the same as an old item
     *
     * @param sectionItem to compare to
     * @return whether passed in item is the same as this
     */
    boolean isItemTheSame(SectionItem sectionItem);

    /**
     * Check if the content of the item is the same as another item
     * @param sectionItem
     * @return whether contents of the passed in item is the same as this content
     */
    boolean areContentsTheSame(SectionItem sectionItem);

    /**
     * Called when the view holder is reused to another item
     *
     * @param viewHolder
     */
    void onDetach(SectionItemViewHolder viewHolder);

    /**
     *
     * @return the template the item will use as a view
     */
    String template();

    /**
     *
     * @return the templateReference the item will use as a view
     */
    String templateReference();

    /**
     * A default template to use if none is found
     * @return layout reference to use as default
     */
    int defaultTemplate();

    /**
     *
     * @return true if the item is isClickable
     */
    boolean isClickable();

    /**
     * What group does this item belong in
     * @return group key
     */
    String groupKey();

    /**
     *
     * @return Overlay themes to use for this item
     */
    @Nullable
    List<String> overlayThemes();

    @NonNull
    DividerDecorationConfig getDividerConfig();

    /**
     * Create a detail view to display the content
     * @return detail fragment
     */
    Fragment createDetailView(String moduleId);

    /**
     * Invoked when the view is shown to the user
     * useful for statistics events
     */
    ContentTracker getContentTracker(String moduleId);
}
