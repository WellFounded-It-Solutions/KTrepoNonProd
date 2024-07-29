package se.infomaker.livecontentmanager.query;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import retrofit2.Response;
import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentService;
import timber.log.Timber;

public class ParameterSearchQuery implements Query {

    private static final String Q_KEY = "q";
    private final List<QueryFilter> filters;
    private JSONObject request;

    public ParameterSearchQuery(String contentProvider, String properties, Map<String, String> parameters, List<QueryFilter> filters) {
        this.filters = filters;

        try {
            JSONObject query = new JSONObject(parameters);
            query.put("properties", properties);
            request = JSONUtil.wrap("payload.data.query", query);
            JSONObject payload = request.getJSONObject("payload");
            payload.put("action", "search");
            payload.put("auth", new JSONObject());
            payload.put("contentprovider", contentProvider);
            payload.put("version", 1);

        } catch (JSONException e) {

            Timber.e(e, "Failed to create request");
        }
    }

    public Single<Response<JsonObject>> using(OpenContentService openContent) {
        try {
            JSONObject object = JSONUtil.getJSONObject(toJSONObject(), PAYLOAD_DATA_QUERY_KEY_PATH);
            return openContent.search(JSONUtil.toMap(object));
        } catch (JSONException e) {
            return Single.error(e);
        }
    }

    @Override
    public boolean finishedOnResponse() {
        return true;
    }

    @Override
    public JSONObject toJSONObject() {
        try {
            return applyFilters(new JSONObject(request.toString()));
        } catch (JSONException e) {
            Timber.e(e, "Failed to make request copy");
        }
        return applyFilters(request);
    }

    private JSONObject applyFilters(JSONObject request) {
        if (filters != null)
        {
            try {
                JSONObject queryObject = JSONUtil.getJSONObject(request, PAYLOAD_DATA_QUERY_KEY_PATH);
                String q = queryObject.getString(Q_KEY);
                for (QueryFilter filter : filters)
                {
                    q = filter.createSearchQuery(q);
                }
                queryObject.put(Q_KEY, q);

            } catch (JSONException e) {
                Timber.e(e, "Could not apply search filters");
            }
        }
        return request;
    }
}
