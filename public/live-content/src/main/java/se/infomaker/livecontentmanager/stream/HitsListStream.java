package se.infomaker.livecontentmanager.stream;

import android.os.Handler;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.infomaker.livecontentmanager.config.LiveContentConfig;
import se.infomaker.livecontentmanager.extensions.JSONUtil;
import se.infomaker.livecontentmanager.parser.DefaultPropertyObjectParser;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.parser.PropertyObjectParser;
import se.infomaker.livecontentmanager.query.CreateStreamQuery;
import se.infomaker.livecontentmanager.query.ObjectResolver;
import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryFilter;
import se.infomaker.livecontentmanager.query.QueryManager;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import se.infomaker.livecontentmanager.query.SearchQuery;
import se.infomaker.livecontentmanager.query.runnable.AndroidRunnableHandler;
import se.infomaker.livecontentmanager.query.runnable.RunnableHandler;
import timber.log.Timber;

public class HitsListStream {

    public static final int TWO_HOURS_IN_MILLIS = 2 * 60 * 60 * 1000;
    public static final int DEACTIVATE_DELAY_MILLIS = 3000;
    private final QueryManager queryManager;
    private final String properties;
    private final LiveContentConfig config;
    private final List<QueryFilter> filters;
    private final QueryResponseListener searchListener;
    private final StreamResponseListener streamResponseListener;
    private final Set<StreamListener<PropertyObject>> listeners = new HashSet<>();

    private CreateStreamQuery streamQuery;
    private String streamQueryId;

    private List<PropertyObject> items = new ArrayList<>();
    private boolean active;
    private boolean inErrorState = false;
    private SearchQuery currentSearchQuery;
    private SearchQuery lastSearchQuery;
    private boolean hasReachedEnd;
    private Date lastActive;
    private SearchQuery catchupQuery;
    private final HitsListResponseHandler catchupResponseHandler;
    private final RunnableHandler handler;
    private final Runnable deactivateRunnable = () -> setActive(false);
    private ObjectResolver objectResolver;
    private Date lastUpdated = null;
    private Date lastUpdateAttempt = null;

    public HitsListStream(QueryManager queryManager, RunnableHandler runnableHandler, PropertyObjectParser parser, LiveContentConfig config, String properties, List<QueryFilter> filters, String type) {
        handler = runnableHandler;
        objectResolver = new ObjectResolver(config.getSearch(), config.getProperties(type), parser, queryManager, runnableHandler instanceof AndroidRunnableHandler ? AndroidSchedulers.mainThread(): Schedulers.single());
        this.filters = filters;
        // TODO right place to insert (or dont insert if after end of list)
        streamResponseListener = new StreamResponseListener(objectResolver, parser, type, new StreamResponseListener.StreamListener() {
            @Override
            public void onStreamCreated(Query query, String streamId) {
                streamQueryId = streamId;
                inErrorState = false;
            }

            @Override
            public void onStreamDeleted(Query query, String streamId) {
                streamQueryId = null;
            }

            @Override
            public void onStreamNotify(Query query, List<PropertyObject> list, EventType eventtype) {
                switch (eventtype) {
                    case ADD:
                        if (items.isEmpty()) {
                            items.addAll(list);
                            notifyListenersOnItemsAdded(0, list);
                        } else {
                            purgeExisting(list, true);
                            for (PropertyObject item : list) {
                                addItem(item);
                            }
                        }
                        break;
                    case UPDATE:
                        for (PropertyObject hitsList : list) {
                            updateItem(hitsList);
                        }
                        for (StreamListener<PropertyObject> listener : listeners) {
                            listener.onItemsChanged(list);
                        }
                        break;
                    case DELETE:
                        for (PropertyObject hitsListToDelete : list) {
                            removeItem(hitsListToDelete);
                        }
                        for (StreamListener<PropertyObject> listener : listeners) {
                            listener.onItemsRemoved(list);
                        }
                        break;
                }
            }

            @Override
            public void onError(Exception exception) {
                inErrorState = true;
                for (StreamListener<PropertyObject> listener : listeners) {
                    listener.onError(exception);
                }

            }
        });

        catchupResponseHandler = new HitsListResponseHandler(objectResolver, parser, type) {
            @Override
            public void onResponse(Query query, JSONObject response) {
                super.onResponse(query, response);
                lastUpdated = JSONUtil.getLastUpdated(response);
                catchupQuery = null;
            }
            @Override
            public void onAdd(List<PropertyObject> hitsLists) {
                if (hasReachedEnd)
                {
                    for (StreamListener<PropertyObject> listener : listeners) {
                        listener.onEndReached();
                    }
                }
                int removed = purgeExisting(hitsLists, false);
                if (removed == 0) {
                    asNewresult(hitsLists);
                }
                else {
                    items.addAll(0, hitsLists);
                    notifyListenersOnItemsAdded(0, hitsLists);
                }
            }

            @Override
            void onRemove(List<PropertyObject> hitsLists) {
                for (PropertyObject hitsListToDelete : hitsLists) {
                    removeItem(hitsListToDelete);
                }
                for (StreamListener<PropertyObject> listener : listeners) {
                    listener.onItemsRemoved(hitsLists);
                }
            }

            @Override
            void onEdit(List<PropertyObject> hitsLists) {
                for (PropertyObject hitsList : hitsLists) {
                    updateItem(hitsList);
                }
                for (StreamListener<PropertyObject> listener : listeners) {
                    listener.onItemsChanged(hitsLists);
                }
                for (StreamListener<PropertyObject> listener : listeners) {
                    listener.onItemsChanged(hitsLists);
                }
            }

            @Override
            public void onError(Throwable exception) {
                Timber.w(exception);
                inErrorState = true;
                catchupQuery = null;
                lastUpdateAttempt = new Date();
                for (StreamListener<PropertyObject> listener : listeners) {
                    listener.onError((Exception) exception);
                }
            }
        };

        this.streamQuery = new CreateStreamQuery(config.getStream(), filters);
        this.searchListener = new HitsListResponseHandler(objectResolver, parser, type) {

            @Override
            public void onResponse(Query query, JSONObject response) {
                super.onResponse(query, response);
                lastUpdated = JSONUtil.getLastUpdated(response);
            }

            @Override
            void onAdd(List<PropertyObject> hitsLists) {
                if (currentSearchQuery != null) {
                    lastSearchQuery = currentSearchQuery;
                }
                currentSearchQuery = null;
                if (hitsLists.size() == 0)
                {
                    hasReachedEnd = true;
                    for (StreamListener<PropertyObject> listener : listeners) {
                        listener.onEndReached();
                    }
                }
                else
                {
                    int index = items.size();
                    purgeExisting(hitsLists, true);
                    items.addAll(hitsLists);
                    notifyListenersOnItemsAdded(index, hitsLists);
                }
            }

            @Override
            void onRemove(List<PropertyObject> hitsLists) {
                for (PropertyObject hitsListToDelete : hitsLists) {
                    removeItem(hitsListToDelete);
                }
                for (StreamListener<PropertyObject> listener : listeners) {
                    listener.onItemsRemoved(hitsLists);
                }
            }

            @Override
            void onEdit(List<PropertyObject> hitsLists) {
                for (PropertyObject hitsList : hitsLists) {
                    updateItem(hitsList);
                }
                for (StreamListener<PropertyObject> listener : listeners) {
                    listener.onItemsChanged(hitsLists);
                }
                for (StreamListener<PropertyObject> listener : listeners) {
                    listener.onItemsChanged(hitsLists);
                }
            }

            @Override
            public void onError(Throwable exception) {
                inErrorState = true;
                lastUpdateAttempt = new Date();
                Timber.w(exception);
                for (StreamListener<PropertyObject> listener : listeners) {
                    listener.onError((Exception) exception);
                }
            }
        };
        this.queryManager = queryManager;
        this.config = config;
        this.properties = properties;
    }

    public
    HitsListStream(QueryManager queryManager, DefaultPropertyObjectParser parser, LiveContentConfig config, String properties, List<QueryFilter> filters, String type) {
        this(queryManager, new AndroidRunnableHandler(new Handler()), parser, config, properties, filters, type);
    }

    public boolean hasError() {
        return inErrorState;
    }

    private void removeItem(PropertyObject hitsListToDelete) {
        Iterator<PropertyObject> it = items.iterator();
        while (it.hasNext()) {
            PropertyObject hitsList = it.next();
            if (hitsList.getId().equals(hitsListToDelete.getId())) {
                it.remove();
            }
        }
    }

    private void updateItem(PropertyObject hitsList) {
        for (int i = 0; i < items.size(); i++) {
            if (hitsList.getId().equals(items.get(i).getId())) {
                items.set(i, hitsList);
                return;
            }
        }
        //If item is not in the list, assume it was added with property updater
        addItem(hitsList);
    }

    private void addItem(PropertyObject hitsListToAdd) {
        if (hitsListToAdd == null) {
            Timber.w("Trying to add null object");
            return;
        }
        if (items.isEmpty()) {
            items.add(0, hitsListToAdd);
            notifyListenersOnItemsAdded(0, Arrays.asList(hitsListToAdd));
            return;
        }
        // TODO Check last item to skip iteration
        for (int i = 0; i < items.size(); i++) {
            PropertyObject hitsList = items.get(i);
            if (hitsList != null) {
                if (hitsListToAdd.getPublicationDate().after(hitsList.getPublicationDate())) {
                    items.add(i, hitsListToAdd);
                    notifyListenersOnItemsAdded(i, Arrays.asList(hitsListToAdd));
                    break;
                }
            }
        }

        if (!items.contains(hitsListToAdd) && hasReachedEnd) {
            int index = items.size();
            items.add(hitsListToAdd);
            notifyListenersOnItemsAdded(index, Arrays.asList(hitsListToAdd));
        }
    }

    private void notifyListenersOnItemsAdded(int index, List<PropertyObject> items) {
        for (StreamListener<PropertyObject> listener : listeners) {
            listener.onItemsAdded(index, items);
        }
    }

    /**
     * Remove hits already included in stream
     *
     * This method assumes that there is an overlapping window of hits
     *
     * @param hitsLists
     * @return
     */
    private int purgeExisting(List<PropertyObject> hitsLists, boolean optimized) {
        Iterator<PropertyObject> iterator = hitsLists.iterator();
        int removed = 0;
        while (iterator.hasNext()) {
            PropertyObject hitsList = iterator.next();
            String id = hitsList.getId();
            if (id == null) {
                continue;
            }
            boolean wasRemoved = false;
            for (int i = items.size() - 1; i >=0 ; i--) {
                PropertyObject item = items.get(i);
                if (item != null && id.equals(item.getId()))
                {
                    iterator.remove();
                    removed++;
                    wasRemoved = true;
                    break;
                }
            }
            if (optimized && !wasRemoved) {
                break;
            }
        }
        return removed;
    }

    public PropertyObject get(int position)
    {
        return items.get(position);
    }

    public int size()
    {
        return items.size();
    }

    public void addListener(StreamListener<PropertyObject> listener) {
        handler.removeCallbacks(deactivateRunnable);
        listeners.add(listener);
        setActive(true);
    }

    public void removeListener(StreamListener<PropertyObject> listener)
    {
        if (listeners.remove(listener) && listeners.size() == 0)
        {
            handler.postDelayed(deactivateRunnable, DEACTIVATE_DELAY_MILLIS);
        }
    }

    public void searchMore()
    {
        if (hasReachedEnd)
        {
            Timber.d("Has reached end");
            for (StreamListener<PropertyObject> listener : listeners) {
                listener.onEndReached();
            }
            return;
        }
        if (currentSearchQuery == null)
        {
            if (lastSearchQuery == null)
            {
                currentSearchQuery = createInitialSearch();
            }
            else
            {
                currentSearchQuery = lastSearchQuery.next();
            }
            queryManager.addQuery(currentSearchQuery, searchListener);
        }
        else
        {
            Timber.d("Search already in flight");
        }
    }

    private SearchQuery createInitialSearch() {
        return new SearchQuery(config.getSearch(), properties, 0, config.getPagingLimit(), filters);
    }

    public boolean hasReachedEnd()
    {
        return hasReachedEnd;
    }

    public boolean isActive() {
        return active;
    }

    private void setActive(boolean active) {
        if (this.active == active)
        {
            return;
        }
        this.active = active;
        if (active)
        {
            startStream();
            if (lastActive == null) {
                searchMore();
            }
            else if (shouldCatchup()) {
                catchup();
            }
            else {
                lastActive = null;
                reset();
            }
        }
        else
        {
            lastActive = new Date();
            stopStream();
        }
    }

    private boolean shouldCatchup() {
        return lastSearchQuery != null && System.currentTimeMillis() - lastActive.getTime() < TWO_HOURS_IN_MILLIS && items.size() > 0;
    }

    private void catchup() {
        if (catchupQuery != null) {
            Timber.d("Already trying to catchup");
            return;
        }
        Timber.d("Start catchup");
        catchupQuery = lastSearchQuery.createCatchupQuery();

        queryManager.addQuery(catchupQuery, catchupResponseHandler);
    }

    private void asNewresult(List<PropertyObject> items ) {
        this.items.clear();
        currentSearchQuery = null;
        lastSearchQuery = null;
        hasReachedEnd = false;
        this.items.addAll(items);
        for (StreamListener<PropertyObject> listener : listeners) {
            listener.onReset();
            listener.onItemsAdded(0, items);
        }
    }

    /**
     * Clears any loaded
     */
    public void reset()
    {
        Timber.d("Reset");
        items.clear();
        active = false;
        currentSearchQuery = null;
        lastSearchQuery = null;
        hasReachedEnd = false;

        for (StreamListener<PropertyObject> listener : listeners) {
            listener.onReset();
        }

        setActive(listeners.size() > 0);
    }

    private synchronized    void stopStream() {
        if (streamQueryId != null)
        {
            //Timber.d("Requesting delete stream " + streamQueryId);
            queryManager.removeQuery(streamQuery);
            streamQueryId = null;
        }
    }

    private void startStream() {
        inErrorState = false;
        if (streamQueryId != null) {
            Timber.d("Stream already open");
            return;
        }
        queryManager.addQuery(streamQuery, streamResponseListener);
    }

    public List<PropertyObject> getItems()
    {
        return Collections.unmodifiableList(items);
    }

    @Nullable
    public Date getLastUpdated() {
        return lastUpdated;
    }

    @Nullable
    public Date getLastUpdateAttempt() {
        return lastUpdateAttempt;
    }
}
