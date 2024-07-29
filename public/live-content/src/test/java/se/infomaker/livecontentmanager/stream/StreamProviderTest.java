package se.infomaker.livecontentmanager.stream;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.infomaker.livecontentmanager.config.LiveContentConfig;
import se.infomaker.livecontentmanager.config.SearchConfig;
import se.infomaker.livecontentmanager.config.StreamConfig;
import se.infomaker.livecontentmanager.parser.PropertyObjectParser;
import se.infomaker.livecontentmanager.query.QueryFilter;
import se.infomaker.livecontentmanager.query.TestRunnableHandlerFactory;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.Meta;

public class StreamProviderTest {

    private StreamProvider provider;

    @Before
    public void setup() throws IOException {
        TestRunnableHandlerFactory handlerFactory = new TestRunnableHandlerFactory();
        Meta meta = new Meta("test", "1.0", "testIdentifier");
        /*SocketQueryManagerProvider socketQueryManagerProvider = new SocketQueryManagerProvider(meta,  File.createTempFile("tmp", ".tmp"), null);
        provider = new StreamProvider(socketQueryManagerProvider, new TestRunnableHandlerFactory());*/
    }

    @Test
    public void testInvalidUri() throws IOException {
        try {
            LiveContentConfig config = new LiveContentConfig();
            config.setLiveContentUrl("This is no valid uri!");
            //new SocketQueryManagerProvider(new Meta(null, null, null), File.createTempFile("tmp", ".tmp"), null).provide(config);
            Assert.fail();
        }
        catch (RuntimeException e) {
            // This is expected
        }
    }

    @Test
    public void testSameStream()
    {
        String properties = "one,two";
        PropertyObjectParser mock = Mockito.mock(PropertyObjectParser.class);
        HitsListStream first = provider.provide(mock, createLiveContentConfig("http://should.be.the.same", "query"), properties, "Article", null);
        HitsListStream second = provider.provide(mock, createLiveContentConfig("http://should.be.the.same", "query"), properties, "Article", null);
        Assert.assertSame(first, second);
    }

    @NonNull
    private List<String> createProperties(String... properties) {
        List<String> list = new ArrayList<>();
        for (String property : properties) {
            list.add(property);
        }
        return list;
    }

    private List<QueryFilter> createDummyFilters(String... filters)
    {
        List<QueryFilter> list = new ArrayList<>();
        for (final String filter : filters) {
            list.add(new QueryFilter() {
                @NonNull
                @Override
                public String identifier() {
                    return filter;
                }

                @NonNull
                @Override
                public JSONObject createStreamFilter() {
                    return new JSONObject();
                }

                @NonNull
                @Override
                public String createSearchQuery(@NonNull String baseQuery) {
                    return baseQuery + filter;
                }
            });
        }
        return list;
    }

    @NonNull
    private LiveContentConfig createLiveContentConfig(String url, String baseSearchQuery) {
        StreamConfig streamConfig = new StreamConfig("mock", (JsonObject) new JsonParser().parse("{ \"\" : \"{\"}"));
        SearchConfig searchConfig = new SearchConfig(baseSearchQuery, null, null);

        LiveContentConfig liveContentConfig = new LiveContentConfig(streamConfig, searchConfig, null, null, null, null, null);
        liveContentConfig.setLiveContentUrl(url);
        return liveContentConfig;
    }

    @Test
    public void testDifferentStream()
    {
        String properties = "one,two";
        PropertyObjectParser mock = Mockito.mock(PropertyObjectParser.class);
        HitsListStream first = provider.provide(mock, createLiveContentConfig("http://should.be.one", ""), properties, "Article", null);
        HitsListStream second = provider.provide(mock, createLiveContentConfig("http://should.be.two", ""), properties, "Article", null);
        Assert.assertNotSame(first, second);
    }

    @Test
    public void testDifferentProperties()
    {
        PropertyObjectParser mock = Mockito.mock(PropertyObjectParser.class);
        LiveContentConfig config = createLiveContentConfig("http://should.be.one", "");
        HitsListStream first = provider.provide(mock, config, "one,two", "Article", null);
        HitsListStream second = provider.provide(mock, config, "two,one,zebra", "Article", null);
        Assert.assertNotSame(first, second);
    }

    @Test
    public void testSameFilters()
    {
        PropertyObjectParser mock = Mockito.mock(PropertyObjectParser.class);
        LiveContentConfig config = createLiveContentConfig("http://should.be.one", "");
        String properties = "one,two";
        HitsListStream first = provider.provide(mock, config, properties, "Article", createDummyFilters("one", "zebra"));
        HitsListStream second = provider.provide(mock, config, properties, "Article", createDummyFilters("one", "zebra"));
        Assert.assertSame(first, second);
    }

    @Test
    public void testDifferentFilters()
    {
        PropertyObjectParser mock = Mockito.mock(PropertyObjectParser.class);
        LiveContentConfig config = createLiveContentConfig("http://should.be.one", "");
        HitsListStream first = provider.provide(mock, config, "one,two", "Article", createDummyFilters("one", "zebra"));
        HitsListStream second = provider.provide(mock, config, "one,two", "Article", createDummyFilters("one", "cat"));
        Assert.assertNotSame(first, second);
    }

    @Test
    public void testDifferentSearchQuery()
    {
        PropertyObjectParser mock = Mockito.mock(PropertyObjectParser.class);
        LiveContentConfig config1 = createLiveContentConfig("http://should.be.one", "one");
        LiveContentConfig config2 = createLiveContentConfig("http://should.be.one", "two");
        HitsListStream first = provider.provide(mock, config1, "one,two", "Article", createDummyFilters("one", "zebra"));
        HitsListStream second = provider.provide(mock, config2, "one,two", "Article", createDummyFilters("zebra", "zebra"));
        Assert.assertNotSame(first, second);
    }
}
