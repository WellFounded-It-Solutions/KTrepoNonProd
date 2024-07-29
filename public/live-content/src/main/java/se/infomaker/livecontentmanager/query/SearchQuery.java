package se.infomaker.livecontentmanager.query;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Response;
import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.livecontentmanager.config.SearchConfig;
import se.infomaker.livecontentmanager.query.lca.ContentProvider;
import se.infomaker.livecontentmanager.query.lca.QueryBuilder;
import se.infomaker.livecontentmanager.query.lca.SearchData;
import se.infomaker.livecontentmanager.query.lca.SearchPayload;
import se.infomaker.livecontentmanager.query.lca.SearchRequest;
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentService;
import timber.log.Timber;

public class SearchQuery implements Query {
    private static final String ACTION_TYPE_SEARCH = "search";
    private static final String Q_KEY = "q";

    private final SearchConfig config;
    private final String properties;
    private final int limit;
    private transient final List<QueryFilter> filters;
    private final int offset;
    private final SearchRequest request;

    public SearchQuery(SearchConfig searchConfig, String properties, int offset, int pagingLimit, List<QueryFilter> filters) {
        se.infomaker.livecontentmanager.query.lca.SearchQuery query = new QueryBuilder()
                .setContentType(searchConfig.getContentType())
                .setLimit(pagingLimit)
                .setOffset(offset)
                .setProperties(properties)
                .setQuery(searchConfig.getBaseQuery())
                .setSort(searchConfig.getSort())
                .createQuery();

        SearchPayload payload = new SearchPayload(ACTION_TYPE_SEARCH, new ContentProvider(searchConfig.getContentProvider()), new SearchData(query));
        request = new SearchRequest(payload);
        this.config = searchConfig;
        this.offset = offset;
        this.limit = pagingLimit;
        this.properties = properties;
        this.filters = filters;
    }

    @Override
    public boolean finishedOnResponse() {
        return true;
    }

    @Override
    public JSONObject toJSONObject() {
        return applyFilters(request.toJSONObject());
    }

    private JSONObject applyFilters(JSONObject request) {
        if (filters != null) {
            try {
                JSONObject queryObject = JSONUtil.getJSONObject(request, PAYLOAD_DATA_QUERY_KEY_PATH);
                String q = queryObject.getString(Q_KEY);
                for (QueryFilter filter : filters) {
                    q = filter.createSearchQuery(q);
                }
                queryObject.put(Q_KEY, q);

            } catch (JSONException e) {
                Timber.e(e, "Could not apply search filters");
            }
        }
        return request;
    }

    public SearchQuery next() {
        return new SearchQuery(config, properties, offset + limit, limit, filters);
    }

    public SearchQuery createCatchupQuery() {
        return new SearchQuery(config, properties, 0, limit, filters);
    }

    public Single<Response<JsonObject>> using(OpenContentService openContent) {
        try {
            String q = JSONUtil.getString(toJSONObject(), PAYLOAD_DATA_QUERY_KEY_PATH + "." + Q_KEY);

            if (config.getFilters() != null) {
                return openContent.search(offset, limit, properties, config.getFilters(), q, config.getContentType(), config.getSort());
            }
            else {
                return openContent.search(offset, limit, properties, q, config.getContentType(), config.getSort());
            }
        } catch (JSONException e) {
            return Single.error(e);
        }
    }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "limit=" + limit +
                ", filters=" + filters +
                ", offset=" + offset +
                ", request=" + request +
                '}';
    }
}
