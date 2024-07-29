package se.infomaker.livecontentmanager.query;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;

import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.livecontentmanager.config.StreamConfig;

public class StreamQueryTest {

    @Test
    public void testApplyFilters() throws JSONException {
        JSONObject filterOutput = new JSONObject();
        filterOutput.put("magic", "cat");
        filterOutput.put("brown", "dog");
        ArrayList<QueryFilter> filters = createQueryFilters(":test", filterOutput);
        CreateStreamQuery streamQuery = new CreateStreamQuery(createStreamConfig(), filters);
        JSONObject object = streamQuery.toJSONObject();
        JSONObject query = JSONUtil.getJSONObject(object, "payload.data.query.bool");
        JSONArray generatedFilters = query.getJSONArray("filter");
        Assert.assertEquals(1, generatedFilters.length());
        JSONAssert.assertEquals(filterOutput, generatedFilters.getJSONObject(0), true);
    }

    @NonNull
    private ArrayList<QueryFilter> createQueryFilters(final String identifier, final JSONObject filterOutput) {
        QueryFilter filter = new QueryFilter() {
            @NonNull
            @Override
            public String identifier() {
                return identifier;
            }

            @NonNull
            @Override
            public JSONObject createStreamFilter() {
                return filterOutput;
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
    private StreamConfig createStreamConfig() {
        return new StreamConfig("hallpressen", (JsonObject) new JsonParser().parse("{}"));
    }
}
