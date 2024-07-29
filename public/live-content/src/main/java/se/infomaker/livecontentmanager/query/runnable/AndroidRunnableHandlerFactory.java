package se.infomaker.livecontentmanager.query.runnable;

import android.os.Handler;
import android.os.Looper;

import javax.inject.Inject;

public class AndroidRunnableHandlerFactory implements RunnableHandlerFactory {

    @Inject
    public AndroidRunnableHandlerFactory() {}

    @Override
    public RunnableHandler create() {
        return new AndroidRunnableHandler(new Handler(Looper.getMainLooper()));
    }
}
