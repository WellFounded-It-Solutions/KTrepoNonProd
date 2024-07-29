package se.infomaker.livecontentmanager.query;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class DeleteStreamQuery implements Query{
    private final String contentProvider;
    private String streamId;

    public DeleteStreamQuery(String contentProvider, String streamId) {
        this.contentProvider = contentProvider;
        this.streamId = streamId;
    }

    @Override
    public boolean finishedOnResponse() {
        return true;
    }

    @Override
    public JSONObject toJSONObject() {

        try {
            JSONObject query = new JSONObject();
            JSONObject payload = new JSONObject();
            payload.put("action", "streamDelete");
            payload.put("version", 1);
            payload.put("auth", new JSONObject());
            JSONObject data = new JSONObject();
            data.put("streamId", streamId);
            payload.put("data", data);
            payload.put("version", 1);
            JSONObject contentProvider = new JSONObject();
            contentProvider.put("id", this.contentProvider);
            payload.put("contentProvider", contentProvider);
            query.put("payload", payload);
            return query;
        } catch (JSONException e) {
            Timber.e(e, "Could not create request");
        }

        return new JSONObject();
    }

    public String getStreamId() {
        return streamId;
    }

    @Override
    public String toString() {
        return "DeleteStreamQuery{" +
                "contentProvider='" + contentProvider + '\'' +
                ", streamId='" + streamId + '\'' +
                '}';
    }
}
