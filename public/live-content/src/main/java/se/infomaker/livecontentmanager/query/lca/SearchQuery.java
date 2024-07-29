package se.infomaker.livecontentmanager.query.lca;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class SearchQuery {
    public static final String DEFAULT_SORT_INDEX_FIELD = "Pubdate";
    @SerializedName("q")
    private String query;

    @SerializedName("start")
    private int offset;

    @SerializedName("properties")
    private String properties;

    @SerializedName("sort")
    private Map<String, String> sort;

    private int limit;

    @SerializedName("contenttype")
    private String contentType;

    public SearchQuery(String query, int offset, String properties, Map<String, String> sort, int limit, String contentType) {
        this.query = query;
        this.offset = offset;
        this.properties = properties;
        this.sort = sort;
        this.limit = limit;
        this.contentType = contentType;
    }
}
