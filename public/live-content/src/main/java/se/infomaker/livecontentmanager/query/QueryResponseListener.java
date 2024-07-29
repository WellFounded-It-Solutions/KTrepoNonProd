package se.infomaker.livecontentmanager.query;

import org.json.JSONObject;

public interface QueryResponseListener {
    /**
     *
     * @param response a response to the query
     */
    void onResponse(Query query, JSONObject response);

    /**
     *
     * @param exception
     */
    void onError(Throwable exception);
}
