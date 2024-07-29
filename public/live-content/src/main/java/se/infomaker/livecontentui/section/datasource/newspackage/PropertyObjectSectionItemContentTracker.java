package se.infomaker.livecontentui.section.datasource.newspackage;

import org.jetbrains.annotations.NotNull;

import se.infomaker.livecontentui.StatsHelper;
import se.infomaker.livecontentui.impressions.ContentTracker;
import se.infomaker.livecontentui.section.PropertyObjectSectionItem;

public class PropertyObjectSectionItemContentTracker implements ContentTracker {
    private PropertyObjectSectionItem item;
    private String moduleId;

    public PropertyObjectSectionItemContentTracker(PropertyObjectSectionItem propertyObject) {
        this.item = propertyObject;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public void register() {
        StatsHelper.logArticleListShowStatsEvent(item.getPropertyObject(), moduleId);
    }

    @Override
    public boolean isSameAs(@NotNull ContentTracker other) {
        if (other instanceof PropertyObjectSectionItemContentTracker) {
            return item.isItemTheSame(((PropertyObjectSectionItemContentTracker) other).item);
        }
        return false;
    }
}
