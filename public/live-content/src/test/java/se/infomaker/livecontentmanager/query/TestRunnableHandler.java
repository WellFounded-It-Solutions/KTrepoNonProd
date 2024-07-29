package se.infomaker.livecontentmanager.query;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import se.infomaker.livecontentmanager.query.runnable.RunnableHandler;

public class TestRunnableHandler implements RunnableHandler {
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private final HashMap<Runnable, ScheduledFuture<?>> tasks = new HashMap<>();

    @Override
    public boolean postDelayed(Runnable r, long delayMillis) {
        ScheduledFuture<?> future = worker.schedule(r, delayMillis, TimeUnit.MILLISECONDS);
        tasks.put(r, future);
        return true;
    }

    @Override
    public void removeCallbacks(Runnable r) {
        ScheduledFuture<?> future = tasks.get(r);
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(true);
        }
    }
}
