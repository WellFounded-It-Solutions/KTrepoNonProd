package se.infomaker.livecontentmanager.query.lcc;

import com.google.gson.JsonParser;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import se.infomaker.livecontentmanager.JUnitTree;
import com.navigaglobal.mobile.auth.BasicAuthAuthorizationProvider;
import se.infomaker.livecontentmanager.config.SearchConfig;
import se.infomaker.livecontentmanager.config.StreamConfig;
import se.infomaker.livecontentmanager.query.CreateStreamQuery;
import se.infomaker.livecontentmanager.query.DeleteStreamQuery;
import se.infomaker.livecontentmanager.query.ParameterSearchQuery;
import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import se.infomaker.livecontentmanager.query.SearchQuery;
import se.infomaker.livecontentmanager.query.TestRunnableHandler;
import se.infomaker.livecontentmanager.query.lcc.infocaster.InfocasterConnection;
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentBuilder;
import se.infomaker.livecontentmanager.query.lcc.opencontent.OpenContentService;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.Meta;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerManager;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerService;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerServiceBuilder;
import se.infomaker.livecontentmanager.util.EvalQueryResponseListener;
import timber.log.Timber;

public class LCCQueryManagerTest {

    private LCCQueryManager queryManager;

    Scheduler immediate = new Scheduler() {
        @Override
        public Worker createWorker() {
            return new ExecutorScheduler.ExecutorWorker(Runnable::run, true);
        }
    };
    private StreamConfig streamConfig;

    @Before
    public void setup() throws URISyntaxException {
        Timber.uprootAll();
        Timber.plant(new JUnitTree());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> immediate);
        InfocasterConnection connection = new InfocasterConnection.Builder().setRunnableHandler(new TestRunnableHandler()).setUrl(TestURLS.INFOCASTER_URL).create();
        OpenContentService openContentService = new OpenContentBuilder().setAuthorizationProvider(new BasicAuthAuthorizationProvider("bt", "daic6Nid")).setBaseUrl(TestURLS.OPEN_CONTENT_URL).build();
        QueryStreamerService service = new QueryStreamerServiceBuilder().setBaseUrl(TestURLS.QUERYSTREAMER_URL)
                .setLog(true)
                .setId("gota-test")
                .setReadToken("407d2c1b-e43a-4f9c-9634-b4dbdbaf7c90").build();
        QueryStreamerManager streamerManager = new QueryStreamerManager(connection, service, new Meta("test", "1.0", "createStream"));
        queryManager = new LCCQueryManager(openContentService, streamerManager);

        streamConfig = new StreamConfig("this is ignored in lcc", new JsonParser().parse("{\"must\": [{\"match\": {\"ArticleType\": \"article\"}}]}").getAsJsonObject());
    }

    @Test
    public void testSearchQuery() throws URISyntaxException, InterruptedException {
        SearchConfig searchConfig = new SearchConfig("*:*", "article", null);
        searchConfig.setSortIndex("uuid");
        SearchQuery searchQuery = new SearchQuery(searchConfig, "uuid", 0, 5, null);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject[] result = new JSONObject[1];
        result[0] = null;
        queryManager.addQuery(searchQuery, new QueryResponseListener() {
            @Override
            public void onResponse(Query query, JSONObject response) {
                result[0] = response;
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                Timber.e(exception);
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        Assert.assertNotNull(result[0]);
    }

    @Test
    public void testParameterSearchQuery() {
        SearchConfig searchConfig = new SearchConfig("*:*", "", null);
        searchConfig.setSortIndex("uuid");
        ArrayList<String> propertyList = new ArrayList<>();
        propertyList.add("uuid");
        propertyList.add("contenttype");
        propertyList.add("ListRelation.ArticleRelation.ArticleHeadline");
        HashMap<String, String> params = new HashMap<>();
        params.put("q", searchConfig.getBaseQuery());
        params.put("contenttype", "Package");
        ParameterSearchQuery query = new ParameterSearchQuery(searchConfig.getContentProvider(), "uuid,contenttype,ListRelation[ArticleRelation[ArticleHeadline]]", params, null);
        EvalQueryResponseListener responseEvaluator = new EvalQueryResponseListener() {
            @Override
            public void evaluate(JSONObject response, Throwable exception) {
                Assert.assertNotNull(response);
                Assert.assertNull(exception);
            }
        };
        queryManager.addQuery(query, responseEvaluator);
        responseEvaluator.waitForResult();
    }

    @Test
    public void testCreateStream() throws InterruptedException {
        CreateStreamQuery createStreamQuery = new CreateStreamQuery(streamConfig, null);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject[] result = new JSONObject[1];
        result[0] = null;
        queryManager.addQuery(createStreamQuery, new QueryResponseListener() {
            @Override
            public void onResponse(Query query, JSONObject response) {
                result[0] = response;
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                Timber.e(exception);
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        Assert.assertNotNull(result[0]);
    }

    @Test
    public void testDeleteStream() throws InterruptedException, JSONException {
        CreateStreamQuery createStreamQuery = new CreateStreamQuery(streamConfig, null);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject[] result = new JSONObject[1];
        result[0] = null;
        queryManager.addQuery(createStreamQuery, new QueryResponseListener() {
            @Override
            public void onResponse(Query query, JSONObject response) {
                result[0] = response;
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                Timber.e(exception);
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        Assert.assertNotNull(result[0]);
        String streamId = result[0].getJSONObject("payload").getJSONObject("data").getString("streamId");

        CountDownLatch deleteLatch = new CountDownLatch(1);
        result[0] = null;
        DeleteStreamQuery deleteStreamQuery = new DeleteStreamQuery(null, streamId);
        queryManager.addQuery(deleteStreamQuery, new QueryResponseListener() {
            @Override
            public void onResponse(Query query, JSONObject response) {
                result[0] = response;
                deleteLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                Timber.e(exception);
                deleteLatch.countDown();
            }
        });
        deleteLatch.await();
        Assert.assertNotNull(result[0]);
    }
}
