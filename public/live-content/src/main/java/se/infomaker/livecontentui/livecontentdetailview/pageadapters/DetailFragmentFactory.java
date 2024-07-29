package se.infomaker.livecontentui.livecontentdetailview.pageadapters;

import android.content.Context;
import androidx.fragment.app.Fragment;

import java.util.List;

import se.infomaker.livecontentmanager.parser.PropertyObject;


public interface DetailFragmentFactory {
    /**
     * Creates a detail fragment for the provided propertyObject
     * @param context
     * @param moduleId
     * @param propertyObject
     * @return detail fragment
     */
    Fragment createFragment(Context context, String moduleId, PropertyObject propertyObject, List<String> overlayThemes);
}
