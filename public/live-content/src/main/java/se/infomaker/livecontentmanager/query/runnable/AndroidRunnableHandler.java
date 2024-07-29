package se.infomaker.livecontentmanager.query.runnable;

import android.os.Handler;

/**
 * Android specific runnable handler
 * See: https://developer.android.com/reference/android/os/Handler.html
 */
public class AndroidRunnableHandler implements RunnableHandler {
    private final Handler handler;

    public AndroidRunnableHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean postDelayed(Runnable r, long delayMillis) {
        return handler.postDelayed(r, delayMillis);
    }

    @Override
    public void removeCallbacks(Runnable r) {
        handler.removeCallbacks(r);
    }
}
