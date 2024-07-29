package se.infomaker.livecontentmanager.query;

import org.json.JSONObject;

public interface Query {
    String PAYLOAD_DATA_QUERY_KEY_PATH = "payload.data.query";
    /**
     *
     * @return true if the query is considered done when one response is received
     */
    boolean finishedOnResponse();

    /**
     * Converts the query to a JSONObject representations
     * @return
     */
    JSONObject toJSONObject();
}
