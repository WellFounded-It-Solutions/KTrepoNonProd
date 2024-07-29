package se.infomaker.livecontentui.livecontentrecyclerview.adapter;

import android.os.Bundle;

import org.jetbrains.annotations.Nullable;

import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentui.StatsHelper;
import se.infomaker.livecontentui.impressions.ContentTracker;

class PropertyObjectContentTracker implements ContentTracker {
    private final PropertyObject propertyObject;
    private final String moduleId;
    private final Bundle statsExtras;

    public PropertyObjectContentTracker(PropertyObject object, String moduleId) {
        this(object, moduleId, null);
    }

    public PropertyObjectContentTracker(PropertyObject object, String moduleId, Bundle statsExtras) {
        this.propertyObject = object;
        this.moduleId = moduleId;
        this.statsExtras = statsExtras;
    }

    @Override
    public void register() {
        StatsHelper.logArticleListShowStatsEvent(propertyObject, moduleId, statsExtras);
    }

    @Override
    public boolean isSameAs(@Nullable ContentTracker other) {
        if (other instanceof PropertyObjectContentTracker) {
            return propertyObject.getId().equals(((PropertyObjectContentTracker) other).propertyObject.getId());
        }
        return false;
    }
}
