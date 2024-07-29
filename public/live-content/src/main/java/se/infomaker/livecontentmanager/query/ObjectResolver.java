package se.infomaker.livecontentmanager.query;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.infomaker.livecontentmanager.config.SearchConfig;
import se.infomaker.livecontentmanager.model.StreamEventWrapper;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.parser.PropertyObjectParser;

public class ObjectResolver {
    private final SearchConfig config;
    private final String properties;
    private final Scheduler scheduler;
    private PropertyObjectParser parser;
    private QueryManager queryManager;

    public ObjectResolver(SearchConfig config, String properties, PropertyObjectParser parser, QueryManager queryManager) {
        this(config,properties,parser,queryManager, null);
    }

    public ObjectResolver(SearchConfig config, String properties, PropertyObjectParser parser, QueryManager queryManager, Scheduler scheduler) {
        this.config = config;
        this.properties = properties;
        this.parser = parser;
        this.queryManager = queryManager;
        this.scheduler = scheduler != null ? scheduler : AndroidSchedulers.mainThread();
    }

    public Observable<PropertyObject> resolve(String uuid, String type) {
        MatchFilter filter = new MatchFilter("uuid", uuid);
        ArrayList<QueryFilter> filters = new ArrayList<>();
        filters.add(filter);
        SearchQuery query = new SearchQuery(config, properties, 0, 1, filters);
        ObservableResponseListener listener = new ObservableResponseListener(query, parser, type);
        return listener.getObservable();
    }

    public Single<List<PropertyObject>> fromEventWrapper(StreamEventWrapper event, String type) {
        return Observable.fromIterable(event.getObjects()).observeOn(Schedulers.io())
                .map(PropertyObject::getId).flatMap(uuid -> resolve(uuid, type)).subscribeOn(scheduler).toList();
    }

    private class ObservableResponseListener{

        private final String type;
        private Observable<PropertyObject> observable;
        private SearchQuery query;
        private PropertyObjectParser parser;

        ObservableResponseListener(SearchQuery query, PropertyObjectParser parser, String type) {
            this.query = query;
            this.parser = parser;
            this.type = type;
        }

        Observable<PropertyObject> getObservable() {
            observable = Observable.create(emitter -> queryManager.addQuery(query, new QueryResponseListener() {
                @Override
                public void onResponse(Query query, JSONObject response) {
                    if (!emitter.isDisposed()) {
                        List<PropertyObject> propertyObjects = parser.fromSearch(response, type);
                        for (PropertyObject propertyObject : propertyObjects) {
                            emitter.onNext(propertyObject);
                        }
                        emitter.onComplete();
                    }
                }

                @Override
                public void onError(Throwable exception) {
                    emitter.onError(exception);
                }
            }));
            return observable;
        }
    }
}
