
package se.infomaker.livecontentmanager.stream;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Single;
import se.infomaker.livecontentmanager.config.TransformSettingsConfig;
import se.infomaker.livecontentmanager.model.StreamEventWrapper;
import se.infomaker.livecontentmanager.parser.DefaultPropertyObjectParser;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.parser.PropertyObjectParser;
import se.infomaker.livecontentmanager.query.ObjectResolver;
import se.infomaker.livecontentmanager.query.Query;

public class StreamResponseListenerTest {

    @Test
    public void testStreamCreated() throws JSONException {
        StreamResponseListener.StreamListener streamListener = Mockito.mock(StreamResponseListener.StreamListener.class);
        StreamResponseListener listener = new StreamResponseListener(null, new DefaultPropertyObjectParser(new HashMap<>(), new HashMap<>(), new TransformSettingsConfig()),  "Article",  streamListener);
        JSONObject jsonObject = createResponse("streamCreated");

        listener.onResponse(Mockito.mock(Query.class), jsonObject);
        Mockito.verify(streamListener, Mockito.times(1)).onStreamCreated(Mockito.any(Query.class), Mockito.anyString());
    }

    @Test
    public void testStreamNotifyAdd() throws JSONException {
        StreamResponseListener.StreamListener streamListener = Mockito.mock(StreamResponseListener.StreamListener.class);

        PropertyObjectParser parser = Mockito.mock(PropertyObjectParser.class);
        ArrayList<PropertyObject> objects = new ArrayList<>();
        objects.add(Mockito.mock(PropertyObject.class));
        StreamEventWrapper wrapper = new StreamEventWrapper(EventType.ADD, objects);
        Mockito.when(parser.fromStreamNotification(Mockito.any(JSONObject.class), Mockito.any())).thenReturn(wrapper);
        StreamResponseListener listener = new StreamResponseListener(mockObjectResolver(), parser,  "Article",  streamListener);
        JSONObject jsonObject = createResponse("streamNotify");
        listener.onResponse(Mockito.mock(Query.class), jsonObject);
        Mockito.verify(streamListener, Mockito.times(1)).onStreamNotify(Mockito.any(), ArgumentMatchers.anyList(), ArgumentMatchers.any());
    }

    @NonNull
    private ObjectResolver mockObjectResolver() {
        ObjectResolver objectResolver = Mockito.mock(ObjectResolver.class);
        Mockito.when(objectResolver.fromEventWrapper(Mockito.any(), Mockito.any())).thenReturn(Single.just(new ArrayList<>()));
        return objectResolver;
    }

    @Test
    public void testStreamNotifyUpdate() throws JSONException {
        StreamResponseListener.StreamListener streamListener = Mockito.mock(StreamResponseListener.StreamListener.class);
        PropertyObjectParser parser = Mockito.mock(PropertyObjectParser.class);
        ArrayList<PropertyObject> objects = new ArrayList<>();
        objects.add(Mockito.mock(PropertyObject.class));
        StreamEventWrapper wrapper = new StreamEventWrapper(EventType.UPDATE, objects);
        Mockito.when(parser.fromStreamNotification(Mockito.any(JSONObject.class), Mockito.any())).thenReturn(wrapper);
        StreamResponseListener listener = new StreamResponseListener(mockObjectResolver(), parser,  "Article", streamListener);
        JSONObject jsonObject = createResponse("streamNotify");
        listener.onResponse(Mockito.mock(Query.class), jsonObject);
        Mockito.verify(streamListener, Mockito.times(1)).onStreamNotify(Mockito.any(), ArgumentMatchers.anyList(), ArgumentMatchers.any());
    }

    @Test
    public void testStreamNotifyDelete() throws JSONException {
        StreamResponseListener.StreamListener streamListener = Mockito.mock(StreamResponseListener.StreamListener.class);

        PropertyObjectParser parser = Mockito.mock(PropertyObjectParser.class);
        ArrayList<PropertyObject> objects = new ArrayList<>();
        objects.add(Mockito.mock(PropertyObject.class));
        StreamEventWrapper wrapper = new StreamEventWrapper(EventType.DELETE, objects);
        Mockito.when(parser.fromStreamNotification(Mockito.any(JSONObject.class), Mockito.any())).thenReturn(wrapper);

        StreamResponseListener listener = new StreamResponseListener(null, parser,  "Article", streamListener);
        JSONObject jsonObject = createResponse("streamNotify");
        listener.onResponse(Mockito.mock(Query.class), jsonObject);
        Mockito.verify(streamListener, Mockito.times(1)).onStreamNotify(Mockito.any(), ArgumentMatchers.anyList(), ArgumentMatchers.any());
    }

    @Test
    public void testStreamNotifyStreamDeleted() throws JSONException {
        StreamResponseListener.StreamListener streamListener = Mockito.mock(StreamResponseListener.StreamListener.class);
        PropertyObjectParser parser = Mockito.mock(PropertyObjectParser.class);
        StreamEventWrapper wrapper = new StreamEventWrapper(EventType.DELETE, new ArrayList<>());
        Mockito.when(parser.fromStreamNotification(Mockito.any(JSONObject.class), Mockito.any())).thenReturn(wrapper);

        StreamResponseListener listener = new StreamResponseListener(null, parser,  "Article", streamListener);
        JSONObject jsonObject = createResponse("streamDeleted");
        listener.onResponse(Mockito.mock(Query.class), jsonObject);
        Mockito.verify(streamListener, Mockito.times(1)).onStreamDeleted(Mockito.any(Query.class), Mockito.anyString());
    }

    @NonNull
    private JSONObject createResponse(String action) throws JSONException {
        JSONObject response = new JSONObject();
        JSONObject payload = new JSONObject();
        payload.put("action", action);
        response.put("payload", payload);
        JSONObject data = new JSONObject();
        data.put("streamId", "the stream id");
        payload.put("data", data);
        return response;
    }
}
