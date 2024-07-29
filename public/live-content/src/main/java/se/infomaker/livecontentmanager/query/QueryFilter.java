package se.infomaker.livecontentmanager.query;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Modifies search/stream queries by adding filters
 */
public interface QueryFilter extends Serializable{
    /**
     * The same filter configuration must always return the same identifier
     * @return a string identifying the filter
     */
    @NonNull
    String identifier();

    /**
     * Creates a stream filter that can be appended to the query
     * @return filter
     */
    @NonNull
    JSONObject createStreamFilter();

    /**
     * Adds the filter to the provided base query and returns the modified query
     * @param baseQuery query
     * @return modified query
     */
    @NonNull
    String createSearchQuery(@NonNull String baseQuery);
}
