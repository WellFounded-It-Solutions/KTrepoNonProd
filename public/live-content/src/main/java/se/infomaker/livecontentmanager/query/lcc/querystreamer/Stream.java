package se.infomaker.livecontentmanager.query.lcc.querystreamer;

import org.json.JSONObject;

import se.infomaker.livecontentmanager.query.CreateStreamQuery;
import se.infomaker.livecontentmanager.query.QueryResponseListener;

public class Stream {
    private final QueryResponseListener listener;
    private CreateStreamQuery query;
    private String streamId;

    public Stream(CreateStreamQuery query, JSONObject jsonObject, QueryResponseListener listener) {
        this.query = query;
        this.listener = listener;
        streamId = jsonObject.optString("streamId", null);
    }

    public CreateStreamQuery getQuery() {
        return query;
    }

    public String getStreamId() {
        return streamId;
    }

    public boolean hasStreamId() {
        return streamId != null;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public QueryResponseListener getListener() {
        return listener;
    }

    @Override
    public String toString() {
        return "Stream{" +
                "listener=" + listener +
                ", query=" + query +
                ", streamId='" + streamId + '\'' +
                '}';
    }
}
