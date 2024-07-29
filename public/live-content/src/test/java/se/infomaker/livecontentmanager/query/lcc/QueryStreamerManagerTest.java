package se.infomaker.livecontentmanager.query.lcc;

import com.google.gson.JsonParser;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import se.infomaker.livecontentmanager.JUnitTree;
import se.infomaker.livecontentmanager.config.StreamConfig;
import se.infomaker.livecontentmanager.query.CreateStreamQuery;
import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import se.infomaker.livecontentmanager.query.TestRunnableHandler;
import se.infomaker.livecontentmanager.query.lcc.infocaster.InfocasterConnection;
import se.infomaker.livecontentmanager.query.lcc.infocaster.Status;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.Meta;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerServiceBuilder;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerManager;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerService;
import timber.log.Timber;

public class QueryStreamerManagerTest {

    private QueryStreamerManager streamerManager;
    private StreamConfig streamConfig;

    Scheduler immediate = new Scheduler() {
        @Override
        public Worker createWorker() {
            return new ExecutorScheduler.ExecutorWorker(Runnable::run, true);
        }
    };
    private InfocasterConnection connection;

    @Before
    public void setup() throws URISyntaxException {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> immediate);

        Timber.uprootAll();
        Timber.plant(new JUnitTree());

        connection = new InfocasterConnection.Builder().setRunnableHandler(new TestRunnableHandler()).setUrl(TestURLS.INFOCASTER_URL).create();
        QueryStreamerService service = new QueryStreamerServiceBuilder().setBaseUrl(TestURLS.QUERYSTREAMER_URL)
                .setLog(true)
                .setId("gota-test")
                .setReadToken("407d2c1b-e43a-4f9c-9634-b4dbdbaf7c90").build();
        streamerManager = new QueryStreamerManager(connection, service, new Meta("test", "1.0", "createStream"));

        streamConfig = new StreamConfig("this is ignored in lcc", new JsonParser().parse("{\"must\": [{\"match\": {\"ArticleType\": \"article\"}}]}").getAsJsonObject());
    }

    @Test
    public void testSwitchInfocaster() throws InterruptedException, JSONException {
        Assert.assertEquals(0, streamerManager.getActiveStreams().size());

        CreateStreamQuery createStreamQuery = new CreateStreamQuery(streamConfig, null);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject[] result = new JSONObject[1];
        result[0] = null;
        connection.open("testSwitchInfocaster");
        streamerManager.create(createStreamQuery, new QueryResponseListener() {
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

        // Check that the stream is up
        Assert.assertEquals(1, streamerManager.getActiveStreams().size());
        connection.close(streamerManager.getListenerId());
        connection.close("testSwitchInfocaster");
        waitForStreamSize(0);
        // Check that the stream is down;
        Assert.assertEquals(0, streamerManager.getActiveStreams().size());
        connection.open("testSwitchInfocaster");
        CountDownLatch upAgainLatch = new CountDownLatch(1);
        connection.getStatus().filter(Status::isConnected).distinctUntilChanged().subscribe(status -> upAgainLatch.countDown());
        upAgainLatch.await();

        waitForStreamSize(1);
        // Check that the stream is resumed
        Assert.assertEquals(1, streamerManager.getActiveStreams().size());
    }

    @Test
    public void testRemoveStream() throws JSONException, InterruptedException {
        Assert.assertEquals(0, streamerManager.getActiveStreams().size());

        CreateStreamQuery createStreamQuery = new CreateStreamQuery(streamConfig, null);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject[] result = new JSONObject[1];
        result[0] = null;

        streamerManager.create(createStreamQuery, new QueryResponseListener() {
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

        // Check that the stream is up
        Assert.assertEquals(1, streamerManager.getActiveStreams().size());

        Assert.assertTrue(streamerManager.remove(createStreamQuery));
        Thread.sleep(1000);
        Assert.assertEquals(0, streamerManager.getActiveStreams().size());

    }

    private void waitForStreamSize(int streamSize) throws InterruptedException {
        int aWhile = 6000;
        while (aWhile > 0) {

            aWhile -= 100;
            if (streamerManager.getActiveStreams().size() == streamSize) {
                break;
            }
            Thread.sleep(100);
        }
    }

}
