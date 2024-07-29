package se.infomaker.livecontentmanager.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.livecontentmanager.config.StreamConfig;
import se.infomaker.livecontentmanager.query.lca.JsonObjectPayload;
import timber.log.Timber;

public class CreateStreamQuery implements Query {

    private static final String FILTER_KEY = "filter";
    private static final String DATA_QUERY_BOOL_KEY_PATH = "payload.data.query.bool";
    private static final String DATA_QUERY_QUERY_STRING_KEY_PATH = "payload.data.query.query_string";

    private transient final List<QueryFilter> filters;
    private JsonObjectPayload payload;
    private boolean finishedOnResponse = false;
    private final StreamDestination destination;

    public CreateStreamQuery(StreamConfig config, List<QueryFilter> filters) {
        this(config, filters, null);
    }

    public CreateStreamQuery(StreamConfig config, List<QueryFilter> filters, StreamDestination destination)
    {
        this.filters = filters;
        JsonObject data = new JsonObject();
        JsonObject query = new JsonObject();
        JsonObject configuredQuery = config.getBaseQuery();
        if (configuredQuery.keySet().size() == 1 && configuredQuery.has("query_string")) {
            query = configuredQuery;
        }
        else {
            query.add("bool", config.getBaseQuery());
        }
        data.add("query", query);
        this.destination = destination;
        if (destination != null) {
            finishedOnResponse = true;
            data.add("destination", new Gson().toJsonTree(destination));
        }
        payload = new JsonObjectPayload("streamCreate", config.getContentProvider(), data);
    }

    @Override
    public boolean finishedOnResponse() {
        return finishedOnResponse;
    }

    @Override
    public JSONObject toJSONObject() {
        try {
            return applyFilters(new JSONObject(new Gson().toJson(this)));
        } catch (JSONException e) {
            throw new RuntimeException("Could not convert json to json!", e);
        }
    }

    private JSONObject applyFilters(JSONObject query) {
        try {
            JSONObject queryStringObject = JSONUtil.optJSONObject(query, DATA_QUERY_QUERY_STRING_KEY_PATH);
            if (queryStringObject != null) {
                try {
                    String queryStringQuery = queryStringObject.getString("query");
                    if (filters != null) {
                        for (QueryFilter filter : filters) {
                            queryStringQuery = filter.createSearchQuery(queryStringQuery);
                        }
                    }
                    queryStringObject.put("query", queryStringQuery);
                }
                catch (JSONException e) {
                    Timber.e("Could not apply query_string filters.");
                }
                return query;
            }

            JSONObject bool = JSONUtil.getJSONObject(query, DATA_QUERY_BOOL_KEY_PATH);
            JSONArray searchFilters = bool.optJSONArray(FILTER_KEY);
            if (searchFilters == null)
            {
                searchFilters = new JSONArray();
                bool.put(FILTER_KEY, searchFilters);
            }
            if (filters != null)
            {
                for (QueryFilter filter : filters)
                {
                    searchFilters.put(filter.createStreamFilter());
                }
            }

        } catch (JSONException e) {
            Timber.e(e, "Failed to apply filters");
        }

        return query;
    }

    @Override
    public String toString() {
        try {
            return "CreateStreamQuery{" +
                    "data=" + toJSONObject().toString(2) +
                    '}';
        } catch (JSONException e) {
            return "CreateStreamQuery{" +
                    "data=" + toJSONObject().toString() +
                    '}';
        }
    }

    public JsonObject query() throws JSONException {
        return new JsonParser().parse(JSONUtil
                .getJSONObject(toJSONObject(), "payload.data.query").toString()).getAsJsonObject();
    }

    public boolean hasDestination() {
        return destination != null;
    }

    public StreamDestination getDestination() {
        return destination;
    }
}
