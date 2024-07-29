package se.infomaker.livecontentmanager.query.lca;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchRequest {
    private SearchPayload payload;


    public SearchRequest(SearchPayload payload) {
        this.payload = payload;
    }

    public JSONObject toJSONObject()
    {
        try {
            return new JSONObject(new Gson().toJson(this));
        } catch (JSONException e) {
            // This should NEVER happen
            throw new RuntimeException("Could not convert json to json!", e);
        }
    }
}
