package se.infomaker.livecontentui.section.datasource.newspackage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.articleview.item.author.DividerDecorationConfig;
import se.infomaker.iap.theme.Theme;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import com.navigaglobal.mobile.livecontent.R;
import se.infomaker.livecontentui.impressions.ContentTracker;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;
import se.infomaker.livecontentui.section.SectionItem;
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder;

public class ErrorSectionItem implements SectionItem {

    public static final String NO_GROUP = "no group";
    private final Throwable error;
    private final PropertyObject propertyObject;

    public ErrorSectionItem(Throwable error) {
        this.error = error;
        propertyObject = new PropertyObject(new JSONObject(), UUID.randomUUID().toString());
        if (error == null) {
            propertyObject.putString("teaserHeadline", "Unknown error");
        }
        else {
            safePut(propertyObject,"teaserHeadline", error.getLocalizedMessage());
            safePut(propertyObject, "localizedMessage", error.getLocalizedMessage());
            safePut(propertyObject, "message", error.getMessage());
        }
    }

    private void safePut(PropertyObject target, String key, String value) {
        if (target != null && key != null && value != null) {
            target.putString(key, value);
        }
    }

    @Override
    public String getId() {
        return propertyObject.getId();
    }

    @Override
    public String sectionIdentifier() {
        return "error";
    }

    @Override
    public Set<LiveBinding> bind(PropertyBinder binder, SectionItemViewHolder viewHolder, ResourceManager resourceManager, Theme theme) {
        viewHolder.setItem(this);
        return binder.bind(propertyObject, viewHolder.getViewsArrayList(), null);
    }

    @Override
    public boolean isItemTheSame(SectionItem sectionItem) {
        if (sectionItem instanceof ErrorSectionItem) {
            return error.getLocalizedMessage().equals(((ErrorSectionItem) sectionItem).error.getLocalizedMessage());
        }
        return this == sectionItem;
    }

    @Override
    public boolean areContentsTheSame(SectionItem sectionItem) {
        if (sectionItem instanceof ErrorSectionItem) {
            return error.getLocalizedMessage().equals(((ErrorSectionItem) sectionItem).error.getLocalizedMessage());
        }
        return false;
    }

    @Override
    public void onDetach(SectionItemViewHolder viewHolder) {

    }

    @Override
    public String template() {
        return "section_error_item";
    }

    @Override
    public String templateReference() {
        return template();
    }

    @Override
    public int defaultTemplate() {
        return R.layout.error_default;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public String groupKey() {
        return NO_GROUP;
    }

    @Nullable
    @NonNull
    @Override
    public List<String> overlayThemes() {
        return null;
    }

    @Override
    public DividerDecorationConfig getDividerConfig() {
        return NO_DIVIDER_CONFIG;
    }

    @Override
    public Fragment createDetailView(String moduleId) {
        throw new RuntimeException("Errors should not produce detail views");
    }

    @Override
    public ContentTracker getContentTracker(String moduleId) {
        return null;
    }
}
