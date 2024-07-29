package se.infomaker.iap.ui.util;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class UI {
    /**
     * Extract all views with id from view and put in map with id string as key
     * @param view to extract views from
     * @return map of views in view (including the view)
     */
    public static Map<String,View> extractViews(View view) {
        HashMap<String, View> viewMap = new HashMap<>();
        extractViews(viewMap, view);
        return viewMap;
    }

    private static void extractViews(Map<String, View> target, View view) {
        try {
            if (view.getId() != View.NO_ID) {
                String name = view.getResources().getResourceEntryName(view.getId());
                if (name != null) {
                    target.put(name, view);
                }
            }
        }
        catch (Resources.NotFoundException e) {
            Timber.e(e, "Could not find view");
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                extractViews(target, group.getChildAt(i));
            }
        }
    }
}
