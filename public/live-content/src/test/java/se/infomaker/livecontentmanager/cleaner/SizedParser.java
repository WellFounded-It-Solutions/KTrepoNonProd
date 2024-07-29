package se.infomaker.livecontentmanager.cleaner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import se.infomaker.livecontentmanager.model.StreamEventWrapper;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.parser.PropertyObjectParser;

public class SizedParser implements PropertyObjectParser
{
    private final int page;
    private final int total;
    private int created;

    public SizedParser(int total, int page)
    {
        this.total = total;
        this.page = page;
    }

    private List<PropertyObject> create(int page) {
        ArrayList<PropertyObject> lists = new ArrayList<>();
        for (int i = 0; i < page; i++) {
            PropertyObject propertyObject = new PropertyObject(new JSONObject(), UUID.randomUUID().toString()) {

                private Date date = new Date();

                public Date getPublicationDate() {
                    return date;
                }
            };
            lists.add(propertyObject);
        }
        return lists;
    }

    public void reset() {
        created = 0;
    }

    @NotNull
    @Override
    public List<PropertyObject> fromSearch(@NotNull JSONObject response, String type) {
        created += page;
        if (created > total) {
            return Collections.emptyList();
        }
        else {
            return create(page);
        }
    }

    @NotNull
    @Override
    public StreamEventWrapper fromStreamNotification(@Nullable JSONObject event, String type) {
        return null;
    }

    @NotNull
    @Override
    public Set<String> getAllIds(@NotNull List<? extends PropertyObject> from, @NotNull String type) {
        return Collections.emptySet();
    }
}
