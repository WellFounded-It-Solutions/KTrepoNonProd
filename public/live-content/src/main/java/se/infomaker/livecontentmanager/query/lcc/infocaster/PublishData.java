package se.infomaker.livecontentmanager.query.lcc.infocaster;

import com.google.gson.JsonObject;

import org.json.JSONObject;

public class PublishData {
    private boolean broadcast;
    private String publisherId;
    private String broadcastId;
    private String channel;
    private GenericPayload payload;

    public String getChannel() {
        return channel;
    }

    public JsonObject getPayload() {
        return getPayload(JsonObject.class);
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public String getBroadcastId() {
        return broadcastId;
    }

    public <T> T getPayload(Class<T> classOfT) {
        if (payload != null) {
            return payload.getPayload(classOfT);
        }
        return null;
    }

    public JSONObject getPayloadAsJSONObject() {
        if (payload != null) {
            return payload.asJSONObjectOrNull();
        }
        return null;
    }
}
