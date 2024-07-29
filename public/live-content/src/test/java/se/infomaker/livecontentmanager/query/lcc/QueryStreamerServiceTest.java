package se.infomaker.livecontentmanager.query.lcc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import se.infomaker.livecontentmanager.JUnitTree;
import se.infomaker.livecontentmanager.config.StreamConfig;
import se.infomaker.livecontentmanager.query.CreateStreamQuery;
import se.infomaker.livecontentmanager.query.TestRunnableHandler;
import se.infomaker.livecontentmanager.query.lcc.infocaster.InfocasterConnection;
import se.infomaker.livecontentmanager.query.lcc.infocaster.Status;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.CreateStream;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.Meta;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerServiceBuilder;
import se.infomaker.livecontentmanager.query.lcc.querystreamer.QueryStreamerService;
import timber.log.Timber;

public class QueryStreamerServiceTest {

    private QueryStreamerService service;
    private StreamConfig config;
    private InfocasterConnection connection;

    @Before
    public void setup() throws URISyntaxException {
        Timber.uprootAll();
        Timber.plant(new JUnitTree());

        service = new QueryStreamerServiceBuilder().setBaseUrl(TestURLS.QUERYSTREAMER_URL)
                .setLog(true)
                .setId("gota-test")
                .setReadToken("407d2c1b-e43a-4f9c-9634-b4dbdbaf7c90").build();

        config = new StreamConfig("blah" ,new JsonParser().parse("{\"must\": [{\"match\": {\"ArticleType\": \"article\"}}]}").getAsJsonObject());

        connection = new InfocasterConnection.Builder().setRunnableHandler(new TestRunnableHandler()).setUrl(TestURLS.INFOCASTER_URL).create();
    }

    @Test
    public void createStream() throws URISyntaxException {
        CreateStreamQuery query = new CreateStreamQuery(config, null);
        Meta meta = new Meta("test", "1.0", "createStream");

        connection.open("createStream");
        JsonObject result = connection.getStatus()
                .filter(Status::isConnected)
                .distinctUntilChanged()
                .map(Status::getDestination)
                .map(destination -> new CreateStream.Builder().setDestination(destination).setMeta(meta).setQuery(query.query()).create())
                .firstOrError()
                .flatMap(service::createStream)
                .blockingGet();
        Assert.assertNotNull(result.get("streamId").getAsString());
        connection.close("createStream");
    }

    @Test
    public void deleteStream() {
        CreateStreamQuery query = new CreateStreamQuery(config, null);
        Meta meta = new Meta("test", "1.0", "createStream");

        connection.open("deleteStream");
        JsonObject created = connection.getStatus().filter(Status::isConnected).map(Status::getDestination)
                .map(destination -> new CreateStream.Builder().setDestination(destination)
                        .setMeta(meta).setQuery(query.query()).create())
                .firstOrError()
                .flatMap(createStream -> service.createStream(createStream))
                .blockingGet();
        String createdStreamId = created.get("streamId").getAsString();
        Assert.assertNotNull(createdStreamId);
        JsonObject deleted = service.deleteStream(createdStreamId).blockingGet();
        String deletedStreamId = deleted.get("streamId").getAsString();
        Assert.assertEquals(createdStreamId, deletedStreamId);
        connection.close("deleteStream");
    }
}
