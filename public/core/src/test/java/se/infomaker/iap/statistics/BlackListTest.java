package se.infomaker.iap.statistics;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import se.infomaker.frt.statistics.blacklist.BlackList;
import se.infomaker.frt.statistics.blacklist.BlackListBackend;
import se.infomaker.frt.statistics.blacklist.BlackListManager;
import se.infomaker.frt.statistics.blacklist.Store;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class BlackListTest {

    public static final String PACKAGE_NAME = "se.infomaker.test";

    private BlackListBackend blackListBackend;

    @Before
    public void setUp() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new HttpLoggingInterceptor(System.out::println))
                .build();
        blackListBackend = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://s3-eu-west-1.amazonaws.com/app-statistics-disabler/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BlackListBackend.class);
    }

    @Test
    public void backendRequest() throws IOException {
        Response<BlackList> response = blackListBackend.blacklist(PACKAGE_NAME, 1).execute();
        Assert.assertNotNull(response.body().getDisable());
    }

    @Test
    public void verifyForceUpdate() throws Exception {
        MemoryStore<BlackList> memoryStore = new MemoryStore<>();
        Assert.assertNull(memoryStore.get());
        memoryStore.set(new BlackList(new ArrayList<>()));
        BlackListManager blackList = new BlackListManager(blackListBackend
                  , memoryStore, PACKAGE_NAME, 1);
        CountDownLatch latch = new CountDownLatch(1);
        blackList.forceUpdate(() -> {
            latch.countDown();
            return null;
        });
        latch.await();
        Assert.assertFalse(memoryStore.get().getDisable().isEmpty());
    }

    @Test
    public void verifyDoNotUpdate() throws InterruptedException {
        MemoryStore<BlackList> store = new MemoryStore<>();
        store.set(new BlackList(new ArrayList<>()));
        BlackListManager blackList = new BlackListManager(blackListBackend
                , store, PACKAGE_NAME, 1);
        CountDownLatch latch = new CountDownLatch(1);
        blackList.conditionalUpdate(result -> {
            Assert.assertFalse(result);
            latch.countDown();
            return null;
        });
        latch.await();
    }

    @Test
    public void verifyInitialUpdate() throws InterruptedException {
        MemoryStore<BlackList> memoryStore = new MemoryStore<>();
        Assert.assertNull(memoryStore.get());
        BlackListManager blackList = new BlackListManager(blackListBackend
                , memoryStore, PACKAGE_NAME, 1);
        boolean updated = false;
        long before = System.currentTimeMillis();
        while (!updated && System.currentTimeMillis() - before < 2000) {
            Thread.sleep(50);
            if (memoryStore.get() != null) {
                updated = true;
            }
        }
        Assert.assertFalse(memoryStore.get().getDisable().isEmpty());
    }

    @Test
    public void verifyDisabledFeature() {
        MemoryStore<BlackList> memoryStore = new MemoryStore<>();
        ArrayList<String> list = new ArrayList<>();
        list.add("feature");
        memoryStore.set(new BlackList(list));
        BlackListManager blackList = new BlackListManager(blackListBackend, memoryStore, PACKAGE_NAME, 1);
        Assert.assertFalse(blackList.isEnabled("feature"));
    }

    @Test
    public void verifyEnabledFeature() {
        MemoryStore<BlackList> memoryStore = new MemoryStore<>();
        ArrayList<String> list = new ArrayList<>();
        list.add("feature1");
        memoryStore.set(new BlackList(list));
        BlackListManager blackList = new BlackListManager(blackListBackend, memoryStore, PACKAGE_NAME, 1);
        Assert.assertTrue(blackList.isEnabled("feature2"));
    }

    private static class MemoryStore<T> implements Store<T> {
        private T value;
        private Date lastSet;

        @Nullable
        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(@Nullable T value) {
            this.value = value;
            lastSet = new Date();
        }

        @Nullable
        @Override
        public Date lastSet() {
            return lastSet;
        }
    }
}