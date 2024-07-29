package se.infomaker.livecontentmanager.query.lcc.infocaster;

import com.google.gson.JsonObject;

public class PublishPayload {
    private JsonObject result;
    private JsonObject parameters;
    private String streamId;

    public JsonObject getResult() {
        return result;
    }

    public JsonObject getParameters() {
        return parameters;
    }

    public String getStreamId() {
        return streamId;
    }
}
