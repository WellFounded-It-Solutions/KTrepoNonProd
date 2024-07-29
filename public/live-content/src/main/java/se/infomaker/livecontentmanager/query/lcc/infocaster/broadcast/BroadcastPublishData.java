package se.infomaker.livecontentmanager.query.lcc.infocaster.broadcast;

import com.google.gson.JsonObject;

import java.util.Map;

class BroadcastPublishData {
    private String broadcastId;
    private Map<String, String> filter;

    private JsonObject payload;

    public String getBroadcastId() {
        return broadcastId;
    }

    public Map<String, String> getFilter() {
        return filter;
    }

    public JsonObject getPayload() {
        return payload;
    }
}
