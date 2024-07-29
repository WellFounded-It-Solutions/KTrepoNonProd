package se.infomaker.livecontentmanager;

import timber.log.Timber;

public class JUnitTree extends Timber.DebugTree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        StringBuilder builder = new StringBuilder();
        builder.append(tag).append(" - ").append(message);
        if (t != null)
        {
            builder.append(t);
        }
        System.out.println(builder.toString());
        if (t != null) {
            t.printStackTrace();
        }
    }
}
