package se.infomaker.livecontentui.section;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.Theme;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.livecontentrecyclerview.binder.PropertyBinder;
import se.infomaker.livecontentui.livecontentrecyclerview.view.LiveBinding;
import se.infomaker.livecontentui.section.adapter.SectionItemViewHolder;

public abstract class PropertyObjectSectionItem implements SectionItem, ContentPresentationAware {
    private final PropertyObject propertyObject;
    private final String template;
    private final String groupKey;
    private final String sectionIdentifier;
    private final String templateReference;
    private List<String> overlayThemes;
    private JSONObject context;

    public PropertyObjectSectionItem(PropertyObject propertyObject, String sectionIdentifier, String groupKey) {
        this(propertyObject, sectionIdentifier, groupKey, null, null, null);
    }

    public PropertyObjectSectionItem(PropertyObject propertyObject, String sectionIdentifier, String groupKey, String template, String templateReference, JSONObject context) {
        this.sectionIdentifier = sectionIdentifier;
        this.propertyObject = propertyObject;
        this.groupKey = groupKey;
        this.template = template;
        this.templateReference = templateReference;
        this.context = context;
    }

    @Override
    public String getId() {
        return propertyObject.getId();
    }

    @Override
    public String sectionIdentifier() {
        return sectionIdentifier;
    }

    @Override
    public Set<LiveBinding> bind(PropertyBinder binder, SectionItemViewHolder viewHolder, ResourceManager resourceManager, Theme theme) {
        viewHolder.setItem(this);
        Set<LiveBinding> bind = binder.bind(propertyObject, viewHolder.getViewsArrayList(), context);
        theme.apply(viewHolder.itemView);
        return bind;
    }

    @Override
    public boolean isItemTheSame(SectionItem sectionItem) {
        if (sectionItem instanceof PropertyObjectSectionItem) {
            return propertyObject.getId().equals(((PropertyObjectSectionItem) sectionItem).propertyObject.getId())
                    && sectionIdentifier.equals(((PropertyObjectSectionItem) sectionItem).sectionIdentifier);
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(SectionItem sectionItem) {
        if (sectionItem instanceof PropertyObjectSectionItem) {
            return (propertyObject.areContentsTheSame(((PropertyObjectSectionItem) sectionItem).propertyObject)) &&
                    (overlayThemes == null || overlayThemes.equals(((PropertyObjectSectionItem) sectionItem).overlayThemes));
        }
        return false;
    }

    @Override
    public void onDetach(SectionItemViewHolder viewHolder) {

    }

    @Override
    public String template() {
        return template;
    }

    @Override
    public String groupKey() {
        return groupKey;
    }

    public PropertyObject getPropertyObject() {
        return propertyObject;
    }

    @Override
    public String templateReference() {
        return templateReference;
    }

    @Nullable
    @Override
    public List<String> overlayThemes() {
        return overlayThemes;
    }

    public void setOverlayThemes(List<String> overlayThemes) {
        this.overlayThemes = overlayThemes;
    }

    @Nullable
    @Override
    public JSONObject getContext() {
        return context;
    }

    public void setContext(JSONObject context) {
        this.context = context;
    }

    @Nullable
    @Override
    public JSONObject getContent() {
        return propertyObject.getProperties();
    }
}
