package se.infomaker.livecontentmanager.stream;

import androidx.annotation.NonNull;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import se.infomaker.livecontentmanager.config.SearchConfig;
import se.infomaker.livecontentmanager.model.StreamEventWrapper;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.parser.PropertyObjectParser;
import se.infomaker.livecontentmanager.query.ObjectResolver;
import se.infomaker.livecontentmanager.query.Query;
import timber.log.Timber;

public class HitsListResponseHandlerTest {

    private boolean called;

    @Test
    public void testAdd()
    {
        SearchConfig searchConfig = new SearchConfig("*:*", "article", null);
        searchConfig.setSortIndex("uuid");
        ArrayList<String> properties = new ArrayList<>();
        properties.add("uuid");

        HitsListResponseHandler hitsListResponseHandler = new HitsListResponseHandler(mockObjectResolver(), createArticleParser(EventType.ADD), "Article") {
            @Override
            public void onError(Throwable exception) {
                Timber.e(exception);
            }

            @Override
            public void onAdd(List<PropertyObject> hitsLists) {
                super.onAdd(hitsLists);
                called = true;
            }
        };
        hitsListResponseHandler.onResponse(Mockito.mock(Query.class), new JSONObject());
        Assert.assertTrue(called);
    }

    @NonNull
    private ObjectResolver mockObjectResolver() {
        ObjectResolver mock = Mockito.mock(ObjectResolver.class);
        ArrayList<PropertyObject> objects = new ArrayList<>();
        Mockito.when(mock.fromEventWrapper(Mockito.any(), Mockito.any())).thenReturn(Single.just(objects));
        return mock;
    }

    @Test
    public void testRemove()
    {
        HitsListResponseHandler hitsListResponseHandler = new HitsListResponseHandler(mockObjectResolver(), createArticleParser(EventType.DELETE), "Article") {
            @Override
            public void onError(Throwable exception) {

            }

            @Override
            void onRemove(List<PropertyObject> hitsLists) {
                super.onRemove(hitsLists);
                called = true;
            }
        };
        hitsListResponseHandler.onResponse(Mockito.mock(Query.class), new JSONObject());
        Assert.assertTrue(called);
    }

    @Test
    public void testEdit()
    {
        HitsListResponseHandler hitsListResponseHandler = new HitsListResponseHandler(mockObjectResolver(), createArticleParser(EventType.UPDATE), "Article") {
            @Override
            public void onError(Throwable exception) {

            }

            @Override
            public void onEdit(List<PropertyObject> hitsLists) {
                super.onEdit(hitsLists);
                called = true;
            }
        };
        hitsListResponseHandler.onResponse(Mockito.mock(Query.class), new JSONObject());
        Assert.assertTrue(called);
    }

    private PropertyObjectParser createArticleParser(EventType eventtype) {
        PropertyObjectParser parser = Mockito.mock(PropertyObjectParser.class);
        Mockito.when(parser.fromStreamNotification(Mockito.any(JSONObject.class), Mockito.any())).thenReturn(new StreamEventWrapper(eventtype, new ArrayList<>()));
        return parser;
    }
}
