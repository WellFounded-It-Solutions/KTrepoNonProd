package se.infomaker.livecontentmanager.query.lca;

import com.google.gson.JsonObject;

public class JsonObjectPayload extends Payload{
    private JsonObject data;

    public JsonObjectPayload(String action, String contentProvider, JsonObject data) {
        super(action, new ContentProvider(contentProvider));
        this.data = data;
    }
}
