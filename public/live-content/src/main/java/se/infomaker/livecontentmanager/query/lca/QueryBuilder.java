package se.infomaker.livecontentmanager.query.lca;

import java.util.Map;

public class QueryBuilder {
    private String query;
    private int offset;
    private String properties;
    private Map<String, String> sort;
    private int limit;
    private String contentType;

    public QueryBuilder setQuery(String query) {
        this.query = query;
        return this;
    }

    public QueryBuilder setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public QueryBuilder setProperties(String properties) {
        this.properties = properties;
        return this;
    }

    public QueryBuilder setSort(Map<String, String> sortIndexField) {
        this.sort = sortIndexField;
        return this;
    }

    public QueryBuilder setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public QueryBuilder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public SearchQuery createQuery() {
        return new SearchQuery(query, offset, properties, sort, limit, contentType);
    }
}