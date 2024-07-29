package se.infomaker.livecontentmanager.query;

import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple QueryFilter helper
 */
public class FilterHelper {

    private static final String QUERY_FILTERS = "queryFilters";

    /**
     * Puts a list of filters in intent
     * @param intent to put filters in
     * @param filterLists filters to insert. It is safe to call this method with null parameter
     */
    public static void put(Intent intent, List<QueryFilter> filterLists) {
        if (filterLists == null) {
            return;
        }
        ArrayList<QueryFilter> filters = new ArrayList<>(filterLists);
        intent.putExtra(QUERY_FILTERS, filters);
    }

    /**
     * Puts a list of filters in a bunde
     * @param bundle
     * @param filterLists
     */
    public static void put(Bundle bundle, List<QueryFilter> filterLists) {
        if (filterLists == null) {
            return;
        }
        ArrayList<QueryFilter> filters = new ArrayList<>(filterLists);
        bundle.putSerializable(QUERY_FILTERS, filters);
    }

    /**
     * Extracts filters from intent
     * @param intent to extract filters from
     * @return a list of extracted filters or null if non exist
     */
    public static List<QueryFilter> getFilters(Intent intent) {
        return getFilters(intent.getExtras());
    }

    /**
     * Get filters from bundle
     * @param bundle
     * @return
     */
    public static List<QueryFilter> getFilters(Bundle bundle) {
        if (bundle != null) {
            Serializable value = bundle.getSerializable(QUERY_FILTERS);
            if (value instanceof ArrayList<?>) {
                return  (ArrayList<QueryFilter>) value;
            }
        }
        return null;
    }
}
