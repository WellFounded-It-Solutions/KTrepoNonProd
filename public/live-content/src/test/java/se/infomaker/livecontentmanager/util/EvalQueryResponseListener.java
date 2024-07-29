package se.infomaker.livecontentmanager.util;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

import se.infomaker.livecontentmanager.query.Query;
import se.infomaker.livecontentmanager.query.QueryResponseListener;
import timber.log.Timber;

public abstract class EvalQueryResponseListener implements QueryResponseListener {

    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private JSONObject result = null;
    private Throwable throwable = null;

    @Override
    public void onResponse(Query query, JSONObject response) {
        result = response;
        countDownLatch.countDown();
    }

    @Override
    public void onError(Throwable exception) {
        Timber.e(exception);
        throwable = exception;
        countDownLatch.countDown();
    }

    public abstract void evaluate(JSONObject response, Throwable exception);

    public void waitForResult() {
        try {
            countDownLatch.await();
            evaluate(result, throwable);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
