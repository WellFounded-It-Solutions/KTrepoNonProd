package se.infomaker.livecontentmanager.query.lcc;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.livecontentmanager.query.CreateStreamQuery;
import se.infomaker.livecontentmanager.query.DeleteStreamQuery;
import se.infomaker.livecontentmanager.query.ParameterSearchQuery;
import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryManager;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import se.infomaker.livecontentmanager.query.SearchQuery;
import se.infomaker.livecontentmanager.query.UpdateStreamQuery;
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentService;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerManager;
import timber.log.Timber;

@Singleton
public class LCCQueryManager implements QueryManager {

    private final OpenContentService openContent;
    private final QueryStreamerManager queryStreamer;

    @Inject
    public LCCQueryManager(OpenContentService openContent, QueryStreamerManager queryStreamerManager) {
        this.openContent = openContent;
        this.queryStreamer = queryStreamerManager;
    }

    @Override
    public void addQuery(Query query, QueryResponseListener listener) {
        if (query instanceof ParameterSearchQuery) {
            ParameterSearchQuery parameterSearchQuery = (ParameterSearchQuery) query;
            Single.just(openContent).observeOn(Schedulers.io()).flatMap(parameterSearchQuery::using)
                    .map(result -> {
                        JSONObject response = JSONUtil.wrap("payload.data.result", new JSONObject(result.body().toString()));
                        response.getJSONObject("payload").put("action", "lcc");
                        response.getJSONObject("payload").put("lastupdated", result.headers().get("date"));
                        return response;
                    })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe((JSONObject result) -> listener.onResponse(query, result), throwable -> {
                          Timber.d(throwable, "Failed query");
                        listener.onError(throwable);
                    });
        }
        else if (query instanceof SearchQuery) {
            SearchQuery searchQuery = (SearchQuery) query;
            Single.just(openContent).observeOn(Schedulers.io()).flatMap(searchQuery::using)
                    .map(result -> {
                        JSONObject response = JSONUtil.wrap("payload.data.result", new JSONObject(result.body().toString()));
                        response.getJSONObject("payload").put("action", "lcc");
                        response.getJSONObject("payload").put("lastupdated", result.headers().get("date"));
                        return response;
                    })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe((JSONObject result) -> listener.onResponse(query, result), listener::onError);
        }
        else if (query instanceof CreateStreamQuery) {
            try {
                queryStreamer.create((CreateStreamQuery) query, listener);
            } catch (JSONException e) {
                Timber.e(e, "invalid query " + query);
            }
        }
        else if (query instanceof DeleteStreamQuery) {
            queryStreamer.delete((DeleteStreamQuery) query, listener);
        }
        else if (query instanceof UpdateStreamQuery) {
            queryStreamer.update((UpdateStreamQuery) query, listener);
        }
        else {
            Timber.e("Unsupported query: " + query);
        }
    }

    @Override
    public boolean removeQuery(Query query) {
        if (query instanceof CreateStreamQuery ||
                query instanceof DeleteStreamQuery ||
                query instanceof UpdateStreamQuery
                ) {
            return queryStreamer.remove(query);
        }
        return false;
    }
}
