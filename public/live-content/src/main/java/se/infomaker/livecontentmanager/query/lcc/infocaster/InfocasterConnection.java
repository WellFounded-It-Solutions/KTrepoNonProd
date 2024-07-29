package se.infomaker.livecontentmanager.query.lcc.infocaster;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import se.infomaker.frtutilities.gson.typeadapters.RuntimeTypeAdapterFactory;
import se.infomaker.livecontentmanager.query.lcc.infocaster.broadcast.BroadcastPublishEvent;
import se.infomaker.livecontentmanager.query.lcc.infocaster.broadcast.InfocasterInstanceService;
import se.infomaker.livecontentmanager.query.lcc.infocaster.broadcast.SubscriptionRequest;
import se.infomaker.livecontentmanager.query.runnable.AndroidRunnableHandler;
import se.infomaker.livecontentmanager.query.runnable.RunnableHandler;
import timber.log.Timber;

public class InfocasterConnection extends WebSocketListener {

    private static final int NORMAL_CLOSURE_CODE = 1000;
    private final RunnableHandler handler;
    private final String eventNotifierBroadcastId;
    private final Set<String> listenerIds = new HashSet<>();

    public static class Builder {
        private String eventNotifierBroadcastId;
        private String url;
        private Map<String, Class<? extends Event>> eventTypes = new HashMap<>();
        private RunnableHandler runnableHandler;
        private OkHttpClient okHttpClient;

        public Builder() {
            addEventType("sessionInit", SessionInitEvent.class);
            addEventType("publish", PublishEvent.class);
            addEventType("broadcastPublish", BroadcastPublishEvent.class);
            addEventType("ping", PingEvent.class);
            addEventType("subscribed", SubscribedEvent.class);
            addEventType("unsubscribed", UnsubscribeEvent.class);
        }

        public Builder setEventNotifierBroadcastId(String eventNotifierBroadcastId) {
            this.eventNotifierBroadcastId = eventNotifierBroadcastId;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setRunnableHandler(RunnableHandler runnableHandler) {
            this.runnableHandler = runnableHandler;
            return this;
        }

        public Builder setOkHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

        public Builder addEventType(String type, Class<? extends Event> clazz) {
            eventTypes.put(type, clazz);
            return this;
        }

        public InfocasterConnection create() throws URISyntaxException {

            return new InfocasterConnection(url, eventNotifierBroadcastId, eventTypes, runnableHandler, okHttpClient);
        }
    }

    private final Gson gson;
    private final String url;
    private final Map<String, String> queryParams;
    private final BehaviorRelay<Status> status = BehaviorRelay.createDefault(Status.NOT_CONNECTED);
    private final PublishRelay<Event> events = PublishRelay.create();
    private WebSocket ws;
    private boolean shouldBeConnected = false;
    private InfocasterInstanceService instanceService;
    private final OkHttpClient okHttpClient;

    private InfocasterConnection(String url, String eventNotifierBroadcastId, Map<String, Class<? extends Event>> eventTypes, RunnableHandler handler, OkHttpClient okHttpClient) throws URISyntaxException {
        this.eventNotifierBroadcastId = eventNotifierBroadcastId;
        this.okHttpClient = okHttpClient;
        final RuntimeTypeAdapterFactory<Event> typeFactory = RuntimeTypeAdapterFactory
                .of(Event.class, "type");
        if (eventTypes != null) {
            for (String type : eventTypes.keySet()) {
                typeFactory.registerSubtype(eventTypes.get(type), type);
            }
        }
        this.handler = handler != null ? handler : new AndroidRunnableHandler(new Handler(Looper.getMainLooper()));
        gson = new GsonBuilder()
                .setLenient()
                .registerTypeAdapterFactory(typeFactory)
                .registerTypeAdapter(GenericPayload.class, new GenericPayloadDeserializer())
                .create();
        this.url = url;
        queryParams = extractQueryParams(url);

        events.filter(event -> event instanceof SessionInitEvent)
                .map(event -> (SessionInitEvent) event)
                .subscribe(event -> {
                    instanceService = new InfocasterInstanceService.Builder()
                            .setData(event.getData())
                            .setOkHttpClient(okHttpClient)
                            .build();
                    status.accept(Status.withSession(event));
                }, throwable -> Timber.e(throwable, "Unexpected event"));
        status.subscribe(status -> {
            if (!status.isConnected()) {
                instanceService = null;
            }
        });
    }

    private static Map<String, String> extractQueryParams(String url){
        Map<String, String> params = new HashMap<>();

        try {
            int queryIndex = url.indexOf("?");
            if (queryIndex == -1) {
                return params;
            }
            String query = url.substring(queryIndex + 1);
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                params.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            Timber.e(e, "Failed to parse query params");
        }
        return params;
    }


    public void subscribeBroadcast(String listenerId, final List<Map<String, String>> filter) {
        if (filter == null || filter.size() == 0) {
            Timber.w("Will not subscribe with empty filter");
            return;
        }
        Timber.d("Subscribing with filters: %s", filter);
        status.subscribeOn(Schedulers.newThread()).filter(Status::isConnected).firstOrError()
                .map(status -> status.getSession().getData())
                .flatMap(session -> {
                    SubscriptionRequest request = new SubscriptionRequest(session.getSessionId(), session.getSessionSecret(), filter);
                    return instanceService.subscribeBroadcast(session.getInstanceId(),
                            session.getPublisherId(),
                            eventNotifierBroadcastId,
                            queryParams,
                            request);
                })
                .observeOn(getMainThread())
                .subscribe(response -> Timber.d("Subscribed broadcast: %s", response.toString()), throwable -> Timber.e(throwable, "Failed to subscribe to broadcast"));
        open(listenerId);
    }

    private Scheduler getMainThread() {
        if (handler instanceof AndroidRunnableHandler) {
            return AndroidSchedulers.mainThread();
        }
        else {
            return Schedulers.single();
        }
    }

    public void unsubscribeBroadcast(final List<Map<String, String>> filter) {
        if (shouldBeConnected) {
            status.subscribeOn(Schedulers.newThread()).filter(Status::isConnected).firstOrError()
                    .map(status -> status.getSession().getData())
                    .flatMap(session -> {
                        SubscriptionRequest request = new SubscriptionRequest(session.getSessionId(), session.getSessionSecret(), filter);
                        return instanceService.unsubscribeBroadcast(session.getInstanceId(),
                                session.getPublisherId(),
                                eventNotifierBroadcastId,
                                queryParams,
                                request);
                    })
                    .observeOn(getMainThread())
                    .subscribe(response -> Timber.d("Unsubscribed broadcast: %s", response.toString()), throwable -> Timber.e(throwable, "Failed to unsubscribe"));
        } else {
            Timber.d("Not connected, nothing to unsubscribe");
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Timber.e("Unexpected binary data: %s", bytes.utf8());
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Timber.d("Message: %s", text);
        try {
            Event event = gson.fromJson(text, Event.class);
            events.accept(event);
        } catch (Exception e) {
            Timber.e(e, "Failed to parse message");
        }

    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Timber.d("Closing socket");
        if (shouldBeConnected) {
            ws = null;
            Timber.d("Reopening socket");
            handler.postDelayed(this::open, 0);
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Timber.w(t, "Socket failure ");
        if (shouldBeConnected) {
            Timber.d("Reopening socket");
            handler.postDelayed(this::open, 0);
        }
    }

    /**
     * Connect the infocaster
     * if the infocaster already is connected this is a NOP
     */
    public void open(String id) {
        Timber.d("Open for %s", id);
        listenerIds.add(id);
        open();
    }

    private void open() {
        shouldBeConnected = true;
        if (ws == null) {
            Timber.d("Creating socket");
            ws = okHttpClient.newWebSocket(new Request.Builder().url(url).build(), this);
        }
    }

    public InfocasterInstanceService getInstanceService() {
        return instanceService;
    }

    /**
     * Disconnect the infocaster
     */
    public void close(String id) {
        Timber.d("Close for %s", id);
        if (listenerIds.remove(id) && listenerIds.isEmpty()) {
            Observable.just(listenerIds)
                    .delay(5, TimeUnit.SECONDS)
                    .filter(Set::isEmpty)
                    .subscribe(strings -> close(), throwable -> Timber.e(throwable, "Failed to close connection."));
        }
    }

    private synchronized void close() {
        shouldBeConnected = false;
        if (ws != null) {
            Timber.d("Closing socket");
            ws.close(NORMAL_CLOSURE_CODE, null);
            ws = null;
            status.accept(Status.NOT_CONNECTED);
        }
    }

    /**
     * Provides the current status of the infocaster
     *
     * @return an observable providing the infocaster status
     */
    public Observable<Status> getStatus() {
        return status;
    }

    /**
     * Get all events (except SessionInitEvent)
     *
     * @return
     */
    public Observable<PublishEvent> getEvents() {
        return events.filter(event -> (event instanceof PublishEvent)).map(event -> (PublishEvent) event);
    }

    public Observable<BroadcastPublishEvent> getBroadcastEvents() {
        return events.filter(event -> (event instanceof BroadcastPublishEvent)).map(event -> (BroadcastPublishEvent) event);
    }
}