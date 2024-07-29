package se.infomaker.livecontentui.section.datasource;

import java.util.Date;
import java.util.List;

import se.infomaker.livecontentui.section.SectionItem;

public class DataSourceResponse {
    public final List<SectionItem> items;
    public final Throwable error;
    public final Date lastUpdated;

    public DataSourceResponse(List<SectionItem> items, Throwable error, Date lastUpdated) {
        this.items = items;
        this.error = error;
        this.lastUpdated = lastUpdated;
    }
}
