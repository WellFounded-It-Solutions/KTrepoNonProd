package se.infomaker.livecontentmanager.query.lcc.infocaster;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import se.infomaker.frtutilities.JSONUtil;

public class PublishEvent implements Event {
    private PublishData data;

    public PublishData getData() {
        return data;
    }

    public String getStreamId() {
        if (data != null) {
            PublishPayload payload = data.getPayload(PublishPayload.class);
            if (payload != null) {
                return payload.getStreamId();
            }
        }
        return null;
    }

    public String getChannel() {
        return data.getChannel();
    }

    public boolean isBroadcast() {
        return data.isBroadcast();
    }

    public String getUUID() {
        return data.getPayload().get("uuid").getAsString();
    }

    public JSONObject toJSONObject() throws JSONException {
        Gson gson = new Gson();
        JSONObject out = new JSONObject(gson.toJson(this));
        // Overwrite payload with JSONObject version of payload.
        JSONUtil.put(out, "data.payload", data.getPayloadAsJSONObject());
        return out;
    }
}
