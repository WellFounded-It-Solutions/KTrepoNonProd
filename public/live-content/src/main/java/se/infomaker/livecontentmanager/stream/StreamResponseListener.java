package se.infomaker.livecontentmanager.stream;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import se.infomaker.frtutilities.JSONUtil;
import se.infomaker.livecontentmanager.model.StreamEventWrapper;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.parser.PropertyObjectParser;
import se.infomaker.livecontentmanager.query.ObjectResolver;
import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import timber.log.Timber;

class StreamResponseListener implements QueryResponseListener {
    private static final String STREAM_NOTIFY = "streamNotify";
    private static final String STREAM_DELETED = "streamDeleted";
    private static final String STREAM_CREATED = "streamCreated";
    private final String type;
    private ObjectResolver resolver;
    private final PropertyObjectParser parser;
    private final StreamListener listener;

    public interface StreamListener
    {
        void onStreamCreated(Query query, String streamId);
        void onStreamDeleted(Query query, String streamId);
        void onStreamNotify(Query query, List<PropertyObject> list, EventType eventType);
        void onError(Exception exception);
    }

    public StreamResponseListener(ObjectResolver resolver, PropertyObjectParser parser, String type,  StreamListener listener) {
        this.resolver = resolver;
        this.parser = parser;
        this.listener = listener;
        this.type = type;
    }

    @Override
    public void onResponse(Query query, JSONObject response) {
        String action = JSONUtil.optString(response, "payload.action");
        if (STREAM_CREATED.equals(action))
        {
            try {
                String id = JSONUtil.getString(response, "payload.data.streamId");
                listener.onStreamCreated(query, id);
            } catch (JSONException e) {
                onError(e);
                Timber.w(e, "Could not extract stream id");
            }
        }
        else if (STREAM_NOTIFY.equals(action))
        {
            StreamEventWrapper event = parser.fromStreamNotification(response, type);

            if (event.isNotEmpty()) {
                switch (event.getEvent()) {
                    case ADD:
                    case UPDATE: {
                        resolver.fromEventWrapper(event, type).subscribe((propertyObjects, throwable) -> {
                            if (throwable != null) {
                                Timber.w(throwable, "Failed to resolve notification objects");
                            }
                            else {
                                listener.onStreamNotify(query, propertyObjects, event.getEvent());
                            }
                        });
                        break;
                    }

                    case DELETE:
                    case UNKNOWN:
                    default: {
                        listener.onStreamNotify(query, event.getObjects(), event.getEvent());
                    }
                }
            }
            else {
                Timber.w("Empty event for response: " + response);
            }
        }
        else if (STREAM_DELETED.equals(action))
        {
            try {
                listener.onStreamDeleted(query, JSONUtil.getString(response, "payload.data.streamId"));
            } catch (JSONException e) {
                onError(e);
                Timber.w(e, "Could not get stream id");
            }
        }
    }

    @Override
    public void onError(Throwable exception) {

        listener.onError(new Exception(exception));
    }
}
