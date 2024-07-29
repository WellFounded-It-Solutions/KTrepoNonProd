package se.infomaker.livecontentmanager.query.lcc;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import se.infomaker.livecontentmanager.JUnitTree;
import se.infomaker.livecontentmanager.query.TestRunnableHandler;
import se.infomaker.livecontentmanager.query.lcc.infocaster.InfocasterConnection;
import se.infomaker.livecontentmanager.query.lcc.infocaster.PublishEvent;
import se.infomaker.livecontentmanager.query.lcc.infocaster.SessionInitEvent;
import se.infomaker.livecontentmanager.query.lcc.infocaster.Status;
import timber.log.Timber;

public class InfocasterTest {

    private InfocasterConnection connection;

    @Before
    public void setup() throws URISyntaxException {
        Timber.uprootAll();
        Timber.plant(new JUnitTree());
        connection = new InfocasterConnection.Builder().setRunnableHandler(new TestRunnableHandler()).setUrl(TestURLS.INFOCASTER_URL).create();
    }

    @Test
    public void testConnect() throws URISyntaxException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final SessionInitEvent[] socketId = {null};
        connection.open("testConnect");
        connection.getStatus().filter(Status::isConnected).subscribe(status -> {
            socketId[0] = status.getSession();
            latch.countDown();
        });

        latch.await();
        Assert.assertNotNull(socketId[0]);
        connection.close("testConnect");
    }

    @Test
    public void testDisconnect() throws URISyntaxException, InterruptedException {
        CompositeDisposable disposable = new CompositeDisposable();
        try
        {
            final CountDownLatch connectedLatch = new CountDownLatch(1);
            final CountDownLatch disconnectedLatch = new CountDownLatch(1);
            final SessionInitEvent[] socketId = {null};
            connection.open("testDisconnect");
            disposable.add(connection.getStatus().filter(Status::isConnected).subscribe(status -> {
                connectedLatch.countDown();
                socketId[0] = status.getSession();
            }));

            connectedLatch.await();
            Assert.assertNotNull(socketId[0]);
            disposable.add(connection.getStatus().filter(status -> !status.isConnected()).subscribe(status -> {
                disconnectedLatch.countDown();
                socketId[0] = status.getSession();
            }));
            connection.close("testDisconnect");

            disconnectedLatch.await(10, TimeUnit.SECONDS);
            Assert.assertNull(socketId[0]);

        }
        finally {
            disposable.clear();
        }
    }
}
