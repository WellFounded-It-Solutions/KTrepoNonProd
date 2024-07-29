package se.infomaker.livecontentmanager.stream;

import org.json.JSONObject;

import java.util.List;

import se.infomaker.livecontentmanager.model.StreamEventWrapper;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.parser.PropertyObjectParser;
import se.infomaker.livecontentmanager.query.ObjectResolver;
import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import se.infomaker.livecontentmanager.query.SearchQuery;

public abstract class HitsListResponseHandler implements QueryResponseListener {
    private ObjectResolver objectResolver;
    private final PropertyObjectParser parser;
    private final String type;

    public HitsListResponseHandler(ObjectResolver objectResolver, PropertyObjectParser parser, String type) {
        this.objectResolver = objectResolver;
        this.parser = parser;
        this.type = type;
    }

    @Override
    public void onResponse(Query query, JSONObject response) {
        if (query instanceof SearchQuery) {
            onAdd(parser.fromSearch(response, type));
        }
        else {
            StreamEventWrapper event = parser.fromStreamNotification(response, type);

            switch (event.getEvent()) {
                case ADD: {
                    objectResolver.fromEventWrapper(event, type).subscribe(this::onAdd);
                    break;
                }
                case DELETE: {
                    onRemove(event.getObjects());
                    break;
                }
                case UPDATE: {
                    objectResolver.fromEventWrapper(event, type).subscribe(this::onEdit);
                    break;
                }
            }
        }
    }

    /**
     * Called when HitsLists are added
     * @param objects
     */
    void onAdd(List<PropertyObject> objects) { /* NOP*/ }

    /**
     * Called when HitsLists are removed
     * @param objects
     */
    void onRemove(List<PropertyObject> objects) { /* NOP*/ }

    /**
     * Called when HitsLists are updated
     * @param objects
     */
    void onEdit(List<PropertyObject> objects) { /* NOP*/ }
}
