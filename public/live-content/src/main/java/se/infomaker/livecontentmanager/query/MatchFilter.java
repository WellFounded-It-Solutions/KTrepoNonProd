package se.infomaker.livecontentmanager.query;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

public class MatchFilter implements QueryFilter {
    private final String key;
    private final String value;

    public MatchFilter(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @NonNull
    @Override
    public String identifier() {
        return key + ":" + value;
    }

    @NonNull
    @Override
    public JSONObject createStreamFilter() {
        JSONObject filter = new JSONObject();
        JSONObject match = new JSONObject();
        try {
            match.put(key, value);
            filter.put("match_phrase", match);
        } catch (JSONException e) {
            Timber.e(e, "Could not create filter " + key + " : " + value);
        }

        return filter;
    }

    @NonNull
    @Override
    public String createSearchQuery(@NonNull String baseQuery) {
        return "(" + baseQuery + ") AND " + key + ":\"" + value +"\"";
    }
}
