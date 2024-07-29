package se.infomaker.livecontentmanager.query.lcc.querystreamer;

import com.google.gson.JsonObject;

public class UpdateStream {
    private JsonObject query;
    private Config config;

    public UpdateStream(JsonObject query, Config config) {
        this.query = query;
        this.config = config;
    }
}
