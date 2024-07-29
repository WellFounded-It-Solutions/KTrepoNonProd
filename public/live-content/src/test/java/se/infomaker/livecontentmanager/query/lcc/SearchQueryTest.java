package se.infomaker.livecontentmanager.query.lcc;

import androidx.annotation.NonNull;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;

import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.livecontentmanager.config.SearchConfig;
import se.infomaker.livecontentmanager.query.QueryFilter;
import se.infomaker.livecontentmanager.query.SearchQuery;

public class SearchQueryTest {

    @Test
    public void testApplyFilters() throws JSONException {
        ArrayList<QueryFilter> filters = createQueryFilters(":test");
        SearchQuery searchQuery = new SearchQuery(createSearchConfig(), "", 0 , 5, filters);
        JSONObject object = searchQuery.toJSONObject();
        String query = JSONUtil.getString(object, "payload.data.query.q");
        Assert.assertTrue(query.endsWith(":test"));
    }

    @NonNull
    private ArrayList<QueryFilter> createQueryFilters(final String identifier) {
        QueryFilter filter = new QueryFilter() {
            @NonNull
            @Override
            public String identifier() {
                return identifier;
            }

            @NonNull
            @Override
            public JSONObject createStreamFilter() {
                return new JSONObject();
            }

            @NonNull
            @Override
            public String createSearchQuery(@NonNull String baseQuery) {
                return baseQuery + identifier;
            }
        };
        ArrayList<QueryFilter> filters = new ArrayList<>();
        filters.add(filter);
        return filters;
    }

    @NonNull
    private SearchConfig createSearchConfig() {
        SearchConfig searchConfig = new SearchConfig("this is the base query", null, null);
        searchConfig.setSortIndex("blue");
        searchConfig.setContentProvider("hallpressen");
        return searchConfig;
    }
}
