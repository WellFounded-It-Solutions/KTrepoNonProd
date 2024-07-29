package se.infomaker.livecontentmanager.stream;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import junit.framework.Assert;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import se.infomaker.livecontentmanager.JUnitTree;
import se.infomaker.livecontentmanager.cleaner.SizedParser;
import se.infomaker.livecontentmanager.config.LiveContentConfig;
import se.infomaker.livecontentmanager.config.SearchConfig;
import se.infomaker.livecontentmanager.config.StreamConfig;
import se.infomaker.livecontentmanager.config.TransformSettingsConfig;
import se.infomaker.livecontentmanager.model.StreamEventWrapper;
import se.infomaker.livecontentmanager.parser.PropertyObjectParser;
import se.infomaker.livecontentmanager.parser.DefaultPropertyObjectParser;
import se.infomaker.livecontentmanager.parser.PropertyObject;
import se.infomaker.livecontentmanager.query.CreateStreamQuery;
import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryManager;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import se.infomaker.livecontentmanager.query.TestRunnableHandler;
import se.infomaker.livecontentmanager.query.runnable.RunnableHandler;
import timber.log.Timber;

public class HitsListStreamTest {

    @Mock
    private SizedParser parser;

    @Mock
    private QueryManager queryManager;

    private RunnableHandler runnableHandler = new TestRunnableHandler();


    private String properties;

    private boolean endReached;
    private boolean reset;

    @Before
    public void setup()
    {
        endReached = false;
        Timber.uprootAll();
        Timber.plant(new JUnitTree());
        MockitoAnnotations.initMocks(this);
    }

    private LiveContentConfig createConfig() {
        return new LiveContentConfig(
                createEmptyStreamConfig(),
                createEmptySearchConfig(),
                null,
                null,
                null,
                null,
                null
        );
    }

    private StreamConfig createEmptyStreamConfig() {
        String query = "{\n" +
                "      \"contentProvider\": \"hallpressenStream\",\n" +
                "      \"baseQuery\": {\n" +
                "        \"must\": [\n" +
                "          {\n" +
                "            \"term\": {\n" +
                "              \"Channels\": \"minapp\"\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"term\": {\n" +
                "              \"Status\": \"publicerad\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"should\": [\n" +
                "          {\n" +
                "            \"term\": {\n" +
                "              \"group\": \"jonkopingsposten\"\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"term\": {\n" +
                "              \"group\": \"foretagsam\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"minimum_should_match\": 1\n" +
                "      }\n" +
                "    }";
        return new StreamConfig(
                "hallpressenStream",
                (JsonObject) new JsonParser().parse(query)
        );
    }

    @Test
    public void testListenEmptyStream() throws InterruptedException {

        LiveContentConfig config = createConfig();

        QueryManager emptyQueryManager = Mockito.mock(QueryManager.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                QueryResponseListener listener = (QueryResponseListener) invocation.getArguments()[1];
                listener.onResponse((Query) invocation.getArguments()[0], new JSONObject());
                return null;
            }
        }).when(emptyQueryManager).addQuery(Mockito.any(Query.class), Mockito.any(QueryResponseListener.class));
        Mockito.when(parser.fromSearch(Mockito.any(JSONObject.class), Mockito.any())).thenReturn(Collections.emptyList());

        HitsListStream hitsListStream = new HitsListStream(emptyQueryManager, runnableHandler, new DefaultPropertyObjectParser(new HashMap<>(), new HashMap<>(), new TransformSettingsConfig()), config, properties, null, "");
        final CountDownLatch latch = new CountDownLatch(1);
        hitsListStream.addListener(new StreamListener<PropertyObject>() {
            @Override
            public void onItemsAdded(int index, List<PropertyObject> items) {
                Assert.fail();
            }

            @Override
            public void onItemsRemoved(List<PropertyObject> items) {

            }

            @Override
            public void onItemsChanged(List<PropertyObject> items) {

            }

            @Override
            public void onEndReached() {
                endReached = true;
                latch.countDown();
            }

            @Override
            public void onReset() {
                Assert.fail();
            }

            @Override
            public void onError(Exception exception) {

            }
        });
        latch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue(endReached);
        Assert.assertEquals(0, hitsListStream.size());
    }

    @NonNull
    private SearchConfig createEmptySearchConfig() {
        SearchConfig searchConfig = new SearchConfig("", "", "");
        searchConfig.setSortIndex("");
        searchConfig.setContentProvider("");
        return searchConfig;
    }

    @NonNull
    private JSONObject emptySearchResponse() throws JSONException {
        return new JSONObject("{\"payload\":{\"data\":{\"result\":\"{\\\"hits\\\":{\\\"totalHits\\\":0,\\\"hits\\\":[],\\\"includedHits\\\":0},\\\"facet\\\":{\\\"fields\\\":[]},\\\"stats\\\":{\\\"duration\\\":8}}\"},\"action\":\"searchResult\"},\"customId\":\"c6e3f40a-a04e-480e-b7be-527b74b85cf9\"}");
    }

    @Test
    public void testExhaustStream() throws InterruptedException {

        LiveContentConfig config = createConfig();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                QueryResponseListener listener = (QueryResponseListener) invocation.getArguments()[1];
                listener.onResponse((Query) invocation.getArguments()[0], new JSONObject());
                return null;
            }
        }).when(queryManager).addQuery(Mockito.any(Query.class), Mockito.any(QueryResponseListener.class));


        final CountDownLatch latch = new CountDownLatch(1);
        PropertyObjectParser propertyObjectParser = new SizedParser(100, 5);
        final HitsListStream stream = new HitsListStream(queryManager, runnableHandler, propertyObjectParser, config, properties, null, "Article");
        Assert.assertFalse(stream.isActive());
        final AtomicInteger added = new AtomicInteger();
        stream.addListener(new StreamListener<PropertyObject>() {

            @Override
            public void onItemsAdded(int index, List<PropertyObject> items) {
                added.addAndGet(items.size());
                stream.searchMore();
            }

            @Override
            public void onItemsRemoved(List<PropertyObject> items) {

            }

            @Override
            public void onItemsChanged(List<PropertyObject> items) {

            }

            @Override
            public void onEndReached() {
                endReached = true;
                Assert.assertEquals(100, added.get());
                latch.countDown();
            }

            @Override
            public void onReset() {
                Assert.fail();
            }

            @Override
            public void onError(Exception exception) {

            }
        });
        latch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue(endReached);
        Assert.assertEquals(added.get(), 100);
        Assert.assertNotNull(stream.get(0));
    }

    @Test
    public void testResetStream() throws InterruptedException {

        LiveContentConfig config = createConfig();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                QueryResponseListener listener = (QueryResponseListener) invocation.getArguments()[1];
                listener.onResponse((Query) invocation.getArguments()[0], new JSONObject());
                return null;
            }
        }).when(queryManager).addQuery(Mockito.any(Query.class), Mockito.any(QueryResponseListener.class));


        final CountDownLatch latch = new CountDownLatch(1);
        parser = new SizedParser(100, 5);
        final HitsListStream stream = new HitsListStream(queryManager, runnableHandler, parser, config, properties, null,"Article");
        Assert.assertFalse(stream.isActive());

        final AtomicInteger added = new AtomicInteger();
        stream.addListener(new StreamListener<PropertyObject>() {

            public boolean searchMore = true;

            @Override
            public void onItemsAdded(int index, List<PropertyObject> items) {
                added.addAndGet(items.size());
                if (searchMore) {
                    stream.searchMore();
                }
            }

            @Override
            public void onItemsRemoved(List<PropertyObject> items) {

            }

            @Override
            public void onItemsChanged(List<PropertyObject> items) {

            }

            @Override
            public void onEndReached() {
                searchMore = false;
                endReached = true;
                Assert.assertEquals(100, added.get());
                latch.countDown();
            }

            @Override
            public void onReset() {
                reset = true;
            }

            @Override
            public void onError(Exception exception) {

            }
        });
        latch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue(endReached);
        Assert.assertEquals(100, stream.size());
        Assert.assertEquals(100, added.get());
        added.set(0);
        Assert.assertFalse(reset);
        final CountDownLatch resetLoadLatch = new CountDownLatch(1);
        parser.reset();
        stream.addListener(new StreamListener<PropertyObject>() {
            @Override
            public void onItemsAdded(int index, List<PropertyObject> items) {
                resetLoadLatch.countDown();
                Timber.d("Added " + items.size());
            }

            @Override
            public void onItemsRemoved(List<PropertyObject> items) {

            }

            @Override
            public void onItemsChanged(List<PropertyObject> items) {

            }

            @Override
            public void onEndReached() {
                Assert.fail();
            }

            @Override
            public void onReset() {

            }

            @Override
            public void onError(Exception exception) {

            }
        });

        stream.reset();

        resetLoadLatch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue(reset);
        Assert.assertEquals(5, stream.size());
        Assert.assertEquals(5, added.get());
    }

    @Test
    public void testStream() throws InterruptedException {

        boolean first[] = new boolean[1];
        first[0] = true;
        PropertyObjectParser parser = new PropertyObjectParser() {
            @NotNull
            @Override
            public Set<String> getAllIds(@NotNull List<? extends PropertyObject> from, @NotNull String type) {
                return null;
            }

            @NotNull
            @Override
            public StreamEventWrapper fromStreamNotification(@org.jetbrains.annotations.Nullable JSONObject event, String type) {
                if (CreateStreamQuery.class.getSimpleName().equals(event.optString("type"))) {
                    ArrayList<PropertyObject> lists = new ArrayList<>();
                    lists.add(new DummyPropertyObject(date));
                    date = new Date(date.getTime() + 1000);
                    return new StreamEventWrapper(EventType.ADD, lists);
                }
                else {
                    return new StreamEventWrapper(EventType.ADD, new ArrayList<>());
                }
            }

            @NotNull
            @Override
            public List<PropertyObject> fromSearch(@NotNull JSONObject response, String type) {
                if (first[0]) {
                    first[0] = false;
                    return new ArrayList<>();
                }
                ArrayList<PropertyObject> list = new ArrayList<>();
                list.add(new DummyPropertyObject(new Date()));
                return list;
            }

            private Date date = new Date();

        };

        LiveContentConfig config = createConfig();

        HitsListStream stream = new HitsListStream(queryManager, runnableHandler, parser, config, properties, null, "Article");

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                QueryResponseListener listener = (QueryResponseListener) invocation.getArguments()[1];
                Query query = (Query) invocation.getArguments()[0];
                JSONObject object = new JSONObject();
                object.put("type", query.getClass().getSimpleName());
                if (query instanceof CreateStreamQuery)
                {
                    JSONObject payload = new JSONObject();
                    payload.put("action", "streamNotify");
                    object.put("payload", payload);
                    for (int i = 0; i < 5; i++) {
                        listener.onResponse(query, object);
                    }
                }
                else
                {
                    listener.onResponse(query, object);
                }
                return null;
            }
        }).when(queryManager).addQuery(Mockito.any(Query.class), Mockito.any(QueryResponseListener.class));

        // 5 stream events and one end reached == 6
        final CountDownLatch countDownLatch = new CountDownLatch(6);
        final AtomicInteger added = new AtomicInteger();
        stream.addListener(new StreamListener<PropertyObject>() {
            @Override
            public void onItemsAdded(int index, List<PropertyObject> items) {
                Assert.assertEquals(1, items.size());
                added.addAndGet(items.size());
                countDownLatch.countDown();
            }

            @Override
            public void onItemsRemoved(List<PropertyObject> items) {

            }

            @Override
            public void onItemsChanged(List<PropertyObject> items) {

            }

            @Override
            public void onEndReached() {
                endReached = true;
                countDownLatch.countDown();
            }

            @Override
            public void onReset() {
                Assert.fail();
            }

            @Override
            public void onError(Exception exception) {

            }
        });

        countDownLatch.await(100, TimeUnit.MILLISECONDS);
        Assert.assertTrue(endReached);
        Assert.assertEquals(5, added.get());
    }

    @Test
    public void testPruneDuplicateArticles() throws InterruptedException {
        ArrayList<PropertyObject> firstList = new ArrayList<>();
        fill(firstList, 10);

        ArrayList<PropertyObject> secondList = new ArrayList<>();
        secondList.add(firstList.get(9));
        secondList.add(firstList.get(8));
        fill(secondList, 10);
        Mockito.when(parser.fromSearch(Mockito.any(JSONObject.class), Mockito.any())).thenReturn(firstList, secondList);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                QueryResponseListener listener = (QueryResponseListener) invocation.getArguments()[1];
                listener.onResponse((Query) invocation.getArguments()[0], new JSONObject());
                return null;
            }
        }).when(queryManager).addQuery(Mockito.any(Query.class), Mockito.any(QueryResponseListener.class));

        LiveContentConfig config = createConfig();

        final HitsListStream stream = new HitsListStream(queryManager, runnableHandler, parser, config, properties, null, "Article");
        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicInteger added = new AtomicInteger();
        stream.addListener(new StreamListener<PropertyObject>() {
            int count = 0;
            @Override
            public void onItemsAdded(int index, List<PropertyObject> items) {
                if (count++ == 0) {
                    stream.searchMore();
                }
                latch.countDown();
                added.addAndGet(items.size());
            }

            @Override
            public void onItemsRemoved(List<PropertyObject> items) {

            }

            @Override
            public void onItemsChanged(List<PropertyObject> items) {

            }

            @Override
            public void onEndReached() {

            }

            @Override
            public void onReset() {

            }

            @Override
            public void onError(Exception exception) {

            }
        });
        latch.await(5, TimeUnit.SECONDS);
        Assert.assertEquals(18, stream.size());
    }

    @Test
    public void testCatchup() throws InterruptedException {
        ArrayList<PropertyObject> firstList = new ArrayList<>();
        fill(firstList, 10);
        ArrayList<PropertyObject> secondList = new ArrayList<>();
        fill(secondList, 100);
        // For a catchup to be successfull the new result must contain an article in the existing resultd
        secondList.add(firstList.get(0));
        Mockito.when(parser.fromSearch(Mockito.any(JSONObject.class), Mockito.any())).thenReturn(firstList, secondList);


        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                QueryResponseListener listener = (QueryResponseListener) invocation.getArguments()[1];
                JSONObject payload = new JSONObject();
                payload.put("action", "streamCreated");
                JSONObject data = new JSONObject();
                data.put("streamId", "theStreamId");
                payload.put("data", data);
                JSONObject wrapper = new JSONObject();
                listener.onResponse((Query) invocation.getArguments()[0], wrapper.put("payload", payload));
                return null;
            }
        }).when(queryManager).addQuery(Mockito.any(Query.class), Mockito.any(QueryResponseListener.class));

        final CountDownLatch latch = new CountDownLatch(1);
        StreamListener<PropertyObject> firstListener = new StreamListener<PropertyObject>() {
            @Override
            public void onItemsAdded(int index, List<PropertyObject> items) {
                latch.countDown();
            }

            @Override
            public void onItemsRemoved(List<PropertyObject> items) {

            }

            @Override
            public void onItemsChanged(List<PropertyObject> items) {

            }

            @Override
            public void onEndReached() {

            }

            @Override
            public void onReset() {

            }

            @Override
            public void onError(Exception exception) {

            }
        };

        LiveContentConfig config = createConfig();

        HitsListStream stream = new HitsListStream(queryManager, runnableHandler, parser, config, properties, null, "Article");
        stream.addListener(firstListener);
        latch.await(2, TimeUnit.SECONDS);
        stream.removeListener(firstListener);
        Thread.sleep(1500);
        Assert.assertTrue(stream.isActive());
        Thread.sleep(1510);
        Assert.assertFalse(stream.isActive());
        final CountDownLatch secondLatch = new CountDownLatch(1);
        stream.addListener(new StreamListener<PropertyObject>() {
            @Override
            public void onItemsAdded(int index, List<PropertyObject> items) {
                secondLatch.countDown();
            }

            @Override
            public void onItemsRemoved(List<PropertyObject> items) {

            }

            @Override
            public void onItemsChanged(List<PropertyObject> items) {

            }

            @Override
            public void onEndReached() {

            }

            @Override
            public void onReset() {

            }

            @Override
            public void onError(Exception exception) {

            }
        });
        secondLatch.await(1, TimeUnit.SECONDS);
        Assert.assertEquals(110, stream.size());
    }

    private void fill(List<PropertyObject> hitsLists, int size) {
       while (hitsLists.size() < size) {
            PropertyObject list = new DummyPropertyObject(UUID.randomUUID().toString());
            hitsLists.add(list);
        }
    }

    private class DummyPropertyObject extends PropertyObject {
        private Date pubDate = new Date();

        public DummyPropertyObject() {
            super(new JSONObject(), "1");
        }

        public DummyPropertyObject(String id) {
            super(new JSONObject(), id);
        }

        public DummyPropertyObject(Date date) {
            super(new JSONObject(), UUID.randomUUID().toString());
            pubDate = date;
        }

        @Override
        public Date getPublicationDate() {
            return pubDate;
        }
    }
}
